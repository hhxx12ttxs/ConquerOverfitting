double x0 = min;
double x1 = max;
double y0 = f.value(x0);
double y1 = f.value(x1);
if ((y0 * y1) >= 0) {
throw org.apache.commons.math.MathRuntimeException.createIllegalArgumentException((&quot;function values at endpoints do not have different signs, &quot; + &quot;endpoints: [{0}, {1}], values: [{2}, {3}]&quot;), min, max, y0, y1);
}
double x2 = x0;
double y2 = y0;
double oldDelta = x2 - x1;
int i = 0;

