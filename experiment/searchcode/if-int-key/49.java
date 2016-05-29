public void keyPressed(KeyEvent e){
int key = e.getKeyCode();
if(key == KeyEvent.VK_LEFT)
dx = -1;
if(key == KeyEvent.VK_RIGHT)
if(key == KeyEvent.VK_DOWN)
dy = -1;
}

public void keyReleased(KeyEvent e){
int key = e.getKeyCode();
if(key == KeyEvent.VK_LEFT)

