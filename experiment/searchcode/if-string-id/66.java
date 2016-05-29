TestString st = new TestString();
System.out.println(st.getIdNo());
}
String idNo = &quot;371523198902283752&quot;;
public String getIdNo() {
if(idNo != null &amp;&amp; idNo.length() > 4){
String StringPre = idNo.substring(0,idNo.length() - 4).replaceAll(&quot;.&quot;, &quot;*&quot;);

