public void draw(Graphics g) {
Point p1, p2;
p1 = classFrom.getBorderPoint(borderPointFrom);
if (classTo == null) {
int len = (int)Math.sqrt((p2.x - p1.x)*(p2.x - p1.x)+(p2.y - p1.y)*(p2.y - p1.y));
if (len == 0) len = 1;
int vectorLen = 15;
Point p = new Point(vectorLen*(p1.x - p2.x)/len, vectorLen*(p1.y - p2.y)/len);

