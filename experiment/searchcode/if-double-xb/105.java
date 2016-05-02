package ds.importData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import ds.function.Function;

import prclqz.DAO.IDAO;
import prclqz.DAO.MyDAOImpl;
import prclqz.core.Message;
import prclqz.core.enumLib.*;
import prclqz.lib.EnumMapTool;
import prclqz.methods.IMethod;
import prclqz.methods.MethodException;
import prclqz.core.Const;
import test.EnumTools;

/**
 * ??????????
 * @author DS
 *
 */
public class MPredictDeathByProvince implements IMethod{
    final int ND1=2010;
    final int ND2=2160;
    double E0;
    
    public static void main(String argv[]) throws Exception{
    	int dqx=11;
    	/**********for test*************/
    	MPredictDeathByProvince m = new MPredictDeathByProvince();
    	String path = "d:\\prclqz\\??????\\";
    	/**********?????????????***************/
    	Map<Integer,Map<CXAll,Map<XBAll, double[]>>> ExpectedAgePredict = EnumMapTool.createExpectedAgePredict(180);//??
    	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path+"beijing.txt"))));
    	int x=0;
    	while(br.ready()){
    		String[] str = br.readLine().split(",");
    		ExpectedAgePredict.get(dqx).get(CXAll.Chengxiang).get(XBAll.all)[x] = Double.parseDouble(str[1]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Chengxiang).get(XBAll.Male)[x] = Double.parseDouble(str[2]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Chengxiang).get(XBAll.Female)[x] = Double.parseDouble(str[3]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Chengzhen).get(XBAll.all)[x] = Double.parseDouble(str[4]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Chengzhen).get(XBAll.Male)[x] = Double.parseDouble(str[5]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Chengzhen).get(XBAll.Female)[x] = Double.parseDouble(str[6]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Nongcun).get(XBAll.all)[x] = Double.parseDouble(str[7]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Nongcun).get(XBAll.Male)[x] = Double.parseDouble(str[8]);
    		ExpectedAgePredict.get(dqx).get(CXAll.Nongcun).get(XBAll.Female)[x] = Double.parseDouble(str[9]);
    		x++;
    	}
    	/**********?????***********/
    	Map<Integer,Map<CXAll,Map<XBAll,Map<DPPredict,double[]>>>> Lives = EnumMapTool.createLives(Const.MAX_AGE);//??
    	for(CX cx:CX.values()){
    		for(XB xb:XB.values()){
    			BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path+"??_"+cx.getChinese()+"_"+xb.getChinese()+".txt"))));
    			for(int i=0;i<111;i++){
    				String[] str = br1.readLine().split(",");
    				x=1;
    				for(DPPredict dpp:DPPredict.values()){
    						Lives.get(dqx).get(cx.toCXAll()).get(xb.toXBAll()).get(dpp)[i] = Double.parseDouble(str[x]);
    						x++;
    				}
    			}
    		}
    	}
    	/************??globals***************/
    	HashMap<String, Object> globals = new HashMap<String, Object>();
    	globals.put("ExpectedAgePredict", ExpectedAgePredict);
    	globals.put("Lives", Lives);
    	
    	//????
    	m.calculate(new MyDAOImpl(), globals);
    }
    
    public void calculate(IDAO m, HashMap<String, Object> globals){
    	int DQ = 11;//TODO ?????
    	int X=0;
	
		//????(MPredictAgeByProvince)?????????;
    	Map<Integer,Map<CXAll,Map<XBAll,double[]>>> ExpectedAgePredict = (Map<Integer,Map<CXAll,Map<XBAll,double[]>>>)globals.get("ExpectedAgePredict");
    	
    	//????????????
    	Map<Integer,Map<CX,Map<XB,Map<DPPredict,double[]>>>> Lives = (Map<Integer,Map<CX,Map<XB,Map<DPPredict,double[]>>>>)globals.get("Lives");
    	//??????????
		Map<Integer,Map<CX,Map<XB,Map<Year,double[]>>>> DeathPossibility = new HashMap<Integer,Map<CX,Map<XB,Map<Year,double[]>>>>();//??????,?????????????
		globals.put("DeathPossibility",DeathPossibility);
		
		
	    DeathPossibility.put(DQ,EnumMapTool.createDeathPossibility());
	    Map<CXAll,Map<XBAll,double[]>>ProvinceAgePredict = ExpectedAgePredict.get(DQ);  //?? ??????????
	    Map<DPPredict,double[]> AAA = EnumMapTool.createAAA(Const.MAX_AGE);//???
	    for(CX cx:CX.values()){
	    	for(XB xb:XB.values()){
	    		Map<Year,double[]>ProvinceDeathPossibility = DeathPossibility.get(DQ).get(cx).get(xb); //????????????,?????????
	    		Map<DPPredict,double[]>ProvinceLives = Lives.get(DQ).get(cx.toCXAll()).get(xb.toXBAll()); //?????????
	    		//????=??????????
	    		/*
	    		 * REPL ????.Q2010 WITH ???.QX ALL
	    		 * REPL AAA.QX WITH ???.QX ALL
				 * REPL AAA.EX WITH ???.EX ALL
				 * REPL AAA.AX WITH ???.AX ALL
	    		 */
	    		for(X=Const.MAX_AGE-1;X>=0;X--){
	    			ProvinceDeathPossibility.get(Year.Y2010)[X] = ProvinceLives.get(DPPredict.QX)[X];
	    			AAA.get(DPPredict.QX)[X] = Function.Round(ProvinceLives.get(DPPredict.QX)[X],6);
	    			AAA.get(DPPredict.EX)[X] = Function.Round(ProvinceLives.get(DPPredict.EX)[X],6);
	    			AAA.get(DPPredict.AX)[X] = Function.Round(ProvinceLives.get(DPPredict.AX)[X],6);
			    }
			    
			    //FOR ND0=2011 TO ND2
	    		int ND2=2159;
			    for(int ND0 = 2011;ND0<=ND2;ND0++){
			    	/*
			    	 * SELE  D
						USE &??????????	&&
						LOCA FOR ??=ND0
						E1=??B-IIF(XB=1,'M','F')
						E0=&E1
					
			    	 */
			    	E0 = ProvinceAgePredict.get(cx.toCXAll()).get(XBAll.getFromXB(xb))[ND0-1981];
			    	//System.out.println(""+ND0+cx.toString()+xb.toString()+"E0:"+E0);
					//REPL AAA.QX WITH ????.&QND1   &&??????all??????????
			    	//TODO:foxpro????????112-,-
			    	AAA.get(DPPredict.QX)[0] = ProvinceDeathPossibility.get(Year.getYear(ND0-1))[0];
			    	
					double N=1;
					while(true){
					    for(X=Const.MAX_AGE-1;X>=0;X--){
					    	AAA.get(DPPredict.QX)[X] = Function.Round(N*AAA.get(DPPredict.QX)[X],6);
					    }
					    X=0;
//					    System.out.println(N+" "+AAA.get(DPPredict.QX)[X]);
					    AAA.get(DPPredict.LX1)[0] = 100000;					    
					   	AAA.get(DPPredict.DX)[0] = Function.Chunk(100000*AAA.get(DPPredict.QX)[0]);//TODO:Chunk?
					   	
					   //	System.out.println(N+" "+AAA.get(DPPredict.LX1)[X]+" "+AAA.get(DPPredict.DX)[X]);
					    //System.out.println(AAA.get(DPPredict.DX)[X]);
					    while(X!=Const.MAX_AGE-1){ //TODO:DO WHILE .NOT.EOF()	&&X<110,????????110??109???
					    	double LX = AAA.get(DPPredict.LX1)[X] - AAA.get(DPPredict.DX)[X];
					    	X++;
					    	AAA.get(DPPredict.LX1)[X] = Function.Round(LX,6);
					    	AAA.get(DPPredict.DX)[X] = Function.Round(LX*AAA.get(DPPredict.QX)[X],6);
					    }
//					    System.out.println(":"+debug);
//					    for(int i=0;i<Const.MAX_AGE;i++){
//					    	System.out.println(AAA.get(DPPredict.LX1)[i]+" "+AAA.get(DPPredict.DX)[i]);
//					    }
						X=Const.MAX_AGE-1;
					    AAA.get(DPPredict.DX)[X] = AAA.get(DPPredict.LX1)[X];
					    //System.out.println(AAA.get(DPPredict.DX)[X] * AAA.get(DPPredict.AX)[X]);
					    AAA.get(DPPredict.TX)[X] = Math.round(AAA.get(DPPredict.DX)[X] * AAA.get(DPPredict.AX)[X]);
					    //System.out.println(AAA.get(DPPredict.TX)[X]);
					    AAA.get(DPPredict.LX2)[X] = Function.Round(AAA.get(DPPredict.DX)[X] * AAA.get(DPPredict.AX)[X],6);
					    AAA.get(DPPredict.QX)[X] = 1;
//					    System.out.println(AAA.get(DPPredict.DX)[X]);
//					    System.out.println(AAA.get(DPPredict.TX)[X]);
//					    System.out.println(AAA.get(DPPredict.LX2)[X]);
//					    System.out.println(AAA.get(DPPredict.QX)[X]);

					    while(X>0){
							double LX = AAA.get(DPPredict.LX1)[X];
							double TX0 = AAA.get(DPPredict.TX)[X];
							X--;
							AAA.get(DPPredict.LX2)[X] = Function.Round(LX + AAA.get(DPPredict.DX)[X]*AAA.get(DPPredict.AX)[X],6);
							AAA.get(DPPredict.TX)[X] = Math.round(TX0 + AAA.get(DPPredict.LX2)[X]);
					    }
					    //REPL EX WITH TX/LX1 FOR LX1<>0
					    for(X=Const.MAX_AGE-1;X>=0;X--){
					    	if(AAA.get(DPPredict.LX1)[X]!=0){
					    		AAA.get(DPPredict.EX)[X] = Function.Round(AAA.get(DPPredict.TX)[X]/AAA.get(DPPredict.LX1)[X],6); 
					    	}
					    }
					    
					    X=0;
					    //System.out.println(N+" "+AAA.get(DPPredict.EX)[X]);
					
					    double round1 = Function.Round(AAA.get(DPPredict.EX)[X],3);
					    double round2 = Function.Round(E0,3);
					   // System.out.println(N+" "+round1+" "+round2);
//					    debug++;
					    //return;
					    if(round1 == round2){
					    	break;
					    }
					    else if(round1 > round2){
					    	N=N*1.00001;
					    	for(int i=Const.MAX_AGE-1;i>=0;i--){
					    		(AAA.get(DPPredict.QX))[i] = Function.Round((ProvinceDeathPossibility.get(Year.getYear(ND0-1)))[i],6);
					    	}
//						    System.out.println(":"+debug);
//					    	debug++;
//						    for(int i=0;i<Const.MAX_AGE;i++){
//						    	System.out.println(AAA.get(DPPredict.QX)[i]);//+" : "+AAA.get(DPPredict.LX2)[i]+" : "+AAA.get(DPPredict.TX)[i]+" : "+AAA.get(DPPredict.EX)[i]+" : "+AAA.get(DPPredict.QX)[i]);
//						    }
					    	continue;
					    }
					    else if(round1 < round2){
						    N=N*0.99999;
						    continue;
					    }
					    AAA.get(DPPredict.QX)[110] = 1;
					}
//				    for(int i=0;i<Const.MAX_AGE;i++){
//				    	System.out.println(AAA.get(DPPredict.MX)[i]+" : "+AAA.get(DPPredict.QX)[i]+" : "+AAA.get(DPPredict.LX1)[i]+" : "+AAA.get(DPPredict.DX)[i]+" : "+AAA.get(DPPredict.LX2)[i]+" : "+AAA.get(DPPredict.TX)[i]+" : "+AAA.get(DPPredict.EX)[i]);
//				    }
					X=0;
					for(X = Const.MAX_AGE-1;X>=0;X--){
					    if(AAA.get(DPPredict.LX2)[X]!=0)
					    	AAA.get(DPPredict.MX2)[X] = Function.Round(AAA.get(DPPredict.DX)[X]/AAA.get(DPPredict.LX2)[X],6);
					    ProvinceDeathPossibility.get(Year.getYear(ND0))[X] = Function.Round(AAA.get(DPPredict.QX)[X],6);
					}
					//System.out.println(ND0);
					//break;
			    }
			    //break;
			}
	    	//break;
	    }
	    /***************test??******************/
	    System.out.println("------------------\nEND");
	    for(CX cx:CX.values()){
	    	for(XB xb:XB.values()){
	    		Map<Year, double[]> p = DeathPossibility.get(DQ).get(cx).get(xb);
	    		EnumTools.outputDeathPossibility(p,"d:\\prclqz\\??????\\??\\"+cx.getChinese()+xb.getChinese());
	    	}
	    }
	}

	@Override
	public void setParam(String params, int type) throws MethodException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Message checkDatabase(IDAO m, HashMap<String, Object> globals)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

