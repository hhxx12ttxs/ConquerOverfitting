public class cyclic {


public static void  cyclic(double []  a,  double [] b,  double [] c,  double  alpha,
double  beta,  double [] r,  double [] x) throws NRException
int n = a.length;
if (n <= 2)
throw new NRException(&quot;n too small in cyclic&quot;);

