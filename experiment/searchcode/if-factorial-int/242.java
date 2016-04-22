package trace_quiz.sp03;

import obpro.cui.Input;

/**
 * �v���O�������F 
 * �쐬�ҁF 
 * �o�[�W�����F 1.0 () 
 */

public class FactorialCalculator {

	public static void main(String[] args) {
		FactorialCalculator factorialCalculator = new FactorialCalculator();
		factorialCalculator.main();
	}

	// 
	void main() {
		// 
		int number;
		System.out.println("�K��̌v�Z�����܂��D�������͂��ĉ�����>>");
		number = Input.getInt();
		
		// 
		int answer;
		answer = factorial(number);
		
		// 
		System.out.println("Factorial=" + answer);
	}

	// 
	int factorial(int n) {
		if(n == 1) {
			return 1;
		} else {
			return (n * factorial(n - 1));
		}
	}
}

