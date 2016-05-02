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
 * ??????????????????
 * @author Jack Long
 *
 */
public class MRecordNationalPolicyAndPolicyRate implements
		IRecordNationalPolicyAndPolicyRateMethod
{

	@Override
	public void calculate ( IDAO m , HashMap< String , Object > globals )
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
		String diDai = xbb.getDiDai();
		String dqB = xbb.getDqName ( );
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
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
			//??
			Map<Year,Map<PolicyBirth,Double>> NationPolicyBirthRate = ( Map < Year , Map < PolicyBirth , Double >> ) predictVarMap.get ( "PolicyBirthRate" );
		
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
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.FnSD , tempVar.DDFR );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NySD , tempVar.DDFR );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.FnDD , tempVar.N2D1C );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NyDD , tempVar.N2D1N );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.FnFSingle , tempVar.N1D2C );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NyFSingle , tempVar.N1D2N );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.FnMSingle , tempVar.N2D1C );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NyMSingle , tempVar.N2D1N );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.FnSF , tempVar.NNFC );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NySF , tempVar.NNFN );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NcNYTFR ,
						tempVar.CNTFR.get( CX.Nongchun ).get( NY.Nongye ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NcFNTFR ,
						tempVar.CNTFR.get( CX.Nongchun ).get( NY.Feinong ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NcTFR ,
						tempVar.cxPolicyTFR0.get( CX.Nongchun ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.CsNYTFR ,
						tempVar.CNTFR.get( CX.Chengshi ).get( NY.Nongye ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.CsFNTFR ,
						tempVar.CNTFR.get( CX.Chengshi ).get( NY.Feinong ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.CsTFR ,
						tempVar.cxPolicyTFR0.get( CX.Chengshi ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.DqNYTFR ,
						tempVar.nyPolicyTFR0.get( NY.Nongye ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.DqFNTFR ,
						tempVar.nyPolicyTFR0.get( NY.Feinong ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.DqTFR , tempVar.dqPolicyTFR0 );
				NationPolicyBirthRate.get( Year.getYear( year ) )
						.put(
								PolicyBirth.NCNYMarryTFR ,
								tempVar.CNHunneiTFR.get( CX.Nongchun ).get(
										NY.Nongye ) );
				NationPolicyBirthRate.get( Year.getYear( year ) )
						.put(
								PolicyBirth.NCFNMarryTFR ,
								tempVar.CNHunneiTFR.get( CX.Nongchun ).get(
										NY.Feinong ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.NCMarryTFR ,
						tempVar.cxHunneiTFR.get( CX.Nongchun ) );
				NationPolicyBirthRate.get( Year.getYear( year ) )
						.put(
								PolicyBirth.CZNYMarryTFR ,
								tempVar.CNHunneiTFR.get( CX.Chengshi ).get(
										NY.Nongye ) );
				NationPolicyBirthRate.get( Year.getYear( year ) )
						.put(
								PolicyBirth.CZFNMarryTFR ,
								tempVar.CNHunneiTFR.get( CX.Chengshi ).get(
										NY.Feinong ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.CZMarryTFR ,
						tempVar.cxHunneiTFR.get( CX.Chengshi ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.QSNYMarryTFR ,
						tempVar.nyHunneiTFR.get( NY.Nongye ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.QSFNMarryTFR ,
						tempVar.nyHunneiTFR.get( NY.Feinong ) );
				NationPolicyBirthRate.get( Year.getYear( year ) ).put(
						PolicyBirth.QSMarryTFR , tempVar.dqHunneiTFR );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

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

