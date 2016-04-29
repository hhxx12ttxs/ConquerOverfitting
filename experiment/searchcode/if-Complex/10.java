package util;

public class Complex{
	double a,b;
	
	public Complex(double a,double b){
		this.a=a;
		this.b=b;
	}
	
	public Complex add(Complex other){
		Complex result=new Complex(a+other.a,b+other.b);
		return result;
	}
	
	public Complex subtract(Complex other){
		Complex result=new Complex(a-other.a,b-other.b);
		return result;
	}
	
	public Complex multiply(Complex other){
		Complex result=new Complex(a*other.a-b*other.b,a*other.b+b*other.a);
		return result;
	}
	
	public Complex divide(Complex other){
		double norm=other.norm();
		Complex result=other.conjugate().multiply(this);
		if (norm==0){
			result.a=1;
			result.b=0;
		}else{
			result.a/=norm;
			result.b/=norm;
		}
		return result;
	}
	
	public double norm(){
		return a*a+b*b;
	}
	
	public Complex conjugate(){
		return new Complex(a,-b);
	}
	
	public Complex copy(){
		return new Complex(a,b);
	}
	
	public String toString(){
		String s=""+a+" "+b+"i";
		return s;
	}
}
