return div(v1, v2, DEF_DIV_SCALE);
}
public static double div(double v1, double v2, int scale)
{
if (scale < 0) {
BigDecimal b2 = new BigDecimal(Double.toString(v2));
if (b2.intValue() == 0) {
System.out.println(&quot;Dividend not zero!&quot;);

