public TimeInterval(Timestamp startTs, Timestamp endTs) throws Exception {
if(startTs.ts > endTs.ts) {
throw new Exception(&quot;TimeInterval: start after end&quot;);
public TimeInterval(Timestamp startTs, int durationInDays) throws Exception {
this();
Timestamp startTS1 = startTs;

