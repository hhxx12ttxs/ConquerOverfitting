public static boolean rectRect(double x, double y, double w, double h, double x2, double y2, double w2, double h2) {
return x < x2 + w2 &amp;&amp; x + w > x2 &amp;&amp; y < y2 + h2 &amp;&amp; y + h > y2;
}

public static boolean pointRect(double x, double y, double x1, double y1, double w1, double h1){

