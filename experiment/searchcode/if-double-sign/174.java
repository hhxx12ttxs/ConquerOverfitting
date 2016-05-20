/*
 * WindRudderControl.java
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

import java.util.logging.Logger;

/**
 * The Class WindRudderControl.
 */
public class WindRudderControl {
    
    /** The speed threshold. */
    public static double speedThreshold = 50d;
    
    /** The bias. */
    public static double bias = 3d;
    
    /**
     * Gets the rudder.
     * 
     * @param oldApparent
     *            the old apparent
     * @param newApparent
     *            the new apparent
     * @param speed
     *            the speed
     * @return the rudder
     */
    public static double getRudder(double oldApparent, double newApparent, double speed) {
        double diff = Math.min(((oldApparent - newApparent) + 360) % 360, ((newApparent - oldApparent) + 360) % 360);
        diff /= (WindRudderControl.bias + ((speed / WindRudderControl.speedThreshold) * (speed / WindRudderControl.speedThreshold)));
        if (diff > 45) {
            diff = 45;
        }
        if (speed < .15) {
            diff = 0;
        }
        double sign = 1d;
        if (((newApparent - oldApparent) > 0) && ((newApparent - oldApparent) < 180)) {
            sign = -1d;
        }
        if (((newApparent - oldApparent) < 0) && ((newApparent - oldApparent) < -180)) {
            sign = -1d;
        }
        LOG.finer("WRC: " + oldApparent + "  " + newApparent + "  " + speed + "  " + diff + "  " + sign);
        return sign * diff;
    }
    private static final Logger LOG = Logger.getLogger(WindRudderControl.class.getName());
    
}

