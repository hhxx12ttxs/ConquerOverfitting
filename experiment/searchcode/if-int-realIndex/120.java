/*
 * Created on Apr 22, 2008
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright @2008 the original author or authors.
 */
package org.fest.swing.demo.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.fest.swing.demo.model.WebFeed;
import org.fest.swing.demo.view.WebFeedTree.FolderNode;
import org.fest.swing.demo.view.WebFeedTree.WebFeedNode;

/**
 * Understands a <code>{@link WebFeedDnDHandler}</code> for performing DND of <code>{@link WebFeed}</code>s in a
 * <code>{@link WebFeedTree}</code>.
 *
 * @author Alex Ruiz
 */
class WebFeedTreeDnDHandler extends WebFeedDnDHandler<WebFeedTree> {
  
  private static final long serialVersionUID = 1L;

  private static final int[] DEFAULT_ROWS = null;
  private static final int DEFAULT_ADD_INDEX = -1;
  private static final int DEFAULT_ADD_COUNT = 0;
  
  private int[] rows;
  private int addIndex; // Location where items were added
  private int addCount; // Number of items added.

  WebFeedTreeDnDHandler() {
    super(WebFeedTree.class);
    reset();
  }
  
  /** {@inheritDoc} */
  WebFeed[] exportWebFeeds(WebFeedTree tree) {
    rows = tree.getSelectionRows();
    List<WebFeed> webFeeds = new ArrayList<WebFeed>();
    for (int i = 0; i < rows.length; i++) {
      TreePath path = tree.getPathForRow(rows[i]);
      Object value = path.getLastPathComponent();
      if (!(value instanceof WebFeedNode)) return new WebFeed[0];
      WebFeedNode node = (WebFeedNode)value;
      webFeeds.add(node.webFeed);
    }
    return webFeeds.toArray(new WebFeed[webFeeds.size()]);
  }
  
  /** {@inheritDoc} */
  void importWebFeeds(WebFeedTree tree, WebFeed[] webFeeds) {
    int index = tree.getRowForPath(tree.getSelectionPath());
    // Prevent the user from dropping data back on itself.
    if (rows != null && index >= rows[0] - 1 && index <= rows[rows.length - 1]) {
      rows = null;
      return;
    }
    int max = tree.getRowCount();
    if (index < 0) index = max;
    else if (index > max) index = max;
    addIndex = index;
    addCount = webFeeds.length;
    for (WebFeed webFeed : webFeeds)
      index = addWebFeed(tree, index, webFeed);
  }

  private int addWebFeed(WebFeedTree tree, int index, WebFeed webFeed) {
    int realIndex = index;
    TreePath path = tree.getPathForRow(realIndex++);
    Object value = path.getLastPathComponent();
    if (value instanceof FolderNode) {
      FolderNode parent = (FolderNode) value;
      webFeed.folder(parent.folder);
      tree.addWebFeed(webFeed);
    }
    return realIndex;
  }

  /** {@inheritDoc} */
  void cleanup(WebFeedTree source, WebFeedSelection selection, boolean remove) {
    if (remove && rows != null) {
      DefaultTreeModel model = (DefaultTreeModel) source.getModel();
      // If we are moving items around in the same table, we need to adjust the rows accordingly, since those after the 
      // insertion point have moved.
      if (addCount > 0) {
        for (int i = 0; i < rows.length; i++) 
          if (rows[i] > addIndex) rows[i] += addCount;
      }
      for (int i = rows.length - 1; i >= 0; i--) {
        TreePath path = source.getPathForRow(rows[i]);
        if (path == null) continue;
        model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
      }
    }
    new UpdateWebFeedFolderWorker(selection.webFeeds()).execute();
    reset();
  }

  private void reset() {
    rows = DEFAULT_ROWS;
    addIndex = DEFAULT_ADD_INDEX;
    addCount = DEFAULT_ADD_COUNT;
  }
}

