package recitation1;

public class MathClass implements Sum, Factorial, NumberHolder {
	int n;

	@Override
	public void setNumber(int newNumber) {
		n = newNumber;
	}

	@Override
	public int getNumber() {
		return n;
	}

	@Override
	public int getFactorial() {
		if (n==0 || n==1) return 1;
		int factorial = n;
		for (int i = n-1 ;i > 1;i--) {
			factorial = factorial * i;
		}
		return factorial;
	}

	@Override
	public int getSum() {
		if (n==0) return 0;
		if (n==1) return 1;
		int sum = n;
		for (int i = n-1 ;i > 0;i--) {
			sum += i;
		}
		return sum;
	}
}
