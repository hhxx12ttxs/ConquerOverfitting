public class P134 {
static long f(int p1, int p2) {
long m = 10;
while (m < p1) {
m *= 10;
}
long v = EEA.inv(m, p2).longValue();
long q = (v * (p2 - p1)) % p2;
return m * q + p1;
}

public static void main(String[] args) {

