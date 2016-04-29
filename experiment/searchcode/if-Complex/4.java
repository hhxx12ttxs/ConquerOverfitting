/**
 * Craig Dazey
 * Dr. Kiper
 * 2-10-2015
 * CSE 211
 * Complex number class that is tested using TDD and junit
 * Assignment 3
 */
public class Complex {

    private double real, complex;

    public Complex()    {
        this.real = 0;
        this.complex = 0;
    }

    public Complex(double real, double complex)    {
        this.real = real;
        this.complex = complex;
    }

    public Complex add(Complex other)   {
        Complex c = new Complex();
        c.setReal(this.real + other.real);
        c.setComplex(this.complex + other.complex);
        return c;
    }

    public Complex subtract(Complex other)   {
        Complex c = new Complex();
        c.setReal(this.real - other.real);
        c.setComplex(this.complex - other.complex);
        return c;
    }

    public Complex multiply(Complex other)   {
        Complex c = new Complex();
        c.setReal((this.real * other.real) - (this.complex * other.complex));
        c.setComplex((this.real * other.complex) + (this.complex * other.real));
        return c;
    }

    public Complex divide(Complex other)   {
        Complex c = new Complex();
        c.setReal(((this.real * other.real) + (this.complex * other.complex)) / ((Math.pow(other.real, 2) + Math.pow(other.complex, 2))));
        c.setComplex(((this.complex * other.real) - (this.real * other.complex)) / ((Math.pow(other.real, 2) + Math.pow(other.complex, 2))));
        if(Double.isNaN(c.getReal()) || Double.isNaN(c.getComplex()))   {
            throw new IllegalArgumentException("Invalid input while dividing");
        }
        return c;
    }

    public double getReal() {
        return real;
    }

    public void setReal(double real) {
        this.real = real;
    }

    public double getComplex() {
        return complex;
    }

    public void setComplex(double complex) {
        this.complex = complex;
    }

    @Override
    public String toString()    {
        if(complex >= 0) {
            return String.format("%.3f+%.3fi", real, complex);
        } else  {
            return String.format("%.3f%.3fi", real, complex);
        }
    }
}

