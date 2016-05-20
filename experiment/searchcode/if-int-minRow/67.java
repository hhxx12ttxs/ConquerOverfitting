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

package com.netscape.admin.dirserv.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.event.*;
import com.netscape.admin.dirserv.*;
import com.netscape.admin.dirserv.task.*;
import com.netscape.management.client.util.Debug;
import com.netscape.management.client.util.UITools;
import com.netscape.management.nmclf.SuiOptionPane;
import netscape.ldap.*;
import netscape.ldap.util.*;

/**
 * Panel for Directory Server encrypted attribute management
 *
 * @author	chrisho
 * @author	pinaki
 * @author	rweltman
 * @author      cheston
 * @version %I%, %G%
 * @date		12/31/97
 * @see		com.netscape.admin.dirserv
 */
public class LDBMAttrEncPanel extends BlankPanel {

	/**
	 * Standard constructor
	 *
	 * @param model The Directory model
	 */
	public LDBMAttrEncPanel(IDSModel model ) {
		this(model, DN_PREFIX);
		Debug.println("LDBMAttrEncPanel() : default applied");
	}

	/**
	 * Standard constructor
	 *
	 * @param model The Directory model
	 */
	public LDBMAttrEncPanel(IDSModel model, String dnEntry) {
		super(model, _section, false);
		_helpToken = "configuration-database-attrenc-help";
		_dnEntry = dnEntry;
		Debug.println("LDBMAttrEncPanel() dnEntry:" + dnEntry);
		_refreshWhenSelect = false;
	}

	/**
	 * Called the first time the panel is selected
	 */
	public void init() {
		/* Keep track of indexes currently in the directory, ones we
		   change, ones we add, which ones are system indexes, and any
		   matching rules for the indexes */
		_attributes = new Hashtable();
		_changedAttributes = new Hashtable();
		_newAttributes = new Hashtable();
		_encAlgs = new Hashtable();

		_myPanel.setLayout(new GridBagLayout());

		JComponent Pane = createEncAttrTable(_myPanel);
		_Pane = (JPanel)Pane;
        resetGBC();
		_gbc.fill = GridBagConstraints.BOTH;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.gridheight = GridBagConstraints.RELATIVE;
		_gbc.weighty = 1.0;
		_gbc.insets = new Insets( 0, 0, 0, 0 );
		_myPanel.add(Pane, _gbc);

		_bAdd = makeJButton(_section, "add");
		_bAdd.setActionCommand(ADD);
		_bDelete = makeJButton(_section, "delete");
		_bDelete.setActionCommand(DELETE);
		//_bDelete.setEnabled( false );
		JButton[] buttons = { _bAdd, _bDelete };
		JPanel buttonPanel = UIFactory.makeJButtonPanel( buttons );
		resetGBC();
		_gbc.gridwidth = _gbc.REMAINDER;
		_gbc.fill = _gbc.HORIZONTAL;
		_gbc.weightx = 1.0;
		_gbc.weighty = 0;
		_gbc.insets = new Insets( UIFactory.getDifferentSpace(), 0, 0, 0 );
		_myPanel.add(buttonPanel, _gbc);
	}

	/**
	 * Document events for editing the matching rule fields
	 */
	public void insertUpdate(DocumentEvent e) {
		modelUpdate();
	}

	public void removeUpdate(DocumentEvent e) {
		modelUpdate();
	}

	public void changedUpdate(DocumentEvent e) {
		modelUpdate();
	}

	private void modelUpdate() {
		// need to do some hacking: the user clicks on the field,
		// insertUpdate gets invoked even if the user doesn't type anything.
		int row = _encAttrTableSystem.getEditingRow();
		int col = _encAttrTableSystem.getEditingColumn();

		if ((row != -1) && (col != -1)) {
			setValidFlag();
			setDirtyFlag();
		}
	}

	protected JTable createTable(EncAttrTableModel model) {
        JTable table = new JTable( model ) {
            /**
              * @overrides JTable@getScrollableTracksViewportWidth
              */
             public boolean getScrollableTracksViewportWidth()
             {
                 Component parent =  getParent();
                 if (parent != null && parent instanceof JViewport) {
                     return (getPreferredSize().width < parent.getSize().width);
                 }
                 return false;
             }

             /**
              * @overrides JTable@getScrollableTracksViewportHeight
              */
             public boolean getScrollableTracksViewportHeight()
             {
                 Component parent =  getParent();
                 if (parent != null && parent instanceof JViewport) {
                     return (getPreferredSize().height < parent.getSize().height);
                 }
                 return false;
             }
        };

		table.setColumnSelectionAllowed(false);		
		for (int i=0; i<numColumns; i++) {
			int width = Integer.parseInt(
				DSUtil._resource.getString(_section,
										   "table-columnwidth-"+i));
			TableColumn tcol = table.getColumn(headers[i]);
			tcol.setPreferredWidth(width);
            tcol.setHeaderRenderer( new CenterAlignedHeaderRenderer ());
			if ( (i > 0) && (i < (numColumns-1)) ) {
				/* Allow coloring the checkbox cell when selected */
				tcol.setCellRenderer( new CheckBoxTableCellRenderer());
			}
		}

		TableColumn lastCol = table.getColumn(headers[numColumns-1]);
		JTextField t = new JTextField();
		t.getDocument().addDocumentListener(this);
		DefaultCellEditor cEditor = new DefaultCellEditor(t);
		cEditor.setClickCountToStart( 1 );
		lastCol.setCellEditor(cEditor);
        lastCol.setCellRenderer( new LabelTableCellRenderer() );
	
		return table;
	}

	protected JComponent createEncAttrTable(JPanel panel) {
		int different = UIFactory.getDifferentSpace();
		numColumns = Integer.parseInt(DSUtil._resource.getString(_section,
						  "table-columns"));

        /* populate data */        
        _attributes = getEncryptedAttributes();
		constructDataModel(false);

        /* create system panel */
		JLabel systemIntroLabel = makeJLabel(_section, "system");		
	    JPanel systemPanel = new JPanel (new GridBagLayout());
		Border emptyBorder = new EmptyBorder( 0, 0, 0, 0 );
		systemPanel.setBorder( emptyBorder );
        
		resetGBC();
		_gbc.gridwidth = _gbc.REMAINDER;
		Insets nullInsets = new Insets( 0, 0, 0, 0 );
		_gbc.insets = nullInsets;
		systemPanel.add(systemIntroLabel, _gbc);

        _encAttrTableSystem = createTable(_systemDataModel);
		systemIntroLabel.setLabelFor(_encAttrTableSystem);
		_encAttrTableSystem.setRowSelectionAllowed(true);
                
                _encAttrTableSystem.getSelectionModel().setSelectionMode(
															 ListSelectionModel.SINGLE_SELECTION );
		_encAttrTableSystem.getSelectionModel().addListSelectionListener( this );

        JScrollPane scrollpaneSystem = new JScrollPane(_encAttrTableSystem);
		scrollpaneSystem.setBorder( UITools.createLoweredBorder() );

        resetGBC();
		_gbc.weightx = 1.0;
		_gbc.weighty = 1.0;
		_gbc.fill = GridBagConstraints.BOTH;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.insets = nullInsets;
		systemPanel.add(scrollpaneSystem, _gbc);

        /* set minimum size for the panels to 4 rows: 3 + title */
		int minHeight   = 4 * (_encAttrTableSystem.getRowHeight());
                
		scrollpaneSystem.setMinimumSize(new Dimension (100, minHeight));

        /* set preferred size for the system panel to 7 rows.
           Normally, you would not have to do this, but there
           is a bug in swing 1.0.3 because of which setDividerLocation
           call is ignored if performed before the pane is displayed   */

		int prefHeight  = 7 * (_encAttrTableSystem.getRowHeight());
		scrollpaneSystem.setPreferredSize(new Dimension (100, prefHeight));
		return systemPanel;
	}

	private void constructDataModel(boolean isUpdate) {
		Enumeration attrNames = _attributes.keys();

		if (!isUpdate) {
			_systemDataModel = new EncAttrTableModel();
			headers = new String[numColumns];
			for (int i=0; i<numColumns; i++) {
				headers[i]=DSUtil._resource.getString(_section, 
				  "table-columnheading-"+i);
				_systemDataModel.addColumn(headers[i]);
			}
		} else {
			_systemDataModel.removeAllRows();
		}

		while (attrNames.hasMoreElements()) {
			String attrStr = (String)attrNames.nextElement();
				fillRow(_systemDataModel, attrStr);					
		}
	}

   /**
     * Update on-screen data from Directory.
	 *
	 * Note: we overwrite the data that the user may have modified.  This is done in order to keep
	 * the coherency between the refresh behaviour of the different panels of the configuration tab.
     *
     **/
	public boolean refresh () {
		_attributes = getEncryptedAttributes();
		constructDataModel( true );
		_changedAttributes.clear();
		_newAttributes.clear();
		clearDirtyFlag();
		
		int row = _encAttrTableSystem.getSelectedRow();
		if ( row >= 0 ) {
			_bDelete.setEnabled(true);
		} else {
			_bDelete.setEnabled(false);
		}
		
		return true;
	}

	/**
	 * Reset screen state to what is in the Directory
	 */
	public void resetCallback() {
		if (isDirty()) {
			_attributes = getEncryptedAttributes();
			constructDataModel( true );
			_changedAttributes.clear();
			_newAttributes.clear();
			clearDirtyFlag();
		}
		int row = _encAttrTableSystem.getSelectedRow();
		if ( row >= 0 ) {
			_bDelete.setEnabled(true);
		} else {
			_bDelete.setEnabled(false);
		}
	}

	private Vector getNewIndexesInVector( Hashtable attrs) {
		Vector attrNames = new Vector();		
		Enumeration keys = attrs.keys();
		Vector newVals;
		String attrName;
		while (keys.hasMoreElements()) {
			String name = (String)keys.nextElement();
			Vector vals = (Vector)attrs.get(name);
			int nTypes = vals.size();
			if ( nTypes < 1 ) {
				continue;
			}

			attrName = name + ":";
			for( int i = 0; i < nTypes; i++ ) {
				attrName += (String)vals.elementAt( i );
				if ( i < (nTypes - 1) ) {
					attrName += ',';
				}
			}
			/*String matchingRule = (String)_matchingRules.get( name );
			if ( matchingRule != null ) {
				matchingRule = matchingRule.trim();
				if ( matchingRule.length() > 0 ) {
					attrName += ":" + matchingRule;
				}
			}*/
			attrNames.addElement(attrName);
		}		

		return attrNames;	
	}

	private String getNewIndexes( Hashtable attrs ) {
		String attrNames = "";
		Enumeration keys = attrs.keys();
		Vector newVals;
		while (keys.hasMoreElements()) {
			String name = (String)keys.nextElement();
			Vector vals = (Vector)attrs.get(name);
			int nTypes = vals.size();
			if ( nTypes < 1 ) {
				continue;
			}

			attrNames += name + ":";
			for( int i = 0; i < nTypes; i++ ) {
				attrNames += (String)vals.elementAt( i );
				if ( i < (nTypes - 1) ) {
					attrNames += ',';
				}
			}
                        /*
			String matchingRule = (String)_matchingRules.get( name );
			if ( matchingRule != null ) {
				matchingRule = matchingRule.trim();
				if ( matchingRule.length() > 0 ) {
					attrNames += ":" + matchingRule;
				}
			}*/
			attrNames += ' ';
		}
		return attrNames.trim();
	}

	/**
	 * Called when the Save button is hit
	 */
	public void okCallback() {
		if ( !isDirty() ) {
			return;
		}
		// in case an edit is in progress, close it and save the data
		TableCellEditor tce = _encAttrTableSystem.getCellEditor();
		if ( tce != null ) {
			tce.stopCellEditing();
		}
		/* Are any indexes to be deleted? */
		if ( requiresConfirmation(
			GlobalConstants.PREFERENCES_CONFIRM_DELETE_INDEX ) ) {
			Enumeration keys = _changedAttributes.keys();
			boolean deleting = false;
			String list = "";
			Vector deleted = new Vector();
			while ( keys.hasMoreElements() ) {
				String name = (String)keys.nextElement();
				Vector vals = (Vector)_changedAttributes.get(name);
				if ( vals.size() < 1 ) {
					deleting = true;
					list += name  + ' ';
					deleted.addElement( name );
				}
			}
			if ( deleting ) {
				int response = DSUtil.showConfirmationDialog(
					getModel().getFrame(),
					"confirm-delete",
					list.trim(),
					_section );
				if ( response != JOptionPane.YES_OPTION ) {
					/* Restore the deleted index */
					Enumeration e = deleted.elements();
					while( e.hasMoreElements() ) {
						fillRow(_systemDataModel, (String)e.nextElement());
					}
					return;
				}
			}
		}
		/* Make a list of new indexes */
		Vector vAttrNames = getNewIndexesInVector( _newAttributes );
		/* Now check for added types to existing indexes */
		Vector vChangedAttrs = getNewIndexesInVector( _changedAttributes );
		for (int i=0; i < vChangedAttrs.size(); i++) {
			vAttrNames.addElement(vChangedAttrs.elementAt(i));			
		}

		boolean createIndexes = false;					
		

		String[] attrNames = null;

		if (vAttrNames.size() > 0) {
			attrNames = new String[vAttrNames.size()];
			/* Create the actual indexes */	   
			vAttrNames.copyInto(attrNames);
			createIndexes = true;
			String title = DSUtil._resource.getString(_section,"title");
			_progressDialog = new GenericProgressDialog(getModel().getFrame(), 
														true, 
														GenericProgressDialog.DEFAULT_OPTION, 
														title,
														null,
														this);		   

			/*if (attrNames != null) {						
				_progressDialog.addStep(DSUtil._resource.getString(_section, "LDAPMode-firstStep-title"));		
				_progressDialog.addStep(DSUtil._resource.getString(_section, "LDAPMode-secondStep-title"));
			} else {*/
				_progressDialog.addStep(DSUtil._resource.getString(_section, "LDAPMode-firstStep-title"));				
			//}
			_progressDialog.disableCancelButton();
			
			_statusProgressDialog = new LDAPBasicProgressDialog(getModel().getFrame(), 
																DSUtil._resource.getString(_section, "LDAPMode-Status-title"), 
																true, 
																null,
																this);
			_statusProgressDialog.waitForClose();			
		} else {
			/* If we are here we only have attributes to delete (that are supposed to be in _changedAttributes).
			   If nothing has to be done (_changedAttributes is empty) we reset and skip.
			   */
			if (_changedAttributes.size() < 1) {
				resetCallback();
				return;
			}
			String title = DSUtil._resource.getString(_section,"title");
			_progressDialog = new GenericProgressDialog(getModel().getFrame(), 
														true, 
														GenericProgressDialog.ONLY_CLOSE_BUTTON_OPTION, 
														title,
														null,
														this);
			_progressDialog.addStep(DSUtil._resource.getString(_section, "LDAPMode-deleteStep-title"));			
			_progressDialog.disableCancelButton();
		}

		Thread th = new Thread(new IndexRunnable(attrNames));
		try {
			th.start();
			_progressDialog.packAndShow();
		} catch (Exception e) {
			Debug.println("IndexManagementPanel.okCallBack(): "+e);
		}
		int row = _encAttrTableSystem.getSelectedRow();
		if ( row >= 0 ) {
			_bDelete.setEnabled(true);
		} else {
			_bDelete.setEnabled(false);
		}
	}

	class IndexRunnable implements Runnable {

		String[] _attrNames = null;		

		IndexRunnable(String[] attrNames) {
			_attrNames = attrNames;
		}
		public void run() {
			/* Update the directory entries */
			_addedEntries = null;
			boolean status = updateDirectoryEntries();
			if (_taskCancelled) {
				cancel();
				_taskCancelled = false;
				return;
			}
			if (!status) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {							
						public void run() {							
							resetCallback();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				_progressDialog.closeCallBack();
				_taskCancelled = false;
				return;
			}
			
			// Now handle the new additions
			status = addNewIndexEntries();
			if (_taskCancelled) {
				cancel();
				_taskCancelled = false;
				return;
			}
			if (!status) {
				_progressDialog.disableCancelButton();
				cleanUpEntries();
				if (_statusProgressDialog != null) {
					_statusProgressDialog.hide();
				}
				try {
					SwingUtilities.invokeAndWait(new Runnable() {							
						public void run() {				
							resetCallback();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				_progressDialog.closeCallBack();
				_taskCancelled = false;
				return;
			}

			if ( status ) {				
					_progressDialog.stepCompleted(0);
				_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "LDAPMode-finished-title"));
			} else {
				_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "error-cleanup-title"));
				_progressDialog.disableCancelButton();
				cleanUpEntries();								
				_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "LDAPMode-endError-title"));
			}
			try {
				SwingUtilities.invokeAndWait(new Runnable() {							
					public void run() {				
						resetCallback();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			_taskCancelled = false;
			_progressDialog.enableButtons(true);
			_progressDialog.waitForClose();
		}	  
	}

	private boolean updateDirectoryEntries() {
		boolean status = true;
		Enumeration keys = _changedAttributes.keys();
		while ( status && keys.hasMoreElements() ) {
			String name = (String)keys.nextElement();
			Vector vals = (Vector)_changedAttributes.get(name);
			String dn = "cn=" + name + "," + RDN_INDEX + _dnEntry;
			if (_taskCancelled) {
				return false;
			}
			Debug.println("IndexManagement.updateDirectoryEntries() : deleting dn :" + dn);
			_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "delete-entry-title", dn));
			if( vals.size() < 1 ) {				
				if(AttrEncUtility.deleteAttribute(getModel(),dn)) {
					_attributes = getEncryptedAttributes();
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								constructDataModel( true );
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
					_changedAttributes.remove( name );
				}
				continue;
			}
			String[] newVals = new String[vals.size()];
			vals.copyInto( newVals );
                        /*
			String matchingRule = (String)_matchingRules.get( name );
			if ( matchingRule != null ) {
				matchingRule = matchingRule.trim();
				if ( matchingRule.length() < 1 ) {
					matchingRule = null;
				}
			}*/
			Debug.print( "IndexManagement.updateDirectoryEntries: " + name + " < " );
			for( int i = 0; i < newVals.length; i++ ) {
				Debug.print( newVals[i] + " " );
			}
                        /*
			if ( matchingRule != null ) {
				Debug.print( "> < " + matchingRule + " " );
			}*/
			Debug.println( ">" );
			
			LDAPModificationSet changes = new LDAPModificationSet();
			/* Delete the old attributes */
			changes.add( LDAPModification.DELETE,
						 new LDAPAttribute("nsindextype") );
			changes.add( LDAPModification.REPLACE,
						 new LDAPAttribute("nsmatchingrule") );
			/* Add the new ones */
			changes.add( LDAPModification.ADD,
						 new LDAPAttribute("nsindextype",
										   newVals) );
			/*
                        if ( matchingRule != null ) {
				changes.add( LDAPModification.ADD,
							 new LDAPAttribute("nsmatchingrule",
											   matchingRule) );
			}
                         */
			if (_taskCancelled) {
				return false;
			}
			Debug.println("IndexManagement.updateDirectoryEntries: modifying dn:" + dn);			
			_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "modify-entry-title", DSUtil.abreviateString(dn, 20)));
			if( !IndexAttrUtility.modifyAttributes(getModel(), dn, changes) ) {
				Debug.println( "IndexManagementPanel.updateDirectoryEntries: failed " +
							   "to modify " + dn );
				try {
					SwingUtilities.invokeAndWait(new ErrorMessageDisplayer(_progressDialog,"errorupdatingentries", DSUtil.abreviateString(dn, 20)));
				} catch (Exception e) {
				}
				status = false;
			} else {
				_changedAttributes.remove( name );
			}
		}
		return status;
	}

	private boolean addNewIndexEntries() {
		_addedEntries = new Vector();
		boolean status = true;
		Enumeration keys = _newAttributes.keys();
		while ( status && keys.hasMoreElements()) {
			String name = (String)keys.nextElement();
			Vector vals = (Vector)_newAttributes.get(name);
			if( vals.size() > 0 ) {
				String[] newVals = new String[vals.size()];
				vals.copyInto( newVals );
				String dn = "cn=" + name + "," + RDN_INDEX + _dnEntry;
				LDAPAttributeSet attrs = new LDAPAttributeSet();
				attrs.add(new LDAPAttribute("objectclass", OCLASSES));
				attrs.add(new LDAPAttribute("cn", name));
				attrs.add(new LDAPAttribute("nsEncryptionAlgorithm", (String)_encAlgs.get(name)));
				LDAPEntry entry = new LDAPEntry(dn, attrs);
				if (_taskCancelled) {
					return false;
				}
				Debug.println("IndexManagementPanel.okCallback(): adding dn:" + dn);
				_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "add-entry-title", dn));
                                if( !AttrEncUtility.addAttribute(getModel(),entry) ) {
					Debug.println( "IndexManagementPanel.okCallback: failed " +
								   "to add " + dn );
					status = false;
				} else {
					_addedEntries.addElement(name);
					_newAttributes.remove( name );
				}
				if (!status) {
					//_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "add-entry-error-title", DSUtil.abreviateString(dn, 20)));
					try {
						SwingUtilities.invokeAndWait(new ErrorMessageDisplayer(_progressDialog,"errorupdatingentries", DSUtil.abreviateString(dn, 20)));
					} catch (Exception e) {
					}					
				}
			}			
		}
		return status;
	}

	private void cleanUpEntries() {
		if (_addedEntries != null) {
			for (int i=0; i< _addedEntries.size(); i++) {
				String dn = (String)_addedEntries.elementAt(i);
				IndexAttrUtility.deleteAttribute(getModel(), dn);
			}
		}
	}

	protected void addAttribute() {
		Vector data = getAvailAttributes();
		Object[] selectedItems = null;	
		String attrName = null;
                String EncMethod = null;
		if (data != null) {
			NewEncAttrPanel child =
				new NewEncAttrPanel( new DefaultResourceModel(), data );
			SimpleDialog dlg =
				new SimpleDialog( getModel().getFrame(),
								  child.getTitle(),
								  SimpleDialog.OK |
								  SimpleDialog.CANCEL |
								  SimpleDialog.HELP,
								  child );
			dlg.setComponent( child );
			dlg.setOKButtonEnabled( false );
			dlg.setDefaultButton( SimpleDialog.OK );
			dlg.getAccessibleContext().setAccessibleDescription(DSUtil._resource.getString(_section,
																						   "selectAttribute-description"));
			dlg.packAndShow();
			selectedItems = child.getSelectedItems();
                        
                        if(selectedItems != null)
                        {
                            Vector encMethods = new Vector();
                            encMethods.add("AES");
                            encMethods.add("3DES");
                            EncMethodSelectionPanel child2 =
                                new EncMethodSelectionPanel( new DefaultResourceModel(), encMethods);
                            dlg = new SimpleDialog( getModel().getFrame(),
                                                                  child2.getTitle(),
                                                                  SimpleDialog.OK |
								  SimpleDialog.CANCEL |
								  SimpleDialog.HELP,
								  child2 );
                            dlg.setComponent( child2 );
                            dlg.setOKButtonEnabled( false );
                            dlg.setDefaultButton( SimpleDialog.OK );
                            dlg.getAccessibleContext().setAccessibleDescription(DSUtil._resource.getString(_section,
																						   "selectEncMethod-description"));
                            dlg.packAndShow();
                            EncMethod = (String) child2.getSelectedItem();
                        }
 		}		
		if ((selectedItems != null)&&(EncMethod != null)) {
			int lastAttributeToAdd = selectedItems.length - 1;
			for (int i=0; i<selectedItems.length; i++) {
				attrName = (String)selectedItems[i];
				// turn on equality and presence indexes for a new attribute
				Vector v = new Vector();
				initRow(attrName, v);
                                v.setElementAt(EncMethod,1);
				dumpVector( "addAttribute " + attrName, v );
				_systemDataModel.addRow(v);
				Vector val = new Vector();
				_encAlgs.put(attrName,EncMethod);
                                _attributes.put(attrName,EncMethod);
                                val.addElement(FILTERS[0]);
				//val.addElement( FILTERS[2] );
				/* If during the manipulations we delete an attribute (attrName) we make _changedAttributes.put( attrName, new Vector() ).
				   If there is the key in this Hashtable (_changedAttributes) that means that the user has deleted it.  So we don't have to
				   update _newAttributes (for the non existent attribute index) but _changedAttributes (for the existing attribute indexes) */
                                if (!_changedAttributes.containsKey(attrName)) {
					_newAttributes.put(attrName, val);
				} else {
					_changedAttributes.put(attrName, val);
				}
				/* We just do this for the last attribute we add (the last added attribute is the one that is selected) */
				if (i == lastAttributeToAdd) {
					int row = _systemDataModel.getRowIndex(attrName);
					_encAttrTableSystem.setRowSelectionInterval(row, row);
					setDirtyFlag();
					setValidFlag();
				}
			}
		}
	}

	protected void deleteAttribute() {
		int row = _encAttrTableSystem.getSelectedRow();
		if ( row >= 0 ) {
			_systemDataModel.removeRow( row );
			setDirtyFlag();
			setValidFlag();
		}
	}

	private void cancel() {
		_progressDialog.setTextInLabel(DSUtil._resource.getString(_section, "LDAPMode-cancelled-title"));
		cleanUpEntries();
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {							
				public void run() {			
					resetCallback();
				}
			});
		} catch (Exception e) {
				e.printStackTrace();
		}
		if (_statusProgressDialog != null) {
			_statusProgressDialog.hide();
		}				
		_progressDialog.closeCallBack();
	}

	/**
	 * Some list component changed
	 *
	 * @param e Event indicating what changed
	 */
	public void valueChanged(ListSelectionEvent e) {
		int minRow = e.getFirstIndex();
		int maxRow = e.getLastIndex();
		int selectedRow = _encAttrTableSystem.getSelectionModel().getMaxSelectionIndex();
		boolean selected = (minRow <= selectedRow) && (selectedRow <= maxRow);			
		_bDelete.setEnabled( selected );
	}

	/**
	 * A button was pressed
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		/* Prevent the VM from crashing on processing multiple events */
		_bAdd.setEnabled( false );
		_bDelete.setEnabled( false );

		if ( ((JButton)source).getActionCommand().equals(ADD) ) {
			getModel().setWaitCursor(true);			
			// in case an edit is in progress, close it and save the data
			TableCellEditor tce = _encAttrTableSystem.getCellEditor();
			if ( tce != null ) {
				tce.stopCellEditing();
			}
			addAttribute();
			getModel().setWaitCursor(false);
		} else if ( ((JButton)source).getActionCommand().equals(DELETE) ) {
			// in case an edit is in progress, close it and save the data
			TableCellEditor tce = _encAttrTableSystem.getCellEditor();
			if ( tce != null ) {
				tce.stopCellEditing();
			}
			deleteAttribute();
		} else if (e.getActionCommand().equals( GenericProgressDialog.SHOW_LOGS)) {
			Debug.println("IndexManagement.okcallback.actionPerformed: SHOW_LOGS");
			if (_statusProgressDialog != null) {
				_statusProgressDialog.pack();
				_statusProgressDialog.setLocationRelativeTo((Component)_progressDialog);
				_statusProgressDialog.setLocation(_progressDialog.getWidth(), _progressDialog.getHeight());
				_statusProgressDialog.show();
			}
		} else if ( e.getActionCommand().equals( GenericProgressDialog.CLOSE ) ) {
			if (_statusProgressDialog != null) {
				_statusProgressDialog.hide();
			}
			_progressDialog.closeCallBack();	
		} else if ( e.getActionCommand().equals( LDAPTask.CLOSE ) ) {
			if (_statusProgressDialog != null) {
				_statusProgressDialog.hide();
				_statusProgressDialog.dispose();
			}			
		} else if ( e.getActionCommand().equals( GenericProgressDialog.CANCEL ) ) {			
			_progressDialog.disableCancelButton();
			_taskCancelled = true;					
		}

		_bAdd.setEnabled( true );
		int row = _encAttrTableSystem.getSelectedRow();
		if ( row >= 0 ) {
			_bDelete.setEnabled(true);
		} else {
			_bDelete.setEnabled(false);
		}
	}

	private void dumpVector( String title, Vector v ) {
//		Enumeration e = v.elements();
//		while( e.hasMoreElements() ) {
//			System.out.println( "  <" + e.nextElement() + ">" );
//		}
	}

	/**
	 * Get a complete list of attributes, minus those already in one of
	 * the two tables
	 */
	private Vector getAvailAttributes() {
		LDAPSchema sch = getModel().getSchema();
		if (sch == null)
			return null;
		
		Vector v = new Vector();
		Vector names = _systemDataModel.getColumn(0);
		for( int i = 0; i < names.size(); i++ ) {
			names.setElementAt(
							((String)names.elementAt(i)).toLowerCase(), i );
		}
		
		dumpVector( "User", names );

		for (Enumeration e = sch.getAttributeNames(); e.hasMoreElements();) { 

			String str = (String)e.nextElement();

			/* If it's already in one of the tables, ignore it */
			if ( (names.indexOf( str ) >= 0) ) {
				continue;
			}

			if (v.size() == 0) {
				v.addElement(str);
			} else {
				int i=0;
				while (i<v.size()) {
					if (str.compareTo((String)v.elementAt(i)) < 0) {
						v.insertElementAt(str, i);
						break;
					}
					i++;
				}
				if (i == v.size()) {
					v.insertElementAt(str, i);
				}
			}
		}

		return v;
	}
/*
	private boolean isSystemIndex(String str) {
		return ( _systemIndexes.containsKey( str ) );
	}
*/
	private void fillRow(EncAttrTableModel model, String str) {
                
		String cn = (String)_attributes.get(str);;
		//dumpVector( "fillRow in " + str, v );
		Vector vals = new Vector();
		if ( cn != null ) {
			initRow(str, vals);
			String encAlg  = (String)_encAlgs.get( str );
			if ( encAlg == null ) {
				encAlg = "";
			}
			vals.setElementAt( encAlg, 1 );
			dumpVector( "fillRow out " + str, vals );
			model.addRow(vals);
		}
	}

	private void initRow(String str, Vector vals) {
		vals.addElement(str);/*
		for (int i = 1; i < headers.length-1; i++)
			vals.addElement(new Boolean(false));*/
		vals.addElement("");
	}

	/**
	 * Get the list of attributes currently indexed from the server
	 */
	private Hashtable getEncryptedAttributes() {
		LDAPConnection ld = getModel().getServerInfo().getLDAPConnection();
		String searchBase = _dnEntry;
		Hashtable table = new Hashtable();
		String[] attrs = {"cn", "nsEncryptionAlgorithm"};
		/* Each attribute to be indexed is represented by an entry at this
		   level */
		try {   
			Debug.println("getIndexedAttributes() search base :" + searchBase);
			LDAPSearchResults res = ld.search(searchBase, ld.SCOPE_SUB, 
											  "objectclass=nsAttributeEncryption", null,
											  false); 
			while (res.hasMoreElements()) {
				LDAPEntry entry = (LDAPEntry)res.nextElement();
				String cn = getAttribute(entry,attrs[0]);
				Debug.println("getIndexedAttributes() index cn:" + cn);
				String encAlg = getAttribute(entry, attrs[1]);
                                //_systemIndexes.put(cn,cn);
                                _encAlgs.put(cn,encAlg);
				table.put(cn,encAlg);
			}

		} catch (LDAPException e) {
			Debug.println("IndexManagementPanel.createIndexTable: " + e);
			//table.clear();
		}

		return table;
	}

	/**
	 * Get the first value of an attribute from an entry, assuming it
	 * is a String
	 */
	private String getAttribute( LDAPEntry entry, String attrName ) {
		LDAPAttribute attr = entry.getAttribute( attrName );
		String token = "";
		if ( attr != null ) {
			Enumeration en = attr.getStringValues();
			if ( en.hasMoreElements() ) {
				token = (String)en.nextElement();
			}
		}
		return token;
	}
  
	class EncAttrTableModel extends AbstractTableModel {
		EncAttrTableModel() {}

		public int getColumnCount() {
			return _columnNames.size();
		}

		public int getRowCount() {
			if (getColumnCount() > 0 ) {
				Vector v = (Vector)_tableColumns.elementAt(0);
				return v.size();
			}
			return 0;
		}

		public String getColumnName(int column) {
			if (column >= _columnNames.size())
				return "";
			return (String)_columnNames.elementAt(column);
		}

		public Object getValueAt(int row, int col) {
			if (getColumnCount() > 0) {
				Vector v = (Vector)_tableColumns.elementAt(col);
				return v.elementAt(row);
			}
			return null;
		}

		public boolean isCellEditable(int row, int col) {
			return ( (this != _systemDataModel) && (col != 0) );
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public void setValueAt(Object aValue, int row, int column) {
			if (isCellEditable(row, column)) {
				Vector col = (Vector)_tableColumns.elementAt(column);
				col.setElementAt(aValue, row);
				Vector rowVector = new Vector();
				for (int i = 1; i < headers.length-1; i++) {
					if ( ((Boolean)getValueAt(row, i)).equals(
						new Boolean("true")) ) {
						rowVector.addElement(FILTERS[i-1]);
					}
				}
				String name = (String)getValueAt(row, 0);

				if (_newAttributes.containsKey(name)) {
					_newAttributes.put(name, rowVector);
				} else {
					_changedAttributes.put(name, rowVector);
				}
                                /*
				if ( column == headers.length-1 ) {
					_matchingRules.put( name, aValue );
				}*/

				setDirtyFlag();
				setValidFlag();
			}
		}

		void removeRow( int row ) {
			for (int i=0; i<_tableColumns.size(); i++) {
				Vector v = (Vector)_tableColumns.elementAt(i);
				if( i == 0 ) {
					String attrName = (String)v.elementAt( row );
					/* If the attribute index we are deleting is in the Hashtable _newAttributes
					   that means that it is not in the server (it has been added by the user and then
					   removed).  We don't need to to update _changedAttributes in this case: we just remove
					   the attribute from _newAttributes (we DON'T NEED to do any server update for this
					   attribute */
					//if (_newAttributes.containsKey (attrName)) {
						_newAttributes.remove( attrName );
					//} else {
						_changedAttributes.put( attrName, new Vector() );
					//}
				}
				v.removeElementAt(row);
			}
			fireTableRowsDeleted(row, row);
		}

		void removeAllRows() {
			for (int i=0; i<_tableColumns.size(); i++) {
				Vector v = (Vector)_tableColumns.elementAt(i);
				v.removeAllElements();
			}
			fireTableRowsDeleted(0, _tableColumns.size());
		}

		void insertRowAt(Vector values, int row) {
			for (int i=0; i<values.size(); i++) {
				Vector v = (Vector)_tableColumns.elementAt(i);
				v.insertElementAt(values.elementAt(i), row);
			}
			fireTableRowsInserted( row, row );
		}

		void addRow(Vector values) {
			int row = 0;
			Vector v = (Vector)_tableColumns.elementAt(0);
			String str = (String)values.elementAt(0);
			/* Insert so the list is in alphabetical order */
			while ((row < v.size()) &&
				   (!((String)v.elementAt(row)).equals("")) &&
				   (str.compareTo((String)v.elementAt(row)) > 0))
				row++;
			for (int i=0; i<values.size(); i++) {
				v = (Vector)_tableColumns.elementAt(i);
				v.insertElementAt(values.elementAt(i), row);
			}

			fireTableRowsInserted( row, row );
		}

		void addColumn(String name) {
			_columnNames.addElement(name);
			_tableColumns.addElement(new Vector());
		}

		int getRowIndex(String name) {
			Vector v = (Vector)_tableColumns.elementAt(0);
			for (int i = 0; i < v.size(); i++)
				if ( name.equals((String)v.elementAt(i)) )
					return i;  
			return 0;
		}

		Vector getColumn(int col) {
			return (Vector)_tableColumns.elementAt(col);
		}

		protected Vector _columnNames = new Vector();
		protected Vector _tableColumns = new Vector();
	};

	static final private String ADD = "add";
	static final private String DELETE = "delete";
        static final private String MODIFY = "modify";
	static final private String RDN_INDEX = "cn=encrypted attributes," ;
	static final private String RDN_MONITOR = "cn=monitor," ;
	static final private int SUCCESS = 0;
	static final private int FAILURE = 1;
	static final private int MODIFYAGAIN = 2;
	static final private String _section = "attr-enc";
	// All indexes are represented by an entry directly under this:
	static final private String DN_PREFIX = "cn=encrypted attributes,cn=ldbm database,cn=plugins,cn=config";
	/* The FILTERS order must be the same as headings in the properties file */
	static final private String[] FILTERS = {"nsEncryptionAlgorithm"};
	// Object classes of an index entry
	static final private String[] OCLASSES = { "top", "nsAttributeEncryption" };

	private Vector _addedEntries = null;
	private String[] headers = null;
	private JTable _encAttrTableSystem = null;
	private Hashtable _attributes = null;
	private Hashtable _changedAttributes = null;
	private Hashtable _newAttributes = null;
        private Hashtable _encAlgs = null;
	private EncAttrTableModel _systemDataModel = null;
	private int numColumns;
	private JButton _bAdd;
	private JButton _bDelete;
	private String _dnEntry = null;
	private String _conf_prefix = null;
	private JPanel _Pane = null;
	LDAPBasicProgressDialog _statusProgressDialog;
	GenericProgressDialog _progressDialog;
	private boolean _taskCancelled = false;

	class ErrorMessageDisplayer implements Runnable {
		public ErrorMessageDisplayer(Component comp, String msg, String arg) {
			_comp = comp;
			_msg = msg;
			_arg = arg;
		}
		public void run() {
			DSUtil.showInformationDialog( _comp, _msg, _arg, _section);
		}
	}
	Component _comp;
	String _msg;
	String _arg;
}

