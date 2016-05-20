package esami2013.appello02.sol1;

/**
 * An Iterator<Long> generating a piece of Fibonacci's series 
 */

public class Fibonacci implements java.util.Iterator<Long> {
	
	private int l;
	private long prev = 0;
	private long cur = 0;

	public Fibonacci(int length) {
		l = length;
	}
	
	@Override
	public boolean hasNext() {
		return l > 0;
	}

	@Override
	public Long next() {
		l--;
		if(cur == 0) {
			cur = 1;
			return cur;
		}
		cur += prev;
		prev = cur - prev;
		return cur;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	

}

