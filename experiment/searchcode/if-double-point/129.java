<<<<<<< HEAD
/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.geo.builders;

import java.io.IOException;
import java.util.*;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.ElasticSearchParseException;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.DistanceUnit.Distance;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class ShapeBuilder implements ToXContent {

    protected static final ESLogger LOGGER = ESLoggerFactory.getLogger(ShapeBuilder.class.getName());

    private static final boolean DEBUG;
    static {
        // if asserts are enabled we run the debug statements even if they are not logged
        // to prevent exceptions only present if debug enabled
        boolean debug = false;
        assert debug = true;
        DEBUG = debug;
    }

    public static final double DATELINE = 180;
    public static final GeometryFactory FACTORY = new GeometryFactory();
    public static final JtsSpatialContext SPATIAL_CONTEXT = new JtsSpatialContext(true);

    protected final boolean wrapdateline = true;

    protected ShapeBuilder() {

    }

    protected static Coordinate coordinate(double longitude, double latitude) {
        return new Coordinate(longitude, latitude);
    }

    /**
     * Create a new point
     * 
     * @param longitude longitude of the point
     * @param latitude latitude of the point
     * @return a new {@link PointBuilder}
     */
    public static PointBuilder newPoint(double longitude, double latitude) {
        return newPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Create a new {@link PointBuilder} from a {@link Coordinate}
     * @param coordinate coordinate defining the position of the point
     * @return a new {@link PointBuilder}
     */
    public static PointBuilder newPoint(Coordinate coordinate) {
        return new PointBuilder().coordinate(coordinate);
    }

    /**
     * Create a new set of points
     * @return new {@link MultiPointBuilder}
     */
    public static MultiPointBuilder newMultiPoint() {
        return new MultiPointBuilder();
    }

    /**
     * Create a new lineString
     * @return a new {@link LineStringBuilder}
     */
    public static LineStringBuilder newLineString() {
        return new LineStringBuilder();
    }

    /**
     * Create a new Collection of lineStrings
     * @return a new {@link MultiLineStringBuilder}
     */
    public static MultiLineStringBuilder newMultiLinestring() {
        return new MultiLineStringBuilder();
    }

    /**
     * Create a new Polygon
     * @return a new {@link PointBuilder}
     */
    public static PolygonBuilder newPolygon() {
        return new PolygonBuilder();
    }

    /**
     * Create a new Collection of polygons
     * @return a new {@link MultiPolygonBuilder}
     */
    public static MultiPolygonBuilder newMultiPolygon() {
        return new MultiPolygonBuilder();
    }

    /**
     * create a new Circle
     * @return a new {@link CircleBuilder}
     */
    public static CircleBuilder newCircleBuilder() {
        return new CircleBuilder();
    }

    /**
     * create a new rectangle
     * @return a new {@link EnvelopeBuilder}
     */
    public static EnvelopeBuilder newEnvelope() {
        return new EnvelopeBuilder();
    }

    @Override
    public String toString() {
        try {
            XContentBuilder xcontent = JsonXContent.contentBuilder();
            return toXContent(xcontent, EMPTY_PARAMS).prettyPrint().string();
        } catch (IOException e) {
            return super.toString();
        }
    }

    /**
     * Create a new Shape from this builder. Since calling this method could change the
     * defined shape. (by inserting new coordinates or change the position of points)
     * the builder looses its validity. So this method should only be called once on a builder  
     * @return new {@link Shape} defined by the builder
     */
    public abstract Shape build();

    /**
     * Recursive method which parses the arrays of coordinates used to define
     * Shapes
     * 
     * @param parser
     *            Parser that will be read from
     * @return CoordinateNode representing the start of the coordinate tree
     * @throws IOException
     *             Thrown if an error occurs while reading from the
     *             XContentParser
     */
    private static CoordinateNode parseCoordinates(XContentParser parser) throws IOException {
        XContentParser.Token token = parser.nextToken();

        // Base case
        if (token != XContentParser.Token.START_ARRAY) {
            double lon = parser.doubleValue();
            token = parser.nextToken();
            double lat = parser.doubleValue();
            token = parser.nextToken();
            return new CoordinateNode(new Coordinate(lon, lat));
        }

        List<CoordinateNode> nodes = new ArrayList<CoordinateNode>();
        while (token != XContentParser.Token.END_ARRAY) {
            nodes.add(parseCoordinates(parser));
            token = parser.nextToken();
        }

        return new CoordinateNode(nodes);
    }

    /**
     * Create a new {@link ShapeBuilder} from {@link XContent}
     * @param parser parser to read the GeoShape from
     * @return {@link ShapeBuilder} read from the parser or null
     *          if the parsers current token has been <code><null</code>
     * @throws IOException if the input could not be read
     */
    public static ShapeBuilder parse(XContentParser parser) throws IOException {
        return GeoShapeType.parse(parser);
    }

    protected static XContentBuilder toXContent(XContentBuilder builder, Coordinate coordinate) throws IOException {
        return builder.startArray().value(coordinate.x).value(coordinate.y).endArray();
    }

    protected static Coordinate shift(Coordinate coordinate, double dateline) {
        if (dateline == 0) {
            return coordinate;
        } else {
            return new Coordinate(-2 * dateline + coordinate.x, coordinate.y);
        }
    }

    /**
     * get the shapes type
     * @return type of the shape
     */
    public abstract GeoShapeType type();

    /**
     * Calculate the intersection of a line segment and a vertical dateline.
     * 
     * @param p1
     *            start-point of the line segment
     * @param p2
     *            end-point of the line segment
     * @param dateline
     *            x-coordinate of the vertical dateline
     * @return position of the intersection in the open range (0..1] if the line
     *         segment intersects with the line segment. Otherwise this method
     *         returns {@link Double#NaN}
     */
    protected static final double intersection(Coordinate p1, Coordinate p2, double dateline) {
        if (p1.x == p2.x) {
            return Double.NaN;
        } else {
            final double t = (dateline - p1.x) / (p2.x - p1.x);
            if (t > 1 || t <= 0) {
                return Double.NaN;
            } else {
                return t;
            }
        }
    }

    /**
     * Calculate all intersections of line segments and a vertical line. The
     * Array of edges will be ordered asc by the y-coordinate of the
     * intersections of edges.
     * 
     * @param dateline
     *            x-coordinate of the dateline
     * @param edges
     *            set of edges that may intersect with the dateline
     * @return number of intersecting edges
     */
    protected static int intersections(double dateline, Edge[] edges) {
        int numIntersections = 0;
        assert !Double.isNaN(dateline);
        for (int i = 0; i < edges.length; i++) {
            Coordinate p1 = edges[i].coordinate;
            Coordinate p2 = edges[i].next.coordinate;
            assert !Double.isNaN(p2.x) && !Double.isNaN(p1.x);  
            edges[i].intersect = IntersectionOrder.SENTINEL;

            double position = intersection(p1, p2, dateline);
            if (!Double.isNaN(position)) {
                if (position == 1) {
                    if (Double.compare(p1.x, dateline) == Double.compare(edges[i].next.next.coordinate.x, dateline)) {
                        // Ignore the ear
                        continue;
                    } else if (p2.x == dateline) {
                        // Ignore Linesegment on dateline
                        continue;
                    }
                }
                edges[i].intersection(position);
                numIntersections++;
            }
        }
        Arrays.sort(edges, INTERSECTION_ORDER);
        return numIntersections;
    }

    /**
     * Node used to represent a tree of coordinates.
     * <p/>
     * Can either be a leaf node consisting of a Coordinate, or a parent with
     * children
     */
    protected static class CoordinateNode implements ToXContent {

        protected final Coordinate coordinate;
        protected final List<CoordinateNode> children;

        /**
         * Creates a new leaf CoordinateNode
         * 
         * @param coordinate
         *            Coordinate for the Node
         */
        protected CoordinateNode(Coordinate coordinate) {
            this.coordinate = coordinate;
            this.children = null;
        }

        /**
         * Creates a new parent CoordinateNode
         * 
         * @param children
         *            Children of the Node
         */
        protected CoordinateNode(List<CoordinateNode> children) {
            this.children = children;
            this.coordinate = null;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            if (children == null) {
                builder.startArray().value(coordinate.x).value(coordinate.y).endArray();
            } else {
                builder.startArray();
                for (CoordinateNode child : children) {
                    child.toXContent(builder, params);
                }
                builder.endArray();
            }
            return builder;
        }
    }

    /**
     * This helper class implements a linked list for {@link Coordinate}. It contains
     * fields for a dateline intersection and component id 
     */
    protected static final class Edge {
        Coordinate coordinate; // coordinate of the start point
        Edge next; // next segment
        Coordinate intersect; // potential intersection with dateline
        int component = -1; // id of the component this edge belongs to

        protected Edge(Coordinate coordinate, Edge next, Coordinate intersection) {
            this.coordinate = coordinate;
            this.next = next;
            this.intersect = intersection;
            if (next != null) {
                this.component = next.component;
            }
        }

        protected Edge(Coordinate coordinate, Edge next) {
            this(coordinate, next, IntersectionOrder.SENTINEL);
        }

        private static final int top(Coordinate[] points, int offset, int length) {
            int top = 0; // we start at 1 here since top points to 0
            for (int i = 1; i < length; i++) {
                if (points[offset + i].y < points[offset + top].y) {
                    top = i;
                } else if (points[offset + i].y == points[offset + top].y) {
                    if (points[offset + i].x < points[offset + top].x) {
                        top = i;
                    }
                }
            }
            return top;
        }

        /**
         * Concatenate a set of points to a polygon
         * 
         * @param component
         *            component id of the polygon
         * @param direction
         *            direction of the ring
         * @param points
         *            list of points to concatenate
         * @param pointOffset
         *            index of the first point
         * @param edges
         *            Array of edges to write the result to
         * @param edgeOffset
         *            index of the first edge in the result
         * @param length
         *            number of points to use
         * @return the edges creates
         */
        private static Edge[] concat(int component, boolean direction, Coordinate[] points, final int pointOffset, Edge[] edges, final int edgeOffset,
                int length) {
            assert edges.length >= length+edgeOffset;
            assert points.length >= length+pointOffset;
            edges[edgeOffset] = new Edge(points[pointOffset], null);
            for (int i = 1; i < length; i++) {
                if (direction) {
                    edges[edgeOffset + i] = new Edge(points[pointOffset + i], edges[edgeOffset + i - 1]);
                    edges[edgeOffset + i].component = component;
                } else {
                    edges[edgeOffset + i - 1].next = edges[edgeOffset + i] = new Edge(points[pointOffset + i], null);
                    edges[edgeOffset + i - 1].component = component;
                }
            }

            if (direction) {
                edges[edgeOffset].next = edges[edgeOffset + length - 1];
                edges[edgeOffset].component = component;
            } else {
                edges[edgeOffset + length - 1].next = edges[edgeOffset];
                edges[edgeOffset + length - 1].component = component;
            }

            return edges;
        }

        /**
         * Create a connected list of a list of coordinates
         * 
         * @param points
         *            array of point
         * @param offset
         *            index of the first point
         * @param length
         *            number of points
         * @return Array of edges
         */
        protected static Edge[] ring(int component, boolean direction, Coordinate[] points, int offset, Edge[] edges, int toffset,
                int length) {
            // calculate the direction of the points:
            // find the point a the top of the set and check its
            // neighbors orientation. So direction is equivalent
            // to clockwise/counterclockwise
            final int top = top(points, offset, length);
            final int prev = (offset + ((top + length - 1) % length));
            final int next = (offset + ((top + 1) % length));
            final boolean orientation = points[offset + prev].x > points[offset + next].x;
            return concat(component, direction ^ orientation, points, offset, edges, toffset, length);
        }

        /**
         * Set the intersection of this line segment to the given position
         * 
         * @param position
         *            position of the intersection [0..1]
         * @return the {@link Coordinate} of the intersection
         */
        protected Coordinate intersection(double position) {
            return intersect = position(coordinate, next.coordinate, position);
        }

        public static Coordinate position(Coordinate p1, Coordinate p2, double position) {
            if (position == 0) {
                return p1;
            } else if (position == 1) {
                return p2;
            } else {
                final double x = p1.x + position * (p2.x - p1.x);
                final double y = p1.y + position * (p2.y - p1.y);
                return new Coordinate(x, y);
            }
        }

        @Override
        public String toString() {
            return "Edge[Component=" + component + "; start=" + coordinate + " " + "; intersection=" + intersect + "]";
        }
    }

    protected static final IntersectionOrder INTERSECTION_ORDER = new IntersectionOrder();

    private static final class IntersectionOrder implements Comparator<Edge> {
        private static final Coordinate SENTINEL = new Coordinate(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        
        @Override
        public int compare(Edge o1, Edge o2) {
            return Double.compare(o1.intersect.y, o2.intersect.y);
        }

    }

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_COORDINATES = "coordinates";

    protected static final boolean debugEnabled() {
        return LOGGER.isDebugEnabled() || DEBUG;
    }

    /**
     * Enumeration that lists all {@link GeoShapeType}s that can be handled
     */
    public static enum GeoShapeType {
        POINT("point"),
        MULTIPOINT("multipoint"),
        LINESTRING("linestring"),
        MULTILINESTRING("multilinestring"),
        POLYGON("polygon"),
        MULTIPOLYGON("multipolygon"),
        ENVELOPE("envelope"),
        CIRCLE("circle");

        protected final String shapename;

        private GeoShapeType(String shapename) {
            this.shapename = shapename;
        }

        public static GeoShapeType forName(String geoshapename) {
            String typename = geoshapename.toLowerCase(Locale.ROOT);
            for (GeoShapeType type : values()) {
                if(type.shapename.equals(typename)) {
                    return type;
                }
            }
            throw new ElasticSearchIllegalArgumentException("unknown geo_shape ["+geoshapename+"]");
        }

        public static ShapeBuilder parse(XContentParser parser) throws IOException {
            if (parser.currentToken() == XContentParser.Token.VALUE_NULL) {
                return null;
            } else if (parser.currentToken() != XContentParser.Token.START_OBJECT) {
                throw new ElasticSearchParseException("Shape must be an object consisting of type and coordinates");
            }

            GeoShapeType shapeType = null;
            Distance radius = null;
            CoordinateNode node = null;

            XContentParser.Token token;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    String fieldName = parser.currentName();

                    if (FIELD_TYPE.equals(fieldName)) {
                        parser.nextToken();
                        shapeType = GeoShapeType.forName(parser.text());
                    } else if (FIELD_COORDINATES.equals(fieldName)) {
                        parser.nextToken();
                        node = parseCoordinates(parser);
                    } else if (CircleBuilder.FIELD_RADIUS.equals(fieldName)) {
                        parser.nextToken();
                        radius = Distance.parseDistance(parser.text(), DistanceUnit.METERS);
                    } else {
                        parser.nextToken();
                        parser.skipChildren();
                    }
                }
            }

            if (shapeType == null) {
                throw new ElasticSearchParseException("Shape type not included");
            } else if (node == null) {
                throw new ElasticSearchParseException("Coordinates not included");
            } else if (radius != null && GeoShapeType.CIRCLE != shapeType) {
                throw new ElasticSearchParseException("Field [" + CircleBuilder.FIELD_RADIUS + "] is supported for [" + CircleBuilder.TYPE
                        + "] only");
            }

            switch (shapeType) {
                case POINT: return parsePoint(node);
                case MULTIPOINT: return parseMultiPoint(node);
                case LINESTRING: return parseLineString(node);
                case MULTILINESTRING: return parseMultiLine(node);
                case POLYGON: return parsePolygon(node);
                case MULTIPOLYGON: return parseMultiPolygon(node);
                case CIRCLE: return parseCircle(node, radius);
                case ENVELOPE: return parseEnvelope(node);
                default:
                    throw new ElasticSearchParseException("Shape type [" + shapeType + "] not included");
            }
        }
        
        protected static PointBuilder parsePoint(CoordinateNode node) {
            return newPoint(node.coordinate);
        }

        protected static CircleBuilder parseCircle(CoordinateNode coordinates, Distance radius) {
            return newCircleBuilder().center(coordinates.coordinate).radius(radius);
        }

        protected static EnvelopeBuilder parseEnvelope(CoordinateNode coordinates) {
            return newEnvelope().topLeft(coordinates.children.get(0).coordinate).bottomRight(coordinates.children.get(1).coordinate);
        }

        protected static MultiPointBuilder parseMultiPoint(CoordinateNode coordinates) {
            MultiPointBuilder points = new MultiPointBuilder();
            for (CoordinateNode node : coordinates.children) {
                points.point(node.coordinate);
            }
            return points;
        }

        protected static LineStringBuilder parseLineString(CoordinateNode coordinates) {
            LineStringBuilder line = newLineString();
            for (CoordinateNode node : coordinates.children) {
                line.point(node.coordinate);
            }
            return line;
        }

        protected static MultiLineStringBuilder parseMultiLine(CoordinateNode coordinates) {
            MultiLineStringBuilder multiline = newMultiLinestring();
            for (CoordinateNode node : coordinates.children) {
                multiline.linestring(parseLineString(node));
            }
            return multiline;
        }

        protected static PolygonBuilder parsePolygon(CoordinateNode coordinates) {
            LineStringBuilder shell = parseLineString(coordinates.children.get(0));
            PolygonBuilder polygon = new PolygonBuilder(shell.points);
            for (int i = 1; i < coordinates.children.size(); i++) {
                polygon.hole(parseLineString(coordinates.children.get(i)));
            }
            return polygon;
        }

        protected static MultiPolygonBuilder parseMultiPolygon(CoordinateNode coordinates) {
            MultiPolygonBuilder polygons = newMultiPolygon();
            for (CoordinateNode node : coordinates.children) {
                polygons.polygon(parsePolygon(node));
            }
            return polygons;
        }
    }
=======
package org.my.algo.one;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CircleAlgo {
	static final double eps = 1e-9;

	  public static double sqr(double x) {
	    return x * x;
	  }

	  public static double quickHypot(double x, double y) {
	    return Math.sqrt(x * x + y * y);
	  }

	  public static class Point {
	    double x, y;

	    public Point(double x, double y) {
	      this.x = x;
	      this.y = y;
	    }
	  }

	  public static class Circle {
	    double r, x, y;

	    public Circle(double x, double y, double r) {
	      this.x = x;
	      this.y = y;
	      this.r = r;
	    }

	    public boolean contains(Point p) {
	      return quickHypot(p.x - x, p.y - y) < r + eps;
	    }
	  }

	  public static Circle getCircumCircle(Point a, Point b) {
	    double x = (a.x + b.x) / 2.;
	    double y = (a.y + b.y) / 2.;
	    double r = quickHypot(a.x - x, a.y - y);
	    return new Circle(x, y, r);
	  }

	  public static Circle getCircumCircle(Point a, Point b, Point c) {
	    double Bx = b.x - a.x;
	    double By = b.y - a.y;
	    double Cx = c.x - a.x;
	    double Cy = c.y - a.y;
	    double d = 2 * (Bx * Cy - By * Cx);
	    double z1 = Bx * Bx + By * By;
	    double z2 = Cx * Cx + Cy * Cy;
	    double cx = Cy * z1 - By * z2;
	    double cy = Bx * z2 - Cx * z1;
	    double x = (double) cx / d;
	    double y = (double) cy / d;
	    double r = quickHypot(x, y);
	    return new Circle(x + a.x, y + a.y, r);
	  }

	  public static class Line {
	    double a, b, c;

	    public Line(double a, double b, double c) {
	      this.a = a;
	      this.b = b;
	      this.c = c;
	    }

	    public Line(Point p1, Point p2) {
	      a = +(p1.y - p2.y);
	      b = -(p1.x - p2.x);
	      c = p1.x * p2.y - p2.x * p1.y;
	    }
	  }

	  // geometric solution
	  public static List<Point2D.Double> circleLineIntersection(Circle circle, Line line) {
	    double a = line.a;
	    double b = line.b;
	    double c = line.c + circle.x * a + circle.y * b;
	    double r = circle.r;
	    double aabb = a * a + b * b;
	    List<Point2D.Double> res = new ArrayList<Point2D.Double>();
	    double d = c * c / aabb - r * r;
	    if (d > eps)
	      return res;
	    double x0 = -a * c / aabb;
	    double y0 = -b * c / aabb;
	    if (d > -eps) {
	      res.add(new Point2D.Double(x0 + circle.x, y0 + circle.y));
	      return res;
	    }
	    d /= -aabb;
	    double k = Math.sqrt(d < 0 ? 0 : d);
	    res.add(new Point2D.Double(x0 + k * b + circle.x, y0 - k * a + circle.y));
	    res.add(new Point2D.Double(x0 - k * b + circle.x, y0 + k * a + circle.y));
	    return res;
	  }

	  // algebraic solution
	  public static List<Point2D.Double> circleLineIntersection2(Circle circle, Line line) {
	    double a = line.a;
	    double b = line.b;
	    double c = line.c;
	    double CX = circle.x, CY = circle.y;
	    double R = circle.r;
	    // ax+by+c=0
	    // (by+c+aCX)^2+(ay-aCY)^2=(aR)^2
	    boolean swap = false;
	    if (Math.abs(a) < Math.abs(b)) {
	      swap = true;
	      double t = a;
	      a = b;
	      b = t;
	      t = CX;
	      CX = CY;
	      CY = t;
	    }
	    List<Point2D.Double> res = new ArrayList<Point2D.Double>();
	    double A = a * a + b * b;
	    double B = 2.0 * b * (c + a * CX) - 2.0 * a * a * CY;
	    double C = sqr(c + a * CX) + a * a * (CY * CY - R * R);
	    double d = B * B - 4 * A * C;
	    if (d < -eps)
	      return res;
	    d = Math.sqrt(d < 0 ? 0 : d);
	    double y1 = (-B + d) / (2 * A);
	    double x1 = (-c - b * y1) / a;
	    double y2 = (-B - d) / (2 * A);
	    double x2 = (-c - b * y2) / a;
	    if (swap) {
	      double t = x1;
	      x1 = y1;
	      y1 = t;
	      t = x2;
	      x2 = y2;
	      y2 = t;
	    }
	    res.add(new Point2D.Double(x1, y1));
	    if (d > eps)
	      res.add(new Point2D.Double(x2, y2));
	    return res;
	  }

	  public static List<Point2D.Double> circleCircleIntersection(Circle c1, Circle c2) {
	    if (quickHypot(c1.x - c2.x, c1.y - c2.y) < eps) {
	      if (Math.abs(c1.r - c2.r) < eps) {
	        // infinity intersection points
	        return null;
	      }
	      return new ArrayList<Point2D.Double>();
	    }
	    c2.x -= c1.x;
	    c2.y -= c1.y;
	    double A = -2 * c2.x;
	    double B = -2 * c2.y;
	    double C = c2.x * c2.x + c2.y * c2.y + c1.r * c1.r - c2.r * c2.r;
	    List<Point2D.Double> res = circleLineIntersection(new Circle(0, 0, c1.r), new Line(A, B, C));
	    for (Point2D.Double point : res) {
	      point.x += c1.x;
	      point.y += c1.y;
	    }
	    return res;
	  }

	  public static double circleCircleIntersectionArea(Circle c1, Circle c2) {
	    double r = Math.min(c1.r, c2.r);
	    double R = Math.max(c1.r, c2.r);
	    double d = quickHypot(c1.x - c2.x, c1.y - c2.y);
	    if (d < R - r + eps) {
	      return Math.PI * r * r;
	    }
	    if (d > R + r - eps) {
	      return 0;
	    }
	    double area = r * r * Math.acos((d * d + r * r - R * R) / 2 / d / r) + R * R
	        * Math.acos((d * d + R * R - r * r) / 2 / d / R) - 0.5
	        * Math.sqrt((-d + r + R) * (d + r - R) * (d - r + R) * (d + r + R));
	    return area;
	  }

	  static Circle minEnclosingCircleWith2Points(List<Point> points, Point q1, Point q2) {
	    Circle circle = getCircumCircle(q1, q2);
	    for (int i = 0; i < points.size(); i++) {
	      if (!circle.contains(points.get(i))) {
	        circle = getCircumCircle(q1, q2, points.get(i));
	      }
	    }
	    return circle;
	  }

	  static Circle minEnclosingCircleWith1Point(List<Point> pointsList, Point q) {
	    List<Point> points = new ArrayList<Point>(pointsList);
	    Collections.shuffle(points);
	    Circle circle = getCircumCircle(points.get(0), q);
	    for (int i = 1; i < points.size(); i++) {
	      if (!circle.contains(points.get(i))) {
	        circle = minEnclosingCircleWith2Points(points.subList(0, i), points.get(i), q);
	      }
	    }
	    return circle;
	  }

	  public static Circle minEnclosingCircle(Point[] pointsArray) {
	    if (pointsArray.length == 0) {
	      return new Circle(0, 0, 0);
	    }
	    if (pointsArray.length == 1) {
	      return new Circle(pointsArray[0].x, pointsArray[0].y, 0);
	    }
	    List<Point> points = Arrays.asList(pointsArray);
	    Collections.shuffle(points);
	    Circle circle = getCircumCircle(points.get(0), points.get(1));
	    for (int i = 2; i < points.size(); i++) {
	      if (!circle.contains(points.get(i))) {
	        circle = minEnclosingCircleWith1Point(points.subList(0, i), points.get(i));
	      }
	    }
	    return circle;
	  }

	  // Usage example
	  public static void main(String[] args) {
	    for (int x1 = -4; x1 <= 4; x1++)
	      for (int y1 = -4; y1 <= 4; y1++)
	        for (int x2 = -4; x2 <= 4; x2++)
	          for (int y2 = -4; y2 <= 4; y2++)
	            for (int x3 = -4; x3 <= 4; x3++)
	              for (int y3 = -4; y3 <= 4; y3++) {
	                if (x1 == x2 && y1 == y2)
	                  continue;
	                Point p1 = new Point(x1, y1);
	                Point p2 = new Point(x2, y2);
	                Line line = new Line(p1, p2);
	                for (int r = 0; r <= 4; r++) {
	                  Circle circle = new Circle(x3, y3, r);
	                  List<Point2D.Double> res1 = circleLineIntersection(circle, line);
	                  List<Point2D.Double> res2 = circleLineIntersection2(circle, line);

	                  boolean eq = res1.size() == res2.size();
	                  if (eq) {
	                    int n = res1.size();
	                    boolean eq1 = true;
	                    for (int i = 0; i < n; i++) {
	                      eq1 &= Math.abs(res1.get(i).x - res2.get(i).x) < eps
	                          && Math.abs(res1.get(i).y - res2.get(i).y) < eps;
	                    }
	                    boolean eq2 = true;
	                    for (int i = 0; i < n; i++) {
	                      eq2 &= Math.abs(res1.get(i).x - res2.get(n - 1 - i).x) < eps
	                          && Math.abs(res1.get(i).y - res2.get(n - 1 - i).y) < eps;
	                    }
	                    eq = eq1 || eq2;
	                  }
	                  if (!eq) {
	                    System.out.println(res1);
	                    System.out.println(res2);
	                  }
	                }
	              }
	  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

