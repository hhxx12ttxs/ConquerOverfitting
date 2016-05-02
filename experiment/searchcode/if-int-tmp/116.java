import java.util.Scanner;

public class LargeMultiply{
    public static void addDigit(int[] result, int pos, int data){
        result[pos] += data;        

        for(int it = pos; it >= 0; it--){
            int tmp = result[it];
            result[it] = tmp % 10;
            if(tmp < 10){
                break;
            }else{
                result[it - 1] += tmp / 10;
            }
        }
    }
    public static void multiply(int[] a, int[] b){
        int[] result = new int[a.length + b.length];
        for(int j = b.length - 1; j >= 0; j--){ /* j [b.length-1 0 ]*/
            for(int i = a.length - 1; i >= 0; i--){
                /*distance to the right of result should be j+i */
                int jr = b.length - 1 - j; /*jr [0, b.length - 1]*/
                int ir = a.length - 1 - i; /*ir [0, a.length - 1]*/
                
                int resultr = result.length - 1 - (jr + ir); /*resultr [result.length - 1, ... 1]*/
                
                int multiply = b[j] * a[i];

                addDigit(result, resultr, multiply);
                // result[resultr] += multiply % 10;
                // result[resultr - 1] += multiply / 10;

                // int it = 0;
                // while(it < 2 && ){
                //     int tmp = result[resultr - it];
                //     result[resultr - it] = tmp % 10;
                //     result[resultr - it - 1] += tmp / 10;
                    
                //     it++;
                // }
            }
        }


        for(int i = 0; i < result.length; i++){
            System.out.print(result[i]);
        }
    }

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.print("length of a:");
        int al = scanner.nextInt();
        int[] a = new int[al];
        
        for(int i = 0; i < al; i++){
            a[i] = scanner.nextInt();
        }

        System.out.print("length of b:");
        int bl = scanner.nextInt();
        int[] b = new int[bl];
        for(int j = 0; j < bl; j++){
            b[j] = scanner.nextInt();
        }

        multiply(a, b);
    }
}
