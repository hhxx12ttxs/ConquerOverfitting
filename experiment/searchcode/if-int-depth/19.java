@Override
public int maxDepth(TreeNode<E> root) {
return maxDepth(root, 0);
}

private int maxDepth(TreeNode<E> root, int depth) {
if (root == null) {
return depth;

