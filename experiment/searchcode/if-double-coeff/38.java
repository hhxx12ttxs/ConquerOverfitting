this.scale = scale;
}

private int coeff(double x) {
return (int)(Math.abs(x)/this.scale);
}

public Color color(Vector point, Vector direction) {
int coeff = 0;
if (0 <= point.x)
coeff += 1;
if (0 <= point.y)

