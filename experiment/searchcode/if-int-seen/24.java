public class CartInSupermarketEasy {
private boolean[][] seen;
private int[][] results;
public int calc(int N, int K) {
seen = new boolean[N + 1][K + 1];
results = new int[N + 1][K + 1];

