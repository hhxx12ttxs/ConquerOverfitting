package gameEngine.utility;

public class Credits {
private int amount;
public Credits(int amount) {
this.amount = amount;
}
public boolean remove(int amount) {
if (amount <= this.amount) {

