ParsePosition pos = new ParsePosition(0);
char[] c = pattern.toCharArray();
int fmtCount = 0;
while (pos.getIndex() < pattern.length())
String name = desc;
String args = null;
int i = desc.indexOf(&#39;,&#39;);
if (i > 0)
{
name = desc.substring(0, i).trim();

