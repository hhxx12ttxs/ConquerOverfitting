public static String fileType(String fileName) {
String f = fileName.toLowerCase();
//text
if (f.endsWith(&quot;.ics&quot;)) return &quot;text/calendar&quot;;
if (f.endsWith(&quot;.xml&quot;)) return &quot;text/xml&quot;;

if (f.endsWith(&quot;.asf&quot;)) return &quot;video/x-ms-asf&quot;;

