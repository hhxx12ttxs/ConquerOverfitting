public static final long millisPerYear   =  365 * millisPerDay;

public static Map<String, Long> unitMillis;

static
{
unitMillis = new LinkedHashMap<String, Long>();
unitMillis.put(&quot;years&quot;, millisPerYear);
unitMillis.put(&quot;months&quot;, millisPerMonth);

