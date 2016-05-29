private double threshold;

@Override
public double getTotal(Sale sale) {
double preis = sale.getPreDiscountTotal();
if (preis < this.threshold){
return preis;
}else if((preis-this.discount) < this.threshold){

