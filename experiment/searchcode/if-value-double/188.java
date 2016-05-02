package com.grt192.core;

import java.util.Hashtable;
import java.util.Vector;

import com.grt192.event.SensorChangeListener;
import com.grt192.event.SensorDataListener;
import com.grt192.event.SensorEvent;

/**
 * Superclass for all specific sensors on a robot.
 * 
 * @author anand
 */
public abstract class Sensor extends GRTObject {

    public static final double FALSE = 0.0;
    public static final double TRUE = 1.0;
    public static final double ERROR = -999;
    protected Hashtable state;
    protected int sleepTime = 50;
    private boolean running;
    private boolean suspended;
    protected String id;
    private double changeThreshold;
    private double lastDatumSent;
    private Vector sensorDataListeners;
    private Vector sensorChangeListeners;

    public Sensor() {
        state = new Hashtable();
        running = false;
        suspended = false;
        changeThreshold = 0;
        lastDatumSent = -999;
        sensorDataListeners = new Vector();
        sensorChangeListeners = new Vector();
    }

    public void run() {
        running = true;
        while (running) {
            try {
                if (!suspended) {
                    poll();
                    notifyDataListeners();
                }
                sleep(sleepTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public abstract void poll();

    public void setState(String key, double value) {
        double previousState = getState(key);
        state.put(key, new Double(value));
        notifySensorChangeListeners(key, previousState);
    }

    public void setState(String key, boolean value) {
        if (value) {
            state.put(key, new Double(TRUE));
        } else {
            state.put(key, new Double(FALSE));
        }

    }

    public void setState(String key, Object value) {
        state.put(key, value);
    }

    public synchronized double getState(String key) {
        if (state.containsKey(key)) {
            return ((Double) (state.get(key))).doubleValue();
        } else {
            return -255.0;
        }
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setChangeThreshold(double threshold) {
        changeThreshold = threshold;
    }

    public Hashtable getSensorState() {
        return state;
    }

    public void stopSensor() {
        running = false;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void pause() {
        this.suspended = true;
    }

    public boolean isRunning() {
        return running;
    }

    public void unpause() {
        this.suspended = false;
    }

    public void addSensorDataListener(SensorDataListener s) {
        sensorDataListeners.addElement(s);
    }

    public void removeSensorDataListener(SensorDataListener s) {
        sensorDataListeners.removeElement(s);
    }

    public Vector getSensorDataListeners() {
        return sensorDataListeners;
    }

    protected void notifyDataListeners() {
        for (int i = 0; i < sensorDataListeners.size(); i++) {
            ((SensorDataListener) sensorDataListeners.elementAt(i)).didRecieveData(new SensorEvent(this,
                    SensorEvent.DATA_AVAILABLE, this.state));
        }
    }

    protected void notifySensorChangeListeners(String key, double previousState) {
        SensorEvent e = new SensorEvent(this, SensorEvent.DATA_AVAILABLE, this.state);

        if (Math.abs(getState(key) - lastDatumSent) > changeThreshold) {
            for (int i = 0; i < sensorChangeListeners.size(); i++) {
                ((SensorChangeListener) sensorChangeListeners.elementAt(i)).sensorStateChanged(e, key);
            }
        }
        lastDatumSent = getState(key);
    }

    public void removeSensorChangeListener(SensorChangeListener s) {
        sensorChangeListeners.removeElement(s);
    }

    public Vector getSensorChangeListeners() {
        return sensorChangeListeners;
    }

    public void addSensorChangeListener(SensorChangeListener s) {
        sensorChangeListeners.addElement(s);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }
}

