public void setPadding(int padding, int alignment) {
if ((alignment == TOP || alignment == BOTTOM || alignment == LEFT || alignment == RIGHT) &amp;&amp; padding >= 0) {
notifyStyleChanged();
}

public int getPadding(int alignment) {
if (alignment == TOP || alignment == BOTTOM || alignment == LEFT || alignment == RIGHT) {

