public class Solution {
public int removeDuplicates(int[] A) {
int len = A.length;
if (len == 0) {
return 0;
}
int result = 0;
int p = 0;
int q = p + 1;
while (p < len &amp;&amp; q < len) {

