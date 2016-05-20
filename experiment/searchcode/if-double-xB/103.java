package prclqz.methods;


import java.util.Map;

import static prclqz.core.enumLib.SYSFMSField.*;
//import static prclqz.core.enumLib.Couple.*;
import static prclqz.core.enumLib.HunpeiField.*;
import static prclqz.core.enumLib.XB.*;
import static prclqz.core.enumLib.CX.*;
import static prclqz.core.enumLib.NY.*;
import static prclqz.core.Const.*;

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
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.Policy;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

import prclqz.lib.EnumMapTool;
import prclqz.parambeans.ParamBean3;
import test.EnumTools;
/**
 * ?????????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MEstimateOfYihaiAndTefuParent implements IMethod
{
	public static void main(String[] args) throws Exception {
		HashMap<String, Object> globals = new HashMap<String, Object>();
		IDAO m = new MyDAOImpl();

		
		MainTaskBean task;
		task = m.getTask(args[0]);
		
		HashMap<String, BornXbbBean> xbbMap = m.getARBornXbbBeans(task);

		
		TempVariablesBean tempVar = new TempVariablesBean(0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0);

		HashMap<String, Object> predictVarMap = new HashMap<String, Object>();
		globals.put("predictVarMap", predictVarMap);
		int dqx = 10;
		int year = 2010;
		// //////////setup///////////////////
		tempVar.setYear(year);
		tempVar.setProvince(dqx);
		tempVar.teFuFaCN = EnumMapTool.createSingleChild ( );
		tempVar.teFuMaCN = EnumMapTool.createSingleChild ( );
		Map<CX, Map<NY, Map<HunpeiField, double[]>>> ziNv = EnumTools
				.creatCXNYZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
		Map<CX, Map<HunpeiField, double[]>> ziNvOfCX = EnumTools
				.creatCXZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
		Map<NY, Map<HunpeiField, double[]>> ziNvOfNY = EnumTools
				.creatNYFNZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
		Map<HunpeiField, double[]> ziNvOfAll = EnumTools.creatAllZiNvFromFile(
				MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put("HunpeiOfCXNY" + dqx, ziNv);
		predictVarMap.put("HunpeiOfCX" + dqx, ziNvOfCX);
		predictVarMap.put("HunpeiOfNY" + dqx, ziNvOfNY);
		predictVarMap.put("HunpeiOfAll" + dqx, ziNvOfAll);
		
//		Map<Babies,double[]> PolicyBabies = EnumTools.createPolicyBabies(MAX_AGE, tempVar.getYear(), dqx);
//		predictVarMap.put("PolicyBabies" + dqx, PolicyBabies);
		

		//????????  ???
		//StringList strValues = ( StringList ) globals.get ( "strValues" );
//		BornXbbBean xbb = xbbMap.get(""+dqx);
//		xbb.setDiDai();
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = EnumTools.createCXNYYearXBDoubleArrMapFromFile2(MAX_AGE, tempVar.getYear(), dqx);
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = EnumTools.createCXNYYearXBDoubleArrMapFromFile3(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuCXNY"+dqx, SonDiePopulationPredictOfTeFuCXNY );
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx, SonDiePopulationPredictOfYiHaiCXNY);
		double[][] husbandRate = EnumTools.createHusbandRate(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put("HusbandRate", husbandRate);
		
		ParamBean3 envParm = new ParamBean3();
		envParm.setBegin(2010);
		globals.put("SetUpEnvParamBean", envParm);
		
		BabiesBornBean bBornBean = new BabiesBornBean(0, 0, 0,
				0, 0, 0, 0,
				0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0);
		//tempVar.singleSonFaCN = EnumTools.create;
		
		bBornBean.singleChildCN.get(CX.Nongchun).put(NY.Nongye, 147829.480260);
		bBornBean.singleChildCN.get(CX.Nongchun).put(NY.Feinong, 8891.149695);
		bBornBean.singleChildCN.get(CX.Chengshi).put(NY.Nongye, 214251.946999);
		bBornBean.singleChildCN.get(CX.Chengshi).put(NY.Feinong, 194789.570908);

		
		tempVar.setBabiesBorn(bBornBean);
		globals.put("tempVarBean", tempVar);	
		globals.put("bornXbbBeanMap", xbbMap);
		MEstimateOfYihaiAndTefuParent hehe = new MEstimateOfYihaiAndTefuParent();
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
//		String diDai = xbb.getDiDai();
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
//		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
		//??
		//????????
		Map<Babies,double[]> policyBabies = (Map<Babies, double[]>) predictVarMap.get("PolicyBabies"+dqx);
		//?? ???????
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY"+dqx );//provincMigMap.get(""+dqx);
		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx );//provincMigMap.get("NY"+dqx);
		Map<CX,Map<HunpeiField,double[]>> ziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX"+dqx );//provincMigMap.get("CX"+dqx);
		Map<HunpeiField,double[]> ziNvOfAll = (Map<HunpeiField, double[]>) predictVarMap.get( "HunpeiOfAll"+dqx );//provincMigMap.get("All"+dqx);
		//???????(????????)
//		Map<CX,Map<Babies,double[]>> BirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfCX"+dqx );
//		Map<NY,Map<Babies,double[]>> BirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfNY"+dqx );
//		Map<Babies,double[]> BirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "BirthPredictOfAll"+dqx );
//		Map<CX,Map<Babies,double[]>> OverBirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfCX"+dqx );
//		Map<NY,Map<Babies,double[]>> OverBirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfNY"+dqx );
//		Map<Babies,double[]> OverBirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "OverBirthPredictOfAll"+dqx );
//		Map<Babies,double[]> PolicyBabies = ( Map < Babies , double [ ] > ) predictVarMap.get ( "PolicyBabies"+dqx );
		
		//??????
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "PopulationPredictOfCXNY"+dqx );
//		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
//		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
//		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );

		//??????
//		Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
		
		//????????
//		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
//		Map<CX,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX"+dqx );
//		Map<NY,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY"+dqx );
//		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll"+dqx );
		
		//??????--??
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx );
		
		//??????--??
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY"+dqx );
		
		//????
		double[][] husbandRate = ( double [ ][ ] ) predictVarMap.get("HusbandRate");
		
		//?????
		ParamBean3 envParm = (ParamBean3) globals.get("SetUpEnvParamBean");
		
		tempVar.singleSonFaCN = EnumMapTool.createSingleChild ( );
		tempVar.singleSonMaCN = EnumMapTool.createSingleChild ( );
		
		int i;
		for ( CX cx : CX.values ( ) )
			for ( NY ny : NY.values ( ) )
			{
				
				//????????? TODO Mark!!
				Map < HunpeiField , double [ ] > zn = ziNv.get ( cx ).get ( ny );
				Map<Year,Map<XB,double[]>>sondie = SonDiePopulationPredictOfYiHaiCXNY.get ( cx ).get ( ny );
//				sondie.get ( Year.getYear ( year ) ).get ( Female )[X] = 0;
				//????=IIF(CX=1,??????,IIF(CX=2,??????,IIF(CX=3,??????,??????)))
				Double yiHai = bBornBean.singleChildCN.get ( cx ).get ( ny );
				
				double MANL=0,momMANL=0,momMINL=0,DS;
				double[] ARD = new double[MAX_AGE];
				double[] ARFR1 = new double[MAX_AGE];
				double[] ARDuToNot = new double[MAX_AGE];
				double[] ARFND1 = new double[MAX_AGE];
				int NL;
				double [ ] ARFND2 = new double [ MAX_AGE ];
				double wifeRS = 0;
				double DDuToNot = 0;
				double DuToNotMomRS = 0;
				
				
				if( year == envParm.getBegin ( )){
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					MANL = 0;
					for ( X = 0;X < MAX_AGE;X ++ )
					{
						if ( zn.get ( D ) [ X ] != 0.0 && X > MANL )
							MANL = X;
					}
					
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X <= MANL )
						{
							ARD [ i++ ] = zn.get ( D ) [ X ];
						}
					}
					
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X >= 15.0 && X <= 49.0 )
						{
							ARFR1 [ i++ ] = zn.get ( FR1 ) [ X ];
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////
					
					
					//*****?????? ???????
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					for ( NL = 1;NL <= MANL + 1.0;NL ++ )
					{
						if ( ARD [ NL ] == 0.0 )
						{
							// TODO ??--LOOP
							continue;
						}
						DS = ARD [ NL ];
						momMINL = NL + 14;
						momMANL = NL + 48;

						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( ( X - momMINL ) >= 1.0
									&& ( X - momMANL ) <= 0.0 )
							{
								sondie.get ( Year.getYear ( year ) ).get (
										Female ) [ X ] = sondie.get (
										Year.getYear ( year ) ).get ( Female ) [ X ]
										+ DS * ARFR1 [ ( int ) ( X - momMINL ) ];// TODO
																					// ???????
							}
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////
					
					
					//*****??????
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X >= 15.0 && X <= 110.0 )
						{
							ARFND2 [ i++ ] = sondie.get ( Year.getYear ( year ) )
									.get ( Female ) [ X ];
						}
					}
					for ( NL = 15;NL <= 64.0;NL ++ )
					{ // ????
						wifeRS = ARFND2 [ NL - 14 ];
						// ??NL = '??' - allt(STR(NL,3));
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{ // ????
							if ( true )
							{
								sondie.get ( Year.getYear ( year ) )
										.get ( Male ) [ X ] = sondie.get (
										Year.getYear ( year ) ).get ( Male ) [ X ]
										+ wifeRS * husbandRate [ X ] [ NL ];// ????.&??NL
							}
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////

				}else{
					//&&???????????
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( X >= 15.0 && X <= 49.0 )
						{
							sondie.get ( Year.getYear ( year ) ).get ( Female ) [ X ] = sondie
									.get ( Year.getYear ( year ) )
									.get ( Female ) [ X ]
									+ yiHai * zn.get ( FR1 ) [ X ];
						}
					}
					for ( NL = 15;NL <= 64.0;NL ++ )
					{
						//TODO LOCATE FOR X=NL
						wifeRS = yiHai * zn.get ( FR1 ) [ NL ];
						// ??NL = '??' - allt(STR(NL,3));
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( true )
							{
								sondie.get ( Year.getYear ( year ) )
										.get ( Male ) [ X ] = sondie.get (
										Year.getYear ( year ) ).get ( Male ) [ X ]
										+ wifeRS * husbandRate [ X ] [ NL ];// ????.&??NL
							}
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////
					
					// TODO skip RELEASE AR???
					
					//////////////translated by Foxpro2Java Translator successfully:///////////////
					MANL = 0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( zn.get ( SingleToNot ) [ X ] != 0.0 && X > MANL )
							MANL = X;
					}
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X <= MANL )
						{
							ARDuToNot [ i++ ] = zn.get ( SingleToNot ) [ X ];
						}
					}
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X >= 15.0 && X <= 49.0 )
						{
							ARFR1 [ i++ ] = zn.get ( FR1 ) [ X ];
						}
					}
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X >= 15.0 && X <= 110.0 )
						{
							ARFND1 [ i++ ] = sondie.get ( Year.getYear ( year ) )
									.get ( Female ) [ X ];
						}
					}
					for ( NL = 1;NL <= MANL + 1.0;NL ++ )
					{
						if ( ARDuToNot [ NL ] == 0.0 )
						{
						}
						DDuToNot = ARDuToNot [ NL ];
						momMINL = NL + 14.0;
						momMANL = NL + 48.0;
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( ( X - momMINL ) >= 1.0
									&& ( X - momMANL ) <= 0.0 )
							{
								sondie.get ( Year.getYear ( year ) ).get (
										Female ) [ X ] = sondie.get (
										Year.getYear ( year ) ).get ( Female ) [ X ]
										- DDuToNot
										* ARFR1 [ ( int ) ( X - momMINL ) ];
							}
						}
					}
					for ( X = 0, i = 1;X < MAX_AGE;X ++ )
					{
						if ( X >= 15.0 && X <= 110.0 )
						{
							ARFND2 [ i++ ] = sondie.get ( Year.getYear ( year ) )
									.get ( Female ) [ X ];
						}
					}
					for ( NL = 15;NL <= 64.0;NL ++ )
					{
						DuToNotMomRS = ARFND1 [ NL - 14 ] - ARFND2 [ NL - 14 ];
						// ??NL = '??' - allt(STR(NL,3));
						for ( X = MAX_AGE - 1;X >= 0;X -- )
						{
							if ( true )
							{
								sondie.get ( Year.getYear ( year ) )
										.get ( Male ) [ X ] = sondie.get (
										Year.getYear ( year ) ).get ( Male ) [ X ]
										- DuToNotMomRS
										* husbandRate [ X ] [ NL ];// ????.&??NL
							}
						}
					}
					/////////////end of translating, by Foxpro2Java Translator///////////////

				}//END-ELSE
//				tempVar.singleSonFaCN.get ( Nongchun ).put( Nongye, 0.0);

				
	//////////////translated by Foxpro2Java Translator successfully:///////////////
				if ( cx == Nongchun && ny == Nongye )
				{
					tempVar.singleSonFaCN.get ( Nongchun ).put ( Nongye , 0.0 );
					tempVar.singleSonMaCN.get ( Nongchun ).put ( Nongye , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.singleSonFaCN
									.get ( Nongchun )
									.put (
											Nongye ,
											( tempVar.singleSonFaCN.get (
													Nongchun ).get ( Nongye ) + sondie
													.get ( Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.singleSonMaCN.get ( Nongchun ).put (
									Nongye ,
									( tempVar.singleSonMaCN.get ( Nongchun )
											.get ( Nongye ) + sondie.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				} else if ( cx == Chengshi && ny == Nongye )
				{
					tempVar.singleSonFaCN.get ( Chengshi ).put ( Nongye , 0.0 );
					tempVar.singleSonMaCN.get ( Chengshi ).put ( Nongye , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.singleSonFaCN
									.get ( Chengshi )
									.put (
											Nongye ,
											( tempVar.singleSonFaCN.get (
													Chengshi ).get ( Nongye ) + sondie
													.get ( Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.singleSonMaCN.get ( Chengshi ).put (
									Nongye ,
									( tempVar.singleSonMaCN.get ( Chengshi )
											.get ( Nongye ) + sondie.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				} else if ( cx == Nongchun && ny == Feinong )
				{
					tempVar.singleSonFaCN.get ( Nongchun ).put ( Feinong , 0.0 );
					tempVar.singleSonMaCN.get ( Nongchun ).put ( Feinong , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.singleSonFaCN
									.get ( Nongchun )
									.put (
											Feinong ,
											( tempVar.singleSonFaCN.get (
													Nongchun ).get ( Feinong ) + sondie
													.get ( Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.singleSonMaCN.get ( Nongchun ).put (
									Feinong ,
									( tempVar.singleSonMaCN.get ( Nongchun )
											.get ( Feinong ) + sondie.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				} else if ( cx == Chengshi && ny == Feinong )
				{
					tempVar.singleSonFaCN.get ( Chengshi ).put ( Feinong , 0.0 );
					tempVar.singleSonMaCN.get ( Chengshi ).put ( Feinong , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.singleSonFaCN
									.get ( Chengshi )
									.put (
											Feinong ,
											( tempVar.singleSonFaCN.get (
													Chengshi ).get ( Feinong ) + sondie
													.get ( Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.singleSonMaCN.get ( Chengshi ).put (
									Feinong ,
									( tempVar.singleSonMaCN.get ( Chengshi )
											.get ( Feinong ) + sondie.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				}
				tempVar.S_singleSonFa = 0;
				tempVar.S_singleSonMa = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						tempVar.S_singleSonFa += sondie.get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
						tempVar.S_singleSonMa += sondie.get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
			
			}// END-FOR-CX-NY
		
	
		/**
		 * ????
		 */
		tempVar.teFuFaCN = EnumMapTool.createSingleChild ( );
		tempVar.teFuMaCN = EnumMapTool.createSingleChild ( );
		for ( CX cx : CX.values ( ) )
			for ( NY ny : NY.values ( ) )
			{
				
				//????????? TODO Mark!!
				Map < HunpeiField , double [ ] > zn = ziNv.get ( cx ).get ( ny );
				Map<Year,Map<XB,double[]>>tefu = SonDiePopulationPredictOfTeFuCXNY.get ( cx ).get ( ny );
//				sondie.get ( Year.getYear ( year ) ).get ( Female )[X] = 0;
				//????=IIF(CX=1,??????,IIF(CX=2,??????,IIF(CX=3,??????,??????)))
				Double yiHai = bBornBean.singleChildCN.get ( cx ).get ( ny );
				
				double MANL=0,momMANL=0,momMINL=0,DS;
				double[] ARSWD = new double[MAX_AGE + 1];
				double[] ARFR1 = new double[MAX_AGE];
				double[] ARDuToNot = new double[MAX_AGE];
				double[] ARFND1 = new double[MAX_AGE];
				int NL;
				double [ ] ARFND2 = new double [ MAX_AGE ];
				double wifeRS = 0;
				double DSW = 0;
				double newSondieMa = 0;
				
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				MANL = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( zn.get ( SWD ) [ X ] != 0.0 && X > MANL )
						MANL = X;
				}

				for ( X = 0, i = 1;X < MAX_AGE;X ++ )
				{
					if ( X <= MANL )
					{
						ARSWD [ i++ ] = zn.get ( SWD ) [ X ];
					}
				}
				for ( X = 0, i = 1;X < MAX_AGE;X ++ )
				{
					if ( X >= 15.0 && X <= 49.0 )
					{
						ARFR1 [ i++ ] = zn.get ( FR1 ) [ X ];
					}
				}
				for ( X = 0, i = 1;X < MAX_AGE;X ++ )
				{
					if ( X >= 15.0 )
					{
						ARFND2 [ i++ ] = tefu.get ( Year.getYear ( year ) ).get (
								Female ) [ X ];
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////

				//TODO *****???????
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( NL = 1;NL <= MANL + 1.0 ;NL ++ )
				{
					if ( ARSWD [ NL ] == 0.0 )
					{
						// LOOP
						continue;
					}
					DSW = ARSWD [ NL ];
					momMINL = NL + 14.0;
					momMANL = NL + 48.0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( ( X - momMINL ) >= 1.0 && ( X - momMANL ) <= 0.0 )
						{
							tefu.get ( Year.getYear ( year ) ).get ( Female ) [ X ] = tefu
									.get ( Year.getYear ( year ) )
									.get ( Female ) [ X ]
									+ DSW * ARFR1 [ ( int ) ( X - momMINL ) ];
						}
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				//*****???????
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( NL = 15;NL <= 64.0;NL ++ )
				{ // ????

					// LOCATE FOR X=NL2
					newSondieMa = tefu.get ( Year.getYear ( year ) ).get (
							Female ) [ NL ]
							- ARFND2 [ NL - 14 ];

					// ??NL = '??' - ALLT(STR(NL2,3));

					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{ // ????
						if ( true )
						{
							tefu.get ( Year.getYear ( year ) ).get ( Male ) [ X ] = tefu
									.get ( Year.getYear ( year ) ).get ( Male ) [ X ]
									+ newSondieMa * husbandRate [ X ] [ NL ];// ????.&??NL
						}
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				if ( cx == Nongchun && ny == Nongye )
				{
					tempVar.teFuFaCN.get ( Nongchun ).put ( Nongye , 0.0 );
					tempVar.teFuMaCN.get ( Nongchun ).put ( Nongye , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.teFuFaCN.get ( Nongchun )
									.put (
											Nongye ,
											( tempVar.teFuFaCN.get ( Nongchun )
													.get ( Nongye ) + tefu.get (
													Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.teFuMaCN.get ( Nongchun ).put (
									Nongye ,
									( tempVar.teFuMaCN.get ( Nongchun ).get (
											Nongye ) + tefu.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				} else if ( cx == Chengshi && ny == Nongye )
				{
					tempVar.teFuFaCN.get ( Chengshi ).put ( Nongye , 0.0 );
					tempVar.teFuMaCN.get ( Chengshi ).put ( Nongye , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.teFuFaCN.get ( Chengshi )
									.put (
											Nongye ,
											( tempVar.teFuFaCN.get ( Chengshi )
													.get ( Nongye ) + tefu.get (
													Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.teFuMaCN.get ( Chengshi ).put (
									Nongye ,
									( tempVar.teFuMaCN.get ( Chengshi ).get (
											Nongye ) + tefu.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				} else if ( cx == Nongchun && ny == Feinong )
				{
					tempVar.teFuFaCN.get ( Nongchun ).put ( Feinong , 0.0 );
					tempVar.teFuMaCN.get ( Nongchun ).put ( Feinong , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.teFuFaCN
									.get ( Nongchun )
									.put (
											Feinong ,
											( tempVar.teFuFaCN.get ( Nongchun )
													.get ( Feinong ) + tefu
													.get ( Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.teFuMaCN.get ( Nongchun ).put (
									Feinong ,
									( tempVar.teFuMaCN.get ( Nongchun ).get (
											Feinong ) + tefu.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
				} else if ( cx == Chengshi && ny == Feinong )
				{
					tempVar.teFuFaCN.get ( Chengshi ).put ( Feinong , 0.0 );
					tempVar.teFuMaCN.get ( Chengshi ).put ( Feinong , 0.0 );
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.teFuFaCN
									.get ( Chengshi )
									.put (
											Feinong ,
											( tempVar.teFuFaCN.get ( Chengshi )
													.get ( Feinong ) + tefu
													.get ( Year.getYear ( year ) )
													.get ( Male ) [ X ] ) );
							tempVar.teFuMaCN.get ( Chengshi ).put (
									Feinong ,
									( tempVar.teFuMaCN.get ( Chengshi ).get (
											Feinong ) + tefu.get (
											Year.getYear ( year ) ).get (
											Female ) [ X ] ) );
						}
					}
					
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////

			System.out.println("End!");
			}// END-FOR-CX-NY
		
			
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

