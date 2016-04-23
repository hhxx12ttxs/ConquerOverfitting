import java.util.Scanner;

public class factorial
{

  public static void main(String[] args)
  {
    Scanner scan = new Scanner(System.in);
    double factorial = -1;
    double factor = 1;
    System.out.println("input a non negative integer for a factorial");
    factorial = scan.nextInt();
   
    if (factorial <= 0)
    while (factorial <= -1)
    {

      if (factorial >= 0)
        {for (int i = 1; i <= factorial; i++)
      {
        factor = factor * i;
      }}
        else
        System.out.println("you are dumb input a non negative number ");
      factorial = scan.nextInt();
      for (int i = 1; i <= factorial; i++)
      {
        factor = factor * i;
      }
      } 
    else
    {
      for (int i = 1; i <= factorial; i++)
      {
        factor = factor * i;
      }
    }
    
    System.out.print(factorial + "!" + "=" + factor);  

  }
  

    }

