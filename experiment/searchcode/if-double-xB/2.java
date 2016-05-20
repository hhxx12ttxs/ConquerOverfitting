package test;

import java.awt.print.Printable;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import prclqz.core.StringList;
import prclqz.core.enumLib.Abstract;
import prclqz.core.enumLib.Babies;
import prclqz.core.enumLib.BabiesBorn;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.Couple;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.HunpeiGL;
import prclqz.core.enumLib.NY;
import prclqz.core.enumLib.PolicyBirth;
import prclqz.core.enumLib.QYPHField;
import prclqz.core.enumLib.SYSFMSField;
import prclqz.core.enumLib.SumBM;
import prclqz.core.enumLib.Summary;
import prclqz.core.enumLib.XB;
import prclqz.core.enumLib.Year;

//import test.Parser;

public class EnumTools {
	public static void main(String[] args) {
		try {
			Map<Year, Map<PolicyBirth, Double>> test = createPolicyBirth(111,
					2010, 10);
			System.out.println("Haha");
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	// 10?9????
	public static Map<NY, Map<Babies, double[]>> createBirthPredictOfNY(
			int length, int year, int dqx) throws Exception {
		// DB???= =
		Map<NY, Map<Babies, double[]>> m = new EnumMap<NY, Map<Babies, double[]>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(ny);
			int dn;// ???
			if (ny.getChinese().equals("??")) {
				dn = 10;// ??????
			} else {
				dn = 9;// ??????
			}
			// TODO
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}

	public static Map<CX, Map<Babies, double[]>> createBirthPredictOfCX(
			int length, int year, int dqx) throws Exception {
		// DB???= =
		Map<CX, Map<Babies, double[]>> m = new EnumMap<CX, Map<Babies, double[]>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(cx);
			int dn;// ???
			if (cx.getChinese().equals("??")) {
				dn = 24;// ??????
			} else {
				dn = 14;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}

	public static Map<NY, Map<Babies, double[]>> createNationBirthPredictOfNY(
			int length, int year, int dqx) throws Exception {
		// DB???= =
		Map<NY, Map<Babies, double[]>> m = new EnumMap<NY, Map<Babies, double[]>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(ny);
			int dn;// ???
			if (ny.getChinese().equals("??")) {
				dn = 110;// ??????
			} else {
				dn = 109;// ??????
			}
			// TODO
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}

	public static Map<CX, Map<Babies, double[]>> createNationBirthPredictOfCX(
			int length, int year, int dqx) throws Exception {
		// DB???= =
		Map<CX, Map<Babies, double[]>> m = new EnumMap<CX, Map<Babies, double[]>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(cx);
			int dn;// ???
			if (cx.getChinese().equals("??")) {
				dn = 127;// ??????
			} else {
				dn = 117;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}
	
	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> creatPopulationPredictOfCXNY(
			int length, int year, int dqx) throws Exception {
		// ???map
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> mm = m.get(cx);
			for (NY ny : NY.values()) {
				// ???????????
				mm.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mmm = mm.get(ny);
				int dn;
				// getEditableNum:Chengshi??0,Nongchun??1
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 11;// ????
					else
						dn = 12;// ????
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 21;// ????
					else
						dn = 22;// ????
				}
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				for (int y = 2005; y <= 2100; y++) {
					mmm.put(Year.getYear(y),
							new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmmm = mmm.get(Year.getYear(y));
					for (XB xb : XB.values()) {
						String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
								+ Integer.toString(y);
						mmmm.put(xb, parse_res.get(chn_name));
					}
				}
			}
		}

		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> creatPopulationPredictOfNY(
			int length, int year, int dqx) throws Exception {

		Map<NY, Map<Year, Map<XB, double[]>>> mm = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			mm.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mmm = mm.get(ny);

			int dn;
			if (ny.getChinese().equals("??") == true)
				dn = 5;
			else
				dn = 3;
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int y = 2005; y <= 2100; y++) {
				mmm.put(Year.getYear(y), new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmmm = mmm.get(Year.getYear(y));
				for (XB xb : XB.values()) {
					String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
							+ Integer.toString(y);
					mmmm.put(xb, parse_res.get(chn_name));
				}
			}
		}
		return mm;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> creatPopulationPredictOfCX(
			int length, int year, int dqx) throws Exception {

		Map<CX, Map<Year, Map<XB, double[]>>> mm = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			mm.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mmm = mm.get(cx);

			int dn;
			if (cx.getChinese().equals("??"))
				dn = 2;
			else
				dn = 4;
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int y = 2005; y <= 2100; y++) {
				mmm.put(Year.getYear(y), new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmmm = mmm.get(Year.getYear(y));
				for (XB xb : XB.values()) {
					String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
							+ Integer.toString(y);
					mmmm.put(xb, parse_res.get(chn_name));
				}
			}
		}
		return mm;
	}

	public static Map<Year, Map<XB, double[]>> creatPopulationPredictOfAll(
			int length, int year, int dqx) throws Exception {
		Map<Year, Map<XB, double[]>> m = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);

		int dn = 1;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

		for (int y = 2005; y <= 2100; y++) {
			m.put(Year.getYear(y), new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mm = m.get(Year.getYear(y));
			for (XB xb : XB.values()) {
				String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
						+ Integer.toString(y);
				mm.put(xb, parse_res.get(chn_name));
			}
		}
		return m;
	}

	//

	public static Map<CX, Map<NY, Map<HunpeiField, double[]>>> creatCXNYNationZiNvFromFile(
			int length, int year, int dqx) throws Exception {
		// ???map
		Map<CX, Map<NY, Map<HunpeiField, double[]>>> m = new EnumMap<CX, Map<NY, Map<HunpeiField, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<HunpeiField, double[]>>(NY.class));
			Map<NY, Map<HunpeiField, double[]>> mm = m.get(cx);
			for (NY ny : NY.values()) {
				// ???????????
				mm.put(ny,
						new EnumMap<HunpeiField, double[]>(HunpeiField.class));
				Map<HunpeiField, double[]> mmm = mm.get(ny);

				int dn;
				// getEditableNum:Chengshi??0,Nongchun??1
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 163;// ????
					else
						dn = 164;// ????
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 161;// ????
					else
						dn = 162;// ????
				}
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);

				for (HunpeiField hpf : HunpeiField.values()) {
					String chn_name = HunpeiField.getChinese(hpf);
					mmm.put(hpf, parse_res.get(chn_name));
				}
			}
		}

		return m;
	}
	
	public static Map<CX, Map<NY, Map<HunpeiField, double[]>>> creatCXNYZiNvFromFile(
			int length, int year, int dqx) throws Exception {
		// ???map
		Map<CX, Map<NY, Map<HunpeiField, double[]>>> m = new EnumMap<CX, Map<NY, Map<HunpeiField, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<HunpeiField, double[]>>(NY.class));
			Map<NY, Map<HunpeiField, double[]>> mm = m.get(cx);
			for (NY ny : NY.values()) {
				// ???????????
				mm.put(ny,
						new EnumMap<HunpeiField, double[]>(HunpeiField.class));
				Map<HunpeiField, double[]> mmm = mm.get(ny);

				int dn;
				// getEditableNum:Chengshi??0,Nongchun??1
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 63;// ????
					else
						dn = 64;// ????
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 61;// ????
					else
						dn = 62;// ????
				}
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);

				for (HunpeiField hpf : HunpeiField.values()) {
					String chn_name = HunpeiField.getChinese(hpf);
					mmm.put(hpf, parse_res.get(chn_name));
				}
			}
		}

		return m;
	}

	public static Map<NY, Map<HunpeiField, double[]>> creatNYFNNationZiNvFromFile(
			int length, int year, int dqx) throws Exception {

		Map<NY, Map<HunpeiField, double[]>> mm = new EnumMap<NY, Map<HunpeiField, double[]>>(
				NY.class);

		for (NY ny : NY.values()) {
			mm.put(ny, new EnumMap<HunpeiField, double[]>(HunpeiField.class));
			Map<HunpeiField, double[]> mmm = mm.get(ny);

			int dn;
			if (ny.getChinese().equals("??") == true)
				dn = 167;
			else
				dn = 168;
			// int[] args = new int[4];
			// args[Parser.CHENG_ZHEN] = 0;
			// args[Parser.NONG_CUN] = 0;
			// // ?????????/??
			// // ??NY?enum?????getEditableNum??????????????
			// if (ny.getChinese().equals("??") == true) {
			// args[Parser.NONG_YE] = 1;
			// args[Parser.FEI_NONG] = 0;
			// } else {
			// args[Parser.FEI_NONG] = 1;
			// args[Parser.NONG_YE] = 0;
			// }

			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (HunpeiField hpf : HunpeiField.values()) {
				String chn_name = HunpeiField.getChinese(hpf);
				mmm.put(hpf, parse_res.get(chn_name));
			}
		}
		return mm;
	}
	/**
	 * ?????createNYZiNvFromFile
	 * 
	 * @param length
	 * @param year
	 * @param dqx
	 * @return
	 * @throws Exception
	 */
	public static Map<NY, Map<HunpeiField, double[]>> creatNYFNZiNvFromFile(
			int length, int year, int dqx) throws Exception {

		Map<NY, Map<HunpeiField, double[]>> mm = new EnumMap<NY, Map<HunpeiField, double[]>>(
				NY.class);

		for (NY ny : NY.values()) {
			mm.put(ny, new EnumMap<HunpeiField, double[]>(HunpeiField.class));
			Map<HunpeiField, double[]> mmm = mm.get(ny);

			int dn;
			if (ny.getChinese().equals("??") == true)
				dn = 67;
			else
				dn = 68;
			// int[] args = new int[4];
			// args[Parser.CHENG_ZHEN] = 0;
			// args[Parser.NONG_CUN] = 0;
			// // ?????????/??
			// // ??NY?enum?????getEditableNum??????????????
			// if (ny.getChinese().equals("??") == true) {
			// args[Parser.NONG_YE] = 1;
			// args[Parser.FEI_NONG] = 0;
			// } else {
			// args[Parser.FEI_NONG] = 1;
			// args[Parser.NONG_YE] = 0;
			// }

			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (HunpeiField hpf : HunpeiField.values()) {
				String chn_name = HunpeiField.getChinese(hpf);
				mmm.put(hpf, parse_res.get(chn_name));
			}
		}
		return mm;
	}

	public static Map<CX, Map<HunpeiField, double[]>> creatCXNationZiNvFromFile(
			int length, int year, int dqx) throws Exception {

		Map<CX, Map<HunpeiField, double[]>> mm = new EnumMap<CX, Map<HunpeiField, double[]>>(
				CX.class);

		for (CX cx : CX.values()) {
			mm.put(cx, new EnumMap<HunpeiField, double[]>(HunpeiField.class));
			Map<HunpeiField, double[]> mmm = mm.get(cx);

			int dn;
			if (cx.getEditableNum() == 0) {
				dn = 166;
			} else {
				dn = 165;
			}
			// int[] args = new int[4];
			// // ??????????/??
			// if (cx.getEditableNum() == 0) {
			// args[Parser.CHENG_ZHEN] = 1;
			// args[Parser.NONG_CUN] = 0;
			// } else {
			// args[Parser.NONG_CUN] = 1;
			// args[Parser.CHENG_ZHEN] = 0;
			// }
			// args[Parser.NONG_YE] = 0;
			// args[Parser.FEI_NONG] = 0;

			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (HunpeiField hpf : HunpeiField.values()) {
				String chn_name = HunpeiField.getChinese(hpf);
				mmm.put(hpf, parse_res.get(chn_name));
			}
		}
		return mm;
	}
	public static Map<CX, Map<HunpeiField, double[]>> creatCXZiNvFromFile(
			int length, int year, int dqx) throws Exception {

		Map<CX, Map<HunpeiField, double[]>> mm = new EnumMap<CX, Map<HunpeiField, double[]>>(
				CX.class);

		for (CX cx : CX.values()) {
			mm.put(cx, new EnumMap<HunpeiField, double[]>(HunpeiField.class));
			Map<HunpeiField, double[]> mmm = mm.get(cx);

			int dn;
			if (cx.getEditableNum() == 0) {
				dn = 66;
			} else {
				dn = 65;
			}
			// int[] args = new int[4];
			// // ??????????/??
			// if (cx.getEditableNum() == 0) {
			// args[Parser.CHENG_ZHEN] = 1;
			// args[Parser.NONG_CUN] = 0;
			// } else {
			// args[Parser.NONG_CUN] = 1;
			// args[Parser.CHENG_ZHEN] = 0;
			// }
			// args[Parser.NONG_YE] = 0;
			// args[Parser.FEI_NONG] = 0;

			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (HunpeiField hpf : HunpeiField.values()) {
				String chn_name = HunpeiField.getChinese(hpf);
				mmm.put(hpf, parse_res.get(chn_name));
			}
		}
		return mm;
	}
	public static Map<HunpeiField, double[]> creatAllNationZiNvFromFile(int length,
			int year, int dqx) throws Exception {

		Map<HunpeiField, double[]> mmm = new EnumMap<HunpeiField, double[]>(
				HunpeiField.class);

		int dn = 169;
		// int[] args = new int[4];
		// args[Parser.NONG_CUN] = 0;
		// args[Parser.CHENG_ZHEN] = 0;
		// args[Parser.NONG_YE] = 0;
		// args[Parser.FEI_NONG] = 0;

		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

		for (HunpeiField hpf : HunpeiField.values()) {
			String chn_name = HunpeiField.getChinese(hpf);
			mmm.put(hpf, parse_res.get(chn_name));
		}
		return mmm;
	}
	
	public static Map<HunpeiField, double[]> creatAllZiNvFromFile(int length,
			int year, int dqx) throws Exception {

		Map<HunpeiField, double[]> mmm = new EnumMap<HunpeiField, double[]>(
				HunpeiField.class);

		int dn = 69;
		// int[] args = new int[4];
		// args[Parser.NONG_CUN] = 0;
		// args[Parser.CHENG_ZHEN] = 0;
		// args[Parser.NONG_YE] = 0;
		// args[Parser.FEI_NONG] = 0;

		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

		for (HunpeiField hpf : HunpeiField.values()) {
			String chn_name = HunpeiField.getChinese(hpf);
			mmm.put(hpf, parse_res.get(chn_name));
		}
		return mmm;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createCXYearXBDoubleArrMapFromFile(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 13;
					else
						dn = 23;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<Year, Map<XB, double[]>> createYearXBDoubleArrMapFromFile(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 6;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}

	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createCXNYYearXBDoubleArrMapFromFile2(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 702;
					else
						dn = 704;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 701;
					else
						dn = 703;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createCXYearXBDoubleArrMapFromFile2(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 705;
					else
						dn = 706;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createNYYearXBDoubleArrMapFromFile2(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 708;
			else
				dn = 707;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<Year, Map<XB, double[]>> createYearXBDoubleArrMapFromFile2(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 709;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}

	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createCXNYYearXBDoubleArrMapFromFile3(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 602;
					else
						dn = 604;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 601;
					else
						dn = 603;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createCXYearXBDoubleArrMapFromFile3(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 605;
					else
						dn = 606;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createNYYearXBDoubleArrMapFromFile3(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 608;
			else
				dn = 607;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<Year, Map<XB, double[]>> createYearXBDoubleArrMapFromFile3(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 609;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}

	public static Map<Babies, double[]> createBirthPredictALL(int length,
			int year, int dqx) throws Exception {
		// createBirthPredictALL
		// DB???= =
		Map<Babies, double[]> m = new EnumMap<Babies, double[]>(Babies.class);
		int dn = 8;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			Babies bb = Babies.getBabies(i);
			String chn_name = "B" + Integer.toString(i);
			double[] tmp = new double[111];
			for (int j = 15; j < 50; j++) {
				tmp[j] = parse_res.get(chn_name)[j - 15];
			}
			m.put(bb, tmp);
		}
		return m;
	}

	public static Map<Babies, double[]> createNationBirthPredictALL(int length,
			int year, int dqx) throws Exception {
		// createBirthPredictALL
		// DB???= =
		Map<Babies, double[]> m = new EnumMap<Babies, double[]>(Babies.class);
		int dn = 108;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			Babies bb = Babies.getBabies(i);
			String chn_name = "B" + Integer.toString(i);
			double[] tmp = new double[111];
			for (int j = 15; j < 50; j++) {
				tmp[j] = parse_res.get(chn_name)[j - 15];
			}
			m.put(bb, tmp);
		}
		return m;
	}

	public static Map<CX, Map<Babies, double[]>> createOverBirthPredictOfCX(
			int length, int year, int dqx) throws Exception {

		Map<CX, Map<Babies, double[]>> m = new EnumMap<CX, Map<Babies, double[]>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(cx);
			int dn;// ???
			if (cx.getChinese().equals("??")) {
				dn = 34;// ??????
			} else {
				dn = 33;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}
	
	public static Map<CX, Map<Babies, double[]>> createNationOverBirthPredictOfCX(
			int length, int year, int dqx) throws Exception {

		Map<CX, Map<Babies, double[]>> m = new EnumMap<CX, Map<Babies, double[]>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(cx);
			int dn;// ???
			if (cx.getChinese().equals("??")) {
				dn = 135;// ??????
			} else {
				dn = 134;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}
	public static Map<NY, Map<Babies, double[]>> createOverBirthPredictOfNY(
			int length, int year, int dqx) throws Exception {

		Map<NY, Map<Babies, double[]>> m = new EnumMap<NY, Map<Babies, double[]>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(ny);
			int dn;// ???
			if (ny.getChinese().equals("??")) {
				dn = 36;// ??????
			} else {
				dn = 35;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}
	
	public static Map<NY, Map<Babies, double[]>> createNationOverBirthPredictOfNY(
			int length, int year, int dqx) throws Exception {

		Map<NY, Map<Babies, double[]>> m = new EnumMap<NY, Map<Babies, double[]>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Babies, double[]>(Babies.class));
			Map<Babies, double[]> mm = m.get(ny);
			int dn;// ???
			if (ny.getChinese().equals("??")) {
				dn = 133;// ??????
			} else {
				dn = 132;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				double[] tmp = new double[111];
				String chn_name = "B" + Integer.toString(i);
				for (int j = 15; j < 50; j++) {
					tmp[j] = parse_res.get(chn_name)[j - 15];
				}
				Babies bb = Babies.getBabies(i);
				mm.put(bb, tmp);
			}
		}
		return m;
	}
	
	public static Map<Babies, double[]> createOverBirthPredictALL(int length,
			int year, int dqx) throws Exception {
		// createBirthPredictALL
		// DB???= =
		Map<Babies, double[]> m = new EnumMap<Babies, double[]>(Babies.class);
		int dn = 30;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			Babies bb = Babies.getBabies(i);
			String chn_name = "B" + Integer.toString(i);
			double tmp[] = new double[111];
			for (int j = 15; j < 50; j++) {
				tmp[j] = parse_res.get(chn_name)[j - 15];
			}
			m.put(bb, tmp);
		}
		return m;
	}

	public static Map<Babies, double[]> createNationOverBirthPredictALL(int length,
			int year, int dqx) throws Exception {
		// createBirthPredictALL
		// DB???= =
		Map<Babies, double[]> m = new EnumMap<Babies, double[]>(Babies.class);
		int dn = 111;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			Babies bb = Babies.getBabies(i);
			String chn_name = "B" + Integer.toString(i);
			double tmp[] = new double[111];
			for (int j = 15; j < 50; j++) {
				tmp[j] = parse_res.get(chn_name)[j - 15];
			}
			m.put(bb, tmp);
		}
		return m;
	}
	
	public static double[][] createHusbandRate(int length, int year, int dqx)
			throws Exception {
		HashMap<String, double[]> tmp = Parser.parse(77, year, dqx);
		double[][] m = new double[111][111];
		for (int i = 15; i < 111; i++) {
			for (int j = 15; j < 111; j++) {
				m[i][j] = tmp.get("??" + Integer.toString(j))[i - 15];
			}
		}
		return m;
	}

	public static Map<Babies, double[]> createPolicyBabies(int length,
			int year, int dqx) throws Exception {
		Map<Babies, double[]> m = new EnumMap<Babies, double[]>(Babies.class);
		int dn = 31;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			Babies bb = Babies.getBabies(i);
			String chn_name = "B" + Integer.toString(i);
			double[] tmp = new double[111];
			for (int j = 15; j < 50; j++) {
				tmp[j] = parse_res.get(chn_name)[j - 15];
			}
			m.put(bb, tmp);
		}
		return m;
	}
	public static Map<Babies, double[]> createNationPolicyBabies(int length,
			int year, int dqx) throws Exception {
		Map<Babies, double[]> m = new EnumMap<Babies, double[]>(Babies.class);
		int dn = 114;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			Babies bb = Babies.getBabies(i);
			String chn_name = "B" + Integer.toString(i);
			double[] tmp = new double[111];
			for (int j = 15; j < 50; j++) {
				tmp[j] = parse_res.get(chn_name)[j - 15];
			}
			m.put(bb, tmp);
		}
		return m;
	}
	// public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>>
	// createPopulationPredictOfCXNY(
	// int length, int year, int dqx) throws Exception {
	// Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX,
	// Map<NY, Map<Year, Map<XB, double[]>>>>(
	// CX.class);
	//
	// for (CX cx : CX.values()) {
	// m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
	// Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
	// for (NY ny : NY.values()) {
	// m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
	// Map<Year, Map<XB, double[]>> mm = m2.get(ny);
	// int dn;
	// if (cx.getEditableNum() == 0) {
	// if (ny.getChinese().equals("??") == true)
	// dn = 602;
	// else
	// dn = 604;
	// } else {
	// if (ny.getChinese().equals("??") == true)
	// dn = 601;
	// else
	// dn = 603;
	// }
	// for (Year y : Year.getAllYears()) {
	// mm.put(y, new EnumMap<XB, double[]>(XB.class));
	// Map<XB, double[]> mmm = mm.get(y);
	// for (XB xb : XB.values()) {
	// String sex = xb.getEditableNum() == 0 ? "M" : "F";
	// String _y = "" + y;
	// _y = _y.substring(1);
	// String dest_name = sex + _y;
	// HashMap<String, double[]> parse_res = Parser.parse(dn,
	// year, dqx);
	// mmm.put(xb, parse_res.get(dest_name));
	// }
	// }
	// }
	// }
	// return m;
	// }

	/* ?????? ??????? ??? 15 25 */
	public static Map<CX, Map<Year, Map<XB, double[]>>> createPopulationDeathOfCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			int dn;// ???
			if (cx.getChinese().equals("??")) {
				dn = 25;// ??????
			} else {
				dn = 15;// ??????
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);

			for (int i = 2005; i <= 2100; i++) {
				mm.put(Year.getYear(i), new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(Year.getYear(i));

				for (XB xb : XB.values()) {
					String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
							+ Integer.toString(i);
					mmm.put(xb, parse_res.get(chn_name));
				}
			}
		}
		return m;
	}

	/* ???? ???7 */
	public static Map<Year, Map<XB, double[]>> createPopulationDeathOfAll(
			int length, int year, int dqx) throws Exception {
		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		int dn = 7;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			mm.put(Year.getYear(i), new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(Year.getYear(i));

			for (XB xb : XB.values()) {
				String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
						+ Integer.toString(i);
				mmm.put(xb, parse_res.get(chn_name));
			}
		}
		return mm;
	}
	
	public static Map<Year, Map<XB, double[]>> createNationPopulationDeathOfAll(
			int length, int year, int dqx) throws Exception {
		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		int dn = 107;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; i++) {
			mm.put(Year.getYear(i), new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(Year.getYear(i));

			for (XB xb : XB.values()) {
				String chn_name = (xb.getEditableNum() == 0 ? "M" : "F")
						+ Integer.toString(i);
				mmm.put(xb, parse_res.get(chn_name));
			}
		}
		return mm;
	}

	public static Map<Year, Map<Summary, Double>> createSummaryOfAll(
			int length, int year, int dqx) throws Exception {
		Map<Year, Map<Summary, Double>> mm = new EnumMap<Year, Map<Summary, Double>>(
				Year.class);
		int dn = 51;
		HashMap<String, double[]> parse_res = Parser.parse_DS(dn, year, dqx, 2);
		for (int i = 2000; i <= 2009; i++) {
			mm.put(Year.getYear(i), new EnumMap<Summary, Double>(Summary.class));
			Map<Summary, Double> mmm = mm.get(Year.getYear(i));

			for (Summary sm : Summary.values()) {
				if (sm == Summary.DQ)
					continue;
				String chn_name = Summary.getChinese(sm);
				mmm.put(sm, parse_res.get(chn_name)[i - 2000]);
			}
		}
		for (int i = 2010; i <= 2100; i++) {
			mm.put(Year.getYear(i), new EnumMap<Summary, Double>(Summary.class));
		}
		return mm;
	}

	public static Map<CX, Map<Year, Map<Summary, Double>>> createSummaryOfCX(
			int length, int year, int dqx) throws Exception {
		int dn;
		Map<CX, Map<Year, Map<Summary, Double>>> m = new EnumMap<CX, Map<Year, Map<Summary, Double>>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<Summary, Double>>(Year.class));
			Map<Year, Map<Summary, Double>> mm = m.get(cx);
			if (cx.getChinese().equals("??")) {
				dn = 52;
			} else {
				dn = 54;
			}

			HashMap<String, double[]> parse_res = Parser.parse_DS(dn, year,
					dqx, 2);
			for (int i = 2000; i <= 2009; i++) {
				mm.put(Year.getYear(i), new EnumMap<Summary, Double>(
						Summary.class));
				Map<Summary, Double> mmm = mm.get(Year.getYear(i));

				for (Summary sm : Summary.values()) {
					if (sm == Summary.DQ)
						continue;
					String chn_name = Summary.getChinese(sm);
					mmm.put(sm, parse_res.get(chn_name)[i - 2000]);
				}
			}
			for (int i = 2010; i <= 2100; i++) {
				mm.put(Year.getYear(i), new EnumMap<Summary, Double>(
						Summary.class));
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<Summary, Double>>> createSummaryOfNY(
			int length, int year, int dqx) throws Exception {
		int dn;
		Map<NY, Map<Year, Map<Summary, Double>>> m = new EnumMap<NY, Map<Year, Map<Summary, Double>>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<Summary, Double>>(Year.class));
			Map<Year, Map<Summary, Double>> mm = m.get(ny);
			if (ny.getChinese().equals("??")) {
				dn = 55;
			} else {
				dn = 53;
			}

			HashMap<String, double[]> parse_res = Parser.parse_DS(dn, year,
					dqx, 2);
			for (int i = 2000; i <= 2009; i++) {
				mm.put(Year.getYear(i), new EnumMap<Summary, Double>(
						Summary.class));
				Map<Summary, Double> mmm = mm.get(Year.getYear(i));

				for (Summary sm : Summary.values()) {
					if (sm == Summary.DQ)
						continue;
					String chn_name = Summary.getChinese(sm);
					mmm.put(sm, parse_res.get(chn_name)[i - 2000]);
				}
			}
			for (int i = 2010; i <= 2100; i++) {
				mm.put(Year.getYear(i), new EnumMap<Summary, Double>(
						Summary.class));
			}
		}
		return m;
	}

	/**
	 * ?????Map<NY,Map<XB,double>>,???CX?,??Nongchun??NYQF,chengshi??FNQF
	 * 
	 * @param year
	 * @param dqx
	 * @param path
	 * @return
	 */
	public static Map<CX, Map<XB, double[]>> createDeathRate(int year, int dqx,
			String path) {
		path = path + "variable_" + Integer.toString(year) + "_"
				+ Integer.toString(dqx) + ".txt";
		Map<CX, Map<XB, double[]>> m = new EnumMap<CX, Map<XB, double[]>>(
				CX.class);
		m.put(CX.Chengshi, new EnumMap<XB, double[]>(XB.class));
		m.put(CX.Nongchun, new EnumMap<XB, double[]>(XB.class));
		File file = new File(path);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "GBK");
			BufferedReader br = new BufferedReader(isr);

			String s;
			while ((s = br.readLine()) != null) {
				String temp = s;
				String[] as = temp.split(" +");
				if (as[0].equals("NYQF")) {
					m.get(CX.Nongchun).put(XB.Female, new double[112]);
					for (int i = 1; i <= 111; i++) {
						temp = br.readLine();
						as = temp.split(" +");
						m.get(CX.Nongchun).get(XB.Female)[i] = Double
								.parseDouble(as[5]);
					}
				} else if (as[0].equals("NYQM")) {
					m.get(CX.Nongchun).put(XB.Male, new double[112]);
					for (int i = 1; i <= 111; i++) {
						temp = br.readLine();
						as = temp.split(" +");
						m.get(CX.Nongchun).get(XB.Male)[i] = Double
								.parseDouble(as[5]);
					}
				} else if (as[0].equals("FNQF")) {
					m.get(CX.Chengshi).put(XB.Female, new double[112]);
					for (int i = 1; i <= 111; i++) {
						temp = br.readLine();
						as = temp.split(" +");
						m.get(CX.Chengshi).get(XB.Female)[i] = Double
								.parseDouble(as[5]);
					}
				} else if (as[0].equals("FNQM")) {
					m.get(CX.Chengshi).put(XB.Male, new double[112]);
					for (int i = 1; i <= 111; i++) {
						temp = br.readLine();
						as = temp.split(" +");
						m.get(CX.Chengshi).get(XB.Male)[i] = Double
								.parseDouble(as[5]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m;
	}

	public static Map<Year, Map<Couple, Double>> createCoupleAndChildrenOfAll(int dqx,
			int year) throws Exception {
		Map<Year, Map<Couple, Double>> mm = new EnumMap<Year, Map<Couple, Double>>(
				Year.class);

		int dn = 49;

		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; ++i) {
			// TODO ???foxpro??????????,??????Double?
			mm.put(Year.getYear(i), new EnumMap<Couple, Double>(Couple.class));
			Map<Couple, Double> mmm = mm.get(Year.getYear(i));
			for (Couple cpl : Couple.values()) {
				String chn_name = Couple.getChinese(cpl);
				mmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
			}
		}

		return mm;
	}

	public static Map<Year, Map<Couple, Double>> createNationCoupleAndChildrenOfAll(int dqx,
			int year) throws Exception {
		Map<Year, Map<Couple, Double>> mm = new EnumMap<Year, Map<Couple, Double>>(
				Year.class);

		int dn = 159;

		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; ++i) {
			// TODO ???foxpro??????????,??????Double?
			mm.put(Year.getYear(i), new EnumMap<Couple, Double>(Couple.class));
			Map<Couple, Double> mmm = mm.get(Year.getYear(i));
			for (Couple cpl : Couple.values()) {
				String chn_name = Couple.getChinese(cpl);
				mmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
			}
		}

		return mm;
	}
	public static Map<NY, Map<Year, Map<Couple, Double>>> createCoupleAndChildrenOfNY(
			int dqx, int year) throws Exception {
		Map<NY, Map<Year, Map<Couple, Double>>> m = new EnumMap<NY, Map<Year, Map<Couple, Double>>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<Couple, Double>>(Year.class));
			Map<Year, Map<Couple, Double>> mm = m.get(ny);

			int dn;
			if (ny.getChinese().equals("??")) {
				dn = 45;// ??
			} else {
				dn = 46;// ??
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
			for (int i = 2005; i <= 2100; ++i) {
				// TODO ???foxpro??????????,??????Double?
				mm.put(Year.getYear(i), new EnumMap<Couple, Double>(
						Couple.class));
				Map<Couple, Double> mmm = mm.get(Year.getYear(i));
				for (Couple cpl : Couple.values()) {
					String chn_name = Couple.getChinese(cpl);
					mmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
				}
			}
		}

		return m;
	}

	public static Map<NY, Map<Year, Map<Couple, Double>>> createNationCoupleAndChildrenOfNY(
			int dqx, int year) throws Exception {
		Map<NY, Map<Year, Map<Couple, Double>>> m = new EnumMap<NY, Map<Year, Map<Couple, Double>>>(
				NY.class);
		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<Couple, Double>>(Year.class));
			Map<Year, Map<Couple, Double>> mm = m.get(ny);

			int dn;
			if (ny.getChinese().equals("??")) {
				dn = 155;// ??
			} else {
				dn = 156;// ??
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
			for (int i = 2005; i <= 2100; ++i) {
				// TODO ???foxpro??????????,??????Double?
				mm.put(Year.getYear(i), new EnumMap<Couple, Double>(
						Couple.class));
				Map<Couple, Double> mmm = mm.get(Year.getYear(i));
				for (Couple cpl : Couple.values()) {
					String chn_name = Couple.getChinese(cpl);
					mmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
				}
			}
		}

		return m;
	}
	
	public static Map<CX, Map<Year, Map<Couple, Double>>> createCoupleAndChildrenOfCX(
			int dqx, int year) throws Exception {
		Map<CX, Map<Year, Map<Couple, Double>>> m = new EnumMap<CX, Map<Year, Map<Couple, Double>>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<Couple, Double>>(Year.class));
			Map<Year, Map<Couple, Double>> mm = m.get(cx);

			int dn;
			// getEditableNum:Chengshi??0,Nongchun??1
			if (cx.getEditableNum() == 0) {
				dn = 48;// ??
			} else {
				dn = 47;// ??
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
			for (int i = 2005; i <= 2100; ++i) {
				// TODO ???foxpro??????????,??????Double?
				mm.put(Year.getYear(i), new EnumMap<Couple, Double>(
						Couple.class));
				Map<Couple, Double> mmm = mm.get(Year.getYear(i));
				for (Couple cpl : Couple.values()) {
					String chn_name = Couple.getChinese(cpl);
					mmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
				}
			}
		}

		return m;
	}

	public static Map<CX, Map<Year, Map<Couple, Double>>> createNationCoupleAndChildrenOfCX(
			int dqx, int year) throws Exception {
		Map<CX, Map<Year, Map<Couple, Double>>> m = new EnumMap<CX, Map<Year, Map<Couple, Double>>>(
				CX.class);
		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<Couple, Double>>(Year.class));
			Map<Year, Map<Couple, Double>> mm = m.get(cx);

			int dn;
			// getEditableNum:Chengshi??0,Nongchun??1
			if (cx.getEditableNum() == 0) {
				dn = 158;// ??
			} else {
				dn = 157;// ??
			}
			HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
			for (int i = 2005; i <= 2100; ++i) {
				// TODO ???foxpro??????????,??????Double?
				mm.put(Year.getYear(i), new EnumMap<Couple, Double>(
						Couple.class));
				Map<Couple, Double> mmm = mm.get(Year.getYear(i));
				for (Couple cpl : Couple.values()) {
					String chn_name = Couple.getChinese(cpl);
					mmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
				}
			}
		}

		return m;
	}
	public static Map<CX, Map<NY, Map<Year, Map<Couple, Double>>>> createCoupleAndChildrenOfCXNY(
			int dqx, int year) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<Couple, Double>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<Couple, Double>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<Couple, Double>>>(NY.class));
			Map<NY, Map<Year, Map<Couple, Double>>> mm = m.get(cx);
			for (NY ny : NY.values()) {
				// ???????????
				mm.put(ny, new EnumMap<Year, Map<Couple, Double>>(Year.class));
				Map<Year, Map<Couple, Double>> mmm = mm.get(ny);
				int dn;
				// getEditableNum:Chengshi??0,Nongchun??1
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??"))
						dn = 43;// ????
					else
						dn = 44;// ????
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 41;// ????
					else
						dn = 42;// ????
				}
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				for (int i = 2005; i <= 2100; ++i) {
					// TODO ???foxpro??????????,??????Double?
					mmm.put(Year.getYear(i), new EnumMap<Couple, Double>(
							Couple.class));
					Map<Couple, Double> mmmm = mmm.get(Year.getYear(i));
					for (Couple cpl : Couple.values()) {
						String chn_name = Couple.getChinese(cpl);
						mmmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
					}
				}

			}
		}

		return m;
	}
	
	public static Map<CX, Map<NY, Map<Year, Map<Couple, Double>>>> createNationCoupleAndChildrenOfCXNY(
			int dqx, int year) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<Couple, Double>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<Couple, Double>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<Couple, Double>>>(NY.class));
			Map<NY, Map<Year, Map<Couple, Double>>> mm = m.get(cx);
			for (NY ny : NY.values()) {
				// ???????????
				mm.put(ny, new EnumMap<Year, Map<Couple, Double>>(Year.class));
				Map<Year, Map<Couple, Double>> mmm = mm.get(ny);
				int dn;
				// getEditableNum:Chengshi??0,Nongchun??1
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??"))
						dn = 153;// ????
					else
						dn = 154;// ????
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 151;// ????
					else
						dn = 152;// ????
				}
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				for (int i = 2005; i <= 2100; ++i) {
					// TODO ???foxpro??????????,??????Double?
					mmm.put(Year.getYear(i), new EnumMap<Couple, Double>(
							Couple.class));
					Map<Couple, Double> mmmm = mmm.get(Year.getYear(i));
					for (Couple cpl : Couple.values()) {
						String chn_name = Couple.getChinese(cpl);
						mmmm.put(cpl, parse_res.get(chn_name)[i - 2005]);
					}
				}

			}
		}

		return m;
	}

	/************************ ????? ****************************/
	public static void outputCXNYZinv(
			Map<CX, Map<NY, Map<HunpeiField, double[]>>> zinv, int year,
			int dqx, String path) throws Exception {
		ArrayList<String> header = Parser.getHeader("D:\\prclqz\\?????.txt");
		for (CX cx : CX.values()) {
			for (NY ny : NY.values()) {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(path + "\\java??\\"
							+ cx.getChinese() + ny.getChinese() + '_'
							+ Integer.toString(year) + "_"
							+ Integer.toString(dqx) + ".txt");
					for (String str : header) {
						fos.write((str + ",").getBytes());
					}
					fos.write("\n".getBytes());
					for (int i = 0; i < 111; i++) {
						for (HunpeiField hpf : HunpeiField.values()) {
							fos.write((Double.toString(zinv.get(cx).get(ny)
									.get(hpf)[i]) + ",").getBytes());
						}
						fos.write("\n".getBytes());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void outputSummaryOfBirthAndMigration(
			Map<Year, Map<SumBM, Double>> sumBM, StringList strValues,
			int year, int dqx, String path) throws Exception {
		ArrayList<String> header = Parser.getHeader("D:\\prclqz\\??????.txt");
		FileOutputStream fos = null;
		try {
			// ????
			fos = new FileOutputStream(path + "\\java??\\??????" + "_"
					+ Integer.toString(year) + "_" + Integer.toString(dqx)
					+ ".txt");
			// for(String str:header){
			// fos.write((str+",").getBytes());
			// }
			for (SumBM sb : SumBM.values()) {
				fos.write((SumBM.getChinese(sb) + ",").getBytes());
			}
			fos.write("\n".getBytes());
			// ?????
			int i = 0;
			for (SumBM sb : SumBM.values()) {
				i++;
				if (i == 1 || i == 2) {
					fos.write((strValues.get(sumBM.get(Year.getYear(year)).get(
							sb)) + ",").getBytes());
				} else {
					if (sumBM.get(Year.getYear(year)).get(sb) == null)
						fos.write((Double.toString(0) + ",").getBytes());
					else
						fos.write((Double.toString((sumBM.get(Year
								.getYear(year)).get(sb))) + ",").getBytes());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void outputSummary(Map<Summary, Double> summary, int cx,
			int year, int dqx, String path) throws Exception {
		FileOutputStream fos = null;
		try {
			// ????
			fos = new FileOutputStream(path + "\\java??\\??????" + "_"
					+ Integer.toString(cx) + "_" + Integer.toString(year) + "_"
					+ Integer.toString(dqx) + ".txt");
			// for(String str:header){
			// fos.write((str+",").getBytes());
			// }
			for (Summary sm : Summary.values()) {
				fos.write((Summary.getChinese(sm) + ",").getBytes());
			}
			fos.write("\n".getBytes());
			// ?????
			for (Summary sm : Summary.values()) {
				if (summary.get(sm) == null)
					fos.write((Double.toString(0.0) + ",").getBytes());
				else
					fos.write((Double.toString(summary.get(sm)) + ",")
							.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void outputCoupleAndChildrenOfCXNY(
			Map<CX, Map<NY, Map<Year, Map<Couple, Double>>>> coupleAndChildrenOfCXNY,
			int year, int dqx, String path) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> header = Parser.getHeader("D:\\prclqz\\?????.txt");
		for (CX cx : CX.values()) {
			for (NY ny : NY.values()) {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(path + "\\java??\\Couple_"
							+ cx.getChinese() + ny.getChinese() + '_'
							+ Integer.toString(year) + "_"
							+ Integer.toString(dqx) + ".txt");
					for (String str : header) {
						fos.write((str + ",").getBytes());
					}
					fos.write("\n".getBytes());
					for (Couple cp : Couple.values()) {
						Double d = coupleAndChildrenOfCXNY.get(cx).get(ny)
								.get(Year.getYear(year)).get(cp);

						fos.write(((Double.toString(d == null ? 0 : d)) + ",")
								.getBytes());
					}
					fos.write("\n".getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Map<Year, Map<PolicyBirth, Double>> createPolicyBirth(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<PolicyBirth, Double>> m = new EnumMap<Year, Map<PolicyBirth, Double>>(
				Year.class);
		int dn = 40;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		int i = 0;
		for (Year y : Year.getAllYears()) {
			m.put(y, new EnumMap<PolicyBirth, Double>(PolicyBirth.class));
			Map<PolicyBirth, Double> mm = m.get(y);
			for (PolicyBirth bb : PolicyBirth.values()) {
				String header = new String(PolicyBirth.getChinese(bb));
				Double tmp = parse_res.get(header)[i];
				mm.put(bb, tmp);
			}
			i++;
		}
		/*
		 * int dn = 8; HashMap<String, double[]> parse_res = Parser.parse(dn,
		 * year, dqx); for (int i = 2005; i <= 2100; i++) { Babies bb =
		 * Babies.getBabies(i); String chn_name = "B" + Integer.toString(i);
		 * double[] tmp = new double[111]; for (int j = 15; j < 50; j++) {
		 * tmp[j] = parse_res.get(chn_name)[j - 15]; } m.put(bb, tmp); }
		 */
		return m;
	}

	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createPopulationPredictOfCXNY(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 11;
					else
						dn = 12;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 21;
					else
						dn = 22;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}


	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createNationPopulationPredictOfCXNY(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 130;
					else
						dn = 131;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 140;
					else
						dn = 141;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createPopulationPredictOfCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 2;
					else
						dn = 4;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<CX, Map<Year, Map<XB, double[]>>> createNationPopulationPredictOfCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 102;
					else
						dn = 104;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<NY, Map<Year, Map<XB, double[]>>> createPopulationPredictOfNY(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 5;
			else
				dn = 3;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createNationPopulationPredictOfNY(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 105;
			else
				dn = 103;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<Year, Map<XB, double[]>> createPopulationPredictOfAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 1;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	
	public static Map<Year, Map<XB, double[]>> createNationPopulationPredictOfAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 101;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	
	public static Map<Year, Map<XB, double[]>> createPopulationMigrationOfAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 6;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	
	
	public static Map<Year, Map<XB, double[]>> createNationPopulationMigrationOfAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 106;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	
	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createSonDiePopulationPredictOfYiHaiCXNY(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 702;
					else
						dn = 704;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 701;
					else
						dn = 703;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createSonDiePopulationPredictOfYiHaiCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 705;
					else
						dn = 706;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createSonDiePopulationPredictOfYiHaiNY(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 708;
			else
				dn = 707;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<Year, Map<XB, double[]>> createSonDiePopulationPopulationPredictOfYiHaiAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 709;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	

	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createSonDiePopulationPredictOfTeFuCXNY(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 602;
					else
						dn = 604;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 601;
					else
						dn = 603;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createSonDiePopulationPredictOfTeFuCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 605;
					else
						dn = 606;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createSonDiePopulationPredictOfTeFuNY(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 608;
			else
				dn = 607;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<Year, Map<XB, double[]>> createSonDiePopulationPopulationPredictOfTeFuAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 609;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createNationSonDiePopulationPredictOfYiHaiCXNY(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 902;
					else
						dn = 901;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 904;
					else
						dn = 903;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createNationSonDiePopulationPredictOfYiHaiCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 905;
					else
						dn = 906;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createNationSonDiePopulationPredictOfYiHaiNY(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 908;
			else
				dn = 907;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<Year, Map<XB, double[]>> createNationSonDiePopulationPopulationPredictOfYiHaiAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 909;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}
	

	public static Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> createNationSonDiePopulationPredictOfTeFuCXNY(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<NY, Map<Year, Map<XB, double[]>>>> m = new EnumMap<CX, Map<NY, Map<Year, Map<XB, double[]>>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<NY, Map<Year, Map<XB, double[]>>>(NY.class));
			Map<NY, Map<Year, Map<XB, double[]>>> m2 = m.get(cx);
			for (NY ny : NY.values()) {
				m2.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
				Map<Year, Map<XB, double[]>> mm = m2.get(ny);
				int dn;
				if (cx.getEditableNum() == 0) {
					if (ny.getChinese().equals("??") == true)
						dn = 802;
					else
						dn = 801;
				} else {
					if (ny.getChinese().equals("??") == true)
						dn = 804;
					else
						dn = 803;
				}
				for (Year y : Year.getAllYears()) {
					mm.put(y, new EnumMap<XB, double[]>(XB.class));
					Map<XB, double[]> mmm = mm.get(y);
					for (XB xb : XB.values()) {
						String sex = xb.getEditableNum() == 0 ? "M" : "F";
						String _y = "" + y;
						_y = _y.substring(1);
						String dest_name = sex + _y;
						HashMap<String, double[]> parse_res = Parser.parse(dn,
								year, dqx);
						mmm.put(xb, parse_res.get(dest_name));
					}
				}
			}
		}
		return m;
	}

	public static Map<CX, Map<Year, Map<XB, double[]>>> createNationSonDiePopulationPredictOfTeFuCX(
			int length, int year, int dqx) throws Exception {
		Map<CX, Map<Year, Map<XB, double[]>>> m = new EnumMap<CX, Map<Year, Map<XB, double[]>>>(
				CX.class);

		for (CX cx : CX.values()) {
			m.put(cx, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(cx);
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					int dn;
					if (cx.getEditableNum() == 0)
						dn = 805;
					else
						dn = 806;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}

	public static Map<NY, Map<Year, Map<XB, double[]>>> createNationSonDiePopulationPredictOfTeFuNY(
			int length, int year, int dqx) throws Exception {
		Map<NY, Map<Year, Map<XB, double[]>>> m = new EnumMap<NY, Map<Year, Map<XB, double[]>>>(
				NY.class);

		for (NY ny : NY.values()) {
			m.put(ny, new EnumMap<Year, Map<XB, double[]>>(Year.class));
			Map<Year, Map<XB, double[]>> mm = m.get(ny);
			int dn;
			if (ny.getChinese() == "??")
				dn = 808;
			else
				dn = 807;
			for (Year y : Year.getAllYears()) {
				mm.put(y, new EnumMap<XB, double[]>(XB.class));
				Map<XB, double[]> mmm = mm.get(y);
				for (XB xb : XB.values()) {
					String sex = xb.getEditableNum() == 0 ? "M" : "F";
					String _y = "" + y;
					_y = _y.substring(1);
					String dest_name = sex + _y;
					HashMap<String, double[]> parse_res = Parser.parse(dn,
							year, dqx);
					mmm.put(xb, parse_res.get(dest_name));
				}
			}
		}
		return m;
	}
	public static Map<Year, Map<XB, double[]>> createNationSonDiePopulationPopulationPredictOfTeFuAll(
			int length, int year, int dqx) throws Exception {

		Map<Year, Map<XB, double[]>> mm = new EnumMap<Year, Map<XB, double[]>>(
				Year.class);
		for (Year y : Year.getAllYears()) {
			mm.put(y, new EnumMap<XB, double[]>(XB.class));
			Map<XB, double[]> mmm = mm.get(y);
			for (XB xb : XB.values()) {
				String sex = xb.getEditableNum() == 0 ? "M" : "F";
				String _y = "" + y;
				_y = _y.substring(1);
				String dest_name = sex + _y;
				int dn = 809;
				HashMap<String, double[]> parse_res = Parser.parse(dn, year,
						dqx);
				mmm.put(xb, parse_res.get(dest_name));
			}
		}
		return mm;
	}

	public static Map<Year,Map<BabiesBorn,Double>> createBabiesBorn(
			int length, int year, int dqx) throws Exception {
		Map<Year, Map<BabiesBorn, Double>> m = new EnumMap<Year, Map<BabiesBorn, Double>>(Year.class);
		int dn = 50;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; ++i) {
			m.put(Year.getYear(i), new EnumMap<BabiesBorn, Double>(BabiesBorn.class));
			Map<BabiesBorn, Double> mm = m.get(Year.getYear(i));
			for (BabiesBorn baby : BabiesBorn.values()){
				String chn_name = BabiesBorn.getChinese(baby);
				mm.put(baby, parse_res.get(chn_name)[i - 2005]);	
			}
		}
		return m;
			
	}
	
	public static Map<Year,Map<BabiesBorn,Double>> createNationBabiesBorn(
			int length, int year, int dqx) throws Exception {
		Map<Year, Map<BabiesBorn, Double>> m = new EnumMap<Year, Map<BabiesBorn, Double>>(Year.class);
		int dn = 160;
		HashMap<String, double[]> parse_res = Parser.parse(dn, year, dqx);
		for (int i = 2005; i <= 2100; ++i) {
			m.put(Year.getYear(i), new EnumMap<BabiesBorn, Double>(BabiesBorn.class));
			Map<BabiesBorn, Double> mm = m.get(Year.getYear(i));
			for (BabiesBorn baby : BabiesBorn.values()){
				String chn_name = BabiesBorn.getChinese(baby);
				mm.put(baby, parse_res.get(chn_name)[i - 2005]);	
			}
		}
		return m;
			
	}
}

