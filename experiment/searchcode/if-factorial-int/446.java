import java.util.Scanner;

public class Lesson_5_2_1_Factorial_Recursive {

    public static void main(String[] args) {
        
        Scanner s = new Scanner(System.in);
        System.out.print("Enter your integer here: ");
        int yourInt = s.nextInt();
        int factorial = 0;
        
        factorial = recursiveFactorial(yourInt);
        
        System.out.println(factorial);
        
    }
    
    static int recursiveFactorial(int a){
        if(a == 1){
            return 1;
        }
        return a * recursiveFactorial(a-1);
    }

}
