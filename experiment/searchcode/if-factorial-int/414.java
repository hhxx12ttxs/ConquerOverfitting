package recursion;

/**
 * 5的阶乘=5*4*3*2*1
 * Created by yan on 2016/4/10.
 */
public class Factorial {

    public int doFactorial(int i){
        if(i==1){
            return 1;
        }else{
            return i*doFactorial(i-1);
        }
    }

    public static void main(String args[]){
        System.out.println(new Factorial().doFactorial(5));
    }
}

