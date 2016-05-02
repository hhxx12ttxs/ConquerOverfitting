/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.guzz.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
//import java.text.SimpleDateFormat;
import java.util.* ;



/**
 * ???
 * 
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class ViewFormat {	

	public static final ViewFormat instance = new ViewFormat() ;
	
	/**
	 * ?????????????????<br>
	 * ????????"?"??":", ?"o"??"0"
	 * @see #seconds2TimeSeg(int)
	 * @return ?????????????????-1?
	 */
	public static int timeSeg2Seconds(String seg){
		if(seg == null ||seg.length() < 5) return -1 ;
		
		seg = StringUtil.replaceString(seg, "?", ":") ;
		seg = StringUtil.replaceString(seg, "o", "0") ;
		String[] segs = StringUtil.splitString(seg, ":") ;
		
		try{
			int hour = new Integer(segs[0]).intValue() ;
			int min = new Integer(segs[1]).intValue() ;
			int sec = new Integer(segs[2]).intValue() ;
			return hour*3600 + min* 60 + sec ;
		}catch(Exception e){
			//e.printStackTrace() ;
			return -1 ;
		}		
	}
	
	/**???????????????????????html?*/
	public String toDisplayHtml(String content, String type){
		content = StringUtil.replaceString(content, "\r\n", "<br/>") ;
		content = StringUtil.replaceString(content, "\n", "<br/>") ;
		content = StringUtil.replaceString(content, "\r", "<br/>") ;
		
//		String c = ubbParser.parse(content) ;
		
		return StringUtil.replaceString(content, " ", "&nbsp;") ;
		
//		return c ;
	}
	
	/**
	 * timeSeg2Seconds?????
	 * @see #timeSeg2Seconds(String)
	 * @return ??????<=0??? ""
	 */
	public static String seconds2TimeSeg(int sec){
		if(sec <=0) return "00:00:00" ;
		
		int ms = sec%60 ;
		int mm = ((sec - ms)%3600)/60 ;
		int mh = (sec - sec%3600)/3600 ;
		
		StringBuffer sb = new StringBuffer() ;
		if(mh == 0){
			sb.append("00") ;
		}else if(mh <10){
			sb.append('0') ;
			sb.append(mh) ;
		}else{
			sb.append(mh) ;
		}
		
		sb.append(':') ;
		
		if(mm == 0){
			sb.append("00") ;
		}else if(mm <10){
			sb.append('0') ;
			sb.append(mm) ;
		}else{
			sb.append(mm) ;
		}
		
		sb.append(':') ;
		
		if(ms == 0){
			sb.append("00") ;
		}else if(ms < 10){
			sb.append('0') ;
			sb.append(ms) ;
		}else{
			sb.append(ms) ;
		}
		
		return sb.toString() ;
	}
	
	/**
	 * ?????????? "245?3??34?7?" ??????
	 * @return ????????????????<=0??? "0?" ?
	 */
	public static String seconds2String(int sec){
		if(sec <= 0) return "0?" ;
		
		int left = sec%86400 ;
		int day = (sec - left)/86400 ;
		
		sec = left ;
		int hour = sec/3600 ;
		int min  = (sec - hour*3600)/60 ;
		int second = sec - hour*3600 - min*60 ;
		
		String tf = "" ;
		if(day > 0){
			tf = tf + day + "?" ;
		}
		if(hour > 0 ){
			tf = tf + hour + "??" ;
		}
		if(min > 0){
			if(second > 0){
				tf = tf + min + "?" ;
			}else{
				tf = tf + min + "??" ;
			}
		}
		if(second > 0){
			tf = tf + second + "?" ;
		}		
		
		return tf ;
	}
	
	public static String formatDate(Date d){
		if(d == null) return "" ;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm") ;
		return fmt.format(d) ;
	}
	
	/**
	 * ????1?????????	
	 * @return ???????
	 */
	public static String formatDate(String regx, Date d){
		if(regx == null || d == null) return "" ;
		SimpleDateFormat fmt = new SimpleDateFormat(regx) ;
		return fmt.format(d) ;
	}
			
	public static String formatFileLength(int bytes){
		return formatFileLength((long) bytes) ;
	}
	
	/**
	 * ?byte????? "32.7Mb", "24.56Kb", "234??" ??
	 */
	public static String formatFileLength(long bytes){
		if(bytes <= 0) return "0??" ;
		
		double gb = (bytes)/1073741824.0 ;		
		double mb = bytes/1048576.0 ;
		double kb  = bytes/1024.0 ;
		
		NumberFormat fm = NumberFormat.getInstance() ;
		fm.setMaximumFractionDigits(2) ;
		
		if(gb >= 1){
			return fm.format(gb) + "GB" ;
		}else if(mb >= 1){
			return fm.format(mb) + "MB" ;
		}else if(kb >= 1){
			return fm.format(kb) + "KB" ;
		}
		
		return fm.format(bytes) + "??" ;
	}
	
	/**????????*/
	public static Date getCurrentTime(){
		return Calendar.getInstance().getTime() ;
	}
	
	/**
	 * ??????keywords???????????.
	 * ???????null?????0???null?
	 */
	public static String reassembleKeywords(String keywords){
		
		if(StringUtil.isEmpty(keywords)){
    		return null;
    	}
		
		keywords = keywords.trim() ;
		
		keywords = keywords.replace(',', ' ') ;
		keywords = keywords.replace('?', ' ') ;
		keywords = keywords.replace('?', ' ') ;
		keywords = StringUtil.squeezeWhiteSpace(keywords) ;
    	
		keywords = keywords.replace(' ', ';') ;
    	
    	return keywords ;
	}
	
	/**
	 * ???????????????????
	 * ??????null??????0????
	 */
	public static String[] splitKeywords(String keywords){
		if(keywords == null) return new String[0] ;
				
		String[] words = StringUtil.splitString(keywords, ";") ;
		
		for(int i = 0 ; i < words.length ; i++ ){
			words[i] = words[i].trim() ;
		}
		
		return words ;
	}
			
	/**
	 * ???????????????????????????trim???
	 * ??????null??????0????
	 */
	public static String[] reassembleAndSplitKeywords(String keywords){
		if(keywords == null) return new String[0] ;
		
		keywords = reassembleKeywords(keywords) ;
		
		String[] words = StringUtil.splitString(keywords, ";") ;
		
		for(int i = 0 ; i < words.length ; i++ ){
			words[i] = words[i].trim() ;
		}
		
		return words ;
	}
	
	/**
	 * ??????????????????????????????????
	 * ??????null??????0????
	 */
//	public static String[] inputToNoEmptyValueArray(String keywords){
//		if(keywords == null) return new String[0] ;
//				
//		String[] words = reassembleAndSplitKeywords(keywords) ;
//		String[] mw = new String[0] ;
//		for(int i = 0 ; i < words.length ; i++ ){
//			if(StringUtil.notEmpty(words[i])){
//				mw = (String[]) ArrayUtils.add(mw, words[i].trim()) ;
//			}
//		}
//		
//		return mw ;
//	}
	
	/**
	 * ????????????<br>
	 * ????1???2???"book"????????????1???2?"book"???
	 * ????????????????????????
	 * <b>???????????????""</b>
	 * 
	 * 
	 * @param array1 ??1
	 * @param array2 ??2
	 * @param ignoreCase ???????
	 * @return ???????????????????""?????????????????
	 */
	public static boolean removeDuplicateKeywords(String[] array1, String[] array2, boolean ignoreCase){
		boolean modified = false ;
		
		if(array1 == null || array2 == null) return modified ;
		if(array1.length == 0 || array2.length == 0) return modified ;
				
		for(int i = 0 ; i < array1.length ; i++){
			String word = array1[i] ;
			if(word == null) continue ;
			
			for(int j = 0 ; j < array2.length ; j++){
				if(ignoreCase){
					if(word.equalsIgnoreCase(array2[j]) && word.length() > 0){
						array1[i] = "" ;
						array2[j] = "" ;
						modified = true ;
						//break ;
					}
				}else{
					if(word.equals(array2[j]) && word.length() > 0){
						array1[i] = "" ;
						array2[j] = "" ;
						modified = true ;
						//break ;
					}
				}
			}
		}
		
		return modified ;
	}
	
//	public static void  main(String[] args){
//		System.out.println(formatFileLength(2142208)) ;
//		System.out.println(formatFileLength(11870208)) ;
//		System.out.println(formatFileLength(1)) ;
//		System.out.println(formatFileLength(1218422352)) ;
//	}

}

