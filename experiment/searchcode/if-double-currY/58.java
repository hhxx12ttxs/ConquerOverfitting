@Override
public CoordProxy getNextStep(int currX, int currY, int targetX, int targetY) {
double i, L;
L = Math.sqrt((targetX - currX)*(targetX - currX)+ (targetY - currY)*(targetY - currY));

if (L == 0) {
dX = 0;
dY = 0;

