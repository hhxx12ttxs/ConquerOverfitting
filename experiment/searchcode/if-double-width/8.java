public double y;
public double width;
public double height;

public MapBoundingBox(double x, double y, double width, double height) {
this.height = height;
}
public boolean contains(double x, double y) {
if(x >= this.x &amp;&amp; x <= (this.x+this.width)) {

