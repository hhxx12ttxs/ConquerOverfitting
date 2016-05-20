package org.cremag.utils.stats;

import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class StatisticalResult implements Comparable<StatisticalResult> {
	
	String id;
	String name;
	Double pValue;
	Double foldChange;
	Double backgroundMean;
	Double queryMean;
	
	private static Logger logger = Logger.getLogger(StatisticalResult.class);
	
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("name", name);
		json.put("pvalue", StatisticalResult.roundToSignificantDigits(pValue,2));
		json.put("foldchange", StatisticalResult.roundToSignificantDigits(foldChange,2));
		json.put("backgroundmean", StatisticalResult.roundToSignificantDigits(backgroundMean,2));
		json.put("querymean", StatisticalResult.roundToSignificantDigits(queryMean,2));
		return json;
	}

	public JSONObject getJSON(int bonf) {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("name", name);
		json.put("pvalue", StatisticalResult.roundToSignificantDigits(pValue,2));
		json.put("bonferroni", StatisticalResult.bonferroniCorrected(pValue, bonf));
		json.put("foldchange", StatisticalResult.roundToSignificantDigits(foldChange,2));
		json.put("backgroundmean", StatisticalResult.roundToSignificantDigits(backgroundMean,2));
		json.put("querymean", StatisticalResult.roundToSignificantDigits(queryMean,2));
		return json;
	}
	
	@Override
	public int compareTo(StatisticalResult result) {
		double diff = this.pValue - result.pValue;
		if (diff > 0) return 1;
		else if(diff < 0) return -1;
		else {
			diff = result.queryMean - this.queryMean;
			if (diff > 0) return 1;
			else if(diff < 0) return -1;
			return 0;
		}
	}
	
	
	public static String roundToSignificantDigits(Double number, Integer digits) {
		try {
			BigDecimal bd = new BigDecimal(number);
			bd = bd.round(new MathContext(digits));
			double rounded = bd.doubleValue();
			return "" + rounded;
		} catch (Exception e) {
			logger.error("ERROR" + e.getMessage());
			return "ERROR";
		}
	}
	
	
	private static String bonferroniCorrected(Double pvalue, Integer n) {
		try {
		double val = pvalue * new Double(n);
		if(val > 1) val = 1;
		BigDecimal bd = new BigDecimal(val);
		bd = bd.round(new MathContext(2));
		double rounded = bd.doubleValue();
		return "" + rounded;
		} catch (Exception e) {
			return "ERROR";
		}
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPValue() {
		return pValue;
	}
	public void setPValue(Double pvalue) {
		this.pValue = pvalue;
	}
	public Double getFoldChange() {
		return foldChange;
	}
	public void setFoldChange(Double foldChange) {
		this.foldChange = foldChange;
	}
	public Double getBackgroundMean() {
		return backgroundMean;
	}
	public void setBackgroundMean(Double backgroundMean) {
		this.backgroundMean = backgroundMean;
	}
	public Double getQueryMean() {
		return queryMean;
	}
	public void setQueryMean(Double setMean) {
		this.queryMean = setMean;
	}


	
}

