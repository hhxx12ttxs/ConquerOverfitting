for (int j = i - 1; j >= 0; --j) {
if (num[i] > num[j]) {
if (p == -1) {
} else if (j == p &amp;&amp; num[i] < num[q]) {
p = j;
q = i;
}
}
}
}
if (p != -1) {
int t = num[p];

