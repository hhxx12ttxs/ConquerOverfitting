public class Solution {
public int removeDuplicates(int[] A) {
int len = A.length;
if( len < 1 ){
return len;
}
if( len < 2 ){
return 1;
}
int index = 1;
int processing = A[0];

