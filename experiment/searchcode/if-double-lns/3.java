double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
String sf=&quot;&quot;+f1;
if(sf.length()==4)
return sf+&quot;0&quot;;
String[] lns = ln.split(&quot;\t&quot;);
if (lns.length > 1) {
if (!acc.containsKey(lns[0])) {
acc.put(lns[0], 0.0);

