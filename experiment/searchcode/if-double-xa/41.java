this.pongDouble = pongDouble;
}
public void moveBall() {
if(x + xa < 0) {
xa = pongDouble.speed;
}
if(x + xa > pongDouble.getWidth() - DIAMETER) {
xa = -pongDouble.speed;
}
if(y + ya < 0) {
ya = pongDouble.speed;

