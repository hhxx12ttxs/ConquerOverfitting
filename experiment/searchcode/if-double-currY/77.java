FxUtil.setTransitionDuration(el(), 0);
if (currY != matrixY) {
// scroll on going
double diffY = currY - matrixY;
public void onDragMove(DragEvent e) {
double currY = getScrollPositionY();
if (currY > 0) {
// exceed top boundary
if (e.OffsetY > 0) {

