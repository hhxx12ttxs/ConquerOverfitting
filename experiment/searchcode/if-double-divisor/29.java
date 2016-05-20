package org.krsbuilt.universe.libs.physics;

import java.lang.Math;
/**
 * Created by IntelliJ IDEA.
 * User: Kyle
 * Date: 6/5/11
 * Time: 7:10 PM
 * ...
 */
public class Scalar {
    protected double mag;

    public Scalar() {
        this.mag = 0;
    }

    public Scalar(double mag) {
        this.mag = mag;
    }

    public double getMag() {
        return this.mag;
    }

    public Scalar negative() {
        return new Scalar(-this.mag);
    }

    public Scalar add(Scalar addScalar) {
        return new Scalar(this.mag + addScalar.mag);
    }

    public Scalar subtract(Scalar subtractScalar) {
        return this.add(subtractScalar.negative());
    }

    public Scalar multiply(double multiplier) {
        return new Scalar(this.mag * multiplier);
    }

    public Scalar multiply(Scalar multiplier) {
        return multiply(multiplier.mag);
    }

    public Scalar divide(double divisor) {
        return multiply(Math.pow(divisor, -1.0));
    }

    public Scalar divide(Scalar divisor) {
        return divide(divisor.mag);
    }

    public boolean equals(Object comparisonObject) {
        if (comparisonObject instanceof Scalar)
            return this.mag == ((Scalar) comparisonObject).getMag();
        else
            return false;
    }

    public Vector toVector() {
        return new Vector(this.mag);
    }

    public Energy toEnergy() {
        return new Energy(this.mag);
    }

    public Power toPower() {
        return new Power(this.mag);
    }

    public Mass toMass() {
        return new Mass(this.mag);
    }

    public Distance toDistance() {
        return new Distance(this.mag);
    }
}

