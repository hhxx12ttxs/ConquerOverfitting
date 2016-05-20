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
 * ??????
 * @author Jack Long
 * @email  prclqz@zju.edu.cn
 *
 */
public class MDeathCalculating implements IDeathCalculatingMethod
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
		String dqB = xbb.getDqName ( );
		//define local variables
		int X=0;
		Map<NY,Map<HunpeiField,double[]>> ziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY"+dqx );//provincMigMap.get("NY"+dqx);
		Map<CX,Map<Year,Map<XB,double[]>>>PopulationPredictOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfCX"+dqx );
		Map<NY,Map<Year,Map<XB,double[]>>> PopulationPredictOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationPredictOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationPredictOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationPredictOfAll"+dqx );

		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationMigrationOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfCX"+dqx );
		//Map<NY,Map<Year,Map<XB,double[]>>> PopulationMigrationOfNY = ( Map < NY , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationMigrationOfNY"+dqx );
		Map<Year,Map<XB,double[]>> PopulationMigrationOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationMigrationOfAll"+dqx );
		
		//??????
		Map<CX,Map<Year,Map<XB,double[]>>> PopulationDeathOfCX = ( Map < CX , Map < Year , Map < XB , double [ ] >>> ) predictVarMap.get ( "PopulationDeathOfCX"+dqx );
		Map<Year,Map<XB,double[]>> PopulationDeathOfAll = ( Map < Year , Map < XB , double [ ] >> ) predictVarMap.get ( "PopulationDeathOfAll"+dqx );
		
		
		//??????
		//Map<CX,Map<XB,double[]>> deathRate = ( Map < CX , Map < XB , double [ ] >> ) predictVarMap.get ( "DeathRate"+dqx );
		
//		//????????
//		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> CoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY"+dqx );
//		Map<CX,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX"+dqx );
//		Map<NY,Map<Year,Map<Couple,Double>>> CoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY"+dqx );
//		Map<Year,Map<Couple,Double>> CoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll"+dqx );
		
		//??????--??
		//Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfYiHaiCXNY"+dqx );
		//Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> yiHaiCN = SonDiePopulationPredictOfYiHaiCXNY;
//		yiHaiCN.get ( Chengshi ).get ( Nongye ).get ( Year.getYear ( year ) ).get ( Male )[X] = 0;
		
		//??????--??
		Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> SonDiePopulationPredictOfTeFuCXNY = ( Map < CX , Map < NY , Map < Year , Map < XB , double [ ] >>> > ) predictVarMap.get ( "SonDiePopulationPredictOfTeFuCXNY"+dqx );
		//Map<CX,Map<NY,Map<Year,Map<XB,double[]>>>> tefuCN = SonDiePopulationPredictOfTeFuCXNY;
		
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
		Map < Year , Map < XB , double [ ] >> pplPredict,pplMig,pplDeath;
		Map<HunpeiField,double[]> zn;
		switch(tempVar.cxI)
		{
		case 1:
			pplPredict = PopulationPredictOfAll;
			pplMig = PopulationMigrationOfAll;
			zn = null;
			pplDeath = PopulationDeathOfAll;
			break;
		case 2:
			pplPredict = PopulationPredictOfCX.get ( CX.Chengshi );
			pplMig = PopulationMigrationOfCX.get ( CX.Chengshi );
			zn = null;
			pplDeath = PopulationDeathOfCX.get ( CX.Chengshi );
			break;
		case 3:
			pplPredict = PopulationPredictOfNY.get ( Feinong );
//			pplMig = PopulationMigrationOfNY.get ( Feinong );
			pplMig = null;
			zn = ziNvOfNY.get ( Feinong );
//			pplDeath = PopulationDeathOfNY.get ( Feinong );
			pplDeath = null;
			break;
		case 4:
			pplPredict = PopulationPredictOfCX.get ( CX.Nongchun );
			pplMig = PopulationMigrationOfCX.get ( CX.Nongchun );
			zn = null;
			pplDeath = PopulationDeathOfCX.get ( CX.Nongchun );
			break;
		case 5:
			pplPredict = PopulationPredictOfNY.get ( Nongye );
//			pplMig = PopulationMigrationOfNY.get ( Nongye );
			pplMig = null;
			zn = ziNvOfNY.get ( Nongye );
//			pplDeath = PopulationDeathOfNY.get ( Nongye );
			pplDeath = null;
			break;
		default: 
			pplPredict = pplMig = pplDeath = null;
			zn = null;
		}
		
		//////////////translated by Foxpro2Java Translator successfully:///////////////
		tempVar.MINLM = 9999;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( pplPredict.get( Year.getYear( year - 1 ) ).get( XB.Male )[ X ] == 0.0
					&& X > 0.0 && X < tempVar.MINLM )
				tempVar.MINLM = X;
		}
		tempVar.MINLF = 9999;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if ( pplPredict.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ] == 0.0
					&& X > 0.0 && X < tempVar.MINLF )
				tempVar.MINLF = X;
		}

		/**
		 *  COUNT TO CM0 FOR &????..&MND1=0
		 *	COUNT TO CF0 FOR &????..&FND1=0
		 *	MINLM=IIF(CM0>0,MINLM,110)
		 *	MINLF=IIF(CF0>0,MINLF,110)
		 */
		
		int CM0 = 0 ,CF0 = 0 ;
		for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
		{
			if( pplPredict.get( Year.getYear( year - 1 ) ).get( XB.Male )[ X ] == 0)
				CM0++;
			if( pplPredict.get( Year.getYear( year - 1 ) ).get( XB.Female )[ X ] == 0)
				CF0++;
		}
		tempVar.MINLM = CM0>0? tempVar.MINLM:110;
		tempVar.MINLF = CF0>0? tempVar.MINLF:110;
		
		if ( tempVar.cxI == 1.0 )
		{
			tempVar.dqDeathM = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( true )
				{
					tempVar.dqDeathM += pplDeath.get( Year.getYear( year ) )
							.get( XB.Male )[ X ] / 10000.0;
				}
			}
			tempVar.dqDeathF = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( true )
				{
					tempVar.dqDeathF += pplDeath.get( Year.getYear( year ) )
							.get( XB.Female )[ X ] / 10000.0;
				}
			}
		} else if ( tempVar.cxI == 2.0 )
		{
			tempVar.cDeathM = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( true )
				{
					tempVar.cDeathM += pplDeath.get( Year.getYear( year ) )
							.get( XB.Male )[ X ] / 10000.0;
				}
			}
			tempVar.cDeathF = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( true )
				{
					tempVar.cDeathF += pplDeath.get( Year.getYear( year ) )
							.get( XB.Female )[ X ] / 10000.0;
				}
			}
		} else if ( tempVar.cxI == 4.0 )
		{
			tempVar.xDeathM = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( true )
				{
					tempVar.xDeathM += pplDeath.get( Year.getYear( year ) )
							.get( XB.Male )[ X ] / 10000.0;
				}
			}
			tempVar.xDeathF = 0;
			for ( X = MAX_AGE - 1 ; X >= 0 ; X-- )
			{
				if ( true )
				{
					tempVar.xDeathF += pplDeath.get( Year.getYear( year ) )
							.get( XB.Female )[ X ] / 10000.0;
				}
			}
		}
		
		/**
		 * DIMENSION QXM(MINLM),QXF(MINLF)
		 * ????
		 */
		
		//GO TOP
		X = 0;
		int RE;
		for ( RE = 1 ; RE < tempVar.MINLM ; RE++ )
		{
			tempVar.QXM[ RE ] = pplDeath.get( Year.getYear( year ) ).get(XB.Male )[ X ]
					/ pplPredict.get( Year.getYear( year - 1 ) ).get( XB.Male )[ X ];
			X++;
		}
		
		// GO TOP
		X = 0;
		for ( RE = 1 ; RE <= tempVar.MINLF ; RE++ )
		{
			tempVar.QXF[ RE ] = pplDeath.get( Year.getYear( year ) ).get(
					XB.Female )[ X ]
					/ pplPredict.get( Year.getYear( year - 1 ) )
							.get( XB.Female )[ X ];
			X++;
		}
		/////////////end of translating, by Foxpro2Java Translator///////////////

		tempVar.MINLM = tempVar.MINLM;
		tempVar.MINLF = tempVar.MINLF;
		
		/**********test??************/
//		System.out.println(tempVar.cxI+"BEGIN:");
//		System.out.println("MINLM: "+tempVar.MINLM);
//		System.out.println("MINLF: "+tempVar.MINLF);
//		System.out.println("dqDeathM: "+tempVar.dqDeathM);
//		System.out.println("dqDeathF: "+tempVar.dqDeathF);
//		System.out.println("cDeathM: "+tempVar.cDeathM);
//		System.out.println("cDeathF: "+tempVar.cDeathF);
//		System.out.println("xDeathM: "+tempVar.xDeathM);
//		System.out.println("xDeathF: "+tempVar.xDeathF);
//		System.out.println("QXM:");
//		for(int i=0;i<tempVar.MINLM;i++){
//			System.out.print(i+": ");
//			System.out.println(tempVar.QXM[i]);
//		}
//		System.out.println("QXF:");
//		for(int i=0;i<tempVar.MINLF;i++){
//			System.out.print(i+": ");
//			System.out.println(tempVar.QXF[i]);
//		}
//		System.out.println(tempVar.cxI+"END");
		/***********test End**********/
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

