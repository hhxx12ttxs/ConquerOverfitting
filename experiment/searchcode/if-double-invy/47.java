int invY = 150;
int invX = sideBarStartX;
paintInventory(invX, invY, g2d);


if (draggingItem) {
if (dragging != null) {
for (int j = 0; j < 5; j++) {
g2d.drawRect(invX + 1, invY + 1, 62, 62);
if (i == 0)
g2d.drawString(j + 1 + &quot;&quot;, invX + 55, invY + 60);

