package payonthego;

public class KeyPadScreenAdapter extends KeyPad {
private int offsetX;
this.offsetX = offsetX;
this.offsetY = offsetY;
}

public String keyPressed(int x, int y) {
if (x-offsetX < 0) return null;

