public void increment(final double d) {
if (incMoment) {
moment.increment(d);
}
}

@java.lang.Override
public void clear() {
if (incMoment) {
moment.clear();
}
}

@java.lang.Override
public double getResult() {

