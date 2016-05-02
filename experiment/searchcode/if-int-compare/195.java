import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



public class zTest {
	
	
	
	public static void readStrength(Map<String, Double> strengthMap,
			String filename, int uvThreshold) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		int userM = 0, userF = 0;
		String str;
		while ((str = br.readLine()) != null) {
			String[] splits = str.split("\\p{Blank}+");
			if(splits.length < 5) continue;
			int uvF = Integer.parseInt(splits[3]);
			int uvM = Integer.parseInt(splits[4]);
			userM += uvM;
			userF += uvF;
			if (uvF + uvM < uvThreshold || uvF == 0 || uvM == 0)
				continue;
			double rate = ((double) uvF) / uvM;
			strengthMap.put(splits[0], rate);
		}
		br.close();
		
		for(String pageid:strengthMap.keySet()) {
			
			strengthMap.put(pageid, strengthMap.get(pageid) / (((double) userF) / userM));
		}
		
		System.out.println(userF+"\t"+userM+"\t"+(((double) userF) / (userF+userM)));

	}
	
	static Map<String, Double> strengthAll = null;
	static Map<String, Double> strengthM = null;
	static Map<String, Double> strengthF = null;

	public static void main(String[] args) throws IOException {
		
		List<Double> femaleRates = new ArrayList<Double>();
		List<Double> maleRates = new ArrayList<Double>();
		List<Double> totalRates = new ArrayList<Double>();
		
		if (strengthAll == null) {
			strengthAll=new HashMap<String, Double>();
			strengthM=new HashMap<String, Double>();
			strengthF=new HashMap<String, Double>();
//			readStrength(strengthAll,"./total",100);
			for(String pageid:strengthAll.keySet()) {
				double rate = strengthAll.get(pageid);
				if(rate > 1) {femaleRates.add(rate);strengthF.put(pageid, rate);} 
				else {maleRates.add(1.0/rate);strengthM.put(pageid, 1.0/rate);} 
			}
			
			
			List<Map.Entry<String, Double>> fStreSorted = new LinkedList<Map.Entry<String, Double>>(strengthF.entrySet());
			
			Collections.sort(fStreSorted, new Comparator<Map.Entry<String, Double>>() {   
	            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {   
	            	double b1 = o1.getValue();
	            	double b2 = o2.getValue();
	            	if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;  
	            }   
	        });  
			
			
			List<Map.Entry<String, Double>> mStreSorted = new LinkedList<Map.Entry<String, Double>>(strengthM.entrySet());
			
			Collections.sort(mStreSorted, new Comparator<Map.Entry<String, Double>>() {   
	            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {   
	            	double b1 = o1.getValue();
	            	double b2 = o2.getValue();
	            	if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;  
	            }   
	        });  
			
			
			
			Collections.sort(femaleRates, new Comparator<Double>() {
				public int compare(Double b1, Double b2) {
					if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;
				}
			});

			Collections.sort(maleRates, new Comparator<Double>() {
				public int compare(Double b1, Double b2) {
					if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;
				}
			});

			Collections.sort(totalRates, new Comparator<Double>() {
				public int compare(Double b1, Double b2) {
					if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;
				}
			});
			
			
			for(int i = 0; i < 10; i++) {
				System.out.println("M: "+mStreSorted.get(i).getKey()+"\t"+mStreSorted.get(i).getValue());
				System.out.println("F: "+fStreSorted.get(i).getKey()+"\t"+fStreSorted.get(i).getValue());
			}
			
			
		}
	}

}

