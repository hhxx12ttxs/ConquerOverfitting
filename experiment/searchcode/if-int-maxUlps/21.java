* @return <ul><li>0 if  {@link #equals(double, double, int) equals(x, y, maxUlps)}</li>
*       <li>&amp;lt; 0 if !{@link #equals(double, double, int) equals(x, y, maxUlps)} &amp;amp;&amp;amp; x &amp;lt; y</li>
public static int compareTo(final double x, final double y, final int maxUlps) {
if (equals(x, y, maxUlps)) {

