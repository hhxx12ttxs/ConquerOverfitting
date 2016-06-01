private int picHeight = 8; // Works better with power  of 2
private PVector captureOffset;

private boolean invY;

// output
protected int col;
public void setPosition(PVector pos) {
this.pos.set(pos);

if (boardView != null) {
if (invY) {
boardView.setBottomLeftCorner(new PVector(pos.x, paperScreen.drawingSize.y - pos.y));

