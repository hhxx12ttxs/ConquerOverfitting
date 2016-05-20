package thaw.plugins.index;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import thaw.core.Config;
import thaw.fcp.FreenetURIHelper;
import thaw.core.I18n;
import thaw.gui.IconBox;
import thaw.core.Logger;
import thaw.fcp.FCPQueueManager;
import thaw.gui.JDragTree;
import thaw.plugins.ToolbarModifier;

/**
 * Manages the index tree and its menu (right-click).
 */
public class IndexTree extends java.util.Observable implements MouseListener, ActionListener, javax.swing.event.TreeSelectionListener {

	public final static Color SELECTION_COLOR = new Color(190, 190, 190);
	public final static Color LOADING_COLOR = new Color(230, 230, 230);
	public final static Color LOADING_SELECTION_COLOR = new Color(150, 150, 150);

	private JPanel panel;

	private JTree tree;
	private IndexRoot root;

	private JPopupMenu indexFolderMenu;
	private Vector indexFolderActions; /* IndexManagementHelper.MenuAction */
	// downloadIndexes
	// createIndex
	// addIndex
	// addCategory
	// renameCategory
	// deleteCategory
	// copyKeys


	private JPopupMenu indexAndFileMenu; /* hem ... and links ... */
	private Vector indexAndFileActions; /* hem ... and links ... */ /* IndexManagementHelper.MenuAction */

	private JMenu indexMenu;
	// download
	// insert
	// renameIndex
	// delete
	// change keys
	// copy public key
	// copy private key

	private JMenu fileMenu;
	// addFileAndInsert
	// addFileWithoutInserting
	// addAKey

	private JMenu linkMenu;
	// addALink

	private JMenu commentMenu;
	// readComments
	// postComments

	private boolean selectionOnly;

	private Vector selectedNodes = null;

	private DefaultTreeModel treeModel;

	private IndexBrowserPanel indexBrowser;

	private ToolbarModifier toolbarModifier;
	private Vector toolbarActions;


	private Vector updatingIndexes;


	/**
	 * @param queueManager Not used if selectionOnly is set to true
	 * @param config Not used if selectionOnly is set to true (used for lastDestinationDirectory and lastSourceDirectory)
	 */
	public IndexTree(final String rootName, boolean selectionOnly,
			 final FCPQueueManager queueManager,
			 final IndexBrowserPanel indexBrowser,
			 final Config config) {
		this.indexBrowser = indexBrowser;

		updatingIndexes = new Vector();

		this.selectionOnly = selectionOnly;

		panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 10));

		boolean loadOnTheFly = false;

		if (config != null && config.getValue("loadIndexTreeOnTheFly") != null)
			loadOnTheFly = Boolean.valueOf(config.getValue("loadIndexTreeOnTheFly")).booleanValue();

		root = new IndexRoot(queueManager, indexBrowser, rootName, loadOnTheFly);

		treeModel = new DefaultTreeModel(root);

		final IndexTreeRenderer treeRenderer = new IndexTreeRenderer();
		treeRenderer.setLeafIcon(IconBox.minIndexReadOnly);

		if (!selectionOnly) {
			tree = new JDragTree(treeModel);
		} else {
			tree = new JTree(treeModel);
		}
		
		tree.addMouseListener(this);
		tree.addTreeSelectionListener(this);

		tree.setCellRenderer(treeRenderer);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);

		// Menus :

		JMenuItem item;


		indexFolderMenu = new JPopupMenu(I18n.getMessage("thaw.plugin.index.category"));
		indexFolderActions = new Vector();

		indexAndFileMenu = new JPopupMenu();
		indexAndFileActions = new Vector();

		item = new JMenuItem("");
		indexAndFileMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.NodeNameDisplayer(item));

		indexAndFileMenu.addSeparator();

		indexMenu = new JMenu(I18n.getMessage("thaw.plugin.index.index"));
		indexMenu.setIcon(IconBox.minIndex);
		fileMenu = new JMenu(I18n.getMessage("thaw.common.files"));
		fileMenu.setIcon(IconBox.minFile);
		linkMenu = new JMenu(I18n.getMessage("thaw.plugin.index.links"));
		linkMenu.setIcon(IconBox.minLink);
		commentMenu = new JMenu(I18n.getMessage("thaw.plugin.index.comment.commentList"));
		commentMenu.setIcon(IconBox.minReadComments);


		// Folder menu
		item = new JMenuItem("");
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.NodeNameDisplayer(item));

		indexFolderMenu.addSeparator();
		
		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addAlreadyExistingIndex"),
			     IconBox.minIndexReadOnly);
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexReuser(queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addCategory"),
				IconBox.minFolderNew);
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexFolderAdder(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.createIndex"), IconBox.minIndexNew);
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexCreator(queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.downloadIndexes"),
				     IconBox.minRefreshAction);
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexDownloader(queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.markAllAsSeen"));
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexHasChangedFlagReseter(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.autoSortFolderAction"));
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexSorter(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.sortAlphabetically"));
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexFolderReorderer(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.rename"));
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexRenamer(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addToBlackList"),
							IconBox.minStop);
		indexFolderMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexBlackLister(indexBrowser, item));
		
		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.delete"), IconBox.minDelete);
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.IndexDeleter(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.copyKeys"), IconBox.minCopy);
		indexFolderMenu.add(item);
		indexFolderActions.add(new IndexManagementHelper.PublicKeyCopier(item));


		// Index menu

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.downloadIndex"),
				     IconBox.minRefreshAction);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexDownloader(queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.insertIndex"),
				     IconBox.minInsertions);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexUploader(queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.autoSortAction"));
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexSorter(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.rename"));
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexRenamer(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.exportIndex"),
				     IconBox.minExportAction);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexExporter(item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.importIndex"),
				     IconBox.minImportAction);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexImporter(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.delete"),
				     IconBox.minDelete);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexDeleter(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addToBlackList"),
				     IconBox.minStop);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexBlackLister(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.indexSettings"),
				     IconBox.minIndexSettings);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexModifier(queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.copyPrivateKey"));
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.PrivateKeyCopier(item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.copyKey"),
				     IconBox.minCopy);
		indexMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.PublicKeyCopier(item));


		// File menu

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addFilesWithInserting"),
				     IconBox.minInsertions);
		fileMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.FileInserterAndAdder(config, queueManager, indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addFilesWithoutInserting"),
				     IconBox.minAdd);
		fileMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.FileAdder(config, queueManager, indexBrowser, item));


		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addKeys"),
				     IconBox.minKey);
		fileMenu.add(item);
		IndexManagementHelper.IndexAction ac = new IndexManagementHelper.KeyAdder(indexBrowser, item);
		indexAndFileActions.add(ac);


		// Link menu
		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.addLink"),
				     IconBox.minMakeALinkAction);
		linkMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.LinkAdder(indexBrowser, item));


		// Comment menu
		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.comment.readComments"),
				     IconBox.minReadComments);
		commentMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexCommentViewer(indexBrowser, item));

		item = new JMenuItem(I18n.getMessage("thaw.plugin.index.comment.add"),
				     IconBox.minAddComment);
		commentMenu.add(item);
		indexAndFileActions.add(new IndexManagementHelper.IndexCommentAdder(queueManager, indexBrowser, item));


		indexAndFileMenu.add(indexMenu);
		indexAndFileMenu.add(fileMenu);
		indexAndFileMenu.add(linkMenu);
		indexAndFileMenu.add(commentMenu);

		updateMenuState(null);

		panel.add(new JScrollPane(tree), BorderLayout.CENTER);

		// Toolbar
		JButton button;
		IndexManagementHelper.IndexAction action;
		toolbarActions = new Vector();

		toolbarModifier = new ToolbarModifier(indexBrowser.getMainWindow());

		button = new JButton(IconBox.refreshAction);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.downloadIndexes"));
		button.setMnemonic(KeyEvent.VK_R);
		action = new IndexManagementHelper.IndexDownloader(queueManager, indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.folderNew);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.addCategory"));
		action = new IndexManagementHelper.IndexFolderAdder(indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.indexReuse);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.addAlreadyExistingIndex"));
		action = new IndexManagementHelper.IndexReuser(queueManager, indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.indexNew);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.createIndex"));
		action = new IndexManagementHelper.IndexCreator(queueManager, indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.indexSettings);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.indexSettings"));
		action = new IndexManagementHelper.IndexModifier(queueManager, indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.copy);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.copyKeys"));
		action = new IndexManagementHelper.PublicKeyCopier(button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.stop);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.addToBlackList"));
		button.setMnemonic(KeyEvent.VK_B);
		action = new IndexManagementHelper.IndexBlackLister(indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.delete);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.delete"));
		action = new IndexManagementHelper.IndexDeleter(indexBrowser, button);
		action.setTargets(null);
		if (!selectionOnly)
			tree.addKeyListener((IndexManagementHelper.IndexDeleter)action);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);


		toolbarModifier.addButtonToTheToolbar(null);

		button = new JButton(IconBox.addToIndexAction);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.addFilesWithoutInserting"));
		action = new IndexManagementHelper.FileAdder(config, queueManager, indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);

		button = new JButton(IconBox.makeALinkAction);
		button.setToolTipText(I18n.getMessage("thaw.plugin.index.addLink"));
		action = new IndexManagementHelper.LinkAdder(indexBrowser, button);
		action.setTargets(null);
		toolbarModifier.addButtonToTheToolbar(button);
		toolbarActions.add(action);
	}


	public IndexBrowserPanel getIndexBrowserPanel() {
		return indexBrowser;
	}


	/**
	 * Used by IndexBrowserPanel when the visibility changed
	 */
	public ToolbarModifier getToolbarModifier() {
		return toolbarModifier;
	}


	public javax.swing.JComponent getPanel() {
		return panel;
	}

	public void addTreeSelectionListener(final javax.swing.event.TreeSelectionListener tsl) {
		tree.addTreeSelectionListener(tsl);
	}
	
	public void checkSelection() {
		
		final TreePath[] paths = tree.getSelectionPaths();

		if(paths == null)
			return;
			
		selectedNodes = new Vector();
		
		for (int i = 0 ; i < paths.length ; i++) {
			selectedNodes.add(paths[i].getLastPathComponent());
		}

		indexBrowser.getDetailPanel().setTargets(selectedNodes);

		// Update toolbar
		for (final Iterator it = toolbarActions.iterator();
		     it.hasNext(); ) {
			final IndexManagementHelper.IndexAction action = (IndexManagementHelper.IndexAction)it.next();
			action.setTargets(selectedNodes);
		}
		
		// Update nodes
		
		for (int i = 0 ; i < paths.length ; i++) {
			IndexTreeNode selectedNode = (IndexTreeNode)paths[i].getLastPathComponent();
			
			if ((indexBrowser != null) && (selectedNode instanceof Index)) {
				indexBrowser.getUnknownIndexList().addLinks(((Index)selectedNode));

				if (((Index)selectedNode).hasChanged()) {
					((Index)selectedNode).setHasChangedFlag(false);
					redraw(paths[i]);
				}

				if (((Index)selectedNode).hasNewComment()) {
					((Index)selectedNode).setNewCommentFlag(false);
					redraw(paths[i]);
				}
			}
		}

		toolbarModifier.displayButtonsInTheToolbar();

		// Notify observers

		setChanged();
		notifyObservers(selectedNodes); /* will make the toolbar visible */
	}


	public void updateMenuState(final Vector nodes) {
		IndexManagementHelper.IndexAction action;
		

		for(final Iterator it = indexFolderActions.iterator();
		    it.hasNext();) {
			action = (IndexManagementHelper.IndexAction)it.next();
			action.setTargets(nodes);
		}

		for(final Iterator it = indexAndFileActions.iterator();
		    it.hasNext();) {
			action = (IndexManagementHelper.IndexAction)it.next();
			action.setTargets(nodes);
		}
	}


	public JTree getTree() {
		return tree;
	}

	public IndexRoot getRoot() {
		return root;
	}

	public void mouseClicked(final MouseEvent e) {
		checkSelection();
	}

	public void mouseEntered(final MouseEvent e) { }
	public void mouseExited(final MouseEvent e) { }

	public void mousePressed(final MouseEvent e) {
		if (!selectionOnly)
			showPopupMenu(e);
	}

	public void mouseReleased(final MouseEvent e) {
		if (!selectionOnly)
			showPopupMenu(e);
	}

	protected void showPopupMenu(final MouseEvent e) {
		if(e.isPopupTrigger()) {
			if(selectedNodes == null)
				return;
			
			updateMenuState(selectedNodes);

			if(selectedNodes.size() == 1 && selectedNodes.get(0) instanceof IndexFolder) {
				indexFolderMenu.show(e.getComponent(), e.getX(), e.getY());
			} else if(selectedNodes.size() >= 1) {
				indexAndFileMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public IndexTreeNode getSelectedNode() {
		final Object obj = tree.getLastSelectedPathComponent();

		if (obj == null)
			return null;

		if (obj instanceof IndexTreeNode)
			return (IndexTreeNode)obj;

		if (obj instanceof DefaultMutableTreeNode)
			return ((IndexTreeNode)(((DefaultMutableTreeNode)obj).getUserObject()));

		Logger.notice(this, "getSelectedNode(): Unknow kind of node ?!");

		return null;
	}


	public void actionPerformed(final ActionEvent e) {

	}



	public void refresh() {
		refresh(((IndexTreeNode)null));
	}


	public void refresh(IndexTreeNode node) {
		if (node != null)
			node.forceFlagsReload();

		if (treeModel != null) {
			if (node != null && node.isInTree())
				treeModel.reload(node.getTreeNode());
			else
				treeModel.reload(getRoot().getTreeNode());
		}
	}


	public void refresh(TreePath path) {
		Object[] nodes = path.getPath();
		for (int i = 0 ; i < nodes.length ; i++)
			refresh((IndexTreeNode)(nodes[i]));
	}


	public void redraw() {
		redraw((IndexTreeNode)null);
	}

	public void redraw(IndexTreeNode node, boolean parents) {
		if (!parents)
			redraw(node);
		else {
			while (node != null) {
				redraw(node);
				node = ((IndexTreeNode)(((MutableTreeNode)node).getParent()));
			}
		}
	}

	public void redraw(IndexTreeNode node) {
		if (node != null)
			node.forceFlagsReload();

		if (treeModel != null) {
			if (node != null && node.isInTree()) {
				treeModel.nodeChanged(node.getTreeNode());
			} else {
				treeModel.nodeChanged(getRoot().getTreeNode());
			}
		}
	}

	public void redraw(TreePath path) {
		if (path != null) {
			Object[] nodes = (path.getPath());

			for (int i = 0 ; i < nodes.length ; i++) {
				IndexTreeNode node = (IndexTreeNode)nodes[i];
				redraw(node);
			}
		}
		else
			redraw(getRoot());
	}



	/**
	 * Will find the corresponding index and all its parents, then
	 * will unfold the tree according to the path to reach the index
	 * and then select the index. (won't touch filetable / linktable)
	 */
	public Index selectIndex(int id) {
		int nmbFolders;
		int[] parentFolders = new int[64];

		if (indexBrowser == null
		    || indexBrowser.getDb() == null) {
			Logger.error(this, "selectIndex() : No access to the db ?!");
			return null;
		}

		try {
			synchronized(indexBrowser.getDb().dbLock) {
				PreparedStatement st =
					indexBrowser.getDb().getConnection().prepareStatement("SELECT folderId FROM indexParents WHERE indexId = ?");

				st.setInt(1, id);

				ResultSet set = st.executeQuery();

				for(nmbFolders = 0 ; set.next() ; nmbFolders++) {
					if (set.getObject("folderId") != null)
						parentFolders[nmbFolders] = set.getInt("folderId");
					else
						parentFolders[nmbFolders] = -1;
				}
				
				st.close();

				if (nmbFolders == 0) {
					Logger.error(this, "Unable to select specified index : Not found.");
					return null;
				}
			}

		} catch(SQLException e) {
			Logger.error(this, "Unable to select specified index because : "+e.toString());
			return null;
		}

		IndexTreeNode[] nodes = new IndexTreeNode[nmbFolders+1 /* +1 for the index */];

		nodes[0] = getRoot();

		for (int i = 1 ; i < nmbFolders; i++) {
			IndexFolder folder= null;

			for (int j= 0 ; j < nmbFolders && folder == null; j++) {
				if (parentFolders[j] < 0)
					continue;

				folder = ((IndexFolder)nodes[i-1]).getChildFolder(parentFolders[j]);

			}

			nodes[i] = folder;

			if (folder == null) {
				Logger.error(this, "SelectIndex : Woops, something is missing.");

				Logger.error(this, "Path found :");

				for (int j = 0 ; j < nodes.length && nodes[j] != null ; j++) {
					Logger.error(this,
						     " -> "
						     +Integer.toString(((IndexTreeNode)nodes[j]).getId())
						     + " - "
						     +nodes[j].toString());
				}

				return null;
			}
		}

		nodes[nmbFolders] = ((IndexFolder)nodes[nmbFolders-1]).getChildIndex(id);


		TreePath path = new TreePath(((Object[])nodes));

		tree.setSelectionPath(path);

		return ((Index)nodes[nmbFolders]);
	}



	public class IndexTreeRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		public IndexTreeRenderer() {
			super();
		}

		public java.awt.Component getTreeCellRendererComponent(final JTree tree,
								       Object value,
								       final boolean selected,
								       final boolean expanded,
								       final boolean leaf,
								       final int row,
								       final boolean hasFocus) {
			setBackgroundNonSelectionColor(Color.WHITE);
			setBackgroundSelectionColor(IndexTree.SELECTION_COLOR);
			setTextNonSelectionColor(Color.BLACK);
			setTextSelectionColor(Color.BLACK);

			if(value instanceof DefaultMutableTreeNode || value instanceof IndexTreeNode) {
				Object o;

				if (value instanceof DefaultMutableTreeNode)
					o = ((DefaultMutableTreeNode)value).getUserObject();
				else
					o = value;

				if(o instanceof Index) {
					final Index index = (Index)o;

					if (isIndexUpdating(index)) {
						setBackgroundNonSelectionColor(IndexTree.LOADING_COLOR);
						setBackgroundSelectionColor(IndexTree.LOADING_SELECTION_COLOR);
					}

					if (index.downloadSuccessful()) {
						if (index.isModifiable()) {
							setLeafIcon(IconBox.minIndex);
						} else {
							setLeafIcon(IconBox.minIndexReadOnly);
						}
					} else
						setLeafIcon(IconBox.minStop);

					if (index.isObsolete()) {
						setTextNonSelectionColor(Color.RED);
						setTextSelectionColor(Color.RED);
					}

				}

				if (o instanceof IndexTreeNode) {
					/* Remember that for the index category,
					   this kind of query is recursive */
					boolean hasChanged = ((IndexTreeNode)o).hasChanged();
					boolean newComment = ((IndexTreeNode)o).hasNewComment();
					boolean publishPrivateKey = ((IndexTreeNode)o).publishPrivateKey();

					int style = Font.PLAIN;

					if (publishPrivateKey)
						style |= Font.ITALIC;
					if (hasChanged)
						style |= Font.BOLD;

					if (style == 0)
						style = Font.PLAIN;

					setFont(new Font("Dialog", style, 12));

					if (newComment)
						value = "* "+o.toString();
				}
			}

			return super.getTreeCellRendererComponent(tree,
								  value,
								  selected,
								  expanded,
								  leaf,
								  row,
								  hasFocus);

		}
	}


	public boolean addToRoot(final IndexTreeNode node) {
		return addToIndexFolder(root, node);
	}

	public boolean addToIndexFolder(final IndexFolder target, final IndexTreeNode node) {
		if ((node instanceof Index) && alreadyExistingIndex(node.getPublicKey())) {
			Logger.notice(this, "Index already added");
			return false;
		}

		node.setParent(target.getId());
		target.getTreeNode().insert(node.getTreeNode(), target.getTreeNode().getChildCount());
		treeModel.reload(target);

		return true;
	}


	public boolean alreadyExistingIndex(final String key) {
		if ((key == null) || (key.length() <= 10))
			return false;

		String realKey = FreenetURIHelper.getComparablePart(key);

		try {
			synchronized(indexBrowser.getDb().dbLock) {
				final Connection c = indexBrowser.getDb().getConnection();
				PreparedStatement st;

				String query;

				query = "SELECT id FROM indexes WHERE LOWER(publicKey) LIKE ?";


				Logger.info(this, query + " : " + realKey+"%");
	
				st = c.prepareStatement(query);

				st.setString(1, realKey+"%");

				if (st.execute()) {
					final ResultSet results = st.getResultSet();

					if (results.next()) {
						st.close();
						return true;
					}
					st.close();
				}
				else
					st.close();
			}

		} catch(final java.sql.SQLException e) {
			Logger.warning(this, "Exception while trying to check if '"+key+"' is already know: '"+e.toString()+"'");
		}

		return false;
	}



	/**
	 * @param node can be null
	 */
	public void reloadModel(final MutableTreeNode node) {
		treeModel.reload(node);
	}


	public void reloadModel() {
		treeModel.reload();
	}


	/* TODO : Improve this ; quite ugly */


	public void addUpdatingIndex(Index index) {
		updatingIndexes.add(new Integer(index.getId()));
	}

	public void removeUpdatingIndex(Index index) {
		updatingIndexes.remove(new Integer(index.getId()));
	}

	public int numberOfUpdatingIndexes() {
		return updatingIndexes.size();
	}

	public boolean isIndexUpdating(Index index) {
		return (updatingIndexes.indexOf(new Integer(index.getId())) >= 0);
	}


	public void valueChanged(TreeSelectionEvent arg0) {
		checkSelection();		
	}
}

