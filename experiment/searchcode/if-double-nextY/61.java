double centerX = circle.getLayoutX();
double centerY = circle.getLayoutY();

double nextX = centerX - 2 * mouseX;
double nextY = centerY + 2 * mouseY;
circle.setLayoutX(Main.SCENE_X - 1 * circle.getRadius());
else if (nextX < 0)
circle.setLayoutX(circle.getRadius());

if (nextY <= Main.SCENE_Y &amp;&amp; nextY >= 0) {

