private boolean moveLeft() {
// 1マス先の座標
int nextX = x - 1;
int nextY = y;
if (nextX < 0) {
nextX = 0;
}
// その場所に障害物がなければ移動を開始
if (!map.isHit(nextX, nextY)) {

