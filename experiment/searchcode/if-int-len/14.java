int len = A.length;
int i = 0;

while (i < len) {
if (A[i] > 0 &amp;&amp; A[i] <= len &amp;&amp; A[i] != i + 1 &amp;&amp; A[i] != A[A[i] - 1]) {
i--;
}

i++;
}

for (int j = 0; j < len; j++) {
if (A[j] != j + 1) {
return j + 1;
}
}

return len + 1;
}
}

