static int[] selectionSort(int[] data) {

// 選択ソートの実装
int minPos, tmp;

for (int i = 0; i < data.length - 1; i++) {
minPos = i;
for (int j = i; j < data.length; j++) {
// data[i]～data[n]内最小値を持つ要素の添え字を調べる
if (data[minPos] > data[j]) {

