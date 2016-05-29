if (groundItem != null) {
if (groundItem.isOnScreen()) {
final int invy = Inventory.getCount(ids);
if (Clicking.click(&quot;Take &quot; + groundItem.getDefinition().getName(), groundItem)) {
final int invy = Inventory.getCount(groundItem.getID());
if (Clicking.click(&quot;Take &quot; + groundItem.getDefinition().getName(), groundItem)) {

