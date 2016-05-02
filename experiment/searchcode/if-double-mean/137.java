/**
 * 
 * @author susmit shukla
 * 
 */
public class PercolationStats {

    /*
     * mean site open ratio for sample size n
     */
    private double mean;

    /*
     * standard deviation
     */
    private double stddev;

    /*
     * confidenceLo
     */
    private double confidenceLo;

    /*
     * confidenceHi
     */
    private double confidenceHi;

    /**
     * perform T independent computational experiments on an N-by-N grid
     * 
     * @param N
     * @param T
     */
    public PercolationStats(int N, int T) {
        if (N <= 0 || T <= 0) {
            throw new IllegalArgumentException(
                    "input out of range, please specify positive input");
        }
        double[] openRatio = new double[T];
        for (int i = 0; i < T; i++) {
            Percolation percolation = new Percolation(N);
            int openCtr = 0;
            while (!percolation.percolates() && openCtr <= N * N) {
                int randomI = StdRandom.uniform(1, N + 1);
                int randomJ = StdRandom.uniform(1, N + 1);
                if (!percolation.isOpen(randomI, randomJ)) {
                    percolation.open(randomI, randomJ);
                    openCtr++;
                }
            }
            openRatio[i] = ((openCtr * 1.0) / (N * N));
        }
        mean = StdStats.mean(openRatio);
        stddev = StdStats.stddev(openRatio);
        confidenceLo = mean() - ((1.96 * stddev()) / Math.sqrt(T));
        confidenceHi = mean() + ((1.96 * stddev()) / Math.sqrt(T));
    }

    /**
     * sample mean of percolation threshold
     * 
     * @return
     */
    public double mean() {
        return mean;
    }

    // sample standard deviation of percolation threshold
    public double stddev() {
        return stddev;
    }

    // returns lower bound of the 95% confidence interval
    public double confidenceLo() {
        return confidenceLo;
    }

    // returns upper bound of the 95% confidence interval
    public double confidenceHi() {
        return confidenceHi;
    }

    // test client, described below
    public static void main(String[] args) {
        // int[] allInputs = StdIn.readAllInts();
        int n = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);
        PercolationStats percolationStats = new PercolationStats(n, t);
        System.out.println(String.format("mean = %41.20f",
                percolationStats.mean()));
        System.out.println(String.format("stddev = %39.20f",
                percolationStats.stddev()));
        System.out.println(String.format(
                "95%% confidence interval = %1.20f, %1.20f",
                percolationStats.confidenceLo(),
                percolationStats.confidenceHi()));
    }

}

