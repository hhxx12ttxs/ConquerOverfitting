private double y;
private double y2;

public InterpLinear(double x, double x2, double y, double y2) {
this.x = x;
this.y = y;
this.x2 = x2;
this.y2 = y2;
if (x == x2)
return;

