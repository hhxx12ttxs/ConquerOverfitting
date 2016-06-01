public double toNanos(double d)   { return d; }
public double toMicros(double d)  { return d/(C1/C0); }
public double toMillis(double d)  { return d/(C2/C0); }
public double convert(double d, TimeUnit u) { return u.toNanos(d); }
},
MICROSECONDS {
public double toNanos(double d)   { return x(d, C1/C0, MAX/(C1/C0)); }

