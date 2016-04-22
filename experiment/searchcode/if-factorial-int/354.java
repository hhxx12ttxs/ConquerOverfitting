package zad2;

public class FactorialRecursively  extends Thread {

	private int n;
	private long value;

	public FactorialRecursively(int n) {
		this.n = n;
	};
	
	public long getValue(){
		return value;
	}

	@Override
	public void run() {
		value = factorial(n);
	}

	private long factorial(int n) {
		if (n < 1)
			return 1;
		else
			return n * factorial(n - 1);
	}
	
}
