return accumulator;
}
private double getDelta(double previousAngle, double currentAngle, double tolerance)
{
double delta = currentAngle - previousAngle;
if(Math.abs(delta) > tolerance)

