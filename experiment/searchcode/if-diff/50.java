for (int i = 0; i < A.length; i++) {
for (int j = i + 1; j < A.length; j++) {
int diff =  Math.abs(A[i]-A[j]) ;
if (diff <= D &amp;&amp; best < diff) {
best = diff;
}
}
}

return best;
}

