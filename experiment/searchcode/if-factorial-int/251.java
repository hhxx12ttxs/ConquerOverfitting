package homework7_8;

public class Main {

	public static void main(String[] args) {
		System.out.println(factorial(15, 1));
	}

	public static final long factorial(int range, long factorial) {
		if (range > 0) {
			factorial *= range;
			range--;
			return factorial(range, factorial);
		}
		return factorial;
	}
}


