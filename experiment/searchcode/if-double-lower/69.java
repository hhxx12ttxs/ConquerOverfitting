public CalDecimal(String num) {
type = &quot;Decimal&quot;;
String lowerNum = num.toLowerCase();
int hasE = lowerNum.indexOf(&#39;e&#39;);
if (hasE != -1) {
double fraction = Double.parseDouble(lowerNum.substring(0, hasE));
double exponent;

