public void setValue(int x, int y, double v) {
if (isValid(x, y)) {
values[x + y * width] = v;
}
}

public double getValue(int x, int y) {
if (!isValid(x, y)) {

