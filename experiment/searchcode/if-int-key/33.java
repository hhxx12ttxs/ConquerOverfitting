public class KeyController extends KeyAdapter {
static int noKey = -1;
int key = noKey;
int CENTRE = 0;
int LEFT = 4;

public int getDirection() {

if (key == KeyEvent.VK_DOWN) {
return DOWN;

