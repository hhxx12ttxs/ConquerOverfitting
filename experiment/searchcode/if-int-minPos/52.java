minPos = i;
for (int j = i; j < list.length; j++) {
if (list[j] < list[minPos]) {
minPos = j;
}
}

if (minPos != i) {
int tmp = list[i];
list[i] = list[minPos];
list[minPos] = tmp;

