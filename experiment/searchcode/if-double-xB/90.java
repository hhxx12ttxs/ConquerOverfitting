package prclqz.methods;

import java.util.Map;

import static prclqz.core.enumLib.Summary.*;
import static prclqz.core.enumLib.SYSFMSField.*;
import static prclqz.core.enumLib.BabiesBorn.*;
import static prclqz.core.enumLib.HunpeiField.*;
import static prclqz.core.enumLib.XB.*;
import static prclqz.core.enumLib.CX.*;
import static prclqz.core.enumLib.NY.*;
import static prclqz.core.Const.*;

import java.util.HashMap;
import java.util.Map;

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
import prclqz.core.enumLib.Policy;
import prclqz.core.enumLib.PolicyBirth;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.Summary;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

import prclqz.lib.EnumMapTool;
import prclqz.parambeans.ParamBean3;
/**
 * ??????
 * @author prclqz@zju.edu.cn
 *
 */
public class MSumOfBornBabies implements IMethod
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
		String diDai = xbb.getDiDai();
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
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
		
		//?????
		Map<Year,Map<BabiesBorn,Double>> babiesBorn = ( Map < Year , Map < BabiesBorn , Double >> ) predictVarMap.get ( "BabiesBorn"+dqx );
		
//		babiesBorn.get ( Year.getYear ( year ) ).get ( CFnDDB );
		//????_??
		Map<Year,Map<Summary,Double >> SummaryOfAll = ( Map < Year , Map < Summary , Double >> ) predictVarMap.get ( "SummaryOfAll"+dqx );
		Map<CX,Map<Year,Map<Summary,Double >>> SummaryOfCX = ( Map < CX , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfCX"+dqx );
		Map<NY,Map<Year,Map<Summary,Double >>> SummaryOfNY = ( Map < NY , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfNY"+dqx );
		
//		SummaryOfCX.get ( CX.Chengshi ).get ( Year.getYear ( year ) ).put ( LandAvg , 0.0 );
		
		//////////////translated by Foxpro2Java Translator successfully:///////////////
//		for ( X = MAX_AGE - 1;X >= 0;X -- )
//		{
		
		for ( int yearX = envParm.getEnd ( ); yearX >= envParm.getBegin ( );yearX -- )
		{
			if ( true )
			{
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CFnDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CFnD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CFnNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CNyDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CNyD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CNyNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XFnDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XFnD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XFnNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XNyDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XNyDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XNyD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XNyD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XNyNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XNyNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyDDB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnDDB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyD_B )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnD_B ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnNNB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyNNB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnDDB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnDDB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyDDB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnD_B1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnD_B1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyD_B1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnNNB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyNNB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnDDB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyDDB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnD_B2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyD_B2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnNNB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyNNB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnDDB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjDDB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjDDB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnD_B )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjD_B1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjD_B2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnNNB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjNNB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjNNB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XFnNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyDDB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjDDB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyDDB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjDDB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyD_B ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjD_B1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyD_B1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjD_B2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyNNB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjNNB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjNNB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CFnNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjDDB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjDDB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjDDB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjDDB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjDDB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjDDB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjDDB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjD_B ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjD_B )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjD_B ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjD_B1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjD_B1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjD_B2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjD_B2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjNNB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjNNB )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjNNB ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjNNB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjNNB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XNyDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XNyDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XNyNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XFnNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyNNB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CNyDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CNyNNB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CFnNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						XHjB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( XHjB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						CHjB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CHjB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( CHjB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZHjB ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( ZHjB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZHjB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( ZFnDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZFnD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZFnNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZFnB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( ZFnDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZFnD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZFnNNB2 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyB1 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( ZNyDDB1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZNyD_B1 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZNyNNB1 ) );
				babiesBorn.get ( Year.getYear ( yearX ) ).put (
						ZNyB2 ,
						babiesBorn.get ( Year.getYear ( yearX ) ).get ( ZNyDDB2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZNyD_B2 )
								+ babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZNyNNB2 ) );
				SummaryOfAll.get ( Year.getYear ( yearX ) )
						.put (
								BirthYihai ,
								babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZHjB1 ) / 10000.0 );
				SummaryOfAll.get ( Year.getYear ( yearX ) )
						.put (
								PolicyErhai ,
								babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZHjB2 ) / 10000.0 );
				SummaryOfCX.get ( CX.Chengshi ).get ( Year.getYear ( yearX ) )
						.put (
								BirthYihai ,
								( babiesBorn.get ( Year.getYear ( yearX ) )
										.get ( CHjB1 ) ) / 10000.0 );
				SummaryOfCX.get ( CX.Chengshi ).get ( Year.getYear ( yearX ) )
						.put (
								PolicyErhai ,
								babiesBorn.get ( Year.getYear ( yearX ) ).get (
										CHjB2 ) / 10000.0 );
				SummaryOfCX.get ( CX.Nongchun ).get ( Year.getYear ( yearX ) )
						.put (
								BirthYihai ,
								( babiesBorn.get ( Year.getYear ( yearX ) )
										.get ( XHjB1 ) ) / 10000.0 );
				SummaryOfCX.get ( CX.Nongchun ).get ( Year.getYear ( yearX ) )
						.put (
								PolicyErhai ,
								babiesBorn.get ( Year.getYear ( yearX ) ).get (
										XHjB2 ) / 10000.0 );
				SummaryOfNY.get ( Feinong ).get ( Year.getYear ( yearX ) )
						.put (
								BirthYihai ,
								( babiesBorn.get ( Year.getYear ( yearX ) )
										.get ( ZFnB1 ) ) / 10000.0 );
				SummaryOfNY.get ( Feinong ).get ( Year.getYear ( yearX ) )
						.put (
								PolicyErhai ,
								babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZFnB2 ) / 10000.0 );
				SummaryOfNY.get ( Nongye ).get ( Year.getYear ( yearX ) )
						.put (
								BirthYihai ,
								( babiesBorn.get ( Year.getYear ( yearX ) )
										.get ( ZNyB1 ) ) / 10000.0 );
				SummaryOfNY.get ( Nongye ).get ( Year.getYear ( yearX ) )
						.put (
								PolicyErhai ,
								babiesBorn.get ( Year.getYear ( yearX ) ).get (
										ZNyB2 ) / 10000.0 );
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

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

