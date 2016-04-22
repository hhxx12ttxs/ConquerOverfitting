import java.util.Scanner;
public class Factorial
{
public static void main(String[] args)
{
  int factorial;
  int product;
  Scanner scan= new Scanner(System.in);
  System.out.println("Please imput the number :");
  factorial=scan.nextInt();
  product=factorial;
  while(factorial<1){
    System.out.println("you have imputed a negative number.Try again.");
    factorial=scan.nextInt();
  product=factorial;
  }
  if(factorial==0){
    System.out.println("the factorial of 0 is: 1.");}
  else{
  for(int i=1;i<factorial;i++){
  product*=(factorial-i);
  }
  System.out.println("the factorial of "+factorial+" is: "+product);
  }
}
}
