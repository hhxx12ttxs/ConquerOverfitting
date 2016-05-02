package test;

import static prclqz.core.Const.MAX_AGE;
import static prclqz.lib.EnumMapTool.createCXXBDoubleArrMap;
import static prclqz.lib.EnumMapTool.createSYSFMSMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import prclqz.DAO.IDAO;
import prclqz.DAO.MyDAOImpl;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.MainTaskBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.DuiJiGuSuanFa;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;
import prclqz.lib.EnumMapTool;
import prclqz.methods.MAgingCalculation;
import prclqz.parambeans.ParamBean3;
import prclqz.view.DefaultView;
import prclqz.view.IView;
import test.test;

public class AgingTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		HashMap<String, Object> globals = new HashMap<String, Object>();
		IDAO m = new MyDAOImpl();
		IView v = new DefaultView();
		globals.put("view", v);

		MainTaskBean task;
		task = m.getTask(args[0]);
		v.setTaskName(task.getName());

		HashMap<String, BornXbbBean> xbbMap = m.getARBornXbbBeans(task);
		globals.put("bornXbbBeanMap", xbbMap);
		// ////////DS/////////////
		TempVariablesBean tempVar = new TempVariablesBean(0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0);
		tempVar.CNTFR = EnumMapTool.createSingleChild();
		tempVar.nyPolicyTFR0 = EnumMapTool.createNYSingleChild();
		tempVar.nyImplTFR0 = EnumMapTool.createNYSingleChild();
		tempVar.nyShiHunTFR0 = EnumMapTool.createNYSingleChild();
		tempVar.cxPolicyTFR0 = EnumMapTool.createCXSingleChild();
		tempVar.cxImplTFR0 = EnumMapTool.createCXSingleChild();
		tempVar.cxShiHunTFR0 = EnumMapTool.createCXSingleChild();
		tempVar.teFuFaCN = EnumMapTool.createSingleChild();
		tempVar.teFuMaCN = EnumMapTool.createSingleChild();
		tempVar.CNHunneiTFR = EnumMapTool.createSingleChild();
		tempVar.nyHunneiTFR = EnumMapTool.createNYSingleChild();
		tempVar.cxHunneiTFR = EnumMapTool.createCXSingleChild();
		tempVar.singleSonFaCN = EnumMapTool.createSingleChild();
		tempVar.singleSonMaCN = EnumMapTool.createSingleChild();

		tempVar.QXM = new double[MAX_AGE];
		tempVar.QXF = new double[MAX_AGE];

		tempVar.TJ = DuiJiGuSuanFa.getFromId(3);
		// /////////////////////////end//////////////////
		globals.put("tempVarBean", tempVar);

		HashMap<String, Object> predictVarMap = new HashMap<String, Object>();
		globals.put("predictVarMap", predictVarMap);

		// //////////setup///////////////////
		tempVar.province = 11;
		tempVar.year = 2010;

		ParamBean3 pb3 = new ParamBean3();
		pb3.setDq1(11);
//		pb3.setDq2(65);
		pb3.setDq2(17);
		int dqx;
		// ???????
		Map<CX, Map<SYSFMSField, double[]>> shengYuMoShi;
		// ??????
		Map<CX, Map<XB, double[]>> deathRate;
		System.out.println("??:");
		for (dqx = pb3.getDq1(); dqx <= pb3.getDq2(); dqx++) {
			System.out.println("??"+dqx+"?");
			test.FillTempVarFromFile(tempVar, "D:\\prclqz\\????\\??\\variable_" + tempVar.getYear() + "_" + dqx + ".txt");
			System.out.println(dqx+"?FillTempVarFromFile??");
			if (!xbbMap.containsKey("" + dqx))
				continue;
			// ???????
			shengYuMoShi = createSYSFMSMap(MAX_AGE);
			m.setShengYuMoShi(shengYuMoShi, dqx, task.getParam_id(),
					tempVar.getTJ());
			predictVarMap.put("ShengYuMoShi" + dqx, shengYuMoShi);

			// ????
			deathRate = createCXXBDoubleArrMap(MAX_AGE);
			String fieldName = tempVar.getYear() < 2101 ? "Q"
					+ tempVar.getYear() : "Q2100";
			m.setDeathRate(deathRate, dqx, task.getParam_id(), fieldName);
//			predictVarMap.put("DeathRate" + dqx, m);
			predictVarMap.put("DeathRate" + dqx, deathRate);

			// ????? By DB
			System.out.println("??????????");
			Map<CX, Map<NY, Map<HunpeiField, double[]>>> ziNv = EnumTools
					.creatCXNYZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
			Map<CX, Map<HunpeiField, double[]>> ziNvOfCX = EnumTools
					.creatCXZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
			Map<NY, Map<HunpeiField, double[]>> ziNvOfNY = EnumTools
					.creatNYFNZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
			Map<HunpeiField, double[]> ziNvOfAll = EnumTools
					.creatAllZiNvFromFile(MAX_AGE, tempVar.getYear(), dqx);
			predictVarMap.put("HunpeiOfCXNY" + dqx, ziNv);
			predictVarMap.put("HunpeiOfCX" + dqx, ziNvOfCX);
			predictVarMap.put("HunpeiOfNY" + dqx, ziNvOfNY);
			predictVarMap.put("HunpeiOfAll" + dqx, ziNvOfAll);

			Map<CX, Map<Year, Map<XB, double[]>>> PopulationMigrationOfCX = EnumTools
					.createCXYearXBDoubleArrMapFromFile(MAX_AGE,
							tempVar.getYear(), dqx);
			Map<Year, Map<XB, double[]>> PopulationMigrationOfAll = EnumTools
					.createYearXBDoubleArrMapFromFile(MAX_AGE,
							tempVar.getYear(), dqx);
			predictVarMap.put("PopulationMigrationOfCX" + dqx,
					PopulationMigrationOfCX);
			predictVarMap.put("PopulationMigrationOfAll" + dqx,
					PopulationMigrationOfAll);
			// ???????? ????
			Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> SonDiePopulationPredictOfYiHaiCXNY = EnumTools
					.createCXNYYearXBDoubleArrMapFromFile2(MAX_AGE,
							tempVar.getYear(), dqx);
//			Map<CX, Map<Year, Map<XB, double[]>>> SonDiePopulationPredictOfYiHaiCX = EnumTools
//					.createCXYearXBDoubleArrMapFromFile2(MAX_AGE,
//							tempVar.getYear(), dqx);
//			Map<NY, Map<Year, Map<XB, double[]>>> SonDiePopulationPredictOfYiHaiNY = EnumTools
//					.createNYYearXBDoubleArrMapFromFile2(MAX_AGE,
//							tempVar.getYear(), dqx);
//			Map<Year, Map<XB, double[]>> SonDiePopulationPredictOfYiHaiAll = EnumTools
//					.createYearXBDoubleArrMapFromFile2(MAX_AGE,
//							tempVar.getYear(), dqx);
			Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> SonDiePopulationPredictOfTeFuCXNY = EnumTools
					.createCXNYYearXBDoubleArrMapFromFile3(MAX_AGE,
							tempVar.getYear(), dqx);
//			Map<CX, Map<Year, Map<XB, double[]>>> SonDiePopulationPredictOfTeFuCX = EnumTools
//					.createCXYearXBDoubleArrMapFromFile3(MAX_AGE,
//							tempVar.getYear(), dqx);
//			Map<NY, Map<Year, Map<XB, double[]>>> SonDiePopulationPredictOfTeFuNY = EnumTools
//					.createNYYearXBDoubleArrMapFromFile3(MAX_AGE,
//							tempVar.getYear(), dqx);
//			Map<Year, Map<XB, double[]>> SonDiePopulationPredictOfTeFuAll = EnumTools
//					.createYearXBDoubleArrMapFromFile3(MAX_AGE,
//							tempVar.getYear(), dqx);
			predictVarMap.put("SonDiePopulationPredictOfTeFuCXNY" + dqx,
					SonDiePopulationPredictOfTeFuCXNY);
//			predictVarMap.put("SonDiePopulationPredictOfTeFuCX" + dqx,
//					SonDiePopulationPredictOfTeFuCX);
//			predictVarMap.put("SonDiePopulationPredictOfTeFuNY" + dqx,
//					SonDiePopulationPredictOfTeFuNY);
//			predictVarMap.put("SonDiePopulationPredictOfTeFuAll" + dqx,
//					SonDiePopulationPredictOfTeFuAll);

			predictVarMap.put("SonDiePopulationPredictOfYiHaiCXNY" + dqx,
					SonDiePopulationPredictOfYiHaiCXNY);
//			predictVarMap.put("SonDiePopulationPredictOfYiHaiCX" + dqx,
//					SonDiePopulationPredictOfYiHaiCX);
//			predictVarMap.put("SonDiePopulationPredictOfYiHaiNY" + dqx,
//					SonDiePopulationPredictOfYiHaiNY);
//			predictVarMap.put("SonDiePopulationPredictOfYiHaiAll" + dqx,
//					SonDiePopulationPredictOfYiHaiAll);
			
		}
		
		//????,????
		MAgingCalculation test_inst = new MAgingCalculation();
		//??dqx
		int _dqx = 11;
		m.setTempVariable(tempVar, "Province", _dqx, task.getId());
		//????
		test_inst.calculate(m, globals);
		//????
		HashMap<String, Object> res_predictVarMap = (HashMap<String, Object>)globals.get("predictVarMap");
		Map<HunpeiField, double[]> res_ziNvOfAll = (Map<HunpeiField, double[]>)res_predictVarMap.get("HunpeiOfAll" + _dqx);
		
		File f = new File("D:\\prclqz\\????\\??\\out.txt");
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(f);
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		BufferedWriter out_br = new BufferedWriter (new OutputStreamWriter(out, "GBK"));
		for(double[] tmp_v : res_ziNvOfAll.values()){
			for(double x : tmp_v)
				out_br.write(x+",");
			out_br.write("\n");
		}
		System.out.println("end");
	}

}

