public class Solution {
public int sqrt(int x) {
if (x <= 1)
return x;
int start = 1, end = x / 2;
while (start <= end) {
int mid = start + (end - start) / 2;

