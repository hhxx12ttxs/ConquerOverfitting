static double sxy;
static double sy2;

static FitResult calcres() {
FitResult res = new FitResult();
double ss = s0 * sx2 - sx * sx;
res.residual = sy2 - sy * sy / s0;
ErrorLog.assertion(s0 > 0);
ErrorLog.checkDouble(sy);
ErrorLog.checkDouble(sy2);
ErrorLog.checkDouble(res.residual);
}
return res;
}
}

