/*
 * CourseControl.java
 *
 * Created on May 21, 2010, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package boat.control;


/**
 *
 * @author Alex
 */
public class WindRudderControl {

    public static double speedThreshold = 30d;

    public static double getRudder(double oldApparent, double newApparent, double speed) {
        double diff = Math.min((oldApparent - newApparent + 360) % 360,(newApparent - oldApparent + 360) % 360);
        diff /= (3 + (speed / speedThreshold));
        if (diff > 45) diff = 45;
        if (speed < 15) diff = 0;
        double sign = 1d;        
        if (((newApparent - oldApparent) > 0) && ((newApparent - oldApparent) < 180)) sign = -1d;
        if (((newApparent - oldApparent) < 0) && ((newApparent - oldApparent) < -180)) sign = -1d;
        return sign * diff;
    }        
    
}

