public static void main(String[] args) {
long a = 11934;
long b = 8007;
long[] ret = eea(a, b);
System.out.println(&quot;GCD of &quot; + a + &quot; and &quot; + b + &quot; is: &quot;+ret[0]);
public static long[] eea(long a, long b) {
if (b==0) return new long[] {a, 1, 0};
long ret[] = eea(b, a % b);
long y = ret[2];
ret[2] = ret[1] - a/b * y;

