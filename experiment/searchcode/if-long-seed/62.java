private static final DistributionRandom globalRnd = new DistributionRandom();
private static ThreadLocal<DistributionRandom> localRnd = null;
private static long STRING_SEED = &quot;DistributionRandom&quot;.hashCode() << (long) 32;

public static Random rnd() {
if (localRnd != null) {

