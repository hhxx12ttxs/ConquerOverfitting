package prclqz.methods;

import static prclqz.core.Const.MAX_AGE;
import static prclqz.core.enumLib.Policy.getPolicyById;

import java.util.Map;


import java.util.HashMap;

import prclqz.DAO.IDAO;
import prclqz.DAO.MyDAOImpl;
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.MainTaskBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.Const;
import prclqz.core.Message;
import prclqz.core.StringList;
import prclqz.core.enumLib.Abstract;
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.BabiesBorn;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.Policy;
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
 * ????
 * @author Jack Long
 * @email  prclqz@zju.edu.cn
 *
 */
public class MMainExtremeValues implements IMainExtremeValuesMethod
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
		int year = 2011;
		// //////////setup///////////////////
		tempVar.setProvince(dqx);
		tempVar.setYear(year);
		tempVar.setPolicy(Policy.danDu);
		tempVar.setPolicyTime(2011);
		tempVar.setNowN1D2X(2.0);
		tempVar.setNowN2D1C(1.0);
		tempVar.setNowN1D2X(2.0);
		tempVar.setNowN1D2C(1.0);
		tempVar.setNNFX(1.0);
		tempVar.setNNFC(1.0);

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

		globals.put("tempVarBean", tempVar);	
		globals.put("bornXbbBeanMap", xbbMap);
		MDepositedCouples hehe = new MDepositedCouples();
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
//		int year = tempVar.getYear();
		BornXbbBean xbb = xbbMap.get(""+dqx);
//		String diDai = xbb.getDiDai();
		String dqB = xbb.getDqName ( );
		//define local variables
		int X=0;
//		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
//		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
//		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
		//????????
//		Map<Babies,double[]> policyBabies = (Map<Babies, double[]>) predictVarMap.get("PolicyBabies"+dqx);
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
//
//		//??????
//		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX"+dqx );
//		Map<NY,Map<Year,Map<XB,double[]>>> PopulationMigrationOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfNY"+dqx );
//		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
//		
//		//??????
//		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
//		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
//		
//		
//		//??????
//		Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
//		
//		//????????
//		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
//		Map<CX,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX"+dqx );
//		Map<NY,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY"+dqx );
//		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll"+dqx );
//		
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
		
		
		//????
//		double[][] husbandRate = ( double [ ][ ] ) predictVarMap.get("HusbandRate");
		
		//?????
		ParamBean3 envParm = (ParamBean3) globals.get("SetUpEnvParamBean");
		
		//????????					
//		Map<Year,Map<PolicyBirth,Double>> PolicyBirthRate = ( Map < Year , Map < PolicyBirth , Double >> ) predictVarMap.get ( "PolicyBirthRate"+dqx );
		
		//?????
//		Map<Year,Map<BabiesBorn,Double>> babiesBorn = ( Map < Year , Map < BabiesBorn , Double >> ) predictVarMap.get ( "BabiesBorn"+dqx );
		
		//????_??
		Map<Year,Map<Summary,Double >> SummaryOfAll = ( Map < Year , Map < Summary , Double >> ) predictVarMap.get ( "SummaryOfAll"+dqx );
		Map<CX,Map<Year,Map<Summary,Double >>> SummaryOfCX = ( Map < CX , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfCX"+dqx );
		Map<NY,Map<Year,Map<Summary,Double >>> SummaryOfNY = ( Map < NY , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfNY"+dqx );
		
		//??????????				
	//	Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration = ( Map < Year , Map < SumBM , Double >> ) predictVarMap.get ( "SummaryOfBirthAndMigration");
	//	Map<Year,Map<SumBM,Double>> sumBM = SummaryOfBirthAndMigration;
		
		//????????
		Map<Abstract,Double> PolicySimulationAbstract = (Map< Abstract , Double >) predictVarMap.get( "PolicySimulationAbstract" ); 
		/////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		
		Map<Year,Map<Summary,Double >> summary;
		String cxB = "";
		switch ( tempVar.cxI )
		{
		case 1 :
			summary = SummaryOfAll;
			cxB = " ";
			break;
		case 2 :
			summary = SummaryOfCX.get( CX.Chengshi );
			cxB = "??";
			break;
		case 3 :
			summary = SummaryOfNY.get( NY.Feinong );
			cxB = "??";
			break;
		case 4 :
			summary = SummaryOfCX.get( CX.Nongchun );
			cxB = "??";
			break;
		case 5 :
			summary = SummaryOfNY.get( NY.Nongye );
			cxB = "??";
			break;
		default :
			summary = null;
		}
		
		
		int yx;
		double MA1 = Integer.MIN_VALUE,
			MA2 = Integer.MIN_VALUE,
			MI1 = Integer.MAX_VALUE,
			MI2 = Integer.MAX_VALUE,
			MATFR0 = Integer.MIN_VALUE;
		int MAXND1 = 0,MAXND2 = 0,MIND1 = 0,MIND2 = 0,MAND3 = 0;
		System.out.println(envParm.getBegin());
		if(summary.get( Year.getYear( envParm.getBegin() ) ) == null)
			System.out.println("null");
		for(yx = envParm.getBegin(); yx<=envParm.getEnd();yx++){
			if(summary.get( Year.getYear( yx ) ).get( Summary.Popl ) > MA1 ){
				MA1 = summary.get( Year.getYear( yx ) ).get( Summary.Popl );
				MAXND1 = yx;
			}
			if(summary.get( Year.getYear( yx ) ).get( Summary.InMig ) > MA2 ){
				MA2 = summary.get( Year.getYear( yx ) ).get( Summary.InMig );
				MAXND2 = yx;
			}
			if(summary.get( Year.getYear( yx ) ).get( Summary.Popl ) < MI1 ){
				MI1 = summary.get( Year.getYear( yx ) ).get( Summary.Popl );
				MIND1 = yx;
			}
			if(summary.get( Year.getYear( yx ) ).get( Summary.InMig ) < MI2 ){
				MI2 = summary.get( Year.getYear( yx ) ).get( Summary.InMig );
				MIND2 = yx;
			}
			if(summary.get( Year.getYear( yx ) ).get( Summary.ImplTFR ) > MATFR0 ){
				MATFR0 = summary.get( Year.getYear( yx ) ).get( Summary.ImplTFR );
				MAND3 = yx;
			}
		}
		
		yx = 2020;
		double ZRK20,CSRK20,CSL20,TFR20,JQR20,ZZL20,CSH20;
		ZRK20 = summary.get( Year.getYear( yx ) ).get( Summary.Popl );
		CSRK20 = summary.get( Year.getYear( yx ) ).get( Summary.Birth );
		CSL20 = summary.get( Year.getYear( yx ) ).get( Summary.BirthRate );
		TFR20 = summary.get( Year.getYear( yx ) ).get( Summary.ImplTFR );
		JQR20 = summary.get( Year.getYear( yx ) ).get( Summary.InMigRate );
		ZZL20 = summary.get( Year.getYear( yx ) ).get( Summary.TotalIncRate );
		CSH20 = summary.get( Year.getYear( yx ) ).get( Summary.ChengshiB );
		
		Map< Abstract , Double > pysim = PolicySimulationAbstract;
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				pysim.put( Abstract.DJMoShi , strValues
						.add( SYSFMSField.getChinese( SYSFMSField
								.getSFMSfromId( tempVar.SFMS ) ) ) );
				// strValues.add( Policy.getChinese( Policy.getPolicyById(
				// tempVar.feiNongPolicy1 ) ));
				pysim.put( Abstract.Policy1 , strValues.add( Policy
						.getChinese( Policy
								.getPolicyById( tempVar.feiNongPolicy1 ) ) ) );
				pysim.put( Abstract.Time1 , (double) tempVar.feiNongTime1 );
				pysim.put( Abstract.Policy2 , strValues.add( Policy
						.getChinese( Policy
								.getPolicyById( tempVar.feiNongPolicy2 ) ) ) );
				pysim.put( Abstract.Time2 , (double) tempVar.feiNongTime2 );
				pysim.put( Abstract.Policy3 , strValues.add( Policy
						.getChinese( Policy
								.getPolicyById( tempVar.feiNongPolicy3 ) ) ) );
				pysim.put( Abstract.Time3 , (double) tempVar.feiNongTime3 );
				pysim.put( Abstract.DiQuB , strValues.add( dqB ) );
				pysim.put( Abstract.CXB , strValues.add( cxB ) );

				String FA = Policy
						.getChinese( getPolicyById( tempVar.feiNongPolicy1 ) )
						+ ( tempVar.feiNongTime1 == 0 ? "" : ""
								+ tempVar.feiNongTime1 )
						+ Policy
								.getChinese( getPolicyById( tempVar.feiNongPolicy2 ) )
						+ ( tempVar.feiNongTime2 == 0 ? "" : ""
								+ tempVar.feiNongTime2 )
						+ Policy
								.getChinese( getPolicyById( tempVar.feiNongPolicy3 ) )
						+ ( tempVar.feiNongTime3 == 0 ? "" : ""
								+ tempVar.feiNongTime3 );

				pysim.put( Abstract.BornPlan , strValues.add( FA ) );

				String QY = ( tempVar.QY == 0 ? "??" : ( tempVar.QY == 1 ? "??"
						: ( tempVar.QY == 2 ? "??" : "??" ) ) );
				pysim.put( Abstract.MigratePlan , strValues.add( QY ) );
				pysim.put( Abstract.MaxGuimoND , (double) MAXND1 );
				pysim.put( Abstract.MaxGuimo , MA1 );
				pysim.put( Abstract.MinGuimoND , (double) MIND1 );
				pysim.put( Abstract.MinGuimo , MI1 );
				pysim.put( Abstract.MaxMigrateND , (double) MAXND2 );
				pysim.put( Abstract.MaxMigrate , MA2 );
				pysim.put( Abstract.MinMigrateND , (double) MIND2 );
				pysim.put( Abstract.MinMigrate , MI2 );
				pysim.put( Abstract.MaxTFRND , (double) MAND3 );
				pysim.put( Abstract.MaxTFR , MATFR0 );
				pysim.put( Abstract.T_PPL20 , ZRK20 );
				pysim.put( Abstract.Born20 , CSRK20 );
				pysim.put( Abstract.BornRate20 , CSL20 );
				pysim.put( Abstract.BirthRate20 , TFR20 );
				pysim.put( Abstract.IngrateRate20 , JQR20 );
				pysim.put( Abstract.T_incr_Rate20 , ZZL20 );
				pysim.put( Abstract.CSH20 , CSH20 );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		/**********test??***********/
		System.out.println(tempVar.cxI);
		System.out.println(MAXND1);
		System.out.println(MAXND2);
		System.out.println(MIND1);
		System.out.println(MIND2);
		System.out.println(MAND3);
		System.out.println(ZRK20);
		System.out.println(CSRK20);
		System.out.println(CSL20);
		System.out.println(TFR20);
		System.out.println(JQR20);
		System.out.println(ZZL20);
		System.out.println(CSH20);
		for(Abstract abs:Abstract.values()){
			System.out.println(Abstract.getChinese(abs));
			System.out.println(pysim.get(abs));
		}
		/**********test END*********/

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

