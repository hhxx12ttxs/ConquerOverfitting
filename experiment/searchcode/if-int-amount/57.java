package A;

public class Treasure extends Item {

private int amount = 0;

public Treasure(String imageFile, boolean visible, boolean canPickUp, int amount) {
public void setAmount(int amount) {
if(amount >-1) this.amount = amount;
}

}

