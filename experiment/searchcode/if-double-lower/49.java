public class SortEstimate {
public double howMany(int c, int time) {
double lower = 1;
double upper = Math.max((double) time / c, 2);
double current = (lower + upper) / 2;
if (f(c, current) < time) {
lower = current;
} else {
upper = current;

