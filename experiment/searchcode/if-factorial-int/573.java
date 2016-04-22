/**
 * Created by vinaytejakairamkonda on 9/11/15.
 */
public class FactorialRecursion
{
    public static void main(String[] args[])
    {

    }

    public int computeFactorial(int n)
    {
        if(n==1)
        {
            return 1;
        }
        else
        {
            return n*computeFactorial(n-1);
        }
    }
}

