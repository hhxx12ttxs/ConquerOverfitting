/**
 * ExtNumeric.java   1.00    2004/03/10
 * Sinyee Framework.
 * Copyright 2004-2006 SINYEE I.T. Co., Ltd. All rights reserved.
 * @author SINYEE I.T. Co., Ltd.
 */
package com.rainstars.common.util.tool;

import java.math.BigDecimal;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Function	:  拡張数字編集クラス
 *
 * @version 1.00
 * @author  Tayu      2004/03/10  New
 */

public class ExtNumeric {


	/**
		* constructor function
		* Init class
		*/
	public ExtNumeric() {
	}

	/**
	 * Function:数字長さは足らない場合、後ろに０が入る<br>
	 * @param aInsurantCode
	 * @param n
	 * @return
	 */
	public static String formatStringBack(String aInsurantCode, int n){
		if (aInsurantCode == null)
					aInsurantCode = "";

		aInsurantCode = aInsurantCode.trim();



		int strlen = aInsurantCode.length();
		for (int i = strlen; i < n; i++) {
				aInsurantCode =  aInsurantCode + "0";
		}
		
		return (aInsurantCode);
	
	}


	/**
	 * Function:0を削除
	 * 例えば：13年02月03日
	 * 02月の0をdelete
	 * 03日の0をdelete<br>
	 * @param num
	 * @return
	 */
	public static String del0(String num) {
		if ("".equals(num) || num == null)
			return "";

		int i = 0;
		while (num.substring(i, i + 1).equals("0")) {
			i++;
			if (i == num.length())
				return "";
		}

		return num.substring(i);
	}
	
	public static String del0(long num) {
		if (num == 0)
			return "";

		return String.valueOf(num);
	}

    /**
     * Function:0を追加
     * 例えば：1
     * 01<br>
     * @param num
     * @return
     */   
    public static String extend0(String num) {
        if ("".equals(num) || num == null)
            return "";
        
        if(num.length() == 1)
            num = "0" + num;
        
        return num;
    }
	
	/**
	 * Function:0を削除
	 * 例えば：13年02月03日
	 * 02月の0をdelete
	 * 03日の0をdelete<br>
	 * 0は0を返却
	 * @param num
	 * @return
	 */
	public static String del0Ex(String num) {
		if ("".equals(num) || num == null)
			return "";

		int i = 0;
		while (num.substring(i, i + 1).equals("0")) {
			i++;
			if (i == num.length())
				return "0";
		}

		return num.substring(i);
	}
	
	/**
	 * Function:数字ゼロ埋め処理 Move to ExtNumber<br>
	 * @param num		数字
	 * @param digit		桁数
	 * @return
	 */
	public static String zeroFormat(String num, int digit) {
		String strnum = "";

		for (int i = (num + "").length(); i < digit; i++) {
			strnum = "0" + strnum;
		}
		strnum = strnum + num;
		return (strnum);
	}
	
	/**
	 * Function:数字ゼロ埋め処理 Move to ExtNumber<br>
	 * @param num		数字
	 * @param digit		桁数
	 * @return
	 */
	public static String zeroFormatConst(String num, int digit) {
		String strnum = "";
		
		if (num== null) num = "";
		
		if (num.length()>=4) return num.substring(0,4);

		for (int i = (num + "").length(); i < digit; i++) {
			strnum = "0" + strnum;
		}
		strnum = strnum + num;
		return (strnum);
	}
	


//modified by Ura 2004/07/05 様式２被保険者番号が10位不足場合の処理は正しいでしょうか
    /**
     * 数字ゼロ埋め処理
     * @param num
     * @param digit
     * @return
     */
    public static String zeroAdd(String num, int digit) {
        String addstr = "";
        for (int i = 0;i < digit - (num + "").length(); i++) {
            addstr += "0";
        }
        return (num + addstr);
    }

	/**
	 * Function:コンマ数字を数字だけに変換<br>
	 * @param strnum	数字
	 * @return
	 */
	public static String numFormat(String strnum) {
		strnum = DoText.replace(strnum, ",", "");
		return strnum;
	}


	/**
	 * Function:数字チェック<br>
	 * @param num	数字
	 * @return	true.数字; false.数字ではない
	 */
	public static boolean isNumeric(String num) {
		Pattern p = Pattern.compile("^[0-9]+$");
		Matcher m = p.matcher(num);
		return m.matches();
	}

	/**
	 * Function:数字チェック<br>
	 * @param num	数字
	 * @return	true.数字; false.数字ではない
	 */
	public static boolean isMinusNumeric(String num) {
		Pattern p = Pattern.compile("^[-]{0,1}[0-9]+$");
		Matcher m = p.matcher(num);
		return m.matches();
	}

	/**
	 * Function:数字チェック(マイナス,ピリオド可能)<br>
	 * @param num	数字
	 * @return	true.数字; false.数字ではない
	 */
	public static boolean isDecimal(String num) {
		Pattern p = Pattern.compile("^[-]?[0-9]*[.]?[0-9]+");
		Matcher m = p.matcher(num);
		return m.matches();
	}

	/**
	 * Function:数字チェック(マイナス,ピリオド可能)<br>
	 * @param num	数字
	 * @param floatnum	小数位数
	 * @return	true.数字; false.数字ではない
	 */
	public static boolean isDecimal(String num,int floatnum) {
		Pattern p = Pattern.compile("^[-]?[0-9]*[.]?[0-9]{0," + floatnum + "}");
		Matcher m = p.matcher(num);
		return m.matches();
	}

	/**
	 * Function:切り捨てを行います
	 * ※標準の floorでどうしても誤差がでてしまうので独自に実装しています
	 * それともコンパイルミス？<br>
	 * 	Modify By Tayu on 2004/07/15,to do with the value such as 0.9999999999999999999998
	 * @param aValue	対象数値
	 * @return	数値
	 */
	public static int floorEx(double aValue) {
		if (aValue>0) aValue = aValue + 0.000000001;
		
		return (int) aValue;
	}

	public static int floorEx(float aValue) {
		double rtnValue = aValue;
		if (rtnValue>0) rtnValue = rtnValue + 0.000000001;
		return (int) rtnValue;
		
	}


	/**
	 * Function:切り上げを行います<br>
	 * ※標準の ceilでどうしても誤差がでてしまうので独自に実装しています
	 * それともコンパイルミス？
	 * @param aValue	対象数値
	 * @return	数値
	 */
	public static long ceilEx(float aValue) {
		long returnValue = 0;
		String[] num = DoText.split(aValue + "", ".");
		if (num.length == 2) {
			if (aValue > 0) {
				returnValue = Long.parseLong(num[0]) + 1;
			} else {
				returnValue = Long.parseLong(num[0]);
			}
		} else {
			returnValue = Long.parseLong(num[0]);
		}
		return returnValue;
	}

	public static long ceilEx(double aValue) {
		long returnValue = 0;
		String[] num = DoText.split(aValue + "", ".");
		if (num.length == 2) {
			if (aValue > 0) {
				returnValue = Long.parseLong(num[0]) + 1;
			} else {
				returnValue = Long.parseLong(num[0]);
			}
		} else {
			returnValue = Long.parseLong(num[0]);
		}
		return returnValue;
	}

	/**
	 * Function: 四捨五入を行います
	 * ※標準の roundの説明で浮動小数点以下の精度がどうのこうのと書いてあって
	 * 心配なので独自に実装しています<br>
	 * @param aValue	対象数値
	 * @return	数値
	 */
	public static long roundEx(float aValue) {
		return Math.round(aValue);
	}
	
	public static long roundEx(double aValue) {
		return Math.round(aValue);
	}

	/**
	 * Function: ＪＡＶＡの小数DIV,少数切<br>
	 * 
	 * Produce Describe:
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static long div(long a1,long a2){
	    long rtn = (new BigDecimal(a1+"")).divide(new BigDecimal(a2+""),BigDecimal.ROUND_DOWN).longValue();
	    return rtn;
	}
	
	/**
	 * Function: ＪＡＶＡの小数DIV,少数切<br>
	 * 
	 * Produce Describe:
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double div(double a1,double a2){
		double rtn = (new BigDecimal(a1+"")).divide(new BigDecimal(a2+""),BigDecimal.ROUND_DOWN).doubleValue();
	    return rtn;
	}

	/**
	 * Function:	ＪＡＶＡの小数かけるエラー修正	<br>
	 * 
	 * Produce Describe:
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double multi(double a1,double a2){
		double rtn =  (new BigDecimal(a1 + "").multiply(new BigDecimal(a2 + ""))).doubleValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}
	

	public static double multi(double a1,float a2){
		double rtn =  (new BigDecimal(a1 + "").multiply(new BigDecimal(a2 + ""))).doubleValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}
	
	

	public static double multi(float a1,double a2){
		double rtn =  (new BigDecimal(a1 ).multiply(new BigDecimal(a2+""))).doubleValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}
	
	

	public static double multi(float a1,float a2){
		double rtn = (new BigDecimal(a1 ).multiply(new BigDecimal(a2 + ""))).doubleValue();
		//compareValue(a1*a2,rtn);
		return  rtn;
	}
	
	
	public static double multi(long a1,float a2){
		double rtn =  (new BigDecimal(a1 ).multiply(new BigDecimal(a2 + ""))).doubleValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}
	

	public static double multi(float a1,long a2){
		double rtn = (new BigDecimal(a1 + "" ).multiply(new BigDecimal(a2))).doubleValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}
	
	public static double multi(long a1,double a2){
		double rtn = (new BigDecimal(a1 ).multiply(new BigDecimal(a2 + ""))).doubleValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}
	
	public static double multi(double a1,long a2){
		double rtn = (new BigDecimal(a1 + "").multiply(new BigDecimal(a2))).doubleValue();
		//compareValue(a1*a2,rtn);
		return  rtn;
	}
	
	
	public static void compareValue(double a1,double a2){
		if (((long)a1 - (long) a2) != 0){
			//System.out.println("WARN::::::::THERE SOMETHING WRONG WITH THIS USER");
		}
	}
    
    /**
     * Formater money to ###.###
     * 
     * eg:123456789--->123,456,789
     * 
     * @param num
     */
    public static String formatMoney(long num) {
        return DoNumber.formatCurrent(num);
    }
    
    
	/**
	 * Function: 指定位数のランダム数字を取る<br>
	 * 
	 * Produce Describe:
	 * 
	 * @param length
	 * @return
	 */
	public static String getRand(int length){
		Random rand = new Random();
		String tmpnum = String.valueOf(rand.nextLong());
		
		if (tmpnum.length() >= length){
			return tmpnum.substring(0,length);
		} else {
			return zeroAdd(tmpnum,length);
		}
	
	}
	
	/**
	 * Function:	ＪＡＶＡの小数かけるエラー修正	<br>
	 * 
	 * Produce Describe:
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static float munius(float a1,float a2){
		float rtn =  (new BigDecimal(a1 + "").add(new BigDecimal("-" + a2 + ""))).floatValue();
		//compareValue(a1*a2,rtn);
		return rtn;
	}

}

