LinkedList<Range> ret = new LinkedList<Range>();
for (Range range : sortedRanges) {
if (iRange == null) {
iRange = range;
} else if (iRange.getTo().equals(range.getFrom())) {
iRange = new Range(iRange.getFrom(), range.getTo());
} else {
ret.add(iRange);
iRange = range;
}
}
if (iRange != null) {
ret.add(iRange);

