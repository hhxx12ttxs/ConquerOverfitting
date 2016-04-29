package core;

/**
 * Complex number type
 * 
 * This is an immutable data type, hence re and im are final. Changing 
 * them will result in a compile-time error.
 * 
 * @author 	Will Ridgers
 */
public class Complex implements MathsObject {
	
	/**
	 * The real part
	 */
	private final Rational re;
	
	/**
	 * The imaginary part
	 */
	private final Rational im;
	
	/**
	 * Default complex number constructor, gives 0 + 0i
	 * 
	 */
	public Complex() {
		re = new Rational();
		im = new Rational();
	}
	
	/**
	 * Complex number constructor, gives x + 0i
	 * 
	 * @param x	Real part as integer
	 */
	public Complex(Integer x) {
		re = new Rational(x);
		im = new Rational();
	}
	
	/**
	 * Complex number constructor, gives x + yi
	 * 
	 * @param x	Real part as integer
	 * @param y Imaginary part as integer
	 */
	public Complex(Integer x, Integer y) {
		re = new Rational(x);
		im = new Rational(y);
	}
	/**
	 * Complex number constructor, gives x + 0i
	 * 
	 * @param x Real part as Rational
	 * @param y Imaginary part as Rational
	 */
	public Complex(Rational x) {
		re = x;
		im = new Rational();
	}
	
	/**
	 * Complex number constructor, gives x + yi
	 * 
	 * @param x Real part as Rational
	 * @param y Imaginary part as Rational
	 */
	public Complex(Rational x, Rational y) {
		re = x;
		im = y;
	}

	/**
	 * Complex number constructor, gives x + yi
	 * 
	 * @param x Complex number
	 */
	public Complex(Complex x) {
		re = x.getReal();
		im = x.getImag();
	}
	
	/**
	 * Get real part of complex number
	 * 
	 * @return real part
	 */
	public Rational getReal() {
		return re;
	}
	
	/**
	 * Get imaginary part of complex number
	 * 
	 * @return imaginary part
	 */
	public Rational getImag() {
		return im;
	}
	
    /**
     * Add complex number and get result
     *
     * @param x Complex number to add
     * @return Result of addition
     */
	public Complex add(Complex x) {
		return new Complex(
			re.add(x.getReal()),
			im.add(x.getImag())
		);
	}
	
    /**
     * Subtract complex number and get result
     *
     * @param x Complex number to subtract
     * @return Result of subtraction
     */
	public Complex subtract(Complex x) {
		return new Complex(
			re.subtract(x.getReal()),
			im.subtract(x.getImag())
		);
	}
	
    /**
     * Multiply by complex number and get result
     *
     * @param x Complex number to multiply by
     * @return Result of multiplication
     */
	public Complex multiply(Complex x) {
		return new Complex(
			(re.multiply(x.getReal())).subtract(im.multiply(x.getImag())),
			(re.multiply(x.getImag())).add(im.multiply(x.getReal()))
		);
	}
	
	/**
	 * Divide by a complex number
	 * 
	 * @param x number to divide by
	 * @return  result of division as complex number
	 */
	public Complex divide(Complex x) {
		Rational denom = (x.getReal().multiply(x.getReal())).add(x.getImag().multiply(x.getImag()));
		
		return new Complex(
			((re.multiply(x.getReal())).divide(denom)).add((im.multiply(x.getImag())).divide(denom)),
			((im.multiply(x.getReal())).divide(denom)).subtract((re.multiply(x.getImag())).divide(denom))
		);
	}

	@Override
	public String getObjectName() { return "ComplexNumber"; }

	/**
	 * Get string representation of Complex number
	 * 
	 * @return 	value of complex number as string
	 */
	public String toString() {
		if (re.equals(0)) return im.toString() + "i";
		if (im.equals(0)) return re.toString();
		
		return "(" + re.toString() + " + " + im.toString() + "i)";
	}
}

