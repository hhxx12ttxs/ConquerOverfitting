public static Toss getRandom(double radius) {
double x,y;
double size = Math.random();
double sign = Math.random();
size = size * radius;
sign = Math.random();
size = size * radius;
if (sign>0.5)
y = size;
else
y = - size;
return new Toss(x,y);
}
private double x,y;
}

