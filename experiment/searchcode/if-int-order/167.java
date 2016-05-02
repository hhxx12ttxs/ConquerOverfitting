package com.exedosoft.plat.util;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.exedosoft.plat.DOServletContext;
import com.exedosoft.plat.DOThreadContext;
import com.exedosoft.plat.SessionContext;
import com.exedosoft.plat.util.xml.DOMXmlUtil;

/**
 * @author IBM
 */
public class DOGlobals {

	public static boolean RESULT_LIST_PINYIN = false;

	public static final int GREENCHANNEL_YES = 1;

	public static final int GREENCHANNEL_NO = 0;

	public static final int LOGIN_CA_YES = 1;

	public static final int LOGIN_CA_NO = 0;

	private DOGlobals() {
	}

	private static Log log = LogFactory.getLog(DOGlobals.class);

	/**
	 * ??????
	 */
	public static Map<String, String> globalConfigs = new HashMap<String, String>();

	/**
	 * ?????????GET?????
	 */
	public static String URL = "eeplat";

	/**
	 * ??????jsp???
	 */
	public static String PRE_FULL_FOLDER = "/" + URL + "/exedo/webv3/";

	/**
	 * ?????URL????????url?,?downloadfile_db.jsp,???????????
	 */
	public static String UPLOAD_URL = "/" + URL + "/upload/";

	/**
	 * ???URL??????
	 */

	public static String OUT_HTML_URL = "/" + URL + "/exedo/webv3/outhtml/";

	public static String PRE_FOLDER = "exedo/webv3/";

	public static int TransactionIsolation = 1;

	public static int MAX_CACHE = 1000;

	public static int MAX_RECORD_DIRECT_DB = 12000;

	/**
	 * ???????????????????????????????????
	 */
	public static boolean AUTH_ROLE_ONLY = false;

	/**
	 * ???????????????????????false. ?false ??????? ???????????????????????
	 * 
	 * ????????????????jdbc??????
	 * 
	 */

	public static boolean SEARCH_RESULTSIZE = false;

	public static int OPERATION_ICON = 0;

	public static int LIST_SHOWVALUE_MAX = 10;

	public static String UPLOAD_TEMP = "F:\\work\\workspace\\exedop\\.deployables\\exedo\\upload\\";

	public static String OUT_HTML = "F:\\eclipse32\\workspace\\anoles_client\\WebContent\\exedo\\web\\outhtml\\";

	public static String OUT_TEMPLATE = "F:\\eclipse32\\workspace\\anoles_client\\WebContent\\exedo\\web\\template\\";

	// ///////////////////????DB_CONFIG?????
	// // public static String DB_CONFIG =
	// "D:\\eclipse\\workspace\\exedo\\db\\mydb";

	public static String SESSION_PARTER = "com.exedosoft.plat.SessionParterDefault";

	public static String SERVLET_INIT_CLASS = "";

	public static String EXEDO_ORDER = "EXEDO_ORDER";

	public static String EXEDO_ORDER_TYPE = "EXEDO_ORDER_TYPE";

	/**
	 * 
	 */
	public static String WORK_DIR = "";

	/**
	 * ???????xml??
	 */
	public static String GT_OUT_XML = "";

	/**
	 * ?????????????????1?????????????2???????????????
	 */
	public static int WF_BACK_FORWORD = 1;

	/**
	 * ????CA??
	 */
	public static int LOGIN_CA = 0;

	/**
	 * ????CA??
	 */
	public static int LOGIN_GREENCHANNEL = 0;

	/**
	 * ??????
	 */
	public static int EXECUTOR_POOL_SIZE = 10;

	public static int IS_SCHEDULE = 1;

	// /////////13693033315
	private static DOGlobals globals = null;

	public static Document timeDefineDoc = null;

	static {

		try {

			InputStream is = DOGlobals.class
					.getResourceAsStream("/globals.xml");
			Document doc = DOMXmlUtil.getInstance().getDocument(is);
			NodeList nodeList = doc.getElementsByTagName("property");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				String attrName = element.getAttribute("name");
				String nodeValue = null;
				if (element.getFirstChild() != null) {
					nodeValue = element.getFirstChild().getNodeValue();
				}
				globalConfigs.put(attrName, nodeValue);

				if ("uploadfiletemp".equals(attrName)) {
					System.out.println("UPLOADFILE::::::::::::::::::::"
							+ nodeValue);
					UPLOAD_TEMP = nodeValue;
					continue;
				}
				if ("uploadfileurl".equals(attrName)) {
					System.out.println("UPLOADFILE::::::::::::::::::::"
							+ nodeValue);
					UPLOAD_URL = nodeValue;
					continue;
				}
				if ("webmodule".equals(attrName)) {
					System.out.println("webmodule::::::::::::::::::::"
							+ nodeValue);
					URL = nodeValue;
					PRE_FULL_FOLDER = "/" + URL + "/exedo/webv3/";
					UPLOAD_URL = "/" + URL + "/upload/";
					OUT_HTML_URL = "/" + URL + "/exedo/webv3/outhtml/";
					continue;
				}
				if ("search.resultsize".equals(attrName)) {
					System.out.println("search.resultsize::::::::::::::::::::"
							+ nodeValue);
					if ("1".equals(nodeValue))
						SEARCH_RESULTSIZE = true;
					continue;
				}

				if ("operation.icon".equals(attrName)) {
					System.out.println("operation.icon::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						OPERATION_ICON = Integer.parseInt(nodeValue);
					continue;
				}

				if ("transaction.isolation".equals(attrName)) {
					System.out
							.println("transaction.isolation::::::::::::::::::::"
									+ nodeValue);
					if (nodeValue != null)
						TransactionIsolation = Integer.parseInt(nodeValue);
					continue;
				}

				if ("servlet.init.class".equals(attrName)) {
					System.out.println("servlet.init.class::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						SERVLET_INIT_CLASS = nodeValue;
					continue;
				}

				if ("list.showvalue.max".equals(attrName)) {

					System.out.println("list.showvalue.max::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						LIST_SHOWVALUE_MAX = Integer.parseInt(nodeValue);
					continue;

				}

				if ("wf.back.forword".equals(attrName)) {

					System.out.println("wf.back.forword::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null) {
						WF_BACK_FORWORD = Integer.parseInt(nodeValue);
					}
					continue;

				}

				// ///////ca ??
				if ("login.ca".equals(attrName)) {

					System.out.println("login.ca::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null) {
						LOGIN_CA = Integer.parseInt(nodeValue);
					}
					continue;

				}

				// ////////??????

				// ///////ca ??
				if ("is.shedule".equals(attrName)) {

					System.out.println("is.shedule::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null) {
						IS_SCHEDULE = Integer.parseInt(nodeValue);
					}
					continue;

				}

				// /////////????
				if ("login.greenchannel".equals(attrName)) {

					System.out.println("login.greenchannel::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null) {
						LOGIN_GREENCHANNEL = Integer.parseInt(nodeValue);
					}
					continue;

				}

				if ("work.dir".equals(attrName)) {

					System.out.println("????::::::::::::::::::::" + nodeValue);
					if (nodeValue != null) {
						WORK_DIR = nodeValue;
					}
					continue;
				}
				// wf.back.forword

				if ("gt.outxml".equals(attrName)) {

					System.out.println("gt.outxml::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						GT_OUT_XML = nodeValue;
					continue;
				}

				if ("exedo.order".equals(attrName)) {

					System.out.println("exedo.order::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						EXEDO_ORDER = nodeValue;
					continue;
				}

				if ("exedo.order.type".equals(attrName)) {

					System.out.println("exedo.order.type::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						EXEDO_ORDER_TYPE = nodeValue;
					continue;
				}

				// ??????
				if ("executor.pool.size".equals(attrName)) {

					System.out
							.println("??????::::::::::::::::::::" + nodeValue);
					if (nodeValue != null)
						EXECUTOR_POOL_SIZE = Integer.parseInt(nodeValue);
					continue;

				}

				// //////////SESSION_PARTER session_parter???parter
				if ("sessionparter.class".equals(attrName)) {

					System.out.println("SESSION_PARTER::::::::::::::::::::"
							+ nodeValue);
					if (nodeValue != null)
						SESSION_PARTER = nodeValue;
					continue;

				}

			}

			URL url = DOGlobals.class.getResource("/globals.xml");
			String aPath = url.getPath().toLowerCase();
			aPath = aPath.replaceAll("/[.]/", "/");

			if (aPath.indexOf("web-inf") != -1) {
				OUT_HTML = aPath.substring(0, aPath.indexOf("web-inf"))
						+ "/exedo/web/outhtml/";
			}

			/**
			 * ??????????????
			 */
			Collections.unmodifiableMap(globalConfigs);
			log.info("Init global config finished!");

			// ///////////??????
			// if (IS_SCHEDULE == 1) {
			// GTSchedule.scheduleStat();
			// }

			// ///////????8?????
			// GTSchedule.scheduleStat();

		} catch (Exception e) {

			System.out.println("Error Read:::::::::::::globals.xml");
			e.printStackTrace();
			// System.exit(-1);

		}
	}

	public static String getValue(String aKey) {
		return globalConfigs.get(aKey);
	}

	/**
	 * ??DOGlobals ??
	 */
	public synchronized static DOGlobals getInstance() {

		if (globals == null) {
			globals = new DOGlobals();
		}
		return globals;
	}

	public DOThreadContext getRuleContext() {
		return SessionContext.getInstance().getThreadContext();
	}

	public SessionContext getSessoinContext() {

		return SessionContext.getInstance();

	}

	public void refreshContext(DOServletContext context) {
		SessionContext.getInstance().refreshContext(context);

	}

	/**
	 * ??????Quartz???????????servletcontext ???null,?????????ServletContext
	 * ???????????????
	 * 
	 * @return
	 */
	public DOServletContext getServletContext() {

		return SessionContext.getInstance().getServletContext();

	}

	public static void main(String[] args) {

	}

}

