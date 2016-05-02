package FastBE.invertedIndex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class targetSubSpace extends price{
	margin	subMargin;
	DSP_Info	trafficValue;
	compressedTA compTA;
	ArrayList<double[]> addTAFactor;
	int[]	numAddedTAs;
	static String targetName; 
	static String priceName; 
	static String trafficName; 
	static String compressedTAName; 
	static String TAFactorName; 
	static String numAddTAName;
	static String marginName;
	
	public targetSubSpace(){
		super();
		trafficValue = new DSP_Info();
		subMargin = null;
//		numAddedTAs = 0;
	}
	public void addMargin(margin M){
		subMargin = new margin(M);
	}
	
	
	public targetSubSpace(JSONObject jsonInv, taxonomyTree taxonomy, config configObj){
		targetName = configObj.getKeyword(config.TARGET);
		priceName = configObj.getKeyword(config.PRICE_INFO); 
		trafficName = configObj.getKeyword(config.TRAFFIC);
		compressedTAName = configObj.getKeyword(config.COMPRESSED_TA); 
		TAFactorName = configObj.getKeyword(config.PRICE_TA_FACTOR); 
		numAddTAName =configObj.getKeyword(config.NUM_ADDED_TAS);
		marginName =configObj.getKeyword(config.MARGIN);

		invSub = new target(jsonInv,taxonomy, configObj);
		prices = new DSP_Info((JSONObject)jsonInv.get(priceName));
		trafficValue = new DSP_Info((JSONObject)jsonInv.get(trafficName));
		subMargin = new margin(jsonInv, configObj);
		if(jsonInv.containsKey(compressedTAName)){
			JSONArray jsonArray = (JSONArray) jsonInv.get(compressedTAName);
			compTA = new compressedTA(jsonArray, configObj);
		}
	}
	
	margin getMargin(){
		return subMargin;
	}
		
	private void setTargetSubSpace(target inv, DSP_Info pr, DSP_Info traf,compressedTA compTA_, margin Margin){
		invSub = new target(inv) ;
		prices = new DSP_Info(pr) ;
		trafficValue = new DSP_Info(traf);
		compTA = null;
		if(compTA_ != null)
			compTA = new compressedTA(compTA_);
		if(Margin != null)
			addMargin(Margin);

	}

	public targetSubSpace(targetSubSpace newTA){
		setTargetSubSpace((target)newTA.getTarget(), newTA.getAllPrices(), newTA.getTraffics(), newTA.getCompressedTA(), newTA.getMargin());
	}

	public compressedTA getCompressedTA(){
		return compTA;
	}
	
//	public double[] getTAFactor(){
//		return addTAFactor;
//	}
//
//	
//	public void setCompressedTA(HashSet<String> comTA){
//		if(comTA == null)  return;
//		compressedTA = new HashSet<String>();
//		Set<String> keySet = invSub.getKeySet();
//		for(String key : comTA)
//			if(!keySet.contains(key))
//				compressedTA.add( new String(key));
//	}
//	
//	public void setTAFactor(double[] taFactor){
//		if(taFactor == null)  return;
//		if(taFactor.length == 0) return;
//		
//		addTAFactor = new double[taFactor.length];
//		for(int i=0;i < addTAFactor.length; i++)
//			addTAFactor[i]=taFactor[i];
//	}
	
	public double getPrice(String priceType){
		double pr = super.getPrice(priceType);
		double prFactor = 1.0;
		if(compTA != null)
			prFactor = compTA.getFactor();
		
		if(pr >= 0.0)
			pr *= prFactor;
		
		return pr ;
	}

	public double getPrice(String priceType, double traf, String[] allowPriceTypes, String[] AbPrType, double[] AbPr, config configObj){
		AbPrType[0] = new String(priceType);
		AbPr[0] = super.getPrice(priceType);
		
		if(AbPr[0] != config.UNKNOWN_PRICE)		return AbPr[0];
		if(traf < 0.5)	return 	config.DEFAULT_ROI_RPICE;
		
		double cost = config.UNKNOWN_PRICE;
		
		for(String prType: allowPriceTypes){
			if(super.getPrice(prType) != config.UNKNOWN_PRICE){
				String matTrafType = (String) configObj.getConfig(configObj.getKeyword(config.PRICE_CONFIG_TAG)).get(prType);
				double curCost = super.getPrice(prType)*getTraffic(matTrafType);
				if(cost > curCost || cost == config.UNKNOWN_PRICE){
						cost = curCost;
						AbPrType[0] = new String(prType);
						AbPr[0] = super.getPrice(prType);
				}
			}
		}
		if(cost != config.UNKNOWN_PRICE)	return cost/traf;
		
		return config.DEFAULT_ROI_RPICE;
	}

	public DSP_Info getTraffics(){
		return trafficValue;
	}
	
	public double getTraffic(String trafficType){
		if(trafficValue.containsKey(trafficType))
			return  trafficValue.get(trafficType);
		return config.UNKNOWN_TRAFFIC;
	}
	
	public void setTraffic(String trafficType, Double value){
		trafficValue.put(trafficType, value);
	}

	public void setTraffic(DSP_Info tr){
		trafficValue.putAll(tr);
	}

	public void addTraffic(DSP_Info tr){
		for(String trafficType : tr.keySet()){
			double value = tr.get(trafficType).doubleValue();
			if(	trafficValue.containsKey(trafficType))
				value += trafficValue.get(trafficType).doubleValue();
			trafficValue.put(trafficType,new Double(value));
		}
	}

	public void minTraffic(DSP_Info tr){
		for(String trafficType : tr.keySet()){
			double value = tr.get(trafficType).doubleValue();
			if(	trafficValue.containsKey(trafficType))
			{
				if(value > trafficValue.get(trafficType).doubleValue())
					value = trafficValue.get(trafficType).doubleValue();
			}
					
			trafficValue.put(trafficType,new Double(value));
		}
	}

	
	public String toString() {
		StringBuilder results = new StringBuilder();
		results.append(invSub.toString()+"\t" + prices.toString()+"\t"+trafficValue.toString());
		return results.toString();
	}

	public String toJSONString(){
		return toJSONObj().toJSONString();
	}

	/**
	 * @return JSONObject of the price instance.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSONObj(){
		JSONObject jsonPrice = new JSONObject();
		if(invSub != null)
			jsonPrice.put(targetName, invSub.toJSONObj());
		if(prices != null)
			jsonPrice.put(priceName, prices.toJSONObj());
		if(trafficValue != null)
			jsonPrice.put(trafficName, trafficValue.toJSONObj());
		if(compTA != null)
			jsonPrice.put(compressedTAName, compTA.toJSONObj());
		if(subMargin != null)
			jsonPrice.put(marginName, subMargin.toJSONObj());

		return jsonPrice;
	}
	
	public void andTarget(target query){
		if(compTA != null)
			compTA.addTA(query.getKeySet());
		this.getTarget().andExp(query);
	}
	
	public targetSpace expandMargin2TS(){
		if (subMargin == null) return null;
		targetSpace taSpace = new targetSpace();
		for(String dim : subMargin.keySet()){
			for(String value : subMargin.get(dim).keySet()){
				target TA = new target();
				TA.addClause(dim, value);
				targetSubSpace TSS = new targetSubSpace(this);
				TSS.andTarget(TA);	// update target info
				if(!(TSS.getTarget().equals(this.getTarget()) || TSS.getTarget().isEmpty())){
					TSS.trafficValue = new DSP_Info(subMargin.get(dim).get(value));
					TSS.trafficValue.multiply(config.TRAFFIC_MODULATOR);
					taSpace.addTA(TSS);
				}
			}
		}
		return taSpace;
	}
}

