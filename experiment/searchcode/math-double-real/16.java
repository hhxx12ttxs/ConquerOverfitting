package math.complex;

/**
 * User: Marcin Matuszak
 * Date: 11/26/12
 */
public class ComplexNumber {

    private final double re;
    private final double im;


    public ComplexNumber(double real, double imag) {
        re = real;
        im = imag;
    }


    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im <  0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }


    public double abs()   { return Math.hypot(re, im); }
    public double phase() { return Math.atan2(im, re); }


    public ComplexNumber plus(ComplexNumber b) {
        ComplexNumber a = this;
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new ComplexNumber(real, imag);
    }


    public ComplexNumber minus(ComplexNumber b) {
        ComplexNumber a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new ComplexNumber(real, imag);
    }


    public ComplexNumber times(ComplexNumber b) {
        ComplexNumber a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new ComplexNumber(real, imag);
    }



    public ComplexNumber times(double alpha) {
        return new ComplexNumber(alpha * re, alpha * im);
    }


    public ComplexNumber conjugate() {  return new ComplexNumber(re, -im); }


    public ComplexNumber reciprocal() {
        double scale = re*re + im*im;
        return new ComplexNumber(re / scale, -im / scale);
    }


    public double re() { return re; }
    public double im() { return im; }


    public ComplexNumber divides(ComplexNumber b) {
        ComplexNumber a = this;
        return a.times(b.reciprocal());
    }


    public ComplexNumber exp() {
        return new ComplexNumber(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }


    public ComplexNumber sin() {
        return new ComplexNumber(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }


    public ComplexNumber cos() {
        return new ComplexNumber(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }


    public ComplexNumber tan() {
        return sin().divides(cos());
    }


    public static ComplexNumber plus(ComplexNumber a, ComplexNumber b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        ComplexNumber sum = new ComplexNumber(real, imag);
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplexNumber that = (ComplexNumber) o;

        if (Double.compare(that.im, im) != 0) return false;
        if (Double.compare(that.re, re) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = re != +0.0d ? Double.doubleToLongBits(re) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = im != +0.0d ? Double.doubleToLongBits(im) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

