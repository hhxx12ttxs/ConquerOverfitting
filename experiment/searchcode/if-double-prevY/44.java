private double slope;
private double prevx = 0.5;
private double slxx;
private double prevy = 0.5;
double maxDistance = (Ball.HEIGHT_SCALE + Paddle.HEIGHT_SCALE) / 2.0;

double yintercept = (prevy-(slope*prevx));
if (ball.getVelocity()[0] < 0) {

