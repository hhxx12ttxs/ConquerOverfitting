import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Spliterator;
assertThat(getFriday13th().collect(Collectors.toList())).containsExactly(
LocalDate.parse(&quot;1901-09-13&quot;), LocalDate.parse(&quot;1901-12-13&quot;), LocalDate.parse(&quot;1902-06-13&quot;), LocalDate.parse(&quot;1903-02-13&quot;),

