public void tryMove(double xa, double ya) {
onGround = false;
if (level.isFree(this, x + xa, y, w, h, xa, 0)) {
if (ya > 0) onGround = true;
hitWall(0, ya);
if (ya < 0) {
double yy = y / 10;

