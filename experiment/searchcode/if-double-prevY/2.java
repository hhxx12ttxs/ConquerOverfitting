int toReturn[] = new int[5];
int i = 0;

if(prevX == parent.x &amp;&amp; prevY == parent.y)
i = -1;
else {
if(parent.y < prevY)
i = 2;
else if(parent.y > prevY)
i = 1;
else if(parent.x < prevX)

