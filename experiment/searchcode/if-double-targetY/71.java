double deltaY = -(origin.getY() - target.getY());

double angleCompletion = 0;
if (deltaX > 0) {
angleCompletion = PI;
}

double angle = Math.atan(deltaY / deltaX) + angleCompletion;

double targetX;
double targetY;

if (!targeting) {

