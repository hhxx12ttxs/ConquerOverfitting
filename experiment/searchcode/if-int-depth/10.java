public class maxDepth {
public static int maxDepth(TreeNode root) {
if (root == null) return 0;
if (root.left == null &amp;&amp; root.right == null) return 1;
int leftDepth = 0;
int rightDepth = 0;

