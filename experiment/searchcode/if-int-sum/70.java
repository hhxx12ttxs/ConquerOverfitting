public class ShorterSuperSum {
public int calculate(int k, int n) {
if (k == 0) {
return n;
}
int sum = 0;
for (int i = 1; i <= n; i++) {
sum += calculate(k - 1, i);
}
return sum;
}
}

