package test;

//import static prclqz.core.enumLib.Couple.*;
import static prclqz.core.enumLib.HunpeiField.*;
import static prclqz.core.enumLib.XB.*;
import static prclqz.core.enumLib.CX.*;
import static prclqz.core.enumLib.NY.*;
import static prclqz.core.Const.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import prclqz.DAO.IDAO;
import prclqz.DAO.MyDAOImpl;
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.MainTaskBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.Message;
import prclqz.core.StringList;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.Policy;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;
import prclqz.methods.IMethod;
import prclqz.methods.MethodException;
import test.EnumTools;


/**
 * ??????????????(?????)
 * @author prclqz@zju.edu.cn
 *
 */
public class MSingleAndNotSingleForTest implements IMethod
{
	//TODO ??lz?babiesbornbean
	public static void main(String[] argv)throws Exception{
		HashMap<String,Object>globals = new HashMap<String,Object>();
		IDAO m = new MyDAOImpl();
		int dqx=11;
		int year=2010;
		/***??tempvar***/
		TempVariablesBean tempVar = test.parse(year, dqx-10);
		/*??BabiesBorn*/
		BabiesBornBean babiesBorn=new BabiesBornBean(0, 0, 0, 0, 0, 0, 0, 0, 0,59209,27193, 19209, 24946, 0,0,0,0,0,0,0,0);
		/**??BabiesBorn End*/
		tempVar.setProvince(dqx);
		tempVar.setYear(year);
		tempVar.setBabiesBorn(babiesBorn);
		/***??CoupleAndChildrenOfCXNY ????***/
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = new EnumMap<CX, Map<NY,Map<Year,Map<Couple,Double>>>>(CX.class);
		for(CX cx:CX.values()){
			CoupleAndChildrenOfCXNY.put(cx, new EnumMap<NY, Map<Year,Map<Couple,Double>>>(NY.class));
			for(NY ny:NY.values()){
				CoupleAndChildrenOfCXNY.get(cx).put(ny, new EnumMap<Year, Map<Couple,Double>>(Year.class));
				CoupleAndChildrenOfCXNY.get(cx).get(ny).put(Year.getYear(year), new EnumMap<Couple, Double>(Couple.class));
			}
		}
		/***??predictVarMap?????***/
		HashMap<String,Object> predictVarMap = new HashMap<String,Object>();
		Map<CX, Map<NY, Map<HunpeiField, double[]>>> ziNv = EnumTools
				.creatCXNYZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx-10);
		Map<CX,Map<XB,double[]>> deathRate = EnumTools.createDeathRate(year, dqx-10, "d:\\prclqz\\??????????????\\??\\");
		
		predictVarMap.put("HunpeiOfCXNY" + dqx, ziNv);
		predictVarMap.put("DeathRate" + dqx, deathRate);
		predictVarMap.put ( "CoupleAndChildrenOfCXNY"+dqx ,CoupleAndChildrenOfCXNY);
		/***??xbbMap***/
		MainTaskBean task;
		task = m.getTask("FirstTask");
		HashMap<String,BornXbbBean> xbbMap = m.getARBornXbbBeans(task);
		/***??strValues***/
		StringList strValues = new StringList();
		/***??globals***/
		globals.put("strValues", strValues);
		globals.put("bornXbbBeanMap",xbbMap);
		globals.put("predictVarMap", predictVarMap);
		globals.put("tempVarBean", tempVar);
		/***????***/
		MSingleAndNotSingleForTest hehe = new MSingleAndNotSingleForTest();
		hehe.calculate(m, globals);
	}
	@Override
	public void calculate ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		//get variables from globals
		TempVariablesBean tempVar = (TempVariablesBean) globals.get("tempVarBean");
		HashMap<String,Object> predictVarMap = (HashMap<String, Object>) globals.get("predictVarMap");
		HashMap<String,BornXbbBean> xbbMap = (HashMap<String, BornXbbBean>) globals.get("bornXbbBeanMap");
		StringList strValues = ( StringList ) globals.get ( "strValues" );
		
		//get running status
		int dqx = tempVar.getProvince();
		int year = tempVar.getYear();
		BornXbbBean xbb = xbbMap.get(""+dqx);
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//?? ???????
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY"+dqx );//provincMigMap.get(""+dqx);
		//??????
		Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
		
		//????????
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
		for ( CX cx : CX.values ( ) )
			for ( NY ny : NY.values ( ) )
			{
				System.out.println("/**********"+cx.getChinese()+ny.getChinese()+"**************/");
				Map < HunpeiField , double [ ] > zn = ziNv.get ( cx ).get ( ny );
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						zn.get ( SingleToNot ) [ X ] = 0.0;
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				
				double SB1 = 0.0;
				double NB2 = zn.get ( N ) [ 0 ];
				
				double [ ][ ] ARFR = new double [ 4 ] [ 36 ];
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X >= 15.0 && X <= 49.0 )
					{
						ARFR [ 1 ] [ X-14 ] = X;
						ARFR [ 2 ] [ X-14 ] = zn.get ( FR1 ) [ X ];
						ARFR [ 3 ] [ X-14 ] = zn.get ( FR2 ) [ X ];
					}
				}
				double MAFR1 = 0;
				double MAFR2 = 0;
				int MAFR1X =0 ,MAFR2X =0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true && zn.get ( FR1 ) [ X ] > MAFR1 ){
						MAFR1 = zn.get ( FR1 ) [ X ];
						MAFR1X = X;
					}
					if ( true && zn.get ( FR2 ) [ X ] > MAFR2 ){
						MAFR2 = zn.get ( FR2 ) [ X ];
						MAFR2X = X;
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				double [ ] ARFR1 = new double [ MAX_AGE ];
				/****by DS:??X??0??????,?MAX_AGE????ARFR1???????*****/
				int TMP=1;
				for(X=0;X<MAX_AGE;X++)
				{
					if ( X <= MAFR2X && zn.get ( FR1 ) [ X ] != 0.0 )
					{
						ARFR1 [ TMP++ ] = zn.get ( FR1 ) [ X ];//??: ARFR1[X]=...
					}
				}
				/*******end***********************************************/
				/////////////end of translating, by Foxpro2Java Translator///////////////

				//TODO count to sx1
				/******by DS*********/
				int SX1=0;
				for(X=MAX_AGE-1;X>=0;X--){
					if(X<=MAFR2X && zn.get(FR1)[X]!=0.0)
						SX1++;
				}
				/********end**********/
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				double SFR1 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X <= MAFR2X && zn.get ( FR1 ) [ X ] != 0.0 )
					{
						SFR1 += zn.get ( FR1 ) [ X ];
					}
				}
				int X2 , X1;
				double B2X , B2S , SB2;
				for ( X2 = 1;X2 <= 35;X2 ++ )
				{
					B2X = ARFR [ 1 ] [ X2 ];
					B2S = NB2 * ARFR [ 3 ] [ X2 ];
					SB2 = 0.0;
					if ( B2S != 0.0 )
					{
						SB1 = 0.0;
						if ( B2X >= 25.0 )
						{
							for ( X1 = 1;X1 <= X2 - 1;X1 ++ )
							{
								SB1 = SB1 + ARFR [ 2 ] [ X1 ];
							}
							for ( X1 = 1;X1 <= X2 - 1;X1 ++ )
							{
								for ( X = MAX_AGE - 1;X >= 0;X -- )
								{
								//LOCATE FOR X=ARFR(X2,1)-ARFR(X1,1) TODO ?????
//									if ( true )
									if( X == (ARFR[1][X2]-ARFR[1][X1]))
									{
										zn.get ( SingleToNot ) [ X ] = zn.get ( SingleToNot ) [ X ]	+ B2S * ARFR [ 2 ] [ X1 ] / SB1;
									}
								}
								SB2 = SB2 + B2S * ARFR [ 2 ] [ X1 ] / SB1;
								
							}
						}
						else if ( B2X < 25.0 )
						{
							for ( X1 = 1;X1 <= X2 - 1.0;X1 ++ )
							{
								SB1 = SB1 + ARFR1 [ X1 ]; // TODO ????14??
							}
							for ( X1 = 1;X1 <= X2 - 1.0;X1 ++ )
							{
								for ( X = MAX_AGE - 1;X >= 0;X -- )
								{
									//TODO LOCATE FOR X=ARFR(X2,1)-ARFR(X1,1)
//									if ( true )
									if( X == (ARFR[1][X2]-ARFR[1][X1]))
									{
										zn.get ( SingleToNot ) [ X ] = zn.get ( SingleToNot ) [ X ]
												+ B2S * ARFR1 [ X1 ] / SB1;
									}
								}
								SB2 = SB2 + B2S * ARFR1 [ X1 ] / SFR1;
							}
						}
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				
				//SUM ??? TO S??? ?????????????
				double SSingleToNot=0;
				for(X = MAX_AGE-1;X>=0;X--){
					SSingleToNot+=zn.get(SingleToNot)[X];
				}
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X > 0.0 )
					{
						zn.get ( NF ) [ X ] = zn.get ( NF ) [ X ]
								+ zn.get ( SingleToNot ) [ X ] * 1.0
								/ ( 1.0 + xbb.getXbb ( ) );
						zn.get ( NM ) [ X ] = zn.get ( NM ) [ X ]
								+ zn.get ( SingleToNot ) [ X ] * xbb.getXbb ( )
								/ ( 1.0 + xbb.getXbb ( ) );
					}
					if ( X > 0.0 )
					{
						zn.get ( DF ) [ X ] = zn.get ( DF ) [ X ]
								- zn.get ( SingleToNot ) [ X ] * 1.0
								/ ( 1.0 + xbb.getXbb ( ) );
						zn.get ( DM ) [ X ] = zn.get ( DM ) [ X ]
								- zn.get ( SingleToNot ) [ X ] * xbb.getXbb ( )
								/ ( 1.0 + xbb.getXbb ( ) );
					}
				}

				// TODO COUNT TO S0 FOR NF<0.OR.NM<0.OR.DM<0.OR.DF<0
				int S0 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( zn.get ( NF ) [ X ] < 0.0 || zn.get ( NM ) [ X ] < 0.0
							|| zn.get ( DM ) [ X ] < 0.0
							|| zn.get ( DF ) [ X ] < 0.0 )
					{
						S0 ++ ;
					}
				}

				double SD1 = 0;
				double SN1 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X > 0.0 )
					{
						SD1 += zn.get ( DF ) [ X ] + zn.get ( DM ) [ X ];
						SN1 += zn.get ( NF ) [ X ] + zn.get ( NM ) [ X ];
					}
				}
				double DI = 1.0;
				/////////////end of translating, by Foxpro2Java Translator///////////////
				
				//TODO DO WHILE S0>0
				while( S0 >0 )
				{
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					double [ ][ ] ARS0 = new double [ 5 ] [ MAX_AGE ];//DM DF NM NF
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							ARS0 [ 0 ] [ X ] = zn.get ( DM ) [ X ];
							ARS0 [ 1 ] [ X ] = zn.get ( DF ) [ X ];
							ARS0 [ 2 ] [ X ] = zn.get ( NM ) [ X ];
							ARS0 [ 3 ] [ X ] = zn.get ( NF ) [ X ];
						}
					}
					if ( S0 == 1.0 )
					{
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( X > 1.0 && X + 1.0 < MAX_AGE
									&& ARS0 [ 1 ] [ X ] < 0.0 )
							{
								zn.get ( DM ) [ X ] = ( ARS0 [ 1 ] [ X - 1 ]
										+ ARS0 [ 1 ] [ X ] + ARS0 [ 1 ] [ X + 1 ] ) / 3.0;
							}
							if ( X > 1.0 && X + 1.0 < MAX_AGE
									&& ARS0 [ 2 ] [ X ] < 0.0 )
							{
								zn.get ( DF ) [ X ] = ( ARS0 [ 2 ] [ X - 1 ]
										+ ARS0 [ 2 ] [ X ] + ARS0 [ 2 ] [ X + 1 ] ) / 3.0;
							}
							if ( X > 1.0 && X + 1.0 < MAX_AGE
									&& ARS0 [ 3 ] [ X ] < 0.0 )
							{
								zn.get ( NM ) [ X ] = ( ARS0 [ 3 ] [ X - 1 ]
										+ ARS0 [ 3 ] [ X ] + ARS0 [ 3 ] [ X + 1 ] ) / 3.0;
							}
							if ( X > 1.0 && X + 1.0 < MAX_AGE
									&& ARS0 [ 4 ] [ X ] < 0.0 )
							{
								zn.get ( NF ) [ X ] = ( ARS0 [ 4 ] [ X - 1 ]
										+ ARS0 [ 4 ] [ X ] + ARS0 [ 4 ] [ X + 1 ] ) / 3.0;
							}
						}
					} else
					{
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( X > 2.0 && X + 2.0 < MAX_AGE
									&& ARS0 [ 1 ] [ X ] < 0.0 )
							{
								zn.get ( DM ) [ X ] = ( ARS0 [ 1 ] [ X - 2 ]
										+ ARS0 [ 1 ] [ X - 1 ]
										+ ARS0 [ 1 ] [ X ]
										+ ARS0 [ 1 ] [ X + 1 ] + ARS0 [ 1 ] [ X + 2 ] ) / 5.0;
							}
							if ( X > 2.0 && X + 2.0 < MAX_AGE
									&& ARS0 [ 2 ] [ X ] < 0.0 )
							{
								zn.get ( DF ) [ X ] = ( ARS0 [ 2 ] [ X - 2 ]
										+ ARS0 [ 2 ] [ X - 1 ]
										+ ARS0 [ 2 ] [ X ]
										+ ARS0 [ 2 ] [ X + 1 ] + ARS0 [ 2 ] [ X + 2 ] ) / 5.0;
							}
							if ( X > 2.0 && X + 2.0 < MAX_AGE
									&& ARS0 [ 3 ] [ X ] < 0.0 )
							{
								zn.get ( NM ) [ X ] = ( ARS0 [ 3 ] [ X - 2 ]
										+ ARS0 [ 3 ] [ X - 1 ]
										+ ARS0 [ 3 ] [ X ]
										+ ARS0 [ 3 ] [ X + 1 ] + ARS0 [ 3 ] [ X + 2 ] ) / 5.0;
							}
							if ( X > 2.0 && X + 2.0 < MAX_AGE
									&& ARS0 [ 4 ] [ X ] < 0.0 )
							{
								zn.get ( NF ) [ X ] = ( ARS0 [ 4 ] [ X - 2 ]
										+ ARS0 [ 4 ] [ X - 1 ]
										+ ARS0 [ 4 ] [ X ]
										+ ARS0 [ 4 ] [ X + 1 ] + ARS0 [ 4 ] [ X + 2 ] ) / 5.0;
							}
						}
					}
					// TODO COUNT TO S0 FOR DM<0.OR.DF<0.OR.NF<0.OR.NM<0
					S0 = 0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( zn.get ( NF ) [ X ] < 0.0
								|| zn.get ( NM ) [ X ] < 0.0
								|| zn.get ( DM ) [ X ] < 0.0
								|| zn.get ( DF ) [ X ] < 0.0 )
						{
							S0 ++ ;
						}
					}
					DI = DI + 1.0;
					if ( DI > 10.0 )
					{
						int MF;
						HunpeiField FIE1 = DM;
						double SMI , SMA;
						for ( MF = 1;MF <= 4.0;MF ++ )
						{
							// TODO
							// FIE1=IIF(MF=1,'DM',IIF(MF=2,'DF',IIF(MF=3,'NM','NF')))
							switch ( MF )
							{
							case 1 :
								FIE1 = DM;
							case 2 :
								FIE1 = DF;
							case 3 :
								FIE1 = NM;
							case 4 :
								FIE1 = DF;
							}
							SMI = 0;
							for ( X = MAX_AGE - 1;X >= 0;X -- )
							{
								if ( zn.get ( FIE1 ) [ X ] < 0.0 && X > 1.0 )
								{
									SMI += zn.get ( FIE1 ) [ X ];
								}
							}
							SMA = 0;
							for ( X = MAX_AGE - 1;X >= 0;X -- )
							{
								if ( zn.get ( FIE1 ) [ X ] > 0.0 && X > 1.0 )
								{
									SMA += zn.get ( FIE1 ) [ X ];
								}
							}
							for ( X = MAX_AGE - 1;X >= 0;X -- )
							{
								if ( zn.get ( FIE1 ) [ X ] > 0.0 && X > 1.0 )
								{
									zn.get ( FIE1 ) [ X ] = zn.get ( FIE1 ) [ X ]
											+ SMI * zn.get ( FIE1 ) [ X ] / SMA;
								}
								if ( zn.get ( FIE1 ) [ X ] < 0.0 && X > 1.0 )
								{
									zn.get ( FIE1 ) [ X ] = 0.0;
								}
							}
						}
						
						
						// TODO COUNT TO S0 FOR DM<0.OR.DF<0.OR.NF<0.OR.NM<0
						S0 = 0;
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( zn.get ( NF ) [ X ] < 0.0
									|| zn.get ( NM ) [ X ] < 0.0
									|| zn.get ( DM ) [ X ] < 0.0
									|| zn.get ( DF ) [ X ] < 0.0 )
							{
								S0 ++ ;
							}
						}

						if ( S0 > 0.0 )
						{
							for ( X = MAX_AGE - 1;X >= 0;X -- )
							{
								if ( zn.get ( DM ) [ X ] < 0.0 )
								{
									zn.get ( NM ) [ X ] = zn.get ( NM ) [ X ]
											+ zn.get ( DM ) [ X ];
									zn.get ( DM ) [ X ] = 0.0;
								}
								if ( zn.get ( DF ) [ X ] < 0.0 )
								{
									zn.get ( NF ) [ X ] = zn.get ( NF ) [ X ]
											+ zn.get ( DF ) [ X ];
									zn.get ( DF ) [ X ] = 0.0;
								}
								if ( zn.get ( NM ) [ X ] < 0.0 )
								{
									zn.get ( DM ) [ X ] = zn.get ( DM ) [ X ]
											+ zn.get ( NM ) [ X ];
									zn.get ( NM ) [ X ] = 0.0;
								}
								if ( zn.get ( NF ) [ X ] < 0.0 )
								{
									zn.get ( DF ) [ X ] = zn.get ( DF ) [ X ]
											+ zn.get ( NF ) [ X ];
									zn.get ( NF ) [ X ] = 0.0;
								}
							}
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////
					
					//TODO ?????????? 
					break;
				}//END WHILE
				
				
				// TODO COUNT TO S0 FOR DM<0.OR.DF<0.OR.NF<0.OR.NM<0
				S0 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( zn.get ( NF ) [ X ] < 0.0
							|| zn.get ( NM ) [ X ] < 0.0
							|| zn.get ( DM ) [ X ] < 0.0
							|| zn.get ( DF ) [ X ] < 0.0 )
					{
						S0 ++ ;
					}
				}
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				double SD2 = 0 , SN2 = 0 , DS = 0 , DMS = 0 , DFS = 0 , NS = 0 , NMS = 0 , NFS = 0 , DDBSS = 0 , DDBMS = 0 , DDBFS = 0 , DS30 = 0 , DMS30 = 0 , DFS30 = 0 , NS30 = 0 , NMS30 = 0 , NFS30 = 0 , DDBSS30 = 0 , DDBMS30 = 0 , DDBFS30 = 0 , DDS = 0 , NMDFS = 0 , NFDMS = 0 , NNS = 0 , duijiSingle = 0 , duijiYi2 = 0 , duijiShuangfei = 0 , shuangfeiYi2 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X > 0.0 )
					{
						SD2 += zn.get ( DF ) [ X ] + zn.get ( DM ) [ X ];
						SN2 += zn.get ( NF ) [ X ] + zn.get ( NM ) [ X ];
					}
				}
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X > 0.0 )
					{
						if ( SD2 != 0.0 )
						{
							zn.get ( DM ) [ X ] = SD1 * zn.get ( DM ) [ X ]
									/ SD2;
						} else
						{
							zn.get ( DM ) [ X ] = 0.0;
						}
						if ( SD2 != 0.0 )
						{
							zn.get ( DF ) [ X ] = SD1 * zn.get ( DF ) [ X ]
									/ SD2;
						} else
						{
							zn.get ( DF ) [ X ] = 0.0;
						}
						if ( SN2 != 0.0 )
						{
							zn.get ( NM ) [ X ] = SN1 * zn.get ( NM ) [ X ]
									/ SN2;
						} else
						{
							zn.get ( NM ) [ X ] = 0.0;
						}
						if ( SN2 != 0.0 )
						{
							zn.get ( NF ) [ X ] = SN1 * zn.get ( NF ) [ X ]
									/ SN2;
						} else
						{
							zn.get ( NF ) [ X ] = 0.0;
						}
					}
					if ( true )
					{
						zn.get ( D ) [ X ] = zn.get ( DM ) [ X ]
								+ zn.get ( DF ) [ X ];
					}
				}
				DS = 0;
				DMS = 0;
				DFS = 0;
				NS = 0;
				NMS = 0;
				NFS = 0;
				DDBSS = 0;
				DDBMS = 0;
				DDBFS = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						DS += zn.get ( D ) [ X ];
						DMS += zn.get ( DM ) [ X ];
						DFS += zn.get ( DF ) [ X ];
						NS += zn.get ( N ) [ X ];
						NMS += zn.get ( NM ) [ X ];
						NFS += zn.get ( NF ) [ X ];
						DDBSS += zn.get ( DDBS ) [ X ];
						DDBMS += zn.get ( DDBM ) [ X ];
						DDBFS += zn.get ( DDBF ) [ X ];
					}
				}
				DS30 = 0;
				DMS30 = 0;
				DFS30 = 0;
				NS30 = 0;
				NMS30 = 0;
				NFS30 = 0;
				DDBSS30 = 0;
				DDBMS30 = 0;
				DDBFS30 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X <= 30.0 )
					{
						DS30 += zn.get ( D ) [ X ];
						DMS30 += zn.get ( DM ) [ X ];
						DFS30 += zn.get ( DF ) [ X ];
						NS30 += zn.get ( N ) [ X ];
						NMS30 += zn.get ( NM ) [ X ];
						NFS30 += zn.get ( NF ) [ X ];
						DDBSS30 += zn.get ( DDBS ) [ X ];
						DDBMS30 += zn.get ( DDBM ) [ X ];
						DDBFS30 += zn.get ( DDBF ) [ X ];
					}
				}
				DDS = 0;
				NMDFS = 0;
				NFDMS = 0;
				NNS = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X >= 15.0 && X <= 49.0 )
					{
						DDS += zn.get ( DD ) [ X ];
						NMDFS += zn.get ( NMDF ) [ X ];
						NFDMS += zn.get ( NFDM ) [ X ];
						NNS += zn.get ( NN ) [ X ];
					}
				}
				duijiSingle = 0;
				duijiYi2 = 0;
				duijiShuangfei = 0;
				shuangfeiYi2 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X >= 20.0 && X <= 49.0 )
					{
						duijiSingle += zn.get ( SingleDJ ) [ X ];
						duijiYi2 += zn.get ( Yi2Single ) [ X ];
						duijiShuangfei += zn.get ( ShuangfeiDJ ) [ X ];
						shuangfeiYi2 += zn.get ( Yi2Shuangfei ) [ X ];
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				double SWNM , SWDDM , SWNF , SWDDF ,SWDM,SWDF;
				if(ny == Nongye){
					//////////////translated by Foxpro2Java Translator successfully:///////////////
//					double SWNM , SWDDM , SWNF , SWDDF;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							zn.get ( HunpeiField.SWDM ) [ X ] = zn.get ( DM ) [ X ]
									* deathRate.get ( Nongchun ).get ( Male ) [ X+1 ];
							zn.get ( HunpeiField.SWDF ) [ X ] = zn.get ( DF ) [ X ]
									* deathRate.get ( Nongchun ).get ( Female ) [ X+1 ];
							zn.get ( SWD ) [ X ] = zn.get ( HunpeiField.SWDM ) [ X ]
									+ zn.get ( HunpeiField.SWDF ) [ X ];
						}
					}
//					zn.get ( SWDM ) [ X ] = 0; ???? TODO!!
					SWNM = 0;
					SWDDM = 0;
					SWDM = SWDF =0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( X >= 15.0 && X < 60.0 )
						{
							SWDM += zn.get ( DM ) [ X ]
									* deathRate.get ( Nongchun ).get ( Male ) [ X+1 ];
							SWNM += zn.get ( NM ) [ X ]
									* deathRate.get ( Nongchun ).get ( Male ) [ X+1 ];
							SWDDM += zn.get ( DDBM ) [ X ]
									* deathRate.get ( Nongchun ).get ( Male ) [ X+1 ];
						}
					}
//					zn.get ( SWDF ) [ X ] = 0;
					SWNF = 0;
					SWDDF = 0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( X >= 15.0 && X < 60.0 )
						{
							SWDF += zn.get ( DF ) [ X ]
									* deathRate.get ( Nongchun ).get ( Female ) [ X+1 ];
							SWNF += zn.get ( NF ) [ X ]
									* deathRate.get ( Nongchun ).get ( Female ) [ X+1 ];
							SWDDF += zn.get ( DDBF ) [ X ]
									* deathRate.get ( Nongchun ).get ( Female ) [ X+1 ];
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////

				}else{
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							/********by DS:foxpro??RECN(),???java????X?,??RECN()??1???,???X+1**/
							zn.get ( HunpeiField.SWDM ) [ X ] = zn.get ( DM ) [ X ]
									* deathRate.get ( Chengshi ).get ( Male ) [ X+1 ];
							zn.get ( HunpeiField.SWDF ) [ X ] = zn.get ( DF ) [ X ]
									* deathRate.get ( Chengshi ).get ( Female ) [ X+1 ];
							/***********end ********************************************************/
							zn.get ( SWD ) [ X ] = zn.get ( HunpeiField.SWDM ) [ X ]
									+ zn.get ( HunpeiField.SWDF ) [ X ];
						}
					}
//					zn.get ( SWDM ) [ X ] = 0;
					SWNM = 0;
					SWDDM = 0;
					SWDM = SWDF =0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( X >= 15.0 && X < 60.0 )
						{
							SWDM += zn.get ( DM ) [ X ]
									* deathRate.get ( Chengshi ).get ( Male ) [ X+1 ];
							SWNM += zn.get ( NM ) [ X ]
									* deathRate.get ( Chengshi ).get ( Male ) [ X+1 ];
							SWDDM += zn.get ( DDBM ) [ X ]
									* deathRate.get ( Chengshi ).get ( Male ) [ X+1 ];
						}
					}
//					zn.get ( SWDF ) [ X ] = 0;
					SWNF = 0;
					SWDDF = 0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( X >= 15.0 && X < 60.0 )
						{
							SWDF += zn.get ( DF ) [ X ]
									* deathRate.get ( Chengshi ).get ( Female ) [ X+1 ];
							SWNF += zn.get ( NF ) [ X ]
									* deathRate.get ( Chengshi ).get ( Female ) [ X+1 ];
							SWDDF += zn.get ( DDBF ) [ X ]
									* deathRate.get ( Chengshi ).get ( Female ) [ X+1 ];
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////
				}
//				System.out.println("SWDM:"+SWDM);
//				System.out.println("SWNM:"+SWNM);
//				System.out.println("SWDDM:"+SWDDM);
//				System.out.println("SWDF:"+SWDF);
//				System.out.println("SWNF:"+SWNF);
//				System.out.println("SWDDF:"+SWDDF);
//				System.out.println("DS:"+DS);
//				System.out.println("DMS:"+DMS);
//				System.out.println("DFS:"+DFS);
//				System.out.println("NS:"+NS);
//				System.out.println("NMS:"+NMS);
//				System.out.println("NFS:"+NFS);
//				System.out.println("DDBSS:"+DDBSS);
//				System.out.println("DDBMS:"+DDBMS);
//				System.out.println("DDBFS:"+DDBFS);
//				
//				System.out.println("DS30:"+DS30);
//				System.out.println("DMS30:"+DMS30);
//				System.out.println("DFS30:"+DFS30);
//				System.out.println("NS30:"+NS30);
//				System.out.println("NMS30:"+NMS30);
//				System.out.println("NFS30:"+NFS30);
//				System.out.println("DDBSS30:"+DDBSS30);
//				System.out.println("DDBMS30:"+DDBMS30);
//				System.out.println("DDBFS30:"+DDBFS30);
//				
//				System.out.println("DDS:"+DDS);
//				System.out.println("NMDFS:"+NMDFS);
//				System.out.println("NFDMS:"+NFDMS);
//				System.out.println("NNS:"+NNS);
//				
//				System.out.println("????:"+duijiSingle);
//				System.out.println("???2:"+duijiYi2);
//				System.out.println("????:"+duijiShuangfei);
//				System.out.println("???2:"+shuangfeiYi2);
//				
//				System.out.println("SD2:"+SD2);
//				System.out.println("SN2:"+SN2);
//				System.out.println("S0:"+S0);
//				
//
//				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						zn.get ( D ) [ X ] = zn.get ( DM ) [ X ]
								+ zn.get ( DF ) [ X ];
						zn.get ( N ) [ X ] = zn.get ( NM ) [ X ]
								+ zn.get ( NF ) [ X ];
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				Map < Year , Map < Couple , Double >> couple = CoupleAndChildrenOfCXNY.get ( cx ).get ( ny );
				
//				couple.get ( Year.getYear ( year ) ).get ( Couple.NyPolicy1 )= 0.0 ;
//				couple.get ( Year.getYear ( year ) ).put ( Couple.NyPolicy1 , 0.0 );
				
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
//				strValues.add ( Policy.getPolicyById ( tempVar.getFeiNongPolicy1 ( ) ).toString ( )+ (tempVar.feiNongTime1 ==0?"":""+tempVar.feiNongTime1))
				couple.get ( Year.getYear ( year ) ).put ( Couple.FnPolicy1, strValues.add ( Policy.getPolicyById ( tempVar.feiNongPolicy1  ).toString ( )+ (tempVar.feiNongTime1 ==0?"":""+tempVar.feiNongTime1)) );
				couple.get ( Year.getYear ( year ) ).put ( Couple.NyPolicy1, strValues.add ( Policy.getPolicyById ( tempVar.nongYePolicy1 ).toString ( )+ (tempVar.nongYeTime1 ==0?"":""+tempVar.nongYeTime1)) );
				couple.get ( Year.getYear ( year ) ).put ( Couple.FnPolicy2, strValues.add ( Policy.getPolicyById ( tempVar.feiNongPolicy2 ).toString ( )+ (tempVar.feiNongTime2 ==0?"":""+tempVar.feiNongTime2)) );
				couple.get ( Year.getYear ( year ) ).put ( Couple.NyPolicy2, strValues.add ( Policy.getPolicyById ( tempVar.nongYePolicy2 ).toString ( )+ (tempVar.nongYeTime2 ==0?"":""+tempVar.nongYeTime2)) );
//				strValues.add ( tempVar.adjustType.equals ( "???????" )? Policy.getPolicyById ( tempVar.feiNongPolicy3 ).toString ( )+tempVar.feiNongTime3:"" )
				couple.get ( Year.getYear ( year ) ).put ( Couple.FnPolicy3, strValues.add ( tempVar.adjustType.equals ( "???????" )? Policy.getPolicyById ( tempVar.feiNongPolicy3 ).toString ( )+tempVar.feiNongTime3:"" ) );
				couple.get ( Year.getYear ( year ) ).put ( Couple.NyPolicy3, strValues.add ( tempVar.adjustType.equals ( "???????" )? Policy.getPolicyById ( tempVar.nongYePolicy3 ).toString ( )+tempVar.nongYeTime3:"" ) );
				
				couple.get ( Year.getYear ( year ) ).put ( Couple.FDBirth,bBornBean.B2);
				couple.get ( Year.getYear ( year ) ).put ( Couple.MDBirth,bBornBean.B3);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DDBirth,couple.get ( Year.getYear ( year ) ).get ( Couple.FDBirth) + couple.get ( Year.getYear ( year ) ).get ( Couple.MDBirth) );
				couple.get ( Year.getYear ( year ) ).put ( Couple.SFBirth,bBornBean.B4);
				couple.get ( Year.getYear ( year ) ).put ( Couple.TFR,bBornBean.A);
				couple.get ( Year.getYear ( year ) ).put ( Couple.SDBirth,bBornBean.DB);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FNShuangD,tempVar.DDFR);
				couple.get ( Year.getYear ( year ) ).put ( Couple.NYShuangD,tempVar.DDFR);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FNFemalD,tempVar.N1D2C);
				couple.get ( Year.getYear ( year ) ).put ( Couple.NYFemalD,tempVar.N1D2N);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FNMaleD,tempVar.N2D1C);
				couple.get ( Year.getYear ( year ) ).put ( Couple.NYMaleD,tempVar.N2D1N);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FNShuangF,tempVar.NNFC);
				couple.get ( Year.getYear ( year ) ).put ( Couple.NYShuangF,tempVar.NNFN);
				couple.get ( Year.getYear ( year ) ).put ( Couple.D,DS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.N,NS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DM,DMS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DF,DFS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.NM,NMS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.NF,NFS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou,DDBSS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DDBM,DDBMS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DDBF,DDBFS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Du30,DS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DuM30,DMS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DuF30,DFS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FeiDu30,NS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FeiDuM30,NMS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FeiDuF30,NFS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou30,DDBSS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou30M,DDBMS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou30F,DDBFS30);
				couple.get ( Year.getYear ( year ) ).put ( Couple.SDCouple,DDS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FDCouple,NMDFS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.MDCouple,NFDMS);
				couple.get ( Year.getYear ( year ) ).put ( Couple.SingleDJ,duijiSingle);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Yi2Single,duijiYi2);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DDCouple,couple.get ( Year.getYear ( year ) ).get ( Couple.FDCouple) + couple.get ( Year.getYear ( year ) ).get ( Couple.MDCouple) + couple.get ( Year.getYear ( year ) ).get ( Couple.SingleDJ) + couple.get ( Year.getYear ( year ) ).get ( Couple.Yi2Single));
				couple.get ( Year.getYear ( year ) ).put ( Couple.SFDJ,duijiShuangfei);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Yi2SF,shuangfeiYi2);
				couple.get ( Year.getYear ( year ) ).put ( Couple.SFCouple,NNS + duijiShuangfei + shuangfeiYi2);
				couple.get ( Year.getYear ( year ) ).put ( Couple.CoupleS,couple.get ( Year.getYear ( year ) ).get ( Couple.SDCouple) + couple.get ( Year.getYear ( year ) ).get ( Couple.DDCouple) + couple.get ( Year.getYear ( year ) ).get ( Couple.SFCouple));
				couple.get ( Year.getYear ( year ) ).put ( Couple.DuM_SW,SWDM);
				couple.get ( Year.getYear ( year ) ).put ( Couple.DuF_SW,SWDF);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FeiduM_SW,SWNM);
				couple.get ( Year.getYear ( year ) ).put ( Couple.FeiduF_SW,SWNF);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou_SWM,SWDDM);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou_SWF,SWDDF);
				couple.get ( Year.getYear ( year ) ).put ( Couple.Du_SW,couple.get ( Year.getYear ( year ) ).get ( Couple.DuM_SW) + couple.get ( Year.getYear ( year ) ).get ( Couple.DuF_SW));
				couple.get ( Year.getYear ( year ) ).put ( Couple.Feidu_SW,couple.get ( Year.getYear ( year ) ).get ( Couple.FeiduM_SW) + couple.get ( Year.getYear ( year ) ).get ( Couple.FeiduF_SW));
				couple.get ( Year.getYear ( year ) ).put ( Couple.Ke2Hou_SW,couple.get ( Year.getYear ( year ) ).get ( Couple.Ke2Hou_SWM) + couple.get ( Year.getYear ( year ) ).get ( Couple.Ke2Hou_SWF));
				
				/////////////end of translating, by Foxpro2Java Translator///////////////
				
			}//END of CX-NY LOOP
		/***********test??***************/
		//EnumTools.outputCXNYZinv(ziNv, year, dqx, "d:\\prclqz\\??????????????");
		//EnumTools.outputCoupleAndChildrenOfCXNY(CoupleAndChildrenOfCXNY,year,dqx,"d:\\prclqz\\??????????????");
		System.out.println("End");
		/**********test end**************/
	}

	@Override
	public Message checkDatabase ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		return null;
	}

	@Override
	public void setParam ( String params , int type ) throws MethodException
	{
		
	}

}

