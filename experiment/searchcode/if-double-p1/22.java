private static final double MIN_POS = Double.MIN_VALUE;
private DeltaP() {}

public static double logDiff(double p1, double p2, double norm) {
p1 = Math.min(p1, norm);
p2 = Math.min(p2, norm);
if (p1 == 0 || p2 == 0) return 0;
return p1 == p2 ? MIN_POS :

