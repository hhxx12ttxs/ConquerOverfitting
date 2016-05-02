<<<<<<< HEAD
/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.tools.rectangle;

import java.util.Objects;

import javafx.geometry.Orientation;
import javafx.geometry.Point2D;

/**
 * The edge of a rectangle, i.e. a vertical or horizontal line segment.
 */
public class Edge2D {

    /*
     * ATTRIBUTES
     */

    /**
     * The edge's center point.
     */
    private final Point2D centerPoint;

    /**
     * The edge's orientation.
     */
    private final Orientation orientation;

    /**
     * The edge's length.
     */
    private final double length;

    /*
     * ATTRIBUTES
     */

    /**
     * Creates a new edge which is specified by its center point, orientation and length.
     * 
     * @param centerPoint
     *            the edge's center point
     * @param orientation
     *            the edge's orientation
     * @param length
     *            the edge's length; must be non-negative.
     */
    public Edge2D(Point2D centerPoint, Orientation orientation, double length) {
        Objects.requireNonNull(centerPoint, "The specified center point must not be null.");
        Objects.requireNonNull(orientation, "The specified orientation must not be null.");
        if (length < 0)
            throw new IllegalArgumentException(
                    "The length must not be negative, i.e. zero or a positive value is alowed.");

        this.centerPoint = centerPoint;
        this.orientation = orientation;
        this.length = length;
    }

    /*
     * CORNERS AND DISTANCES
     */

    /**
     * Returns the edge's upper left end point. It has ({@link #getLength() length} / 2) distance from the center point
     * and depending on the edge's orientation either the same X (for {@link Orientation#HORIZONTAL}) or Y (for
     * {@link Orientation#VERTICAL}) coordinate.
     * 
     * @return the edge's upper left point
     */
    public Point2D getUpperLeft() {
        if (isHorizontal()) {
            // horizontal
            double cornersX = centerPoint.getX() - (length / 2);
            double edgesY = centerPoint.getY();
            return new Point2D(cornersX, edgesY);
        } else {
            // vertical
            double edgesX = centerPoint.getX();
            double cornersY = centerPoint.getY() - (length / 2);
            return new Point2D(edgesX, cornersY);
        }
    }

    /**
     * Returns the edge's lower right end point. It has ({@link #getLength() length} / 2) distance from the center point
     * and depending on the edge's orientation either the same X (for {@link Orientation#HORIZONTAL}) or Y (for
     * {@link Orientation#VERTICAL}) coordinate.
     * 
     * @return the edge's lower right point
     */
    public Point2D getLowerRight() {
        if (isHorizontal()) {
            // horizontal
            double cornersX = centerPoint.getX() + (length / 2);
            double edgesY = centerPoint.getY();
            return new Point2D(cornersX, edgesY);
        } else {
            // vertical
            double edgesX = centerPoint.getX();
            double cornersY = centerPoint.getY() + (length / 2);
            return new Point2D(edgesX, cornersY);
        }
    }

    /**
     * Returns the distance of the specified point to the edge in terms of the dimension orthogonal to the edge's
     * orientation. The sign denotes whether on which side of the edge, the point lies.<br>
     * So e.g. if the edge is horizontal, only the Y coordinate's difference between the specified point and the edge is
     * considered. If the point lies to the right of the edge, the returned value is positive.
     * 
     * @param otherPoint
     *            the point to where the distance is computed
     * @return the distance
     */
    public double getOrthogonalDifference(Point2D otherPoint) {
        Objects.requireNonNull(otherPoint, "The other point must nt be null.");

        if (isHorizontal())
            // horizontal -> subtract y coordinates
            return otherPoint.getY() - centerPoint.getY();
        else
            // vertical-> subtract x coordinates
            return otherPoint.getX() - centerPoint.getX();
    }

    /*
     * ATTRIBUTE ACCESS
     */

    /**
     * @return the edge's center point
     */
    public Point2D getCenterPoint() {
        return centerPoint;
    }

    /**
     * Returns this edge's orientation. Note that the orientation can also be checked with {@link #isHorizontal()} and
     * {@link #isVertical()}.
     * 
     * @return the edge's orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Indicates whether this is a {@link Orientation#HORIZONTAL horizontal} edge.
     * 
     * @return true if {@link #getOrientation()} returns {@link Orientation#HORIZONTAL}
     */
    public boolean isHorizontal() {
        return orientation == Orientation.HORIZONTAL;
    }

    /**
     * Indicates whether this is a {@link Orientation#VERTICAL horizontal} edge.
     * 
     * @return true if {@link #getOrientation()} returns {@link Orientation#VERTICAL}
     */
    public boolean isVertical() {
        return orientation == Orientation.VERTICAL;
    }

    /**
     * @return the edge's length
     */
    public double getLength() {
        return length;
    }

    /*
     * EQUALS, HASHCODE & TOSTRING
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((centerPoint == null) ? 0 : centerPoint.hashCode());
        long temp;
        temp = Double.doubleToLongBits(length);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge2D other = (Edge2D) obj;
        if (centerPoint == null) {
            if (other.centerPoint != null)
                return false;
        } else if (!centerPoint.equals(other.centerPoint))
            return false;
        if (Double.doubleToLongBits(length) != Double.doubleToLongBits(other.length))
            return false;
        if (orientation != other.orientation)
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Edge2D [centerX = " + centerPoint.getX() + ", centerY = " + centerPoint.getY()
                + ", orientation = " + orientation + ", length = " + length + "]";
    }

}
=======
package visual.node;

import GroupNode.CustomStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

public class CustomConnectionWidget extends ConnectionWidget {
    private Point lastPoint;

    public CustomConnectionWidget(Scene scene) {
        super(scene);
    }

    @Override
    protected void paintWidget() {
        Graphics2D gr = getGraphics();
        gr.setColor(getForeground());
        GeneralPath path = null;

        Point firstControlPoint = getFirstControlPoint();
        Point lastControlPoint = getLastControlPoint();

        //checking to see if we should draw line through the AnchorShape. If the
        //AnchorShape is hollow, the cutdistance will be true.
        boolean isSourceCutDistance = this.getSourceAnchorShape().getCutDistance() != 0.0;
        boolean isTargetCutDistance = this.getTargetAnchorShape().getCutDistance() != 0.0;

        double firstControlPointRotation =
                firstControlPoint != null && (this.getSourceAnchorShape().isLineOriented()
                || isSourceCutDistance)
                ? this.getSourceAnchorShapeRotation() : 0.0;
        double lastControlPointRotation =
                lastControlPoint != null
                && (this.getTargetAnchorShape().isLineOriented() || isTargetCutDistance)
                ? getTargetAnchorShapeRotation() : 0.0;

        List<Point> points;
        if ((isSourceCutDistance || isTargetCutDistance) && this.getControlPoints().size() >= 2) {
            points = new ArrayList<Point>(this.getControlPoints());
            points.set(0, new Point(
                    firstControlPoint.x + (int) (this.getSourceAnchorShape().getCutDistance() * Math.cos(firstControlPointRotation)),
                    firstControlPoint.y + (int) (this.getSourceAnchorShape().getCutDistance() * Math.sin(firstControlPointRotation))));
            points.set(this.getControlPoints().size() - 1, new Point(
                    lastControlPoint.x + (int) (this.getTargetAnchorShape().getCutDistance() * Math.cos(lastControlPointRotation)),
                    lastControlPoint.y + (int) (this.getTargetAnchorShape().getCutDistance() * Math.sin(lastControlPointRotation))));
        } else {
            points = this.getControlPoints();
        }

        if (this.getControlPointCutDistance() > 0) {
            for (int a = 0; a < points.size() - 1; a++) {
                Point p1 = points.get(a);
                Point p2 = points.get(a + 1);
                double len = p1.distance(p2);

                if (a > 0) {
                    Point p0 = points.get(a - 1);
                    double ll = p0.distance(p1);
                    if (len < ll) {
                        ll = len;
                    }
                    ll /= 2;
                    double cll = this.getControlPointCutDistance();
                    if (cll > ll) {
                        cll = ll;
                    }
                    double direction = Math.atan2(p2.y - p1.y, p2.x - p1.x);
                    if (!Double.isNaN(direction)) {
                        path = customAddToPath(path,
                                p1.x + (int) (cll * Math.cos(direction)),
                                p1.y + (int) (cll * Math.sin(direction)));
                    }
                } else {
                    path = customAddToPath(path, p1.x, p1.y);
                }

                if (a < points.size() - 2) {
                    Point p3 = points.get(a + 2);
                    double ll = p2.distance(p3);
                    if (len < ll) {
                        ll = len;
                    }
                    ll /= 2;
                    double cll = this.getControlPointCutDistance();
                    if (cll > ll) {
                        cll = ll;
                    }
                    double direction = Math.atan2(p2.y - p1.y, p2.x - p1.x);
                    if (!Double.isNaN(direction)) {
                        path = customAddToPath(path,
                                p2.x - (int) (cll * Math.cos(direction)),
                                p2.y - (int) (cll * Math.sin(direction)));
                    }
                } else {
                    path = customAddToPath(path, p2.x, p2.y);
                }
            }
        } else {
            for (Point point : points) {
                path = customAddToPath(path, point.x, point.y);
            }
        }
        if (path != null) {
            Stroke previousStroke = gr.getStroke();
            gr.setPaint(getForeground());
            gr.setStroke(new CustomStroke(new Rectangle2D.Float(0,0,5,3), 10f));
            gr.draw(path);
            gr.setStroke(previousStroke);
        }


        AffineTransform previousTransform;

        if (firstControlPoint != null) {
            previousTransform = gr.getTransform();
            gr.translate(firstControlPoint.x, firstControlPoint.y);
            if (this.getSourceAnchorShape().isLineOriented()) {
                gr.rotate(firstControlPointRotation);
            }
            this.getSourceAnchorShape().paint(gr, true);
            gr.setTransform(previousTransform);
        }

        if (lastControlPoint != null) {
            previousTransform = gr.getTransform();
            gr.translate(lastControlPoint.x, lastControlPoint.y);
            if (this.getTargetAnchorShape().isLineOriented()) {
                gr.rotate(lastControlPointRotation);
            }
            this.getTargetAnchorShape().paint(gr, false);
            gr.setTransform(previousTransform);
        }

        if (this.isPaintControlPoints()) {
            int last = this.getControlPoints().size() - 1;
            for (int index = 0; index <= last; index++) {
                Point point = this.getControlPoints().get(index);
                previousTransform = gr.getTransform();
                gr.translate(point.x, point.y);
                if (index == 0 || index == last) {
                    this.getEndPointShape().paint(gr);
                } else {
                    this.getControlPointShape().paint(gr);
                }
                gr.setTransform(previousTransform);
            }
        }
    }

    private GeneralPath customAddToPath(GeneralPath path, int x, int y) {
        if (path == null) {
            path = new GeneralPath();
            path.moveTo(x, y);
        } else {
//            int x1 = (int) ((x - lastPoint.getX()));
//            int y1 = (int) ((y - lastPoint.getY()));
//            int x2 = x;
//            int y2 = y;
//            path.quadTo(x1, y1, x2, y2);
        path.lineTo(x, y);
        }
        lastPoint = new Point(x,y);

        return path;
    }

    /**
     * Returns the rotation of the source anchor shape.
     * @return the source anchor shape rotation
     */
    private double getSourceAnchorShapeRotation() {
        if (this.getControlPoints().size() <= 1) {
            return 0.0;
        }
        Point point1 = this.getControlPoints().get(0);
        Point point2 = this.getControlPoints().get(1);
        return Math.atan2(point2.y - point1.y, point2.x - point1.x);
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

