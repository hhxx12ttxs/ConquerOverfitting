public class Solution {
public List<Integer> getRow(int rowIndex) {
if(rowIndex<0) return null;

int[] resultArray = new int[rowIndex+1];
resultArray[0] = 1;

