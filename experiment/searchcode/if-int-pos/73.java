package leetcode;

public class SearchInsertPosition {
public int searchInsert(int[] A, int target) {
int pos = 0;
while (pos < A.length) {
if (A[pos] >= target) {

