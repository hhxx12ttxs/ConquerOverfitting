for (int i = 0; i < n; ++i) {
long l = prev(x), r = next(x);
int k = 1;
while (i - k >= 0 &amp;&amp; i + k < n &amp;&amp; l < x &amp;&amp; r < x) {
x = next(x);
}

if (ans < 0) ans += MOD;
return ans;
}

private long next(long x) {

