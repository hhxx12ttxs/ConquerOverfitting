public int maxDepth(TreeNode root) {
return maxDepth(root, 0);
}

public int maxDepth(TreeNode root, int curDepth){
if(root == null)
return curDepth;
int depthLeft = maxDepth(root.left, curDepth+1);

