long curScanOffset = scanIndex.firstEntry().getValue();
int scanNumInternal = 1, nextScanNumRaw, length;
long nextScanOffset;
if (curScanNumRaw == Integer.MAX_VALUE) {
&quot;The index contained an element less than zero: &#39;%s&#39;&quot;, matcherIdxEntry.group(0)));
}
if (offsetCur <= offsetPrev) {
throw new IndexBrokenException(String.format(

