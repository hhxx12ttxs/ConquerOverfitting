int len = num.length;

int i = len - 2;

while (i >= 0) {
if (num[i] < num[i + 1]) {
return;
}

int j = len - 1;

while (j > i) {
if (num[j] > num[i]) {

