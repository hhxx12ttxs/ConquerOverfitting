public class Solution {
public int removeElement(int[] A, int elem) {
int start = 0, end = A.length-1;
while(start <= end) {
if(A[start] == elem) {
A[start] = A[end];

