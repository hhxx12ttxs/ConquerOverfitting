hashvalue = tmphashvalue;
}

public Tuple(Tuple rhs) {
coords = new double[3];
coords[0] = rhs.coords[0];
if (!(obj instanceof Tuple)) {
return false;
}
Tuple t = (Tuple) obj;
double rhs_coords[] = t.getCoords();
return (coords[0] == rhs_coords[0]) &amp;&amp; (coords[1] == rhs_coords[1]) &amp;&amp;

