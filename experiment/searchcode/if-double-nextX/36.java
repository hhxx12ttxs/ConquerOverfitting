public void animate(long timeProgress) {
double nextX = targetX;
double nextY = targetY;
if (targetX != -1) {
double deltaX = targetX - initialX;
nextX = initialX
+ Math.round((float) deltaX / getDuration() * timeProgress);

