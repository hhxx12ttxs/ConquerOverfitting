if (id.startsWith(&quot;+&quot;) || id.startsWith(&quot;-&quot;)) {
int offset = parseOffset(id);
if (offset == 0L) {
int offset = 0;
try {
int hoursInMinutes = hoursOffset * 60;
if (hoursInMinutes < 0) {

