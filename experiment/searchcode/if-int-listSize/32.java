public class Solution {
public int minimumTotal(List<List<Integer>> triangle) {
if(triangle == null || triangle.size()==0) return 0;
int listsize = triangle.size();
int[][] min = new int[listsize][];

