public class Solution {
public int removeDuplicates(int[] A) {
int len = A.length;
if (len <= 1) {
return len;
}
int p = 0;
int q = 0;

