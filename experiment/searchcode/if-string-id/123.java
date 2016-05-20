package com.grt192.sensor.spot;

import com.grt192.core.Sensor;
import com.grt192.utils.Assert;
import com.sun.spot.resources.transducers.IAnalogInput;
import com.sun.spot.resources.transducers.IIOPin;
import com.sun.spot.sensorboard.EDemoBoard;
import java.io.IOException;

/**
 * A maxbotix EZ4 sonar, that supports PWM and analog interfaces.
 * This is a free ranging sonar. Due to crosstalk, only one of these
 * can be used at a time without additional configuration.
 * This can't hot swap interfaces
 * PWM pin on sonar: PW
 * ANalog pin on sonar: AN
 * @author ajc
 */
public class GRTSonar extends Sensor {

    /** sensor state key */
    public static final String DISTANCE = "Distance";
    //
    private static final int NO_INTERFACE = -1;
    //analog conversions
    private static double MV_PER_VOLT = 1000;
    private static double INCH_PER_MV = 1.0 / 9.8; //5v
    private IAnalogInput analog;//note: min pollTime for sonar is 50s
    private int anPin;
    //pwm conversions
    private static double INCH_PER_US = 1.0 / 147.0;
    private static int PULSE_WAITTIME = 0; // on getPulse() we do no waiting
    private IIOPin pwm;
    private EDemoBoard spotboard;
    private int pwmPin;
    //mode settings
    public static final int ANALOG = 0;
    public static final int PWM = 1;
    private int mode;

    /**
     * Makes a new pwm-interface Maxbotix sonar
     * Note: without special configuration, 2 pwm sonars can't be read simultaneously.1
     * @param pin d[0-5] gpio on EDemoBoard
     * @param pollTime Time (mS) between polls
     * @param id accessor name
     * @return
     */
    public static GRTSonar fromPWM(int pin, int pollTime, String id){
        return new GRTSonar(PWM, pin, NO_INTERFACE, pollTime, id);
    }

    /**
     * Makes a new analog-interface Maxbotix sonar
     * @param pin a[0-6] analog pins on EDemoBoard
     * @param pollTime Time (mS) between polls
     * @param id accessor name
     * @return
     */
    public static GRTSonar fromAnalog(int pin, int pollTime, String id){
        return new GRTSonar(ANALOG, NO_INTERFACE,pin, pollTime, id);
    }
    
    private GRTSonar(int mode, int pwmPin, int anPin, int pollTime, String id) {
        this.pwmPin = pwmPin;
        this.anPin = anPin;
        this.mode = mode;
        switch (mode) {
            case ANALOG:
                initAnalog();
                break;
            case PWM:
                initPWM();
                break;
            default:
                Assert.shouldNotReachHereFatal("Sensor interface not defined");
        }
        setSleepTime(pollTime);
        setID(id);
        setState(DISTANCE, 0);
    }

    /** Initialize the analog interface */
    private void initAnalog() {
        mode = ANALOG;
        analog = EDemoBoard.getInstance().getAnalogInputs()[anPin];
    }

    /** Initialize the digital interface */
    private void initPWM() {
        mode = PWM;
        spotboard = EDemoBoard.getInstance();
        pwm = spotboard.getIOPins()[pwmPin];
        pwm.setAsOutput(false); //input; we're reading
    }

    /**
     * Calculates distance with an analog interface
     */
    public double analogDistance() {
        try {
            return analog.getVoltage() * MV_PER_VOLT * INCH_PER_MV;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return ERROR;
    }

    /** 
     * Calculates distance with a pwm interface.
     * This will hang if there is no pulse on the pin.
     */
    public double digitalDistance() {
        return spotboard.getPulse(pwm, true, PULSE_WAITTIME) * INCH_PER_US;
    }

    /** Gets the sonar's distance using provided interface **/
    public double getDistance() {
        switch (mode) {
            case ANALOG:
                return analogDistance();
            case PWM:
                return digitalDistance();
        }
        Assert.shouldNotReachHereFatal("Sonar interface not defined");
        return ERROR;
    }

    public void poll() {
        setState(DISTANCE, getDistance());
    }
}

