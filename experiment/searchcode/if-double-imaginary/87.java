import java.awt.geom.Rectangle2D;

/**
 * This class is a subclass of the FractalGenerator class, providing
 * implementations of its getInitialRange() and numIterations() methods in order
 * to produce a Burning Ship fractal when they are called by a FractalExplorer
 * instance
 */
public class BurningShip extends FractalGenerator {
    /** Maximum number of iterations of the fractal's generation function **/
    public static final int MAX_ITERATIONS = 2000;

    /**
     * Provides the name of the fractal this class generates
     */
    @Override
    public String toString() {
        return "Burning Ship";
    }

    /**
     * Sets a Rectangle2D.Double object's size and location so that it covers
     * the portion of the complex plane we want to be looking at by default
     * when we generate our fractal
     */
    public void getInitialRange(Rectangle2D.Double range) {
        range.x = -2;
        range.y = -2.5;
        range.width = 4;
        range.height = 4;
    }

    /**
     * Gets the number of iterations of the fractal's generation function
     * needed at a certain complex value x + iy before |z| > 2 and the point
     * is no longer in the fractal's set
     *
     * Uses the generation function z(n) = (|Re(z(n-1))| + |Im(z(n-1))|)^2 + c,
     * where c = x + iy and n is an integer number of iterations
     */
    public int numIterations(double x, double y) {
        double re = 0, im = 0;
        double nextRe, nextIm, absRe, absIm;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // Check if |z|^2 > (2)^2 for computation efficiency
            if ((re * re) + (im * im) > 4) {
                return i;
            }

            // Get the absolute values of our real and imaginary components
            absRe = Math.abs(re);
            absIm = Math.abs(im);

            // Get the next values of our real and imaginary components
            nextRe = x + (absRe * absRe) - (absIm * absIm);
            nextIm = y + (2 * absRe * absIm);

            // Update our real and imaginary components since they are no longer
            // needed for computation
            re = nextRe;
            im = nextIm;
        }
        // If the point never gets iterated out of the Mandelbrot set, return -1
        return -1;
    }
}

