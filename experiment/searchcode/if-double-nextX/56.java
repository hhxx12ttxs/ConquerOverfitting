if (movingRight || Main.sns.getO()) {
if (Main.sns.getO()) {
setMovingRight(Main.sns.getO());
}
int nextX = 0;
int nextY = 0;
// tangent sin and cos, counting speed to move across diagonal line
if (this.getIY() != this.getNCY() &amp;&amp; this.getNSY() != this.getFY()) {
double p = this.getNCX() - this.getIX();

