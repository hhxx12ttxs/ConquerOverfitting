package FastBE.invertedIndex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class compressedTA {
	ArrayList<HashSet<String>> compTA;
	ArrayList<double[]> addTAFactor;
	int[]	numAddedTAs;
	String CompTA;
	String TADim;
	String AddTAFacotr;
	String NumAddedTA;

	public compressedTA(JSONArray jsonInv, config configObj){
		int size = jsonInv.size();
		if( size <= 0 )	return;
		
		CompTA = configObj.getKeyword(config.COMPRESSED_TA);
		TADim = configObj.getKeyword(config.TA_DIM);
		AddTAFacotr = configObj.getKeyword(config.ADD_TA_PRICE_FACTOR);
		NumAddedTA = configObj.getKeyword(config.NUM_ADDED_TAS);
		
		compTA = new ArrayList<HashSet<String>>(size);
		addTAFactor = new ArrayList<double[]>(size);
		numAddedTAs = new int[size];
		
		for(int i=0;i<size;i++){
			JSONObject jsonObj = (JSONObject) jsonInv.get(i);
			// read TADim
			if(jsonObj.containsKey(TADim)){
				JSONArray TAItems = (JSONArray) jsonObj.get(TADim);
				HashSet<String> cTA = new HashSet<String>();
				for(int j = 0; j < TAItems.size();j++)
					cTA.add((String) TAItems.get(j));
				compTA.add(i, cTA);
			}
			// read AddTAFactor
			if(jsonObj.containsKey(AddTAFacotr)){
				JSONArray PriceItems = (JSONArray) jsonObj.get(AddTAFacotr);
				double[] priceList = new double[PriceItems.size()];
				for(int j = 0; j < PriceItems.size();j++)
					priceList[j] = ((Double) PriceItems.get(j)).doubleValue();
				addTAFactor.add(priceList);
			}
			// read num of already added attributes
			if(jsonObj.containsKey(NumAddedTA)){
				Long numItems = (Long) jsonObj.get(NumAddedTA);
				numAddedTAs[i] = numItems.intValue();
			}
		}
	}
	
	public compressedTA(compressedTA TA){
		release();
		if(TA.compTA != null){
			if(compTA == null )
				compTA = new ArrayList<HashSet<String>>();
			for(int i = 0; i < TA.compTA.size(); i++){
				HashSet<String> temp = new HashSet<String>();
				temp.addAll(TA.compTA.get(i));
				compTA.add(i, temp);
			}
		}
		
		if(TA.addTAFactor != null){
			if(addTAFactor == null)
				addTAFactor = new ArrayList<double[]>(TA.addTAFactor.size());
			for(int i = 0; i < TA.addTAFactor.size(); i++){
				double[] temp = TA.addTAFactor.get(i).clone();
				addTAFactor.add(i, temp);
			}
		}
		
		if(TA.numAddedTAs != null){
			if(numAddedTAs == null)
				numAddedTAs = new int[TA.numAddedTAs.length];
			for(int i = 0; i < TA.numAddedTAs.length; i++)
				numAddedTAs[i] = TA.numAddedTAs[i];
		}		
		CompTA = new String(TA.CompTA);
		TADim = new String(TA.TADim);
		AddTAFacotr = new String(TA.AddTAFacotr);
		NumAddedTA = new String(TA.NumAddedTA);
	}

	private void release(){
		if(compTA != null && (!compTA.isEmpty())){
			for(int i = 0; i < compTA.size(); i++){
				compTA.get(i).clear();
				compTA.remove(i);
			}
		}
		
		if(addTAFactor != null && (!addTAFactor.isEmpty())){
			for(int i = 0; i < addTAFactor.size(); i++){
				double[] temp = addTAFactor.get(i);
				temp = null;
				addTAFactor.remove(i);
			}
		}
		if(numAddedTAs != null)
			numAddedTAs = null;
	}
	
	public double getFactor(){
		double factor = 0.0;
		if(numAddedTAs!=null && (numAddedTAs.length != 0)){
			for(int i = 0; i < numAddedTAs.length; i++){
				int num = numAddedTAs[i];
				if(num > addTAFactor.get(i).length)
					num = addTAFactor.get(i).length ;
				if( num > 0){
					double[] temp = addTAFactor.get(i);
					factor += temp[num-1] - 1;
				}
			}
			factor += 1.0;
		}
		return factor;
	}

	public void addTA(Set<String> TASet){
		if(compTA != null && (!compTA.isEmpty())){
			for(int TACate=0;TACate<compTA.size();TACate++){
				int oldLen = compTA.get(TACate).size();
				compTA.get(TACate).removeAll(TASet);
				int addingSize = oldLen - compTA.get(TACate).size();
				if(addingSize > 0 )
					numAddedTAs[TACate] += addingSize;
			}
		}
	}

	public String toJSONString(){
		return toJSONObj().toJSONString();
	}

	/**
	 * @return JSONObject of the price instance.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray toJSONObj(){
//		JSONObject jsonCompTA = new JSONObject();
		JSONArray jarCompTA = new JSONArray();
		
		if(compTA != null && (!compTA.isEmpty())){
			for(int TACate=0;TACate<compTA.size();TACate++){
				// Serialize compTA
				JSONObject jsonTA = new JSONObject();
				JSONArray jsonCTA = new JSONArray();
				HashSet<String> comp = compTA.get(TACate);
				for( String cTA : comp)
					jsonCTA.add(cTA);
				jsonTA.put(TADim, jsonCTA);
				// Serialize addTAFactor
				JSONArray jsonCATF = new JSONArray();
				double[] ATF = addTAFactor.get(TACate);
				for( int i=0;i<ATF.length;i++)
					jsonCATF.add((Double) ATF[i]);
				jsonTA.put(AddTAFacotr, jsonCATF);
				// Serialize compTA
				JSONArray jsonCNUM = new JSONArray();
				jsonTA.put(NumAddedTA, new Integer(numAddedTAs[TACate])) ;
				
				jarCompTA.add(jsonTA);
			}
//			jsonCompTA.put(CompTA, jarCompTA);
		}

		return jarCompTA;
	}

}

