package util;

public class Complex{
double a,b;

public Complex(double a,double b){
this.a=a;
public Complex divide(Complex other){
double norm=other.norm();
Complex result=other.conjugate().multiply(this);
if (norm==0){

