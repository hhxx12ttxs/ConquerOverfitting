import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeamCarver {

    private interface Pixel {
        int x();

        int y();

        int idx();
    }

    private class PixelImpl implements Pixel {

        private final int x;
        private final int y;
        private final int idx;

        public PixelImpl(final int idx) {
            this.idx = idx;
            this.x = idx % current.width();
            this.y = idx / current.width();
        }

        public PixelImpl(final int x, final int y) {
            this.x = x;
            this.y = y;
            this.idx = y * current.width() + x;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int idx() {
            return idx;
        }

        @Override
        public String toString() {
            return String.format("Pix(%d,%d)", x, y);
        }
    }

    private class TransposedPixelImpl implements Pixel {

        private final int x;
        private final int y;
        private final int idx;

        public TransposedPixelImpl(final int idx) {
            this.idx = idx;
            this.y = idx % current.width();
            this.x = idx / current.width();
        }

        public TransposedPixelImpl(final int x, final int y) {
            this.x = x;
            this.y = y;
            this.idx = x * current.width() + y;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int idx() {
            return idx;
        }

        @Override
        public String toString() {
            return String.format("TPix(%d,%d)", x, y);
        }
    }

    private Picture current;

    private double[] distTo;
    private int[] edgeTo;

    public SeamCarver(Picture picture) {
        current = new Picture(picture);
        distTo = new double[current.width() * current.height()];
        edgeTo = new int[current.width() * current.height()];
    }

    private Pixel newPixel(int x, int y, boolean transposed) {
        if (!transposed) {
            return new PixelImpl(x, y);
        } else {
            return new TransposedPixelImpl(x, y);
        }
    }

    private Pixel newPixel(int idx, boolean transposed) {
        if (!transposed) {
            return new PixelImpl(idx);
        } else {
            return new TransposedPixelImpl(idx);
        }
    }

    /**
     * Returns the current picture
     * 
     * @return
     */
    public Picture picture() {
        return new Picture(current);
    }

    /**
     * Returns the width of the current picture
     * 
     * @return
     */
    public int width() {
        return current.width();
    }

    /**
     * Returns the height of the current picture
     * 
     * @return
     */
    public int height() {
        return current.height();
    }

    /**
     * Returns the energy of pixel at column x and row y in the current picture
     * 
     * @param x
     * @param y
     * @return
     */

    public double energy(int x, int y) {
        if (x >= current.width() || x < 0 || y >= current.height() || y < 0)
            throw new IndexOutOfBoundsException(String.format(
                    "Pixel (%d, %d) out of bounds", x, y));
        if (x == 0 || y == 0 || x == current.width() - 1
                || y == current.height() - 1)
            return 195075;

        Color topPixel = current.get(x - 1, y);
        Color bottomPixel = current.get(x + 1, y);
        Color leftPixel = current.get(x, y - 1);
        Color rightPixel = current.get(x, y + 1);

        return dualGradientEnergy(topPixel, bottomPixel)
                + dualGradientEnergy(leftPixel, rightPixel);

    }

    private double dualGradientEnergy(Color a, Color b) {
        double bg = a.getBlue() - b.getBlue();
        double rg = a.getRed() - b.getRed();
        double gg = a.getGreen() - b.getGreen();
        return bg * bg + rg * rg + gg * gg;
    }

    private void relax(Pixel v, Pixel w, double[] energies) {
        double weight = energies[w.idx()];
        if (distTo[w.idx()] > distTo[v.idx()] + weight) {
            distTo[w.idx()] = distTo[v.idx()] + weight;
            edgeTo[w.idx()] = v.idx();
        }
    }

    private int[] findSeam(boolean useTransposedMatrix) {
        boolean t = useTransposedMatrix;
        int width = current.width();
        int height = current.height();
        if (t) {
            width = current.height();
            height = current.width();
        }

        // Calculate the energy matrix, transposition independent
        double[] energies = new double[current.width() * current.height()];
        for (int i = 0; i < current.width(); i++)
            for (int j = 0; j < current.height(); j++) {
                Pixel p = newPixel(i, j, false);
                energies[p.idx()] = energy(i, j);
            }

        // Initialize the graph
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);
        Arrays.fill(edgeTo, -1);
        for (int x = 0; x < width; ++x) {
            Pixel top = newPixel(x, 0, t);
            distTo[top.idx()] = 0;
        }

        // Relax pixels in topological order
        for (int j = 0; j < height - 1; j++)
            for (int i = 0; i < width; i++) {
                if (i != 0)
                    relax(newPixel(i, j, t), newPixel(i - 1, j + 1, t),
                            energies);
                relax(newPixel(i, j, t), newPixel(i, j + 1, t), energies);
                if (i != width - 1)
                    relax(newPixel(i, j, t), newPixel(i + 1, j + 1, t),
                            energies);
            }

        // Find the minimum energy
        Pixel minimumEnergyPath = newPixel(0, height - 1, t);
        for (int i = 1; i < width; ++i) {
            Pixel candidate = newPixel(i, height - 1, t);
            if (distTo[candidate.idx()] < distTo[minimumEnergyPath.idx()]) {
                minimumEnergyPath = candidate;
            }
        }

        // Calculate the path to that energy
        List<Integer> path = new ArrayList<Integer>();
        int currIdx = minimumEnergyPath.idx();
        while (currIdx != -1) {
            path.add(0, newPixel(currIdx, t).x());
            currIdx = edgeTo[currIdx];
        }

        // Return the path
        int[] p = new int[path.size()];
        for (int i = 0; i < path.size(); i++) {
            p[i] = path.get(i);
        }

        return p;
    }

    /**
     * Returns the sequence of indices for horizontal seam in current picture.
     * 
     * @return
     */

    public int[] findHorizontalSeam() {
        return findSeam(true);
    }

    /**
     * Returns the sequence of indices for vertical seam in current picture.
     * 
     * @return
     */

    public int[] findVerticalSeam() {
        return findSeam(false);
    }

    /**
     * Removes an horizontal seam of the current picture
     * 
     * @param a
     */
    public void removeHorizontalSeam(int[] a) {
        // Check there are seams to remove
        if (current.height() <= 1)
            throw new IllegalArgumentException("Current height <= 1");
        // Check the seam is of height length and inside bounds
        if (a.length != current.width())
            throw new IllegalArgumentException("Seam is not complete");
        for (int i = 0; i < a.length; i++) {
            if (a[i] < 0 || a[i] >= current.height()) {
                throw new IllegalArgumentException("Seam out of bounds");
            }
        }
        // Check the seam elements have distance of one
        for (int i = 1; i < a.length; i++) {
            if (Math.abs(a[i - 1] - a[i]) > 1)
                throw new IllegalArgumentException(
                        "Too much distance in the seam");
        }

        Picture newPicture = new Picture(current.width(), current.height() - 1);
        for (int i = 0; i < current.width(); i++) {
            int removedY = a[i];
            int posY = 0;
            for (int j = 0; j < current.height(); j++) {
                if (j != removedY) {
                    newPicture.set(i, posY, current.get(i, j));
                    posY++;
                }
            }
        }

        current = newPicture;
    }

    /**
     * Removes a vertical seam from the current picture
     * 
     * @param a
     */
    public void removeVerticalSeam(int[] a) {
        // Check there are seams to remove
        if (current.width() <= 1)
            throw new IllegalArgumentException("Current width <= 1");
        // Check the seam is of height length and inside bounds
        if (a.length != current.height())
            throw new IllegalArgumentException("Seam is not complete");
        for (int i = 0; i < a.length; i++) {
            if (a[i] < 0 || a[i] >= current.width()) {
                throw new IllegalArgumentException("Seam out of bounds");
            }
        }
        // Check the seam elements have distance of one
        for (int i = 1; i < a.length; i++) {
            if (Math.abs(a[i - 1] - a[i]) > 1)
                throw new IllegalArgumentException(
                        "Too much distance in the seam");
        }

        Picture newPicture = new Picture(current.width() - 1, current.height());
        for (int j = 0; j < current.height(); j++) {
            int removedX = a[j];
            int posX = 0;
            for (int i = 0; i < current.width(); i++) {
                if (i != removedX) {
                    newPicture.set(posX, j, current.get(i, j));
                    posX++;
                }
            }
        }

        current = newPicture;
    }
}

