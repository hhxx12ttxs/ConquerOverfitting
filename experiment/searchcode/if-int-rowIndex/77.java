public class Solution {
public ArrayList<Integer> getRow(int rowIndex) {
// Start typing your Java solution below
rowIndex++;
ArrayList<Integer> res = new ArrayList<Integer>();
if(rowIndex==0) return res;
res.add(1);

