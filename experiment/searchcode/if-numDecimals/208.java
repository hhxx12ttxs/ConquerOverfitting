package com.hoson.map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.hoson.DBUtil;
import com.hoson.StringUtil;
import com.hoson.app.App;

public class WarningAnyChart {
	/**
	 * ��õ��챨�����
	 * 
	 * @param request
	 * @return
	 */
	public static String getWarningData(HttpServletRequest request) {
		// String start_time = StringUtil.getNowDate() +"";//�������
		String start_time = "2009-4-13";// �����������
		String sql = "select t.STATION_ID st_id "
				+ "from T_MONITOR_WARNING_REAL t where t.START_TIME='"
				+ start_time + "' ";
		Connection conn = null;
		List<?> list = null;
		Set<String> station = new HashSet<String>();
		try {
			conn = DBUtil.getConn(request);
			list = DBUtil.query(conn, sql);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String xmlString = "<?xml version='1.0' encoding='UTF-8'?>";
		xmlString += "<anychart><settings><animation enabled='True' /></settings><charts><chart plot_type='CategorizedVertical'>"

				+ "  <data_plot_settings default_series_type='Line'>"
				+ "<line_series><tooltip_settings enabled='true'><format>"
				+ "վλ���: {%SeriesName}{thousandsSeparator:,numDecimals:0} \n"
				+ "����: {%Name}{numDecimals:0} \n"
				+ "��ֵ: {%YValue} \n"
				+ "��Ⱦ����: {%inid}{numDecimals:0}  \n"
				+ "��������: {%wtp}{numDecimals:0}"
				+ "</format></tooltip_settings></line_series></data_plot_settings><data>";

		for (Object object : list) {
			station.add(Obj2Str(((HashMap) object).get("st_id")));
		}
		if (station.size() > 0) {
			Iterator<String> it = station.iterator();
			List data = null;
			List data2 = null;
			String st_id = "";
			while (it.hasNext()) {
				st_id = it.next();
				String sql1 = "select t.STATION_ID st_id,t.INFECTANT_ID in_id ,t.AVG_VALUE val,"
						+ "t.WARNING_TYPE w_tp from T_MONITOR_WARNING_REAL t where t.START_TIME='"
						+ start_time + "' and t.STATION_ID='" + st_id + "' ";
				String sql2 = "SELECT t.STATION_DESC st_name from T_CFG_STATION_INFO t WHERE t.STATION_ID='"
						+ st_id + "'  ";
				try {
					data = DBUtil.query(conn, sql1);
					data2 = DBUtil.query(conn, sql2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				xmlString += "<series name='"
						+ Obj2Str(((HashMap) data2.get(0)).get("st_name"))
						+ "'>";
				for (int i = 1; i <= data.size(); i++) {
					xmlString += "<point name='" + i + "' y='"
							+ Obj2Str(((HashMap) data.get(i - 1)).get("val"))
							+ "' >"
							+ "<attributes><attribute name='inid'><![CDATA["
							+ Obj2Str(((HashMap) data.get(i - 1)).get("in_id"))
							+ "]]></attribute><attribute name='wtp'><![CDATA["
							+ Obj2Str(((HashMap) data.get(i - 1)).get("w_tp"))
							+ "]]></attribute></attributes></point>";
				}
				xmlString += "</series>";

			}

		}
		xmlString += "</data>"
				+ "<chart_settings><title><text>"
				+ start_time
				+ " �������</text><background enabled='false' /></title><chart_background enabled='false'/> "
				+ "<axes> <y_axis><title> <text>ƽ��ֵ</text></title></y_axis> "
				+ "<x_axis tickmarks_placement='Center'><labels /> <title><text>����</text> "
				+ "</title></x_axis> </axes></chart_settings>" + " </chart>"
				+ "</charts></anychart>";
		return xmlString;
	}

	/**
	 * ���±�������ͳ�����
	 * 
	 * @param req
	 * @return
	 */

	public static String getWarningOfMonth(HttpServletRequest req, String date) {
		if (StringUtil.isempty(date)) {
			date = (StringUtil.getNowDate() + "").substring(0, 7);// ��ǰʱ��
		}
		// String date = "2009-04-01";
		String start = date + "-01";
		String end = date.substring(0, 5)
				+ (Integer.parseInt(date.substring(5, 7).trim()) + 1) + "-01";
		String sql = "";
		try {
			sql = "SELECT  COUNT(*) times,t.START_TIME time from T_MONITOR_WARNING_REAL t where "
					+ "t.STATION_ID LIKE '"
					+ App.get("default_area_id")
					+ "%' "
					+ " and t.START_TIME >='"
					+ ""
					+ start
					+ "' and t.START_TIME<'"
					+ ""
					+ end
					+ "' GROUP BY t.START_TIME";
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Connection conn = null;
		List<?> list = null;
		try {
			conn = DBUtil.getConn(req);
			list = DBUtil.query(conn, sql);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			DBUtil.close(conn);
		}
		String key = "";
		String value = "0";
		Map<Integer, String> data = new HashMap<Integer, String>();
		for (int i = 1; i <= 31; i++) {
			data.put(i, "");
		}
		if (!list.isEmpty()) {
			for (Object object : list) {
				key = Obj2Str(((HashMap) object).get("time"));
				value = Obj2Str(((HashMap) object).get("times"));
				if (key.length() > 2) {
					data.put(Integer.parseInt(key.substring(key.length() - 2,
							key.length())), value);
				}

			}
		}
		String xmlString = "<?xml version='1.0' encoding='UTF-8'?>";
		xmlString += "<anychart><settings><animation enabled='True' /></settings><charts><chart plot_type='CategorizedVertical'>"
				+ "<chart_settings><title><text>����������ƣ�</text><background enabled='false' /></title><chart_background enabled='false'/> "
				+ "<axes><y_axis><title> <text>����"
				+ "</text></title></y_axis> <x_axis tickmarks_placement='Center'><labels />"
				+ "<title> <text>ʱ��</text></title></x_axis></axes></chart_settings><data_plot_settings default_series_type='Line'>"
				+ "<line_series><tooltip_settings enabled='true'><format>"
				+ "����:{%inid} \n"
				+ "��������: {%YValue}{numDecimals:0}"
				+ "</format></tooltip_settings> </line_series></data_plot_settings><data><series name='�������'>";
		for (int i = 1; i <= 31; i++) {
			xmlString += "<point name='" + i + "' y='" + data.get(i) + "' >"
					+ "<attributes><attribute name='inid'><![CDATA[" + date + i
					+ "]]></attribute></attributes></point>";
		}
		xmlString += " </series></data></chart> </charts></anychart>";
		return xmlString;
	}

	/*
	 * public static void main(String[] args) { String date =
	 * StringUtil.getNowDate() + ""; String start = date.substring(0, 8) + "1";
	 * String end = date.substring(0, 5) + (Integer.parseInt(date.substring(5,
	 * 7)) + 1) + "-1"; System.out.println(start+"======="+end+"===="+date); }
	 */

	/**
	 * ����xml�ĵ�
	 * 
	 * @param req
	 */

	public void saveAsXml(HttpServletRequest req, String date) {
		try {

			String urlContent = getWarningOfMonth(req, date);
			// String fileName
			// =this.getClass().getClassLoader().getResource("/").toString().replace("WEB-INF/classes/",
			// "").replace("file:","")+"pages/flashmap/water.xml";
			// ͯ���޸� ԭ�ȷ�����Ŀ¼���ڿո�ʱ���ո���� %20 �����ļ��Ҳ�����
			String fileName = req.getSession().getServletContext()
					.getRealPath("")
					+ "/pages/flashmap/warning.xml";
			File file = new File(fileName);
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			output.write(urlContent);
			output.flush();
			output.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public void saveAsXml(HttpServletRequest req,String url) {
	// try {
	//
	// // String fileName
	// //
	// =this.getClass().getClassLoader().getResource("/").toString().replace("WEB-INF/classes/",
	// // "").replace("file:","")+"pages/flashmap/water.xml";
	// // ͯ���޸� ԭ�ȷ�����Ŀ¼���ڿո�ʱ���ո���� %20 �����ļ��Ҳ�����
	// String fileName = req.getSession().getServletContext()
	// .getRealPath("")
	// + "/pages/compare/fx.xml";
	// File file = new File(fileName);
	// BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
	// new FileOutputStream(file), "UTF-8"));
	// output.write(url);
	// output.flush();
	// output.close();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	/**
	 * objectת���ַ�
	 * 
	 * @param obj
	 * @return
	 */

	public static String Obj2Str(Object obj) {

		return obj == null ? "" : obj.toString();

	}
}

