public void merge(int[] nums1, int m, int[] nums2, int n) {
int p1 = m - 1;
int p2 = n - 1;
if (m == 0) {
for (int i = 0; i < n; i++) {
for (int i = m + n - 1; i >= 0 &amp;&amp; p1 >= 0 &amp;&amp; p2 >= 0; i--) {
if (nums1[p1] < nums2[p2]) {
nums1[i] = nums2[p2];
p2--;
} else {

