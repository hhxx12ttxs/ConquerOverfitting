return retVal;
}

//returns true if t has definitely passed
public static boolean after(Instant t) {
Instant thisMoment = Instant.now();
if (thisMoment.minusNanos(epsilonNanos).isAfter(t)) {
return true;
} else {

