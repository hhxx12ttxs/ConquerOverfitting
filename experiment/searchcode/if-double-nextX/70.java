throw new UnsupportedOperationException();
}

public void forwardOne() {
if (++nextX <= maxX) {
return;
}
nextX = min().getBlockX();

if (++nextY <= maxY) {

