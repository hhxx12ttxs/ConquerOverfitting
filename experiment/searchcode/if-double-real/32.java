public class Complex {
private double real;
private double imag;
public Complex(){
setReal(0);
setImag(0);
}
public Complex(double r, double i){
setReal(r);
setImag(i);
}

public static Complex add(Complex a, Complex b){

