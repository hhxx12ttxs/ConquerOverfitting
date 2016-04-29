public class Complex {

	float realNum, complexNum;
	//The default constructor
	public Complex() {
		realNum = 0;
		complexNum = 0;
	}
	//Constructor with custom values
	public Complex(float realNum, float complexNum) {
		this.realNum = realNum;
		this.complexNum = complexNum;
	}
	//Simple complex number addition
	public Complex add(Complex number) {
		Complex c = new Complex();
		c.realNum = this.realNum + number.realNum;
		c.complexNum = this.complexNum + number.complexNum;
		return c;
	}
	//Simple complex number subtraction
	public Complex subtract(Complex number) {
		Complex c = new Complex();
		c.realNum = this.realNum - number.realNum;
		c.complexNum = this.complexNum - number.complexNum;
		return c;
	}
	//Simple complex number multiplication
	public Complex multiply(Complex number) {
		Complex c = new Complex();
		c.realNum = (this.realNum * number.realNum)
				- (this.complexNum * number.complexNum);
		c.complexNum = (this.complexNum * number.realNum)
				+ (this.realNum * number.complexNum);
		return c;
	}
	//Simple complex number division
	public Complex divide(Complex number) {
		Complex c = new Complex();
		c.realNum = (float) (((this.realNum * number.realNum) + (this.complexNum * number.complexNum)) / (Math
				.pow(number.realNum, 2) + Math.pow(number.complexNum, 2)));
		c.complexNum = (float) (((this.complexNum * number.realNum) - (this.realNum * number.complexNum)) / (Math
				.pow(number.realNum, 2) + Math.pow(number.complexNum, 2)));
		return c;
	}
	//The toString method, returns the complex number as a string. 
	@Override
	public String toString() {
		if(complexNum>0)
			return (realNum + "+" + complexNum + "i");
		else
			return (realNum + "" + complexNum + "i");

	}
}

