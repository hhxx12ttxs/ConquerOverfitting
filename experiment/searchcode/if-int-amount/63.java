public void setAmount(int amount) {
this.amount = amount;
}

public int add(int amount) {
if (!getItem().isStackable())
return amount;
this.amount = this.amount + amount;
if(this.amount <= maxStack)
return 0;
int left = this.amount - maxStack;

