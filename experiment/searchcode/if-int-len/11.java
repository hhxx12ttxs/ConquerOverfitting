for(int i = 0; i < S.length - 1; i++){
P[2 - 1][i] = S[i] == S[i + 2 - 1];
}

// len 3 to max
for(int len = 3; len <= S.length; len++){

for(int i = 0; i < S.length - (len - 1); i++){
P[len - 1][i] = P[len - 1 - 2][i + 1] &amp;&amp; S[i] == S[i + len - 1];

