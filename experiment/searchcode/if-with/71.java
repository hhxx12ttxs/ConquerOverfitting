//        System.out.printf(&quot;Asking if instrumentable %s\n&quot;, className);
if (className.startsWith(&quot;java/applet/&quot;)
return true;
if (className.startsWith(&quot;java/lang/&quot;)
|| className.startsWith(&quot;java/net/&quot;)

