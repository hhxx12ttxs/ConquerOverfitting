// sort the optima from best to worst, followed by NaN elements
int lastNaN = optima.length;
for (int i = 0; i < lastNaN; ++i) {
Arrays.sort(optima, 0, lastNaN);
if (goalType == GoalType.MAXIMIZE) {
for (int i = 0, j = lastNaN - 1; i < j; ++i, --j) {

