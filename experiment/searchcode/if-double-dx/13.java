public class SRM514 {

public double theMinDistance(int d, int x, int y) {

int dx = 0;

if (x > d)
dx = x - d;
else if (x < -d)
dx = -d - x;

int dy = 0;

if (y > d)
dy = y - d;

