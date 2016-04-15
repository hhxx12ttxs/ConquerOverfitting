package com.d45labs.shapedetector.fourier;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * represents an complex number as BigDecimal 
 * some methods has presicion loss.
 * @author osvaldo
 */
public class Complex {

    private BigDecimal real;
    private BigDecimal img;

    public Complex() {
    }

    public Complex(BigDecimal real, BigDecimal img) {
        this.real = real;
        this.img = img;
    }

    public Complex(double real, double img) {
        this.real = new BigDecimal(real);
        this.img = new BigDecimal(img);
    }

    public BigDecimal getImg() {
        return img;
    }

    public void setImg(BigDecimal img) {
        this.img = img;
    }

    public BigDecimal getReal() {
        return real;
    }

    public void setReal(BigDecimal real) {
        this.real = real;
    }

    @Override
    public String toString() {
//                return real + "+" + img + 'i';
        return DecimalFormat.getInstance().format(real) + "+" + DecimalFormat.getInstance().format(img) + 'i';
//        String rs = real.toString();
//        String is = img.toString();
//        return (rs.contains(".")?rs.toString().substring(0,rs.indexOf(".")+5):rs) + (img.doubleValue()>0 ?"+":"") + (is.contains(".")?is.toString().substring(0,is.indexOf(".")+5):is) + 'i';
    }

    /**
     * adds to this complex another complex.
     * @param complex 
     */
    public Complex sum(Complex complex) {
        BigDecimal realb = this.real.add(complex.getReal());
        BigDecimal imgb = this.img.add(complex.getImg());
        return new Complex(realb, imgb);
    }

//    public void substract(Complex complex) {
//        this.real = this.real.subtract(complex.getReal());
//        this.img = this.img.subtract(complex.getImg());
//    }

    /**
     * multilply this complex by an scalar 
     * @param scalar 
     */
    public Complex scalar(BigDecimal scalar) {
        this.real = this.real.multiply(scalar);
        this.img = this.img.multiply(scalar);
        return new Complex(real, img);
    }

    /**
     * multiplies by a complex
     * @param complex 
     */
    public Complex multiply(Complex complex) {
//                BigDecimal r1 = BigDecimal.ONE;
//                r1 = r1.multiply(this.real).multiply(complex.getReal());
//                BigDecimal r2 = BigDecimal.ONE;
//                r2 = r2.multiply(this.img).multiply(complex.getImg());
//        
//                BigDecimal i1 = BigDecimal.ONE;
//                i1 = i1.multiply(this.real).multiply(complex.getImg());
//                BigDecimal i2 = BigDecimal.ONE;
//                i2 = i2.multiply(this.img).multiply(complex.getReal());
//                this.real = r1.subtract(r2);
//                this.img = i1.add(i2);
        double a = this.real.doubleValue();
        double b = this.img.doubleValue();
        double c = complex.getReal().doubleValue();
        double d = complex.getImg().doubleValue();

        this.real = new BigDecimal((a * c) - (b * d));
        this.img = new BigDecimal((a * d) + (b * c));
        return new Complex(real, img);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Complex other = (Complex) obj;
        if (this.real != other.real && (this.real == null || !this.real.equals(other.real))) {
            return false;
        }
        if (this.img != other.img && (this.img == null || !this.img.equals(other.img))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.real != null ? this.real.hashCode() : 0);
        hash = 17 * hash + (this.img != null ? this.img.hashCode() : 0);
        return hash;
    }

    /**
     * computes modulus
     * @return 
     */
    public BigDecimal mod() {
        return new BigDecimal(Math.sqrt((real.pow(2).add(img.pow(2))).doubleValue()));
    }

    /**
     * computes phi
     * @return 
     */
    public BigDecimal arg() {
        return new BigDecimal(Math.atan2(this.img.doubleValue(), this.real.doubleValue()));
    }

    /**
     * computes this complex as: e^(i@)= cos(@) +i sin(@)
     * @param img it means @
     * @return 
     */
    public static Complex euler(double img) {
        double reald = Math.cos(img);
        double imgd = Math.sin(img);
        return new Complex(reald, imgd);
    }
}
