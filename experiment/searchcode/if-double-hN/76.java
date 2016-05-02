package com.lqyandpy.DBN;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import com.lqyandpy.RBM.CDTrain;
import com.lqyandpy.RBM.Data;
import com.lqyandpy.RBM.Data.Case;
import com.lqyandpy.RBM.RBM;
import com.lqyandpy.RBM.BasicRBMNode;
import com.lqyandpy.RBM.WeightDecay;



public class DBNTrain {
	//public double learningrate;
	private Data dataSet;
	private SimpleDBN dbn;
	private double rate=0.001;
	
	
	//public Weight_Decay wd;
	public static void print_time(String s)
	{
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		System.out.println(s + " at " + sf.format(new java.util.Date()));
	}
	
	public DBNTrain(Data argD,SimpleDBN argN){
		this.dataSet=argD;
		this.dbn=argN;
	}
	
	public void setLearningRate(double argD){
		this.rate=argD;
	}
	
	public void greedyLayerwiseTraining(double argSC,double argLR,WeightDecay argWD,int max_try,int batch_size){//ֹͣ����,ѧϰ��,�����ڵ����Ŀ,Ȩֵ˥������
		//this.dbn.RBMStack.clear();
		//RBM tempR=new RBM(this.dataSet.getDimension(),argH[0],argG);
		RBM tempR = this.dbn.getRBM(0);
		CDTrain tempCDT=new CDTrain(dataSet, tempR,max_try);
		if(batch_size == 1)
			tempCDT.PersistentCD(argSC,argWD);
		else
			tempCDT.MiniBatchCD(argSC, argWD, batch_size);
		//this.dbn.InsertRBM(tempR);
		this.greedyLayerwiseTraining(tempR, argSC,argLR,argWD,max_try,batch_size);
	}
	
	public void greedyLayerwiseTraining(RBM argSeed,double argSC,double argLR,WeightDecay argWD,int max_try,int batch_size){//��һ��RBM��ֹͣ������ѧϰ�ʣ�Ȩֵ˥������
		//this.dbn.InsertRBM(argSeed);
		//RBM tempR=argSeed.CopyTiedRBM(h_node[this.dbn.RBMStack.size()],argSeed.hn);//��һ���Ѿ�ѵ�����ˣ�ֱ�ӿ�ʼѵ���ڶ���
		
		for(int i=1;i<this.dbn.RBMStack.size();i++){
			RBM tempR = this.dbn.getRBM(i);
			DBNTrain.print_time("get data");
			Data tempD=this.getDataForNextLayer();//ȡ����ѵ�����
			DBNTrain.print_time("get CDTrain instance");
			CDTrain tempCDT=new CDTrain(tempD,tempR,max_try);
			//tempCDT.PersistentCD(argSC,argWD);//ѵ������RBM
			if(batch_size == 1)
				tempCDT.PersistentCD(argSC,argWD);
			else
				tempCDT.MiniBatchCD(argSC, argWD, batch_size);
			
			
			System.out.println("��ɵ�"+(i+1)+"��ѵ��");
			
			//this.dbn.InsertRBM(tempR);//��ѵ���õ�RBM����ջ��
			
			//tempR=tempR.CopyTiedRBM(h_node[this.dbn.RBMStack.size()],tempR.hn);//ȡ����һ��ԭʼRBM
		}
	}
	
	public Data getDataForNextLayer(){//�ӵ�ǰ��RBM����ȡ����Ϊ��һ��RBM��ѵ������
		double[][] tempDB=new double[this.dataSet.getDataCount()][this.dbn.getTop().hn];
		int tempC=0;
		for(Case v:this.dataSet.getDataSet()){
			double[] tempI=v.getTheCase();
			for(RBM m:this.dbn.RBMStack){
				m.clearNodeState(0);
				m.clearNodeState(1);
				
				m.setNodeState(tempI,0);
				tempI=m.getNodeState(1);
				
				m.clearNodeState(0);
				m.clearNodeState(1);
				
				
			}
			tempDB[tempC++]=tempI;
		}
		
		return new Data(tempDB,false,false);//���㶼�Ƕ�ֵ�ģ�����Ҫ�淶��
	}

	
}

