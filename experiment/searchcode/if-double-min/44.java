public void accumulate(double value) {
if (set) {
min = Math.min(min, value);
} else {
min = value;
set = true;
}
}

public double result() {
if (set) {

