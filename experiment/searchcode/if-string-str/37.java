package test;

public class replaceTest {

public static void main(String[] args) {
String str = &quot;_08?+<?S\&quot;@&quot;;

if (str.contains(&quot;@&quot;))
str = str.replaceAll(&quot;@&quot;, &quot;\\$&quot;);
if (str.contains(&quot;\&quot;&quot;))

