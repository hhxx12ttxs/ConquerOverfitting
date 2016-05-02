
package com.grt192.sensor;

import com.grt192.core.Sensor;
import com.grt192.event.component.MaxBotixEvent;
import com.grt192.event.component.MaxBotixListener;
import com.sun.spot.resources.transducers.IAnalogInput;
import com.sun.spot.sensorboard.EDemoBoard;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author grtstudent
 */
public class GRTMaxBotixSonar extends Sensor {

    public static final double MV_PER_INCH = 98;
    public static final double SPIKE_THRESHOLD = 1.0;
    public static final double CHANGE_THRESHOLD = .001;
//    private AnalogChannel input;
    private IAnalogInput input;
    private Vector maxBotixListeners;

    public static final int HISTORY_SIZE = 7;
    private double[] history;
    private int pointer = 0;

    public GRTMaxBotixSonar(int port, int pollTime, String id) {
        this.id = id;
        this.sleepTime = pollTime;
        input = EDemoBoard.getInstance().getAnalogInputs()[port];
//        input = new AnalogChannel(port);
        
        maxBotixListeners = new Vector();
        history = new double[HISTORY_SIZE];
    }

    public void poll() {
        //TODO calibrate for range value
        double previousValue = getState("Value");
        try {
            setState("Value", input.getVoltage());
            //        setState("Value", input.getAverageValue());
            //        setState("Value", input.getVoltage());
            //        setState("Average Value", input.getAverageVoltage());
            //        setState("Distance", inches(input.getAverageVoltage()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
//        setState("Value", input.getAverageValue());
//        setState("Value", input.getVoltage());
//        setState("Average Value", input.getAverageVoltage());
//        setState("Distance", inches(input.getAverageVoltage()));

        //start average
        history[(pointer++)%HISTORY_SIZE] = getState("Distance");
        setState("Deviation", deviation(history));
        setState("Average", average(history));

        //fuck hardcoding lets go log all variables!
//        Enumeration keys = getState().keys();
//        while (keys.hasMoreElements()) {
//            String key = (String) keys.nextElement();
//            logVar(key, getState(key));
//        }

        if (Math.abs(getState("Value") - previousValue) >= SPIKE_THRESHOLD) {
            notifyMaxBotixSpike();
        }
        if (Math.abs(getState("Value") - previousValue) >= CHANGE_THRESHOLD) {
            notifyMaxBotixChange();
        }
        notifyMaxBotixListeners();
    }

    private static double inches(double mv) {
        return MV_PER_INCH * mv;
    }

    public static double deviation(double[] history) {
        double mean = average(history);
        int max = history.length - 1;
        int min = 0;
        for (int i = 0; i < history.length; i++) {
            if (history[i] <= history[min]) {
                min = i;
            } else if (history[i] >= history[max]) {
                max = i;
            }
        }
        double sum = 0;
        for (int j = 0; j < history.length; j++) {
            if (j != max && j != min) {
                sum += ((mean - history[j]) * (mean - history[j]));
            }
        }
        sum = Math.sqrt(sum / (history.length - 2));
        return sum;
    }

    public static double average(double[] history) {
        int max = history.length - 1;
        int min = 0;
        for (int i = 0; i < history.length; i++) {
            if (history[i] <= history[min]) {
                min = i;
            } else if (history[i] >= history[max]) {
                max = i;
            }
        }
        double sum = 0;
        for (int j = 0; j < history.length; j++) {
            if (j != max && j != min) {
                sum += history[j];
            }
        }
        sum /= history.length - 2;
        return sum;
    }

    protected void notifyMaxBotixSpike() {
        for (int i = 0; i < maxBotixListeners.size(); i++) {
            ((MaxBotixListener) maxBotixListeners.elementAt(i)).didRangeSpike(
                    new MaxBotixEvent(this,
                    MaxBotixEvent.DEFAULT,
                    getState("Value")));
        }
    }

    protected void notifyMaxBotixChange() {
        for (int i = 0; i < maxBotixListeners.size(); i++) {
            ((MaxBotixListener) maxBotixListeners.elementAt(i)).didRangeChange(
                    new MaxBotixEvent(this,
                    MaxBotixEvent.DEFAULT,
                    getState("Value")));
        }
    }

    protected void notifyMaxBotixListeners() {
        for (int i = 0; i < maxBotixListeners.size(); i++) {
            ((MaxBotixListener) maxBotixListeners.elementAt(i)).didReceiveRange(
                    new MaxBotixEvent(this,
                    MaxBotixEvent.DEFAULT,
                    getState("Value")));
        }
    }

    public void addMaxBotixListener(MaxBotixListener m) {
        maxBotixListeners.addElement(m);
    }

    public void removeMaxBotixListener(MaxBotixListener m) {
        maxBotixListeners.removeElement(m);
    }
}

