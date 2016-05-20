import java.util.Scanner;

public class LCM{
    public static int lcm(int a, int b){
        return (a / gcd(a, b)) * b;
    }

    public static int gcd(int a, int b){
        if(a == b){
            return a;
        }

        if(a == 0 || b == 0){
            return 0;
        }

        if(a % 2 == 0){ // a is even
            if(b % 2 == 0){ // b is even
                return (gcd(a >> 1, b >> 1) << 1);
            }else{
                return gcd(a >> 1, b);
            }
        }else{ // a is odd
            if( b % 2 == 0){
                return gcd(a, b >> 1);
            }else{ // a and b are both odd
                if(a > b){
                    return gcd((a - b)>>1, b);
                }else{
                    return gcd((b - a)>>1, a);
                }
            }
        }
    }

    public static void main(String[] args){
        System.out.print("a:");
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();

        System.out.print("b:");
        int b = scanner.nextInt();

        System.out.println("The greatest common devisor is:" + gcd(a, b));
        System.out.println("The lease common multiple is:" + lcm(a, b));
    }
}
