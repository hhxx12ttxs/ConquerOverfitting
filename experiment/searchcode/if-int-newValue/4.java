public IntMinMax(IntMinMax rhs) {
this.min = rhs.min;
this.max = rhs.max;
}
public void init(int newValue) {
this.min = newValue;
this.max = newValue;
}
public void update(int newValue) {
if (newValue < min) {
min = newValue;

