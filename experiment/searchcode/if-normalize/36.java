final static public String getAreaCode(String normalizeString) {
if(normalizeString.length() == 7 || normalizeString.length() == 8 ) {
if(normalizeString.endsWith(&quot;000&quot;)) return null ;
return null ;
} else if(normalizeString.length() >= 9 &amp;&amp; normalizeString.length() <= 11 ) {

