for(Range<V> checkRange : this.rangeList) {
if(checkRange.isInRange(value)) {
rangeValue = checkRange.getRangeValue();
for(Range<V> range : this.rangeList) {
range.setStart(end);
end += range.getRangeSize();
}

if(this.defaultRange != null) this.defaultRange.setRange(end, this.subRangeSize);

