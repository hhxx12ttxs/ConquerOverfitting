public class GasStation {
public int canCompleteCircuit(int[] gas, int[] cost) {
int len = gas.length;
for (int i = 2 * len - 1; i >= 0; --i) {
int remain = gas[i % len] - cost[(i + len - 1) % len];
total += remain;
if (total < 0) {

