org.apache.commons.math.stat.descriptive.moment.Skewness.copy(original, this);
}

@java.lang.Override
public void increment(final double d) {
if (incMoment) {
moment.increment(d);
}
}

@java.lang.Override
public double getResult() {
if ((moment.n) < 3) {

