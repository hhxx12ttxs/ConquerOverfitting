private static final long K = 1024;

public static String format(long size) {
double ret = size;

if (ret < K) return ret + &quot;B&quot;;

ret /= K;
if (ret < K) return ret + &quot;KB&quot;;

ret /= K;
if (ret < K) return ret + &quot;MB&quot;;

