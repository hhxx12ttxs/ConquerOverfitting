package filters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public DateRangeFilter createDateRangeFilter(String from, String to) {
if (from != null &amp;&amp; to != null) {
return new DateRangeFilter(LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME),LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

