public class Solution {
public int minPathSum(int[][] grid) {
int[] row = new int[grid[0].length];
for(int j = 0; j < grid[0].length; j++){
if(j > 0){
row[j] = i > 0 ? Math.min(row[j - 1], row[j]) : row[j - 1];

