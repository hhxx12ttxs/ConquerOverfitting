public static String extractString(List<MatchingCharacter> list)
{
String ret = &quot;&quot;;
for (MatchingCharacter chr : list)
if (chr == null)
ret += &quot;-&quot;;
else
ret += chr.getCharacter();
while (ret.length() > 0 &amp;&amp; ret.charAt(0) == &#39;-&#39;)

