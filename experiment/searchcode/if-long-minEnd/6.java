public static DateRange getIntersectionRange(DateRange dr1, DateRange dr2) {
Date minEnd = min(dr1.getTo(), dr2.getTo());
Date maxStart = max(dr1.getFrom(), dr2.getFrom());
long res = minEnd.getTime() - maxStart.getTime();
if (res > 0) {

