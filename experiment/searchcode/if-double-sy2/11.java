int sy2 = (int) (this.wy + this.wheight);
if (sx2 <= xBound &amp;&amp; sy2 <= yBound) {
Draw.image(dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this.getZ(),this.image);
} else if (sx2 >= xBound &amp;&amp; sy2 <= yBound) {

