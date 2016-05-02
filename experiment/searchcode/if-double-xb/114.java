package test;

import java.io.BufferedWriter;
import static prclqz.core.enumLib.SYSFMSField.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;
import prclqz.parambeans.ParamBean3;

public class Dumper {
	public Dumper(HashMap<String, Object> globals, int dqx) {
		this.globals = globals;
		this.dqx = dqx;
	}

	public void dump() {
		HashMap<String, Object> predictVarMap = (HashMap<String, Object>) globals.get("predictVarMap");
		Map<CX, Map<NY, Map<HunpeiField, double[]>>> cx_ny = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) (predictVarMap).
				get("HunpeiOfCXNY" + dqx);
		Map<NY, Map<HunpeiField, double[]>> ny = (Map<NY, Map<HunpeiField, double[]>>) (predictVarMap).get("HunpeiOfNY" + dqx);
		Map<CX, Map<HunpeiField, double[]>> cx = (Map<CX, Map<HunpeiField, double[]>>) (predictVarMap).get("HunpeiOfCX" + dqx);
		Map<HunpeiField, double[]> all = (Map<HunpeiField, double[]>) (predictVarMap).get("HunpeiOfAll" + dqx);
		TempVariablesBean tempVar = (TempVariablesBean) globals.get("tempVarBean");
		int year = tempVar.getYear();

		// ?61?69????
		// 61 nc_ny
		Map<HunpeiField, double[]> data_hunpei;
		data_hunpei = cx_ny.get(CX.Nongchun).get(NY.Nongye);
		dumpHunpei(data_hunpei, "" + 61 + "_" + dqx + ".txt");
		data_hunpei = cx_ny.get(CX.Nongchun).get(NY.Feinong);
		dumpHunpei(data_hunpei, "" + 62 + "_" + dqx + ".txt");
		data_hunpei = cx_ny.get(CX.Chengshi).get(NY.Nongye);
		dumpHunpei(data_hunpei, "" + 63 + "_" + dqx + ".txt");
		data_hunpei = cx_ny.get(CX.Chengshi).get(NY.Feinong);
		dumpHunpei(data_hunpei, "" + 64 + "_" + dqx + ".txt");
		data_hunpei = cx.get(CX.Nongchun);
		dumpHunpei(data_hunpei, "" + 65 + "_" + dqx + ".txt");
		data_hunpei = cx.get(CX.Chengshi);
		dumpHunpei(data_hunpei, "" + 66 + "_" + dqx + ".txt");
		data_hunpei = ny.get(NY.Nongye);
		dumpHunpei(data_hunpei, "" + 67 + "_" + dqx + ".txt");
		data_hunpei = ny.get(NY.Feinong);
		dumpHunpei(data_hunpei, "" + 68 + "_" + dqx + ".txt");
		data_hunpei = all;
		dumpHunpei(data_hunpei, "" + 69 + "_" + dqx + ".txt");
		
		Map<CX,Map<XB,double[]>> death_rate;
		Map<XB,double[]> data_death_rate;
		death_rate = (Map<CX,Map<XB,double[]>>)(predictVarMap).get("DeathRate" + dqx);
		data_death_rate = death_rate.get(CX.Chengshi);
		dumpDeathRate(data_death_rate, "" + 80 + "_" + 81 + "_" + dqx + ".txt");
		data_death_rate = death_rate.get(CX.Nongchun);
		dumpDeathRate(data_death_rate, "" + 82 + "_" + 83 + "_" + dqx + ".txt");
		
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> NationziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY" );
		Map<CX,Map<HunpeiField,double[]>> NationziNvOfCX = (Map<CX, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfCX" );
		Map<NY,Map<HunpeiField,double[]>> NationziNvOfNY = (Map<NY, Map<HunpeiField, double[]>>) predictVarMap.get( "HunpeiOfNY" );
		Map<HunpeiField,double[]> NationziNvOfAll = (Map< HunpeiField , double[] >) predictVarMap.get( "HunpeiOfAll" );
		
		dumpHunpei(NationziNv.get(CX.Nongchun).get(NY.Nongye), ""+161+"_"+dqx+".txt");
		dumpHunpei(NationziNv.get(CX.Nongchun).get(NY.Feinong), ""+162+"_"+dqx+".txt");
		dumpHunpei(NationziNv.get(CX.Chengshi).get(NY.Nongye), ""+163+"_"+dqx+".txt");
		dumpHunpei(NationziNv.get(CX.Chengshi).get(NY.Feinong), ""+164+"_"+dqx+".txt");
		dumpHunpei(NationziNvOfCX.get(CX.Nongchun), ""+165+"_"+dqx+".txt");
		dumpHunpei(NationziNvOfCX.get(CX.Chengshi), ""+166+"_"+dqx+".txt");
		dumpHunpei(NationziNvOfNY.get(NY.Nongye), ""+167+"_"+dqx+".txt");
		dumpHunpei(NationziNvOfNY.get(NY.Feinong), ""+168+"_"+dqx+".txt");
		dumpHunpei(NationziNvOfAll, ""+169+"_"+dqx+".txt");
		
		Map<CX,Map<NY,Map<Year,Map<Couple,Double>>>> NationCoupleAndChildrenOfCXNY = ( Map < CX , Map < NY , Map < Year , Map < Couple , Double >>> > ) predictVarMap.get ( "CoupleAndChildrenOfCXNY" );
		Map<CX,Map<Year,Map<Couple,Double>>> NationCoupleAndChildrenOfCX = ( Map < CX , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfCX" );
		Map<NY,Map<Year,Map<Couple,Double>>> NationCoupleAndChildrenOfNY = ( Map < NY , Map < Year , Map < Couple , Double >>> ) predictVarMap.get ( "CoupleAndChildrenOfNY" );
		Map<Year,Map<Couple,Double>> NationCoupleAndChildrenOfAll = ( Map < Year , Map < Couple , Double >> ) predictVarMap.get ( "CoupleAndChildrenOfAll" );
		
		dumpCouple(NationCoupleAndChildrenOfCXNY.get(CX.Nongchun).get(NY.Nongye), ""+151+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfCXNY.get(CX.Nongchun).get(NY.Feinong), ""+152+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfCXNY.get(CX.Chengshi).get(NY.Nongye), ""+153+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfCXNY.get(CX.Chengshi).get(NY.Feinong), ""+154+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfCX.get(CX.Nongchun), ""+155+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfCX.get(CX.Chengshi), ""+156+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfNY.get(NY.Nongye), ""+157+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfNY.get(NY.Feinong), ""+158+"_"+dqx+".txt");
		dumpCouple(NationCoupleAndChildrenOfAll, ""+159+"_"+dqx+".txt");
		
		HashMap<String,BornXbbBean> xbbMap = (HashMap<String, BornXbbBean>) globals.get("bornXbbBeanMap");
		BornXbbBean xbb = xbbMap.get(""+dqx);
		String diDai = xbb.getDiDai();
		
		if(diDai.equals ( "??" ))
			diDai = "??";
		Map<String,Map<SYSFMSField,double[]>> birthWill = (Map<String, Map<SYSFMSField, double[]>>) predictVarMap.get("BirthWill");
		dumpBirthWill(birthWill, ""+90+"_"+dqx+".txt", yiyuan);
	}
	
	private void dumpBirthWill(Map<String, Map<SYSFMSField, double[]>> data, String fileName, SYSFMSField sysfms){
		File file = new File(PATH + fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			file.delete();
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		BufferedWriter bfos = null;
		String[] diDais = {"??", "??", "??", "??", "??", "??", "??", "??"};
		try{
			bfos = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file)));
			for(int j = 0; j != data.get("??").get(yiyuan).length; ++j){
				bfos.write("" + j + ",");
				for (String didai: diDais){
					bfos.write(data.get(didai).get(sysfms)[j] + ",");
				}
				bfos.write("\r\n");
			}
			bfos.close();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void dumpCouple(Map<Year, Map<Couple, Double>> data, String fileName){
		File file = new File(PATH + fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			file.delete();
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		BufferedWriter bfos = null;
		try{
			bfos = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file)));
			ParamBean3 envparam = (ParamBean3) globals.get("SetUpEnvParamBean");
			for (int year = 2005; year <= 2100; ++year) {
				bfos.write("" + year);
				for (Couple cp : data.get(Year.getYear(year)).keySet()) {
					bfos.write(data.get(Year.getYear(year)).get(cp) + ",");
				}
				bfos.write("\r\n");
			}
			bfos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void dumpDeathRate(Map<XB,double[]> data, String fileName){
		File file = new File(PATH + fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			file.delete();
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		BufferedWriter bfos = null;
		try{
			bfos = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file)));
			int l = data.get(XB.Female).length;
			for (int ii = 0; ii != l; ++ii) {
				bfos.write("" + ii);
				for (XB xb : data.keySet()) {
					bfos.write(data.get(xb)[ii] + ",");
				}
				bfos.write("\r\n");
			}
			bfos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void dumpHunpei(Map<HunpeiField, double[]> data, String fileName) {
		File file = new File(PATH + fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			file.delete();
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		BufferedWriter bfos = null;
		try {
			bfos = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int i = 0;

		int l = data.get(HunpeiField.DD_NOT_F_F).length;
		try {
			for (int ii = 0; ii != l; ++ii) {
				bfos.write("" + ii);
				for (HunpeiField f : data.keySet()) {
					bfos.write("," + data.get(f)[ii]);
				}
				bfos.write("\r\n");
			}
			bfos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final static String PATH = "D:\\DB\\java_dump\\";
	private HashMap<String, Object> globals;
	private int dqx;
}

