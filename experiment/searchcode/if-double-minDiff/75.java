public int closestValue(TreeNode root, double target) {
ArrayList<Double> res = new ArrayList<Double>();
doTraverse(root, res);
double minDiff = Double.POSITIVE_INFINITY;
double result = 0;
for(int i = 0; i < res.size(); i++) {
if(minDiff > Math.abs(target - res.get(i))) {

