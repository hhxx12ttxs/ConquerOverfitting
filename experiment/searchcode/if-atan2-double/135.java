/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleboat.util;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;
import simpleboat.enumeration.PointOfSail;

/**
 *
 * @author lars-win7
 */
public class Util {

    /**
     * Calculates the difference between two angles in degree.
     * Abs means values between 0 and 180.
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

    /**
     * Calculates the difference between two angles in degree.
     * Signed means values between -180 and 180.
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

    /**
     * Calculates the angle between 2 points, whereas the angle is between
     * 0 and 360 degree and the order of the points has an effect.
     * @param currentPoint the point of the boat.
     * @param targetPoint the point of the next waypoint.
     * @return the angle between the 2 points in degree (0-360).
     */
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
     * Calculates a new waypoint as a stopover, if the boat has to steer against the wind.
     * @param currentPoint the current position of the boat.
     * @param targetPoint the target point of the boat.
     * @param differenceAngle the angle between the target angle and the wind angle.
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
     * Calculates the arithmetic average of a given list containing double values.
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
     * direction and the relative wind direction (dependent on the boat direction).
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
     * Calculates the absolute wind based on the heading of the boat and
     * the relative wind direction.
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
     * Calculates the pitch angle based on the accelerometer and the
     * roll angle.
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
     * Calculates the yaw angle (compass) based on the magnetometer, the
     * roll and the pitch angle.
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


}

