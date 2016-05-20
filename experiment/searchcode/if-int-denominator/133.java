import java.math.BigInteger;


public class BigRational implements Comparable, ArithmeticObject {
	private BigInteger numerator;
	private BigInteger denominator;
	/**
	 * Creates a new rational number
	 * @param numer numerator of the bigrational
	 * @param denom denominator of the bigrational
	 */
	public BigRational(BigInteger numer, BigInteger denom){
		this.numerator = numer;
		this.denominator = denom;
		if (this.denominator.equals(BigInteger.ZERO)){
			throw new ArithmeticException( "Divide by zero" );
		}
	}
	/**
	 * returns numerator of the BigRational
	 * @return numerator of the BigRational
	 */
	public BigInteger getNumerator(){
		return this.numerator;
	}
	/**
	 * returns denominator of the BigRational
	 * @return the denomoninator of the BigRational
	 */
	public BigInteger getDenominator() {
		return this.denominator;
	}
	/**
	 * Reduces the BigRational into its lowest terms
	 * @return the simplfied BigRational
	 */
	public BigRational reduce(){
		BigInteger divisor;
		divisor = this.numerator.gcd(this.denominator);
		if (divisor == BigInteger.ONE)
			return this;
		else
			return new BigRational(this.numerator.divide((BigInteger) divisor), this.denominator.divide((BigInteger) divisor));
	}
	/**
	 * return the BigRational as a String
	 * @return a String representation of the BigRational
	 */
	public String toString(){
		return (this.numerator + "/" + this.denominator);
	}
	/**
	 * Test to see if this BigRational is equal to another one
	 * @return true if they are equal, and false if they're not
	 */
	public boolean equals(Object arg0) {
		BigRational value = (BigRational) arg0;
		value.reduce();
		this.reduce();
		
		if (this.numerator.equals(value.numerator) && this.denominator.equals(value.denominator))
			return true;
		else
			return false;
	}
	/**
	 * See if this BigRational is bigger, smaller, or equal to another one
	 * @return 0 if they are equal, 1 if this is greater, and -1 if this is smaller
	 */
	public int compareTo(Object arg0) {
		BigRational value = (BigRational) arg0;
		this.reduce();
		value.reduce();
		if (this.numerator.multiply(value.denominator).equals(this.denominator.multiply(value.numerator)))
			return 0;
		else if(this.numerator.multiply(value.denominator).compareTo(this.denominator.multiply(value.numerator)) == 1)
			return 1;
		else
			return -1;
	}
	/**
	 * Take the absolute value of the BigRational
	 * @return a new BigRational
	 */
	public Object abs() {
		this.numerator = this.numerator.abs();
		this.denominator = this.denominator.abs();
		
		//return new BigRational(this.numerator, this.denominator).reduce();
		return this.reduce();
	}
	/**
	 * Add two BigRationals together
	 * @return a new BigRational that is the addition of the other two
	 */
	public Object add(Object other) {
		BigRational value = (BigRational) other;
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		numer = numer.multiply(value.denominator).add(value.numerator.multiply(denom));
		denom = denom.multiply(value.denominator);
		
		return new BigRational(numer, denom).reduce();
	}
	/**
	 * Divide a BigRational by another one
	 * @return a new BigRational that is the division of the two
	 */
	public Object divide(Object other) throws ArithmeticException {
		BigRational value = (BigRational) other;
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		numer = numer.multiply(value.denominator);
		denom = denom.multiply(value.numerator);
		
		return new BigRational(numer, denom).reduce();
	}
	/**
	 * Multiply two BigRationals together
	 * @return a BigRational that is the multiplication of two
	 */
	public Object multiply(Object other) {
		BigRational value = (BigRational) other;
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		numer = numer.multiply(value.numerator);
		denom = denom.multiply(value.denominator);
		
		return new BigRational(numer, denom).reduce();
	}
	/**
	 * Negate the BigRational
	 * @return A BigRational that is the opposite of the BigRational
	 */
	public Object negate() {
		this.reduce();
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;

		if (numer.compareTo(BigInteger.ZERO) == -1)
			numer.negate();
		else if(denom.compareTo(BigInteger.ZERO) == -1)
			denom.negate();
		else
			numer.negate();
		
		return new BigRational(numer, denom).reduce();
	}
	/**
	 * Subtract A BigRational from another one
	 * @return A BigRational that is created from the subtraction
	 */
	public Object subtract(Object other) {
		BigRational value = (BigRational) other;
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		numer = numer.multiply(value.denominator).subtract(value.numerator.multiply(denom));
		denom = denom.multiply(value.denominator);
		
		return new BigRational(numer, denom).reduce();
	}
	/**
	 * Raises the BigRational to a power
	 * @param power power to raise the Bigrational to
	 * @return the BigRational raised to the specified power
	 */
	public Object power(int power){
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		numer = numer.pow(power);
		denom = denom.pow(power);
		
		return new BigRational(numer, denom).reduce();
	}
	/**
	 * Returns the reciprocal of the BigRational
	 * @return the reciprocal of the BigRational
	 */
	public Object reciprocal(){
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		return new BigRational(denom, numer).reduce();
	}
	/**
	 * Returns the modular division of the BigRational
	 * @return the modular division of the BigRational
	 */
	public Object remainder(){
		BigInteger numer = this.numerator;
		BigInteger denom = this.denominator;
		
		return numer.mod(denom);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BigInteger numer = new BigInteger("6");
		BigInteger denom = new BigInteger("8");
		BigRational value = new BigRational(numer, denom);
		
		System.out.println(value.reduce().toString());

	}

}

