extendYTo(y);
}

public void extendYTo(double y) {
if (noYdata) {
ymin = y;
ymax = y;
noYdata = false;
xmax = d;
}
}
}

public void pushY(double d) {
if (noYdata) {
ymin = d;
ymax = d;

