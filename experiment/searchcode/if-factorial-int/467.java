public class ExerciseA {
  public static void main(String[] args) {
    ExerciseA a = new ExerciseA();
    System.out.println(a.factorial(4));
    System.out.println(a.factorial(10));
    System.out.println(a.factorialIteratively(4));
    System.out.println(a.factorialIteratively(10));
  }

  public int factorial(int n){
    if (n ==1) {
      return 1;
    } else {
      int result = n * factorial(n-1);
      return result;
    }
  }

  public int factorialIteratively(int n){
    int result = 1;
    for (int i = 2; i <= n ;i++ ) {
      result = result * i;
    }
    return result;
  }
}
