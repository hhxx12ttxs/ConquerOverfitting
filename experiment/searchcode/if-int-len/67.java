public class Solution {
public int removeDuplicates(int[] A) {
int len = A.length;
if(len < 2) return len;
int i = 0;
int j = 1;
while(j < len){

