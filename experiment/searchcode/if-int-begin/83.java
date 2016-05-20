package javacommon.util;

import java.util.Hashtable;
import java.util.Map;

/**
 * ????JSON?
 * 
 * @author pch 2010.5.26
 * 
 */
public class JsonUtil {

	/**
	 * ??JSON????MAP
	 */
	private static Map<String, Object> map;
	/**
	 * ???????
	 */
	private static String ID_BEGIN = "{'id'";
	/**
	 * ???????
	 */
	private static String ID_END = "}";
	/**
	 * ?????
	 */
	public static String FILTER_URL = "url";

	/**
	 * ???????
	 * 
	 * @param allString
	 *            ???????
	 * @param begin
	 *            ????URL????????index
	 * @param end
	 *            ????URL????????index
	 * @return map (??menu:????URL?)
	 */
	private static Map encURL(String allString, int begin, int end) {
		if (map == null) {
			map = new Hashtable<String, Object>();
		}
		for (int j = (begin - 1); j >= 0; j--) {
			// ?????URL??????
			String bebinStr = allString.substring(0, begin + 1);
			// ???URL???ID
			int idBegin = bebinStr.indexOf(ID_BEGIN, j);
			if (idBegin >= 0) {
				int idEnd = allString.indexOf(ID_END, end + 1);
				if (idEnd >= 0) {
					String menu = allString.substring(idBegin, idEnd + 1);
					map.put("menu", menu);
					map.put("begin", idBegin);
					map.put("end", idEnd);
					return map;
				}
			}
		}
		return map;
	}

	/**
	 * ????????????????????
	 * 
	 * @param map(menus:???????,menu:?????,begin:menu???????,end:menu???????)
	 * @return String ???????
	 */
	private static String findParent(Map<String, Object> map) {

		String str = (String) map.get("menus");
		String menu = (String) map.get("menu");
		int begin = (Integer) map.get("begin");
		int end = (Integer) map.get("end");
		// ??????
		char sufix = 'c';
		String suf = null;
		if (begin >= 0) {
			sufix = str.charAt(end + 1);
			suf = String.valueOf(sufix);
			if (suf.equals(",")) {
				menu = menu + ",";
			} else if (suf.equals("]")) {
				menu = "," + menu;
			}
		}
		// ??????
		if (end >= 0) {
			char prefix = str.charAt(begin - 1);
			String pre = String.valueOf(prefix);
			if (pre.equals("[")) {
				if (suf.equals("]")) {
					if ((begin - 2) >= 0) {
						map = encURL(str, begin, end);
						menu = findParent(map);
					}
				}
			}
		}

		return menu;
	}

	/**
	 * ???????
	 * 
	 * @param value
	 *            ?????
	 * @param equalString(url)
	 *            ???????????????????
	 * @return String ???????
	 */
	public static String endValid(String value, String equalString) {
		if (value.length() >= 0) {
			if (equalString.length() >= 0) {
				for (int i = 0; i < value.length(); i++) {
					int urlBegin = value.indexOf(equalString, 0);
					if (urlBegin >= 0) {
						int urlEnd = value.indexOf("'", (urlBegin + equalString
								.length()));
						if (urlEnd >= 0) {
							Map map = encURL(value, urlBegin, urlEnd + 1);
							map.put("menus", value);
							String delete = findParent(map);
							value = value.replace(delete, "");
							return value;
						}
					}
				}
			}
		}
		return null;
	}

}

