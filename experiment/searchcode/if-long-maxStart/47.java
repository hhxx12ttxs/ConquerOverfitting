private final long maxStart;
/**
* The minimum stop time in milliseconds from midnight, January 1, 1970
long minStop,
long maxStop)
{
if (minStart > maxStart || maxStart > minStop || minStop > maxStop) {

