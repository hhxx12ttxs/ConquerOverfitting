public long power0(long target, long power) {
long result = 1;
for (int i = 1; i <= power; i++) {
result *= power;
}

return result;
}

public long power1(long target, long power) {
long result = 1;
if (power == 0) {

