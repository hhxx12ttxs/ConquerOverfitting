package engine;

public class DoublePoint {

public double x = 0.0;
public double y = 0.0;
public DoublePoint invert() {
multiply(-1);
return this;
}

public DoublePoint normalize()
{
if (x >= y) {

