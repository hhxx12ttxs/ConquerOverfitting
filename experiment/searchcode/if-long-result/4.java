public long countGoodSequences(long K, long A, long B) {
if (A == B)
return go(A, K);
if (K > B)
return 0;
long result = 0;
if (A <= K &amp;&amp; K <= B) {
result += 2 * countGoodSequences(K, A >> 1, B >> 1);
return result;
}

private long go(long a, long k) {
if (a < k)
return 0;
if (a == k)

