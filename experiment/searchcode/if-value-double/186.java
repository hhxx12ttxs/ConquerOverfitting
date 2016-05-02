/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package com.grt192.controller.pid;

import java.util.TimerTask;

/**
 * Class implements a PID Control Loop.
 *
 * Creates a separate thread which reads the given PIDSource and takes
 * care of the integral calculations, as well as writing the given
 * PIDOutput
 */
public class PIDController {

    public static final double DEFAULT_PERIOD = .05;
    private double p;			// factor for "proportional" control
    private double i;			// factor for "integral" control
    private double d;			// factor for "derivative" control
    private double maxOutput = 1.0;	// |maximum output|
    private double minOutput = -1.0;	// |minimum output|
    private double maxInput = 0.0;		// maximum input - limit setpoint to this
    private double minInput = 0.0;		// minimum input - limit setpoint to this
    private boolean continuous = false;	// do the endpoints wrap around? eg. Absolute encoder
    private boolean enabled = false; 			//is the pid controller enabled
    private double prevError = 0.0;	// the prior sensor input (used to compute velocity)
    private double totalError = 0.0; //the sum of the errors for use in the integral calc
    private double tolerance = 0.05;	//the percetage error that is considered on target
    private double setpoint = 0.0;
    private double error = 0.0;
    private double result = 0.0;
    private double period = DEFAULT_PERIOD;
    PIDSource pidInput;
    PIDOutput pidOutput;
    java.util.Timer controlLoop;

    private class PIDTask extends TimerTask {

        private PIDController m_controller;

        public PIDTask(PIDController controller) {
            if (controller == null) {
                throw new NullPointerException("Given GRTPIDController was null");
            }
            m_controller = controller;
        }

        public void run() {
            m_controller.calculate();
        }
    }

    /**
     * Allocate a PID object with the given constants for P, I, D
     * @param Kp the proportional coefficient
     * @param Ki the integral coefficient
     * @param Kd the derivative coefficient
     * @param source The PIDSource object that is used to get values
     * @param output The PIDOutput object that is set to the output value
     * @param period the loop time for doing calculations. This particularly effects calculations of the
     * integral and differential terms. The default is 50ms.
     */
    public PIDController(double Kp, double Ki, double Kd,
            PIDSource source, PIDOutput output,
            double period) {

        if (source == null) {
            throw new NullPointerException("Null PIDSource was given");
        }
        if (output == null) {
            throw new NullPointerException("Null PIDOutput was given");
        }


        controlLoop = new java.util.Timer();


        p = Kp;
        i = Ki;
        d = Kd;

        pidInput = source;
        pidOutput = output;
        period = period;

        controlLoop.schedule(new PIDTask(this), 0L, (long) (period * 1000));
    }

    /**
     * Allocate a PID object with the given constants for P, I, D, using a 50ms period.
     * @param Kp the proportional coefficient
     * @param Ki the integral coefficient
     * @param Kd the derivative coefficient
     * @param source The PIDSource object that is used to get values
     * @param output The PIDOutput object that is set to the output value
     */
    public PIDController(double Kp, double Ki, double Kd,
            PIDSource source, PIDOutput output) {
        this(Kp, Ki, Kd, source, output, DEFAULT_PERIOD);
    }

    /**
     * Free the PID object
     */
    protected void free() {
        controlLoop.cancel();
        controlLoop = null;
    }

    /**
     * Read the input, calculate the output accordingly, and write to the output.
     * This should only be called by the PIDTask
     * and is created during initialization.
     */
    private void calculate() {
        boolean enabled;
        PIDSource pidInput;
//        System .out.println("running");

        synchronized (this) {
            if (this.pidInput == null) {
                return;
            }
            if (pidOutput == null) {
                return;
            }
            enabled = this.enabled; // take snapshot of these values...
            pidInput = this.pidInput;
        }

        if (enabled) {
            double input = pidInput.pidGet();
            double result;
            PIDOutput pidOutput = null;

            synchronized (this) {
                error = setpoint - input;
//                System.out.println("Error" + m_error);
//                System.out.println("setpoint" + m_setpoint);
//                System.out.println("input" + input);

                if (continuous) {
                    if (Math.abs(error)
                            > (maxInput - minInput) / 2) {
                        if (error > 0) {
                            error = error - maxInput + minInput;
                        } else {
                            error = error
                                    + maxInput - minInput;
                        }
                    }
                }

                if (((totalError + error) * i < maxOutput)
                        && ((totalError + error) * i > minOutput)) {
                    totalError += error;
                }

                result = (p * error + i * totalError + d * (error - prevError));
                prevError = error;

                if (result > maxOutput) {
                    result = maxOutput;
                } else if (result < minOutput) {
                    result = minOutput;
                }
                pidOutput = this.pidOutput;
                result = result;
            }

            pidOutput.pidWrite(result);
        }
    }

    /**
     * Set the PID Controller gain parameters.
     * Set the proportional, integral, and differential coefficients.
     * @param p Proportional coefficient
     * @param i Integral coefficient
     * @param d Differential coefficient
     */
    public synchronized void setPID(double p, double i, double d) {
        this.p = p;
        this.i = i;
        this.d = d;
    }

    /**
     * Get the Proportional coefficient
     * @return proportional coefficient
     */
    public double getP() {
        return p;
    }

    /**
     * Get the Integral coefficient
     * @return integral coefficient
     */
    public double getI() {
        return i;
    }

    /**
     * Get the Differential coefficient
     * @return differential coefficient
     */
    public synchronized double getD() {
        return d;
    }

    /**
     * Return the current PID result
     * This is always centered on zero and constrained the the max and min outs
     * @return the latest calculated output
     */
    public synchronized double get() {
        return result;
    }

    /**
     *  Set the PID controller to consider the input to be continuous,
     *  Rather then using the max and min in as constraints, it considers them to
     *  be the same point and automatically calculates the shortest route to
     *  the setpoint.
     * @param continuous Set to true turns on continuous, false turns off continuous
     */
    public synchronized void setContinuous(boolean continuous) {
        continuous = continuous;
    }

    /**
     *  Set the PID controller to consider the input to be continuous,
     *  Rather then using the max and min in as constraints, it considers them to
     *  be the same point and automatically calculates the shortest route to
     *  the setpoint.
     */
    public synchronized void setContinuous() {
        this.setContinuous(true);
    }

    /**
     * Sets the maximum and minimum values expected from the input.
     *
     * @param minimumInput the minimum value expected from the input
     * @param maximumInput the maximum value expected from the output
     */
    public synchronized void setInputRange(double minimumInput, double maximumInput) {
//        if (minimumInput > maximumInput) {
//            throw new BoundaryException("Lower bound is greater than upper bound");
//        }
        minInput = minimumInput;
        maxInput = maximumInput;
        setSetpoint(setpoint);
    }

    /**
     * Sets the minimum and maximum values to write.
     *
     * @param minimumOutput the minimum value to write to the output
     * @param maximumOutput the maximum value to write to the output
     */
    public synchronized void setOutputRange(double minimumOutput, double maximumOutput) {
//        if (minimumOutput > maximumOutput) {
//            throw new BoundaryException("Lower bound is greater than upper bound");
//        }
        minOutput = minimumOutput;
        maxOutput = maximumOutput;
    }

    /**
     * Set the setpoint for the GRTPIDController
     * @param setpoint the desired setpoint
     */
    public synchronized void setSetpoint(double setpoint) {
        if (maxInput > minInput) {
            if (setpoint > maxInput) {
                setpoint = maxInput;
            } else if (setpoint < minInput) {
                setpoint = minInput;
            } else {
                setpoint = setpoint;
            }
        } else {
            setpoint = setpoint;
        }
    }

    /**
     * Returns the current setpoint of the GRTPIDController
     * @return the current setpoint
     */
    public synchronized double getSetpoint() {
        return setpoint;
    }

    /**
     * Returns the current difference of the input from the setpoint
     * @return the current error
     */
    public synchronized double getError() {
        return error;
    }

    /**
     * Set the percentage error which is considered tolerable for use with
     * OnTarget. (Input of 15.0 = 15 percent)
     * @param percent error which is tolerable
     */
    public synchronized void setTolerance(double percent) {
        tolerance = percent;
    }

    /**
     * Return true if the error is within the percentage of the total input range,
     * determined by setTolerance. This assumes that the maximum and minimum input
     * were set using setInput.
     * @return true if the error is less than the tolerance
     */
    public synchronized boolean onTarget() {
        return (Math.abs(error) < tolerance / 100
                * (maxInput - minInput));
    }

    /**
     * Begin running the GRTPIDController
     */
    public synchronized void enable() {
        enabled = true;
    }

    /**
     * Stop running the GRTPIDController, this sets the output to zero before stopping.
     */
    public synchronized void disable() {
        pidOutput.pidWrite(0);
        enabled = false;
    }

    /**
     * Return true if GRTPIDController is enabled.
     */
    public synchronized boolean isEnable() {
        return enabled;
    }

    /**
     * Reset the previous error,, the integral term, and disable the controller.
     */
    public synchronized void reset() {
        disable();
        prevError = 0;
        totalError = 0;
        result = 0;
    }
}

