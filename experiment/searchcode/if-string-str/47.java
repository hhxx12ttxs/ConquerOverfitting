public J1_stringconcat (String str) {
this.s = str+str+str;
}

public static int test() {
String str = &quot;&quot;+1+&quot;111&quot;;
if (Integer.parseInt(&quot;4&quot;+&quot;2&quot;)==42)
str = str+&quot;22&quot;;
else
str = str+&quot;33&quot;;
int i=0;

