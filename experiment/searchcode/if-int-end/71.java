public class Solution {
public int search(int[] A, int target) {

if (A == null || A.length == 0) {
int end = A.length - 1;
while (start + 1 < end) {
int middle = (start + end) >> 1;

