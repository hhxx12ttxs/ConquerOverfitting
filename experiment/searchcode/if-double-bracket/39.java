/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.soybeanMilk.core.config.parser;

import java.util.ArrayList;
import java.util.List;

import org.soybeanMilk.core.config.parser.ParseException;
import org.soybeanMilk.core.exe.Invoke;

/**
 * 调用语句解析器，它解析诸如"myReulst = myResolver.method(argKey0, argKey1, "string")"之类字符串中的与{@linkplain Invoke 调用}对应的属性
 * @author earthangry@gmail.com
 * @date 2010-11-25
 */
public class InvokeStatementParser
{
	protected static final char ARG_TYPE_START='(';
	protected static final char ARG_TYPE_END=')';
	
	protected static final char[] FORMAT_CHARS={'\n', '\r', '\t', ' '};
	protected static final char[] KEY_CHAR_EQUAL={'='};
	protected static final char[] METHOD_ARG_NAME_END={'\n', '\r', '\t', ' ', ',', ARG_TYPE_START};
	protected static final char[] METHOD_ARG_TYPE_END={ARG_TYPE_END};
	protected static final char[] SINGLE_QUOTE={'\''};
	protected static final char[] DOUBLE_QUOTE={'"'};
	
	/**输入语句*/
	private String statement;
	
	/**调用结果关键字*/
	private String resultKey;
	
	/**调用目标*/
	private String resolver;
	
	/**调用方法名*/
	private String methodName;
	
	/**调用参数*/
	private String[] args;
	
	/**参数类型*/
	private String[] argTypes;
	
	/**内部缓存，它临时存储解析字符*/
	private StringBuffer cache;
	/**当前解析位置*/
	private int currentIdx;
	/**解析的结束位置*/
	private int endIdx;
	/**总长度*/
	private int length;
	
	public InvokeStatementParser(String statement)
	{
		if(statement==null || statement.length()==0)
			throw new ParseException("[statement] must not be empty");
		
		this.statement=statement;
		
		this.cache=new StringBuffer();
		this.currentIdx=0;
		this.length=this.statement.length();
		this.endIdx=this.length;
	}
	
	/**
	 * 获取调用结果关键字
	 * @return
	 * @date 2012-5-6
	 */
	public String getResultKey() {
		return resultKey;
	}
	
	/**
	 * 获取调用目标对象
	 * @return
	 * @date 2012-5-6
	 */
	public String getResolver() {
		return resolver;
	}
	
	/**
	 * 获取调用方法名
	 * @return
	 * @date 2012-5-6
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * 获取调用方法参数数组
	 * @return
	 * @date 2012-5-6
	 */
	public String[] getArgs() {
		return args;
	}
	
	/**
	 * 获取调用方法参数类型
	 * @return
	 * @date 2012-5-11
	 */
	public String[] getArgTypes() {
		return argTypes;
	}

	public void parse()
	{
		String resultKey=null;
		String resolver=null;
		String methodName=null;
		String[] args=null;
		String[] argTypes=null;
		
		//方法的左括弧
		int methodLeftBracketIdx=indexOf('(');
		if(methodLeftBracketIdx < 0)
			throw new ParseException("no method left bracket character '(' found in statement \""+this.statement+"\"");
		
		int methodRightBracketIdx=lastIndexOf(')', this.length);
		if(methodRightBracketIdx < 0)
			throw new ParseException("no method right bracket character ')' found in statement \""+this.statement+"\"");
		
		//等号位置
		int equalCharIdx=indexOf('=', methodLeftBracketIdx);
		
		//方法名之前的'.'访问符位置
		int methodLeftDotIdx=lastIndexOf('.', methodLeftBracketIdx);
		if(methodLeftDotIdx < 0)
			throw new ParseException("no method name start character  '.' found in statement \""+this.statement+"\"");
		
		setCurrentIdx(0);
		
		//解析方法的结果关键字
		if(equalCharIdx < 0)
			resultKey=null;
		else
		{
			ignoreFormatChars();
			
			resultKey=parseUtil(KEY_CHAR_EQUAL, true, true, FORMAT_CHARS);
			if(resultKey==null || resultKey.length()==0)
				throw new ParseException("no result key segment found in statement \""+this.statement+"\"");
			
			//移到'='之后
			setCurrentIdx(equalCharIdx+1);
		}
		
		//解析调用目标
		ignoreFormatChars();
		setEndIdx(methodLeftDotIdx);
		resolver=parseUtil(null, true, true, FORMAT_CHARS);
		if(resolver==null || resolver.length()==0)
			throw new ParseException("no resolver segment found in statement \""+this.statement+"\"");
		
		//移到'.'
		setCurrentIdx(methodLeftDotIdx+1);
		
		//解析方法名
		ignoreFormatChars();
		setEndIdx(methodLeftBracketIdx);
		methodName=parseUtil(null, true, true, FORMAT_CHARS);
		if(methodName==null || methodName.length()==0)
			throw new ParseException("no method name segment found in statement \""+this.statement+"\"");
		
		//移到'('之后
		setCurrentIdx(methodLeftBracketIdx+1);
		
		//解析方法参数
		setEndIdx(methodRightBracketIdx);
		List<String> argStrList=new ArrayList<String>();
		List<String> argTypeStrList=new ArrayList<String>();
		String arg=null;
		String argType=null;
		
		while(getCurrentIdx() < this.length)
		{
			ignoreFormatChars();
			
			//遇到方法的右括弧
			if(getCurrentIdx() >= methodRightBracketIdx)
			{
				if(arg != null)
				{
					argStrList.add(arg);
					argTypeStrList.add(argType);
				}
				arg=null;
				argType=null;
				
				break;
			}
			
			char c=getCurrentChar();
			
			//参数分隔符
			if(c == ',')
			{
				if(arg != null)
				{
					argStrList.add(arg);
					argTypeStrList.add(argType);
				}
				arg=null;
				argType=null;
				
				setCurrentIdx(getCurrentIdx()+1);
			}
			//字符串或者字符
			else if(c == '"')
			{
				this.cache.append('"');
				setCurrentIdx(getCurrentIdx()+1);
				
				arg=parseUtil(DOUBLE_QUOTE, true, true, null);
				
				arg+="\"";
				
				setCurrentIdx(getCurrentIdx()+1);
			}
			else if(c == '\'')
			{
				this.cache.append('\'');
				setCurrentIdx(getCurrentIdx()+1);
				
				arg=parseUtil(SINGLE_QUOTE, true, true, null);
				arg+="'";
				
				setCurrentIdx(getCurrentIdx()+1);
			}
			//参数类型
			else if(c == ARG_TYPE_START)
			{
				setCurrentIdx(getCurrentIdx()+1);
				ignoreFormatChars();
				
				argType=parseUtil(METHOD_ARG_TYPE_END, true, true, FORMAT_CHARS);
				
				setCurrentIdx(getCurrentIdx()+1);
			}
			else
			{
				arg=parseUtil(METHOD_ARG_NAME_END, true, true, null);
			}
		}
		
		args=argStrList.size()==0 ? null : argStrList.toArray(new String[argStrList.size()]);
		argTypes=argTypeStrList.size()==0 ? null : argTypeStrList.toArray(new String[argTypeStrList.size()]);
		
		this.resultKey=resultKey;
		this.resolver=resolver;
		this.methodName=methodName;
		this.args=args;
		this.argTypes=argTypes;
	}
	
	private int getCurrentIdx()
	{
		return this.currentIdx;
	}
	
	/**
	 * 设置解析当前位置
	 * @param currentIdx
	 */
	private void setCurrentIdx(int currentIdx) {
		this.currentIdx = currentIdx;
	}
	
	/**
	 * 设置解析结束位置
	 * @param endIdx
	 */
	private void setEndIdx(int endIdx) {
		this.endIdx = endIdx;
	}
	
	/**
	 * 忽略所有格式字符
	 */
	private void ignoreFormatChars()
	{
		parseUtil(FORMAT_CHARS, false, false, null);
	}
	
	/**
	 * 从当前位置（{@linkplain #getCurrentIdx()}）解析，直到遇到特殊字符或者不是特殊字符
	 * @param specialChars 特殊字符集合
	 * @param inSpecial 是否当遇到特殊字符时停止，否则，当不是特殊字符时停止
	 * @param record 是否保存遇到的字符
	 * @param ignoreChars 设置不保存的字符集合
	 * @return
	 */
	private String parseUtil(char[] specialChars, boolean inSpecial, boolean record, char[] ignoreChars)
	{
		//前一个字符是否是转义标记'\'
		boolean preEscMark=false;
		
		for(; currentIdx<endIdx; currentIdx++)
		{
			char c=getChar(currentIdx);
			
			if(c == '\\' && !preEscMark)
			{
				preEscMark=true;
				
				if(record)
					cache.append(c);
			}
			else
			{
				//如果前一个字符是'\'，则不对此字符做任何特殊过滤
				if(preEscMark)
				{
					if(record)
						cache.append(c);
					
					preEscMark=false;
				}
				else
				{
					if(!inSpecial && !contain(specialChars, c))
						break;
					else if(inSpecial && contain(specialChars, c))
						break;
					
					if(record && !contain(ignoreChars, c))
						cache.append(c);
				}
			}
		}
		
		String re=null;
		if(record)
			re=cache.toString();
		else
			re=null;
		
		cache.delete(0, cache.length());
		
		return re;
	}
	
	/**
	 * 取得当前位置的字符
	 * @return
	 */
	private char getCurrentChar()
	{
		return getChar(currentIdx);
	}
	
	private int indexOf(char c)
	{
		return indexOf(c, this.length);
	}
	
	private int indexOf(char c, int endIdx)
	{
		int i=0;
		for(; i<endIdx; i++)
		{
			if(getChar(i) == c)
				break;
		}
		
		return i == endIdx ? -1 : i;
	}
	
	private int lastIndexOf(char c, int endIdx)
	{
		int i=endIdx-1;
		
		for(; i>=0; i--)
		{
			if(getChar(i) == c)
				break;
		}
		
		return i;
	}
	
	private boolean contain(char[] chars, char c)
	{
		if(chars==null || chars.length==0)
			return false;
		
		for(char ch : chars)
			if(ch == c)
				return true;
		
		return false;
	}
	
	private char getChar(int idx)
	{
		return this.statement.charAt(idx);
	}
	
	/**
	 * 设置{@linkplain Arg}的关键字属性或者值属性，它根据字符串的语法格式（与Java语法一样）来确定应该设置哪个属性。<br>
	 * 比如，["abc"]是字符串值、[myresult_key]是关键字、['a']是字符值、[3.5f]是数值
	 * @param arg
	 * @param stmt 符合Java语法的字符串，可以包含转义字符和'\\uxxxx'格式字符
	 */
	/*
	public static void stringToArgProperty(Arg arg, String stmt)
	{
		if(stmt==null || stmt.length()==0)
			return;
		
		if(arg.getType() == null)
			throw new ParseException("the [type] property of this Arg must not be null");
		
		Type wrapType=SoybeanMilkUtils.toWrapperType(arg.getType());
		
		String key=null;
		Object value=null;
		
		//首字符为数字，则认为是数值
		if(stmt.length()>0 && SoybeanMilkUtils.isDigit(stmt.charAt(0)))
		{
			if(Byte.class.equals(wrapType))
				value=new Byte(stmt);
			else if(Double.class.equals(wrapType))
				value=new Double(stmt);
			else if(Float.class.equals(wrapType))
				value=new Float(stmt);
			else if(Integer.class.equals(wrapType))
				value=new Integer(stmt);
			else if(Long.class.equals(wrapType))
				value=new Long(stmt);
			else if(Short.class.equals(wrapType))
				value=new Short(stmt);
			else
				throw new ParseException("can not create Number instance of class "+SbmUtils.toString(arg.getType())+" with value \""+stmt+"\"");
		}
		else if("true".equals(stmt))
		{
			value=Boolean.TRUE;
		}
		else if("false".equals(stmt))
		{
			value=Boolean.FALSE;
		}
		else if("null".equals(stmt))
		{
			if(SoybeanMilkUtils.isPrimitive(arg.getType()))
				throw new ParseException("can not set null to primitive type");
			
			value=null;
		}
		else if(stmt.startsWith("\"") && stmt.endsWith("\"")
				&& stmt.length()>1)
		{
			stmt=SoybeanMilkUtils.unEscape(stmt);
			
			if(stmt.length()<2)
				throw new ParseException("illegal String definition "+stmt);
			
			value=stmt.substring(1, stmt.length()-1);
		}
		else if(stmt.startsWith("'") && stmt.endsWith("'"))
		{
			stmt=SoybeanMilkUtils.unEscape(stmt);
			
			if(stmt.length() != 3)
				throw new ParseException("illegal char definition "+stmt);
			
			value=stmt.charAt(1);
		}
		else
			key=stmt;
		
		arg.setKey(key);
		arg.setValue(value);
	}
	*/
}

