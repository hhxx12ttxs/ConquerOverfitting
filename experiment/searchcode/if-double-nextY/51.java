public static void fractalLine(int x, int y, double h, double r) {

if(h <= 1){
p.flyTo(x, y);
int nextX = x + (int)(h / 3.0 * Math.cos(Math.toRadians(r)));
int nextY = y + (int)(h / 3.0 * Math.sin(Math.toRadians(r)));
fractalLine(nextX, nextY, h/3.0, r-60);

