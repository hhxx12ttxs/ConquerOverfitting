public class LongArithmetic {

/**
* @Ensures false;
*/
public long add_longs(long a, long b) {
long ret = (a + b);
public boolean add_overflow(long a) {
long max_val = Long.MAX_VALUE;

boolean ret_val = true;
if (a>0) {

