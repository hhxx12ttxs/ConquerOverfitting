
public class Feb4
{
  public static void method1()
  {
    System.out.println("Hello world");
    method1();
  }
  
  public static long factorial(int n)
  {
    if( n==0 || n==1)
      return 1;
    return n*factorial(n-1);
  }
  
  public static long fibonacci(int n)
  {
    if ( n==0 || n==1)
      return 1;
    return fibonacci(n-1) + fibonacci(n-2);
  }
  
  public static long fibonacci2(int n)
  {
    if ( n==0 || n==1)
      return 1;
    long[] values = new long[n+1];
    values[0] = 1;
    values[1] = 1;
    for(int i=2;i<=n;i++)
      values[i] = values[i-1] + values[i-2];
    return values[n];
  }
  
  public static void main(String[] arg)
  {
//    method1();
    for(int i=1;i<160;i++)
//      System.out.println(i+"\t"+factorial(i));
      System.out.println(i+"\t"+fibonacci2(i));
  }
}
