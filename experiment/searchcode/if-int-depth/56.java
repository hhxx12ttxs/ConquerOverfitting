return true;
}
int left_depth = depth(root.left);
int right_depth = depth(root.right);
if(left_depth - right_depth > 1 || right_depth - left_depth > 1){
private int depth(TreeNode node){
if(node == null){
return 0;
}
int left_depth = depth(node.left);

