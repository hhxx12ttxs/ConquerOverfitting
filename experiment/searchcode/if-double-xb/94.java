package prclqz.methods;

import java.awt.SystemTray;
import java.util.Map;

import static prclqz.core.enumLib.SumBM.*;
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
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
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
import prclqz.core.enumLib.Year;
import prclqz.lib.EnumMapTool;
import prclqz.parambeans.ParamBean3;
import test.EnumTools;
/**
 * ??????
 * @author Jack Long
 * @email  prclqz@zju.edu.cn
 *
 */
public class MResultOfAbstract implements IResultOfAbstractMethod
{

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
		String dqB = xbb.getDqName ( );
		//define local variables
//		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
//		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
//		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
		//?? ???????
//		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY"+dqx );//provincMigMap.get(""+dqx);
//		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx );//provincMigMap.get("NY"+dqx);
//		Map<CX,Map<HunpeiField,double[]>> ziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX"+dqx );//provincMigMap.get("CX"+dqx);
//		Map<HunpeiField,double[]> ziNvOfAll = (Map<HunpeiField, double[]>) predictVarMap.get( "HunpeiOfAll"+dqx );//provincMigMap.get("All"+dqx);
//		
//		//????????
//		Map<Babies,double[]> policyBabies = (Map<Babies, double[]>) predictVarMap.get("PolicyBabies"+dqx);
//		//???????(????????)
//		Map<CX,Map<Babies,double[]>> BirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfCX"+dqx );
//		Map<NY,Map<Babies,double[]>> BirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfNY"+dqx );
//		Map<Babies,double[]> BirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "BirthPredictOfAll"+dqx );
//		Map<CX,Map<Babies,double[]>> OverBirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfCX"+dqx );
//		Map<NY,Map<Babies,double[]>> OverBirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfNY"+dqx );
//		Map<Babies,double[]> OverBirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "OverBirthPredictOfAll"+dqx );
//		Map<Babies,double[]> PolicyBabies = ( Map < Babies , double [ ] > ) predictVarMap.get ( "PolicyBabies"+dqx );
//		
//		//??????
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "PopulationPredictOfCXNY"+dqx );
//		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
//		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
//		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );
//
//		//??????
//		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX"+dqx );
//		Map<NY,Map<Year,Map<XB,double[]>>> PopulationMigrationOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfNY"+dqx );
//		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
//		
//		//??????
//		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
//		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
		
		
		//??????
//		Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
		
		//????????
//		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
//		Map<CX,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX"+dqx );
//		Map<NY,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY"+dqx );
//		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll"+dqx );
		
		//??????--??
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx );
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> yiHaiCN = SonDiePopulationPredictOfYiHaiCXNY;
//		yiHaiCN.get ( Chengshi ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( Male )[X] = 0;
		
		//??????--??
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY"+dqx );
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> tefuCN = SonDiePopulationPredictOfTeFuCXNY;
		
		// ?????? CX,NY?All
//		Map<CX,Map<Year,Map<XB,double[]>>>SonDiePopulationPredictOfTeFuCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCX"+dqx ),
//			SonDiePopulationPredictOfYiHaiCX=( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCX"+dqx );
//		
//		Map<CX,Map<Year,Map<XB,double[]>>> yiHaiC = SonDiePopulationPredictOfYiHaiCX,
//			tefuC = SonDiePopulationPredictOfTeFuCX;
		
//		Map<NY,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfTeFuNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuNY"+dqx ),
//			SonDiePopulationPredictOfYiHaiNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiNY"+dqx );
//		Map<NY,Map<Year,Map<XB,double[]>>> yiHaiN = SonDiePopulationPredictOfYiHaiNY,
//			tefuN = SonDiePopulationPredictOfTeFuNY;
//		Map<Year,Map<XB,double[]>> SonDiePopulationPredictOfTeFuAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuAll"+dqx ),
//			SonDiePopulationPredictOfYiHaiAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiAll"+dqx );
//		Map<Year,Map<XB,double[]>> yiHaiA = SonDiePopulationPredictOfYiHaiAll,
//			tefuA = SonDiePopulationPredictOfTeFuAll;
//		
//		
//		//????
//		double[][] husbandRate = ( double [ ][ ] ) predictVarMap.get("HusbandRate");
		
		//?????
		ParamBean3 envParm = (ParamBean3) globals.get("SetUpEnvParamBean");
		
		//????????					
//		Map<Year,Map<PolicyBirth,Double>> PolicyBirthRate = ( Map < Year , Map < PolicyBirth , Double >> ) predictVarMap.get ( "PolicyBirthRate"+dqx );
//		
//		//?????
//		Map<Year,Map<BabiesBorn,Double>> babiesBorn = ( Map < Year , Map < BabiesBorn , Double >> ) predictVarMap.get ( "BabiesBorn"+dqx );
//		
		//????_??
		Map<Year,Map<Summary,Double >> SummaryOfAll = ( Map < Year , Map < Summary , Double >> ) predictVarMap.get ( "SummaryOfAll"+dqx );
		Map<CX,Map<Year,Map<Summary,Double >>> SummaryOfCX = ( Map < CX , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfCX"+dqx );
		Map<NY,Map<Year,Map<Summary,Double >>> SummaryOfNY = ( Map < NY , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfNY"+dqx );
		
		//??????????				
//		Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration = ( Map < Year , Map < SumBM , Double >> ) predictVarMap.get ( "SummaryOfBirthAndMigration");
//		Map<Year,Map<SumBM,Double>> sumBM = SummaryOfBirthAndMigration;
		
		/////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		
		Map<Year,Map<Summary,Double >> summary;
		//System.out.println(tempVar.cxI);
		switch ( tempVar.cxI )
		{
		case 1 :
			summary = SummaryOfAll;
			break;
		case 2 :
			summary = SummaryOfCX.get( CX.Chengshi );
			break;
		case 3 :
			summary = SummaryOfNY.get( Feinong );
			break;
		case 4 :
			summary = SummaryOfCX.get( CX.Nongchun );
			break;
		case 5 :
			summary = SummaryOfNY.get( Nongye );
			break;
		default :
			summary = null;
		}
		
//////////////translated by Foxpro2Java Translator successfully:///////////////
		// for(X = MAX_AGE-1; X >= 0 ; X--){
		// if( true ){
		// ?? = year;

		// IIF(CX=1,DQB,IIF(CX=2,'??',IIF(CX=3,'??',IIF(CX=4,'??','??'))))
		int cx = tempVar.cxI;
		// strValues.add( cx == 1?dqB:(cx == 2?"??":(cx == 3?"??":(cx ==
		// 4?"??":"??"))) );
		summary.get( Year.getYear( year ) ).put(
				Summary.DQ ,
				strValues.add( cx == 1 ? dqB : ( cx == 2 ? "??"
						: ( cx == 3 ? "??" : ( cx == 4 ? "??" : "??" ) ) ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.Popl , tempVar.Popl_T );
		summary.get( Year.getYear( year ) ).put( Summary.Male , tempVar.Popl_M );
		summary.get( Year.getYear( year ) ).put( Summary.Female ,tempVar.Popl_F );

		// IIF(CX=1,DQ????M,IIF(CX=2,C????M,IIF(CX=3,0,IIF(CX=4,X????M,0))))
		double dqDeathM = cx == 1 ? tempVar.dqDeathM
				: ( cx == 2 ? tempVar.cDeathM : ( cx == 3 ? 0
						: ( cx == 4 ? tempVar.xDeathM : 0 ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.DeathM , dqDeathM );

		// IIF(CX=1,DQ????F,IIF(CX=2,C????F,IIF(CX=3,0,IIF(CX=4,X????F,0))))
		double dqDeathF = cx == 1 ? tempVar.dqDeathF
				: ( cx == 2 ? tempVar.cDeathF : ( cx == 3 ? 0
						: ( cx == 4 ? tempVar.xDeathF : 0 ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.DeathF , dqDeathF );

		// IIF(CX=1,????TFR0,IIF(CX=2,????TFR0,IIF(CX=3,????TFR0,IIF(CX=4,????TFR0,????TFR0))))
		double TFR0 = cx == 1 ? tempVar.dqPolicyTFR0
				: ( cx == 2 ? tempVar.cxPolicyTFR0.get( CX.Chengshi )
						: ( cx == 3 ? tempVar.nyPolicyTFR0.get( Feinong )
								: ( cx == 4 ? tempVar.cxPolicyTFR0
										.get( CX.Nongchun )
										: tempVar.nyPolicyTFR0.get( Nongye ) ) ) );
		summary.get( Year.getYear( year ) )
				.put( Summary.PolicyBirthRate , TFR0 );

		// IIF(CX=1,????TFR,IIF(CX=2,????TFR,IIF(CX=3,????TFR,IIF(CX=4,????TFR,????TFR))))
		double hunneiTFR = cx == 1 ? tempVar.dqHunneiTFR
				: ( cx == 2 ? tempVar.cxHunneiTFR.get( CX.Chengshi )
						: ( cx == 3 ? tempVar.nyHunneiTFR.get( Feinong )
								: ( cx == 4 ? tempVar.cxHunneiTFR
										.get( CX.Nongchun )
										: tempVar.nyHunneiTFR.get( Nongye ) ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.MarryNPolicyTFR ,
				hunneiTFR );

		// IIF(CX=1 AND ZC??=2,????TFR0,IIF(CX=2 AND ZC??=2,????TFR0,IIF(CX=3
		// AND ZC??=2,????TFR0,IIF(CX=4 AND ZC??=2,????TFR0,IIF(CX=5 AND
		// ZC??=2,????TFR0,0)))))
		double implTFR0 = cx == 1 ? tempVar.dqImplTFR0
				: ( cx == 2 ? tempVar.cxImplTFR0.get( CX.Chengshi )
						: ( cx == 3 ? tempVar.nyImplTFR0.get( Feinong )
								: ( cx == 4 ? tempVar.cxImplTFR0
										.get( CX.Nongchun )
										: tempVar.nyImplTFR0.get( Nongye ) ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.ImplTFR , implTFR0 );

		// IIF(CX=1 AND ZC??=2,????TFR0,IIF(CX=2 AND ZC??=2,????TFR0,IIF(CX=3
		// AND ZC??=2,????TFR0,IIF(CX=4 AND ZC??=2,????TFR0,IIF(CX=5 AND
		// ZC??=2,????TFR0,0)))))
		double shihunTFR0 = cx == 1 ? tempVar.dqShiHunTFR0
				: ( cx == 2 ? tempVar.cxShiHunTFR0.get( CX.Chengshi )
						: ( cx == 3 ? tempVar.nyShiHunTFR0.get( Feinong )
								: ( cx == 4 ? tempVar.cxShiHunTFR0
										.get( CX.Nongchun )
										: tempVar.nyShiHunTFR0.get( Nongye ) ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.MarryBirthRate ,
				shihunTFR0 );
		/******DONE ???????*******/
		summary.get( Year.getYear( year ) ).put( Summary.Birth ,
				tempVar.bornPopulation );
		//System.out.println(tempVar.bornPopulation);
		summary.get( Year.getYear( year ) ).put( Summary.BirthM ,
				tempVar.bornPopulationM );
		//System.out.println(tempVar.bornPopulationF);
		summary.get( Year.getYear( year ) ).put( Summary.BirthF ,
				tempVar.bornPopulationF );
		//System.out.println(tempVar.bornPopulationM);
		summary.get( Year.getYear( year ) ).put( Summary.BirthRate ,
				tempVar.bornPopulation / tempVar.nianzhongRK * 1000.0 );
		
		
		
		
		// IIF(ND0>2008 and ZC??=2,??S/10000,0)
		double ovb = year > 2008 && tempVar.ZCimplement == 2 ? tempVar.overBirthS / 10000
				: 0;
		summary.get( Year.getYear( year ) ).put( Summary.OverBirth , ovb );

		// IIF(CX=1,DQ????,IIF(CX=2,XFN????,IIF(CX=3,CFN????,IIF(CX=4,XNY????,CNY????))))
		double policyBirth = cx == 1 ? tempVar.DqPlyBorn
				: ( cx == 2 ? tempVar.XFNPlyBorn
						: ( cx == 3 ? tempVar.CFNPlyBorn
								: ( cx == 4 ? tempVar.XNYPlyBorn
										: tempVar.CNYPlyBorn ) ) );
		summary.get( Year.getYear( year ) ).put( Summary.PolicyBirth ,
				policyBirth );

		summary.get( Year.getYear( year ) ).put(
				Summary.Death ,
				summary.get( Year.getYear( year ) ).get( Summary.DeathM )
						+ summary.get( Year.getYear( year ) ).get(
								Summary.DeathF ) );
		summary.get( Year.getYear( year ) ).put(
				Summary.DeathRate ,
				summary.get( Year.getYear( year ) ).get( Summary.Death )
						/ tempVar.nianzhongRK * 1000.0 );
		
		/******DONE ?????????,?????????ok*******/
		summary.get( Year.getYear( year ) ).put(
				Summary.AutoIncRate ,
				summary.get( Year.getYear( year ) ).get( Summary.BirthRate ) 
				- summary.get( Year.getYear( year ) ).get( Summary.DeathRate ) );
		
		summary.get( Year.getYear( year ) ).put( Summary.PredictAgeM ,
				tempVar.EM0 );
		summary.get( Year.getYear( year ) ).put( Summary.PredictAgeF ,
				tempVar.EF0 );
		summary.get( Year.getYear( year ) ).put( Summary.PoplAvg ,
				tempVar.nianzhongRK );
		summary.get( Year.getYear( year ) ).put( Summary.IncNum ,
				tempVar.xinzengRK );
		summary.get( Year.getYear( year ) ).put( Summary.TotalIncRate ,
				tempVar.xinzengRK / tempVar.nianzhongRK * 1000.0 );
		
		/****TODO xbb????*****/
		summary.get( Year.getYear( year ) ).put( Summary.BirthXBB , 
				xbb.getXbb() * 100.0 );
		//System.out.println(xbb.getDqName());
		//System.out.println(xbb.getXbb());
			
		summary.get( Year.getYear( year ) ).put( Summary.InMig ,
				tempVar.inMigRK );
		summary.get( Year.getYear( year ) ).put( Summary.InMigRate ,
				tempVar.inMigRK / tempVar.nianzhongRK * 1000.0 );
		
		summary.get( Year.getYear( year ) ).put( Summary.Chengshi ,
				tempVar.CRK / 10000.0 );
		summary.get( Year.getYear( year ) ).put( Summary.Nongchun ,
				tempVar.XRK / 10000.0 );
		
		/******DONE test.java??CSH??????cshLeve ???cshLevel?CSHLevel?????= =*****/
		summary.get( Year.getYear( year ) ).put( Summary.ChengshiB ,
				tempVar.CSHLevel );
		//System.out.println(tempVar.CSHLevel);
		summary.get( Year.getYear( year ) ).put( Summary.FN ,
				tempVar.FNRK / 10000.0 );
		summary.get( Year.getYear( year ) ).put( Summary.NY ,
				tempVar.NYRK / 10000.0 );
		summary.get( Year.getYear( year ) ).put( Summary.FNB ,
				tempVar.FNRK / ( tempVar.FNRK + tempVar.NYRK ) * 100.0 );
		summary.get( Year.getYear( year ) ).put( Summary.Babies ,
				tempVar.T_yingyouer );
		summary.get( Year.getYear( year ) )
				.put( Summary.PreSchool , tempVar.XQ );
		summary.get( Year.getYear( year ) ).put( Summary.PrimarySch ,
				tempVar.XX );
		summary.get( Year.getYear( year ) )
				.put( Summary.MiddleSch , tempVar.CZ );
		summary.get( Year.getYear( year ) ).put( Summary.HighSch , tempVar.GZ );
		summary.get( Year.getYear( year ) ).put( Summary.University ,
				tempVar.DX );
		summary.get( Year.getYear( year ) ).put( Summary.GAAge , tempVar.JYM + tempVar.JYF );
		summary.get( Year.getYear( year ) ).put( Summary.GAAgeM , tempVar.JYM );
		summary.get( Year.getYear( year ) ).put( Summary.GAAgeF , tempVar.JYF );
		summary.get( Year.getYear( year ) ).put( Summary.IntnYoung ,
				tempVar.T_teen );
		summary.get( Year.getYear( year ) ).put( Summary.IntnLabor , tempVar.T_labor );
		summary.get( Year.getYear( year ) ).put( Summary.IntnQingLabor ,
				tempVar.T_younglabor );
		summary.get( Year.getYear( year ) ).put( Summary.IntnOld ,
				tempVar.T_old );
		summary.get( Year.getYear( year ) ).put( Summary.IntnOldM ,
				tempVar.T_oldM );
		summary.get( Year.getYear( year ) ).put( Summary.IntnOldF ,
				tempVar.T_oldF );
		summary.get( Year.getYear( year ) ).put( Summary.HighAge ,
				tempVar.T_high );
		summary.get( Year.getYear( year ) ).put( Summary.HighAgeM ,
				tempVar.T_highM );
		summary.get( Year.getYear( year ) ).put( Summary.HighAgeF ,
				tempVar.T_highF );
		summary.get( Year.getYear( year ) ).put( Summary.LongAge ,
				tempVar.changshou_M0 + tempVar.changshou_F0 );
		summary.get( Year.getYear( year ) ).put( Summary.LongAgeM ,
				tempVar.changshou_M0 );
		summary.get( Year.getYear( year ) ).put( Summary.LongAgeF ,
				tempVar.changshou_F0 );
		summary.get( Year.getYear( year ) ).put( Summary.Mid , tempVar.NLZ );

		/*****DONE tempVar.ST?AV??0(GroupingByAge????ST?AV????????)*******/
		summary.get( Year.getYear( year ) ).put( Summary.AgeDif ,
				tempVar.ST / tempVar.AV * 100.0 );
		//System.out.println(summary.get( Year.getYear( year ) ).get( Summary.AgeDif ));
		
		
		summary.get( Year.getYear( year ) ).put( Summary.NationYoung ,
				tempVar.GB_shounian );
		summary.get( Year.getYear( year ) ).put( Summary.NationLaborF ,
				tempVar.G_labornianF );
		summary.get( Year.getYear( year ) ).put( Summary.NationOldF ,
				tempVar.G_oldnianF );
		summary.get( Year.getYear( year ) ).put( Summary.NationLaborM ,
				tempVar.GB_labornianM );
		summary.get( Year.getYear( year ) ).put( Summary.NationOldM ,
				tempVar.GB_oldnianM );
		summary.get( Year.getYear( year ) ).put( Summary.NationPre ,
				tempVar.GB_laoqian );
		summary.get( Year.getYear( year ) ).put( Summary.NationMidM ,
				tempVar.GB_laolingM );
		summary.get( Year.getYear( year ) ).put( Summary.NationMidF ,
				tempVar.GB_laolingF );
		summary.get( Year.getYear( year ) ).put( Summary.NationYoungL ,
				tempVar.GB_qinglao );
		summary.get( Year.getYear( year ) ).put( Summary.NationMidLM ,
				tempVar.GB_zhonglaoM );
		summary.get( Year.getYear( year ) ).put( Summary.NationMidLF ,
				tempVar.GB_zhonglingF );
		summary.get( Year.getYear( year ) ).put( Summary.NationAfterM ,
				tempVar.GB_laohouM );
		summary.get( Year.getYear( year ) ).put( Summary.NationAfterF ,
				tempVar.GB_laohouF );
		summary.get( Year.getYear( year ) ).put( Summary.BirthF15 ,
				tempVar.yuLing_F15 );
		summary.get( Year.getYear( year ) ).put( Summary.BirthF20 ,
				tempVar.yuLing_F20 );
		summary.get( Year.getYear( year ) ).put( Summary.BirthM15 ,
				tempVar.hunYu_M15 );
		summary.get( Year.getYear( year ) ).put( Summary.BirthM20 ,
				tempVar.hunYu_M20 );

		// IIF(???15=0,0,???15/???15*100)
		double ylingF = tempVar.yuLing_F15 == 0 ? 0 : tempVar.hunYu_M15
				/ tempVar.yuLing_F15 * 100;
		summary.get( Year.getYear( year ) ).put( Summary.MarryXbb15 , ylingF );

		// IIF(???20=0,0,???20/???20*100)
		ylingF = tempVar.yuLing_F20 == 0 ? 0 : tempVar.hunYu_M20
				/ tempVar.yuLing_F20 * 100;

		summary.get( Year.getYear( year ) ).put( Summary.MarryXbb20 , ylingF );
		// }
		// }

		/****TODO DS:?????????????????0,????????????,??????***********/
		if ( year >= tempVar.getPolicyTime()
				&& ( year - tempVar.getPolicyTime() + 1.0 ) <= 35.0 )
		{
			// for(X = MAX_AGE-1; X >= 0 ; X--){
			// if( true ){
			// IIF(CX=1,??????B2+??????B2+??????B2+??????B2+??????B2+??????B2+??????B2+??????B2,;
			// IIF(CX=2,??????B2+??????B2+??????B2+??????B2,IIF(CX=3,??????B2+??????B2+??????B2+??????B2,;
			// IIF(CX=4,??????B2+??????B2+??????B2+??????B2,??????B2+??????B2+??????B2+??????B2))))
			// / 10000.0

			double djb = 0;
			if ( tempVar.cxI == 1 )
			{
				for ( CX cc : CX.values() )
					for ( NY nn : prclqz.core.enumLib.NY.values() )
					{
						djb += bBornBean.singleRlsB2CN.get( cc ).get( nn )
								+ bBornBean.feiRlsB2CN.get( cc ).get( nn );
					}
			} else if ( tempVar.cxI == 2 )
			{
				for ( NY nn : prclqz.core.enumLib.NY.values() )
				{
					djb += bBornBean.singleRlsB2CN.get( CX.Chengshi ).get( nn )
							+ bBornBean.feiRlsB2CN.get( CX.Chengshi ).get( nn );
				}
			} else if ( tempVar.cxI == 3 )
			{
				for ( CX cc : CX.values() )
				{
					djb += bBornBean.singleRlsB2CN.get( cc ).get( Feinong )
							+ bBornBean.feiRlsB2CN.get( cc ).get( Feinong );
				}
			} else if ( tempVar.cxI == 4 )
			{
				for ( NY nn : prclqz.core.enumLib.NY.values() )
				{
					djb += bBornBean.singleRlsB2CN.get( CX.Nongchun ).get( nn )
							+ bBornBean.feiRlsB2CN.get( CX.Nongchun ).get( nn );
				}
			} else
			{
				for ( CX cc : CX.values() )
				{
					djb += bBornBean.singleRlsB2CN.get( cc ).get( Nongye )
							+ bBornBean.feiRlsB2CN.get( cc ).get( Nongye );
				}
			}

			djb = djb / 10000;
			summary.get( Year.getYear( year ) ).put( Summary.DJBirth , djb );
			// }
			// if( true ){
			// IIF(CX=1,????????+????????+????????+????????+????????+????????+????????+????????,;
			// IIF(CX=2,????????+????????+????????+????????,IIF(CX=3,????????+????????+????????+????????,;
			// IIF(CX=4,????????+????????+????????+????????,????????+????????+????????+????????))))
			// / 10000.0
			double djc = 0;
			if ( tempVar.cxI == 1 )
			{
				for ( CX cc : CX.values() )
					for ( NY nn : prclqz.core.enumLib.NY.values() )
					{
						djc += bBornBean.singleDJCN.get( cc ).get( nn )
								+ bBornBean.shuangFeiDJCN.get( cc ).get( nn );
					}
			} else if ( tempVar.cxI == 2 )
			{
				for ( NY nn : prclqz.core.enumLib.NY.values() )
				{
					djc += bBornBean.singleDJCN.get( CX.Chengshi ).get( nn )
							+ bBornBean.shuangFeiDJCN.get( CX.Chengshi ).get(
									nn );
				}
			} else if ( tempVar.cxI == 3 )
			{
				for ( CX cc : CX.values() )
				{
					djc += bBornBean.singleDJCN.get( cc ).get( Feinong )
							+ bBornBean.shuangFeiDJCN.get( cc ).get( Feinong );
				}
			} else if ( tempVar.cxI == 4 )
			{
				for ( NY nn : prclqz.core.enumLib.NY.values() )
				{
					djc += bBornBean.singleDJCN.get( CX.Nongchun ).get( nn )
							+ bBornBean.shuangFeiDJCN.get( CX.Nongchun ).get(
									nn );
				}
			} else
			{
				for ( CX cc : CX.values() )
				{
					djc += bBornBean.singleDJCN.get( cc ).get( Nongye )
							+ bBornBean.shuangFeiDJCN.get( cc ).get( Nongye );
				}
			}
			djc = djc / 10000;
			summary.get( Year.getYear( year ) ).put( Summary.DJCouple , djc );
			// }
			// }
		}

		if ( tempVar.cxI > 5.0 )
		{
			// TODO ?????????????
			// *STORE 0 TO
			// ????????,??????B2,??????B2,????????,????????,??????B2,??????B2,;
			// *????????,????????,??????B2,??????B2,????????,????????,??????B2,??????B2,????????
		}
		//TODO ??????????
		if ( year == envParm.getEnd() )
		{
			//for(X = MAX_AGE-1; X >= 0 ; X--){
				if ( summary.get( Year.getYear( year ) ).get( Summary.Popl ) == 0.0 )	
				{
					summary.get( Year.getYear( year ) ).put(Summary.Popl ,
						summary.get( Year.getYear( year ) ).get( Summary.Male ) 
									+ summary.get( Year.getYear( year ) ).get(Summary.Female ) );
				 }
				if ( true )
				{
					summary.get( Year.getYear( year ) ).put(
							Summary.NationMid ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationMidM )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.NationMidF ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationMidL ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationMidLM )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.NationMidLF ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationAfter ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationAfterM )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.NationAfterF ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationLabor ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationLaborM )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.NationLaborF ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationOld ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationOldM )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.NationOldF ) );
				}
				if ( summary.get( Year.getYear( year ) ).get( Summary.Popl ) != 0.0 )
				{
					summary.get( Year.getYear( year ) ).put(
							Summary.NationYoungRate ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationYoung )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationOldRate ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationOld )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationLaborB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationLabor )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnYoungB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.IntnYoung )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnOldB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.IntnOld )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnLaborB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.IntnLabor )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) * 100.0 );
				}
				if ( summary.get( Year.getYear( year ) )
						.get( Summary.NationOld ) != 0.0 )
				{
					summary.get( Year.getYear( year ) ).put(
							Summary.NationHighAgeB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.HighAge )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.NationOld ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationLongAgeB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.LongAge )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.NationOld ) * 100.0 );
				}
				if ( summary.get( Year.getYear( year ) ).get( Summary.IntnOld ) != 0.0 )
				{
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnHighAgeB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.HighAge )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.IntnOld ) * 100.0 );
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnLongAgeB ,
							summary.get( Year.getYear( year ) ).get(
									Summary.LongAge )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.IntnOld ) * 100.0 );
				}
				if ( summary.get( Year.getYear( year ) )
						.get( Summary.IntnYoung ) != 0.0 )
				{
					// IIF(????<>0,????/????*100,0)
					double sn;
					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.IntnYoung ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.IntnOld )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.IntnYoung ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnOldYoungB , sn );
				}
				if ( summary.get( Year.getYear( year ) ).get(
						Summary.NationYoung ) != 0.0 )
				{
					double sn;
					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.NationYoung ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.NationOld )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.NationYoung ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////

					summary.get( Year.getYear( year ) ).put(
							Summary.NationOldYoungRate , sn );
				}
				if ( summary.get( Year.getYear( year ) )
						.get( Summary.IntnLabor ) != 0.0 )
				{
					double sn;
					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.IntnLabor ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.IntnOld )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.IntnLabor ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////

					summary.get( Year.getYear( year ) ).put(
							Summary.IntnNegOld , sn );

					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.IntnLabor ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.IntnOld )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.IntnLabor ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////
					summary.get( Year.getYear( year ) ).put(
							Summary.IntnNegYoung , sn );

					summary.get( Year.getYear( year ) ).put(
							Summary.IntnAllNeg ,
							summary.get( Year.getYear( year ) ).get(
									Summary.IntnNegOld )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.IntnNegYoung ) );

					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.IntnLabor ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.IntnQingLabor )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.IntnLabor ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////

					summary.get( Year.getYear( year ) ).put(
							Summary.IntnQingLaborB , sn );
				}

				double sn;
				if ( summary.get( Year.getYear( year ) ).get(
						Summary.NationLabor ) != 0.0 )
				{

					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.NationLabor ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.NationOld )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.NationLabor ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////

					summary.get( Year.getYear( year ) ).put(
							Summary.NationNegOld , sn );

					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.NationLabor ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.NationYoung )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.NationLabor ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////
					summary.get( Year.getYear( year ) ).put(
							Summary.NationNegYoung , sn );
					summary.get( Year.getYear( year ) ).put(
							Summary.NationAllNeg ,
							summary.get( Year.getYear( year ) ).get(
									Summary.NationNegOld )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.NationNegYoung ) );

					// ////////////translated by Foxpro2Java Translator
					// successfully:///////////////
					if ( summary.get( Year.getYear( year ) ).get(
							Summary.NationLabor ) != 0.0 )
					{
						sn = summary.get( Year.getYear( year ) ).get(
								Summary.NationYoungL )
								/ summary.get( Year.getYear( year ) ).get(
										Summary.NationLabor ) * 100.0;
					} else
					{
						sn = 0.0;
					}
					// ///////////end of translating, by Foxpro2Java
					// Translator///////////////
					summary.get( Year.getYear( year ) ).put(
							Summary.NationQingLaborB , sn );
				}
				if ( summary.get( Year.getYear( year ) ).get( Summary.Popl ) != 0.0 )
				{
					summary.get( Year.getYear( year ) ).put(
							Summary.PeopDensity ,
							summary.get( Year.getYear( year ) ).get(
									Summary.Popl )
									* 10000.0
									/ ( tempVar.landMJ * 1000.0 / 100.0 ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.WaterAvg ,
							( tempVar.waterS * 10000.0 )
									/ summary.get( Year.getYear( year ) ).get(
											Summary.Popl ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.LandAvg ,
							( tempVar.landMJ * 1000.0 * 15.0 )
									/ ( summary.get( Year.getYear( year ) )
											.get( Summary.Popl ) * 10000.0 ) );
				}
				if ( true )
				{
					summary.get( Year.getYear( year ) ).put(
							Summary.NDJBirth ,
							summary.get( Year.getYear( year ) ).get(
									Summary.Birth )
									- summary.get( Year.getYear( year ) ).get(
											Summary.DJBirth ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.BirthErhai ,
							summary.get( Year.getYear( year ) ).get(
									Summary.PolicyErhai )
									+ summary.get( Year.getYear( year ) ).get(
											Summary.OverBirth ) );
					summary.get( Year.getYear( year ) ).put(
							Summary.AutoInc ,
							summary.get( Year.getYear( year ) ).get(
									Summary.Birth )
									- summary.get( Year.getYear( year ) ).get(
											Summary.Death ) );
				}
			}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		/***********test ??*************/
		//EnumTools.outputSummary(summary.get(Year.getYear(year)), tempVar.cxI, year, dqx, "d:\\prclqz\\??????");
		/***********test End************/
		
	}

	@Override
	public Message checkDatabase ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParam ( String params , int type ) throws MethodException
	{
		// TODO Auto-generated method stub

	}

}

