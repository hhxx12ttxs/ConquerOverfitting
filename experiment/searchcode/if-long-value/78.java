package Task;

public class AliceGameEasy {
public long findMinimumValue(long x, long y) {
long turn = 0;
long n = (long)Math.sqrt((x + y) * 2);

if (n * (n+1) != (x+y) * 2) {

