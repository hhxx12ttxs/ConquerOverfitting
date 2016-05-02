package com.mtoolkit.util;

import java.util.Map;

/**
 * String utility.
 * 
 * @author 	<a href="mailto:xishizhang@gmail.com">ZhangShixi</a>
 * @version 1.0, 21/10/2011
 * @since 	JDK1.5
 */
public class StringUtil {
	
	private static final char OPEN_BRACE    = '{';
	private static final char CLOSE_BRACE   = '}';
	private static final String BLANK_SPACE = "" ;

    private StringUtil() {
    }

    public static boolean isNullEmpty(String source) {
        return EmptyUtil.isNullEmpty(source);
    }
    
    public static boolean isNotNullEmpty(String source) {
        return EmptyUtil.isNotNullEmpty(source);
    }

    /**
	 * Counts char number of the specified string value.
	 * 
	 * @param  value target string value.
	 * @return ASCII char count.
	 * 
	 * @throws NullPointerException if {@code value} is null.
	 */
	public static int asciiCount(String value) {
		return CharsetUtil.asciiCount(value);
	}

	/**
	 * Transforms a halfwidth string value to fullwidth. 
	 * 
	 * @param  value target value.
	 * 
	 * @return fullwidth string value.
	 */
	public static String toHalfwidth(String value) {
		return CharsetUtil.toHalfwidth(value);
	}

	/**
	 * Transforms a fullwidth string value to halfwidth. 
	 * 
	 * @param  value target value.
	 * 
	 * @return halfwidth string value.
	 */
	public static String toFullwidth(String value) {
		return CharsetUtil.toFullwidth(value);
	}
	
	/**
	 * Replaces the given parameter values to place holders. Examples:
	 * <pre>
	 * 		String source = "name={0},age={1}";
	 * 		Object[] params = new Object[]{"ZhangShixi", Integer.valueOf(26)};
	 * 		String result = replaceArgs(source, params);
	 * 		//the result is: "name=ZhangShixi,age=26".
	 * </pre>
	 * 
	 * @param  source a source string that may contain one or more '?' 
     * 		   		  in parameter place holders.
	 * @param  params replace parameter values.
	 * 
	 * @return a string that replaced the given values to place holders.
	 * 
	 * @throws NullPointerException if {@code source} is null.
	 */
	public static String replaceHolderArgs(String source, Object... params) {
		if (source == null) {
			throw new NullPointerException("source");
		}
		if (params == null || params.length == 0) {
            return source;
        }

        StringBuilder result = new StringBuilder(source);
        StringBuilder temp = new StringBuilder();
        Object param   = null;
        int startIndex = 0;
        int endIndex   = 0;

        for (int count = 0; count < params.length; count++) {
            param = params[count];

            temp.delete(0, temp.length());
            temp.append(OPEN_BRACE);
            temp.append(count);
            temp.append(CLOSE_BRACE);

            while (true) {
                startIndex = result.indexOf(temp.toString(), 0);
                if (startIndex == -1) {
                    break;
                }
                endIndex = startIndex + temp.length();
                result.replace(startIndex, endIndex,
                        param == null ? BLANK_SPACE : param.toString());
            }

            startIndex = 0;
            endIndex = 0;
        }

        return result.toString();
	}
	
	/**
	 * Replaces the given parameter values to named place holders. Examples:
	 * <pre>
	 * 		String source = "name={name},age={age}";
	 * 		Map<String, Object> params = new HashMap<String, Object>();
	 * 		params.put("name", "ZhangShixi");
	 * 		params.put("age", Integer.valueOf(26));
	 * 		String result = replaceArgs(source, params);
	 * 		//the result is: "name=ZhangShixi,age=26".
	 * </pre>
	 * 
	 * @param  source a source string that may contain one or more '?' 
     * 		   		  in parameter place holders.
	 * @param  params replace parameter values map.
	 * 
	 * @return a string that replaced the given values to named place holders.
	 * 
	 * @throws NullPointerException if {@code source} is null.
	 */
	public static String replaceNamedArgs(String source, Map<String, Object> params) {
		if (source == null) {
			throw new NullPointerException("source");
		}
        if (params == null || params.isEmpty()) {
            return source;
        }
        
        String paramName  = null;
        Object paramValue = null;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char point = source.charAt(i);
            
            if (OPEN_BRACE == point) {
            	int end = source.indexOf(CLOSE_BRACE, i);
            	paramName = source.substring(i + 1, end);
            	paramValue = params.get(paramName);
            	
            	if (paramValue == null) {
            		result.append(OPEN_BRACE)
            			  .append(paramName)
            			  .append(CLOSE_BRACE);
            	} else {
            		result.append(paramValue);
            	}
            	
            	i += paramName.length() + 1;
            } else {
            	result.append(point);
            }
        }
        
        return result.toString();
    }
	
    /**
     * 返回一个新字符串，它是此字符串的一个子字符串。
     * 该子字符串从指定的beginIndex处开始，直到索引endIndex-1处的字符。
     * 此方法是对String的substring(beginIndex, endIndex)方法的一次转换封装，
     * 因为String提供的substring(beginIndex, endIndex)方法具有隐性的内存消耗，
     * 详情请参见JDK String源码。
     * @param source 源字符串。
     * @param beginIndex 起始索引（包括）。
     * @param endIndex 结束索引（不包括）。 
     * @return 截取的子字符串。
     */
    public static String subString(String source, int beginIndex, int endIndex) {
        return new String(source.substring(beginIndex, endIndex));
    }

    /**
     * 返回一个新字符串，它是此字符串的一个子字符串。
     * 该子字符串是源字符串从索引0开始，字节长度为byteLength的子字符串。
     * 此方法实际调用重载方法
     * subByteString(String source, int beginIndex, int endIndex)实现。
     * 其中，指定beginIndex=0，endIndex=byteLength。
     * @param source 源字符串。
     * @param byteLength 字节长度。
     * @return 截取的子字符串。
     */
    public static String subByteString(String source, int byteLength) {
        return subByteString(source, 0, byteLength);
    }

    /**
     * 返回一个新字符串，它是此字符串的一个子字符串。
     * 该子字符串是源字符串从索引0开始，直到索引endIndex-1处的字符。
     * 此方法是根据字符串中的字符所占的字节数来计算，以满足常见的将中文看作2位，英文看作1位
     * 这种需求情况。
     * 若指定的起始索引处为占用2个字节的字符的中间位置，则丢弃此字符。
     * 若指定的结束索引处为占用2个字节的字符的中间位置，则保留此字符。
     * @param source 源字符串。
     * @param beginIndex 起始索引（包括）。
     * @param endIndex 结束索引（不包括）。
     * @return 截取的子字符串。
     */
    public static String subByteString(String source, int beginIndex, int endIndex) {
        int length = 0;
        int begin = 0;
        int end = 0;

        for (int count = 0; count < source.length(); count++) {
            if (beginIndex >= length) {
                begin = count;
            }
            if (endIndex >= length) {
                end = count;
            }

            char c = source.charAt(count);
            length += String.valueOf(c).getBytes().length;

            if (length > endIndex) {
                break;
            }
        }

        return subString(source, begin, end);
    }
    
}

