/**
 * DoNumber.java   1.00    2004/01/14
 *
 * Sinyee Framework.
 * Copyright 2004-2006 SINYEE I.T. Co., Ltd. All rights reserved.
 * @author SINYEE I.T. Co., Ltd.
 *
 * History:
 */
package com.rainstars.common.util.tool;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;


/**
 * Class Function	:	DoNumber.java
 * @author 		:	TAYU
 * History		
 * 		1.0.0	2003/09/05		TangGuangYu		Create
 */
public class DoNumber {
	
	/**
	 * Function Description: intNullDowith
	 * @author: TangGuangYu
	 * @param str
	 * @return
	 * @version: 
	 *      Create: 2003/09/05    TangGuangYu
	 */
	public static int intNullDowith(String str){
		
		if (str == null){
			return 0;
		}else if (str.trim().equals("")){
			return 0;
		}else{
			return Integer.parseInt(str);
		}
		
	}
	
	/**
	 * Function Description: intNullDowith
	 * @author: TangGuangYu
	 * @param str
	 * @return
	 * @version: 
	 *      Create: 2003/09/05    TangGuangYu
	 */
	public static int intNullDowith(Long l){
		
		if (l == null){
			return 0;
		}else{
			return l.intValue();
		}
		
	}
	
	
	/**
	 * Function Description: intNullDowith
	 * @author: TangGuangYu
	 * @param str
	 * @return
	 * @version: 
	 *      Create: 2003/09/05    TangGuangYu
	 */
	public static int intNullDowith(Integer i){
		
		if (i == null){
			return 0;
		}else{
			return i.intValue();
		}
		
	}
	
	/**
	 * Function Description: dblNullDowith
	 * @author: TangGuangYu
	 * @param str
	 * @return
	 * @version: 
	 *      Create: 2003/09/05    TangGuangYu
	 */
	public static double dblNullDowith(String str){
		if (str == null){
			return 0;
		}else if (str.trim().equals("")){
			return 0;
		}else{
			return Double.parseDouble(str);
		}
	}
	
	/**
	 * Function Description: dblNullDowith
	 * @author: TangGuangYu
	 * @param str
	 * @return
	 * @version: 
	 *      Create: 2003/09/05    TangGuangYu
	 */
	public static long lngNullDowith(String str){
		if (str == null){
			return 0;
		}else if (str.trim().equals("")){
			return 0;
		}else{
			return Long.parseLong(str);
		}
	}
	
	public static long lngNullDowith(Long l){
		if (l == null){
			return 0;
		} else{
			return l.longValue();
		}
	}
	
	public static long lngNullDowith(Integer i){
		if (i == null){
			return 0;
		} else{
			return i.longValue();
		}
	}
	
	
	/**
	 * Function Description: srtNullDowith
	 * @author: TangGuangYu
	 * @param str
	 * @return
	 * @version: 
	 *      Create: 2003/09/05    TangGuangYu
	 */
	public static short srtNullDowith(String str){
		if (str == null){
			return 0;
		}else if (str.trim().equals("")){
			return 0;
		}else{
			return Short.parseShort(str);
		}	
	}
	
	public static String formatCurrent(Long num){
	    if (num == null) return "";
	    return formatCurrent(num.longValue());
	}
	
	public static String formatCurrent(long num){
		NumberFormat numberformat = NumberFormat.getCurrencyInstance(Locale.JAPAN);
        DecimalFormat decimalformat = (DecimalFormat) numberformat;
        decimalformat.setDecimalSeparatorAlwaysShown(true);
        String s = "###,###";
        decimalformat.applyPattern(s);
        String result = decimalformat.format(num);
        return result;
		
	}
	
	/**
	 * Function: お金のフォーマット表示する<br>
	 * 				例えば：	10  	--> 10
	 * 						1000 	--> 1,000
	 * 						1000000 --> 1,000,000
	 * 
	 * Produce Describe:
	 * 
	 * @param strInt
	 * @return
	 */
	public static String formatCurrent(String strInt){
	    if (strInt == null || "".equals(strInt)){
	        return "";
	    }
	    

	    int strLen 		= strInt.length();
	    String rtnStr 	= "";
	    int rightSite = 0;
	    for (int i=strLen  ;i>1;i--){
	        // System.out.println(i);
	        // System.out.println(i - ((int) (i/3)) * 3);
	        rightSite = strLen - i;
	        if (( rightSite + 1 - ((int) ((rightSite + 1)/3)) * 3) == 0){
	            
	            rtnStr =  strInt.substring(i-1,i) + rtnStr;
	            rtnStr =  "," + rtnStr ;
	            
	        } else {
	            rtnStr =  strInt.substring(i-1,i) + rtnStr;
	        }
	    }
	    
	    rtnStr =  strInt.substring(0,1) + rtnStr; 
	    
	    
	    return rtnStr;
	}
	
	/**
	 * 
	 * function:Long 转 String
	 *
	 * @param num
	 * @return
	 * 
	 * author:DBA Sep 17, 2009 11:15:48 AM
	 */
	public static String lngNullDowithEx(Long num){
			
		if(num==null||num==0){
			
			return "";
		
		}else {
			
			return num+"";
		}
	}
	
	/**
	 * 
	 * function:Integer 转 String
	 *
	 * @param num
	 * @return
	 * 
	 * author:DBA Sep 17, 2009 11:15:48 AM
	 */
	public static String intNullDowithEx(Integer num){
			
		if(num==null||num==0){
			
			return "";
		
		}else {
			
			return num+"";
		}
	}
	
	/**
	 * function: 获取范围内随机数
	 * 
	 * @param max
	 * @param min
	 * @return
	 * 
	 * @author Benjamin 2008-11-27 下午01:34:14
	 */
	public static int getRandom(int min, int max){
		
		Random r=new Random();
		
		int m = min+r.nextInt(max-min);
		
		return m;

	}
	
	/**
	 * Function:	<br>
	 * 
	 * Produce Describe:
	 * 
	 * @param argvs
	 */
	public static void main(String[] argvs){
	    System.out.println("=================== TEST BEGIN =====================");
	   
	    System.out.println(DoNumber.formatCurrent(""));
	    System.out.println(DoNumber.formatCurrent("0"));
	    System.out.println(DoNumber.formatCurrent("1"));
	    System.out.println(DoNumber.formatCurrent("12"));
	    System.out.println(DoNumber.formatCurrent("123"));
	    System.out.println(DoNumber.formatCurrent("1234"));
	    System.out.println(DoNumber.formatCurrent("12345"));
	    System.out.println(DoNumber.formatCurrent("123456"));
	    System.out.println(DoNumber.formatCurrent("1234567"));
	    
	    System.out.println(DoNumber.lngNullDowithEx(0L));

	}
}

