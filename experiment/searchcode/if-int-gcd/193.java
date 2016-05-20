package ce1002.E1.s101502002;

import java.util.Scanner;
public class E12 {
	public static void main(String[]args){
		Scanner input=new Scanner(System.in);
		
		System.out.print("a=");
		int a =input.nextInt();
		
		System.out.print("b=");
		int b=input.nextInt();
		
		int c=GCD(a,b);
		System.out.println("gcd("+a +","+b +")="+c);
	}


	public static int GCD(int a,int b)//?????????
	{
		int c;
		if(a<b)
		{
			int buffer=a;
			a=b;
			b=buffer;
		}
     	while( b != 0 )//?????
     	{
         	c = a%b;
         	a = b;
         	b = c;
     	}
     return a;
 	}
}
