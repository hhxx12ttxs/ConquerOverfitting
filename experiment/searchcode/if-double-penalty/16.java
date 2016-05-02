package FastBE.invertedIndex;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class budgetInfo {
	double Price;	 
	double FloorPrice;	
	double CampaignBudget;	
	int	goal;
	String BudgetControlMode;	
	JSONArray budgetScheduel;
	//BudgetList		 	 
	String BidMode; //	String	bid|fixed	 
	String PayMode; //	String	CPC|CPM|CP_download|CP_activation	 
	String TrafficMode;
	double Penalty;
	long remainTime; // in seconds
	public budgetInfo(){
		Price = 0.0;	 
		FloorPrice = 0.0;	
		CampaignBudget = 0.0;	
		goal = 0;
		BudgetControlMode = config.BUDGET_AUTO;	
		budgetScheduel = null;
		//BudgetList	 	 
		BidMode = config.FIXED_PRICE; //		 
		PayMode = config.PAYMODE_CPC; //		 
		Penalty = 0.0;
	}
	
	public budgetInfo(JSONObject jsonInv, config configObj){
		if(jsonInv.containsKey(configObj.getKeyword(config.PRICE)))
			Price = ((Number) jsonInv.get(configObj.getKeyword(config.PRICE))).doubleValue();
		if(jsonInv.containsKey(configObj.getKeyword(config.FLOOR_PRICE)))
			FloorPrice = ((Number) jsonInv.get(configObj.getKeyword(config.FLOOR_PRICE))).doubleValue();
		if(jsonInv.containsKey(configObj.getKeyword(config.CAMPAIGN_BUDGET)))
			CampaignBudget = ((Number) jsonInv.get(configObj.getKeyword(config.CAMPAIGN_BUDGET))).doubleValue();
		if(jsonInv.containsKey(configObj.getKeyword(config.PENALTY)))
			Penalty = ((Number) jsonInv.get(configObj.getKeyword(config.PENALTY))).doubleValue();
		if(jsonInv.containsKey(configObj.getKeyword(config.BUDGET_CONTROL_MODE)))
			BudgetControlMode = (String) jsonInv.get(configObj.getKeyword(config.BUDGET_CONTROL_MODE));
		if(jsonInv.containsKey(configObj.getKeyword(config.BID_MODE)))
			BidMode = (String) jsonInv.get(configObj.getKeyword(config.BID_MODE));
		if(jsonInv.containsKey(configObj.getKeyword(config.PAY_MODE))){
			PayMode = (String) jsonInv.get(configObj.getKeyword(config.PAY_MODE));
			TrafficMode = configObj.pay2Traffic.get(PayMode);
		}
		if(jsonInv.containsKey(configObj.getKeyword(config.BUDGET_SCHEDUEL))){
			budgetScheduel = (JSONArray) jsonInv.get(configObj.getKeyword(config.BUDGET_SCHEDUEL));
		}
		if(Price != 0)
			goal = (int) (CampaignBudget/Price+0.5);
	}
			
	public budgetInfo(budgetInfo newBudget){
		Price = newBudget.Price;	 
		FloorPrice = newBudget.FloorPrice;	
		CampaignBudget = newBudget.CampaignBudget;	 
		BudgetControlMode = newBudget.BudgetControlMode;	
		goal = newBudget.goal;
		if(newBudget.budgetScheduel != null)
			budgetScheduel = (JSONArray) newBudget.budgetScheduel.clone();
		else budgetScheduel = null;
		//BudgetList	 	 
		BidMode = newBudget.BidMode; //		 
		PayMode = newBudget.PayMode; //		 
		Penalty = newBudget.Penalty;
	}
	

	public double getPrice(String priceType){
		return Price;
	}

	public String getPayMode(){
		return PayMode;
	}

	public String getTrafficMode(){
		return TrafficMode;
	}

	public int getGoal(){
		return goal;
	}

	public double getCampaignBudget(){
		return CampaignBudget;
	}
	
	public String toJSONString(config configObj){
		return toJSONObj(configObj).toJSONString();
	}

	/**
	 * @return JSONObject of the price instance.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSONObj(config configObj){
		JSONObject jsonPrice = new JSONObject();
		jsonPrice.put(configObj.getKeyword(config.PRICE),Price);
		jsonPrice.put(configObj.getKeyword(config.FLOOR_PRICE),FloorPrice);
		jsonPrice.put(configObj.getKeyword(config.CAMPAIGN_BUDGET),CampaignBudget);
		jsonPrice.put(configObj.getKeyword(config.PENALTY),Penalty);
		jsonPrice.put(configObj.getKeyword(config.BUDGET_CONTROL_MODE),BudgetControlMode);
		if(budgetScheduel != null)
			jsonPrice.put(configObj.getKeyword(config.BUDGET_SCHEDUEL), budgetScheduel);
		jsonPrice.put(configObj.getKeyword(config.BID_MODE),BidMode);
		jsonPrice.put(configObj.getKeyword(config.PAY_MODE),PayMode);
		return jsonPrice;
	}
}

