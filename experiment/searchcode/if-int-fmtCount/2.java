break;
case &#39;{&#39;:
fmtCount++;
seekNonWs(pattern, pos);
int start = pos.getIndex();
Validate.isTrue(foundDescriptions.size() == fmtCount);
if (c[pos.getIndex()] != &#39;}&#39;) {
throw new IllegalArgumentException(&quot;Unreadable format element at position &quot; + start);

