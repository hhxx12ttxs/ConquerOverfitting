/**
 * Created by jaturul on 28.01.16.
 */
public class Factorial
{
    int ComputeFactorial(int n)
    {
        int result;
        if ( (n == 1) || (n == 0) )
        {
            return 1;
        }
        result = n * ComputeFactorial(n-1);

        return result;
    }

}

