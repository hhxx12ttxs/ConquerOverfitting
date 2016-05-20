
public class SumOfIntegers {
	public static void main(String[] args) {
			      int lowerbound = 1;      // Store the lowerbound
			      int upperbound = 1000;   // Store the upperbound
			      int sum = 0;   // Declare an int variable "sum" to accumulate the numbers
			                     // Set the initial sum to 0
			      // Use a for-loop to repeatitively sum from the lowerbound to the upperbound
			      for (int number = 0; number <= upperbound; number++) {
			         if ((number%13==0) || (number%15==0) || (number%17==0) && !(number%30==0))
			        	 sum = sum + number;
			      } 
			      // Print the result
			      System.out.println("The sum from " + lowerbound + " to " + upperbound + " is " + sum);
			   }
			}

