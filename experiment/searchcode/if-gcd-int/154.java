package ce1002.E1.s100201510;

import java.util.Scanner;

public class E12 {
	public static void main(String[] args) {
		System.out.printf("a=");
		int a = new Scanner(System.in).nextInt();
		System.out.printf("b=");
		int b = new Scanner(System.in).nextInt();
		System.out.printf("gcd(%d,%d)=%d" , a , b , gcd( a , b ) );
	}
	
	// use Euclidean algorithm.
	public static int gcd(int x1 , int x2){
		int buff;
		while(x1 * x2 != 0){
			if(x1 < x2){
				buff = Math.max(x1, x2);
				x2 = x1;
				x1 = buff;
			}
			
			x1 = x1 % x2;
		}
		
		return Math.max(x1, x2);
	}
}

