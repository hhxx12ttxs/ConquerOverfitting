public class TheEquation {
public int leastSum(int X, int Y, int P) {
int minSum = P * 2;
for (int a = 1; a < minSum; a++) {
for (int b = 1; a + b < minSum; b++) {
if ((a * X + b * Y) % P == 0) {
minSum = a + b;
}
}
}
return minSum;
}
}

