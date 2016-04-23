class FactorialDemo{
  public static void main(String []args){
    int n = Integer.parseInt(args[0]);
    System.out.println("Factorial of " + n + " is " + factorial(n));
  }

  static int factorial(int x){
    if(x == 1)return 1;
    return x * factorial(x-1);
  }
}

