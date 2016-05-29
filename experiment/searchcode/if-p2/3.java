Point p2 = super.getPoint();
int dx = p2.x - p1.x;
int dy = p2.y - p1.y;
if(dx == 2 &amp;&amp; dy == 0){ //prawo
p2.move(p2.x+1, p2.y);
if(!(p2.x<rozmiarx))
p2.move(p2.x-6, p2.y);

