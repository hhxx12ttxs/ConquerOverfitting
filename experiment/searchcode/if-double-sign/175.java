/*
 * CourseRudderControl.java
 * 
 * Project: RobSail2012
 * Package: boat.control.rudder
 * Last update: 16.04.2012
 * 
 * Contact: Alexander Schlaefer (schlaefer@rob.uni-luebeck.de)
 *          Nikolaus Ammann (ammann@rob.uni-luebeck.de)
 * 
 * Copyright 2012 Institute for Robotics and Cognitive Systems.
 */
package boat.control.rudder;

import boat.data.FilterData;
import boat.data.SensorData;
import boat.planning.BoatState;
import java.util.logging.Logger;

/**
 * The Class CourseRudderControl.
 */
public class CourseRudderControl implements RudderControl {
    
    /** The speed threshold. */
    public double speedThreshold = 50d;
    
    /** The bias. */
    public double bias = 3d;
    
    /*
     * (non-Javadoc)
     * @see boat.control.rudder.RudderControl#getRudder(double,
     * boat.planning.BoatState)
     */
    @Override
    public double getRudder(double newCourse, BoatState bs, SensorData sd, FilterData fd) {
        
        double oldCourse = map.util.Navigation.getCourse(bs.getSpeed());
        double speed = bs.getSpeed().length();
        
        double diff = Math.min(((oldCourse - newCourse) + 360) % 360, ((newCourse - oldCourse) + 360) % 360);
        diff /= (this.bias + ((speed / this.speedThreshold) * (speed / this.speedThreshold)));
        if (diff > 45) {
            diff = 45;
        }
        if (speed < 1) {
            diff = 0; // 15
        }
        double sign = -1d;
        if (((newCourse - oldCourse) > 0) && ((newCourse - oldCourse) < 180)) {
            sign = 1d;
        }
        if (((newCourse - oldCourse) < 0) && ((newCourse - oldCourse) < -180)) {
            sign = 1d;
        }
        LOG.finest("CRC: " + oldCourse + "  " + newCourse + "  " + speed + "  " + diff + "  " + sign);
        return sign * diff;
    }
    private static final Logger LOG = Logger.getLogger(CourseRudderControl.class.getName());
}

