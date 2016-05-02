package com.lqyandpy.RBM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.lqyandpy.DBN.DBNTrain;
import com.lqyandpy.RBM.Data.Case;

public class CDTrain {
	
	private Data dataSet;
	private RBM rbm;
	private double rate=0.001;
	private Random r=new Random();
	private int max_try = 50;
	public CDTrain(Data argD,RBM argR,int max_try){
		this.dataSet=argD;
		this.rbm=argR;
		this.max_try = max_try;
		for(RBMNode n:this.rbm.vNodes){//ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝Ýłďż˝Ęźďż˝ďż˝bias
			//if(n.getType()==0){
//				double tempP=this.dataSet.getVariableProbability(n.getID());
//				if(tempP==0){
//					n.setBias(-4);
//				}else if(tempP==1){
//					n.setBias(4);
//				}else{n.setBias(Math.log(tempP/(1-tempP)));}
				n.setBias(r.nextDouble());
			//}
		}
	}
	
	public void setLearningRate(double argD){
		this.rate=argD;
	}

	public RBM getRBM(){
		return this.rbm;
	}
	
	public double Errorta(){
		System.out.println("--------------------------------ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż1¤7----------------------------------");
		double tempE=0;
		
		for(Case c:this.dataSet.getDataSet()){
			this.rbm.clearNodeState(0);
			this.rbm.clearNodeState(1);
			
			double[] v=c.getTheCase();
			this.rbm.setNodeState(v, 0);//ÎŞďż˝Éźďż˝Úľă¸łÖľclamp
			double[] h0=this.rbm.getNodeState(1);//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Úľďż˝ČĄďż˝ďż1¤7
			this.rbm.clearNodeState(0);//ďż˝ďż˝ŐżÉźďż˝ďż˝
			double[] v1=this.rbm.getNodeState(0);//ďż˝ÔżÉźďż˝ďż˝ČĄďż˝ďż1¤7
			this.rbm.clearNodeState(1);//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ăĄ1¤7
			
			double tempForm=0;
			for(int i=0;i<v.length;i++){
				tempForm+=Math.pow((double)v[i]-v1[i], 2);
			}

			tempE+=Math.sqrt(tempForm);
		}
		
		return tempE;
	}
	
	public void CD(double argSC,WeightDecay argWD){//ďż˝ďż˝CDďż˝ďż˝REÍŁďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝L2ďż˝ďż˝L1
		int epoch=0;
		while(true){
			for(int i=0;i<this.dataSet.getDataCount();i++){//ďż˝ďż˝ĘźŇťďż˝ďż˝Ńľďż˝ďż˝
				this.rbm.clearNodeState(0);
				this.rbm.clearNodeState(1);//ďż˝ďż˝É¨ďż˝Éžďż˝ďż˝ďż˝ďż˝ďż˝Ä˛ďż˝ďż˝ďż˝×´Ě1¤7

				double[] v0=this.dataSet.getData(i);
				this.rbm.setNodeState(v0, 0);//ďż˝ďż˝ďż˝ĂżÉźďż˝ďż˝ďż˝×´ĚŹÎŞV0
				double[] tempHCV0=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ä¸ďż˝ďż˝ďż1¤7
//				for(RBMNode n:this.rbm.Nodes){
//					if(n.getType()==1){
//						tempHCV0[n.getID()]=n.getProbability();
//					}
//				}
				for(RBMNode n:this.rbm.hNodes)
						tempHCV0[n.getID()]=n.getProbability();
				double[] h0=this.rbm.getNodeState(1);//ČĄďż˝ďż˝H0;
				this.rbm.clearNodeState(0);//ďż˝ďż˝ŐżÉźďż˝ďż˝
				double[] v1=this.rbm.getNodeState(0);//ČĄďż˝ďż˝V1
				this.rbm.clearNodeState(1);//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż1¤7
				double[] tempHCV1=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝Úľďż˝Ä¸ďż˝ďż˝ďż˝
				for(RBMNode n:this.rbm.hNodes)
						tempHCV1[n.getID()]=n.getProbability();
				
				for(int v=0;v<this.rbm.vn;v++){
					for(int h=0;h<this.rbm.hn;h++){
						double tempDW=((double)v0[v]*tempHCV0[h]-(double)v1[v]*tempHCV1[h])/this.rbm.getNode(v, 0).getVariance();
						if(argWD!=null){
							tempDW+=argWD.getWeightDecay(this.rbm.W, v, h);
						}
						this.rbm.W[v][h]+=this.rate*tempDW;
					}
				}

				//this.rbm.UpdateRBM();//ďż˝ďż˝ďż˝ďż˝RBM,ďż˝ďż˝ďż˝ďż˝Öťďż˝ďż˝Ęžďż˝Ëťďż˝ďż˝ďż˝Č¨Öľďż˝ďż˝ďż˝Úľďż˝ďż˝ĆŤďż˝ďż˝ÖľŇŞďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż1¤7
//				for(RBMNode n:this.rbm.Nodes){//ďż˝ďż˝ďż˝Â˝Úľďż˝bias
//					if(n.getType()==0){//ďż˝ďż˝ďż˝ÂżÉźďż˝ďż˝ďż˝bias
//						n.setBias(n.getBias()+this.rate*((v0[n.getID()]-v1[n.getID()])/n.getVariance()));
//					}else{//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝ďż˝bias
//						n.setBias(n.getBias()+this.rate*(tempHCV0[n.getID()]-tempHCV1[n.getID()]));
//					}
//				}
				for(RBMNode n:this.rbm.vNodes)
					n.setBias(n.getBias()+this.rate*((v0[n.getID()]-v1[n.getID()])/n.getVariance()));
				for(RBMNode n:this.rbm.hNodes)
					n.setBias(n.getBias()+this.rate*(tempHCV0[n.getID()]-tempHCV1[n.getID()]));
			}
			
			epoch++;
//			double tempE=this.Errorta();
//			System.out.println("ďż˝ďż˝"+epoch+" ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝Řšďż˝ďż˝ďż˝ďż1¤7"+tempE);
//			if(tempE<=argSC){
//				System.out.println("ďż˝ďż˝ďż˝ďż˝"+epoch+" ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝RBM ďż˝ďż˝ďż˝ďż˝");
//				break;				
//			}
			
		}
		
		
		
		
	}
	
	public void PersistentCD(double argSC,WeightDecay argWD){//tielmanďż˝ďż˝PCDŃľďż˝ďż˝ďż˝ăˇ¨ďż˝ďż˝ďż˝Ćźďż˝Ęšďż˝ďż˝L2
		/* ďż˝ďż˝ďż˝ďż˝ďż˝ÝśČľÄľÚśďż˝ďż˝ďż˝Ęąďż˝ďż˝Ęšďż˝Ăľďż˝Ç°Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ç°Ňťďż˝ďż˝×´ĚŹďż˝ďż1¤7
		 * 
		 * */
		int epoch=0;
		while(true){
			double[] v1=this.sample();
			for(int i=0;i<this.dataSet.getDataCount();i++){//ďż˝ďż˝ĘźŇťďż˝ďż˝Ńľďż˝ďż˝
				this.rbm.clearNodeState(0);
				this.rbm.clearNodeState(1);//ďż˝ďż˝É¨ďż˝Éžďż˝ďż˝ďż˝ďż˝ďż˝Ä˛ďż˝ďż˝ďż˝×´Ě1¤7
				
				double[] v0=this.dataSet.getData(i);
				this.rbm.setNodeState(v0, 0);//ďż˝ďż˝ďż˝ĂżÉźďż˝ďż˝ďż˝×´ĚŹÎŞV0
				double[] tempHCV0=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ä¸ďż˝ďż˝ďż1¤7
//				for(RBMNode n:this.rbm.Nodes){
//					if(n.getType()==1){
//						tempHCV0[n.getID()]=n.getProbability();
//					}
//				}
				for(RBMNode n:this.rbm.hNodes)
					tempHCV0[n.getID()]=n.getProbability();

				this.rbm.setNodeState(v1, 0);				
				double[] tempHCV1=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝Úľďż˝Ä¸ďż˝ďż˝ďż˝
//				for(RBMNode n:this.rbm.Nodes){
//					if(n.getType()==1){
//						tempHCV1[n.getID()]=n.getProbability();
//					}
//				}
				for(RBMNode n:this.rbm.hNodes)
						tempHCV1[n.getID()]=n.getProbability();
				this.rbm.getNodeState(1);//ďż˝ďż˝Ý¸ďż˝ďż˝Ę¸ďż˝ďż˝ďż˝ďż˝ă¸łÖ1¤7
				this.rbm.clearNodeState(0);
				v1=this.rbm.getNodeState(0);//ďż˝ďż˝Ý¸ďż˝ďż˝Ę¸ďż˝ďż˝Ô˛ă¸łÖľďż˝ďż˝ďż˝ďż˝ďż˝ďż1¤7
				this.rbm.clearNodeState(1);
				
				for(int v=0;v<this.rbm.vn;v++){
					for(int h=0;h<this.rbm.hn;h++){
						double tempDW=(double)v0[v]*tempHCV0[h]-(double)v1[v]*tempHCV1[h];
						if(argWD!=null){
							tempDW+=argWD.getWeightDecay(this.rbm.W, v, h);
						}
						this.rbm.W[v][h]+=this.rate*tempDW;
					}
				}

				//this.rbm.UpdateRBM();//ďż˝ďż˝ďż˝ďż˝RBM,ďż˝ďż˝ďż˝ďż˝Öťďż˝ďż˝Ęžďż˝Ëťďż˝ďż˝ďż˝Č¨Öľďż˝ďż˝ďż˝Úľďż˝ďż˝ĆŤďż˝ďż˝ÖľŇŞďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż1¤7
//				for(RBMNode n:this.rbm.Nodes){//ďż˝ďż˝ďż˝Â˝Úľďż˝bias
//					//double tempWD=argWD==null?0:n.bias*argWD.getWeightCost();Č¨ÖľËĽďż˝ďż˝Í¨ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ú˝Úľďż˝ĆŤďż˝ďż˝
//					if(n.getType()==0){//ďż˝ďż˝ďż˝ÂżÉźďż˝ďż˝ďż˝bias
//						n.setBias(n.getBias()+this.rate*((v0[n.getID()]-v1[n.getID()])/n.getVariance()));
//					}else{//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝ďż˝bias
//						n.setBias(n.getBias()+this.rate*(tempHCV0[n.getID()]-tempHCV1[n.getID()]));
//					}
//				}
				for(RBMNode n:this.rbm.vNodes)//ďż˝ďż˝ďż˝Â˝Úľďż˝bias
					n.setBias(n.getBias()+this.rate*((v0[n.getID()]-v1[n.getID()])/n.getVariance()));
				for(RBMNode n:this.rbm.hNodes)//ďż˝ďż˝ďż˝Â˝Úľďż˝bias
					n.setBias(n.getBias()+this.rate*(tempHCV0[n.getID()]-tempHCV1[n.getID()]));
			}
			
			epoch++;
//			double tempE=this.Errorta();
//			System.out.println("ďż˝ďż˝"+epoch+" ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝Řšďż˝ďż˝ďż˝ďż1¤7"+tempE);
//			if(tempE<=argSC || epoch >= this.max_try){
//				System.out.println("ďż˝ďż˝ďż˝ďż˝"+epoch+" ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝RBM Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝");
//				break;				
//			}
			
		}

	}
	
	
	public void MiniBatchCD(double argSC,WeightDecay argWD,int argMBS){
		//Ęšďż˝ďż˝minibatchďż˝ďż˝CD1Ńľďż˝ďż˝
		//ŇŞďż˝ďż˝ďż˝minibatchďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ń§Ď°ďż˝ĘŁďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ůśďż˝ČˇĘľďż˝ďż˝ďż1¤7
		//argMBS minibatchďż˝ďż˝ďż˝ďż˝ďż˝ďż˝
		
		int epoch=0;
		ArrayList<Data> tempD=this.dataSet.splitMiniBatch(argMBS);
		
		while(true){
			DBNTrain.print_time("pretrain for epoch " + epoch);
			for(Data d:tempD){//ďż˝ďż˝Ęźďż˝ďż˝Ňťďż˝ďż˝minibatchŃľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝
				//DBNTrain.print_time("prepare data for a batch");
				double[][] tempDeltaW=new double[this.rbm.vn][this.rbm.hn];//deltawďż˝Űźďż˝Öľďż˝ďż˝ďż˝ďż˝Ęąďż˝ďż˝ďż˝ćŁŹŇťďż˝ďż˝minibatchďż˝ďż˝Éşďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Č¨Öľ
				double[] tempDeltaBV=new double[this.rbm.vn];//ďż˝Ô˛ďż˝ďż˝deltabiasďż˝Űźďż˝Öľ
				double[] tempDeltaBH=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝deltabiasďż˝Űźďż˝Öľ
				
				for(Case c:d.getDataSet()){//ďż˝ďż˝ďż˝ďż˝minibatchďż˝ďż˝ĂżŇťďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝deltaw
					//DBNTrain.print_time("train a single case");
					this.rbm.clearNodeState(0);
					this.rbm.clearNodeState(1);
					
					double[] v0=c.getTheCase();
					this.rbm.setNodeState(v0, 0);//ďż˝ďż˝ďż˝ĂżÉźďż˝ďż˝ďż˝×´ĚŹÎŞV0
					double[] tempHCV0=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ä¸ďż˝ďż˝ďż1¤7
//					for(RBMNode n:this.rbm.Nodes){
//						if(n.getType()==1){
//							tempHCV0[n.getID()]=n.getProbability();
//						}
//					}
					for(RBMNode n:this.rbm.hNodes)
						tempHCV0[n.getID()]=n.getProbability();
					double[] h0=this.rbm.getNodeState(1);//ČĄďż˝ďż˝H0;
					this.rbm.clearNodeState(0);//ďż˝ďż˝ŐżÉźďż˝ďż˝
					double[] v1=this.rbm.getNodeState(0);//ČĄďż˝ďż˝V1
					this.rbm.clearNodeState(1);//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż1¤7
					double[] tempHCV1=new double[this.rbm.hn];//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝Úľďż˝Ä¸ďż˝ďż˝ďż˝
//					for(RBMNode n:this.rbm.Nodes){
//						if(n.getType()==1){
//							tempHCV1[n.getID()]=n.getProbability();
//						}
//					}
					for(RBMNode n:this.rbm.hNodes)
						tempHCV1[n.getID()]=n.getProbability();
					
					for(int v=0;v<this.rbm.vn;v++){
						for(int h=0;h<this.rbm.hn;h++){
							tempDeltaW[v][h]+=(double)v0[v]*tempHCV0[h]-(double)v1[v]*tempHCV1[h];//ďż˝Ýśďż˝ďż˝Űźďż˝
						}
					}
					
//					for(RBMNode n:this.rbm.Nodes){//ďż˝ďż˝ďż˝Â˝Úľďż˝biasďż˝ďż˝note ďż˝Úľďż˝ĆŤďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Â˛ďż˝ďż˝ďż˝ŇŞČ¨ÖľËĽďż˝ďż˝
//						if(n.getType()==0){//ďż˝ďż˝ďż˝ÂżÉźďż˝ďż˝ďż˝bias
//							tempDeltaBV[n.getID()]+=v0[n.getID()]-v1[n.getID()];
//						}else{//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝ďż˝bias
//							tempDeltaBH[n.getID()]+=tempHCV0[n.getID()]-tempHCV1[n.getID()];
//						}
//					}
					for(RBMNode n:this.rbm.vNodes)
						tempDeltaBV[n.getID()]+=v0[n.getID()]-v1[n.getID()];
					for(RBMNode n:this.rbm.hNodes)
						tempDeltaBH[n.getID()]+=tempHCV0[n.getID()]-tempHCV1[n.getID()];
					
				}//Ňťďż˝ďż˝minibatchďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ČŤďż˝ďż˝ďż˝ďż˝ďż1¤7
				
				//Tool.PrintW(tempDeltaW);
				//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Č¨Öľ
				//DBNTrain.print_time("update all Weight");
				for(int v=0;v<this.rbm.vn;v++){
					for(int h=0;h<this.rbm.hn;h++){
						this.rbm.W[v][h]+=(this.rate*(tempDeltaW[v][h])+(argWD==null?0:argWD.getWeightDecay(this.rbm.W, v, h)))/(double)d.getDataCount();
					}
				}
				
				//this.rbm.UpdateRBM();//ďż˝ďż˝ďż˝ďż˝RBM,ďż˝ďż˝ďż˝ďż˝Öťďż˝ďż˝Ęžďż˝Ëťďż˝ďż˝ďż˝Č¨Öľďż˝ďż˝ďż˝Úľďż˝ďż˝ĆŤďż˝ďż˝ÖľŇŞďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż1¤7
//				for(RBMNode n:this.rbm.Nodes){//ďż˝ďż˝ďż˝Â˝Úľďż˝bias
//					if(n.getType()==0){//ďż˝ďż˝ďż˝ÂżÉźďż˝ďż˝ďż˝bias
//						n.setBias(n.getBias()+this.rate/(double)d.getDataCount()*(tempDeltaBV[n.getID()]));
//					}else{//ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝Ř˛ďż˝ďż˝bias
//						n.setBias(n.getBias()+this.rate/(double)d.getDataCount()*(tempDeltaBH[n.getID()]));
//					}
//				}
				//DBNTrain.print_time("update all bias");
				for(RBMNode n:this.rbm.vNodes)
					n.setBias(n.getBias()+this.rate/(double)d.getDataCount()*(tempDeltaBV[n.getID()]));
				for(RBMNode n:this.rbm.hNodes)
					n.setBias(n.getBias()+this.rate/(double)d.getDataCount()*(tempDeltaBH[n.getID()]));
			}
			
			epoch++;
//			DBNTrain.print_time("calculate the error");
//			double tempE=this.Errorta();
			
			//System.out.println("ďż˝ďż˝"+epoch+" ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝ďż˝Řšďż˝ďż˝ďż˝ďż1¤7"+tempE);
			//if(tempE<=argSC || epoch >= this.max_try){
			if(epoch >= this.max_try){
				System.out.println("ďż˝ďż˝ďż˝ďż˝"+epoch+" ďż˝ďż˝Ńľďż˝ďż˝ďż˝ďż˝RBM Ńľďż˝ďż˝ďż˝ďż˝ďż˝ďż˝");
				break;				
			}
			
		}

	}
	
	public double[] sample(){//ďż˝ďż˝ďż˝ďż˝Ýźďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ČĄďż˝ďż˝
		return this.dataSet.getData(this.r.nextInt(this.dataSet.getDataCount()));
	}
	
	
	
}

