sb.append(nextY + &quot; &quot; + nextX + &quot; &quot;);
if ((nextY % 2 == 1 &amp;&amp; nextX == m) || (nextY % 2 == 0 &amp;&amp; nextX == 1)) nextY++;
else if (nextY % 2 == 0) nextX--;
else nextX++;
sb.append(nextY + &quot; &quot; + nextX + &quot; &quot;);

