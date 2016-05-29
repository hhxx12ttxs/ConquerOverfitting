Task t = get(i);
long o = t.occurrence();
if (o < tmin) tmin = o;
if (o > tmax) tmax = o;
protected Task addItem(@NotNull Task i) {
long occ = i.occurrence();
if ((occ < min) || (occ > max)) {

