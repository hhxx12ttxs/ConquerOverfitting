package interviewbit.binarysearch.sqrt;

public class Solution {
public int sqrt(int A) {
if (A == 0) {
while (start <= end) {
int mid = (start + end) / 2;
if (mid <= A / mid) {
start = mid + 1;
ans = mid;

