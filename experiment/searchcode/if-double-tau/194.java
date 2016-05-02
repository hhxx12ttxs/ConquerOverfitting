//
// Berlin Brown
//
// $Id: IGG.java,v 1.2 2004/06/17 10:49:02 bigbinc Exp $
//
//
// author      Simon Levy
//



package org.retro.neural;

import java.awt.Point;
import java.util.Random;



public class IGG extends Map2D {

    // constant width parameter
    private double _sigma;


    public IGG(double [][] X, double [] P, Random rand) 

        throws IllegalArgumentException {

	super(X, 2, 2, P, rand);
    }

    /** 
     * Builds IGG with random initial weights.
     * @param X data to learn
     * @param P probability density function over X
     * @throws IllegalArgumentException if x and P have different lengths
     * @throws IllegalArgumentException if elements of P do not sum to 1
     */
    public IGG(double [][] X, double [] P) throws IllegalArgumentException {
        this(X, P, new Random());
    }


    /** 
     * Builds IGG with random initial weights.
     * @param X data to learn
     * @param P probability density function over X
     * @param seed seed for random-number generator
     * @throws IllegalArgumentException if X and P have different lengths
     * @throws IllegalArgumentException if elements of P do not sum to 1
     */
    public IGG(double [][] X, double [] P , long seed)  
        throws IllegalArgumentException {
        this(X, P, new Random(seed));
    }


    /** 
     * Builds IGG with random initial weights and uniform probability
     * density function.
     * @param X data to learn
     */
    public IGG(double [][] X) {
        this(X, new Random());
    }


    /** 
     * Builds IGG with random initial weights and uniform probability
     * density function.
     * @param X data to learn
     * @param seed seed for random-number generator
     */
    public IGG(double [][] X, long seed) {
        this(X, new Random(seed));
    }

    /** 
     * Builds IGG with random initial weights and uniform probability
     * density function.
     * @param X data to learn
     * @param rand random-number generator
     */
    public IGG(double [][] X, Random rand) {
        this(X, updf(X), rand);
    }


    /** 
     * Runs the growth phase.  Learning rate e(t) = e0.
     * @param Nmin desired network size
     * @param e0 learning rate
     * @param sigma width parameter
     * @param lambda_g saturation parameter
     */
    public void grow(int Nmin, double e0, double sigma, int lambda_g) {

	// super.learn() will get value through this.sigma()
	_sigma = sigma;

        int [][] tau = new int [_N1][_N2]; // variable counter

        // set up a task that will fire every REPORT_MSEC milliseconds
        ReportTimerTask task = new ReportTimerTask("Growth");
        task.launch();

	// repeat until the desired network size is achieved
	for (int t=0; _N1*_N2 < Nmin; ++t) {

          // 2. Generate at random an input signal X according to P(X)
          SNVector x = randomSignal();

          // 3. Determine the winner s(X) = s
          Point s = winner(x);

          // 4. Increase a local counter variable of the winner
          tau[s.x][s.y]++;

          // 6. Adapt each unit r according to dwr = e(t)*hrs*(X-wr), 
          //    where e(t) = e0
          adapt(s, x, e0, t, 0); // pass 0 for tmax, since ignored by sigma()

          // 7. If the number of input signals generated for the current
          //    network size reaches a multiple lambda_g of this network
          //    size; i.e., if lambda_g * _N1 * _N2 = t
          if (lambda_g*_N1*_N2 == t) {
        
              // Determine the unit q with the largest value of tau
              Point q = tau_max(tau);

              // Determine the direct neighbor f of q with the most distant
              // reference vector
              Point f = farthest_neighbor(q);

              // Case 1: q and f are in the same row of the grid
              if (same_row(q, f)) {

                  // Insert a new column with _N1 units between the
                  // columns of q and f.  Interpolate the reference
                  // vectors of the new units from the reference
                  // vectors of their respective direct neighbors in
                  // the same row.
                  _A = insert_column(_A, q, f, _N1, _N2);

                  // Adjust the variable for the number of columns
                  _N2++;
              }


              // Case 2: q and f are in the same column of the grid
              if (same_column(q, f)) {

                  // Insert a new row with _N2 units between the
                  // rows of q and f.  Interpolate the reference
                  // vectors of the new units from the reference
                  // vectors of their respective direct neighbors in
                  // the same column.
                  _A = insert_row(_A, q, f, _N1, _N2);

                  // Adjust the variable for the number of columns
                  _N1++;
              }

              // Reset all local counter values
              tau = new int [_N1][_N2];

              // Reset the time parameter
              t = 0;
          }

          // used by timer task
          _percent_done = (int)Math.ceil(100*_N1*_N2/Nmin);
        }

	task.complete(); // else we'd run forever
    }

    /** 
     * Runs the fine-tuning phase. Learning rate e(t) = e0(e1/e0)^{t/tmax}.
     * @param e0 learning rate
     * @param e1 learning rate
     * @param sigma width parameter
     * @param lambda_f saturation parameter
     */
    public void tune(double e0, double e1, double sigma, int lambda_f) 

        throws IllegalArgumentException {

        int tmax = _N1 * _N2 * lambda_f;

	// super.learn() will get value through this.sigma()
	_sigma = sigma;

	learn(e0, e1, tmax, "Fine-tuning");

        System.err.println("  Fine-tuning phase complete");
    }

    // Initialize the connection set C to form a rectangular _N1x_N2
    // grid.  We do this by approximating a square in the two
    // dimensions of greatest variance. 
    protected void initA() {

        int m = _PX.length, n = _PX[0].length();

        // get variance from PDF
        SNMatrix a = new SNMatrix(m, n);
        for (int i=0; i<m; ++i) {
            a.setRow(i, randomSignal());
        }
        SNVector mean = a.meanRows();
        SNMatrix diff = SNMatrix.subtract(a, mean.repmat(m));
        SNVector vrnc = SNMatrix.arrayMultiply(diff, diff).sumRows();

        // get two dimensions of greatest variance
        SNVector svrnc = vrnc.sort();
        int xdim = vrnc.find(svrnc.getValue(svrnc.length()-1))[0];
        int ydim = vrnc.find(svrnc.getValue(svrnc.length()-2))[0];

	System.err.println(xdim + " " + ydim);

        // find points closest to corners of square in those dimensions
        _A[0][0] = closest(a, 0, 0, xdim, ydim);
        _A[1][0] = closest(a, 1, 0, xdim, ydim);
        _A[1][1] = closest(a, 1, 1, xdim, ydim);
        _A[0][1] = closest(a, 0, 1, xdim, ydim);
    }

    // returns width parameter: constant, so ignores current and max time
    protected double sigma(int t, int tmax) {
	return _sigma;
    }

    // Determine the direct neighbor f of q with the most distant
    // reference vector
    private Point farthest_neighbor(Point q) {

        Point f = q;

        if (q.x > 0) {
            f = check_farther(f, q, -1, 0); // upper neighbor
        }
        if (q.x < _N1-1) {
            f = check_farther(f, q, +1, 0); // lower neighbor
        }
        if (q.y > 0) {
            f = check_farther(f, q, 0, -1); // left neighbor
        }
        if (q.y < _N2-1) {
            f = check_farther(f, q, 0, +1); // right neighbor
        }

        return f;
    }

    // update farthest-neighbor variable based on distance
    private Point check_farther(Point f, Point q, int di, int dj) {
        Point c = new Point(q.x+di, q.y+dj);
        SNVector wq = vector(q);
        SNVector wc = vector(c);
        SNVector wf = vector(f);
        if (wq.nycDist(wc) > wq.nycDist(wf)) {
            f = c;
        }
        return f;
    }

    // Determine the unit q with the largest value of tau
    private Point tau_max(int [][] tau) {
        
        int imax = 0, jmax = 0;

        for (int i=0; i<_N1; ++i) {
            for (int j=0; j<_N2; ++j) {
                if (tau[i][j] > tau[imax][jmax]) {
                    imax = i;
                    jmax = j;
                }
            }
        }

        return new Point(imax, jmax);
    }

    // bozo filter for grow(), tune() args
    private void bozo_filter(double [][] X, double [] P) 
        throws IllegalArgumentException {
    }

    // find point closest to specified 2D coordinate in specified dimensions
    private static SNVector closest(SNMatrix a, double x, double y, 
                                    int xdim, int ydim) {

        double mindst = Double.POSITIVE_INFINITY;
        int j = -1;
        for (int i=0; i<a.getRows(); ++i) {
            SNVector v = a.getRow(i);
            double xdiff = v.getValue(xdim) - x;
            double ydiff = v.getValue(ydim) - y;
            double sqrdst = xdiff*xdiff + ydiff*ydiff;
            if (sqrdst < mindst) {
                mindst = sqrdst;
                j = i;
            }
        }
        return a.getRow(j);
    }

    // return true if two units are in same grid row, false otherwise
    private static boolean same_row(Point a, Point b) {
        return (a.x == b.x) & ((a.y == b.y+1) | (a.y == b.y-1));
    }

    // return true if two units are in same grid column, false otherwise
    private static boolean same_column(Point a, Point b) {
        return (a.y == b.y) & ((a.x == b.x+1) | (a.x == b.x-1));
    }

    // Insert a new column with _N1 units between the columns of q and
    // f.  Interpolate the reference vectors of the new units from the
    // reference vectors of their respective direct neighbors in the
    // same row.
    private static SNVector [][] insert_column(SNVector [][] A, 
                                               Point q, Point f, 
                                               int _N1, int _N2) {

        SNVector [][] newA = new SNVector [_N1] [_N2+1];

        Point a = q.y < f.y ? q : f;
        Point b = q.y > f.y ? q : f;

        for (int i=0; i<_N1; ++i) {

            // copy previous columns
            for (int j=0; j<=a.y; ++j) {
                newA[i][j] = A[i][j];
            }

            // interpolate between previous and next columns
            newA[i][b.y] = interp(A[i][a.y], A[i][b.y]);

            // copy next columns
            for (int j=b.y; j<_N2; ++j) {
                newA[i][j+1] = A[i][j];
            }
        }

        return newA;

    }


    // Insert a new row with _N2 units between the rows of q and f.
    // Interpolate the reference vectors of the new units from the
    // reference vectors of their respective direct neighbors in the
    // same column.
    private static SNVector [][] insert_row(SNVector [][] A, 
                                            Point q, Point f,
                                            int _N1, int _N2) {

        SNVector [][] newA = new SNVector [_N1+1] [_N2];

        Point a = q.x < f.x ? q : f;
        Point b = q.x > f.x ? q : f;

        for (int j=0; j<_N2; ++j) {

            // copy previous rows
            for (int i=0; i<=a.x; ++i) {
                newA[i][j] = A[i][j];
            }

            // interpolate between previous and next rows
            newA[b.x][j] = SNVector.add(A[a.x][j], A[b.x][j]);
            newA[b.x][j].divide(2);

            // copy next rows
            for (int i=b.x; i<_N1; ++i) {
                newA[i+1][j] = A[i][j];
            }
        }

        return newA;

    }

    // return interpolation between two vectors
    private static SNVector interp(SNVector x, SNVector y) {
	SNVector v = SNVector.add(x, y);
	v.divide(2);
	return v;
    }

} // end of the class ................................


