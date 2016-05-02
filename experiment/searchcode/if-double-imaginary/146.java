import java.awt.geom.Rectangle2D;

/**
 * This class is a subclass of the FractalGenerator class, providing
 * implementations of its getInitialRange() and numIterations() methods in order
 * to produce a Mandelbrot fractal when they are called by a FractalExplorer
 * instance
 */
public class Mandelbrot extends FractalGenerator {
    /** Maximum number of iterations of the fractal's generation function **/
    public static final int MAX_ITERATIONS = 2000;

    /**
     * Sets a Rectangle2D.Double object's size and location so that it covers
     * the portion of the complex plane we want to be looking at by default
     * when we generate our fractal
     */
    public void getInitialRange(Rectangle2D.Double range) {
        range.x = -2;
        range.y = -1.5;
        range.width = 3;
        range.height = 3;
    }

    /**
     * Gets the number of iterations of the fractal's generation function
     * needed at a certain complex value x + iy before |z| > 2 and the point
     * is no longer in the Mandelbrot set
     */
    public int numIterations(double x, double y) {
        double re = 0, im = 0;
        double nextRe, nextIm;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // Check if |z|^2 > (2)^2 for computation efficiency
            if ((re * re) + (im * im) > 4) {
                return i;
            }

            // Get the next values of our real and imaginary components
            nextRe = (re * re) - (im * im) + x;
            nextIm = (2 * re * im) + y;

            // Update our real and imaginary components since they are no longer
            // needed for computation
            re = nextRe;
            im = nextIm;
        }
        // If the point never gets iterated out of the Mandelbrot set, return -1
        return -1;
    }
}

