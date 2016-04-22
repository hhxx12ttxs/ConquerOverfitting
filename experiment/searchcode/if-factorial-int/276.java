/*
 * ����� �޼ҵ� ����
 * 
 */
public class Book6_3 {
	
	public static void main(String[] args) {
		
		System.out.println("3 factorial : " + factorial(3));
		System.out.println("12 factorial : " + factorial(12));
		
	}
	
	public static int factorial (int num) {
		
		if(num==1) {
			return 1;
		} else 
			return num * factorial(num-1);
	}

}

