package seker.algorithm.recursion;


/**
 * 阶乘
 * 
 * @author liuxinjian
 */
public class Factorial {
    
    /**
     * 阶乘：递归的方法实现
     * 
     * @param n
     * @return
     * @throws Exception
     */
    public int factorialRecursion(int n) throws Exception {
        if (0 == n) {
            return 1;
        } else if (n > 0) {
            return n * factorialRecursion(n - 1);
        } else {
            throw new Exception("negative can't factorial.");
        }
    }
    
    /**
     * 阶乘：非递归的方式(循环；迭代)实现
     * 
     * @param n
     * @return
     * @throws Exception
     */
    public int factorialRecurrence(int n) throws Exception {
        if (0 == n) {
            return 1;
        } else if (n > 0) {
            int result = 1;
            for (int i = 1; i <= n; i ++) {
                result *= i;
            }
            return result;
        } else {
            throw new Exception("negative can't factorial.");
        }
    }
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        Factorial factorial = new Factorial();
        
        System.out.println(factorial.factorialRecursion(0));
        System.out.println(factorial.factorialRecursion(1));
        System.out.println(factorial.factorialRecursion(2));
        System.out.println(factorial.factorialRecursion(3));
        System.out.println(factorial.factorialRecursion(4));
        System.out.println(factorial.factorialRecursion(5));
        
        System.out.println();
        
        System.out.println(factorial.factorialRecurrence(0));
        System.out.println(factorial.factorialRecurrence(1));
        System.out.println(factorial.factorialRecurrence(2));
        System.out.println(factorial.factorialRecurrence(3));
        System.out.println(factorial.factorialRecurrence(4));
        System.out.println(factorial.factorialRecurrence(5));
    }
}


