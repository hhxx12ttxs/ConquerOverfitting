public class FoxAndGCDLCM {
public long get(long G, long L) {
if (L % G != 0) {
return -1;
for (long i = 1; i * i <= product; i++) {
if (product % i == 0) {
long j = product / i;
if (gcd(i, j) == 1) {
result = Math.min(result, G * (i + j));

