public class AliceGameEasy {
public long findMinimumValue(long x, long y) {
long square = (x + y) * 2;
long root = (long)Math.sqrt(square);
if(root * (root + 1) != square) {

