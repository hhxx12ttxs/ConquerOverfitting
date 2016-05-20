
public class AbsoluteDiscountPricing implements ISalePricing{

	private double discount;
	private double threshold;
	
	@Override
	public double getTotal(Sale sale) {
		double preis = sale.getPreDiscountTotal();
		if (preis < this.threshold){
			return preis;
		}else if((preis-this.discount) < this.threshold){
			return this.threshold;
		}
		
		return preis-discount;
	}
	
	public AbsoluteDiscountPricing(double discount, double threshold){
		this.discount = discount;
		this.threshold = threshold;
	}
	
	
	
	
}

