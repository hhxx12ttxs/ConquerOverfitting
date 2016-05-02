package prclqz.methods;

import static prclqz.core.enumLib.NY.Feinong;
import static prclqz.core.enumLib.NY.Nongye;
import static prclqz.core.enumLib.Policy.getPolicyById;

import java.util.Map;


import java.util.HashMap;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;
import static prclqz.core.Const.*;
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
 * ??????
 * @author prclqz@zju.edu.cn
 *
 */
public class MAbstractOfNation implements IAbstractOfNationMethod
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
	public void calculate ( IDAO m , HashMap< String , Object > globals ,
			IBabiesRateMethod bRate , 
			INationalBornRateMethod nationBRate ,
			IRecordNationalPolicyAndPolicyRateMethod recordPolicy ,
			IDeathCalculatingMethod deathCal ,
			ICalculateExpectedAgeMethod calAge , 
			IGroupingByAgeMethod groupAge ,
			IResultOfAbstractMethod resultAbs ,
			IMainExtremeValuesMethod mainVals ) throws Exception
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
		String diDai = xbb.getDiDai();
		String dqB = xbb.getDqName ( );
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
		//??  ?? ???????
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>)predictVarMap.get( "HunpeiOfCXNY"+dqx);
		Map<CX,Map<HunpeiField,double[]>> ziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX"+dqx);
		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx);
		Map<HunpeiField,double[]> ziNvOfAll = (Map<HunpeiField, double[]>) predictVarMap.get( "HunpeiOfAll"+dqx);
			//??
			Map<CX,Map<NY,Map<HunpeiField,double[]>>> NationziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY" );
			Map<CX,Map<HunpeiField,double[]>> NationziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX" );
			Map<NY,Map<HunpeiField,double[]>> NationziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY" );
			Map<HunpeiField,double[]> NationziNvOfAll = (Map< HunpeiField , double[] >) predictVarMap.get( "HunpeiOfAll" );
		
		//???????(???)
		Map<CX,Map<Babies,double[]>> BirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfCX"+dqx );
		Map<NY,Map<Babies,double[]>> BirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfNY"+dqx );
		Map<Babies,double[]> BirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "BirthPredictOfAll"+dqx );
			//??
			Map<CX,Map<Babies,double[]>> NationBirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfCX" );
			Map<NY,Map<Babies,double[]>> NationBirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfNY" );
			Map<Babies,double[]> NationBirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "BirthPredictOfAll" );
		
		//???????---?????
		Map<CX,Map<Babies,double[]>> OverBirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfCX"+dqx );
		Map<NY,Map<Babies,double[]>> OverBirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfNY"+dqx );
		Map<Babies,double[]> OverBirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "OverBirthPredictOfAll"+dqx );
		Map<Babies,double[]> PolicyBabies = ( Map < Babies , double [ ] > ) predictVarMap.get ( "PolicyBabies"+dqx );
			//??
			Map<CX,Map<Babies,double[]>> NationOverBirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfCX" );
			Map<NY,Map<Babies,double[]>> NationOverBirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfNY" );
			Map<Babies,double[]> NationOverBirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "OverBirthPredictOfAll" );
			Map<Babies,double[]> NationPolicyBabies = ( Map < Babies , double [ ] > ) predictVarMap.get ( "PolicyBabies" );
		
		//??????
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "PopulationPredictOfCXNY"+dqx );
		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );
			//??
			Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationPopulationPredictOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "PopulationPredictOfCXNY" );
			Map<CX,Map<Year,Map<XB,double[]>>>NationPopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX" );
			Map<NY,Map<Year,Map<XB,double[]>>> NationPopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY" );
			Map<Year,Map<XB,double[]>> NationPopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll" );

		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
			//??
			Map<CX,Map<Year,Map<XB,double[]>>> NationPopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX" );
			Map<Year,Map<XB,double[]>> NationPopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll" );
		
		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
			//??
			Map<CX,Map<Year,Map<XB,double[]>>> NationPopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX" );
			Map<Year,Map<XB,double[]>> NationPopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll" );
		
		
		//??????
		Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
		
		//????????
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
		Map<CX,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX"+dqx );
		Map<NY,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY"+dqx );
		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll"+dqx );
			//??
			Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> NationCoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY" );
			Map<CX,Map<Year,Map<Couple,Double>>> NationCoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX" );
			Map<NY,Map<Year,Map<Couple,Double>>> NationCoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY" );
			Map<Year,Map<Couple,Double>> NationCoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll" );
		
		
		//??????--??  //??????--?? CXNY
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx );
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY"+dqx );
			//??
			Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationSonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY" );
			Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationSonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY" );
		// ?????? CX,NY
		Map<CX,Map<Year,Map<XB,double[]>>>SonDiePopulationPredictOfTeFuCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCX"+dqx ),
			SonDiePopulationPredictOfYiHaiCX=( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfTeFuNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuNY"+dqx ),
			SonDiePopulationPredictOfYiHaiNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiNY"+dqx );
			// ??
			Map<CX,Map<Year,Map<XB,double[]>>>NationSonDiePopulationPredictOfTeFuCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCX" ),
			NationSonDiePopulationPredictOfYiHaiCX=( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCX" );
			Map<NY,Map<Year,Map<XB,double[]>>> NationSonDiePopulationPredictOfTeFuNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuNY" ),
			NationSonDiePopulationPredictOfYiHaiNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiNY" );
		//All
		Map<Year,Map<XB,double[]>> SonDiePopulationPredictOfTeFuAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuAll"+dqx ),
			SonDiePopulationPredictOfYiHaiAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiAll"+dqx );
			//??
			Map<Year,Map<XB,double[]>> NationSonDiePopulationPredictOfTeFuAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuAll" ),
			NationSonDiePopulationPredictOfYiHaiAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiAll" );
		
		
		//????
		double[][] husbandRate = ( double [ ][ ] ) predictVarMap.get("HusbandRate");
		
		//?????
		ParamBean3 envParm = (ParamBean3) globals.get("SetUpEnvParamBean");
		
		//????????					
		Map<Year,Map<PolicyBirth,Double>> PolicyBirthRate = ( Map < Year , Map < PolicyBirth , Double >> ) predictVarMap.get ( "PolicyBirthRate"+dqx );
		
		//?????
		Map<Year,Map<BabiesBorn,Double>> babiesBorn = ( Map < Year , Map < BabiesBorn , Double >> ) predictVarMap.get ( "BabiesBorn"+dqx );
		
		//????_??
		Map<Year,Map<Summary,Double >> SummaryOfAll = ( Map < Year , Map < Summary , Double >> ) predictVarMap.get ( "SummaryOfAll"+dqx );
		Map<CX,Map<Year,Map<Summary,Double >>> SummaryOfCX = ( Map < CX , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfCX"+dqx );
		Map<NY,Map<Year,Map<Summary,Double >>> SummaryOfNY = ( Map < NY , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfNY"+dqx );
			//??
			Map<Year,Map<Summary,Double >> NationSummaryOfAll = ( Map < Year , Map < Summary , Double >> ) predictVarMap.get ( "SummaryOfAll" );
			Map<CX,Map<Year,Map<Summary,Double >>> NationSummaryOfCX = ( Map < CX , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfCX" );
			Map<NY,Map<Year,Map<Summary,Double >>> NationSummaryOfNY = ( Map < NY , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfNY" );
		
		//??????????				
		Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration = ( Map < Year , Map < SumBM , Double >> ) predictVarMap.get ( "SummaryOfBirthAndMigration"+"0");
		Map<Year,Map<SumBM,Double>> sumBM = SummaryOfBirthAndMigration;
		
		//????????
		Map<Abstract,Double> PolicySimulationAbstract = (Map< Abstract , Double >) predictVarMap.get( "PolicySimulationAbstract" ); 
		
		//?????
		Map<Year,Map<BabiesBorn,Double>> GiveBabiesBorn = (Map< Year , Map< BabiesBorn , Double >>) predictVarMap.get( "BabiesBorn"+dqx );
			//??
			Map<Year,Map<BabiesBorn,Double>> NationGiveBabiesBorn = (Map< Year , Map< BabiesBorn , Double >>) predictVarMap.get( "BabiesBorn" );
		
		//??????????
		Map<Year,double[]> BirthRatePredict = (Map< Year , double[] >) predictVarMap.get( "BirthRatePredict"+dqx );
		Map<Year,double[]> NationBirthRatePredict = (Map< Year , double[] >) predictVarMap.get( "BirthRatePredict" );
		/////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		
//		NationBirthRatePredict.get( Year.getYear( year ) )[X]
		for(CX cx: CX.values())
			for(NY ny : NY.values()){
				tempVar.cx = cx;
				tempVar.ny = ny;
				Map< HunpeiField , double[] > zn = ziNv.get( cx ).get( ny );
				//??????
				bRate.calculate( m , globals );
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				double hunneiTFR = 0;
				for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( ( zn.get( HunpeiField.DD )[ X ]
							+ zn.get( HunpeiField.NMDF )[ X ]
							+ zn.get( HunpeiField.NFDM )[ X ]
							+ zn.get( HunpeiField.NN )[ X ]
							+ zn.get( HunpeiField.SingleDJ )[ X ]
							+ zn.get( HunpeiField.ShuangfeiDJ )[ X ]
							+ zn.get( HunpeiField.Yi2Single )[ X ] + zn
							.get( HunpeiField.Yi2Shuangfei )[ X ] ) != 0.0 )
					{
						hunneiTFR += ( zn.get( HunpeiField.DDB )[ X ]
								+ zn.get( HunpeiField.NMDFB )[ X ]
								+ zn.get( HunpeiField.NFDMB )[ X ]
								+ zn.get( HunpeiField.NNB )[ X ]
								+ zn.get( HunpeiField.SingleRls )[ X ] + zn
								.get( HunpeiField.ShuangFeiRls )[ X ] )
								/ ( zn.get( HunpeiField.DD )[ X ]
										+ zn.get( HunpeiField.NMDF )[ X ]
										+ zn.get( HunpeiField.NFDM )[ X ]
										+ zn.get( HunpeiField.NN )[ X ]
										+ zn.get( HunpeiField.SingleDJ )[ X ]
										+ zn.get( HunpeiField.ShuangfeiDJ )[ X ]
										+ zn.get( HunpeiField.Yi2Single )[ X ] + zn
										.get( HunpeiField.Yi2Shuangfei )[ X ] );
					}
				}
				double danfangB2 = 0;
				for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( true )
					{
						danfangB2 += zn.get( HunpeiField.SingleRls )[ X ];
					}
				}
				double feifangB2 = 0;
				for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( true )
					{
						feifangB2 += zn.get( HunpeiField.ShuangFeiRls )[ X ];
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				bBornBean.singleRlsB2CN.get( cx ).put( ny , danfangB2 );
				bBornBean.feiRlsB2CN.get( cx ).put( ny , feifangB2 );
				bBornBean.marriageTFRCN.get( cx ).put( ny , hunneiTFR );
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				
				bBornBean.singleDJCN.get( cx ).put( ny , 0.0 );
				bBornBean.shuangFeiDJCN.get( cx ).put( ny , 0.0 );
				for(X = Const.MAX_AGE-1; X >= 0 ; X--){
				if( X >= 15.0 && X <= 49.0 ){
					bBornBean.singleDJCN.get( cx ).put( ny , 
							bBornBean.singleDJCN.get( cx ).get( ny )+
							zn.get( HunpeiField.SingleDJ)[X]);
					bBornBean.shuangFeiDJCN.get( cx ).put( ny ,
							bBornBean.shuangFeiDJCN.get( cx ).get( ny )+
							zn.get( HunpeiField.ShuangfeiDJ)[X]);
				}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
			}//END-FOR-CX-NY
		
		
		double [] ARFND1 = new double[Const.MAX_AGE];
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1;X >= 0;X -- )
		{
			if ( true )
			{
				ARFND1 [ X ] = NationPopulationPredictOfAll.get ( Year.getYear ( year - 1 ) )
						.get ( XB.Female ) [ X ];
			}
		}
		
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		Map< Year , Map< XB , double[] >> n_pplPredict = NationPopulationPredictOfAll;
		Map< Babies , double[] > n_policyBabies = NationPolicyBabies;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + n_pplPredict.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) > 0.0 )
			{
				NationBirthRatePredict.get( Year.getYear( year ) )[ X ] = NationOverBirthPredictOfAll
						.get( Babies.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + n_pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.dqImplTFR0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + n_pplPredict.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) > 0.0 )
			{
				tempVar.dqImplTFR0 += ( NationBirthPredictOfAll.get( Babies
						.getBabies( year ) )[ X ] + NationOverBirthPredictOfAll
						.get( Babies.getBabies( year ) )[ X ] )
						/ ( ( ARFND1[ X - 1 ] + n_pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.dqOverBrithTFR = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + n_pplPredict.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) > 0.0 )
			{
				tempVar.dqOverBrithTFR += ( NationOverBirthPredictOfAll
						.get( Babies.getBabies( year ) )[ X ] )
						/ ( ( ARFND1[ X - 1 ] + n_pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.dqPolicyTFR0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + n_pplPredict.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) > 0.0 )
			{
				tempVar.dqPolicyTFR0 += n_policyBabies.get( Babies
						.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + n_pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.DqPlyBorn = 0;
		tempVar.XFNPlyBorn = 0;
		tempVar.CFNPlyBorn = 0;
		tempVar.XNYPlyBorn = 0;
		tempVar.CNYPlyBorn = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.DqPlyBorn += n_policyBabies.get( Babies
						.getBabies( year ) )[ X ];
				tempVar.XFNPlyBorn += NationziNv.get( CX.Nongchun ).get(
						NY.Feinong ).get( HunpeiField.PlyBorn )[ X ];
				tempVar.CFNPlyBorn += NationziNv.get( CX.Chengshi ).get(
						NY.Feinong ).get( HunpeiField.PlyBorn )[ X ];
				tempVar.XNYPlyBorn += NationziNv.get( CX.Nongchun ).get(
						NY.Nongye ).get( HunpeiField.PlyBorn )[ X ];
				tempVar.CNYPlyBorn += NationziNv.get( CX.Chengshi ).get(
						NY.Nongye ).get( HunpeiField.PlyBorn )[ X ];
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCX.get( CX.Chengshi )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.cxImplTFR0.put( CX.Chengshi , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ARFND1[ X - 1 ]
							+ NationPopulationPredictOfCX.get( CX.Chengshi )
									.get( Year.getYear( year ) )
									.get( XB.Female )[ X ] != 0.0 )
			{
				tempVar.cxImplTFR0.put( CX.Chengshi , ( tempVar.cxImplTFR0
						.get( CX.Chengshi ) + NationBirthPredictOfCX.get(
						CX.Chengshi ).get( Babies.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
								.get( CX.Chengshi ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCX.get( CX.Nongchun )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.cxImplTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ARFND1[ X - 1 ]
							+ NationPopulationPredictOfCX.get( CX.Nongchun )
									.get( Year.getYear( year ) )
									.get( XB.Female )[ X ] != 0.0 )
			{
				tempVar.cxImplTFR0.put( CX.Nongchun , ( tempVar.cxImplTFR0
						.get( CX.Nongchun ) + NationBirthPredictOfCX.get(
						CX.Nongchun ).get( Babies.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
								.get( CX.Nongchun ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfNY.get( NY.Feinong )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.nyImplTFR0.put( NY.Feinong , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ARFND1[ X - 1 ]
							+ NationPopulationPredictOfNY.get( NY.Feinong )
									.get( Year.getYear( year ) )
									.get( XB.Female )[ X ] != 0.0 )
			{
				tempVar.nyImplTFR0.put( NY.Feinong , ( tempVar.nyImplTFR0
						.get( NY.Feinong ) + NationBirthPredictOfNY.get(
						NY.Feinong ).get( Babies.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY
								.get( NY.Feinong ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfNY.get( NY.Nongye ).get(
						Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.nyImplTFR0.put( NY.Nongye , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ARFND1[ X - 1 ]
							+ NationPopulationPredictOfNY.get( NY.Nongye ).get(
									Year.getYear( year ) ).get( XB.Female )[ X ] != 0.0 )
			{
				tempVar.nyImplTFR0.put( NY.Nongye , ( tempVar.nyImplTFR0
						.get( NY.Nongye ) + NationBirthPredictOfNY.get(
						NY.Nongye ).get( Babies.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY
								.get( NY.Nongye ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.overBirthS = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.overBirthS += NationOverBirthPredictOfAll.get( Babies
						.getBabies( year ) )[ X ];
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

		//??????
		nationBRate.calculate( m , globals , bRate );
		recordPolicy.calculate( m , globals );
		
		
		double td = strValues.add ( Policy.getChinese( getPolicyById ( tempVar.feiNongPolicy1 ) )
				+ ( tempVar.feiNongTime1 == 0 ? "" : "" + tempVar.feiNongTime1 )
				+ Policy.getChinese( getPolicyById ( tempVar.feiNongPolicy2 ) )
				+ ( tempVar.feiNongTime2 == 0 ? "" : "" + tempVar.feiNongTime2 )
				+ Policy.getChinese( getPolicyById ( tempVar.feiNongPolicy3 ) )
				+ ( tempVar.feiNongTime3 == 0 ? "" : "" + tempVar.feiNongTime3 )
				+ ( tempVar.QY == 0 ? "??" : ( tempVar.QY == 1 ? "??"
						: ( tempVar.QY == 2 ? "??" : "??" ) ) ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.Dq , strValues.add ( "??" ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.Policy , td );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.ImplTFR ,
				tempVar.dqImplTFR0 );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.PolicyBirtRate ,
				tempVar.dqPolicyTFR0 );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.OverBirthRate ,
				tempVar.dqOverBrithTFR );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.OverBirthNum ,
				tempVar.overBirthS );

		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.DQTFR , tempVar.dqPolicyTFR0 );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.CZTFR ,
				tempVar.cxPolicyTFR0.get ( CX.Chengshi ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.FNTFR ,
				tempVar.nyPolicyTFR0.get ( Feinong ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NCTFR ,
				tempVar.cxPolicyTFR0.get ( CX.Nongchun ) );
		sumBM.get ( Year.getYear ( year ) ).put ( SumBM.NYTFR ,
				tempVar.nyPolicyTFR0.get ( Nongye ) ); 
		sumBM.get( Year.getYear( year ) ).put( SumBM.QGMigS , (double)tempVar.SQY );
		
		//????TFR0=???TFR
		tempVar.nationalImplableTFR0 = sumBM.get( Year.getYear( year ) ).get( SumBM.ImplTFR );
		
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		double CZRK = 0;
		double NCRK = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				CZRK += ( NationPopulationPredictOfCX.get( CX.Chengshi ).get(
						Year.getYear( year ) ).get( XB.Male )[ X ] + NationPopulationPredictOfCX
						.get( CX.Chengshi ).get( Year.getYear( year ) ).get(
								XB.Female )[ X ] ) / 10000.0;
				NCRK += ( NationPopulationPredictOfCX.get( CX.Nongchun ).get(
						Year.getYear( year ) ).get( XB.Male )[ X ] + NationPopulationPredictOfCX
						.get( CX.Nongchun ).get( Year.getYear( year ) ).get(
								XB.Female )[ X ] ) / 10000.0;
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

		tempVar.CSHLevel = CZRK / (NCRK +CZRK) * 100;
		Map < Year , Map < XB , double [ ] >> pplPredict,pplMig,pplDeath;
		Map<HunpeiField,double[]> zn;
		Map< Babies , double[] > birthPredict;
		Map< Year , Map< Summary , Double >> sum ;
		for(int cx=1; cx<=5; cx++)
		{
			tempVar.cxI = cx;
			switch(cx)
			{
			case 1:
				pplPredict = NationPopulationPredictOfAll;
				zn = NationziNvOfAll;
				birthPredict = NationBirthPredictOfAll;
				pplDeath = NationPopulationDeathOfAll;
				pplMig = NationPopulationMigrationOfAll;
				sum = SummaryOfAll;
				break;
			case 2:
				pplPredict = NationPopulationPredictOfCX.get ( CX.Chengshi );
				zn = NationziNvOfCX.get( CX.Chengshi );
				birthPredict = NationBirthPredictOfCX.get( CX.Chengshi );
				pplDeath = NationPopulationDeathOfCX.get( CX.Chengshi );
				pplMig = NationPopulationMigrationOfCX.get( CX.Chengshi );
				sum = SummaryOfCX.get( CX.Chengshi );
				break;
			case 3:
				pplPredict = NationPopulationPredictOfNY.get ( Feinong );
				zn = NationziNvOfNY.get ( Feinong );
				birthPredict = NationBirthPredictOfNY.get( Feinong );
				pplDeath = null;
				pplMig = null;
				sum = SummaryOfNY.get( Feinong );
				break;
			case 4:
				pplPredict = NationPopulationPredictOfCX.get ( CX.Nongchun );
				zn = NationziNvOfCX.get( CX.Nongchun );
				birthPredict = NationBirthPredictOfCX.get( CX.Nongchun );
				pplDeath = NationPopulationDeathOfCX.get( CX.Nongchun );
				pplMig = NationPopulationMigrationOfCX.get( CX.Nongchun );
				sum = SummaryOfCX.get( CX.Nongchun );
				break;
			case 5:
				pplPredict = NationPopulationPredictOfNY.get ( Nongye );
				zn = NationziNvOfNY.get ( Nongye );
				birthPredict = NationBirthPredictOfNY.get( Nongye );
				pplDeath = null;
				pplMig = null;
				sum = SummaryOfNY.get( Nongye );
				break;
			default: 
				pplPredict = null;
				birthPredict = null;
				zn = null;
				pplDeath = null;
				pplMig = null;
				sum = null;
			}
			
			tempVar.bornPopulation = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				tempVar.bornPopulation += ( birthPredict.get( Babies
						.getBabies( year ) )[ X ] + zn
						.get( HunpeiField.Over_Birth )[ X ] ) / 10000;
			}
			
			for ( X = MAX_AGE - 1;X >= 0;X -- )
			{
				if ( true )
				{
					ARFND1 [ X ] = pplPredict.get ( Year.getYear ( year - 1 ) )
							.get ( XB.Female ) [ X ];
				}
			}
			
//&????..&FND2			pplPredict.get( Year.getYear( year ) ).get( XB.Female )[X]
//&????..&BX  			birthPredict.get( Babies.getBabies( year ) )[ X ]
//&????	 				zn.get( HunpeiField.x )[ X ]
//????					pplMig.get( Year.getYear( year ) ).get( XB.Female )[X]
//????					pplDeath.get( Year.getYear( year ) ).get( XB.Female )[X]
			//////////////translated by Foxpro2Java Translator successfully:///////////////
			tempVar.dqPolicyTFR0 = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( X > 1.0
						&& ( ARFND1[ X - 1 ] + pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) != 0.0 )
				{
					tempVar.dqPolicyTFR0 += birthPredict.get( Babies
							.getBabies( year ) )[ X ]
							/ ( ( ARFND1[ X - 1 ] + pplPredict.get(
									Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
				}
			}
			tempVar.implableTFR0 = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( X > 1.0
						&& ( ARFND1[ X - 1 ] + pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) != 0.0 )
				{
					tempVar.implableTFR0 += ( birthPredict.get( Babies
							.getBabies( year ) )[ X ] + zn
							.get( HunpeiField.Over_Birth )[ X ] )
							/ ( ( ARFND1[ X - 1 ] + pplPredict.get(
									Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
				}
			}
			if ( cx == 1.0 )
			{
				tempVar.dqImplTFR0 = tempVar.implableTFR0;
			} else if ( cx == 2.0 )
			{
				tempVar.cxImplTFR0.put( CX.Chengshi , tempVar.implableTFR0 );
			} else if ( cx == 3.0 )
			{
				tempVar.nyImplTFR0.put( NY.Feinong , tempVar.implableTFR0 );
			} else if ( cx == 4.0 )
			{
				tempVar.cxImplTFR0.put( CX.Nongchun , tempVar.implableTFR0 );
			} else if ( cx == 5.0 )
			{
				tempVar.nyImplTFR0.put( NY.Nongye , tempVar.implableTFR0 );
			}
			/////////////end of translating, by Foxpro2Java Translator///////////////
			
			switch(cx){
			case 1:
				tempVar.dqImplTFR0 = tempVar.implableTFR0;
				break;
			case 2:
				tempVar.cxImplTFR0.put( CX.Chengshi , tempVar.implableTFR0 );
				break;
			case 3:
				tempVar.nyImplTFR0.put( NY.Feinong , tempVar.implableTFR0 );
				break;
			case 4:
				tempVar.cxImplTFR0.put( CX.Nongchun , tempVar.implableTFR0 );
				break;
			case 5:
				tempVar.nyImplTFR0.put( NY.Nongye , tempVar.implableTFR0 );
				break;
			}
			
			//////////////translated by Foxpro2Java Translator successfully:///////////////
			if ( cx <= 2.0 || cx == 4.0 )
			{
				tempVar.inMigRK = 0;
				for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( true )
					{
						tempVar.inMigRK += ( pplMig.get( Year.getYear( year ) )
								.get( XB.Male )[ X ] + pplMig.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
					}
				}
				tempVar.deathM = 0;
				for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( true )
					{
						tempVar.deathM += pplDeath.get( Year.getYear( year ) )
								.get( XB.Male )[ X ] / 10000.0;
					}
				}
				tempVar.deathF = 0;
				for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( true )
					{
						tempVar.deathF += pplDeath.get( Year.getYear( year ) )
								.get( XB.Female )[ X ] / 10000.0;
					}
				}
				
				//????????TODO
				deathCal.calculate( m , globals );
				calAge.calculate( m , globals );
				
				
				if ( cx == 2.0 || cx == 3.0 )
				{
					tempVar.CEM0 = tempVar.EM0;
					tempVar.CEF0 = tempVar.EF0;
				} else if ( cx == 4.0 || cx == 5.0 )
				{
					tempVar.XEM0 = tempVar.EM0;
					tempVar.XEF0 = tempVar.EF0;
				}
			} else
			{
				tempVar.inMigRK = 0;
				for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					if ( true )
					{
						tempVar.inMigRK += zn.get( HunpeiField.HJQY )[X] / 10000.0;
					}
				}
				tempVar.deathM = 0.0;
				tempVar.deathF = 0.0;
			}
			/////////////end of translating, by Foxpro2Java Translator///////////////
			SumBM FIE1;
			switch(cx){
			case 1:
				FIE1 = SumBM.DQMigS;
				break;
			case 2:
				FIE1 = SumBM.ChengshiMig;
				break;
			case 3:
				FIE1 = SumBM.FeinongMig;
				break;
			case 4:
				FIE1 = SumBM.NongchunMig;
				break;
			case 5:
				FIE1 = SumBM.NongyeMig;
				break;
			default:
				FIE1 = null;
			}
			
			sumBM.get( Year.getYear( year ) ).put( FIE1 , tempVar.inMigRK );
			
			tempVar.cxI = cx;
			tempVar.EF0 = (cx ==2 || cx== 3)? tempVar.CEF0 : ((cx == 4 || cx == 5)? tempVar.XEF0 : tempVar.EF0 );
			tempVar.EM0 = (cx ==2 || cx== 3)? tempVar.CEM0 : ((cx == 4 || cx == 5)? tempVar.XEM0 : tempVar.EM0 );
			//??????
			groupAge.calculate( m , globals );
			resultAbs.calculate( m , globals );
			
			if( cx == 1){
				sum.get( Year.getYear( year ) ).put(
						Summary.Male ,
						sum.get( Year.getYear( year ) ).get( Summary.Male )
								+ 1975
								* sum.get( Year.getYear( year ) ).get(
										Summary.Male )
								/ sum.get( Year.getYear( year ) ).get(
										Summary.Popl ) );
				sum.get( Year.getYear( year ) ).put(
						Summary.Female ,
						sum.get( Year.getYear( year ) ).get( Summary.Female )
								+ 1975
								* sum.get( Year.getYear( year ) ).get(
										Summary.Female )
								/ sum.get( Year.getYear( year ) ).get(
										Summary.Popl ) );
				sum.get( Year.getYear( year ) )
						.put(
								Summary.Popl ,
								sum.get( Year.getYear( year ) ).get(
										Summary.Popl ) + 1975 );
			}
			
			//?????? TODO
			if( year == envParm.getEnd())
				mainVals.calculate( m , globals );
			
		}//&&?????\??????
	}
	
	@Override
	public void calculate ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
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

