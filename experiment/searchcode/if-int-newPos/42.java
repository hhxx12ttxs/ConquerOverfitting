if (orientation == Direction.UP || orientation == Direction.DOWN) {
if (dir == Direction.DOWN) {
if (!collisionMap[(int) newPos.getX()][(int) newPos.getY() + 2]
&amp;&amp; !collisionMap[(int) newPos.getX()][(int) newPos.getY() + 2]) {
pos = newPos;
}
} else if (dir == Direction.RIGHT) {

