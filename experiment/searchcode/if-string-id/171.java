/**
 * 
 */
package org.svnadmin.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.svnadmin.Constants;
import org.svnadmin.entity.I18n;
import org.svnadmin.service.I18nService;

/**
 * ??????
 * 
 * @author <a href="mailto:yuanhuiwu@gmail.com">Huiwu Yuan</a>
 * @since 3.0.2
 */
public class I18N {
	
	/**
	 * ??
	 */
	private static Map<String,String> cache = new HashMap<String,String>();
	/**
	 * ??????
	 */
	private static I18nService i18nService = SpringUtils.getBean(I18nService.BEAN_NAME);
	
	/**
	 * ???????
	 * @param request ??
	 * @return ???????
	 */
	public static final String getDefaultLang(HttpServletRequest request){
		//from session
		String result = (String) request.getSession().getAttribute(Constants.SESSION_KEY_LANG);
		if(result != null){
			return result;
		}
		//from request local
		Locale locale = request.getLocale();
		String country = locale.getCountry();
		String language = locale.getLanguage();
		
		if(country.length() != 0 && i18nService.existsLang(language+"_"+country)){//????????????
			result = language+"_"+country;
		}
		if(result == null && i18nService.existsLang(language)){//????????????
			result = language;
		}
		if(result == null){
			result = Locale.SIMPLIFIED_CHINESE.toString();//default zh_CN
			getLbl(result, result, Locale.SIMPLIFIED_CHINESE.getDisplayLanguage());//??????????i18n
		}
		
		
		request.getSession().setAttribute(Constants.SESSION_KEY_LANG, result);//set to session
		return result;
	}
	
	/**
	 * @param request ??
	 * @param id ??id
	 * @param defValue ???
	 * @return ????????
	 */
	public static final String getLbl(HttpServletRequest request,String id,String defValue){
		return getLbl(getDefaultLang(request), id, defValue, null);
	}
	/**
	 * @param request ??
	 * @param id ??id
	 * @param defValue ???
	 * @param args ??
	 * @return ????????
	 */
	public static final String getLbl(HttpServletRequest request,String id,String defValue,Object[] args){
		return getLbl(getDefaultLang(request), id, defValue, args);
	}
	/**
	 * @param lang ??
	 * @param id ??id
	 * @param defValue ???
	 * @return ????????
	 */
	public static final String getLbl(String lang,String id,String defValue){
		return getLbl(lang, id, defValue, null);
	}
	/**
	 * @param lang ??
	 * @param id key
	 * @param defValue ???
	 * @param args ??
	 * @return ????????
	 */
	public static final String getLbl(String lang,String id,String defValue,Object[] args){
		String key = lang+"$"+id;
		//from cache
		if(cache.containsKey(key)){
			return format(cache.get(key), args);
		}
		//from database
		I18n i18n = i18nService.getI18n(lang,id);
		if(i18n == null){//???????
			i18n = new I18n();
			i18n.setLang(lang);
			i18n.setId(id);
			i18n.setLbl(defValue);
			i18nService.insert(i18n);
		}
		
		cache.put(key, i18n.getLbl());//put into cache
		
		return format(i18n.getLbl(), args);
	}
	
	/**
	 * ?????
	 * @param pattern the pattern for this message format
	 * @param arguments ??
	 * @return ???????
	 */
	private static String format(String pattern,Object[] arguments){
//		format = new MessageFormat(pattern);
//      format.setLocale(locale);
//      format.applyPattern(pattern);
//		str = format.format(args)
		if(pattern == null){
			return "";
		}
		return MessageFormat.format(pattern, arguments);
	}
	
	/**
	 * ????
	 */
	public static synchronized void clearCache(){
		cache.clear();
	}

	/**
	 * ??Service??DAO???????????????
	 * @param id ??id
	 * @param defValue ???
	 * @return ????????
	 * 
	 * @see LangProvider
	 */
	public static final String getLbl(String id,String defValue){
		return getLbl(LangProvider.getCurrentLang(), id, defValue, null);
	}
	/**
	 * ??Service??DAO???????????????
	 * @param id key
	 * @param defValue ???
	 * @param args ??
	 * @return ????????
	 * 
	 * @see LangProvider
	 */
	public static final String getLbl(String id,String defValue,Object[] args){
		return getLbl(LangProvider.getCurrentLang(), id, defValue, args);
	}

}

