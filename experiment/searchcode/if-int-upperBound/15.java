public class Range implements Comparable<Range> {
private int lowerBound;
private int upperBound;

Range(int lowerBound, int upperBound) {
assert(lowerBound <= upperBound);
this.upperBound = Math.min(this.upperBound, range.getUpperBound());
}

@Override
public int compareTo(Range range) {
if(this.upperBound < range.lowerBound) {

