// Layout the thingies left to right then down to up
double currX = 0;
double currY = 0;
double maxY = 0;

Map<String, Point2D.Double> layout = new TreeMap<String, Double>();
for (int i = 0; i < slides.size(); i++)
{
layout.put(slides.get(i).getTitle(), new Point2D.Double(currX, currY));
maxY = Math.max(maxY, currY + slides.get(i).getNode().getHeight());

