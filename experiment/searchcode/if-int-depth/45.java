package Fight;

public class MinimumDepthofBinaryTree_111 {

public int minDepth(TreeNode root) {
int leftDepth = minDepth(root.left);
int rightDepth = minDepth(root.right);

if (leftDepth == 0) {
return rightDepth + 1;

