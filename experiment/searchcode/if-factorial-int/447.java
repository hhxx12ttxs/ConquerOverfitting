public class Problem034 {

  public static void main(String[] args) {

    calcFactorials();

    int sum = 0;

    for (int n = 3; n <=  9 * factorial[9]; n++)
      if (sumOfFactorialsOfDigit(n) == n)
        sum += n;

    System.out.println(sum);

    return;
  }

  static int[] factorial;

  static void calcFactorials() {
    factorial = new int[10];
    factorial[0] = 1;
    for (int n = 1; n <= 9; n++)
      factorial[n] = n * factorial[n - 1];
    return;
  }

  static int sumOfFactorialsOfDigit(int n) {
    int sum = 0;
    for (int N = n; N > 0; N /= 10)
      sum += factorial[N % 10];
    return sum;
  }
}

