public void init (Level level) {
this.level = level;
}

public void tryMove (double xa, double ya) {
onGround = false;
if (level.isFree(this, x + xa, y, w, h, xa, 0)) {
if (level.isFree(this, x, y + ya, w, h, 0, ya)) {
y += ya;
} else {
if (ya > 0) onGround = true;
hitWall(0, ya);
if (ya < 0) {
double yy = y / 10;

