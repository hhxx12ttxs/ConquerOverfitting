(nrow . ncol) = (crow . ccol) + move[i]
if (nrow >= 0 &amp;&amp; nrow < height &amp;&amp; ncol >= 0 &amp;&amp; ncol < width) {
if (maze[nrow][ncol] == &#39;.&#39; &amp;&amp; mark[nrow][ncol] == 0) {
mark[nrow][ncol] = mark[crow][ccol] + 1;

