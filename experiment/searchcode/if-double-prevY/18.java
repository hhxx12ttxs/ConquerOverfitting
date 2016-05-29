public double x, y;
private double prevX, prevY;
private double magSquared;
private double magnitude;

public Vec2() {
y/=other.y;
return this;
}

public void updateVars(){
if(!(prevX==x &amp;&amp; prevY==y)) {
magSquared = pow(x, 2) + pow(y, 2);

