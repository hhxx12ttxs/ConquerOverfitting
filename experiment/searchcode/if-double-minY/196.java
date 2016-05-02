import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.StringTokenizer;

public class Scribble implements Shape, Transferable, Serializable, Cloneable {
    protected double[] points = new double[64];
    protected int numPoints = 0;
    double maxX = Double.NEGATIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;
    double minX = Double.POSITIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;

    public void moveto(double x, double y) {
        if (numPoints + 3 > points.length)
            reallocate();
        points[numPoints++] = Double.NaN;
        lineto(x, y);
    }

    public void lineto(double x, double y) {
        if (numPoints + 2 > points.length) reallocate();
        points[numPoints++] = x;
        points[numPoints++] = y;
        if (x > maxX) maxX = x;
        if (x < minX) minX = x;
        if (y > maxY) maxY = y;
        if (y < minY) minY = y;
    }

    public void translate(double x, double y) {
        for (int i = 0; i < numPoints; i++) {
            if (Double.isNaN(points[i])) continue;
            points[i++] += x;
            points[i] += y;
        }
        minX += x;
        maxX += x;
        minY += y;
        maxY += y;
    }

    protected void reallocate() {
        double[] newpoints = new double[points.length * 2];
        System.arraycopy(points, 0, newpoints, 0, numPoints);
        points = newpoints;
    }

    public Object clone() throws CloneNotSupportedException{
        try {
            Scribble s = (Scribble) super.clone();
            s.points = points.clone();
            return s;
        }
        catch (CloneNotSupportedException e) {
            return this;
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < numPoints; i++) {
            if (Double.isNaN(points[i])) {
                b.append("m ");
            }
            else {
                b.append(points[i]);
                b.append(' ');
            }
        }
        return b.toString();
    }

    public static Scribble parse(String s) throws NumberFormatException {
        StringTokenizer st = new StringTokenizer(s);
        Scribble scribble = new Scribble();
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.charAt(0) == 'm') {
                scribble.moveto(Double.parseDouble(st.nextToken()),
                        Double.parseDouble(st.nextToken()));
            }
            else {
                scribble.lineto(Double.parseDouble(t),
                        Double.parseDouble(st.nextToken()));
            }
        }
        return scribble;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) (minX - 0.5f), (int) (minY - 0.5f),
                (int) (maxX - minX + 0.5f), (int) (maxY - minY + 0.5f));
    }

    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean contains(Point2D p) {
        return false;
    }

    public boolean contains(Rectangle2D r) {
        return false;
    }

    public boolean contains(double x, double y) {
        return false;
    }

    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    public boolean intersects(Rectangle2D r) {
        if (numPoints < 4) return false;
        int i = 0;
        double x1, y1, x2 = 0.0, y2 = 0.0;
        while (i < numPoints) {
            if (Double.isNaN(points[i])) {
                i++;
                x2 = points[i++];
                y2 = points[i++];
            } else {
                x1 = x2;
                y1 = y2;
                x2 = points[i++];
                y2 = points[i++];
                if (r.intersectsLine(x1, y1, x2, y2)) return true;
            }
        }

        return false;
    }

    public boolean intersects(double x, double y, double w, double h) {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return new ScribbleIterator(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    public class ScribbleIterator implements PathIterator {
        protected int i = 0;                 // Position in array
        protected AffineTransform transform;

        public ScribbleIterator(AffineTransform transform) {
            this.transform = transform;
        }

        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }

        public boolean isDone() {
            return i >= numPoints;
        }

        public void next() {
            if (Double.isNaN(points[i])) i += 3;
            else i += 2;
        }

        public int currentSegment(float[] coords) {
            int retval;
            if (Double.isNaN(points[i])) {
                coords[0] = (float) points[i + 1];
                coords[1] = (float) points[i + 2];
                retval = SEG_MOVETO;
            } else {
                coords[0] = (float) points[i];
                coords[1] = (float) points[i + 1];
                retval = SEG_LINETO;
            }

            if (transform != null) transform.transform(coords, 0, coords, 0, 1);

            return retval;
        }

        public int currentSegment(double[] coords) {
            int retval;
            if (Double.isNaN(points[i])) {
                coords[0] = points[i + 1];
                coords[1] = points[i + 2];
                retval = SEG_MOVETO;
            } else {
                coords[0] = points[i];
                coords[1] = points[i + 1];
                retval = SEG_LINETO;
            }
            if (transform != null) transform.transform(coords, 0, coords, 0, 1);
            return retval;
        }
    }

    public static DataFlavor scribbleDataFlavor = new DataFlavor(Scribble.class, "Scribble");

    public static DataFlavor[] supportedFlavors = {
            scribbleDataFlavor,
            DataFlavor.stringFlavor
    };

    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.equals(scribbleDataFlavor) ||
                flavor.equals(DataFlavor.stringFlavor));
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (flavor.equals(scribbleDataFlavor)) {
            return this;
        }
        else if (flavor.equals(DataFlavor.stringFlavor)) {
            return toString();
        }
        else
            throw new UnsupportedFlavorException(flavor);
    }
}


