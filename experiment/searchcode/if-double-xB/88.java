package prclqz.methods;

import static prclqz.core.Const.MAX_AGE;
import static prclqz.core.enumLib.CX.Chengshi;
import static prclqz.core.enumLib.CX.Nongchun;
import static prclqz.core.enumLib.NY.Feinong;
import static prclqz.core.enumLib.NY.Nongye;
import static prclqz.core.enumLib.Policy.getPolicyById;

import java.util.EnumMap;
import java.util.Map;


import java.util.HashMap;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

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
 * ????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MSumOfNation implements IMethod
{
	public static void main(String[] args)throws Exception{
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
//		Map<Year,Map<PolicyBirth,Double>> PolicyBirthRate = EnumTools.createPolicyBirth(MAX_AGE, tempVar.getYear(), dqx);
//		predictVarMap.put ( "PolicyBirthRate"+dqx, PolicyBirthRate );
		
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = EnumTools.createCoupleAndChildrenOfCXNY(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfCXNY"+dqx, CoupleAndChildrenOfCXNY );
		
		Map < CX , Map < Year , Map < Couple , Double >>>  CoupleAndChildrenOfCX = EnumTools.createCoupleAndChildrenOfCX(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfCX"+dqx, CoupleAndChildrenOfCX );
		
		Map<NY,Map<Year,Map<Couple,Double>>>  CoupleAndChildrenOfNY = EnumTools.createCoupleAndChildrenOfNY(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfNY"+dqx, CoupleAndChildrenOfNY );
		
		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = EnumTools.createCoupleAndChildrenOfAll(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfAll"+dqx, CoupleAndChildrenOfAll );
		
		
		
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> NationCoupleAndChildrenOfCXNY = EnumTools.createNationCoupleAndChildrenOfCXNY(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfCXNY", NationCoupleAndChildrenOfCXNY );
		
		Map < CX , Map < Year , Map < Couple , Double >>>  NationCoupleAndChildrenOfCX = EnumTools.createNationCoupleAndChildrenOfCX(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfCX", NationCoupleAndChildrenOfCX );
		
		Map<NY,Map<Year,Map<Couple,Double>>>  NationCoupleAndChildrenOfNY = EnumTools.createNationCoupleAndChildrenOfNY(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfNY", NationCoupleAndChildrenOfNY );
		
		Map<Year,Map<Couple,Double>> NationCoupleAndChildrenOfAll = EnumTools.createNationCoupleAndChildrenOfAll(dqx, tempVar.getYear());
		predictVarMap.put ( "CoupleAndChildrenOfAll", NationCoupleAndChildrenOfAll );
		
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
		
		Map<CX, Map<NY, Map<HunpeiField, double[]>>> NationziNv = EnumTools
				.creatCXNYNationZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
		Map<CX, Map<HunpeiField, double[]>> NationziNvOfCX = EnumTools
				.creatCXNationZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
		Map<NY, Map<HunpeiField, double[]>> NationziNvOfNY = EnumTools
				.creatNYFNNationZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
		Map<HunpeiField, double[]> NationziNvOfAll = EnumTools.creatAllNationZiNvFromFile(
				MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put("HunpeiOfCXNY", NationziNv);
		predictVarMap.put("HunpeiOfCX", NationziNvOfCX);
		predictVarMap.put("HunpeiOfNY", NationziNvOfNY);
		predictVarMap.put("HunpeiOfAll", NationziNvOfAll);
		
		Map<CX,Map<Babies,double[]>> BirthPredictOfCX = EnumTools.createBirthPredictOfCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "BirthPredictOfCX"+dqx, BirthPredictOfCX );
		Map<NY,Map<Babies,double[]>> BirthPredictOfNY = EnumTools.createBirthPredictOfNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "BirthPredictOfNY"+dqx, BirthPredictOfNY );
		Map<Babies,double[]> BirthPredictOfAll = EnumTools.createBirthPredictALL(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "BirthPredictOfAll"+dqx, BirthPredictOfAll );

		Map<CX,Map<Babies,double[]>> NationBirthPredictOfCX = EnumTools.createNationBirthPredictOfCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "BirthPredictOfCX", NationBirthPredictOfCX );
		Map<NY,Map<Babies,double[]>> NationBirthPredictOfNY = EnumTools.createNationBirthPredictOfNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "BirthPredictOfNY", NationBirthPredictOfNY );
		Map<Babies,double[]> NationBirthPredictOfAll = EnumTools.createNationBirthPredictALL(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "BirthPredictOfAll", NationBirthPredictOfAll );
		
		Map<CX,Map<Babies,double[]>> OverBirthPredictOfCX = EnumTools.createOverBirthPredictOfCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "OverBirthPredictOfCX"+dqx, OverBirthPredictOfCX );
		Map<NY,Map<Babies,double[]>> OverBirthPredictOfNY = EnumTools.createOverBirthPredictOfNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "OverBirthPredictOfNY"+dqx, OverBirthPredictOfNY );
		Map<Babies,double[]> OverBirthPredictOfAll = EnumTools.createOverBirthPredictALL(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "OverBirthPredictOfAll"+dqx, OverBirthPredictOfAll );
		
		Map<Babies,double[]> PolicyBabies = EnumTools.createPolicyBabies(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PolicyBabies"+dqx, PolicyBabies);
		
		Map<Babies,double[]> NationPolicyBabies = EnumTools.createNationPolicyBabies(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PolicyBabies", NationPolicyBabies);
		
		Map<CX,Map<Babies,double[]>> NationOverBirthPredictOfCX = EnumTools.createNationOverBirthPredictOfCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "OverBirthPredictOfCX", NationOverBirthPredictOfCX );
		Map<NY,Map<Babies,double[]>> NationOverBirthPredictOfNY = EnumTools.createNationOverBirthPredictOfNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "OverBirthPredictOfNY", NationOverBirthPredictOfNY );
		Map<Babies,double[]> NationOverBirthPredictOfAll = EnumTools.createNationOverBirthPredictALL(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "OverBirthPredictOfAll", NationOverBirthPredictOfAll );
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = EnumTools.createPopulationPredictOfCXNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfCXNY"+dqx, PopulationPredictOfCXNY );
		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = EnumTools.createPopulationPredictOfCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfCX"+dqx, PopulationPredictOfCX );
		Map<NY,Map<Year,Map<XB,double[]>>>PopulationPredictOfNY = EnumTools.createPopulationPredictOfNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfNY"+dqx, PopulationPredictOfNY );
		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = EnumTools.createPopulationPredictOfAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfAll"+dqx, PopulationPredictOfAll );
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationPopulationPredictOfCXNY = EnumTools.createNationPopulationPredictOfCXNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfCXNY", NationPopulationPredictOfCXNY );
		Map<CX,Map<Year,Map<XB,double[]>>>NationPopulationPredictOfCX = EnumTools.createNationPopulationPredictOfCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfCX", NationPopulationPredictOfCX );
		Map<NY,Map<Year,Map<XB,double[]>>>NationPopulationPredictOfNY = EnumTools.createNationPopulationPredictOfNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfNY", NationPopulationPredictOfNY );
		Map<Year,Map<XB,double[]>> NationPopulationPredictOfAll = EnumTools.createNationPopulationPredictOfAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationPredictOfAll", NationPopulationPredictOfAll );
		
		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = EnumTools.createPopulationMigrationOfAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationMigrationOfAll"+dqx, PopulationMigrationOfAll );
		Map<Year,Map<XB,double[]>> NationPopulationMigrationOfAll = EnumTools.createNationPopulationMigrationOfAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationMigrationOfAll", NationPopulationMigrationOfAll );
				
		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = EnumTools.createPopulationDeathOfAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationDeathOfAll"+dqx, PopulationDeathOfAll );
		
		Map<Year,Map<XB,double[]>> NationPopulationDeathOfAll = EnumTools.createNationPopulationDeathOfAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "PopulationDeathOfAll", NationPopulationDeathOfAll );
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = EnumTools.createSonDiePopulationPredictOfYiHaiCXNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx, SonDiePopulationPredictOfYiHaiCXNY );
		Map<CX,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfYiHaiCX= EnumTools.createSonDiePopulationPredictOfYiHaiCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiCX"+dqx, SonDiePopulationPredictOfYiHaiCX );
		Map<NY,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfYiHaiNY= EnumTools.createSonDiePopulationPredictOfYiHaiNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiNY"+dqx, SonDiePopulationPredictOfYiHaiNY );
		Map<Year,Map<XB,double[]>> SonDiePopulationPredictOfYiHaiAll = EnumTools.createSonDiePopulationPopulationPredictOfYiHaiAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiAll"+dqx, SonDiePopulationPredictOfYiHaiAll );
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationSonDiePopulationPredictOfYiHaiCXNY = EnumTools.createNationSonDiePopulationPredictOfYiHaiCXNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiCXNY", NationSonDiePopulationPredictOfYiHaiCXNY );
		Map<CX,Map<Year,Map<XB,double[]>>> NationSonDiePopulationPredictOfYiHaiCX= EnumTools.createNationSonDiePopulationPredictOfYiHaiCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiCX", NationSonDiePopulationPredictOfYiHaiCX );
		Map<NY,Map<Year,Map<XB,double[]>>> NationSonDiePopulationPredictOfYiHaiNY= EnumTools.createNationSonDiePopulationPredictOfYiHaiNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiNY", NationSonDiePopulationPredictOfYiHaiNY );
		Map<Year,Map<XB,double[]>> NationSonDiePopulationPredictOfYiHaiAll = EnumTools.createNationSonDiePopulationPopulationPredictOfYiHaiAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfYiHaiAll", NationSonDiePopulationPredictOfYiHaiAll );
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = EnumTools.createSonDiePopulationPredictOfTeFuCXNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuCXNY"+dqx, SonDiePopulationPredictOfTeFuCXNY );
		Map<CX,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfTeFuCX= EnumTools.createSonDiePopulationPredictOfTeFuCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuCX"+dqx, SonDiePopulationPredictOfTeFuCX );
		Map<NY,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfTeFuNY= EnumTools.createSonDiePopulationPredictOfTeFuNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuNY"+dqx, SonDiePopulationPredictOfTeFuNY );
		Map<Year,Map<XB,double[]>> SonDiePopulationPredictOfTeFuAll = EnumTools.createSonDiePopulationPopulationPredictOfTeFuAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuAll"+dqx, SonDiePopulationPredictOfTeFuAll );
		
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationSonDiePopulationPredictOfTeFuCXNY = EnumTools.createNationSonDiePopulationPredictOfTeFuCXNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuCXNY", NationSonDiePopulationPredictOfTeFuCXNY );
		Map<CX,Map<Year,Map<XB,double[]>>> NationSonDiePopulationPredictOfTeFuCX= EnumTools.createNationSonDiePopulationPredictOfTeFuCX(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuCX", NationSonDiePopulationPredictOfTeFuCX );
		Map<NY,Map<Year,Map<XB,double[]>>> NationSonDiePopulationPredictOfTeFuNY= EnumTools.createNationSonDiePopulationPredictOfTeFuNY(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuNY", NationSonDiePopulationPredictOfTeFuNY );
		Map<Year,Map<XB,double[]>> NationSonDiePopulationPredictOfTeFuAll = EnumTools.createNationSonDiePopulationPopulationPredictOfTeFuAll(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put ( "SonDiePopulationPredictOfTeFuAll", NationSonDiePopulationPredictOfTeFuAll );
		
		Map<Year,Map<BabiesBorn,Double>> GiveBabiesBorn = EnumTools.createBabiesBorn(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put( "BabiesBorn"+dqx, GiveBabiesBorn );		

		Map<Year,Map<BabiesBorn,Double>> NationGiveBabiesBorn = EnumTools.createNationBabiesBorn(MAX_AGE, tempVar.getYear(), dqx);
		predictVarMap.put( "BabiesBorn", NationGiveBabiesBorn );	
		
		tempVar.setBabiesBorn(bBornBean);
		globals.put("tempVarBean", tempVar);	
		globals.put("bornXbbBeanMap", xbbMap);
		MSumOfNation hehe = new MSumOfNation();
		hehe.calculate(m, globals);
	}

	@Override
	public void calculate ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		//get variables from globals
		TempVariablesBean tempVar = (TempVariablesBean) globals.get("tempVarBean");
//		HashMap<String, Object> provincMigMap = (HashMap<String, Object>) globals.get("provinceMigMap");
		HashMap<String,Object> predictVarMap = (HashMap<String, Object>) globals.get("predictVarMap");
		HashMap<String,BornXbbBean> xbbMap = (HashMap<String, BornXbbBean>) globals.get("bornXbbBeanMap");
		StringList strValues = ( StringList ) globals.get ( "strValues" );
		
		//get running status
		int dqx = tempVar.getProvince();
		int year = tempVar.getYear();
		BornXbbBean xbb = xbbMap.get(""+dqx);
		//String diDai = xbb.getDiDai();
		//String dqB = xbb.getDqName ( );
		//define local variables
		int X=0;
		//BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
		//Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
		//Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
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
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationMigrationOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
			//??
			Map<Year,Map<XB,double[]>> NationPopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll" );
		
		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
			//??
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
		// ?????? CX,NY?All
		Map<CX,Map<Year,Map<XB,double[]>>>SonDiePopulationPredictOfTeFuCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCX"+dqx ),
			SonDiePopulationPredictOfYiHaiCX=( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> SonDiePopulationPredictOfTeFuNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuNY"+dqx ),
			SonDiePopulationPredictOfYiHaiNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiNY"+dqx );
			//??
			Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationSonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY" );
			Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> NationSonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY" );
			// ?????? CX,NY?All
			Map<CX,Map<Year,Map<XB,double[]>>>NationSonDiePopulationPredictOfTeFuCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCX" ),
			NationSonDiePopulationPredictOfYiHaiCX=( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCX" );
			Map<NY,Map<Year,Map<XB,double[]>>> NationSonDiePopulationPredictOfTeFuNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuNY" ),
			NationSonDiePopulationPredictOfYiHaiNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiNY" );
		
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
		
		//??????????				
		Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration = ( Map < Year , Map < SumBM , Double >> ) predictVarMap.get ( "SummaryOfBirthAndMigration");
		Map<Year,Map<SumBM,Double>> sumBM = SummaryOfBirthAndMigration;
		
		//????????
		Map<Abstract,Double> PolicySimulationAbstract = (Map< Abstract , Double >) predictVarMap.get( "PolicySimulationAbstract" ); 
		
		//?????
		Map<Year,Map<BabiesBorn,Double>> GiveBabiesBorn = (Map< Year , Map< BabiesBorn , Double >>) predictVarMap.get( "BabiesBorn"+dqx );
			//??
			Map<Year,Map<BabiesBorn,Double>> NationGiveBabiesBorn = (Map< Year , Map< BabiesBorn , Double >>) predictVarMap.get( "BabiesBorn" );
		
		/////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		
		int LBX,XBX,cxx;
		CX cx;
		NY ny;
		XB xb;
		double[] nation = null ,province = null;
		
		for ( LBX = 1 ; LBX <= 6 ; LBX++ )
		{
			for ( XBX = 1 ; XBX <= ( LBX == 6 ? 1 : 2 ) ; XBX++ )
			{
				xb = ( XBX == 1 ? XB.Male : XB.Female );

				switch ( LBX )
				{
				case 1 :
					nation = NationPopulationPredictOfAll.get(
							Year.getYear( year ) ).get( xb );
					province = PopulationPredictOfAll
							.get( Year.getYear( year ) ).get( xb );
					break;
				case 2 :
					nation = NationSonDiePopulationPredictOfTeFuAll.get(
							Year.getYear( year ) ).get( xb );
					province = SonDiePopulationPredictOfTeFuAll.get(
							Year.getYear( year ) ).get( xb );
					break;
				case 3 :
					nation = NationSonDiePopulationPredictOfYiHaiAll.get(
							Year.getYear( year ) ).get( xb );
					province = SonDiePopulationPredictOfYiHaiAll.get(
							Year.getYear( year ) ).get( xb );
					break;
				case 4 :
					nation = NationPopulationMigrationOfAll.get(
							Year.getYear( year ) ).get( xb );
					province = PopulationMigrationOfAll.get(
							Year.getYear( year ) ).get( xb );
					break;
				case 5 :
					nation = NationPopulationDeathOfAll.get(
							Year.getYear( year ) ).get( xb );
					province = PopulationDeathOfAll.get( Year.getYear( year ) )
							.get( xb );
					break;
				case 6 :
					nation = NationBirthPredictOfAll.get( Babies
							.getBabies( year ) );
					province = BirthPredictOfAll.get( Babies.getBabies( year ) );
					break;
				}

				for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
				{
					nation[ X ] += province[ X ];
				}

				for ( cxx = 1 ; cxx <= 2 ; cxx++ )
				{
					cx = ( cxx == 1 ) ? CX.Chengshi : CX.Nongchun;
					ny = ( cxx == 1 ) ? NY.Nongye : NY.Feinong;

					if ( LBX <= 3 )
					{
						/*
						 * ??=IIF(CXX=1,'??','??') ??????='??'-??-RK??
						 * ??????='??'-??-RK?? REPLACE &??????..&FIEXB WITH
						 * &??????..&FIEXB+&??????..&FIEXB ALL
						 */
						switch ( LBX )
						{
						case 1 :
							nation = NationPopulationPredictOfCX.get( cx ).get(
									Year.getYear( year ) ).get( xb );
							province = PopulationPredictOfCX.get( cx ).get(
									Year.getYear( year ) ).get( xb );
							break;
						case 2 :
							nation = NationSonDiePopulationPredictOfTeFuCX.get(
									cx ).get( Year.getYear( year ) ).get( xb );
							province = SonDiePopulationPredictOfTeFuCX.get( cx )
									.get( Year.getYear( year ) ).get( xb );
							break;
						case 3 :
							nation = NationSonDiePopulationPredictOfYiHaiCX
									.get( cx ).get( Year.getYear( year ) ).get(
											xb );
							province = SonDiePopulationPredictOfYiHaiCX
									.get( cx ).get( Year.getYear( year ) ).get(
											xb );
							break;
						}
						for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
						{
							nation[ X ] += province[ X ];
						}
						/*
						 * ??=IIF(CXX=1,'??','??') ??????='??'-??-RK??
						 * ??????='??'-??-RK?? REPLACE &??????..&FIEXB WITH
						 * &??????..&FIEXB+&??????..&FIEXB ALL
						 */
						switch ( LBX )
						{
						case 1 :
							nation = NationPopulationPredictOfNY.get( ny ).get(
									Year.getYear( year ) ).get( xb );
							province = PopulationPredictOfNY.get( ny ).get(
									Year.getYear( year ) ).get( xb );
							break;
						case 2 :
							nation = NationSonDiePopulationPredictOfTeFuNY.get(
									ny ).get( Year.getYear( year ) ).get( xb );
							province = SonDiePopulationPredictOfTeFuNY.get( ny )
									.get( Year.getYear( year ) ).get( xb );
							break;
						case 3 :
							nation = NationSonDiePopulationPredictOfYiHaiNY
									.get( ny ).get( Year.getYear( year ) ).get(
											xb );
							province = SonDiePopulationPredictOfYiHaiNY
									.get( ny ).get( Year.getYear( year ) ).get(
											xb );
							break;
						}
						for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
						{
							nation[ X ] += province[ X ];
						}
						for ( NY nyx : NY.values() )
						{
							switch ( LBX )
							{
							case 1 :
								nation = NationPopulationPredictOfCXNY.get( cx )
										.get( nyx ).get( Year.getYear( year ) )
										.get( xb );
								province = PopulationPredictOfCXNY.get( cx )
										.get( nyx ).get( Year.getYear( year ) )
										.get( xb );
								break;
							case 2 :
								nation = NationSonDiePopulationPredictOfTeFuCXNY
										.get( cx ).get( nyx ).get(
												Year.getYear( year ) ).get( xb );
								province = SonDiePopulationPredictOfTeFuCXNY
										.get( cx ).get( nyx ).get(
												Year.getYear( year ) ).get( xb );
								break;
							case 3 :
								nation = NationSonDiePopulationPredictOfYiHaiCXNY
										.get( cx ).get( nyx ).get(
												Year.getYear( year ) ).get( xb );
								province = SonDiePopulationPredictOfYiHaiCXNY
										.get( cx ).get( nyx ).get(
												Year.getYear( year ) ).get( xb );
								break;
							}
							for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
							{
								nation[ X ] += province[ X ];
							}
						}// NY-Loop
					} else if ( LBX == 6 )
					{

						nation = NationBirthPredictOfCX.get( cx ).get(
								Babies.getBabies( year ) );
						province = BirthPredictOfCX.get( cx ).get(
								Babies.getBabies( year ) );
						for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
						{
							nation[ X ] += province[ X ];
						}

						nation = NationBirthPredictOfNY.get( ny ).get(
								Babies.getBabies( year ) );
						province = BirthPredictOfNY.get( ny ).get(
								Babies.getBabies( year ) );
						for ( X = Const.MAX_AGE - 1 ; X >= 0 ; X-- )
						{
							nation[ X ] += province[ X ];
						}

					}// end-IF
				}// cxx -Loop

			}// XB Loop
		}// type Loop
		
		/**
		 * *****??????*****
		 */
		for(X = Const.MAX_AGE-1; X>=0; X--){
			NationPolicyBabies.get( Babies.getBabies( year ) )[ X ] += PolicyBabies
					.get( Babies.getBabies( year ) )[ X ];
			NationOverBirthPredictOfAll.get( Babies.getBabies( year ) )[ X ] += OverBirthPredictOfAll
					.get( Babies.getBabies( year ) )[ X ];
			NationOverBirthPredictOfCX.get( CX.Chengshi ).get(
					Babies.getBabies( year ) )[ X ] += OverBirthPredictOfCX
					.get( CX.Chengshi ).get( Babies.getBabies( year ) )[ X ];
			NationOverBirthPredictOfCX.get( CX.Nongchun ).get(
					Babies.getBabies( year ) )[ X ] += OverBirthPredictOfCX
					.get( CX.Nongchun ).get( Babies.getBabies( year ) )[ X ];
			NationOverBirthPredictOfNY.get( NY.Nongye ).get(
					Babies.getBabies( year ) )[ X ] += OverBirthPredictOfNY
					.get( NY.Nongye ).get( Babies.getBabies( year ) )[ X ];
			NationOverBirthPredictOfNY.get( NY.Feinong ).get(
					Babies.getBabies( year ) )[ X ] += OverBirthPredictOfNY
					.get( NY.Feinong ).get( Babies.getBabies( year ) )[ X ];
			
		}
		
		
//		Map< HunpeiField , double[] > HunpeiOfAll2 = (Map< HunpeiField , double[] >) predictVarMap.get( "HunpeiOfAll" );
//		HunpeiOfAll2.get( HunpeiField.SingleToNot )[3] = 0;
		
		Map<HunpeiField,double[]> nation_zn = NationziNvOfAll; //HunpeiOfAll2;//
		Map<HunpeiField,double[]> province_zn = ziNvOfAll;
		for(HunpeiField hpf : HunpeiField.getARHunpeiFileds())
		{
			for(X = Const.MAX_AGE-1; X>=0; X--)
			{
				double tmp = province_zn.get( hpf )[X];
				try{
					nation_zn.get( hpf )[X] += tmp;
				}catch (Exception e) {
					System.out.println(e.toString());
				}
			}
		}
		for(CX cx2 : CX.values()){
			nation_zn = NationziNvOfCX.get( cx2 );
			province_zn = ziNvOfCX.get( cx2 );
			for(HunpeiField hpf : HunpeiField.getARHunpeiFileds())
			{
				for(X = Const.MAX_AGE-1; X>=0; X--)
				{
					nation_zn.get( hpf )[X] += province_zn.get( hpf )[X];
				}
			}
		}
		for(NY ny2 : NY.values()){
			nation_zn = NationziNvOfNY.get( ny2 );
			province_zn = ziNvOfNY.get( ny2 );
			for(HunpeiField hpf : HunpeiField.getARHunpeiFileds())
			{
				for(X = Const.MAX_AGE-1; X>=0; X--)
				{
					nation_zn.get( hpf )[X] += province_zn.get( hpf )[X];
				}
			}
		}
		for(CX cx2 : CX.values()){
			for(NY ny2 : NY.values()){
				nation_zn = NationziNv.get( cx2 ).get( ny2 );
				province_zn = ziNv.get( cx2 ).get( ny2 );
				for(HunpeiField hpf : HunpeiField.getARHunpeiFileds())
				{
					for(X = Const.MAX_AGE-1; X>=0; X--)
					{
						nation_zn.get( hpf )[X] += province_zn.get( hpf )[X];
					}
				}
			}
		}
		
		
		/**
		 * ***********???????***************
		 * 
		 */
		if( 
			(SYSFMSField.getSFMSfromId( tempVar.SFMS )==SYSFMSField.yiyuan && 
				year == tempVar.policyTime 
			)
			 ||
			 ( SYSFMSField.getSFMSfromId( tempVar.SFMS )==SYSFMSField.yiyuan &&
				 year<=2050 &&
				 year % 5 == 0
			 )	
			)
		{
			//* TODO ????????????
		}
		
		
		/**
		 * *********************************
		 *	  ?????????		
		 *********************************
		 */
		if ( year == envParm.getEnd() )
		{
			for ( BabiesBorn bb : BabiesBorn.getStatisticsFields() )
			{
				for ( int yearx = envParm.getEnd() ; yearx >= envParm
						.getBegin() ; yearx-- )
				{
					NationGiveBabiesBorn.get( Year.getYear( yearx ) ).put(
							bb ,
							NationGiveBabiesBorn.get( Year.getYear( yearx ) )
									.get( bb )
									+ GiveBabiesBorn
											.get( Year.getYear( yearx ) ).get(
													bb ) );
				}
			}
			for ( Couple cp : Couple.getCopyCouple2() )
			{
				for ( int yearx = envParm.getEnd() ; yearx >= envParm
						.getBegin() ; yearx-- )
				{
					NationCoupleAndChildrenOfAll.get( Year.getYear( yearx ) )
							.put(
									cp ,
									NationCoupleAndChildrenOfAll.get(
											Year.getYear( yearx ) ).get( cp )
											+ CoupleAndChildrenOfAll.get(
													Year.getYear( yearx ) )
													.get( cp ) );
				}
			}
			// CX
			for ( CX cx2 : CX.values() )
			{
				for ( Couple cp : Couple.getCopyCouple2() )
					for ( int yearx = envParm.getEnd() ; yearx >= envParm
							.getBegin() ; yearx-- )
					{
						NationCoupleAndChildrenOfCX.get( cx2 ).get(
								Year.getYear( yearx ) ).put(
								cp ,
								NationCoupleAndChildrenOfCX.get( cx2 ).get(
										Year.getYear( yearx ) ).get( cp )
										+ CoupleAndChildrenOfCX.get( cx2 ).get(
												Year.getYear( yearx ) )
												.get( cp ) );
					}
			}
			// NY
			for ( NY ny2 : NY.values() )
			{
				for ( Couple cp : Couple.getCopyCouple2() )
					for ( int yearx = envParm.getEnd() ; yearx >= envParm
							.getBegin() ; yearx-- )
					{
						NationCoupleAndChildrenOfNY.get( ny2 ).get(
								Year.getYear( yearx ) ).put(
								cp ,
								NationCoupleAndChildrenOfNY.get( ny2 ).get(
										Year.getYear( yearx ) ).get( cp )
										+ CoupleAndChildrenOfNY.get( ny2 ).get(
												Year.getYear( yearx ) )
												.get( cp ) );
					}
			}
			// CXNY
			for ( CX cx2 : CX.values() )
				for ( NY ny2 : NY.values() )
				{
					for ( Couple cp : Couple.getCopyCouple2() )
						for ( int yearx = envParm.getEnd() ; yearx >= envParm
								.getBegin() ; yearx-- )
						{
							NationCoupleAndChildrenOfCXNY
									.get( cx2 )
									.get( ny2 )
									.get( Year.getYear( yearx ) )
									.put(
											cp ,
											NationCoupleAndChildrenOfCXNY.get(
													cx2 ).get( ny2 ).get(
													Year.getYear( yearx ) )
													.get( cp )
													+ CoupleAndChildrenOfCXNY
															.get( cx2 )
															.get( ny2 )
															.get(
																	Year
																			.getYear( yearx ) )
															.get( cp ) );
						}
				}
		}// ENDIF
		
		System.out.println("Hello");
		
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

