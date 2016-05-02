/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boat.util;

import boat.planning.CircularObstacle;
import boat.planning.cardiff.AStar.Node;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

/**
 *
 * @author lars-win7
 */
public class Util {

    /**
     * Calculates the difference between two angles in degree. Abs means values
     * between 0 and 180.
     *
     * @param angle1 the first angle.
     * @param angle2 the second angle.
     * @return the difference between the two angles in degree.
     */
    public static double calcAngleDifferenceAbs(double angle1, double angle2) {
        double difference = 0;
        if (angle1 > angle2) {
            difference = Math.min(angle1 - angle2, angle2 + 360 - angle1);
        } else {
            difference = Math.min(angle2 - angle1, angle1 + 360 - angle2);
        }
        return difference;
    }

    public static double absoluteAngularDifference(double angle1, double angle2) {
        return calcAngleDifferenceAbs(angle1, angle2);
    }

    /**
     * Calculates the difference between two angles in degree. Signed means
     * values between -180 and 180.
     *
     * @param targetAngle the target angle.
     * @param currentAngle the current angle.
     * @return the difference between the two angles in degree.
     */
    public static double calcAngleDifferenceSigned(double targetAngle, double currentAngle) {
        double angleDifference = 0;
        if (targetAngle > currentAngle) {
            if ((targetAngle - currentAngle) < ((currentAngle + 360) - targetAngle)) {
                angleDifference = targetAngle - currentAngle; // Turn right
            } else {
                angleDifference = -((currentAngle + 360) - targetAngle); // Turn left
            }
        } else {
            if ((currentAngle - targetAngle) < ((targetAngle + 360) - currentAngle)) {
                angleDifference = -(currentAngle - targetAngle); // Turn left
            } else {
                angleDifference = (targetAngle + 360) - currentAngle; // Turn right
            }
        }
        return angleDifference;
    }

    public static double angularDifferenceSignend(double targetAngle,
            double currentAngle) {
        return calcAngleDifferenceSigned(targetAngle, currentAngle);
    }

    /**
     * Calculates the angle between 2 points, whereas the angle is between 0 and
     * 360 degree and the order of the points has an effect.
     *
     * @param currentPoint the point of the boat.
     * @param targetPoint the point of the next waypoint.
     * @return the angle between the 2 points in degree (0-360).
     * @deprecated use <code>calculateAngleTwoPointsDeg</code> instead
     */
    @Deprecated 
    public static double calcAngleBetweenTwoPoints(Tuple2d currentPoint,
            Tuple2d targetPoint) {
        double targetX = targetPoint.x;
        double targetY = targetPoint.y;
        double currentX = currentPoint.x;
        double currentY = currentPoint.y;
        double differenceAngleRad = 0;
        double differenceX = targetX - currentX;
        double differenceY = targetY - currentY;
        if (differenceX >= 0 && differenceY > 0) {
            // targetAngle = [0-90]
            differenceAngleRad = Math.atan(differenceX / differenceY);
        } else if (differenceX <= 0 && differenceY > 0) {
            // targetAngle = [270-360]
            differenceAngleRad = Math.atan(differenceX / differenceY)
                    + 2 * Math.PI;
        } else {
            // targetAngle = [90-270]
            differenceAngleRad = Math.atan(differenceX / differenceY)
                    + Math.PI;
        }
        double targetAngleDegree = Math.toDegrees(differenceAngleRad);
        return targetAngleDegree;
    }
    
    /**
     * Calculates the angle between 2 points, where thea angle is between 0 and
     * 2*Pi. The order of the points has an effect.
     * @param currentPoint the point of the boat
     * @param target point of the next waypoint
     * @return 
     */
    public static double calculteAngleTwoPointsRad(Tuple2d currentPoint, Tuple2d target){
        final double deltaX = target.x - currentPoint.x;
        final double deltaY = target.y - currentPoint.y;
        double angle = Math.atan2(deltaX, deltaY);
        if (angle < 0){
            angle += 2*Math.PI;
        }
        return angle;
    }
    
    /**
     * Calculates the angle between 2 points, where thea angle is between 0 and
     * 360 degree. The order of the points has an effect.
     * @param currentPoint the point of the boat
     * @param target point of the next waypoint
     * @return 
     */
    public static double calcAngleTwoPointsDeg(Tuple2d currentPoint, Tuple2d target){
        return Math.toDegrees(calculteAngleTwoPointsRad(currentPoint, target));
    }
    

    /**
     * Calculates a new waypoint as a stopover, if the boat has to steer against
     * the wind.
     *
     * @param currentPoint the current position of the boat.
     * @param targetPoint the target point of the boat.
     * @param differenceAngle the angle between the target angle and the wind
     * angle.
     * @return the stopover waypoint.
     * @deprecated use calcCrosspoint instead
     */
    @Deprecated
    public static Point2d calcNextWaypointTacking(Point2d boatPosition, Point2d targetPosition, double differenceAngle) {
        Point2d newWaypoint = new Point2d(); // stopover waypoint
        Vector2D boatVector = new Vector2D(boatPosition); //vector of boat (extracted from coordinates)
        Vector2D targetVector = new Vector2D(targetPosition); //vector of target ( -''- )
        Vector2D distanceVector = Vector2D.createVectorAB(boatVector, targetVector); //calculates vector from position to target
        final double alpha = 45 - differenceAngle; // angle in triangle (boatPos, targetPos, newWaypoint), between distanceVector and newWaypointVector
        Vector2D orthogonalVector;
        if (differenceAngle >= 0) { // if target is to right of wind direction
            orthogonalVector = Vector2D.createOrthogonalVector(distanceVector, true);
        } else {
            orthogonalVector = Vector2D.createOrthogonalVector(distanceVector, false);
        }
        final double hypotenuse = distanceVector.getLength();
        final double alphaRad = Math.toRadians(alpha);
        // sin(a) = opposite / hypotenuse
        final double opposite = Math.sin(alphaRad) * hypotenuse;
        //        // a^2 + b^2 = c^2, opposite^2 + adjacent^2 = hypotenuse^2
        //        double adjacent = Math.sqrt(Math.pow(hypotenuse, 2) - Math.pow(opposite, 2));
        //cos(a) = adjacent / hypotenuse
        final double adjacent = Math.cos(alphaRad) * hypotenuse;
        // "Kathetensatz von Euklid" a^2 = c * p, b^2 = c * q, c = p + q
        double p = Math.pow(adjacent, 2) / hypotenuse; //adjacent^2 = hypotenuse * p
        double q = Math.pow(opposite, 2) / hypotenuse; //opposite^2 = hypotenuse * p
        // DEBUG
        double c = p + q;
//        System.out.println("p + q = " + c + " - hypotenuse: " + hypotenuse);
        // DEBUG end
        // "Hoehensatz von Euklid" h^2 = p * q
        double heightHyp = Math.sqrt(p * q);

        //apply values to vector lengths to get stopover coordinates
        double factorDistanceVector = p / hypotenuse;
        distanceVector.scale(factorDistanceVector);
        double factorOrthogonalVector = heightHyp / orthogonalVector.getLength();
        orthogonalVector.scale(factorOrthogonalVector);
        Vector2D stopover = Vector2D.vectorAddition(boatVector, distanceVector);
        stopover = Vector2D.vectorAddition(stopover, orthogonalVector);

        newWaypoint = new Point2d(stopover.x, stopover.y);
        return newWaypoint;
    }

    /**
     *
     */
    public static LinkedList<Point2d> calcBuoyStopOvers(Point2d currentPoint, Point2d buoyPoint) {
        LinkedList<Point2d> newWaypoints = new LinkedList<Point2d>();
        Vector2d boatVector = new Vector2d(currentPoint.x, currentPoint.y);
        Vector2d northVector = new Vector2d(0, 1);
        double angle = Math.toDegrees(boatVector.angle(northVector)); //returns angle between position and north vector in radians [0,PI]
        System.out.println("Angle: " + angle);

        return newWaypoints;
    }

    /**
     * Calculates the arithmetic average of a given list containing double
     * values.
     *
     * @param list
     * @return
     */
    public static double calcAverage(Collection<Double> list) {
        double sum = 0;
        for (Double value : list) {
            sum += value;
        }
        return (sum / list.size());
    }

    /**
     * Calculates the variance of a given list containing double values.
     *
     * @param list
     * @return
     */
    public static double calcVariance(Collection<Double> list) {
        double variance = 0;
        double average = calcAverage(list);
        for (Double value : list) {
            variance += Math.pow((value - average), 2);
        }
        return (variance / (list.size() - 1));
    }

    /**
     * Calculates the heading of the boat based on the absolute (global) wind
     * direction and the relative wind direction (dependent on the boat
     * direction).
     *
     * @param relativeWind the relative wind direction in degree (0-360).
     * @param absoluteWind the absolute wind direction in degree (0-360).
     * @return the heading of the boat in degree (0-360).
     */
    public static double calcHeading(double relativeWind, double absoluteWind) {
        double heading = 0;
        heading = absoluteWind - relativeWind;
        if (heading < 0) {
            heading += 360;
        }
        return heading;
    }

    /**
     * Calculates the absolute wind based on the heading of the boat and the
     * relative wind direction.
     *
     * @param relativeWind the relative wind direction in degree (0-360).
     * @param heading the heading of the boat in degree (0-360).
     * @return the absolute wind direction in degree (0-360).
     */
    public static double calcAbsoluteWind(double relativeWind, double heading) {
        double absoluteWind = 0;
        absoluteWind = (heading + relativeWind) % 360;
        return absoluteWind;
    }

    /**
     * Calculates the angle of the compass (yaw) based on the raw data of the
     * magnetometer (x and y).
     *
     * @param magX the x-axis of the magnetometer.
     * @param magY the y-axis of the magnetometer.
     * @return the angle (yaw) in degree 0-360.
     */
    public static double calcCompassAngle(double magX, double magY) {
        double angleRad = -Math.atan2(magY, magX);
        double angleDegree = Math.toDegrees(angleRad);
        if (angleDegree < 0) {
            angleDegree = 360 + angleDegree;
        }
        return angleDegree;
    }

    /**
     * Calculates the median of a given list.
     *
     * @param list the given list.
     * @return the median of that list.
     */
    public static double calcMedian(List<Double> list) {
        Collections.sort(list);
        int median = list.size() / 2;
        return list.get(median);
    }

    /**
     * Calculates the roll angle based on the accelerometer.
     *
     * @param accY the y-axis of the accelerometer
     * @param accZ the z-axis of the accelerometer
     * @return the roll angle in degrees (-90,90)
     */
    public static double calcRoll(double accY, double accZ) {
        double roll = Math.atan(accY / accZ);
        roll = Math.toDegrees(roll);
        return roll;
    }

    /**
     * Calculates the pitch angle based on the accelerometer and the roll angle.
     *
     * @param accX the x-axis of the accelerometer.
     * @param accY the y-axis of the accelerometer.
     * @param accZ the z-axis of the accelerometer.
     * @param roll the roll angle in degrees (-90,90).
     * @return the pitch angle in degrees (-90,90).
     */
    public static double calcPitch(double accX, double accY, double accZ, double roll) {
        roll = Math.toRadians(roll);
        double y = -accX;
        double x = accY * Math.sin(roll) + accZ * Math.cos(roll);
        double pitch = Math.atan(y / x);
        pitch = Math.toDegrees(pitch);
        return pitch;
    }

    /**
     * Calculates the yaw angle (compass) based on the magnetometer, the roll
     * and the pitch angle.
     *
     * @param magX the x-axis of the magnetometer.
     * @param magY the y-axis of the magnetometer.
     * @param magZ the z-axis of the magnetometer.
     * @param roll the roll angle in degrees (-90,90).
     * @param pitch the pitch angle in degrees (-90,90).
     * @return the yaw angle in degrees (0, 360).
     */
    public static double calcYaw(double magX, double magY, double magZ, double roll, double pitch) {
        roll = Math.toRadians(roll);
        pitch = Math.toRadians(pitch);
        double y = magZ * Math.sin(roll) - magY * Math.cos(roll);
        double x = magX * Math.cos(pitch) + magY * Math.sin(pitch)
                * Math.sin(roll) + magZ * Math.sin(pitch) * Math.cos(roll);
        double yaw = Math.atan2(y, x);
        yaw = Math.toDegrees(yaw);
        if (yaw < 0) {
            yaw += 360;
        }
        return yaw;
    }

    public static boolean smoothDoubleComparison(double d1, double d2, double margin) {
        double d2LSmall = d2 - margin;
        double d2LBig = d2 + margin;
        return (d1 > d2LSmall && d1 < d2LBig);
    }

    public static boolean calcDistribution(List<Double> list, double min, double percentage) {
        double countPos = 0;
        double countNeg = 0;
        for (Double angle : list) {
            if (angle > 0 && angle > min) {
                countPos++;
            } else if (angle < 0 && angle < -min) {
                countNeg++;
            }
        }
        if ((countPos / (double) list.size()) > percentage) {
            if ((countNeg / (double) list.size()) > percentage) {
                return true;
            }
        }
        return false;
    }

    public static boolean calcDistributionForWind(List<Double> list, double threshold, double percentage) {
        int counter = 0;
        double min = list.size() * percentage;
        for (double angle : list) {
            if (angle < threshold || angle > (360 - threshold)) {
                counter++;
            }
        }
        return (counter > min);
    }

    public static boolean calcDistributionForCrossWind(List<Double> list, double threshold, double percentage) {
        int counter = 0;
        double min = list.size() * percentage;
        for (double angle : list) {
            if (angle > (90 - threshold) && angle < (90 + threshold)) {
                counter++;
            } else if (angle > (270 - threshold) && angle < (270 + threshold)) {
                counter++;
            }
        }
        return (counter >= min);
    }

    public static boolean calcDistributionBackWind(List<Double> list, double threshold, double percentage) {
        int counter = 0;
        double min = list.size() * percentage;
        for (double angle : list) {
            if (angle < 180 + threshold && angle > (180 - threshold)) {
                counter++;
            }
        }
        return (counter > min);
    }

    public static boolean countPosNeg(List<Double> list, int min) {
        int count = 0;
        for (int i = 0; i < list.size() - 1; i++) {
            if (((list.get(i) < 0) && (list.get(i + 1) > 0))
                    || ((list.get(i) > 0) && list.get(i + 1) < 0)) {
                count++;
            }
        }
        return count >= min;
    }

    public static boolean calcDistributionSmaller(List<Double> list, double threshold, double percentage) {
        int counter = 0;
        double min = list.size() * percentage;
        for (double value : list) {
            if (value < threshold) {
                counter++;
            }
        }
        return (counter > min);
    }

    public static boolean calcDistributionGreater(List<Double> list, double threshold, double percentage) {
        int counter = 0;
        double min = list.size() * percentage;
        for (double value : list) {
            if (value > threshold) {
                counter++;
            }
        }
        return (counter > min);
    }

    /**
     * Rotate a point around by a certain angle around a center.
     *
     * @param center the point around which will be rotated
     * @param angleInDegree the rotation angle in degree
     * @param point the point that will be rotated
     * @return the rotated point
     */
    public static Point2D rotatePoint(Point2D center, double angleInDegree,
            Point2D point) {
        double angleInRadians = Math.toRadians(angleInDegree);
        AffineTransform at = new AffineTransform();
        at.rotate(angleInRadians, center.getX(), center.getY());
        at.transform(point, point);
        return point;
    }

    /**
     * Rotate a number of points around by a certain angle around a center.
     *
     * @param center the point around which will be rotated
     * @param angleInDegree the rotation angle in degree
     * @param points the points that will be rotated (array)
     * @return the rotated points
     */
    public static Point2D[] rotatePoints(Point2D center, double angleInDegree,
            Point2D[] points) {
        double angleInRadians = Math.toRadians(angleInDegree);
        AffineTransform at = new AffineTransform();
        at.rotate(angleInRadians, center.getX(), center.getY());
        at.transform(points, 0, points, 0, points.length);
        return points;
    }

    /**
     * Rotate a number of points around by a certain angle around a center.
     *
     * @param center the point around which will be rotated
     * @param angleInDegree the rotation angle in degree
     * @param points the points that will be rotated (list)
     * @return the rotated points
     */
    public static Point2D[] rotatePoints(Point2D center, double angleInDegree,
            List<Point2D> points) {
        Point2D[] pointsArray = points.toArray(new Point2D.Double[points.size()]);
        double angleInRadians = Math.toRadians(angleInDegree);
        AffineTransform at = new AffineTransform();
        at.rotate(angleInRadians, center.getX(), center.getY());
        at.transform(pointsArray, 0, pointsArray, 0, pointsArray.length);
        return pointsArray;
    }

    /**
     * Rotate a number of points around by a certain angle around a center.
     *
     * @param center the point around which will be rotated
     * @param angleInDegree the rotation angle in degree
     * @param points the points that will be rotated (list)
     * @return the rotated points
     */
    public static List<Point2D> rotatePointList(Point2D center, double angleInDegree,
            List<Point2D> points) {
        Point2D[] pointsArray = points.toArray(new Point2D.Double[points.size()]);
        double angleInRadians = Math.toRadians(angleInDegree);
        AffineTransform at = new AffineTransform();
        at.rotate(angleInRadians, center.getX(), center.getY());
        at.transform(pointsArray, 0, pointsArray, 0, pointsArray.length);
        List<Point2D> pointsList = Arrays.asList(pointsArray);
        return pointsList;
    }

    /**
     * Rotate a number of points around by a certain angle around a center.
     *
     * @param center the point around which will be rotated
     * @param angleInDegree the rotation angle in degree
     * @param points the points that will be rotated (list)
     * @return the rotated points
     */
    public static List<Shape> rotateShapes(Point2D center, double angleInDegree,
            List<Shape> shapes) {

        double angleInRadians = Math.toRadians(angleInDegree);
        AffineTransform at = new AffineTransform();
        at.rotate(angleInRadians, center.getX(), center.getY());
        List<Shape> transformedShapes = new ArrayList<Shape>();
        for (Shape shape : shapes) {
            Shape transformedShape = at.createTransformedShape(shape);
            transformedShapes.add(transformedShape);
        }
        return transformedShapes;
    }

    public static List<CircularObstacle> rotateObstacles(Point2D center, double angleInDegree, List<CircularObstacle> obs) {
        double angleInRadians = Math.toRadians(angleInDegree);
        AffineTransform at = new AffineTransform();
        at.rotate(angleInRadians, center.getX(), center.getY());
        for (CircularObstacle co : obs) {
            final Point2d p = co.getPosition();
            final Point2D p2 = new Point2D.Double(p.x, p.y);
            at.transform(p2, p2);
            co.setPosition(new Point2d(p2.getX(), p2.getY()));
        }
        return obs;
    }

    /**
     * Calculates the angle between two points.
     *
     * @param current the current boat (i.e. the boat position)
     * @param target the target point (i.e. the next waypoint)
     * @return the angle between both points
     */
    public static double angleBetweenTwoPoints(Point2d current, Point2d target) {
        return calcAngleBetweenTwoPoints(current, target);
    }

    public static double angleBetweenTwoPoints(Point2D current, Point2D target) {
        Point2d currentPoint2d = new Point2d(current.getX(), current.getY());
        Point2d targetPoint2d = new Point2d(target.getX(), target.getY());
        return calcAngleBetweenTwoPoints(currentPoint2d, targetPoint2d);
    }

    public static Point2D getPoint2D(Point2d point) {
        return new Point2D.Double(point.x, point.y);
    }

    public static Shape getShape(CircularObstacle obstacle) {
        Point2d point = obstacle.getPosition();
        double radius = obstacle.getRadius();
        double diameter = 2d * radius;
        Shape shape = new Ellipse2D.Double(point.x - radius, point.y - radius,
                diameter, diameter);
        return shape;
    }

    public static Line2D getLine2D(Point2d start, Point2d end) {
        Point2D startPoint2D = getPoint2D(start);
        Point2D endPoint2D = getPoint2D(end);
        return new Line2D.Double(startPoint2D, endPoint2D);
    }

    public static List<Point2D> getPoints2D(List<Point2d> points) {
        List<Point2D> list = new ArrayList<Point2D>();
        for (Point2d point : points) {
            list.add(Util.getPoint2D(point));
        }
        return list;
    }
    
    public static List<Point2D> getNodePoints2D(List<Node> points) {
        List<Point2D> list = new ArrayList<Point2D>();
        for (Node point : points) {
            final Point2d pointd = point.getPoint();
            list.add(new Point2D.Double(pointd.x, pointd.y));
        }
        return list;
    }
    
    public static boolean isManeuver(double currentWindDirection, 
            double newWindDirection) {
        if (isPort(currentWindDirection) && isStarboard(newWindDirection)) {
            return true;
        } else if (isStarboard(currentWindDirection) && isPort(newWindDirection)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isPort(double windDirection) {
        if (windDirection > 0 && windDirection < 180) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isStarboard(double windDirection) {
        if (windDirection > 180 && windDirection < 360) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean windIsInIntervall(double windDirection, double min,
            double max) {
        return (windDirection >= min && windDirection < max);
    }
    
    public static Quadrant getQuadrant(double windDir){
        if (windIsInIntervall(windDir, 0, 90)){
            return Quadrant.NW;
        } else if (windIsInIntervall(windDir, 90, 180)){
            return Quadrant.SW;
        } else if (windIsInIntervall(windDir, 180, 270)){
            return Quadrant.SE;
        } else {
            return Quadrant.NE;
        }
    }
    
    public static boolean isClockwise(double currentAngle, double targetAngle) {
        return Util.angularDifferenceSignend(targetAngle, currentAngle) < 0;
    }
    
    public static boolean isCounterClockwise(double currentAngle, double targetAngle) {
        return !isClockwise(currentAngle, targetAngle);
    }
    
    public enum Quadrant {
        NE,SE,SW,NW;
    }
 }

