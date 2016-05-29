package cn.fox.math;

public class Function {
public static double exp(double x) {
if(x>50) x=50;
else if(x<-50) x=-50;
return Math.exp(x);
}

public static double tanh(double x) {
return (exp(x)-exp(-x))/(exp(x)+exp(-x));

