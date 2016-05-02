/*
 * CourseControl.java
 *
 * Created on May 21, 2010, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package boat.control;

import map.util.*;
import javax.vecmath.*;

import boat.planning.*;

/**
 *
 * @author Alex
 */
public class DirectSailControl implements SailControl {
    
    public static double vFactor = 1d; // sensitivity to speed

    public static double rFactor = 0.5d; // sensitivity to rudder, [0..1]

    public static double mainOffset = 0d;

    public static double jibFactor = 1.0d;

    public static double mainFactor = 1.2d;

    // idea: negate current v, normalize desired v, compute rudder as agle to the vector sum -> angle depends on speed
    public double getJib(double speed, double relativeWindDirection, double relativeWindSpeed) {
        Vector2d wTmp = Navigation.getWindVector(relativeWindDirection, relativeWindSpeed);
        Vector3d w = new Vector3d(wTmp.x, wTmp.y, 0d); // relative wind
        Vector3d v = new Vector3d(0d, -speed - 0.1d, 0d); // relative boat speed negated,i.e., relative to own axis of motion
        // if co-linear, add random offset ... should be deterministic
        if (Math.abs(w.angle(v))<0.1) {
            w.x += Math.random();
            w.y += Math.random();
        }
        w.normalize();
        v.normalize();
        v.scale(vFactor);
        Vector3d s = new Vector3d(v);
        s.add(w);
        Vector3d d = new Vector3d();
        d.cross(s,v);
        double angle = s.angle(v)/Math.PI*180;
        angle *= jibFactor;
//        System.out.println(" MMMMM " + angle + " " + jibFactor + " " + s + " " + v + " " + speed + " " + relativeWindDirection + " " + relativeWindSpeed);
        if (angle > 90) angle = 90;
        return -(angle*(Math.signum(d.z))); // use cross product to determine left / right;
    }

    // add rudder to open / close main accordingly
    public double getMain(double speed, double relativeWindDirection, double relativeWindSpeed, double rudder) {
        Vector2d wTmp = Navigation.getWindVector(relativeWindDirection, relativeWindSpeed);
        Vector3d w = new Vector3d(wTmp.x, wTmp.y, 0d); // relative wind
        Vector3d v = new Vector3d(0d, -speed - 0.1d, 0d); // relative boat speed negated,i.e., relative to own axis of motion
        // if co-linear, add random offset ... should be deterministic
        if (Math.abs(w.angle(v))<0.1) {
            w.x += Math.random();
            w.y += Math.random();
        }
        w.normalize();
        v.normalize();
        v.scale(vFactor);
        Vector3d s = new Vector3d(v);        
        s.add(w);
        Vector3d d = new Vector3d();
        d.cross(s,v);

        double angle = s.angle(v)/Math.PI*180;
        angle *= mainFactor;
        if ((Math.signum(rudder) == -Math.signum(d.z)) && (Math.abs(rudder) > 30) && (mainOffset < 45)) mainOffset++;
        else mainOffset = 0;
        angle += mainOffset;
        if (angle > 90) angle = 90;
        double result = -Math.signum(d.z) * angle;
        //System.out.println(rudder + "  " + angle + " " + mainOffset);

//        double angle = s.angle(v)/Math.PI*180 + mainOffset;
//        angle *= - Math.signum(d.z);
//        double result = angle;
//        if ((relativeWindDirection > 40) && (relativeWindDirection < 320) && (Math.signum(angle) == Math.signum(rudder))) result += rudder/2d;
//        if (result > 90) result = 90;
//        if (result < -90) result = -90;
        return result; // use cross product to determine left / right;
    }                    

//    // add rudder to open / close main accordingly
//    public static double getMain(double speed, double relativeWindDirection, double relativeWindSpeed, double rudder) {
//        Vector2d wTmp = Navigation.getWindVector(relativeWindDirection, relativeWindSpeed);
//        Vector3d w = new Vector3d(wTmp.x, wTmp.y, 0d); // relative wind
//        Vector3d v = new Vector3d(0d, -speed, 0d); // relative boat speed negated,i.e., relative to own axis of motion
//        Vector2d rTmp  = Navigation.getWindVector(-rudder, rFactor * vFactor); // rudder same as wind opposite direction ...
//        Vector3d r = new Vector3d(rTmp.x, rTmp.y, 0d); // rudder
//        // if co-linear, add random offset ... should be deterministic
//        if (Math.abs(w.angle(v))<0.1) {
//            w.x += Math.random();
//            w.y += Math.random();
//        }
//        w.normalize();
//        v.normalize();
//        v.scale((1-rFactor) * vFactor);
//        Vector3d s = new Vector3d(v);
//        s.add(r);
//        s.add(w);
//        Vector3d d = new Vector3d();
//        d.cross(s,v);
//        double angle = s.angle(v)/Math.PI*180 + mainOffset;
//        if (angle > 90) angle = 90;
//        return -(angle*(Math.signum(d.z))); // use cross product to determine left / right;
//    }

}


