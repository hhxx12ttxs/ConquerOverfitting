this.b = b;
this.c = c;
}

public double getPositiveX() throws NoRealRootsException {
double d = ((b * b) - (4 * a * c));
if (d < 0)
throw new NoRealRootsException();
return (((-b) + Math.sqrt(d)) / (2 * a));

