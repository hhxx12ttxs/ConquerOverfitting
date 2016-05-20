package prclqz.methods;

import static prclqz.core.enumLib.HunpeiField.*;
import static prclqz.core.enumLib.XB.*;
import static prclqz.core.enumLib.CX.*;
import static prclqz.core.enumLib.NY.*;
import static prclqz.core.Const.*;
import java.util.HashMap;
import java.util.Map;

import prclqz.DAO.IDAO;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.Message;
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

/**
 * ??????
 * @author prclqz@zju.edu.cn
 *
 */
public class MSumAllByAge implements IMethod
{

	@Override
	public void calculate ( IDAO m , HashMap < String , Object > globals )
			throws Exception
	{
		//get variables from globals
		TempVariablesBean tempVar = (TempVariablesBean) globals.get("tempVarBean");
		HashMap<String,Object> predictVarMap = (HashMap<String, Object>) globals.get("predictVarMap");
		//get running status
		int dqx = tempVar.getProvince();
		int year = tempVar.getYear();
		//define local variables
		int X=0;
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
		//??????
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> PopulationPredictOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "PopulationPredictOfCXNY"+dqx );
		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );
		
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		for ( X = MAX_AGE - 1;X >= 0;X -- )
		{
			if ( true )
			{
				ziNv.get ( Nongchun ).get ( Nongye ).get ( D ) [ X ] = ziNv
						.get ( Nongchun ).get ( Nongye ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DM ) [ X ];
				ziNv.get ( Nongchun ).get ( Nongye ).get ( N ) [ X ] = ziNv
						.get ( Nongchun ).get ( Nongye ).get ( NF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NM ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Nongchun ).get ( Feinong ).get ( D ) [ X ] = ziNv
						.get ( Nongchun ).get ( Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DM ) [ X ];
				ziNv.get ( Nongchun ).get ( Feinong ).get ( N ) [ X ] = ziNv
						.get ( Nongchun ).get ( Feinong ).get ( NF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NM ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Chengshi ).get ( Nongye ).get ( D ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( DF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( DM ) [ X ];
				ziNv.get ( Chengshi ).get ( Nongye ).get ( N ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( NF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( NM ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Chengshi ).get ( Feinong ).get ( D ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( DM ) [ X ];
				ziNv.get ( Chengshi ).get ( Feinong ).get ( N ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( NF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( NM ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Nongchun ).get ( Feinong ).get ( HJM ) [ X ] = ziNv
						.get ( Nongchun ).get ( Feinong ).get ( DM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DDBM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NM ) [ X ];
				ziNv.get ( Nongchun ).get ( Feinong ).get ( HJF ) [ X ] = ziNv
						.get ( Nongchun ).get ( Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DDBF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NF ) [ X ];
				ziNv.get ( Nongchun ).get ( Feinong ).get ( HJ ) [ X ] = ziNv
						.get ( Nongchun ).get ( Feinong ).get ( HJM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Nongchun ).get ( Nongye ).get ( HJM ) [ X ] = ziNv
						.get ( Nongchun ).get ( Nongye ).get ( DM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DDBM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NM ) [ X ];
				ziNv.get ( Nongchun ).get ( Nongye ).get ( HJF ) [ X ] = ziNv
						.get ( Nongchun ).get ( Nongye ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DDBF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NF ) [ X ];
				ziNv.get ( Nongchun ).get ( Nongye ).get ( HJ ) [ X ] = ziNv
						.get ( Nongchun ).get ( Nongye ).get ( HJM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Chengshi ).get ( Feinong ).get ( HJM ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( DM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( DDBM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( NM ) [ X ];
				ziNv.get ( Chengshi ).get ( Feinong ).get ( HJF ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( DDBF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( NF ) [ X ];
				ziNv.get ( Chengshi ).get ( Feinong ).get ( HJ ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( HJM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Feinong ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				ziNv.get ( Chengshi ).get ( Nongye ).get ( HJM ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( DM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( DDBM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( NM ) [ X ];
				ziNv.get ( Chengshi ).get ( Nongye ).get ( HJF ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( DF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( DDBF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( NF ) [ X ];
				ziNv.get ( Chengshi ).get ( Nongye ).get ( HJ ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( HJM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Feinong ).get ( DF ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DF ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( DM ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( DM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DM ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( D ) [ X ] = ziNvOfNY.get (
						Feinong ).get ( DM ) [ X ]
						+ ziNvOfNY.get ( Feinong ).get ( DF ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Feinong ).get ( NF ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( NF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NF ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( NM ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( NM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NM ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( N ) [ X ] = ziNvOfNY.get (
						Feinong ).get ( NM ) [ X ]
						+ ziNvOfNY.get ( Feinong ).get ( NF ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Feinong ).get ( N ) [ X ] = ziNvOfNY.get (
						Feinong ).get ( NM ) [ X ]
						+ ziNvOfNY.get ( Feinong ).get ( NF ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( D ) [ X ] = ziNvOfNY.get (
						Feinong ).get ( DM ) [ X ]
						+ ziNvOfNY.get ( Feinong ).get ( DF ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Feinong ).get ( HJF ) [ X ] = ziNv.get (
						Chengshi ).get ( Feinong ).get ( HJF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( HJF ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( HJM ) [ X ] = ziNv.get (
						Chengshi ).get ( Feinong ).get ( HJM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( HJM ) [ X ];
				ziNvOfNY.get ( Feinong ).get ( HJ ) [ X ] = ziNvOfNY.get (
						Feinong ).get ( HJF ) [ X ]
						+ ziNvOfNY.get ( Feinong ).get ( HJM ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Nongye ).get ( DF ) [ X ] = ziNv.get ( Chengshi )
						.get ( Nongye ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DF ) [ X ];
				ziNvOfNY.get ( Nongye ).get ( DM ) [ X ] = ziNv.get ( Chengshi )
						.get ( Nongye ).get ( DM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DM ) [ X ];
				ziNvOfNY.get ( Nongye ).get ( D ) [ X ] = ziNvOfNY
						.get ( Nongye ).get ( DM ) [ X ]
						+ ziNvOfNY.get ( Nongye ).get ( DF ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Nongye ).get ( NF ) [ X ] = ziNv.get ( Chengshi )
						.get ( Nongye ).get ( NF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NF ) [ X ];
				ziNvOfNY.get ( Nongye ).get ( NM ) [ X ] = ziNv.get ( Chengshi )
						.get ( Nongye ).get ( NM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NM ) [ X ];
				ziNvOfNY.get ( Nongye ).get ( N ) [ X ] = ziNvOfNY
						.get ( Nongye ).get ( NM ) [ X ]
						+ ziNvOfNY.get ( Nongye ).get ( NF ) [ X ];
			}
			if ( true )
			{
				ziNvOfNY.get ( Nongye ).get ( HJF ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( HJF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( HJF ) [ X ];
				ziNvOfNY.get ( Nongye ).get ( HJM ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( HJM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( HJM ) [ X ];
				ziNvOfNY.get ( Nongye ).get ( HJ ) [ X ] = ziNvOfNY.get (
						Nongye ).get ( HJF ) [ X ]
						+ ziNvOfNY.get ( Nongye ).get ( HJM ) [ X ];
			}
			if ( true )
			{
				ziNvOfAll.get ( DF ) [ X ] = ziNv.get ( Chengshi ).get (
						Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DF ) [ X ];
				ziNvOfAll.get ( DM ) [ X ] = ziNv.get ( Chengshi ).get (
						Feinong ).get ( DM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( DM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( DM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( DM ) [ X ];
				ziNvOfAll.get ( D ) [ X ] = ziNvOfAll.get ( DM ) [ X ]
						+ ziNvOfAll.get ( DF ) [ X ];
			}
			if ( true )
			{
				ziNvOfAll.get ( NF ) [ X ] = ziNv.get ( Chengshi ).get (
						Feinong ).get ( NF ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( NF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NF ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NF ) [ X ];
				ziNvOfAll.get ( NM ) [ X ] = ziNv.get ( Chengshi ).get (
						Feinong ).get ( NM ) [ X ]
						+ ziNv.get ( Chengshi ).get ( Nongye ).get ( NM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Feinong ).get ( NM ) [ X ]
						+ ziNv.get ( Nongchun ).get ( Nongye ).get ( NM ) [ X ];
				ziNvOfAll.get ( N ) [ X ] = ziNvOfAll.get ( NM ) [ X ]
						+ ziNvOfAll.get ( NF ) [ X ];
			}
			if ( true )
			{
				ziNvOfAll.get ( HJF ) [ X ] = ziNvOfAll.get ( DF ) [ X ]
						+ ziNvOfAll.get ( NF ) [ X ];
				ziNvOfAll.get ( HJM ) [ X ] = ziNvOfAll.get ( DM ) [ X ]
						+ ziNvOfAll.get ( NM ) [ X ];
				ziNvOfAll.get ( HJ ) [ X ] = ziNvOfAll.get ( HJM ) [ X ]
						+ ziNvOfAll.get ( HJF ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfCXNY.get ( Nongchun ).get ( Feinong ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = ziNv.get (
						Nongchun ).get ( Feinong ).get ( HJM ) [ X ];
				PopulationPredictOfCXNY.get ( Nongchun ).get ( Feinong ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = ziNv
						.get ( Nongchun ).get ( Feinong ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfCXNY.get ( Chengshi ).get ( Feinong ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = ziNv.get (
						Chengshi ).get ( Feinong ).get ( HJM ) [ X ];
				PopulationPredictOfCXNY.get ( Chengshi ).get ( Feinong ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = ziNv
						.get ( Chengshi ).get ( Feinong ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfNY.get ( Feinong ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = PopulationPredictOfCXNY
						.get ( Nongchun ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ PopulationPredictOfCXNY.get ( Chengshi ).get (
								Feinong ).get ( Year.getYear ( year ) ).get (
								Male ) [ X ];
				PopulationPredictOfNY.get ( Feinong ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = PopulationPredictOfCXNY
						.get ( Nongchun ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ]
						+ PopulationPredictOfCXNY.get ( Chengshi ).get (
								Feinong ).get ( Year.getYear ( year ) ).get (
								Female ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfCXNY.get ( Nongchun ).get ( Nongye ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = ziNv.get (
						Nongchun ).get ( Nongye ).get ( HJM ) [ X ];
				PopulationPredictOfCXNY.get ( Nongchun ).get ( Nongye ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = ziNv
						.get ( Nongchun ).get ( Nongye ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfCXNY.get ( Chengshi ).get ( Nongye ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = ziNv.get (
						Chengshi ).get ( Nongye ).get ( HJM ) [ X ];
				PopulationPredictOfCXNY.get ( Chengshi ).get ( Nongye ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = ziNv
						.get ( Chengshi ).get ( Nongye ).get ( HJF ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfNY.get ( Nongye )
						.get ( Year.getYear ( year ) ).get ( Male ) [ X ] = PopulationPredictOfCXNY
						.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ PopulationPredictOfCXNY.get ( Chengshi )
								.get ( Nongye ).get ( Year.getYear ( year ) )
								.get ( Male ) [ X ];
				PopulationPredictOfNY.get ( Nongye )
						.get ( Year.getYear ( year ) ).get ( Female ) [ X ] = PopulationPredictOfCXNY
						.get ( Nongchun ).get ( Nongye ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ]
						+ PopulationPredictOfCXNY.get ( Chengshi )
								.get ( Nongye ).get ( Year.getYear ( year ) )
								.get ( Female ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfCX.get ( Nongchun ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = PopulationPredictOfCXNY
						.get ( Nongchun ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ PopulationPredictOfCXNY.get ( Nongchun )
								.get ( Nongye ).get ( Year.getYear ( year ) )
								.get ( Male ) [ X ];
				PopulationPredictOfCX.get ( Nongchun ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = PopulationPredictOfCXNY
						.get ( Nongchun ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ]
						+ PopulationPredictOfCXNY.get ( Nongchun )
								.get ( Nongye ).get ( Year.getYear ( year ) )
								.get ( Female ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfCX.get ( Chengshi ).get (
						Year.getYear ( year ) ).get ( Male ) [ X ] = PopulationPredictOfCXNY
						.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ]
						+ PopulationPredictOfCXNY.get ( Chengshi )
								.get ( Nongye ).get ( Year.getYear ( year ) )
								.get ( Male ) [ X ];
				PopulationPredictOfCX.get ( Chengshi ).get (
						Year.getYear ( year ) ).get ( Female ) [ X ] = PopulationPredictOfCXNY
						.get ( Chengshi ).get ( Feinong ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ]
						+ PopulationPredictOfCXNY.get ( Chengshi )
								.get ( Nongye ).get ( Year.getYear ( year ) )
								.get ( Female ) [ X ];
			}
			if ( true )
			{
				PopulationPredictOfAll.get ( Year.getYear ( year ) )
						.get ( Male ) [ X ] = PopulationPredictOfCX.get (
						Chengshi ).get ( Year.getYear ( year ) ).get ( Male ) [ X ]
						+ PopulationPredictOfCX.get ( Nongchun ).get (
								Year.getYear ( year ) ).get ( Male ) [ X ];
				PopulationPredictOfAll.get ( Year.getYear ( year ) ).get (
						Female ) [ X ] = PopulationPredictOfCX.get ( Chengshi )
						.get ( Year.getYear ( year ) ).get ( Female ) [ X ]
						+ PopulationPredictOfCX.get ( Nongchun ).get (
								Year.getYear ( year ) ).get ( Female ) [ X ];
			}
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

