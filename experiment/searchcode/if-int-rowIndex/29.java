public class PascalsTriangleII {
public ArrayList<Integer> getRow(int rowIndex) {
ArrayList<Integer> res = new ArrayList<Integer>();
rowIndex = rowIndex + 1;
if(rowIndex <= 0) return res;

