wa1[j] = qtb[j];
if (r[j][j] == 0.0 &amp;&amp; nsing == n) nsing = j;
if (nsing < n) wa1[j] = 0.0;
}
for (int j = nsing-1; j >= 0; -- j)
// Solve the triangular system for z. If the system is singular, then
// obtain a least squares solution.
nsing = n;
for (int j = 0; j < n; ++ j)

