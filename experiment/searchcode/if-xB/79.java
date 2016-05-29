public static boolean parseName(String x,String[] namertn,String[] rest){
if(x.length()<=0) return false;
StringBuffer xb=new StringBuffer(x);
char c=xb.charAt(0);
StringBuffer ix=new StringBuffer(&quot;&quot;);
if(&#39;a&#39;<=c &amp;&amp; c<=&#39;z&#39; || &#39;A&#39;<=c &amp;&amp; c<=&#39;Z&#39;|| c==&#39;_&#39;){

