public final class Complex{
	
	private final double realPart;
	private final double imaginaryPart;

	public Complex(double realPart, double imaginaryPart){
		this.realPart = realPart;
		this.imaginaryPart  = imaginaryPart;
	}

	public Complex(String complexExpression){
		this.realPart = 2;
		this.imaginaryPart  = 3;
	}

	public double getRealPart(){
		return realPart;
	}

	public double getImaginaryPart(){
		return imaginaryPart;
	}

	public double Angle(){
		return Math.atan2(imaginaryPart,realPart);
	}

	public Complex Conjugate(Complex complexNumber){
		return new Complex(realPart,imaginaryPart);
	}

	public Complex Magnitude(Complex complexNumber){
		return new Complex(realPart,imaginaryPart);
	}

	/**
	*Add complex number Parameter to this conjugate
	*/
	public Complex Add(Complex complex){

		Complex addedComplex = new Complex(realPart+complex.getRealPart(),imaginaryPart+complex.getImaginaryPart());
		return addedComplex;
		//System.out.println("\n Sum of complex numbers " + realPart + "+" + imaginaryPart + "i and "  + complex.getRealPart() + "+" + complex.getImaginaryPart() + "i is "  + addedComplex.toString());
	
	}

	/**
	*Substracts complex number Parameter to this conjugate
	*/
	public Complex Substract(Complex complex){

		Complex substractedComplex = new Complex(this.realPart-complex.getRealPart(),this.imaginaryPart-complex.getImaginaryPart());
		return substractedComplex; 
	
	}

	/**
	*
	*/
	public Complex Divide(Complex complexToDivide){
			double modulus = Math.pow(complexToDivide.mod(),2);
			Complex dividedComplex = new Complex(((realPart*complexToDivide.getRealPart()+imaginaryPart*complexToDivide.getImaginaryPart())/modulus),(imaginaryPart*complexToDivide.getRealPart()-realPart*complexToDivide.getImaginaryPart())/modulus);
			return dividedComplex;
	}

	/**
	*Multiplies two Complex numbers and returns the result
	*/
	public Complex Multiply(Complex complexToMultiply){
		
		Complex productComplex = new Complex((realPart*complexToMultiply.getRealPart() - imaginaryPart*complexToMultiply.getImaginaryPart()), (realPart*complexToMultiply.getImaginaryPart() + imaginaryPart*complexToMultiply.getRealPart()));
		return productComplex;

	}
// //
// 	public double Add(Complex complexExpression){
		
// 	}

// 	//
// 	public double Substract(String complexExpression){
			
// 	}

// 	//
// 	public double Divide(String complexExpression){
			
// 	}

// 	//
// 	public double Multiply(String complexExpression){
			
// 	}

	/**
	*
	*/
	private double mod(){

		if (realPart!=0 || imaginaryPart!=0) {
            return Math.sqrt(realPart*realPart+imaginaryPart*imaginaryPart);
        } else {
            return 0d;
        }

	}

	private String complexNumRep(double realNumber,double imaginaryNumber){

		if (realNumber!=0 && imaginaryNumber>0) {
            return realNumber+" + "+imaginaryNumber+"i";
        }
        if (realPart!=0 && imaginaryPart<0) {
            return realNumber+" - "+(-imaginaryNumber)+"i";
        }
        if (imaginaryNumber==0) {
            return String.valueOf(realNumber);
        }
        if (realNumber==0) {
            return imaginaryNumber+"i";
        }
        return realNumber+" + i*"+imaginaryNumber;
	}

	public String toString(){
		return complexNumRep(realPart,imaginaryPart);
	}

}
