String[] tmp2 = tmp[1].split(&quot;\\+&quot;);
Double test = Double.parseDouble(variables.get(tmp2[0]))+Double.parseDouble(variables.get(tmp2[1].replace(&quot;;&quot;,&quot;&quot;)));
String[] tmp2 = tmp[1].split(&quot;\\-&quot;);
Double test = Double.parseDouble(variables.get(tmp2[0]))-Double.parseDouble(variables.get(tmp2[1].replace(&quot;;&quot;,&quot;&quot;)));

