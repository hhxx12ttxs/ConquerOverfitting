Permutation(columnIndex, 8, 0);
}

private static void Permutation(int columnIndex[], int length, int index) {
int i, temp;
if (index == length) {
if (Check(columnIndex, length) != 0) {
for (int j = 0; j < columnIndex.length; j++) {

