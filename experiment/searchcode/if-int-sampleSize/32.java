private static double RADIUS = 1000.0;
private int sampleSize;

private ArrayList<Point> points;

private void assignPoints() {
for (int i = 0; i < sampleSize; i++)
points.add(new Point(Utils.randomTill(RADIUS), Utils.randomTill(RADIUS)));

