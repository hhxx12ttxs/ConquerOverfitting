public static void fractalLine(int x, int y, double h, double r) {

if(h <= 1){
p.flyTo(x, y);
// 第二段,起點(x,y)+h/3+(cos(r),sin(r)),長度h/3,角度r+60
int nextX = x + (int)(h / 3.0 * Math.cos(Math.toRadians(r)));

