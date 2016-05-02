package FastBE.invertedIndex;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

public class Optimization {
	int	maxBuy;
	double	costMaxBuy;
	int profitBuy;
	double costMaxProfitBuy;
	double optCostWithGoal;
	optGraph graph;
	
	
	public Optimization(){
		graph = null;
	}
	
	public Optimization(optGraph curGraph){
		graph = curGraph;
	}
	
	public Optimization(String  jsonGraphName){
		graph = new optGraph(jsonGraphName);
	}
	
	public Optimization(JSONObject  jsonGraph){
		graph = new optGraph(jsonGraph);
	}
	
	// 1.0 only works for one campaign and single goal
	public void simpleExpoDist(double representRatio){
		if(BEDebug.Debug){
			System.out.println("---enter simpleExpoDist");
		}
		int size = graph.SE_price.length;
		if(BEDebug.Debug){
			System.out.println("graph.SE_price size : " + size);
		}
		
		//double[] buy = new double[size];
		int representGoal = (int) (graph.goal*representRatio+0.5);
		int distGoal = graph.goal;
		
		if(BEDebug.Debug){
			System.out.println("---graph.goal:" + graph.goal);
		}
		
		maxBuy = 0;
		costMaxBuy = 0.0;
		profitBuy = 0;
		costMaxProfitBuy = 0.0;
		// calculate the min of buy for all TAs
		for(int i =0; i< size; i++){
			maxBuy += graph.SE_traffic[i];
			costMaxBuy += graph.SE_price[i]*graph.SE_traffic[i];
			if(graph.SE_price[i] <= graph.AE_price){
				profitBuy+=graph.SE_traffic[i];
				costMaxProfitBuy += graph.SE_price[i]*graph.SE_traffic[i];
			}
		}
			
		if(BEDebug.Debug){
			System.out.println("maxBuy:" + maxBuy);
			System.out.println("profitBuy:" + profitBuy);
		}
		
		if(maxBuy < graph.goal){
			for(int i =0; i< size; i++)
				graph.SE_buy[i] = graph.SE_traffic[i];
			return;
		}
	
		// assign min-buy for all TAs
		for(int i =0; i< size; i++){
			if(graph.SE_price[i] <= graph.AE_price){
				graph.SE_buy[i]= (int) (representGoal*graph.SE_traffic[i]/profitBuy + 0.5);
				distGoal -= graph.SE_buy[i];
				if(BEDebug.Debug){
					System.out.println("---simpleExpoDist---graph.SE_buy[" + i + "] :" + graph.SE_buy[i] );
				}
			}
			
		}
		
		// distribute from low price to high price
	    Map<Double, Integer> map = new TreeMap<Double, Integer>();
		for (int i = 0; i < size; i++) 
		        map.put(new Double(graph.SE_price[i]), new Integer(i));
		
		int [] indices= new int[size];
		int id = 0;
		for(Integer idx :   map.values())
			indices[id++] =	idx.intValue();
		
		optCostWithGoal = 0.0;
		for (int i = 0; i < size; i++) {
			if(distGoal > 0){
				int idx = indices[i];
				if(distGoal > graph.SE_traffic[idx] - graph.SE_buy[idx]){
					graph.SE_buy[idx] = graph.SE_traffic[idx];
					distGoal -= (graph.SE_traffic[idx] - graph.SE_buy[idx]);
				}
				else{
					graph.SE_buy[idx] += distGoal;
					distGoal = 0;
				}
				optCostWithGoal += graph.SE_buy[idx]*graph.SE_price[idx];
			}
		}
	}
}

