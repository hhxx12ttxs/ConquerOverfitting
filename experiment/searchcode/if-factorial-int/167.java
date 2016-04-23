/*Chernov Dmitriy
 * 271 group
 */
package robot;

public class FactorialUtil
{
    public static int factorial(int n)
    {
        if (n == 0) return 1;
        return n * factorial(n-1);
    }
}
