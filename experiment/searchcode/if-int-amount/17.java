private Material material;
private int amount;

public StockItem(Material material, int amount) {
this.material = material;
public boolean changeAmount(int delta) {
int newAmount = amount + delta;
if (newAmount >= 0) {
amount = newAmount;

