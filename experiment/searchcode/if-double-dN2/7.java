/** BEGIN COPYRIGHT BLOCK
 * Copyright (C) 2001 Sun Microsystems, Inc. Used by permission.
 * Copyright (C) 2005 Red Hat, Inc.
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * END COPYRIGHT BLOCK **/

package com.netscape.admin.dirserv;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.UnsupportedEncodingException;

import netscape.ldap.*;
import netscape.ldap.util.*;

import com.netscape.management.client.*;
import com.netscape.management.client.console.ConsoleInfo;
import com.netscape.management.client.console.Console;
import com.netscape.management.client.util.*;
import com.netscape.management.client.ug.*;
import com.netscape.management.nmclf.SuiConstants;
import com.netscape.management.client.ace.ACIManager;
import com.netscape.management.client.preferences.*;

import com.netscape.admin.dirserv.account.AccountInactivation;
import com.netscape.admin.dirserv.browser.*;
import com.netscape.admin.dirserv.task.CreateVLVIndex;
import com.netscape.admin.dirserv.panel.*;
import com.netscape.admin.dirserv.propedit.*;
import com.netscape.admin.dirserv.roledit.RoleEditorDialog;

public class DSContentPage extends JPanel 
implements IPage, 
    SuiConstants,    
    IAuthenticationChangeListener, 
    BrowserEventListener, 
    TreeSelectionListener, 
    TreeWillExpandListener,
    ListSelectionListener,
    ActionListener, 
    MouseListener,
    FocusListener,
    IContentPageInfo {


    public DSContentPage() {
    }

	public DSContentPage(DSResourceModel resourceModel) {
		_resourceModel = resourceModel;
	}



    /**
     * IPage implementation
     * ====================
     */

    /**
     * When the view is closing, we shutdown the BrowserController 
     * (all threads are stopped).
     * Warning: this method is called systematically even if initialize()
     * has not been called.
     */
    public void actionViewClosing(IFramework parent)
    throws CloseVetoException {
        Debug.println("DSContentPage.actionViewClosing");
        if (_controller != null) { // ie initialize() has been called.
            _controller.shutDown();
        }
        if (_attributeController != null) {
            _attributeController.shutDown();
        }
        if (_childrenController != null) {
            _childrenController.shutDown();
        }

    }
    
    
    /**
     * This method is deprecated and no longer called by the console sdk.
     */
    public Object clone() {
        throw new IllegalStateException("DSContentPage.clone() should not be called");
    }
    
    
    public IFramework getFramework() {
        return _framework;
    }
    
    public String getPageTitle() {
        return DSUtil._resource.getString("browser", "tab-title");
    }
    
    /**
     * We build the UI components here.
     */
    public void initialize(IFramework parent) {
        Debug.println("DSContentPage.initialize");
        
        _framework = (DSFramework)parent;

        // Get the preferences                
        _showRemoteInformationDialog = _preferences.getBoolean(SHOW_REMOTE_INFORMATION_DIALOG, true);        
        _showDisplayedChildrenLimitExceededDialog = _preferences.getBoolean(SHOW_DISPLAYED_CHILDREN_LIMIT_EXCEEDED, true);                
        _showSortedChildrenLimitExceededDialog = _preferences.getBoolean(SHOW_SORTED_CHILDREN_LIMIT_EXCEEDED, true);
        final boolean isSorted = _preferences.getBoolean(SORT_PREFERENCES, false);
        final boolean followReferrals = _preferences.getBoolean(FOLLOW_REFERRALS_PREFERENCES, true);
        _display = _preferences.getInt(DISPLAY_PREFERENCES, BrowserController.DISPLAY_ACI_COUNT);
        _layout = _preferences.getString(LAYOUT_PREFERENCES, ContentMenuController.NODE_LEAF_LAYOUT);

        // Build the components
        _tree = new JTree();
        _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.addTreeSelectionListener(this);
        _tree.addTreeWillExpandListener(this);
        _tree.addMouseListener(this);
        _tree.addFocusListener(this);

        _list = new JList() {
            // 609907 Disable this new JDK1.4 method as typing a key in the list
            // will cause a linear search for an entry which starts with the
            // typed key. If the list is a long one, every entry in the list
            // will be visited which might freeze the console for a long time. 
            public int getNextMatch(String prefix, int startIndex,
                                    javax.swing.text.Position.Bias bias) {
                return -1;
            }
         };

        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.addListSelectionListener(this);
        _list.addMouseListener(this);
        _list.addFocusListener(this);
        _listDefaultColor = _list.getBackground();
        _listDisabledColor = _framework.getBackground();

        _selectedDnLabel = new JLabel(NO_SELECTED_DN);
        _selectedDnLabel.setLabelFor(_tree);
        _statusLabel = new JLabel("");        
        _statusLabel.setLabelFor(_tree);
                

        // Retreive the LDAP connection
        ConsoleInfo serverInfo = _framework.getServerObject().getServerInfo();
        final LDAPConnection ldc = serverInfo.getLDAPConnection();        
        
        // Create the connection pool
        _connectionPool = new LDAPConnectionPool();        

        // Create the icon pool
        _iconPool = new IconPool();

        layoutComponents();

        // Set the browser controller
        _controller = new BrowserController(_tree, _connectionPool, _iconPool);
        _controller.addBrowserEventListener(this);
        _controller.setLDAPConnection(ldc);
        _controller.setShowContainerOnly(false);
        _controller.setContainerClasses(CONTAINER_OBJECTCLASSES);                                        
        _controller.setMaxChildren(DISPLAYED_CHILDREN_LIMIT);

        /* Create the children controller */
        _childrenController = new ChildrenController(_list,
                                                     _connectionPool,
                                                     _iconPool);
        _childrenController.setLDAPConnection(ldc);
        _childrenController.addBrowserEventListener(this);
        _childrenController.setContainerClasses(CONTAINER_OBJECTCLASSES);    
        _childrenController.setMaxChildren(DISPLAYED_CHILDREN_LIMIT);
        
        //Create the attribute controller
        _attributeController = new AttributeController(_connectionPool,
                                                       _attributePanel);


        /* Create the database config object */
        _databaseConfig = new DatabaseConfig();
        
        // NUMSUBORDINATE HACK
        // We attach the hacker to the (tree) browser controller.
        _controller.setNumSubordinateHacker(_databaseConfig.getNumSubordinateHacker());

        /* Create the menu controller */
        _menuController = new ContentMenuController(DSContentPage.this,
                                                _contextMenu,
                                                _framework,                                                
                                                _databaseConfig,                                                        
                                                DSContentPage.this);
        _menuController.addShortCutRegisterer(_tree);
        _menuController.addShortCutRegisterer(_list);
        // At last, we start a SwingWorker which is going to
        // determine what are the naming contexts and call
        // _controller.addSuffix().
        // TODO: temporary implementation
        SwingWorker worker = new SwingWorker() {
            LDAPConnection _ldc = ldc;
            String[] _suffixes;
            
            public Object construct() {                
                try {
                    _databaseConfig.reload(ldc);
                } catch (LDAPException lde) {
                    final LDAPException e = lde;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String[] args = {Helper.getLDAPErrorMessage(e)};
                            DSUtil.showErrorDialog( _framework,
                                                    "reading-databaseconfig-error-title",
                                                    "reading-databaseconfig-error-msg",
                                                    args,
                                                    "browser" );
                        }});
                }                
                
                /* Create the entry editor */
                _entryEditor = new EntryEditor(getSchema(), _framework, _databaseConfig);                
                Vector vSuffixes = _databaseConfig.getRootSuffixesWithEntry();                
                if (vSuffixes.size() > 0) {
                    _suffixes = new String[vSuffixes.size()];
                    vSuffixes.copyInto(_suffixes);
                }
                return null;
            }

            public void finished() {
                _framework.setBusyCursor(false);
                if (_suffixes != null) {
                    // We populate the browser controller
                    for (int i = 0; i < _suffixes.length; i++) {
                        _controller.addSuffix(_suffixes[i], null);
                    }
                }
                else {
                    // Suffix has no entry
                    Debug.println(0, "DSContentPage.initialize: no suffix found");
                }
                // Even if suffix has no entry, we should show system entries
                _controller.addSuffix("cn=schema", null);
                _controller.addSuffix("cn=monitor", null);
                _controller.addSuffix("cn=config", null);
                
                _childrenController.setSorted(isSorted);
                _controller.setShowContainerOnly(_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT));
                _controller.setSorted(isSorted);
                

                _controller.setFollowReferrals(followReferrals);
                _childrenController.setFollowReferrals(followReferrals);
                

                /* Initialize the display */                
                _controller.setDisplayFlags(_display);
                _childrenController.setDisplayFlags(_display);                
                                    
                if (_isPageSelected) {
                    _menuController.populateMenuItems();
                }

                _tree.clearSelection();                
                _menuController.disableMenus();
                _isInitialized = true;
            }
        };
        _framework.setBusyCursor(true);
        worker.start();
    }
    
    
    /**
     * Component layout.
     */
    void layoutComponents() {
         GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridwidth  = gbc.REMAINDER;
        gbc.gridheight = 1;
        gbc.weightx    = 1;
        gbc.weighty    = 1;
        gbc.fill       = gbc.BOTH;
        gbc.anchor     = gbc.NORTHWEST;
        gbc.insets     = new Insets(COMPONENT_SPACE, COMPONENT_SPACE, 0, COMPONENT_SPACE);
        gbc.ipadx      = 0;
        gbc.ipady      = 0;
        
        _treePanel = new JScrollPane(_tree);
        _treePanel.setBorder( new BevelBorder(BevelBorder.LOWERED,
                                             UIManager.getColor("controlHighlight"),
                                             UIManager.getColor("control"),
                                             UIManager.getColor("controlDkShadow"),
                                             UIManager.getColor("controlShadow")));
        _treePanel.setPreferredSize(new Dimension(200, 200));
        _treePanel.setMinimumSize(new Dimension(1, 1));

        _childrenPanel =  new SuiScrollPane(_list);
        _childrenPanel.setBorder( new BevelBorder(BevelBorder.LOWERED,
                                             UIManager.getColor("controlHighlight"),
                                             UIManager.getColor("control"),
                                             UIManager.getColor("controlDkShadow"),
                                             UIManager.getColor("controlShadow")));
        _childrenPanel.setPreferredSize(new Dimension(200, 200));
        _childrenPanel.setMinimumSize(new Dimension(1, 1));

        _attributePanel = new JPanel(new GridBagLayout());

        //Add the scroll panes to a split pane.
        _splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        _splitPanel.setLeftComponent(_treePanel);

        if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
            _splitPanel.setRightComponent(_childrenPanel);        
        } else if (_layout.equals(ContentMenuController.ATTRIBUTE_LAYOUT)) {
            _splitPanel.setRightComponent(_attributePanel);
        }


        _splitPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    
        _displayedPanel = new JPanel(new BorderLayout());
        
        add(_displayedPanel, gbc);        
        
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, COMPONENT_SPACE, 0, COMPONENT_SPACE);
        add(_selectedDnLabel, gbc);
        
        gbc.gridx++;
        gbc.weightx = 0;
        add(_statusLabel, gbc);                        

        if (_layout.equals(ContentMenuController.ONLY_TREE_LAYOUT)) {
            _displayedPanel.add(_treePanel);                
        } else {
            _displayedPanel.add(_splitPanel);                        
        }

    }
    
    
    /**
     * Called by Framework when page is selected
     */
    public void pageSelected(IFramework framework) {
        Debug.println( "DSContentPage.pageSelected " );                
        _isPageSelected = true;
        if (_isInitialized) {
            try {                            
                refreshDatabaseConfigAndSuffixes(/*refreshAll=*/false);
                /* Give the focus to the main tree */
                _tree.grabFocus();               
                _menuController.populateMenuItems();
                /* Refresh the menus */
                _menuController.recreateDynamicMenus();            
            } catch (Exception e) {                
            }
        }
        
        if (_refreshUponSelect) {
            actionRefreshTree();
            _refreshUponSelect = false; // We only refresh upon select when required
        }
    }

        
    /**
     * Should we ask to BrowserController to stop the on-going refresh ?
     */
    public void pageUnselected(IFramework parent) {
        Debug.println("DSContentPage.pageUnselected");
        _isPageSelected = false;
        _menuController.unpopulateMenuItems();
        _controller.stopRefresh();
    }

    /**
     * IContentPageInfo implementation
     * ============================================
     */
        /**
         * Implements IContentPageInfo
         */
    public boolean isRootSelected() {
        boolean isRootSelected = false;
        IBrowserNodeInfo node = getSelectedNodeInfo();        
        if (node != null) {
            isRootSelected = node.isRootNode();
        }
        return isRootSelected;
    }

    /**
     * Implements IContentPageInfo
     */    
    public boolean isSelectedNodeRemote() {
        boolean isSelectedNodeRemote = false;        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {            
            isSelectedNodeRemote = node.isRemote();
        }
        return isSelectedNodeRemote;
    }

    public boolean isSelectedNodeSuffix() {
        boolean isSelectedNodeSuffix = false;        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {            
            isSelectedNodeSuffix = node.isSuffix();
        }
        return isSelectedNodeSuffix;
    }
        /**
         * Implements IContentPageInfo
         */
    public boolean isClipboardEmpty() {
        return (_clipboard.isEmpty());
    }

        /**
         * Implements IContentPageInfo
         *
         * Should not be called if the node is remote
         */   
    public Integer getSelectionVlvState() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        return getNodeVlvState(node);
    }
 
    /**
     *
     * Should not be called if the node is remote
     */       
    Integer getNodeVlvState(IBrowserNodeInfo node) {
        Integer state = new Integer(CreateVLVIndex.CAN_NOT_HAVE_INDEX);
        if (node != null) {                        
            String dn = Helper.getNodeInfoDN(node);            
            if (dn != null) {
                state =  (Integer)_vlvCache.get(dn);            
                if (state == null) {                                        
                    try {
                        state = new Integer(CreateVLVIndex.CAN_NOT_HAVE_INDEX);
                        state = new Integer(CreateVLVIndex.indexStatus(dn, 
                                                                       _framework.getServerObject().getServerInfo()));                            
                    } catch (LDAPException lde) {                    
                        /* If something gone wrong we keep the default value CAN_NOT_HAVE_INDEX:
                         this is called by the menu controller and it has no context enough to display error*/                        
                    }                
                    _vlvCache.put(dn, state);
                }
            }
        }
        return state;
    }

   /**
   * This method should return what type of PWP may be added to the selected
   * node.  Options are NO_PWP, USER_PWP, SUBTREE_PWP or BOTH_PWP (defined in
   * PasswordPolicyPanel.
   *
   * Should not be called if the node is remote
   */
    public Integer getSelectionPWPState() {
        Integer state = new Integer(PasswordPolicyPanel.BOTH_PWP);

        return state;
    }

        /**
         * Implements IContentPageInfo
         *
         * Should not be called if the node is remote
         */
    public Integer getSelectionActivationState() {
        Integer state = new Integer(AccountInactivation.CANNOT_BE_ACTIVATED_INACTIVATED);
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null && node.getErrorType() == node.ERROR_NONE) {            
            String dn = Helper.getNodeInfoDN(node);            
            if (dn != null) {
                state =  (Integer)_activationCache.get(dn);                            
                if (state == null) {                                                        
                    try {
                        state = new Integer(AccountInactivation.CANNOT_BE_ACTIVATED_INACTIVATED);
                        String[] attrs = {"nsrole", "nsroledn", "objectclass", "nsAccountLock"};
                        LDAPConnection ldc0 = getConnectionForNode(node);
                        LDAPConnection ldc = prepareReferralConnection(ldc0);
                        LDAPEntry entry = ldc.read(dn, attrs);
                        if (entry != null) {
                            AccountInactivation account = new AccountInactivation(entry);
                            state = new Integer(account.operationAllowed(ldc));                                
                        }
                        try {
                            ldc.disconnect(); // prepareReferralConnection clones connection
                        } catch (Exception ignore) {}
                        _connectionPool.releaseConnection(ldc0);
                    } catch (LDAPException lde) {                                                
                        /* If something gone wrong we keep the default value CANNOT_BE_ACTIVATED_INACTIVATED:
                         this is called by the menu controller and it has no context enough to display error*/
                    }                    
                    _activationCache.put(dn, state);
                }
            }
        }
        return state;
    }

        /**
         * Implements IContentPageInfo
         */
    public String getPanelLayout() {
        return _layout;
    }

        /**
         * Implements IContentPageInfo
         */
    public int getDisplay() {
        return _display;
    }

        /**
         * Implements IContentPageInfo
         */
    public boolean isSorted() {
        return _controller.isSorted();
    }

        /**
         * Implements IContentPageInfo
         */
    public boolean getFollowReferrals() {
        return _controller.getFollowReferrals();
    }

        /**
         * Implements IContentPageInfo
         */
    public String getSelectedPartitionView() {
        return _selectedPartitionView;
    }

    /**
     * IAuthenticationChangeListener implementation
     * ============================================
     */

    /**
     * Called when authentication changes.
     *
     * @param oldAuth Previous authentication DN.
     * @param newAuth New authentication DN.
     */
    public void authenticationChanged(String oldAuth,
                                      String newAuth,
                                      String oldPassword,
                                      String newPassword) {
        Debug.println("DSContentPage.authenticationChanged(): new bind DN = " +
                      newAuth + " old bind DN = " + oldAuth);
        // If this page is selected, refresh the current view.  Otherwise,
        // just set the flag which tells this model to force its view
        // to be refreshed the next time it's selected

        /* The controllers and the framework are initialized when the page is selected: DSContentPage may receive 
           an authentication change event before being selected, and so the Framework, the BrowserController 
           and the ChildrenController could be null */
        if (_framework != null) {
            LDAPConnection serverLdc = _framework.getServerObject().getServerInfo().getLDAPConnection();
            if (_controller != null) {
                _controller.setLDAPConnection(serverLdc);            
            }
            if (_childrenController != null) {
                _childrenController.setLDAPConnection(serverLdc);
            }
        }


        if (_isPageSelected) {
            actionRefreshTree();
        } else if (_isInitialized) {
            _refreshUponSelect = true;
        }       
    }

    /**
     * Get new authentication credentials from a dialog, reauthenticate
     * the connection in _serverInfo.
     *     
     *
     * @return <CODE>true</CODE> if the connection was reauthenticated.
     */
    public boolean getNewAuthentication() {
        boolean status = false;
                    
        DSUtil.DeferAuthListeners deferAuthListeners = null;
        String dn = (String)_framework.getServerObject().getServerInfo().get( "rootdn" );            
        deferAuthListeners  = DSUtil.reauthenticateDefer(_framework.getServerObject().getServerInfo().getLDAPConnection(),
                                                         _framework,
                                                         _authListeners, dn, null);
        status = (deferAuthListeners != null);
        
        /* Notify the listeners */
        if (status) {                
            deferAuthListeners.notifyListeners();            
        }    

        return status;
    }    

    public void setAuthenticationChangeListener(Vector authListeners) {
        _authListeners = authListeners;
    }

    /**
     * BrowserEventListener implementation
     * ===================================
     *
     * BrowserEvent are used to update _statusLabel.
     */
    
    public void processBrowserEvent(BrowserEvent e) {
        switch(e.getID()) {
            case BrowserEvent.UPDATE_START:
                _statusLabel.setText("Updating..."); // TODO: i18n
                break;
            case BrowserEvent.UPDATE_END:
                _statusLabel.setText("");
                break;
        }
    }

    
    
    /**
     * TreeSelectionListener implementation
     * ====================================
     *
     * TreeSelectionEvent are used to update _selectedDnLabel.
     */
    
    public void valueChanged(TreeSelectionEvent e) {                        
        if (_isInitialized) {
            _framework.setBusyCursor(true);
            // Manually set the tree as the last focused item since we know it was just
            // clicked. We need to do this since getSelectedNodeInfo() will return info
            // about the selected node from either the tree or the list depending on which
            // is set as the _lastFocusComponent.  If the list is set here instead of the
            // tree, we will fail to fill in the child entries in the main content pane.
            // We have to do this since the focusEvent is fired off after the
            // TreeSelectionEvent.
            _lastFocusComponent = (Component)_tree;
            IBrowserNodeInfo node = getSelectedNodeInfo();        
            if (node == null) {            
                _menuController.disableMenus();
                _selectedDnLabel.setText(NO_SELECTED_DN);
                if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {                
                    _childrenController.setBaseNodeInfo(null, false);
                    _list.setBackground(_listDefaultColor);
                } else if (_layout.equals(ContentMenuController.ATTRIBUTE_LAYOUT)) {
                    _attributeController.clearAttributePanel();
                }
            } else {                                    
                String dn = Helper.getNodeInfoDN(node);            
                if ((dn == null) ||
                    dn.equals("")) {
                    _selectedDnLabel.setText(NO_SELECTED_DN);                
                } else {                
                    _selectedDnLabel.setText(findDisplayNameFromNode(node));                
                }

                _menuController.updateMenuState();
                
                if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {                                    
                    boolean displayChildren = checkChildrenForNode(node);
                    if (displayChildren) {

                        int vlvState = getNodeVlvState(node).intValue();
                        boolean hasIndex = (vlvState == CreateVLVIndex.HAS_INDEX);

                        _childrenController.setBaseNodeInfo(node, hasIndex);
                        _list.setBackground(_listDefaultColor);
                    } else {
                        _childrenController.setBaseNodeInfo(null, false);
                        _list.setBackground(_listDisabledColor);
                    }
                } else if (_layout.equals(ContentMenuController.ATTRIBUTE_LAYOUT)) {
                    _attributeController.updateAttributePanel(node);
                }                                                        
            }
            _framework.setBusyCursor(false);
        }
    }

    /**
     * TreeWillExpandListener implementation
     * ====================================
     *
     * In the case of many entries, we ask the user if he/she wants to continue with
     * expand operation
     */
    
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        _framework.setBusyCursor(true);
        if (_isInitialized) {
            TreePath path = event.getPath();
            if (path != null) {
                IBrowserNodeInfo node = _controller.getNodeInfoFromPath(path);
                if (node != null) {
                    boolean continueExpand = checkChildrenForNode(node);
                    if (!continueExpand) {
                        /* Cancel the expansion */
                        throw new ExpandVetoException(event);
                    }
                }
            }        
        }
        _framework.setBusyCursor(false);
    }    

    /**
     * ListSelectionListener implementation
     * ====================================
     *
     * ListSelectionEvent are used to update _selectedDnLabel.
     */
    
    public void valueChanged(ListSelectionEvent e) {                        
        _framework.setBusyCursor(true);
        if (_isInitialized) {
            IBrowserNodeInfo node = getSelectedNodeInfo();
            if (node == null) {
                _menuController.disableMenus();
                _selectedDnLabel.setText(NO_SELECTED_DN);            
            } else {

                if (node.getErrorType() == node.ERROR_SEARCHING_CHILDREN) {
                    _menuController.disableMenus();
                    _selectedDnLabel.setText(NO_SELECTED_DN);
                    return;
                }

                String dn = Helper.getNodeInfoDN(node);
                if ((dn == null) ||
                    dn.equals("")) {
                    _selectedDnLabel.setText(NO_SELECTED_DN);                
                } else {                
                    _selectedDnLabel.setText(findDisplayNameFromNode(node));                
                }            
                _menuController.updateMenuState();
            }                        
        }    
        _framework.setBusyCursor(false);
    }

    /**
     * ActionListener implementation
     * ====================================
     *     
     */
    public void actionPerformed(ActionEvent e) {
        setBusyCursor(true);
        String cmd = e.getActionCommand();

        Debug.println("DSContentPage.actionPerformed(): "+cmd);

        if ( cmd.equals( ContentMenuController.OPEN ) ) {
            actionEdit();

        } else if ( cmd.equals( ContentMenuController.ADVANCED_OPEN ) ) {
            actionAdvancedEdit();

        } else if ( cmd.equals( ContentMenuController.AUTHENTICATE ) ) {
            actionAuthenticate();

        } else if( cmd.equals( ContentMenuController.COPY ) ) {
            actionCopy(  );

        } else if( cmd.equals( ContentMenuController.PASTE ) ) {            
            actionPaste( );            

        } else if( cmd.equals( ContentMenuController.CUT ) ) {
            actionCut();

        } else if( cmd.equals( ContentMenuController.DELETE ) ) {
            actionDelete();

        } else if( cmd.equals( ContentMenuController.COPYDN ) ) {
            actionCopyDN( );

        } else if( cmd.equals( ContentMenuController.COPYLDAPURL ) ) {
            actionCopyLDAPURL( );

        } else if( cmd.equals( ContentMenuController.NEW_USER ) ) {
            actionNewUser( );

        } else if( cmd.equals( ContentMenuController.NEW_GROUP ) ) {
            actionNewGroup( );

        } else if( cmd.equals( ContentMenuController.NEW_ORGANIZATIONALUNIT ) ) {
            actionNewOrganizationalUnit( );

        } else if( cmd.equals( ContentMenuController.NEW_ROLE ) ) {
            actionNewRole( );

        } else if( cmd.equals( ContentMenuController.NEW_COS ) ) {
            actionNewCos( );

        } else if( cmd.equals( ContentMenuController.NEW_OBJECT ) ) {
            actionNewObject( );

        } else if( cmd.equals( ContentMenuController.ACL ) ) {
            actionACL( );

        } else if( cmd.equals( ContentMenuController.ROLES ) ) {
            actionRoles( );

        } else if( cmd.equals( ContentMenuController.SET_REFERRALS ) ) {
            actionSetReferral( );

        } else if( cmd.equals( ContentMenuController.SEARCH_UG ) ) {
            actionSearchUG( );

        } else if( cmd.equals( ContentMenuController.CREATE_VLV_INDEX ) ) {
            actionCreateVLVIndex( );

        } else if( cmd.equals( ContentMenuController.DELETE_VLV_INDEX ) ) {
            actionDeleteVLVIndex( );

		} else if( cmd.equals( ContentMenuController.SET_PWP_USER ) ) {
			actionSetPWP(PasswordPolicyPanel.FINEGRAINED_USER );

		} else if( cmd.equals( ContentMenuController.SET_PWP_SUBTREE ) ) {
			actionSetPWP(PasswordPolicyPanel.FINEGRAINED_SUBTREE );

        } else if ( cmd.equals( ContentMenuController.FOLLOW_REFERRALS ) ) {
            actionFollowReferrals();

        } else if ( cmd.equals( ContentMenuController.REFRESHTREE ) ) {
            actionRefreshTree();

        } else if ( cmd.equals( ContentMenuController.REFRESHNODE ) ) {
            actionRefreshNode();
            
        } else if ( cmd.equals( ContentMenuController.ACTIVATE ) ) {
            actionActivate();            

        } else if ( cmd.equals( ContentMenuController.INACTIVATE ) ) {
            actionInactivate();            
            
        }  else if ( cmd.equals( ContentMenuController.VIEW_ALL_PARTITIONS )) {
            actionSelectPartitionView(ContentMenuController.VIEW_ALL_PARTITIONS);

        } else if (cmd.equals( ContentMenuController.DISPLAY_ACCOUNT_INACTIVATION )) {            
            actionDisplayAccountInactivation();

        } else if (cmd.equals( ContentMenuController.DISPLAY_ACI_COUNT )) {            
            actionDisplayACICount();

        } else if (cmd.equals( ContentMenuController.DISPLAY_ROLE_COUNT )) {            
            actionDisplayRoleCount();        

        } else if (cmd.equals( ContentMenuController.NODE_LEAF_LAYOUT )) {
            actionNodeLeafLayout( );        
            
        } else if (cmd.equals( ContentMenuController.ONLY_TREE_LAYOUT )) {
            actionOnlyTreeLayout( );

        } else if (cmd.equals( ContentMenuController.ATTRIBUTE_LAYOUT )) {
            actionAttributeLayout( );

        } else if (cmd.equals( ContentMenuController.SORT )) {
            actionSetSorted( );

        } else {
            Vector suffixWithNoEntryList = _databaseConfig.getSuffixesWithoutEntryList();            
            if (suffixWithNoEntryList != null) {
                if (suffixWithNoEntryList.indexOf(cmd) >= 0) {                    
                    actionCreateRootEntry(cmd);
                } else {
                    Vector ldbmDatabaseList = _databaseConfig.getDatabaseList(DatabaseConfig.LDBM_DATABASES);
                    if (ldbmDatabaseList.indexOf(cmd) >= 0) {                    
                        actionSelectPartitionView(cmd);                    
                    }
                }
            }
        }
        setBusyCursor(false);
    }

    /**
     * MouseListener implementation
     * ====================================
     *
     * Called when mouse activity occurs in resource
     * tree.  Informs model of run().
     */
    public void mouseClicked(MouseEvent e) {
        if (_isInitialized && (e.getClickCount() == 2)) {
            if (_lastFocusComponent == _tree) {
                TreePath path = getLastSelectedPath();
                if (path != null) {
                    TreePath clickedPath = _tree.getPathForLocation(e.getX(), e.getY());
                    if ((clickedPath != null) &&
                        clickedPath.equals(path)) {
                        setBusyCursor(true);
                        actionEdit();
                        setBusyCursor(false);
                    }
                }
            } else if (_lastFocusComponent == _list) {
                int index = getLastSelectedIndex();
                if (index >= 0) { // double click                                
                    int clickedIndex = _list.locationToIndex(e.getPoint());
                    if (clickedIndex == index) {
                        setBusyCursor(true);
                        actionEdit();                        
                        setBusyCursor(false);                    
                    }
                }
            }            
        }
        
        // (miodrag) If a tree node is selected, the tree does not generate
        // a TreeSelectionLinstener.valueChanged event when we click 
        // a list node and then click back the same tree node, as the
        // tree selection did not change. To compensate for this, we 
        // setup the popup menu and node name on each mouse click.
        else if (_isInitialized && e.getClickCount() == 1) {
            IBrowserNodeInfo node = getSelectedNodeInfo();            
            if (node != null) {
                if (node.getErrorType() == node.ERROR_SEARCHING_CHILDREN) {
                    _menuController.disableMenus();
                    _selectedDnLabel.setText(NO_SELECTED_DN);
                    return;
                }

                _menuController.updateMenuState();
                String dn = Helper.getNodeInfoDN(node);            
                if ((dn == null) ||
                    dn.equals("")) {
                    _selectedDnLabel.setText(NO_SELECTED_DN);                
                } else {                
                    _selectedDnLabel.setText(findDisplayNameFromNode(node));                
                }
            }
        }
    }


    /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mousePressed(MouseEvent e) {
        if ((_contextMenu != null) && (e.isPopupTrigger())) {
            if (_contextMenu.getComponentCount() > 0) {
                Point p = _treePanel.getViewport().getViewPosition();
                _contextMenu.show((Component)e.getSource(), e.getX() - p.x,
                        e.getY() - p.y);
            }
        }            
    }

    /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mouseEntered(MouseEvent e) {
    }

   /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mouseExited(MouseEvent e) {
    }

   /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mouseReleased(MouseEvent e) {
        if ((_contextMenu != null) && (e.isPopupTrigger())) {
            if (_contextMenu.getComponentCount() > 0) {
                Point p = _treePanel.getViewport().getViewPosition();
                _contextMenu.show((Component)e.getSource(), e.getX() - p.x,
                        e.getY() - p.y);
            }
        }
    }

   /**
     * FocusListener implementation
     * ====================================
     */
    public void focusGained(FocusEvent e) {
        // (miodrag) If a popup menu or a dialog is opened, both the _tree
        // and the _list will loose focus. We record the last focus component
        // as it is much safer to use then calling hasFocus on the _tree and
        // _list.
        _lastFocusComponent = (Component) e.getSource();
    }
    public void focusLost(FocusEvent e) {
    }


    private void refreshDatabaseConfigAndSuffixes(boolean refreshAll) {
        ConsoleInfo serverInfo = _framework.getServerObject().getServerInfo();
        LDAPConnection ldc = serverInfo.getLDAPConnection();
        try {
            _framework.setBusyCursor(true);
            Vector oldSuffixes = _databaseConfig.getRootSuffixesWithEntry();
            _databaseConfig.reload(ldc);
            Vector newSuffixes = _databaseConfig.getRootSuffixesWithEntry();

            if (refreshAll) {
                _controller.removeAllSuffixes();
                for (int i=0; i<newSuffixes.size(); i++) {
                    String suffix = (String)newSuffixes.elementAt(i);
                    _controller.addSuffix(suffix, null);
                }
                _controller.addSuffix("cn=schema", null);
                _controller.addSuffix("cn=monitor", null);
                _controller.addSuffix("cn=config", null);
                return;
            }
                        
            Vector addedSuffixes = new Vector();
            Vector deletedSuffixes = new Vector();
            for (int i=0; i<oldSuffixes.size(); i++) {                
                DN oldDN = new DN((String)oldSuffixes.elementAt(i));
                boolean toDelete = true;
                for (int j=0; (j<newSuffixes.size()) && toDelete; j++) {
                    DN newDN = new DN((String)newSuffixes.elementAt(j));
                    if (newDN.equals(oldDN)) {
                        toDelete = false;
                    }
                }
                if (toDelete) {
                    deletedSuffixes.add(oldSuffixes.elementAt(i));
                }
            }

            for (int i=0; i<newSuffixes.size(); i++) {                
                DN newDN = new DN((String)newSuffixes.elementAt(i));
                boolean toAdd = true;
                for (int j=0; (j<oldSuffixes.size()) && toAdd; j++) {
                    DN oldDN = new DN((String)oldSuffixes.elementAt(j));
                    if (newDN.equals(oldDN)) {
                        toAdd = false;
                    }
                }
                if (toAdd) {
                    addedSuffixes.add(newSuffixes.elementAt(i));
                }
            }

            for (int i=0; i<deletedSuffixes.size(); i++) {
                _controller.removeSuffix((String)deletedSuffixes.elementAt(i));
            }
            for (int i=0; i<addedSuffixes.size(); i++) {
                _controller.addSuffix((String)addedSuffixes.elementAt(i), null);
            }

        } catch (LDAPException e) {
            String[] args = {Helper.getLDAPErrorMessage(e)};
            DSUtil.showErrorDialog( _framework,
                                    "reading-databaseconfig-error-title",
                                    "reading-databaseconfig-error-msg",
                                    args,
                                    "browser" );
        }
        finally {
            _framework.setBusyCursor(false);
        }
    }
    
        /**
         * This method is called before we expand a node in the tree, or we select a node and we have
         * a node/leaf view.  In other words, before we search for the children of a node.
         *
         * Checks the number of children of a given node, and, considering the display options
         * chosen, asks the user for confirmation to continue or not.  
         *
         * Return true if the user chooses to continue with the operation (display children of the node).
         * false otherwise.
         */
    private boolean checkChildrenForNode(IBrowserNodeInfo node) {
        
        int vlvState = getNodeVlvState(node).intValue();
        boolean hasIndex = (vlvState == CreateVLVIndex.HAS_INDEX);

        if (hasIndex) {
            return true; // Node has VLV index
        }

        boolean continueOperation = true;
        boolean limitDlgShown = false;
        int numSubordinates = node.getNumSubOrdinates();
        if (_controller.isSorted()  &&
            _showSortedChildrenLimitExceededDialog) {
            if (numSubordinates > NO_VLV_SORTED_LIMIT) {
                boolean createVlvIndex = true;
                String dn = Helper.getNodeInfoDN(node);        
                if (dn != null) {
                    Integer state =  (Integer)_vlvCache.get(dn);
                    if ((state != null) &&
                        ((state.intValue() == CreateVLVIndex.HAS_INDEX) || (state.intValue() ==CreateVLVIndex.CAN_NOT_HAVE_INDEX))) {
                        createVlvIndex = false;
                    }
                }
                if (createVlvIndex) {
                    SortedChildrenLimitExceededDialog dlg = new SortedChildrenLimitExceededDialog(_framework,
                                                                                                  node);
                    dlg.packAndShow();
                    limitDlgShown = true;
                    _showSortedChildrenLimitExceededDialog = dlg.continueShowingDialog();

                    if (!_showSortedChildrenLimitExceededDialog) {                    
                        _preferences.set(SHOW_SORTED_CHILDREN_LIMIT_EXCEEDED, _showSortedChildrenLimitExceededDialog);
                    }
                }                    
            }
        }
        
        if (_showDisplayedChildrenLimitExceededDialog && !limitDlgShown &&
            continueOperation &&(numSubordinates > DISPLAYED_CHILDREN_LIMIT)) {

            DisplayedChildrenLimitExceededDialog dlg = new DisplayedChildrenLimitExceededDialog(_framework,
                                                                                                node);
            dlg.packAndShow();
            _showDisplayedChildrenLimitExceededDialog = dlg.continueShowingDialog();

            if (!_showDisplayedChildrenLimitExceededDialog) {                    
                _preferences.set(SHOW_DISPLAYED_CHILDREN_LIMIT_EXCEEDED, _showDisplayedChildrenLimitExceededDialog);
            }
        }
        
        return continueOperation;
    }
    

    private String findDisplayNameFromNode(IBrowserNodeInfo node) {
        String display = null;
        LDAPUrl url = node.getURL();
        if (node.isRemote()) {
            display = url.getDN()+"   ( "+url.getHost()+":"+url.getPort()+" )";
        } else {
            display = url.getDN();
        }
        return display;
    }

    private boolean continueOperation(IBrowserNodeInfo node) {
        boolean continueOperation = true;
        int error = node.getErrorType();
        if (error == IBrowserNodeInfo.ERROR_NONE) {
            if (node.isRemote() &&
                _showRemoteInformationDialog) {
                /* Display a message telling that this operation is performed in 
                   a remote node */
                RemoteNodeInformationDialog dlg = new RemoteNodeInformationDialog(_framework,
                                                                                  node);
                dlg.packAndShow();
                _showRemoteInformationDialog = dlg.continueShowingDialog();
                if (!_showRemoteInformationDialog) {                    
                    _preferences.set(SHOW_REMOTE_INFORMATION_DIALOG, _showRemoteInformationDialog);
                }
                continueOperation = !dlg.isCancelled();
            }
        }
        else if (error == IBrowserNodeInfo.ERROR_SOLVING_REFERRAL) {
            String errorUrl = (String)node.getErrorArg();
            Exception errorException = node.getErrorException();       
            NonResolvedReferralDialog dlg = new NonResolvedReferralDialog(_framework, 
                                                                          node.getURL(),
                                                                          errorUrl,
                                                                          errorException);
            dlg.packAndShow();
            continueOperation = ! dlg.isCancel();            
        }
        else if (error == IBrowserNodeInfo.ERROR_SEARCHING_CHILDREN) {
            continueOperation = false;
        }        
        return continueOperation;
    }        
        

    private void actionFollowReferrals() {            
        _controller.setFollowReferrals(!_controller.getFollowReferrals());
        _childrenController.setFollowReferrals(_controller.getFollowReferrals());

        /* Clear the selection... */
        _tree.clearSelection();
        
        /* Refresh the attribute table */
        _attributeController.reset();
        
        /* Refresh the vlv cache */
        _vlvCache.clear();

        /* Refresh the activation cache */
        _activationCache.clear();

        _preferences.set(FOLLOW_REFERRALS_PREFERENCES, _controller.getFollowReferrals());
    }

    private void actionRefreshTree() {        
        
        /* Clear the selection... */
        _list.clearSelection();        
        _tree.clearSelection();

        /* Refresh the database config */
        /* NOTE: if we implement a listener mechanism for the database config,
           we should modify this */
        refreshDatabaseConfigAndSuffixes(/*refreshAll=*/true);
        
        /* Refresh the schema */
        setSchema(null);        
        _allAttrs = null;

        /* Refresh the attribute table */
        _attributeController.reset();
        
        /* Refresh the vlv cache */
        _vlvCache.clear();

        /* Refresh the activation cache */
        _activationCache.clear();
        
        /* Refresh the entry Editor */
        _entryEditor.setSchema(getSchema());

        /* Refresh the menus */
        _menuController.recreateDynamicMenus();        

        /* Execute the refresh in the tree */        
        _tree.grabFocus();
        _controller.startRefresh();
    }

    private void actionRefreshNode() {        

        if (_lastFocusComponent == _list) {
            /* Execute the refresh for the list node */
            _childrenController.startRefresh(getSelectedNodeInfo());
            return;
        }

        /* Execute the refresh for the tree node */
        _tree.grabFocus();        
        _controller.startRefresh(getSelectedNodeInfo());

        /* Unselect/seclect the node for the full refresh */
        TreePath path = _tree.getSelectionPath();
        _tree.clearSelection();
        _tree.setSelectionPath(path);
    }

    private void actionSearchUG() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {
                // We cannot pass ldc as is to the ResourcePickerDlg :
                // we must update the search constraints in order the ResourcePickerDlg
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection rpLdc = prepareReferralConnection(ldc);
                // TODO: the sequence above is useless because the
                // ResourcePickerDlg overrides the search constraints without
                // preserving _manageDSAITControl.
                // We have to find a trick here.
                
                ConsoleInfo info = (ConsoleInfo)_framework.getServerObject().getServerInfo().clone();                
                info.setUserBaseDN( Helper.getNodeInfoDN(node) );
                info.setUserLDAPConnection(rpLdc);
                info.setLDAPConnection(rpLdc);
                info.setUserHost(rpLdc.getHost());
                info.setUserPort(rpLdc.getPort());
                // user/group window search
                Debug.println(9, "DSContentPage.actionSearchUG: cloned connection ["
                              + rpLdc + "] isConnected=" + rpLdc.isConnected()
                              + " isAuthenticated=" + rpLdc.isAuthenticated());
                ResourcePickerDlg resourcePickerDlg =
                new ResourcePickerDlg( info, new SearchUG(_framework, _iconPool, rpLdc, _entryEditor),
                                       _framework );
                resourcePickerDlg.appendSearchInterface( new DSSearchPanel() );    
                resourcePickerDlg.show();

                /* richm 09/09/2005
                   although actionEdit and actionAdvancedEdit close the ldc cloned for editing
                   purposes, we cannot do that here, because SearchUG will eventually spawn
                   a separate thread on which the editing occurs - if we close it here, we
                   may close it out from underneath the editor
                */
                
                _connectionPool.releaseConnection(ldc);
            }    
        }
    }    

    private void actionSelectPartitionView(String partition) {        
        // Update the connection constraints of the controller        
        _selectedPartitionView = partition;
        
        if (!_selectedPartitionView.equals(ContentMenuController.VIEW_ALL_PARTITIONS)) {
            /* If we want to see just a partition, we check in the controls of the connection
               if there's already a control to search just a partition (with OID = SEARCH_OID).
               If it is the case, we replace this control.   If we don't find a control of this
               type we just add the new control */            
            LDAPConnection controllerLdc = _controller.getLDAPConnection();
            LDAPControl[] ctls = controllerLdc.getSearchConstraints().getServerControls();
            byte[] vals = null;
            try {
                vals = _selectedPartitionView.getBytes( "UTF8" );                        
            } catch ( UnsupportedEncodingException uEEx ) {
                Debug.println( "DSContentPage.actionSelectPartitionView() "+uEEx);                
            }
            LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);
            if (ctls != null) {
                boolean controlFound = false;
                for (int i=0; (i < ctls.length) && !controlFound; i++) {
                    if (ctls[i].getID().equals(SEARCH_OID)) {
                        controlFound = true;                        
                        ctls[i] = searchControl;
                    }
                }
                if (!controlFound) {
                    LDAPControl[] newCtls = new LDAPControl[ctls.length + 1];
                    for (int i=0; i < ctls.length; i++) {
                        newCtls[i] = ctls[i];
                    }                    
                    newCtls[newCtls.length - 1] = searchControl;
                }                
            } else {
                LDAPControl[] newCtls = {searchControl};
                controllerLdc.getSearchConstraints().setServerControls(newCtls);
            }
            
            /* Update the tree */
            String selectedSuffix = _databaseConfig.getSuffixForDatabase(partition);
            _controller.removeAllSuffixes();            
            _controller.addSuffix(selectedSuffix, null);            
        } else {
            /* If we want to see all the partitions, we check if there's the control
               to see just one partition.  If it is the case, we remove this control */
            LDAPConnection controllerLdc = _controller.getLDAPConnection();
            LDAPControl[] ctls = controllerLdc.getSearchConstraints().getServerControls();            
            if (ctls != null) {
                Vector v = new Vector();
                for (int i=0; i < ctls.length; i++) {
                    if (!ctls[i].getID().equals(SEARCH_OID)) {
                        v.addElement(ctls[i]);
                    }
                }
                LDAPControl[] newCtls = new LDAPControl[v.size()];
                v.copyInto(newCtls);
                controllerLdc.getSearchConstraints().setServerControls(newCtls);
            }

            /* Update the tree */
            _controller.removeAllSuffixes();
            Vector vSuffixes = _databaseConfig.getRootSuffixesWithEntry();
            // We populate the browser controller            
            for (int i = 0; i < vSuffixes.size(); i++) {
                _controller.addSuffix((String)vSuffixes.elementAt(i), null);
            }
            _controller.addSuffix("cn=schema", null);
            _controller.addSuffix("cn=monitor", null);
            _controller.addSuffix("cn=config", null);            
        }        
        _menuController.updateMenuState();
        if (!getSelectedPartitionView().equals(ContentMenuController.VIEW_ALL_PARTITIONS) &&
            getFollowReferrals()) {
            actionFollowReferrals();
        } else {
            /* Follow referrals clears selection: do the same thing */            
            _tree.clearSelection();
        }
    }

    private void actionDisplayACICount() {
        if ((_display & BrowserController.DISPLAY_ACI_COUNT) == 0) {
            _display = _display | BrowserController.DISPLAY_ACI_COUNT;
        } else {
            _display = _display &  (~BrowserController.DISPLAY_ACI_COUNT);
        }
        _controller.setDisplayFlags(_display);
        _childrenController.setDisplayFlags(_display);        
        if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
            _childrenController.startRefresh();
        }
        _preferences.set(DISPLAY_PREFERENCES, _display);                
    }

    private void actionDisplayRoleCount() {        
        if ((_display & BrowserController.DISPLAY_ROLE_COUNT) == 0) {
            _display = _display | BrowserController.DISPLAY_ROLE_COUNT;
        } else {
            _display = _display &  (~BrowserController.DISPLAY_ROLE_COUNT);
        }
        _controller.setDisplayFlags(_display);
        _childrenController.setDisplayFlags(_display);        
        if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
            _childrenController.startRefresh();
        }
        _preferences.set(DISPLAY_PREFERENCES, _display);        
    }

    private void actionDisplayAccountInactivation() {
        if ((_display & BrowserController.DISPLAY_ACTIVATION_STATE) == 0) {
            _display = _display | BrowserController.DISPLAY_ACTIVATION_STATE;
        } else {
            _display = _display &  (~BrowserController.DISPLAY_ACTIVATION_STATE);
        }
        _controller.setDisplayFlags(_display);
        _childrenController.setDisplayFlags(_display);        
        if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
            _childrenController.startRefresh();
        }
        _preferences.set(DISPLAY_PREFERENCES, _display);
    }

    private void actionOnlyTreeLayout() {
        if (!_layout.equals(ContentMenuController.ONLY_TREE_LAYOUT)) {                    
            _displayedPanel.remove(_splitPanel);
            _displayedPanel.add(_treePanel);        
            
            _layout = ContentMenuController.ONLY_TREE_LAYOUT;
                
            if (_controller.isShowContainerOnly()) {
                _controller.setShowContainerOnly(false);
            }                        

            // Clear selection and get the focus            
            _tree.clearSelection();
            _tree.grabFocus();

            validate();
            repaint();
            
            _preferences.set(LAYOUT_PREFERENCES, _layout);
        }
    }

    private void actionNodeLeafLayout() {
        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {                
            _splitPanel.setRightComponent(_childrenPanel);

            if (_layout.equals(ContentMenuController.ONLY_TREE_LAYOUT)) {
                _displayedPanel.remove(_treePanel);  
                _splitPanel.setLeftComponent(_treePanel);
                _displayedPanel.add(_splitPanel);
            }                        

            _layout = ContentMenuController.NODE_LEAF_LAYOUT;
            
            if (!_controller.isShowContainerOnly()) {
                _controller.setShowContainerOnly(true);
            }                    

            // Clear selection and get the focus            
            _tree.clearSelection();
            _tree.grabFocus();

            IBrowserNodeInfo node = getSelectedNodeInfo();                                            
            int vlvState = getNodeVlvState(node).intValue();
            boolean hasIndex = (vlvState == CreateVLVIndex.HAS_INDEX);
            
            _childrenController.setBaseNodeInfo(node, hasIndex);
            _list.setBackground(_listDefaultColor);

            validate();
            repaint();

            _preferences.set(LAYOUT_PREFERENCES, _layout);
        }
    }

    private void actionAttributeLayout() {
        if (!_layout.equals(ContentMenuController.ATTRIBUTE_LAYOUT)) {            
            _splitPanel.setRightComponent(_attributePanel);
            
            if (_layout.equals(ContentMenuController.ONLY_TREE_LAYOUT)) {
                _displayedPanel.remove(_treePanel);  
                _splitPanel.setLeftComponent(_treePanel);
                _displayedPanel.add(_splitPanel);                                
            }                                
        
            _layout = ContentMenuController.ATTRIBUTE_LAYOUT;    
                
            if (_controller.isShowContainerOnly()) {
                _controller.setShowContainerOnly(false);
            }

            // Clear selection and get the focus            
            _tree.clearSelection();
            _tree.grabFocus();
            IBrowserNodeInfo node = getSelectedNodeInfo();
            if (node == null) {            
                _attributeController.clearAttributePanel();                
            } else {            
                _attributeController.updateAttributePanel(node);
            }            
            
            validate();
            repaint();            

            _preferences.set(LAYOUT_PREFERENCES, _layout);
        }
    }        

    private void actionSetSorted() {        
        _controller.setSorted(!_controller.isSorted());
        _childrenController.setSorted(_controller.isSorted());        

        _preferences.set(SORT_PREFERENCES, _controller.isSorted());
        
        // Clear selection
        _tree.clearSelection();
    }

    private void actionCopy() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            if (continueOperation(node)) {
                LDAPConnection ldc = null;
                try {                
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                    String dn = Helper.getNodeInfoDN(node);
                    /* Do the copy */
                    LDAPConnection ldcNr = prepareReferralConnection(ldc);
                    /* For the copy we need to not limit the number of results in the
                     search constraints */
                    LDAPSearchConstraints cons =
                        ldcNr.getSearchConstraints();
                    cons.setMaxResults(0);
                    ldcNr.setSearchConstraints(cons);

                    Copier copier = new Copier(ldcNr, dn, _framework, _clipboard);
                    copier.execute();

                    try {
                        ldcNr.disconnect(); // prepareReferralConnection clones connection
                     } catch (Exception ignore) {}   
                    _connectionPool.releaseConnection(ldc);
                    /* In order to update the Paste menu */
                    _menuController.updateMenuState();
                }
            }
        }
    }        

    private void actionCopyDN() {    
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            if (continueOperation(node)) {
                String dn = Helper.getNodeInfoDN(node);
                StringSelection ss = new StringSelection( dn );
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                                                             ss,
                                                                             ss );
            }
        }
    }

    private void actionCopyLDAPURL() {    
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            StringSelection ss = new StringSelection( node.getURL().toString() );
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                                                         ss,
                                                                         ss );
        }
    }

    private void actionPaste() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            if (continueOperation(node)) {
                LDAPConnection ldc = null;
                try {                
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                    String dn = Helper.getNodeInfoDN(node);
                    /* Do the paste */
                    LDAPConnection ldcNr = prepareReferralConnection(ldc);
                    Paster paster = new Paster(ldcNr, dn, _framework, _clipboard);
                    paster.execute();
                    
                    /* Update the tree */
                    Vector addedRootEntries = paster.getPastedRootEntries();
                    if (addedRootEntries != null) {                        
                        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                            (_lastFocusComponent == _tree)) {
                            Enumeration e = addedRootEntries.elements();
                            while (e.hasMoreElements()) {
                                String pastedDn = (String)e.nextElement();
                                _controller.notifyEntryAdded(node, pastedDn);
                                if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
                                    _childrenController.notifyEntryAdded(pastedDn);
                                }
                            }
                        } else if (_lastFocusComponent == _list) {                            
                            _childrenController.notifyEntryChanged(node);
                            TreePath path = getLastSelectedPath();
                            if (path != null) {
                                IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                                if (treeNode != null) {
                                    _controller.notifyChildEntryChanged(treeNode, dn);
                                }
                            }
                        }                        
                    }            
                    try {
                        ldcNr.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    _connectionPool.releaseConnection(ldc);
                }
            }
        }        
    }
       
    private void actionCut() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            if (continueOperation(node)) {
                LDAPConnection ldc = null;
                try {                
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                    String dn = Helper.getNodeInfoDN(node);
                    /* Do the cut */
                    LDAPConnection ldcNr = prepareReferralConnection(ldc);
                    /* For the cut we need to not limit the number of results in the
                       search constraints */
                    LDAPSearchConstraints cons =
                        ldcNr.getSearchConstraints();
                    cons.setMaxResults(0);
                    ldcNr.setSearchConstraints(cons);

                    CutDeleter cutter = new CutDeleter(ldcNr, dn, _framework, _clipboard, true);
                    cutter.execute();
                    /* In order to update the Paste menu */
                    _menuController.updateMenuState();

                    if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                            (_lastFocusComponent == _tree)) {
                        /* Select the parent of the entry we tried to delete */
                        TreePath parentPath = getLastSelectedPath().getParentPath();
                        _tree.setSelectionPath(parentPath);
                    }
                    
                    if (cutter.isWholeTreeDeleted()) {
                        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                            (_lastFocusComponent == _tree)) {
                            /* If everything deleted, just inform the browser controller */
                            _controller.notifyEntryDeleted(node);
                        } else if (_lastFocusComponent == _list) {
                            _childrenController.notifyEntryDeleted(node);    
                            TreePath path = getLastSelectedPath();
                            if (path != null) {
                                IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                                if (treeNode != null) {
                                    _controller.notifyChildEntryDeleted(treeNode, dn);
                                }
                            }                            
                        }
                        checkObjectDeleted(dn, ldcNr);
                    } else {
                        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                            (_lastFocusComponent == _tree)) {
                            /* If not, refresh it */
                            _controller.startRefresh(node);
                        } else if (_lastFocusComponent == _list) {
                            _childrenController.notifyEntryChanged(node);
                            TreePath path = getLastSelectedPath();
                            if (path != null) {
                                IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                                if (treeNode != null) {
                                    _controller.notifyChildEntryChanged(treeNode, dn);
                                }
                            }
                        }
                    }
                    try {
                        ldcNr.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    _connectionPool.releaseConnection(ldc);
                }
            }
        }
    }

    private void actionDelete() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null) {
            if (continueOperation(node)) {
                LDAPConnection ldc = null;
                try {                
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                    String dn = Helper.getNodeInfoDN(node);
                    
                    /* Do the delete */
                    LDAPConnection ldcNr = prepareReferralConnection(ldc);
                    /* For the delete we need to not limit the number of results in the
                       search constraints */
                    LDAPSearchConstraints cons =
                        ldcNr.getSearchConstraints();
                    cons.setMaxResults(0);
                    ldcNr.setSearchConstraints(cons);

                    CutDeleter deleter = new CutDeleter(ldcNr, dn, _framework, _clipboard, false);
                    deleter.execute();                
                    
                    if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                        (_lastFocusComponent == _tree)) {
                        /* Select the parent of the entry we tried to delete */
                        TreePath parentPath = getLastSelectedPath().getParentPath();
                        _tree.setSelectionPath(parentPath);
                    }
                    
                    if (deleter.isWholeTreeDeleted()) {
                        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                            (_lastFocusComponent == _tree)) {
                            /* If everything deleted, just inform the browser controller */
                            _controller.notifyEntryDeleted(node);
                        } else if (_lastFocusComponent == _list) {                            
                            _childrenController.notifyEntryDeleted(node);
                            TreePath path = getLastSelectedPath();
                            if (path != null) {
                                IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                                if (treeNode != null) {
                                    _controller.notifyChildEntryDeleted(treeNode, dn);
                                }
                            }                            
                        }
                        checkObjectDeleted(dn, ldcNr);
                    } else {
                        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                            (_lastFocusComponent == _tree)) {
                            /* If not, refresh it */
                            _controller.startRefresh(node);
                        } else if (_lastFocusComponent == _list) {
                            _childrenController.notifyEntryChanged(node);
                            TreePath path = getLastSelectedPath();
                            if (path != null) {
                                IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                                if (treeNode != null) {
                                    _controller.notifyChildEntryChanged(treeNode, dn);
                                }
                            }
                        }
                    }
                    try {
                        ldcNr.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    _connectionPool.releaseConnection(ldc);
                }
            }
        }
    }   

        /**
          * This method is called after we delete a WHOLE subtree
          *
          * It checks if the 'New Root Entry' menu has to be updated or not.
          *
          * @returns true if the DatabaseConfig has been updated. false otherwise.
          * 
          */
    private boolean checkObjectDeleted(String deletedObjectDn, LDAPConnection ldc) {
        boolean databaseConfigUpdated = false;
        /* We see if we deleted one of the root suffixes, and if so, tell to update the menu */                
        DN dn1 = new DN (deletedObjectDn);        
        Vector rootSuffixesWithEntry = _databaseConfig.getRootSuffixesWithEntry();
        for (int i=0; (i< rootSuffixesWithEntry.size()) && !databaseConfigUpdated; i++) {
            String currentSuffixDn = (String)rootSuffixesWithEntry.elementAt(i);
            DN dn2 = new DN(currentSuffixDn);
            if (dn1.equals(dn2)) {                
                _databaseConfig.setHasRootEntry(currentSuffixDn, false);
                databaseConfigUpdated = true;                    
            }
        }
        if (databaseConfigUpdated) {
            /* Update the new root entry menu items */
            _menuController.recreateNewRootEntryMenus();
        }
        return databaseConfigUpdated;
    }

    private void actionACL(  ) {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if ( node != null ) {
            if (continueOperation(node)) {
                String dn = Helper.getNodeInfoDN(node);
                
                LDAPConnection ldc = null;
                try {
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                
                    // We cannot pass ldc as is to the ACIManager :
                    // we must update the search constraints in order the ACI manager
                    // is not returned a referral exception (in the case of node
                    // is a referral).
                    LDAPConnection aciLdc = prepareReferralConnection(ldc);
                    
                    //
                    // Let's select the base dn for searching users.
                    // Note that we implement a temporary solution here: 
                    // for 5.0 RTM we should be able to pass all the accessible 
                    // suffixes to the ACI editor. See:
                    // https://scopus.mcom.com/bugsplat/show_bug.cgi?id=523566
                    //
                    LDAPConnection ugLdc = Console.getConsoleInfo().getUserLDAPConnection();
                    String ugDn;
                    if (ugLdc.getHost().equals(aciLdc.getHost()) && 
                        (ugLdc.getPort() == aciLdc.getPort())) {
                        Debug.println("ContentModel.actionACL: ACI and users are on the same directory");
                    ugDn = Console.getConsoleInfo().getUserBaseDN();
                    }
                    else {
                        Debug.println("ContentModel.actionACL: ACI and users are on different directories");
                        ugDn = _databaseConfig.getRootSuffixForEntry(dn);
                    }
                    Debug.println("ContentModel.actionACL: users will be search from " + ugDn);
                    ACIManager acm = new ACIManager(_framework, dn, aciLdc, dn, aciLdc, ugDn);                    
                    acm.show();
                    try {
                        aciLdc.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    _connectionPool.releaseConnection(ldc);
                    if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                        (_lastFocusComponent == _tree)) {
                        _controller.notifyEntryChanged(node);
                    } else if (_lastFocusComponent == _list) {
                        _childrenController.notifyEntryChanged(node);
                        TreePath path = getLastSelectedPath();
                        if (path != null) {
                            IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                            if (treeNode != null) {
                                _controller.notifyChildEntryChanged(treeNode, dn);
                            }
                        }
                    }
                    /* Refresh the attribute table */
                    _attributeController.reset();
                }            
            }
        }
    }

    private void actionRoles() {                
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if ( node != null ) {
            /* Only allow to set roles of the entries in the server */
            if (!node.isRemote()) {
                String dn = Helper.getNodeInfoDN(node);
                
                LDAPConnection ldc = prepareReferralConnection(_framework.getServerObject().getServerInfo().getLDAPConnection());
            
                ConsoleInfo ci = new ConsoleInfo(
                                                 ldc.getHost(),
                                                 ldc.getPort(),
                                                 ldc.getAuthenticationDN(),
                                                 ldc.getAuthenticationPassword(),
                                                 dn );        
                ci.setCurrentDN( dn );
                ci.setUserGroupDN( dn );
                ci.setLDAPConnection( ldc );
                RoleEditorDialog ed = new RoleEditorDialog(_framework, ci);
                ed.show();                
                if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                    (_lastFocusComponent == _tree)) {
                    _controller.notifyEntryChanged(node);
                } else if (_lastFocusComponent == _list) {
                    _childrenController.notifyEntryChanged(node);
                    TreePath path = getLastSelectedPath();
                    if (path != null) {
                        IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                        if (treeNode != null) {
                            _controller.notifyChildEntryChanged(treeNode, dn);
                        }
                    }
                }
                /* Refresh the attribute table */
                _attributeController.reset();
                try {
                    ldc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
            } else {
                DSUtil.showErrorDialog( _framework,
                                        "unsupported-remote-error-title",
                                        "unsupported-remote-error-msg",
                                        (String[]) null,
                                        "browser" );
            }
        }
    }

    private void actionSetReferral() {                        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if ( node != null ) {                        
            String dn = Helper.getNodeInfoDN(node);            
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {                                
                displayConnectionError(e, node);
            }
            if (ldc != null) {                
                ReferralEditor ed = new ReferralEditor(_framework, 
                                                       ldc, dn,
                                                       _connectionPool);                
                ed.packAndShow();
                _connectionPool.releaseConnection(ldc);
                if (ed.isObjectModified()) {
                    if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                        (_lastFocusComponent == _tree)) {
                        _controller.notifyEntryChanged(node);
                    } else if (_lastFocusComponent == _list) {
                        _childrenController.notifyEntryChanged(node);
                        TreePath path = getLastSelectedPath();
                        if (path != null) {
                            IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                            if (treeNode != null) {
                                _controller.notifyChildEntryChanged(treeNode, Helper.getNodeInfoDN(node));
                            }
                        }
                    }
                }
                if (ed.isAuthModified()) {
                    _controller.notifyAuthDataChanged(null);
                }
                /* Refresh the attribute table */
                _attributeController.reset();
            }
        }
    }

    /**
     * NOTE: for the VLV index we can use the server info (and its connection)
     * without worries because we can only create VLV indexes in the current server
     */
    private void actionCreateVLVIndex(  ) {
        Thread thread = new Thread(new Runnable() {
            public void run() {        
                IBrowserNodeInfo node = getSelectedNodeInfo();
                if ( node != null ) {
                    if (!node.isRemote()) {
                        CreateVLVIndex task =
                            new CreateVLVIndex( _framework.getServerObject().getServerInfo() );
                        task.execute(Helper.getNodeInfoDN(node));                    
                        _vlvCache.remove(Helper.getNodeInfoDN(node));
                        _menuController.updateMenuState();
                        _childrenController.notifyIndexChanged(/*hasIndex=*/true);
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                DSUtil.showErrorDialog( _framework,
                                                        "unsupported-remote-error-title", 
                                                        "unsupported-remote-error-msg", 
                                                        (String[]) null, 
                                                        "browser" );
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    private void actionDeleteVLVIndex() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                IBrowserNodeInfo node = getSelectedNodeInfo();
                if ( node != null ) {
                    CreateVLVIndex.deleteIndex(Helper.getNodeInfoDN(node), _framework.getServerObject().getServerInfo() );                    
                    _vlvCache.remove(Helper.getNodeInfoDN(node));
                    _menuController.updateMenuState();
                    _childrenController.notifyIndexChanged(/*hasIndex=*/false);
                }
            }
        });
        thread.start();
    }

	private void actionSetPWP( int type ) {

		IBrowserNodeInfo node = getSelectedNodeInfo();
		if ( node != null ) {                        
			String dn = Helper.getNodeInfoDN(node);
			if ( dn != null) {
				BlankPanel child = new BlankPanel(_resourceModel);
				child.setLayout(new BorderLayout());

				PasswordPolicyTabbedDialog tabchild = 
						new PasswordPolicyTabbedDialog(_resourceModel, dn, type);
				child.add(tabchild);
	
				String subres = (type == PasswordPolicyPanel.FINEGRAINED_USER ?
												"titleuser" : "titlesubtree" );
																
				SimpleDialog dlg = new SimpleDialog( _resourceModel.getFrame(),
					DSUtil._resource.getString("passwordpolicy-finegrained",
												subres),
					SimpleDialog.CLOSE, child );

				child.init();
				dlg.packAndShow();

				if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                (_lastFocusComponent == _tree)) {
                	_controller.notifyEntryChanged(node);
                } else if (_lastFocusComponent == _list) {
                	_childrenController.notifyEntryChanged(node);
                    TreePath path = getLastSelectedPath();
                    if (path != null) {
                    	IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                        if (treeNode != null) {
                        	_controller.notifyChildEntryChanged(treeNode, Helper.getNodeInfoDN(node));
                        }
                    }
				}
			}
        } 
    }

    private void actionInactivate(  ) {                    
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final IBrowserNodeInfo node = getSelectedNodeInfo();
                if ( node != null ) {
                    if (!node.isRemote()) {
                        String dn = Helper.getNodeInfoDN(node);
                        LDAPConnection ldc = prepareReferralConnection(_framework.getServerObject().getServerInfo().getLDAPConnection());                        
                        final Inactivator task = new Inactivator(_framework, 
                                                                 dn, 
                                                                 ldc);
                        task.execute();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                updateViewAfterInactivation(node,
                                                            task.getLockingInfrastructureDNs());                                                
                            }
                        });
                        /* Refresh the attribute table */
                        _attributeController.reset();
                        _activationCache.remove(Helper.getNodeInfoDN(node));
                        _menuController.updateMenuState();
                        try {
                            ldc.disconnect(); // prepareReferralConnection clones connection
                        } catch (Exception ignore) {}
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                DSUtil.showErrorDialog( _framework,
                                                        "unsupported-remote-error-title", 
                                                        "unsupported-remote-error-msg", 
                                                        (String[]) null, 
                                                        "browser" );
                            }
                        });
                    }
                }
            }

            private void updateViewAfterInactivation(IBrowserNodeInfo node,
                                                     Vector lockingInfrastructureDNs) {                        
                String dn = Helper.getNodeInfoDN(node);
                if ((lockingInfrastructureDNs != null) &&
                    (lockingInfrastructureDNs.size() > 0)) {                    
                    /* We know the suffix node is just after the root node, and that it is
                       one of the parents of the node we added... */
                    TreePath parentPath = getLastSelectedPath();
                    while (parentPath.getPathCount() > 2) {
                        parentPath = parentPath.getParentPath();
                    }
                    IBrowserNodeInfo suffixNode = _controller.getNodeInfoFromPath(parentPath);
                    String nextEntry;
                    
                    /* If the selected node in the tree is the suffix node we have to update the children panel*/
                    boolean updateChildrenPanel = false;                                                        
                    if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {                                
                        TreePath path = getLastSelectedPath();                                                                
                        if (path.getPathCount() == 2) {                                    
                            updateChildrenPanel = true;
                        }
                    }
                    
                    boolean nodeLockingEntryAdded = false;
                    boolean leafLockingEntryAdded = false;
                    for (int i=0; i<lockingInfrastructureDNs.size(); i++) {
                        nextEntry = (String)lockingInfrastructureDNs.elementAt(i);
                        if (_controller.isShowContainerOnly()) {
                            /* The entry containing 'cn=nsAccountInactivationTmp' is the only entry
                               of the locking infrastructure that is a node */
                            if (nextEntry.indexOf("cn=nsAccountInactivationTmp") == 0) {
                                _controller.notifyEntryAdded(suffixNode, nextEntry);
                                nodeLockingEntryAdded = true;
                            } else if (nextEntry.indexOf("cn=\"cn=nsDisabledRole") == 0) {
                                leafLockingEntryAdded = true;
                            }
                            if (updateChildrenPanel) {                                        
                                /* The entry containing 'cn="cn=nsDisabledRole' is the only entry
                                   of the locking infrastructure that is not direct son of the suffix */
                                if (nextEntry.indexOf("cn=\"cn=nsDisabledRole") < 0) {
                                    _childrenController.notifyEntryAdded(nextEntry);                                            
                                }
                            }
                        } else {
                                /* The entry containing 'cn="cn=nsDisabledRole' is the only entry
                                   of the locking infrastructure that is not direct son of the suffix */
                            if (nextEntry.indexOf("cn=\"cn=nsDisabledRole") < 0) { 
                                _controller.notifyEntryAdded(suffixNode, nextEntry);                                        
                            }
                        }
                    }
                    if (_controller.isShowContainerOnly() &&
                        leafLockingEntryAdded &&
                        !nodeLockingEntryAdded) {
                        /* The "cn=nsAccountInactivationTmp" entry was already added, and we have added the
                           "cn=\"cn=nsDisabledRole" entry.  So cn=nsAccountInactivationTmp was a leaf and now it's
                           a node... we have to update the tree */
                        String suffixDn = Helper.getNodeInfoDN(suffixNode);
                        if (suffixDn != null) {
                            _controller.notifyEntryAdded(suffixNode, "cn=nsAccountInactivationTmp, "+suffixDn);
                        }
                    }
                }
                
                if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                    (_lastFocusComponent == _tree)) {
                    _controller.notifyEntryChanged(node);
                } else if (_lastFocusComponent == _list) {
                    _childrenController.notifyEntryChanged(node);                            
                    TreePath path = getLastSelectedPath();
                    if (path != null) {
                        IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                        if (treeNode != null) {
                            _controller.notifyChildEntryChanged(treeNode, dn);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private void actionActivate() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                IBrowserNodeInfo node = getSelectedNodeInfo();
                if ( node != null ) {                    
                    LDAPConnection ldc = prepareReferralConnection(_framework.getServerObject().getServerInfo().getLDAPConnection());                        
                        
                    String dn = Helper.getNodeInfoDN(node);
                    Activator task = new Activator(_framework, 
                                                   dn, 
                                                   ldc);
                    task.execute();
                    if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                        (_lastFocusComponent == _tree)) {
                        _controller.notifyEntryChanged(node);
                    } else if (_lastFocusComponent == _list) {
                        _childrenController.notifyEntryChanged(node);
                        TreePath path = getLastSelectedPath();
                        if (path != null) {
                            IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                            if (treeNode != null) {
                                _controller.notifyChildEntryChanged(treeNode, dn);
                            }
                        }
                    }
                    try {
                        ldc.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    /* Refresh the attribute table */
                    _attributeController.reset();
                    _activationCache.remove(Helper.getNodeInfoDN(node));
                    _menuController.updateMenuState();                    
                }                
            }
        });
        thread.start();
    }

    private void actionEdit() {        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            if (continueOperation(node)) {
                LDAPConnection ldc = null;
                try {
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                    // We cannot pass ldc as is to the entry editor :
                    // we must update the search constraints in order the entry editor
                    // is not returned a referral exception (in the case of node
                    // is a referral).
                    LDAPConnection editLdc = prepareReferralConnection(ldc);
                    /* If we edit roles we need to not limit the number of results in the
                       search constraints */
                    LDAPSearchConstraints cons =
                        editLdc.getSearchConstraints();
                    cons.setMaxResults(0);
                    editLdc.setSearchConstraints(cons);
                    
                    String oldDn = Helper.getNodeInfoDN(node);        
                    boolean objectModified = _entryEditor.editObject(oldDn, editLdc, false);
                    if (objectModified) {                        
                        String editedObjectDn = _entryEditor.getEditedObjectDn();
                        updateAfterModify(node, editedObjectDn, editLdc);                        
                    }
                    try {
                        editLdc.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    _connectionPool.releaseConnection(ldc);
                } 
            }
        }
    }


    private void actionAdvancedEdit() {        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            if (continueOperation(node)) {
                LDAPConnection ldc = null;
                try {
                    ldc = getConnectionForNode(node);
                } catch (LDAPException e) {
                    displayConnectionError(e, node);
                }
                if (ldc != null) {
                    // We cannot pass ldc as is to the entry editor :
                    // we must update the search constraints in order the entry editor
                    // is not returned a referral exception (in the case of node
                    // is a referral).
                    LDAPConnection editLdc = prepareReferralConnection(ldc);
                    /* If we edit roles we need to not limit the number of results in the
                       search constraints */
                    LDAPSearchConstraints cons =
                        editLdc.getSearchConstraints();
                    cons.setMaxResults(0);
                    editLdc.setSearchConstraints(cons);

                    String oldDn = Helper.getNodeInfoDN(node);        
                    boolean objectModified = _entryEditor.editObject(oldDn, editLdc, true);                    
                    if (objectModified) {                        
                        String editedObjectDn = _entryEditor.getEditedObjectDn();
                        updateAfterModify(node, editedObjectDn, editLdc);                        
                    }
                    try {
                        editLdc.disconnect(); // prepareReferralConnection clones connection
                    } catch (Exception ignore) {}
                    _connectionPool.releaseConnection(ldc);
                } 
            }
        }
    }

    private void actionNewUser(  ) {        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {                    
                // We cannot pass ldc as is to the entry editor :
                // we must update the search constraints in order the entry editor
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection editLdc = prepareReferralConnection(ldc);
                    
                String parentDn = Helper.getNodeInfoDN(node);
                boolean objectCreated = _entryEditor.createUser(parentDn, editLdc);                
                if (objectCreated) {    
                    LDAPEntry newObject = _entryEditor.getCreatedObject();                    
                    updateViewAfterAdd(newObject, node);
                    checkObjectAdded(newObject.getDN(), editLdc);
                }
                try {
                    editLdc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
                _connectionPool.releaseConnection(ldc);
            }
        }
    }

    private void actionNewGroup(  ) {    
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {                    
                // We cannot pass ldc as is to the entry editor :
                // we must update the search constraints in order the entry editor
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection editLdc = prepareReferralConnection(ldc);
                    
                String parentDn = Helper.getNodeInfoDN(node);                
                boolean objectCreated = _entryEditor.createGroup(parentDn, editLdc);                
                if (objectCreated) {                    
                    LDAPEntry newObject = _entryEditor.getCreatedObject();
                    updateViewAfterAdd(newObject, node);
                    checkObjectAdded(newObject.getDN(), editLdc);
                }
                try {
                    editLdc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
                _connectionPool.releaseConnection(ldc);
            }
        }
    }

    private void actionNewRole(  ) {        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {                    
                // We cannot pass ldc as is to the entry editor :
                // we must update the search constraints in order the entry editor
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection editLdc = prepareReferralConnection(ldc);
                /* We are editing a role: we need to not limit the number of results in the search constraints */
                LDAPSearchConstraints cons =
                    editLdc.getSearchConstraints();
                cons.setMaxResults(0);
                editLdc.setSearchConstraints(cons);

                String parentDn = Helper.getNodeInfoDN(node);                
                boolean objectCreated = _entryEditor.createRole(parentDn, editLdc);                
                if (objectCreated) {                    
                    LDAPEntry newObject = _entryEditor.getCreatedObject();
                    updateViewAfterAdd(newObject, node);
                    checkObjectAdded(newObject.getDN(), editLdc);
                }
                try {
                    editLdc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
                _connectionPool.releaseConnection(ldc);
            }
        }
    }

    private void actionNewCos(  ) {        
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {                    
                // We cannot pass ldc as is to the entry editor :
                // we must update the search constraints in order the entry editor
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection editLdc = prepareReferralConnection(ldc);
                    
                String parentDn = Helper.getNodeInfoDN(node);                
                boolean objectCreated = _entryEditor.createCos(parentDn, editLdc);                
                if (objectCreated) {                    
                    LDAPEntry newObject = _entryEditor.getCreatedObject();
                    updateViewAfterAdd(newObject, node);
                    checkObjectAdded(newObject.getDN(), editLdc);
                }
                try {
                    editLdc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
                _connectionPool.releaseConnection(ldc);
            }
        }
    }

    private void actionNewOrganizationalUnit(  ) {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {                    
                // We cannot pass ldc as is to the entry editor :
                // we must update the search constraints in order the entry editor
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection editLdc = prepareReferralConnection(ldc);
                    
                String parentDn = Helper.getNodeInfoDN(node);            
                boolean objectCreated = _entryEditor.createOrganizationalUnit(parentDn, editLdc);                
                if (objectCreated) {                    
                    LDAPEntry newObject = _entryEditor.getCreatedObject();
                    updateViewAfterAdd(newObject, node);
                    checkObjectAdded(newObject.getDN(), editLdc);
                }
                try {
                    editLdc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
                _connectionPool.releaseConnection(ldc);
            }
        }
    }

    private void actionCreateRootEntry( String suffixDn ) {
        LDAPConnection ldc = _framework.getServerObject().getServerInfo().getLDAPConnection();
        boolean objectCreated = _entryEditor.createRootObject(suffixDn, ldc);
        if (objectCreated) {
            String parentSuffix = _databaseConfig.getParentSuffix(suffixDn);
            if (parentSuffix != null) {
                if (!parentSuffix.equals("")) {
                    _controller.addSuffix(suffixDn, parentSuffix);
                } else {
                    _controller.addSuffix(suffixDn, null);
                }
            }
            checkObjectAdded(suffixDn, ldc);
        }
    }

    private void actionNewObject() {
        IBrowserNodeInfo node = getSelectedNodeInfo();
        if (node != null ) {
            LDAPConnection ldc = null;
            try {
                ldc = getConnectionForNode(node);
            } catch (LDAPException e) {
                displayConnectionError(e, node);
            }
            if (ldc != null) {                    
                // We cannot pass ldc as is to the entry editor :
                // we must update the search constraints in order the entry editor
                // is not returned a referral exception (in the case of node
                // is a referral).
                LDAPConnection editLdc = prepareReferralConnection(ldc);
                /* If we create a role we need to not limit the number of results in the
                   search constraints */
                LDAPSearchConstraints cons =
                    editLdc.getSearchConstraints();
                cons.setMaxResults(0);
                editLdc.setSearchConstraints(cons);

                String parentDn = Helper.getNodeInfoDN(node);                
                boolean objectCreated = _entryEditor.createObject(parentDn, editLdc);                
                if (objectCreated) {
                    LDAPEntry newObject = _entryEditor.getCreatedObject();
                    updateViewAfterAdd(newObject, node);
                    checkObjectAdded(newObject.getDN(), editLdc);
                }
                try {
                    editLdc.disconnect(); // prepareReferralConnection clones connection
                } catch (Exception ignore) {}
                _connectionPool.releaseConnection(ldc);
            } 
        }        
    }

        /**
          * Method called after editing an entry.  It assumes that the entry has been 
          * modified.  It updates consequently the tree (or the children list) and the
          * entry cache of the attribute controller.
          */
    private void updateAfterModify(IBrowserNodeInfo node, String editedObjectDn, LDAPConnection editLdc) {
        String oldDn = Helper.getNodeInfoDN(node);
        DN oldDN = new DN(oldDn);
        DN newDN = new DN(editedObjectDn);
        if (!oldDN.equals(newDN)) {                            
            /* We modified the rdn, have to check the new root menu items */
            checkObjectAdded(editedObjectDn, editLdc);
            if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                (_lastFocusComponent == _tree)) {
                                /* Update the tree */                                
                TreePath parentPath = _controller.notifyEntryDeleted(node);
                IBrowserNodeInfo parentNode = _controller.getNodeInfoFromPath(parentPath);
                TreePath newPath = _controller.notifyEntryAdded(parentNode,
                                                                editedObjectDn);
                _tree.setSelectionPath(newPath);
                _selectedDnLabel.setText(editedObjectDn);
            } else if (_lastFocusComponent == _list) {
                /* Update the list */
                _childrenController.notifyEntryDNChanged(node, editedObjectDn);

                TreePath path = getLastSelectedPath();
                if (path != null) {
                    IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                    if (treeNode != null) {
                        _controller.notifyChildEntryDeleted(treeNode, oldDn);
                        _controller.notifyChildEntryAdded(treeNode, editedObjectDn);
                        _selectedDnLabel.setText(editedObjectDn);
                    }
                }
            }
        } else {
            if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
                (_lastFocusComponent == _tree)) {
                            _controller.notifyEntryChanged(node);
            } else if (_lastFocusComponent == _list) {
                _childrenController.notifyEntryChanged(node);
                TreePath path = getLastSelectedPath();
                if (path != null) {
                    IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                    if (treeNode != null) {                                        
                        _controller.notifyChildEntryChanged(treeNode, editedObjectDn);
                    }
                }
            }
        }                                    
        /* Refresh the attribute table */
        _attributeController.reset();        
    }

        /**
          * This method is called after we add an entry or we modify an rdn.
          *
          * It checks if the 'New Root Entry' menu has to be udpated or not.
          * It checks if the user can see the added entry or not and if he/she can't
          * display an information dialog giving the reason
          *
          * @returns true if the DatabaseConfig has been updated. false otherwise.
          */
    private boolean checkObjectAdded(String newObjectDn, LDAPConnection ldc) {        
        boolean databaseConfigUpdated = false;
        /* We see if we added one of the root suffixes, and if so, tell to update the menu */                
        DN dn1 = new DN (newObjectDn);        
        Vector rootSuffixesWithoutEntry = _databaseConfig.getRootSuffixesWithoutEntry();
        for (int i=0; (i< rootSuffixesWithoutEntry.size()) && !databaseConfigUpdated; i++) {
            String currentSuffixDn = (String)rootSuffixesWithoutEntry.elementAt(i);
            DN dn2 = new DN(currentSuffixDn);
            if (dn1.equals(dn2)) {                
                _databaseConfig.setHasRootEntry(currentSuffixDn, true);                
                databaseConfigUpdated = true;
            }
        }

        if (databaseConfigUpdated) {
            /* Update the new root entry menu items */
            _menuController.recreateNewRootEntryMenus();
        }
        
        /* We try to see if the user can see it or not,
           in order to display a dialog telling him/her why he/she can't see it */        
        if (!_selectedPartitionView.equals(ContentMenuController.VIEW_ALL_PARTITIONS)) {
            LDAPSearchConstraints cons =
                (LDAPSearchConstraints)ldc.getSearchConstraints().clone();
            byte[] vals = null;
            try {
                vals = _selectedPartitionView.getBytes( "UTF8" );
            } catch ( UnsupportedEncodingException uEEx ) {
                Debug.println( "DSContentPage.checkObjectAdded() "+uEEx);                
            }
                    
            LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);
            cons.setServerControls(searchControl);
            cons.setMaxResults( 0 );
            try {                                        
                String[] attrs = {"dn"};
                LDAPEntry entry = ldc.read(newObjectDn, attrs, cons);
                if (entry == null) {                                                                                    
                    DSUtil.showInformationDialog(_framework,"add-entry-to-different-partition", (String)null);
                }
            } catch (LDAPException lde) {
                if (lde.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
                    DSUtil.showInformationDialog(_framework,"add-entry-to-different-partition", (String)null);
                } else {
                    Debug.println("DSContentPage.checkObjectAdded() " +lde);
                }                    
            }
        }
        return databaseConfigUpdated;
    } 

        /**
          * Method called after added an entry.  It updates properly the tree and (if visible)
          * the list of children.
          *
          * It assumes that the parentNode corresponds to the selection done by the user
          */
    private void updateViewAfterAdd(LDAPEntry newObject, IBrowserNodeInfo parentNode) {    
        String newObjectDn = newObject.getDN();
        if (!_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
            (_lastFocusComponent == _tree)) {
            if (_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
                _childrenController.notifyEntryAdded(newObjectDn);
                if (isContainerObject(newObject)) {
                    _controller.notifyEntryAdded(parentNode, newObjectDn);
                }
            } else {
                /* We show everything in the tree in this case */
                _controller.notifyEntryAdded(parentNode, newObjectDn);
            }
        } else if (_lastFocusComponent == _list) {                    
            _childrenController.notifyEntryChanged(parentNode);
            TreePath path = getLastSelectedPath();
            if (path != null) {
                IBrowserNodeInfo treeNode = _controller.getNodeInfoFromPath(path);
                if (treeNode != null) {
                    _controller.notifyChildEntryAdded(treeNode, newObjectDn);
                }
            }
        }
    }

    private boolean isContainerObject(LDAPEntry entry) {
        boolean isContainerObject = false;
        LDAPAttribute objectclassAttribute = entry.getAttribute("objectclass");
        if (objectclassAttribute!=null) {
            String value;
            Enumeration e = objectclassAttribute.getStringValues();
            while (e.hasMoreElements() && !isContainerObject) {
                value = (String)e.nextElement();
                for (int i=0; (i<CONTAINER_OBJECTCLASSES.length) && !isContainerObject; i++) {
                    if (value.equalsIgnoreCase(CONTAINER_OBJECTCLASSES[i])) {
                        isContainerObject = true;
                    }
                }
            }
        }
        return isContainerObject;
    }

    private void actionAuthenticate() {
        boolean status = getNewAuthentication();
        /* If everything went OK, refresh the tree... */
        if ( status ) {
            _controller.setLDAPConnection(_framework.getServerObject().getServerInfo().getLDAPConnection());            
            actionRefreshTree();
        } else {            
            Debug.println("ContentModel.actionAuthenticate(): could not reauthenticate");
        }
    }

    /**
     * Get the schema of the Directory instance
     *
     * @return A reference to a schema object.
     */
    public LDAPSchema getSchema() {
        return DSUtil.getSchema( _framework.getServerObject().getServerInfo() );
    }

    /**
     * Sets a reference to the schema of the Directory instance
     *
     * @param schema A reference to a schema object.
     */
    public void setSchema( LDAPSchema schema ) {
        DSUtil.setSchema( _framework.getServerObject().getServerInfo(), schema );
    }



    /* Fast method to detect if an entry has children or not */
    protected boolean entryHasChildren(LDAPEntry entry) {
        boolean hasChildren = false;        
        LDAPAttribute attr = entry.getAttribute("numsubordinates");
        if ( attr != null ) {
            Enumeration e = attr.getStringValues();
            if ( e.hasMoreElements() ) {
                String s = (String)e.nextElement();
                int count = Integer.parseInt( s );
                if ( count > 0 ) {
                    hasChildren = true;
                }
            }
        }

        /* Check if is one of the entries whose parent is a suffix...
         TO MODIFY Maybe we could cache the list of entries without suffix */
        if (!hasChildren) {
            Vector v = _databaseConfig.getSuffixesWithEntryList();            
            DN entryDN = new DN(entry.getDN());
            Enumeration e = v.elements();
            while (e.hasMoreElements() && !hasChildren) {
                DN currentDN = new DN((String)e.nextElement());
                if (currentDN.getParent().equals(entryDN)) {
                    hasChildren = true;
                }
            }
        }
        return hasChildren;
    }

    
    protected LDAPConnection getConnectionForNode(IBrowserNodeInfo node) throws LDAPException {    
        LDAPConnection ldc = _connectionPool.getConnection(node.getURL());
        Debug.println(9, "DSContentPage.getConnectionForNode: for node [" + node + "] getting "
                      + "connection [" + ldc + "] isConnected=" + ldc.isConnected()
                      + " isAuthenticated=" + ldc.isAuthenticated());
        return ldc;
    }

    /**
     * Prepare connection to follow referrals. The return connection is a clone of
     * the original one and should be closed after use
     */
    protected LDAPConnection prepareReferralConnection(LDAPConnection ldc) {    
        Debug.println(9, "DSContentPage.prepareReferralConnection: original connection ["
                      + ldc + "] isConnected=" + ldc.isConnected()
                      + " isAuthenticated=" + ldc.isAuthenticated());
        LDAPConnection result =(LDAPConnection)ldc.clone();
        LDAPSearchConstraints lsc = (LDAPSearchConstraints)ldc.getSearchConstraints();
        
        if (!getFollowReferrals()) {
            // disable referrals
            lsc.setServerControls(_manageDSAITControl);
            Debug.println(9, "DSContentPage.prepareReferralConnection: no follow referrals connection ["
                          + result + "] isConnected=" + result.isConnected()
                          + " isAuthenticated=" + result.isAuthenticated());
        }
        else { // follow referrals using the current credentials
            Debug.println(9, "DSContentPage.prepareReferralConnection: follow referrals connection ["
                          + result + "] isConnected=" + result.isConnected()
                          + " isAuthenticated=" + result.isAuthenticated());
            final String dn = ldc.getAuthenticationDN();
            final String pw = ldc.getAuthenticationPassword();
            lsc.setRebindProc(new LDAPRebind() {
                public LDAPRebindAuth getRebindAuthentication(String host, int port) {
                    return new LDAPRebindAuth(dn, pw);
                }
            });
            lsc.setReferrals(true);            
        }

        result.setSearchConstraints(lsc);

        return result;
    }

    
    protected IBrowserNodeInfo getSelectedNodeInfo() {
        IBrowserNodeInfo node = null;
        /* If the layout is not the children layout, or if nothing selected in the children panel,
           return anyway the node of the tree as sometimes the focus gets lost */
        if ((_lastFocusComponent == _tree) ||        
        !_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT) ||
        (getLastSelectedIndex() < 0)) {
            TreePath path = getLastSelectedPath();
            if (path != null) {
                node = _controller.getNodeInfoFromPath(path);
            }
        } else if(_layout.equals(ContentMenuController.NODE_LEAF_LAYOUT)) {
            int index = getLastSelectedIndex();
            if (index >= 0) {
                node = _childrenController.getNodeInfoFromIndex(index);
            }
        }
        return node;
    }

    protected TreePath getLastSelectedPath() {
        return _tree.getSelectionPath();
    }    

    protected int getLastSelectedIndex() {
        return _list.getSelectedIndex();        
    }

    private void setBusyCursor(boolean state) {        
        int cursorCode = state? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR;
        Cursor cursor = Cursor.getPredefinedCursor(cursorCode);
        setCursor(this, cursor);        
    }

    private void setCursor(Container container, Cursor cursor) {
        Component[] components = container.getComponents();
        if (components != null) {
            for (int i=0; i<components.length; i++) {
                if (components[i] instanceof Container) {
                    setCursor((Container)components[i], cursor);
                }
            }
        }
        container.setCursor(cursor);
    }

    private void displayConnectionError(LDAPException e, IBrowserNodeInfo node) {
        String[] args = {
            findDisplayNameFromNode(node),
            Helper.getLDAPErrorMessage(e)};
        
        DSUtil.showErrorDialog( _framework,
                                "getting-connection-error-title",
                                "getting-connection-error-msg",
                                args,
                                "browser" );
    }   

	DSResourceModel _resourceModel;

    DSFramework _framework;
    DatabaseConfig _databaseConfig;
    EntryEditor _entryEditor;
    LDAPConnectionPool _connectionPool;
    IconPool _iconPool;
    BrowserController _controller;
    ContentMenuController _menuController;
    protected JTree _tree;
    protected JScrollPane _treePanel;
    Component _lastFocusComponent;

    protected JList _list;
    protected JScrollPane _childrenPanel;
    Color _listDefaultColor;
    Color _listDisabledColor;

    ChildrenController _childrenController;

    JPanel _attributePanel;
    AttributeController _attributeController;
    
    protected JPopupMenu _contextMenu = new JPopupMenu();
    JLabel _selectedDnLabel;
    JLabel _statusLabel;    
    JSplitPane _splitPanel;
    JPanel _displayedPanel;

    boolean _showRemoteInformationDialog;
    boolean _showDisplayedChildrenLimitExceededDialog;
    boolean _showSortedChildrenLimitExceededDialog;    
        
    private String _selectedPartitionView = ContentMenuController.VIEW_ALL_PARTITIONS;

    static ResourceSet _resource = DSUtil._resource;    

    static final String NO_SELECTED_DN = "  "; // A blanc space to forbid resizing

    static final String[] CONTAINER_OBJECTCLASSES = {
        "organization",
        "organizationalUnit",
        "netscapeServer",
        "netscapeResource",
        "domain"};

    static final String DEFAULT_NEW =
    _resource.getString("EntryObject", "defaultnew");
        
    static final String SEARCH_OID = "2.16.840.1.113730.3.4.14";

   
    static final String SHOW_REMOTE_INFORMATION_DIALOG = "SHOW_REMOTE_INFORMATION_DIALOG";
    static final String SHOW_DISPLAYED_CHILDREN_LIMIT_EXCEEDED = "SHOW_DISPLAYED_CHILDREN_LIMIT_EXCEEDED";
    static final String SHOW_SORTED_CHILDREN_LIMIT_EXCEEDED = "SHOW_SORTED_CHILDREN_LIMIT_EXCEEDED";    

    static final String SORT_PREFERENCES = "SORT_PREFERENCES";
    static final String FOLLOW_REFERRALS_PREFERENCES = "FOLLOW_REFERRALS_PREFERENCES";
    static final String DISPLAY_PREFERENCES = "DISPLAY_PREFERENCES";
    static final String LAYOUT_PREFERENCES = "LAYOUT_PREFERENCES";

    static final int NO_VLV_SORTED_LIMIT = 300;
    static final int DISPLAYED_CHILDREN_LIMIT = 1000; // 2K is the server side default limit

    Preferences _preferences = PreferenceManager.getPreferenceManager(
                                                                      Framework.IDENTIFIER, 
                                                                      Framework.VERSION).getPreferences(DSPreferencesTab.PREFERENCES_GROUP);

    public static  LDAPControl _manageDSAITControl = new LDAPControl( LDAPControl.MANAGEDSAIT, true, null );

    private Hashtable _vlvCache = new Hashtable();
    private Hashtable _activationCache = new Hashtable();    
    
    private String _layout = ContentMenuController.NODE_LEAF_LAYOUT;        
    private int _display;

    private boolean _isInitialized = false;
    private boolean _isPageSelected = false;
    private boolean _refreshUponSelect = false;
    
    private Clipboard _clipboard = new Clipboard();

        /* Objects interested in authentication changes */
    private Vector _authListeners = new Vector();

    /* List of all the attributesfor the property editor */
    private String[] _allAttrs = null;
}


/* TO MODIFY / COMPLETE: by default we follow the referrals when 
   we make searches.
   
   We should use the substitute of DSEntryList / DSEntryObject */
class SearchUG implements IRPCallBack, MouseListener {

    SearchUG( JFrame frame, IconPool iconPool, LDAPConnection ldc, EntryEditor entryEditor ) {        
        _frame = frame;        
        _iconPool = iconPool;
        _ldc = ldc;
        _entryEditor = entryEditor;
        Debug.println(9, "DSContentPage.SearchUG: _ldc [" + _ldc + "] isConnected=" + ldc.isConnected() +
                      " isAuthenticated=" + ldc.isAuthenticated());
    }    
    
    /**
     * Callback from User/Group search dialog.
     *
     * @param vResult Selected items.
     */
    public void getResults(Vector vResult) {        
        Enumeration e = vResult.elements();
        Vector v = new Vector();
        while ( e.hasMoreElements() ) {
            Object o = e.nextElement();
            if ((o != null) && (o instanceof LDAPEntry)) {
                LDAPEntry entry = (LDAPEntry)o;
                Debug.println( "DSContentPage.getResults: selected " +
                               entry );            
                v.addElement( createNode(entry) );
            }
        }
        if (v.size() > 0) {            
            _listModel = new CustomListModel();
            e = v.elements();
            while (e.hasMoreElements()) {
                _listModel.addElement(e.nextElement());
            }
            _list = new JList(_listModel);
            _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            _list.addMouseListener(this);
            _list.setVisibleRowCount(15);                        
            JScrollPane pane = new JScrollPane(_list);        
            pane.setBorder(UITools.createLoweredBorder());
            _list.setCellRenderer(new CustomCellRenderer());
            String title = DSUtil._resource.getString( "searchResults", "title" );
            final ResultsDialog dlg = new ResultsDialog( _frame,
                                                         title,
                                                         AbstractDialog.CLOSE,
                                                         pane );            
            dlg.setFocusComponent( _list );
            dlg.pack();        
            if (dlg.getWidth() > _frame.getWidth()) {
                dlg.setSize(_frame.getWidth(), dlg.getHeight());
            }
            Debug.println(9, "DSContentPage.SearchUG.getResults: _ldc [" + _ldc + "] isConnected=" + _ldc.isConnected() +
                          " isAuthenticated=" + _ldc.isAuthenticated());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dlg.showModal();        
                    /*
                      We need to close the connection that was opened in actionSearchUG and passed
                      to the constructor of SearchUG
                    */
                    try {
                        Debug.println(9, "DSContentPage.SearchUG.getResults: before disconnect connection ["
                                      + _ldc + "] isConnected=" + _ldc.isConnected()
                                      + " isAuthenticated=" + _ldc.isAuthenticated());
                        _ldc.disconnect(); // prepareReferralConnection clones connection
                        Debug.println(9, "DSContentPage.SearchUG.getResults: after disconnect connection ["
                                      + _ldc + "] isConnected=" + _ldc.isConnected()
                                      + " isAuthenticated=" + _ldc.isAuthenticated());
                    } catch (Exception ignore) {
                        Debug.println(9, "DSContentPage.SearchUG.getResults: caught exception ["
                                      + ignore + "] disconnecting connection ["
                                      + _ldc + "] isConnected=" + _ldc.isConnected()
                                      + " isAuthenticated=" + _ldc.isAuthenticated());
                    }
                }
            }); 
        }
    }

    /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mousePressed(MouseEvent e) {
    }

   /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mouseEntered(MouseEvent e) {
    }

   /**
     * MouseListener implementation
     * ====================================
     *     
     */
    public void mouseExited(MouseEvent e) {
    }

   /**
     * MouseListener implementation
     * ====================================
     *     
     */
     public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() == 2) {
             int clickedIndex = _list.locationToIndex(e.getPoint());
             if ((clickedIndex == _list.getSelectedIndex()) &&
                 (_listModel.getSize() > 0)){
                 CustomNode node = (CustomNode)_list.getSelectedValue();
                 boolean objectModified =  _entryEditor.editObject(node.getDN(), _ldc, false);                 
                 if (objectModified) {                     
                     String editedObjectDn = _entryEditor.getEditedObjectDn();
                     node.setDN(editedObjectDn);
                     node.setDisplayName(editedObjectDn);
                     _listModel.fireContentsChanged(_listModel, clickedIndex, clickedIndex);
                 }
             }     
         }
     }        
    /* This is a very lightweight version of the method ChildrenController.updateNodeRendering().
       In this one we only take into account the value of the objectclass to use the proper icon.
       Another difference is that the whole DN is used as display name for the nodes.
       */
    private CustomNode createNode(LDAPEntry entry) {
        CustomNode node = new CustomNode(entry.getDN());

        /* Get the objectclass */
        LDAPAttribute objectClass = entry.getAttribute("objectclass");
        
        Icon icon = _iconPool.getIcon(objectClass, 0);
        node.setIcon(icon);
        node.setDisplayName(node.getDN());        

        return node;
    }

    JList _list;
    CustomListModel _listModel;

    JFrame _frame;
    IconPool _iconPool;
    LDAPConnection _ldc;
    EntryEditor _entryEditor;
}

/** 
 * A lightweight version of the ChildrenCellRenderer of ChildrenController
 */
class CustomCellRenderer implements ListCellRenderer {
    JLabel _label;
    Border _noFocusBorder;
    Border _focusBorder;
    
    Icon _currentIcon;    
    boolean _currentSelected;
    boolean _currentFocus;

    public Component getListCellRendererComponent(JList list, 
                                                  Object value, 
                                                  int index, 
                                                  boolean isSelected,
                                                  boolean hasFocus) {
        if (_label == null) {
            _focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
            _noFocusBorder = new EmptyBorder(1, 1, 1, 1);
            
            _label = new JLabel();        
            _label.setOpaque(true);
            
            // To force update of border and background the first time the label is created
            _currentFocus = !hasFocus;
            _currentSelected = !isSelected;
        }

        CustomNode node = (CustomNode)value;
                
        // Set the foreground and background colors: change them only if necessary
        if (_currentSelected != isSelected) {
            _currentSelected = isSelected;
            if (isSelected) {
                _label.setBackground(list.getSelectionBackground());
                _label.setForeground(list.getSelectionForeground());
            }
            else {
                _label.setBackground(list.getBackground());
                _label.setForeground(list.getForeground());
            }
        }

        // Update the border if necessary        
        if (hasFocus != _currentFocus) {
            _currentFocus = hasFocus;
            Border borderToUse = hasFocus ? _focusBorder : _noFocusBorder;            
            _label.setBorder(borderToUse);
        }
        
        // Set the icon and text
        Icon newIcon = node.getIcon();
        
        if (newIcon != _currentIcon) {
            _currentIcon = newIcon;
            _label.setIcon(_currentIcon);
        }    
        
        _label.setText(node.getDisplayName());
        
        return _label;
    }
}

/**
  * Class created just to be able to call the method fireContentsChanged
  * from outside the class
  */
class CustomListModel extends DefaultListModel {
    public void fireContentsChanged(Object source,
                                    int index0,
                                    int index1) {
        super.fireContentsChanged(source, index0, index1);
    }
}


/**
  * Lightweight version of BasicNode
  */
class CustomNode {
    String _dn;
    Icon _icon;
    String _displayName;
    
    CustomNode(String dn) {
        _dn = dn;
    }

    public String getDN() {
        return _dn;
    }

    public void setDN(String dn) {
        _dn = dn;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    public Icon getIcon() {
        return _icon;
    }
    
    public void setIcon(Icon icon) {
        _icon = icon;
    }
}

class ResultsDialog extends AbstractDialog {                
    ResultsDialog( Frame parentFrame, String title, int buttons,
                   Component component ) {
        super( parentFrame, title, buttons );
        setComponent(component);
    }        
}



/**
 * Class which can run as a thread under a progress dialog.  Used to activate a set of entries
 */
class Activator implements ActionListener, Runnable {
    Activator(JFrame frame,
              String dn,
              LDAPConnection ldc) {
        _frame = frame;
        _dn = dn;
        _ldc = ldc;
    }
    
    public void execute() {
        createActivateProgressDialog();
        Thread thread = new Thread(Activator.this);
        thread.start();
        _dlg.packAndShow();
    }

    protected void createActivateProgressDialog() {
        String title = DSUtil._resource.getString("accountinactivation-activate", "title");
        _dlg = new GenericProgressDialog(_frame, 
                                         true, 
                                         GenericProgressDialog.TEXT_FIELD_AND_CANCEL_BUTTON_OPTION, 
                                         title,
                                         null,
                                         null);
        _dlg.setTextInTextAreaLabel(DSUtil._resource.getString("accountinactivation-activate", "rejected-objects"));
        _dlg.setTextAreaRows(5);
        _dlg.setTextAreaColumns(30);
        _dlg.setLabelColumns(50);
        _dlg.setLocationRelativeTo(_frame);
        _dlg.pack();
        
        _taskCancelled = false;
        _dlg.addActionListener(this);        
        _dlg.enableButtons(true);        
    }


    public void run() {        
        /* We make sure we read all the attributes of the entry. 
           We need this because when we activate/inactivate we need the values of nsrole... */
        String[] attrs = {"nsrole", "nsroledn", "objectclass", "nsAccountLock"};        
        
        try {
            LDAPEntry entry = _ldc.read(_dn, attrs);
            
            if (entry != null) {                
                AccountInactivation account = new AccountInactivation(entry);
                int result = account.activate(_ldc);
                String abreviatedDN = DSUtil.abreviateString(entry.getDN(), 45);
                String[] arg = {abreviatedDN};
                _dlg.setTextInLabel(DSUtil._resource.getString("accountinactivation-activate", "entry", arg));
                switch (result) {            
                case AccountInactivation.INACTIVATED_THROUGH_UNKNOWN_MECHANICS:
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-activate", "inactivatedthroughunknownmechanics", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.ERROR:
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-activate", "unknown-error", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.CAN_NOT_ACTIVATE:
                    Vector vLockingRoles = null;
                    try {
                        vLockingRoles = account.getLockingRoles(_ldc);
                    } catch (LDAPException ex) {
                        Debug.println("DSContentPage.actionActivate() "+ex);                    
                    }
                    if (vLockingRoles != null) {
                        String sLockingRoles = "";
                        for (int j=0; j < vLockingRoles.size(); j++) {
                            sLockingRoles = sLockingRoles + 
                                "'" + 
                                DSUtil.abreviateString((String)vLockingRoles.elementAt(j), 45) +
                                "'"+ 
                                "\n";
                        }
                        String[] attributes = {abreviatedDN, sLockingRoles};
                        _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                     "accountinactivation-activate", "cannotactivate", attributes)+"\n");
                    } else {
                        _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                      "accountinactivation-activate", "cannotactivate-lockingrolesnotfound", abreviatedDN)+"\n");
                    }                    
                    break;
                case AccountInactivation.ROOT_OR_CONFIG_ENTRY:
                    /* Assume we could not find the suffixes because the entry is in the config, is cn=config, is cn=schema, cn=monitor... */
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-activate", "rootorconfigentry", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.SUCCESS:                    
                    break;                    
                }                
            }
        } catch (LDAPException e) {
            String abreviatedDN = DSUtil.abreviateString(_dn, 45);
            String ldapError =  Helper.getLDAPErrorMessage(e);
            String[] arg = {_dn, ldapError};
            _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                         "accountinactivation-activate", "errorreadingentry", arg)+"\n");                        
        }        
        
        if (_taskCancelled) {
            _dlg.closeCallBack();
        } else {
            _dlg.setTextInLabel(DSUtil._resource.getString("accountinactivation-activate", "finished"));
            _dlg.waitForClose();
        }         
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GenericProgressDialog.CANCEL)) {
            _taskCancelled = true;
            _dlg.disableCancelButton();
        } else if (e.getActionCommand().equals(GenericProgressDialog.CLOSE)) {
            _dlg.closeCallBack();
        }
    }

    boolean _taskCancelled = false;
    JFrame _frame;
    GenericProgressDialog _dlg;
    String _dn;
    LDAPConnection _ldc;
}


/**
 * Class which can run as a thread under a progress dialog.  Used to activate a set of entries
 */
class Inactivator implements ActionListener, Runnable {
    Inactivator(JFrame frame,
              String dn,
              LDAPConnection ldc) {
        _frame = frame;
        _dn = dn;
        _ldc = ldc;
    }
    
    public void execute() {
        createInactivateProgressDialog();
        Thread thread = new Thread(Inactivator.this);
        thread.start();
        _dlg.packAndShow();
    }

    protected void createInactivateProgressDialog() {
        String title = DSUtil._resource.getString("accountinactivation-inactivate", "title");
        _dlg = new GenericProgressDialog(_frame, 
                                         true, 
                                         GenericProgressDialog.TEXT_FIELD_AND_CANCEL_BUTTON_OPTION, 
                                         title,
                                         null,
                                         null);
        _dlg.setTextInTextAreaLabel(DSUtil._resource.getString("accountinactivation-inactivate", "rejected-objects"));
        _dlg.setTextAreaRows(5);
        _dlg.setTextAreaColumns(30);
        _dlg.setLabelColumns(50);
        _dlg.setLocationRelativeTo(_frame);
        _dlg.pack();

        _taskCancelled = false;
        _dlg.addActionListener(this);        
        _dlg.enableButtons(true);        
    }

    public void run() {
        /* We make sure we read all the attributes of the entry. 
           We need this because when we activate/inactivate we need the values of nsrole... */
        String[] attrs = {"nsrole", "nsroledn", "objectclass", "nsAccountLock"};        
        
        try {
            LDAPEntry entry = _ldc.read(_dn, attrs);
            
            if (entry != null) {                
                AccountInactivation account = new AccountInactivation(entry);
                int result = account.inactivate(_ldc);
                _lockingInfrastructureDNs = account.getLockingInfrastructureDNs();
                String abreviatedDN = DSUtil.abreviateString(entry.getDN(), 45);
                String[] arg = {abreviatedDN};
                _dlg.setTextInLabel(DSUtil._resource.getString("accountinactivation-inactivate", "entry", arg));
                switch (result) {              
                case AccountInactivation.INACTIVATED_THROUGH_UNKNOWN_MECHANICS:
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-inactivate", "inactivatedthroughunknownmechanics", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.ERROR:
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-inactivate", "unknown-error", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.CAN_NOT_INACTIVATE:
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-inactivate", "cannotinactivate", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.ROOT_OR_CONFIG_ENTRY:
                    /* Assume we could not find the suffixes because the entry is in the config, is cn=config, is cn=schema, cn=monitor... */
                    _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                                 "accountinactivation-inactivate", "rootorconfigentry", abreviatedDN)+"\n");                    
                    break;
                case AccountInactivation.SUCCESS:                                    
                    break;
                }
            }
        } catch (LDAPException e) {
            String abreviatedDN = DSUtil.abreviateString(_dn, 45);
            String ldapError =  Helper.getLDAPErrorMessage(e);
            String[] arg = {_dn, ldapError};
            _dlg.appendTextToTextArea(DSUtil._resource.getString(
                                                         "accountinactivation-inactivate", "errorreadingentry", arg)+"\n");
        }
        
        
        if (_taskCancelled) {
            _dlg.closeCallBack();
        } else {
            _dlg.setTextInLabel(DSUtil._resource.getString("accountinactivation-inactivate", "finished"));
            _dlg.waitForClose();
        }        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GenericProgressDialog.CANCEL)) {
            _taskCancelled = true;
            _dlg.disableCancelButton();
        } else if (e.getActionCommand().equals(GenericProgressDialog.CLOSE)) {
            _dlg.closeCallBack();
        }
    }

    /**
      * Method that returns the list of DNs of the entries that have been added.
      * This entries are the roles and the Cos that create the locking structure
      */
    public Vector getLockingInfrastructureDNs() {
        return _lockingInfrastructureDNs;
    }

    boolean _taskCancelled = false;
    JFrame _frame;
    GenericProgressDialog _dlg;
    String _dn;
    LDAPConnection _ldc;
    Vector _lockingInfrastructureDNs;
}

/**
 * Callback for the UserGroup editor, when the Advanced button
 * is hit.
 */
class UserGroupAdapter implements IResEditorAdvancedOpt {
    UserGroupAdapter( IAdapterInfo page, String buttonText, LDAPEntry entry, LDAPConnection ldc ) {
        _buttonText = buttonText;
        _page = page;
        _entry = entry;
        _ldc = ldc;
    }

    UserGroupAdapter( IAdapterInfo page, String buttonText, String parentDn, LDAPConnection ldc ) {
        _buttonText = buttonText;
        _page = page;
        _parentDn = parentDn;
        _ldc = ldc;
    }
    public String getButtonText() { return _buttonText; }

    public boolean run( ConsoleInfo info,
                        ResourcePageObservable observableentry ) {
        boolean status;
        LDAPEntry entry;
        DSEntryDialog dlg;
        if ( observableentry.isNewUser() ) {
            /* Preliminary DN - null from Kingpin */
            String dn = observableentry.getDN();
            Debug.println( "UserGroupAdapter.run: DN = " +
                           dn );
            /* Get all attributes set by the custom editor */
            LDAPAttributeSet set = new LDAPAttributeSet();
            Enumeration en = observableentry.getAttributesList();
            if ( en != null ) {
                while( en.hasMoreElements() ) {
                    String attrName = (String)en.nextElement();
                    Vector v = observableentry.get( attrName );
                    String[] values = new String[v.size()];
                    v.copyInto( values );
                    set.add( new LDAPAttribute( attrName, values ) );
                    v.removeAllElements();
                }
            }
            /* Get all required attributes for the object classes */
            String key = "cn";
            Vector v = observableentry.get( "objectclass" );
            LDAPSchema schema = _page.getSchema();
            en = v.elements();
            while( en.hasMoreElements() ) {
                String ocName = (String)en.nextElement();                    
                Enumeration e = DSSchemaHelper.allRequiredAttributes(ocName, schema);
                while( e.hasMoreElements() ) {
                    String name = (String)e.nextElement();
                    if ( set.getAttribute( name ) == null ) {
                        set.add( new LDAPAttribute( name, "" ) );
                    }
                }
                if ( ocName.equalsIgnoreCase( "person" ) ) {
                    key = ResourceEditor.getUserRDNComponent();
                } else if ( ocName.equalsIgnoreCase( "groupofuniquenames" ) ) {
                    key = ResourceEditor.getGroupRDNComponent();
                } else if ( ocName.equalsIgnoreCase("organizationalunit" ) ) {
                    key = "ou";
                }
            }
            Debug.println( "UserGroupAdapter.run: " +
                           "key = " + key );
            if ( dn == null ) {
                /* Get rdn */
                String rdn = getFirstStringValue( set, key );
                if ( (rdn == null) || (rdn.length() < 1) ) {
                    rdn = DSContentPage.DEFAULT_NEW;
                    //                        set.remove( key );
                    Debug.println( "UserGroupAdapter." +
                                   "run: Adding " + key + "=" + rdn );
                    //                        set.add( new LDAPAttribute( key, rdn ) );
                }
                dn = key + "=" + rdn + ", " + _parentDn;
                Debug.println( "UserGroupAdapter.run: " +
                               "Setting dn = " + dn );
            }
            
            entry = new LDAPEntry( dn, set );
            
            dlg = _page.editGeneric(false, false, entry, _ldc );            

        } else {
            /* Get all attributes set by the custom editor */
            LDAPAttributeSet set = new LDAPAttributeSet();
            Enumeration en = observableentry.getAttributesList();
            if ( en != null ) {
                while( en.hasMoreElements() ) {
                    String attrName = (String)en.nextElement();
                    Vector v = observableentry.get( attrName );
                    String[] values = new String[v.size()];
                    v.copyInto( values );
                    set.add( new LDAPAttribute( attrName, values ) );
                    v.removeAllElements();
                }
            }

            String dn = observableentry.getDN();
            entry = new LDAPEntry( dn, set );
            dlg =  _page.editGeneric(false, false, entry, _ldc );
        }

        if ( dlg != null ) {
            /* Notify the observable from the kingpin editor */            
            LDAPModificationSet mods = dlg.getChanges();
            String[] rdnAttrs = dlg.getNamingAttributes();

            if ((mods != null) && (mods.size() > 0)) {            
                updateObservable( observableentry, mods );
            }    

            // The naming attribute may have changed if any
            // objectclasses were deleted.  Update the
            // observable entry with the new naming attribute.
            // We don't need to worry about multiple RDN
            // attributes here since the user has no chance
            // to select multiple naming attributes when using
            // the UG adapter.
            if ((rdnAttrs != null) && (rdnAttrs[0] != null)) {
                observableentry.setIndexAttribute(rdnAttrs[0]);
            }
        }
        return (dlg != null);
    }

    private String getFirstStringValue( LDAPAttributeSet set,
                                        String name ) {
        LDAPAttribute attr = set.getAttribute( name );
        if ( attr != null ) {
            Enumeration en = attr.getStringValues();
            if ( (en != null) && en.hasMoreElements() ) {
                return ((String)en.nextElement()).trim();
            }
        }
        return null;
    }
    private void updateObservable(
                                  ResourcePageObservable observableentry,
                                  LDAPModificationSet mods ) {
        
        Vector v = new Vector();
        for( int i = 0; i < mods.size(); i++ ) {
            LDAPModification mod = mods.elementAt( i );
            LDAPAttribute attr = mod.getAttribute();
            
            Enumeration en = attr.getByteValues();
            if ( en == null ) {                    
                en = attr.getStringValues();    
            }
            while( en.hasMoreElements() ) {
                v.addElement( en.nextElement() );
            }
            switch( mod.getOp() ) {
            case LDAPModification.ADD: {
                observableentry.add( attr.getName(), v );
                break;
            }
            case LDAPModification.REPLACE: {
                observableentry.replace( attr.getName(), v );
                break;
            }
            case LDAPModification.DELETE: {
                // Kingpin doesn't support this yet...
                //                    observableentry.delete( attr.getName(), v );
                // So try for strings...
                Enumeration vals = attr.getStringValues();
                if ( vals != null ) {        
                    if (vals.hasMoreElements()) {
                        observableentry.delete(
                                               attr.getName(),
                                               (String)vals.nextElement() );
                    } else {
                        observableentry.delete( attr.getName() );
                    }                                            
                    while( vals.hasMoreElements() ) {
                        observableentry.delete(
                                               attr.getName(),
                                               (String)vals.nextElement() );
                    }
                } else {
                    // Not strings, so just delete all values
                    observableentry.delete( attr.getName() );
                }
                break;
            }
            }
            v.removeAllElements();
        }
    }
    private String _buttonText = null;
    IAdapterInfo _page;
    LDAPEntry _entry;
    String _parentDn;
    LDAPConnection _ldc;
}

class DeleteConfirmationDisplayer implements Runnable {
    public DeleteConfirmationDisplayer(DSFramework framework, 
                                       IBrowserNodeInfo node, 
                                       boolean many,
                                       boolean followReferrals) {                
        _frame = framework;
        
        _args = new String[1];
        _args[0] = DSUtil.abreviateString(
                                          Helper.getNodeInfoDN(node),
                                          30);        
            
        if (node.isRemote()) {
            if (many) {
                _msg = "confirmDeleteReferredTree";
            } else {
                _msg = "confirmDeleteReferredObject";
            }            
        } else {
            if (many) {
                _msg = "confirmDeleteTree";
            } else {
                _msg = "confirmDeleteObject";
            }
        }                
    }
    public void run() {
        _doDelete = false;
        
        int option = DSUtil.showConfirmationDialog(
                                                   _frame,
                                                   _msg,
                                                   _args,
                                                   "browser");
        _doDelete = option == JOptionPane.YES_OPTION;        
    }

    public boolean doDelete() {
        return _doDelete;
    }

    String _msg;
    String[] _args;
    boolean _doDelete;    
    JFrame _frame;
}

class DialogDisplayerAndDisposer implements Runnable {
    DialogDisplayerAndDisposer(JDialog dlg, JFrame frame) {
        _dlg = dlg;
        _frame = frame;
    }
    public void run() {
        _dlg.show();
        //ModalDialogUtil.disposeAndRaise(_dlg, _frame);

    }
    JDialog _dlg;
    JFrame _frame;
}

class Helper {    
    static String getLDAPErrorMessage(LDAPException e) {
        String ldapError;
        String ldapMessage;

        ldapError =  e.errorCodeToString();
        ldapMessage = e.getLDAPErrorMessage();
        if ((ldapMessage != null) &&
            (ldapMessage.length() > 0)) {
            ldapError = ldapError + ". "+ldapMessage;
        }
        return ldapError;
    }

    static String getNodeInfoDN(IBrowserNodeInfo node) {
        String dn = null;
        LDAPUrl url = node.getURL();
        if (url != null) {            
            dn = url.getDN();
        } 
        return dn;
    }       

    /**
     * Check if parentDN is a parent entry for a childDN. 
     */
    static boolean isChildOf(String childDN, String parentDN) {
        DN child  = new DN(childDN);
        DN parent = new DN(parentDN);
        return child.getParent().equals(parent);
    }
}



class Copier implements Runnable, ActionListener {
    boolean _isCancelled = false;
    
    JFrame _frame;
    GenericProgressDialog _progressDialog;                    
    
    int _numberCopiedObjects = 0;            
    
    String _startDn;

    LDAPConnection _ldc;    
    String[] COPY_ATTRS = { "*", "numsubordinates" }; // To be in sync with the CUT_ATTRS

    Clipboard _clipboard;
    
    final String CHILDREN_FILTER = "|(objectclass=*)(objectclass=ldapsubentry)";
    
    /**
      * The connection needs to have the _manageDSAITControl */
    public Copier(LDAPConnection ldc, 
                  String startDn, 
                  JFrame frame,
                  Clipboard clipboard) {
        _frame = frame;
        _ldc = ldc;
        _startDn = startDn;
        _clipboard = clipboard;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GenericProgressDialog.CANCEL)) {
            _isCancelled = true;
            _progressDialog.disableCancelButton();
        } else if (e.getActionCommand().equals(GenericProgressDialog.CLOSE)) {
            _progressDialog.closeCallBack();
        }
    }
    
    public void execute() {        
        if (_startDn.equals("")) {
            /* If the user chose to copy the root entry we
               display an error message */            
            DSUtil.showErrorDialog( _frame,
                                    "copy-root-error-title", 
                                    "copy-root-error-msg", 
                                    (String[]) null, 
                                    "browser" );        
        } else {
            createCopyProgressDialog();
            Thread thread = new Thread(Copier.this);
            thread.start();
            _progressDialog.packAndShow();
            
        }
    }

    public void run() {
        try {            
            copyTree(_ldc, _startDn);                    
            updateProgressDialogForEnd();                                        
        } catch (LDAPException lde) {
            /* If something went wrong display an error message and enable
               the close button of the progress dialog */
            String[] arg = {Helper.getLDAPErrorMessage(lde)};
            String msg = DSUtil._resource.getString("browser", 
                                                    "copying-error-label",
                                                    arg);
            _progressDialog.setTextInLabel(msg);                                        
            _progressDialog.waitForClose();            
        }        
    }

    private void copyTree (LDAPConnection ldc, String baseDn) throws LDAPException {        
        boolean copyOk = true;            
        LDAPEntry baseEntry = ldc.read(baseDn,
                                       COPY_ATTRS);
        recursiveCopy(baseEntry, ldc);                
    }        
    
    private void recursiveCopy(LDAPEntry baseEntry, LDAPConnection ldc) throws LDAPException {

        if (baseEntry == null) {
            return;
        }

        Vector containerChildren = new Vector();            
        String baseDN = baseEntry.getDN();

        if (DSContentModel.entryHasChildren(baseEntry)) {
            LDAPSearchResults search_results = ldc.search(baseDN,
                                                          ldc.SCOPE_ONE,
                                                          CHILDREN_FILTER,
                                                          COPY_ATTRS,
                                                          false);
            /* Copy each child found, recursively */
            while ( search_results.hasMoreElements() &&
                    !_isCancelled) {
                /* Get the next child */
                Object nextElement = search_results.nextElement();
                LDAPEntry entry = null;
                if (nextElement instanceof LDAPEntry) {
                    entry = (LDAPEntry)nextElement;

                    // Skip children returned by views
                    if (!Helper.isChildOf(entry.getDN(), baseDN)) {
                        continue;
                    }

                    if ( DSContentModel.entryHasChildren( entry )) {
                        containerChildren.addElement( entry );
                    } else {
                        String dn = entry.getDN();                    
                        if ((_numberCopiedObjects % 5) == 0) {
                            String[] arg = {DSUtil.abreviateString(dn, 45)};
                            String msg = DSUtil._resource.getString("browser",
                                                                    "copying-object-label",
                                                                    arg);
                            _progressDialog.setTextInLabel(msg);
                        }
                        _numberCopiedObjects++;
                        _clipboard.putEntry(entry);
                    }
                } else if (nextElement instanceof LDAPException) {
                    LDAPException ex = (LDAPException)nextElement;
                    ldc.abandon(search_results);
                    /* The entry has been deleted */
                    if (ex.getLDAPResultCode() == 32) {
                        containerChildren.clear();
                    } else {
                        /* Don't know what to do */
                        throw ex;
                    }
                }
            }

            if (_isCancelled) {
                ldc.abandon(search_results);
            } else {
                /* Recursively copy container entries bellow this entry...*/
                for (int i=0; 
                     (i<containerChildren.size()) && !_isCancelled;
                     i++) {                    
                    recursiveCopy((LDAPEntry)containerChildren.elementAt(i),
                                  ldc);                
                }
            }
        }
        /* Copy this container object */
        if (!_isCancelled) {                                        
            String[] arg = {DSUtil.abreviateString(baseDN, 45)};
            String msg = DSUtil._resource.getString("browser", 
                                                    "copying-object-label",
                                                    arg);
            _progressDialog.setTextInLabel(msg);
            _numberCopiedObjects++;                    
            
            _clipboard.putEntry(baseEntry);                                        
        }
    }
    
    private void updateProgressDialogForEnd() {
        if (_isCancelled || 
            (_numberCopiedObjects < 2)) {
            _progressDialog.closeCallBack();
        } else {                
            String[] arg = {String.valueOf(_numberCopiedObjects)};                
            _progressDialog.setTextInLabel(DSUtil._resource.getString("browser",
                                                                      "copied-objects-label",
                                                                      arg));
            _progressDialog.waitForClose();            
        }
    }        
    
    private void createCopyProgressDialog() {
        String title =  DSUtil._resource.getString("browser", "copy-objects-title");
        
        _progressDialog = new GenericProgressDialog(_frame, 
                                                    true, 
                                                    GenericProgressDialog.ONLY_CANCEL_BUTTON_OPTION, 
                                                    title,
                                                    null,
                                                    null);                    
        _progressDialog.setLabelColumns(50);
        _progressDialog.addActionListener(Copier.this);        
    }
}


class CutDeleter implements Runnable, ActionListener {
    boolean _cut;
    
    boolean _isCancelled = false;
    
    JFrame _frame;
    GenericProgressDialog _progressDialog;                    
    
    int _numberDeletedObjects = 0;            

    boolean _isWholeTreeDeleted = false;
    
    String _startDn;

    LDAPEntry _baseEntry;

    Clipboard _clipboard;

    LDAPConnection _ldc;    

    String[] _attrs;
    String[] CUT_ATTRS = { "*", "numsubordinates" };        // To be in sync with the COPY_ATTRS
    String[] NOT_CUT_ATTRS = { "numsubordinates" };
    
    final String CHILDREN_FILTER = "|(objectclass=*)(objectclass=ldapsubentry)";

    /**
      * The connection needs to have the _manageDSAITControl */
    public CutDeleter( LDAPConnection ldc, 
                       String startDn, 
                       JFrame frame, 
                       Clipboard clipboard,
                       boolean cut) {
        _frame = frame;
        _ldc = ldc;
        _startDn = startDn;
        _cut = cut;
        _clipboard = clipboard;
        if (_cut) {
            _attrs = CUT_ATTRS;
            _clipboard.clean();
        } else {
            _attrs = NOT_CUT_ATTRS;
        }
    }
    
    public void actionPerformed(ActionEvent e) {        
        if (e.getActionCommand().equals(GenericProgressDialog.CANCEL)) {
            _isCancelled = true;
            _progressDialog.disableCancelButton();
        } else if (e.getActionCommand().equals(GenericProgressDialog.CLOSE)) {
            _progressDialog.closeCallBack();
        }
    }
    
    public void execute() {
        if (_startDn.equals("")) {
            /* If the user chose to copy the root entry we
               display an error message */            
            DSUtil.showErrorDialog( _frame,
                                    "delete-root-error-title", 
                                    "delete-root-error-msg", 
                                    (String[]) null,
                                    "browser" );                                                
        } else  {
            boolean manyEntries = false;
            try {
                _baseEntry = _ldc.read(_startDn, _attrs);
                manyEntries = DSContentModel.entryHasChildren(_baseEntry);
            } catch (LDAPException lde) {                
            }
            boolean doDelete = confirmDelete(manyEntries);                            
                
            if (doDelete) {                
                createDeleteProgressDialog(manyEntries);
                Thread thread = new Thread(CutDeleter.this);
                thread.start();
                _progressDialog.packAndShow();
            }
        }
    }
    
    public boolean isWholeTreeDeleted() {
        return _isWholeTreeDeleted;
    }

    public void run() {        
        boolean deleteOK = deleteTree(_ldc, _startDn);        
        _isWholeTreeDeleted = !entryExists(_ldc, _startDn);
        updateProgressDialogForEnd(deleteOK);        
    }

    private boolean confirmDelete(boolean manyEntries) {
        boolean confirmDelete = true;                
        boolean requiresConfirmation = false;

        String[] args = new String[1];
        args[0] = DSUtil.abreviateString(
                                         _startDn,
                                         30);        
        String msg = null;
        if (manyEntries) {           
            msg = "confirmDeleteTree";
        } else {
            msg = "confirmDeleteObject";
        }

        requiresConfirmation = (manyEntries && DSUtil.requiresConfirmation(GlobalConstants.PREFERENCES_CONFIRM_DELETE_SUBTREE)) ||
            (!manyEntries && DSUtil.requiresConfirmation(GlobalConstants.PREFERENCES_CONFIRM_DELETE_ENTRY));

        if  (requiresConfirmation) {
            int option = DSUtil.showConfirmationDialog(
                                                       _frame,
                                                       msg,
                                                       args,
                                                       "browser");    
            confirmDelete = option == JOptionPane.YES_OPTION;
        }
        return confirmDelete;
    }
    
    private boolean deleteTree(LDAPConnection ldc, 
                               String dn) {        
        try {
            ldc.setOption(LDAPConnection.MAXBACKLOG,
                          new Integer(0));
        } catch (LDAPException e) {
        }
            

        boolean deleteOk = true;
        try {
            deleteOk &= recursiveDelete(_baseEntry, ldc);
        }    catch ( LDAPException e ) {
            Debug.println("CutDeleter.deleteTree(): "+e);
            _progressDialog.appendTextToTextArea(dn+": "+e+"\n");
            deleteOk = false;
        }
        
        return deleteOk;            
    }        
    
    private boolean recursiveDelete(LDAPEntry baseEntry, LDAPConnection ldc) throws LDAPException {        

        if (baseEntry == null) {
            return true;
        }

        Vector containerChildren = new Vector();
        boolean deleteOk = true;
        String baseDN = baseEntry.getDN();

        LDAPSearchResults search_results = ldc.search(baseDN,
                                                      ldc.SCOPE_ONE,
                                                      CHILDREN_FILTER,
                                                      _attrs,
                                                      false);
        /* Delete each child found, recursively */            
        while ( search_results.hasMoreElements() &&
                !_isCancelled) {            
            /* Get the next child */
            Object nextElement = search_results.nextElement();
            LDAPEntry entry = null;
            try {                
                if (nextElement instanceof LDAPEntry) {
                    entry = (LDAPEntry)nextElement;
                    String dn = entry.getDN();            

                    // Skip children returned by views
                    if (!Helper.isChildOf(dn, baseDN)) {
                        continue;
                    }

                    ldc.delete( dn );                                        
                    if ((_numberDeletedObjects % 5) == 0) {
                        String[] arg = {DSUtil.abreviateString(dn, 45)};
                        String msg = DSUtil._resource.getString("browser", 
                                                                "deleting-object-label",
                                                                arg);                    
                        _progressDialog.setTextInLabel(msg);                                                        
                    }
                    _numberDeletedObjects++;
                    if (_cut) {
                        _clipboard.putEntry(entry);
                    }
                } else if (nextElement instanceof LDAPException) {
                    throw (LDAPException)nextElement;
                }
            } catch (LDAPException e) {
                if (e.getLDAPResultCode() != LDAPException.NOT_ALLOWED_ON_NONLEAF) {
                    String ldapError =  Helper.getLDAPErrorMessage(e);
                    if (entry != null) {
                        Debug.println("CutDeleter.recursiveDelete: " +
                                      "error deleting entry=" +
                                      entry.getDN() +
                                      ":" + ldapError);
                        _progressDialog.appendTextToTextArea(entry.getDN()+": "+ldapError+"\n");
                    } else {
                        /* Case where we got an exception */
                        Debug.println("CutDeleter.recursiveDelete: " +
                                      "error deleting "+ ldapError);
                        _progressDialog.appendTextToTextArea(ldapError+"\n");
                    }
                    deleteOk = false;
                } else {
                    containerChildren.addElement( entry );
                }
            }            
        }        
        if (_isCancelled) {            
            ldc.abandon(search_results);            
        } else {
            /* Recursively delete container entries bellow this entry...*/
            for (int i=0; 
                 (i<containerChildren.size()) && !_isCancelled;
                 i++) {                    
                deleteOk &= recursiveDelete((LDAPEntry)containerChildren.elementAt(i),
                                            ldc);                    
            }
        }
        
        /* Finally delete the entry */
        if (!_isCancelled) {
            try {                
                ldc.delete( baseDN );                
                String[] arg = {DSUtil.abreviateString(baseDN, 45)};
                String msg = DSUtil._resource.getString("browser", 
                                                        "deleting-object-label",
                                                        arg);                
                _progressDialog.setTextInLabel(msg);                
                _numberDeletedObjects++;
                if (_cut) {
                    _clipboard.putEntry(baseEntry);
                }
            } catch (LDAPException e) {
                Debug.println("CutDeleter.recursiveDelete: " +
                              "error deleting entry=" +
                              baseDN +
                              ":" + e);
                _progressDialog.appendTextToTextArea(baseDN+": "+e+"\n");
                deleteOk = false;
            }            
        }
        return deleteOk;
    }   
    
    private void updateProgressDialogForEnd(boolean deleteOk) {
        if (_isCancelled || 
            ((_numberDeletedObjects < 2) && deleteOk)) {
            _progressDialog.closeCallBack();
        } else {                
            String[] arg = {String.valueOf(_numberDeletedObjects)};                
            _progressDialog.setTextInLabel(DSUtil._resource.getString("browser",
                                                                      "deleted-objects-label",
                                                                      arg));
            _progressDialog.waitForClose();
        } 
    }        
    
    private void createDeleteProgressDialog(boolean many) {
        String title;
        if (many) {
            title =  DSUtil._resource.getString("browser", "delete-objects-title");
        } else {
            title =  DSUtil._resource.getString("browser", "delete-object-title");
        }
        _progressDialog = new GenericProgressDialog(_frame, 
                                                    true, 
                                                    GenericProgressDialog.TEXT_FIELD_AND_CANCEL_BUTTON_OPTION, 
                                                    title,
                                                    null,
                                                    null);
        _progressDialog.setTextInTextAreaLabel( DSUtil._resource.getString("browser", "non-deleted-objects-label")); 
        _progressDialog.setTextAreaRows(3);            
        _progressDialog.setLabelColumns(50);
        _progressDialog.addActionListener(CutDeleter.this);
    }

    private boolean entryExists(LDAPConnection ldc, String dn) {
        boolean entryExists = false;        
        try {
            LDAPEntry entry = ldc.read(dn,
                                           NOT_CUT_ATTRS );
            entryExists = entry != null;
        }    catch ( LDAPException e ) {
            if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
                Debug.println("CutDeleter.entryExists(): "+e);
            }            
        }
        return entryExists;
    }
}


class Paster implements Runnable, ActionListener {
    boolean _isCancelled = false;
    
    JFrame _frame;
    GenericProgressDialog _progressDialog;                    
    
    Clipboard _clipboard;

    Vector _pastedRootEntries;

    int _numberPastedObjects = 0;

    String _startDn;

    LDAPConnection _ldc;    
    
    public Paster(LDAPConnection ldc, String startDn, JFrame frame, Clipboard clipboard) {
        _frame = frame;
        _ldc = ldc;
        _startDn = startDn;
        _clipboard = clipboard;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GenericProgressDialog.CANCEL)) {
            _isCancelled = true;
            _progressDialog.disableCancelButton();
        } else if (e.getActionCommand().equals(GenericProgressDialog.CLOSE)) {
            _progressDialog.closeCallBack();
        }
    }
    
    public void execute() {
        if (_startDn.equals("") &&
            (_clipboard.getSize() > 0)) {
            /* Can't paste in the root */                        
            DSUtil.showErrorDialog( _frame,
                                    "paste-in-root-error-title", 
                                    "paste-in-root-error-msg", 
                                    (String[]) null, 
                                    "browser" );            
        } else {            
            createPasteProgressDialog();
            Thread thread = new Thread(Paster.this);
            thread.start();
            _progressDialog.packAndShow();
        }
    }

    public void run() {
        _pastedRootEntries = new Vector();
        boolean pasteOk = pasteTree(_ldc, _startDn, _pastedRootEntries);
        updateProgressDialogForEnd(pasteOk);            
    }
    
    public Vector getPastedRootEntries() {
        return _pastedRootEntries;
    }

    /* We make the assumption that the clipboard is a LIFO list */
    
    private boolean pasteTree(LDAPConnection ldc, 
                              String baseDN,
                              Vector pastedRootEntries) {
        Vector triedToPasteRootEntries = new Vector();
        boolean pasteOk = true;            
        for (int i=0 ; (i< _clipboard.getSize())&& !_isCancelled; i++) {            
            LDAPEntry currentEntry = _clipboard.getEntryAt(i);
            LDAPEntry entryToPaste = getEntryToPaste(currentEntry, baseDN, triedToPasteRootEntries);
            try {
                ldc.add(entryToPaste);
                
                if ((_numberPastedObjects % 5) == 0) {
                    String[] arg = {DSUtil.abreviateString(entryToPaste.getDN(), 45)};
                    String msg = DSUtil._resource.getString("browser", 
                                                            "pasting-object-label",
                                                            arg);
                    _progressDialog.setTextInLabel(msg);
                }

                _numberPastedObjects++;                
                DN lastTriedDN = (DN)triedToPasteRootEntries.elementAt(triedToPasteRootEntries.size() - 1);
                DN currentPastedDN = new DN(currentEntry.getDN());
                if (lastTriedDN.equals(currentPastedDN)) {
                    pastedRootEntries.addElement(entryToPaste.getDN());
                }
            } catch (LDAPException lde) {
                pasteOk = false;
                Debug.println("Paster.pasteTree: " +
                              "error pasting entry=" +
                              entryToPaste.getDN()+
                              ":" + lde);
                _progressDialog.appendTextToTextArea(entryToPaste.getDN()+": "+lde+"\n");                    
            }
        }                                              
        return pasteOk;
    }        
    
    private LDAPEntry getEntryToPaste(LDAPEntry entry, String baseDn, Vector triedToPasteRootEntries) {
        /* We calculate the new dn of the entry, and we take into account that we may be adding
           a tree, so we check if we have added the parent entry for this entry to calculate the
           new dn.  This dn is composed of the rdn of the entry relative to the old parent and the dn of the new
           parent (baseDn).
           This method updates the list of subtree roots that have been added.  The last added root is put
           at the end of the list.
           */
        DN entryDN = new DN(entry.getDN());            
        String[] rdns = LDAPDN.explodeDN( entry.getDN(),
                                          false );
        String newDn = "";
        boolean pastedRootFound = false;        
        for (int i=0; (i<triedToPasteRootEntries.size()) && !pastedRootFound; i++) {
            if (entryDN.isDescendantOf((DN)triedToPasteRootEntries.elementAt(i))) {
                pastedRootFound = true;
                DN rootDN = (DN)triedToPasteRootEntries.elementAt(i);
                int rootRDNCount = rootDN.countRDNs();
                int entryRDNCount = entryDN.countRDNs();
                
                for (int j=0; j <= entryRDNCount - rootRDNCount; j++) {
                    newDn += rdns[j]+", ";
                }
                newDn += baseDn;                
            }
        }
        if (!pastedRootFound) {
            triedToPasteRootEntries.addElement(entryDN);
            newDn = rdns[0] + ", " + baseDn;
        }
            
        LDAPAttributeSet set = entry.getAttributeSet();        
        
        LDAPEntry entryToPaste = new LDAPEntry( newDn, set);
        
        return entryToPaste;            
    }
    
    private void updateProgressDialogForEnd(boolean pasteOk) {
        if (_isCancelled ||
            ((_numberPastedObjects < 2) && pasteOk)) {
            _progressDialog.closeCallBack();
        } else {                
            String[] arg = {String.valueOf(_numberPastedObjects)};                
            _progressDialog.setTextInLabel(DSUtil._resource.getString("browser",
                                                                      "pasted-objects-label",
                                                                      arg));
            _progressDialog.waitForClose();
        } 
    }
    
    private void createPasteProgressDialog() {
        String title =  DSUtil._resource.getString("browser", "paste-objects-title");
        
        _progressDialog = new GenericProgressDialog(_frame, 
                                                    true, 
                                                    GenericProgressDialog.TEXT_FIELD_AND_CANCEL_BUTTON_OPTION, 
                                                    title,
                                                    null,
                                                    null);
        _progressDialog.setTextInTextAreaLabel( DSUtil._resource.getString("browser", "non-pasted-objects-label"));
        _progressDialog.setTextAreaRows(3);                        
        _progressDialog.setLabelColumns(50);
        _progressDialog.addActionListener(Paster.this);            
    }        
}


class Clipboard {
    Vector _container;
    Clipboard() {
        _container = new Vector();
    }
    
    public boolean isEmpty() {
        return (_container.size() == 0);
    }

    public LDAPEntry getEntryAt(int index) {
        return (LDAPEntry)_container.elementAt(index);
    }

    /* The clipboard is supposed to be a LIFO list...*/
    public void putEntry(LDAPEntry entry) {
        _container.insertElementAt(entry, 0);
    }   
    
    public void clean() {
        _container.clear();
    }        

    public int getSize() {
        return _container.size();
    }    
}

class EntryEditor implements IAdapterInfo {
    public EntryEditor(LDAPSchema schema,
                       JFrame frame,
                       DatabaseConfig databaseConfig) {
        _schema = schema;
        _frame = frame;
        _databaseConfig = databaseConfig;
    }    

    /* Returns true if the entry was created, false otherwise */
    public boolean createUser(String parentDn,
                              LDAPConnection ldc) {
        _createdObject = null;
        _ldc = ldc;        
        Vector objectClass = (Vector) ResourceEditor.getNewObjectClasses().get(ResourceEditor.KEY_NEW_USER_OBJECTCLASSES);
        return newPredefinedObject( objectClass, parentDn );        
    }

    /* Returns true if the entry was created, false otherwise */
    public boolean createGroup(String parentDn,
                              LDAPConnection ldc) {
        _createdObject = null;
        _ldc = ldc;
        Vector objectClass = (Vector) ResourceEditor.getNewObjectClasses().get(ResourceEditor.KEY_NEW_GROUP_OBJECTCLASSES);
        return newPredefinedObject( objectClass, parentDn );            
    }

    /* Returns true if the entry was created, false otherwise */
    public boolean createOrganizationalUnit(String parentDn,
                              LDAPConnection ldc) {
        _createdObject = null;
        _ldc = ldc;         
        Vector objectClass = (Vector) ResourceEditor.getNewObjectClasses().get(ResourceEditor.KEY_NEW_OU_OBJECTCLASSES);
        return newPredefinedObject( objectClass, parentDn );
    }

    /* Returns true if the entry was created, false otherwise */
    public boolean createCos(String parentDn,
                              LDAPConnection ldc) {
        _createdObject = null;
        _ldc = ldc;         
        Vector objectClass = new Vector();
        objectClass.addElement("top");
        objectClass.addElement("ldapsubentry");
        objectClass.addElement("cossuperdefinition");
        return newPredefinedObject( objectClass, parentDn );        
    }

    /* Returns true if the entry was created, false otherwise */
    public boolean createRole(String parentDn,
                              LDAPConnection ldc) {
        _createdObject = null;
        _ldc = ldc;             
        Vector objectClass = new Vector();
        objectClass.addElement("top");
        objectClass.addElement("ldapsubentry");
        objectClass.addElement("nsroledefinition");
        return newPredefinedObject( objectClass, parentDn );        
    }

    /* Returns true if the entry was created, false otherwise */
    public boolean createObject(String parentDn,
                              LDAPConnection ldc) {
        _createdObject = null;
        _ldc = ldc;         
        boolean objectCreated = false;

        // Display the class chooser dialog        
        ChooseObjectClassDialog dlg = new ChooseObjectClassDialog(_frame, _schema);
        dlg.show();
        dlg.dispose();
        
        if ( ! dlg.isCancel() ) {
            String selectedObjectClass = dlg.getSelectedValue();
            Vector v = DSSchemaHelper.getObjectClassVector( selectedObjectClass, _schema );

            if ( isStandardObjectClass( v.elements() ) ) {
                /* Yes, it is. So call the UserGroup editor. */
                objectCreated = newPredefinedObject( v, parentDn );                
                return objectCreated;
            } 

            /* Not a standard object class, so do it ourselves */
            /* Get all required attributes */
            Hashtable ht = new Hashtable();
            DSSchemaHelper.allRequiredAttributes(selectedObjectClass, _schema, ht);    
            Enumeration e = ht.elements();
    
            LDAPAttributeSet set = new LDAPAttributeSet();
            String prefix = null;
            while( e.hasMoreElements() ) {
                String name = (String)e.nextElement();

                LDAPAttribute attr = new LDAPAttribute( name );
                /* Initialize the objectclass attribute with the
                   chain of superiors */
                if ( name.equalsIgnoreCase( "objectclass" ) ) {
                    v = DSSchemaHelper.getObjectClassVector( selectedObjectClass, _schema );
                    for( int i = v.size() - 1; i >= 0; i-- ) {
                        attr.addValue( (String)v.elementAt( i ) );
                    }
                } else {
                    /* Not objectclass, initialize with the empty string */
                    attr.addValue( "" );
                    if ( (prefix == null) && !name.equalsIgnoreCase( "aci" ) ) {
                        prefix = name;
                    }
                }
                set.add( attr );
            }                            
                                                                
            /* Last resort, first optional attribute */
            if ( prefix == null ) {    
                ht = new Hashtable();    
                DSSchemaHelper.allOptionalAttributes(selectedObjectClass, _schema, ht);
                e = ht.elements();                
    
                if ( e.hasMoreElements() ) {
                    prefix = (String)e.nextElement();
                    /* aci is not valid for use as an RDN */
                    if ( prefix.equalsIgnoreCase( "aci" ) ) {
                        if ( e.hasMoreElements() ) {
                            prefix = (String)e.nextElement();
                        } else {
                            prefix = null;
                        }
                    }
                }
            }
            /* Can't figure out what attribute to use for RDN */
            if ( prefix == null ) {
                DSUtil.showErrorDialog( _frame, "noRDN", selectedObjectClass,
                                        "EntryObject" );
                return false;
            }

            /* Preliminary DN ??? */
            String value = DEFAULT_NEW;
            LDAPAttribute attr = set.getAttribute( prefix );
            if ( attr != null ) {
                Debug.println( "EntryEditor.createObject: prefix attribute = " + attr );
                Enumeration en = attr.getStringValues();
                if ( (en != null) && en.hasMoreElements() ) {
                    value = (String)en.nextElement();
                    if ( value.length() < 1 ) {
                        value = DEFAULT_NEW;
                        set.remove( prefix );
                        set.add( new LDAPAttribute( prefix, value ) );
                    }
                } else {
                    attr.addValue( value );
                }
            } else {
                attr = new LDAPAttribute( prefix, value );
                set.add( attr );
            }
            String dn = prefix + "=" + value;            
            if ( (parentDn != null) && (parentDn.length() > 0) ) {
                DN newDN = new DN( parentDn );
                newDN.addRDN( new RDN( dn ) );
                dn = newDN.toRFCString();
            }
            LDAPEntry entry = new LDAPEntry( dn, set );                                        
            if ( addGeneric( entry, _ldc ) ) {
                objectCreated = true;
            }        
        }
        return objectCreated;
    }

    /* Returns true if the entry was created, false otherwise */
    public boolean createRootObject(String suffixDn, LDAPConnection ldc) {
        boolean done = false;
        boolean objectCreated = false;

        /* See if it has the rights (only directory manager can perform this task) */        
        if (!DSUtil.isLocalDirectoryManager(ldc)) {
            DSUtil.showInformationDialog(_frame,
                                         "addRootEntry-needtobedirectorymanager",
                                         (String[])null,
                                         "dscontentmodel");
            done = true;
        }
        
        if (!done) {
            // Display the class chooser dialog            
            ChooseObjectClassDialog dlg = new ChooseObjectClassDialog(_frame, _schema);
            dlg.show();
            dlg.dispose();
            
            if ( ! dlg.isCancel() ) {
                /* With the chosen objectclass, default acis and the name of the suffix, we create
                   the 'basic' entry that we display in the property editor */
                String selectedObjectClass = dlg.getSelectedValue();                
                
                String[] rdns = LDAPDN.explodeDN( suffixDn, false );
                // RDNs need eacaping, but RDN attribute values do not
                rdns[0] = LDAPDN.unEscapeRDN(rdns[0]);
                
                String namingAttribute = rdns[0].substring( 0, rdns[0].indexOf('=') );
                String value = rdns[0].substring( rdns[0].indexOf('=')+1 );
                
                LDAPAttributeSet set = new LDAPAttributeSet();
                
                /* The naming attribute (obtained from the suffix) */
                LDAPAttribute attr = new LDAPAttribute( namingAttribute );
                attr.addValue(value);
                
                set.add(attr);            
                
                /* The aci attribute with the default values */
                String[] aciValues = {"(targetattr != \"userPassword\") (version 3.0; acl \"Anonymous access\"; allow (read, search, compare)userdn = \"ldap:///anyone\";)",
                                      "(targetattr != \"nsroledn||aci\")(version 3.0; acl \"Allow self entry modification except for nsroledn and aci attributes\"; allow (write)userdn =\"ldap:///self\";)",
                                      "(targetattr = \"*\")(version 3.0; acl \"Configuration Adminstrator\"; allow (all) userdn = \"ldap:///"+getAdminDN(ldc)+"\";)",
                                      "(targetattr =\"*\")(version 3.0;acl \"Configuration Administrators Group\";allow (all) (groupdn = \"ldap:///cn=Configuration Administrators, ou=Groups, ou=TopologyManagement, o=NetscapeRoot\");)",
                                      "(targetattr = \"*\")(version 3.0; acl \"SIE Group\"; allow (all)groupdn = \"ldap:///"+suffixDn+"\";)"
                };
                
                
                attr = new LDAPAttribute ("aci", aciValues);
                
                set.add(attr);
                
                /* The required attributes */
                Hashtable ht = new Hashtable();
                DSSchemaHelper.allRequiredAttributes(selectedObjectClass, _schema, ht);
                Enumeration e = ht.elements();            
                while( e.hasMoreElements() ) {
                    String name = (String)e.nextElement();
                    
                    if (!name.equalsIgnoreCase (namingAttribute)) {
                        attr = new LDAPAttribute( name );
                        /* Initialize the objectclass attribute with the
                           chain of superiors */
                        if ( name.equalsIgnoreCase( "objectclass" ) ) {
                            Vector v = DSSchemaHelper.getObjectClassVector( selectedObjectClass, _schema );
                            for( int i = v.size() - 1; i >= 0; i-- ) {
                                attr.addValue( (String)v.elementAt( i ) );
                            }
                        } else  {
                            /* Not objectclass nor the attribute we already added, initialize with the empty string */
                            attr.addValue( "" );                            
                        }
                        set.add( attr );
                    }
                }
                
                LDAPEntry entry = new LDAPEntry( suffixDn, set );
                LDAPAttributeSet attrs = null;
                
                /* With the 'basic' entry, we call the property editor.  We don't allow to modify the naming
                   of the entry, as we are adding a suffix entry with a fixed dn */
                DSEntryDialog entryDlg = doGenericDialog( entry, DSEntryPanel.SHOWINGDN_NONAMING, ldc );
                if ( entryDlg != null ) {
                    attrs = entryDlg.getAttributes();
                }
                /* The user has clicked on cancel, we return */
                if ( attrs == null ) {
                    done = true;
                }
                
                if (!done) {
                    /* We add the entry in the directory */
                    LDAPEntry newObject = new LDAPEntry(entryDlg.getDN(), attrs);
                    objectCreated = addObject( newObject, ldc );
                }
            }                
        }
        return objectCreated;
    }

    /**
     * Method used to edit an entry.
     *
     * @param entryDn the dn of the entry.
     * @param ldc the LDAPConnection to the server where the entry is located.
     * @param isAdvanced set this option to true in order to display ALWAYS the generic property editor.
     *
     * @return true if the entry was modified, false otherwise */     
    public boolean editObject(String entryDn, LDAPConnection ldc, boolean isAdvanced) {    
        Debug.println(9, "DSContentPage.editObject: begin entryDN [" + entryDn
                      + "] ldc [" + ldc + "] isConnected=" + ldc.isConnected() +
                      " isAuthenticated=" + ldc.isAuthenticated());
        boolean objectModified = false;
        _editedObjectDn = null;
        _ldc = ldc;
        /* Fetch all attributes of the entry */
        if (_allAttrs == null) {
            String[] editableAttributes = DSSchemaHelper.getOperationalAttributes(_schema);                
            String[] allAttributes = null;
            if (editableAttributes == null) {
                allAttributes = new String[1];
                allAttributes[0] = "*";
            } else {
                allAttributes = new String[editableAttributes.length + 1];
                for (int i=0; i < editableAttributes.length; i++) {
                    allAttributes[i] = editableAttributes[i];
                }
                allAttributes[editableAttributes.length] = "*";                            
            }
            _allAttrs = allAttributes;
        }

        if (_allAttrs == null) {
            Debug.println(9, "DSContentPage.editObject: _allAttrs == null");
        } else {
            for (int ii = 0; ii < _allAttrs.length; ++ii) {
                Debug.println(9, "DSContentPage.editObject: _allAttrs[" + ii + "]=["
                             + _allAttrs[ii] + "]");
            }
        }
        
        LDAPSearchConstraints searchConstraints = (LDAPSearchConstraints)_ldc.getSearchConstraints().clone();
        if (!entryDn.trim().equals("")) {
            searchConstraints.setServerControls(DSContentPage._manageDSAITControl);
        }
        LDAPEntry entry = null;
        boolean msgDisplayed = false;
        try {
            entry = _ldc.read(entryDn, _allAttrs, searchConstraints);
        } catch (LDAPException lde) {
            Debug.println("DSContentPage.editObject: LDAPException [" + lde + "]");
            /* The entry could not be read... */
            String[] args = {
                entryDn,
                Helper.getLDAPErrorMessage(lde)};
            
            DSUtil.showErrorDialog( _frame,
                                    "reading-object-error-title",
                                    "reading-object-error-msg",
                                    args,
                                    "EntryEditor" );            
            msgDisplayed = true;
        }

        if (entry != null) {
            LDAPAttribute objectclass = entry.getAttribute("objectclass");            
            /* In the case we don't want to call always the generic editor, check if this has an 
               objectclass known to the UserGroup editor. Get the object class to java class association. */
            if (!isAdvanced && isStandardObjectClass(objectclass.getStringValues())) {
                /* Yes, it is. So call the UserGroup editor. */    
                ConsoleInfo info = new ConsoleInfo();
                info.setUserLDAPConnection(_ldc);
                info.setUserHost(_ldc.getHost());
                info.setUserPort(_ldc.getPort());
                info.setLDAPConnection(_ldc);
                
                String rootSuffix = _databaseConfig.getRootSuffixForEntry(entryDn);
                if (rootSuffix != null) {
                    info.setUserBaseDN(rootSuffix);
                }
                
                ResourceEditor ed = new DSResourceEditor(_frame, info, entry );    
                
                String buttonText =
                    _resource.getString( "UserGroup",
                                         "Advanced-label" );
                /* Install an "Advanced" button for generic editing */
                ed.registerAdvancedOption( new UserGroupAdapter(EntryEditor.this,
                                                                buttonText,
                                                                entry,
                                                                _ldc));                
                ed.getAccessibleContext().setAccessibleDescription(_resource.getString("EntryEditor",
                                                                                       "predefined-description"));
                ed.showModal();                    
                if (ed.getSaveStatus()) {
                    objectModified = true;
                    _editedObjectDn = ed.getLDAPEntry().getDN();
                }
            } else {
                /* Got this far because there is no known customized editor
                   for this object or because the user wants to display the generic editor. */                    
                DSEntryDialog dlg = editGeneric(true, true, entry, _ldc );
                if (dlg != null) {
                    objectModified = true;
                    _editedObjectDn = dlg.getDN();
                }
            }
        } else if (!msgDisplayed) {
            // No entry and no exception !! -> case where we don't have the rights to read an entry
            String[] args = {entryDn};
            DSUtil.showErrorDialog( _frame,
                                    "reading-object-no-rights-title",
                                    "reading-object-no-rights-msg",
                                    args,
                                    "EntryEditor" );
        }    
        return objectModified;
    }

    public LDAPEntry getCreatedObject() {
        return _createdObject;
    }

    public String getEditedObjectDn() {
        return _editedObjectDn;
    }

    /* Gets the dn of the administrator, it it does not find it, returns the value
       by default proposed in the install */
    private String getAdminDN(LDAPConnection ldc) {        
        String adminDN ="uid=admin,ou=Administrators, ou=TopologyManagement, o=NetscapeRoot";
        
        LDAPSearchConstraints cons =
            (LDAPSearchConstraints)ldc.getSearchConstraints().clone();
        cons.setMaxResults( 0 );


        String[] attrs = {"uniquemember"};
        try {
            LDAPEntry findEntry = ldc.read("cn=Configuration Administrators, ou=Groups, ou=TopologyManagement, o=NetscapeRoot", attrs, cons);
            if (findEntry!=null) {
                LDAPAttribute attr = findEntry.getAttribute( "uniquemember" );
                if ( attr != null ) {
                    Enumeration en = attr.getStringValues();
                    if ( (en != null) && en.hasMoreElements() )
                        adminDN = (String)en.nextElement();
                }
            }
        } catch (LDAPException e) {
            Debug.println("EntryEditor.getAdminDN(): "+e);
        }

        return adminDN;
    }

    private boolean isStandardObjectClass( Enumeration en ) {
        /* Check if this has an objectclass known to the UserGroup
           editor. Get the object class to java class association. */
        boolean isStandardObjectClass = false;
        Hashtable htable = ResourceEditor.getResourceEditorExtension();
        if ( htable == null ) {
            Debug.println( "EntryEditor.isStandardObjectClass: " +
                           "No resourceEditorExtension in ConsoleInfo" );            
        } else {
            while( en.hasMoreElements() &&
                   !isStandardObjectClass ) {
                /* Check each object class of this entry to see if
                   it is one of the known ones. */
                String name = ((String)en.nextElement()).toLowerCase();
                if ( htable.get( name ) != null ) {
                    isStandardObjectClass = true;
                }
            }
        }
        return isStandardObjectClass;
    }

    protected boolean newPredefinedObject( Vector objectClass, String parentDn ) {
        boolean objectCreated = false;
        ConsoleInfo info = new ConsoleInfo();
        
        info.setLDAPConnection( _ldc );
        info.setAuthenticationDN( _ldc.getAuthenticationDN() );
        info.setAuthenticationPassword( _ldc.getAuthenticationPassword() );
        info.setUserLDAPConnection( _ldc);
        info.setUserHost( _ldc.getHost());
        info.setUserPort( _ldc.getPort());
        String rootSuffix = _databaseConfig.getRootSuffixForEntry(parentDn);
        if (rootSuffix != null) {
            info.setUserBaseDN(rootSuffix);
        }         
        info.put( "NewUserBaseDN", parentDn );
        
        //DSAdmin.dumpConsoleInfo( info );
        ResourceEditor ed = null;
        try {
            ed = new DSResourceEditor( _frame,
                                       info, 
                                       objectClass,
                                       parentDn );        
        }
        catch(ArrayIndexOutOfBoundsException x) {
             // This exception means that the resource extension for
            // objectClass is not correctly setup.
            // It's probably for a role entry.
             Debug.println("EntryEditor.newPredefinedObject: got" + x +
                          " while instantiating a resource editor");

            // We popup a dialog to warn the user            
            JOptionPane.showMessageDialog(_frame,
                                          _resource.getString("EntryObject", "role-extension-error"),
                                          _resource.getString("EntryObject", "role-extension-title"), 
                                          JOptionPane.ERROR_MESSAGE);
            ModalDialogUtil.sleep();
         }
         
         if (ed != null) {
             String buttonText =
                 _resource.getString( "UserGroup", "Advanced-label" );
             /* Install an "Advanced" button for generic editing */             
             ed.registerAdvancedOption( new UserGroupAdapter(EntryEditor.this,
                                                             buttonText,
                                                             parentDn,
                                                             _ldc));             
             ed.getAccessibleContext().setAccessibleDescription(_resource.getString("EntryEditor",
                                                                                    "new-predefined-description"));
             ed.showModal();             
             boolean saved = ed.getSaveStatus();
             ModalDialogUtil.disposeAndRaise(ed, _frame);
             if ( saved ) {
                 objectCreated = true;
                 _createdObject = ed.getLDAPEntry();                                
             }
         }
         return objectCreated;
    }

    /**
     * Put up a generic property editing dialog for a single object.
     *
     * @param modal true if the dialog is to be modal
     */
    private boolean addGeneric( LDAPEntry entry, LDAPConnection ldc ) {
        LDAPAttributeSet attrs = null;
        DSEntryDialog dlg = doGenericDialog( entry, DSEntryPanel.SHOWINGDN_NAMING, ldc );
        if ( dlg != null ) {
            attrs = dlg.getAttributes();
        }
        if ( attrs == null ) {
            return false;
        }
            
        LDAPEntry newEntry = new LDAPEntry(dlg.getDN(), attrs);
        return addObject( newEntry, ldc );
    }

    private boolean addObject( LDAPEntry entry, LDAPConnection ldc ) {
        /* Loop here, in case reauthentication is required */                
        if ( ldc == null ) {
            Debug.println( "EntryEditor.addEntry: no LDAP connection" );
            return false;
        }        
        boolean result = false;
        boolean done = false;

        while ( !done ) {
            try {
                ldc.add( entry );                
                _createdObject = entry;
                result = true;
                done = true;
            } catch ( LDAPException e ) {
                Debug.println( "EntryEditor.addObject() Adding " + entry.getDN() + ", " + e );
                /* Allow reauthenticating on insufficient privileges */
                if ( e.getLDAPResultCode() ==
                     e.INSUFFICIENT_ACCESS_RIGHTS ) {
                    /* Put up dialog indicating the problem */
                    DSUtil.showPermissionDialog( _frame, ldc );                    
                } else if ( e.getLDAPResultCode() ==
                            e.NO_SUCH_OBJECT ) {
                    /* Put up dialog indicating the problem */
                    DSUtil.showErrorDialog(_frame, "noParentForAdd",
                                           "", "EntryObject");
                    result = false;                    
                    done = true;                    
                } else {
                    /* Some other error. Just show a dialog. */
                    DSUtil.showLDAPErrorDialog( _frame,
                                                e, "111-title" );
                    result = false;                    
                    done = true;
                }
            }
        }
        return result;
    }
    
    private DSEntryDialog doGenericDialog( LDAPEntry entry, int options, LDAPConnection ldc ) {        
        /* Make a list of attribute names */
        LDAPAttributeSet set = entry.getAttributeSet();
        int i = 0;
        LDAPSchema schema = getSchema();
        
        Enumeration attrSet = set.getAttributes();
        Vector v = new Vector();
        while( attrSet.hasMoreElements() ) {
            LDAPAttribute attr = (LDAPAttribute)attrSet.nextElement();            
            v.addElement( attr.getName().toLowerCase() );                        
        }        
        
        String[] names = new String[v.size()];
        String[] labels = new String[v.size()];
        for( i = 0; i < names.length; i++ ) {
            names[i] = (String)v.elementAt( i );
        }
        
        /* Sort the attributes alphabetically by name */
        DSUtil.trimAndBubbleSort( names, true );
        for( i = 0; i < names.length; i++ ) {
            labels[i] = names[i];
        }        

        /* Make a single page with all attributes */
        EntryPageDescription page =
            new EntryPageDescription( names, labels );
        /* Data model for property editor */
        DSPropertyModel model = new DSPropertyModel( _schema,
                                                     entry, page );
        /* Editor window */
        DSEntryPanel child = new DSEntryPanel( model , options, ldc);
        /* Do it as a dialog */
        DSEntryDialog dlg = new DSEntryDialog( _frame, child );
        dlg.setTitle( _resource.getString( "EntryObject", "property-dialog-title",
                                           entry.getDN()) );
        dlg.getAccessibleContext().setAccessibleDescription( _resource.getString( "EntryObject", "property-dialog-description",
                                                                                  entry.getDN()) );
        dlg.setLocationRelativeTo(_frame);
        dlg.pack();
        int height = dlg.getHeight();
        int width = dlg.getWidth();

        int maxHeight = _frame.getHeight();
        int maxWidth = _frame.getWidth();

        boolean resize = false;
        if (maxHeight < height) {
            height = maxHeight;
            resize = true;
        }
        if (maxWidth < width) {
            width =maxWidth;
            resize = true;
        }

        if (resize) {
            dlg.setSize(width, height);
        }

        DialogDisplayerAndDisposer displayer = new DialogDisplayerAndDisposer(dlg, _frame);
        if (SwingUtilities.isEventDispatchThread()) {
            displayer.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(displayer);                        
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if ( dlg.isCancel() ) {
            dlg = null;
        }
        return dlg;
    }

    public void setSchema(LDAPSchema schema) {
        _schema = schema;
    }

    /**
     * IAdapterInfo implementation
     * ====================
     */
    
    public LDAPSchema getSchema() {
        return _schema;
    }

    /**
     * Put up a generic property editing dialog for a single DS object.
     *     
     * @param save  true if the changed information should be saved;
     *              for example, if this dialog is being called as the
     *              Advanced dialog from the UserGroup editor, then
     *              the UserGroup editor is responsible for saving the
     *              changes back to the directory, and the DSEntryDialog
     *              should just report back those changes to the
     *              UserGroup dialog
     *              If this dialog is being called as a generic editor
     *              directly, then save should be true; in this case if
     *              there's a rename and a modification to do, if the rename
     *              works we return the dialog, null otherwise.
     * @param entry the entry that we want to display
     */
    public DSEntryDialog editGeneric(boolean allowNamingMod, boolean save, LDAPEntry entry, LDAPConnection ldc) {        
        int type = allowNamingMod? DSEntryPanel.SHOWINGDN_NAMING : DSEntryPanel.SHOWINGDN_NONAMING;
        
        DSEntryDialog dlg = doGenericDialog( entry, type, ldc );
        if (save && (dlg != null)) {                        
            LDAPModificationSet mods = dlg.getChanges();
                                                            
            DN oldDN = new DN(entry.getDN());
            DN newDN = new DN(dlg.getDN());                                
            
            boolean modrdn = !oldDN.equals(newDN);
            
            if ( modrdn ) {
                try {
                    String newrdn = newDN.explodeDN(false)[0];
                    if ((mods != null) && (mods.size() > 0)) {
                        /* If we have to modify the rdn and do some mods first we rename the entry to an always valid name:
                           use nsuniqueid for this */
                        LDAPAttribute attr = entry.getAttribute("nsuniqueid");
                        if (attr != null) {
                            String value = (String)attr.getStringValues().nextElement();
                            String intermediateRdn = "nsuniqueid="+value;
                                        
                            ldc.rename( entry.getDN(), intermediateRdn, false );
                            
                            String intermediateDn;
                            if (oldDN.getParent() != null) {
                                intermediateDn = intermediateRdn + ", "+oldDN.getParent();
                            } else {
                                intermediateDn = intermediateRdn;
                            }                                                    
                            
                            if ((mods != null) && (dlg != null)) {    
                                if (!saveChanges( mods, dlg.getAttributes(), ldc, intermediateDn)) {
                                /* try to go back to old dn */
                                    String oldRdn = oldDN.explodeDN(false)[0];
                                    ldc.rename( intermediateDn, oldRdn, false );                                    
                                    dlg = null;
                                }                            
                            }
                            
                            if (dlg != null) {                                    
                                ldc.rename( intermediateDn, newrdn, false );
                            }
                        }
                    } else {
                        /* Just rename the entry if no mods to do */
                        ldc.rename( entry.getDN(), newrdn, false );
                    }
                } catch ( LDAPException lde ) {
                    /* Something went wrong when renaming */                        
                        /* The entry could not be read... */
                    String[] args = {
                        entry.getDN(),
                        Helper.getLDAPErrorMessage(lde)};
                    
                    DSUtil.showErrorDialog( _frame,
                                            "renaming-object-error-title",
                                            "renaming-object-error-msg",
                                            args,
                                            "EntryEditor" );
                }                                    
            } else if (mods != null) {
                if ( !saveChanges( mods, dlg.getAttributes(), ldc, dlg.getDN()) ) {
                    dlg = null;
                }
            }                
        }    
        return dlg;    
    }

    boolean saveChanges( LDAPModificationSet mods, 
                         LDAPAttributeSet set, 
                         LDAPConnection ldc,
                         String dn) {        
        boolean saveChanges = true;
        LDAPSearchConstraints cons =
            (LDAPSearchConstraints)ldc.getSearchConstraints().clone();        
        cons.setServerControls( DSContentPage._manageDSAITControl );

        if ( (mods != null) &&
             (mods.size() > 0) ) {
            try {            
                ldc.modify( dn, mods, (LDAPConstraints)cons );                                    
				if ( DSUtil.requiresRestart ( dn, mods )) {
					DSUtil.showInformationDialog( _frame,
								"requires-restart", (String)null );
				}
            } catch ( LDAPException e ) {
                Debug.println( "DSContentPage$EntryEditor.saveChanges Modifying " + dn + ", " + e );
                /* Allow reauthenticating on insufficient privileges */
                if ( e.getLDAPResultCode() ==
                     e.INSUFFICIENT_ACCESS_RIGHTS ) {
                    /* Put up dialog indicating the problem */
                    DSUtil.showPermissionDialog( _frame, ldc );
                    /* If the user cancels reauthentication, bail */                    
                    saveChanges = false;                
                    
                } else if ( e.getLDAPResultCode() ==
                            e.NO_SUCH_OBJECT ) {
                    /* Entry doesn't exist, try adding it */
                    LDAPEntry entry = new LDAPEntry(dn, set);
                    saveChanges = addObject( entry, ldc );                                          
                } else {
                    /* Some other error. Just show a dialog. */
                    DSUtil.showLDAPErrorDialog( _frame,
                                                e, "111-title" );
                    saveChanges = false;
                }
            }
        }
        return saveChanges;
    }

    LDAPConnection _ldc;
    LDAPSchema _schema;
    JFrame _frame;
    DatabaseConfig _databaseConfig;
    ResourceSet _resource = DSUtil._resource;
    String[] _allAttrs; // Contains all the attributes an entry may have
    String _editedObjectDn;
    LDAPEntry _createdObject;

    final String DEFAULT_NEW =
    _resource.getString("EntryObject", "defaultnew");
}


interface IAdapterInfo {
    public LDAPSchema getSchema();
    public DSEntryDialog editGeneric(boolean allowNamingMod, boolean save, LDAPEntry entry, LDAPConnection ldc);
}

interface IContentPageInfo {
    public boolean isRootSelected();
    public boolean isSelectedNodeRemote();
    public boolean isSelectedNodeSuffix();
    public boolean isClipboardEmpty();
    public boolean isSorted();
    public Integer getSelectionActivationState();
    public Integer getSelectionVlvState();
    public Integer getSelectionPWPState();
    public boolean getFollowReferrals();
    public int getDisplay();
    public String getPanelLayout();    
    public String getSelectedPartitionView();
}


class AttributeController implements Runnable {
    LDAPConnectionPool _connectionPool;
    JPanel _panel;
    AttributeTableModel _attributeTableData;
    JTable _attributeTable;
        
    IBrowserNodeInfo _waitingNode;
    
    boolean _shutDown = false;        
    boolean _isWorking = false;
    boolean _isAbandoned = true;
    boolean _isCancelled = false;

    Hashtable _entryCache = new Hashtable();

    LDAPConnection _ldc;
    LDAPSearchListener _listener;    
    
    int DEFAULT_ATTRIBUTE_SIZE = 100;
    int DEFAULT_VALUE_SIZE = 150;
    
    static final String ALL_ENTRY_FILTER = "|(objectclass=*)(objectclass=ldapsubentry)";
    static final String[] ALL_ATTRS = {"*", "nsrole", "nsroledn"};    
    
    public AttributeController(LDAPConnectionPool connectionPool,
                          JPanel panel) {
        _panel = panel;        

        _connectionPool = connectionPool;                

        //Create the attribute viewing table.
        _attributeTableData = new AttributeTableModel();

        _attributeTable = new JTable(_attributeTableData) {
        
            /**
             * @overrides JTable@getScrollableTracksViewportWidth
             */
            public boolean getScrollableTracksViewportWidth() {
                Component parent =  getParent();
                if (parent != null && parent instanceof JViewport) {
                    return (getPreferredSize().width < parent.getSize().width);
                }
                return false;
            }

            /**
             * @overrides JTable@getScrollableTracksViewportHeigt
             */
            public boolean getScrollableTracksViewportHeight() {
                Component parent =  getParent();
                if (parent != null && parent instanceof JViewport) {
                    return (getPreferredSize().height < parent.getSize().height);
                }
                return false;
            }
        };

        _attributeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _attributeTable.setRowSelectionAllowed(false);
        JScrollPane scroll = new JScrollPane(_attributeTable);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = gbc.REMAINDER;
        gbc.fill = gbc.BOTH;
        _panel.add(scroll, gbc);

        wakeUp();
    }

    public void updateAttributePanel(IBrowserNodeInfo node) {
        stopUpdate();        
                
        _waitingNode = node;
    
        if (!_isWorking) {
            synchronized(this) {                
                notify();
            }
        }
    }          

    

    public void clearAttributePanel() {    
        stopUpdate();
        
        _attributeTableData.cleanData();
        _attributeTableData.fireTableDataChanged();
        _attributeTable.getTableHeader().resizeAndRepaint();        
        _panel.validate();
        _panel.repaint();
    }

    public void reset() {
        _entryCache.clear();
    }
    
    public void shutDown() {
        _shutDown = true;
        stopUpdate();
    }

    public void wakeUp() {        
        _shutDown = false;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {        
        while (!_shutDown) {            
            _isWorking = true;
            if (_waitingNode != null) {                
                readAndUpdate();                
            }
            try {
                synchronized(this) {
                    _isWorking = false;                    
                    wait();                                            
                }
            } catch (Exception e) {                    
            }
        }
    }

    private void stopUpdate() {
        if (_isWorking &&
            !_isCancelled) {
            _isCancelled = true;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    abandon();             
                }
            });
            thread.start();
        }
    }

    private void abandon() {        
        if (!_isAbandoned &&
            (_listener != null) &&
            (_ldc != null) &&
            (_listener != null)) {
            _isAbandoned = true;                
            try {
                _ldc.abandon(_listener);
            } catch (Exception e) {                    
            }
        }
    }                    

    private void readAndUpdate() {        
        IBrowserNodeInfo node = _waitingNode;        
        _waitingNode = null;
        LDAPEntry entry = (LDAPEntry)_entryCache.get(node.getURL());
        if (entry == null) {
            boolean gotEntry = false;        
            try {
                _ldc = _connectionPool.getConnection(node.getURL());
                String dn = Helper.getNodeInfoDN(node);
                LDAPSearchConstraints constraints = (LDAPSearchConstraints)_ldc.getSearchConstraints().clone();                
                constraints.setServerControls(DSContentPage._manageDSAITControl);     
                
                _listener = _ldc.search(dn,
                                        _ldc.SCOPE_BASE,
                                        ALL_ENTRY_FILTER,
                                        ALL_ATTRS,
                                        false,
                                        _listener,
                                        constraints);
                while (!gotEntry &&
                       !_isCancelled) {                
                    LDAPMessage result =  _listener.getResponse();                                    
                    if (result != null) {                        
                        if (result instanceof LDAPSearchResult) {
                            entry = ((LDAPSearchResult)result).getEntry();
                            gotEntry = true;
                        }
                    } else {
                        _isCancelled = true;
                    }                
                }                
                if (_isCancelled) {
                    entry = null;
                    if (_isCancelled  &&
                        !_isAbandoned) {
                        _isAbandoned = true;
                        _ldc.abandon(_listener);                    
                    }
                }
            } catch (LDAPException e) {
                // TO MODIFY
            } 

            if (entry != null) {
                _entryCache.put(node.getURL(), entry);
            }
        }
        if (entry != null) {
            updateTableFromEntry(entry);
        }

        _isCancelled = false;
    }

    private void updateTableFromEntry(LDAPEntry entry) {        
        int longestAttributeSize = 0;
        int longestValueSize = 0;            
        
        _attributeTableData.cleanData();
        LDAPAttributeSet set = entry.getAttributeSet();
        int numberAttributes = set.size();
        int currentRow = 0;
        
        String longestAttribute = null;
        int longestAttributeRow = 0;
        String longestValue = null;
        int longestValueRow = 0;

        /* In order to sort the attributes */
        String[] attributeNames = new String[numberAttributes];
        for (int i=0; i<numberAttributes; i++) {
            LDAPAttribute attr = set.elementAt(i);
            String attributeName = attr.getName();
            attributeNames[i] = attributeName;            
        }

        /* Sort the attributes */
        DSUtil.bubbleSort(attributeNames, true);

        /* Populate the table and calculate the preferred size */
        for (int i=0; i<numberAttributes; i++) {            
            LDAPAttribute attr = set.getAttribute(attributeNames[i]);
            String attributeName = attributeNames[i];            
            int attrLength = attributeName.length();
            if ( attrLength > longestAttributeSize) {
                longestAttributeSize = attrLength;
                longestAttributeRow = currentRow;
                longestAttribute = attributeName;                    
            }
            Enumeration values = attr.getStringValues();
            while(values.hasMoreElements()) {
                String value = (String)values.nextElement();
                int valueLength = value.length();
                if (valueLength > longestValueSize) {
                    longestValueSize = valueLength;
                    longestValueRow = currentRow;
                    longestValue = value;                        
                }
                _attributeTableData.addRow(attributeName, 
                                           value);                    
                currentRow++;
            }
        }
        
        final int preferredAttributeSize = getComponentWidth(longestAttribute, longestAttributeRow, 0) + 10;
        final int preferredValueSize = getComponentWidth(longestValue, longestValueRow, 1) + 10;        
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {               
                TableColumn tcol = _attributeTable.getColumn(_attributeTableData.COLUMN_NAMES[0]);                 
                if (preferredAttributeSize > DEFAULT_ATTRIBUTE_SIZE) {
                    tcol.setPreferredWidth(preferredAttributeSize);
                } else {
                    tcol.setPreferredWidth(DEFAULT_ATTRIBUTE_SIZE);
                }                                

                tcol = _attributeTable.getColumn(_attributeTableData.COLUMN_NAMES[1]);
                if (preferredValueSize > DEFAULT_VALUE_SIZE) {
                    tcol.setPreferredWidth(preferredValueSize);
                } else {
                    tcol.setPreferredWidth(DEFAULT_VALUE_SIZE);
                }                
                _attributeTableData.fireTableDataChanged();
                _attributeTable.getTableHeader().resizeAndRepaint();
            }    
        });
    }

    private int getComponentWidth(String value, int row, int column) {
        TableCellRenderer renderer = _attributeTable.getCellRenderer(row, column);
        Component comp = renderer.getTableCellRendererComponent(_attributeTable,
                                                                value,
                                                                false, 
                                                                false,
                                                                row,
                                                                column);
        return (int)comp.getPreferredSize().getWidth();    
    }
}

/* Our particular table model, conceived to display attributes */

class AttributeTableModel extends AbstractTableModel {
    public AttributeTableModel() {            
    }
    
    public int getRowCount() {
        return _values.size();
    }
    public int getColumnCount() {
        return 2;
    }
    
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return _attributes.elementAt(row);
        } else {
            return _values.elementAt(row);
        }
    }
    
    public void cleanData() {
        _attributes.clear();
        _values.clear();
    }
    
    public void addRow(String attributeName, String value) {        
        _attributes.addElement(attributeName);
        _values.addElement(value);    
    }
    
    public String getColumnName(int column) {
        if (column >= 2) {
            return "";
        } else {
            return COLUMN_NAMES[column];
        }
    }
    
    Vector _attributes = new Vector();
    Vector _values = new Vector();
    public final String[] COLUMN_NAMES = {DSUtil._resource.getString("AttributeController","attribute-label"),
                                          DSUtil._resource.getString("AttributeController","value-label")};
}


class RemoteNodeInformationDialog extends AbstractDialog {
    JCheckBox _cb;
    IBrowserNodeInfo _node;
    boolean _isCancelled = false;
    String _section = "RemoteNodeInformationDialog";
    static ResourceSet _resource = DSUtil._resource;

    /* We assume that the node is a valid node with a non null URL */
    public RemoteNodeInformationDialog(JFrame frame,
                                 IBrowserNodeInfo node) {
        super( frame, "", true, OK | CANCEL);        
        _node = node;
        layoutComponents();    
    }

    public void packAndShow() {
        pack();
        show();
    }

    public boolean continueShowingDialog() {
        return !_cb.isSelected();
    }
    
    public boolean isCancelled() {
        return _isCancelled;
    }

    protected void cancelInvoked() {
        _isCancelled = true;
        super.cancelInvoked();
    }

    protected void layoutComponents() {
        setTitle(_resource.getString(_section, "title-label"));
        JPanel panel = new JPanel(new GridBagLayout());
        setComponent(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets     = new Insets(COMPONENT_SPACE, COMPONENT_SPACE, COMPONENT_SPACE, COMPONENT_SPACE);

        LDAPUrl url = _node.getURL();
        int port = url.getPort();
        String host = url.getHost();
        String[] args = {host,
                         String.valueOf(port)};
        String msg = _resource.getString(_section, "main-message-label", args);
        MultilineLabel lMain = new MultilineLabel(msg, 2, 40);

        _cb = UIFactory.makeJCheckBox(null,
                                      _section,
                                      "checkbox",
                                      false,
                                      _resource);

        gbc.gridwidth = gbc.REMAINDER;
        gbc.anchor = gbc.NORTHWEST;
        gbc.fill = gbc.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(lMain, gbc);

        gbc.fill = gbc.NONE;        
        panel.add(_cb, gbc);
        
        setOKButtonText(_resource.getString("general", "Continue-label"));
    }
}


class DisplayedChildrenLimitExceededDialog extends AbstractDialog {
    JCheckBox _cb;
    IBrowserNodeInfo _node;
    boolean _isCancelled = false;
    String _section = "DisplayedChildrenLimitExceededDialog";
    static ResourceSet _resource = DSUtil._resource;

    /* We assume that the node is a valid node with a non null URL */
    public DisplayedChildrenLimitExceededDialog(JFrame frame,
                                                IBrowserNodeInfo node) {
        super( frame, "", true, OK);        
        _node = node;
        layoutComponents();    
    }

    public void packAndShow() {
        pack();
        show();
    }

    public boolean continueShowingDialog() {
        return !_cb.isSelected();
    }
    
    public boolean isCancelled() {
        return _isCancelled;
    }

    protected void cancelInvoked() {
        _isCancelled = true;
        super.cancelInvoked();
    }

    protected void layoutComponents() {
        setTitle(_resource.getString(_section, "title-label"));
        JPanel panel = new JPanel(new GridBagLayout());
        setComponent(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets     = new Insets(COMPONENT_SPACE, COMPONENT_SPACE, COMPONENT_SPACE, COMPONENT_SPACE);

        int numSubordinates = _node.getNumSubOrdinates();
        
        String[] args = {String.valueOf(numSubordinates),
                         String.valueOf(DSContentPage.DISPLAYED_CHILDREN_LIMIT)};
        
        String msg = _resource.getString(_section, "main-message-label", args);
        MultilineLabel lMain = new MultilineLabel(msg, 2, 40);

        _cb = UIFactory.makeJCheckBox(null,
                                      _section,
                                      "checkbox",
                                      false,
                                      _resource);

        gbc.gridwidth = gbc.REMAINDER;
        gbc.anchor = gbc.NORTHWEST;
        gbc.fill = gbc.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(lMain, gbc);

        gbc.fill = gbc.NONE;        
        panel.add(_cb, gbc);
        
        setOKButtonText(_resource.getString("general", "Continue-label"));
    }
}


class SortedChildrenLimitExceededDialog extends AbstractDialog {
    JCheckBox _cb;
    IBrowserNodeInfo _node;
    boolean _isCancelled = false;
    String _section = "SortedChildrenLimitExceededDialog";
    static ResourceSet _resource = DSUtil._resource;

    /* We assume that the node is a valid node with a non null URL */
    public SortedChildrenLimitExceededDialog(JFrame frame,
                                             IBrowserNodeInfo node) {
        super( frame, "", true, OK );        
        _node = node;
        layoutComponents();    
    }

    public void packAndShow() {
        pack();
        show();
    }

    public boolean continueShowingDialog() {
        return !_cb.isSelected();
    }
    
    public boolean isCancelled() {
        return _isCancelled;
    }

    protected void cancelInvoked() {
        _isCancelled = true;
        super.cancelInvoked();
    }

    protected void layoutComponents() {
        setTitle(_resource.getString(_section, "title-label"));
        JPanel panel = new JPanel(new GridBagLayout());
        setComponent(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets     = new Insets(COMPONENT_SPACE, COMPONENT_SPACE, COMPONENT_SPACE, COMPONENT_SPACE);
        
        int numSubordinates = _node.getNumSubOrdinates();
        
        String[] args = {String.valueOf(numSubordinates)};
        String msg = _resource.getString(_section, "main-message-label", args);
        MultilineLabel lMain = new MultilineLabel(msg, 2, 40);
        
        _cb = UIFactory.makeJCheckBox(null,
                                      _section,
                                      "checkbox",
                                      false,
                                      _resource);
        
        gbc.gridwidth = gbc.REMAINDER;
        gbc.anchor = gbc.NORTHWEST;
        gbc.fill = gbc.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(lMain, gbc);

        gbc.fill = gbc.NONE;        
        panel.add(_cb, gbc);
        
        setOKButtonText(_resource.getString("general", "Continue-label"));
    }

}

