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

import prclqz.DAO.IDAO;
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.Message;
import prclqz.core.StringList;
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.Policy;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

import prclqz.lib.EnumMapTool;
import prclqz.parambeans.ParamBean3;
/**
 * ????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MSumOfDqTefuParent implements IMethod
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
		
		
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1;X >= 0;X -- )
		{
			if ( true )
			{
				tefuC.get ( Chengshi ).get ( Year.getYear ( year ) )
						.get ( Male ) [ X ] = tefuCN.get ( Chengshi ).get (
						Feinong ).get ( Year.getYear ( year ) ).get ( Male ) [ X ]
						+ tefuCN.get ( Chengshi ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				tefuC.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = tefuCN.get ( Chengshi ).get ( Feinong )
						.get ( Year.getYear ( year ) ).get ( Female ) [ X ]
						+ tefuCN.get ( Chengshi ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				tefuC.get ( Nongchun ).get ( Year.getYear ( year ) )
						.get ( Male ) [ X ] = tefuCN.get ( Nongchun ).get (
						Feinong ).get ( Year.getYear ( year ) ).get ( Male ) [ X ]
						+ tefuCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				tefuC.get ( Nongchun ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = tefuCN.get ( Nongchun ).get ( Feinong )
						.get ( Year.getYear ( year ) ).get ( Female ) [ X ]
						+ tefuCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				tefuN.get ( Feinong ).get ( Year.getYear ( year ) ).get ( Male ) [ X ] = tefuCN
						.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ tefuCN.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				tefuN.get ( Feinong ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = tefuCN.get ( Chengshi ).get ( Feinong )
						.get ( Year.getYear ( year ) ).get ( Female ) [ X ]
						+ tefuCN.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				tefuN.get ( Nongye ).get ( Year.getYear ( year ) ).get ( Male ) [ X ] = tefuCN
						.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ tefuCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				tefuN.get ( Nongye ).get ( Year.getYear ( year ) )
						.get ( Female ) [ X ] = tefuCN.get ( Nongchun ).get (
						Nongye ).get ( Year.getYear ( year ) ).get ( Female ) [ X ]
						+ tefuCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				tefuA.get ( Year.getYear ( year ) ).get ( Male ) [ X ] = tefuC
						.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
								Male ) [ X ]
						+ tefuC.get ( Nongchun ).get ( Year.getYear ( year ) )
								.get ( Male ) [ X ];
				tefuA.get ( Year.getYear ( year ) ).get ( Female ) [ X ] = tefuC
						.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
								Female ) [ X ]
						+ tefuC.get ( Nongchun ).get ( Year.getYear ( year ) )
								.get ( Female ) [ X ];
			}
			if ( true )
			{
				yiHaiC.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
						Male ) [ X ] = yiHaiCN.get ( Chengshi ).get ( Feinong )
						.get ( Year.getYear ( year ) ).get ( Male ) [ X ]
						+ yiHaiCN.get ( Chengshi ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				yiHaiC.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = yiHaiCN.get ( Chengshi )
						.get ( Feinong ).get ( Year.getYear ( year ) ).get (
								Female ) [ X ]
						+ yiHaiCN.get ( Chengshi ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				yiHaiC.get ( Nongchun ).get ( Year.getYear ( year ) ).get (
						Male ) [ X ] = yiHaiCN.get ( Nongchun ).get ( Feinong )
						.get ( Year.getYear ( year ) ).get ( Male ) [ X ]
						+ yiHaiCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				yiHaiC.get ( Nongchun ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = yiHaiCN.get ( Nongchun )
						.get ( Feinong ).get ( Year.getYear ( year ) ).get (
								Female ) [ X ]
						+ yiHaiCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				yiHaiN.get ( Feinong ).get ( Year.getYear ( year ) )
						.get ( Male ) [ X ] = yiHaiCN.get ( Chengshi ).get (
						Feinong ).get ( Year.getYear ( year ) ).get ( Male ) [ X ]
						+ yiHaiCN.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				yiHaiN.get ( Feinong ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = yiHaiCN.get ( Chengshi )
						.get ( Feinong ).get ( Year.getYear ( year ) ).get (
								Female ) [ X ]
						+ yiHaiCN.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				yiHaiN.get ( Nongye ).get ( Year.getYear ( year ) ).get ( Male ) [ X ] = yiHaiCN
						.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ yiHaiCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				yiHaiN.get ( Nongye ).get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = yiHaiCN.get ( Nongchun ).get ( Nongye )
						.get ( Year.getYear ( year ) ).get ( Female ) [ X ]
						+ yiHaiCN.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
			if ( true )
			{
				yiHaiA.get ( Year.getYear ( year ) ).get ( Male ) [ X ] = yiHaiC
						.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
								Male ) [ X ]
						+ yiHaiC.get ( Nongchun ).get ( Year.getYear ( year ) )
								.get ( Male ) [ X ];
				yiHaiA.get ( Year.getYear ( year ) ).get ( Female ) [ X ] = yiHaiC
						.get ( Chengshi ).get ( Year.getYear ( year ) ).get (
								Female ) [ X ]
						+ yiHaiC.get ( Nongchun ).get ( Year.getYear ( year ) )
								.get ( Female ) [ X ];
			}
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////
		
		double TFM,TFF,s_yihaiFa,s_yihaiMa;
		for ( CX cx : CX.values ( ) )
			for ( NY ny : NY.values ( ) )
			{
				Map<Year,Map<XB,double[]>>sondie = SonDiePopulationPredictOfYiHaiCXNY.get ( cx ).get ( ny );
				Map<Year,Map<XB,double[]>>tefu = SonDiePopulationPredictOfTeFuCXNY.get ( cx ).get ( ny );
				Map < Year , Map < Couple , Double >> couple = CoupleAndChildrenOfCXNY.get ( cx ).get ( ny );
				
				//////////////translated by Foxpro2Java Translator successfully:///////////////
				TFM = 0;
				TFF = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X >= 49.0 )
					{
						TFM += tefu.get ( Year.getYear ( year ) ).get ( Male ) [ X ];
						TFF += tefu.get ( Year.getYear ( year ) ).get ( Female ) [ X ];
					}
				}
				s_yihaiFa = 0;
				s_yihaiMa = 0;
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( X >= 60.0 )
					{
						s_yihaiFa += sondie.get ( Year.getYear ( year ) ).get (
								Male ) [ X ];
						s_yihaiMa += sondie.get ( Year.getYear ( year ) ).get (
								Female ) [ X ];
					}
				}
				for ( X = MAX_AGE - 1;X >= 0;X -- )
				{
					if ( true )
					{
						couple.get ( Year.getYear ( year ) ).put (
								Couple.TeFuF , TFF );
						couple.get ( Year.getYear ( year ) ).put (
								Couple.TeFuM , TFM );
						couple.get ( Year.getYear ( year ) ).put ( Couple.TeFu ,
								TFF + TFM );
						couple.get ( Year.getYear ( year ) ).put (
								Couple.YiHaiFa , s_yihaiFa );
						couple.get ( Year.getYear ( year ) ).put (
								Couple.YiHaiMa , s_yihaiMa );
						couple.get ( Year.getYear ( year ) ).put (
								Couple.YiHaiP , s_yihaiFa + s_yihaiMa );
					}
				}
				/////////////end of translating, by Foxpro2Java Translator///////////////

			}
		

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

