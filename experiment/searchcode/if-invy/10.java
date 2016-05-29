g.drawImage(buf, 0, gameRightY, 520, gameHeight, 0, 340, 520, 503, null);
}
if (invyEnabled) {
g.drawImage(buf, gameRightX, invyTop, gameWidth, gameHeight, 520, 0, 765, 503, null);
int x = e.getX();
int y = e.getY();
if (x < invyRect.x &amp;&amp; y < chatRect.y) {
clientCanvas.dispatchEvent(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)(e.getX()*screenScaleX)+5, (int)(e.getY()*screenScaleY)+5, e.getClickCount(), e.isPopupTrigger()));

