public String elapsed() {
long millis = delta();
if (millis > 1000L)  {
char xx[] = new char[2];
xx[0] = (char)(&#39;0&#39; + (millis % 1000)/100);
throw new Error(&quot;Can only add to a stopped Timer&quot;);
}
endMillis += tim.delta();
}

private long delta() {
if (endMillis == 0) {

