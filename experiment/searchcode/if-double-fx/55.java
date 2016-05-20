package tracketeer.search.geom2d;

import java.awt.*;
import java.util.Collection;

public class Geom2D {

    /**
     * Calculates the direction vector from one point to another.
     *
     * @param start
     * @param end
     * @param factor Length of the resulting vector
     * @return The normalised vector between start and end.
     */
    public final static Vector heading(Point start, Point end, double factor) {
        double fx = end.getX() - start.getX();
        double fy = end.getY() - start.getY();
        double length = Math.sqrt(Math.pow(fx, 2) + Math.pow(fy, 2));

        fx /= length; fy /= length;
        fx *= factor; fy *= factor;

        return new Vector((int) Math.round(fx), (int) Math.round(fy));
    }

    public final static Vector heading(Point start, Point end) {
        return heading(start, end, 1);
    }

    public final static Vector add(Vector a, Vector b) {
        return new Vector(a.x + b.x, a.y + b.y);
    }

    public final static Vector center(Shape shape) {
        double x = shape.getBounds().getCenterX();
        double y = shape.getBounds().getCenterY();

        return new Vector((int) Math.round(x), (int) Math.round(y));
    }

    public final static double length(Point vector) {
        return Math.sqrt(Math.pow(vector.getX(), 2) + Math.pow(vector.getY(), 2));
    }

    public final static double distance(Point a, Point b) {
        return length(new Point(b.x - a.x, b.y - a.y));
    }

    /**
     * Computes the intersection point of two lines.
     *
     * Source: http://www.java-gaming.org/topics/utils-essentials/22144/view.html
     *         Post #32 by 'DavidX'
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param x4
     * @param y4
     * @return
     */
    public final static Point intersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            return new Point((int) (x1 + ua*(x2 - x1)), (int) (y1 + ua*(y2 - y1)));
        }

        return null;
    }

    public final static Point intersection(Line a, Line b) {
        return intersection(a.getStart().x, a.getStart().y, a.getEnd().x, a.getEnd().y,
                b.getStart().x, b.getStart().y, b.getEnd().x, b.getEnd().y);
    }

    public static java.util.List<Vector> intersection(Line line, Shape shape) {
        if (shape instanceof Figure<?>) {
            return ((Figure<?>) shape).intersections(line);
        } else { // not accurate obviously but better than nothing
            return new Rect(shape.getBounds()).intersections(line);
        }
    }

    public final static <P extends Point> P closest(Collection<P> points, Point target) {
        if (points.size() == 1) return points.iterator().next();

        double minDistance = Double.MAX_VALUE;
        P result = null;

        for (P pt : points) {
            double distance = Geom2D.distance(target, pt);
            if (distance < minDistance) {
                minDistance = distance;
                result = pt;
            }
        }

        return result;
    }
}

