public Inventory() {
for(int i = 0; i < 10; i++) {
addItem(ItemCreator.createItem());
}
}

public void addItem(Item item, int x, int y) {
if(items[x][y] == null) {
items[x][y] = item;

