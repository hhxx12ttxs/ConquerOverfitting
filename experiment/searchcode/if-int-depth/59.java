package leetCode;

public class MaximumDepthofBinaryTree {
public int maxDepth(TreeNode root) {
private int getMax(TreeNode node, int depth, int maxDepth) {
if (node.left == null &amp;&amp; node.right == null) {
if (depth > maxDepth) {

