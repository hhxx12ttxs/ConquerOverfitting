package prclqz.methods;

import java.util.EnumMap;
import java.util.Map;

import static prclqz.core.enumLib.Summary.*;
import static prclqz.core.enumLib.SYSFMSField.*;
import static prclqz.core.enumLib.BabiesBorn.*;
import static prclqz.core.enumLib.HunpeiField.*;
import static prclqz.core.enumLib.XB.*;
import static prclqz.core.enumLib.CX.*;
import static prclqz.core.enumLib.NY.*;
import static prclqz.core.enumLib.Policy.*;
import static prclqz.core.Const.*;

import java.util.HashMap;

import prclqz.DAO.IDAO;
import prclqz.DAO.MyDAOImpl;
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.MainTaskBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.Message;
import prclqz.core.StringList;
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.BabiesBorn;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.PolicyBirth;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.SumBM;
import prclqz.core.enumLib.Summary;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Policy;
import prclqz.core.enumLib.Year;
import prclqz.lib.EnumMapTool;
import prclqz.methods.IAbstractOfPredictMethod;
import prclqz.methods.ICalculateExpectedAgeMethod;
import prclqz.methods.IDeathCalculatingMethod;
import prclqz.methods.IGroupingByAgeMethod;
import prclqz.methods.IMainExtremeValuesMethod;
import prclqz.methods.IResultOfAbstractMethod;
import prclqz.methods.MCalculateExpectedAge;
import prclqz.methods.MDeathCalculating;
import prclqz.methods.MGroupingByAge;
import prclqz.methods.MMainExtremeValues;
import prclqz.methods.MResultOfAbstract;
import prclqz.methods.MethodException;
import prclqz.parambeans.ParamBean3;
import test.EnumTools;
import test.MDqImplementableBornRateForTest;
/**
 * ??????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MAbstractOfPredict implements IAbstractOfPredictMethod
{

//	public static void main(String[] argv)throws Exception{
//		HashMap<String,Object>globals = new HashMap<String,Object>();
//		IDAO m = new MyDAOImpl();
//		int dqx=12;//??
//		int year=2010;
//		/***??tempvar***/
//		TempVariablesBean tempVar = test.parse(year, dqx-10);
//		tempVar.setProvince(dqx);
//		tempVar.setYear(year);
//		/***?????***/
//		HashMap<String,Object> predictVarMap = new HashMap<String,Object>();
//		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = EnumTools.creatNYFNZiNvFromFile(MAX_AGE, year, dqx-10);
//		predictVarMap.put("HunpeiOfNY"+dqx, ziNvOfNY);
//		
//		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = EnumTools.creatPopulationPredictOfCX(MAX_AGE, year, dqx-10);
//		predictVarMap.put ( "PopulationPredictOfCX"+dqx ,PopulationPredictOfCX);
//		
//		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = EnumTools.creatPopulationPredictOfNY(MAX_AGE, year, dqx-10);
//		predictVarMap.put ( "PopulationPredictOfNY"+dqx ,PopulationPredictOfNY);
//		
//		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = EnumTools.creatPopulationPredictOfAll(MAX_AGE, year, dqx-10);
//		predictVarMap.put ( "PopulationPredictOfAll"+dqx ,PopulationPredictOfAll);
//		
//		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = EnumTools.createCXYearXBDoubleArrMapFromFile(MAX_AGE, year, dqx-10);
//		predictVarMap.put("PopulationMigrationOfCX"+dqx , PopulationMigrationOfCX);
//		
//		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = EnumTools.createYearXBDoubleArrMapFromFile(MAX_AGE, year, dqx-10);
//		predictVarMap.put("PopulationMigrationOfAll"+dqx , PopulationMigrationOfAll);
//		
//		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = EnumTools.createPopulationDeathOfCX(MAX_AGE, year,dqx-10);
//		predictVarMap.put("PopulationDeathOfCX"+dqx , PopulationDeathOfCX);
//		
//		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = EnumTools.createPopulationDeathOfAll(MAX_AGE, year,dqx-10);
//		predictVarMap.put("PopulationDeathOfAll"+dqx , PopulationDeathOfAll);
//		
//		Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration =new EnumMap<Year, Map<SumBM,Double>>(Year.class);
//		for(int i=2000;i<=2100;i++){
//			SummaryOfBirthAndMigration.put(Year.getYear(i), new EnumMap<SumBM,Double>(SumBM.class));
//		}
//		predictVarMap.put("SummaryOfBirthAndMigration"+dqx , SummaryOfBirthAndMigration);
//		
//		
//		Map<Year,Map<Summary,Double >> SummaryOfAll = EnumTools.createSummaryOfAll(MAX_AGE, year, dqx-10);
//		predictVarMap.put ( "SummaryOfAll"+dqx, SummaryOfAll);
//		
//		Map<CX,Map<Year,Map<Summary,Double >>> SummaryOfCX = EnumTools.createSummaryOfCX(MAX_AGE, year, dqx-10);
//		predictVarMap.put ( "SummaryOfCX"+dqx, SummaryOfCX);
//		
//		Map<NY,Map<Year,Map<Summary,Double >>> SummaryOfNY = EnumTools.createSummaryOfNY(MAX_AGE, year, dqx-10); 
//		predictVarMap.put("SummaryOfNY"+dqx, SummaryOfNY);
//		
//		/***??xbbMap***/
//		MainTaskBean task;
//		task = m.getTask("FirstTask");
//		HashMap<String,BornXbbBean> xbbMap = m.getARBornXbbBeans(task);
//		xbbMap.get(""+dqx).setXbb(1.1437);
//		/***??strValues***/
//		StringList strValues = new StringList();
//		/***??parambean***/
//		ParamBean3 envParm = new ParamBean3();
//		envParm.setBegin(2000);
//		envParm.setEnd(2100);
//		/***??globals***/
//		globals.put("tempVarBean", tempVar);
//		globals.put("predictVarMap", predictVarMap);
//		globals.put("bornXbbBeanMap", xbbMap);
//		globals.put("strValues", strValues);
//		globals.put("SetUpEnvParamBean", envParm);
//		/***????***/
//		MAbstractOfPredictForTest hehe = new MAbstractOfPredictForTest();
//		hehe.calculate(m, globals, 
//				new MDeathCalculating(), 
//				new MCalculateExpectedAge(), 
//				new MGroupingByAge(), 
//				new MResultOfAbstract(),
//				new MMainExtremeValues());
//	} 
	@Override
	public void calculate ( IDAO m, HashMap<String, Object> globals,
			IDeathCalculatingMethod deathCal, ICalculateExpectedAgeMethod calAge,
			IGroupingByAgeMethod groupAge,
			IResultOfAbstractMethod resultAbs,IMainExtremeValuesMethod mainVals )
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
		String dqB = xbb.getDqName ( );
		//define local variables
		int X=0;
		
		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx );//provincMigMap.get("NY"+dqx);
		//??????
		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );

		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
		
		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
		
		//?????
		ParamBean3 envParm = (ParamBean3) globals.get("SetUpEnvParamBean");
		
		//??????????				
		Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration = ( Map < Year , Map < SumBM , Double >> ) predictVarMap.get ( "SummaryOfBirthAndMigration"+dqx);
		Map<Year,Map<SumBM,Double>> sumBM = SummaryOfBirthAndMigration;
		
//////////////translated by Foxpro2Java Translator successfully:///////////////
//		for(X = MAX_AGE-1; X >= 0 ; X--){
//		if( true ){ 
		
		// TODO ????? ???????
		//td=???String?index
		double td = strValues.add ( Policy.getChinese( getPolicyById ( tempVar.feiNongPolicy1 ) )
				+ ( tempVar.feiNongTime1 == 0 ? "" : "" + tempVar.feiNongTime1 )
				+ Policy.getChinese( getPolicyById ( tempVar.feiNongPolicy2 ) )
				+ ( tempVar.feiNongTime2 == 0 ? "" : "" + tempVar.feiNongTime2 )
				+ Policy.getChinese( getPolicyById ( tempVar.feiNongPolicy3 ) )
				+ ( tempVar.feiNongTime3 == 0 ? "" : "" + tempVar.feiNongTime3 )
				+ ( tempVar.QY == 0 ? "??" : ( tempVar.QY == 1 ? "??"
						: ( tempVar.QY == 2 ? "??" : "??" ) ) ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.Dq , strValues.add ( dqB ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.Policy , td );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.ImplTFR , tempVar.dqImplTFR0 );
		
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.PolicyBirtRate ,
				tempVar.dqPolicyTFR0 );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.OverBirthRate ,
				tempVar.dqOverBrithTFR );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.OverBirthNum ,
				tempVar.overBirthS );
		// }
		// if( true ){
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.FNFSingle , tempVar.N1D2C );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.FNMSingle , tempVar.N2D1C );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.FNSFSingle , tempVar.NNFC );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NYFSingle , tempVar.N1D2N );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NYMSingle , tempVar.N2D1N );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NYSFSingle , tempVar.NNFN );
		// }
		// if( true ){
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.DQTFR , tempVar.dqPolicyTFR0 );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.CZTFR ,
				tempVar.cxPolicyTFR0.get ( CX.Chengshi ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.FNTFR ,
				tempVar.nyPolicyTFR0.get ( Feinong ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NCTFR ,
				tempVar.cxPolicyTFR0.get ( CX.Nongchun ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NYTFR ,
				tempVar.nyPolicyTFR0.get ( Nongye ) );
//		}
//		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

		//TODO SELECT A
		//DQQY=0
//		double DQQY = 0;
		Map < Year , Map < XB , double [ ] >> pplPredict,pplMig,pplDeath;
		Map<HunpeiField,double[]> zn;
		double CEM0=-1,CEF0=-1,XEM0=-1,XEF0=-1;

		for(int cx=1; cx<=5; cx++)
		{
			tempVar.cxI = cx;
			switch(cx)
			{
			case 1:
				pplPredict = PopulationPredictOfAll;
				pplMig = PopulationMigrationOfAll;
				zn = null;
				pplDeath = PopulationDeathOfAll;
				break;
			case 2:
				pplPredict = PopulationPredictOfCX.get ( CX.Chengshi );
				pplMig = PopulationMigrationOfCX.get ( CX.Chengshi );
				zn = null;
				pplDeath = PopulationDeathOfCX.get ( CX.Chengshi );
				break;
			case 3:
				pplPredict = PopulationPredictOfNY.get ( Feinong );
//				pplMig = PopulationMigrationOfNY.get ( Feinong );
				pplMig = null;
				zn = ziNvOfNY.get ( Feinong );
//				pplDeath = PopulationDeathOfNY.get ( Feinong );
				pplDeath = null;
				break;
			case 4:
				pplPredict = PopulationPredictOfCX.get ( CX.Nongchun );
				pplMig = PopulationMigrationOfCX.get ( CX.Nongchun );
				zn = null;
				pplDeath = PopulationDeathOfCX.get ( CX.Nongchun );
				break;
			case 5:
				pplPredict = PopulationPredictOfNY.get ( Nongye );
//				pplMig = PopulationMigrationOfNY.get ( Nongye );
				pplMig = null;
				zn = ziNvOfNY.get ( Nongye );
//				pplDeath = PopulationDeathOfNY.get ( Nongye );
				pplDeath = null;
				break;
			default: 
				pplPredict = pplMig = pplDeath = null;
				zn = null;
			}
			
			//////////////translated by Foxpro2Java Translator successfully:///////////////
			X = 0;
			/***DS:?????year??year-1 ?????-1????????****/
			tempVar.bornPopulation = ( pplPredict.get (
					Year.getYear ( year ) ).get ( XB.Male ) [ X ] + pplPredict
					.get ( Year.getYear ( year ) ).get ( XB.Female ) [ X ] ) / 10000.0;
			//System.out.println("bornPopulation:"+tempVar.bornPopulation);
			tempVar.bornPopulationM = pplPredict
					.get ( Year.getYear ( year ) ).get ( XB.Male ) [ X ] / 10000.0;
			//System.out.println("bornPopulationM:"+tempVar.bornPopulationM);
			tempVar.bornPopulationF = pplPredict
					.get ( Year.getYear ( year ) ).get ( XB.Female ) [ X ] / 10000.0;
			//System.out.println("bornPopulationF:"+tempVar.bornPopulationF);
			if ( cx <= 2.0 || cx == 4.0 )
			{
				tempVar.inMigRK = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						tempVar.inMigRK += ( pplMig.get ( Year.getYear ( year ) ).get ( XB.Male ) [ X ] 
								           + pplMig.get (Year.getYear ( year ) ).get ( XB.Female ) [ X ] ) / 10000.0;
					}
				}
				//System.out.println(tempVar.inMigRK);
				/**
				 * DO ??????
  				 * DO ??????
				 */
				deathCal.calculate ( m , globals ); //TODO			
				calAge.calculate ( m , globals ); //TODO 
				
				if ( cx == 2.0 || cx == 3.0 )
				{
					CEM0 = tempVar.EM0;
					CEF0 = tempVar.EF0;
				} else if ( cx == 4.0 || cx == 5.0 )
				{
					XEM0 = tempVar.EM0;
					XEF0 = tempVar.EF0;
				}
			} else
			{
				tempVar.inMigRK = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						tempVar.inMigRK += zn.get ( HJQY ) [ X ] / 10000.0;
					}
				}
//				System.out.print(cx);
//				System.out.print("  ");
//				System.out.println(tempVar.inMigRK);
			}
			/////////////end of translating, by Foxpro2Java Translator///////////////
			
			/**
			 * FIE1=IIF(CX=1,'DQ????',ALLTRIM(??B)-'??')
  			 * SELECT 402
  			 * REPLACE &FIE1 WITH ???RK
			 */
			/****by DS***/
			switch (cx) {
			case 1:
				sumBM.get ( Year.getYear ( year ) ).put ( SumBM.DQMigS , tempVar.inMigRK );
				break;
			case 2:
				sumBM.get ( Year.getYear ( year ) ).put ( SumBM.ChengshiMig , tempVar.inMigRK );
				break;
			case 3:
				sumBM.get ( Year.getYear ( year ) ).put ( SumBM.FeinongMig , tempVar.inMigRK );
				break;
			case 4:
				sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NongchunMig , tempVar.inMigRK );
				break;
			case 5:
				sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NongyeMig , tempVar.inMigRK );
				break;
			default:
				break;
			}
			//System.out.println(sumBM.get( Year.getYear(year) ).get( SumBM.FeinongMig ));
			/***end***/
			if ( cx == 1 )
			{
				tempVar.SQY += tempVar.inMigRK;
				tempVar.SDQQY = tempVar.inMigRK;
			} else if ( cx == 2 || cx == 4 )
			{
				tempVar.DQQY += tempVar.inMigRK;
			}
			
			/****by DS:foxpro2java????????????****/
			if( cx == 2 || cx == 3 )
				tempVar.EM0 = CEM0;
			else 
				if( cx == 4 || cx == 5 )
					tempVar.EM0 = XEM0;

			if( cx == 2 || cx == 3 )
				tempVar.EF0 = CEF0;
			else 
				if( cx == 4 || cx == 5 )
					tempVar.EF0 = XEF0;
			/****end********************************/
			
			//second level
			groupAge.calculate ( m , globals ); //TODO
			
			//TODO foxpro????DQB = IIF(CX=1,DQB,??B)  ?????????????????????
			
			resultAbs.calculate ( m , globals ); //TODO
//			if(cx == 1){
//				????
//			}
			if( year == envParm.getEnd())
				mainVals.calculate(m, globals); //TODO
		}// END-IF-For 1~5
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.LocalMig , tempVar.SDQQY );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.QGMigS , Double.valueOf(tempVar.SQY) );

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

	@Override
	public void calculate ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		
	}
}

