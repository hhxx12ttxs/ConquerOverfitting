private final String value;

public StringTag(String name, String value) {
super(name);
this.value = value;
String name = getName();
String append = &quot;&quot;;
if ((name != null) &amp;&amp; (!name.equals(&quot;&quot;))) {
append = &quot;(\&quot;&quot; + getName() + &quot;\&quot;)&quot;;

