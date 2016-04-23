//Kyle Albonico
//CSE2 11:10-12
//Calculates a factorial based on user input

import java.util.Scanner; //import scanner
public class RunFactorial {
    public static void main(String[] args) {
        
        Scanner myScanner = new Scanner(System.in); //declare scanner
        System.out.print("Please enter an integer that is between 9 and 16: ");
        int factorialValue; //initiate input value
        long factorial; //initiate output value which must be a long because 16! will be too large for an int
        
        do {
            factorialValue = myScanner.nextInt(); //integer to run factorial
            factorial = 1; //initiate factorial value
            if (factorialValue < 9 || factorialValue > 16) { //values outside accepted range
                System.out.println("Invalid input, enter again!");
            }
        } while (factorialValue < 9 || factorialValue > 16);
        
        int factorialInput; 
        factorialInput = factorialValue; //saves original input value when it is used as counter
        
        while (factorialValue > 0) {
            factorial = factorial * factorialValue; //calculates factorial
            factorialValue--; //decrement counter
        }
        
        System.out.println(factorialInput+"! = "+factorial); //print factorial value
        
    }
}
