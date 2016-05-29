//修正double类型的误差。去除2.0的.0。
public void dr1() {
int i = NewResult.indexOf(&quot;.&quot;);
if (i > -1) {
NewResult = afFormat.format(Double.parseDouble(NewResult));
for (int j = NewResult.length() - 1; j > i; j--) {

