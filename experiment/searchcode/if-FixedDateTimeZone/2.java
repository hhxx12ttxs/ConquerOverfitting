import org.joda.time.DateTimeZone;

/**
* Test cases for FixedDateTimeZone.
*
* @author Stephen Colebourne
*/
public class TestFixedDateTimeZone extends TestCase {
public void testEquals() throws Exception {
FixedDateTimeZone zone1 = new FixedDateTimeZone(&quot;A&quot;, &quot;B&quot;, 1, 5);
FixedDateTimeZone zone1b = new FixedDateTimeZone(&quot;A&quot;, &quot;B&quot;, 1, 5);

