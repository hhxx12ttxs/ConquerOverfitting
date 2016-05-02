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
import java.util.EnumMap;

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
import prclqz.core.enumLib.PolicyBirth;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

import prclqz.lib.EnumMapTool;
import prclqz.parambeans.ParamBean3;
import test.EnumTools;
/**
 * ????????????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MRecordPolicyAndPolicyRate implements IMethod
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

		tempVar.setFeiNongPolicy1(171);
		tempVar.setFeiNongTime1(0);
		tempVar.nongYePolicy1 = 171;
		tempVar.nongYeTime1 = 0;
		tempVar.feiNongPolicy2 = 172;
		tempVar.feiNongTime2 = 2011;
		tempVar.nongYePolicy2 = 172;
		tempVar.nongYeTime2 = 2011;
		tempVar.feiNongPolicy3 = 175;
		tempVar.feiNongTime3 = 2015;
		tempVar.nongYePolicy3 = 175;
		tempVar.nongYeTime3 = 2015;
		tempVar.adjustType = "???????";
		tempVar.DDFR = 2.000;
		tempVar.N2D1C = 1;
		tempVar.N1D2C = 1;
		tempVar.N1D2N = 2.00;
		tempVar.N2D1N = 2.00;
		tempVar.NNFN = 1.00;
		tempVar.NNFC = 1;
		tempVar.CNTFR = new EnumMap<CX,Map<NY,Double>>(CX.class);
		tempVar.CNTFR.put(Nongchun, new EnumMap<NY,Double>(NY.class));
		tempVar.CNTFR.put(Chengshi, new EnumMap<NY,Double>(NY.class));
		Map<NY,Double> _tmp = tempVar.CNTFR.get(Nongchun);
		_tmp.put(Nongye, 1.263768);
		_tmp.put(Feinong, 1.098917);
		_tmp = tempVar.CNTFR.get(Chengshi);
		_tmp.put(Nongye, 1.350991);
		_tmp.put(Feinong, 1.194692);
		
		tempVar.cxPolicyTFR0 = new EnumMap<CX,Double>(CX.class);
		tempVar.cxPolicyTFR0.put(Chengshi, 1.278835);
		tempVar.cxPolicyTFR0.put(Nongchun, 1.257530);	
		
		tempVar.nyPolicyTFR0 = new EnumMap<NY, Double>(NY.class);
		tempVar.nyPolicyTFR0.put(Feinong, 1.190294);
		tempVar.nyPolicyTFR0.put(Nongye, 1.307879);
		
		tempVar.dqPolicyTFR0 = 1.274611;
		tempVar.CNHunneiTFR = new EnumMap<CX,Map<NY,Double>>(CX.class);
		tempVar.CNHunneiTFR.put(Nongchun, new EnumMap<NY,Double>(NY.class));
		tempVar.CNHunneiTFR.put(Chengshi, new EnumMap<NY,Double>(NY.class));
		
		_tmp = tempVar.CNHunneiTFR.get(Nongchun);
		_tmp.put(Nongye, 1.323994);
		_tmp.put(Feinong, 1.088629);
		_tmp = tempVar.CNHunneiTFR.get(Chengshi);
		_tmp.put(Nongye, 1.323262);
		_tmp.put(Feinong, 1.161190);
		
		tempVar.cxHunneiTFR = new EnumMap<CX,Double>(CX.class);
		tempVar.cxHunneiTFR.put(Chengshi, 1.250148);
		tempVar.cxHunneiTFR.put(Nongchun, 1.313912);	

		tempVar.nyHunneiTFR = new EnumMap<NY, Double>(NY.class);
		tempVar.nyHunneiTFR.put(Feinong, 1.157701);
		tempVar.nyHunneiTFR.put(Nongye, 1.323309);
		tempVar.dqHunneiTFR = 1.271119;
		
		StringList strValues = new StringList();
		globals.put("strValues", strValues);

		
		ParamBean3 envParm = new ParamBean3();
		envParm.setBegin(2010);
		globals.put("SetUpEnvParamBean", envParm);
		
		BabiesBornBean bBornBean = new BabiesBornBean(0, 0, 0,
				0, 0, 0, 0,
				0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0);
		//tempVar.singleSonFaCN = EnumTools.create;
		
		//TODO ???????????
//		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = EnumTools.createPopulationPredictOfCXNY(MAX_AGE, tempVar.getYear(), dqx);
		Map<Year,Map<PolicyBirth,Double>> PolicyBirthRate = EnumTools.createPolicyBirth(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PolicyBirthRate"+dqx, PolicyBirthRate );
		
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = EnumTools.createCoupleAndChildrenOfCXNY(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfCXNY"+dqx, CoupleAndChildrenOfCXNY );
		
		Map < CX , Map < Year , Map < Couple , Double >>>  CoupleAndChildrenOfCX = EnumTools.createCoupleAndChildrenOfCX(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfCX"+dqx, CoupleAndChildrenOfCX );
		
		Map<NY,Map<Year,Map<Couple,Double>>>  CoupleAndChildrenOfNY = EnumTools.createCoupleAndChildrenOfNY(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfNY"+dqx, CoupleAndChildrenOfNY );
		
		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = EnumTools.createCoupleAndChildrenOfAll(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfAll"+dqx, CoupleAndChildrenOfAll );
		
		tempVar.setBabiesBorn(bBornBean);
		globals.put("tempVarBean", tempVar);	
		globals.put("bornXbbBeanMap", xbbMap);
		MRecordPolicyAndPolicyRate hehe = new MRecordPolicyAndPolicyRate();
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
//		BornXbbBean xbb = xbbMap.get(""+dqx);
//		String diDai = xbb.getDiDai();
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
//		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
//		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
		//??
		//????????
		Map<Babies,double[]> policyBabies = (Map<Babies, double[]>) predictVarMap.get("PolicyBabies"+dqx);
		//???????(????????)
		Map<CX,Map<Babies,double[]>> BirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfCX"+dqx );
		Map<NY,Map<Babies,double[]>> BirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "BirthPredictOfNY"+dqx );
		Map<Babies,double[]> BirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "BirthPredictOfAll"+dqx );
		Map<CX,Map<Babies,double[]>> OverBirthPredictOfCX = ( Map < CX , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfCX"+dqx );
		Map<NY,Map<Babies,double[]>> OverBirthPredictOfNY = ( Map < NY , Map < Babies , double [ ] >> ) predictVarMap.get ( "OverBirthPredictOfNY"+dqx );
		Map<Babies,double[]> OverBirthPredictOfAll = ( Map < Babies , double [ ] > ) predictVarMap.get ( "OverBirthPredictOfAll"+dqx );
		Map<Babies,double[]> PolicyBabies = ( Map < Babies , double [ ] > ) predictVarMap.get ( "PolicyBabies"+dqx );
		
		//??????
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "PopulationPredictOfCXNY"+dqx );
		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );

		//??????
		Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
		
		//????????
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
		Map<CX,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX"+dqx );
		Map<NY,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY"+dqx );
		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll"+dqx );
		
		//??????--??
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx );
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> yiHaiCN = SonDiePopulationPredictOfYiHaiCXNY;
//		yiHaiCN.get ( Chengshi ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( Male )[X] = 0;
		
		//??????--??
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY"+dqx );
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> tefuCN = SonDiePopulationPredictOfTeFuCXNY;
		
		// ?????? CX,NY?All
		Map<CX,Map<Year,Map<XB,double[]>>>SonDiePopulationPredictOfTeFuCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCX"+dqx ),
			SonDiePopulationPredictOfYiHaiCX=( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCX"+dqx );
		
		Map<CX,Map<Year,Map<XB,double[]>>> yiHaiC = SonDiePopulationPredictOfYiHaiCX,
			tefuC = SonDiePopulationPredictOfTeFuCX;
		
		Map<NY,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfTeFuNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuNY"+dqx ),
			SonDiePopulationPredictOfYiHaiNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiNY"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> yiHaiN = SonDiePopulationPredictOfYiHaiNY,
			tefuN = SonDiePopulationPredictOfTeFuNY;
		Map<Year,Map<XB,double[]>> SonDiePopulationPredictOfTeFuAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuAll"+dqx ),
			SonDiePopulationPredictOfYiHaiAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiAll"+dqx );
		Map<Year,Map<XB,double[]>> yiHaiA = SonDiePopulationPredictOfYiHaiAll,
			tefuA = SonDiePopulationPredictOfTeFuAll;
		
		
		//????
		double[][] husbandRate = ( double [ ][ ] ) predictVarMap.get("HusbandRate");
		
		//?????
		ParamBean3 envParm = (ParamBean3) globals.get("SetUpEnvParamBean");
		
		//????????					
		Map<Year,Map<PolicyBirth,Double>> PolicyBirthRate = ( Map < Year , Map < PolicyBirth , Double >> ) predictVarMap.get ( "PolicyBirthRate"+dqx );
		
//////////////translated by Foxpro2Java Translator successfully:///////////////
//		for(X = MAX_AGE-1; X >= 0 ; X--){ 
//		if( true ){
		
		//TODO LOCA FOR ??=ND0 ???????
		
		//ALLT(??ZC1) - IIF(????1=0,'',ALLT(STR(????1,4)))
		//strValues.add ( Policy.getPolicyById ( tempVar.feiNongPolicy1  ).toString ( )+ ) 
//		strValues.add ( ""+Policy.getPolicyById ( tempVar.feiNongPolicy1 )+(tempVar.feiNongTime1 ==0?"":""+tempVar.feiNongTime1) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.FnPolicy1 ,
				strValues.add ( ""
						+ Policy.getPolicyById ( tempVar.feiNongPolicy1 )
						+ ( tempVar.feiNongTime1 == 0 ? "" : ""
								+ tempVar.feiNongTime1 ) ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NyPolicy1 ,
				strValues.add ( Policy.getPolicyById ( tempVar.nongYePolicy1 )
						.toString ( )
						+ ( tempVar.nongYeTime1 == 0 ? "" : ""
								+ tempVar.nongYeTime1 ) ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.FnPolicy2 ,
				strValues.add ( Policy.getPolicyById ( tempVar.feiNongPolicy2 )
						.toString ( )
						+ ( tempVar.feiNongTime2 == 0 ? "" : ""
								+ tempVar.feiNongTime2 ) ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NyPolicy2 ,
				strValues.add ( Policy.getPolicyById ( tempVar.nongYePolicy2 )
						.toString ( )
						+ ( tempVar.nongYeTime2 == 0 ? "" : ""
								+ tempVar.nongYeTime2 ) ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.FnPolicy3 ,
				strValues
						.add ( tempVar.adjustType.equals ( "???????" ) ? Policy
								.getPolicyById ( tempVar.feiNongPolicy3 )
								.toString ( )
								+ tempVar.feiNongTime3 : "" ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NyPolicy3 ,
				strValues
						.add ( tempVar.adjustType.equals ( "???????" ) ? Policy
								.getPolicyById ( tempVar.nongYePolicy3 )
								.toString ( )
								+ tempVar.nongYeTime3 : "" ) );
		// }
		// if( true ){
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.FnSD ,
				tempVar.DDFR );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.NySD ,
				tempVar.DDFR );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.FnDD ,
				tempVar.N2D1C );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.NyDD ,
				tempVar.N2D1N );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.FnFSingle , tempVar.N1D2C );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NyFSingle , tempVar.N1D2N );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.FnMSingle , tempVar.N2D1C );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NyMSingle , tempVar.N2D1N );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.FnSF ,
				tempVar.NNFC );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.NySF ,
				tempVar.NNFN );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NcNYTFR ,
				tempVar.CNTFR.get ( Nongchun ).get ( Nongye ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NcFNTFR ,
				tempVar.CNTFR.get ( Nongchun ).get ( Feinong ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.NcTFR ,
				tempVar.cxPolicyTFR0.get ( Nongchun ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.CsNYTFR ,
				tempVar.CNTFR.get ( Chengshi ).get ( Nongye ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.CsFNTFR ,
				tempVar.CNTFR.get ( Chengshi ).get ( Feinong ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.CsTFR ,
				tempVar.cxPolicyTFR0.get ( Chengshi ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.DqNYTFR , tempVar.nyPolicyTFR0.get ( Nongye ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.DqFNTFR , tempVar.nyPolicyTFR0.get ( Feinong ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put ( PolicyBirth.DqTFR ,
				tempVar.dqPolicyTFR0 );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NCNYMarryTFR ,
				tempVar.CNHunneiTFR.get ( Nongchun ).get ( Nongye ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NCFNMarryTFR ,
				tempVar.CNHunneiTFR.get ( Nongchun ).get ( Feinong ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.NCMarryTFR , tempVar.cxHunneiTFR.get ( Nongchun ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.CZNYMarryTFR ,
				tempVar.CNHunneiTFR.get ( Chengshi ).get ( Nongye ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.CZFNMarryTFR ,
				tempVar.CNHunneiTFR.get ( Chengshi ).get ( Feinong ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.CZMarryTFR , tempVar.cxHunneiTFR.get ( Chengshi ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.QSNYMarryTFR , tempVar.nyHunneiTFR.get ( Nongye ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.QSFNMarryTFR , tempVar.nyHunneiTFR.get ( Feinong ) );
		PolicyBirthRate.get ( Year.getYear ( year ) ).put (
				PolicyBirth.QSMarryTFR , tempVar.dqHunneiTFR );
//		}
//		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		//TODO ??????? getCopyCouple
		
		for(Couple cp:Couple.getCopyCouple ( )){
			
			CoupleAndChildrenOfCX.get ( Nongchun ).get ( Year.getYear ( year )).put ( cp , CoupleAndChildrenOfCXNY.get ( Nongchun ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( cp )+CoupleAndChildrenOfCXNY.get ( Nongchun ).get ( Feinong ).get ( Year.getYear ( year ) ).get ( cp ) );
			CoupleAndChildrenOfCX.get ( Chengshi ).get ( Year.getYear ( year )).put ( cp , CoupleAndChildrenOfCXNY.get ( Chengshi ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( cp )+CoupleAndChildrenOfCXNY.get ( Chengshi ).get ( Feinong ).get ( Year.getYear ( year ) ).get ( cp ) );
			
			CoupleAndChildrenOfNY.get ( Nongye ).get ( Year.getYear ( year )).put ( cp , CoupleAndChildrenOfCXNY.get ( Nongchun ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( cp )+CoupleAndChildrenOfCXNY.get ( Chengshi ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( cp ) );
			CoupleAndChildrenOfNY.get ( Feinong ).get ( Year.getYear ( year )).put ( cp , CoupleAndChildrenOfCXNY.get ( Nongchun ).get ( Feinong ).get ( Year.getYear ( year ) ).get ( cp )+CoupleAndChildrenOfCXNY.get ( Chengshi ).get ( Feinong ).get ( Year.getYear ( year ) ).get ( cp ) );
			
			CoupleAndChildrenOfAll.get ( Year.getYear ( year )).put ( cp , CoupleAndChildrenOfCX.get ( Nongchun ).get ( Year.getYear ( year ) ).get ( cp )+CoupleAndChildrenOfCX.get ( Chengshi ).get ( Year.getYear ( year ) ).get ( cp ) );
		}
		
		System.out.println("End!");

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

