package dmcoder.meetcoder;

public class PowerOfTwo {
public boolean isPowerOf2(int n) {
// Need to take care of zero separately
if (n <= 0) {
return false;
}

return (n &amp; (n - 1)) == 0;
}
}

