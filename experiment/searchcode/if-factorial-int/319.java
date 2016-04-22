package stein.threads;

public class Factorial extends Thread {

	Integer a;

	@Override
	public void run() {
		super.run();
		System.out.println(factorial(n));
	}

	private final int n;

	public Factorial(int n) {
		this.n = n;
	}

	public int factorial(int a) {
		if (a == 0) {
			return 0;
		} else {
			if (a == 1) {
				return 1;
			}
		}
		return a * factorial(a - 1);
	}

}

