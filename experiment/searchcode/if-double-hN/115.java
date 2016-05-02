package com.lqyandpy.DBN;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import com.lqyandpy.RBM.*;


public class SimpleDBN implements Serializable {
	public int Layers=2;
	public ArrayList<RBM> RBMStack=new ArrayList<RBM>();//RBMStack(0)ďż˝ďż˝ďż˝ďż˝×˛ďż1¤7
	public ArrayList<Double> output_layer;
	public double[][] output_w;
	public double[][] input_w; 
	
	public void constructDBN(int input_num,int[] hidden_nums,boolean guass)
	{
		this.RBMStack.clear();
		
		RBM tempR=new RBM(input_num,hidden_nums[0],guass);
		this.RBMStack.add(tempR);
		for(int i = 1;i < hidden_nums.length;++i)
		{
			tempR = new RBM(hidden_nums[i-1],hidden_nums[i],false);
			this.RBMStack.add(tempR);
		}
	}
	public void add_output_layer(int type_sum)
	{
		this.output_layer = new ArrayList<Double>(type_sum);
		this.output_w = new double[type_sum][this.getTop().vn];
		for(int i = 0;i < type_sum;++i)
			for(int j = 0;j < this.getTop().vn;++j)
			{
				double r = Math.random();
				this.output_w[i][j] = r*(-1.0d)+(1-r)*1.0d;
			}
	}
	
	public void init_input_weight(int inputlen,int layerlen)
	{
		this.input_w = new double[layerlen][inputlen];
		for(int i = 0;i < layerlen;++i)
			for(int j = 0;j < inputlen;++j)
			{
				double r = Math.random();
				this.output_w[i][j] = r*(-1.0d)+(1-r)*1.0d;
			}
	}
	
	public void InsertRBM(RBM tempR){
		this.RBMStack.add(tempR);
	}
	
	public RBM getTop(){
		return this.RBMStack.get(this.RBMStack.size()-1);
	}
	
	public RBM getRBM(int argI){
		return this.RBMStack.get(argI<this.RBMStack.size()&&argI>=0?argI:this.RBMStack.size()-1);
	}
	public String toString()
	{
		try{
			ByteArrayOutputStream tempFO=new ByteArrayOutputStream();
			ObjectOutputStream tempOO = new ObjectOutputStream(tempFO);
			
			ArrayList<PermanentRBM> tempPR=new ArrayList<PermanentRBM>();
			for(RBM r:this.RBMStack){
				tempPR.add(r.SaveAS());
			}
			
			tempOO.writeObject(tempPR);

			tempOO.close();
			return tempFO.toByteArray().toString();
        }catch(Exception e){
        	e.printStackTrace();
        }
		return null;	
	}
	public static byte[] toBytes(SimpleDBN dbn)
	{
		try{
			ByteArrayOutputStream tempFO=new ByteArrayOutputStream();
			ObjectOutputStream tempOO = new ObjectOutputStream(tempFO);
			
//			ArrayList<PermanentRBM> tempPR=new ArrayList<PermanentRBM>();
//			for(RBM r:this.RBMStack){
//				tempPR.add(r.SaveAS());
//			}
//			
//			tempOO.writeObject(tempPR);
			tempOO.writeObject(dbn);
			tempOO.close();
			return tempFO.toByteArray();
        }catch(Exception e){
        	e.printStackTrace();
        }
		return null;	
	}
	
	public static SimpleDBN  RebuildDBNbyBytes(byte[] s)
	{
		
		try {
			ByteArrayInputStream byte_in = new ByteArrayInputStream(s);
			ObjectInputStream o_in = new ObjectInputStream(byte_in);
//			ArrayList<PermanentRBM> tempPR = (ArrayList<PermanentRBM>)o_in.readObject();
//			ArrayList<RBM> tempL = new ArrayList<RBM>();
//			for(PermanentRBM pr:tempPR){
//				tempL.add(pr.ReBuildRBM());
//			}
			SimpleDBN dbn = (SimpleDBN)o_in.readObject();
			o_in.close();
//			this.Layers=tempL.size();
//			this.RBMStack=tempL;
			return dbn;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void RebuildDBNbyString(String s)
	{
		
		try {
			ByteArrayInputStream byte_in = new ByteArrayInputStream(s.getBytes());
			ObjectInputStream o_in = new ObjectInputStream(byte_in);
			ArrayList<PermanentRBM> tempPR = (ArrayList<PermanentRBM>)o_in.readObject();
			ArrayList<RBM> tempL = new ArrayList<RBM>();
			for(PermanentRBM pr:tempPR){
				tempL.add(pr.ReBuildRBM());
			}
			o_in.close();
			this.Layers=tempL.size();
			this.RBMStack=tempL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void combineDBN(SimpleDBN other)
	{
		if(other.RBMStack.size() != this.RBMStack.size())
			return;
		for(int i = 0;i < this.RBMStack.size();++i)
		{
			RBM thisone = this.RBMStack.get(i);
			RBM thatone = other.RBMStack.get(i);
			for(int row = 0;row < thisone.W.length;++row)
				for(int col = 0;col < thisone.W[0].length;++col)
					thisone.W[row][col] = (thisone.W[row][col] + thatone.W[row][col])/2.0;
			for(int j = 0; j < thisone.hn;++j)
				thisone.hNodes.get(j).setBias((thisone.hNodes.get(j).getBias() + thatone.hNodes.get(j).getBias())/2.0);
			for(int j = 0; j < thisone.vn;++j)
				thisone.vNodes.get(j).setBias((thisone.vNodes.get(j).getBias() + thatone.vNodes.get(j).getBias())/2.0);
		}
	}
	
	public void PermanentDBN(String argS){
		try{
			FileOutputStream tempFO=new FileOutputStream(argS,false);
			ObjectOutputStream tempOO = new ObjectOutputStream(tempFO);
			
			ArrayList<PermanentRBM> tempPR=new ArrayList<PermanentRBM>();
			for(RBM r:this.RBMStack){
				tempPR.add(r.SaveAS());
			}
			
			tempOO.writeObject(tempPR);

			tempOO.close();
			tempFO.close();
        }catch(Exception e){
        	e.printStackTrace();
        }	
	}
	
	public void RebuildDBN(String argS){
		ArrayList<RBM> tempL=new ArrayList<RBM>();
		File tempF=new File(argS);
		
		try{
			FileInputStream tempFI=new FileInputStream(argS);
			ObjectInputStream tempOI=new ObjectInputStream(tempFI);
			ArrayList<PermanentRBM> tempPRL = (ArrayList<PermanentRBM>)tempOI.readObject();
			
			for(PermanentRBM pr:tempPRL){
				tempL.add(pr.ReBuildRBM());
			}
			tempOI.close();
		}catch(Exception e){
			e.printStackTrace();
		} 
		
		this.Layers=tempL.size();
		this.RBMStack=tempL;
	}
	
	private int find_layer(int[] bin,int key)
	{
		for(int i = 0;i < bin.length;++i)
			if(bin[i] > key)
				return i;
		return -1;	
	}
	public int[] ann_bias;
	public double[][] get_ann_wight(int out_num)
	{
		
		int[] node_bin = new int[this.RBMStack.size()+2];//node_binďż˝Đľďż˝iďż˝ďż˝ÔŞďż˝Řąďż˝Ęžďż˝ďż˝iďż˝ăŁ¨ďż˝ďż˝ďż˝Ďľďż˝ďż˝ÂŁďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ďż˝ăľ˝ďż˝ďż˝ďż˝ďż˝ăŁŠďż˝Úľďż˝ďż˝ĹľÄşďż˝Ňťďż˝ďż˝ďż˝ďż˝
		int layer_sum = this.RBMStack.size()+2;
		node_bin[0] = out_num;
		for(int i = 0;i < this.RBMStack.size() ;++i)
		{
			node_bin[i+1] = node_bin[i] + this.RBMStack.get(this.RBMStack.size()-1-i).hn+1;
		}
		node_bin[this.RBMStack.size()+1] = node_bin[this.RBMStack.size()] + this.RBMStack.get(0).vn + 1;
		int node_sum = node_bin[layer_sum-1];
		double[][] tempW = new double[node_sum][node_sum];
		for(int i = 0;i < node_sum;++i)
			for(int j = 0;j < node_sum;++j)
				tempW[i][j] = Double.NaN;
		for(int i = 0;i < node_sum;++i)
		{
			int layer = find_layer(node_bin,i);
			if(layer == -1)
				return tempW;
			if(layer != 0 && i == node_bin[layer]-1)
				continue;
			else if(layer == 0)
			{
				for(int j = node_bin[0];j < node_bin[1];++j)
				{//ĆŤďż˝ĆşÍˇďż˝ĆŤďż˝Ć˝ÚľçśźŇťďż˝ďż˝,ďż˝ďż˝ďż˝ďż˝Ęźďż˝ďż˝
					double r = Math.random();
					tempW[i][j] = r;
					tempW[j][i] = r;
				}
			}
			else if(layer < layer_sum-1)
			{
				for(int j = node_bin[layer];j < node_bin[layer+1]-1;++j)
				{//ďż˝ďż˝ĆŤďż˝Ć˝ďż˝ďż1¤7
					RBM i_rbm = this.RBMStack.get(this.RBMStack.size() - layer);
					double[][] w = i_rbm.W;
					double r = w[j-node_bin[layer]][i-node_bin[layer-1]];
					tempW[i][j] = r;
					tempW[j][i] = r;
				}
				//ĆŤďż˝Ć˝ďż˝ďż1¤7
				double r = this.RBMStack.get(this.RBMStack.size() - layer).hNodes.get(i-node_bin[layer-1]).getBias();
				tempW[i][node_bin[layer+1]-1] = r;
				tempW[node_bin[layer+1]-1][i] = r;
			}
		}
		this.ann_bias = new int[layer_sum-1];
		for(int i = 1;i < layer_sum;++i)
			this.ann_bias[i-1] = node_bin[i]-1;
		return tempW;
	}
}

