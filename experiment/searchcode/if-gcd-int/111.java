package ce1002.E1.s101502515;

import java.util.Scanner;

public class E12 {
	static int gcd=0;//GCD???
	public static int GCD(int a,int b){//??
		if(a%b==0){
			gcd=b;
		}
		else{
			GCD(b,a%b);}//b>a%b ???
		return gcd;//??a%b==0 ??
	}
	public static void main(String args[]){
		Scanner input = new Scanner(System.in);
		System.out.print("a=");
		int a = input.nextInt();
		System.out.print("b=");
		int b = input.nextInt();
		System.out.print("gcd("+a+","+b+")="+GCD(a,b));
	}

}

