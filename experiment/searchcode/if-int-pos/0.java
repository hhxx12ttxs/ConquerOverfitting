public class Solution {
public void merge(int A[], int m, int B[], int n) {
int aPos = m-1;
int bPos = n-1;
int pos = m+n-1;
while ((pos >= 0) &amp;&amp; (aPos >= 0) &amp;&amp; (bPos >= 0)) {

