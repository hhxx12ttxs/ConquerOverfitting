public class Solution {
public int removeDuplicates(int[] A) {
if (A.length < 3) {
return A.length;
}
int len = 2;
for (int i=2; i < A.length; ++i) {

