package nl.vu.recoprov.baseclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import nl.vu.recoprov.signalaggregators.WeightedSumAggregator;
import nl.vu.recoprov.utils.TransitiveClosure;

//import org.coode.owlapi.rdf.model.RDFGraph;


// should be an RDF graph
// each dependency node should also be encoded in RDF

public class DependencyGraph extends TreeMap<String, DependencyNode>{

	private HashMap<Integer, ArrayList<LabelledEdge>> incidendencyMatrix;
	
	public HashMap<Integer, String> translationMap;
			
	private Set<String> attributes = new LinkedHashSet<String>();
			
	public DependencyGraph(){
		super();
		incidendencyMatrix = new LinkedHashMap<Integer,  ArrayList<LabelledEdge> >();
		translationMap = new HashMap<Integer, String>();
	}


	
	public DependencyNode put (String key, DependencyNode value){
		super.put(key, value);
		
		Set<String> newAttributes = value.getAttributes();
		attributes.addAll(newAttributes);

		return value;
	}
	
	public Set<String> getAttributes(){
		return attributes;
	}
	
//	public getSimilarities(){
//		
//	}

	
	public HashMap<Integer, ArrayList<LabelledEdge>>  getIncidencyMatrix(){
		return this.incidendencyMatrix;
	}
	
	public void setIncidencyMatrix( HashMap<Integer, ArrayList<LabelledEdge>>  input){
		this.incidendencyMatrix = input;
	}
	
	
	public void addEdge(DependencyNode starting_node, DependencyNode i, String s, double score){
		addEdge(starting_node.getLuceneDocNumber(),i.getLuceneDocNumber(),  s,  score);
	}
	
	public void addEdge(Integer starting_node, Integer i, String s, double score){
		
		//System.out.println(  starting_node +":"+ i +":" +s+ " " +score);
		
		LabelledEdge edge = new LabelledEdge(i, s, score);
		addEdge(starting_node, edge);
	}
	
	public void addEdge(Integer starting_node, LabelledEdge edge){

		ArrayList<LabelledEdge> edgearray = incidendencyMatrix.get(starting_node);
		if (edgearray == null){
			edgearray = new ArrayList<LabelledEdge>();
		}
		edgearray.add(edge);
		this.incidendencyMatrix.put(starting_node, edgearray);
	}
	

	public void removeEdge(DependencyNode starting_node, DependencyNode i){
		removeEdge(starting_node.getLuceneDocNumber(),i.getLuceneDocNumber());
	}
	
	public void removeEdge(DependencyNode d, DependencyNode d2, String label){
		
		Integer starting_node = d.getLuceneDocNumber();
		Integer i = d2.getLuceneDocNumber();
		
		ArrayList<LabelledEdge> edgearray = new ArrayList<LabelledEdge>();
		if (incidendencyMatrix.get(starting_node) == null){
			return;
		}
		else{
			edgearray.addAll(incidendencyMatrix.get(starting_node));
		}
		for(LabelledEdge e: incidendencyMatrix.get(starting_node)){
			if((e.getId() == i) && e.getLabel().equals(label))
				edgearray.remove(e);
		}
		this.incidendencyMatrix.put(starting_node, edgearray);
	}
	
	public void removeEdge(Integer starting_node, Integer i){
		ArrayList<LabelledEdge> edgearray = new ArrayList<LabelledEdge>();
		if (incidendencyMatrix.get(starting_node) == null){
			return;
		}
		else{
			edgearray.addAll(incidendencyMatrix.get(starting_node));
		}
		for(LabelledEdge e: incidendencyMatrix.get(starting_node)){
			if(e.getId() == i)
				edgearray.remove(e);
		}
		this.incidendencyMatrix.put(starting_node, edgearray);
	}
	
	public int getId(String s){
		return this.get(s).getLuceneDocNumber();	
	}
	
	public DependencyNode get(Integer i){
		return this.get(this.translationMap.get(i));
	}
	

	
	public void addTranslation(DependencyNode d, String s){
		addTranslation(d.getLuceneDocNumber(), s);
	}	
	public void addTranslation(Integer i, String s){	
		translationMap.put(i, s);
	}
	
	public ArrayList<LabelledEdge> getAllEdges(String starting_node){
		int i = get(starting_node).getLuceneDocNumber();
		return getAllEdges(i);
	}
	
	
	public ArrayList<LabelledEdge> getAllEdges(Integer starting_node){
		return this.incidendencyMatrix.get(starting_node);
	}
	
	public ArrayList<LabelledEdge> getAllAggregatedEdges(Integer starting_node){
		ArrayList<LabelledEdge> edges = new ArrayList<LabelledEdge>();
		
		if(!this.incidendencyMatrix.containsKey(starting_node))
			return edges;
		
		for(LabelledEdge e: this.incidendencyMatrix.get(starting_node)){
			if (e.getLabel().equals(WeightedSumAggregator.FINAL_SCORE))
				edges.add(e);
			else 
				continue;
		}
		//System.out.println("getAllEdges " +starting_node+"->"+ end_node + ": " +edges);
		return edges;
	}
	public ArrayList<LabelledEdge> getAllEdges(Integer starting_node, Integer end_node){
		ArrayList<LabelledEdge> edges = new ArrayList<LabelledEdge>();
		if(!this.incidendencyMatrix.containsKey(starting_node))
			return edges;
		for(LabelledEdge e: this.incidendencyMatrix.get(starting_node)){
			if (e.getId() == end_node)
				edges.add(e);
		}
		//System.out.println("getAllEdges " +starting_node+"->"+ end_node + ": " +edges);
		return edges;
	}
	
	public LabelledEdge getAggregatedEdge(Integer starting_node, Integer end_node){
		for (LabelledEdge edgeBetweenNodes: getAllEdges(starting_node, end_node)){
			if(!edgeBetweenNodes.getLabel().equals(WeightedSumAggregator.FINAL_SCORE))
				continue;
			else
				return edgeBetweenNodes;
		}	
		
		return null;
	}
	
	public LabelledEdge getInferredEdge(Integer starting_node, Integer end_node){
		for (LabelledEdge edgeBetweenNodes: getAllEdges(starting_node, end_node)){
			if(!edgeBetweenNodes.getLabel().equals(TransitiveClosure.INFERRED))
				continue;
			else
				return edgeBetweenNodes;
		}	
		
		return null;
	}
	
	
	public SimilarGraphResults similarToGraph(DependencyGraph g){
		if( this.size()!= g.size()){
			System.out.println("Different size");
			return new SimilarGraphResults();
		}
		if(!this.keySet().equals(g.keySet())){
			System.out.println("Different keyset: ");
			System.out.println(this.keySet());
			System.out.println(g.keySet());
			return new SimilarGraphResults();
		}
		else{
			//same nodes (at least labels)
			
			SimilarGraphResults results= new SimilarGraphResults();
			
			for(DependencyNode d: this.values()){
				Integer i = d.getLuceneDocNumber();
				ArrayList<LabelledEdge> list1 = this.incidendencyMatrix.get(i);
				ArrayList<LabelledEdge> list2 = g.incidendencyMatrix.get(i);
				
				if(list1 != null && !list1.isEmpty()) {
				
					//TODO: optimize by ordering
					for(LabelledEdge l1: list1){
						
						if((l1.getScore()<= 0.0 )||(!l1.getLabel().equals(WeightedSumAggregator.FINAL_SCORE)))
							continue;
						
						Boolean found = false;
						
						if(list2 != null && !list2.isEmpty()){
							for(LabelledEdge l2: list2){
								if((l2.getScore()<= 0.0 )||(!l2.getLabel().equals(WeightedSumAggregator.FINAL_SCORE)))
									continue;
								
								if(l1.getId() == l2.getId()){
									found = true;
									results.add(i,l1, "TP");
									//System.out.println("True positive: from "+ i + " -> "+ l1.getId()  + " exists in both");
									break;
								}
								
							}
						}
						if(!found){
							 results.add(i,l1, "FN");
							 System.out.println("False negative: from "+ i + " -> "+ l1.getId()  + " does not exist in predicted");
						 }	 
						
					}
					
				}
					
				if(list2 != null && !list2.isEmpty()) {
					
					for(LabelledEdge l2: list2){
						
						if((l2.getScore()<= 0.0 )||(!l2.getLabel().equals(WeightedSumAggregator.FINAL_SCORE)))
							continue;
						
						Boolean found = false;
						
						if(list1 != null && !list1.isEmpty()){
							for(LabelledEdge l1: list1){
								
								if((l1.getScore()<= 0.0 )||(!l1.getLabel().equals(WeightedSumAggregator.FINAL_SCORE)))
									continue;
								
								if(l1.getId() == l2.getId()){
									found = true;
									break;
								}
								
							}
						}
						 if(!found){
							 results.add(i,l2, "FP");
							 //System.out.println("False positive: from "+ i + " -> "+ l2.getId()  + " does not exist in default");
						 }
	
						
					}
					
				}
				
				//true negatives
				
				for(DependencyNode d2: this.values()){
					Integer j = d2.getLuceneDocNumber();
					if(i == j)
						continue;
					
					LabelledEdge thisedge = this.getAggregatedEdge(i, j);
					LabelledEdge gedge = g.getAggregatedEdge(i, j);
					

					if(thisedge == null && gedge== null){
						LabelledEdge newedge = new LabelledEdge(j, WeightedSumAggregator.FINAL_SCORE, 1.0 );
						results.add(i, newedge, "TN");
						//System.out.println("True negative: from "+ i + " -> "+ j  + " does not exist in none");

					}
				}
				
			}
			
			return results;
		}	
	}
	
	
	public String toString(){
		String temp = "";
		for (String name: this.keySet()){
			temp+= get(name) + "\n";
			//System.out.println(temp);
			if (getAllEdges(name) == null)
				continue;
			
			for (LabelledEdge e: getAllEdges(name)){
			
				if (e.getScore() == 0.0) 
					continue;
				temp+= e  + "-  ["+ this.get(e.getId()).getCompleteFilepath() + "] \n" ;
			}
		}
		return temp;
	}
	
	public String toCSVString(){
		String acc = "";
		for(String s: attributes){
			acc+= s + DependencyNode.DELIMITER;
		}
		acc+= "filename\n";
		
		for (DependencyNode d:this.values()){
			acc+= d.toCSVString(attributes);
		}
		
		acc+= "\n";

		
		acc+= "from"+ DependencyNode.DELIMITER + "to" + DependencyNode.DELIMITER  + "label" + DependencyNode.DELIMITER  +"score" +"\n";
		
		for (Integer i: this.incidendencyMatrix.keySet()){
			
			for (LabelledEdge e: this.incidendencyMatrix.get(i)){
				if(e.getScore()<=0.0)
					continue;
				acc+= i + DependencyNode.DELIMITER + e.getId() +  DependencyNode.DELIMITER + e.getLabel() +  DependencyNode.DELIMITER + e.getScore()+"\n";
			}

		}
		
		
		return acc;
	}
	
	public String toBooleanArray(){

		StringBuffer acc = new StringBuffer("( ");
		
		for (String s: this.keySet()){
			DependencyNode d = this.get(s);
			
			for (String s2: this.keySet()){
				DependencyNode d2 = this.get(s2);
				
				LabelledEdge l1 = this.getAggregatedEdge(d.getLuceneDocNumber(), d2.getLuceneDocNumber());

				if(l1 != null && (l1.getScore()> 0.0 )&&(l1.getLabel().equals(WeightedSumAggregator.FINAL_SCORE))){
					acc.append(" 1, ");
						
				}
				else{
					acc.append(" 0, ");
				}
				
				
				
			}
		}
		
		
		acc.append(")");
		
		return acc.toString();
	}
	
	public DependencyGraph copyGraph(){
		DependencyGraph clone = new DependencyGraph();
		for (String key:  this.keySet())
		{
			DependencyNode d = this.get(key);
			d = d.copyInGraph(clone);
			clone.put(key, d);
			
			Collection<LabelledEdge> edges = getAllEdges(d.getLuceneDocNumber());
			
			if (edges != null){
			
				for (LabelledEdge e: edges){
					clone.addEdge(d.getLuceneDocNumber(), e.getId(), e.getLabel(), e.getScore());
				}
			}
		}
		
		clone.translationMap = (HashMap<Integer, String>) translationMap.clone();
		clone.attributes = attributes;
		return clone;
		
	}
	
	public class LabelledEdge {
		private int id;
		private String label ;
		private double score = 0.0;
		
		public LabelledEdge(Integer i, String s, double score){
			setId(i);
			setLabel(s);
			this.score = score;
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
		
		public String toString(){
			return label + " -> " + id + " [ score: "+score+ "]";
		}
		

		
	}
	
	
	public class SimilarGraphResults{
		public LinkedHashMap<Integer, ArrayList<LabelledEdge>> FalsePositives;
		public LinkedHashMap<Integer, ArrayList<LabelledEdge>> FalseNegatives;
		public LinkedHashMap<Integer, ArrayList<LabelledEdge>> TruePositives;
		public LinkedHashMap<Integer, ArrayList<LabelledEdge>> TrueNegatives;
		
		private double FP = 0.0;
		private double TP = 0.0;
		private double FN = 0.0;
		private double TN = 0.0;
		
		public Boolean differentNodes;
		
		public SimilarGraphResults(){
			FalsePositives = new LinkedHashMap<Integer, ArrayList<LabelledEdge>>();
			FalseNegatives = new LinkedHashMap<Integer, ArrayList<LabelledEdge>>();
			TruePositives = new LinkedHashMap<Integer, ArrayList<LabelledEdge>>();
			TrueNegatives = new LinkedHashMap<Integer, ArrayList<LabelledEdge>>();
			differentNodes = true;
			
		}
		
		public void add(Integer i, LabelledEdge e, String type){
			
			Map<Integer, ArrayList<LabelledEdge>> map = TrueNegatives;
			
			if(type.equals("FP")){
				map = FalsePositives; 	
				FP++;
			}	
			if(type.equals("TP")){
				map = TruePositives;
				TP++;
			}	
			if(type.equals("TN")){
				map = TrueNegatives; 
				TN++;
			}	
			if(type.equals("FN")){
				map = FalseNegatives; 
				FN++;
			}
			
			ArrayList<LabelledEdge> array = map.get(i);
			if( array == null)
				array = new ArrayList<LabelledEdge> ();
			array.add(e);
			map.put(i, array);
			
		}

		@Override
		public String toString(){
			//return "Results- \nFalsePositives: " + FalsePositives + "\nFalseNegatives: "+FalseNegatives +"\nTruePositives: "+ TruePositives +"\nTrueNegatives: "+ TrueNegatives;
			String temp = "FalsePositives: " + FP + "\nFalseNegatives: "+FN +"\nTruePositives: "+ TP +"\nTrueNegatives: "+ TN;
			double precision = TP /(TP+FP);
			double recall = TP /(TP+FN);
			temp += "\nPrecision: " + precision;
			temp += "\nRecall: " + recall;
			temp += "\nF1-score: " + (2*precision*recall) /(precision+recall);  //2* precision* recall /(precision+recall)
			return temp;
		
		}
		


			
	}
	
}

