package a2;

public class Rational {
	int numerator;
	int denominator;
	
	public Rational(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public Rational plus(Rational B) {
		int reduce = gcd(this.numerator*B.denominator + this.denominator*B.numerator , this.denominator*B.denominator);
		return new Rational((this.numerator*B.denominator + this.denominator*B.numerator)/reduce , (this.denominator*B.denominator)/reduce);
	}
	
	public Rational minus(Rational B) {//multiply denominators together then reduce. 
		int reduce = gcd(this.numerator*B.denominator - this.denominator*B.numerator , this.denominator*B.denominator);
		return new Rational((this.numerator*B.denominator - this.denominator*B.numerator)/reduce , (this.denominator*B.denominator)/reduce);
	}
	
	public Rational times(Rational B) {
		int reduce = gcd(this.numerator*B.numerator, this.denominator * B.denominator);
		return new Rational((this.numerator*B.numerator)/reduce, (this.denominator * B.denominator)/reduce);
	}
	
	public Rational divides(Rational B) {
		int reduce = gcd(numerator*B.denominator, denominator*B.numerator);
		return new Rational(numerator*B.denominator/reduce, denominator*B.numerator/reduce);
	}	
	
	public boolean equals(Object x){
		Rational B = (Rational) x;
		return (double)this.numerator/(double)this.denominator == (double)B.numerator/(double)B.denominator;
	}

	public static int gcd(int p, int q) {
		if (q==0) return p;
		int r = p % q;
		return gcd(q,r);
	}
	
	public String toString(){
		if(this.denominator < 0){
			return -this.numerator + "/" + -this.denominator;
		}
		return this.numerator + "/" + this.denominator;
	}
}

