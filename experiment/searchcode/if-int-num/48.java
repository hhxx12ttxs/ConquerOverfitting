public class Solution {
public int findMin(int[] num) {
if (num == null || num.length == 0) {
while (l+1 < r) {
int mid = (l+r) / 2;

if (num[l] < num[r]) {
return num[l];

