double targetX = xSpeed * elapsedTime + myXPos;
double targetY = ySpeed * elapsedTime + myYPos;
checkOvershoot(targetX, targetY, xSpeed, ySpeed);
if (myCurrentTarget >= myPath.length)
myCurrentTarget = 0;
}

private void checkOvershoot(double targetX, double targetY, double xSpeed, double ySpeed) {

