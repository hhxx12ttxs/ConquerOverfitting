public class Solution {
public int maxSubArray(int[] A) {
int max_sofar = 0;
int max_end_here = 0;
int max_value = Integer.MIN_VALUE;

for (int i = 0; i< A.length; i++) {

