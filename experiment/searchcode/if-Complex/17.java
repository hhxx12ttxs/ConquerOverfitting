package util;

public class PolynomialComplex {
Complex[] a;
public PolynomialComplex(Complex[] a){
this.a=new Complex[a.length];
for (int i=0;i<a.length;i++){
this.a[i]=a[i].copy();

