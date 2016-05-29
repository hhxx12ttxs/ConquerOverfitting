(xb == 0  &amp;&amp; yb == 1 &amp;&amp; zb == 0)  ||
(xb == 0 &amp;&amp; yb == -1 &amp;&amp; zb == 0))) {
continue;
}

if (block.getRelative(xb, yb, zb).getTypeId() == 63
|| block.getRelative(xb, yb, zb).getTypeId() == 68) {

Sign sign = (Sign) block.getRelative(xb, yb, zb).getState();
if (sign.getLine(0).toLowerCase().endsWith(&quot;[dewdd]&quot;) == true

