package th.facebook;

public class NQueens {

public void nQueens(int n) {
int[] columnIndex = new int[n];
private void nQueens(int[] columnIndex, int start, int end) {
if(start > end) {
printQueenLocation(columnIndex);
} else {

