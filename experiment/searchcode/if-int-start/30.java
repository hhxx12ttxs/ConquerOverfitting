public class Solution {
public void sortColors(int[] A) {
// Start typing your Java solution below
sort(A, 0, A.length - 1);
}

private void sort(int[] A, int start, int end) {
if(start >= end) return;

