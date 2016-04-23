public class Main{
  
  static int factorial(int n){
    if(n==1)
      return n;
    return n * factorial(n-1);
  }
  
  public static void main(String[] args){
    boolean a = false && (factorial(10000) > 1);
    System.out.println(a);
  }
  
}

