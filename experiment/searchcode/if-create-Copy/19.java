public class HoldRef2 extends Applet {
private Graphics copy;
private boolean first = true;

public void paint(Graphics g) {
if(first) {
// note: copy is never disposed of
copy = g.create();

copy.setColor(Color.red);

