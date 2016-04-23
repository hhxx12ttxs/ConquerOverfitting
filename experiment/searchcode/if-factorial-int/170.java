public class FindFactorial {

  /**
   * @param args
   */

  private int getFactorial(int n) {
    if (n <= 1)
      return 1;
    else
      return n * getFactorial(n - 1);
  }

  public void displayAnswers(int n) {
    System.out.println(getFactorial(n));
  }
}

