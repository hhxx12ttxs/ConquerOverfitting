public static final long YEAR = DAY * 365;

/** The millis. */
private final long millis;

@Override
public int compareTo(Milliseconds other) {
public Milliseconds(long millis) {
if (millis < 0) {
throw new IllegalArgumentException(&quot;millis=&quot; + millis);

