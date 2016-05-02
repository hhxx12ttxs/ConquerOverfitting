package ds.importData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.functors.IfClosure;

import prclqz.DAO.IDAO;
import prclqz.core.Const;
import prclqz.core.Message;
import prclqz.core.enumLib.*;
import prclqz.lib.EnumMapTool;
import prclqz.methods.IMethod;
import prclqz.methods.MethodException;
/*
 *NPChazhi????????,???aaaChazhi????????
 */
public class MPredictAgeByProvince implements IMethod{
    public static final int YearRange = 179;
    
	int N=0;
	
	/**
	 * test done at 2012-10-28 16:12
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String[] argv)throws Exception{
		MPredictAgeByProvince m = new MPredictAgeByProvince();
		String path="D:\\TODO\\????????????????\\????\\??\\????\\";
		int year[] = {1981,1990,2000,2005,2010};
		/*******????????****/
		Map<Year,Map<Integer,Map<CXAll,Map<XBAll,Double>>>> ExpectedAge = EnumMapTool.createExpectedAge();
		//DONE:??ExpectedAge
		for(int i=0;i<year.length;i++){
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path+year[i]+".txt"))));
			
			int count=0;
			while(br.ready()){
				Map<CXAll, Map<XBAll,Double>> tmp = ExpectedAge.get(Year.getYear(year[i])).get(Const.provinceID[count]);
				String string = br.readLine();
				String[] str = string.split(",");
				tmp.get(CXAll.Chengxiang).put(XBAll.all, Double.parseDouble(str[1]));
				tmp.get(CXAll.Chengxiang).put(XBAll.Male, Double.parseDouble(str[2]));
				tmp.get(CXAll.Chengxiang).put(XBAll.Female, Double.parseDouble(str[3]));
				tmp.get(CXAll.Chengzhen).put(XBAll.all, Double.parseDouble(str[4]));
				tmp.get(CXAll.Chengzhen).put(XBAll.Male, Double.parseDouble(str[5]));
				tmp.get(CXAll.Chengzhen).put(XBAll.Female, Double.parseDouble(str[6]));
				tmp.get(CXAll.Nongcun).put(XBAll.all, Double.parseDouble(str[7]));
				tmp.get(CXAll.Nongcun).put(XBAll.Male, Double.parseDouble(str[8]));
				tmp.get(CXAll.Nongcun).put(XBAll.Female, Double.parseDouble(str[9]));
				
				count++;
			}
		}
		
		/*****???????????*****/
		Map<Integer,Map<CXAll,Map<XBAll, double[]>>> ExpectedAgePredict = EnumMapTool.createExpectedAgePredict(180);//??
		
		
		/*******??globasl******/
		Map<String, Object> globals = new HashMap<String,Object>();
		globals.put("ExpectedAge", ExpectedAge);
		globals.put("ExpectedAgePredict", ExpectedAgePredict);
			
		/*****??*****/
		m.calculate(globals);
	}
    
    public void calculate(Map<String,Object> globals){
    	
    	int dqx=11;//TODO:?????? ???????????
		Map<Year,Map<Integer,Map<CXAll,Map<XBAll,Double>>>> ExpectedAge = (Map<Year, Map<Integer, Map<CXAll,Map<XBAll, Double>>>>) globals.get("ExpectedAge");//??  ??????????1981,1990,2000,2005,2010 ??????????
			
		Map<Integer,Map<CXAll,Map<XBAll,double[]>>> NPChazhi = (Map<Integer,Map<CXAll,Map<XBAll,double[]>>>)globals.get("ExpectedAgePredict");//?????????????????1981-2160       ????????,NPChazhi[0]??1981?
		
		int X;

		//////////////translated by Foxpro2Java Translator successfully:///////////////
		
		Map<CXAll,Map<XBAll,double[]>> ProvinceChazhi = NPChazhi.get(dqx);//??,??????????
	    
	    int SS=0;
	    for(CXAll cxa : CXAll.values()){
	    	for(XBAll xba:XBAll.values()){
	    		double[] AREX = new double[6];
	    		//	??EX=??B-IIF(XB=1,'T',IIF(XB=2,'M','F'))
				//COPY TO ARRAY AREX FIELDS ND81.&??EX,ND90.&??EX,ND00.&??EX,ND05.&??EX,ND10.&??EX FOR ??=DQB
	   
				AREX[1]=ExpectedAge.get(Year.Y1981).get(dqx).get(cxa).get(xba);
				AREX[2]=ExpectedAge.get(Year.Y1990).get(dqx).get(cxa).get(xba);
				AREX[3]=ExpectedAge.get(Year.Y2000).get(dqx).get(cxa).get(xba);
				AREX[4]=ExpectedAge.get(Year.Y2005).get(dqx).get(cxa).get(xba);
				AREX[5]=ExpectedAge.get(Year.Y2010).get(dqx).get(cxa).get(xba);
				//System.out.println(AREX[1]+" "+AREX[2]+" "+AREX[3]+" "+AREX[4]+" "+AREX[5]); done!
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				/*
				  REPLACE &??EX WITH IIF(??>=1981.AND.??<1990,AREX(1)+(AREX(2)-AREX(1))/9*(??-1981),;
				  IIF(??=>1990.AND.??<2000,AREX(2)+(AREX(3)-AREX(2))/10*(??-1990),;
				  IIF(??=>2000.AND.??<2005,AREX(3)+(AREX(4)-AREX(3))/5*(??-2000),;
				  IIF(??=>2005.AND.??<=2010,AREX(4)+(AREX(5)-AREX(4))/5*(??-2005),0)))) all
				*/
				double[] cxex = ProvinceChazhi.get(cxa).get(xba);
				if( AREX[1]!=0.0 && AREX[2]!=0.0 && AREX[3]!=0.0 && AREX[4]!=0.0 && AREX[5]!=0.0){
				    for(X = YearRange; X >= 0 ; X--){
					if( true ){
					    	if(X>=0 && X<1990-1981){
					    		cxex[X] = AREX[1]+(AREX[2]-AREX[1])/9*X;
					    	}
					    	else if(X>=1990-1981 && X<2000-1981){
					    		cxex[X] = AREX[2]+(AREX[3]-AREX[2])/10*(X-1990+1981);
						    }
						    else if(X>=2000-1981 && X<2005-1981){
						    	cxex[X] = AREX[3]+(AREX[4]-AREX[3])/5*(X-2000+1981);
						    }
						    else if(X>=2005-1981 && X<=2010-1981){
						    	cxex[X] = AREX[4]+(AREX[5]-AREX[4])/5*(X-2005+1981);
						    }
						    else 
						    	cxex[X] = 0;
					}
				    }
				}
				/*
				  CASE AREX(1)=0.AND.AREX(2)<>0.AND.AREX(3)<>0.AND.AREX(4)<>0.AND.AREX(5)<>0
						REPLACE &??EX WITH IIF(??>1981.AND.??<1990,0,;
						IIF(??=>1990.AND.??<2000,AREX(2)+(AREX(3)-AREX(2))/10*(??-1990),;
						IIF(??=>2000.AND.??<2005,AREX(3)+(AREX(4)-AREX(3))/5*(??-2000),;
						IIF(??=>2005.AND.??<=2010,AREX(4)+(AREX(5)-AREX(4))/5*(??-2005),0)))) all 
				 */
				else if(AREX[1] == 0.0 && AREX[2]!=0.0 && AREX[3]!=0.0 && AREX[4]!=0.0 && AREX[5]!=0.0){
				    for(X = YearRange; X >= 0 ; X--){
						if( true ){
						    if(X>=0 && X<1990-1981){
						    	cxex[X] = 0;
						    }
						    else if(X>=1990-1981 && X<2000-1981){
						    	cxex[X] = AREX[2]+(AREX[3]-AREX[2])/10*(X-1990+1981);
						    }
						    else if(X>=2000-1981 && X<2005-1981){
						    	cxex[X] = AREX[3]+(AREX[4]-AREX[3])/5*(X-2000+1981);
						    }
						    else if(X>=2005-1981 && X<=2010-1981){
						    	cxex[X] = AREX[4]+(AREX[5]-AREX[4])/5*(X-2005+1981);
						    }
						    else 
						    	cxex[X] = 0;
						}
				    }
				}
				/*
				 ???????IIF???
				  CASE AREX(1)<>0.AND.AREX(2)<>0.AND.AREX(3)<>0.AND.AREX(4)=0.AND.AREX(5)<>0
						REPLACE &??EX WITH IIF(??>=1981.AND.??<1990,AREX(1)+(AREX(2)-AREX(1))/9*(??-1981),;
						IIF(??=>1990.AND.??<2000,AREX(2)+(AREX(3)-AREX(2))/10*(??-1990),;
						IIF(??=>2000.AND.??<=2010,AREX(3)+(AREX(5)-AREX(3))/10*(??-2000),0))) all 
				 */
				else if( AREX[1]!=0.0 && AREX[2]!=0.0 && AREX[3]!=0.0 && AREX[4]==0.0 && AREX[5]!=0.0){
				    for(X = YearRange; X >= 0 ; X--){
						if( true ){
						    if(X>=1981-1981 && X<1990-1981){
						    	cxex[X] = AREX[1]+(AREX[2]-AREX[1])/9*(X+1981-1981);
						    }
						    else if(X>=1990-1981 && X<2000-1981){
						    	cxex[X] = AREX[2]+(AREX[3]-AREX[2])/10*(X-1990+1981);
						    }
						    else if(X>=2000-1981 && X<=2010-1981){
						    	cxex[X] = AREX[3]+(AREX[5]-AREX[3])/10*(X-2000+1981);
						    }
						    else 
						    	cxex[X] = 0;
						}
				    }
				}
		
				/*
					CASE AREX(1)=0.AND.AREX(2)=0.AND.AREX(3)<>0.AND.AREX(4)<>0.AND.AREX(5)<>0
						REPLACE &??EX WITH IIF(??>1981.AND.??<1990,0,;
						IIF(??=>1990.AND.??<2000,0,;
						IIF(??=>2000.AND.??<2005,AREX(3)+(AREX(4)-AREX(3))/5*(??-2000),;
						IIF(??=>2005.AND.??<=2010,AREX(4)+(AREX(5)-AREX(4))/5*(??-2005),0)))) all 
				 */
				else if( AREX[1]==0.0 && AREX[2]==0.0 && AREX[3]!=0.0 && AREX[4]!=0.0 && AREX[5]!=0.0){
				    for(X = YearRange; X >= 0 ; X--){
				    	if( true ){
						    if(X>0 && X<1990-1981){          //TODO:???X>0???X>=0,???????????????
						    	cxex[X] = 0.0;
						    }
						    else if(X>=1990-1981 && X<2000-1981){
							cxex[X] = 0.0;
						    }
						    else if(X>=2000-1981 && X<2005-1981){
							cxex[X] = AREX[3]+(AREX[4]-AREX[3])/5*(X-2000+1981);
						    }
						    else if(X>=2005-1981 && X<=2010-1981){
							cxex[X] = AREX[4]+(AREX[5]-AREX[4])/5*(X-2005+1981);
						    }
						    else 
							cxex[X] = 0;
						}
				    }
				}
				/*
				  CASE AREX(1)=0.AND.AREX(2)=0.AND.AREX(3)<>0.AND.AREX(4)=0.AND.AREX(5)<>0
						REPLACE &??EX WITH IIF(??>1981.AND.??<1990,0,;
						IIF(??=>1990.AND.??<2000,0,;
						IIF(??=>2000.AND.??<=2010,AREX(3)+(AREX(5)-AREX(3))/10*(??-2000),0))) all 
				 */
				else if( AREX[1]==0.0 && AREX[2]==0.0 && AREX[3]!=0.0 && AREX[4]==0.0 && AREX[5]!=0.0){
				    for(X = YearRange; X >= 0 ; X--){
					if( true ){
					    if(X>1981-1981 && X<1990-1981){                  //TODO:??????,??>0
						cxex[X] = 0.0;
					    }
					    else if(X>=1990-1981 && X<2000-1981){
						cxex[X] = 0.0;
					    }
					    else if(X>=2000-1981 && X<=2010-1981){
						cxex[X] = AREX[3]+(AREX[5]-AREX[3])/10*(X-2000+1981);
					    }
					    else 
						cxex[X] = 0;
					}
				    }
				}
				/*
				  CASE AREX(1)=0.AND.AREX(2)<>0.AND.AREX(3)<>0.AND.AREX(4)=0.AND.AREX(5)<>0
						REPLACE &??EX WITH IIF(??>1981.AND.??<1990,0,;
						IIF(??=>1990.AND.??<2000,AREX(2)+(AREX(3)-AREX(2))/10*(??-1990),;
						IIF(??=>2000.AND.??<=2010,AREX(3)+(AREX(5)-AREX(3))/10*(??-2000),0))) all 
				 */
				else if( AREX[1]==0.0 && AREX[2]!=0.0 && AREX[3]!=0.0 && AREX[4]==0.0 && AREX[5]!=0.0){
				    for(X = YearRange; X >= 0 ; X--){
					if( true ){
					    if(X>0 && X<1990-1981){
						cxex[X] = 0;
					    }
					    else if(X>=1990-1981 && X<2000-1981){
						cxex[X] = AREX[2]+(AREX[3]-AREX[2])/10*(X-1990+1981);
					    }
					    else if(X>=2000-1981 && X<=2010-1981){
						cxex[X] = AREX[3]+(AREX[5]-AREX[3])/10*(X-2000+1981);
					    }
					    else 
						cxex[X] = 0;
					}
				    }
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				//System.out.println(cxa+" "+xba);
//				for(int i=0;i<=YearRange;i++){
//					System.out.println(cxex[i]);
//				}
//				for(int i=0;i<=YearRange;i++){
//				System.out.println("Year "+(i+1981)+":"+ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.all)[i]+" "+
//									ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Female)[i]+" "+
//									ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.all)[i]+" "+
//									ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Female)[i]+" "+
//									ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.all)[i]+" "+
//									ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Female)[i]);
//				}
//				if(xba.equals(XBAll.Female))
//					return;
	
				/*
			     *****?????*****
				  COUNT TO N FOR &??EX<>0
				  LOCATE FOR &??EX<>0
				  ND0=??
				  CALCULATE AVG(LOG(??-ND0+1)),AVG(&??EX) TO AVX,AVY FOR &??EX<>0
				  SUM LOG(??-ND0+1)*(&??EX),LOG(??-ND0+1),(&??EX),LOG(??-ND0+1)^2 TO SXY,SX,SY,sx2 FOR  &??EX<>0
				  b=(n*sxy-sx*sy)/(n*sx2-sx^2)	&& y=a+bx			a=(N?xy-?x?y)/(N?x^2-(?x)^2)
				  a=AVY-b*AVX						&& b=y(??)-a*x???? 
				  LOCATE FOR ??=2010
				  ??B=AREX[5)/(A+B*LOG(??-ND0+1))
				  REPLACE &??EX WITH (A+B*LOG(??-ND0+1))*??B FOR ??>2010
				*/
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				int ND0 = 0;
				N=0;
				for(int i=YearRange; i>=0; i--){
				    if(cxex[i]!=0){
				    	N++;
				    	ND0=i+1981;//ND0???,??1981,????0???
				    }
				}
				    
				double SXY=0,SX=0,SY=0,SX2=0;
				for(X = YearRange; X >= 0 ; X--){
				    if( cxex[X]!=0.0 ){
						SXY += Math.log(X+1981-ND0+1) * ( cxex[X] );
						SX += Math.log(X+1981-ND0+1);
						SY += ( cxex[X] );
						SX2 += Math.pow(Math.log(X+1981-ND0+1),2.0);
				    }
				}
				double AVX=SX/N;
				double AVY=SY/N;
				double b = ( N * SXY - SX * SY ) / ( N * SX2 - Math.pow(SX,2.0) );
				double A = AVY - b * AVX;
				double CorrectB = AREX[5] / (A + b * Math.log(2010-ND0+1) );
				for(X = YearRange; X >= 0 ; X--){
				    if( X > 2010-1981 ){
					cxex[X] = ( A + b * Math.log(X+1981-ND0+1) ) * CorrectB;
				    }
				}
				
				/*********test??**********/
//				for(int i=0;i<=YearRange;i++){
//				System.out.println("Year "+(i+1981)+":"+ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.all)[i]+" "+
//									ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Female)[i]+" "+
//									ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.all)[i]+" "+
//									ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Female)[i]+" "+
//									ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.all)[i]+" "+
//									ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Female)[i]);
//				}
//				if(cxa.equals(CXAll.Chengzhen) && xba.equals(XBAll.all))
//					return;
	    	}
	    }
		/////////////end of translating, by Foxpro2Java Translator///////////////
			/*
		     *****????*****
		     COPY TO ARRAY ARCX FIELDS ??t,??m,??f,??t,??m,??f,??t,??m,??f
		     REPLACE ??F WITH MAX(ARCX(RECNO(),2),ARCX(RECNO(),3)),;
		     ??M WITH MIN(ARCX(RECNO(),2),ARCX(RECNO(),3)),;
		     ??T WITH MAX(ARCX(RECNO(),4),ARCX(RECNO(),7)),;
		     ??T WITH MIN(ARCX(RECNO(),4),ARCX(RECNO(),7)),;
		     ??M WITH MAX(ARCX(RECNO(),5),ARCX(RECNO(),8)),;
		     ??M WITH MIN(ARCX(RECNO(),5),ARCX(RECNO(),8)),;
		     ??F WITH MAX(ARCX(RECNO(),6),ARCX(RECNO(),9)),;
		     ??F WITH MIN(ARCX(RECNO(),6),ARCX(RECNO(),9)) FOR ??>2010
			*/
		//////////////translated by Foxpro2Java Translator successfully:///////////////
	    double[][] ARCX = new double[YearRange+1][10];
		for(X = YearRange; X >= 0 ; X--){
		    if( true ){
			ARCX[X][1] = ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.all)[X];
			ARCX[X][2] = ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Male)[X];
			ARCX[X][3] = ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Female)[X];
			ARCX[X][4] = ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.all)[X];
			ARCX[X][5] = ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Male)[X];
			ARCX[X][6] = ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Female)[X];
			ARCX[X][7] = ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.all)[X];
			ARCX[X][8] = ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Male)[X];
			ARCX[X][9] = ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Female)[X];
		    }
		}

		for(X = YearRange; X >= 0 ; X--){
		    if( X > 2010-1981 ){
		    	ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Female)[X] = Math.max(ARCX[X][2],ARCX[X][3]);
		    	ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Male)[X] = Math.min(ARCX[X][2],ARCX[X][3]);
		    	ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.all)[X] = Math.max(ARCX[X][4],ARCX[X][7]);
		    	ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.all)[X] = Math.min(ARCX[X][4],ARCX[X][7]);
		    	ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Male)[X] = Math.max(ARCX[X][5],ARCX[X][8]);
		    	ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Male)[X] = Math.min(ARCX[X][5],ARCX[X][8]);
		    	ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Female)[X] = Math.max(ARCX[X][6],ARCX[X][9]);
		    	ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Female)[X] = Math.min(ARCX[X][6],ARCX[X][9]);
			 }
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
//		/*************test??*****************/
//		for(int i=0;i<=YearRange;i++){
//			System.out.println("Year "+(i+1981)+":"+ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.all)[i]+" "+
//								ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Chengxiang).get(XBAll.Female)[i]+" "+
//								ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.all)[i]+" "+
//								ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Chengzhen).get(XBAll.Female)[i]+" "+
//								ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.all)[i]+" "+
//								ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Male)[i]+" "+ProvinceChazhi.get(CXAll.Nongcun).get(XBAll.Female)[i]);
//		}
	}
	@Override
	public void setParam(String params, int type) throws MethodException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void calculate(IDAO m, HashMap<String, Object> globals)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Message checkDatabase(IDAO m, HashMap<String, Object> globals)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
