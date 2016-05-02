package com.swinarta.sunflower.core.util;

public class PriceUtil {

	public static double getPercentagePrice(double price, double disc){
		return  price * ((double)disc/100);
	}

	public static double getCostPrice(Double buyingPrice, Double disc1, 
			Double disc2, Double disc3, Double disc4, Double discPrice, 
			Boolean isTaxIncluded){
		
		double cost = buyingPrice;
		cost -= getPercentagePrice(cost, disc1);
		cost -= getPercentagePrice(cost, disc2);
		cost -= getPercentagePrice(cost, disc3);
		cost -= getPercentagePrice(cost, disc4);
		cost -= discPrice;
		
		if(!isTaxIncluded){
			cost += getPercentagePrice(cost, 10);
		}
		
		return cost;
	}

	public static double getCostPricePerUnit(double buyingPrice, double disc1, 
			double disc2, double disc3, double disc4, double discPrice, 
			boolean isTaxIncluded, int qty){
		
		return getCostPrice(buyingPrice, disc1, disc2, disc3, disc4, discPrice, isTaxIncluded)/((double)qty);
	}
	
}

