public class Solution {
public int[][] generateMatrix(int n) {
if(n < 0) return null;
int[][] rst = new int[n][n];
int counter = 1;
int start = 0;
int end = n - 1;

