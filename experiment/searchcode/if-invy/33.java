Image ii = super.getImage();
BufferedImage buff = toBufferedImage(ii);
if (colors != null) {
if (--cooldown < 0) {
for (int y = 0; y < buff.getHeight(); y++) {
Color c = new Color(buff.getRGB(x, y));
if (c.getRed() == 255 &amp;&amp; c.getGreen() == 0 &amp;&amp; c.getBlue() == 0) {

