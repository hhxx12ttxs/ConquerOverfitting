package Factorial4;
public class FactorialTest {
//4. �������� ���������� ���������� ����� n ����� �������� ++
	public static void main(String[] args) {
	Factorial factorialObj=new Factorial();
	for(int t=-4;t<10;t++){
		int result=factorialObj.countFactorial(t);
		if (result!=-1)
		System.out.println("factorial of "+ t + "="+factorialObj.countFactorial(t));
		else 	System.out.println("Sorry, there is no factorial of negative values like "+ t );
	}
  }
}

