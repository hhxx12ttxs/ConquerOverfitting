image = new BufferedImage(Ref.JWIDTH, Ref.JHEIGHT, BufferedImage.TYPE_INT_RGB);
g = (Graphics2D) image.getGraphics();
g2.drawImage(image, 0, 0, Ref.JWIDTH, Ref.JHEIGHT, null);
g2.dispose();
}

public void exit() {
if(running) {

