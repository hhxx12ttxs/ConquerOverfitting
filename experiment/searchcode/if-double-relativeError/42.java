private double x;
private double y;

public Vecteur(double x, double y) {
this.x = x;
this.y = y;
Vecteur other = (Vecteur) obj;
double relativeError = 0.001;
return (Math.abs(other.x - x) < relativeError &amp;&amp; Math.abs(other.y - y) < relativeError);

