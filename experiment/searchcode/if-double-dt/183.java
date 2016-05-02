package org.cmc.ceresminingco.math2;

import java.util.Collection;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Aaron M. Rawson
 */
public class State {

    static final double G = 1e1;
    static final double RSoften = 1e-6;
    double x;
    double y;
    double z;
    double vx;
    double vy;
    double vz;
    double m;
    double mu;
    int id;

    public State(double x, double y, double z, double vx, double vy, double vz, double m, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.m = m;
        this.mu = m * G;
        this.id = id;
    }

    State(State source) {
        x = source.x;
        y = source.y;
        z = source.z;
        vx = source.vx;
        vy = source.vy;
        vz = source.vz;
        m = source.m;
        mu = source.mu;
        id = source.id;
    }

    public void storeLoc(Vector3f dest) {
        dest.x = (float) x;
        dest.y = (float) y;
        dest.z = (float) z;
    }

    public Vector3f getPosNew() {
        return new Vector3f((float) x, (float) y, (float) z);
    }

    public void setPos(double setx, double sety, double setz) {
        x = setx;
        y = sety;
        z = setz;
    }

    public float getZ() {
        return (float)z;
    }

    public float getY() {
        return (float)y;
    }

    public float getX() {
        return (float)x;
    }

    public double getFullX() {
        return x;
    }

    public double getFullY() {
        return y;
    }

    public double getFullZ() {
        return z;
    }

    public static class Derivative {

        double dx;
        double dy;
        double dz;
        double dvx;
        double dvy;
        double dvz;
    }

    static void gravity(Collection<State> universe, State state, Derivative output) {
        for (State other : universe) {
            //nope, im dumb
            if (other.id != state.id) {
                final double deltaX = other.x - state.x;
                final double deltaY = other.y - state.y;
                final double deltaZ = other.z - state.z;

                double R2 = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                R2 += RSoften;

                final double phi = Math.atan2(deltaY, deltaX);
                final double theta = Math.acos(deltaZ / Math.sqrt(R2));
                final double magnitude = other.mu / R2;

                output.dvx += Math.sin(theta) * Math.cos(phi) * magnitude;
                output.dvy += Math.sin(theta) * Math.sin(phi) * magnitude;
                output.dvz += Math.cos(theta) * magnitude;
            }
        }
    }

    public static void integrate(
            Integrator integrator,
            Collection<State> universe,
            double t,
            double dt) {
        integrator.step(universe, t, dt);
    }

}

