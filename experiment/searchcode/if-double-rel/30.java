package simple;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class YuTest {

   private double K;
   private double ro;

   public YuTest(double k, double ro) {
      K = k;
      this.ro = ro;
   }

   public double responseTime(double service) {
      return service * y(ro, K);
   }

   private double y(double ro, double k) {
      double a = alfa(ro, k);
      double b = beta(ro, k);
      return 1 + a / (k * (a + b) * (1 - ro));
   }

   private double alfa(double ro, double k) {
      return Math.pow(k * ro, k) / (fac(k) * (1 - ro));
   }

   private double beta(double ro, double k) {
      double sum = 0;
      for (int i = 0; i < (int) k; i++) {
         sum += Math.pow(k * ro, i) / fac(i);
      }
      return sum;
   }

   private double fac(double i) {
      return fac((int) i);
   }

   private double fac(int i) {
      if (i < 0)
         throw new IllegalArgumentException("Negative input for factorial");
      if (i == 0 || i == 1)
         return 1;
      return i * fac(i - 1);
   }

   public static void main(String[] args) {
      suchThatTest();
   }

   private static double roSuchThat(double s, double r, double threshold) {
      YuTest y;
      double resp, err;
      for (double _r = .01; _r < 1; _r += 0.001) {
         y = new YuTest(2, _r);
         resp = y.responseTime(s);
         err = rel(r, resp);
         System.out.println("Ro " + _r + " resp -> " + resp);
         if (err < threshold) {
            return _r;
         }
      }
      return 0;
   }

   private static double rel(double a, double b) {
      return Math.abs(a - b) / a;
   }


   private static void simpleTest() {

      YuTest yu = new YuTest(2, 0.49860186);
      System.out.println(yu.responseTime(6906.63333333));

      yu = new YuTest(2, 0.68);
      System.out.println(yu.responseTime(6906.63333333));
   }

   private static void suchThatTest() {
      double l = roSuchThat(6906.63333333, 26019.54429096, 0.001);
      System.out.println(l);
   }
}

