//inventory coords info
private int invX, invY;
private final int slotW = 42, slotH = 42;
private final int slotXInterval = 3,slotYInterval = 3;
public void drawInventory(Graphics g) {
if(inventoryOpen) {
g.drawImage(inventImg, invX, invY, null);
int offsetsX[] = new int[slots];

