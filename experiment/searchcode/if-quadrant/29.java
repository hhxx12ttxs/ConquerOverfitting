return Quadrant3;
}else if(xvalue < 0 &amp;&amp; yvalue > 0){
return Quadrant4;
}else{
return QuadrantONing;
String quadrant = getQuadrant(xvalue, yvalue);
if(Quadrant1.equals(quadrant)) return getQuadrant1();
if(Quadrant2.equals(quadrant)) return getQuadrant2();

