public int compareTo(Point other) {
long diff = (long) this.x - (long) other.x;
if (diff != 0)
return diff > 0 ? 1 : -1;

long diffY = (long) this.y - (long) other.y;
if (diffY != 0)

