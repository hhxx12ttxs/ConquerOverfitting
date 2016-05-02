/**
 * @author  Jane Ullah
 * @purpose Problem 2 - Project Euler
 * @date    12/19/2011
 * @site 	http://janetalkscode.com	
 */
public class Problem2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long a1 = 0L, a2 = 1L;
		long nextTerm = 0L;;
		long sumEvenTerms = 0L;
		boolean fourMillionthReached = false;
		while ( !fourMillionthReached )	{
			if (nextTerm >= 4000000)	{
				fourMillionthReached = true;
			}
			else	{
				nextTerm = a1 + a2;
				if (nextTerm%2 == 0){
					sumEvenTerms += nextTerm;
				}
				a1 = a2;
				a2 = nextTerm;
			}
		}
		System.out.print("Even sum is: " + sumEvenTerms);

	}

}

