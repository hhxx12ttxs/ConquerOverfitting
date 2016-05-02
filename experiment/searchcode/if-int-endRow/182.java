/* BasicTreeUI.java --
 Copyright (C) 2002, 2004, 2005, 2006, Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */


package javax.swing.plaf.basic;

import gnu.classpath.NotImplementedException;
import gnu.javax.swing.tree.GnuPath;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.VariableHeightLayoutCache;

/**
 * A delegate providing the user interface for <code>JTree</code> according to
 * the Basic look and feel.
 * 
 * @see javax.swing.JTree
 * @author Lillian Angel (langel@redhat.com)
 * @author Sascha Brawer (brawer@dandelis.ch)
 * @author Audrius Meskauskas (audriusa@bioinformatics.org)
 */
public class BasicTreeUI
    extends TreeUI
{
  /**
   * The tree cell editing may be started by the single mouse click on the
   * selected cell. To separate it from the double mouse click, the editing
   * session starts after this time (in ms) after that single click, and only no
   * other clicks were performed during that time.
   */
  static int WAIT_TILL_EDITING = 900;

  /** Collapse Icon for the tree. */
  protected transient Icon collapsedIcon;

  /** Expanded Icon for the tree. */
  protected transient Icon expandedIcon;

  /** Distance between left margin and where vertical dashes will be drawn. */
  protected int leftChildIndent;

  /**
   * Distance between leftChildIndent and where cell contents will be drawn.
   */
  protected int rightChildIndent;

  /**
   * Total fistance that will be indented. The sum of leftChildIndent and
   * rightChildIndent .
   */
  protected int totalChildIndent;

  /** Index of the row that was last selected. */
  protected int lastSelectedRow;

  /** Component that we're going to be drawing onto. */
  protected JTree tree;

  /** Renderer that is being used to do the actual cell drawing. */
  protected transient TreeCellRenderer currentCellRenderer;

  /**
   * Set to true if the renderer that is currently in the tree was created by
   * this instance.
   */
  protected boolean createdRenderer;

  /** Editor for the tree. */
  protected transient TreeCellEditor cellEditor;

  /**
   * Set to true if editor that is currently in the tree was created by this
   * instance.
   */
  protected boolean createdCellEditor;

  /**
   * Set to false when editing and shouldSelectCall() returns true meaning the
   * node should be selected before editing, used in completeEditing.
   * GNU Classpath editing is implemented differently, so this value is not
   * actually read anywhere. However it is always set correctly to maintain 
   * interoperability with the derived classes that read this field.
   */
  protected boolean stopEditingInCompleteEditing;

  /** Used to paint the TreeCellRenderer. */
  protected CellRendererPane rendererPane;

  /** Size needed to completely display all the nodes. */
  protected Dimension preferredSize;

  /** Minimum size needed to completely display all the nodes. */
  protected Dimension preferredMinSize;

  /** Is the preferredSize valid? */
  protected boolean validCachedPreferredSize;

  /** Object responsible for handling sizing and expanded issues. */
  protected AbstractLayoutCache treeState;

  /** Used for minimizing the drawing of vertical lines. */
  protected Hashtable drawingCache;

  /**
   * True if doing optimizations for a largeModel. Subclasses that don't support
   * this may wish to override createLayoutCache to not return a
   * FixedHeightLayoutCache instance.
   */
  protected boolean largeModel;

  /** Responsible for telling the TreeState the size needed for a node. */
  protected AbstractLayoutCache.NodeDimensions nodeDimensions;

  /** Used to determine what to display. */
  protected TreeModel treeModel;

  /** Model maintaining the selection. */
  protected TreeSelectionModel treeSelectionModel;

  /**
   * How much the depth should be offset to properly calculate x locations. This
   * is based on whether or not the root is visible, and if the root handles are
   * visible.
   */
  protected int depthOffset;

  /**
   * When editing, this will be the Component that is doing the actual editing.
   */
  protected Component editingComponent;

  /** Path that is being edited. */
  protected TreePath editingPath;

  /**
   * Row that is being edited. Should only be referenced if editingComponent is
   * null.
   */
  protected int editingRow;

  /** Set to true if the editor has a different size than the renderer. */
  protected boolean editorHasDifferentSize;

  /** Boolean to keep track of editing. */
  boolean isEditing;

  /** The current path of the visible nodes in the tree. */
  TreePath currentVisiblePath;

  /** The gap between the icon and text. */
  int gap = 4;

  /** The max height of the nodes in the tree. */
  int maxHeight;
  
  /** The hash color. */
  Color hashColor;

  /** Listeners */
  PropertyChangeListener propertyChangeListener;

  FocusListener focusListener;

  TreeSelectionListener treeSelectionListener;

  MouseListener mouseListener;

  KeyListener keyListener;

  PropertyChangeListener selectionModelPropertyChangeListener;

  ComponentListener componentListener;

  CellEditorListener cellEditorListener;

  TreeExpansionListener treeExpansionListener;

  TreeModelListener treeModelListener;

  /**
   * This timer fires the editing action after about 1200 ms if not reset during
   * that time. It handles the editing start with the single mouse click (and
   * not the double mouse click) on the selected tree node.
   */
  Timer startEditTimer;
  
  /**
   * The zero size icon, used for expand controls, if they are not visible.
   */
  static Icon nullIcon;

  /**
   * The special value of the mouse event is sent indicating that this is not
   * just the mouse click, but the mouse click on the selected node. Sending
   * such event forces to start the cell editing session.
   */
  static final MouseEvent EDIT = new MouseEvent(new Label(), 7, 7, 7, 7, 7, 7,
                                                false);

  /**
   * Creates a new BasicTreeUI object.
   */
  public BasicTreeUI()
  {
    validCachedPreferredSize = false;
    drawingCache = new Hashtable();
    nodeDimensions = createNodeDimensions();
    configureLayoutCache();

    editingRow = - 1;
    lastSelectedRow = - 1;
  }

  /**
   * Returns an instance of the UI delegate for the specified component.
   * 
   * @param c the <code>JComponent</code> for which we need a UI delegate for.
   * @return the <code>ComponentUI</code> for c.
   */
  public static ComponentUI createUI(JComponent c)
  {
    return new BasicTreeUI();
  }

  /**
   * Returns the Hash color.
   * 
   * @return the <code>Color</code> of the Hash.
   */
  protected Color getHashColor()
  {
    return hashColor;
  }

  /**
   * Sets the Hash color.
   * 
   * @param color the <code>Color</code> to set the Hash to.
   */
  protected void setHashColor(Color color)
  {
    hashColor = color;
  }

  /**
   * Sets the left child's indent value.
   * 
   * @param newAmount is the new indent value for the left child.
   */
  public void setLeftChildIndent(int newAmount)
  {
    leftChildIndent = newAmount;
  }

  /**
   * Returns the indent value for the left child.
   * 
   * @return the indent value for the left child.
   */
  public int getLeftChildIndent()
  {
    return leftChildIndent;
  }

  /**
   * Sets the right child's indent value.
   * 
   * @param newAmount is the new indent value for the right child.
   */
  public void setRightChildIndent(int newAmount)
  {
    rightChildIndent = newAmount;
  }

  /**
   * Returns the indent value for the right child.
   * 
   * @return the indent value for the right child.
   */
  public int getRightChildIndent()
  {
    return rightChildIndent;
  }

  /**
   * Sets the expanded icon.
   * 
   * @param newG is the new expanded icon.
   */
  public void setExpandedIcon(Icon newG)
  {
    expandedIcon = newG;
  }

  /**
   * Returns the current expanded icon.
   * 
   * @return the current expanded icon.
   */
  public Icon getExpandedIcon()
  {
    return expandedIcon;
  }

  /**
   * Sets the collapsed icon.
   * 
   * @param newG is the new collapsed icon.
   */
  public void setCollapsedIcon(Icon newG)
  {
    collapsedIcon = newG;
  }

  /**
   * Returns the current collapsed icon.
   * 
   * @return the current collapsed icon.
   */
  public Icon getCollapsedIcon()
  {
    return collapsedIcon;
  }

  /**
   * Updates the componentListener, if necessary.
   * 
   * @param largeModel sets this.largeModel to it.
   */
  protected void setLargeModel(boolean largeModel)
  {
    if (largeModel != this.largeModel)
      {
        tree.removeComponentListener(componentListener);
        this.largeModel = largeModel;
        tree.addComponentListener(componentListener);
      }
  }

  /**
   * Returns true if largeModel is set
   * 
   * @return true if largeModel is set, otherwise false.
   */
  protected boolean isLargeModel()
  {
    return largeModel;
  }

  /**
   * Sets the row height.
   * 
   * @param rowHeight is the height to set this.rowHeight to.
   */
  protected void setRowHeight(int rowHeight)
  {
    if (rowHeight == 0)
      rowHeight = getMaxHeight(tree);
    treeState.setRowHeight(rowHeight);
  }

  /**
   * Returns the current row height.
   * 
   * @return current row height.
   */
  protected int getRowHeight()
  {
    return tree.getRowHeight();
  }

  /**
   * Sets the TreeCellRenderer to <code>tcr</code>. This invokes
   * <code>updateRenderer</code>.
   * 
   * @param tcr is the new TreeCellRenderer.
   */
  protected void setCellRenderer(TreeCellRenderer tcr)
  {
    // Finish editing before changing the renderer.
    completeEditing();

    // The renderer is set in updateRenderer.
    updateRenderer();

    // Refresh the layout if necessary.
    if (treeState != null)
      {
	treeState.invalidateSizes();
	updateSize();
      }
  }

  /**
   * Return currentCellRenderer, which will either be the trees renderer, or
   * defaultCellRenderer, which ever was not null.
   * 
   * @return the current Cell Renderer
   */
  protected TreeCellRenderer getCellRenderer()
  {
    if (currentCellRenderer != null)
      return currentCellRenderer;

    return createDefaultCellRenderer();
  }

  /**
   * Sets the tree's model.
   * 
   * @param model to set the treeModel to.
   */
  protected void setModel(TreeModel model)
  {
    completeEditing();

    if (treeModel != null && treeModelListener != null)
      treeModel.removeTreeModelListener(treeModelListener);

    treeModel = tree.getModel();

    if (treeModel != null && treeModelListener != null)
      treeModel.addTreeModelListener(treeModelListener);

    if (treeState != null)
      {
        treeState.setModel(treeModel);
        updateLayoutCacheExpandedNodes();
        updateSize();
      }
  }

  /**
   * Returns the tree's model
   * 
   * @return treeModel
   */
  protected TreeModel getModel()
  {
    return treeModel;
  }

  /**
   * Sets the root to being visible.
   * 
   * @param newValue sets the visibility of the root
   */
  protected void setRootVisible(boolean newValue)
  {
    tree.setRootVisible(newValue);
  }

  /**
   * Returns true if the root is visible.
   * 
   * @return true if the root is visible.
   */
  protected boolean isRootVisible()
  {
    return tree.isRootVisible();
  }

  /**
   * Determines whether the node handles are to be displayed.
   * 
   * @param newValue sets whether or not node handles should be displayed.
   */
  protected void setShowsRootHandles(boolean newValue)
  {
    completeEditing();
    updateDepthOffset();
    if (treeState != null)
      {
        treeState.invalidateSizes();
        updateSize();
      }
  }

  /**
   * Returns true if the node handles are to be displayed.
   * 
   * @return true if the node handles are to be displayed.
   */
  protected boolean getShowsRootHandles()
  {
    return tree.getShowsRootHandles();
  }

  /**
   * Sets the cell editor.
   * 
   * @param editor to set the cellEditor to.
   */
  protected void setCellEditor(TreeCellEditor editor)
  {
    cellEditor = editor;
    createdCellEditor = true;
  }

  /**
   * Returns the <code>TreeCellEditor</code> for this tree.
   * 
   * @return the cellEditor for this tree.
   */
  protected TreeCellEditor getCellEditor()
  {
    return cellEditor;
  }

  /**
   * Configures the receiver to allow, or not allow, editing.
   * 
   * @param newValue sets the receiver to allow editing if true.
   */
  protected void setEditable(boolean newValue)
  {
    tree.setEditable(newValue);
  }

  /**
   * Returns true if the receiver allows editing.
   * 
   * @return true if the receiver allows editing.
   */
  protected boolean isEditable()
  {
    return tree.isEditable();
  }

  /**
   * Resets the selection model. The appropriate listeners are installed on the
   * model.
   * 
   * @param newLSM resets the selection model.
   */
  protected void setSelectionModel(TreeSelectionModel newLSM)
  {
    if (newLSM != null)
      {
        treeSelectionModel = newLSM;
        tree.setSelectionModel(treeSelectionModel);
      }
  }

  /**
   * Returns the current selection model.
   * 
   * @return the current selection model.
   */
  protected TreeSelectionModel getSelectionModel()
  {
    return treeSelectionModel;
  }

  /**
   * Returns the Rectangle enclosing the label portion that the last item in
   * path will be drawn to. Will return null if any component in path is
   * currently valid.
   * 
   * @param tree is the current tree the path will be drawn to.
   * @param path is the current path the tree to draw to.
   * @return the Rectangle enclosing the label portion that the last item in the
   *         path will be drawn to.
   */
  public Rectangle getPathBounds(JTree tree, TreePath path)
  {
    return treeState.getBounds(path, new Rectangle());
  }

  /**
   * Returns the max height of all the nodes in the tree.
   * 
   * @param tree - the current tree
   * @return the max height.
   */
  int getMaxHeight(JTree tree)
  {
    if (maxHeight != 0)
      return maxHeight;

    Icon e = UIManager.getIcon("Tree.openIcon");
    Icon c = UIManager.getIcon("Tree.closedIcon");
    Icon l = UIManager.getIcon("Tree.leafIcon");
    int rc = getRowCount(tree);
    int iconHeight = 0;

    for (int row = 0; row < rc; row++)
      {
        if (isLeaf(row))
          iconHeight = l.getIconHeight();
        else if (tree.isExpanded(row))
          iconHeight = e.getIconHeight();
        else
          iconHeight = c.getIconHeight();

        maxHeight = Math.max(maxHeight, iconHeight + gap);
      }
     
    treeState.setRowHeight(maxHeight);
    return maxHeight;
  }
  
  /**
   * Get the tree node icon.
   */
  Icon getNodeIcon(TreePath path)
  {
    Object node = path.getLastPathComponent();
    if (treeModel.isLeaf(node))
      return UIManager.getIcon("Tree.leafIcon");
    else if (treeState.getExpandedState(path))
      return UIManager.getIcon("Tree.openIcon");
    else
      return UIManager.getIcon("Tree.closedIcon");
  }

  /**
   * Returns the path for passed in row. If row is not visible null is returned.
   * 
   * @param tree is the current tree to return path for.
   * @param row is the row number of the row to return.
   * @return the path for passed in row. If row is not visible null is returned.
   */
  public TreePath getPathForRow(JTree tree, int row)
  {
    return treeState.getPathForRow(row);
  }

  /**
   * Returns the row that the last item identified in path is visible at. Will
   * return -1 if any of the elments in the path are not currently visible.
   * 
   * @param tree is the current tree to return the row for.
   * @param path is the path used to find the row.
   * @return the row that the last item identified in path is visible at. Will
   *         return -1 if any of the elments in the path are not currently
   *         visible.
   */
  public int getRowForPath(JTree tree, TreePath path)
  {
    return treeState.getRowForPath(path);
  }

  /**
   * Returns the number of rows that are being displayed.
   * 
   * @param tree is the current tree to return the number of rows for.
   * @return the number of rows being displayed.
   */
  public int getRowCount(JTree tree)
  {
    return treeState.getRowCount();
  }

  /**
   * Returns the path to the node that is closest to x,y. If there is nothing
   * currently visible this will return null, otherwise it'll always return a
   * valid path. If you need to test if the returned object is exactly at x,y
   * you should get the bounds for the returned path and test x,y against that.
   * 
   * @param tree the tree to search for the closest path
   * @param x is the x coordinate of the location to search
   * @param y is the y coordinate of the location to search
   * @return the tree path closes to x,y.
   */
  public TreePath getClosestPathForLocation(JTree tree, int x, int y)
  {
    return treeState.getPathClosestTo(x, y);
  }

  /**
   * Returns true if the tree is being edited. The item that is being edited can
   * be returned by getEditingPath().
   * 
   * @param tree is the tree to check for editing.
   * @return true if the tree is being edited.
   */
  public boolean isEditing(JTree tree)
  {
    return isEditing;
  }

  /**
   * Stops the current editing session. This has no effect if the tree is not
   * being edited. Returns true if the editor allows the editing session to
   * stop.
   * 
   * @param tree is the tree to stop the editing on
   * @return true if the editor allows the editing session to stop.
   */
  public boolean stopEditing(JTree tree)
  {
    if (isEditing(tree))
      {
        completeEditing(false, false, true);
        finish();
      }
    return ! isEditing(tree);
  }

  /**
   * Cancels the current editing session.
   * 
   * @param tree is the tree to cancel the editing session on.
   */
  public void cancelEditing(JTree tree)
  {
    // There is no need to send the cancel message to the editor,
    // as the cancellation event itself arrives from it. This would
    // only be necessary when cancelling the editing programatically.
    completeEditing(false, false, false);
    finish();
  }

  /**
   * Selects the last item in path and tries to edit it. Editing will fail if
   * the CellEditor won't allow it for the selected item.
   * 
   * @param tree is the tree to edit on.
   * @param path is the path in tree to edit on.
   */
  public void startEditingAtPath(JTree tree, TreePath path)
  {
    startEditing(path, null);
  }

  /**
   * Returns the path to the element that is being editted.
   * 
   * @param tree is the tree to get the editing path from.
   * @return the path that is being edited.
   */
  public TreePath getEditingPath(JTree tree)
  {
    return editingPath;
  }

  /**
   * Invoked after the tree instance variable has been set, but before any
   * default/listeners have been installed.
   */
  protected void prepareForUIInstall()
  {
    lastSelectedRow = -1;
    preferredSize = new Dimension();
    largeModel = tree.isLargeModel();
    preferredSize = new Dimension();
    setModel(tree.getModel());
  }

  /**
   * Invoked from installUI after all the defaults/listeners have been
   * installed.
   */
  protected void completeUIInstall()
  {
    setShowsRootHandles(tree.getShowsRootHandles());
    updateRenderer();
    updateDepthOffset();
    setSelectionModel(tree.getSelectionModel());
    configureLayoutCache();
    treeState.setRootVisible(tree.isRootVisible()); 
    treeSelectionModel.setRowMapper(treeState);
    updateSize();
  }

  /**
   * Invoked from uninstallUI after all the defaults/listeners have been
   * uninstalled.
   */
  protected void completeUIUninstall()
  {
    tree = null;
  }

  /**
   * Installs the subcomponents of the tree, which is the renderer pane.
   */
  protected void installComponents()
  {
    currentCellRenderer = createDefaultCellRenderer();
    rendererPane = createCellRendererPane();
    createdRenderer = true;
    setCellRenderer(currentCellRenderer);
  }

  /**
   * Creates an instance of NodeDimensions that is able to determine the size of
   * a given node in the tree. The node dimensions must be created before
   * configuring the layout cache.
   * 
   * @return the NodeDimensions of a given node in the tree
   */
  protected AbstractLayoutCache.NodeDimensions createNodeDimensions()
  {
    return new NodeDimensionsHandler();
  }

  /**
   * Creates a listener that is reponsible for the updates the UI based on how
   * the tree changes.
   * 
   * @return the PropertyChangeListener that is reposnsible for the updates
   */
  protected PropertyChangeListener createPropertyChangeListener()
  {
    return new PropertyChangeHandler();
  }

  /**
   * Creates the listener responsible for updating the selection based on mouse
   * events.
   * 
   * @return the MouseListener responsible for updating.
   */
  protected MouseListener createMouseListener()
  {
    return new MouseHandler();
  }

  /**
   * Creates the listener that is responsible for updating the display when
   * focus is lost/grained.
   * 
   * @return the FocusListener responsible for updating.
   */
  protected FocusListener createFocusListener()
  {
    return new FocusHandler();
  }

  /**
   * Creates the listener reponsible for getting key events from the tree.
   * 
   * @return the KeyListener responsible for getting key events.
   */
  protected KeyListener createKeyListener()
  {
    return new KeyHandler();
  }

  /**
   * Creates the listener responsible for getting property change events from
   * the selection model.
   * 
   * @returns the PropertyChangeListener reponsible for getting property change
   *          events from the selection model.
   */
  protected PropertyChangeListener createSelectionModelPropertyChangeListener()
  {
    return new SelectionModelPropertyChangeHandler();
  }

  /**
   * Creates the listener that updates the display based on selection change
   * methods.
   * 
   * @return the TreeSelectionListener responsible for updating.
   */
  protected TreeSelectionListener createTreeSelectionListener()
  {
    return new TreeSelectionHandler();
  }

  /**
   * Creates a listener to handle events from the current editor
   * 
   * @return the CellEditorListener that handles events from the current editor
   */
  protected CellEditorListener createCellEditorListener()
  {
    return new CellEditorHandler();
  }

  /**
   * Creates and returns a new ComponentHandler. This is used for the large
   * model to mark the validCachedPreferredSize as invalid when the component
   * moves.
   * 
   * @return a new ComponentHandler.
   */
  protected ComponentListener createComponentListener()
  {
    return new ComponentHandler();
  }

  /**
   * Creates and returns the object responsible for updating the treestate when
   * a nodes expanded state changes.
   * 
   * @return the TreeExpansionListener responsible for updating the treestate
   */
  protected TreeExpansionListener createTreeExpansionListener()
  {
    return new TreeExpansionHandler();
  }

  /**
   * Creates the object responsible for managing what is expanded, as well as
   * the size of nodes.
   * 
   * @return the object responsible for managing what is expanded.
   */
  protected AbstractLayoutCache createLayoutCache()
  {
    return new VariableHeightLayoutCache();
  }

  /**
   * Returns the renderer pane that renderer components are placed in.
   * 
   * @return the rendererpane that render components are placed in.
   */
  protected CellRendererPane createCellRendererPane()
  {
    return new CellRendererPane();
  }

  /**
   * Creates a default cell editor.
   * 
   * @return the default cell editor.
   */
  protected TreeCellEditor createDefaultCellEditor()
  {
    DefaultTreeCellEditor ed;
    if (currentCellRenderer != null
        && currentCellRenderer instanceof DefaultTreeCellRenderer)
      ed = new DefaultTreeCellEditor(tree,
                                (DefaultTreeCellRenderer) currentCellRenderer);
    else
      ed = new DefaultTreeCellEditor(tree, null);
    return ed;
  }

  /**
   * Returns the default cell renderer that is used to do the stamping of each
   * node.
   * 
   * @return the default cell renderer that is used to do the stamping of each
   *         node.
   */
  protected TreeCellRenderer createDefaultCellRenderer()
  {
    return new DefaultTreeCellRenderer();
  }

  /**
   * Returns a listener that can update the tree when the model changes.
   * 
   * @return a listener that can update the tree when the model changes.
   */
  protected TreeModelListener createTreeModelListener()
  {
    return new TreeModelHandler();
  }

  /**
   * Uninstall all registered listeners
   */
  protected void uninstallListeners()
  {
    tree.removePropertyChangeListener(propertyChangeListener);
    tree.removeFocusListener(focusListener);
    tree.removeTreeSelectionListener(treeSelectionListener);
    tree.removeMouseListener(mouseListener);
    tree.removeKeyListener(keyListener);
    tree.removePropertyChangeListener(selectionModelPropertyChangeListener);
    tree.removeComponentListener(componentListener);
    tree.removeTreeExpansionListener(treeExpansionListener);

    TreeCellEditor tce = tree.getCellEditor();
    if (tce != null)
      tce.removeCellEditorListener(cellEditorListener);
    if (treeModel != null)
      treeModel.removeTreeModelListener(treeModelListener);
  }

  /**
   * Uninstall all keyboard actions.
   */
  protected void uninstallKeyboardActions()
  {
    tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).setParent(
                                                                              null);
    tree.getActionMap().setParent(null);
  }

  /**
   * Uninstall the rendererPane.
   */
  protected void uninstallComponents()
  {
    currentCellRenderer = null;
    rendererPane = null;
    createdRenderer = false;
    setCellRenderer(currentCellRenderer);
  }

  /**
   * The vertical element of legs between nodes starts at the bottom of the
   * parent node by default. This method makes the leg start below that.
   * 
   * @return the vertical leg buffer
   */
  protected int getVerticalLegBuffer()
  {
    return getRowHeight() / 2;
  }

  /**
   * The horizontal element of legs between nodes starts at the right of the
   * left-hand side of the child node by default. This method makes the leg end
   * before that.
   * 
   * @return the horizontal leg buffer
   */
  protected int getHorizontalLegBuffer()
  {
    return rightChildIndent / 2;
  }

  /**
   * Make all the nodes that are expanded in JTree expanded in LayoutCache. This
   * invokes updateExpandedDescendants with the root path.
   */
  protected void updateLayoutCacheExpandedNodes()
  {
    if (treeModel != null && treeModel.getRoot() != null)
      updateExpandedDescendants(new TreePath(treeModel.getRoot()));
  }

  /**
   * Updates the expanded state of all the descendants of the <code>path</code>
   * by getting the expanded descendants from the tree and forwarding to the
   * tree state.
   * 
   * @param path the path used to update the expanded states
   */
  protected void updateExpandedDescendants(TreePath path)
  {
    Enumeration expanded = tree.getExpandedDescendants(path);
    while (expanded.hasMoreElements())
      treeState.setExpandedState((TreePath) expanded.nextElement(), true);
  }

  /**
   * Returns a path to the last child of <code>parent</code>
   * 
   * @param parent is the topmost path to specified
   * @return a path to the last child of parent
   */
  protected TreePath getLastChildPath(TreePath parent)
  {
    return (TreePath) parent.getLastPathComponent();
  }

  /**
   * Updates how much each depth should be offset by.
   */
  protected void updateDepthOffset()
  {
    depthOffset += getVerticalLegBuffer();
  }

  /**
   * Updates the cellEditor based on editability of the JTree that we're
   * contained in. If the tree is editable but doesn't have a cellEditor, a
   * basic one will be used.
   */
  protected void updateCellEditor()
  {
    if (tree.isEditable() && cellEditor == null)
      setCellEditor(createDefaultCellEditor());
    createdCellEditor = true;
  }

  /**
   * Messaged from the tree we're in when the renderer has changed.
   */
  protected void updateRenderer()
  {
    if (tree != null)
      {
	TreeCellRenderer rend = tree.getCellRenderer();
	if (rend != null)
	  {
	    createdRenderer = false;
	    currentCellRenderer = rend;
	    if (createdCellEditor)
	      tree.setCellEditor(null);
	  }
	else
	  {
	    tree.setCellRenderer(createDefaultCellRenderer());
	    createdRenderer = true;
	  }
      }
    else
      {
	currentCellRenderer = null;
	createdRenderer = false;
      }

    updateCellEditor();
  }

  /**
   * Resets the treeState instance based on the tree we're providing the look
   * and feel for. The node dimensions handler is required and must be created
   * in advance.
   */
  protected void configureLayoutCache()
  {
    treeState = createLayoutCache();
    treeState.setNodeDimensions(nodeDimensions);
  }

  /**
   * Marks the cached size as being invalid, and messages the tree with
   * <code>treeDidChange</code>.
   */
  protected void updateSize()
  {
    preferredSize = null;
    updateCachedPreferredSize();
    tree.treeDidChange();
  }

  /**
   * Updates the <code>preferredSize</code> instance variable, which is
   * returned from <code>getPreferredSize()</code>.
   */
  protected void updateCachedPreferredSize()
  {
    validCachedPreferredSize = false;
  }

  /**
   * Messaged from the VisibleTreeNode after it has been expanded.
   * 
   * @param path is the path that has been expanded.
   */
  protected void pathWasExpanded(TreePath path)
  {
    validCachedPreferredSize = false;
    treeState.setExpandedState(path, true);
    tree.repaint();
  }

  /**
   * Messaged from the VisibleTreeNode after it has collapsed
   */
  protected void pathWasCollapsed(TreePath path)
  {
    validCachedPreferredSize = false;
    treeState.setExpandedState(path, false);
    tree.repaint();
  }

  /**
   * Install all defaults for the tree.
   */
  protected void installDefaults()
  {
    LookAndFeel.installColorsAndFont(tree, "Tree.background",
                                     "Tree.foreground", "Tree.font");
    
    hashColor = UIManager.getColor("Tree.hash");
    if (hashColor == null)
      hashColor = Color.black;
    
    tree.setOpaque(true);

    rightChildIndent = UIManager.getInt("Tree.rightChildIndent");
    leftChildIndent = UIManager.getInt("Tree.leftChildIndent");
    totalChildIndent = rightChildIndent + leftChildIndent;
    setRowHeight(UIManager.getInt("Tree.rowHeight"));
    tree.setRowHeight(getRowHeight());
    tree.setScrollsOnExpand(UIManager.getBoolean("Tree.scrollsOnExpand"));
    setExpandedIcon(UIManager.getIcon("Tree.expandedIcon"));
    setCollapsedIcon(UIManager.getIcon("Tree.collapsedIcon"));
  }

  /**
   * Install all keyboard actions for this
   */
  protected void installKeyboardActions()
  {
    InputMap focusInputMap =
      (InputMap) SharedUIDefaults.get("Tree.focusInputMap");
    SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_FOCUSED,
                                     focusInputMap);
    InputMap ancestorInputMap =
      (InputMap) SharedUIDefaults.get("Tree.ancestorInputMap");
    SwingUtilities.replaceUIInputMap(tree,
                                 JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                 ancestorInputMap);

    SwingUtilities.replaceUIActionMap(tree, getActionMap());
  }

  /**
   * Creates and returns the shared action map for JTrees.
   *
   * @return the shared action map for JTrees
   */
  private ActionMap getActionMap()
  {
    ActionMap am = (ActionMap) UIManager.get("Tree.actionMap");
    if (am == null)
      {
        am = createDefaultActions();
        UIManager.getLookAndFeelDefaults().put("Tree.actionMap", am);
      }
    return am;
  }

  /**
   * Creates the default actions when there are none specified by the L&F.
   *
   * @return the default actions
   */
  private ActionMap createDefaultActions()
  {
    ActionMapUIResource am = new ActionMapUIResource();
    Action action;

    // TreeHomeAction.
    action = new TreeHomeAction(-1, "selectFirst");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeHomeAction(-1, "selectFirstChangeLead");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeHomeAction(-1, "selectFirstExtendSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeHomeAction(1, "selectLast");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeHomeAction(1, "selectLastChangeLead");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeHomeAction(1, "selectLastExtendSelection");
    am.put(action.getValue(Action.NAME), action);

    // TreeIncrementAction.
    action = new TreeIncrementAction(-1, "selectPrevious");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeIncrementAction(-1, "selectPreviousExtendSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeIncrementAction(-1, "selectPreviousChangeLead");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeIncrementAction(1, "selectNext");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeIncrementAction(1, "selectNextExtendSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeIncrementAction(1, "selectNextChangeLead");
    am.put(action.getValue(Action.NAME), action);

    // TreeTraverseAction.
    action = new TreeTraverseAction(-1, "selectParent");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeTraverseAction(1, "selectChild");
    am.put(action.getValue(Action.NAME), action);
    
    // TreeToggleAction.
    action = new TreeToggleAction("toggleAndAnchor");
    am.put(action.getValue(Action.NAME), action);

    // TreePageAction.
    action = new TreePageAction(-1, "scrollUpChangeSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreePageAction(-1, "scrollUpExtendSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreePageAction(-1, "scrollUpChangeLead");
    am.put(action.getValue(Action.NAME), action);
    action = new TreePageAction(1, "scrollDownChangeSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreePageAction(1, "scrollDownExtendSelection");
    am.put(action.getValue(Action.NAME), action);
    action = new TreePageAction(1, "scrollDownChangeLead");
    am.put(action.getValue(Action.NAME), action);
    
    // Tree editing actions
    action = new TreeStartEditingAction("startEditing");
    am.put(action.getValue(Action.NAME), action);
    action = new TreeCancelEditingAction("cancel");
    am.put(action.getValue(Action.NAME), action);
    

    return am;
  }

  /**
   * Converts the modifiers.
   * 
   * @param mod - modifier to convert
   * @returns the new modifier
   */
  private int convertModifiers(int mod)
  {
    if ((mod & KeyEvent.SHIFT_DOWN_MASK) != 0)
      {
        mod |= KeyEvent.SHIFT_MASK;
        mod &= ~ KeyEvent.SHIFT_DOWN_MASK;
      }
    if ((mod & KeyEvent.CTRL_DOWN_MASK) != 0)
      {
        mod |= KeyEvent.CTRL_MASK;
        mod &= ~ KeyEvent.CTRL_DOWN_MASK;
      }
    if ((mod & KeyEvent.META_DOWN_MASK) != 0)
      {
        mod |= KeyEvent.META_MASK;
        mod &= ~ KeyEvent.META_DOWN_MASK;
      }
    if ((mod & KeyEvent.ALT_DOWN_MASK) != 0)
      {
        mod |= KeyEvent.ALT_MASK;
        mod &= ~ KeyEvent.ALT_DOWN_MASK;
      }
    if ((mod & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0)
      {
        mod |= KeyEvent.ALT_GRAPH_MASK;
        mod &= ~ KeyEvent.ALT_GRAPH_DOWN_MASK;
      }
    return mod;
  }

  /**
   * Install all listeners for this
   */
  protected void installListeners()
  {
    propertyChangeListener = createPropertyChangeListener();
    tree.addPropertyChangeListener(propertyChangeListener);

    focusListener = createFocusListener();
    tree.addFocusListener(focusListener);

    treeSelectionListener = createTreeSelectionListener();
    tree.addTreeSelectionListener(treeSelectionListener);

    mouseListener = createMouseListener();
    tree.addMouseListener(mouseListener);

    keyListener = createKeyListener();
    tree.addKeyListener(keyListener);

    selectionModelPropertyChangeListener =
      createSelectionModelPropertyChangeListener();
    if (treeSelectionModel != null
        && selectionModelPropertyChangeListener != null)
      {
        treeSelectionModel.addPropertyChangeListener(
            selectionModelPropertyChangeListener);
      }

    componentListener = createComponentListener();
    tree.addComponentListener(componentListener);

    treeExpansionListener = createTreeExpansionListener();
    tree.addTreeExpansionListener(treeExpansionListener);

    treeModelListener = createTreeModelListener();
    if (treeModel != null)
      treeModel.addTreeModelListener(treeModelListener);

    cellEditorListener = createCellEditorListener();
  }

  /**
   * Install the UI for the component
   * 
   * @param c the component to install UI for
   */
  public void installUI(JComponent c)
  {
    tree = (JTree) c;

    prepareForUIInstall();
    installDefaults();
    installComponents();
    installKeyboardActions();
    installListeners();
    completeUIInstall();
  }
  
  /**
   * Uninstall the defaults for the tree
   */
  protected void uninstallDefaults()
  {
    tree.setFont(null);
    tree.setForeground(null);
    tree.setBackground(null);
  }

  /**
   * Uninstall the UI for the component
   * 
   * @param c the component to uninstall UI for
   */
  public void uninstallUI(JComponent c)
  {
    completeEditing();

    prepareForUIUninstall();
    uninstallDefaults();
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallComponents();
    completeUIUninstall();
  }

  /**
   * Paints the specified component appropriate for the look and feel. This
   * method is invoked from the ComponentUI.update method when the specified
   * component is being painted. Subclasses should override this method and use
   * the specified Graphics object to render the content of the component.
   * 
   * @param g the Graphics context in which to paint
   * @param c the component being painted; this argument is often ignored, but
   *          might be used if the UI object is stateless and shared by multiple
   *          components
   */
  public void paint(Graphics g, JComponent c)
  {
    JTree tree = (JTree) c;
    
    int rows = treeState.getRowCount();
    
    if (rows == 0)
      // There is nothing to do if the tree is empty.
      return;

    Rectangle clip = g.getClipBounds();

    Insets insets = tree.getInsets();

    if (clip != null && treeModel != null)
      {
        int startIndex = tree.getClosestRowForLocation(clip.x, clip.y);
        int endIndex = tree.getClosestRowForLocation(clip.x + clip.width,
                                                     clip.y + clip.height);

        // Also paint dashes to the invisible nodes below.
        // These should be painted first, otherwise they may cover
        // the control icons.
        if (endIndex < rows)
          for (int i = endIndex + 1; i < rows; i++)
            {
              TreePath path = treeState.getPathForRow(i);
              if (isLastChild(path))
                paintVerticalPartOfLeg(g, clip, insets, path);
            }

        // The two loops are required to ensure that the lines are not
        // painted over the other tree components.

        int n = endIndex - startIndex + 1;
        Rectangle[] bounds = new Rectangle[n];
        boolean[] isLeaf = new boolean[n];
        boolean[] isExpanded = new boolean[n];
        TreePath[] path = new TreePath[n];
        int k;

        k = 0;
        for (int i = startIndex; i <= endIndex; i++, k++)
          {
            path[k] = treeState.getPathForRow(i);
            isLeaf[k] = treeModel.isLeaf(path[k].getLastPathComponent());
            isExpanded[k] = tree.isExpanded(path[k]);
            bounds[k] = getPathBounds(tree, path[k]);

            paintHorizontalPartOfLeg(g, clip, insets, bounds[k], path[k], i,
                                     isExpanded[k], false, isLeaf[k]);
            if (isLastChild(path[k]))
              paintVerticalPartOfLeg(g, clip, insets, path[k]);
          }

        k = 0;
        for (int i = startIndex; i <= endIndex; i++, k++)
          {
            paintRow(g, clip, insets, bounds[k], path[k], i, isExpanded[k],
                     false, isLeaf[k]);
          }
      }
  }

  /**
   * Check if the path is referring to the last child of some parent.
   */
  private boolean isLastChild(TreePath path)
  {
    if (path instanceof GnuPath)
      {
        // Except the seldom case when the layout cache is changed, this
        // optimized code will be executed.
        return ((GnuPath) path).isLastChild;
      }
    else
      {
        // Non optimized general case.
        TreePath parent = path.getParentPath();
        if (parent == null)
          return false;
        int childCount = treeState.getVisibleChildCount(parent);
        int p = treeModel.getIndexOfChild(parent, path.getLastPathComponent());
        return p == childCount - 1;
      }
  }

  /**
   * Ensures that the rows identified by beginRow through endRow are visible.
   * 
   * @param beginRow is the first row
   * @param endRow is the last row
   */
  protected void ensureRowsAreVisible(int beginRow, int endRow)
  {
    if (beginRow < endRow)
      {
        int temp = endRow;
        endRow = beginRow;
        beginRow = temp;
      }

    for (int i = beginRow; i < endRow; i++)
      {
        TreePath path = getPathForRow(tree, i);
        if (! tree.isVisible(path))
          tree.makeVisible(path);
      }
  }

  /**
   * Sets the preferred minimum size.
   * 
   * @param newSize is the new preferred minimum size.
   */
  public void setPreferredMinSize(Dimension newSize)
  {
    preferredMinSize = newSize;
  }

  /**
   * Gets the preferred minimum size.
   * 
   * @returns the preferred minimum size.
   */
  public Dimension getPreferredMinSize()
  {
    if (preferredMinSize == null)
      return getPreferredSize(tree);
    else
      return preferredMinSize;
  }

  /**
   * Returns the preferred size to properly display the tree, this is a cover
   * method for getPreferredSize(c, false).
   * 
   * @param c the component whose preferred size is being queried; this argument
   *          is often ignored but might be used if the UI object is stateless
   *          and shared by multiple components
   * @return the preferred size
   */
  public Dimension getPreferredSize(JComponent c)
  {
    return getPreferredSize(c, false);
  }

  /**
   * Returns the preferred size to represent the tree in c. If checkConsistancy
   * is true, checkConsistancy is messaged first.
   * 
   * @param c the component whose preferred size is being queried.
   * @param checkConsistancy if true must check consistancy
   * @return the preferred size
   */
  public Dimension getPreferredSize(JComponent c, boolean checkConsistancy)
  {
    if (! validCachedPreferredSize)
      {
        Rectangle size = tree.getBounds();
        // Add the scrollbar dimensions to the preferred size.
        preferredSize = new Dimension(treeState.getPreferredWidth(size),
                                      treeState.getPreferredHeight());
        validCachedPreferredSize = true;
      }
    return preferredSize;
  }

  /**
   * Returns the minimum size for this component. Which will be the min
   * preferred size or (0,0).
   * 
   * @param c the component whose min size is being queried.
   * @returns the preferred size or null
   */
  public Dimension getMinimumSize(JComponent c)
  {
    return preferredMinSize = getPreferredSize(c);
  }

  /**
   * Returns the maximum size for the component, which will be the preferred
   * size if the instance is currently in JTree or (0,0).
   * 
   * @param c the component whose preferred size is being queried
   * @return the max size or null
   */
  public Dimension getMaximumSize(JComponent c)
  {
    return getPreferredSize(c);
  }

  /**
   * Messages to stop the editing session. If the UI the receiver is providing
   * the look and feel for returns true from
   * <code>getInvokesStopCellEditing</code>, stopCellEditing will be invoked
   * on the current editor. Then completeEditing will be messaged with false,
   * true, false to cancel any lingering editing.
   */
  protected void completeEditing()
  {
    completeEditing(false, true, false);
  }

  /**
   * Stops the editing session. If messageStop is true, the editor is messaged
   * with stopEditing, if messageCancel is true the editor is messaged with
   * cancelEditing. If messageTree is true, the treeModel is messaged with
   * valueForPathChanged.
   * 
   * @param messageStop message to stop editing
   * @param messageCancel message to cancel editing
   * @param messageTree message to treeModel
   */
  protected void completeEditing(boolean messageStop, boolean messageCancel,
                                 boolean messageTree)
  {
    // Make no attempt to complete the non existing editing session.
    if (!isEditing(tree))
      return;
    
    if (messageStop)
      {
        getCellEditor().stopCellEditing();
        stopEditingInCompleteEditing = true;
      }

    if (messageCancel)
      {
        getCellEditor().cancelCellEditing();
        stopEditingInCompleteEditing = true;
      }

    if (messageTree)
      {
        TreeCellEditor editor = getCellEditor();
        if (editor != null)
          {
            Object value = editor.getCellEditorValue();
            treeModel.valueForPathChanged(tree.getLeadSelectionPath(), value);
          }
      }
  }

  /**
   * Will start editing for node if there is a cellEditor and shouldSelectCall
   * returns true. This assumes that path is valid and visible.
   * 
   * @param path is the path to start editing
   * @param event is the MouseEvent performed on the path
   * @return true if successful
   */
  protected boolean startEditing(TreePath path, MouseEvent event)
  {
    updateCellEditor();
    TreeCellEditor ed = getCellEditor();

    if (ed != null && (event == EDIT || ed.shouldSelectCell(event))
        && ed.isCellEditable(event))
      {
        Rectangle bounds = getPathBounds(tree, path);

        // Extend the right boundary till the tree width.
        bounds.width = tree.getWidth() - bounds.x;

        editingPath = path;
        editingRow = tree.getRowForPath(editingPath);

        Object value = editingPath.getLastPathComponent();

        stopEditingInCompleteEditing = false;
        boolean expanded = tree.isExpanded(editingPath);
        isEditing = true;
        editingComponent = ed.getTreeCellEditorComponent(tree, value, true,
                                                         expanded,
                                                         isLeaf(editingRow),
                                                         editingRow);

        // Remove all previous components (if still present). Only one
        // container with the editing component inside is allowed in the tree.
        tree.removeAll();

        // The editing component must be added to its container. We add the
        // container, not the editing component itself.
        Component container = editingComponent.getParent();
        container.setBounds(bounds);
        tree.add(container);
        editingComponent.requestFocus();

        return true;
      }
    return false;
  }

  /**
   * If the <code>mouseX</code> and <code>mouseY</code> are in the expand or
   * collapse region of the row, this will toggle the row.
   * 
   * @param path the path we are concerned with
   * @param mouseX is the cursor's x position
   * @param mouseY is the cursor's y position
   */
  protected void checkForClickInExpandControl(TreePath path, int mouseX,
                                              int mouseY)
  {
    if (isLocationInExpandControl(path, mouseX, mouseY))
      handleExpandControlClick(path, mouseX, mouseY);
  }

  /**
   * Returns true if the <code>mouseX</code> and <code>mouseY</code> fall in
   * the area of row that is used to expand/collpse the node and the node at row
   * does not represent a leaf.
   * 
   * @param path the path we are concerned with
   * @param mouseX is the cursor's x position
   * @param mouseY is the cursor's y position
   * @return true if the <code>mouseX</code> and <code>mouseY</code> fall in
   *         the area of row that is used to expand/collpse the node and the
   *         node at row does not represent a leaf.
   */
  protected boolean isLocationInExpandControl(TreePath path, int mouseX,
                                              int mouseY)
  {
    boolean cntlClick = false;
    if (! treeModel.isLeaf(path.getLastPathComponent()))
      {
        int width;
        Icon expandedIcon = getExpandedIcon();
        if (expandedIcon != null)
          width = expandedIcon.getIconWidth();
        else
          // Only guessing. This is the width of
          // the tree control icon in Metal L&F.
          width = 18;

        Insets i = tree.getInsets();
        
        int depth;
        if (isRootVisible())
          depth = path.getPathCount()-1;
        else
          depth = path.getPathCount()-2;
        
        int left = getRowX(tree.getRowForPath(path), depth)
                   - width + i.left;
        cntlClick = mouseX >= left && mouseX <= left + width;
      }
    return cntlClick;
  }

  /**
   * Messaged when the user clicks the particular row, this invokes
   * toggleExpandState.
   * 
   * @param path the path we are concerned with
   * @param mouseX is the cursor's x position
   * @param mouseY is the cursor's y position
   */
  protected void handleExpandControlClick(TreePath path, int mouseX, int mouseY)
  {
    toggleExpandState(path);
  }

  /**
   * Expands path if it is not expanded, or collapses row if it is expanded. If
   * expanding a path and JTree scroll on expand, ensureRowsAreVisible is
   * invoked to scroll as many of the children to visible as possible (tries to
   * scroll to last visible descendant of path).
   * 
   * @param path the path we are concerned with
   */
  protected void toggleExpandState(TreePath path)
  {
    // tree.isExpanded(path) would do the same, but treeState knows faster.
    if (treeState.isExpanded(path))
      tree.collapsePath(path);
    else
      tree.expandPath(path);
  }

  /**
   * Returning true signifies a mouse event on the node should toggle the
   * selection of only the row under the mouse. The BasisTreeUI treats the
   * event as "toggle selection event" if the CTRL button was pressed while
   * clicking. The event is not counted as toggle event if the associated
   * tree does not support the multiple selection.
   * 
   * @param event is the MouseEvent performed on the row.
   * @return true signifies a mouse event on the node should toggle the
   *         selection of only the row under the mouse.
   */
  protected boolean isToggleSelectionEvent(MouseEvent event)
  {
    return 
      (tree.getSelectionModel().getSelectionMode() != 
        TreeSelectionModel.SINGLE_TREE_SELECTION) &&
      ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0);  
  }

  /**
   * Returning true signifies a mouse event on the node should select from the
   * anchor point. The BasisTreeUI treats the event as "multiple selection
   * event" if the SHIFT button was pressed while clicking. The event is not
   * counted as multiple selection event if the associated tree does not support
   * the multiple selection.
   * 
   * @param event is the MouseEvent performed on the node.
   * @return true signifies a mouse event on the node should select from the
   *         anchor point.
   */
  protected boolean isMultiSelectEvent(MouseEvent event)
  {
    return 
      (tree.getSelectionModel().getSelectionMode() != 
        TreeSelectionModel.SINGLE_TREE_SELECTION) &&
      ((event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0);  
  }

  /**
   * Returning true indicates the row under the mouse should be toggled based on
   * the event. This is invoked after checkForClickInExpandControl, implying the
   * location is not in the expand (toggle) control.
   * 
   * @param event is the MouseEvent performed on the row.
   * @return true indicates the row under the mouse should be toggled based on
   *         the event.
   */
  protected boolean isToggleEvent(MouseEvent event)
  {
    boolean toggle = false;
    if (SwingUtilities.isLeftMouseButton(event))
      {
        int clickCount = tree.getToggleClickCount();
        if (clickCount > 0 && event.getClickCount() == clickCount)
          toggle = true;
      }
    return toggle;
  }

  /**
   * Messaged to update the selection based on a MouseEvent over a particular
   * row. If the even is a toggle selection event, the row is either selected,
   * or deselected. If the event identifies a multi selection event, the
   * selection is updated from the anchor point. Otherwise, the row is selected,
   * and the previous selection is cleared.</p>
   * 
   * @param path is the path selected for an event
   * @param event is the MouseEvent performed on the path.
   * 
   * @see #isToggleSelectionEvent(MouseEvent)
   * @see #isMultiSelectEvent(MouseEvent)
   */
  protected void selectPathForEvent(TreePath path, MouseEvent event)
  {
    if (isToggleSelectionEvent(event))
      {
        // The event selects or unselects the clicked row.
        if (tree.isPathSelected(path))
          tree.removeSelectionPath(path);
        else
          {
            tree.addSelectionPath(path);
            tree.setAnchorSelectionPath(path);
          }
      }
    else if (isMultiSelectEvent(event))
      {
        // The event extends selection form anchor till the clicked row.
        TreePath anchor = tree.getAnchorSelectionPath();
        if (anchor != null)
          {
            int aRow = getRowForPath(tree, anchor);
            tree.addSelectionInterval(aRow, getRowForPath(tree, path));
          }
        else
          tree.addSelectionPath(path);
      }
    else
      {
        // This is an ordinary event that just selects the clicked row.
        tree.setSelectionPath(path);
        if (isToggleEvent(event))
          toggleExpandState(path);
      }
  }

  /**
   * Returns true if the node at <code>row</code> is a leaf.
   * 
   * @param row is the row we are concerned with.
   * @return true if the node at <code>row</code> is a leaf.
   */
  protected boolean isLeaf(int row)
  {
    TreePath pathForRow = getPathForRow(tree, row);
    if (pathForRow == null)
      return true;

    Object node = pathForRow.getLastPathComponent();
    return treeModel.isLeaf(node);
  }
  
  /**
   * The action to start editing at the current lead selection path.
   */
  class TreeStartEditingAction
      extends AbstractAction
  {
    /**
     * Creates the new tree cancel editing action.
     * 
     * @param name the name of the action (used in toString).
     */
    public TreeStartEditingAction(String name)
    {
      super(name);
    }    
    
    /**
     * Start editing at the current lead selection path.
     * 
     * @param e the ActionEvent that caused this action.
     */
    public void actionPerformed(ActionEvent e)
    {
      TreePath lead = tree.getLeadSelectionPath();
      if (!tree.isEditing()) 
        tree.startEditingAtPath(lead);
    }
  }  

  /**
   * Updates the preferred size when scrolling, if necessary.
   */
  public class ComponentHandler
      extends ComponentAdapter
      implements ActionListener
  {
    /**
     * Timer used when inside a scrollpane and the scrollbar is adjusting
     */
    protected Timer timer;

    /** ScrollBar that is being adjusted */
    protected JScrollBar scrollBar;

    /**
     * Constructor
     */
    public ComponentHandler()
    {
      // Nothing to do here.
    }

    /**
     * Invoked when the component's position changes.
     * 
     * @param e the event that occurs when moving the component
     */
    public void componentMoved(ComponentEvent e)
    {
      if (timer == null)
        {
          JScrollPane scrollPane = getScrollPane();
          if (scrollPane == null)
            updateSize();
          else
            {
              // Determine the scrollbar that is adjusting, if any, and
              // start the timer for that. If no scrollbar is adjusting,
              // we simply call updateSize().
              scrollBar = scrollPane.getVerticalScrollBar();
              if (scrollBar == null || !scrollBar.getValueIsAdjusting())
                {
                  // It's not the vertical scrollbar, try the horizontal one.
                  scrollBar = scrollPane.getHorizontalScrollBar();
                  if (scrollBar != null && scrollBar.getValueIsAdjusting())
                    startTimer();
                  else
                    updateSize();
                }
              else
                {
                  startTimer();
                }
            }
        }
    }

    /**
     * Creates, if necessary, and starts a Timer to check if needed to resize
     * the bounds
     */
    protected void startTimer()
    {
      if (timer == null)
        {
          timer = new Timer(200, this);
          timer.setRepeats(true);
        }
      timer.start();
    }

    /**
     * Returns the JScrollPane housing the JTree, or null if one isn't found.
     * 
     * @return JScrollPane housing the JTree, or null if one isn't found.
     */
    protected JScrollPane getScrollPane()
    {
      JScrollPane found = null;
      Component p = tree.getParent();
      while (p != null && !(p instanceof JScrollPane))
        p = p.getParent();
      if (p instanceof JScrollPane)
        found = (JScrollPane) p;
      return found;
    }

    /**
     * Public as a result of Timer. If the scrollBar is null, or not adjusting,
     * this stops the timer and updates the sizing.
     * 
     * @param ae is the action performed
     */
    public void actionPerformed(ActionEvent ae)
    {
      if (scrollBar == null || !scrollBar.getValueIsAdjusting())
        {
          if (timer != null)
            timer.stop();
          updateSize();
          timer = null;
          scrollBar = null;
        }
    }
  }

  /**
   * Listener responsible for getting cell editing events and updating the tree
   * accordingly.
   */
  public class CellEditorHandler
      implements CellEditorListener
  {
    /**
     * Constructor
     */
    public CellEditorHandler()
    {
      // Nothing to do here.
    }

    /**
     * Messaged when editing has stopped in the tree. Tells the listeners
     * editing has stopped.
     * 
     * @param e is the notification event
     */
    public void editingStopped(ChangeEvent e)
    {
      stopEditing(tree);
    }

    /**
     * Messaged when editing has been canceled in the tree. This tells the
     * listeners the editor has canceled editing.
     * 
     * @param e is the notification event
     */
    public void editingCanceled(ChangeEvent e)
    {
      cancelEditing(tree);
    }
  } // CellEditorHandler

  /**
   * Repaints the lead selection row when focus is lost/grained.
   */
  public class FocusHandler
      implements FocusListener
  {
    /**
     * Constructor
     */
    public FocusHandler()
    {
      // Nothing to do here.
    }

    /**
     * Invoked when focus is activated on the tree we're in, redraws the lead
     * row. Invoked when a component gains the keyboard focus. The method
     * repaints the lead row that is shown differently when the tree is in
     * focus.
     * 
     * @param e is the focus event that is activated
     */
    public void focusGained(FocusEvent e)
    {
      repaintLeadRow();
    }

    /**
     * Invoked when focus is deactivated on the tree we're in, redraws the lead
     * row. Invoked when a component loses the keyboard focus. The method
     * repaints the lead row that is shown differently when the tree is in
     * focus.
     * 
     * @param e is the focus event that is deactivated
     */
    public void focusLost(FocusEvent e)
    {
      repaintLeadRow();
    }

    /**
     * Repaint the lead row.
     */
    void repaintLeadRow()
    {
      TreePath lead = tree.getLeadSelectionPath();
      if (lead != null)
        tree.repaint(tree.getPathBounds(lead));
    }
  }

  /**
   * This is used to get multiple key down events to appropriately genereate
   * events.
   */
  public class KeyHandler
      extends KeyAdapter
  {
    /** Key code that is being generated for. */
    protected Action repeatKeyAction;

    /** Set to true while keyPressed is active */
    protected boolean isKeyDown;

    /**
     * Constructor
     */
    public KeyHandler()
    {
      // Nothing to do here.
    }

    /**
     * Invoked when a key has been typed. Moves the keyboard focus to the first
     * element whose first letter matches the alphanumeric key pressed by the
     * user. Subsequent same key presses move the keyboard focus to the next
     * object that starts with the same letter.
     * 
     * @param e the key typed
     */
    public void keyTyped(KeyEvent e)
    {
      char typed = Character.toLowerCase(e.getKeyChar());
      for (int row = tree.getLeadSelectionRow() + 1;
        row < tre
