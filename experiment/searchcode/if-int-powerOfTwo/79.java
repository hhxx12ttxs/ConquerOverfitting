// Given an integer, write a function to determine if it is a power of two.

public class PowerOfTwo {
public boolean isPowerOfTwo(int n) {
return n > 0 &amp;&amp; (n &amp; (n-1)) == 0;
}
}

