Point p2 = super.getPoint();

int dx = p2.x - p1.x;
int dy = p2.y - p1.y;

if(dx == 2 &amp;&amp; dy == 0){ //prawo
p2.move(p2.x+2, p2.y);
if(!(p2.x<rozmiarx))
p2.move(p2.x-8, p2.y);

