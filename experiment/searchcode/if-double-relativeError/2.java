public boolean checkAbsolute(double measured, double reference,
double absoluteError) {
if (Double.isInfinite(absoluteError))
public boolean checkRelative(double measured, double reference,
double relativeError) {
if (Double.isInfinite(relativeError))
return true;
return checkAbsolute(measured, reference, reference * relativeError);

