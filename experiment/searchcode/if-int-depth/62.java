package tree;

public class MinBinaryTreeDepth {

public int minDepth(TreeNode root) {
if(root == null) return 0;
public int depth(TreeNode node, int depth){
if(node == null) return Integer.MAX_VALUE;
if(node.left == null &amp;&amp; node.right == null) return depth;

