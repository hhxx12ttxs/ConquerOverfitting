public void animate(long timeProgress) {
double nextX = targetX;
double nextY = targetY;
if (targetX != -1) {
+ Math.round((float) deltaX / getDuration() * timeProgress);
}
if (targetY != -1) {
double deltaY = targetY - initialY;

