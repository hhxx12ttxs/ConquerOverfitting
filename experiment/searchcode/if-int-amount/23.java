public class Resource implements Token<Resource> {
private int amount;
public Resource(int startAmount) {
amount = startAmount;
}
public int getAmount() {
return amount;
}
public void addAmount(int change) {
amount += change;

