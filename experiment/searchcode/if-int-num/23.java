public class Solution {
public boolean isUgly(int num) {
if(num == 1)
return true;
if( num > 1 &amp;&amp; num % 2 == 0)
{
num = num /2;
}
if( num > 1 &amp;&amp; num % 3 == 0)

