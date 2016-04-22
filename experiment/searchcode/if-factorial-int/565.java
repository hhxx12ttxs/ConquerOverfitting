package hw_1;

public class Factorial {
    static int factorial(int number){
        if (number == 0){
            return 1;
        }
        int n = 1;
        for (int i = 1; i <= number; i++){
            n = n*i;
        }
        return n;
    }
}

