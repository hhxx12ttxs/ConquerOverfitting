package ce1002.E1.s101502017;
import java.util.*;
public class E12 {
	public static void main(String[] args) {
		int a, b,c,d=0,e=0;
		Scanner input = new Scanner(System.in);
		System.out.print("a = ");
		a = input.nextInt();
		System.out.print("b = ");
		b = input.nextInt();
		d=a;
		e=b;
		c = GCD(a,b);
			
		System.out.print("gcd("+ d +","+ e +") = "+ c);
	}
	public static int GCD(int a, int b) {
		   if (b==0) return a;
		   return GCD(b,a%b);
		}

}

