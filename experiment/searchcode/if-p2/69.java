return false;
}
if (Math.abs(p1.x) == Math.abs(p1.y)) {
if (p.x > p2.x &amp;&amp; p.y > p2.y) {

while (p.x > ++p2.x &amp;&amp; p.y > ++p2.y) {
Piece pp = b.getPiece(p2);
if (pp != null) {
return false;

