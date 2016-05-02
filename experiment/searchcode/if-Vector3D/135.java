/*
 * CourseControl.java
 *
 * Created on May 21, 2010, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package boat.control;

import javax.vecmath.*;

/**
 *
 * @author Alex
 */
public class CourseRudderControl implements RudderControl {
    
    public static double speedThreshold = 30d; // length of new vector, if speed is larger -> less rudder, otherwise more
    
    // idea: negate current v, normalize desired v, compute rudder as angle to the vector sum -> angle depends on speed
    protected Vector3d getRudderVector(Vector2d vOld, Vector2d vNew) {
        Vector3d v_old = new Vector3d(vOld.x, vOld.y, 0d);
        Vector3d v_new = new Vector3d(vNew.x, vNew.y, 0d);
        // if more than 90 deg create vecor of 90 deg to maximize rudder
        if (v_new.angle(v_old) > Math.PI/2d) {
            Vector3d n = new Vector3d(v_new);
            // should be deterministic, how to avoid adding same direction? ... special cases
            if (Math.abs(n.angle(v_old) - Math.PI)<0.1) {
                n.x += Math.random();
                n.y += Math.random();
            }
            n.cross(n,v_old);
            n.cross(v_old,n);
            n.normalize();
            n.scale(v_new.length());
            v_new = n;
        }
        v_new.normalize();
        v_new.scale(speedThreshold);
        v_new.add(v_old);
        return v_new;
    }
    
    // idea: negate current v, normalize desired v, compute rudder as agle to the vector sum -> angle depends on speed
    public double getRudder(Vector2d vOld, Vector2d vNew) {
        Vector3d v_old = new Vector3d(vOld.x, vOld.y, 0d);
        Vector3d v_new = getRudderVector(vOld, vNew);
        Vector3d r = new Vector3d();
        r.cross(v_new,v_old);
        return (Math.toDegrees(v_new.angle(v_old))*(Math.signum(r.z))); 
    }
    
}

