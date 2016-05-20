package ce1002.E1.s976002019;

import java.util.Scanner;

public class E12 {
	public static void main(String arg[]){
		int a, b;
		
		System.out.print("a=");
		Scanner input1 = new Scanner(System.in);/*??a*/
		a=input1.nextInt();
		System.out.print("b=");
		Scanner input2 = new Scanner(System.in);/*??b*/
		b=input2.nextInt();
		System.out.print("gcd("+a+","+b+")="+gcd(a,b));/*????*/
		
		
	}
	
	static int gcd(int a, int b){
		if(a-b==0){ /*???????*/
			return a;
		}
		else if (a-b<0){ /*??????*/
			return gcd(b, a);
		}
		else{				
		
		return gcd(a-b, b); 
		}
	}

}

