char tmp = str.charAt(i);
if(tmp == &#39;\n&#39;)      ret += &quot;\\n&quot;;
else if(tmp == &#39;\t&#39;) ret += &quot;\\t&quot;;
else if(tmp == &#39;\b&#39;) ret += &quot;\\b&quot;;
else if(tmp == &#39;\r&#39;) ret += &quot;\\r&quot;;
else if(tmp == &#39;\f&#39;) ret += &quot;\\f&quot;;

