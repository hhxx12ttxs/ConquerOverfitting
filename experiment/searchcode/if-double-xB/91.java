package prclqz.methods;

import static prclqz.core.enumLib.NY.Feinong;
import static prclqz.core.enumLib.NY.Nongye;
import static prclqz.core.enumLib.Policy.getPolicyById;

import java.util.Map;


import java.util.HashMap;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;
import static prclqz.core.Const.*;
import prclqz.DAO.IDAO;
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
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
/**
 * ???????
 * @author Jack Long
 * @email  prclqz@zju.edu.cn
 *
 */
public class MNationalBornRate implements INationalBornRateMethod
{

	@Override
	public void calculate ( IDAO m , HashMap< String , Object > globals ,
			IBabiesRateMethod bRate ) throws Exception
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
		//?? ???????
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY"+dqx );//provincMigMap.get(""+dqx);
		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx );//provincMigMap.get("NY"+dqx);
		Map<CX,Map<HunpeiField,double[]>> ziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX"+dqx );//provincMigMap.get("CX"+dqx);
		Map<HunpeiField,double[]> ziNvOfAll = (Map<HunpeiField, double[]>) predictVarMap.get( "HunpeiOfAll"+dqx );//provincMigMap.get("All"+dqx);
			//??
			Map<CX,Map<NY,Map<HunpeiField,double[]>>> NationziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>)predictVarMap.get( "HunpeiOfCXNY" );
			Map<NY,Map<HunpeiField,double[]>> NationziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY" );
			Map<CX,Map<HunpeiField,double[]>> NationziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX" );
			Map<HunpeiField,double[]> NationziNvOfAll = (Map<HunpeiField, double[]>)predictVarMap.get( "HunpeiOfAll" );
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
		
		double [] ARFND1 = new double[MAX_AGE];
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCXNY.get( CX.Nongchun )
						.get( NY.Nongye ).get( Year.getYear( year - 1 ) ).get(
								XB.Female )[ X ];
			}
		}
		tempVar.CNTFR.get( CX.Nongchun ).put( NY.Nongye , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY.get(
							CX.Nongchun ).get( NY.Nongye ).get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.CNTFR
						.get( CX.Nongchun )
						.put(
								NY.Nongye ,
								( tempVar.CNTFR.get( CX.Nongchun ).get(
										NY.Nongye ) + ( NationziNv.get(
										CX.Nongchun ).get( NY.Nongye ).get(
										HunpeiField.DDB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Nongye ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Nongye ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Nongye ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Nongye ).get(
												HunpeiField.SingleRls )[ X ] + NationziNv
										.get( CX.Nongchun ).get( NY.Nongye )
										.get( HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY
												.get( CX.Nongchun ).get(
														NY.Nongye ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCXNY.get( CX.Nongchun )
						.get( NY.Feinong ).get( Year.getYear( year - 1 ) ).get(
								XB.Female )[ X ];
			}
		}
		tempVar.CNTFR.get( CX.Nongchun ).put( NY.Feinong , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY.get(
							CX.Nongchun ).get( NY.Feinong ).get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.CNTFR
						.get( CX.Nongchun )
						.put(
								NY.Feinong ,
								( tempVar.CNTFR.get( CX.Nongchun ).get(
										NY.Feinong ) + ( NationziNv.get(
										CX.Nongchun ).get( NY.Feinong ).get(
										HunpeiField.DDB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Feinong ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Feinong ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Feinong ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNv.get( CX.Nongchun ).get(
												NY.Feinong ).get(
												HunpeiField.SingleRls )[ X ] + NationziNv
										.get( CX.Nongchun ).get( NY.Feinong )
										.get( HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY
												.get( CX.Nongchun ).get(
														NY.Feinong ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCXNY.get( CX.Chengshi )
						.get( NY.Nongye ).get( Year.getYear( year - 1 ) ).get(
								XB.Female )[ X ];
			}
		}
		tempVar.CNTFR.get( CX.Chengshi ).put( NY.Nongye , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY.get(
							CX.Chengshi ).get( NY.Nongye ).get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.CNTFR
						.get( CX.Chengshi )
						.put(
								NY.Nongye ,
								( tempVar.CNTFR.get( CX.Chengshi ).get(
										NY.Nongye ) + ( NationziNv.get(
										CX.Chengshi ).get( NY.Nongye ).get(
										HunpeiField.DDB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Nongye ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Nongye ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Nongye ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Nongye ).get(
												HunpeiField.SingleRls )[ X ] + NationziNv
										.get( CX.Chengshi ).get( NY.Nongye )
										.get( HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY
												.get( CX.Chengshi ).get(
														NY.Nongye ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCXNY.get( CX.Chengshi )
						.get( NY.Feinong ).get( Year.getYear( year - 1 ) ).get(
								XB.Female )[ X ];
			}
		}
		tempVar.CNTFR.get( CX.Chengshi ).put( NY.Feinong , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY.get(
							CX.Chengshi ).get( NY.Feinong ).get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.CNTFR
						.get( CX.Chengshi )
						.put(
								NY.Feinong ,
								( tempVar.CNTFR.get( CX.Chengshi ).get(
										NY.Feinong ) + ( NationziNv.get(
										CX.Chengshi ).get( NY.Feinong ).get(
										HunpeiField.DDB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Feinong ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Feinong ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Feinong ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNv.get( CX.Chengshi ).get(
												NY.Feinong ).get(
												HunpeiField.SingleRls )[ X ] + NationziNv
										.get( CX.Chengshi ).get( NY.Feinong )
										.get( HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCXNY
												.get( CX.Chengshi ).get(
														NY.Feinong ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfNY.get( NY.Nongye ).get(
						Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.nyPolicyTFR0.put( NY.Nongye , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY.get(
							NY.Nongye ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.nyPolicyTFR0.put( NY.Nongye , ( tempVar.nyPolicyTFR0
						.get( NY.Nongye ) + ( NationziNvOfNY.get( NY.Nongye )
						.get( HunpeiField.PlyBorn )[ X ] )
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY
								.get( NY.Nongye ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.nyImplTFR0.put( NY.Nongye , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY.get(
							NY.Nongye ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.nyImplTFR0
						.put(
								NY.Nongye ,
								( tempVar.nyImplTFR0.get( NY.Nongye ) + ( NationziNvOfNY
										.get( NY.Nongye ).get( HunpeiField.DDB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.SingleRls )[ X ] + NationziNvOfNY
										.get( NY.Nongye ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY
												.get( NY.Nongye ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.nyShiHunTFR0.put( NY.Nongye , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( NationziNvOfNY.get( NY.Nongye ).get( HunpeiField.coupleNum )[ X ] != 0.0
					&& ( NationziNvOfNY.get( NY.Nongye ).get( HunpeiField.DDB )[ X ]
							+ NationziNvOfNY.get( NY.Nongye ).get(
									HunpeiField.NMDFB )[ X ]
							+ NationziNvOfNY.get( NY.Nongye ).get(
									HunpeiField.NFDMB )[ X ]
							+ NationziNvOfNY.get( NY.Nongye ).get(
									HunpeiField.NNB )[ X ]
							+ NationziNvOfNY.get( NY.Nongye ).get(
									HunpeiField.SingleRls )[ X ] + NationziNvOfNY
							.get( NY.Nongye ).get( HunpeiField.ShuangFeiRls )[ X ] ) > 0.0 )
			{
				tempVar.nyShiHunTFR0
						.put(
								NY.Nongye ,
								( tempVar.nyShiHunTFR0.get( NY.Nongye ) + ( NationziNvOfNY
										.get( NY.Nongye ).get( HunpeiField.DDB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.SingleRls )[ X ] + NationziNvOfNY
										.get( NY.Nongye ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ NationziNvOfNY.get( NY.Nongye ).get(
												HunpeiField.coupleNum )[ X ] ) );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfNY.get( NY.Feinong )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.nyPolicyTFR0.put( NY.Feinong , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY.get(
							NY.Feinong ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.nyPolicyTFR0.put( NY.Feinong , ( tempVar.nyPolicyTFR0
						.get( NY.Feinong ) + ( NationziNvOfNY.get( NY.Feinong )
						.get( HunpeiField.PlyBorn )[ X ] )
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY
								.get( NY.Feinong ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.nyImplTFR0.put( NY.Feinong , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY.get(
							NY.Feinong ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 ) != 0.0 )
			{
				tempVar.nyImplTFR0
						.put(
								NY.Feinong ,
								( tempVar.nyImplTFR0.get( NY.Feinong ) + ( NationziNvOfNY
										.get( NY.Feinong )
										.get( HunpeiField.DDB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.SingleRls )[ X ] + NationziNvOfNY
										.get( NY.Feinong ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfNY
												.get( NY.Feinong ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.nyShiHunTFR0.put( NY.Feinong , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( NationziNvOfNY.get( NY.Feinong ).get( HunpeiField.coupleNum )[ X ] != 0.0
					&& ( NationziNvOfNY.get( NY.Feinong ).get( HunpeiField.DDB )[ X ]
							+ NationziNvOfNY.get( NY.Feinong ).get(
									HunpeiField.NMDFB )[ X ]
							+ NationziNvOfNY.get( NY.Feinong ).get(
									HunpeiField.NFDMB )[ X ]
							+ NationziNvOfNY.get( NY.Feinong ).get(
									HunpeiField.NNB )[ X ]
							+ NationziNvOfNY.get( NY.Feinong ).get(
									HunpeiField.SingleRls )[ X ] + NationziNvOfNY
							.get( NY.Feinong ).get( HunpeiField.ShuangFeiRls )[ X ] ) > 0.0 )
			{
				tempVar.nyShiHunTFR0
						.put(
								NY.Feinong ,
								( tempVar.nyShiHunTFR0.get( NY.Feinong ) + ( NationziNvOfNY
										.get( NY.Feinong )
										.get( HunpeiField.DDB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.NMDFB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.NFDMB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.NNB )[ X ]
										+ NationziNvOfNY.get( NY.Feinong ).get(
												HunpeiField.SingleRls )[ X ] + NationziNvOfNY
										.get( NY.Feinong ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( NationziNvOfNY.get( NY.Feinong )
												.get( HunpeiField.coupleNum )[ X ] ) ) );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCX.get( CX.Nongchun )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.cxPolicyTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX.get(
							CX.Nongchun ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.cxPolicyTFR0.put( CX.Nongchun , ( tempVar.cxPolicyTFR0
						.get( CX.Nongchun ) + NationziNvOfCX.get( CX.Nongchun )
						.get( HunpeiField.PlyBorn )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
								.get( CX.Nongchun ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.cxImplTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX.get(
							CX.Nongchun ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.cxImplTFR0
						.put(
								CX.Nongchun ,
								( tempVar.cxImplTFR0.get( CX.Nongchun ) + ( NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.DDB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NMDFB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NFDMB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NNB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.SingleRls )[ X ] + NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
												.get( CX.Nongchun ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.cxShiHunTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( NationziNvOfCX.get( CX.Nongchun ).get( HunpeiField.coupleNum )[ X ] != 0.0
					&& NationBirthPredictOfCX.get( CX.Nongchun ).get(
							Babies.getBabies( year ) )[ X ] > 0.0 )
			{
				tempVar.cxShiHunTFR0
						.put(
								CX.Nongchun ,
								( tempVar.cxShiHunTFR0.get( CX.Nongchun ) + ( NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.DDB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NMDFB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NFDMB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NNB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.SingleRls )[ X ] + NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.coupleNum )[ X ] ) );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCX.get( CX.Nongchun )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.cxPolicyTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX.get(
							CX.Nongchun ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.cxPolicyTFR0.put( CX.Nongchun , ( tempVar.cxPolicyTFR0
						.get( CX.Nongchun ) + NationziNvOfCX.get( CX.Nongchun )
						.get( HunpeiField.PlyBorn )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
								.get( CX.Nongchun ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.cxImplTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX.get(
							CX.Nongchun ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.cxImplTFR0
						.put(
								CX.Nongchun ,
								( tempVar.cxImplTFR0.get( CX.Nongchun ) + ( NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.DDB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NMDFB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NFDMB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NNB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.SingleRls )[ X ] + NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
												.get( CX.Nongchun ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.cxShiHunTFR0.put( CX.Nongchun , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( NationziNvOfCX.get( CX.Nongchun ).get( HunpeiField.coupleNum )[ X ] != 0.0
					&& NationBirthPredictOfCX.get( CX.Nongchun ).get(
							Babies.getBabies( year ) )[ X ] > 0.0 )
			{
				tempVar.cxShiHunTFR0
						.put(
								CX.Nongchun ,
								( tempVar.cxShiHunTFR0.get( CX.Nongchun ) + ( NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.DDB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NMDFB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NFDMB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.NNB )[ X ]
										+ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.SingleRls )[ X ] + NationziNvOfCX
										.get( CX.Nongchun ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ NationziNvOfCX.get( CX.Nongchun )
												.get( HunpeiField.coupleNum )[ X ] ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfCX.get( CX.Chengshi )
						.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.cxPolicyTFR0.put( CX.Chengshi , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX.get(
							CX.Chengshi ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.cxPolicyTFR0.put( CX.Chengshi , ( tempVar.cxPolicyTFR0
						.get( CX.Chengshi ) + NationziNvOfCX.get( CX.Chengshi )
						.get( HunpeiField.PlyBorn )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
								.get( CX.Chengshi ).get( Year.getYear( year ) )
								.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.cxImplTFR0.put( CX.Chengshi , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX.get(
							CX.Chengshi ).get( Year.getYear( year ) ).get(
							XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.cxImplTFR0
						.put(
								CX.Chengshi ,
								( tempVar.cxImplTFR0.get( CX.Chengshi ) + ( NationziNvOfCX
										.get( CX.Chengshi ).get(
												HunpeiField.DDB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.NMDFB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.NFDMB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.NNB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.SingleRls )[ X ] + NationziNvOfCX
										.get( CX.Chengshi ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfCX
												.get( CX.Chengshi ).get(
														Year.getYear( year ) )
												.get( XB.Female )[ X ] ) / 2.0 ) ) );
			}
		}
		tempVar.cxShiHunTFR0.put( CX.Chengshi , 0.0 );
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( NationziNv.get( CX.Chengshi ).get( NY.Nongye ).get(
					HunpeiField.coupleNum )[ X ]
					+ NationziNv.get( CX.Chengshi ).get( NY.Feinong ).get(
							HunpeiField.coupleNum )[ X ] != 0.0
					&& ( NationBirthPredictOfCX.get( CX.Chengshi ).get(
							Babies.getBabies( year ) )[ X ] ) > 0.0 )
			{
				tempVar.cxShiHunTFR0
						.put(
								CX.Chengshi ,
								( tempVar.cxShiHunTFR0.get( CX.Chengshi ) + ( NationziNvOfCX
										.get( CX.Chengshi ).get(
												HunpeiField.DDB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.NMDFB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.NFDMB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.NNB )[ X ]
										+ NationziNvOfCX.get( CX.Chengshi )
												.get( HunpeiField.SingleRls )[ X ] + NationziNvOfCX
										.get( CX.Chengshi ).get(
												HunpeiField.ShuangFeiRls )[ X ] )
										/ ( NationziNv.get( CX.Chengshi ).get(
												NY.Nongye ).get(
												HunpeiField.coupleNum )[ X ] + NationziNv
												.get( CX.Chengshi ).get(
														NY.Feinong ).get(
														HunpeiField.coupleNum )[ X ] ) ) );
			}
		}
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				ARFND1[ X ] = NationPopulationPredictOfAll.get(
						Year.getYear( year - 1 ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.nationalPlyTFR0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfAll.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.nationalPlyTFR0 += NationPolicyBabies.get( Babies
						.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfAll
								.get( Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.dqImplTFR0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfAll.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 != 0.0 )
			{
				tempVar.dqImplTFR0 += ( NationziNvOfAll.get( HunpeiField.DDB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.NMDFB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.NFDMB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.NNB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.SingleRls )[ X ] + NationziNvOfAll
						.get( HunpeiField.ShuangFeiRls )[ X ] )
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfAll
								.get( Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.dqShiHunTFR0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( ( NationziNvOfAll.get( HunpeiField.coupleNum )[ X ] ) != 0.0
					&& NationBirthPredictOfAll.get( Babies.getBabies( year ) )[ X ] > 0.0 )
			{
				tempVar.dqShiHunTFR0 += ( NationziNvOfAll.get( HunpeiField.DDB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.NMDFB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.NFDMB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.NNB )[ X ]
						+ NationziNvOfAll.get( HunpeiField.SingleRls )[ X ] + NationziNvOfAll
						.get( HunpeiField.ShuangFeiRls )[ X ] )
						/ NationziNvOfAll.get( HunpeiField.coupleNum )[ X ];
			}
		}
		tempVar.dqOverBrithTFR = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X > 1.0
					&& ( ARFND1[ X - 1 ] + NationPopulationPredictOfAll.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) > 0.0 )
			{
				tempVar.dqOverBrithTFR += NationOverBirthPredictOfAll
						.get( Babies.getBabies( year ) )[ X ]
						/ ( ( ARFND1[ X - 1 ] + NationPopulationPredictOfAll
								.get( Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 );
			}
		}
		tempVar.CRK = 0;
		tempVar.XRK = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.CRK += NationPopulationPredictOfCX.get( CX.Chengshi )
						.get( Year.getYear( year ) ).get( XB.Male )[ X ]
						+ NationPopulationPredictOfCX.get( CX.Chengshi ).get(
								Year.getYear( year ) ).get( XB.Female )[ X ];
				tempVar.XRK += NationPopulationPredictOfCX.get( CX.Nongchun )
						.get( Year.getYear( year ) ).get( XB.Male )[ X ]
						+ NationPopulationPredictOfCX.get( CX.Nongchun ).get(
								Year.getYear( year ) ).get( XB.Female )[ X ];
			}
		}
		tempVar.CSHLevel = tempVar.CRK / ( tempVar.CRK + tempVar.XRK ) * 100.0;
		tempVar.FNRK = 0;
		tempVar.NYRK = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.FNRK += NationPopulationPredictOfNY.get( NY.Feinong )
						.get( Year.getYear( year ) ).get( XB.Male )[ X ]
						+ NationPopulationPredictOfNY.get( NY.Feinong ).get(
								Year.getYear( year ) ).get( XB.Female )[ X ];
				tempVar.NYRK += NationPopulationPredictOfNY.get( NY.Nongye )
						.get( Year.getYear( year ) ).get( XB.Male )[ X ]
						+ NationPopulationPredictOfNY.get( NY.Nongye ).get(
								Year.getYear( year ) ).get( XB.Female )[ X ];
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		Map<HunpeiField,double[]> zn;
		for(int cx=1; cx<=5; cx++)
		{
			switch(cx)
			{
			case 1:
				zn = NationziNvOfAll;
				break;
			case 2:
				zn = NationziNvOfCX.get( CX.Chengshi );
				break;
			case 3:
				zn = NationziNvOfNY.get ( Feinong );
				break;
			case 4:
				zn = NationziNvOfCX.get( CX.Nongchun );
				break;
			case 5:
				zn = NationziNvOfNY.get ( Nongye );
				break;
			default: 
				zn = null;
			}
			
			//???????
			tempVar.cxI = cx;
			bRate.calculate( m , globals );
			
			//////////////translated by Foxpro2Java Translator successfully:///////////////
			tempVar.cxHunneiTFR.put( CX.Nongchun , 0.0 );
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( ( zn.get( HunpeiField.DD )[ X ]
						+ zn.get( HunpeiField.NMDF )[ X ]
						+ zn.get( HunpeiField.NFDM )[ X ]
						+ zn.get( HunpeiField.NN )[ X ]
						+ zn.get( HunpeiField.SingleDJ )[ X ] + zn
						.get( HunpeiField.ShuangfeiDJ )[ X ] ) != 0.0 )
				{
					tempVar.cxHunneiTFR
							.put(
									CX.Nongchun ,
									( tempVar.cxHunneiTFR.get( CX.Nongchun ) + ( zn
											.get( HunpeiField.DDB )[ X ]
											+ zn.get( HunpeiField.NMDFB )[ X ]
											+ zn.get( HunpeiField.NFDMB )[ X ]
											+ zn.get( HunpeiField.NNB )[ X ]
											+ zn.get( HunpeiField.SingleRls )[ X ] + zn
											.get( HunpeiField.ShuangFeiRls )[ X ] )
											/ ( zn.get( HunpeiField.DD )[ X ]
													+ zn.get( HunpeiField.NMDF )[ X ]
													+ zn.get( HunpeiField.NFDM )[ X ]
													+ zn.get( HunpeiField.NN )[ X ]
													+ zn
															.get( HunpeiField.SingleDJ )[ X ] + zn
													.get( HunpeiField.ShuangfeiDJ )[ X ] ) ) );
				}
			}
			/////////////end of translating, by Foxpro2Java Translator///////////////
		} //END FOR LOOP
	}
	
	@Override
	public void calculate ( IDAO m , HashMap< String , Object > globals )
			throws Exception
	{
	}

	@Override
	public Message checkDatabase ( IDAO m , HashMap< String , Object > globals )
			throws Exception
	{
		return null;
	}

	@Override
	public void setParam ( String params , int type ) throws MethodException
	{
	}

	

}

