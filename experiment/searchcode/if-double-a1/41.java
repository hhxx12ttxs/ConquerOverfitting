return new NumReturn(a1.dvalue + a2.dvalue, a2.pointerLocation, &#39;d&#39;);
} else if (a1.type == &#39;i&#39; &amp;&amp; a2.type == &#39;d&#39;) {
a1.dvalue = (double) a1.value;
else if (a1.type == &#39;d&#39; &amp;&amp; a2.type == &#39;i&#39;) {
a2.dvalue = (double) a2.value;
System.out.println(a1.dvalue + a2.dvalue);

