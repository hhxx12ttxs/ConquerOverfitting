import java.math.BigInteger;

public class Main {
	public static void main(String[] args) {
		new Main();
	}
	
	Main() {
		BigInteger oneMillion = new BigInteger("1000000");
		BigInteger[] factorial = new BigInteger[101];
		
		factorial[0] = factorial[1] = BigInteger.ONE;
		
		for(int i = 2; i < factorial.length; ++i) {
			factorial[i] = factorial[i - 1].multiply(new BigInteger("" + i));
		}
		
		int count = 0;
		
		for(int i = 1; i <= 100; ++i) {
			for(int j = 2; j <= i - 1; ++j) {
				BigInteger n = factorial[i];
				BigInteger r = factorial[j];
				BigInteger c = factorial[i - j];
				
				if(n.divide(r.multiply(c)).compareTo(oneMillion) > 0) {
					++count;
				}
			}
		}
		
		System.out.println(count);
	}
}

