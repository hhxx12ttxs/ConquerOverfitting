this.threshold = threshold;
}

public boolean isNoise(Point previous, Point current) {
if (previous == null || current == null) return true;
int dy = current.y - previous.y;
double r = ratio(previous, current);
kicked.translate((int)(dx * r), (int)(dy * r));

