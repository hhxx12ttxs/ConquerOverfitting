public void keyTyped(KeyEvent e) {}

@Override
public void keyPressed(KeyEvent e) {
int key = e.getKeyCode();
pressed = true;
public void keyReleased(KeyEvent e) {
int key = e.getKeyCode();
pressed = false;
if(key==KeyEvent.VK_LEFT||key==KeyEvent.VK_A){

