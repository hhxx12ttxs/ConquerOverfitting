/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.explorer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.etable.TableColumnSelector;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RowModel;
import org.netbeans.swing.outline.TreePathSupport;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * <p>Explorer view displaying nodes in a tree table.</p>
 * 
 * <p>Related documentation:
 * <ul>
 * <li><a href="http://weblogs.java.net/blog/timboudreau/archive/2008/06/egads_an_actual.html">Egads! An actual Swing Tree-Table!</a>
 * <li><a href="http://blogs.sun.com/geertjan/entry/swing_outline_component">Swing Outline Component</a>
 * </ul>
 * 
 * 
 * @author David Strupl
 */
public class OutlineView extends JScrollPane {

    private static final String TREE_HORIZONTAL_SCROLLBAR = "TREE_HORIZONTAL_SCROLLBAR";    // NOI18N

    private static RequestProcessor REVALIDATING_RP = new RequestProcessor("OutlineView", 1);   // NOI18N

    /** The table */
    private OutlineViewOutline outline;
    /** Explorer manager, valid when this view is showing */
    ExplorerManager manager;
    private final Object managerLock = new Object();
    /** not null if popup menu enabled */
    private PopupAdapter popupListener;
    /** the most important listener (on four types of events */
    private TableSelectionListener managerListener = null;
    /** weak variation of the listener for property change on the explorer manager */
    private PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    private VetoableChangeListener wlvc;
    
    private OutlineModel model;
    private NodeTreeModel treeModel;
    private PropertiesRowModel rowModel;
    /** */
    private NodePopupFactory popupFactory;
    
    /** true if drag support is active */
    private transient boolean dragActive = true;

    /** true if drop support is active */
    private transient boolean dropActive = true;

    /** Drag support */
    transient OutlineViewDragSupport dragSupport;

    /** Drop support */
    transient OutlineViewDropSupport dropSupport;
    transient boolean dropTargetPopupAllowed = true;

    // default DnD actions
    transient private int allowedDragActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    transient private int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;

    /** Listener on keystroke to invoke default action */
    private ActionListener defaultTreeActionListener;

    // whether to show horizontal scrollbar
    private boolean isTreeHScrollBar = false;  // Do not show tree horizontal scroll bar by default for compatibility reasons
    private JScrollBar hScrollBar;
    private int treeHorizontalScrollBarPolicy = isTreeHScrollBar ?
                                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED :
                                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
    private ScrollListener listener;
    
    private Selection selection = null;

    /** Creates a new instance of TableView */
    public OutlineView() {
        this(null);
    }    
    
    /** Creates a new instance of TableView */
    public OutlineView(String nodesColumnLabel) {
        treeModel = new NodeTreeModel();
        rowModel = new PropertiesRowModel();
        model = createOutlineModel(treeModel, rowModel, nodesColumnLabel);
        outline = new OutlineViewOutline(model, rowModel);
        rowModel.setOutline(outline);
        outline.setRenderDataProvider(new NodeRenderDataProvider(outline));
        SheetCell tableCell = new SheetCell.OutlineSheetCell(outline);
        outline.setDefaultRenderer(Node.Property.class, tableCell);
        outline.setDefaultEditor(Node.Property.class, tableCell);

        hScrollBar = createHorizontalScrollBar();
        hScrollBar.setUnitIncrement(10);
        setLayout(new OutlineScrollLayout());
        add(hScrollBar, TREE_HORIZONTAL_SCROLLBAR);

        setViewportView(outline);
        setPopupAllowed(true);
        // do not care about focus
        setRequestFocusEnabled (false);
        outline.setRequestFocusEnabled(true);
        java.awt.Color c = javax.swing.UIManager.getColor("Table.background1");
        if (c == null) {
            c = javax.swing.UIManager.getColor("Table.background");
        }
        if (c != null) {
            getViewport().setBackground(c);
        }
        getActionMap().put("org.openide.actions.PopupAction", new PopupAction());
        popupFactory = new OutlinePopupFactory();
        // activation of drop target
        setDropTarget( DragDropUtilities.dragAndDropEnabled );
        defaultTreeActionListener = new DefaultTreeAction (outline);
        outline.registerKeyboardAction(
            defaultTreeActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED
        );

        final Color focusSelectionBackground = outline.getSelectionBackground();
        final Color focusSelectionForeground = outline.getSelectionForeground();
        outline.addFocusListener(new java.awt.event.FocusListener(){
            public void focusGained(java.awt.event.FocusEvent ev) {
                outline.setSelectionBackground(focusSelectionBackground);
                outline.setSelectionForeground(focusSelectionForeground);
            }

            public void focusLost(java.awt.event.FocusEvent ev) {
                outline.setSelectionBackground(SheetCell.getNoFocusSelectionBackground());
                outline.setSelectionForeground(SheetCell.getNoFocusSelectionForeground());
            }

        });
        TableColumnSelector tcs = Lookup.getDefault ().lookup (TableColumnSelector.class);
        if (tcs != null) {
            outline.setColumnSelector(tcs);
        }

        if (UIManager.getColor("control") != null) { // NOI18N
            getOutline().setGridColor(UIManager.getColor("control")); // NOI18N
        }
        
        if (DragDropUtilities.dragAndDropEnabled ) {//&& dragActive) {
            setDragSource(true);
        }
        
        setBorder( BorderFactory.createEmptyBorder() );

        initializeTreeScrollSupport();
    }

    /**
     * This method allows plugging own OutlineModel to the OutlineView.
     * You can override it and create different model in the subclass.
     */
    protected OutlineModel createOutlineModel(NodeTreeModel treeModel, RowModel rowModel, String label) {
        if (label == null) {
            label = NbBundle.getMessage(OutlineView.class, "NodeOutlineModel_NodesColumnLabel"); // NOI18N
        }
        return new NodeOutlineModel(treeModel, rowModel, false, label);
    }

    /** Initialize support for horizontal scrolling.
     */
    private void initializeTreeScrollSupport() {
        if (UIManager.getColor("Table.background") != null) { // NOI18N
            getViewport().setBackground(UIManager.getColor("Table.background")); // NOI18N
        }

        listener = new ScrollListener();

        if (isTreeHScrollBar) {
            outline.getColumnModel().addColumnModelListener(listener);
        }

        final RequestProcessor.Task revalidatingTask = REVALIDATING_RP.create(new Runnable() {
            @Override
            public void run() {
                if (!SwingUtilities.isEventDispatchThread()) {
                    try {
                        SwingUtilities.invokeAndWait(this);
                    } catch (InterruptedException ex) {
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    listener.revalidateScrollBar();
                    //System.err.println("OutlineTask revalidating... :-)");
                    revalidate();
                }

            }
        });
        outline.setTreeWidthChangeTask(revalidatingTask);
        getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                revalidatingTask.schedule(100);
            }
        });

        hScrollBar.addAdjustmentListener(listener);
        hScrollBar.getModel().addChangeListener(listener);
    }

    /**
     * Returns the horizontal scroll bar policy value for the tree column.
     * @return the <code>treeHorizontalScrollBarPolicy</code> property
     * @see #setTreeHorizontalScrollBarPolicy
     * @since 6.30
     */
    public int getTreeHorizontalScrollBarPolicy() {
        return treeHorizontalScrollBarPolicy;
    }

    /**
     * Determines when the horizontal scrollbar appears in the tree column.
     * The options are:<ul>
     * <li><code>ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED</code>
     * <li><code>ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER</code>
     * <li><code>ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS</code>
     * </ul>
     *
     * @param policy one of the three values listed above
     * @exception IllegalArgumentException if <code>policy</code>
     *				is not one of the legal values shown above
     * @see #getTreeHorizontalScrollBarPolicy
     * @since 6.30
     *
     * @beaninfo
     *   preferred: true
     *       bound: true
     * description: The tree column scrollbar policy
     *        enum: HORIZONTAL_SCROLLBAR_AS_NEEDED ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
     *              HORIZONTAL_SCROLLBAR_NEVER ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
     *              HORIZONTAL_SCROLLBAR_ALWAYS ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
     */
    public void setTreeHorizontalScrollBarPolicy(int policy) {
        if (policy == treeHorizontalScrollBarPolicy) {
            return ;
        }
	switch (policy) {
            case HORIZONTAL_SCROLLBAR_AS_NEEDED:
            case HORIZONTAL_SCROLLBAR_NEVER:
            case HORIZONTAL_SCROLLBAR_ALWAYS:
                    break;
            default:
                throw new IllegalArgumentException("invalid treeHorizontalScrollBarPolicy");
	}
	int old = treeHorizontalScrollBarPolicy;
	treeHorizontalScrollBarPolicy = policy;
        boolean wasHScrollBarVisible = isTreeHScrollBar;
        isTreeHScrollBar = (policy != JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (wasHScrollBarVisible != isTreeHScrollBar) {
            if (!wasHScrollBarVisible) {
                outline.getColumnModel().addColumnModelListener(listener);
            } else {
                outline.getColumnModel().removeColumnModelListener(listener);
            }
            outline.setTreeHScrollingEnabled(isTreeHScrollBar, hScrollBar);
        }
	firePropertyChange("treeHorizontalScrollBarPolicy", old, policy);
	revalidate();
	repaint();
    }

    private boolean horizontalScrollBarIsNeeded = false;
    
    private void sayHorizontalScrollBarNeeded(boolean horizontalScrollBarIsNeeded) {
        this.horizontalScrollBarIsNeeded = horizontalScrollBarIsNeeded;
    }

    @Override
    public int getHorizontalScrollBarPolicy() {
        if (horizontalScrollBarIsNeeded) {
            return JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
        } else {
            return super.getHorizontalScrollBarPolicy();
        }
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public void requestFocus () {
        outline.requestFocus();
    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow () {
        return outline.requestFocusInWindow();
    }
    
    /**
     * Getter for the embeded table component.
     */
    public Outline getOutline() {
        return outline;
    }
    
    /** Is it permitted to display a popup menu?
     * @return <code>true</code> if so
     */
    public boolean isPopupAllowed () {
        return popupListener != null;
    }

    /**
     * Set the properties which are shown in the non-tree columns of this
     * Outline view.
     * <p>
     * The passed set of properties are typically
     * <i>prototypes</i> - for a given Node's
     * property to be shown in a given column, that Node must have a Property
     * which <code>equals()</code> one of the prototype properties.  By default,
     * this means that the return values of the prototype property's getName()
     * and getValueType() must exactly match.
     * <p>
     * It is also possible to use the actual Property objects from one Node
     * being shown, if they are available.
     *
     * @deprecated This method is here to enable easy replacement
     * of TreeTableView with OutlineView.
     * Use setPropertyColumns(), addPropertyColumn() and
     * removePropertyColumn() instead.
     * @param newProperties An array of prototype properties
     */
    @Deprecated
    public void setProperties(Node.Property[] newProperties) {
        setProperties(newProperties, true);
    }

    private void setProperties(Node.Property[] newProperties, boolean doCleanColumns) {
        if (doCleanColumns) {
            TableColumnModel tcm = outline.getColumnModel();
            if (tcm instanceof ETableColumnModel) {
                ((ETableColumnModel) tcm).clean();
            }
        }
        rowModel.setProperties(newProperties);
        outline.tableChanged(null);
    }

    /**
     * Adds a property column which will match any property with the passed
     * name.
     *
     * @param name The programmatic name of the property
     * @param displayName A localized display name for the property which can
     * be shown in the table header
     * @since 6.25
     */
    public final void addPropertyColumn(String name, String displayName) {
        addPropertyColumn (name, displayName, null);
    }
    /**
     * Adds a property column which will match any property with the passed
     * name.
     *
     * @param name The programmatic name of the property
     * @param displayName A localized display name for the property which can
     * be shown in the table header
     * @param description The description which will be used as a tooltip in
     * the table header
     * @since 6.25
     */
    public final void addPropertyColumn(String name, String displayName, String description) {
        Parameters.notNull("name", name); //NOI18N
        Parameters.notNull("displayName", displayName); //NOI18N
        Property[] p = rowModel.getProperties();
        Property[] nue = new Property[p.length + 1];
        System.arraycopy(p, 0, nue, 0, p.length);
        nue[p.length] = new PrototypeProperty(name, displayName, description);
        setProperties (nue, false);
    }

    /**
     * Remove the first property column for properties named <code>name</code>
     * @param name The <i>programmatic</i> name of the Property, i.e. the
     * return value of <code>Property.getName()</code>
     *
     * @return true if a column was removed
     * @since 6.25
     */
    public final boolean removePropertyColumn(String name) {
        Parameters.notNull("name", name); //NOI18N
        Property[] props = rowModel.getProperties();
        List<Property> nue = new LinkedList<Property>(Arrays.asList(props));
        boolean found = false;
        for (Iterator<Property> i=nue.iterator(); i.hasNext();) {
            Property p = i.next();
            if (name.equals(p.getName())) {
                found = true;
                i.remove();
                break;
            }
        }
        if (found) {
            props = nue.toArray(new Property[props.length - 1]);
            setProperties (props, false);
        }
        return found;
    }

    /**
     * Set the description (table header tooltip) for the property column
     * representing properties that have the passed programmatic (not display)
     * name, or for the tree column.
     *
     * @param columnName The programmatic name (Property.getName()) of the
     * column, or name of the tree column
     * @param description Tooltip text for the column header for that column
     */
    public final void setPropertyColumnDescription(String columnName, String description) {
        Parameters.notNull ("columnName", columnName); //NOI18N
        Property[] props = rowModel.getProperties();
        for (Property p : props) {
            if (columnName.equals(p.getName())) {
                p.setShortDescription(description);
            }
        }
        if (columnName.equals(model.getColumnName(0))) {
            outline.setNodesColumnDescription(description);
        }
    }

    /**
     * Set all of the non-tree columns property names and display names.
     *
     * @param namesAndDisplayNames An array, divisible by 2, of
     * programmatic name, display name, programmatic name, display name...
     * @since 6.25
     */
    public final void setPropertyColumns(String... namesAndDisplayNames) {
        if (namesAndDisplayNames.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of names and " + //NOI18N
                    "display names: " + Arrays.asList(namesAndDisplayNames)); //NOI18N
        }
        Property[] props = new Property[namesAndDisplayNames.length / 2];
        for (int i = 0; i < namesAndDisplayNames.length; i+=2) {
            props[i / 2] = new PrototypeProperty (namesAndDisplayNames[i], namesAndDisplayNames[i+1]);
        }
        setProperties (props, true);
    }
    
    /** Enable/disable displaying popup menus on tree view items.
    * Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed (boolean value) {
        if (popupListener == null && value) {
            // on
            popupListener = new PopupAdapter ();
            outline.addMouseListener (popupListener);
            addMouseListener(popupListener);
            return;
        }
        if (popupListener != null && !value) {
            // off
            outline.removeMouseListener (popupListener);
            removeMouseListener (popupListener);
            popupListener = null;
            return;
        }
    }

    /**
     * Enable/disable double click to invoke default action.
     * If the default action is not enabled, double click expand/collapse node.
     * @param defaultActionAllowed Provide <code>true</code> to enable
     * @see {@link #isDefaultActionAllowed()}
     * @since 6.32
     */
    public void setDefaultActionAllowed(boolean defaultActionAllowed) {
        outline.setDefaultActionAllowed(defaultActionAllowed);
    }

    /**
     * Tells if double click invokes default action.
     * @return <code>true</code> if the default action is invoked, or <code>false</code> when it's not.
     * @see {@link #setDefaultActionAllowed(boolean)}
     * @since 6.32
     */
    public boolean isDefaultActionAllowed() {
        return outline.isDefaultActionAllowed();
    }

    /**
     * Set the tree column as sortable
     * @param treeSortable <code>true</code> to make the tree column sortable,
     *        <code>false</code> otherwise. The tree column is sortable by default.
     * @since 6.24
     */
    public void setTreeSortable(boolean treeSortable) {
        outline.setTreeSortable(treeSortable);
    }
    
    /** Initializes the component and lookup explorer manager.
     */
    @Override
    public void addNotify () {
        super.addNotify ();
        lookupExplorerManager ();
        ViewUtil.adjustBackground(outline);
        ViewUtil.adjustBackground(getViewport());
        if (selection != null) {
            selection.setTo(outline.getSelectionModel());
        }
    }
    
    /**
     * Method allowing to read stored values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void readSettings(Properties p, String propertyPrefix) {
        outline.readSettings(p, propertyPrefix);
    }

    /**
     * Method allowing to store customization values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void writeSettings(Properties p, String propertyPrefix) {
        outline.writeSettings(p, propertyPrefix);
    }

    /**
     * Allows customization of the popup menus.
     */
    public void setNodePopupFactory(NodePopupFactory newFactory) {
        popupFactory = newFactory;
    }
    
    /**
     * Getter for the current popup customizer factory.
     */
    public NodePopupFactory getNodePopupFactory() {
        return popupFactory;
    }
    
    /** Registers in the tree of components.
     */
    private void lookupExplorerManager () {
    // Enter key in the tree

        if (managerListener == null) {
            managerListener = new TableSelectionListener();
        }
        
        ExplorerManager newManager = ExplorerManager.find(this);
        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener (wlvc);
                manager.removePropertyChangeListener (wlpc);
            }

            manager = newManager;

            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));
        }
        
        synchronizeRootContext();
        synchronizeSelectedNodes(true);
        
        // Sometimes the listener is registered twice and we get the 
        // selection events twice. Removing the listener before adding it
        // should be a safe fix.
        outline.getSelectionModel().removeListSelectionListener(managerListener);
        outline.getSelectionModel().addListSelectionListener(managerListener);
    }
    
    /** Synchronize the root context from the manager of this Explorer.
    */
    final void synchronizeRootContext() {
        if( null != treeModel ) {
            treeModel.setNode(manager.getRootContext());
        }
    }

    /** Synchronize the selected nodes from the manager of this Explorer.
     */
    final void synchronizeSelectedNodes(boolean scroll, Node... nodes) {
        if (! needToSynchronize ()) {
            return ;
        }
        expandSelection();
        outline.invalidate();
        invalidate();
        validate();
        Node[] arr = manager.getSelectedNodes ();
        outline.getSelectionModel().clearSelection();
        int size = outline.getRowCount();
        int firstSelection = -1;
        for (int i = 0; i < size; i++) {
            Node n = getNodeFromRow(i);
            for (int j = 0; j < arr.length; j++) {
                if ((n != null) && (n.equals(arr[j]))) {
                    outline.getSelectionModel().addSelectionInterval(i, i);
                    if (firstSelection == -1) {
                        firstSelection = i;
                    }
                }
            }
        }
        if (scroll && (firstSelection >= 0)) {
            JViewport v = getViewport();
            if (v != null) {
                Rectangle rect = outline.getCellRect(firstSelection, 0, true);
                if (v.getExtentSize().height > rect.height) {
                    rect.height = v.getExtentSize().height;
                }
                int ho = outline.getSize().height;
                if (ho > 0) {
                    if (rect.y + rect.height > ho) {
                        rect.height = ho - rect.y;
                        if (rect.height <= 0) {
                            rect.height = 40;
                        }
                    }
                }
                v.setViewPosition(new Point()); // strange line - but without
                                                // it the next one is wrong
                outline.scrollRectToVisible(rect);
            }
        }
    }

    private boolean needToSynchronize () {
        boolean doSync = false;
        Node [] arr = manager.getSelectedNodes ();
        if (outline.getSelectedRows ().length != arr.length) {
            doSync = true;
        } else if (arr.length > 0) {
            List<Node> nodes = Arrays.asList (arr);
            for (int idx : outline.getSelectedRows ()) {
                Node n = getNodeFromRow (idx);
                if (n == null || ! nodes.contains (n)) {
                    doSync = true;
                    break;
                }
            }
        }
        return doSync;
    }

    /**
     * Tries to expand nodes selected in the explorer manager.
     */
    private void expandSelection() {
        Node[] arr = manager.getSelectedNodes ();
        for (int i = 0; i < arr.length; i++) {
            if ( (arr[i].getParentNode() == null) && (! outline.isRootVisible())) {
                // don't try to show root if it is invisible
                continue;
            }
            TreeNode tn = Visualizer.findVisualizer(arr[i]);
            if (tn != null) {
                ArrayList<TreeNode> al = new ArrayList<TreeNode> ();
                while (tn != null) {
                    al.add(tn);
                    tn = tn.getParent();
                }
                Collections.reverse(al);
                TreePath tp = new TreePath(al.toArray());
                while ((tp != null) && (tp.getPathCount() > 0)) {
                    tp = tp.getParentPath();
                    if (tp != null) {
                        outline.expandPath(tp);
                    }
                }
            }
        }
    }
    
    /**
     * Deinitializes listeners.
     */
    @Override
    public void removeNotify () {
        super.removeNotify ();
        selection = new Selection(outline.getSelectionModel());
        outline.getSelectionModel().clearSelection();
        outline.getSelectionModel().removeListSelectionListener(managerListener);
        synchronized (managerLock) {
            if (manager != null) {
                manager.removePropertyChangeListener (wlpc);
                manager.removeVetoableChangeListener (wlvc);
                manager = null;
            }
        }
    }

    /**
     * Shows popup menu invoked on the table.
     */
    void showPopup(int xpos, int ypos, final JPopupMenu popup) {
        if ((popup != null) && (popup.getSubElements().length > 0)) {
            final PopupMenuListener p = new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popup.removePopupMenuListener(this);
                    outline.requestFocus();
                }
                public void popupMenuCanceled(PopupMenuEvent e) {
                    
                }
            };
            popup.addPopupMenuListener(p);
            popup.show(this, xpos, ypos);
        }
    }    
    
    /**
     * Find relevant actions and call the factory to create a popup.
     */
    private JPopupMenu createPopup(Point p) {
        int[] selRows = outline.getSelectedRows();
        ArrayList<Node> al = new ArrayList<Node> (selRows.length);
        for (int i = 0; i < selRows.length; i++) {
            Node n = getNodeFromRow(selRows[i]);
            if (n != null) {
                al.add(n);
            }
        }
        Node[] arr = al.toArray (new Node[al.size ()]);
        if (arr.length == 0) {
            if (manager.getRootContext() != null) {
                // display the context menu of the root node
                JPopupMenu popup = manager.getRootContext().getContextMenu();
                if (popup != null && popup.getSubElements().length > 0) {
                    popupFactory.addNoFilterItem(outline, popup);
                    return popup;
                }
            }
            // we'll have an empty popup
        }
        p = SwingUtilities.convertPoint(this, p, outline);
        int column = outline.columnAtPoint(p);
        int row = outline.rowAtPoint(p);
        return popupFactory.createPopupMenu(row, column, arr, outline);
    }
    
    /**
     * 
     */
    Node getNodeFromRow(int rowIndex) {
        int row = outline.convertRowIndexToModel(rowIndex);
        TreePath tp = outline.getLayoutCache().getPathForRow(row);
        if (tp == null) {
            return null;
        }
        return Visualizer.findNode(tp.getLastPathComponent());
    }
    
    /** Returns the point at which the popup menu is to be shown. May return null.
     * @return the point or null
     */    
    private Point getPositionForPopup () {
        int i = outline.getSelectionModel().getLeadSelectionIndex();
        if (i < 0) return null;
        int j = outline.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if (j < 0) {
            j = 0;
        }

        Rectangle rect = outline.getCellRect(i, j, true);
        if (rect == null) return null;

        Point p = new Point(rect.x + rect.width / 3,
                rect.y + rect.height / 2);
        
        // bugfix #36984, convert point by TableView.this
        p =  SwingUtilities.convertPoint (outline, p, OutlineView.this);

        return p;
    }

    /**
     * Action registered in the component's action map.
     */
    private class PopupAction extends javax.swing.AbstractAction implements Runnable {
        public void actionPerformed(ActionEvent evt) {
            SwingUtilities.invokeLater(this);
        }
        public void run() {
            Point p = getPositionForPopup ();
            if (p == null) {
                return ;
            }
            if (isPopupAllowed()) {
                JPopupMenu pop = createPopup(p);
                showPopup(p.x, p.y, pop);
            }
        }
    };
    
    /**
     * Mouse listener that invokes popup.
     */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

	PopupAdapter() {}
	
        protected void showPopup (MouseEvent e) {
            int selRow = outline.rowAtPoint(e.getPoint());

            if (selRow != -1) {
                if (! outline.getSelectionModel().isSelectedIndex(selRow)) {
                    outline.getSelectionModel().clearSelection();
                    outline.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
            } else {
                outline.getSelectionModel().clearSelection();
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), OutlineView.this);
            if (isPopupAllowed()) {
                JPopupMenu pop = createPopup(p);
                OutlineView.this.showPopup(p.x, p.y, pop);
                e.consume();
            }
        }

    }

    /**
     * Called when selection in tree is changed.
     */
    final private void callSelectionChanged (Node[] nodes) {
        manager.removePropertyChangeListener (wlpc);
        manager.removeVetoableChangeListener (wlvc);
        try {
            manager.setSelectedNodes(nodes);
        } catch (PropertyVetoException e) {
            synchronizeSelectedNodes(false);
        } finally {
            // to be sure not to add them twice!
            manager.removePropertyChangeListener (wlpc);
            manager.removeVetoableChangeListener (wlvc);
            manager.addPropertyChangeListener (wlpc);
            manager.addVetoableChangeListener (wlvc);
        }
    }
    
    /** 
     * Check if selection of the nodes could break
     * the selection mode set in the ListSelectionModel.
     * @param nodes the nodes for selection
     * @return true if the selection mode is broken
     */
    private boolean isSelectionModeBroken(Node[] nodes) {
        
        // if nodes are empty or single then everthing is ok
        // or if discontiguous selection then everthing ok
        if (nodes.length <= 1 || outline.getSelectionModel().getSelectionMode() == 
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
            return false;
        }

        // if many nodes
        
        // breaks single selection mode
        if (outline.getSelectionModel().getSelectionMode() == 
            ListSelectionModel.SINGLE_SELECTION) {
            return true;
        }
        
        // check the contiguous selection mode

        // check selection's rows
        
        // all is ok
        return false;
    }

    /********** Support for the Drag & Drop operations *********/
    /** Drag support is enabled by default.
    * @return true if dragging from the view is enabled, false
    * otherwise.
    */
    public boolean isDragSource() {
        return dragActive;
    }

    /** Enables/disables dragging support.
    * @param state true enables dragging support, false disables it.
    */
    public void setDragSource(boolean state) {
        // create drag support if needed
        if (state && (dragSupport == null)) {
            dragSupport = new OutlineViewDragSupport(this, getOutline());
        }

        // activate / deactivate support according to the state
        dragActive = state;

        if (dragSupport != null) {
            dragSupport.activate(dragActive);
        }
    }

    /** Drop support is enabled by default.
    * @return true if dropping to the view is enabled, false
    * otherwise<br>
    */
    public boolean isDropTarget() {
        return dropActive;
    }

    /** Enables/disables dropping support.
    * @param state true means drops into view are allowed,
    * false forbids any drops into this view.
    */
    public void setDropTarget(boolean state) {
        // create drop support if needed
        if (dropActive && (dropSupport == null)) {
            dropSupport = new OutlineViewDropSupport(this, outline, dropTargetPopupAllowed);
        }

        // activate / deactivate support according to the state
        dropActive = state;

        if (dropSupport != null) {
            dropSupport.activate(dropActive);
        }
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions (copy, move, link) are allowed by default.
    * @return int representing set of actions which are allowed when dragging from
    * asociated component.
     */
    public int getAllowedDragActions() {
        return allowedDragActions;
    }

    /** Sets allowed actions for dragging
    * @param actions new drag actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDragActions(int actions) {
        // PENDING: check parameters
        allowedDragActions = actions;
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions are allowed by default.
    * @return int representing set of actions which are allowed when dropping
    * into the asociated component.
    */
    public int getAllowedDropActions() {
        return allowedDropActions;
    }

    /** Actions constants from {@link java.awt.dnd.DnDConstants}.
    * @param t The transferable for which the allowed drop actions are requested
    * @return int representing set of actions which are allowed when dropping
    * into the asociated component. By default it returns {@link #getAllowedDropActions()}.
    */
    protected int getAllowedDropActions(Transferable t) {
        return getAllowedDropActions();
    }

    /** Sets allowed actions for dropping.
    * @param actions new allowed drop actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDropActions(int actions) {
        // PENDING: check parameters
        allowedDropActions = actions;
    }
    
    public void addTreeExpansionListener( TreeExpansionListener l ) {
        TreePathSupport tps = getOutline().getOutlineModel().getTreePathSupport();
        if( tps != null )
            tps.addTreeExpansionListener(l);
    }
    
    public void removeTreeExpansionListener( TreeExpansionListener l ) {
        TreePathSupport tps = getOutline().getOutlineModel().getTreePathSupport();
        if( tps != null )
            tps.removeTreeExpansionListener(l);
    }

    /** Collapses the tree under given node.
    *
    * @param n node to collapse
    */
    public void collapseNode(Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));
        getOutline().collapsePath(treePath);
    }

    /** Expandes the node in the tree.
    *
    * @param n node
    */
    public void expandNode(Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        lookupExplorerManager();

        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));

        getOutline().expandPath(treePath);
    }

    /** Test whether a node is expanded in the tree or not
    * @param n the node to test
    * @return true if the node is expanded
    */
    public boolean isExpanded(Node n) {
        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));
        return getOutline().isExpanded(treePath);
    }
    
    /**
     * Listener attached to the explorer manager and also to the
     * changes in the table selection.
     */
    private class TableSelectionListener implements VetoableChangeListener, ListSelectionListener, PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            synchronized (managerLock) {
                if (manager == null) return; // the tree view has been removed before the event got delivered
                if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
                    synchronizeRootContext();
                }
                if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    synchronizeSelectedNodes(true);
                }
            }
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
            int selectedRows[] = outline.getSelectedRows();
            ArrayList<Node> selectedNodes = new ArrayList<Node> (selectedRows.length);
            for (int i = 0; i < selectedRows.length;i++) {
                Node n = getNodeFromRow(selectedRows[i]);
                if (n != null) {
                    selectedNodes.add(n);
                }
            }
            callSelectionChanged(selectedNodes.toArray (new Node[selectedNodes.size ()]));
        }

        public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                // issue 11928 check if selecetion mode will be broken
                Node[] nodes = (Node[])evt.getNewValue();
                if (isSelectionModeBroken(nodes)) {
                    throw new PropertyVetoException("selection mode " +  " broken by " + Arrays.asList(nodes), evt); // NOI18N
                }
            }
        }
    }


    /** Invokes default action.
     */
    private class DefaultTreeAction implements ActionListener {

        private Outline outline;

        DefaultTreeAction (Outline outline) {
            this.outline = outline;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed (ActionEvent e) {
            if (outline.getSelectedColumn () != 0) {
                return;
            }

            Node[] nodes = manager.getSelectedNodes ();
            TreeView.performPreferredActionOnNodes(nodes);
        }
    }

    /**
     * Extension of the ETable that allows adding a special comparator
     * for sorting the rows.
     */
    static class OutlineViewOutline extends Outline {
        private final PropertiesRowModel rowModel;
        private static final String COLUMNS_SELECTOR_HINT = "ColumnsSelectorHint"; // NOI18N

        private boolean treeSortable = true;

        private boolean isHScrollingEnabled;
        private JScrollBar hScrollBar;
        private TMScrollingListener tmScrollingListener;
        private int treePositionX = 0;
        private int[] rowWidths;
        private RequestProcessor.Task changeTask;
        private boolean defaultActionAllowed = true;
        private String nodesColumnDescription;
        //private int maxRowWidth;

        public OutlineViewOutline(final OutlineModel mdl, PropertiesRowModel rowModel) {
            super(mdl);
            this.rowModel = rowModel;
            setSelectVisibleColumnsLabel(NbBundle.getMessage(OutlineView.class, "CTL_ColumnsSelector")); //NOI18N
            
            // fix for #198694
            // default action map for JTable defines these shortcuts
            // but we use our own mechanism for handling them
            // following lines disable default L&F handling (if it is
            // defined on Ctrl-c, Ctrl-v and Ctrl-x)
            removeDefaultCutCopyPaste(getInputMap(WHEN_FOCUSED));
            removeDefaultCutCopyPaste(getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        }
        
        private void removeDefaultCutCopyPaste(InputMap map) {
            map.put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
            map.put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
            map.put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
            map.put(KeyStroke.getKeyStroke("COPY"), "none"); // NOI18N
            map.put(KeyStroke.getKeyStroke("PASTE"), "none"); // NOI18N
            map.put(KeyStroke.getKeyStroke("CUT"), "none"); // NOI18N

            if (Utilities.isMac()) {
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK), "none"); // NOI18N
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK), "none"); // NOI18N
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_MASK), "none"); // NOI18N
            } else {
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "none"); // NOI18N
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "none"); // NOI18N
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "none"); // NOI18N
            }
        }

        PropertiesRowModel getRowModel() {
            return rowModel;
        }
        
        @Override
        public Object transformValue(Object value) {
            if (value instanceof OutlineViewOutlineColumn) {
                OutlineViewOutlineColumn c = (OutlineViewOutlineColumn) value;
                String dn = c.getRawColumnName ();
                if (dn == null) {
                    dn = c.getHeaderValue ().toString ();
                }
                String desc = c.getShortDescription (null);
                if (desc == null) {
                    return dn;
                }
                return NbBundle.getMessage (OutlineView.class, "OutlineViewOutline_NameAndDesc", dn, desc); // NOI18N
            } else if (COLUMNS_SELECTOR_HINT.equals (value)) {
                return NbBundle.getMessage (OutlineView.class, COLUMNS_SELECTOR_HINT);
            } else if (value instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) value;
                Mnemonics.setLocalizedText (b, b.getText ());
                return b;
            } else if (value instanceof VisualizerNode) {
                return Visualizer.findNode (value);
            }
            return PropertiesRowModel.getValueFromProperty(value);
        }

        void setTreeHScrollingEnabled(boolean isHScrollingEnabled, JScrollBar hScrollBar) {
            this.isHScrollingEnabled = isHScrollingEnabled;
            this.hScrollBar = hScrollBar;
            if (isHScrollingEnabled) {
                if (tmScrollingListener == null) {
                    tmScrollingListener = new TMScrollingListener();
                    rowWidths = new int[getOutlineModel().getRowCount()];
                    getOutlineModel().addTableModelListener(tmScrollingListener);
                    getOutlineModel().addTreeModelListener(tmScrollingListener);
                }
            } else {
                if (tmScrollingListener != null) {
                    getOutlineModel().removeTableModelListener(tmScrollingListener);
                    getOutlineModel().removeTreeModelListener(tmScrollingListener);
                    tmScrollingListener = null;
                    rowWidths = null;
                }
            }
        }

        void setTreeWidthChangeTask(RequestProcessor.Task changeTask) {
            this.changeTask = changeTask;
        }

        int getTreePositionX() {
            return treePositionX;
        }

        void setTreePositionX(int treePositionX) {
            if (treePositionX == this.treePositionX) {
                return ;
            }
            this.treePositionX = treePositionX;
            tableChanged(new TableModelEvent(getModel(), 0, getRowCount(), 0));// convertColumnIndexToView(0)));
        }

        private void setPreferredTreeWidth(int row, int width) {
            if (isHScrollingEnabled && rowWidths[row] != width) {
                rowWidths[row] = width;
                changeTask.schedule(100);
            }
        }

        int getTreePreferredWidth() {
            //int ci = convertColumnIndexToView(0);
            Rectangle visibleRect = getVisibleRect();
            int r1 = rowAtPoint(new Point(0, visibleRect.y));
            if (r1 < 0) {
                return 0;
            }
            if (hScrollBar.isVisible()) {
                // To prevent from "dancing" include the width of the row(s) under the horizontal scroll bar as well.
                visibleRect.height += hScrollBar.getSize().height;
            }
            int r2 = rowAtPoint(new Point(0, visibleRect.y + visibleRect.height));
            if (r2 < 0) r2 = getRowCount() - 1;
            int width = 0;
            for (int r = r1; r <= r2; r++) {
                if (rowWidths[r] > width) {
                    width = rowWidths[r];
                }
            }
            width += 2*getIntercellSpacing().width;
            return width;
        }

        void setDefaultActionAllowed(boolean defaultActionAllowed) {
            this.defaultActionAllowed = defaultActionAllowed;
        }
        
        boolean isDefaultActionAllowed() {
            return defaultActionAllowed;
        }
        
        /** Translate the tree column renderer */
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            TableCellRenderer result = super.getCellRenderer(row, column);
            if (isHScrollingEnabled) {
                int c = convertColumnIndexToModel(column);
                if (c == 0) {
                    result = new TranslatedTableCellRenderer(this, result);
                }
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean editCellAt(int row, int column, EventObject e) {
            Object o = getValueAt(row, column);
            if (o instanceof Node.Property) { // && (e == null || e instanceof KeyEvent)) {
                Node.Property p = (Node.Property)o;
                if (p.getValueType() == Boolean.class || p.getValueType() == Boolean.TYPE) {
                    PropertiesRowModel.toggleBooleanProperty(p);
                    Rectangle r = getCellRect(row, column, true);
                    repaint (r.x, r.y, r.width, r.height);
                    return false;
                }
            }
            boolean res = false;
            boolean actionPerformed = false;
            boolean isTreeColumn = convertColumnIndexToModel(column) == 0;
            if (isTreeColumn && row != -1 && e instanceof MouseEvent && SwingUtilities.isLeftMouseButton ((MouseEvent) e)) {
                int clickCount = ((MouseEvent) e).getClickCount();
                if (clickCount > 1) {
                    // Default action.
                    Node node = Visualizer.findNode (o);
                    if (node != null) {
                        if (defaultActionAllowed) {
                            Action a = TreeView.takeAction (node.getPreferredAction (), node);

                            if (a != null) {
                                if (a.isEnabled ()) {
                                    a.actionPerformed (new ActionEvent (node, ActionEvent.ACTION_PERFORMED, "", ((MouseEvent) e).getModifiers())); // NOI18N
                                    return false;
                                } else {
                                    Logger.getLogger (OutlineView.class.getName ()).info ("Action " + a + " on node " + node + " is disabled");
                                }
                            }
                        }
                    }
                }
            }
            if(e instanceof MouseEvent ) {
                //try invoking custom editor

                final Rectangle r = getCellRect(row, column, true);
                MouseEvent me = (MouseEvent) e;
                me.translatePoint(treePositionX, 0);
                int x = me.getX();
                if ( x > ((r.x + r.width) - 24) && x < (r.x + r.width)
                    && o instanceof Node.Property
                    && !isTreeColumn ) {

                    Node.Property p = (Node.Property)o;
                    if( !Boolean.TRUE.equals(p.getValue("suppressCustomEditor") ) ) { //NOI18N
                        PropertyPanel panel = new PropertyPanel(p);
                        @SuppressWarnings("deprecation")
                        PropertyEditor ed = panel.getPropertyEditor();

                        if ((ed != null) && ed.supportsCustomEditor()) {
                            Action act = panel.getActionMap().get("invokeCustomEditor"); //NOI18N

                            if (act != null) {
                                SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            r.x = 0;
                                            r.width = getWidth();
                                            OutlineViewOutline.this.repaint(r);
                                        }
                                    }
                                );
                                act.actionPerformed(null);

                                return false;
                            }
                        }
                    }
                }
            }
            return super.editCellAt(row, column, e);
        }
        
        @Override
        protected TableColumn createColumn(int modelIndex) {
            return new OutlineViewOutlineColumn(modelIndex);
        }

        boolean isTreeSortable() {
            return this.treeSortable;
        }

        void setTreeSortable(boolean treeSortable) {
            this.treeSortable = treeSortable;
        }

        private void setNodesColumnDescription(String description) {
            nodesColumnDescription = description;
        }


        /**
         * Extension of ETableColumn using TableViewRowComparator as
         * comparator.
         */
        private class OutlineViewOutlineColumn extends OutlineColumn {
            private String tooltip;
            private final Comparator originalNodeComparator = new NodeNestedComparator ();

            public OutlineViewOutlineColumn(int index) {
                super(index);
            }
            @Override
            public boolean isSortingAllowed() {
                int index = getModelIndex();
                Object sortable;
                if (index > 0) {
                    sortable = rowModel.getPropertyValue("SortableColumn", index - 1); // NOI18N
                } else {
                    return isTreeSortable();
                }
                if (sortable != null) {
                    return Boolean.TRUE.equals(sortable);
                }
                return super.isSortingAllowed();
            }

            @Override
            public Comparator getNestedComparator () {
                // it it's the tree column
                if (getModelIndex () == 0 && super.getNestedComparator () == null) {
                    return originalNodeComparator;
                }
                return super.getNestedComparator ();
            }

            @Override
            protected TableCellRenderer createDefaultHeaderRenderer() {
                TableCellRenderer orig = super.createDefaultHeaderRenderer();
                OutlineViewOutlineHeaderRenderer ovohr = new OutlineViewOutlineHeaderRenderer(orig);
                return ovohr;
            }

            public String getShortDescription (String defaultValue) {
                TableModel model = getModel();
                if (model.getRowCount() <= 0) {
                    return null;
                }
                if (getModelIndex () == 0) {
                    // 1st column
                    if (nodesColumnDescription != null) {
                        return nodesColumnDescription;
                    }
                    return defaultValue;
                }
                return rowModel.getShortDescription (getModelIndex () - 1);
            }

            public String getRawColumnName () {
                TableModel model = getModel();
                if (model.getRowCount() <= 0) {
                    return null;
                }
                if (getModelIndex () == 0) {
                    return null;
                }
                return rowModel.getRawColumnName (getModelIndex () - 1);
            }

            /** This is here to compute and set the header tooltip. */
            class OutlineViewOutlineHeaderRenderer implements TableCellRenderer {
                private TableCellRenderer orig;
                public OutlineViewOutlineHeaderRenderer(TableCellRenderer delegate) {
                    orig = delegate;
                }
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component oc = orig.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (tooltip == null) {
                        tooltip = getShortDescription (value.toString ());
                    }
                    if ((tooltip != null) && (oc instanceof JComponent)) {
                        JComponent jc = (JComponent)oc;
                        jc.setToolTipText(tooltip);
                    }
                    return oc;
                }
            }

            private class NodeNestedComparator implements
                    Comparator {
                public int compare (Object o1, Object o2) {
                    assert o1 instanceof Node : o1 + " is instanceof Node";
                    assert o2 instanceof Node : o2 + " is instanceof Node";
                    return ((Node) o1).getDisplayName ().compareTo (((Node) o2).getDisplayName ());
                }
            }

        }

        private class TranslatedTableCellRenderer extends JComponent implements TableCellRenderer {

            private OutlineViewOutline outline;
            private TableCellRenderer delegate;
            private Component component;

            public TranslatedTableCellRenderer(OutlineViewOutline outline, TableCellRenderer delegate) {
                this.outline = outline;
                this.delegate = delegate;
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                this.component = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setPreferredTreeWidth(row, this.component.getPreferredSize().width);
                return this;
            }

            @Override
            public void setBounds(int x, int y, int width, int height) {
                //System.out.println("setBounds("+x+", "+y+", "+width+", "+height+"), translate = "+outline.getTreePositionX());
                component.setBounds(x, y, Math.max(width, outline.getTreePreferredWidth()), height);
            }

            @Override
            public Dimension getPreferredSize() {
                return component.getPreferredSize();
            }

            @Override
            public void paint(Graphics g) {
                if (!(component instanceof TranslatedTableCellRenderer)) {
                    g.translate(-outline.getTreePositionX(), 0);
                }
                try {
                    component.paint(g);
                } catch (NullPointerException npe) {
                    // http://netbeans.org/bugzilla/show_bug.cgi?id=194055
                    javax.swing.border.Border border = null;
                    Exceptions.printStackTrace(Exceptions.attachMessage(npe,
                            "Failed painting of component "+component+
                            " with border "+((component instanceof JComponent) ? (border = ((JComponent) component).getBorder()) : null)+
                            ((border instanceof javax.swing.border.CompoundBorder) ?
                                ", with outsideBorder = "+((javax.swing.border.CompoundBorder) border).getOutsideBorder()+
                                " and insideBorder = "+((javax.swing.border.CompoundBorder) border).getInsideBorder() : "")
                    ));
                }
            }

            @Override
            public String getToolTipText() {
                if (component instanceof JComponent) {
                    return ((JComponent) component).getToolTipText();
                } else {
                    return super.getToolTipText();
                }
            }

            @Override
            public Point getToolTipLocation(MouseEvent event) {
                if (component instanceof JComponent) {
                    return ((JComponent) component).getToolTipLocation(event);
                } else {
                    return super.getToolTipLocation(event);
                }
            }

            @Override
            public String getToolTipText(MouseEvent event) {
                if (component instanceof JComponent) {
                    return ((JComponent) component).getToolTipText(event);
                } else {
                    return super.getToolTipText(event);
                }
            }

            @Override
            public JToolTip createToolTip() {
                if (component instanceof JComponent) {
                    return ((JComponent) component).createToolTip();
                } else {
                    return super.createToolTip();
                }
            }

        }
        
        private class TMScrollingListener implements TableModelListener, TreeModelListener {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateRowWidths();
            }

            // TreeModelListener is usually notified sooner
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                updateRowWidths();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                updateRowWidths();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                updateRowWidths();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                updateRowWidths();
            }
            
            private void updateRowWidths() {
                int rowCount = getOutlineModel().getRowCount();
                if (rowCount != rowWidths.length) {
                    rowWidths = Arrays.copyOf(rowWidths, rowCount);
                }
            }

        }
        
    }
    
    private static class OutlinePopupFactory extends NodePopupFactory {
        public OutlinePopupFactory() {
        }

        @Override
        public JPopupMenu createPopupMenu(int row, int column, Node[] selectedNodes, Component component) {
            if (component instanceof ETable) {
                ETable et = (ETable)component;
                int modelRowIndex = et.convertColumnIndexToModel(column);
                setShowQuickFilter(modelRowIndex != 0);
            }
            return super.createPopupMenu(row, column, selectedNodes, component);
        }
    }
    
    private static class NodeOutlineModel extends DefaultOutlineModel {

        public NodeOutlineModel(NodeTreeModel treeModel, RowModel rowModel, boolean largeModel, String nodesColumnLabel) {
            super( treeModel, rowModel, largeModel, nodesColumnLabel );
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                Node treeNode = getNodeAt(rowIndex);
                return null != treeNode && treeNode.canRename();
            }
            return super.isCellEditable(rowIndex, columnIndex);
        }

        @Override
        protected void setTreeValueAt(Object aValue, int rowIndex) {
            Node n = getNodeAt(rowIndex);
            if( null != n ) {
                n.setName(aValue == null ? "" : aValue.toString());
            }
        }

        protected final Node getNodeAt( int rowIndex ) {
            Node result = null;
            TreePath path = getLayout().getPathForRow(rowIndex);
            if (path != null) {
                result = Visualizer.findNode(path.getLastPathComponent());
            }
            return result;
        }
    }

    static final class PrototypeProperty extends PropertySupport.ReadWrite<Object> {
        PrototypeProperty (String name, String displayName) {
            this (name, displayName, null);
        }

        PrototypeProperty (String name, String displayName, String description) {
            super (name, Object.class, displayName, description);
        }

        @Override
        public Object getValue() throws IllegalAccessException,
                                        InvocationTargetException {
            throw new AssertionError();
        }

        @Override
        public void setValue(Object val) throws IllegalAccessException, 
                                                IllegalArgumentException,
                                                InvocationTargetException {
            throw new AssertionError();
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof Property &&
                    getName().equals(((Property)o).getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }
    }

    /* Horizontal scrolling support.
     */
    private final class ScrollListener extends ComponentAdapter implements ChangeListener,
                                                                           TableColumnModelListener,
                                                                           AdjustmentListener {
        ScrollListener() {
        }

        //ScrollBar or Viewport change
        public void stateChanged(ChangeEvent evt) {
            if (evt.getSource() == hScrollBar.getModel()) {
                int value = hScrollBar.getModel().getValue();
                outline.setTreePositionX(value);
            } else { // Viewport

            }
        }

        private void revalidateScrollBar() {
            if (!isDisplayable()) {
                return;
            }
            if (!isTreeHScrollBar) {
                return ;
            }

            if (
                (outline.getColumnModel().getColumnCount() > 0)
            ) {
                int column = outline.convertColumnIndexToView(0);
                int extentWidth = outline.getColumnModel().getColumn(column).getWidth();
                int maxWidth = outline.getTreePreferredWidth();
                int positionX = outline.getTreePositionX();

                int value = Math.max(0, Math.min(positionX, maxWidth - extentWidth));

                hScrollBar.setValues(value, extentWidth, 0, maxWidth);
                hScrollBar.setBlockIncrement(extentWidth);
            }
        }

        @Override
        public void columnAdded(TableColumnModelEvent e) {
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            revalidate();
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            revalidateScrollBar();
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int value = hScrollBar.getModel().getValue();
            outline.setTreePositionX(value);
        }
    }

    private static class OutlineScrollLayout extends ScrollPaneLayout.UIResource {

        public OutlineScrollLayout() {
        }

        private JScrollBar thsb;

        @Override
        public void addLayoutComponent(String s, Component c) {
            if (s.equals(TREE_HORIZONTAL_SCROLLBAR)) {
                thsb = (JScrollBar)addSingletonComponent(thsb, c);
            } else {
                super.addLayoutComponent(s, c);
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = super.preferredLayoutSize(parent);
            OutlineView ov = (OutlineView) parent;
            int thsbPolicy = ov.treeHorizontalScrollBarPolicy;
            if ((thsb != null) && (thsbPolicy != HORIZONTAL_SCROLLBAR_NEVER)) {
                if (thsbPolicy == HORIZONTAL_SCROLLBAR_ALWAYS) {
                    dim.height += thsb.getPreferredSize().height;
                }
                else {
                    Dimension extentSize = null;
                    Dimension viewSize = null;
                    Component view = null;

                    if (viewport !=  null) {
                        extentSize = viewport.getPreferredSize();
                        viewSize = viewport.getViewSize();
                        view = viewport.getView();
                    }

                    if ((viewSize != null) && (extentSize != null)) {
                        boolean canScroll = true;
                        if (view instanceof Scrollable) {
                            canScroll = !((Scrollable)view).getScrollableTracksViewportWidth();
                        }
                        if (canScroll && (viewSize.width > extentSize.width)) {
                            dim.height += thsb.getPreferredSize().height;
                        }
                    }
                }
            }
            return dim;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return super.minimumLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            OutlineView ov = (OutlineView) parent;
            if (ov.isTreeHScrollBar && ov.outline.getColumnModel().getColumnCount() > 0) {
                int column = ov.outline.convertColumnIndexToView(0);
                int extentWidth = ov.outline.getColumnModel().getColumn(column).getWidth();
                int maxWidth = ov.outline.getTreePreferredWidth();
                boolean hsbvisible = thsb.isVisible();
                boolean hideHsb = (maxWidth <= extentWidth) && ov.treeHorizontalScrollBarPolicy != JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;

                JScrollBar hsbOrig = hsb;
                boolean hsbNeeded;
                if (!hideHsb) {
                    // Set fake horizontal scroll bar so that the view size is set correctly
                    Component view = (viewport != null) ? viewport.getView() : null;
                    boolean viewTracksViewportWidth = false;
                    if (view instanceof Scrollable) {
                        Scrollable sv = (Scrollable)view;
                        viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
                    }
                    Dimension viewPrefSize =
                            (view != null) ? view.getPreferredSize()
                                           : new Dimension(0,0);
                    // Compute availR width: ( see super.layoutContainer(parent); )
                    Rectangle availR = ov.getBounds();
                    Insets insets = parent.getInsets();
                    availR.width -= insets.left + insets.right;
                    if ((rowHead != null) && (rowHead.isVisible())) {
                        int rowHeadWidth = Math.min(availR.width,
                                                    rowHead.getPreferredSize().width);
                        availR.width -= rowHeadWidth;
                    }
                    Border viewportBorder = ov.getViewportBorder();
                    if (viewportBorder != null) {
                        Insets vpbInsets = viewportBorder.getBorderInsets(parent);
                        availR.width -= vpbInsets.left + vpbInsets.right;
                    }
                    if (availR.width < 0) {
                        hsbNeeded = false;
                    }
                    if (ov.getHorizontalScrollBarPolicy() == HORIZONTAL_SCROLLBAR_ALWAYS) {
                        hsbNeeded = true;
                    }
                    else if (ov.getHorizontalScrollBarPolicy() == HORIZONTAL_SCROLLBAR_NEVER) {
                        hsbNeeded = false;
                    }
                    else {  // hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED
                        Dimension extentSize =
                            (viewport != null) ? viewport.toViewCoordinates(availR.getSize())
                                               : new Dimension(0,0);

                        hsbNeeded = !viewTracksViewportWidth && (viewPrefSize.width > extentSize.width);
                    }

                    if (hsbNeeded) {
                        hsb = createFakeHSB(hsbOrig);
                    } else {
                        ov.sayHorizontalScrollBarNeeded(true);
                    }
                } else {
                    hsbNeeded = false;
                }
                super.layoutContainer(parent);
                if (!hideHsb) {
                    if (hsbNeeded) {
                        JScrollBar fakeHsb = hsb;
                        fakeHsb.setVisible(false);
                        hsb = hsbOrig;
                        Rectangle r = fakeHsb.getBounds();
                        r.height /= 2;
                        r.y += r.height;
                        hsb.setBounds(r);
                        hsb.setVisible(true);
                    } else {
                        ov.sayHorizontalScrollBarNeeded(false);
                        hsbPolicy = ov.getHorizontalScrollBarPolicy();
                        hsb.setVisible(false);
                    }
                }
                
                if (hideHsb) {
                    thsb.setVisible(false);
                    ov.hScrollBar.setValues(0, 0, 0, 0);
                } else {
                    Rectangle vr = viewport.getBounds();
                    Rectangle r;
                    r = new Rectangle(getColumnXPos(ov.outline, column),
                                      vr.y + vr.height,
                                      extentWidth,
                                      thsb.getPreferredSize().height);
                    thsb.setBounds(r);
                    if (!hsbvisible) {
                        thsb.setVisible(true);
                        ov.listener.revalidateScrollBar();
                    }
                }
            } else {
                super.layoutContainer(parent);
            }
        }

        private JScrollBar createFakeHSB(final JScrollBar hsb) {
            return new JScrollBar(JScrollBar.HORIZONTAL) {

                @Override
                public Dimension getPreferredSize() {
                    Dimension dim = hsb.getPreferredSize();
                    return new Dimension(dim.width, 2*dim.height);
                }

            };
        }

        private int getColumnXPos(OutlineViewOutline outline, int column) {
            if (column < 0) {
                if (!outline.getComponentOrientation().isLeftToRight()) {
                    return outline.getWidth();
                } else {
                    return 0;
                }
            } else if (column >= outline.getColumnCount()) {
                if (outline.getComponentOrientation().isLeftToRight()) {
                    return outline.getWidth();
                } else {
                    return 0;
                }
            } else {
                TableColumnModel cm = outline.getColumnModel();
                int x = 0;
                if (outline.getComponentOrientation().isLeftToRight()) {
                    for (int i = 0; i < column; i++) {
                        x += cm.getColumn(i).getWidth();
                    }
                } else {
                    for(int i = cm.getColumnCount()-1; i > column; i--) {
                        x += cm.getColumn(i).getWidth();
                    }
                }
                return x;
            }
        }

    }

    /** Selection persistence, which allows to clear selected nodes. */
    private static class Selection {
        int selectionMode;
        int anchor;
        int lead;
        List<int[]> intervals = new ArrayList<int[]>();

        public Selection(ListSelectionModel sm) {
            selectionMode = sm.getSelectionMode();
            anchor = sm.getAnchorSelectionIndex();
            lead = sm.getLeadSelectionIndex();
            int min = sm.getMinSelectionIndex();
            int max = sm.getMaxSelectionIndex();
            int i1 = -1;
            for (int i = min; i <= max; i++) {
                if (sm.isSelectedIndex(i)) {
                    if (i1 == -1) {
                        i1 = i;
                    }
                } else {
                    if (i1 != -1) {
                        intervals.add(new int[] { i1, i});
                        i1 = -1;
                    }
                }
            }
            if (i1 != -1) {
                intervals.add(new int[] { i1, max});
            }
        }

        public void setTo(ListSelectionModel sm) {
            sm.clearSelection();
            sm.setSelectionMode(selectionMode);
            for (int[] itv : intervals) {
                sm.addSelectionInterval(itv[0], itv[1]);
            }
            sm.setAnchorSelectionIndex(anchor);
            sm.setLeadSelectionIndex(lead);
        }
    }

}

