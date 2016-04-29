package util;

public class PolynomialComplex {
	Complex[] a;
	public PolynomialComplex(Complex[] a){
		this.a=new Complex[a.length];
		for (int i=0;i<a.length;i++){
			this.a[i]=a[i].copy();
		}
	}
	
	public Complex evaluate(Complex x){
		Complex value=new Complex(0,0);
		Complex var=new Complex(1,0);
		for (int i=0;i<a.length;i++){
			value=value.add(a[i].multiply(var));
			var=var.multiply(x);
		}
		return value;
	}

	public int getDegree(){
		for (int i=a.length-1;i>=0;i--){
			if (a[i].a!=0 || a[i].b!=0){
				return i;
			}
		}
		return Integer.MIN_VALUE;
	}
}
