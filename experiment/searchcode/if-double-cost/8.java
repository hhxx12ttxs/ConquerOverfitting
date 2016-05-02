package FastBE.invertedIndex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class targetSpace {
	ArrayList<targetSubSpace>	taSpace;
	invertedIndex targetIndex;
	static String invspaceName;
	
	public targetSpace(){
		taSpace = new ArrayList<targetSubSpace>();
		targetIndex = new invertedIndex();
	}
	
	/**
	 * Load a targeting space from a json file.
	 * @param taFile: json file name of the TA space. 
	 * @param taxonomy: taxonomy tree will be used to propagate parents.
	 * @return the number of TA sub-spaces being loaded.
	 */
	public int loadTASpaceFromFile(String taFile, taxonomyTree taxonomy, config configObj){
		invspaceName = configObj.getKeyword(config.INVENTORY_SPACE_NAME);
		try{
			JSONParser parser = new JSONParser();
			JSONArray taSubSpaces = (JSONArray) ((JSONObject) parser.parse(new FileReader(taFile))).get(invspaceName);

			for(int i = 0; i < taSubSpaces.size(); i++){
				targetSubSpace newTASP = new targetSubSpace((JSONObject) taSubSpaces.get(i),taxonomy, configObj);
				addTA(newTASP);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		generateIndex();
		return(taSpace.size());
	}
	
	/**
	 * Add a new "TA" into the space.
	 * @param newTA : a "TA" to be inserted.
	 */
	public void addTA(targetSubSpace newTA){
		if(taSpace == null)
			taSpace = new ArrayList<targetSubSpace>();
			taSpace.add(newTA);
	}

	public void addTA(targetSpace newTASP){
		if(taSpace == null)
			taSpace = new ArrayList<targetSubSpace>();
		taSpace.addAll(newTASP.getTAList());
	}

	/**
	 * Get a "TA" from the space.
	 * @param index: index of "price" to be retrieved.
	 * @return "TA", null if can not find.
	 */
	public targetSubSpace getTA(int index){
		return(taSpace.get(index));
	}

	public ArrayList<targetSubSpace> getTAList(){
		return(taSpace);
	}
	
	public ArrayList<targetSubSpace> getTAList(int index[]){
		ArrayList<targetSubSpace> newList = new ArrayList<targetSubSpace>();
		for(int i=0;i<index.length;i++)
			newList.add(taSpace.get(index[i]));
		return(newList);
	}

	/**
	 * Clone a sub space with a given list of "TA" IDs.
	 * @param IDs: ID of "TA"s to be cloned.
	 * @return a new taSpace (can be empty).
	 */
	public targetSpace getTASpaceClone(int[] IDs){
		if(IDs.length <= 0) return null;
		targetSpace retSpace = new targetSpace();
		for(int i=0; i<IDs.length; i++){
			retSpace.addTA(new targetSubSpace(getTA(IDs[i])));
		}
		return(retSpace);
	}

	/**
	 * Clone a sub space with a given list of "TA" IDs.
	 * @param IDs: ID of "TA"s to be cloned.
	 * @return a new taSpace (can be empty).
	 */
	public targetSpace createUpdatedPriceSpaceClone(target query, String priceType, double campPrice){
		int[] IDs =  targetIndex.getMatchingBEids(query);
		if(IDs.length <= 0) return null;
		targetSpace retSpace =getTASpaceClone(IDs);
		if( retSpace != null){
			retSpace.andAll(query);
//			if(retSpace.filterWithPrice(priceType, campPrice))
			generateIndex();
			retSpace.uniqHighPrice();
		}
		return(retSpace);
	}
	
	public double[] calROI(String ROI_type, String[] priceTypes, double effTraffic, config configObj){
		// only CPC price type is implemented
		double[] rets = new double[taSpace.size()];
		for(int i =0; i < taSpace.size(); i++){
			targetSubSpace TSS = taSpace.get(i);
			if(TSS.getPrice(ROI_type) != config.UNKNOWN_PRICE)
				rets[i] = TSS.getPrice(ROI_type);
			else{
				rets[i] = TSS.getTraffic(configObj.pay2Traffic.get(ROI_type));
				if(rets[i] < effTraffic ) rets[i] = 0.0;
				else{
					double cost = 999999999999999.0;
					for(int j=0; j<priceTypes.length; j++){
						double pr = TSS.getPrice(priceTypes[j]);
						double tr = TSS.getTraffic(configObj.pay2Traffic.get(priceTypes[j]));
						if(pr != config.UNKNOWN_PRICE && tr!=config.UNKNOWN_TRAFFIC)
							if( cost > pr*tr)
								cost = pr*tr;
					}
					if( cost <= 0.0 || cost > 999999999990.0)
						rets[i] = 0;
					else rets[i] /= cost;
				}
			}
		}
		return rets;
	}
	public double[] calExpandableROI(String ROI_type, String[] priceTypes, double effTraffic, config configObj){
		// only CPC price type is implemented
		double[] rets = new double[taSpace.size()];
		for(int i =0; i < taSpace.size(); i++){
			targetSubSpace TSS = taSpace.get(i);
			if(TSS.getPrice(ROI_type) != config.UNKNOWN_PRICE)
				rets[i] = 0.0;
			else if(TSS.subMargin == null || TSS.subMargin.size()==0)
				rets[i] = 0.0;
			else{
				rets[i] = TSS.getTraffic(configObj.pay2Traffic.get(ROI_type));
				if(rets[i] < effTraffic ) rets[i] = 0.0;
				else{
					double cost = 999999999999999.0;
					for(int j=0; j<priceTypes.length; j++){
						double pr = TSS.getPrice(priceTypes[j]);
						double tr = TSS.getTraffic(configObj.pay2Traffic.get(priceTypes[j]));
						if(pr != config.UNKNOWN_PRICE && tr!=config.UNKNOWN_TRAFFIC)
							if( cost > pr*tr)
								cost = pr*tr;
					}
					if( cost <= 0.0 || cost > 999999999990.0)
						rets[i] = 0;
					else rets[i] /= cost;
				}
			}
		}
		return rets;
	}

	public int[] selectParticles(double prob[], int pNum){
		if(prob.length < 1 || pNum < 1)	return new int[0];
		
		int[] selected = new int[pNum];
		double[] cumProb = new double[prob.length];
		cumProb[0]=prob[0];
		for(int i=1; i < prob.length; i++)
			cumProb[i] = cumProb[i-1] + prob[i];
		
		double maxCum = cumProb[prob.length-1];
		Random rand = new Random(System.currentTimeMillis());
		double[] rnds = new double[pNum];
		
		for(int i = 0; i < pNum; i++)
			rnds[i] = rand.nextDouble()*maxCum;
		Arrays.sort(rnds);
		int start = 0;
		int end = cumProb.length;
		for(int i = 0; i < pNum; i++){
			 start = Arrays.binarySearch(cumProb, start, end, rnds[i]);
			 selected[i] = start; 
		}
		return selected;
	}
	
	public targetSpace createUpdatedPriceSpaceClone(target query){
		int[] IDs =  targetIndex.getMatchingBEids(query);
		if(IDs.length <= 0) return null;
		targetSpace retSpace =getTASpaceClone(IDs);
		if( retSpace != null){
			retSpace.andAll(query);
			retSpace.uniqHighPrice();
			generateIndex();
		}
		return(retSpace);
	}

	public boolean filterWithPrice(String priceType, double campPrice){
		boolean flag = false;
		ArrayList<targetSubSpace> newList = new ArrayList<targetSubSpace>();
		for(int i =0; i < taSpace.size(); i++){
			targetSubSpace TSS = taSpace.get(i);
			if(TSS.getPrice(priceType) < campPrice)
				newList.add(taSpace.get(i));
			else flag = true;
		}
		taSpace = newList;
		return flag;
	}
	
	public int[] getMatchingTAIDs(target TA){
		int[] matchedIds =  targetIndex.getMatchingBEids(TA);
		return matchedIds;
	}
	
	public int[] getMatchingTAIDs(target TA, String priceType){
		int[] matchedIds =  targetIndex.getMatchingBEids(TA);
		int sizeAfterPriceFilter = 0;
		for(int i =0; i < matchedIds.length; i++){
			if(taSpace.get(matchedIds[i]).getPrice(priceType) < 0)
				matchedIds[i] = -1;
			else sizeAfterPriceFilter++;
		}
		int[] results = new int[sizeAfterPriceFilter];
		for(int i =0, j=0; i < matchedIds.length; i++){
			if(matchedIds[i] != -1) 
				results[j++] = matchedIds[i];
		}
		return results;
	}
	public int[] getMatchingTAIDs(target TA, String priceType, double campPrice){
		int[] matchedIds =  targetIndex.getMatchingBEids(TA);
		int sizeAfterPriceFilter = 0;
		for(int i =0; i < matchedIds.length; i++){
			targetSubSpace TSS = taSpace.get(matchedIds[i]);
            double tssPrice = TSS.getPrice(priceType);
            if (tssPrice > campPrice || tssPrice < 0)
				matchedIds[i] = -1;
			else 		
				sizeAfterPriceFilter++;
		}
		int[] results = new int[sizeAfterPriceFilter];
		for(int i =0, j=0; i < matchedIds.length; i++){
			if(matchedIds[i] != -1) 
				results[j++] = matchedIds[i];
		}
		return results;
	}
	
	public int size(){
		return taSpace.size();
	}
	
	public void generateIndex(){
		for(int i=0; i < taSpace.size(); i++)
			targetIndex.addBE(i, taSpace.get(i).getTarget());
	}
	/**
	 * Perform a logic operation AND with query on targets of all "price"s in the space. 
	 * @param query
	 */
	public void andAll(target query){
		for(int i = 0; i < size(); i++){
			
			getTA(i).andTarget(query);
			getTA(i).hashCode();
		}
	}
	
	
	/**
	 * De-dup the space and keep the highest prices for each price types for duplicates. 
	 * @return the size after the de-dup.
	 */
	public int uniqHighPrice(){
		
		if( size() <=1 ) return size();
		
		Collections.sort(taSpace);
		ArrayList<Integer>	dupliInv = new ArrayList<Integer>();
		
		// search for potential duplicates with the same hash code (sorted). 
		// keep highest prices for duplicates.
		// record the IDs of the "price"s in "dupliInv" for deletion.
		for(int i = 1, value = getTA(0).hashCode(); i < size() ; i++){
			if(value == getTA(i).hashCode()){
				if(getTA(i-1).getTarget().isEqual(getTA(i).getTarget())){
					// current target equals to the previous one
					// save the highest prices to current "price"
					getTA(i).keepHighestPrice(getTA(i-1).prices);
					getTA(i).minTraffic(getTA(i-1).getTraffics());
					// record previous inv-cell index for deletion
					dupliInv.add(new Integer(i-1));
				}
			}
			value = getTA(i).hashCode();
		}
		
		// remove the prices whose IDs are in "dupliInv".
		// remove from large to small.
		if(!dupliInv.isEmpty()){
			Collections.sort(dupliInv, Collections.reverseOrder());
			for(int i = 0; i < dupliInv.size(); i++){
				taSpace.remove(dupliInv.get(i).intValue());
			}
		}
		
		return(taSpace.size());
	}

	
	public String toJSONString(){
		return toJSONObj().toJSONString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSONObj(){
		JSONObject jsonSpace = new JSONObject();
		JSONArray jsonList = new JSONArray();
		if(taSpace.size()>0)
			for(int i = 0 ; i < taSpace.size(); i++)
				jsonList.add(taSpace.get(i).toJSONObj());
		
		jsonSpace.put(invspaceName, jsonList);
		return jsonSpace;
	}
	
	public static void main(String[] argv){
//		String invSpaceFile = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\examplePriceFile.txt";
		String invSpaceFile = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\targetSpace.json";
//		String jsonFile = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\taxonomy.txt";
//		String jsonQuerySub = "{ \"ADNW\": [\"a3msn\"], \"AppCate\": [\"game\", \"life\"], \"region\": [\"CNBJ\",\"CNSH\"]	}";
		String jsonQuerySub = "{ \"target\"={\"ADNW\": [\"YOUMI\"], \"AppCate\": [\"life\"], \"GEO\": [\"province_CNBJ\",\"province_CNSH\"]	}}";
		String jsonFile = "C:\\\\Users\\datong.chen\\workspace\\FastBE\\configuration.txt";
//		String jsonFile = argv[0];
		
		config configObj = new config(jsonFile);
		System.out.println("read config from file: " + jsonFile);

		JSONObject invConfig = configObj.getConfig(configObj
				.getKeyword(config.INV_CONFIG_TAG));

		jsonFile = (String) invConfig.get(configObj.getKeyword(config.TAXONOMY_FILE_NAME));
		taxonomyTree taxonomy = new taxonomyTree(jsonFile,configObj);
		
		targetSpace test = new targetSpace();
		int x = test.loadTASpaceFromFile(invSpaceFile,taxonomy,configObj);
		System.out.println("TA space size = "+x);

		for(int i =0 ; i< test.size(); i++){
			System.out.println(test.getTA(i).toJSONString());
		}
		
		JSONParser parser = new JSONParser();
        
		try{
			JSONObject jsonBESub = (JSONObject) parser.parse(jsonQuerySub);
			
			target TAES = new target(jsonBESub,taxonomy,configObj);
			
			int[] matched = test.getMatchingTAIDs(TAES, "CPC");
			targetSpace x1 = test.getTASpaceClone(matched);
			x1.andAll(TAES);
			for(int i = 0 ; i < matched.length; i++){
				System.out.println(matched[i]);
				System.out.println(x1.getTA(i).toJSONString());
			}

		}  catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}

