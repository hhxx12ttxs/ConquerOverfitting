ParsePosition pos = new ParsePosition(0);
char[] c = pattern.toCharArray();
int fmtCount = 0;
case &#39;{&#39;:
fmtCount++;
seekNonWs(pattern, pos);
int start = pos.getIndex();

