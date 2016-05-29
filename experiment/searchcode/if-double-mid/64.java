double l, double r, Function<Double, Double> f, double eps) {
double mid = (l + r) * 0.5;
double fml = f.value((l + mid) * 0.5);
double fmr = f.value((mid + r) * 0.5);
double slm = simpson(fl, fmid, fml, l, mid);
double smr = simpson(fmid, fr, fmr, mid, r);

