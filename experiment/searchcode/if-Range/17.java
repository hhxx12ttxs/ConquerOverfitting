* min and higher than the max values.   These ranges should not overlap, but if they do, assume the most
* severe range is to be applied.  All ranges are optional and it is quite allowed for there to be only one
public void addWatchRange(FloatRange range) {
if(this.watchRange == null) {
this.watchRange = range;
} else {

