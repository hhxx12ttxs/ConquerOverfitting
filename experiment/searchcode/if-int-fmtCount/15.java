final ParsePosition pos = new ParsePosition(0);
final char[] c = pattern.toCharArray();
int fmtCount = 0;
Validate.isTrue(foundFormats.size() == fmtCount);
Validate.isTrue(foundDescriptions.size() == fmtCount);
if (c[pos.getIndex()] != END_FE) {

