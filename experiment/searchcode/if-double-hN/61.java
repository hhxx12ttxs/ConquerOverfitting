package com.lqyandpy.crf;
import java.text.SimpleDateFormat;
import java.util.*;

public class Trainer {
	private double learningrate=0.15;
	
	public double getLearningRate(){
		return this.learningrate;
	}
	
	public void setLearningRate(double argD){
		this.learningrate=argD;
	}
	public static void print_time(String s)
	{
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		System.out.println(s + " at " + sf.format(new java.util.Date()));
	}
	
	public double getWeight(Node argF,Node argT){
		double tempR=Double.NaN;
		for(Link l:argT.getLinks()){
			if(l.From.equals(argF)){
				tempR=l.Weight;
				break;
			}
		}
		return new Double(tempR).equals(Double.NaN)?0:tempR;
	}

	
	public double getMSE(double[][] argA,double[][] argT){
		double tempR=0;
		for(int i=0;i<argA.length;i++){
			for(int j=0;j<argA[0].length;j++){
				tempR+=Math.pow(argA[i][j]-argT[i][j],2);
			}
		}
		
		tempR=Math.sqrt(tempR/(double)(argA.length*argA[0].length));
		
		return tempR;
	}
	
	public void Train(ANN argA,double[][] argD,double argSC,int max_try){
		ArrayList<OutputNode> tempON=argA.getOutputNodes();//ďż˝ďż˝ďż˝ďż˝ďż˝Úľďż˝
		ArrayList<InputNode>  tempIN=argA.getInputNodes();//ďż˝ďż˝ďż˝ďż˝ďż˝Úľďż˝
		ArrayList<HiddenNode> tempHN=argA.getHiddenNodes();//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Úľďż1¤7
		
		double[][] tempIM=new double[argD.length][tempIN.size()];//ďż˝ďż˝ďż˝ďż˝ÖľMatrix
		double[][] tempOM=new double[argD.length][tempON.size()];//ďż˝ďż˝ďż˝ÖľMatrix
		
		for(int i=0;i<argD.length;i++){
			for(int j=0;j<argD[0].length;j++){
				if(j<tempIN.size()){
					tempIM[i][j]=argD[i][j];
				}else{			
					tempOM[i][(int)argD[i][j]] = 1;
					//tempOM[i][j-tempIN.size()]=argD[i][j];
				}
			}
		}
		
		int epoch=0;
		Trainer.print_time("ann train epoch "+epoch);
		while(true){
			double[][] tempO=new double[argD.length][tempON.size()];
			for(int i=0;i<tempO.length;i++){
				//Trainer.print_time("clear nodes");
				argA.clearNodes();
				double[] tempISO=argA.getOutput(tempIM[i]);
				for(int j=0;j<tempO[0].length;j++){
					tempO[i][j]=tempISO[j];
					//System.out.println("["+tempO[i][j]+"  "+tempOM[i][j]+"]");
				}		
			}
			
			double tempMSE=this.getMSE(tempO, tempOM);
			
			System.out.println("ďż˝ďż˝"+epoch+"ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝îŁ1¤7"+tempMSE+"\r\n");
			
			
			if(!new Double(tempMSE).equals(Double.NaN)&&tempMSE<=argSC){
				System.out.println("ďż˝ďż˝"+epoch+"ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝");
				break;
			}
			
			for(int i=0;i<tempIM.length;i++){
				this.OnePointTrain(argA, tempIM[i], tempOM[i], argSC);
			}

			if(epoch++ >= max_try)
			{
				System.out.print("reached max try\n");
				break;
			}
		}
		
	}
	
	public void OnePointTrain(ANN argA,double[] argD,double[] argO,double argSC){//argD
			double[] tempO=argA.getOutput(argD);
			ArrayList<OutputNode> tempON=argA.getOutputNodes();
			int i=0;
			for(OutputNode on:tempON){//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Úľďż˝ďż˝delta
				on.setdelta(on.getActivateFunction().derivation(on.getCachedAccumulate())*(argO[i]-tempO[i]));
				i++;
			}
			
			ArrayList<HiddenNode> tempHN=argA.getHiddenNodes();
			for(HiddenNode hn:tempHN){//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Úľďż˝ďż˝delta
				ArrayList<Node> tempTN=argA.getNodesFrom(hn);//ďż˝ďż˝ďż˝ďż˝Úľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Óľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ď˛ďż˝Úľďż˝
				double tempE=0;
				for(Node tn:tempTN){
					ArrayList<Link> tempL=tn.getLinks();
					tempE+=tn.getdelta()*this.getWeight(hn, tn);//delta*weight
					
				}
				tempE*=hn.getActivateFunction().derivation(hn.getCachedAccumulate());
				hn.setdelta(tempE);
				}
		
			for(Node n:argA.Nodes){
				for(Link l:n.getLinks()){
					l.Weight+=this.learningrate*n.getdelta()*l.From.getActivateFunction().evaluate(l.From.getCachedAccumulate());
				}
			}
			
		}
	
	

}

