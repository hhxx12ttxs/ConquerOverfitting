package FastBE.invertedIndex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
//import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class campaign {
	String	campaignID;
	String	parentID;
	String campaignName;
	int		maxSubCampaignIdx;
	long	startTime;	//in seconds
	long	endTime;	//in seconds
	target	taInfo;
	budgetInfo	budget;
	String	DSP_status;
	long	DSP_timestamp;
//	String	ADNW_status;
//	long	ADNW_timestamp;
	ArrayList<String>	subCampIDs;
	campaignReport	delivered;
	JSONObject	jsonCampaign;
	
	public campaign(){
		
	}
	
	public campaign(campaign TA){
		campaignID = new String(TA.campaignID);
		parentID = new String(TA.parentID);
		campaignName = new String(TA.campaignName);
		maxSubCampaignIdx = TA.maxSubCampaignIdx;
		startTime = TA.startTime;
		endTime = TA.endTime;
		taInfo = new target(TA.taInfo);
		budget = new budgetInfo(TA.budget);
		DSP_status = TA.DSP_status;
		DSP_timestamp = TA.DSP_timestamp;
//		ADNW_status = TA.ADNW_status;
//		ADNW_timestamp = TA.ADNW_timestamp;
		subCampIDs = null;
		delivered = null;
		jsonCampaign = (JSONObject) TA.jsonCampaign.clone(); 
	}
	
	public campaign(JSONObject	jsonObj, taxonomyTree taxonomy, config configObj){
		maxSubCampaignIdx = 0;
		jsonCampaign = new JSONObject(jsonObj);
		campaignID = (String) jsonCampaign.get(configObj.getKeyword(config.CAMPAIGN_ID));
		parentID = (String) jsonCampaign.get(configObj.getKeyword(config.PARENT_CAMPAIGN_ID));
		campaignName = (String) jsonCampaign.get(configObj.getKeyword(config.CAMPAIGN_NAME));

		try {
            String startTimeStr = ((String) jsonCampaign.get(configObj.getKeyword(config.START_TIME)));
            
            if(!startTimeStr.contains(" ")){
                startTimeStr += " 00:00:00";
            }
            Date D = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTimeStr);

            startTime = D.getTime()/1000;
            String endTimeStr = ((String) jsonCampaign.get(configObj.getKeyword(config.END_TIME)));
            if(!endTimeStr.contains(" ")){
                endTimeStr += " 23:59:59";
            }
            D = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTimeStr);

           
            endTime = D.getTime()/1000;
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		taInfo = new target(jsonCampaign, taxonomy, configObj);
		budget = new budgetInfo((JSONObject) jsonCampaign.get(configObj.getKeyword(config.BUDGET_INFO)), configObj);
		DSP_status = (String) jsonCampaign.get(configObj.getKeyword(config.DSP_STATUS));
//		DSP_timestamp = ((Long) jsonCampaign.get(config.DSP_TIMESTAMP)).longValue();
//		ADNW_status = (String) jsonCampaign.get(config.ADNW_STATUS);
//		ADNW_timestamp = ((Long) jsonCampaign.get(config.ADNW_TIMESTAMP)).longValue();
		subCampIDs = new ArrayList<String>();
		if(jsonCampaign.containsKey(configObj.getKeyword(config.CAMPAIGN_REPORT))){
			delivered = new campaignReport((JSONObject) jsonCampaign.get(configObj.getKeyword(config.CAMPAIGN_REPORT)),configObj);
		}
		else delivered = null;
//		JSONArray subList = (JSONArray)  jsonCampaign.get(config.SUB_CAMPAIGN_IDs);
//		for(int i=0;i<subList.size();i++)
//			subCampIDs.add((String) subList.get(i));
	}
	
	public target getTarget(){
		return taInfo;
	}

	public String getParentID(){
		return parentID;
	}

	public String getCampaignID(){
		return campaignID;
	}

	public budgetInfo getBudget(){
		
		return budget;
	}
	
	public campaignReport  getReport(){
		
		return delivered;
	}
	
	public String getNewSolutionID(){
		String solutionID = new Integer(maxSubCampaignIdx).toString();
		maxSubCampaignIdx++;
		
		return solutionID;
	}
	
	public campaign splitNewSubCampaign(target TA, double payPrice, String payMode, int buyAmount, long start, long end, config configObj){
		campaign solution = new campaign(this);
		solution.parentID = this.campaignID;
		solution.campaignID = getNewSolutionID();
		solution.campaignName = new String(campaignName);
		solution.budget.goal = buyAmount;
		solution.budget.PayMode = payMode;
		solution.budget.Price = payPrice;
		solution.budget.CampaignBudget = buyAmount * payPrice;
		solution.budget.remainTime = end - start;
		solution.taInfo = null;
		solution.taInfo = new target(TA);
		solution.startTime = start;
		solution.endTime = end;
		Date D = new Date();
		solution.DSP_timestamp =D.getTime()/1000; 
		solution.DSP_status = configObj.getKeyword(config.DSP_STATUS_NEW);
		return solution;
	}
	
	public long remainingTime(){
		if(delivered == null)
			return endTime - startTime;
		else return endTime - (delivered.endTime+1);
	}
	public String toJSONString(config configObj){
		return toJSONObj(configObj).toJSONString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSONObj(config configObj){
		if(jsonCampaign != null){
			jsonCampaign.remove(configObj.getKeyword(config.CAMPAIGN_ID));
			jsonCampaign.put(configObj.getKeyword(config.CAMPAIGN_ID), campaignID);
			jsonCampaign.remove(configObj.getKeyword(config.PARENT_CAMPAIGN_ID));
			jsonCampaign.put(configObj.getKeyword(config.PARENT_CAMPAIGN_ID),parentID);
			jsonCampaign.remove(configObj.getKeyword(config.CAMPAIGN_NAME));
			jsonCampaign.put(configObj.getKeyword(config.CAMPAIGN_NAME), campaignName);

			
			SimpleDateFormat timeOut = new SimpleDateFormat("yyyy-MM-dd");
			jsonCampaign.remove(configObj.getKeyword(config.START_TIME));
			Date startDate = new Date(startTime*1000);
			jsonCampaign.put(configObj.getKeyword(config.START_TIME),timeOut.format(startDate));

			jsonCampaign.remove(configObj.getKeyword(config.END_TIME));
			Date endDate = new Date(endTime*1000);
			jsonCampaign.put(configObj.getKeyword(config.END_TIME),timeOut.format(endDate));
			jsonCampaign.remove(configObj.getKeyword(config.TARGET));
			jsonCampaign.put(configObj.getKeyword(config.TARGET),taInfo.toJSONObj());
			jsonCampaign.remove(configObj.getKeyword(config.BUDGET_INFO));
			jsonCampaign.put(configObj.getKeyword(config.BUDGET_INFO), budget.toJSONObj(configObj));
			jsonCampaign.remove(config.DSP_STATUS);
			jsonCampaign.put(config.DSP_STATUS,DSP_status);
			jsonCampaign.remove(config.DSP_TIMESTAMP);
			jsonCampaign.put(config.DSP_TIMESTAMP,DSP_timestamp);
//			jsonCampaign.remove(config.ADNW_STATUS);
//			jsonCampaign.put(config.ADNW_STATUS,ADNW_status);
//			jsonCampaign.remove(config.ADNW_TIMESTAMP);
//			jsonCampaign.put(config.ADNW_TIMESTAMP,ADNW_timestamp);
			return jsonCampaign;
		}
		return null;
	}
	
	public String getROIType(){
		return budget.getPayMode();
	}
	public budgetInfo calCurBudget(){
		budgetInfo budInfo = new budgetInfo(budget);
		long lastDeliTime = startTime;
		int	deliveredMount = 0;
		double spent = 0.0;
		
		if(delivered != null){
			lastDeliTime = delivered.getLastDeliverTime();
			spent = delivered.getSpent();
			deliveredMount = delivered.getDelivredStats().get(budget.getTrafficMode()).intValue();
		}
		
		budInfo.goal = budget.getGoal();
		budInfo.CampaignBudget = budget.getCampaignBudget();
		if(budInfo.goal == 0 || budInfo.CampaignBudget == 0){
		    budInfo.goal = 0;
		    budInfo.CampaignBudget = 0;
		    budInfo.Price = 0;
		}
		if(budInfo.goal != 0){
		    budInfo.Price = budInfo.CampaignBudget/budInfo.goal;
		}
		budInfo.remainTime = endTime - lastDeliTime;
		return budInfo;
	}
	public static void main(String[] argv){
//		String jsonFile = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\taxonomy.txt";
		String jsonCamp = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\AECampaign.json";
		String jsonFile = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\configuration.txt";
//		String jsonFile = argv[0];
		config configObj = new config(jsonFile);
		JSONObject invConfig = configObj.getConfig(configObj
				.getKeyword(config.CAMPAIGN_CONFIG_TAG));

		jsonFile = (String) invConfig.get(configObj.getKeyword(config.TAXONOMY_FILE_NAME));
		
		taxonomyTree taxonomy = new taxonomyTree(jsonFile, configObj);
		JSONParser parser = new JSONParser();
        HashMap<String,campaign> AECampList = new HashMap<String,campaign>();
        
		try{
			JSONArray jsonCamps = (JSONArray) ((JSONObject) parser
					.parse(new FileReader((String) invConfig.get(configObj
							.getKeyword(config.AE_CAMPAIGN_FILE_NAME)))))
					.get(configObj.getKeyword(config.CAMPAIGNS));

			for(int i =0;i<jsonCamps.size();i++){
				campaign newCamp = new campaign((JSONObject) jsonCamps.get(i),taxonomy,configObj);
				AECampList.put(newCamp.getCampaignID(),newCamp);
				System.out.println(newCamp.toJSONString(configObj));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
	}

}

