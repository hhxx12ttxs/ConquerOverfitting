this.x = x;
this.y = y;
}

public int compareTo(Point other) {
long diff = (long) x - other.x;
if (diff != 0)
return diff > 0 ? 1 : -1;

diff = (long) y - other.y;
if (diff == 0)
return 0;

