package prclqz.methods;
import static prclqz.core.enumLib.SYSFMSField.*;
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
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

/**
 * ??????????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MBabiesBornByYiyuan implements IBabiesBornMethod 
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
		
		if(diDai.equals ( "??" ))
			diDai = "??";
		
		//define local variables
		int X=0;
		BabiesBornBean bBornBean = tempVar.getBabiesBorn();
		//????
		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
		Map<SYSFMSField,double[]> pBirthWill = birthWill.get(diDai);
		//??
		//????????
		Map<Babies,double[]> policyBabies = (Map<Babies, double[]>) predictVarMap.get("PolicyBabies"+dqx);
		//?? ???????
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY"+dqx );//provincMigMap.get(""+dqx);
		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx );//provincMigMap.get("NY"+dqx);
		Map<CX,Map<HunpeiField,double[]>> ziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX"+dqx );//provincMigMap.get("CX"+dqx);
		Map<HunpeiField,double[]> ziNvOfAll = (Map<HunpeiField, double[]>) predictVarMap.get( "HunpeiOfAll"+dqx );//provincMigMap.get("All"+dqx);
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

		double DDFR1,NMDFFR1,NFDMFR1,NNFR1,
		   		DDFR2,NMDFFR2,NFDMFR2,NNFR2;
		double A, DB, B2,B3 , B4;
		double DD1, NMDF1 , NFDM1, NN1,
				DD2, NMDF2 , NFDM2, NN2;
	
		for ( CX cx : CX.values ( ) )
			for ( NY ny : NY.values ( ) )
			{
				Map < HunpeiField , double [ ] > zn = ziNv.get ( cx ).get ( ny );
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				if ( ny == Nongye )
				{
					tempVar.NMDFR = tempVar.N1D2N;
					tempVar.NFDMR = tempVar.N2D1N;
					tempVar.NNR = tempVar.NNFN;
				} else
				{
					tempVar.NMDFR = tempVar.N1D2C;
					tempVar.NFDMR = tempVar.N2D1C;
					tempVar.NNR = tempVar.NNFC;
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						zn.get ( coupleNum ) [ X ] = zn.get ( DD ) [ X ]
								+ zn.get ( NMDF ) [ X ] + zn.get ( NFDM ) [ X ]
								+ zn.get ( NN ) [ X ]
								+ zn.get ( SingleDJ ) [ X ]
								+ zn.get ( ShuangfeiDJ ) [ X ]
								+ zn.get ( Yi2Single ) [ X ]
								+ zn.get ( Yi2Shuangfei ) [ X ];
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////
				DDFR1 = (tempVar.getDDFR() < 1 )? tempVar.getDDFR() : 1;
				NMDFFR1 = (tempVar.getNMDFR() < 1)? tempVar.getNMDFR() : 1;
				NFDMFR1 = (tempVar.getNFDMR() < 1)? tempVar.getNFDMR() : 1;
				NNFR1 = (tempVar.getNNR() < 1)? tempVar.getNNR() : 1;
				
				DDFR2 = (tempVar.getDDFR() < 1 )? 0 : tempVar.getDDFR()-1;
				NMDFFR2 = (tempVar.getNMDFR() < 1)? 0 :tempVar.getNMDFR()-1;
				NFDMFR2 = (tempVar.getNFDMR() < 1)? 0 :tempVar.getNFDMR()-1;
				NNFR2 = (tempVar.getNNR() < 1)? 0 :tempVar.getNNR()-1;
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						try{
						zn.get ( DDB ) [ X ] = zn.get ( DD ) [ X ] * DDFR1
								* zn.get ( FR1 ) [ X ] + zn.get ( DD ) [ X ]
								* DDFR2 * zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
						}catch( Exception e){
							System.out.println(e);
						}
					}
					if ( true )
					{
						zn.get ( NNB ) [ X ] = zn.get ( NN ) [ X ] * NNFR1
								* zn.get ( FR1 ) [ X ] + zn.get ( NN ) [ X ]
								* NNFR2 * zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
					}
					if ( true )
					{
						zn.get ( NMDFB ) [ X ] = zn.get ( NMDF ) [ X ]
								* NMDFFR1 * zn.get ( FR1 ) [ X ]
								+ zn.get ( NMDF ) [ X ] * NMDFFR2
								* zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
					}
					if ( true )
					{
						zn.get ( NFDMB ) [ X ] = zn.get ( NFDM ) [ X ]
								* NFDMFR1 * zn.get ( FR1 ) [ X ]
								+ zn.get ( NFDM ) [ X ] * NFDMFR2
								* zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
					}
				}
				A = 0;
				bBornBean.DB = 0;
				B2 = 0;
				B3 = 0;
				B4 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						A += zn.get ( FRD ) [ X ];
						bBornBean.DB += zn.get ( DDB ) [ X ];
						B2 += zn.get ( NMDFB ) [ X ];
						B3 += zn.get ( NFDMB ) [ X ];
						B4 += zn.get ( NNB ) [ X ];
					}
				}
				DD1 = 0;
				bBornBean.NMDF1 = 0;
				bBornBean.NFDM1 = 0;
				bBornBean.NN1 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						DD1 += zn.get ( DD ) [ X ] * DDFR1
								* zn.get ( FR1 ) [ X ];
						bBornBean.NMDF1 += zn.get ( NMDF ) [ X ] * NMDFFR1
								* zn.get ( FR1 ) [ X ];
						bBornBean.NFDM1 += zn.get ( NFDM ) [ X ] * NFDMFR1
								* zn.get ( FR1 ) [ X ];
						bBornBean.NN1 += zn.get ( NN ) [ X ] * NNFR1
								* zn.get ( FR1 ) [ X ];
					}
				}
				DD2 = 0;
				NMDF2 = 0;
				NFDM2 = 0;
				bBornBean.NN2 = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						DD2 += zn.get ( DD ) [ X ] * DDFR2
								* zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
						NMDF2 += zn.get ( NMDF ) [ X ] * NMDFFR2
								* zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
						NFDM2 += zn.get ( NFDM ) [ X ] * NFDMFR2
								* zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
						bBornBean.NN2 += zn.get ( NN ) [ X ] * NNFR2
								* zn.get ( FR2 ) [ X ]
								* pBirthWill.get ( yiyuan ) [ X ];
					}
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
				/////////////end of translating, by Foxpro2Java Translator///////////////
				if ( cx == Chengshi && ny == Feinong )
				{
					//////////////translated by Foxpro2Java Translator successfully:///////////////
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
					/////////////end of translating, by Foxpro2Java Translator///////////////

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
					bornByDuiji.calculate(m, globals);
				}
				
			}// end of LOOP-CX-NY
	}
	@Override
	public void calculate ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		// TODO Auto-generated method stub

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

