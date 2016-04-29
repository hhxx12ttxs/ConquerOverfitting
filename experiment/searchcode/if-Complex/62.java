public class Complex {
	private double a = 0;
	private double b = 0;
	public Complex(double real, double img){
		a = real;
		b = img;
	}
	public Complex add(Complex c2){
		Complex c1 = this;
		double re = c1.a + c2.a;
		double re = c1.b + c2.b;
		return new Complex(re,im);
	}
	public Complex multiply(Complex c2){
		Complex c1 = this;
		double re = c1.a * c1.a - c1.b * c2.b;
		double im = c1.a * c2.b - c2.a * c1.b;
		return new Complex(re,im);
	}
	public static double getMandelbrotEscapeVal(Complex c, int maxEscape){
		Complex z = c;
		for(int i = 0; i <= 100; i++){
			if(z.a > 2 || z.b > 2)
				return (i > maxEscape) ? maxEscape : i;
			z += z.multiply(z);
		}
	}
};
