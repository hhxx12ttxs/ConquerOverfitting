currentCount = 0;
for (int currx = 0; currx < width; currx++) {
for (int curry = 0; curry < height; curry++) {
if (field.get(currx, curry)
&amp;&amp; !(currx == x &amp;&amp; curry == y)
&amp;&amp; Math.ceil(dist(x, y, currx, curry)) <= radius) {

