/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nav.util.math;

import exception.InvalidPositionException;
import nav.position.Position;

/**
 * Performs position/sailing calculations.
 *
 * @author Benjamin Jakobus, based on JMarine (by Cormac Gebruers and Benjamin
 *          Jakobus)
 * @version 1.0
 * @since 1.0
 */
public class NavCalculator {

    private double distance;
    private double trueCourse;

    /* The earth's radius in meters */
    public static final int WGS84_EARTH_RADIUS = 6378137;
    private String strCourse;

    /* The sailing calculation type */
    public static enum CalculationType {

        Mercator, GreatCirlce
    }

    /* The degree precision to use for courses */
    public static final int RL_CRS_PRECISION = 1;

    /* The distance precision to use for distances */
    public static final int RL_DIST_PRECISION = 1;
    public static final int METERS_PER_MINUTE = 1852;

    /**
     * Constructor.
     *
     * @param P1            <code>Position</code> p1.
     * @param P2            <code>Position</code> p2.
     * @param calcType      Type of calculation to be performed.
     * @since 1.0
     */
    public NavCalculator(Position P1, Position P2, CalculationType calcType) {
        switch (calcType) {
            case Mercator:
                mercatorSailing(P1, P2);
            case GreatCirlce:
                greatCircleSailing(P1, P2);
        }
    }

    /**
     * Constructor.
     *
     * @since 1.0
     */
    public NavCalculator() {
    }

    /**
     * Determines a great circle track between two positions.
     *
     * @param p1            Offset <code>Position</code>.
     * @param p2            Destination <code>Position</code>.
     * @since 1.0
     */
    public GCSailing greatCircleSailing(Position p1, Position p2) {
        return new GCSailing(new int[0], new float[0]);
    }

    /**
     * Determines a Rhumb Line course and distance between two points.
     *
     * @param p1            Offset <code>Position</code>.
     * @param p2            Destination <code>Position</code>.
     * @since 1.0
     */
    public RLSailing rhumbLineSailing(Position p1, Position p2) {
        RLSailing rl = mercatorSailing(p1, p2);
        return rl;
    }

    /**
     * Calculates the straight line distance between two positions.
     *
     * @param p1            First <code>Position</code>.
     * @param p2            Second <code>Position</code>.
     * @return
     * @since 1.0
     */
    public static double straightLineDistance(Position p1, Position p2) {
        // sqrt ( (x2 - x1)^2 + (y2 - y1) ^ 2 )
        double x = (p2.getLongitude() - p1.getLongitude());
        x = Math.pow(x, 2);
        double y = (p2.getLatitude() - p1.getLatitude());
        y = Math.pow(y, 2);

        return Math.sqrt((x + y));
    }

    /**
     * Determines the rhumb line course and distance between two positions.
     *
     * @param p1                Offset <code>Position</code>.
     * @param p2                Destination <code>Position</code>.
     * @since 1.0
     */
    public RLSailing mercatorSailing(Position p1, Position p2) {

        double dLat = computeDLat(p1.getLatitude(), p2.getLatitude());
        //plane sailing...
        if (dLat == 0) {
            RLSailing rl = planeSailing(p1, p2);
            return rl;
        }

        double dLong = computeDLong(p1.getLongitude(), p2.getLongitude());
        double dmp = (float) computeDMPClarkeSpheroid(p1.getLatitude(), p2.getLatitude());

        trueCourse = (float) Math.toDegrees(Math.atan(dLong / dmp));
        double degCrs = convertCourse((float) trueCourse, p1, p2);
        distance = (float) Math.abs(dLat / Math.cos(Math.toRadians(trueCourse)));

        RLSailing rl = new RLSailing(degCrs, (float) distance);
        trueCourse = rl.getCourse();
        strCourse = (dLat < 0 ? "S" : "N");
        strCourse += " " + trueCourse;
        strCourse += " " + (dLong < 0 ? "W" : "E");
        return rl;

    }

    /**
     * Calculate a plane sailing situation - i.e. where Lats are the same .
     * 
     * @param p1                Offset <code>Position</code>.
     * @param p2                Destination <code>Position</code>.
     * @return                  <code>RLSailing</code> containing course (in degrees)
     *                          and distance (in nautical miles).
     * @since 1.0
     */
    public RLSailing planeSailing(Position p1, Position p2) {
        double dLong = computeDLong(p1.getLongitude(), p2.getLongitude());

        double sgnDLong = 0 - (dLong / Math.abs(dLong));
        if (Math.abs(dLong) > 180 * 60) {
            dLong = (360 * 60 - Math.abs(dLong)) * sgnDLong;
        }

        double redist = 0;
        double recourse = 0;
        if (p1.getLatitude() == 0) {
            redist = Math.abs(dLong);
        } else {
            redist = Math.abs(dLong * (float) Math.cos(p1.getLatitude() * 2 * Math.PI / 360));
        }
        recourse = (float) Math.asin(0 - sgnDLong);
        recourse = recourse * 360 / 2 / (float) Math.PI;

        if (recourse < 0) {
            recourse = recourse + 360;
        }
        return new RLSailing(recourse, redist);
    }

    /**
     * Converts a course from cardinal XddY to ddd notation.
     * 
     * @param tc                True course (in degrees).
     * @param p1                First <code>Position</code>.
     * @param p2                Second <code>Position</code>.
     * @return                  The converted course.
     * @since 1.0
     */
    public static double convertCourse(float tc, Position p1, Position p2) {

        double dLat = p1.getLatitude() - p2.getLatitude();
        double dLong = p1.getLongitude() - p2.getLongitude();
        //NE
        if (dLong >= 0 & dLat >= 0) {
            return Math.abs(tc);
        }

        //SE
        if (dLong >= 0 & dLat < 0) {
            return 180 - Math.abs(tc);
        }

        //SW
        if (dLong < 0 & dLat < 0) {
            return 180 + Math.abs(tc);
        }

        //NW
        if (dLong < 0 & dLat >= 0) {
            return 360 - Math.abs(tc);
        }
        return -1;
    }

    /**
     * Getter method for the distance between two points.
     * 
     * @return distance         Distance between two points (in nautical miles).
     * @since 1.0
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Getter method for the true course.
     *
     * @return true course      True course (in degrees).
     * @since 1.0
     */
    public double getTrueCourse() {
        return trueCourse;
    }

    /**
     * Getter method for the true course.
     *
     * @return true course      True course (in degrees) as a <code>String</code>.
     * @since 1.0
     */
    public String getStrCourse() {
        return strCourse;
    }

    /**
     * Computes the difference in meridional parts for two latitudes in minutes
     * (based on Clark 1880 spheroid).
     *
     * @param lat1              Offset latitude.
     * @param lat2              Destination latitude.
     * @return                  Difference in minutes.
     * @since 1.0
     */
    public static double computeDMPClarkeSpheroid(double lat1, double lat2) {
        double absLat1 = Math.abs(lat1);
        double absLat2 = Math.abs(lat2);

        double m1 = (7915.704468 * (Math.log(Math.tan(Math.toRadians(45
                + (absLat1 / 2)))) / Math.log(10))
                - 23.268932 * Math.sin(Math.toRadians(absLat1))
                - 0.052500 * Math.pow(Math.sin(Math.toRadians(absLat1)), 3)
                - 0.000213 * Math.pow(Math.sin(Math.toRadians(absLat1)), 5));

        double m2 = (7915.704468 * (Math.log(Math.tan(Math.toRadians(45
                + (absLat2 / 2)))) / Math.log(10))
                - 23.268932 * Math.sin(Math.toRadians(absLat2))
                - 0.052500 * Math.pow(Math.sin(Math.toRadians(absLat2)), 3)
                - 0.000213 * Math.pow(Math.sin(Math.toRadians(absLat2)), 5));
        if ((lat1 <= 0 && lat2 <= 0) || (lat1 > 0 && lat2 > 0)) {
            return Math.abs(m1 - m2);
        } else {
            return m1 + m2;
        }
    }

    /**
     * Predicts the position of a target for a given time in the future.
     * 
     * @param time              The number of seconds from now for which to 
     *                          predict the future position.
     *
     * @param speed             The miles per minute that at which the target is traveling
     * @param currentLat        The target's current latitude.
     * @param currentLong       The target's current longitude.
     * @param course            The target's current course in degrees.
     * @return                  The predicted future <code>Position</code>.
     * @since 1.0
     */
    public static Position predictPosition(int time, double speed,
            double currentLat, double currentLong, double course) {
        Position futurePosition = null;
        course = Math.toRadians(course);
        double futureLong = currentLong + speed * time * Math.sin(course);
        double futureLat = currentLat + speed * time * Math.cos(course);
        try {
            futurePosition = new Position(futureLat, futureLong);
        } catch (InvalidPositionException ipe) {
            ipe.printStackTrace();
        }
        return futurePosition;

    }

    /**
     * Computes the coordinate of position B relative to an offset given
     * a distance and an angle.
     *
     * @param offset        The offset position.
     * @param bearing       The bearing between the offset and the coordinate
     *                      that you want to calculate.
     * @param distance      The distance, in meters, between the offset
     *                      and point B.
     * @return              The position of point B that is located from
     *                      given offset at given distance and angle.
     * @since 1.0
     */
    public static Position computePosition(Position initialPos, double heading,
            double distance) {
        if (initialPos == null) {
            return null;
        }
        double angle;
        if (heading < 90) {
            angle = heading;
        } else if (heading > 90 && heading < 180) {
            angle = 180 - heading;
        } else if (heading > 180 && heading < 270) {
            angle = heading - 180;
        } else {
            angle = 360 - heading;
        }

        Position newPosition = null;

        // Convert meters into nautical miles
        distance = distance * 0.000539956803;
        angle = Math.toRadians(angle);
        double initialLat = initialPos.getLatitude();
        double initialLong = initialPos.getLongitude();
        double dlat = distance * Math.cos(angle);
        dlat = dlat / 60;
        dlat = Math.abs(dlat);
        double newLat = 0;
        if ((heading > 270 && heading < 360) || (heading > 0 && heading < 90)) {
            newLat = initialLat + dlat;
        } else if (heading < 270 && heading > 90) {
            newLat = initialLat - dlat;
        }
        double meanLat = (Math.abs(dlat) / 2.0) + newLat;
        double dep = (Math.abs(dlat * 60)) * Math.tan(angle);
        double dlong = dep * (1.0 / Math.cos(Math.toRadians(meanLat)));
        dlong = dlong / 60;
        dlong = Math.abs(dlong);
        double newLong;
        if (heading > 180 && heading < 360) {
            newLong = initialLong - dlong;
        } else {
            newLong = initialLong + dlong;
        }

        if (newLong < -180) {
            double diff = Math.abs(newLong + 180);
            newLong = 180 - diff;
        }

        if (newLong > 180) {
            double diff = Math.abs(newLong + 180);
            newLong = (180 - diff) * -1;
        }

        if (heading == 0 || heading == 360 || heading == 180) {
            newLong = initialLong;
            newLat = initialLat + dlat;
        } else if (heading == 90 || heading == 270) {
            newLat = initialLat;
//            newLong = initialLong + dlong; THIS WAS THE ORIGINAL (IT WORKED)
            newLong = initialLong - dlong;
        }
        try {
            newPosition = new Position(newLat,
                    newLong);
        } catch (InvalidPositionException ipe) {
            ipe.printStackTrace();
            System.out.println(newLat + "," + newLong);
        }
        return newPosition;
    }

    /**
     * Computes the difference in Longitude between two positions and assigns the
     * correct sign -westwards travel, + eastwards travel.
     * 
     * @param lng1                      Offset longitude.
     * @param lng2                      Destination longitude.
     * @return                          Difference in longitude.
     * @since 1.0
     */
    public static double computeDLong(double lng1, double lng2) {
        if (lng1 - lng2 == 0) {
            return 0;
        }

        // both easterly
        if (lng1 >= 0 & lng2 >= 0) {
            return -(lng1 - lng2) * 60;
        }
        //both westerly
        if (lng1 < 0 & lng2 < 0) {
            return -(lng1 - lng2) * 60;
        }

        //opposite sides of Date line meridian

        //sum less than 180
        if (Math.abs(lng1) + Math.abs(lng2) < 180) {
            if (lng1 < 0 & lng2 > 0) {
                return -(Math.abs(lng1) + Math.abs(lng2)) * 60;
            } else {
                return Math.abs(lng1) + Math.abs(lng2) * 60;
            }
        } else {
            //sum greater than 180
            if (lng1 < 0 & lng2 > 0) {
                return -(360 - (Math.abs(lng1) + Math.abs(lng2))) * 60;
            } else {
                return (360 - (Math.abs(lng1) + Math.abs(lng2))) * 60;
            }
        }
    }

    /**
     * Computes the angle between two points.
     *
     * @param p1
     * @param p2
     * @return
     * @since 2.0
     */
    public static int computeAngle(Position p1, Position p2) {
        // cos (adj / hyp)
        double adj = Math.abs(p1.getLongitude() - p2.getLongitude());
        double opp = Math.abs(p1.getLatitude() - p2.getLatitude());
        return (int) Math.toDegrees(Math.atan(opp / adj));

//        int angle = (int)Math.atan2(p2.getLatitude() - p1.getLatitude(),
//                p2.getLongitude() - p1.getLongitude());
        //Actually it's ATan2(dy , dx) where dy = y2 - y1 and dx = x2 - x1, or ATan(dy / dx)
    }

    /**
     *
     * @param p1
     * @param p2
     * @return
     *
     * @since 2.0
     */
    public static int computeHeading(Position p1, Position p2) {
        int angle = computeAngle(p1, p2);
        // NE
        if (p2.getLongitude() >= p1.getLongitude() && p2.getLatitude() >= p1.getLatitude()) {
            return angle;
        } else if (p2.getLongitude() >= p1.getLongitude() && p2.getLatitude() <= p1.getLatitude()) {
            // SE
            return 90 + angle;
        } else if (p2.getLongitude() <= p1.getLongitude() && p2.getLatitude() <= p1.getLatitude()) {
            // SW
            return 270 - angle;
        } else {
            // NW
            return 270 + angle;
        }
    }

    /**
     * Computes the difference in Longitude between two positions and assigns the
     * correct sign -westwards travel, + eastwards travel.
     *
     * @param lng1          Offset longitude.
     * @param lng2          Destination longitude.
     * @return              Difference in longitude.
     * @since 1.0
     */
    public static double computeLongDiff(double lng1, double lng2) {
        if (lng1 - lng2 == 0) {
            return 0;
        }

        // both easterly
        if (lng1 >= 0 & lng2 >= 0) {
            return Math.abs(-(lng1 - lng2) * 60);
        }
        //both westerly
        if (lng1 < 0 & lng2 < 0) {
            return Math.abs(-(lng1 - lng2) * 60);
        }

        if (lng1 == 0) {
            return Math.abs(lng2 * 60);
        }

        if (lng2 == 0) {
            return Math.abs(lng1 * 60);
        }

        return (Math.abs(lng1) + Math.abs(lng2)) * 60;
    }

    /**
     * Compute the difference in latitude between two positions.
     *
     * @param lat1          Offset latitude.
     * @param lat2          Destination latitude.
     * @return              Difference in latitude.
     * @since 1.0
     */
    public static double computeDLat(double lat1, double lat2) {
        //same side of equator

        //plane sailing
        if (lat1 - lat2 == 0) {
            return 0;
        }

        //both northerly
        if (lat1 >= 0 & lat2 >= 0) {
            return -(lat1 - lat2) * 60;
        }
        //both southerly
        if (lat1 < 0 & lat2 < 0) {
            return -(lat1 - lat2) * 60;
        }

        //opposite sides of equator
        if (lat1 >= 0) {
            //heading south
            return -(Math.abs(lat1) + Math.abs(lat2));
        } else {
            //heading north
            return (Math.abs(lat1) + Math.abs(lat2));
        }
    }

    /**
     * Converts meters to degrees.
     *
     * @param meters            The meters that you want to convert into degrees.
     * @return                  The degree equivalent of the given meters.
     * @since 1.0
     */
    public static double toDegrees(double meters) {
        return (meters / METERS_PER_MINUTE) / 60;
    }

    /**
     * Compute the intersection between two line segments, or two lines
     * of infinite length.
     *
     * NB: As noted in the documentation, the following method is not written
     * by the author (Benjamin Jakobus) but is based on a Java API for geometrical
     * calculations in Java released under an LGPL license by
     * "Geotechnical Software Services".
     *
     * @param  x0               X coordinate first end point first line segment.
     * @param  y0               Y coordinate first end point first line segment.
     * @param  x1               X coordinate second end point first line segment.
     * @param  y1               Y coordinate second end point first line segment.
     * @param  x2               X coordinate first end point second line segment.
     * @param  y2               Y coordinate first end point second line segment.
     * @param  x3               X coordinate second end point second line segment.
     * @param  y3               Y coordinate second end point second line segment.
     * @param  intersection[2]  Preallocated by caller to double[2]
     * @return                  -1 if lines are parallel (x,y unset),
     *                          -2 if lines are parallel and overlapping (x, y center)
     *                          0 if intesrection outside segments (x,y set)
     *                          +1 if segments intersect (x,y set)
     * @since 1.0
     */
    public static int findLineSegmentIntersection(double x0, double y0,
            double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            double[] intersection) {
        // TODO: Make limit depend on input domain
        final double LIMIT = 1e-5;
        final double INFINITY = 1e10;

        double x, y;

        //
        // Convert the lines to the form y = ax + b
        //

        // Slope of the two lines
        double a0 = equals(x0, x1, LIMIT)
                ? INFINITY : (y0 - y1) / (x0 - x1);
        double a1 = equals(x2, x3, LIMIT)
                ? INFINITY : (y2 - y3) / (x2 - x3);

        double b0 = y0 - a0 * x0;
        double b1 = y2 - a1 * x2;

        // Check if lines are parallel
        if (equals(a0, a1)) {
            if (!equals(b0, b1)) {
                return -1; // Parallell non-overlapping
            } else {
                if (equals(x0, x1)) {
                    if (Math.min(y0, y1) < Math.max(y2, y3)
                            || Math.max(y0, y1) > Math.min(y2, y3)) {
                        double twoMiddle = y0 + y1 + y2 + y3
                                - min(y0, y1, y2, y3)
                                - max(y0, y1, y2, y3);
                        y = (twoMiddle) / 2.0;
                        x = (y - b0) / a0;
                    } else {
                        return -1;  // Parallell non-overlapping
                    }
                } else {
                    if (Math.min(x0, x1) < Math.max(x2, x3)
                            || Math.max(x0, x1) > Math.min(x2, x3)) {
                        double twoMiddle = x0 + x1 + x2 + x3
                                - min(x0, x1, x2, x3)
                                - max(x0, x1, x2, x3);
                        x = (twoMiddle) / 2.0;
                        y = a0 * x + b0;
                    } else {
                        return -1;
                    }
                }

                intersection[0] = x;
                intersection[1] = y;
                return -2;
            }
        }

        // Find correct intersection point
        if (equals(a0, INFINITY)) {
            x = x0;
            y = a1 * x + b1;
        } else if (equals(a1, INFINITY)) {
            x = x2;
            y = a0 * x + b0;
        } else {
            x = -(b0 - b1) / (a0 - a1);
            y = a0 * x + b0;
        }

        intersection[0] = x;
        intersection[1] = y;

        // Then check if intersection is within line segments
        double distanceFrom1;
        if (equals(x0, x1)) {
            if (y0 < y1) {
                distanceFrom1 = y < y0 ? length(x, y, x0, y0)
                        : y > y1 ? length(x, y, x1, y1) : 0.0;
            } else {
                distanceFrom1 = y < y1 ? length(x, y, x1, y1)
                        : y > y0 ? length(x, y, x0, y0) : 0.0;
            }
        } else {
            if (x0 < x1) {
                distanceFrom1 = x < x0 ? length(x, y, x0, y0)
                        : x > x1 ? length(x, y, x1, y1) : 0.0;
            } else {
                distanceFrom1 = x < x1 ? length(x, y, x1, y1)
                        : x > x0 ? length(x, y, x0, y0) : 0.0;
            }
        }

        double distanceFrom2;
        if (equals(x2, x3)) {
            if (y2 < y3) {
                distanceFrom2 = y < y2 ? length(x, y, x2, y2)
                        : y > y3 ? length(x, y, x3, y3) : 0.0;
            } else {
                distanceFrom2 = y < y3 ? length(x, y, x3, y3)
                        : y > y2 ? length(x, y, x2, y2) : 0.0;
            }
        } else {
            if (x2 < x3) {
                distanceFrom2 = x < x2 ? length(x, y, x2, y2)
                        : x > x3 ? length(x, y, x3, y3) : 0.0;
            } else {
                distanceFrom2 = x < x3 ? length(x, y, x3, y3)
                        : x > x2 ? length(x, y, x2, y2) : 0.0;
            }
        }

        return equals(distanceFrom1, 0.0)
                && equals(distanceFrom2, 0.0) ? 1 : 0;
    }

    /**
     * Return the length of a vector.
     *
     * @param v  Vector to compute length of [x,y,z].
     * @return   Length of vector.
     */
    public static double length(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    /**
     * Compute the length of the line from (x0,y0) to (x1,y1)
     *
     * @param x0, y0  First line end point.
     * @param x1, y1  Second line end point.
     * @return        Length of line from (x0,y0) to (x1,y1).
     */
    public static double length(int x0, int y0, int x1, int y1) {
        return length((double) x0, (double) y0,
                (double) x1, (double) y1);
    }

    /**
     * Compute the length of the line from (x0,y0) to (x1,y1)
     *
     * @param x0, y0  First line end point.
     * @param x1, y1  Second line end point.
     * @return        Length of line from (x0,y0) to (x1,y1).
     */
    public static double length(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Compute the length of a polyline.
     *
     * @param x, y     Arrays of x,y coordinates
     * @param nPoints  Number of elements in the above.
     * @param isClosed True if this is a closed polygon, false otherwise
     * @return         Length of polyline defined by x, y and nPoints.
     */
    public static double length(int[] x, int[] y, boolean isClosed) {
        double length = 0.0;

        int nPoints = x.length;
        for (int i = 0; i < nPoints - 1; i++) {
            length += length(x[i], y[i], x[i + 1], y[i + 1]);
        }

        // Add last leg if this is a polygon
        if (isClosed && nPoints > 1) {
            length += length(x[nPoints - 1], y[nPoints - 1], x[0], y[0]);
        }

        return length;
    }

    /**
     * Check if two double precision numbers are "equal", i.e. close enough
     * to a given limit.
     *
     * @param a      First number to check
     * @param b      Second number to check
     * @param limit  The definition of "equal".
     * @return       True if the two numbers are "equal", false otherwise
     */
    private static boolean equals(double a, double b, double limit) {
        return Math.abs(a - b) < limit;
    }

    /**
     * Check if two double precision numbers are "equal", i.e. close enough
     * to a pre-specified limit.
     *
     * @param a  First number to check
     * @param b  Second number to check
     * @return   True if the twho numbers are "equal", false otherwise
     */
    private static boolean equals(double a, double b) {
        return equals(a, b, 1.0e-5);
    }

    /**
     * Return smallest of four numbers.
     *
     * @param a  First number to find smallest among.
     * @param b  Second number to find smallest among.
     * @param c  Third number to find smallest among.
     * @param d  Fourth number to find smallest among.
     * @return   Smallest of a, b, c and d.
     */
    private static double min(double a, double b, double c, double d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    /**
     * Return largest of four numbers.
     *
     * @param a  First number to find largest among.
     * @param b  Second number to find largest among.
     * @param c  Third number to find largest among.
     * @param d  Fourth number to find largest among.
     * @return   Largest of a, b, c and d.
     * @since 1.0
     */
    private static double max(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    /**
     * Computes the bearing between two points.
     * 
     * @param p1            First <code>Position</code>.
     * @param p2            Second <code>Position</code>.
     * @return              Bearing between two points.
     * @since 1.0
     */
    public static int computeBearing(Position p1, Position p2) {
        int bearing;
        double dLon = computeDLong(p1.getLongitude(), p2.getLongitude());
        double y = Math.sin(dLon) * Math.cos(p2.getLatitude());
        double x = Math.cos(p1.getLatitude()) * Math.sin(p2.getLatitude())
                - Math.sin(p1.getLatitude()) * Math.cos(p2.getLatitude()) * Math.cos(dLon);
        bearing = (int) Math.toDegrees(Math.atan2(y, x));
        return bearing;
    }

    public static double mbarToHG(double mbar) {
        return mbar / 33.8639;
    }

    public static double hgToMeters(double hg) {
        return hg / 3.280839895;
    }
}

