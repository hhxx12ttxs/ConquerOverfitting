public static void main (String[] args)
{
BigInteger sum = new BigInteger(&quot;-4&quot;);
BigDecimal temp, temp2;
double t, t0;
for (int i = 2; i <= 333333; i++)
t = Math.sqrt(Math.pow(i - 1, 2) - Math.pow(((double)i)/2, 2)) * ((double)i)/2;
t0 = Math.sqrt(Math.pow(i + 1, 2) - Math.pow(((double)i)/2, 2)) * ((double)i)/2;

