public static TruncatedRadial3d<Spheroid3d> truncatedSpheroidFromGradient(double r0, double r1,
double h, double m) {
if (m > 0) return spheroidFromPositiveGradient(r0, r1, h, m);
private static TruncatedRadial3d<Spheroid3d> spheroidFromPositiveGradient(double r0, double r1,
double h, double m) {
if (r0 >= r1) throw new IllegalArgumentException(&quot;r1 must be > &quot; + r0 + &quot;: &quot; + r1);

