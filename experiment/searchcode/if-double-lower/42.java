higher = item;
}
}
return higher;
}

public double lower() {
double lower = Double.POSITIVE_INFINITY;
for (double item : this.collection) {
if (item < lower) {

