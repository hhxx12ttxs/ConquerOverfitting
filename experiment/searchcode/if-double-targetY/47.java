double vx = 0;
double vy;
double ax = 0;
double ay;
double bearing;

double targetY;

double size = 200;
//draw a ray trace from the center of the back of the goal to the ball, find where the goalie should be
double x0 = rayOrigin.x;
double y0 = rayOrigin.y;
double x1 = 0;
double y1 = 0;
if(ballPossessor != -1){

