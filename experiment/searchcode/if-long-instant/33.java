public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
if (minuendInstant < subtrahendInstant)
int wow = iChronology.getWeekOfWeekyear(instant);

if (wow > 1)
{
instant -= ((long) DateTimeConstants.MILLIS_PER_WEEK) * (wow - 1);

