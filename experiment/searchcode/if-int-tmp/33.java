public String addBinary(String a, String b) {
String result = &quot;&quot;;
int i, j, tmp, carry = 0;

if(a==null || b==null || a.equals(&quot;&quot;) || b.equals(&quot;&quot;)) return null;
for(i=a.length()-1, j=b.length()-1; i>=0 &amp;&amp; j>=0; i--, j--){
tmp = carry + (int)(a.charAt(i) - &#39;0&#39;) + (int)(b.charAt(j) - &#39;0&#39;);
carry = tmp/2;

