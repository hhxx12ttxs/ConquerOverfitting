/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factorial;

/**
 *
 * @author Matt
 */
public class Factorial {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println(factorial(5));
    }
    
    public static int factorial(int n){
        if (n == 0){
            return 1;
        }
        else
            return n * factorial(n-1);
    }
    
}




// factorial(5) -> return (5 *factorial (4)) = 120
// factorial(4) -> return (4 * factorial (3)) = 24
// factorial(3) -> return (3 * factorial (2)) = 6
// factorial(2) -> return (2 * factorial (1)) = 2 
// factorial(1) -> return (1 * factorial (0)) = 1
// factorial(0) -> return 1




