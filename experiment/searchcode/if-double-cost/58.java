package com.swinarta.sunflower.web.gwt.client.util;

import java.util.Map;

import com.smartgwt.client.data.Record;


public class PriceUtil {

	public static double getCostPricePerUnit(Record buyingRecord, Integer qty){
				
		Double costPrice = PriceUtil.getCostPricePerUnit(buyingRecord.getAttributeAsDouble("buyingPrice"), 
				buyingRecord.getAttributeAsDouble("disc1"), 
				buyingRecord.getAttributeAsDouble("disc2"), 
				buyingRecord.getAttributeAsDouble("disc3"), 
				buyingRecord.getAttributeAsDouble("disc4"),
				buyingRecord.getAttributeAsDouble("discPrice"), 
				buyingRecord.getAttributeAsBoolean("taxIncluded"), 
				qty);
		
		return costPrice;		
	}

	public static double getCostPrice(Map<String, Object> buyingMap){
		
		Double costPrice = PriceUtil.getCostPrice(((Number)buyingMap.get("buyingPrice")).doubleValue(), 
				((Number)buyingMap.get("disc1")).doubleValue(), 
				((Number)buyingMap.get("disc2")).doubleValue(), 
				((Number)buyingMap.get("disc3")).doubleValue(), 
				((Number)buyingMap.get("disc4")).doubleValue(),
				((Number)buyingMap.get("discPrice")).doubleValue(), 
				(Boolean)buyingMap.get("taxIncluded"));
		
		return costPrice;		
	}	

	public static double getCostPricePerUnit(Map<String, Object> buyingMap, Integer qty){
		
		Double costPrice = PriceUtil.getCostPricePerUnit(((Number)buyingMap.get("buyingPrice")).doubleValue(), 
				((Number)buyingMap.get("disc1")).doubleValue(), 
				((Number)buyingMap.get("disc2")).doubleValue(), 
				((Number)buyingMap.get("disc3")).doubleValue(), 
				((Number)buyingMap.get("disc4")).doubleValue(),
				((Number)buyingMap.get("discPrice")).doubleValue(), 
				(Boolean)buyingMap.get("taxIncluded"), 
				qty);
		
		return costPrice;		
	}	
	
	public static double getPercentagePrice(double price, double disc){
		return  price * ((double)disc/100);
	}

	public static double getCostPricePerUnit(double buyingPrice, double disc1, 
			double disc2, double disc3, double disc4, double discPrice, 
			boolean isTaxIncluded, int qty){
		
		return getCostPrice(buyingPrice, disc1, disc2, disc3, disc4, discPrice, isTaxIncluded)/((double)qty);
	}
	
	public static double getCostPrice(double buyingPrice, double disc1, 
			double disc2, double disc3, double disc4, double discPrice, 
			boolean isTaxIncluded){
		
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

	public static Double getMargin(Double costPricePerUnit, Double sellingPricePerUnit){
		Double margin = (sellingPricePerUnit - costPricePerUnit) / costPricePerUnit;
		return roundTwoDecimal(margin * 100);
	}
	
	public static Double getSellingPricePerUnit(Double costPricePerUnit, Double margin){
		Double sellingPricePerUnit = (costPricePerUnit * (margin/100)) + costPricePerUnit;
		return roundTwoDecimal(sellingPricePerUnit);
	}

	public static Double getPricePerUnit(Double price, int qty){
		Double sellingPricePerUnit = price/qty;
		return roundTwoDecimal(sellingPricePerUnit);
	}

	public static Double getPrice(Double pricePerUnit, int qty){
		Double sellingPrice = pricePerUnit*qty;
		return roundTwoDecimal(sellingPrice);
	}

	public static Double roundTwoDecimal(Double d){
		int ix = (int)(d * 100.0); // scale it
		return ((double)ix)/100.0;
	}
	
	public static Double calculatePromoPrice(Double sellingPrice, String promoType, Double promoValue){
		Double discPrice;
		if("VALUE".equalsIgnoreCase(promoType)){
			discPrice = promoValue;
		}else{
			discPrice = getPercentagePrice(sellingPrice, promoValue);
		}
		return sellingPrice - discPrice;
	}
}

