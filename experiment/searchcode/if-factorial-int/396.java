/**
 * 
 */
package recursion;

/**
 * @author changsi
 *
 */
public class Factorial {
	
	public static int factorial(int n){
		if(n==0 || n==1){
			return 1;
		}
		else{
			return n*factorial(n-1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(Factorial.factorial(10));
		
	}

}

