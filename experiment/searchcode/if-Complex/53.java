package rnicolas.equation_solver;

/**
 * Created by rnicolas on 12/11/15.
 */
public class Complex {
    private double r;
    private double i;

    public Complex(double real, double imaginary) {
        r = real;
        i = imaginary;
    }

    public static Complex add (Complex a, Complex b) {
        return (new Complex(a.r + b.r, a.i + b.i));
    }

    public static Complex sub (Complex a, Complex b) {
        return (new Complex(a.r - b.r, a.i - b.i));
    }

    public static Complex mult (Complex a, Complex b) {
        return (new Complex(a.r * b.r - a.i * b.i, a.r * b.i + a.i * b.r));
    }

    public static Complex div (Complex a, Complex b) {
        return (new Complex((a.r * b.r + a.i * b.i) / (b.r * b.r + b.i * b.i),(a.i * b.r - a.r * b.i)/ (b.r * b.r + b.i * b.i)));
    }

    public static Complex sqrt (Complex a) {
        double mode = Math.sqrt(a.r * a.r + a.i * a.i);
        double angle;

        if (a.i >= 0)
            angle = Math.acos(a.r / mode);
        else
            angle = -Math.acos(a.r / mode);
        mode = Math.sqrt(mode);
        angle = angle / 2;
        return (new Complex(mode * Math.cos(angle), mode * Math.sin(angle)));
    }

    public static Complex pow(Complex a, double p) {
        double mode = Math.sqrt(a.r * a.r + a.i * a.i);
        double angle;

        if (a.i >= 0)
            angle = Math.acos(a.r / mode);
        else
            angle = -Math.acos(a.r / mode);
        mode = Math.pow(mode, p);
        angle = angle * p;
        return (new Complex(mode * Math.cos(angle), mode * Math.sin(angle)));
    }

    public double getI() {
        return i;
    }

    public double getR() {
        return r;
    }
}

