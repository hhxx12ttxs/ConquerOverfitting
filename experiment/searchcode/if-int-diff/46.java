for (i = 0; i < arr.length; i++) {
int diff = arr[i] - arr[min];

if (diff < 0)
min = i;
else {
if (diff > max) {
for (int j = i + 1; j < arr.length; j++) {
int diff = arr[j] - arr[i];
if (diff > 0 &amp;&amp; maxDiff < diff) {
maxDiff = diff;
}
}
}

return maxDiff;
}

}

