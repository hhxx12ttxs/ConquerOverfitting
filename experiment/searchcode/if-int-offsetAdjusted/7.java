if (zone != null) {
return zone;
}
if (id.startsWith(&quot;+&quot;) || id.startsWith(&quot;-&quot;)) {
int offset = parseOffset(id);
// adjust instantLocal using the estimate and recalc the offset
int offsetAdjusted = getOffset(instantLocal - offsetLocal);
// if the offsets differ, we must be near a DST boundary

