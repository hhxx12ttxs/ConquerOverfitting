int y = i.getImagePosY();
Direction d = i.getDirection();

int newPos;
switch (d) {
case UP:
newPos = y - getDelta();
if (newPos < 0) {

