public class Solution {
public int removeElement(int[] a, int elem) {
int end = a.length - 1;
int idx = 0;
while (idx <= end) {
if (a[idx] == elem) {

