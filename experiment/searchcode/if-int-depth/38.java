public int maxDepth(TreeNode root) {
if(root == null) return 0;
return depth(root,0);
}

private int depth(TreeNode node, int depth){
if(node!=null){
int leftDepth = 1 + depth(node.left,0);

