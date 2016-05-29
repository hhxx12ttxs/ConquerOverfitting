public static GVector2f getClosestPointOnLine(float sx1, float sy1, float sx2, float sy2, float px, float py){
double xDelta = sx2 - sx1;
double yDelta = sy2 - sy1;

double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
float px,  float py,  float pz){
double xDelta = sx2 - sx1;
double yDelta = sy2 - sy1;
double zDelta = sz2 - sz1;

