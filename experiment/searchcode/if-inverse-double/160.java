/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.spotx.extdrivers.compass;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.util.Utils;
import com.sun.squawk.util.MathUtils;
import java.io.IOException;

/**
 *
 * @author erikkallen
 */
public class Compass {
    private MicroMag3 mag = null;
    private IAccelerometer3D accel = null;
    private static Compass instance = null;
    private int Xmax, Xmin, Ymax, Ymin;
    private int	Xrange, Yrange;
    private int	Xoffset, Yoffset;

    public static Compass getInstance() {
        if (instance == null)
            instance = new Compass();

        return instance;
    }
    private boolean inverse = false;

    private Compass() {
        accel = EDemoBoard.getInstance().getAccelerometer();
        mag = MicroMag3.getInstance();
        mag.setSensitivity(MicroMag3.SENS_256);
    }

    public void calibrate() {

        Xmax = Ymax = -32768; Xmin = Ymin = 32767;
        int Xraw, Yraw;
        // where you store max and min X and Ys // values you get from the ASIC
        // The numbers here will be 16-bit signed values. // Positive = 0x0000 to 0x7FFF // Negative = 0x8000 to 0xFFFF
        // start with lowest possible value // start with highest possible value
        // acquire a (X,Y) point // now sort it
        for ( int i=0;i<100;i++ ) {
            Xraw = mag.sample(MicroMag3.X_AXIS);
            Yraw = mag.sample(MicroMag3.Y_AXIS);

            if( Xraw > Xmax ) Xmax = Xraw;
            if( Xraw < Xmin ) Xmin = Xraw;
            if( Yraw > Ymax ) Ymax = Yraw;
            if( Yraw < Ymin ) Ymin = Yraw;

            Utils.sleep(100);
            System.out.println("Calibrating: "+i);
        }

        Xoffset = ( Xmax + Xmin ) >> 1;
        Yoffset = ( Ymax + Ymin ) >> 1;
        Xrange = ( Xmax - Xmin );
        Yrange = ( Ymax - Ymin );
        System.out.println("Calibration done: X(max): "+Xmax + " X(min): " + Xmin +" Y(max): " + Ymax + " Y(min): " + Ymin);
    }
     

    public void quickCalibrate() {
        Xmin = -141;
        Xmax = 29;
        Ymin = -110;
        Ymax = 71;
        Xoffset = ( Xmax + Xmin ) / 2;
        Yoffset = ( Ymax + Ymin ) / 2;
        Xrange = ( Xmax - Xmin );
        Yrange = ( Ymax - Ymin );
        
        //Calibration done: X(max): 229 X(min): -270 Y(max): 276 Y(min): -294Z(max): 114 Z(min): -63
        //Calibration done: X(max): 190 X(min): -221 Y(max): 243 Y(min): -204
        // X(max): 29 X(min): -141 Y(max): 71 Y(min): -110
    }

    public double getHeading() {
        double heading = 0;
        int	Xraw, Yraw;
        double angle = 0;
        int Xvalue, Yvalue;
        Xraw = mag.sample(MicroMag3.X_AXIS);
        Yraw = mag.sample(MicroMag3.Y_AXIS);

        Xvalue = Xraw - Xoffset; Yvalue = Yraw - Yoffset;
        if( Xrange > Yrange )
            Yvalue = ( Yvalue * Xrange ) / Yrange;
        else
            Xvalue = ( Xvalue * Yrange ) / Xrange;


        angle = MathUtils.atan( (double)Yvalue / (double)Xvalue ) * 180 / Math.PI;


        if( Xvalue >=0 && Yvalue >= 0 )
            heading = 360 - angle;
        else if( Xvalue < 0 && Yvalue >= 0 )
            heading = 180 - angle;
        else if( Xvalue < 0 && Yvalue < 0 )
            heading = 180 - angle;
        else if( Xvalue >= 0 && Yvalue < 0 )
            heading = -angle;

        return heading;
    }
    public void setInverseAxis() {
        this.inverse = true;
    }
    public void setInverseAxisOff() {
        this.inverse = false;
    }
    public double getTiltCompensatedHeading() {
        double heading = 0;
        int	Xraw, Yraw, Zraw;
        double angle = 0;
        double accx = 0, accy = 0, accz=0;
        Xraw = mag.sample(MicroMag3.X_AXIS);
        Yraw = mag.sample(MicroMag3.Y_AXIS);
        Utils.sleep(1);
        Zraw = mag.sample(MicroMag3.Z_AXIS);
        System.out.println("sampled");
        double axSum =0;
        do {
            
            try {
                accx = accel.getAccelY();
                accy = accel.getAccelX();
                accz = accel.getAccelZ();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            axSum = MathUtils.pow(accx, 2)+ MathUtils.pow(accy, 2)+MathUtils.pow(accz, 2);
            //System.out.println("abc: " + axSum);
        } while (axSum < 0.9 || axSum > 1.1);
        accx = -accx;
        if(pow(accx)>(pow(accy) +pow(accz))) {
            if (accx>0)
                accx=Math.sqrt(1-pow(accy) -pow(accz) );
            else
                accx=-Math.sqrt(1-pow(accy) -pow(accz) );
        }

        if (inverse) {
            //Xraw = -Xraw;
            Yraw = -Yraw;
            Zraw = -Zraw;
            
            accx = -accx;
            accy = -accy;
            accz = -accz;
        }

        double xTilt,yTilt;
      
        xTilt = (Xraw * Math.sqrt(1-(accx*accx))) + (Yraw * accy*accx) + (Zraw * Math.sqrt(1-(accy*accy))*accx);
        yTilt = (Yraw * Math.sqrt(1-(accy*accy))) - (Zraw * accy);

        angle = MathUtils.atan( (double)yTilt / (double)xTilt ) * 180 / Math.PI;

        if( Xraw >=0 && Yraw >= 0 )
            heading = 360 - angle;
        else if( Xraw < 0 && Yraw >= 0 )
            heading = 180 - angle;
        else if( Xraw < 0 && Yraw < 0 )
            heading = 180 - angle;
        else if( Xraw >= 0 && Yraw < 0 )
            heading = -angle;
        /*try {
            System.out.println("Proof: " + (accel.getAccelX() + accel.getAccelY() + accel.getAccelZ()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
        if (heading < 0) 
            heading = 0;
        if (heading > 360)
            heading = 360;

        return heading;
    }
    public double pow(double n) {
        return MathUtils.pow(n, 2);
    }

}

