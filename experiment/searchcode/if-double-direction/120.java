package com.androbot.sensors.processors;

import java.util.ArrayList;
import java.util.List;

import com.androbot.sensors.RobotSensor;
import com.androbot.sensors.SensorType;
import com.helper.geometry.PointD;

/**
 * Combines data from several light sensors
 * 
 * @author gyscos
 * 
 */
public class LightProcessor extends SensorProcessor {

    /**
     * Weight of a sensor depending on the distance to the required value.<br />
     * weight(0) = infinity and weight(infinity) = 0
     * 
     * @param delta
     * @return
     */
    public static double getWeight(double delta) {
        if (delta == 0)
            // Shouldn't happen.
            return 100000;

        return 1 / delta;
    }

    List<RobotSensor> lights     = new ArrayList<RobotSensor>();

    List<PointD>      lightPos   = new ArrayList<PointD>();

    List<Boolean>     directives = new ArrayList<Boolean>();

    float             threshold  = 0.1f;

    public RobotSensor addSensor(PointD pos, boolean directive) {
        RobotSensor sensor = new RobotSensor(SensorType.LIGHT_SENSOR, this);

        lights.add(sensor);
        lightPos.add(pos);
        directives.add(directive);

        return sensor;
    }

    public double getLight(double direction) {
        double coefSum = 0;
        double sum = 0;

        for (int i = 0; i < lights.size(); i++) {
            if (!directives.get(i))
                continue;

            PointD pos = lightPos.get(i);
            double delta = pos.arg() - direction;

            if (delta == 0)
                // On exact match, simply returns the sensor value
                return lights.get(i).value;

            double coef = getWeight(delta);
            coefSum += coef;
            sum += coef * lights.get(i).value;
        }

        if (coefSum == 0)
            return 0;
        return sum / coefSum;
    }

    public double getSensorLight(int id) {
        return lights.get(id).value;
    }

    /**
     * Checks if the given direction is dark.
     * 
     * @param direction
     * @return
     */
    public boolean isDark(double direction) {
        return getLight(direction) < threshold;
    }

    /**
     * Checks if a specific sensor is dark.
     * 
     * @param id
     * @return
     */
    public boolean isSensorDark(int id) {
        return lights.get(id).value < threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    @Override
    public void update() {
        // Not much to do. Maybe detect light peaks ? Light changes ?
    }
}

