public String toReplacedHTML(String initData){

int initialIndex = 0;
int inIndex;
int finalIndex;
URL url = getClass().getResource(&quot;/WebContent&quot;+sb.substring((inIndex) , (finalIndex)));
if(url==null)
continue;
sb.replace(initialIndex, finalIndex, url.toString());

