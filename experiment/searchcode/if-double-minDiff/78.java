System.out.println(&quot;DEBUG: &quot; + str);
}
}
public static void d(double str) {
if (debug) {
System.out.println(&quot;DEBUG: &quot; + str);
cnt += 1;

}

double mean = (double) sum / cnt;
double minDiff = Long.MAX_VALUE;

