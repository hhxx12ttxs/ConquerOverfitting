* @since 12/08/2015.
*/
public interface Capacity {

int getValue();

static Capacity fixed(int capacity) {
if (capacity == -1) {
return Constants.INFINITE;
} else if (capacity == 0) {

