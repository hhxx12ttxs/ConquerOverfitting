public ItemStack(Item item, int count) {
super(item.name, item.sprite);
this.item = item;
for (int i = 0; i < count; i++) {
itemList.add(item);
}
}

public void remove(int amount) {
if (itemList.size() >= amount) {

