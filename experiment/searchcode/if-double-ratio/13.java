public class Problem69 {

public void solve() {
double maxRatio = 0.0;
int bestN = 0;

for (int n = 2; n <= 1000000; n++) {
double ratio = (double) n / (double) Numbers.phi(n);

