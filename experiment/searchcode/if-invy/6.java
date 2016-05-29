private int selected;
private int invX;
private int invY;

public Inventory(int x, int y) {
this.invX = x;
this.invY = y;
for (int i = 0; i < 16; i++) {
g.drawImage(this.inventorySlot, this.invX + (i % 4) * 32, this.invY + (i / 4) * 32);

