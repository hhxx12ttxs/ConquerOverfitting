A)
//@ assert true;
//@ assert 0 == (\sum int k; b.length - 1+1 <= k &amp;&amp; k < b.length; b[k]) &amp;&amp; -1 <= b.length - 1 &amp;&amp; b.length - 1 < b.length;
i&#39; = b.length - 1;
//@ assert 0 == (\sum int k; i&#39;+1 <= k &amp;&amp; k < b.length; b[k]) &amp;&amp; -1 <= i&#39; &amp;&amp; i&#39; < b.length;

