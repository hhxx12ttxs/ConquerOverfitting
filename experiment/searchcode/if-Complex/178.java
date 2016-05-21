public class Complex {
public Complex(double real, double imag) {
this.real = real;
public String toString() {
if(imag<0) {
return new String(real+\" - i\"+Math.abs(imag));
public static final Complex multiply(Complex c1, Complex c2) {
double re = c1.real*c2.real - c1.imag*c2.imag;
double im = c1.real*c2.imag + c1.imag*c2.real;
return new Complex(re, im);
}

