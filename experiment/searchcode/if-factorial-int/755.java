class Factorial {
  public static void main(String[] args){
    System.out.println(factorial(5));
  }

  public static int factorial(int n) {
    if(n == 0){
      return 1;
    } else {
      int recurse = factorial(n-1);
      int result = n * recurse;
      return result;
    }
  }
}

