public class FizzBuzzTurbo {

public long[] counts(long A, long B) {
long[] ret = new long[3];
ret[1] = cal(A, B, 5) - ret[2];
return ret;
}

private long cal(long A, long B, long C) {
long a1 = A;
long a2 = B;
while (a1 % C != 0)

