/** Sets the internal &quot;current&quot; time of the clock.
* @throws IllegalArgumentException if newMillis is smaller or equal to the current time. An Clock&#39;s time can only progress forwards.
public synchronized void currentTimeMillis(long newMillis) {
if (newMillis <= currentTimeMillis) throw new IllegalArgumentException(&quot;Clock&#39;s time can only be set forwards.&quot;);

