package JavaTasksUniversityExercise4;

public class Task5 {
	public static void main(String[] args) {
		System.out.println("factorial via recursion: " + factorialRecursion(5));
		System.out.println("factorial via loop: " + loopFactorial(5));
	}

	public static int factorialRecursion(int n) {
		if (n == 1) {
			return 1;
		} else
			return n * factorialRecursion(n - 1);

	}

	public static int loopFactorial(int n) {

		int factorial = 1;
		for (int i = 1; i <= n; i++) {
			factorial *= i;
		}
		return factorial;
	}
}


