public int minimize(String A, String B) {
int minDiff = A.length();
int lengthDiff = B.length() - A.length();
for (int i = 0; i <= lengthDiff; i++) {
int diff = difference(A, comparable);
if (diff < minDiff) {
minDiff = diff;
}
}
return minDiff;
}

private int difference(String A, String B) {

