if (e > umax)
umax = e;
if (e < umin)
umin = e;
u = u + e / n;
}
dt[1] = Math.sqrt(q / n);
partial.in_n += other.in_n;

return true;
}

public DoubleWritable terminate() {
if (partial == null) {

