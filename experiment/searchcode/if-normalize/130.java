package com.achaldave.myapplication2.app;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Achal on 4/9/14.
 *
 * Note:
 * Android's sensors have the following:
 *  - Going left in reality = cw in unit circle
 *  - Going up   in reality = cw in unit circle
 *
 *  This is counterintuitive, so keep it in mind.
 */
public class Orientation implements Comparable<Orientation> {
    private float pitch;
    private float yaw;

    public enum Direction {
        LEFT("left"),
        RIGHT("right"),
        UP("up"),
        DOWN("down");

        private String dir;
        private Direction(String dir) { this.dir = dir; }
        @Override
        public String toString() { return dir; }
    }

    public Orientation() { }

    public Orientation(float pitch, float yaw) {
        setOrientation(pitch, yaw);
    }

    public Orientation(float pitch, float yaw, boolean normalize) {
        setOrientation(pitch, yaw, normalize);
    }

    public Orientation(Orientation other) {
        setOrientation(other.pitch, other.yaw);
    }

    public Orientation(String serialized) {
        String[] split = serialized.split("\\|");
        float pitch = Float.parseFloat(split[0]);
        float yaw = Float.parseFloat(split[1]);
        setOrientation(pitch, yaw);
    }

    public void setOrientation(float pitch, float yaw) {
        setOrientation(pitch, yaw, true);
    }

    public void setOrientation(float pitch, float yaw, boolean normalize) {
        if (normalize) {
            pitch = normalize(pitch);
            yaw = normalize(yaw);
        }
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float distance(Orientation other) {
        return (float) Math.sqrt(square(horzDist(other)) + square(vertDist(other)));
    }

    public float horzDist(Orientation other) {
        return normalize(this.yaw - other.yaw);
    }

    public float vertDist(Orientation other) {
        return normalize(this.pitch - other.pitch);
    }

    public static float square(float x) { return x * x; }

    /* Direction to other from me. */
    public Direction getDirection(Orientation other) {
        /**
         * This is tricky.
         *
         * Remember:
         *  - Moving up   = moving clockwise in unit circle
         *  - Moving left = moving clockwise in unit circle
         *
         *  Therefore,
         *   - if other.pitch is further clockwise than this.pitch, other is UP   to this
         *   - if other.yaw   is further clockwise than this.yaw  , other is LEFT to this
         *
         *  Note that other.pitch - pitch gives us counterclockwise distance (i.e. negative implies cw).
         *
         *  Therefore,
         *   - if (other.pitch - pitch) < 0, other is UP   to this
         *   - if (other.yaw - yaw)     < 0, other is LEFT to this
         */
        float vert = normalize(other.pitch - pitch);
        float horz = normalize(other.yaw - yaw);
        Direction out;

        if (Math.abs(vert) > Math.abs(horz)) {
            out = vert < 0 ? Direction.UP : Direction.DOWN;
        } else {
            out = horz < 0 ? Direction.LEFT : Direction.RIGHT;
        }
        Log.d("Direction", String.format("%s is ***%s*** to %s", other, out, this));
        return out;
    }

    public Orientation sub(Orientation other) {
        return normalized(subRaw(other));
    }

    public Orientation add(Orientation other) {
        return normalized(addRaw(other));
    }

    public Orientation subRaw(Orientation other) {
        float subPitch = this.pitch - other.pitch;
        float subYaw = this.yaw - other.yaw;
        return new Orientation(subPitch, subYaw, false);
    }

    public Orientation addRaw(Orientation other) {
        float addPitch = this.pitch + other.pitch;
        float addYaw = this.yaw + other.yaw;
        return new Orientation(addPitch, addYaw, false);
    }

    public Orientation div(float val) {
        return new Orientation(pitch / val, yaw / val);
    }

    @Override
    public int compareTo(Orientation other) {
        /**
         * TODO: this might need to be negated for left to right
         *
         * Can't tell without testing.
         */
        return Math.round(this.yaw - other.yaw);
    }

    /**
     * Make x range between -PI and PI.
     */
    public static float normalize(float theta) {
        if (theta > Math.PI)
            theta = (float) (2*Math.PI - theta);
        else if (theta < -Math.PI)
            theta = (float) (2*Math.PI + theta);
        return theta;
    }

    public static Orientation normalized(Orientation ornt) {
        return new Orientation(normalize(ornt.pitch), normalize(ornt.yaw));
    }

    public static Orientation getAverage(ArrayList<Orientation> orientations) {
        Orientation avg = new Orientation(0,0);
        for (Orientation currOrnt : orientations) {
            avg = avg.addRaw(currOrnt);
        }
        avg = avg.div(orientations.size());
        return avg;
    }

    public String serialize() {
        return String.format("%f|%f", pitch, yaw);
    }

    public String toString() {
        return String.format("(%f, %f)", pitch, yaw);
    }
}

