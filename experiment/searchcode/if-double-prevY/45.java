prevY -= center[3];
speed[2] -= prevX;
speed[3] -= prevY;
double xVal = speed[2] - center[2];
double radius = Math.sqrt(Math.abs(xVal*xVal)+Math.abs(yVal*yVal));
if(Double.isNaN(xVal)){
System.out.println(xVal);

