private final DateTime mStart, mEnd;

public Interval(DateTime start, DateTime end) {
mStart = start;
mEnd = end;
}

public long getStartMillis() {
long thisStart = getStartMillis();
long thisEnd = getEndMillis();
if (interval == null) {
long now = DateTimeUtils.currentTimeMillis();

