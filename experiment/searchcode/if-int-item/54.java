public static ItemStack cloneItemStack(ItemStack itemStack, int stackSize) {
ItemStack clonedItemStack = itemStack.copy();
clonedItemStack.stackSize = stackSize;
public static boolean equalsIgnoreStackSize(ItemStack itemStack1, ItemStack itemStack2) {
if (itemStack1 != null &amp;&amp; itemStack2 != null) {
// Item ID
if (Item.getIdFromItem(itemStack1.getItem()) - Item.getIdFromItem(itemStack2.getItem()) == 0) {

