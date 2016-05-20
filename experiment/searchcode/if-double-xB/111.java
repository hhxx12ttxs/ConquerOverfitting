package ds.importData;

import java.util.Map;

import ds.function.EnumMapTool;

import prclqz.core.Const;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.CXAll;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.QYPredict;
import prclqz.core.enumLib.XBAll;

public class MNetMigrationRateAndSum {

	/**
	 * ??????????????/??????????????????????
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	/**
	 * ???????????
	 * @param globals
	 */
	public void calculate(Map<String,Object> globals){
		int dqx = 11;
		//????: QY43????????????????????_????????
		//RT = Retention Rate = ???
		Map<XBAll,Map<CXAll,Double>>cxaRTResult = (Map<XBAll, Map<CXAll, Double>>)globals.get("cxRTResult"+dqx);
		Map<XBAll,Map<NY, Double>> nyRTResult = (Map<XBAll,Map<NY, Double>>)globals.get("nyRTResult"+dqx);
		Map<XBAll,Map<CX,Map<NY,Double>>> cxnyRTResult = (Map<XBAll,Map<CX,Map<NY,Double>>>)globals.get("cxnyRTResult"+dqx);
		//???????"?x"
		Map<XBAll,Map<CXAll,Double>>cxaRTResultTX = (Map<XBAll, Map<CXAll, Double>>)globals.get("cxRTResultTX"+dqx);
		Map<XBAll,Map<NY, Double>> nyRTResultTX = (Map<XBAll,Map<NY, Double>>)globals.get("nyRTResultTX"+dqx);
		Map<XBAll,Map<CX,Map<NY,Double>>> cxnyRTResultTX = (Map<XBAll,Map<CX,Map<NY,Double>>>)globals.get("cxnyRTResultTX"+dqx);
		//czrk[3] ???? czrk09, czrk10, czrkzl
		double[] czrk = (double[])globals.get("czrk"+dqx);
		/*****************************************/
		//??:
		//????\???\xx?\xx?????????????????_??5?????2010?.DBF ----- foxpro??: ????
		Map<XBAll,Map<CXAll,double[]>>cxaPopulation = (Map<XBAll, Map<CXAll, double[]>>)globals.get("cxPopulation"+dqx);
		Map<XBAll,Map<NY, double[]>> nyPopulation = (Map<XBAll,Map<NY, double[]>>)globals.get("nyPopulation"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[]>>> cxnyPopulation = (Map<XBAll,Map<CX,Map<NY,double[]>>>)globals.get("cxnyPopulation"+dqx);
		double[] czrkb = (double[])globals.get("RTczrkb"+dqx); 
		
		//????\QY33xx?'??????????????????.DBF(?111?)----- foxpro??:?????_1
		Map<XBAll, Map<CXAll, double[]>> cxaNMPos = (Map<XBAll, Map<CXAll, double[]>>)globals.get("cxaNMPos"+dqx);
		Map<XBAll,Map<NY, double[]>> nyNMPos = (Map<XBAll,Map<NY, double[]>>)globals.get("nyNMPos"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[]>>> cxnyNMPos = (Map<XBAll,Map<CX,Map<NY,double[]>>>)globals.get("cxnyNMPos"+dqx);
			//?????"?_1","1_2"???, double[][]???7*111?????,????"?_1","1_2","2_3","3_4","4_5","5_6","6"
		Map<XBAll, Map<CXAll, double[][]>> cxaNMPosH = (Map<XBAll, Map<CXAll, double[][]>>)globals.get("cxaNMPosH"+dqx);
		Map<XBAll,Map<NY, double[][]>> nyNMPosH = (Map<XBAll,Map<NY, double[][]>>)globals.get("nyNMPosH"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[][]>>> cxnyNMPosH = (Map<XBAll,Map<CX,Map<NY,double[][]>>>)globals.get("cxnyNMPosH"+dqx);
		
		//????\????3\QY34xx???????????????????.DBF ----- foxpro??: ?????
		Map<XBAll, Map<CXAll, double[]>> cxaMigrationRT = (Map<XBAll, Map<CXAll, double[]>>)globals.get("cxaMigrationRT"+dqx);
		Map<XBAll,Map<NY, double[]>> nyMigrationRT = (Map<XBAll,Map<NY, double[]>>)globals.get("nyMigrationRT"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[]>>> cxnyMigrationRT = (Map<XBAll,Map<CX,Map<NY,double[]>>>)globals.get("cxnyMigrationRT"+dqx);
			//?????"?_1","1_2"???, double[][]???7*111?????,????"?_1","1_2","2_3","3_4","4_5","5_6","6"
		Map<XBAll, Map<CXAll, double[][]>> cxaMigrationRTH = (Map<XBAll, Map<CXAll, double[][]>>)globals.get("cxaMigrationRTH"+dqx);
		Map<XBAll,Map<NY, double[][]>> nyMigrationRTH = (Map<XBAll,Map<NY, double[][]>>)globals.get("nyMigrationRTH"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[][]>>> cxnyMigrationRTH = (Map<XBAll,Map<CX,Map<NY,double[][]>>>)globals.get("cxnyMigrationRTH"+dqx);
		
		//????\????3\QY34'-DQB-'??????????????????_????????.DBF ----- foxpro??: ?????????
		Map<XBAll, Map<CXAll, double[]>> cxaNMPosOnRT = (Map<XBAll, Map<CXAll, double[]>>)globals.get("cxaNMPosOnRT"+dqx);
		Map<XBAll,Map<NY, double[]>> nyNMPosOnRT = (Map<XBAll,Map<NY, double[]>>)globals.get("nyNMPosOnRT"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[]>>> cxnyNMPosOnRT = (Map<XBAll,Map<CX,Map<NY,double[]>>>)globals.get("cxnyNMPosOnRT"+dqx);
			//?????"??", 111?
		Map<XBAll, Map<CXAll, double[]>> cxaNMPosOnRTDis = (Map<XBAll, Map<CXAll, double[]>>)globals.get("cxaNMPosOnRTDis"+dqx);
		Map<XBAll,Map<NY, double[]>> nyNMPosOnRTDis = (Map<XBAll,Map<NY, double[]>>)globals.get("nyNMPosOnRTDis"+dqx);
		Map<XBAll,Map<CX,Map<NY,double[]>>> cxnyNMPosOnRTDis = (Map<XBAll,Map<CX,Map<NY,double[]>>>)globals.get("cxnyNMPosOnRTDis"+dqx);
		
		/*************************************************************************************************************************************/
		//????: ??????
		Map<XBAll, Map<CXAll, double[]>> cxaDNin = EnumMapTool.creatCXANetMigrationPopulation();
		Map<XBAll,Map<NY, double[]>> nyDNin = EnumMapTool.creatNYNetMigrationPopulation();
		Map<XBAll,Map<CX,Map<NY,double[]>>> cxnyDNin = EnumMapTool.creatCXNYNetMigrationPopulation();
			//?????"?_1","1_2"???, double[][]???7*111?????,????"?_1","1_2","2_3","3_4","4_5","5_6","6"
		Map<XBAll, Map<CXAll, double[][]>> cxaDNinH = EnumMapTool.creatCXANetMigrationPopulation2d();
		Map<XBAll,Map<NY, double[][]>> nyDNinH = EnumMapTool.creatNYNetMigrationPopulation2d();
		Map<XBAll,Map<CX,Map<NY,double[][]>>> cxnyDNinH = EnumMapTool.creatCXNYNetMigrationPopulation2d();
		
		/******?????******/
		for(XBAll xba:XBAll.values()){
			for(CX cx:CX.values()){
				for(NY ny:NY.values()){
					//REPLACE ????.&FIE0 WITH ????.&ZRK*?????_1.&FIE0 ALL
					for(int i=0;i<Const.MAX_AGE;i++){
						cxnyDNinH.get(xba).get(cx).get(ny)[0][i] = cxnyPopulation.get(xba).get(cx).get(ny)[i]*cxnyNMPosH.get(xba).get(cx).get(ny)[0][i];//0??"?_1" 
					}
					//REPLACE ????.&QYRK WITH ????.&FIE0 all
					for(int i=0;i<Const.MAX_AGE;i++){
						cxnyDNin.get(xba).get(cx).get(ny)[i] = cxnyDNinH.get(xba).get(cx).get(ny)[0][i];  
					}
					/*
					 * FOR T1=1 TO 6
					 * FIE1=CXB-XB-IIF(t1=1,'?_1',IIF(t1=2,'1_2',IIF(t1=3,'2_3',IIF(t1=4,'3_4',IIF(t1=5,'4_5',IIF(t1=6,'5_6','6'))))))
					 * FIE2=CXB-XB-IIF(t1=1,'1_2',IIF(t1=2,'2_3',IIF(t1=3,'3_4',IIF(t1=4,'4_5',IIF(t1=5,'5_6','6')))))
				     * COPY TO ARRAY AR1 FIELDS ????.&FIE1,???.&FIE1
				 	 * REPLACE ????.&FIE2 WITH ar1(RECNO()-1,1)*(ar1(RECNO()-1,2)-1) FOR RECNO()>1
					 * REPLACE ????.&QYRK WITH ????.&QYRK+????.&FIE2 all
					 * ENDFOR
					 */
					for(int i=0;i<6;i++){//??"?_1","1_2"...
						for(int j=1;j<Const.MAX_AGE;j++){
							cxnyDNinH.get(xba).get(cx).get(ny)[i+1][j] = cxnyDNinH.get(xba).get(cx).get(ny)[i][j-1]*cxnyMigrationRT.get(xba).get(cx).get(ny)[j-1];
						}
						for(int j=0;j<Const.MAX_AGE;j++){
							cxnyDNin.get(xba).get(cx).get(ny)[j] = cxnyDNin.get(xba).get(cx).get(ny)[j] + cxnyDNinH.get(xba).get(cx).get(ny)[i+1][j];
						}
					}
				}
			}
		}
		/*****************************/
		for(XBAll xba:XBAll.values()){
			for(CX cx:CX.values()){
				for(int i=0;i<Const.MAX_AGE;i++){
					cxaDNin.get(xba).get(cx.toCXAll())[i] = cxnyDNin.get(xba).get(cx).get(NY.Nongye)[i] + cxnyDNin.get(xba).get(cx).get(NY.Feinong)[i]; 
				}
			}
			for(NY ny:NY.values()){
				for(int i=0;i<Const.MAX_AGE;i++){
					nyDNin.get(xba).get(ny)[i] = cxnyDNin.get(xba).get(CX.Chengshi).get(ny)[i] + cxnyDNin.get(xba).get(CX.Nongchun).get(ny)[i];
				}
			}
			/*
			 * REPLACE ????.M WITH ????.??M+????.??M ALL 
			 * REPLACE ????.F WITH ????.??F+????.??F ALL 
			 * REPLACE ????.T WITH ????.??T+????.??T ALL 
			 */
			for(int i=0;i<Const.MAX_AGE;i++){
				cxaDNin.get(xba).get(CXAll.Chengxiang)[i] = cxaDNin.get(xba).get(CXAll.Chengzhen)[i] + cxaDNin.get(xba).get(CXAll.Nongcun)[i];
			}
		}
		/*****************************/
		for(XBAll xba:XBAll.values()){
			for(CX cx:CX.values()){
				for(NY ny:NY.values()){
					//REPLACE ??????.&CXB WITH ????.&CXB/????.&ZRK FOR ????.&ZRK<>0
					for(int i=0;i<Const.MAX_AGE;i++){
						if(cxnyPopulation.get(xba).get(cx).get(ny)[i] != 0)
							cxnyNMPosOnRT.get(xba).get(cx).get(ny)[i] = cxnyDNin.get(xba).get(cx).get(ny)[i]/cxnyPopulation.get(xba).get(cx).get(ny)[i];
					}
					//SUM ??????.&CXB TO SGL
					double SGL = 0;
					for(int i=0;i<Const.MAX_AGE;i++){
						SGL += cxnyNMPosOnRT.get(xba).get(cx).get(ny)[i];
					}
					//REPLACE ??????.&?? WITH ??????.&CXB/SGL all
					for(int i=0;i<Const.MAX_AGE;i++){
						cxnyNMPosOnRTDis.get(xba).get(cx).get(ny)[i] = cxnyNMPosOnRT.get(xba).get(cx).get(ny)[i]/SGL;  
					}
				}
			}
			
			for(CX cx:CX.values()){
				//REPLACE ??????.&CXB WITH ????.&CXB/????.&CXB FOR ????.&cxb<>0
				for(int i=0;i<Const.MAX_AGE;i++){
					if(cxaPopulation.get(xba).get(cx.toCXAll())[i] != 0){
						cxaNMPosOnRT.get(xba).get(cx.toCXAll())[i] = cxaDNin.get(xba).get(cx.toCXAll())[i]/cxaPopulation.get(xba).get(cx.toCXAll())[i];
					}
				}
				//SUM ??????.&CXB TO SGL
				double SGL = 0;
				for(int i=0;i<Const.MAX_AGE;i++){
					SGL += cxaNMPosOnRT.get(xba).get(cx.toCXAll())[i];
				}
				//REPLACE ??????.&?? WITH ??????.&CXB/SGL all
				for(int i=0;i<Const.MAX_AGE;i++){
					cxaNMPosOnRTDis.get(xba).get(cx.toCXAll())[i] = cxaNMPosOnRT.get(xba).get(cx.toCXAll())[i]/SGL;  
				}
			}
			for(NY ny:NY.values()){
				//REPLACE ??????.&NYB WITH ????.&NYB/????.&NYB FOR ????.&nyb<>0
				for(int i=0;i<Const.MAX_AGE;i++){
					if(nyPopulation.get(xba).get(ny)[i] != 0){
						nyNMPosOnRT.get(xba).get(ny)[i] = nyDNin.get(xba).get(ny)[i]/nyPopulation.get(xba).get(ny)[i];
					}
				}
				//SUM ??????.&NYB TO SGL
				double SGL = 0;
				for(int i=0;i<Const.MAX_AGE;i++){
					SGL += nyNMPosOnRT.get(xba).get(ny)[i];
				}
				//REPLACE ??????.&?? WITH ??????.&NYB/SGL all
				for(int i=0;i<Const.MAX_AGE;i++){
					nyNMPosOnRTDis.get(xba).get(ny)[i] = nyNMPosOnRT.get(xba).get(ny)[i]/SGL;  
				}
			}
			
			/*
			 * ??=XB-'??'
			 * REPLACE ??????.&XB WITH ????.&XB/????.&XB FOR ????.&xb<>0
			 * SUM ??????.&XB TO SGL
			 * REPLACE ??????.&?? WITH ??????.&XB/SGL all
			 */
			for(int i=0;i<Const.MAX_AGE;i++){
				if(cxaPopulation.get(xba).get(CXAll.Chengxiang)[i] != 0){
					cxaNMPosOnRT.get(xba).get(CXAll.Chengxiang)[i] = cxaDNin.get(xba).get(CXAll.Chengxiang)[i]/cxaPopulation.get(xba).get(CXAll.Chengxiang)[i];
				}
			}
			double SGL = 0;
			for(int i=0;i<Const.MAX_AGE;i++){
				SGL += cxaNMPosOnRT.get(xba).get(CXAll.Chengxiang)[i];
			}
			for(int i=0;i<Const.MAX_AGE;i++){
				cxaNMPosOnRTDis.get(xba).get(CXAll.Chengxiang)[i] = cxaNMPosOnRT.get(xba).get(CXAll.Chengxiang)[i]/SGL;  
			}
		}
		/*
		 * GATHER FROM ar1 FIELDS T,M,F,??T,??M,??F,??T,??M,??F,??T,??M,??F,??T,??M,??F,;
		 * ??T,??M,??F,??T,??M,??F,??T,??M,??F,??T,??M,??F
		 */
		for(XBAll xba:XBAll.values()){
			for(CXAll cxa:CXAll.values()){
				double sum = 0;
				for(int i=0;i<Const.MAX_AGE;i++)
					sum += cxaNMPosOnRT.get(xba).get(cxa)[i];
				cxaRTResult.get(xba).put(cxa, sum);
			}
			for(NY ny:NY.values()){
				double sum = 0;
				for(int i=0;i<Const.MAX_AGE;i++)
					sum += nyNMPosOnRT.get(xba).get(ny)[i];
				nyRTResult.get(xba).put(ny, sum);
			}
			for(CX cx:CX.values()){
				for(NY ny:NY.values()){
					double sum = 0;
					for(int i=0;i<Const.MAX_AGE;i++)
						sum += cxnyNMPosOnRT.get(xba).get(cx).get(ny)[i];
					cxnyRTResult.get(xba).get(cx).put(ny, sum);
				}
			}
		}
		/**************************************/
		//TODO REPLACE CZRKB09 WITH aaa09.czrkb,CZRKB10 WITH aaa10.czrkb,CZRKZL WITH CZRKB10-CZRKB09 all
		
		for(XBAll xba:XBAll.values()){
			for(CXAll cxa:CXAll.values()){
				if(czrk[2] != 0)
					cxaRTResultTX.get(xba).put(cxa, cxaRTResult.get(xba).get(cxa)/czrk[2]);
			}
			for(NY ny:NY.values()){
				if(czrk[2] != 0){
					nyRTResultTX.get(xba).put(ny, nyRTResult.get(xba).get(ny)/czrk[2]);
				}
			}
			for(CX cx:CX.values()){
				for(NY ny:NY.values()){
					if(czrk[2] !=0){
						cxnyRTResultTX.get(xba).get(cx).put(ny, cxnyRTResult.get(xba).get(cx).get(ny)/czrk[2]);
					}
				}
			}
		}
	} 
}

