int nh = ih - i + edg1h[i - 1];
int nt = it + edg1t[i - 1];
if (nh + nt > R) continue;
if (enter[nh][nt] &amp;&amp; !exit[nh][nt]) {
int nh = node.h - i + edg1h[i - 1];
int nt = node.t + edg1t[i - 1];
if (nh + nt > R) continue;

