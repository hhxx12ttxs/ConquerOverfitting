for (int tmpResult, mid = (root + tmpInt) / 2; tmpInt +1 < root; mid = (root + tmpInt) / 2) {
tmpResult = x / mid;
if (tmpResult == mid) {
return mid;
} else if (tmpResult > mid) {
tmpInt = mid;
} else {

