public int getPosition(double targetY) {

//
double halfInterval = myLineInterval * 0.5;
double currentY = myPositionZero;
int currentPos = 0;
int finalPos = 0;

//
if (targetY >= currentY) {

