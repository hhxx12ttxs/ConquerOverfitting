String sourceUrlString=&quot;data/test.html&quot;;
if (args.length==0)
System.err.println(&quot;Using default argument of \&quot;&quot;+sourceUrlString+&#39;&quot;&#39;);
else
sourceUrlString=args[0];
if (sourceUrlString.indexOf(&#39;:&#39;)==-1) sourceUrlString=&quot;file:&quot;+sourceUrlString;

