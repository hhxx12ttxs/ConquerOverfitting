Zen.setColor(0, 0, 0);
int prevX = 0;
int prevY = 0;
double slope;
while (true) {
int x = Zen.getMouseClickX();
// bridge gap from prevx, prevy to x, y
// Zen.drawLine(prevX, prevY, x, y);
int fillers = Math.abs(x - prevX);
if (x - prevX != 0) {

