public static double yMax = 0;

public BoundChecker(){}

public void setBound(double xmin, double ymin, double xmax, double ymax){
public boolean checkBound(double x, double y){
if(xMin < x &amp;&amp; x < xMax &amp;&amp; yMin < y &amp;&amp; y < yMax)
return true;
return false;

