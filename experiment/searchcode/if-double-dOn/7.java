a[i] = f.apply(a[i], b[i]);
}
}

public static void checkSameLength(double[] a, double[] b){
if (a.length != b.length) throw new IllegalArgumentException(&quot;vector dimensions don&#39;t match&quot;);
}

public static void clear(double[] a, int from, int to){

