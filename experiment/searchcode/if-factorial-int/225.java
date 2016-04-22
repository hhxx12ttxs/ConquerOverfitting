public class Factorial
{
  public static void main( String[] args )
  {
    System.out.println( "3! = " + calcFactorial(3) );
  }
  
  public static int calcFactorial( int n )
  {
    if ( n == 0 )
    {
      return 1;
    }
    else
    {
      return n * calcFactorial( n - 1 );
    }
  }
}
