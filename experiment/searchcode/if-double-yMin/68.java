abstract public double getXMin();

abstract public double getXMax();

abstract public double getYMin();

abstract public double getYMax();

public boolean contains(GeometricShape z) {
boolean r=false;
if((this.getXMin() <= z.getXMin()) &amp;&amp;

