public class Solution {
public boolean isHappy(int n) {
if (n == 1) return true;
Set<Integer> seen = new HashSet<>();
int k = n;
while (k != 1 &amp;&amp; !seen.contains(k)) {

