for (int i = maxStart; i < 1_000_000; i++) {
int length = getCollatzLength(i);
if (length > maxLength) {
maxStart = i;
private int getCollatzLength(long num) {
if (num == 1) return 1;
if (cache.get(num) != null) return cache.get(num);

