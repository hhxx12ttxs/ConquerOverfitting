ParsePosition pos = new ParsePosition(0);
char[] c = pattern.toCharArray();
int fmtCount = 0;
while (pos.getIndex() < pattern.length()) {
Validate.isTrue(foundDescriptions.size() == fmtCount);
if (c[pos.getIndex()] != END_FE) {

