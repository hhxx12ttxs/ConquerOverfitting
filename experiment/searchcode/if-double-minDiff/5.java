public int closestValue(TreeNode root, double target) {
if (root == null) {
return 0;
int val = root.val;
double diff = Math.abs(target - val);
if (diff < minDiff) {

