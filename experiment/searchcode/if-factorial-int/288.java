import java.util.Scanner;

public class Factorial2 {

	public static void main(String[] args) {
		
		Scanner scn = new Scanner(System.in, "UTF-8");
		System.out.println("Enter N and K (1<K<N): ");
		int n = scn.nextInt();
		int k = scn.nextInt();
		int factorial_n = 1;
		int factorial_k = 1;
		int factorial_nk = 1;
		
		for(int i = 1; i <= n; i++){
			if(n == 0){
				factorial_n = 1;
				break;
			}
			factorial_n *= i;
		}
		
		for(int j = 1; j <= k; j++){
			if(k == 0){
				factorial_k = 1;
				break;
			}
			factorial_k *= j;
		}
		
		for(int m = 1; m <= (n - k); m++){
			if((n - k) == 0){
				factorial_nk = 1;
				break;
			}
			factorial_nk *= m;
		}
		
		int result = (factorial_n * factorial_k) / factorial_nk;
				
		System.out.println("N! = " + factorial_n);
		System.out.println("K! = " + factorial_k);
		System.out.println("(N - K)! = " + factorial_nk);
		System.out.println("N!*K!/(N-K)! = " + result);
		
		scn.close();

	}

}

