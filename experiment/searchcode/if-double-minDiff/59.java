public int closestValue(TreeNode root, double target) {
double minDiff = Math.abs(target-root.val);
double curDiff = Math.abs(target-p.val);
if (curDiff < minDiff) {
closestValue = p.val;
minDiff = curDiff;

