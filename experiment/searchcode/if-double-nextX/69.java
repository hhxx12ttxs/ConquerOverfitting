double x = getX();
double y = getY();

double nextX = 0, nextY = 0;
double preferredAngle = ONE_QUARTER + (ONE_QUARTER / (1 + Math.exp((distance - 350) / 100)) - ONE_EIGHTH);
for (int i = 0; i < 2; i++) {
double a = preferredAngle;
do {
nextX = x + Math.sin(enemyHeading + a * _direction) * 65;

