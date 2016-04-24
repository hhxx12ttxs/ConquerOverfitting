package codechef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//Factorial
public class Factorial {

	public static void main(String[] args) throws IOException {
		
		BufferedReader r = new BufferedReader(new InputStreamReader (System.in));
		int n = Integer.parseInt(r.readLine());
		while(n>0){
			System.out.println(recursion(Integer.parseInt(r.readLine())));
			n--;
		}
	}
	
	private static int recursion(int a){
		if(a<5)
			return 0;
		return (int)Math.floor(a/5)+recursion((int) Math.floor(a/5));
	}
}
