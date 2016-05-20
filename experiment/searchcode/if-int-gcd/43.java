package ce1002.E1.s100201021;
import java.util.Scanner;
public class E12 {
	public static void main(String[] args){
		Scanner input = new Scanner(System.in);
		int ap,a,bp,b,gcd;	//input ap,bp
		System.out.print("a=");
		ap=input.nextInt();
		System.out.print("b=");
		bp=input.nextInt();
		a = ap;
		b = bp;
		if(b>a){
			gcd = b;
			b = a;
			a = gcd;
		}
		gcd = 1;
		for(int i=b;i>1;i--){
			if(a%i==0 && b%i==0){
				gcd *= i;
				a/=i;
				b/=i;
			}
		}
		//output answer
		System.out.print("gcd("+ap+","+bp+")="+ gcd);	
	}
}

