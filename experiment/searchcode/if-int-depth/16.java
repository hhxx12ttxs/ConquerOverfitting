return depth(root)!=-1;
}

private int depth(TreeNode root) {
if (root == null) return 0;
int leftDepth = depth(root.left);
if (leftDepth == -1) return -1;
int rightDepth = depth(root.right);

