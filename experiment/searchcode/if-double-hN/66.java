package com.lqyandpy.RBM;

import java.io.Serializable;
import java.util.*;

import com.lqyandpy.crf.Node;

public class RBM implements Serializable {
	public double[][] W;//Ȩֵ����
	public ArrayList<RBMNode> vNodes=new ArrayList<RBMNode>();//�ڵ�
	public ArrayList<RBMNode> hNodes=new ArrayList<RBMNode>();
	public int hn;//���ڵ���Ŀ�����������
	public int vn;//�Խڵ���Ŀ�����������
	public boolean type;//true:gaussRBM,false:binaryRBM
	
	public RBM(int argV,int argH,boolean argG){//argG true GaussRBM
		//�����ز�Ϳɼ��ڵ���Ŀ
		double[][] tempD=new double[argV][argH];
		this.hn=argH;
		this.vn=argV;
		
		GaussDistribution tempG=new GaussDistribution();
		
		for(int i=0;i<argV;i++)
			for(int j=0;j<argH;j++)
				tempD[i][j]=tempG.next();
		this.W=tempD;
		
		this.type=argG;
		
		this.ConstructRBM(this.W,argG);
	}

	public RBM(double[][] argD,boolean argG){
		//��Ȩ��������RBM
		this.vn=argD.length;
		this.hn=argD[0].length;
		this.W=argD;
		this.ConstructRBM(this.W,argG);
		
	}
	
//	public RBMNode getNode(int argID,int argT){//���ͺ�id
//		RBMNode tempN=null;
//		for(RBMNode n:Nodes){
//			if(n.getID()==argID&&n.getType()==argT){
//				tempN=n;
//				break;
//			}
//		}
//		return tempN;
//	}
	
	public RBMNode getNode(int argID,int argT){//���ͺ�id
		RBMNode tempN= argT == 0?this.vNodes.get(argID):this.hNodes.get(argID);
		
		return tempN;
	}
	
	public double[][] WT(){//W��ת�þ���
		double[][] tempRes=new double[this.W[0].length][this.W.length];
		
		for(int i=0;i<tempRes.length;i++){
			for(int j=0;j<tempRes[0].length;j++){
				tempRes[i][j]=this.W[j][i];
			}
		}
		
	    return tempRes;
	}
	

	
	public double[] getWRow(int argD){
		return this.W[argD];
	}
	
	public double[] getWColumn(int argD){
		double[] tempRes=new double[this.W.length];
		for(int i=0;i<tempRes.length;i++){
			tempRes[i]=this.W[i][argD];
		}
		
		return tempRes;
	}
	
	
	public void setNodeState(double[] argI,int argT){
		for(int i=0;i<argI.length;i++){
			this.getNode(i,argT).setState(argI[i]);
		}
	}
	
	public double[] getNodeState(int argT){//argT 1��/0��
		double[] tempI=new double[argT==0?this.vn:this.hn];
		
		for(int i=0;i<tempI.length;i++){
			tempI[i]=this.getNode(i, argT).getState();
		}
		
		return tempI;
	}
	
//	public void clearNodeState(int argT){
//		for(RBMNode n:this.Nodes){
//			if(n.getType()==argT){
//				n.clearState();
//			}
//		}
//	}
//	
//	public void UpdateRBM(){
//		for(RBMNode n:this.Nodes){
//			double[] tempW=n.getType()==0?this.getWRow(n.getID()):this.getWColumn(n.getID());
//			for(PLink l:n.getLinks()){
//				l.weight=tempW[l.end.getID()];
//			}	
//		}
//	}
	
	public void clearNodeState(int argT){
		ArrayList<RBMNode> tmp = argT==0?this.vNodes:this.hNodes;
		for(RBMNode n:tmp){
				n.clearState();
		}
	}
	
//	public void UpdateRBM(){
//		for(RBMNode n:this.vNodes){
//			double[] tempW=this.getWRow(n.getID());
//			for(PLink l:n.getLinks()){
//				l.weight=tempW[l.end.getID()];
//			}	
//		}
//		for(RBMNode n:this.hNodes){
//			double[] tempW=this.getWColumn(n.getID());
//			for(PLink l:n.getLinks()){
//				l.weight=tempW[l.end.getID()];
//			}	
//		}
//	}
	
	public PermanentRBM SaveAS(){
		PermanentRBM tempP=new PermanentRBM();
		tempP.weight=this.W;
		double[] biasv=new double[this.vn];
		double[] biash=new double[this.hn];
		
//		for(RBMNode n:this.Nodes){
//			if(n.getType()==0){
//				biasv[n.getID()]=n.getBias();
//			}else{
//				biash[n.getID()]=n.getBias();
//			}
//		}
		
		for(RBMNode n:this.vNodes)
			biasv[n.getID()] = n.getBias();
		for(RBMNode n:this.hNodes)
			biash[n.getID()] = n.getBias();
		
		tempP.biasv=biasv;
		tempP.biash=biash;
		
		tempP.type=this.type;
		
		return tempP;
	}
	
	public RBM CopyTiedRBM(){//�������ר����̰�����ѵ��RBM����˶��Ƕ�ֵRBM
		double[][] tempW=this.WT();
		RBM tempRBM=new RBM(tempW,false);
		
//		for(RBMNode n:tempRBM.Nodes){
//			n.setBias(this.getNode(n.getID(), (n.getType()+1)%2).getBias());//��ʼ���ϲ������bias		
//		}
		for(RBMNode n:tempRBM.vNodes)
			n.setBias(this.getNode(n.getID(), (n.getType()+1)%2).getBias());
		for(RBMNode n:tempRBM.hNodes)
			n.setBias(this.getNode(n.getID(), (n.getType()+1)%2).getBias());
		return tempRBM;
	}
	
	public RBM CopyTiedRBM(int hnode,int vnode){//�������ר����̰�����ѵ��RBM����˶��Ƕ�ֵRBM
		
		RBM tempRBM=new RBM(vnode,hnode,false);
		
		return tempRBM;
	}
	
	public double getFreeEnegy(double[] argI){//����Բ�ڵ��������
		double tempFE=1;
		for(int i=0;i<this.hn;i++){
			double tempHO=0;
			for(int j=0;j<this.vn;j++){
				tempHO+=argI[j]*this.W[j][i];
			}
			tempHO=1+Math.exp(tempHO);
			tempFE*=tempHO;
		}
		return tempFE;
	}
	
	public void ConstructRBM(double[][] argD,boolean argG){
		//�Ӿ�����ͼ,�����Ӧvisible����Ŀ�������Ӧhidden����Ŀ��argGָʾ�Ƿ񴴽�guassrbm
		int tempV=this.vn;//�Խڵ���Ŀ
		int tempH=this.hn;//���ڵ���Ŀ
		
		for(int i=0;i<tempH;i++){//��ʼ�����ڵ㣬bias�������
			BasicRBMNode tempR=new BasicRBMNode(this);
			tempR.setType(1);
			tempR.setBias(Math.random());
			tempR.setID(i);
			tempR.setState(Double.NaN);
			//this.Nodes.add(tempR);
			this.hNodes.add(tempR);
		}
		
		for(int i=0;i<tempV;i++){//��ʼ���Խڵ㣬bias�������
			RBMNode tempR;
			if(!argG){
				tempR=new BasicRBMNode(this);
			}else{
				tempR=new GaussRBMNode(this);
			}
			tempR.setType(0);
			tempR.setBias(Math.random());//BUG
			tempR.setID(i);
			tempR.setState(Double.NaN);
			//this.Nodes.add(tempR);
			this.vNodes.add(tempR);
		}
		
		
//		for(RBMNode n:this.Nodes){
//			ArrayList<PLink> tempLKS=new ArrayList<PLink>(); 
//			double[] tempW=n.getType()==0?this.getWRow(n.getID()):this.getWColumn(n.getID());//Ȩ����
//			for(int i=0;i<tempW.length;i++){
//				PLink tempL=new PLink();
//				tempL.weight=tempW[i];
//				tempL.end=this.getNode(i, (n.getType()+1)%2);
//				tempLKS.add(tempL);
//			}
//			n.setLinks(tempLKS);
//		
//		}
		
//		for(RBMNode n:this.vNodes){
//			ArrayList<PLink> tempLKS=new ArrayList<PLink>(); 
//			double[] tempW=this.getWRow(n.getID());//Ȩ����
//			for(int i=0;i<tempW.length;i++){
//				PLink tempL=new PLink();
//				tempL.weight=tempW[i];
//				tempL.end=this.getNode(i, 1);
//				tempLKS.add(tempL);
//			}
//			n.setLinks(tempLKS);
//		
//		}
//		
//		for(RBMNode n:this.hNodes){
//			ArrayList<PLink> tempLKS=new ArrayList<PLink>(); 
//			double[] tempW=this.getWColumn(n.getID());//Ȩ����
//			for(int i=0;i<tempW.length;i++){
//				PLink tempL=new PLink();
//				tempL.weight=tempW[i];
//				tempL.end=this.getNode(i, 0);
//				tempLKS.add(tempL);
//			}
//			n.setLinks(tempLKS);
//		
//		}
		
	}

}

