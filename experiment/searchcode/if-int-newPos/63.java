} else if (dir == Direction.LEFT) {
if (!collisionMap[(int) newPos.getX() + 2][(int) newPos.getY()]
&amp;&amp; !collisionMap[(int) newPos.getX()][(int) newPos.getY() + 1]) {
pos = newPos;
}
} else if (dir == Direction.RIGHT) {

