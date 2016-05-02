/**
 * Sample.java
 *
 * David Finlayson, Ph.D.
 * Operational Geologist
 * Western Coastal and Marine Geology
 * U.S. Geological Survey
 * Pacific Science Center
 * 400 Natural Bridges Drive
 * Santa Cruz, CA 95060
 *
 * Email: dfinlayson@usgs.gov
 * Phone: (831) 427-4757
 *
 * Copyright (C) 2009 David Finlayson
 */package gov.usgs.wr.sxptools.blocks;

import gov.usgs.wr.io.BinaryInputStream;
import gov.usgs.wr.io.BinaryOutputStream;
import gov.usgs.wr.sxptools.transform.SEAVertex;
import java.io.IOException;


/**
 * <p>This class represents a Sample (cXYAPoint) and provides additional fields for
 * storing and accessing advanced properties of the sample.</p>
 *
 * <p>Samples derived directly from the binary SXP file do not contain information
 * about the attitude of the vessel at the exact moment of sample collection nor
 * do they record the location of the sample in the original transducer-centric
 * coordinate system. Hence, the need for the extended attributes of this class.</p>
 *
 * <p>Since Samples are immutable, a
 * {@link gov.usgs.wr.sxptools.blocks.Sample.Builder Builder} class is
 * provided to construct Samples.</p>
 *
 * <p>Samples contain position coordinates in four reference frams (called Axis by
 * SEA). The four main axis may be identified in the geometry of the
 * sonar system; these are the transducer axis, body axis, geographic axis and
 * position axis. Each of which gives slightly differing measurements.</p>
 *
 * <p>Position Axis Set:</p>
 *
 * <p>The position axis set is the one used to define the surveyed seabed. Its axes
 * are north, east and down, and a vector in this set has northing, easting and
 * depth. The origin of the position axis is at that of the position projection
 * and height datum used.<p>
 *
 * <p>The position Axis set is the default axis set and the only one stored in
 * the SXP file itself. To access (or set) the Position coordinates of the sample
 * use the methods {@link gov.usgs.wr.sxptools.blocks.Sample#x() x()},
 * {@link gov.usgs.wr.sxptools.blocks.Sample#y() y()}, and
 * {@link gov.usgs.wr.sxptools.blocks.Sample#x() z()}. Note that the x axis is
 * the northing coordinate and the y axis is the easting coordinate. This is not
 * normal (cartographic) convention, but it falls out of the transformation from
 * the Transducer Axis set below.</p>
 *
 * <p>Geographic Axis Set:</p>
 *
 * <p>The geographic axis set is also north, east and down, but its origin is at
 * the survey center. The xyz location of the geographic axis set origin in the
 * position axis set is given by northing, easting and depth. There is no
 * rotational displacement between the position axis set and geographic axis
 * set. Again, x is northing, y is easting and z is depth (positive down).</p>
 *
 * <p>Body Axis Set:</p>
 *
 * <p>The body axis set has its origin at the survey center. It has one axis in the
 * direction of the vehicle "nose", one on the starboard side and another axis
 * through the bottom of the vehicle. The rotational displacements of the body
 * axis in the geographic axis are roll, pitch and heading. The z displacement
 * of the body axis origin is depth. The boresight of each transducer is given
 * by the rotational displacements skew, elevation and azimuth. Linear and
 * rotational displacements are also required to account for the differences
 * between the positions of the survey center and the attitude system. The
 * attitude system measures the linear accelerations and rotational velocities in
 * each of the three axes and planes.</p>
 *
 * <p>Transducer Axis Set:</p>
 *
 * <p>The transducer axis set is the one in which the sonar measurments are taken.
 * The x-axis of this set is the normal to the transducer face, called boresight.
 * The two measurements made are range and elevation (angle) In a perfect
 * transducer, sonar measurments are made in the transducer x-z plane. The xyz
 * linear displacements in this set are boresight, bottom and right.</p>
 *
 * <p>Vessel Axis Set:</p>
 *
 * <p>A fifth axis set can also be identified, with forward and starboard axes in
 * the horizontal plane, and down. This axis set is the geographic axis set
 * rotated in the horizontal plane by the vehicle heading.</p>
 *
 * @author David Finlayson <dfinlayson@usgs.gov>
 */
public final class Sample
{
    // Public Constants

    public static final int BLOCK_SIZE = 40;
    public static final int SAMPLE_REJECTED = 0;
    public static final int SAMPLE_ACCEPTED = 1;

    // Field constraints
    private static final int MIN_SAMPLES = 0;
    private static final int MAX_SAMPLES = 32768;
    private static final int MIN_STATUS_FLAG = 0;
    private static final int MAX_STATUS_FLAG = 255;
    public static final int MIN_AMPLITUDE_DN = 0;
    public static final int MAX_AMPLITUDE_DN = 65535;
    private static final double MIN_DEPTH = 0.0;
    private static final double MAX_DEPTH = 1000.0;
    private static final double MIN_ROLL = -90.0;
    private static final double MAX_ROLL = 90.0;
    private static final double MIN_HEADING = -360.0;
    private static final double MAX_HEADING = 360.0;
    private static final double MIN_PITCH = -90.0;
    private static final double MAX_PITCH = 90.0;
    private static final double MIN_HEIGHT = -10000.0;
    private static final double MAX_HEIGHT = 10000.0;

    /** Sample number */
    private final int sampNum;
    /** x coordinate of sample in position axis set */
    private final double x;
    /** y coordinate of sample in position axis set */
    private final double y;
    /** z coordinate of sample in position axis set */
    private final double z;
    /** Raw amplitude DN (16-bit) */
    private final int amp;
    /** Processed amplitude DN (16-bit) */
    private final int procAmp;
    /** Sample status */
    private final int status;
    /** Depth of the sample below the waterline */
    private final double depth;
    /** Heading applied to this sample */
    private double heading;
    /** Pitch applied to this sample */
    private double pitch;
    /** Roll applied to this sample */
    private double roll;
    /** Height applied to this sample */
    private double height;
    /** x coordinate of sample in geographic axis set */
    private double geoX;
    /** y coordinate of sample in geographic axis set */
    private double geoY;
    /** z coordinate of sample in geographix axis set */
    private double geoZ;
    /** x coordinate of sample in body axis set */
    private double bodyX;
    /** y coordinate of sample in body axis set */
    private double bodyY;
    /** z coordinate of sample in body axis set */
    private double bodyZ;
    /** x coordinate of sample in transducer axis set */
    private double txerX;
    /** y coordinate of sample in transducer axis set */
    private double txerY;
    /** z coordinate of sample in transducer axis set */
    private double txerZ;

    /**
     * Returns returns a copy of the input sample with its status set to
     * rejected.
     * @param sample the sample to reject
     * @return a new sample
     */
    public static Sample acceptSample(Sample sample)
    {
        Sample.Builder builder = new Sample.Builder(sample);
        builder.status(Sample.SAMPLE_ACCEPTED);
        return builder.build();
    }


    /**
     * Use this builder class to create a new instance of an Sample.
     * See Bloch, J. (2008) Effective Java (2nd Ed). Item 2.
     */
    public static class Builder
    {

        // Required Fields
        private int sampNum;
        private double x;
        private double y;
        private double z;
        private int amp;
        private int procAmp;
        private int status;

        // Optional Fields
        private double depth;
        private double heading;
        private double pitch;
        private double roll;
        private double height;
        private double geoX;
        private double geoY;
        private double geoZ;
        private double bodyX;
        private double bodyY;
        private double bodyZ;
        private double txerX;
        private double txerY;
        private double txerZ;

        /**
         * Initialize a Builder object with Sample data stored in a binary
         * input stream.
         * @param in the binary input stream.
         * @throws java.io.IOException if the underlying stream throws an exception.
         */
        public Builder(BinaryInputStream in) throws IOException
        {
            sampNum(in.readInt32());
            in.skipPadding(4);
            x(in.readReal64());
            y(in.readReal64());
            z(in.readReal32());
            amp(in.readUInt16());
            procAmp(in.readUInt16());
            status(in.readInt8());
            in.skipPadding(7);
        }

        /**
         * Initialize a Builder object with Sample data copied from another
         * Sample.
         * @param sample sample to copy.
         */
        public Builder(Sample sample)
        {
            // required fields are checked
            sampNum(sample.sampNum());
            x(sample.x());
            y(sample.y());
            z(sample.z());
            amp(sample.amp());
            procAmp(sample.procAmp());
            status(sample.status());

            // optional fields are accepted as-is
            depth = sample.depth();
            heading = sample.heading();
            pitch = sample.pitch();
            roll = sample.roll();
            height = sample.height();

            geoX = sample.geographicAxis().x();
            geoY = sample.geographicAxis().y();
            geoZ = sample.geographicAxis().z();

            bodyX = sample.bodyAxis().x();
            bodyY = sample.bodyAxis().y();
            bodyZ = sample.bodyAxis().z();

            txerX = sample.txerAxis().x();
            txerY = sample.txerAxis().y();
            txerZ = sample.txerAxis().z();
        }

        /**
         * Initialize a Builder object with Sample data copied from another
         * Sample.
         * @param sample sample to copy.
         */
        public Builder(Sample2 sample)
        {
            // required fields are checked
            sampNum(sample.sampNum());
            x(sample.x());
            y(sample.y());
            z(sample.z());
            amp(sample.amp());
            procAmp(sample.procAmp());
            status(sample.status());
            // Note I am skipping the tpu value here
            
            // optional fields are accepted as-is
            depth = sample.depth();
            heading = sample.heading();
            pitch = sample.pitch();
            roll = sample.roll();
            height = sample.height();

            geoX = sample.geographicAxis().x();
            geoY = sample.geographicAxis().y();
            geoZ = sample.geographicAxis().z();

            bodyX = sample.bodyAxis().x();
            bodyY = sample.bodyAxis().y();
            bodyZ = sample.bodyAxis().z();

            txerX = sample.txerAxis().x();
            txerY = sample.txerAxis().y();
            txerZ = sample.txerAxis().z();
        }

        /**
         * Initialize a Sample from scratch. These are the minimum required fields
         * to make a valid Sample.
         * @param sampNum the Sample number.
         * @param x the x-coordinate (northing) positive north (m)
         * @param y the y-coordinate (easting) positive east (m)
         * @param z the z-coordinate (depth) positive down (m)
         * @param amp unprocessed amplitude DN
         * @param procAmp processed amplitude DN
         * @param status sample status flag
         */
        public Builder(int sampNum, double x, double y, double z, int amp,
                int procAmp, int status)
        {
            sampNum(sampNum);
            x(x);
            y(y);
            z(z);
            amp(amp);
            procAmp(procAmp);
            status(status);
        }

        /**
         * Set the sample number
         * @param sampNum the sample number (0 to 2147483648L)
         * @throws SEABlockException for invalid sample numbers
         */
        public Builder sampNum(int sampNum)
        {
            if (sampNum < MIN_SAMPLES || MAX_SAMPLES < sampNum) {
                throw new IllegalArgumentException(
                        "MIN_SAMPLES: " + MIN_SAMPLES + " MAX_SAMPLES: " +
                        MAX_SAMPLES + " sampNum: " + sampNum);
            }
            this.sampNum = sampNum;
            return this;
        }

        /**
         * Set the x coordinate of this sample in the position axis set.
         * @param value the x coordinate (m)
         * @return the builder object.
         */
        public Builder x(double value)
        {
            this.x = value;
            return this;
        }

        /**
         * Set the y coordinate of this sample in the position axis set.
         * @param value the y coordinate (m).
         * @return the builder object
         */
        public Builder y(double value)
        {
            this.y = value;
            return this;
        }

        /**
         * Set the z coordinate of this sample in the position axis set.
         * @param value the z coordinate (m)
         * @return the builder object
         */
        public Builder z(double value)
        {
            this.z = value;
            return this;
        }

        /**
         * Set the raw amplitude DN
         * @param amp amplitude value (0 to 65536)
         * @return the builder object
         */
        public Builder amp(int amp)
        {
            if (amp < MIN_AMPLITUDE_DN || MAX_AMPLITUDE_DN < amp) {
                throw new IllegalArgumentException(
                        "MIN_AMPLITUDE_DN: " + MIN_AMPLITUDE_DN +
                        " MAX_AMPLITUDE_DN: " + MAX_AMPLITUDE_DN + " amp: " +
                        amp);
            }
            this.amp = amp;
            return this;
        }

        /**
         * Set the processed amplitude DN
         * @param procAmp processed amplitude value (0 to 65536)
         * @return the builder object
         */
        public Builder procAmp(int procAmp)
        {
            if (procAmp < MIN_AMPLITUDE_DN || MAX_AMPLITUDE_DN < amp) {
                throw new IllegalArgumentException(
                        "MIN_AMPLITUDE_DN: " + MIN_AMPLITUDE_DN +
                        " MAX_AMPLITUDE_DN: " + MAX_AMPLITUDE_DN + " procAmp: " +
                        procAmp);
            }
            this.procAmp = procAmp;
            return this;
        }

        /**
         * Set the sample status (0 = rejected by filters)
         * @param status status flag (0 to 255)
         * @return the builder object.
         */
        public Builder status(int value)
        {
            if (value < MIN_STATUS_FLAG || MAX_STATUS_FLAG < value) {
                throw new IllegalArgumentException(
                        "MIN_STATUS_FLAG: " + MIN_STATUS_FLAG +
                        " MAX_STATUS_FLAG: " + MAX_STATUS_FLAG + " status: " +
                        value);
            }
            this.status = value;
            return this;
        }

        public Builder depth(double value)
        {
            if (value < MIN_DEPTH || MAX_DEPTH < value) {
                throw new IllegalArgumentException(
                        "MIN_DEPTH: " + MIN_DEPTH + " MAX_DEPTH: " + MAX_DEPTH +
                        " depth: " + value);
            }
            this.depth = value;
            return this;
        }

        /**
         * Set the vessel roll value applied to this sample
         * @param value roll (degrees) positive for starboard down.
         * @return the builder object.
         */
        public Builder roll(double value)
        {
            assert (MIN_ROLL < value && value < MAX_ROLL);
            this.roll = value;
            return this;
        }

        /**
         * Set the heading value applied to this sample
         * @param value the heading value (degrees) positive east-of-north
         * @return the builder object
         */
        public Builder heading(double value)
        {
            assert (MIN_HEADING < value && value < MAX_HEADING);
            this.heading = value;
            return this;
        }

        /**
         * Set the pitch value applied to this sample.
         * @param value the pitch value (degrees) positive bow down.
         * @return the builder object
         */
        public Builder pitch(double value)
        {
            assert (MIN_PITCH < value && value < MAX_PITCH);
            this.pitch = value;
            return this;
        }

        /**
         * Set the height value applied to this sample
         * @param value the height value (m) positive down.
         * @return the builder object
         */
        public Builder height(double value)
        {
            assert (MIN_HEIGHT < value && value < MAX_HEIGHT);
            this.height = value;
            return this;
        }

        public Builder geoX(double x)
        {
            geoX = x;
            return this;
        }

        public Builder geoY(double y)
        {
            geoY = y;
            return this;
        }

        public Builder geoZ(double z)
        {
            geoZ = z;
            return this;
        }

        public Builder geographicAxis(SEAVertex geoAxis)
        {
            geoX = geoAxis.x();
            geoY = geoAxis.y();
            geoZ = geoAxis.z();
            return this;
        }

        public Builder bodyX(double x)
        {
            bodyX = x;
            return this;
        }

        public Builder bodyY(double y)
        {
            bodyY = y;
            return this;
        }

        public Builder bodyZ(double z)
        {
            bodyZ = z;
            return this;
        }

        public Builder bodyAxis(SEAVertex bodyAxis)
        {
            bodyX = bodyAxis.x();
            bodyY = bodyAxis.y();
            bodyZ = bodyAxis.z();
            return this;
        }

        public Builder txerX(double x)
        {
            txerX = x;
            return this;
        }

        public Builder txerY(double y)
        {
            txerY = y;
            return this;
        }

        public Builder txerZ(double z)
        {
            txerZ = z;
            return this;
        }

        public Builder txerAxis(SEAVertex txerAxis)
        {
            txerX = txerAxis.x();
            txerY = txerAxis.y();
            txerZ = txerAxis.z();
            return this;
        }

        public Sample build()
        {
            return new Sample(this);
        }
    }

    private Sample(Builder builder)
    {
        sampNum = builder.sampNum;
        x = builder.x;
        y = builder.y;
        z = builder.z;
        amp = builder.amp;
        procAmp = builder.procAmp;
        status = builder.status;
        depth = builder.depth;
        heading = builder.heading;
        pitch = builder.pitch;
        roll = builder.roll;
        height = builder.height;
        geoX = builder.geoX;
        geoY = builder.geoY;
        geoZ = builder.geoZ;
        bodyX = builder.bodyX;
        bodyY = builder.bodyY;
        bodyZ = builder.bodyZ;
        txerX = builder.txerX;
        txerY = builder.txerY;
        txerZ = builder.txerZ;
    }

    /**
     * Get the sample number.
     * @return sample number
     */
    public int sampNum()
    {
        return sampNum;
    }

    /**
     * Return the x coordinate of this sample in the position axis set.
     * @return the x coordinate (m)
     */
    public double x()
    {
        return x;
    }

    /**
     * Return the y coordinate of this sample in the position axis set.
     * @return the y coordinate (m)
     */
    public double y()
    {
        return y;
    }

    /**
     * Return the z coordinate of this sample in the position axis set.
     * @return the z coordinate (m)
     */
    public double z()
    {
        return z;
    }

    /**
     * Get the raw amplitude DN
     * @return DN value
     */
    public int amp()
    {
        return amp;
    }

    /**
     * Get the processed amplitude DN
     * @return amplitude value
     */
    public int procAmp()
    {
        return procAmp;
    }

    /**
     * Get the sample status flag.
     * @return status flag
     */
    public int status()
    {
        return status;
    }

    /**
     * Returns true if the point is flagged rejected
     * @return true if rejected
     */
    public boolean isRejected()
    {
        return status == Sample.SAMPLE_REJECTED;
    }

    /**
     * Returns the depth of the sample below the transducer.
     * @return
     */
    public double depth()
    {
        return depth;
    }

    public double roll()
    {
        return roll;
    }

    public double heading()
    {
        return heading;
    }

    public double pitch()
    {
        return pitch;
    }

    public double height()
    {
        return height;
    }

    public SEAVertex positionAxis()
    {
        return new SEAVertex(x, y, z);
    }

    public double geoX()
    {
        return geoX;
    }

    public double geoY()
    {
        return geoY;
    }

    public double geoZ()
    {
        return geoZ;
    }

    public SEAVertex geographicAxis()
    {
        return new SEAVertex(geoX, geoY, geoZ);
    }

    public double bodyX()
    {
        return bodyX;
    }

    public double bodyY()
    {
        return bodyY;
    }

    public double bodyZ()
    {
        return bodyZ;
    }

    public SEAVertex bodyAxis()
    {
        return new SEAVertex(bodyX, bodyY, bodyZ);
    }

    public double txerX()
    {
        return txerX;
    }

    public double txerY()
    {
        return txerY;
    }

    public double txerZ()
    {
        return txerZ;
    }

    public SEAVertex txerAxis()
    {
        return new SEAVertex(txerX, txerY, txerZ);
    }

    /**
     * Write an XYZAPoint structure to the binary output stream
     * @param out binary output stream
     * @throws IOException if underlying stream throws an exception.
     */
    public void writeTo(BinaryOutputStream out) throws IOException
    {
        out.writeInt32(sampNum);
        out.writePadding(4);
        out.writeReal64(x);
        out.writeReal64(y);
        out.writeReal32((float)z);
        out.writeUInt16(amp);
        out.writeUInt16(procAmp);
        out.writeInt8(status);
        out.writePadding(7);
    }

    @Override
    public String toString()
    {
        return String.format(
                "%d %.2f %.2f %.2f %d %d %d%n", 
                    sampNum, x, y, z, amp, procAmp, status);
    }

    /**
     * Returns the size of a Sample as stored in an SXP file (cXYZAPoint).
     * This number includes only the fields actually written to an SXP file:
     * SampNum, x, y, z, amp, procAmp, and Status.
     * @return
     */
    public static int blockSize()
    {
        return BLOCK_SIZE;
    }

    /**
     * Returns returns a copy of the input sample with its status set to
     * rejected.
     * @param sample the sample to reject
     * @return a new sample
     */
    public Sample newRejectedSample()
    {
        Sample.Builder builder = new Sample.Builder(this);
        builder.status(Sample.SAMPLE_REJECTED);
        return builder.build();
    }

    /**
     * Returns the distance to the given sample.
     * @parm sample to measure distance to (m)
     */
    public double distanceTo(Sample sample)
    {
        return Math.hypot(sample.x() - x, sample.y() - y);
    }
}

