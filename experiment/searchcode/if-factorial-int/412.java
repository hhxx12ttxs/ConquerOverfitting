package common.util.algorithms.numberTheory;

import java.math.BigInteger;

public class Factorial {
	
	public static BigInteger factorial(int n) {		
		BigInteger factorial = BigInteger.ONE;
		if(n==0 || n==1) return factorial;
		for(int i=2; i<=n; i++) {
			factorial = factorial.multiply(BigInteger.valueOf(i));
		}
		return factorial;		
	}
	
	public static void main(String[] args) {
		BigInteger x = factorial(40);
		System.out.println(x);
	}

}

