public class Solution {
public int findPeakElement(int[] num) {
if (num == null){
while(l < r - 1){
int mid = l + (r - l)/2;
if(num[mid] > num[mid - 1]&amp;&amp;num[mid] > num[mid + 1]){

