public static boolean isALoad(int seen) {
return (seen == ALOAD) || ((seen >= ALOAD_0) &amp;&amp; (seen <= ALOAD_3));
}

public static boolean isAStore(int seen) {
return (seen == ASTORE) || ((seen >= ASTORE_0) &amp;&amp; (seen <= ASTORE_3));

