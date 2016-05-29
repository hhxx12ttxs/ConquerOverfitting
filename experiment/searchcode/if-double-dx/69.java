double x = (point - centre) / scale;
if ((x > -0.5D) &amp;&amp; (x < 0.5D)) {
double xs = 4.0D * x * x;
depth = (-96.0D * xs * xs * xs + 193.0D * xs * xs - 98.0D * xs + 1.0D) * scale;
}
return depth;
}

public double twist(double x, double y) {
double dx = 0.0D;

