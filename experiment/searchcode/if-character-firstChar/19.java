this.argc = 1;
this.name = name;
int firstChar = (int) name.charAt(0);
if ((firstChar < 65 || firstChar > 90) &amp;&amp; (firstChar < 97 || firstChar > 122)) {
throw new InvalidCustomFunctionException(&quot;Functions have to start with a lowercase or uppercase character&quot;);

