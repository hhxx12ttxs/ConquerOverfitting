String typestring = getTypeString();
if (value.indexOf(typestring) == 0) {
int pfxlen = typestring.length();
if (value.charAt(pfxlen) == &#39;M&#39;) {
pfxlen += 1;
haveM = true;

