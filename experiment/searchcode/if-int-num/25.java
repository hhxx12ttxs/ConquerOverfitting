int j = -1;
for (int i = 0; i < num.length - 1; i++) {
if (num[i] < num[i + 1])
j = i;
}
if (j == -1) {
for (int i = j + 1; i < num.length; i++) {
if (num[i] > num[j] &amp;&amp; num[i] <= num[k])
k = i;
}
int u = num[j];

