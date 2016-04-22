import java.util.Scanner;
public class Zadacha8 {

	public static void main(String[] args) {
Scanner scanner = new Scanner(System.in);
System.out.pritnln("enter n and k ");
int n = scanner.nextInt();
int k = scanner.nextInt();
int factorial_n1 = 1;
int factorial_k1 = 1;
int factorial_n1k1 = 1;

for (int i = 1; i <= n; i++){
	if( n == 0){
		factorial_n1 = 1;
	}
	factorial_n1 *= i;
}
for (int z = 1; z <= k; z++){
	if(k == 0){
		factorial_k1 = 1;
	}
	factorial_k1 *= z;
}
for (int j = 1; j <= (n-k); j++){
	if ((n-k) == 0){
		factorial_n1k1 = 1;
		{
			factorial_n1k1 *= j;
			
		}
	int sum = (factorial_n1 * factorial_k1 / factorial_n1k1);
	
	System.out.println("n! = " + factorial_n1);
	System.out.println("k! = " + factorial_k1);
	System.out.println("(n-k)!" + factorial_n1k1);
	System.out.println("n! * k! / (n - k)! = " + sum);
	}
}

	}

}

