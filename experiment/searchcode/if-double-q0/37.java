double cz = z.get(z.size() - 1);
double k0 = first(cx, cy, cz);
double q0 = second(cx, cy, cz);
double p0 = third(cx, cy, cz);
double k1 = first(cx + dt / 2 * k0, cy + dt / 2 * q0, cz + dt / 2 * p0);

