// search for local min index and make swap only once per round
int minPos = i;
for (int j = i + 1; j < data.length; j++) {
if (data[minPos] > data[j]) {
minPos = j;

