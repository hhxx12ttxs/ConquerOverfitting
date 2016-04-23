import java.util.Scanner;

public class Factorial {

    public static void main(String[] args) {
        Factorial fac = new Factorial();
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the number to get factorial for:");
        int num = scan.nextInt();
        if (num < 0){
            System.out.println("We need a positive int");
        } else {
            System.out.println(fac.factorial(num));
        }
    }

    public long factorial(int n) {
        if (n <= 1)
            return n;
        return n*factorial(n-1);
    }
}

