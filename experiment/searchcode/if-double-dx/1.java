return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
}

public void add(Vector v) {
if (Double.isNaN(v.dX)) {
v.dX = 0;
}
if (Double.isNaN(v.dY)) {
v.dY = 0;

