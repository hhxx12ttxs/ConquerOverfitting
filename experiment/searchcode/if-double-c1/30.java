System.out.println(&quot;Intersection at &quot; + intersection[0] + &quot;,&quot; + intersection[1]);
}

public static double[] intersect (double[] c1, double[] c2) {
/**
* If line segments, you have to check to make sure return values are on both lines.
* Otherwise, return the endpoints.
*/
double det = c1[0] * c2[1] - c1[1] * c2[0];
if (det == 0) {
System.out.println(&quot;LINES ARE PARALLEL&quot;);

