double t = (double) j / segmentNum;
double nextX = getX(t, v);
double nextY = getY(t, v);
if (j > 0) {
length += distance(x, y, nextX, nextY);
}
x = nextX;
y = nextY;
}
double newResult = 100 * (length - Math.PI / 2) / (Math.PI / 2);

