/**
 * CheckData.java   1.00    2005/05/19
 * Copyright 2004 KANAMIC . All rights reserved.
 */
package com.rainstars.common.util.tool;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.rainstars.common.BaseBo;
import com.rainstars.common.exception.LogicException;

/**
 * 共通入力チェック
 * 
 * @version 1.00
 * @author YT 2005/05/19 New
 */

public class CheckInput extends BaseBo {

	/**
	 * Function: 郵便番号文字数を取得します <br>
	 * 
	 * @return 郵便番号文字数
	 */
	public static int getZipLength() {
		return (8);
	}

	/**
	 * Function:電話番号文字数を取得します。 <br>
	 * 
	 * @return 電話番号文字数
	 */
	public static int getTelLength() {
		return (20);
	}

	/**
	 * Function: 正規表示式使いて、データをチャックします <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param pattern
	 *            正規表示式
	 * @return
	 * @throws LogicException
	 * @throws LogicException
	 */
	public static boolean checkCommon(String str, boolean needinput, String msgparamID, String pattern) throws LogicException {
		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);

		checkRegexp(str, msgparamID, pattern);

		return true;
	}

	/**
	 * Function: 正規表示式使いて、データをチャックします <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックデータ
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param pattern
	 *            正規表示式
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkRegexp(String str, String msgparamID, String pattern) throws LogicException {

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		if (!m.find()) {
			// エラーメッセージのパラメタＩＤからエラーメッセージのパラメタを取る
			throwInputException(msgparamID);
		}

		return true;
	}

	/**
	 * Function: Emailの入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param tel
	 *            Email
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkEmail(String email, boolean needinput, String msgparamID) throws LogicException {

		// 未入力チェック
		checkEmpty(email, needinput, msgparamID);

		if (email == null || "".equals(email)) {
			return true;
		}

		// email pattern
		String emailPattern = "^[a-zA-Z.0-9_\\-]{1,}@[a-zA-Z0-9_\\-]{1,}\\.[a-zA-Z0-9_\\-.]{1,}$";

		checkRegexp(email, msgparamID, emailPattern);

		return true;
	}

	/**
	 * Function: ASCコードの文字チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkAsc(String str, boolean needinput, String msgparamID) throws LogicException {
		return checkAsc(str,needinput,msgparamID,0);
	}
	
	public static boolean checkAsc(String str, boolean needinput, String msgparamID,int length) throws LogicException {

		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);

		if (str == null || "".equals(str)) {
			return true;
		}
		
		byte[] bstr = str.getBytes();
		
		for (int i=0;i<bstr.length;i++){
			if (bstr[i] < 0){
				throwInputException(msgparamID);
			}
		}
		
		if (length != 0 && str.length() > length){
			// 長さチェック
			// エラーメッセージのパラメタＩＤからエラーメッセージのパラメタを取る

			LogicException LogicException = new LogicException("exception.logic.E00005");
			LogicException.setArgs(new String[]{msgparamID, (str.length() - length) + ""});
			throw LogicException;
		}

		return true;
	}	
	
	/**
	 * Function: 英文と数字の入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkNumberEngString(String str, boolean needinput, String msgparamID) throws LogicException {

		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);

		if (str == null || "".equals(str)) {
			return true;
		}
		
		// str pattern
		String strPattern = "^[A-Za-z0-9]+$";

		checkRegexp(str, msgparamID, strPattern);

		return true;
	}
	
	
	/**
	 * Function: 日本円チェック、 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static String checkJPCurrent(String snum, boolean needinput, String msgparamID, long max, long min, boolean minusflag) throws LogicException {

		// 未入力チェック
		if (snum != null ) {
			snum = snum.replaceAll(",","");
		}
		
		// 未入力チェック
		checkEmpty(snum, needinput, msgparamID);

		if (StringUtils.isEmpty(snum))
			return null;

		// 数字チェック
		if (!minusflag) {
			if (!(ExtNumeric.isNumeric(snum))) {
				throwNumberException(msgparamID);
			}
		} else {
			
			if (!(ExtNumeric.isMinusNumeric(snum))) {
				throwNumberException(msgparamID);
			}
		}

		// 入力最大値チェック
		if (max != 0) {
			if (Long.parseLong(snum) > max) {
				throwInputException(msgparamID);
			}
		}

		// 入力最小値チェック
		if (min != 0) {
			if (Long.parseLong(snum) < min) {
				throwInputException(msgparamID);
			}
		}

		return snum;
	}
	
	
	
	/**
	 * Function: 英文と数字の入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkNumberEngString(String str, boolean needinput, String msgparamID, int length) throws LogicException {

		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);

		if (str == null || "".equals(str)) {
			return true;
		}
		
		// 長さチェック
		if (str.length() > length) {

			LogicException LogicException = new LogicException("exception.logic.E00005");
			LogicException.setArgs(new String[]{msgparamID, (str.length() - length) + ""});
			throw LogicException;
		}
		// str pattern
		String strPattern = "^[A-Za-z0-9]+$";

		checkRegexp(str, msgparamID, strPattern);

		return true;
	}
	
	/**
	 * Function: 電話番号、ＦＡＸ番号入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param tel
	 *            電話番号、ＦＡＸ番号
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkTel(String tel, boolean needinput, String msgparamID) throws LogicException {
		// 未入力チェック
		checkEmpty(tel, needinput, msgparamID);

		if (tel == null || "".equals(tel)) {
			return true;
		}

		// tel pattern
		String telPattern = "^[0-9-]{0," + getTelLength() + "}$";
		checkRegexp(tel, msgparamID, telPattern);

		return true;
	}

	public static String checkTel(String tel0,String tel1,String tel2, boolean needinput, String msgparamID) throws LogicException {
		
		String tel = "";
		
		if (tel0 == null) tel0 = "";
		if (tel1 == null) tel1 = "";
		if (tel2 == null) tel2 = "";
		
	    tel = tel0 + "-" + tel1 + "-" + tel2;
	    
		// 未入力チェック
	    String nohalftel = tel0+ tel1  + tel2;
		checkEmpty(nohalftel, needinput, msgparamID);

		if ("".equals(nohalftel)) {
			return null;
		}

		// tel pattern
		String telPattern = "^[0-9]{1,6}[-]{1}[0-9]{1,6}[-]{1}[0-9]{1,6}$";
		checkRegexp(tel, msgparamID, telPattern);

		return tel;
	}
	
	public static void main(String[] argvs) {
		try {
			System.out.println(CheckInput.checkTel("1234-3222", true, ""));
		} catch (LogicException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function: 郵便番号入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param zip
	 *            郵便番号
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkZip(String zip, boolean needinput, String msgparamID) throws LogicException {
		// 未入力チェック
		checkEmpty(zip, needinput, msgparamID);

		if (zip == null || "".equals(zip)) {
			return true;
		}

		// zip pattern
		String zipPattern = "^[0-9-]{0," + getZipLength() + "}$";
		checkRegexp(zip, msgparamID, zipPattern);

		return true;
	}

	/**
	 * Function: ストリングの入力チャック <br>
	 * チェック点：未入力チェック、長さチェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param length
	 *            長さ
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkString(String str, boolean needinput, String msgparamID, int length) throws LogicException {
		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);
		if (str == null || "".equals(str)) {
			return true;
		}
		// 長さチェック
		if (length(str) > length) {

			LogicException LogicException = new LogicException("exception.logic.E00005");
			LogicException.setArgs(new String[]{msgparamID, (length(str) - length) + ""});
			throw LogicException;
		}

		return true;
	}
	
	
	/**
	 * Function: ストリングの入力チャック <br>
	 * チェック点：未入力チェック、長さチェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param length
	 *            長さ
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkStringByBytes(String str, boolean needinput, String msgparamID, int length) throws LogicException {
		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);
		if (str == null || "".equals(str)) {
			return true;
		}
		// 長さチェック
		if (str.getBytes().length > length) {

			LogicException LogicException = new LogicException("exception.kanamic.E21011");
			LogicException.setArgs(new String[]{msgparamID});
			throw LogicException;
		}

		return true;
	}
	

	/**
	 * Function: ストリングの入力チャック <br>
	 * チェック点：未入力チェック、長さチェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param length
	 *            長さ
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkString(String str, boolean needinput, String param1, String msgparamID, int length) throws LogicException {
		// 未入力チェック
		checkEmpty(str, needinput, msgparamID);

		if (str == null || "".equals(str)) {
			return true;
		}

		// 長さチェック
		if (str.length() > length) {

			LogicException LogicException = new LogicException("exception.logic.E00005");
			LogicException.setArgs(new String[]{msgparamID, (str.length() - length) + ""});
			throw LogicException;
		}

		return true;
	}

	/**
	 * Function: 数字チェック(マイナス、ピリオド可能) <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkDecimal(String snum, boolean needinput, String msgparamID) throws LogicException {

		return checkDecimal(snum, needinput, null, msgparamID, 0, 0);
	}

	/**
	 * Function: 数字チェック(マイナス、ピリオド可能) <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkDecimal(String snum, boolean needinput, String param1, String msgparamID, double max, double min) throws LogicException {
		return checkDecimal(snum,needinput,param1,msgparamID,max,min,0);
	}
	public static boolean checkDecimal(String snum, boolean needinput, String param1, String msgparamID, double max, double min,int folatnum) throws LogicException {
		// 未入力チェック
		checkEmpty(snum, needinput,param1, msgparamID);

		if (snum == null || "".equals(snum)) {
			return true;
		}

		// 小数位数チェック
		if (folatnum != 0){
			// 数字チェック
			if (!(ExtNumeric.isDecimal(snum,folatnum))) {
				// エラーメッセージのパラメタＩＤからエラーメッセージのパラメタを取る

				throwNumberException(msgparamID);
			}
		} else {
			// 数字チェック
			if (!(ExtNumeric.isDecimal(snum))) {
				// エラーメッセージのパラメタＩＤからエラーメッセージのパラメタを取る

				throwNumberException(msgparamID);
			}
		}

		//	入力最大値チェック
		if (max != 0) {
			if (Double.parseDouble(snum) > max) {

				throwInputException(msgparamID);
			}
		}

		// 入力最小値チェック
		if (min != 0) {
			if (Double.parseDouble(snum) < min) {
				
				throwInputException(msgparamID);
			}
		}

		return true;
	}

	/**
	 * Function: 数字チェック(マイナス、ピリオド可能) <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkDecimal(String snum, boolean needinput, String param1, String msgparamID) throws LogicException {
		return checkDecimal(snum, needinput, param1, msgparamID, 0, 0);
	}

	/**
	 * Function: 未入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param str
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 */
	public static boolean checkEmpty(String str, boolean needinput, String msgparamID) throws LogicException {
		// 省略可と入力しない時、チェックＯＫを返却
		if (!needinput && (str == null || "".equals(str))) {
			return true;
		}

		// 省略不可と入力しない時、未入力チェックのエラー情報を出てくる
		if (needinput && (str == null || "".equals(str))) {
			throwEmptyException(msgparamID);
		}
		return true;
	}
	
	/**
	 * Function: 未入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param paramID
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return
	 * @throws LogicException
	 * 
	 * add by u2 on 2009-09-17
	 */
	public static boolean checkEmpty(long[] paramID, boolean needinput, String msgparamID) throws LogicException {
		// 省略可と入力しない時、チェックＯＫを返却
		if (!needinput && (paramID == null || paramID.length == 0)) {
			return true;
		}

		// 省略不可と入力しない時、未入力チェックのエラー情報を出てくる
		if (needinput && (paramID == null || paramID.length == 0)) {
			throwEmptyException(msgparamID);
		}
		return true;
	}

	/**
	 * Function: 未入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return true
	 * @throws LogicException
	 */
	public static boolean checkEmpty(String str, boolean needinput, String param1, String msgparamID) throws LogicException {
		// 省略可と入力しない時、チェックＯＫを返却
		if (!needinput && (str == null || "".equals(str))) {
			return true;
		}

		// 省略不可と入力しない時、未入力チェックのエラー情報を出てくる
		if (needinput && (str == null || "".equals(str))) {
			throwEmptyException(msgparamID);
		}
		return true;
	}
	
	/**
	 * Function: 数字入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return true
	 * @throws LogicException
	 *  
	 */
	public static boolean checkNumber(String snum, boolean needinput, String msgparamID) throws LogicException {
		return checkNumber(snum, needinput, msgparamID, 0, 0);
	}

	/**
	 * Function: マイナス数字入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return true
	 * @throws LogicException
	 *  
	 */
	public static boolean checkMinusNumber(String snum, boolean needinput, String msgparamID) throws LogicException {
		return checkNumber(snum, needinput, msgparamID, 0, 0, true);
	}

	/**
	 * Function: 数字入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return true
	 * @throws LogicException
	 *  
	 */
	public static boolean checkNumber(String snum, boolean needinput, String param1, String msgparamID) throws LogicException {
		return checkNumber(snum, needinput, param1, msgparamID, 0, 0);
	}

	/**
	 * Function: 数字入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param max
	 *            最大の値、0時制限しない
	 * @param min
	 *            最小の値、0時制限しない
	 * @return true
	 * @throws LogicException
	 *  
	 */
	public static boolean checkNumber(String snum, boolean needinput, String msgparamID, long max, long min) throws LogicException {
		return checkNumber(snum, needinput, msgparamID, max, min, false);
	}

	/**
	 * Function: 数字入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param max
	 *            最大の値、0時制限しない
	 * @param min
	 *            最小の値、0時制限しない
	 * @param minusflag
	 *            true: マイナス可 false:マイナス不可
	 * @return true
	 * @throws LogicException
	 *  
	 */
	public static boolean checkNumber(String snum, boolean needinput, String msgparamID, long max, long min, boolean minusflag) throws LogicException {

		// 未入力チェック
		checkEmpty(snum, needinput, msgparamID);

		if (snum == null || "".equals(snum))
			return true;

		// 数字チェック
		if (!minusflag) {
			if (!(ExtNumeric.isNumeric(snum))) {
				throwNumberException(msgparamID);
			}
		} else {
			if (!(ExtNumeric.isMinusNumeric(snum))) {
				throwNumberException(msgparamID);
			}
		}

		// 入力最大値チェック
		if (max != 0) {
			if (Long.parseLong(snum) > max) {
				throwInputException(msgparamID);
			}
		}

		// 入力最小値チェック
		if (min != 0) {
			if (Long.parseLong(snum) < min) {
				throwInputException(msgparamID);
			}
		}

		return true;
	}

	/**
	 * Function: 数字入力チェック <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param snum
	 *            チェックのデータ
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @param max
	 *            最大の値、0時制限しない
	 * @param min
	 *            最小の値、0時制限しない
	 * @return true
	 * @throws LogicException
	 *  
	 */
	public static boolean checkNumber(String snum, boolean needinput, String param1, String msgparamID, long max, long min) throws LogicException {

		// 未入力チェック
		checkEmpty(snum, needinput, msgparamID);

		if (snum == null || "".equals(snum))
			return true;

		// 数字チェック
		if (!(ExtNumeric.isNumeric(snum))) {
			throwNumberException(msgparamID);
		}

		// 入力最大値チェック
		if (max != 0) {
			if (Long.parseLong(snum) > max) {
				throwInputException(msgparamID);
			}
		}

		// 入力最小値チェック
		if (min != 0) {
			if (Long.parseLong(snum) < min) {
				throwInputException(msgparamID);
			}
		}

		return true;
	}


	/**
	 * Function: 西暦の入力チェック、正確入力の場合、日付を返却、 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param dateA
	 *            [0] 年 [1] 月 [2] 日
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return Date Object 日付
	 * @throws LogicException
	 */
	public static Date checkDateArray(String[] dateA, boolean needinput, String msgparamID) throws LogicException {

		// 省略可と入力しない時、チェックＯＫを返却
		if (!(needinput) && (dateA[0] == null || "".equals(dateA[0])) && (dateA[1] == null || "".equals(dateA[1])) && (dateA[2] == null || "".equals(dateA[2]))) {
			return null;
		}

		// 省略不可と入力しない時、未入力チェックのエラー情報を出てくる
		if ((needinput) && (dateA[0] == null || "".equals(dateA[0])) && (dateA[1] == null || "".equals(dateA[1])) && (dateA[2] == null || "".equals(dateA[2]))) {
			throwEmptyException(msgparamID);
		}

		// 入力チェック
		if (dateA[0] != null)
			dateA[0] = dateA[0].trim(); // 年
		if (dateA[1] != null)
			dateA[1] = dateA[1].trim(); // 月
		if (dateA[2] != null)
			dateA[2] = dateA[2].trim(); // 日

		int cnt = dateA.length;
		for (int i = 0; i < cnt; i++) {
			boolean ret = ExtNumeric.isNumeric(dateA[i]);
			if (!ret) {
				// 数字以外のデータ入力場合、エラー情報を出てくる
				throwNumberException(msgparamID);
			}
		}

		// 日付チェック 月・日・年
		// boolean ret = ExtDate.isDate(dateA[1], dateA[2], dateA[0]);
		boolean ret = ExtDate.isDate(dateA[1], dateA[2], dateA[0]);
		if (!ret) {
			// 不正確の日付を入力、エラー情報を出てくる、例えば：2005年2月30日
			throwDateException(msgparamID);
		}

		if (dateA[1].length() == 1) {
			dateA[1] = "0" + dateA[1];
		}

		if (dateA[2].length() == 1) {
			dateA[2] = "0" + dateA[2];
		}

		// DateObjectを返却
		return DateUtil.getDate(DateUtil.YYYYMMDD, dateA[0] + dateA[1] + dateA[2]);

	}

	
	/**
	 * Function: 西暦の入力チェック、正確入力の場合、日付を返却、 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param dateA
	 *            [0] 年 [1] 月 [2] 日
	 * @param needinput
	 *            true: 入力必要です false: 省略可
	 * @param msg
	 *            エラーメッセージのパラメタＩＤ（チェックの物の名前ＩＤ）
	 * @return Date Object 日付
	 * @throws LogicException
	 */
	public static Date checkDate(String sdate, boolean needinput, String msgparamID) throws LogicException {

		// 省略可と入力しない時、チェックＯＫを返却
		if (!(needinput) && (sdate == null || "".equals(sdate)) ) {
			return null;
		}

		// 省略不可と入力しない時、未入力チェックのエラー情報を出てくる
		if ((needinput) && (sdate == null || "".equals(sdate))) {
			throwEmptyException(msgparamID);
		}
		// Modify By YEHOOHAHA 2008-4-2
		boolean ret = false;
		if(ExtNumeric.isNumeric(DoText.replace(sdate,"/","")) || ExtNumeric.isNumeric(DoText.replace(sdate,"-",""))){
			ret = true;
		}
		if (!ret) {
			// 数字以外のデータ入力場合、エラー情報を出てくる
			throwNumberException(msgparamID);
		}
		

		// 日付チェック 月・日・年
		// Modify By YEHOOHAHA 2008-4-2
		String[] dateA = new String[sdate.length()];
		if(DoText.split(sdate,"/").length == 3){
			dateA = DoText.split(sdate,"/");
		}
		else if(DoText.split(sdate,"-").length == 3){
			dateA = DoText.split(sdate,"-");
		}
			
			
		if (dateA.length != 3){
			//	不正確の日付を入力、エラー情報を出てくる、例えば：2005年2月30日
			throwDateException(msgparamID);
		}
		ret = ExtDate.isDate(dateA[1], dateA[2], dateA[0]);
		if (!ret) {
			// 不正確の日付を入力、エラー情報を出てくる、例えば：2005年2月30日
			throwDateException(msgparamID);
		}

	

		// DateObjectを返却
		return DateUtil.getDate(DateUtil.YYYYMMDD, dateA[0] + dateA[1] + dateA[2]); 


	}
	
	
	/**
	 * Function: 未入力エラー定義 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param msgParam
	 * @throws LogicException
	 */
	public static void throwEmptyException(String msgParam) throws LogicException {
		// 未入力チェックのエラー情報を出てくる
		LogicException LogicException = new LogicException("exception.logic.E00002");
		LogicException.setArgs(new String[]{msgParam});
		throw LogicException;
	}

	/**
	 * Function: 不正数字入力エラー定義 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param msgParam
	 * @throws LogicException
	 */
	public static void throwNumberException(String msgParam) throws LogicException {
		// 数字以外のデータ入力場合、エラー情報を出てくる
		LogicException LogicException = new LogicException("exception.logic.E00003");
		LogicException.setArgs(new String[]{msgParam});
		throw LogicException;
	}

	/**
	 * Function: 不正日付入力エラー定義 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param msgParam
	 * @throws LogicException
	 */
	public static void throwDateException(String msgParam) throws LogicException {
		// 不正日付入力場合、エラー情報を出てくる
		LogicException LogicException = new LogicException("exception.logic.E00004");
		LogicException.setArgs(new String[]{msgParam});
		throw LogicException;
	}

	/**
	 * Function: 不正入力エラー定義 <br>
	 * 
	 * Produce Describe:
	 * 
	 * @param msgParam
	 * @throws LogicException
	 */
	public static void throwInputException(String msgParam) throws LogicException {
		// 正しく入力されていません
		LogicException LogicException = new LogicException("exception.logic.E00001");
		LogicException.setArgs(new String[]{msgParam});
		throw LogicException;
	}
	
	/**
	 * 
	 * 功能描述：验证字符串（包括最小值定义，最大值定义）
	 * @param str	数据
	 * @param needinput	是否要输入
	 * @param msgparamID	异常显示数据
	 * @param minLength 字符串最小长度
	 * @param maxLength	字符串的最大长度
	 * @throws LogicException
	 */
	public static boolean checkString(String str, boolean needinput, String msgparamID,int minLength, int maxLength) throws LogicException {
		checkString(str, needinput, msgparamID, maxLength);
		
		if (length(str) < minLength) {
			LogicException LogicException = new LogicException("exception.logic.E00006");
			LogicException.setArgs(new String[]{msgparamID});
			throw LogicException;
		}
		return true;
		
	}
	
	/**
	 * 
	 * function: checkMagnitudeOfInteger
	 * 
	 * @param beginTime
	 * @param endTime
	 * @param msgparamID
	 * @return
	 * 
	 * @author SuYanwei Jan 19, 2010 4:46:13 PM
	 */
	public static boolean checkMagnitudeOfInteger(String beginTime, String endTime, String msgparamID) {

		if (Integer.parseInt(beginTime) > Integer.parseInt(endTime)) {
			
			LogicException LogicException = new LogicException("exception.logic.E00007");
			LogicException.setArgs(new String[]{msgparamID});
			throw LogicException;
		}
		return true;
	}
	
	public static int length(String s) {
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}
	
	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	/**
	 * 
	 * Function: 验证非法字符串
	 * 
	 * @param s
	 * @return
	 * 
	 * @author AC Feb 21, 2010 3:33:24 AM
	 */
	public static boolean hasSpecificLetters(String s){
		
		boolean flag = true;

		Pattern pattern = Pattern.compile("[!@#$%^&*()/<>]");
		Matcher matcher = pattern.matcher(s);
		
		while (matcher.find()) {
			flag = false;
			
			throw new LogicException("logic.courseflow.processdefinition.specificletter");
			
		}
		
		return flag;
	}
	
	public static boolean hasAddTask(long[] s){
		
		boolean flag = false;
		
		if(s!=null&&s[0]!=0){
			
			flag = true;
			
			throw new LogicException("logic.exercise.taskList.isNull");
		}
		
		return flag;
		
		
	}
}
