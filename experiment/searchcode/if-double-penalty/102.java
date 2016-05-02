/*
 * StateSelector.java
 *
 * Created on May 14, 2010, 6:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package boat.planning;

import java.util.*;
import javax.vecmath.*;

/**
 *
 * @author Alex
 */
public class StateSelector {
    
    public static double w_o = 100; // weight for distance to nearest obstacle
    
    public static double w_wp = 10; // weight for change in distance to waypoint
    
    public static double w_v = 1; // weight for resulting speed

    public static double w_t = 100; // weight for tack penalty

    public static double getTack(Vector2d v1, Vector2d v2) {
        Vector3d tmp = new Vector3d(v1.x,v1.y,0);
        tmp.cross(tmp, new Vector3d(v2.x,v2.y,0));
        tmp.normalize();
        return tmp.z;
    }

    // distance / objective as
    // d_o = min distance to obstacle -> min
    // d_wp = distance to waypoint -> min
    // v -> max
    public static State select(WayPoint wayPoint, Vector<Obstacle> obstacles, State current_state, Vector<State> next_states, Wind w) {
        State result = new State(current_state.getPosition(), current_state.getVelocity(), current_state.getTargetApparent());
        double d_min = Double.MAX_VALUE;
        for (State s : next_states) {
            double d_o = -Double.MAX_VALUE;
            if (obstacles.size() == 0) d_o = 0;
            else for (Obstacle o : obstacles) d_o = Math.max(d_o, o.getDistance(s.getPosition()));
            // improvement in distance to target
            double d_wp = wayPoint.getPosition().distance(s.getPosition()) - wayPoint.getPosition().distance(current_state.getPosition());
            // get length of speed vector, won't work in quadrature
            double v = s.getVelocity().length();
            // did we change tack?
            double t = Math.abs(getTack(w.getVelocity(),current_state.getVelocity())-getTack(w.getVelocity(),s.getVelocity()));
            if (Double.isNaN(t)) t = 0;
            double d = w_o*d_o + w_wp*d_wp + w_v*(-v) + w_t*t;
            if (!Double.isNaN(d) && (d<d_min)) {
                d_min = d;
                result = s;
            }
        }
        return result;
    }      

}

