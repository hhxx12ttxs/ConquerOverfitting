long maxStart,
long minStop,
long maxStop)
{
if (minStart > maxStart || maxStart > minStop || minStop > maxStop) {
private static boolean validate(long minStart, long maxStart, long minStop, long maxStop) throws InvalidObjectException{
if (minStart > maxStart || maxStart > minStop || minStop > maxStop) {

