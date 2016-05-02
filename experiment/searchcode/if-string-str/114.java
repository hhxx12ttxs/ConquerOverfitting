/**  
* @(#) KeyWordChecker.Java 1.00 2012/05/22  
*  
* Copyright (c) 2012 清华大学自动化系 Bigeye 实验室版权所有  
* Department of Automation, Tsinghua University. All rights reserved.
* 
* @author 宋成儒   
*    
* This software aims to extract title, time, source and text content 
* from news webpages. We grab news webpages from Baidu Rss and stored 
* them in Mysql database. To support the web demo of this project, we
* also provide with interfaces for web communication .  
*/ 
package com.Frank;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** KeyWordChecker类提供了检查字符串内容的诸多方法，
 * 包括字符串是否是时间格式、是否是数字或单词等 */
public class KeyWordChecker {
	/** 停用词集合 */
	public Set<String> uselessSet = new HashSet<String>();
	/** 媒体相关词集合*/
	public Set<String> mediaSet = new HashSet<String>();
	
	/** 生成函数 
	 * @param uselesspath 停用词文件路径
	 * @param mediapath 媒体词文件路径 */
	public KeyWordChecker(String uselesspath, String mediapath)
	{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(uselesspath))); 
			String tempstr = "";
			tempstr=reader.readLine();
			while((tempstr=reader.readLine()) != null)
				if(!tempstr.startsWith("#"))
					uselessSet.add(tempstr.trim());
			reader.close();
			
			reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(mediapath)));
			tempstr=reader.readLine();
			while((tempstr=reader.readLine()) != null)
				if(!tempstr.startsWith("#"))
					mediaSet.add(tempstr.trim());
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 判断字符串是否为英文单词 
	 * @param str 待判断的字符串
	 * @return 返回true表示是英文单词 */
	public boolean isWord(String str)
	{
		return str.matches("[a-zA-Z]+");
	}
	
	/** 判断字符串是否为日期词
	 * @param str 待判断的字符串
	 * @return 返回true表示是日期词 */
	public boolean isDate(String str)
	{
		return str.matches("[0-9]+[日月年]");
	}
	
	/** 判断字符串是否为数字
	 * @param str 待判断的字符串
	 * @return 返回true表示是数字 */
	public boolean isNum(String str)
	{
		return str.matches("[0-9]+");
	}
	
	/** 判断字符串是否包含数字 
	 * @param str 待判断的字符串
	 * @return 返回true表示包含数字 */
	public boolean isConNum(String str)
	{
		Pattern pattern = Pattern.compile(".+?[0-9].+?"); 
	    return pattern.matcher(str).matches();    
	}
	
	/** 判断字符串是否为停用词
	 * @param str 待判断的字符串
	 * @return 返回true表示是停用词 */
	public boolean isUseless(String str)
	{
		return uselessSet.contains(str);
	}
	
	/** 判断字符串是否合法，即非日期词、非数字、不包含数字、不是停用词、不是媒体词
	 * @param str 待判断的字符串
	 * @return 返回true表示合法 */
	public boolean isLegal(String str)
	{
		return !(isDate(str) || isNum(str) || isConNum(str) || isUseless(str) || isMediaWord(str));
	}

	/** 判断字符串是否包含中文字符 
	 * @param str 待判断的字符串
	 * @return 返回true表示包含中文字符 */
	public boolean isConChineseChar(String str) {
		if(str.getBytes().length != str.length())
			return true;
		return false;
	}
	
	/** 判断字符串是否为英文名
	 * @param str 待判断的字符串
	 * @return 返回true表示是英文名 */
	public boolean isEnglishName(String str) {
		Pattern pattern = Pattern.compile("^[a-zA-Z]+$"); 
	    return pattern.matcher(str).matches();
	}
	
	/** 判断字符串是否为媒体词
	 * @param str 待判断的字符串
	 * @return 返回true表示是媒体词 */
	public boolean isMediaWord(String str) {
		Iterator<String> it = mediaSet.iterator();
		while(it.hasNext()) { 
			if(str.contains("微博")) {
				it.next();
				continue;
			}
			if(str.contains(it.next()))
				return true;
		}
		return false;
	}
	
	/** 判断字符串是否为合法来源 
	 * @param str 待判断的字符串
	 * @return 返回true表示是合法来源 */
	public boolean isTrueSource(String str) {
		if(str.length() < 2)
			return false;
		if(str.contains("转载") || str.contains("授权") || str.contains("凡是")) 
			return false;
		if(str.contains("评论") || str.contains("微博"))
			return false;
		if(str.endsWith("来源于") || str.endsWith("来源") || str.endsWith("稿源") || str.endsWith("作者") || str.endsWith("编辑"))
			return false;
		if(isConDateFormat(str))
			return false;
		return true;
	}
	
	/** 判断字符串是否包含日期格式
	 * @param str 待判断的字符串
	 * @return 返回true表示包含日期格式 */
	public boolean isConDateFormat(String str)
	{
		Pattern pattern = Pattern.compile(".+?\\d?\\d?[0-9]{2}[年| |\\/|\\-|.]+\\d?[0-9]{1}[月| |\\/|\\-|.]+\\d?[0-9]{1}日?.+?");
	    return pattern.matcher(str).matches();    
	}
	
	/** 判断字符串是否包含来源格式
	 * @param str 待判断的字符串
	 * @return 返回true表示包含来源格式 */
	public boolean isSourceFormat(String str) {
		if(str.contains("来源") || str.contains("稿源") || str.contains("作者"))
			return true;
		return false;
	}
	
	/** 判断字符串是否为版权声明格式
	 * @param str 待判断的字符串
	 * @return 返回true表示是版权声明格式 */
	public boolean isReproducedFormat(String str) {
		if(str.contains("凡本站") || (str.contains("转载") && str.contains("注明")))
			return true;
		if((str.contains("本站") && str.contains("仅供参考")) || (str.contains("版权") && str.contains("所有")))
			return true;
		if((str.contains("言论") && str.contains("责任") && str.contains("承担")))
			return true;
		if(str.contains("授权") && str.contains("转载"))
			return true;
		if(str.contains("追究") && str.contains("法律") && str.contains("责任"))
			return true;
		if(str.contains("All Rights Reserved") || str.contains("Copyright"))
			return true;
		return false;
	}
	
	/** 判断字符串是否为微博分享词
	 * @param str 待判断的字符串
	 * @return 返回true表示是微博分享词 */
	public boolean isWeibo(String str) {
		if(str.length() <= 20 && (str.contains("微博") || str.contains("订阅") || str.contains("一键") || str.contains("分享")))
			return true;
		return false;
	}
	
	/** 提取字符串中的日期
	 * @param str 待提取的字符串
	 * @return 字符串中包含的日期 */
	public String extractDate(String str)
	{
		Pattern pattern = Pattern.compile("(\\d?\\d?[0-9]{2}[年| |\\/|\\-|.]+\\d?[0-9]{1}[月| |\\/|\\-|.]+\\d?[0-9]{1}日?)");
		Matcher matcher = pattern.matcher(str);
		matcher.find();
	    return matcher.group(1);    
	}
	
	/** 提取字符串中的来源
	 * @param str 待提取的字符串
	 * @return 字符串中包含的来源 */
	public String extractSource(String str)
	{
		str = str.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", " ");
		str = str.replaceAll("\\s+", " ");
		
		String words[] = str.split(" ");
		int i=0;
		for( ; i<words.length; i++) {
			if(isSourceFormat(words[i]) || isMediaWord(words[i]))
				break;
		}
		
		if(i < words.length-1) {
				if(!isTrueSource(words[i])) {
					
					if(!isTrueSource(words[i+1])) {
						if(i < words.length-2)
							return words[i+2];
					} else {
						return words[i+1];
					}
				}
		}
		return words[i];
	}

//	public static void main(String args[]) {
//		KeyWordChecker kwc = new KeyWordChecker(DatabaseInfo.uselesspath, DatabaseInfo.mediapath);
//		if(kwc.isConDateFormat("2012年3月8")) 
//			System.out.println("yes");
//		else
//			System.out.println("no");
//		String str = kwc.extractDate("2012年3月8日 8:13");
//		str = str.replaceAll("[^0-9]", "");
//		System.out.println(str);
//	}
}

