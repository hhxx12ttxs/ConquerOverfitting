package com.allyes.mifcor.data;

import com.allyes.mifcor.utils.A3CloudETLUtils;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A3MobileAdURL {
	private static enum Parameter {
		APP_ID("ai", 0, false),
		PUBLISH_ID("pi", 1, false),
		DIST_ID("di", 2, true),
		USER_ID("sn", 3, true),
		SIM_CARD_NO("si", 4, true),
		IMEI_NO("im", 5, true),
		MOBILE_NO("m", 6, true),
		BASE_STATION_ID("ci", 7, true),
		GPS_COORD("g", 8, true),
		SCREEN_SIZE("ss", 9, true),
		OS("os", 10, true),
		PLATFORM("plf", 11, true),
		CHIP_NO("ch", 12, true),
		DEVICE_MODEL("mo", 13, true),
		BRAND ("mb",14,true),
		OPERATOR("op", 15, true),
		AD_SPACE("c", 16, false),
		CHANNEL_SIZE("s", 17, false),
		AD_SPACE_TYPE ("cht",18,true),
		BORDER_COLOR("bc", 19, true),
		BG_COLOR("bg", 20, true),
		TEXT_COLOR("tc", 21, true),
		LANGUAGE("ul", 22, true),
		OUTPUT_FORMAT("t", 23, false),
		ENCRYPTED("e", 24, false),
		TESTMODE("tm", 25, true),
		DATABASE("d", 26, false),
		CONNECTION_TYPE("n", 27, true),
		VERSION ("v",28,true),  
		TO_URL("u",29,true),
		MAC_ADDR("ct",30,true);

		private static Map<String, Parameter> idMap = new HashMap<String, Parameter>();

		static {
			for (Parameter e : Parameter.values()) {
				idMap.put(e.getId(), e);
			}
		}

		private final String id;
		private final int index;
		private final boolean canEmpty;

		private Parameter(String id, int index, boolean canEmpty) {
			this.id = id;
			this.index = index;
			this.canEmpty = canEmpty;
		}

		public String getId() {
			return this.id;
		}

		public int getIndex() {
			return this.index;
		}

		public boolean canEmpty() {
			return this.canEmpty;
		}

		public static Parameter fromIdValue(String id) {
			return idMap.get(id);
		}
	}

	private String[] paraValues = new String[Parameter.values().length];
	private boolean isValidate = true;

	public A3MobileAdURL() {
	}

	public A3MobileAdURL(String url) throws MalformedURLException {
		parse(url);
	}

	public boolean isValidate(){
		return isValidate;
	}

	public void parse(String url) throws MalformedURLException {
		String[] newValues = new String[this.paraValues.length];

		int len1 = getCharCount(new URL(url).getQuery(), "&");

		int len2 = getCharCount(new URL(url).getQuery(), "=");

		if ((len1 + 1) != len2) {
			// System.out.println("len1:"+len1+",len2:"+len2+",url:"+url);
			isValidate = false;
		} else {
			if (len2 < Parameter.values().length) {
				// System.out.println("len1:"+len1+",len2:"+len2+",url:"+url);
				isValidate = false;
			}
		}

		for (String kv : StringUtils.split(new URL(url).getQuery(), '&')) {
			String[] p = StringUtils.split(kv, "=");

			if (p.length == 0 || p.length > 2) {
				isValidate = false;
			}
			String value = "";
			if (p.length > 1) {
				value = p[1];
			}
			Parameter param = Parameter.fromIdValue(p[0]);
			if (param != null) {
				newValues[param.getIndex()] = value;
			}
		}
		for (Parameter e : Parameter.values()) {
			if (!e.canEmpty() && newValues[e.getIndex()] == null) {
				isValidate = false;
			}
		}

		this.paraValues = newValues;
	}

	public String get(Parameter param) {

		return this.paraValues[param.getIndex()];
	}

	public int param_Validation() {

		// APP_ID("ai", 0, false) 判断是否是数字
		if (!isNumeric(get(Parameter.APP_ID))) {

			return Parameter.APP_ID.getIndex() + 1;
		}

		// PUBLISH_ID("pi", 1, false) 判断是否是数字
		if (!isNumeric(get(Parameter.PUBLISH_ID))) {

			return Parameter.PUBLISH_ID.getIndex() + 1;
		}

		// DIST_ID("di", 2, true)
		String decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.DIST_ID), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 16)) {

			return Parameter.DIST_ID.getIndex() + 1;
		}

		// USER_ID("sn", 3, true)
		if (!regex_matches(get(Parameter.USER_ID), "^[a-zA-Z0-9]{16,41}$", true)) {

			return Parameter.USER_ID.getIndex() + 1;
		}

		// SIM_CARD_NO("si", 4, true) // ^{0,20}$
		/******************************************************************************
		 * if
		 * (!regex_matches(get(Parameter.SIM_CARD_NO),"^[0-9]{15,20}$",true)){
		 * 
		 * return Parameter.SIM_CARD_NO.getIndex()+1; }
		 ******************************************************************************/
		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.SIM_CARD_NO), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 50)) {

			return Parameter.SIM_CARD_NO.getIndex() + 1;
		}

		// IMEI_NO("im", 5, true) //
		/*****************************************************************************
		 * if (!regex_matches(get(Parameter.IMEI_NO),"^[0-9]{15,60}$",true)){
		 * 
		 * //CDMA MEID if
		 * (!regex_matches(get(Parameter.IMEI_NO),"^[a-fA-F0-9]{14}$",true)){
		 * 
		 * return Parameter.IMEI_NO.getIndex()+1; }
		 * 
		 * }
		 ******************************************************************************/

		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.IMEI_NO), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 50)) {

			return Parameter.IMEI_NO.getIndex() + 1;
		}

		// MOBILE_NO("m", 6, true) +86 86 old_^[0-9]{0,11}$
		// reg:"^((\\+86)|(86))?(1)\\d{10}$"
		// reg_temp:^[\+]?[0-9\x2d\x20]{1,20}$
		if (!regex_matches(get(Parameter.MOBILE_NO), "^[\\+]?[0-9\\x2d\\x20]{1,20}$", true)) {

			return Parameter.MOBILE_NO.getIndex() + 1;
		}
		// BASE_STATION_ID("ci", 7, true) old
		// ^\d{1,10}-?(-)\d{1,10}-?(-)\d{1,10}-?(-)\d{1,10}$ new
		// ^\\d{1,10}-\\d{1,10}-\\d{1,10}-\\d{1,10}$
		if (!regex_matches(get(Parameter.BASE_STATION_ID), "^\\d{1,10}-?(-)\\d{1,10}-?(-)\\d{1,10}-?(-)\\d{1,10}$",
				true)) {

			if (!regex_matches(get(Parameter.BASE_STATION_ID), "^[0-9]{0,15}$", true)) {
				return Parameter.BASE_STATION_ID.getIndex() + 1;
			}
		}

		// GPS_COORD("g", 8, true)
		// reg:"^-?\\d{1,3}\\.\\d{0,20}\\|-?\\d{1,3}\\.\\d{0,20}$"
		// reg_temp:^[\+\-]?[\d]+([\.][\d]*)?([Ee][+-]?[\d]+)?\|[\+\-]?[\d]+([\.][\d]*)?([Ee][+-]?[\d]+)?$
		if (!regex_matches(get(Parameter.GPS_COORD),
				"^[\\+\\-]?[\\d]+([\\.][\\d]*)?([Ee][+-]?[\\d]+)?\\|[\\+\\-]?[\\d]+([\\.][\\d]*)?([Ee][+-]?[\\d]+)?$",
				true)) {

			return Parameter.GPS_COORD.getIndex() + 1;

		} else if (getStrLen(get(Parameter.GPS_COORD)) > 60) {

			return Parameter.GPS_COORD.getIndex() + 1;
		}

		// SCREEN_SIZE("ss", 9, true)
		if (!regex_matches(get(Parameter.SCREEN_SIZE), "^\\d{3,4}\\|\\d{3,4}$", true)) {

			return Parameter.SCREEN_SIZE.getIndex() + 1;
		}

		// OS("os", 10, true),

		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.OS), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 100)) {

			return Parameter.OS.getIndex() + 1;
		}

		// PLATFORM("plf", 11, true)
		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.PLATFORM), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 100)) {

			return Parameter.PLATFORM.getIndex() + 1;
		}

		// CHIP_NO("ch", 12, true)
		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.CHIP_NO), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 100)) {

			System.out.println("chip_error!");
			return Parameter.CHIP_NO.getIndex() + 1;

		}

		// DEVICE_MODEL("mo", 13, true)
		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.DEVICE_MODEL), "utf-8");

		if (!is_three_bytes_utf8(decode_str, 100)) {

			return Parameter.DEVICE_MODEL.getIndex() + 1;
		}

		// BRAND ("mb",14,true)
		decode_str = A3CloudETLUtils.URLDecoder(get(Parameter.BRAND), "utf-8");
		if (!is_three_bytes_utf8(decode_str, 100)) {

			return Parameter.BRAND.getIndex() + 1;
		}

		// OPERATOR("op", 15, true)
		// reg："^\\d{5,6}$"
		// temp_reg:^[a-zA-Z0-9]{1,10}$
		if (!regex_matches(get(Parameter.OPERATOR), "^[a-zA-Z0-9]{1,10}$", true)) {

			return Parameter.OPERATOR.getIndex() + 1;
		}
		// AD_SPACE("c", 16, false)
		if (!isNumeric(get(Parameter.AD_SPACE))) {

			return Parameter.AD_SPACE.getIndex() + 1;
		}
		// CHANNEL_SIZE("s", 17, false)
		if (!regex_matches(get(Parameter.CHANNEL_SIZE), "^\\d{2,4}\\|\\d{3,4}$", false)) {

			return Parameter.CHANNEL_SIZE.getIndex() + 1;
		}
		// AD_SPACE_TYPE ("cht",18,true)

		// BORDER_COLOR("bc", 19, true)

		// BG_COLOR("bg", 20, true)

		// TEXT_COLOR("tc", 21, true)

		// LANGUAGE("ul", 22, true)
		if (!regex_matches(get(Parameter.LANGUAGE), "^[a-zA-Z-]{0,10}$", true)) {

			return Parameter.LANGUAGE.getIndex() + 1;
		}
		// OUTPUT_FORMAT("t", 23, false)
		// ENCRYPTED("e", 24, false)
		// TESTMODE("tm", 25, true)
		// DATABASE("d", 26, false)
		// CONNECTION_TYPE("n", 27, true)
		if (!regex_matches(get(Parameter.CONNECTION_TYPE), "^[0-2]*$", true)) {

			return Parameter.CONNECTION_TYPE.getIndex() + 1;
		}
		// VERSION ("v",28,true)
		if (!regex_matches(get(Parameter.VERSION), "^[a-zA-Z0-9_\\.]{0,60}$", true)) {

			return Parameter.VERSION.getIndex() + 1;
		}

		// TO_URL("u",29,true)

		// MAC_ADDR("ct",30,true)
		// ^[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}$
		if ("1".equals(get(Parameter.MAC_ADDR))) {

			return 0;

		} else if (!regex_matches(get(Parameter.MAC_ADDR),
				"^[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}$",
				true)) {

			return Parameter.MAC_ADDR.getIndex() + 1;
		}
		return 0;

	}

	public static boolean regex_matches(String input, String rex) {
		boolean ret = false;

		try {
			ret = Pattern.matches(rex, input);
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public static boolean regex_matches(String input, String rex, boolean allow_null) {
		boolean ret = false;

		if (input == null || input.equals("")) {
			if (allow_null)
				return true;
			else
				return false;
		}

		try {
			ret = regex_matches(input, rex);

		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public String getAppId() {

		if (get(Parameter.APP_ID) == null) {
			return "";
		}
		return get(Parameter.APP_ID);
	}

	public String getPublishId() {

		if (get(Parameter.PUBLISH_ID) == null) {
			return "";
		}

		return get(Parameter.PUBLISH_ID);
	}

	public String getDistId() {

		if (get(Parameter.DIST_ID) == null) {
			return "";
		}

		String dist_id = A3CloudETLUtils.URLDecoder(get(Parameter.DIST_ID), "utf-8");

		if (is_three_bytes_utf8(dist_id)) {
			;
		} else {
			dist_id = get(Parameter.DIST_ID);
		}
		// return A3CloudETLUtils.URLDecoder(get(Parameter.DIST_ID),"utf-8");

		return transStr(dist_id);
	}

	public String getSimCardNo() {

		if (get(Parameter.SIM_CARD_NO) == null) {
			return "";
		}
		return get(Parameter.SIM_CARD_NO);
	}

	public String getImeiNo() {

		if (get(Parameter.IMEI_NO) == null) {
			return "";
		}

		return get(Parameter.IMEI_NO);
	}

	public String getMobileNo() {
		if (get(Parameter.MOBILE_NO) == null) {
			return "";
		}
		return get(Parameter.MOBILE_NO);
	}

	public String getBaseStationId() {
		if (get(Parameter.BASE_STATION_ID) == null) {
			return "";
		}
		return get(Parameter.BASE_STATION_ID);
	}

	public String getGpsCoord() {

		if (get(Parameter.GPS_COORD) == null) {
			return "";
		}
		return get(Parameter.GPS_COORD);
	}

	public String getScreenSize() {
		if (get(Parameter.SCREEN_SIZE) == null) {
			return "";
		}
		return get(Parameter.SCREEN_SIZE);
	}

	public String getOs() {

		String os = get(Parameter.OS);

		if (os == null) {
			return "";
		}

		os = A3CloudETLUtils.URLDecoder(get(Parameter.OS), "utf-8");

		if (is_three_bytes_utf8(os)) {
			// nothing to do
		} else {
			os = get(Parameter.OS);
		}

		return transStr(os);
	}

	public String getPlatform() {

		String platForm = get(Parameter.PLATFORM);
		if (platForm == null) {
			return "";
		}

		platForm = A3CloudETLUtils.URLDecoder(get(Parameter.PLATFORM), "utf-8");

		if (is_three_bytes_utf8(platForm)) {
		} else {
			platForm = get(Parameter.PLATFORM);
		}

		return transStr(platForm);

	}

	public String getChipNo() {

		if (get(Parameter.CHIP_NO) == null) {
			return "";
		}

		String chip_no = A3CloudETLUtils.URLDecoder(get(Parameter.CHIP_NO), "utf-8");

		if (is_three_bytes_utf8(chip_no)) {

			;
		} else {

			chip_no = get(Parameter.CHIP_NO);
		}

		return transStr(chip_no);

		// return A3CloudETLUtils.URLDecoder(get(Parameter.CHIP_NO),"utf-8");
	}

	public String getDeviceModel() {

		if (get(Parameter.DEVICE_MODEL) == null) {
			return "unknown";
		}
		String dm = A3CloudETLUtils.URLDecoder(get(Parameter.DEVICE_MODEL), "utf-8");
		if (is_three_bytes_utf8(dm)) {

			;
		} else {

			dm = "unknown";
		}

		return transStr(dm);

		// return
		// A3CloudETLUtils.URLDecoder(get(Parameter.DEVICE_MODEL),"utf-8");
	}

	public String getOperator() {

		if (get(Parameter.OPERATOR) == null) {
			return "";
		}

		return get(Parameter.OPERATOR);
	}

	public String getAdSpace() {

		if (get(Parameter.AD_SPACE) == null) {
			return "";
		}

		return get(Parameter.AD_SPACE);
	}

	public String getChannelSize() {

		if (get(Parameter.CHANNEL_SIZE) == null) {
			return "";
		}

		return get(Parameter.CHANNEL_SIZE);
	}

	public String getBorderColor() {

		if (get(Parameter.BORDER_COLOR) == null) {
			return "";
		}

		return get(Parameter.BORDER_COLOR);
	}

	public String getBgColor() {

		if (get(Parameter.BG_COLOR) == null) {
			return "";
		}
		return get(Parameter.BG_COLOR);
	}

	public String getTextColor() {

		if (get(Parameter.TEXT_COLOR) == null) {
			return "";
		}

		return get(Parameter.TEXT_COLOR);
	}

	public String getLanguage() {

		if (get(Parameter.LANGUAGE) == null) {
			return "";
		}
		String language = A3CloudETLUtils.URLDecoder(get(Parameter.LANGUAGE), "utf-8");
		if (is_three_bytes_utf8(language)) {

			;
		} else {

			language = get(Parameter.LANGUAGE);
		}

		return transStr(language);

		// return A3CloudETLUtils.URLDecoder(get(Parameter.LANGUAGE),"utf-8");
	}

	public String getOutputFormat() {

		if (get(Parameter.OUTPUT_FORMAT) == null) {
			return "";
		}

		return get(Parameter.OUTPUT_FORMAT);
	}

	public String getEncrypted() {

		if (get(Parameter.ENCRYPTED) == null) {
			return "";
		}

		return get(Parameter.ENCRYPTED);
	}

	public String getDatabase() {

		if (get(Parameter.DATABASE) == null) {
			return "";
		}

		return get(Parameter.DATABASE);
	}

	public String getConnectionType() {

		if (get(Parameter.CONNECTION_TYPE) == null) {
			return "";
		}
		return get(Parameter.CONNECTION_TYPE);
	}

	public String getUserId() {

		String sn = "";

		try {

			sn = transStr(get(Parameter.USER_ID));
			sn = get(Parameter.USER_ID).toLowerCase();
		} catch (Exception e) {

			sn = "";
		}

		return sn;
	}

	public String getToUrl() {
		String result = get(Parameter.TO_URL);
		if (result == null || result.equals("null"))
			return "";
		return result;
	}

	public String getBrand() {

		if (get(Parameter.BRAND) == null) {
			return "unknown";
		}
		String brand = A3CloudETLUtils.URLDecoder(get(Parameter.BRAND), "utf-8");
		if (is_three_bytes_utf8(brand)) {

			;
		} else {

			brand = "unknown";
		}

		return transStr(brand);

		// return A3CloudETLUtils.URLDecoder(get(Parameter.BRAND),"utf-8");
	}

	public String getAdSpaceType() {
		if (get(Parameter.AD_SPACE_TYPE) == null) {
			return "";
		}

		return get(Parameter.AD_SPACE_TYPE);
	}

	public String getVersion() {
		String version = "unknown";
		if (get(Parameter.VERSION) == null) {
			return "unknown";
		}

		version = A3CloudETLUtils.URLDecoder(get(Parameter.VERSION), "utf-8");

		if (is_three_bytes_utf8(version)) {
			;
		} else {
			version = "unknown";
		}

		return version;

	}

	public String getMac() {

		String mac_addr = "";
		try {

			mac_addr = transStr(get(Parameter.MAC_ADDR));

			if ("1".equals(get(Parameter.MAC_ADDR))) {

				mac_addr = "";

			} else {
				mac_addr = get(Parameter.MAC_ADDR).toLowerCase();
			}
		} catch (Exception e) {

			mac_addr = "";
		}

		return mac_addr;
	}

	public static String transStr(String str) {

		if (str == null) {
			str = "";
		} else {
			str = str.replaceAll("\"", "\"\"");
			str = str.replaceAll("\r\n", "");
		}

		return str;
	}

	public static boolean is_three_bytes_utf8(String input, int len) {
		// 0000 - 007F 1 个字节
		// 0080 - 07FF 2个字节
		// 0800 - FFFF 3个字节
		// return Pattern.matches("^[\\u2E80-\\uFAD9]+$",input) ;
		boolean ret = false;

		if (input == null || input.equals(""))
			return true;

		if (getStrLen(input) > len)
			return false;

		try {
			ret = is_three_bytes_utf8(input);
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public static boolean is_three_bytes_utf8(String input) {
		// 0000 - 007F 1 个字节
		// 0080 - 07FF 2个字节
		// 0800 - FFFF 3个字节
		// return Pattern.matches("^[\\u2E80-\\uFAD9]+$",input) ;
		boolean ret = false;
		try {
			ret = Pattern.matches("^[\\u0000-\\u007F\\u0080-\\u07FF\\u0800-\\uFFFF]+$", input);
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public static boolean isNumeric(String str) {
		BigDecimal result = null;
		try {
			result = new BigDecimal(str);
		} catch (Exception e) {
		}
		if (result == null)
			return false;
		else
			return true;
	}

	public static int getStrLen(String str) {

		if (str == null)

			return 0;
		else
			return str.length();
	}

	public static boolean is_version(String input) {
		boolean ret = false;
		// ^[a-zA-Z0-9_\\.]{1,}$ 数字，字母
		try {
			ret = Pattern.matches("^[a-zA-Z0-9_\\.]{1,}$", input);
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public static int getCharCount(String str, String ch) {

		Pattern p = Pattern.compile(ch);
		Matcher m = p.matcher(str);
		int ct = 0;

		while (m.find()) {
			ct++;
		}

		return ct;
	}

	public static void main(String[] args) {

		// System.out.println(is_version("msn_sdk_windowsphone7_1.3jfccb~.%~v"));
		// System.out.println(is_version(""));
		A3MobileAdURL mobileUrl = new A3MobileAdURL();
		// String
		// str="3DAnB%B3%22%E32%E32f6%83%D6%17%26%D6V%16%26%92%D7cv%12fG%26%F6%96B%B3%22%E32%E32g%06%C6c%D4%16%E6G%26%F6%96";
		// str="Android+5.0.2%28%F0%AD%B0%A6%29";
		// str=A3CloudETLUtils.URLDecoder(str, "utf8");
		// str="";
		// System.out.println(str);
		// System.out.println(is_three_bytes_utf8(str));
		String url = "http://de.msn.allyesapp.com/m?ai=1&pi=2&di=&sn=&si=&im=072251004188013097007244002125107222109150107246150153105027&m=&ci=&g=&ss=800|480&os=microsoft+windows+ce+7.10.8773&plf=windows+phone&ch=𭰦𭰦&mo=nokia+710&mb=nokia&op=46001&c=40&s=80|480&ct=&cht=&bc=&bg=000000&tc=ffffff&ul=&t=6&e=1&tm=0&d=a3ms&n=0&v=msn_sdk_windowsphone7_1.5";
		// url="http://10.200.34.202/c?d=a3test&i=z117,273,422,274&rf=&a=dp6a2sphs0cezig3c24wcoc5q&ai=117&pi=12&di=&sn=054d84f731a7ced3&si=310260000000000&im=000000000000000&m=15555215554&ci=&g=&ss=800|480&os=android+2.1-update1&plf=android&ch=armeabi&mo=unknown&mb=unknown&op=31026&c=117&s=80|480&ct=&cht=&bc=&bg=&tc=&ul=en-us&t=6&e=1&tm=0&n=1&v=sprint15&nf=0&u=http%3a%2f%2fwww.baidu.com";
		url = "http://a3.allyes.com/m?ai=85&pi=58&di=&sn=04a518b122104608&si=460004400494101&im=012682006668413&m=&ci=&g=&ss=854|480&os=android+2.3.4+by+%e6%89%8b%e6%9c%ba%e5%93%a5&plf=android+2.3.4+by+%e6%89%8b%e6%9c%ba%e5%93%a5&ch=armeabi-v7a&mo=sony+ericsson+%e7%a5%9e%e6%9c%ba+x12&mb=sony+ericsson&op=46000&c=85&s=80|480&ct=1&bc=&bg=&tc=&ul=zh-cn&t=6&e=1&tm=0&d=a3&n=2";
		url = url.toLowerCase();
		try {
			mobileUrl.parse(url);
			System.out.println("-----:" + mobileUrl.param_Validation());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String str = "000-00-0000-0000a";
		if (!regex_matches(str, "^\\d{3}-\\d{2}-\\d{1,5}-\\d{1,5}$", true)) {

			if (!regex_matches(str, "^[0-9]{0,15}$", true)) {
				System.out.println("=====");
			}
		}
		// System.out.println(!is_three_bytes_utf8("𭰦𭰦"));
		String patternMac = "^[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}$";
		System.out.println("**********************");
		// System.out.println(pa.matcher("00:00:00:00:00:00:00:E0").find());

		// System.out.println(Pattern.matches(patternMac,"23:34:3e:5f:33:3d"));
		patternMac = "^\\d{1,10}-?(-)\\d{1,10}-?(-)\\d{1,10}-?(-)\\d{1,10}$";

		System.out.println(Pattern.matches(patternMac, "310-260--1--1"));

	}
}

