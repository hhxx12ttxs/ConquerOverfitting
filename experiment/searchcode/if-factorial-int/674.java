package matchers;

public class MatcherClass {
	
	public int factorial(int num) {
		if ( num <= 2)
			return  num;
		return num * factorial(num-1);
	}
	
}

