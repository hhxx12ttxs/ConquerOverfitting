private double amount;
private final int number;
private final static double INVERS = -1.0;
private final static double ZERO = 0.0;
public Amount() {
this(ZERO);
}

public Amount clone(){
return new Amount(this.getAmount());
}

public int getNumber() {

