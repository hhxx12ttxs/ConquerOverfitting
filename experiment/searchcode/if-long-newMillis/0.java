public long addNPeriodsToAndCheckBounds(int numPeriods, long milliseconds) throws OutOfBoundsException {
long newMillis = addNPeriodsTo(numPeriods, milliseconds);

if ((numPeriods > 0 &amp;&amp; newMillis <= milliseconds) || (numPeriods < 0 &amp;&amp; milliseconds <= newMillis)) {

