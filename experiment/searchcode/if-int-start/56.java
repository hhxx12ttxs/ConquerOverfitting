public class Solution {
public int findMin(int[] num) {

if (num == null || num.length == 0) {
return 0;
}

int start = 0;
int end = num.length - 1;

