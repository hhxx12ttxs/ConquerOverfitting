Circle c = new Circle(ns[n]);
double dSq = axis.distanceSquared(c.center());
double d = Math.sqrt(dSq);
if(d<r+c.getRadius() &amp;&amp; d>Math.abs(r-c.getRadius())){
if(c.contains(p)){
double dSq = axis.distanceSquared(c.center());
double d = Math.sqrt(dSq);
if(d<r+c.getRadius() &amp;&amp; d>Math.abs(r-c.getRadius())){

