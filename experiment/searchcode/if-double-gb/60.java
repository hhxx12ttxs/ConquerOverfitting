if (size > GB)
disPlaySize = String.format(&quot;%.2f GB &quot;, (double)size / GB);
else if (size < GB &amp;&amp; size > MG)
disPlaySize = String.format(&quot;%.2f MB &quot;, (double)size / MG);
else if (size < MG &amp;&amp; size > KB)

