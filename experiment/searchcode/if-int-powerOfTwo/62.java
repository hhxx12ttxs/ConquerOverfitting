/*
Given an integer, write a function to determine if it is a power of two.

*/

public class PowerOfTwo
{
public boolean isPowerOfTwo(int n)
{
while(n % 2 == 0 &amp;&amp; n > 1)
{
n = n / 2;
}

return (n == 1);
}
}

