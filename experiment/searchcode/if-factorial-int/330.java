
public class Problem34 {

    public static void main(String[] args) {
        int factorial[] = new int[10];
        
        for(int i=0; i<10; i++){
            factorial[i] = 1;
            for(int j=1; j<=i; j++){
                factorial[i] *= j;
            }
            System.out.println(factorial[i]);
        }
        
        int sum = 0;
        for(int i=3; i<10000000; i++){
            char digits[] = Integer.toString(i).toCharArray();
            int sum_factorial = 0;
            for(int j=0; j<digits.length; j++){
                sum_factorial += factorial[(int)(digits[j] - '0')];
            }
            
            if(sum_factorial == i){
                sum += i;
                System.out.println(i);
            }
        }
        
        System.out.println(sum);
    }

}

