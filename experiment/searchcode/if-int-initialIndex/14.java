public String[] parseTD(CppString source) {
int initialIndex = source.indexOf(&quot;<td>&quot;);
int finalIndex = source.indexOf(&quot;</tr>&quot;);
source = source.substring(initialIndex, finalIndex);
public String parseNextAnchorHref(String source) {

int initialIndex = source.indexOf(&quot;<a&quot;);
int endingIndex = source.indexOf(&quot;>&quot;,initialIndex);

