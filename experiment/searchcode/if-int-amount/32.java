private int amount;

public SupermarketProduct(String code, int amount) {
super();
this.setCode(code);
public void setAmount(int amount) {
if(amount>=0)
this.amount=amount;
else
this.amount=0;
}
}

