package trace_quiz.sp03;

import obpro.cui.Input;

/**
 * �v���O�������F FactorialCalculator �K����v�Z����v���O����
 * �쐬�ҁF Rintal Takeda
 * �o�[�W�����F 1.0 (2005/5/11) 
 */

public class FactorialCalculatorAnswer {

	public static void main(String[] args) {
		FactorialCalculatorAnswer factorialCalculatorAnswer = new FactorialCalculatorAnswer();
		factorialCalculatorAnswer.main();
	}

	// �K����v�Z����
	void main() {
		// �������͂���
		int number;
		System.out.println("�K��̌v�Z�����܂��D�������͂��ĉ�����>>");
		number = Input.getInt();
		
		// �K����v�Z����
		int answer;
		answer = factorial(number);
		
		// �v�Z���ʂ��o�͂���
		System.out.println("Factorial=" + answer);
	}

	// �K����v�Z����
	int factorial(int n) {
		if(n == 1) {
			return 1;
		} else {
			return (n * factorial(n - 1));
		}
	}
}

