import java.util.LinkedList;
import java.util.Scanner;

/*Given a number K, find the smallest Fibonacci number that shares a common factor( other than 1 ) with it. A number is said to be a common factor of two numbers if it exactly divides both of them. 
 
Output two separate numbers, F and D, where F is the smallest fibonacci number and D is the smallest number other than 1 which divides K and F.
 
Input Format 
 
First line of the input contains an integer T, the number of testcases.
Then follows T lines, each containing an integer K.
 
Output Format
 
Output T lines, each containing the required answer for each corresponding testcase.
 
 

 

Sample Input 
 
3
3
5
161
 
Sample Output
 
3 3
5 5
21 7
 
Explanation
 
There are three testcases. The first test case is 3, the smallest required fibonacci number  3. The second testcase is 5 and the third is 161. For 161 the smallest fibonacci numer sharing a common divisor with it is 21 and the smallest number other than 1 dividing 161 and 7 is 7.
 
Constraints :
 
1 <= T <= 5
2 <= K <= 1000,000
The required fibonacci number is guranteed to be less than 10^18.*/
public class FibonaciFactor {

	
	public static void main(String[] args) {
		
		Scanner sc=new Scanner(System.in);
		
		int no_tc=sc.nextInt();
		
		for(int i=0;i<no_tc;i++){
			
			long test_number=sc.nextInt();
			
			long first=1;
			long second=1;
			long third;
			
			while(true){
				third=first+second;
				first=second;
				second=third;
				
				
				int result=haveCommonDivisor(test_number, third);
			/*	int gcd=gcd(test_number, third);
				if(gcd!=1){
					System.out.println(third+"\t"+gcd);
					break;
				}
				else 
					continue;*/
				
				if(result!=0){
					System.out.println(third+"\t"+result);
					break;
				}
				else 
					continue;
				
			}
				
				
			
			
			
		}
		
	}
	
	void generateFib(){
		
		LinkedList<Integer> fibList=new LinkedList<Integer>();
		fibList.add(2);
		int first=1;
		int second=2;
		
	}
	static int gcd(int n,int m)
	{ if(m<=n && n%m == 0)
	   return m;
	  if(n < m)
	   return gcd(m,n);
	  else
	   return gcd(m,n%m);
	 }
	
	static int haveCommonDivisor(long a,long b){
		
		long small=a<b?a:b;
		
		for(int i=2;i<=small;i++){
			if(a%i==0 && b%i==0)
				return i;
		}
		
		return 0;
	}
	
	
	
	
	
	static int findGCD(int n1,int n2){
		 while(n1!=n2){
	            if(n1>n2)
	                  n1=n1-n2;
	            else
	                  n2=n2-n1;
	      }
		 
		 return n1;
	}
	


}

