public class MovieSeating {
public long perms(int n, int k) {
if (n < k) {
return 0;
}
long ret = 1;
for (int i = n; i > n - k; --i) {
ret *= i;

