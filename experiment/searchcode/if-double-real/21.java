* This is a class of complex numbers. Define real part, imaginary part and their computation.
*/
public class Complex {

private double real;
private double imag;

public Complex(double r, double i){
if (imag > 0) {
str = Double.toString(real) + &quot;+&quot; + Double.toString(imag) + &quot;i&quot;;
}
else if (imag==0) {

