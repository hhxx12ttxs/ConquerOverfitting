package PolynomialLibrary;

public class ComplexNumber extends NumberField<ComplexNumber> {
	public double R,I;
	public ComplexNumber(double real, double imaginary) {
		R = real;
		I = imaginary;
	}
	@Override
	public ComplexNumber add(ComplexNumber x) {
		return new ComplexNumber(R + x.R, I + x.I);
	}

	@Override
	public ComplexNumber multiply(ComplexNumber x) {
		return new ComplexNumber(R * x.R - I * x.I, R * x.I + I * x.R);
	}
	
	@Override
	public String toString() {
		if (I < 0) return "{x:"+R+"-i*"+(-I)+"}";
		return "{x:"+R+"+i*"+I+"}";
	}

}

