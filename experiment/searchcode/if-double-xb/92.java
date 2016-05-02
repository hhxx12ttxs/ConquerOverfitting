package prclqz.methods;


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

/**
 * ?????????
 * @author Jack Long
 * @email  prclqz@zju.edu.cn
 *
 */
public class MGroupingByAge implements IGroupingByAgeMethod
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
//		//????
//		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
//		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
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
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationMigrationOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
		
		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
		
		
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
		
		//????_??
		Map<Year,Map<Summary,Double >> SummaryOfAll = ( Map < Year , Map < Summary , Double >> ) predictVarMap.get ( "SummaryOfAll"+dqx );
		Map<CX,Map<Year,Map<Summary,Double >>> SummaryOfCX = ( Map < CX , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfCX"+dqx );
		Map<NY,Map<Year,Map<Summary,Double >>> SummaryOfNY = ( Map < NY , Map < Year , Map < Summary , Double >>> ) predictVarMap.get ( "SummaryOfNY"+dqx );
		
		//??????????				
		Map<Year,Map<SumBM,Double>> SummaryOfBirthAndMigration = ( Map < Year , Map < SumBM , Double >> ) predictVarMap.get ( "SummaryOfBirthAndMigration");
		Map<Year,Map<SumBM,Double>> sumBM = SummaryOfBirthAndMigration;
		
		/////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map< Year , Map< XB , double[] >> pplPredict;
		switch ( tempVar.cxI )
		{
		case 1 :
			pplPredict = PopulationPredictOfAll;
			break;
		case 2 :
			pplPredict = PopulationPredictOfCX.get( CX.Chengshi );
			break;
		case 3 :
			pplPredict = PopulationPredictOfNY.get( Feinong );
			break;
		case 4 :
			pplPredict = PopulationPredictOfCX.get( CX.Nongchun );
			break;
		case 5 :
			pplPredict = PopulationPredictOfNY.get( Nongye );
			break;
		default :
			pplPredict = null;
		}

		double RKZ , PZ;

		// ////////////translated by Foxpro2Java Translator successfully:///////////////
		tempVar.Popl_T = 0;
		tempVar.Popl_M = 0;
		tempVar.Popl_F = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.Popl_T += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
				tempVar.Popl_M += pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] / 10000.0;
				tempVar.Popl_F += pplPredict.get( Year.getYear( year ) ).get(
						XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.nianzhongRK = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.nianzhongRK += ( ( pplPredict.get(
						Year.getYear( year - 1 ) ).get( XB.Male )[ X ]
						+ pplPredict.get( Year.getYear( year ) ).get( XB.Male )[ X ]
						+ pplPredict.get( Year.getYear( year - 1 ) ).get(
								XB.Female )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 2.0 ) / 10000.0;
			}
		}
		tempVar.xinzengRK = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				tempVar.xinzengRK += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ]
						- pplPredict.get( Year.getYear( year - 1 ) ).get(
								XB.Male )[ X ]
						+ pplPredict.get( Year.getYear( year ) )
								.get( XB.Female )[ X ] - pplPredict.get(
						Year.getYear( year - 1 ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.RKzengZhangL = tempVar.xinzengRK / tempVar.nianzhongRK * 1000.0;
		tempVar.T_teen = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X <= 14.0 )
			{
				tempVar.T_teen += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.T_labor = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 15.0 && X <= 64.0 )
			{
				tempVar.T_labor += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.T_younglabor = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 15.0 && X <= 44.0 )
			{
				tempVar.T_younglabor += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.T_old = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 65.0 )
			{
				tempVar.T_old += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.T_oldM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 65.0 )
			{
				tempVar.T_oldM += pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.T_oldF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 65.0 )
			{
				tempVar.T_oldF += pplPredict.get( Year.getYear( year ) ).get(
						XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.T_high = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 80.0 )
			{
				tempVar.T_high += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.T_highM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 80.0 )
			{
				tempVar.T_highM += pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.T_highF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 80.0 )
			{
				tempVar.T_highF += pplPredict.get( Year.getYear( year ) ).get(
						XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.yuLing_F15 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 15.0 && X <= 49.0 )
			{
				tempVar.yuLing_F15 += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.hunYu_M15 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 15.0 && X <= 49.0 )
			{
				tempVar.hunYu_M15 += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.yuLing_F20 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 20.0 && X <= 49.0 )
			{
				tempVar.yuLing_F20 += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.hunYu_M20 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 20.0 && X <= 49.0 )
			{
				tempVar.hunYu_M20 += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.changshou_M0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= tempVar.EM0 )
			{
				tempVar.changshou_M0 += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.changshou_F0 = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= tempVar.EF0 )
			{
				tempVar.changshou_F0 += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.T_yingyouer = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X <= 2.0 )
			{
				tempVar.T_yingyouer += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.XQ = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 3.0 && X <= 5.0 )
			{
				tempVar.XQ += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.XX = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 6.0 && X <= 11.0 )
			{
				tempVar.XX += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.CZ = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 12.0 && X <= 14.0 )
			{
				tempVar.CZ += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.GZ = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 15.0 && X <= 17.0 )
			{
				tempVar.GZ += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.DX = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 18.0 && X <= 21.0 )
			{
				tempVar.DX += ( pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] + pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.JYM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 18.0 && X <= 59.0 )
			{
				tempVar.JYM += pplPredict.get( Year.getYear( year ) ).get(
						XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.JYF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 18.0 && X <= 59.0 )
			{
				tempVar.JYF += pplPredict.get( Year.getYear( year ) ).get(
						XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.GB_laoqian = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X <= 15.0 )
			{
				tempVar.GB_laoqian += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.GB_laolingM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 16.0 && X <= 59.0 )
			{
				tempVar.GB_laolingM += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.GB_laolingF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 16.0 && X <= 54.0 )
			{
				tempVar.GB_laolingF += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.GB_qinglao = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 16.0 && X <= 44.0 )
			{
				tempVar.GB_qinglao += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.GB_zhonglaoM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 45.0 && X <= 59.0 )
			{
				tempVar.GB_zhonglaoM += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.GB_zhonglingF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 45.0 && X <= 54.0 )
			{
				tempVar.GB_zhonglingF += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.GB_laohouM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 60.0 )
			{
				tempVar.GB_laohouM += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.GB_laohouF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 55.0 )
			{
				tempVar.GB_laohouF += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.GB_shounian = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X <= 15.0 )
			{
				tempVar.GB_shounian += ( pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] + pplPredict.get(
						Year.getYear( year ) ).get( XB.Female )[ X ] ) / 10000.0;
			}
		}
		tempVar.GB_labornianM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 16.0 && X <= 59.0 )
			{
				tempVar.GB_labornianM += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.GB_oldnianM = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 60.0 )
			{
				tempVar.GB_oldnianM += pplPredict.get( Year.getYear( year ) )
						.get( XB.Male )[ X ] / 10000.0;
			}
		}
		tempVar.G_labornianF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 16.0 && X <= 59.0 )
			{
				tempVar.G_labornianF += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		tempVar.G_oldnianF = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( X >= 60.0 )
			{
				tempVar.G_oldnianF += pplPredict.get( Year.getYear( year ) )
						.get( XB.Female )[ X ] / 10000.0;
			}
		}
		
		
		//TODO ??? 
		//CALC STD(&MND2+&FND2),AVG(&MND2+&FND2) TO ST,AV
		//tempVar.ST = 0;
		//tempVar.AV = 0;
		/*************by DS:??tempVar.ST***************/
		double sum = 0;
		for( X = MAX_AGE-1;X>=0;X--){
			sum+=pplPredict.get(Year.getYear(year)).get(XB.Female)[X];
			sum+=pplPredict.get(Year.getYear(year)).get(XB.Male)[X];
		}
		tempVar.AV = sum/MAX_AGE;
		sum = 0;
		for( X = MAX_AGE-1; X>=0; X--){
			sum += Math.pow(pplPredict.get(Year.getYear(year)).get(XB.Female)[X]+pplPredict.get(Year.getYear(year)).get(XB.Male)[X]-tempVar.AV, 2);
		}
		tempVar.ST = Math.sqrt(sum/MAX_AGE);
		/*************END*****************/
		
		// F0IEL0 = '&MND2+&FND2';
		RKZ = 0;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( true )
			{
				// RKZ += &FIEL0;
				RKZ += pplPredict.get( Year.getYear( year ) ).get( XB.Male )[ X ]
						+ pplPredict.get( Year.getYear( year ) )
								.get( XB.Female )[ X ];
			}
		}
		if ( RKZ > 0.0 )
		{
			// GO TOP
			PZ = RKZ / 2.0;
			// DO WHILE PZ>0.AND.EOF()=.F.
			X = 0;
			while ( PZ > 0 && X < 111 )
			{
				// PZ = PZ - ( &FIEL0 );
				PZ = PZ
						- ( pplPredict.get( Year.getYear( year ) )
								.get( XB.Male )[ X ] + pplPredict.get(
								Year.getYear( year ) ).get( XB.Female )[ X ] );

				// SKIP
				X++;
			}

			// SKIP -1
			X--;
			tempVar.NLZ = X
					+ ( PZ + ( pplPredict.get( Year.getYear( year ) ).get(
							XB.Male )[ X ] + pplPredict.get(
							Year.getYear( year ) ).get( XB.Female )[ X ] ) )
					/ ( ( pplPredict.get( Year.getYear( year ) ).get( XB.Male )[ X ] + pplPredict
							.get( Year.getYear( year ) ).get( XB.Female )[ X ] ) );
		} else
		{
			tempVar.NLZ = 0.0;
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

		/**********test??***********/
//		System.out.println("???T"+tempVar.Popl_T);
//		System.out.println("???F"+tempVar.Popl_F);
//		System.out.println("???M"+tempVar.Popl_M);
//		System.out.println("????"+tempVar.nianzhongRK);
//		System.out.println("????"+tempVar.xinzengRK);
//		System.out.println("????L"+tempVar.RKzengZhangL);
//		System.out.println("T??"+tempVar.T_teen);
//		System.out.println("T??"+tempVar.T_labor);
//		System.out.println("T??"+tempVar.T_younglabor);
//		System.out.println("T??"+tempVar.T_old);
//		System.out.println("T??M"+tempVar.T_oldM);
//		System.out.println("T??F"+tempVar.T_oldF);
//		System.out.println("T??"+tempVar.T_high);
//		System.out.println("T??M"+tempVar.T_highM);
//		System.out.println("T??F"+tempVar.T_highF);
//		System.out.println("??F15"+tempVar.yuLing_F15);
//		System.out.println("??M15"+tempVar.hunYu_M15);
//		System.out.println("??F20"+tempVar.yuLing_F20);
//		System.out.println("??M20"+tempVar.hunYu_M20);
//		System.out.println("??M0:"+tempVar.changshou_M0);
//		System.out.println("??F0:"+tempVar.changshou_F0);
//		System.out.println("T???:"+tempVar.T_yingyouer);
//		System.out.println("JYM:"+tempVar.JYM);
//		System.out.println("XX:"+tempVar.XX);
//		System.out.println("CZ:"+tempVar.CZ);
//		System.out.println("GZ:"+tempVar.GZ);
//		System.out.println("DX:"+tempVar.DX);
//		System.out.println("JYM:"+tempVar.JYM);
//		System.out.println("JYF:"+tempVar.JYF);
//		System.out.println("GB??:"+tempVar.GB_laoqian);
//		System.out.println("GB??M:"+tempVar.GB_laolingM);
//		System.out.println("GB??F:"+tempVar.GB_laolingF);
//		System.out.println("GB??:"+tempVar.GB_qinglao);
//		System.out.println("GB??M:"+tempVar.GB_zhonglaoM);
//		System.out.println("GB??F:"+tempVar.GB_zhonglingF);
//		System.out.println("GB??M:"+tempVar.GB_laohouM);
//		System.out.println("GB??F:"+tempVar.GB_laohouF);
//		System.out.println("G??:"+tempVar.GB_shounian);//shaonian ??shounian
//		System.out.println("G??M:"+tempVar.GB_labornianM);
//		System.out.println("G??M:"+tempVar.GB_oldnianM);
//		System.out.println("G??F:"+tempVar.G_labornianF);
//		System.out.println("G??F:"+tempVar.G_oldnianF);
//		System.out.println("ST:"+tempVar.ST);
//		System.out.println("AV:"+tempVar.AV);
//		System.out.println("RKZ:"+RKZ);
//		System.out.println("NLZ:"+tempVar.NLZ);
		
		/**********test End*********/




		
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

