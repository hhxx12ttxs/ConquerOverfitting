private double epsilon = 0.08;

public Coalescor() {
this.oldValue = 0;
}

public Coalescor(double oldValue) {
this.oldValue = oldValue;
}

public double coalesce(double x) {
if (x > oldValue) {
if (oldValue + epsilon > x) {

