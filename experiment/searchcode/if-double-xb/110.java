package prclqz.methods;

import static prclqz.core.enumLib.HunpeiField.*;
import static prclqz.core.enumLib.XB.*;
import static prclqz.core.enumLib.CX.*;
import static prclqz.core.enumLib.NY.*;
import static prclqz.core.Const.*;
import java.util.HashMap;
import java.util.Map;

import prclqz.DAO.IDAO;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.Message;
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

import prclqz.DAO.IDAO;
import prclqz.core.Message;
/**
 * ??????????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MBabiesBornByHuigui implements IBabiesBornMethod 
{

	

	@Override
	public void calculate(IDAO m, HashMap<String, Object> globals,
			IBabiesBornByParentTypeMethod bornByParentType,IBabiesBornByDuijiMethod bornByDuiji,  IBabiesRateMethod babiesRate,
			IBabiesReleaseClassifyMethod releaAClssfy, IBabiesBornByFutureMethod classification)throws Exception
	{
		//get variables from globals
		TempVariablesBean tempVar = (TempVariablesBean) globals.get("tempVarBean");
		HashMap<String,Object> predictVarMap = (HashMap<String, Object>) globals.get("predictVarMap");
		HashMap<String,BornXbbBean> xbbMap = (HashMap<String, BornXbbBean>) globals.get("bornXbbBeanMap");
		//get running status
		int dqx = tempVar.getProvince();
		int year = tempVar.getYear();
		BornXbbBean xbb = xbbMap.get(""+dqx);
		String diDai = xbb.getDiDai();
		//define local variables
		int X=0;
		//????????
		Map<Babies,double[]> policyBabies = (Map<Babies, double[]>) predictVarMap.get("PolicyBabies"+dqx);
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

		double ZRK=0,CZRK=0;
		// ////////////translated by Foxpro2Java Translator
		// successfully:///////////////
		for ( X = MAX_AGE - 1;X >= 0;X -- )
		{
			if ( true )
			{
				ZRK += PopulationPredictOfAll.get ( Year.getYear ( year ) )
						.get ( Male ) [ X ]
						+ PopulationPredictOfAll.get ( Year.getYear ( year ) )
								.get ( Female ) [ X ];
				CZRK += PopulationPredictOfCX.get ( Chengshi ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ]
						+ PopulationPredictOfCX.get ( Chengshi ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
		}
		tempVar.CSHLevel2 = CZRK / ZRK;
		if ( tempVar.nowPolicyRate1 < 0.99 )
		{
			tempVar.implableTFR0 = tempVar.dqPolicyTFR
					* ( tempVar.nowPolicyRate1 - 0.02067
							* ( tempVar.CSHLevel2 * 100.0 - tempVar.CSHLevel1 ) - 1.072 * ( tempVar.dqPolicyTFR1 - tempVar.dqPolicyTFR ) );
			tempVar.implRate = ( tempVar.nowPolicyRate1 - 0.02067
					* ( tempVar.CSHLevel2 * 100.0 - tempVar.CSHLevel1 ) - 1.072 * ( tempVar.dqPolicyTFR1 - tempVar.dqPolicyTFR ) );
		} else if ( tempVar.nowPolicyRate1 > 1.01 )
		{
			tempVar.implableTFR0 = tempVar.dqPolicyTFR
					* ( tempVar.nowPolicyRate1 - 0.02067
							* ( tempVar.CSHLevel2 * 100.0 - tempVar.CSHLevel1 ) - 1.072 * ( tempVar.dqPolicyTFR - tempVar.dqPolicyTFR1 ) );
			tempVar.implRate = ( tempVar.nowPolicyRate1 - 0.02067
					* ( tempVar.CSHLevel2 * 100.0 - tempVar.CSHLevel1 ) - 1.072 * ( tempVar.dqPolicyTFR - tempVar.dqPolicyTFR1 ) );
		} else if ( tempVar.nowPolicyRate1 >= 0.99
				&& tempVar.nowPolicyRate1 <= 1.01 )
		{
			tempVar.implableTFR0 = tempVar.dqPolicyTFR;
			tempVar.implRate = 1.0;
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		
		
		
		if(  (diDai.equals ( "??" )||diDai.equals ( "??" )) && (tempVar.implableTFR0 < tempVar.dqPolicyTFR) )
			tempVar.implableTFR0 = tempVar.dqPolicyTFR;
		else if( diDai.equals ( "??" ) && tempVar.implableTFR0 < tempVar.MITFR )
			tempVar.implableTFR0 = tempVar.MITFR;
		else
			tempVar.implableTFR0 = tempVar.implableTFR0;
		
//		double NMDFR=0,NFDMR=0,NNR=0;
		// ////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( CX cx : CX.values ( ) )
			for ( NY ny : NY.values ( ) )
			{
				tempVar.cx = cx;
				tempVar.ny = ny;
				
				if ( ny == Nongye )
				{
					tempVar.NMDFR = tempVar.N1D2N * tempVar.implRate;
					tempVar.NFDMR = tempVar.N2D1N * tempVar.implRate;
					tempVar.NNR = tempVar.NNFN * tempVar.implRate;
				} else
				{
					tempVar.NMDFR = tempVar.N1D2C * tempVar.implRate;
					tempVar.NFDMR = tempVar.N2D1C * tempVar.implRate;
					tempVar.NNR = tempVar.NNFC * tempVar.implRate;
				}
				
				// 2nd level
				bornByParentType.calculate ( m , globals );
				
				Map < HunpeiField , double [ ] > zn = ziNv.get ( cx ).get ( ny );
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						zn.get ( OverB_DDB ) [ X ] = zn.get ( DDB ) [ X ]
								- zn.get ( PlyDDB ) [ X ];
						zn.get ( OverB_NNB ) [ X ] = zn.get ( NNB ) [ X ]
								- zn.get ( PlyNNB ) [ X ];
						zn.get ( OverB_NMDFB ) [ X ] = zn.get ( NMDFB ) [ X ]
								- zn.get ( PlyNMDFB ) [ X ];
						zn.get ( OverB_NFDMB ) [ X ] = zn.get ( NFDMB ) [ X ]
								- zn.get ( PlyNFDMB ) [ X ];
						zn.get ( OverB_RlsB ) [ X ] = zn.get ( SingleRls ) [ X ]
								+ zn.get ( ShuangFeiRls ) [ X ]
								- zn.get ( PlyRlsB ) [ X ];
						zn.get ( Over_Birth ) [ X ] = zn.get ( OverB_DDB ) [ X ]
								+ zn.get ( OverB_NNB ) [ X ]
								+ zn.get ( OverB_NMDFB ) [ X ]
								+ zn.get ( OverB_NFDMB ) [ X ]
								+ zn.get ( OverB_RlsB ) [ X ];
					}
				}
				if ( cx == Chengshi && ny == Feinong )
				{
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							OverBirthPredictOfCX.get ( Nongchun ).get (
									Babies.getBabies ( year ) ) [ X ] = ziNv
									.get ( Nongchun ).get ( Feinong ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Nongchun ).get ( Nongye ).get (
											Over_Birth ) [ X ];
						}
						if ( true )
						{
							OverBirthPredictOfCX.get ( Chengshi ).get (
									Babies.getBabies ( year ) ) [ X ] = ziNv
									.get ( Chengshi ).get ( Feinong ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Nongye ).get (
											Over_Birth ) [ X ];
						}
						if ( true )
						{
							OverBirthPredictOfNY.get ( Nongye ).get (
									Babies.getBabies ( year ) ) [ X ] = ziNv
									.get ( Nongchun ).get ( Nongye ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Nongye ).get (
											Over_Birth ) [ X ];
						}
						if ( true )
						{
							OverBirthPredictOfNY.get ( Feinong ).get (
									Babies.getBabies ( year ) ) [ X ] = ziNv
									.get ( Nongchun ).get ( Feinong ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Feinong )
											.get ( Over_Birth ) [ X ];
						}
						if ( true )
						{
							OverBirthPredictOfAll.get ( Babies
									.getBabies ( year ) ) [ X ] = ziNv.get (
									Nongchun ).get ( Feinong )
									.get ( Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Feinong )
											.get ( Over_Birth ) [ X ]
									+ ziNv.get ( Nongchun ).get ( Nongye ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Nongye ).get (
											Over_Birth ) [ X ];
						}
						if ( true )
						{
							ziNvOfNY.get ( Nongye ).get ( Over_Birth ) [ X ] = ziNv
									.get ( Nongchun ).get ( Nongye ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Nongye ).get (
											Over_Birth ) [ X ];
							ziNvOfNY.get ( Feinong ).get ( Over_Birth ) [ X ] = ziNv
									.get ( Nongchun ).get ( Feinong ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Feinong )
											.get ( Over_Birth ) [ X ];
							ziNvOfCX.get ( Chengshi ).get ( Over_Birth ) [ X ] = ziNv
									.get ( Chengshi ).get ( Nongye ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Chengshi ).get ( Feinong )
											.get ( Over_Birth ) [ X ];
							ziNvOfCX.get ( Nongchun ).get ( Over_Birth ) [ X ] = ziNv
									.get ( Nongchun ).get ( Nongye ).get (
											Over_Birth ) [ X ]
									+ ziNv.get ( Nongchun ).get ( Feinong )
											.get ( Over_Birth ) [ X ];
							ziNvOfAll.get ( Over_Birth ) [ X ] = ziNvOfNY.get (
									Nongye ).get ( Over_Birth ) [ X ]
									+ ziNvOfNY.get ( Feinong )
											.get ( Over_Birth ) [ X ];
						}
					}
					tempVar.overBirthS = 0;
					for ( X = MAX_AGE - 1;X >= 0;X -- )
					{
						if ( true )
						{
							tempVar.overBirthS += OverBirthPredictOfAll
									.get ( Babies.getBabies ( year ) ) [ X ];
						}
					}
				}
				
				//?? ???????? AR TODO ?????????
				
				tempVar.S_duiji = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X <= 49.0 )
					{
						tempVar.S_duiji += zn.get ( SingleDJ ) [ X ]
								+ zn.get ( ShuangfeiDJ ) [ X ];
					}
				}
				
				if ( tempVar.S_duiji != 0.0 && year >= tempVar.getPolicyTime ( )
						&& ( year - tempVar.getPolicyTime ( ) + 1.0 ) <= 35.0 )
				{
					//call level-2 procedures
					babiesRate.calculate( m , globals );
					bornByDuiji.calculate(m, globals);
				}
			}
		/////////////end of translating, by Foxpro2Java Translator///////////////


		
		
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

