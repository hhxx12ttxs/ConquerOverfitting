if (value.indexOf(typestring) == 0) {
int pfxlen = typestring.length();
if (value.charAt(pfxlen) == &#39;M&#39;) {
value = value.substring(pfxlen).trim();
} else if (value.charAt(0) != &#39;(&#39;) {
// we are neigher inner nor outer rep.

