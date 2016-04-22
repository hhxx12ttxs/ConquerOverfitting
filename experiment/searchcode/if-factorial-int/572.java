package factorial;

/*
* ����ʱ�䣺2015��11��4�� ����10:08:43
* ��Ŀ��ƣ�ThinkingInRecursion
* @author Zengchao.Geng
* @since JDK 1.6.0
* �ļ���ƣ�FactorialTest.java
* ��˵����һ��������Ľ׳�/�㣨Ӣ�factorial��������С�ڼ����ڸ����������Ļ�
* 		������0�Ľ׳�Ϊ1����Ȼ��n�Ľ׳�д��n!��5! = 5x4x3x2x1 = 120.
*/
public class FactorialTest {
	public static void main(String[] args) {
		
		int result = factorial(5);
		
		System.out.println(result);
	}
	//�õݹ����� n!= n * (n-1)!
	public static int factorial(int n) {
		//n����1 �ǳ��ڡ�
		if (1 == n) {
			return 1;
		} else {
			return n * factorial(n - 1);
		}
	}

}

