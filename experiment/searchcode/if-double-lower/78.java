public static double DBL_EPSILON = Math.pow (2.0 , -52);

public static double R_DT_0(boolean lower_tail, boolean log_p) {
if (lower_tail) {
return R_D__1(lower_tail, log_p);
}
}

public static double R_D__1(boolean lower_tail, boolean log_p) {
if (log_p) {
return 0.;

