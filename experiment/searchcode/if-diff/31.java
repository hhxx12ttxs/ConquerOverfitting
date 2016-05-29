double diffX = reference.x - target.x;
double diffY = reference.y - target.y;

if (diffX == 0 &amp;&amp; diffY > 0) {
return NORTH;
} else if (diffX < 0 &amp;&amp; diffY > 0) {
return NORTHEAST;
} else if (diffX < 0 &amp;&amp; diffY == 0) {

