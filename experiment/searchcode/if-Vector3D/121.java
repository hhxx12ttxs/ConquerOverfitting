/*
 * Navigation.java
 * 
 * Project: RobSail2012
 * Package: map.util
 * Last update: 16.04.2012
 * 
 * Contact: Alexander Schlaefer (schlaefer@rob.uni-luebeck.de)
 *          Nikolaus Ammann (ammann@rob.uni-luebeck.de)
 * 
 * Copyright 2012 Institute for Robotics and Cognitive Systems.
 */

package map.util;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

/**
 * @author Administrator
 */
public class Navigation {
    
    public static double getAngleDiff(double a1, double a2) {
        double result = ((a2 - a1) + 360) % 360;
        if (result > 180) {
            result -= 360;
        }
        return result;
    }
    
    public static double getCourse(Vector2d v) {
        return (360d + Math.toDegrees(Math.atan2(v.x, v.y))) % 360d;
    }
    
    public static Vector2d getSpeedVector(double angle, double length) {
        // System.out.println("\t\t\t" + angle + "\t" + Math.toRadians(-angle +
        // 90) + "\t" + Math.sin(Math.toRadians(-angle + 90)));
        double x = Math.cos(Math.toRadians(-angle + 90)) * length; // -angle b/c
                                                                   // of math
                                                                   // direction,
                                                                   // north up
        double y = Math.sin(Math.toRadians(-angle + 90)) * length; // -angle b/c
                                                                   // of math
                                                                   // direction,
                                                                   // north up
        return new Vector2d(x, y);
    }
    
    public static double getWindDirection(double x, double y) {
        double tmp = Math.toDegrees(Math.atan2(-y, -x));
        return (360 - (tmp - 90)) % 360;
    }
    
    public static double getWindDirection(Vector2d v) {
        return Navigation.getWindDirection(v.x, v.y);
    }
    
    public static Vector2d getWindVector(double angle, double length) {
        double x = Math.cos(Math.toRadians(-angle + 90)) * length * (-1); // -angle
                                                                          // b/c
                                                                          // of
                                                                          // math
                                                                          // direction
        double y = Math.sin(Math.toRadians(-angle + 90)) * length * (-1); // -angle
                                                                          // b/c
                                                                          // of
                                                                          // math
                                                                          // direction
        return new Vector2d(x, y);
    }
    
    // does the OTHER boat have the right of way?
    public static boolean hasRightOfWay(Vector2d wind, Vector2d thisCourse, Point2d thisPos, Vector2d otherCourse,
                                        Point2d otherPos) {
        return ((Navigation.isStarboard(wind, otherCourse) && !Navigation.isStarboard(wind, thisCourse)) || ((Navigation.isStarboard(wind,
                                                                                                                                     otherCourse) == Navigation.isStarboard(wind,
                                                                                                                                                                            thisCourse)) && Navigation.isLeeward(wind,
                                                                                                                                                                                                                 thisCourse,
                                                                                                                                                                                                                 thisPos,
                                                                                                                                                                                                                 otherPos)));
    }
    
    public static boolean isLeeward(Vector2d wind, Vector2d thisCourse, Point2d thisPos, Point2d otherPos) {
        Vector3d vDC = new Vector3d();
        Vector3d vWC = new Vector3d();
        Vector3d vC = new Vector3d(thisCourse.x, thisCourse.y, 0); // course
                                                                   // vector
        Vector3d vD = new Vector3d(otherPos.x - thisPos.x, otherPos.y - thisPos.y, 0); // vector
                                                                                       // to
                                                                                       // other
                                                                                       // boat
        Vector3d vW = new Vector3d(wind.x, wind.y, 0); // wind vector
        vDC.cross(vD, vC);
        vWC.cross(vW, vC);
        return (Math.signum(vDC.z) == Math.signum(vWC.z)); // if both vectors
                                                           // point same
                                                           // hemisphere with
                                                           // respect to course,
                                                           // it leeward
    }
    
    // not implemented, should be okay if both go same direction the normal
    // obstacle is sufficient
    // public static boolean isAhead(Vector2d wind, Point2d thisPos, Point2d
    // otherPos) {
    // Vector3d v = new Vector3d();
    // v.cross(new Vector3d(wind.x, wind.y,0), new Vector3d(, course.y, 0));
    // return (v.z < 0);
    // }
    
    public static boolean isStarboard(Vector2d wind, Vector2d course) {
        Vector3d v = new Vector3d();
        v.cross(new Vector3d(wind.x, wind.y, 0), new Vector3d(course.x, course.y, 0));
        return (v.z < 0);
    }
    
    public static Vector2d rotate(Vector2d vector, double angle) {
        double a = Math.toRadians(-angle); // - b/c of math direction
        Matrix3d r = new Matrix3d();
        r.m00 = Math.cos(a);
        r.m01 = -Math.sin(a);
        r.m10 = Math.sin(a);
        r.m11 = Math.cos(a);
        Matrix3d v = new Matrix3d();
        v.m02 = vector.x;
        v.m12 = vector.y;
        r.mul(v);
        return new Vector2d(r.m02, r.m12);
    }
    
    public static double signum(double d) {
        if (Math.signum(d) < 0)
            return -1;
        else
            return 1;
    }
    
    public static double to180(double angle) {
        double result = angle;
        if (result > 180d) {
            result = 360d - result;
        }
        return result;
    }
    
}

