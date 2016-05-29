g.setColor(Color.white);
if (hoverInv) {
g.fillRoundRect(invX, invY, invWidth, invHeight, 5, 5);
} else if (hoverLeave) {
if (x > invX &amp;&amp; x < invX + invWidth &amp;&amp; y > invY &amp;&amp; y < invY + invHeight) {
frame.toggleIp();
} else if (x > leaveX &amp;&amp; x < leaveX + leaveWidth &amp;&amp; y > leaveY

