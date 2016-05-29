public class Solution {
public int removeDuplicates(int[] A) {
if(A == null || A.length < 2) return A.length;

int a = 1, count = 1;
for(int i = 1; i < A.length; i++){

