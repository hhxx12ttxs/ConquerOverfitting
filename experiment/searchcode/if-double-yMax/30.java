xmax = p.x;
}
if(p.y > ymax) {
ymax = p.y;
}
}
if(Double.isNaN(xmin)) {
xmax = Double.MAX_VALUE;
}
if(Double.isNaN(ymin)) {
ymin = Double.MIN_VALUE;
}
if(Double.isNaN(ymax)) {

