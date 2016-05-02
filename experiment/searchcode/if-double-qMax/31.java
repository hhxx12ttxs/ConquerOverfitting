package tools.DistanceMatrix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import tools.DistanceMatrix.Transform.Transform;
import tools.fasta.fastaUtils;


public class DistanceMatrix {

	private HashMap<String, HashMap<String, DistanceObject>> dist =new HashMap<String, HashMap<String,DistanceObject>>();
	private ArrayList<String> allNames= new ArrayList<String>();
	private ArrayList<String> qnames= new ArrayList<String>();
	private ArrayList<String> tnames= new ArrayList<String>();
	private DistanceObject max= new DistanceObject("","",Double.MIN_VALUE);
	private DistanceObject min= new DistanceObject("","",Double.MAX_VALUE);
	
	public DistanceMatrix(ArrayList<DistanceObject> list){
		int elementCount=0;
		for (DistanceObject d : list) {
			if(d.getDistance()>max.getDistance()){
				max=d;
			}
			if(d.getDistance()<min.getDistance()){
				min=d;
			}
			if(!allNames.contains(d.getQname())){
				allNames.add(d.getQname());
			}
			if(!allNames.contains(d.getTname())){
				allNames.add(d.getTname());
			}
			if(!qnames.contains(d.getQname())){
				qnames.add(d.getQname());
			}
			if(!tnames.contains(d.getTname())){
				tnames.add(d.getTname());
			}
			if(!dist.containsKey(d.getQname())){
				dist.put(d.getQname(), new HashMap<String, DistanceObject>());
			}
			if(dist.get(d.getQname()).containsKey(d.getTname())){
				//Treat doublets here
//				System.err.println("Found doublet for qname:"+d.getQname()+" and tname: "+d.getTname());
				//Keep largest
				if(d.getDistance()>dist.get(d.getQname()).get(d.getTname()).getDistance()){
					dist.get(d.getQname()).put(d.getTname(), d);
				}
			}else{
				dist.get(d.getQname()).put(d.getTname(), d);
				elementCount++;
			}
		}
		if(elementCount!=tnames.size()*qnames.size()){
			//Treat missing objects or doublets here
			System.err.println("Number of qnames ("+qnames.size()+") * number of tnames ("+tnames.size()+")="+tnames.size()*qnames.size());
			System.err.println("and the number of distance objects was: "+elementCount);
			ArrayList<String> missing= new ArrayList<String>();
			if (tnames.size()>qnames.size()){
				for (String tname : tnames) {
					if(!qnames.contains(tname)){
						missing.add(tname);
					}
				}
			}else{
				for (String qname : qnames) {
					if(!tnames.contains(qname)){
						missing.add(qname);
					}
				}
			}
			System.err.println("Missing identifiers:");
			for (String miss :missing){
				System.err.println(miss);
			}
		}
//		for (String s : allNames) {
//			System.err.println(s);
//		}
	}
	
	public void transformAll(Transform f){
		HashMap<String, DistanceObject> hmt;
		for (String qname : qnames) {
			hmt=dist.get(qname);
			for (String tname : hmt.keySet()) {
				System.out.println(tname);
			}
		}
	}
	
	public void transformEucDistSym()throws Exception{
		HashMap<String, HashMap<String, DistanceObject>> tmp= new HashMap<String, HashMap<String,DistanceObject>>();
		String qname,tname;
		double d;
		for(int i=0;i<allNames.size();i++){
			qname=allNames.get(i);
			tmp.put(qname, new HashMap<String, DistanceObject>());
			tmp.get(qname).put(qname, new DistanceObject(qname,qname,0));
		}
		for(int i=0;i<allNames.size();i++){
			qname=allNames.get(i);
			for(int j=0;j<i;j++){
				tname=allNames.get(j);
				d=eucDistSym(dist.get(qname), dist.get(tname), dist.get(qname).get(qname).getDistance(), dist.get(tname).get(tname).getDistance());
				tmp.get(qname).put(tname, new DistanceObject(qname,tname,d));
				tmp.get(tname).put(qname, new DistanceObject(tname,qname,d));
			}
		}
		dist=tmp;
	}
	
	private double eucDistSym(HashMap<String, DistanceObject> q,HashMap<String, DistanceObject> t,double qnorm, double tnorm)throws Exception{
		double d=0;
		if(q.size()==t.size()){
			//calculate
			for (String key : q.keySet()) {
				d+=Math.pow((q.get(key).getDistance())/qnorm-(t.get(key).getDistance())/tnorm, 2);
			}
		}else{
			throw new Exception("Query and target of different size");
		}
		return Math.sqrt(d);
	}
	
	public void transformAndSymetricate(){
		String qname,tname;
		double d,qmax,tmax;
		DistanceObject qd,td;
		HashMap<String, DistanceObject> qhmt;
		for(int i=0;i<allNames.size();i++){
			qname=allNames.get(i);
			qhmt=dist.get(qname);
			qmax=qhmt.get(qname).getDistance();
//			System.err.println(i+". "+qname);
			for(int j=0;j<i;j++){
				tname=allNames.get(j);
				tmax=dist.get(tname).get(tname).getDistance();
				qd=qhmt.get(tname);
				td=dist.get(tname).get(qname);
				d=1-2*(qmax*(qd.getDistance())+tmax*(td.getDistance()))/Math.pow(qmax+tmax, 2);
				qd.setDistance(d);
				td.setDistance(d);
			}
		}
		for(int i=0;i<allNames.size();i++){
			qname=allNames.get(i);
			dist.get(qname).get(qname).setDistance(0);
		}
	}
	
	public void toTriplets(String outfile,double cutoff,boolean biggerBetter) throws Exception{
		BufferedWriter triplets= new BufferedWriter(new FileWriter(outfile));
		String sep=" ";
		String out,name1,name2,name3;
		double dist12,dist13,dist23,diff;
		for(int i =0;i<allNames.size();i++){
			name1=allNames.get(i);
			for (int j=0;j<i-1;j++){
				name2=allNames.get(j);
				dist12= this.dist.get(name1).get(name2).getDistance();
				for (int k=0; k<j-1; k++){
					name3=allNames.get(k);
					dist13= this.dist.get(name1).get(name3).getDistance();
					dist23= this.dist.get(name2).get(name3).getDistance();
					out="";
					
					if((dist12>dist13)==biggerBetter){
						if((dist12>dist23)==biggerBetter){
							if((dist13>dist23)==biggerBetter){
								//dist12, dist13, dist23
								diff=Math.abs(dist12-dist13);
								out=name1+sep+name2+sep+name3;
							}else{
								//dist12, dist23, dist13
								diff=Math.abs(dist12-dist23);
								out=name1+sep+name2+sep+name3;
							}
						}else{
							//dist23, dist12, dist13
							diff=Math.abs(dist23-dist12);
							out=name2+sep+name3+sep+name1;
						}
					}else{
						if((dist13>dist23)==biggerBetter){
							//dist13
							if((dist23>dist12)==biggerBetter){
								//dist13, dist23, dist12
								diff=Math.abs(dist13-dist23);
								out=name1+sep+name3+sep+name2;
							}else{
								//dist13, dist12, dist23
								diff=Math.abs(dist13-dist23);
								out=name1+sep+name3+sep+name2;
							}
						}else{
							//dist23, dist13, dist12
							diff=Math.abs(dist23-dist13);
							out=name2+sep+name3+sep+name1;
						}
					}
					if((diff>cutoff)==biggerBetter){
						triplets.write(out+"\n");
					}
					
				}
			}
		}
		triplets.close();
	}
	
	public void toIsodata() throws Exception{
		toIsodata("");
	}
	
	public void toIsodata(String prefix) throws Exception{
		BufferedWriter names= new BufferedWriter(new FileWriter(prefix+"hhsMatrix_names.csv"));
		BufferedWriter matrix= new BufferedWriter(new FileWriter(prefix+"hhsMatrix_matrix.csv"));
		DecimalFormat df =new DecimalFormat("#.####");
		String sep="\t";
		for(int i=0;i<allNames.size();i++){
			String name= allNames.get(i);
			names.write((i)+"\t"+name+"\n");
			matrix.write(df.format(dist.get(name).get(allNames.get(0)).getDistance()));
			for(int j=1;j<allNames.size();j++){
				matrix.write(sep+df.format(dist.get(name).get(allNames.get(j)).getDistance()));
			}
			matrix.write("\n");
		}
		names.close();
		matrix.close();
	}
	
	public void toPhylip() throws Exception{
		int itemsPerRow=9;
		BufferedWriter stat= new BufferedWriter(new FileWriter("stat.csv"));
		DecimalFormat df =new DecimalFormat("#.####");
		String qname,tname,phy;
		//check for name duplication
		if(!fastaUtils.phylipWarning(allNames)){
			//do the phylip dance
			System.out.println("   "+allNames.size());
			for(int i=0;i<allNames.size();i++){
				phy=(qname=allNames.get(i));
				if(phy.length()>10){
					phy=phy.substring(0, 10);
				}else{
					for(;phy.length()<10;){
						phy+=" ";
					}
				}
				System.out.print(phy);
				for(int j=0;j<allNames.size();j++){
					tname=allNames.get(j);
					stat.write(dist.get(qname).get(tname).getDistance()+"\n");
					System.out.print(" "+df.format(dist.get(qname).get(tname).getDistance()));
					if(j>2 &&(j+2)%itemsPerRow==0&& j!=allNames.size()-1){
						System.out.println();
					}
				}
				System.out.println();
			}
		}
		stat.close();
	}
}

