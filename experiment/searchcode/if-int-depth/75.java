public boolean isBalanced(TreeNode root) {
isBalanced = true;
depth(root);
return isBalanced;
}

private int depth(TreeNode root) {
if (root == null) return 0;

int depthLeft = depth(root.left);
int depthRight = depth(root.right);

