containerPathString = container.getProjectRelativePath()
.toPortableString();

if (cLoc.isPrefixOf(gLoc)) {
int matchingSegments = gLoc.matchingFirstSegments(cLoc);
final String p = location.toString();
final int pLen = p.length();
if (pLen > pfxLen)
return p.substring(pfxLen);
else if (p.length() == pfxLen - 1)

