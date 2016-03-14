package calc;

import dimension.PointsD;
import java.io.Serializable;
public class Complex implements Serializable{
	public static final int REAL=0;
	public static final int IMAG=1;
	
	public double real;
	public double imag;
	/*
	public static void main(String[] args) {
	System.out.println((new Complex(2,-2)).phase());
	}*/
	public Complex(){
		this.real=0d;
		this.imag=0d;
	}
	
	public Complex(double real,double imag){
		this.real=real;
		this.imag=imag;
	}
	
	public static Complex[] makeComplex(PointsD cp){
		Complex[] c=new Complex[cp.n];
		for(int i=0;i<c.length;i++){
			c[i]=new Complex(cp.pt[i].p[REAL],cp.pt[i].p[IMAG]);
		}
		return c;
	}
	
	public Complex(Complex a,String mathkey,Complex b){
		if(mathkey.equals("+")){
			this.real=a.real+b.real;
			this.imag=a.imag+b.imag;
		} else if(mathkey.equals("-")){
			this.real=a.real-b.real;
			this.imag=a.imag-b.imag;
		} else if(mathkey.equals("*")){
			this.real=a.real*b.real-a.imag*b.imag;
			this.imag=a.real*b.imag+a.imag*b.real;
		} else if(mathkey.equals("/")){
			if(b.real*b.real+b.imag*b.imag==0){
				System.out.print("Divide by zero at Complex division");
			} else{
				this.real=(a.real*b.real+a.imag*b.imag)/(b.real*b.real+b.imag*b.imag);
				this.imag=(a.imag*b.real-a.real*b.imag)/(b.real*b.real+b.imag*b.imag);
			}
		} else{
			System.out.println("complex no keisan ga chigau");
		}
	}
	
	public static Complex addition(Complex a,Complex b){
		double real=a.real+b.real;
		double imag=a.imag+b.imag;
		return new Complex(real,imag);
	}
	public static Complex subtraction(Complex a,Complex b){
		double real=a.real-b.real;
		double imag=a.imag-b.imag;
		return new Complex(real,imag);
	}
	public static Complex multiplition(Complex a,Complex b){
		double real=a.real*b.real-a.imag*b.imag;
		double imag=a.real*b.imag+a.imag*b.real;
		return new Complex(real,imag);
	}
	public static Complex division(Complex a,Complex b){
		double real=(a.real*b.real+a.imag*b.imag)/(b.real*b.real+b.imag*b.imag);
		double imag=(a.imag*b.real-a.real*b.imag)/(b.real*b.real+b.imag*b.imag);
		return new Complex(real,imag);
	}
	public static Complex[] getArray(int n){
		Complex[] c=new Complex[n];
		for(int i=0;i<n;i++)c[i]=new Complex();
		return c;
	}
	public static Complex[] additoin(Complex[] a,Complex[] b){
		int ns=Math.min(a.length,b.length);
		int nl=Math.max(a.length,b.length);
		Complex[] cpl=new Complex[nl];
		for(int i=0;i<ns;i++){
			cpl[i]=addition(a[i],b[i]);
		}
		return cpl;
	}
	public static Complex[] subtraction(Complex[] a,Complex[] b){
		int ns=Math.min(a.length,b.length);
		int nl=Math.max(a.length,b.length);
		Complex[] cpl=new Complex[nl];
		for(int i=0;i<ns;i++){
			cpl[i]=subtraction(a[i],b[i]);
		}
		return cpl;
	}
	public static Complex[] multiplition(Complex[] a,Complex[] b){
		int ns=Math.min(a.length,b.length);
		int nl=Math.max(a.length,b.length);
		Complex[] cpl=new Complex[nl];
		for(int i=0;i<ns;i++){
			cpl[i]=multiplition(a[i],b[i]);
		}
		return cpl;
	}
	public static Complex[] division(Complex[] a,Complex[] b){
		int ns=Math.min(a.length,b.length);
		int nl=Math.max(a.length,b.length);
		Complex[] cpl=new Complex[nl];
		for(int i=0;i<ns;i++){
			cpl[i]=division(a[i],b[i]);
		}
		return cpl;
	}
	public static double[] getReal(Complex[] c){
		double[] real=new double[c.length];
		for(int i=0;i<c.length;i++)real[i]=c[i].real;
		return real;
	}
	public static double[] getImag(Complex[] c){
		double[] imag=new double[c.length];
		for(int i=0;i<c.length;i++)imag[i]=c[i].imag;
		return imag;
	}
	public static double[][] getDouble(Complex[] c){
		double[][] dble=new double[2][c.length];
		for(int i=0;i<c.length;i++){
			dble[0][i]=c[i].real;
			dble[1][i]=c[i].imag;
		}
		return dble;
	}
	public static double[] getAbs(Complex[] c){
		double[] abs=new double[c.length];
		for(int i=0;i<c.length;i++)abs[i]=c[i].abs();
		return abs;
	}
	public static double[] getPow(Complex[] c){
		double[] pow=new double[c.length];
		for(int i=0;i<c.length;i++)pow[i]=c[i].pow();
		return pow;
	}
	
	public static Complex makeComplex_AbsPhase(double abs,double phase){
		return new Complex(abs*Math.cos(phase),-abs*Math.sin(phase));
	}
	
	public double abs(){
		return Math.sqrt(this.pow());
	}
	public double pow(){
		return (this.real*this.real+this.imag*this.imag);
	}
	public double phase(){
		return -Math.atan2(this.imag,this.real);
	}
	public double phase180(){
		return -Math.atan2(this.imag,this.real)*(180d)/(Math.PI);
	}
	public Complex exp(){
		double real=Math.exp(this.real)*Math.cos(this.imag);
		double imag=Math.exp(this.real)*Math.sin(this.imag);
		return new Complex(real,imag);
	}
	public Complex conjg(){
		double real=this.real+0d;
		double imag=-this.imag+0d;
		return new Complex(real,imag);
	}
	public static Complex[] conjg(Complex[] a){
		Complex[] c=new Complex[a.length];
		for(int i=0;i<a.length;i++)c[i]=a[i].conjg();
		return c;
	}
	
	public static void initialize(Complex[] a){
		for(int i=0;i<a.length;i++){
			a[i]=new Complex();
		}
	}
	public String toString(){
		return "("+Double.toString(this.real)+","+Double.toString(this.imag)+")";
	}
	public static String toString(Complex complex){
		return complex.toString();
	}
	public Complex clone(){
		double real=this.real;
		double imag=this.imag;
		return new Complex(real,imag);
	}
}

