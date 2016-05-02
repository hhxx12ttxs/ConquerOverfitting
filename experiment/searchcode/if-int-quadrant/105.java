package org.riksa.twirl;

import android.graphics.Rect;
import roboguice.util.Ln;

/**
 * TODO: Oneliner
 * <p/>
 * TODO: Description
 * <p/>
 *
 * @author riksa
 * @version 1.0
 * @since 11/2/13
 */
public class TwirlController {
    private static final double QUADRANT = Math.PI / 2;
    private Rect boundingBox = new Rect(0, 0, 0, 0);
    private double angle;
    private int rotations;
    private int lastX = 0;
    private int lastY = 0;

    @Deprecated
    public Rect getBounds() {
        return boundingBox;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void registerLocation(int x, int y) {
        if (!boundingBox.contains(x, y)) {
            boundingBox.union(x, y);
//            Log.d("Scaling BB %s", boundingBox.toShortString());
//            updateBounds();
        }
        lastX = x;
        lastY = y;
        double newAngle = Math.atan2(y - boundingBox.centerY(), x - boundingBox.centerX());
        if (newAngle < 0)
            newAngle += 2 * Math.PI;
        rotations += fullRotation(angle, newAngle);
        angle = newAngle;
    }

    /**
     * Detects if turning from angle to newAngle crosses the 0 degree line from one way to the other.
     * Returns amount of rotations to add/remove from total
     *
     * @param angle
     * @param newAngle
     * @return
     */
    static int fullRotation(double angle, double newAngle) {
        // in order to increase/decrease revolution count we have to travel from first quadrant to the last or the other way
        // from [-Math.PI/2..0) <--> [0..Math.PI/2]
        if (Math.abs(newAngle) <= QUADRANT && Math.abs(angle) >= 3 * QUADRANT) {
            // from last to first, add one revolution
            return 1;
        }
        if (Math.abs(newAngle) >= 3 * QUADRANT && Math.abs(angle) <= QUADRANT) {
            // from first to last, remove one revolution
            return -1;
        }
        return 0;
    }

    public void start(int x, int y) {
        Log.d("start %d %d", x, y);
        angle = 0d;
        boundingBox.set(x, y, x, y);
        registerLocation(x, y);
    }

    public void stop() {
        Log.d("stop");
    }

    /**
     * Total angle of the crank including full rotations in radians
     *
     * @return
     */
    public double getAngle() {
        double v = angle + rotations * Math.PI * 2;
        Ln.d("angle %.1f", v);
        return v;
    }

    /**
     * Local angle of the crank including (excluding full rotations) in radians
     *
     * @return
     */
    public double getLocalAngle() {
        return angle;
    }

    /**
     * Total number of rotations
     *
     * @return
     */
    public int getRotations() {
        return rotations;
    }

    /**
     * Distance from center to the last known location of finger
     *
     * @return
     */
    public double getHandleDistance() {
        return Math.sqrt(Math.pow(lastY - boundingBox.centerY(), 2) + Math.pow(lastX - boundingBox.centerX(), 2));
    }
}

