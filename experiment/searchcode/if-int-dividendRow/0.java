rs = s.executeQuery();

if (rs.next()) {
return rs.getInt(1);
} else {
throw new IllegalArgumentException(&quot;symbol &quot; + symbol + &quot; not found&quot;);
for (YahooUtil.DividendRow r : rows) {

// Bad data check.
if (r.getDate().before(sanityCheckStartDate) ||

