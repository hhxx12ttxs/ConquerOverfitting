public class FractionConvertToDecimal{

double parse(String ratio) {
if (ratio.contains(&quot;/&quot;)) {
String[] rat = ratio.split(&quot;/&quot;);
return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);

