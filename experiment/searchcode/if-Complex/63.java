public class Complex {
private double a = 0;
private double b = 0;
public Complex(double real, double img){
public static double getMandelbrotEscapeVal(Complex c, int maxEscape){
Complex z = c;
for(int i = 0; i <= 100; i++){
if(z.a > 2 || z.b > 2)

