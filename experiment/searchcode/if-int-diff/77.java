int sum;
int minDiff = Integer.MAX_VALUE, diff;
List<List<Integer>> res = new ArrayList();

if (num.length < 3) {
for (int i = 0; i < num.length - 2; i ++) {
if (i > 0 &amp;&amp; num[i] == num[i-1]) {
continue;
}
int j = i + 1;

