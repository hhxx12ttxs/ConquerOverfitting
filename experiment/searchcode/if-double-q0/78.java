private Good[] basket;
private double minExp;

public Basket() {
basket = new Good[0];
minExp = 0;
basket = tmpBasket;

g.b0 = this;

minExp += g.price * g.q0;
}

public double[] getQuant(double budget) {
double[] q = new double[basket.length];

