
public class ComplexNumber {
private double realPart;
private double imaginaryPart;

public ComplexNumber(double r, double i){
if(r>0 &amp;&amp; r<(1.0/Math.pow(10, 15)))
r=0.0;
if(i>0 &amp;&amp; i<(1.0/Math.pow(10, 15)))

