Your function should return length = 2, with the first two elements of nums being 1 and 2 respectively. It doesn&#39;t matter what you leave beyond the new length.
*/

public class Solution {
public int removeDuplicates(int[] nums) {
int newLen = nums.length;
int replaceIndex = 0;
for(int i = 0; i<nums.length-1;i++){

