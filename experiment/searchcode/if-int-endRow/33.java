public class Solution {
public boolean searchMatrix(int[][] matrix, int target) {
if(matrix.length == 0) {
public boolean searchMatrixHelper(int[][] matrix, int startrow, int endrow, int startcol, int endcol, int target) {
if(startcol > endcol || startrow > endrow) {

