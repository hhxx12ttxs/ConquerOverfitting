this.p1 = p1;
this.p2 = p2;

if (p1.x == p2.x) {
this.isVertical = true;
this.m = 0;
this.m = (p1.y - p2.y) / (p1.x - p2.x);
this.c = p1.y - p1.x * this.m;
}
}

public double f(double x, double y)

