//==============================================================================
// DataObjectTree.java
// Copyright (c) 2000 WAP Solutions Ltd.
//==============================================================================

package wsl.fw.gui;

import java.util.Vector;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Collections;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JTree;
import javax.swing.Icon;
import wsl.fw.util.Type;
import wsl.fw.util.Util;
import wsl.fw.util.Log;
import wsl.fw.datasource.DataManager;
import wsl.fw.datasource.DataObject;
import wsl.fw.datasource.DataChangeListener;
import wsl.fw.datasource.DataChangeNotification;
import wsl.fw.datasource.DataListenerData;
import wsl.fw.datasource.RecordSet;
import wsl.fw.datasource.DataSourceException;

//------------------------------------------------------------------------------
/**
 * Extensions to JTree providing convenience functions for manipulating trees
 * containing DataObjects in DoTreeNodes.
 * You may need to do a refreshModel() after adding nodes to the root.
 * It is a good idea to do a scrollPathToVisible() after adding a node to
 * ensure it is expanded and visible.
 * By default the tree is unsorted and new nodes are added after their siblings.
 * To implement a sorted tree (sorted within sibling set, not the whole tree)
 * use setSort() to set a Comparator succh as DoComparator. The sort criteria
 * only affect modes added with DataObjectTree.addNode() function, not nodes
 * added directly to a parent node.
 */
public class DataObjectTree extends JTree implements DataChangeListener
{
    // version tag
    private final static String _ident = "$Date: 2002/06/11 23:11:42 $  $Revision: 1.1.1.1 $ "
        + "$Archive: /Framework/Source/wsl/fw/gui/DataObjectTree.java $ ";

    // attributes
    private Comparator _comparator = null;

    //--------------------------------------------------------------------------
    /**
     * Default constructor, creates an empty tree (a single non-visible root)
     * that uses the custom DataObjectTreeCellRenderer to draw the tree cells.
     */
    public DataObjectTree()
    {
        super(new DoTreeNode("root"));
        setCellRenderer(new DataObjectTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
    }

    //--------------------------------------------------------------------------
    /**
     * Default constructor, creates a tree with a root
     * that uses the custom DataObjectTreeCellRenderer to draw the tree cells.
     */
    public DataObjectTree(String rootText)
    {
        super(new DoTreeNode(rootText));
        setCellRenderer(new DataObjectTreeCellRenderer());
        setRootVisible(true);
        setShowsRootHandles(true);
    }

    //--------------------------------------------------------------------------
    /**
     * Get the root DefaultMutableTreeNode.
     * @return the root node;
     */
    public DoTreeNode getRoot()
    {
        return (DoTreeNode) getModel().getRoot();
    }

    //--------------------------------------------------------------------------
    /**
     * Set the root node, this will cause the model to be refreshed.
     * @param rootNode, the root node to set.
     */
    public void setRoot(TreeNode rootNode)
    {
        ((DefaultTreeModel) getModel()).setRoot(rootNode);
    }

    /**
     * Clear the tree
     */
    public void clear()
    {
        if(getRoot() != null)
            clearNode(getRoot());
    }

    //--------------------------------------------------------------------------
    /**
     * Refresh the display of the tree model. Do this after insert/update/delete
     * of nodes to ensure the view is up to date.
     */
    public void refreshModel()
    {
        ((DefaultTreeModel) getModel()).reload();
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new node to this tree with the specified parent.
     * @param parent, the parent of the new node.
     * @param obj, the user object for the new node, most often a DataObject.
     */
    public DoTreeNode addNode(DefaultMutableTreeNode parent, Object obj)
    {
        // delegate
        return addNode(parent, obj, null, null);
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new node to this tree with the specified parent.
     * @param parent, the parent of the new node.
     * @param obj, the user object for the new node, most often a DataObject.
     * @param doAddDummy if true adds a child node to the new node, enabling a "+" on it
     */
    public DoTreeNode addNode(DefaultMutableTreeNode parent, Object obj, boolean doAddDummy)
    {
        // delegate
        return addNode(parent, obj, null, null, doAddDummy);
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new node to this tree with the specified parent.
     * @param parent, the parent of the new node.
     * @param obj, the user object for the new node, most often a DataObject.
     * @param extraData, any extra data to store.
     * @param icon, the icon for the new node, if null uses default icons.
     */
    public DoTreeNode addNode(DefaultMutableTreeNode parent, Object obj,
        Object extraData, Icon icon)
    {
        return addNode(parent, obj, extraData, icon, true);
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new node to this tree with the specified parent.
     * @param parent, the parent of the new node.
     * @param obj, the user object for the new node, most often a DataObject.
     * @param extraData, any extra data to store.
     * @param icon, the icon for the new node, if null uses default icons
     * @param doAddDummy if true adds a child node to the new node, enabling a "+" on it
     */
    public DoTreeNode addNode(DefaultMutableTreeNode parent, Object obj,
        Object extraData, Icon icon, boolean doAddDummy)
    {
        // create a new node with the object and optional icon
        DoTreeNode newNode = new DoTreeNode(obj, extraData, icon);

        // get the model and insert the new node into the model, sorted if
        // there is a sort comparator set
        DefaultTreeModel model       = (DefaultTreeModel) getModel();
        int              insertIndex = getInsertIndex(parent, obj);

        model.insertNodeInto(newNode, parent, insertIndex);

        // add a dummy
        if(doAddDummy)
        {
            DoTreeNode dummy = new DoTreeNode("Dummy", null);
            newNode.add(dummy);
        }

        return newNode;
    }

    //--------------------------------------------------------------------------
    /**
     * Get the correct index for inserting a new node. Used by addNode().
     * Index is after all siblings if _comparator is null, else chooses a
     * index in the sorted order using _comparator.
     * Note that if the children are unsorted (i.e. nodes were added before the
     * sort comparator was set) then the results are undefined.
     * @param parent, the parent node.
     * @param obj, the object that is to be compared with the userObjects
     *   of parent's cheildren when performing the sored insert.
     * @return the index at which to insert the node, either at the end or in
     *   the correct sort position depending on _comparator.
     */
    private int getInsertIndex(DefaultMutableTreeNode parent, Object obj)
    {
        int childCount = parent.getChildCount();
        int index;

        // if no comparator insert at end
        if (_comparator == null)
            index = childCount;
        else
        {
            // perform a binary search to find the correct insert position
            // code copied and modified from Collections.binarySearch

        	int low  = 0;
        	int high = childCount - 1;

            index = low;

        	while (low <= high)
            {
        	    int mid = (low + high) / 2;
        	    Object midVal = parent.getChildAt(mid);
        	    int cmp = _comparator.compare(midVal, obj);

        	    if (cmp < 0)
                {
                    // too low
        		    low = mid + 1;
                    index = low;
                }
        	    else if (cmp > 0)
                {
                    // too high
        		    high = mid - 1;
                    index = low;
                }
        	    else
                {
                    // found
        		    index = mid;
                    break;
                }
        	}
        }

        return index;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the DoTreeNode that is currently selected.
     * @return the selected DoTreeNode, null if no selection or if the selected
     *   node is not a DoTreeNode.
     */
    public DoTreeNode getSelectedNode()
    {
        TreePath path = getSelectionPath();
        if (path != null)
        {
            Object obj = path.getLastPathComponent();
            if (obj instanceof DoTreeNode)
                return (DoTreeNode) obj;
        }

        return  null;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the DataObject in the currently selected node.
     * @return the DataObject or null if no selection or no DataObject.
     */
    public DataObject getSelectedDataObject()
    {
        DoTreeNode node = getSelectedNode();
        return (node != null) ? node.getDataObject() : null;
    }

    /**
     * @return the DataObject belonging to the param node
     * @param node the node containing the DataObject
     */
    public DataObject getDataObject(DefaultMutableTreeNode node)
    {
        DataObject dobj = null;
        if(node != null)
        {
            Object obj = node.getUserObject();
            dobj = (obj instanceof DataObject) ? ((DataObject) obj) : null;
        }
        return dobj;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the extra data in the currently selected node.
     * @return the extra data or null if no selection or no extra data.
     */
    public Object getSelectedExtraData()
    {
        DoTreeNode node = getSelectedNode();
        return (node != null) ? node.getExtraData() : null;
    }

    //--------------------------------------------------------------------------
    /**
     * Add a listener to the tree using the specified DataListenerData.
     * If using this function you must call removeListener() or
     * removeAllListeners() to ensure that the listeners are freed.
     * @param dld, the DataListenerData object specifying what to listen to.
     *   The listener object in the dld is set to this (the DataObjectTree).
     */
    public void addListener(DataListenerData dld)
    {
        // any listener we set must return the notifications to us
        dld._listener = this;

        // add to the DataManager
        DataManager.addDataChangeListener(dld);
    }

    //--------------------------------------------------------------------------
    /**
     * Remove a listener.
     * @param dld, a DataListenerData specifying the listener to remove.
     *   The listener object in the dld is set to this (the DataObjectTree).
     */
    public void removeListener(DataListenerData dld)
    {
        // we are the listener, so ensure it is set correctly
        dld._listener = this;

        // remove from the DataManager
        DataManager.removeDataChangeListener(dld);
    }

    //--------------------------------------------------------------------------
    /**
     * Remove all listeners. If any listeners are added this should be called
     * when the panel owning this DataObjectTree is closed.
     */
    public void removeAllListeners()
    {
        DataManager.removeAllDataChangeListeners(this);
    }

    //--------------------------------------------------------------------------
    /**
     * Notification of DataObject change event, the tree will check if it
     * contains any DataObjects which match the key of those changed and will
     * refresh itself.
     * @param DataChangeNotification contains the data regarding data change
     */
    public synchronized void onDataChanged(DataChangeNotification notification)
    {
        // ignore inserts, we don't know if the tree should have the object
        // so just ignore it and let the container worry about refreshing
        if (notification.getChangeType() != DataChangeNotification.INSERT)
        {
            Vector changedNodes = new Vector();

            // get the model and root node
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            DefaultMutableTreeNode root = getRoot();

            // iterate over all elements in the tree looking for matches
            Enumeration enums = root.depthFirstEnumeration();
            while (enums.hasMoreElements())
            {
                DoTreeNode node = (DoTreeNode) enums.nextElement();
                Object obj = node.getUserObject();
                DataObject primary = (obj instanceof DataObject) ? ((DataObject)obj) : null;
                obj = node.getExtraData();
                DataObject secondary = (obj != null && obj instanceof DataObject)? (DataObject)obj: null;

                if(primary != null)
                {
                    // compare primary
                    boolean primaryEqual = (primary != null && primary.equals(notification.getDataObject()));
                    if(primaryEqual)
                    {
                        // import values
                        // dont bother if exactly the same object
                        if(notification.getChangeType() == DataChangeNotification.UPDATE &&
                            primary != notification.getDataObject())
                            primary.importValues(notification.getDataObject());

                        // match found, add to the changed list
                        changedNodes.add(node);
                    }

                    // compare secondary
                    boolean secEqual = (secondary != null && secondary.equals(notification.getDataObject()));
                    if(secEqual)
                    {
                        // import values
                        // dont bother if exactly the same object
                        if(notification.getChangeType() == DataChangeNotification.UPDATE &&
                            secondary != notification.getDataObject())
                            secondary.importValues(notification.getDataObject());

                        // match found, add to the changed list
                        changedNodes.add(node);
                    }
               }
            }

            // do the actual changes at the end so that in the case of deletes
            // the structural change does not upset the enumeration
            for (int i = 0; i < changedNodes.size(); i++)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) changedNodes.get(i);
                if (notification.getChangeType() == DataChangeNotification.DELETE)
                {
                    // delete, remove the node from the model
                    model.removeNodeFromParent(node);
                }
                else if (notification.getChangeType() == DataChangeNotification.UPDATE)
                {
                    // inform the model of the change
                    model.nodeChanged(node);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Build a node from a RecordSet
     * @param rs the RecordSet to build the node from
     * @param parentNode the parent node to build
     */
    public void buildFromRecordSet(RecordSet rs, DoTreeNode parentNode) throws DataSourceException
    {
        buildFromRecordSet(rs, parentNode, true);
    }

    //--------------------------------------------------------------------------
    /**
     * Build a node from a RecordSet
     * @param rs the RecordSet to build the node from
     * @param parentNode the parent node to build
     */
    public void buildFromRecordSet(RecordSet rs, DoTreeNode parentNode, boolean doAddDummy)
        throws DataSourceException
    {
        // validate params
        Util.argCheckNull(rs);
        Util.argCheckNull(parentNode);

        // add to tree and recurse to get sub-branches
        DataObject dobj;
        while(rs.next())
        {
            // get the DataObject
            dobj = rs.getCurrentObject();
            if(dobj != null)
            {
                // add to tree as child
                DoTreeNode newNode = addNode(parentNode, dobj, doAddDummy);
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Build a node from a Vector of DataObjects
     * @param v the Vector to build the node from
     * @param parentNode the parent node to build
     */
    public void buildFromVector(Vector v, DoTreeNode parentNode) throws DataSourceException
    {
        // delegtea
        buildFromVector(v, parentNode, true);
    }

    //--------------------------------------------------------------------------
    /**
     * Build a node from a Vector of DataObjects
     * @param v the Vector to build the node from
     * @param parentNode the parent node to build
     */
    public void buildFromVector(Vector v, DoTreeNode parentNode, boolean doAddDummy)
        throws DataSourceException
    {
        // validate params
        Util.argCheckNull(v);
        Util.argCheckNull(parentNode);

        // add to tree and recurse to get sub-branches
        DataObject dobj;
        for(int i = 0; i < v.size(); i++)
        {
            // get the DataObject
            dobj = (DataObject)v.elementAt(i);
            if(dobj != null)
            {
                // add to tree as child
                DoTreeNode newNode = addNode(parentNode, dobj, doAddDummy);
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * @return the default tree model
     */
    public DefaultTreeModel getDefaultModel()
    {
        return (DefaultTreeModel)getModel();
    }

    //--------------------------------------------------------------------------
    /**
     * Removes all the children from a node
     */
    public void clearNode(DoTreeNode parentNode)
    {

        // iterate the children and remove them
        DefaultMutableTreeNode childNode;
        while(parentNode.getChildCount() > 0)
        {
            childNode = (DefaultMutableTreeNode)parentNode.getChildAt(0);
            getDefaultModel().removeNodeFromParent(childNode);
        }
        setExpandedState(new TreePath(parentNode.getPath()), true);
    }

    //--------------------------------------------------------------------------
    /**
     * Removes all instances of the DataObject param
     * @param dobj the DataObject to remove all instances of
     * @return true if any instances found
     */
    public boolean removeAllInstances(DataObject dobj)
    {
        // iterate over all elements in the tree looking for matches
        Vector v = new Vector();
        boolean isFound = false;
        Enumeration enums = getRoot().depthFirstEnumeration();
        while (enums.hasMoreElements())
        {
            // get the data object
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enums.nextElement();
            DataObject childDobj = getDataObject(node);

            // compare and remove
            if (childDobj != null && childDobj.equals(dobj))
            {
                v.add(node);
                isFound = true;
            }
        }

        // iterate vector and remove
        for(int i = 0; i < v.size(); i++)
            getDefaultModel().removeNodeFromParent((DoTreeNode)v.elementAt(i));
        return isFound;
    }

    //--------------------------------------------------------------------------
    /**
     * Remove the param node from the tree
     */
    public void removeNode(DoTreeNode node)
    {
        getDefaultModel().removeNodeFromParent(node);
    }

    //--------------------------------------------------------------------------
    /**
     * Remove the selected node from the tree
     */
    public void removeSelectedNode()
    {
        removeNode(this.getSelectedNode());
    }

    //--------------------------------------------------------------------------
    /**
     * Set a comparator to sort nodes added by addNode(). Setting a sort does
     * not retroactively sort existing nodes so any sorts should be set before
     * any nodes are added. Nodes are sorted using the comparison and ordering
     * specified by the comparator and are sorted with respect to their
     * siblings, not the whole tree. If the Comparator is null (the default)
     * there is no sorting and new nodes are added after all their siblings.
     * @param comparator, the Comparator to perform the sorting, may be null.
     */
    public void setSort(Comparator comparator)
    {
        _comparator = comparator;
    }


    //--------------------------------------------------------------------------
    // find methods

    /**
     * Find DataObject from class, field and value, Searches from the root.
     * @param c the class
     * @param fieldName the name of the field to compare
     * @param value the value for the field
     * @return DoTreeNode the node of the found DataObject or null if not found
     */
    public DoTreeNode findDataObject(Class c, String fieldName, Object value)
    {
        // delegate with root
        return findDataObject(getRoot(), c, fieldName, value);
    }

    /**
     * Find DataObject from class, field and value.Searches from the param node.
     * @param parentNode the node to search from
     * @param c the class
     * @param fieldName the name of the field to compare
     * @param val the value for the field
     * @return DoTreeNode the node of the found DataObject or null if not found
     */
    public DoTreeNode findDataObject(DoTreeNode parentNode, Class c,
        String fieldName, Object value)
    {
        // validate
        Util.argCheckNull(parentNode);
        Util.argCheckNull(c);
        Util.argCheckEmpty(fieldName);
        Util.argCheckNull(value);

        // compare DataObjects
        DataObject dobj = this.getDataObject(parentNode);
        if(dobj != null && c.isInstance(dobj))
        {
            // compare field values
            Object temp = dobj.getObjectValue(fieldName);
            if(temp != null)
            {
                // if values equal, we have found it
                String strTemp = Type.objectToString(temp);
                if(strTemp.equals(Type.objectToString(value)))
                    return parentNode;
            }
        }

        // not found, recurse into children
        DoTreeNode ret = null;
        Enumeration children = parentNode.children();
        DoTreeNode childNode;
        while(ret == null && children != null && children.hasMoreElements())
        {
            // get the child node
            childNode = (DoTreeNode)children.nextElement();
            if(childNode != null)
                ret = findDataObject(childNode, c, fieldName, value);
        }

        // return
        return ret;
    }
}

//==============================================================================
// end of file DataObjectTree.java
//==============================================================================

