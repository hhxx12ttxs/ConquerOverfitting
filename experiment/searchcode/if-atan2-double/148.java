/**
 *  Licensed under GPL. For more information, see
 *    http://jaxodraw.sourceforge.net/license.html
 *  or the LICENSE file in the jaxodraw distribution.
 */
package net.sf.jaxodraw.object.arc;

import java.awt.Point;
import java.awt.geom.Point2D;

import net.sf.jaxodraw.object.Jaxo3PointObject;
import net.sf.jaxodraw.object.JaxoArrow;
import net.sf.jaxodraw.object.JaxoObject;
import net.sf.jaxodraw.util.JaxoGeometry;
import net.sf.jaxodraw.util.graphics.JaxoGraphics2D;


/**
 * The mother of all arc objects.
 *
 * @since 2.0
 */
public abstract class JaxoArcObject extends Jaxo3PointObject {

    /** 1 degree */
    private static final double DEFAULT_CUTOFF = Math.PI / 180.d;

    /**
     * Sets the points of this arc.
     *
     * @param x1 The x coordinate of the first click point of this arc.
     * @param y1 The y coordinate of the first click point of this arc.
     * @param x2 The x coordinate of the second click point of this arc.
     * @param y2 The y coordinate of the second click point of this arc.
     * @param p3 The x coordinate of the third click point of this arc.
     * @param q3 The y coordinate of the third click point of this arc.
     */
    public final void setArcPts(int x1, int y1, int x2, int y2, int p3, int q3) {
        set3Pts(x1, y1, x2, y2, p3, q3);
    }

    /** {@inheritDoc} */
    public boolean isCopy(JaxoObject comp) {
        boolean isCopy = false;

        if (comp instanceof JaxoArcObject && super.isCopy(comp)) {
            isCopy = true;
        }

        return isCopy;
    }

    /** {@inheritDoc} */
    public void setState(JaxoObject o) {
        copyFrom((JaxoArcObject) o);
    }

    /** {@inheritDoc} */
    public void paintVisualAid(JaxoGraphics2D g2) {
        g2.setColor(JaxoGraphics2D.DEFAULT_HELP_COLOR);
        g2.setStroke(JaxoGraphics2D.DEFAULT_HELP_STROKE);

        g2.drawLine(getX(), getY(), getX2(), getY2());
        g2.drawLine(getX2(), getY2(), getX3(), getY3());
        g2.drawLine(getX3(), getY3(), getX(), getY());
    }

    /**
     * Returns the parameters for the arc calculated from the three points.
     *
     * @return An array with 5 elements: {cx, cy, r, oa, sa} where cx = center-x,
     * cy = center-y, r = radius, oa = opening angle, sa = start angle.
     * The start angle is the angle of the first click point with respect to
     * the center point, and the opening angle is negative (positive) for
     * clockwise (anti-clockwise) arcs. Angles are returned in degrees.
     */
    protected final double[] getArcParameters() {
        double[] params = new double[5];

        if (isSingular() || isOneLine()) {
            params[0] = Double.NaN;
            params[1] = Double.NaN;
            params[2] = Double.POSITIVE_INFINITY;
            params[3] = 0.d;
            params[4] = 0.d;
        } else {
            Point2D.Double cp = getCenterPoint();
            double startAngle = Math.atan2(getY() - cp.y, getX() - cp.x);

            params[0] = cp.x;
            params[1] = cp.y;
            params[2] = getRadius(cp);
            params[3] = -Math.toDegrees(getOpeningAngle(cp));
            params[4] = -Math.toDegrees(startAngle);
        }

        return params;
    }

    /**
     * Checks if this arc is singular.
     *
     * <p>An arc is considered singular if its arc length goes to infinity.
     * This is the case when all three points are co-linear and the middle
     * click-point is on the outside from the other two. In practice it is
     * checked if the angle at the middle click-point is below a cutoff value
     * of one degree.</p>
     *
     * <p>Note that there is another type of singularity where the arc radius
     * goes to infinity while the arc length stays finite. This is the case
     * when the three points are co-linear and the middle click-point is
     * between the other two. Such an arc is not considered singular,
     * it only degenerates into a line.</p>
     *
     * <p>Arc-specific methods generally return simplified values for singular
     * arcs, eg the radius is set to infinity, so one should call this method
     * first to make sure that returned values can be expected to be accurate.</p>
     *
     * @return true if the angle at P2 is smaller than one degree.
     */
    public boolean isSingular() {
        return tooSingular(DEFAULT_CUTOFF);
    }

    /**
     * Tests if the current arc is too close to the singularity
     * where all three points are aligned and the middle click-point (P2)
     * is on the outside from the other two.
     *
     * @param epsilon upper bound for the angle at P2 (in radians).
     * @return true if the angle at P2 is smaller than epsilon.
     */
    protected boolean tooSingular(double epsilon) {

        Point[] pp = getPoints();

        if (pp[0].equals(pp[1]) || pp[0].equals(pp[2])
            || pp[2].equals(pp[1])) {
            return true;
        }

        double dist12 =
            Math.sqrt(((pp[0].x - pp[1].x) * (pp[0].x - pp[1].x))
                + ((pp[0].y - pp[1].y) * (pp[0].y - pp[1].y)));
        double dist23 =
            Math.sqrt(((pp[2].x - pp[1].x) * (pp[2].x - pp[1].x))
                + ((pp[2].y - pp[1].y) * (pp[2].y - pp[1].y)));
        double dist31 =
            Math.sqrt(((pp[2].x - pp[0].x) * (pp[2].x - pp[0].x))
                + ((pp[2].y - pp[0].y) * (pp[2].y - pp[0].y)));

        //angle at P1
        double phi1 =
            Math.acos((
                    ((pp[1].x - pp[0].x) * (pp[2].x - pp[0].x))
                    + ((pp[1].y - pp[0].y) * (pp[2].y - pp[0].y))
                ) / dist12 / dist31);

        //angle at P2
        double phi2 =
            Math.acos((
                    ((pp[0].x - pp[1].x) * (pp[2].x - pp[1].x))
                    + ((pp[0].y - pp[1].y) * (pp[2].y - pp[1].y))
                ) / dist12 / dist23);


        boolean aligned = (Math.abs(phi1) < epsilon
                || Math.abs(Math.PI - phi1) < epsilon);

        return ((Math.abs(phi2) < epsilon) && aligned);
    }

    /** {@inheritDoc} */
    public JaxoArrow.Coordinates arrowCoordinates() {
        if (isSingular()) {
            return new JaxoArrow.Coordinates(Double.NaN, Double.NaN, 0.d);
        }

        if (isOneLine()) {
            double length = Math.sqrt((getX() - getX3()) * (getX() - getX3())
                    + ((getY() - getY3()) * (getY() - getY3())));
            float arp = getArrowPosition();

            double theta = Math.atan2(getY2() - getY(), getX2() - getX());

            double x = getX() + arp * length * Math.cos(theta);
            double y = getY() + arp * length * Math.sin(theta);

            if (isFlip()) {
                theta = theta + Math.PI;
            }

            return new JaxoArrow.Coordinates(x, y, theta);
        }

        Point2D.Double cp = getCenterPoint();
        Point2D.Double ap = getArcPoint(getArrowPosition(), cp);

        double theta = Math.atan2(ap.y - cp.y, ap.x - cp.x);

        if (isClockwise(cp)) {
            theta = theta + (Math.PI / 2.d);
        } else {
            theta = theta - (Math.PI / 2.d);
        }

        if (isFlip()) {
            theta = theta + Math.PI;
        }

        return new JaxoArrow.Coordinates(ap.x, ap.y, theta);
    }

    /**
    * Determines the 'handed-ness' of this arc, ie whether the three points
    * (in click order) lie in clock- or anti-clockwise direction.
    * A straight line and a singular arc are considered clock-wise.
    *
    * @return True if this is a clock-wise arc, false otherwise.
    */
    public final boolean isClockwise() {
        if (isOneLine()) {
            return true;
        }

        return isClockwise(getCenterPoint());
    }

    private boolean isClockwise(Point2D.Double cp) {
        return isClockwise(cp, getMidArcPoint(cp));
    }

    private boolean isClockwise(Point2D.Double cp, Point2D.Double mp) {
        double dir = ((getX() - cp.x) * (mp.y - cp.y))
                - ((getY() - cp.y) * (mp.x - cp.x));

        return (dir >= 0);
    }

    /**
     * Calculates the length of this arc.
     *
     * @return The length of this arc.
     * If the arc is singular, Double.POSITIVE_INFINITY is returned.
     */
    public final double getArcLength() {
        if (isSingular()) {
            return Double.POSITIVE_INFINITY;
        }

        if (isOneLine()) {
            return Math.sqrt((getX() - getX3()) * (getX() - getX3())
                    + ((getY() - getY3()) * (getY() - getY3())));
        }

        Point2D.Double cp = getCenterPoint();
        Point2D.Double mp = getMidArcPoint(cp);

        double rsq = ((getX() - cp.x) * (getX() - cp.x))
                + ((getY() - cp.y) * (getY() - cp.y));
        double ang = Math.acos((((getX() - cp.x) * (mp.x - cp.x))
                + ((getY() - cp.y) * (mp.y - cp.y))) / rsq);

        return 2.d * ang * Math.sqrt(rsq);
    }

    /**
     * Calculates the opening angle of this arc.
     * For clockwise arcs this angle is positive,
     * for anti-clockwise arcs it is negative.
     *
     * @return The opening angle of this arc in radians.
     * If the arc is singular, 2 * PI is returned.
     * If the three arc points are co-linear, 0 is returned.
     */
    public final double getOpeningAngle() {
        if (isSingular()) {
            return 2.d * Math.PI;
        }

        if (isOneLine()) {
            return 0.d;
        }

        return getOpeningAngle(getCenterPoint());
    }

    private double getOpeningAngle(Point2D.Double cp) {
        return getOpeningAngle(cp, getMidArcPoint(cp));
    }

    private double getOpeningAngle(Point2D.Double cp, Point2D.Double mp) {
        double rsq = ((getX() - cp.x) * (getX() - cp.x))
                + ((getY() - cp.y) * (getY() - cp.y));
        double ang = Math.acos((((getX() - cp.x) * (mp.x - cp.x))
                + ((getY() - cp.y) * (mp.y - cp.y))) / rsq);

        if (!isClockwise(cp, mp)) {
            ang = -ang;
        }

        return 2.d * ang;
    }

    /**
     * Calculates the radius of this arc.
     *
     * @return The radius of this arc.
     * If the arc is singular or the three arc points are co-linear,
     * Double.POSITIVE_INFINITY is returned.
     */
    public final double getRadius() {
        if (isOneLine() || isSingular()) {
            return Double.POSITIVE_INFINITY;
        }

        return getRadius(getCenterPoint());
    }

    private double getRadius(Point2D.Double cp) {
        return Math.sqrt(((getX() - cp.x) * (getX() - cp.x))
                + ((getY() - cp.y) * (getY() - cp.y)));
    }

    /**
     * Get equidistant points on the arc.
     * We divide the arc into n equidistant segments of length d,
     * so n * d = l is the total length of the arc.
     * This routine returns an arc point that is i * d away from P1.
     *
     * @param i The index of the point to return.
     * @param n The number of segments to divide the arc.
     *
     * @return A point on the arc.
     * If the arc is singular, a point with arguments NaN is returned.
     */
    public final Point2D.Double getEquidistantPoint(int i, int n) {
        if (isSingular()) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        if (isOneLine()) {
            double dx = (getX3() - getX()) / (double) n;
            double dy = (getY3() - getY()) / (double) n;
            return new Point2D.Double(getX() + i * dx, getY() + i * dy);
        }

        Point2D.Double cp = getCenterPoint();
        Point2D.Double mp = getMidArcPoint(cp);

        double rsq = ((getX() - cp.x) * (getX() - cp.x))
                + ((getY() - cp.y) * (getY() - cp.y));

        double ang =
            Math.acos((
                    ((getX() - cp.x) * (mp.x - cp.x))
                    + ((getY() - cp.y) * (mp.y - cp.y))
                ) / rsq);

        double dphi = (2.d * ang) / n;
        double phi1 = Math.atan2(getY() - cp.y, getX() - cp.x);
        double radius = Math.sqrt(rsq);
        double px;
        double py;

        Point2D.Double point = new Point2D.Double();

        if (isClockwise(cp, mp)) {
            px = cp.x + (radius * Math.cos(phi1 + (i * dphi)));
            py = cp.y + (radius * Math.sin(phi1 + (i * dphi)));
            point.setLocation(px, py);
        } else {
            px = cp.x + (radius * Math.cos(phi1 - (i * dphi)));
            py = cp.y + (radius * Math.sin(phi1 - (i * dphi)));
            point.setLocation(px, py);
        }

        return point;
    }

    /**
     * Get equidistant points on the arc.
     * Dividing the arc into n equidistant segments of length d,
     * so n * d = l is the total length of the arc,
     * this routine returns the n+1 arc points that are i * d away from P1,
     * where 0 <= i <= n.
     *
     * @param n The number of segments to divide the arc.
     *
     * @return The equidistant points of the arc.
     * If the arc is singular, an array of points with argument NaN is returned.
     */
    public Point2D.Float[] getEquidistantPoints(int n) {
        Point2D.Float[] points = new Point2D.Float[n + 1];

        if (isSingular()) {
            Point2D.Float nan = new Point2D.Float(Float.NaN, Float.NaN);

            for (int i = 0; i <= n; i++) {
                points[i] = nan;
            }

            return points;
        }

        if (isOneLine()) {
            float dx = (getX3() - getX()) / (float) n;
            float dy = (getY3() - getY()) / (float) n;

            for (int i = 0; i <= n; i++) {
                points[i] = new Point2D.Float(getX() + i * dx, getY() + i * dy);
            }

            return points;
        }

        Point2D.Double cp = getCenterPoint();
        Point2D.Double mp = getMidArcPoint(cp);

        double rsq = ((getX() - cp.x) * (getX() - cp.x))
                + ((getY() - cp.y) * (getY() - cp.y));
        double ang =
            Math.acos((
                    ((getX() - cp.x) * (mp.x - cp.x))
                    + ((getY() - cp.y) * (mp.y - cp.y))
                ) / rsq);
        double dphi = (2.d * ang) / n;
        double phi1 = Math.atan2(getY() - cp.y, getX() - cp.x);
        double radius = Math.sqrt(rsq);
        double px, py;

        int sign = 1;
        if (!isClockwise(cp, mp)) {
            sign = -1;
        }

        points[0] = new Point2D.Float(getX(), getY());

        for (int i = 1; i <= n; i++) {
            px = cp.x + (radius * Math.cos(phi1 + (sign * i * dphi)));
            py = cp.y + (radius * Math.sin(phi1 + (sign * i * dphi)));
            points[i] = new Point2D.Float((float) px, (float) py);
        }

        return points;
    }

    /**
     * This routine returns an arc point that is t * arcLength away from P1.
     *
     * @param t The parameter, has to be between 0 and 1.
     *
     * @return A point on the arc.
     * If the arc is singular, a point witha rguments NaN is returned.
     */
    public final Point2D.Double getArcPoint(double t) {
        if (isSingular()) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        if (isOneLine()) {
            return new Point2D.Double(getX() + t * (getX3() - getX()), getY() + t * (getY3() - getY()));
        }

        return getArcPoint(t, getCenterPoint());
    }

    private Point2D.Double getArcPoint(double t, Point2D.Double cp) {
        double par = JaxoGeometry.curveParameter(t) * getOpeningAngle(cp);

        double phi1 = Math.atan2(getY() - cp.y, getX() - cp.x);
        double radius = getRadius(cp);
        double px = cp.x + (radius * Math.cos(phi1 + par));
        double py = cp.y + (radius * Math.sin(phi1 + par));

        Point2D.Double point = new Point2D.Double(px, py);

        return point;
    }

    /**
     * Get the center point of the circle that contains the arc.
     *
     * @return The center point of this arc.
     * If the arc is singular or the three arc points are co-linear,
     * a Point with arguments NaN is returned.
     */
    public final Point2D.Double getCenterPoint() {
        if (isOneLine() || isSingular()) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        double aa = (double) (getX2() - getX());
        double bb = (double) (getY2() - getY());
        double cc = (double) (getX3() - getX());
        double dd = (double) (getY3() - getY());
        double ee = (aa * (getX() + getX2())) + (bb * (getY() + getY2()));
        double ff = (cc * (getX() + getX3())) + (dd * (getY() + getY3()));
        double gg =
            2.d * ((aa * (getY3() - getY2())) - (bb * (getX3() - getX2())));
        double px = ((dd * ee) - (bb * ff)) / gg;
        double py = ((aa * ff) - (cc * ee)) / gg;

        return new Point2D.Double(px, py);
    }

    /**
     * Calculates the point in the middle of the arc.
     * This is the point halfway between P1 and P3.
     *
     * @return The mid-arc point of this arc.
     * If the arc is singular, a point with arguments NaN is returned.
     */
    public final Point2D.Double getMidArcPoint() {
        if (isSingular()) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        if (isOneLine()) {
            return new Point2D.Double((getX() + getX3()) / 2.d, (getY() + getY3()) / 2.d);
        }

        return getMidArcPoint(getCenterPoint());
    }

    private Point2D.Double getMidArcPoint(Point2D.Double cp) {
        Point2D.Double p1 =
            new Point2D.Double((double) getX(), (double) getY());
        Point2D.Double p2 =
            new Point2D.Double((double) getX2(), (double) getY2());
        Point2D.Double p3 =
            new Point2D.Double((double) getX3(), (double) getY3());

        double rsq =
            ((p1.x - cp.x) * (p1.x - cp.x)) + ((p1.y - cp.y) * (p1.y - cp.y));

        // to get the mid-arc point, we intersect the circle with the
        // perpendicular of the line P1-P3 going through the point between them
        double x13 = (p3.x + p1.x) / 2.d;
        double y13 = (p3.y + p1.y) / 2.d;
        double px1;
        double px2;
        double py1;
        double py2;
        double rel1;
        double rel2;

        if (getX() == getX3()) {
            py1 = y13;
            py2 = y13;
            px1 = cp.x - Math.sqrt(rsq - ((y13 - cp.y) * (y13 - cp.y)));
            px2 = cp.x + Math.sqrt(rsq - ((y13 - cp.y) * (y13 - cp.y)));
            rel1 = p2.x - p1.x;
            rel2 = px2 - p1.x;
        } else if (getY() == getY3()) {
            px1 = x13;
            px2 = x13;
            py1 = cp.y - Math.sqrt(rsq - ((x13 - cp.x) * (x13 - cp.x)));
            py2 = cp.y + Math.sqrt(rsq - ((x13 - cp.x) * (x13 - cp.x)));
            rel1 = p2.y - p1.y;
            rel2 = py2 - p1.y;
        } else {
            double ma = (p3.y - p1.y) / (p3.x - p1.x);
            double a = 1.d + (1 / ma / ma);
            double b =
                ((2 * cp.y) / ma) - (2 * cp.x) - ((2 * y13) / ma)
                - ((2 * x13) / ma / ma);
            double c =
                (
                    (
                        (cp.x * cp.x) + ((x13 * x13) / ma / ma) + (y13 * y13)
                        + ((2 * y13 * x13) / ma)
                    ) - ((2 * cp.y * x13) / ma) - (2 * cp.y * y13)
                    + (cp.y * cp.y)
                ) - rsq;
            double rad = Math.sqrt((b * b) - (4 * a * c));
            px1 = (-b + rad) / 2 / a;
            px2 = (-b - rad) / 2 / a;
            py1 = (-(px1 - x13) / ma) + y13;
            py2 = (-(px2 - x13) / ma) + y13;
            rel1 = ((ma * (px2 - p1.x)) + p1.y) - py2;
            rel2 = ((ma * (p2.x - p1.x)) + p1.y) - p2.y;
        }

        // there are two solutions: (px1, py1) and (px2, py2),
        // we want the one that is on the same side of the line P1-P3 as P2
        double px = px1;
        double py = py1;

        if ((rel1 * rel2) > 0.d) {
            px = px2;
            py = py2;
        }

        return new Point2D.Double(px, py);
    }
}

