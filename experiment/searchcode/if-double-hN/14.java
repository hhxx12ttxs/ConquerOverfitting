package com.lqyandpy.RBM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class PermanentRBM implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double[][] weight;
	public double[] biasv;
	public double[] biash;
	public boolean type;
	
	public void ReadFromFile(String path){
		try {
			BufferedReader tempF=new BufferedReader(new FileReader(path));
			ArrayList<String> tempSS=new ArrayList<String>();
			String tempS=tempF.readLine();
			while(tempS!=null){
				tempSS.add(tempS);
				tempS=tempF.readLine();
			}
			
			int vn=tempSS.size()-3;
			int hn=tempSS.get(0).split(",").length;
			this.weight=new double[vn][hn];
			
			this.biasv=new double[vn];
			this.biash=new double[hn];
			
			for(int i=0;i<vn;i++){
				String[] tempW=tempSS.get(i).split(",");
				for(int j=0;j<hn;j++){
					this.weight[i][j]=Double.parseDouble(tempW[j]);
				}
			}
			
			String[] tempBV=tempSS.get(tempSS.size()-3).split(",");
			
		//	System.out.println(tempBV.length+","+this.biasv.length+","+this.biash.length);
			for(int i=0;i<vn;i++){
				this.biasv[i]=Double.parseDouble(tempBV[i]);
			}
			
			String[] tempBH=tempSS.get(tempSS.size()-2).split(",");
			for(int i=0;i<hn;i++){
				this.biash[i]=Double.parseDouble(tempBH[i]);
			}
			
			this.type=Boolean.parseBoolean(tempSS.get(tempSS.size()-1));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void WriteToFile(String path){
		//File tempF=new File(path);
		try {
			FileWriter tempF=new FileWriter(path);
			StringBuilder tempS=new StringBuilder();
			
			for(int i=0;i<this.weight.length;i++){
				for(int j=0;j<this.weight[0].length;j++){
					tempS.append(this.weight[i][j]);
					if(j<this.weight[0].length-1)
						tempS.append(",");
				}
				tempS.append("\r\n");
			}
			
			for(int i=0;i<biasv.length;i++){
				tempS.append(biasv[i]);
				if(i<this.biasv.length-1)
					tempS.append(",");
			}
			tempS.append("\r\n");
			
			for(int i=0;i<biash.length;i++){
				tempS.append(biash[i]);
				if(i<this.biash.length-1)
					tempS.append(",");
			}
			
			tempS.append("\r\n");
			tempS.append(this.type);
			
			tempF.write(tempS.toString());
			tempF.flush();
			tempF.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public RBM ReBuildRBM(){
		RBM tempR=new RBM(this.weight,this.type);
		
//		for(RBMNode n:tempR.Nodes){
//			if(n.getType()==0){
//				n.setBias(this.biasv[n.getID()]);
//			}else{
//				n.setBias(this.biash[n.getID()]);
//			}
//		}
		for(RBMNode n:tempR.vNodes)
			n.setBias(this.biasv[n.getID()]);
		for(RBMNode n:tempR.hNodes)
			n.setBias(this.biash[n.getID()]);
		
		return tempR;
	}

}

