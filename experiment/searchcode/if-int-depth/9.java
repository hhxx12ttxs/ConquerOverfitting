// DO NOT write main() function
return depth(root, 0);
}

public int depth(TreeNode node, int currDepth) {
if (node == null) return currDepth;
int leftDepth = 0;
int rightDepth = 0;
if (node.left != null) {

