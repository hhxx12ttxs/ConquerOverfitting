public class Solution {
public List<Integer> getRow(int rowIndex) {
rowIndex++;
if (rowIndex == 0) {
return Collections.emptyList();
} else if (rowIndex == 1) {
List result = new ArrayList<>();

