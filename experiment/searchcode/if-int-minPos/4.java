for (int i = 0; i < a.length - 1; i++) {
int minPos = minimumPos(a, i);
ArrayUtil.swap(a, minPos, i);
}
}

private static int minimumPos(int[] a, int from) {
int minPos = from;
for (int i = from + 1; i < a.length; i++) {

