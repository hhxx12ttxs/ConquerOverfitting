int xA = locationA.getX();
int yA = locationA.getY();
int xB = locationB.getX();
int yB = locationB.getY();

if (wallDirection == Direction.RIGHT) {
for (int wy = wallY; wy < wallY + wallLenght; wy ++) {
if ((yA == wy &amp;&amp; xA == wallX - 1 &amp;&amp; xB == wallX &amp;&amp; (yB == yA || yB == yA - 1 || yB == yA + 1))

