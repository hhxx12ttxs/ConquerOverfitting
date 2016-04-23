package ex01;

public class DoubleFactorial {
  
  public static void main(String[] args) {
    System.out.println(new DoubleFactorial().doubleFactorial(65));
  }

  public long doubleFactorial(int number) {
    if (number <= 1) {
      return 1;
    } else {
      return number * doubleFactorial(number - 2);
    }
  }
}
