public TrailSegment(double x, double y, double rot, int color, TrailSegment previous){
super(x,y, rot);
public void render(int xPos, int yPos, RenderBuffer render){
if (previous != null){
int steps = 32;
if (rot == previous.getRot()){

