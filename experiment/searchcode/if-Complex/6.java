package problems;

public class Complex {
	private Double real;
	private Double imaginary;
	
	public Complex(Double real, Double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public boolean equals (Object o) {
		if (this == o) return true;
		if ( (o == null) || (getClass() != o.getClass()) ) return false;
	
		Complex complex = (Complex) o;
		if ((Double.compare(this.real, complex.real)) != 0 || 
				(Double.compare(this.real, complex.real) != 0)) return false;
		return true;
	}
	
	public Complex add(Complex obj) {
		Complex complex = new Complex(obj.real, obj.imaginary);
		complex.real += this.real;
		complex.imaginary += this.imaginary;
		return complex;
	}
}

