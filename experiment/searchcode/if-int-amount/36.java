public class PaymentAmount implements Comparable<PaymentAmount>{
private int amount;

public PaymentAmount() {
this(0);
}
public PaymentAmount(int amount) {
super();
this.amount = amount;
}

public PaymentAmount(float amount) {

