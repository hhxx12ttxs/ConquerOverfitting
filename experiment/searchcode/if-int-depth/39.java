public int maxDepth(TreeNode root) {
if (root == null) {
return 0;
} else {
int depthLeft = 0;
int depthRight = 0;
if (root.left != null) {

