if (c-1 < 0) return false;
else return true;
}

public boolean moveRightOK ( int r, int c) {
if (c+1 >= nCol) return false;
System.out.print (&quot;\n|&quot;);
for (int j = 0; j < nCol; j++) {
if (r == i &amp;&amp; c == j)
System.out.print (&quot;:&quot;);
else System.out.print (board[i][j]);

