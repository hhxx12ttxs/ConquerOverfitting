nextLocal = Long.MAX_VALUE;
}
long nextAdjusted = nextTransition(instantLocal - offsetAdjusted);
if (nextAdjusted == (instantLocal - offsetAdjusted)) {
int offset = getOffset(instantUTC);
long instantLocal = instantUTC + offset;
// If there is a sign change, but the two values have the same sign...

