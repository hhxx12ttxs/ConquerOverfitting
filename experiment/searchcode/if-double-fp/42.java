void add(String s, char k, double inc, TunableParameter.Adjust a) {
if (findParam((int)k) !=  null)
System.out.printf(&quot;Warning: key &#39;%c&#39;/%d already bound\n&quot;, k, (int)k);
new TunableParameter.Adjust() { public double adjust(double i) {
fp.ardDebugInterval += i;
if (i != 0)
fp.setArduinoDebug(fp.ardDebugInterval);

