for (int i = 0; i < items.length; i++) {
Item item = items[i];
handleItem(item);
}
}

void handleItem(Item item) {
if (isLegendary(item)) {
return;
}

if (item.name.equals(&quot;Craftspeople Potion&quot;)) {

