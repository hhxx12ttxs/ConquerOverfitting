public String nextString() {
Random random = new Random();
StringBuffer buffer = new StringBuffer();
if(!isRangeValid()) {
for(Range currentRange: range) {
startRange = currentRange.startRange;
endRange = currentRange.endRange;
if (startRange == &#39;\0&#39; || endRange == &#39;\0&#39;) {

