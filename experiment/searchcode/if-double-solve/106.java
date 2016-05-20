package tsp;

import java.util.List;

public class BitDPSolver {
    private static final int NOT_INITIALIZED = -1;
    private List<Point> points;
    private final int N;

    public BitDPSolver(List<Point> points) {
        this.points = points;
        this.N = points.size();

        if (points.size() > 30)
            throw new RuntimeException();
    }

    public double solve() {
        // dp[x][bits] を、0 から x への経路で、bits を全て通る最短路とする。(x は bits に含まない)
        double[][] dp = new double[N][1 << N];
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < dp[i].length; ++j)
                dp[i][j] = NOT_INITIALIZED;
        }

        for (int i = 0; i < N; ++i)
            dp[i][0] = d(0, i);

        return calc(dp, 0, ((1 << N) - 1) ^ 1);
    }

    private double calc(double[][] dp, int x, int bits) {
        if (dp[x][bits] != NOT_INITIALIZED)
            return dp[x][bits];

        double minValue = Double.POSITIVE_INFINITY;
        for (int y = 0; y < N; ++y) {
            if ((bits & (1 << y)) == 0)
                continue;

            double v = calc(dp, y, bits ^ (1 << y)) + d(x, y);
            if (v < minValue)
                minValue = v;
        }

        return dp[x][bits] = minValue;
    }

    private double d(int s, int t) {
        Point p1 = points.get(s);
        Point p2 = points.get(t);
        return p1.distance(p2);
    }
}

