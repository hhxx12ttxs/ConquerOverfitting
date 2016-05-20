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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soybeanMilk.SbmUtils;
import org.soybeanMilk.core.Constants;
import org.soybeanMilk.core.Executable;
import org.soybeanMilk.core.ExecuteException;
import org.soybeanMilk.core.ObjectSource;
import org.soybeanMilk.core.bean.Converter;
import org.soybeanMilk.core.bean.DefaultGenericConverter;
import org.soybeanMilk.core.bean.GenericConverter;
import org.soybeanMilk.core.config.Configuration;
import org.soybeanMilk.core.config.Interceptor;
import org.soybeanMilk.core.exe.Action;
import org.soybeanMilk.core.exe.Invoke;
import org.soybeanMilk.core.exe.Invoke.Arg;
import org.soybeanMilk.core.exe.support.DefaultResolverObjectFactory;
import org.soybeanMilk.core.exe.support.DynamicResolver;
import org.soybeanMilk.core.exe.support.FactoryResolver;
import org.soybeanMilk.core.exe.support.KeyArg;
import org.soybeanMilk.core.exe.support.ObjectResolver;
import org.soybeanMilk.core.exe.support.ObjectSourceResolver;
import org.soybeanMilk.core.exe.support.ResolverObjectFactory;
import org.soybeanMilk.core.exe.support.ValueArg;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 配置解析器，它从XML配置文件解析对象。
 * @author earthangry@gmail.com
 * @date 2010-10-1
 */
public class XmlConfigurationParser
{
	private static Log log=LogFactory.getLog(XmlConfigurationParser.class);
	
	protected static final String TAG_ROOT="soybean-milk";
	
	protected static final String TAG_GLOBAL_CONFIG="global-config";
	protected static final String TAG_GENERIC_CONVERTER="generic-converter";
	protected static final String TAG_GENERIC_CONVERTER_ATTR_CLASS="class";
	protected static final String TAG_CONVERTER="converter";
	protected static final String TAG_CONVERTER_ATTR_SRC="src";
	protected static final String TAG_CONVERTER_ATTR_TARGET="target";
	protected static final String TAG_CONVERTER_ATTR_CLASS=TAG_GENERIC_CONVERTER_ATTR_CLASS;
	
	protected static final String TAG_INTERCEPROT="interceptor";
	protected static final String TAG_INTERCEPROT_ATTR_BEFORE="before";
	protected static final String TAG_INTERCEPROT_ATTR_AFTER="after";
	protected static final String TAG_INTERCEPROT_ATTR_EXCEPTION="exception";
	protected static final String TAG_INTERCEPROT_ATTR_EXECUTION_KEY="execution-key";
	
	protected static final String TAG_INCLUDES="includes";
	protected static final String TAG_LOCATION="location";
	
	protected static final String TAG_RESOLVERS="resolvers";
	protected static final String TAG_RESOLVER="resolver";
	protected static final String TAG_RESOLVER_ATTR_ID="id";
	protected static final String TAG_RESOLVER_ATTR_CLASS="class";
	
	protected static final String TAG_EXECUTABLES="executables";
	protected static final String TAG_EXECUTABLES_ATTR_PREFIX="prefix";
	
	protected static final String TAG_ACTION="action";
	protected static final String TAG_ACTION_ATTR_NAME="name";
	
	protected static final String TAG_INVOKE="invoke";
	protected static final String TAG_INVOKE_ATTR_NAME=TAG_ACTION_ATTR_NAME;
	protected static final String TAG_INVOKE_ATTR_METHOD="method";
	protected static final String TAG_INVOKE_ATTR_RESOLVER="resolver";
	protected static final String TAG_INVOKE_ATTR_RESULT_KEY="result-key";
	protected static final String TAG_INVOKE_ATTR_BREAKER="breaker";
	
	protected static final String TAG_ARG="arg";
	protected static final String TAG_ARG_ATTR_TYPE="type";
	
	protected static final String TAG_REF="ref";
	protected static final String TAG_REF_ATTR_NAME="name";
	
	/**主文档*/
	private Document rootDocument;
	
	/**配置对象*/
	private Configuration configuration;
	
	/**主文档包含的模块文档*/
	private List<Document> modules;
	
	/**当前可执行对象前缀*/
	private String currentExecutablePrefix;
	
	/**
	 * 创建解析器，不预设存储配置对象
	 */
	public XmlConfigurationParser()
	{
		this(null);
	}
	
	/**
	 * 创建解析器，并预设存储配置对象，所有的解析结果都将保存到这个配置中
	 * @param configuration 预设配置对象
	 */
	public XmlConfigurationParser(Configuration configuration)
	{
		setConfiguration(configuration);
	}
	
	/**
	 * 取得解析结果
	 * @return
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	 * 设置解析配置对象，所有的解析结果将保存到该配置中，它应该在解析前调用
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * 取得解析文档对象
	 * @return
	 */
	public Document getDocument()
	{
		return this.rootDocument;
	}
	
	/**
	 * 设置解析文档对象
	 * @param document
	 */
	public void setDocument(Document document) {
		this.rootDocument = document;
	}
	
	/**
	 * 取得模块文档对象
	 * @return
	 */
	public List<Document> getModules() {
		return modules;
	}
	
	/**
	 * 设置模块文档对象
	 * @param modules
	 */
	public void setModules(List<Document> modules) {
		this.modules = modules;
	}
	
	/**
	 * 从默认配置文件解析
	 * @return
	 */
	public Configuration parse()
	{
		return parse((String)null);
	}
	
	/**
	 * 从给定配置文件解析
	 * @param configFile 配置文件，可以类路径资源文件，也可以是文件系统文件
	 * @return
	 */
	public Configuration parse(String configFile)
	{
		if(configFile==null || configFile.length()==0)
			configFile=getDefaultConfigFile();
		
		setDocument(parseDocument(configFile));
		
		parseAll();
		
		return getConfiguration();
	}
	
	/**
	 * 从输入流解析
	 * @param in
	 * @return
	 */
	public Configuration parse(InputStream in)
	{
		setDocument(parseDocument(in));
		
		parseAll();
		
		return getConfiguration();
	}
	
	/**
	 * 从文档对象解析
	 * @param document
	 * @return
	 */
	public Configuration parse(Document document)
	{
		setDocument(document);
		
		parseAll();
		
		return getConfiguration();
	}
	
	/**
	 * 解析，如果你没有预设配置对象，这个方法将自动创建
	 * @return 解析结果
	 */
	protected void parseAll()
	{
		if(getConfiguration() == null)
			setConfiguration(createConfigurationInstance());
		
		Element rootDocEle=getDocumentRootElement(rootDocument);
		
		parseGlobalConfigs(rootDocEle);
		parseIncludes(rootDocEle);
		parseResolvers(rootDocEle);
		parseExecutables(rootDocEle);
		
		if(modules != null)
		{
			for(Document doc : modules)
			{
				Element subDoc=getDocumentRootElement(doc);
				
				parseResolvers(subDoc);
				parseExecutables(subDoc);
			}
		}
		
		parseRefs();
	}
	
	/**
	 * 解析全局配置
	 */
	protected void parseGlobalConfigs(Element docRoot)
	{
		Element parent=getSingleElementByTagName(docRoot, TAG_GLOBAL_CONFIG);
		
		parseGenericConverter(parent);
		
		parseInterceptor(parent);
	}
	
	/**
	 * 解析包含的模块配置
	 */
	protected void parseIncludes(Element docRoot)
	{
		List<Element> files=getChildrenByTagName(getSingleElementByTagName(docRoot, TAG_INCLUDES), TAG_LOCATION);
		
		if(files == null || files.isEmpty())
			return;
		
		this.modules = new ArrayList<Document>();
		
		for(Element el : files)
		{
			String fileName=getTextContent(el);
			assertNotEmpty(fileName, "<"+TAG_LOCATION+"> content must not be null");
			
			Document[] docs=parseDocuments(fileName);
			if(docs != null)
			{
				for(Document d : docs)
					this.modules.add(d);
			}
		}
	}
	
	/**
	 * 解析并构建调用目标对象
	 */
	protected void parseResolvers(Element docRoot)
	{
		List<Element> children=getChildrenByTagName(getSingleElementByTagName(docRoot, TAG_RESOLVERS), TAG_RESOLVER);
		
		if(children!=null && !children.isEmpty())
		{
			ResolverObjectFactory rf = configuration.getResolverObjectFactory();
			if(rf == null)
			{
				rf= createResolverObjectFactoryInstance();
				configuration.setResolverObjectFactory(rf);
			}
			
			ResolverObjectFactory drf=(ResolverObjectFactory)rf;
			
			for(Element e : children)
			{
				String id=getAttributeValueIngoreEmpty(e,TAG_RESOLVER_ATTR_ID);
				assertNotEmpty(id,"<"+TAG_RESOLVER+"> attribute ["+TAG_RESOLVER_ATTR_ID+"] must not be null");
				String clazz=getAttributeValueIngoreEmpty(e,TAG_RESOLVER_ATTR_CLASS);
				assertNotEmpty(clazz,"<"+TAG_RESOLVER+"> of id "+SbmUtils.toString(id)+" attribute ["+TAG_RESOLVER_ATTR_CLASS+"] must not be null");
				
				Object resolver=createClassInstance(clazz);
				
				drf.addResolverObject(id,resolver);
			}
		}
	}
	
	/**
	 * 解析并构建可执行对象
	 */
	protected void parseExecutables(Element docRoot)
	{
		List<Element> executables=getChildrenByTagName(docRoot,TAG_EXECUTABLES);
		
		if(executables != null)
		{
			for(Element ele : executables)
			{
				setCurrentExecutablePrefix(getAttributeValue(ele, TAG_EXECUTABLES_ATTR_PREFIX));
				
				List<Element> children=getChildrenByTagName(ele, null);
				
				if(children != null)
				{
					for(Element e : children)
					{
						Executable executable=createExecutableInstance(e.getTagName());
						
						if(executable instanceof Action)
							setActionProperties((Action)executable,e);
						else
							setInvokeProperties((Invoke)executable,e, true);
						
						configuration.addExecutable(executable);
					}
				}
			}
		}
	}
	
	/**
	 * 处理引用
	 */
	protected void parseRefs()
	{
		processExecutableRefs();
		processInterceptorInfoRefs();
	}
	
	/**
	 * 解析通用转换器
	 * @param parent
	 */
	protected void parseGenericConverter(Element parent)
	{
		Element cvtEl = getSingleElementByTagName(parent, TAG_GENERIC_CONVERTER);
		
		String clazz = cvtEl==null ? null : getAttributeValueIngoreEmpty(cvtEl, TAG_GENERIC_CONVERTER_ATTR_CLASS);
		
		GenericConverter genericConverter = configuration.getGenericConverter();
		if(genericConverter == null)
		{
			if(clazz==null || clazz.length()==0)
				genericConverter = createGenericConverterInstance();
			else
				genericConverter = (GenericConverter)createClassInstance(clazz);
			
			configuration.setGenericConverter(genericConverter);
		}
		
		parseSupportConverters(genericConverter, cvtEl);
	}
	
	/**
	 * 解析父元素下的支持转换器并加入给定的通用转换器中
	 * @param genericConverter
	 * @param parent
	 */
	protected void parseSupportConverters(GenericConverter genericConverter, Element parent)
	{
		List<Element> children=getChildrenByTagName(parent, TAG_CONVERTER);
		if(children==null || children.isEmpty())
			return;
		
		for(Element e : children)
		{
			String src = getAttributeValueIngoreEmpty(e, TAG_CONVERTER_ATTR_SRC);
			String target = getAttributeValueIngoreEmpty(e, TAG_CONVERTER_ATTR_TARGET);
			String clazz = getAttributeValueIngoreEmpty(e, TAG_CONVERTER_ATTR_CLASS);
			
			assertNotEmpty(src, "<"+TAG_CONVERTER+"> attribute ["+TAG_CONVERTER_ATTR_SRC+"] must not be empty");
			assertNotEmpty(target, "<"+TAG_CONVERTER+"> attribute ["+TAG_CONVERTER_ATTR_TARGET+"] must not be empty");
			assertNotEmpty(clazz, "<"+TAG_CONVERTER+"> attribute ["+TAG_CONVERTER_ATTR_CLASS+"] must not be empty");
			
			genericConverter.addConverter(nameToType(src), nameToType(target), (Converter)createClassInstance(clazz));
		}
	}
	
	/**
	 * 解析拦截器信息
	 * @param element 父元素
	 */
	protected void parseInterceptor(Element parent)
	{
		Element el=getSingleElementByTagName(parent, TAG_INTERCEPROT);
		if(el == null)
			return;
		
		String before=getAttributeValueIngoreEmpty(el, TAG_INTERCEPROT_ATTR_BEFORE);
		String after=getAttributeValueIngoreEmpty(el, TAG_INTERCEPROT_ATTR_AFTER);
		String exception=getAttributeValueIngoreEmpty(el, TAG_INTERCEPROT_ATTR_EXCEPTION);
		String executionKey=getAttributeValueIngoreEmpty(el, TAG_INTERCEPROT_ATTR_EXECUTION_KEY);
		
		assertNotEmpty(executionKey, "<"+TAG_INTERCEPROT+"> attribute ["+TAG_INTERCEPROT_ATTR_EXECUTION_KEY+"] must not be empty");
		
		Interceptor ii=createInterceptorInfoInstance();
		ii.setExecutionKey(executionKey);
		
		if(before != null)
			ii.setBefore(new ExecutableRefProxy(before, getCurrentExecutablePrefix()));
		if(after != null)
			ii.setAfter(new ExecutableRefProxy(after, getCurrentExecutablePrefix()));
		if(exception != null)
			ii.setException(new ExecutableRefProxy(exception, getCurrentExecutablePrefix()));
		
		getConfiguration().setInterceptor(ii);
	}
	
	/**
	 * 从元素中解析并设置动作的属性。
	 * @param action
	 * @param element
	 */
	protected void setActionProperties(Action action,Element element)
	{
		//动作和调用的名称可以为空字符串""，因为在servlet规范中会有空字符串名的serlvet路径
		
		String name=getAttributeValue(element,TAG_ACTION_ATTR_NAME);
		assertNotNull(name, "<"+TAG_ACTION+"> attribute ["+TAG_ACTION_ATTR_NAME+"] must not be null");
		
		action.setName(formatGlobalExecutableName(name));
		
		List<Element> children=getChildrenByTagName(element, null);
		for(Element e : children)
		{
			String tagName=e.getTagName();
			if(TAG_REF.equals(tagName))
			{
				String refExecutableName=getAttributeValue(e,TAG_REF_ATTR_NAME);
				assertNotNull(refExecutableName, "<"+TAG_REF+"> attribute ["+TAG_REF_ATTR_NAME+"] in <"+TAG_ACTION+"> named "+SbmUtils.toString(action.getName())+" must not be null");
				
				action.addExecutable(new ExecutableRefProxy(refExecutableName, getCurrentExecutablePrefix()));
			}
			else if(TAG_INVOKE.equals(tagName))
			{
				Invoke invoke=createInvokeIntance();
				setInvokeProperties(invoke,e, false);
				
				action.addExecutable(invoke);
			}
		}
	}
	
	/**
	 * 从元素中解析并设置调用的属性
	 * @param invoke
	 * @param element
	 * @param global 是否全局调用
	 */
	protected void setInvokeProperties(Invoke invoke, Element element, boolean global)
	{
		String methodName=getAttributeValueIngoreEmpty(element, TAG_INVOKE_ATTR_METHOD);
		String breaker=getAttributeValueIngoreEmpty(element, TAG_INVOKE_ATTR_BREAKER);
		
		if(breaker != null)
		{
			Serializable brk=null;
			
			if(Boolean.TRUE.toString().equals(breaker))
				brk=Boolean.TRUE;
			else if(Boolean.FALSE.toString().equals(breaker))
				brk=Boolean.FALSE;
			else
				brk=breaker;
			
			invoke.setBreaker(brk);
		}
		
		if(methodName == null)
			setInvokePropertiesStatement(invoke, element, global);
		else
			setInvokePropertiesXml(invoke, element, global);
	}
	
	/**
	 * 设置以表达式方式定义的调用属性
	 * @param invoke
	 * @param element
	 * @param global 是否全局调用
	 */
	protected void setInvokePropertiesStatement(Invoke invoke, Element element, boolean global)
	{
		String statement=getTextContent(element);
		assertNotEmpty(statement, "<"+TAG_INVOKE+"> content must not be empty");
		
		String name=getAttributeValue(element,TAG_INVOKE_ATTR_NAME);
		if(global)
			name=formatGlobalExecutableName(name);
		
		InvokeStatementParser isp=new InvokeStatementParser(statement);
		isp.parse();
		
		invoke.setName(name);
		invoke.setResultKey(isp.getResultKey());
		invoke.setMethodName(isp.getMethodName());
		processInvokeResolverInit(invoke, isp.getResolver());
		processInvokeArgsInit(invoke, isp.getArgs(), isp.getArgTypes());
	}
	
	/**
	 * 设置以XML方式定义的调用属性
	 * @param invoke
	 * @param element
	 * @param global 是否全局调用
	 */
	protected void setInvokePropertiesXml(Invoke invoke,Element element, boolean global)
	{
		String name=getAttributeValue(element,TAG_INVOKE_ATTR_NAME);
		if(global)
			name=formatGlobalExecutableName(name);
		String methodName=getAttributeValueIngoreEmpty(element, TAG_INVOKE_ATTR_METHOD);
		String resolver=getAttributeValueIngoreEmpty(element,TAG_INVOKE_ATTR_RESOLVER);
		String resultKey=getAttributeValueIngoreEmpty(element,TAG_INVOKE_ATTR_RESULT_KEY);
		
		if(methodName == null)
			throw new ParseException("<"+TAG_INVOKE+"> attribute ["+TAG_INVOKE_ATTR_METHOD+"] must not be null");
		if(resolver == null)
			throw new ParseException("<"+TAG_INVOKE+"> attribute ["+TAG_INVOKE_ATTR_RESOLVER+"] must not be null");
		
		invoke.setName(name);
		invoke.setResultKey(resultKey);
		invoke.setMethodName(methodName);
		processInvokeResolverInit(invoke, resolver);
		
		parseArgs(invoke, element);
	}
	
	/**
	 * 解析并构建parent元素下的所有参数信息对象，写入调用对象中
	 * @param invoke
	 * @param parent
	 */
	protected void parseArgs(Invoke invoke, Element parent)
	{
		List<Element> elements=getChildrenByTagName(parent, TAG_ARG);
		if(elements == null)
			return;
		
		String[] strArgs=new String[elements.size()];
		String[] strArgTypes=new String[elements.size()];
		
		for(int i=0, len=strArgs.length;i<len;i++)
		{
			Element e=elements.get(i);
			
			String type=getAttributeValue(e, TAG_ARG_ATTR_TYPE);
			String content=getTextContent(e);
			if(content == null)
				throw new ParseException("<"+TAG_ARG+"> must have text content");
			
			strArgs[i]=content;
			strArgTypes[i]=type;
		}
		
		processInvokeArgsInit(invoke, strArgs, strArgTypes);
	}
	
	/**
	 * 处理Invoke属性初始化
	 * @param invoke
	 * @param resultKey
	 * @param resolver
	 * @param methodName
	 * @param args
	 * @param strArgTypes
	 * @date 2012-5-8
	 */
	/*
	protected void processInvokePropertiesInit(Invoke invoke, String resultKey, String resolver, String methodName, String[] strArgs, String[] strArgTypes)
	{
		invoke.setResultKey(resultKey);
		
		boolean classResolver=false;
		//resolver为类名检测
		if(resolver.indexOf('.') > -1)
		{
			try
			{
				Class<?> rc=Class.forName(resolver);
				invoke.setResolverProvider(new ObjectResolverProvider(null, rc));
				
				classResolver=true;
			}
			catch(Exception e)
			{
				classResolver=false;
			}
		}
		if(!classResolver)
		{
			FactoryResolverProvider frp=new FactoryResolverProvider(configuration.getResolverObjectFactory(), resolver);
			ObjectSourceResolverProvider orp=new ObjectSourceResolverProvider(resolver);
			
			invoke.setResolverProvider(new DynamicResolverProvider(frp, orp));
		}
		
		invoke.setMethodName(methodName);
		
		if(strArgs != null)
		{
			Arg[] args=new Arg[strArgs.length];
			
			for(int i=0; i<strArgs.length; i++)
				args[i]=stringToArg(strArgs[i], (strArgTypes==null ? null : strArgTypes[i]));
			
			invoke.setArgs(args);
		}
	}
	*/
	
	/**
	 * 处理调用目标初始化
	 * @param invoke
	 * @param resolver
	 * @date 2012-5-11
	 */
	protected void processInvokeResolverInit(Invoke invoke, String resolver)
	{
		boolean classResolver=false;
		//resolver为类名检测
		if(resolver.indexOf('.') > -1)
		{
			try
			{
				Class<?> rc=SbmUtils.narrowToClass(nameToType(resolver));
				invoke.setResolver(new ObjectResolver(null, rc));
				
				classResolver=true;
			}
			catch(Exception e)
			{
				classResolver=false;
			}
		}
		if(!classResolver)
		{
			FactoryResolver frp=new FactoryResolver(configuration.getResolverObjectFactory(), resolver);
			ObjectSourceResolver orp=new ObjectSourceResolver(resolver);
			
			invoke.setResolver(new DynamicResolver(frp, orp));
		}
	}
	
	/**
	 * 处理调用参数初始化
	 * @param invoke
	 * @param resolver
	 * @date 2012-5-11
	 */
	protected void processInvokeArgsInit(Invoke invoke, String[] strArgs, String[] strArgTypes)
	{
		if(strArgs != null)
		{
			Arg[] args=new Arg[strArgs.length];
			
			for(int i=0; i<strArgs.length; i++)
				args[i]=stringToArg(strArgs[i], (strArgTypes==null ? null : strArgTypes[i]));
			
			invoke.setArgs(args);
		}
	}
	
	/**
	 * 处理可执行对象引用代理，将它们替换为真正的引用可执行对象，
	 * 比如动作中的可执行对象引用代理。
	 */
	protected void processExecutableRefs()
	{
		Collection<Executable> executables=configuration.getExecutables();
		if(executables == null)
			return;
		
		for(Executable exe : executables)
		{
			if(exe instanceof Action)
			{
				Action action=(Action)exe;
				List<Executable> actionExes=action.getExecutables();
				if(actionExes==null)
					continue;
				
				for(int i=0,len=actionExes.size();i<len;i++)
				{
					Executable e=actionExes.get(i);
					
					if(e instanceof ExecutableRefProxy)
					{
						ExecutableRefProxy proxy=((ExecutableRefProxy)e);
						Executable targetExe=getTargetRefExecutable((ExecutableRefProxy)e);
						
						if(targetExe == null)
							throw new ParseException("can not find Executable named "+SbmUtils.toString(proxy.getRefName())+" referenced in Action "+SbmUtils.toString(action.getName()));
						
						actionExes.set(i, targetExe);
					}
				}
			}
		}
	}

	/**
	 * 替换拦截器的代理为真实的可执行对象
	 */
	protected void processInterceptorInfoRefs()
	{
		Interceptor ii=getConfiguration().getInterceptor();
		if(ii == null)
			return;
		
		{
			Executable before=ii.getBefore();
			if(before instanceof ExecutableRefProxy)
			{
				Executable targetExe=getTargetRefExecutable((ExecutableRefProxy)before);
				if(targetExe == null)
					throw new ParseException("can not find 'before' interceptor named "+SbmUtils.toString(((ExecutableRefProxy)before).getRefName()));
				
				ii.setBefore(targetExe);
			}
		}
		
		{
			Executable after=ii.getAfter();
			if(after instanceof ExecutableRefProxy)
			{
				Executable targetExe=getTargetRefExecutable((ExecutableRefProxy)after);
				if(targetExe == null)
					throw new ParseException("can not find 'after' interceptor named "+SbmUtils.toString(((ExecutableRefProxy)after).getRefName()));
				
				ii.setAfter(targetExe);
			}
		}
		
		{
			Executable exception=ii.getException();
			if(exception instanceof ExecutableRefProxy)
			{
				Executable targetExe=getTargetRefExecutable((ExecutableRefProxy)exception);
				if(targetExe == null)
					throw new ParseException("can not find 'exception' interceptor named "+SbmUtils.toString(((ExecutableRefProxy)exception).getRefName()));
				
				ii.setException(targetExe);
			}
		}
	}
	
	/**
	 * 获取代理目标
	 * @param proxy
	 * @return
	 * @date 2011-3-15
	 */
	protected Executable getTargetRefExecutable(ExecutableRefProxy proxy)
	{
		Executable target=null;
		
		//先从它的文件作用域内取
		if(target == null)
		{
			if(proxy.getCurrentExecutablePrefix() != null)
				target=configuration.getExecutable(proxy.getCurrentExecutablePrefix()+proxy.getRefName());
		}
		
		if(target == null)
			target=configuration.getExecutable(proxy.getRefName());
		
		return target;
	}
	
	/**
	 * 格式化全局可执行对象名称，比如增加当前前缀。
	 * @param rawName
	 * @return
	 * @date 2011-3-15
	 */
	protected String formatGlobalExecutableName(String rawName)
	{
		String cep=getCurrentExecutablePrefix();
		
		return cep == null ? rawName : cep+rawName;
	}
	
	/**
	 * 从输入流解析xml文档对象，此方法同时负责关闭输入流
	 * @param in
	 * @return
	 * @throws ParseException
	 */
	protected Document parseDocument(InputStream in)
	{
		Document doc=null;
		
		try
		{
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			
			dbf.setNamespaceAware(false);
			
			//禁止验证
			dbf.setValidating(false);
			dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			doc=dbf.newDocumentBuilder().parse(in);
			
			in.close();
			
			return doc;
		}
		catch(Exception e)
		{
			throw new ParseException("",e);
		}
	}
	
	/**
	 * 解析名称中带有匹配符的文档
	 * @param fileName
	 * @return
	 */
	protected Document[] parseDocuments(String fileName)
	{
		Document[] docs=null;
		
		if(fileName.endsWith("/*"))
		{
			fileName=formatIncludeFileName(fileName);
			fileName=fileName.substring(0, fileName.length()-2);
			
			File folder=new File(fileName);
			if(!folder.exists() || !folder.isDirectory())
				throw new ParseException("can not find directory "+SbmUtils.toString(fileName));
			
			File[] files=folder.listFiles(new FileFilter()
			{
				//@Override
				public boolean accept(File pathname)
				{
					String name=pathname.getName().toLowerCase();
					if(name.endsWith(".xml"))
						return true;
					else
						return false;
				}
			});
			
			if(files!=null && files.length>0)
			{
				docs=new Document[files.length];
				
				for(int i=0;i<files.length;i++)
				{
					InputStream in=null;
					try
					{
						in=new FileInputStream(files[i]);
					}
					catch(Exception e)
					{
						throw new ParseException("", e);
					}
					
					docs[i]=parseDocument(in);
					
					if(log.isDebugEnabled())
						log.debug("parsed Document object from "+SbmUtils.toString(files[i].getAbsolutePath()));
				}
			}
			else
			{
				if(log.isDebugEnabled())
					log.debug("no xml file found in directory "+SbmUtils.toString(fileName));
			}
		}
		else
		{
			docs=new Document[1];
			docs[0]=parseDocument(fileName);
		}
		
		return docs;
	}
	
	/**
	 * 根据名称取得文档对象
	 * @param fileName
	 * @return
	 */
	protected Document parseDocument(String fileName)
	{
		fileName=formatIncludeFileName(fileName);
		
		InputStream in = null;
		try
		{
			in = getClass().getClassLoader().getResourceAsStream(fileName);
		}
		catch(Exception e){}
		
		if(in == null)
		{
			try
			{
				in=new FileInputStream(fileName);
			}
			catch(Exception e1){}
		}
		
		if(in == null)
			throw new ParseException("can not find config file named "+SbmUtils.toString(fileName));
		
		Document doc=parseDocument(in);
		
		if(log.isDebugEnabled())
			log.debug("parsing Document object from "+SbmUtils.toString(fileName));
		
		return doc;
	}
	
	/**
	 * 格式化包含模块文件
	 * @param rawFileName
	 * @return
	 */
	protected String formatIncludeFileName(String rawFileName)
	{
		return rawFileName;
	}
	
	/**
	 * 取得文档根元素
	 * @return
	 */
	protected Element getDocumentRootElement(Document doc)
	{
		return doc.getDocumentElement();
	}
	
	protected void setCurrentExecutablePrefix(String  currentExecutablePrefix)
	{
		this.currentExecutablePrefix=currentExecutablePrefix;
	}
	
	/**
	 * 获取当前可执行对象前缀
	 * @return
	 * @date 2011-3-15
	 */
	protected String getCurrentExecutablePrefix()
	{
		return this.currentExecutablePrefix;
	}
	
	/**
	 * 取得默认配置文件
	 * @return
	 */
	protected String getDefaultConfigFile()
	{
		return Constants.DEFAULT_CONFIG_FILE;
	}
	
	/**
	 * 取得父元素的直接子元素列表。如果没有，则返回null。
	 * 只有子元素的标签名与给定名称匹配的才会返回，如果名称为null，则返回所有。
	 * @param parent
	 * @param name
	 * @return
	 * @throws Exception
	 */
	protected List<Element> getChildrenByTagName(Element parent,String name)
	{
		if(parent == null)
			return null;
		
		boolean filter= name != null;
		
		NodeList nl=parent.getChildNodes();
		
		List<Element> elements=new ArrayList<Element>();
		for(int i=0;i<nl.getLength();i++)
		{
			Node n=nl.item(i);
			if(!(n instanceof Element))
				continue;
			
			Element e=(Element)nl.item(i);
			
			if(!filter)
				elements.add(e);
			else
			{
				if(name.equals(e.getTagName()))
					elements.add(e);
			}
		}
		
		return elements;
	}
	
	/**
	 * 取得父元素的单一子元素
	 * @param parent
	 * @param name
	 * @return
	 */
	protected Element getSingleElementByTagName(Element parent,String name)
	{
		if(parent == null)
			return null;
		
		NodeList nodes=parent.getElementsByTagName(name);
		
		if(nodes==null || nodes.getLength()==0)
			return null;
		
		return (Element)nodes.item(0);
	}
	
	/**
	 * 确保给定的对象不为null，如果是字符串，同时确保它不为空字符串。
	 * @param o 对象
	 * @param msg 失败时的异常消息
	 */
	protected void assertNotEmpty(Object o,String msg)
	{
		boolean toThrow=false;
		
		if(o == null)
			toThrow=true;
		else if(o instanceof String)
		{
			String s=(String)o;
			if(s.length() == 0)
				toThrow=true;
		}
		
		if(toThrow)
			throw new ParseException(msg);
	}
	
	/**
	 * 确保参数不为null
	 * @param o
	 * @param msg
	 */
	protected void assertNotNull(Object o,String msg)
	{
		if(o == null)
			throw new ParseException(msg);
	}
	
	/**
	 * 获取元素的属性值，如果属性未定义或者值为空字符串，将返回null
	 * @param element
	 * @param attrName
	 * @return
	 */
	protected String getAttributeValueIngoreEmpty(Element element,String attrName)
	{
		String v=element.getAttribute(attrName);
		return v==null || v.length()==0 ? null : v;
	}
	
	/**
	 * 获取元素的属性值，空字符串也将被如实返回
	 * @param element
	 * @param attrName
	 * @return
	 */
	protected String getAttributeValue(Element element, String attrName)
	{
		Attr attr=element.getAttributeNode(attrName);
		return attr == null ? null : attr.getValue();
	}
	
	/**
	 * 获取元素文本内容
	 * @param element
	 * @return
	 */
	protected String getTextContent(Element element)
	{
		String re=element.getTextContent();
		
		return re==null || re.length()==0 ? null : re;
	}
	
	/**
	 * 将字符串转换为Arg对象
	 * @param strArg
	 * @param strType
	 * @return
	 * @date 2012-5-8
	 */
	protected Arg stringToArg(String strArg, String strType)
	{
		Arg re=null;
		
		Type argType=null;
		if(strType!=null && strType.length()>0)
			argType=nameToType(strType);
		
		if(strArg==null || strArg.length()==0)
			re=new ValueArg(strArg, argType);
		else
		{
			int len=strArg.length();
			char first=strArg.charAt(0);
			char end=strArg.charAt(len-1);
			
			//数值
			if(Character.isDigit(first))
			{
				Type wrappedType=(argType == null ? null : SbmUtils.wrapType(argType));
				
				if(Byte.class.equals(wrappedType))
				{
					re=new ValueArg(new Byte(strArg), argType);
				}
				else if(Short.class.equals(wrappedType))
				{
					re=new ValueArg(new Short(strArg), argType);
				}
				else if(Integer.class.equals(wrappedType))
				{
					re=new ValueArg(new Integer(strArg), argType);
				}
				else if(Long.class.equals(wrappedType))
				{
					re=new ValueArg(new Long(strArg), argType);
				}
				else if(Float.class.equals(wrappedType))
				{
					re=new ValueArg(new Float(strArg), argType);
				}
				else if(Double.class.equals(wrappedType))
				{
					re=new ValueArg(new Double(strArg), argType);
				}
				else if('L' == end)
				{
					re=new ValueArg(new Long(strArg.substring(0, len-1)), Long.class);
				}
				else if('l' == end)
				{
					re=new ValueArg(new Long(strArg.substring(0, len-1)), long.class);
				}
				else if('F' == end)
				{
					re=new ValueArg(new Float(strArg.substring(0, len-1)), Float.class);
				}
				else if('f'==end)
				{
					re=new ValueArg(new Float(strArg.substring(0, len-1)), float.class);
				}
				else if('D' == end)
				{
					re=new ValueArg(new Double(strArg.substring(0, len-1)), Double.class);
				}
				else if('d' == end)
				{
					re=new ValueArg(new Double(strArg.substring(0, len-1)), double.class);
				}
				else
				{
					boolean point=strArg.indexOf('.') >= 0;
					
					if(point)
						re=new ValueArg(new Double(strArg), argType);
					else
						re=new ValueArg(new Integer(strArg), argType);
				}
			}
			else if(first =='"')
			{
				String ue=SbmUtils.unEscape(strArg);
				len=ue.length();
				
				if(len<2 || ue.charAt(len-1) != '"')
					throw new ParseException("illegal String definition: "+strArg);
				
				if(len == 2)
					re=new ValueArg("", argType);
				else
					re=new ValueArg(ue.subSequence(1, len-1), argType);
			}
			else if(first =='\'')
			{
				String ue=SbmUtils.unEscape(strArg);
				len=ue.length();
				
				if(len!=3 || end!= '\'')
					throw new ParseException("illegal char definition: "+strArg);
				
				re=new ValueArg(ue.charAt(1), argType);
			}
			else if("true".equals(strArg))
			{
				re=new ValueArg(Boolean.TRUE, argType);
			}
			else if("false".equals(strArg))
			{
				re=new ValueArg(Boolean.FALSE, argType);
			}
			else if("null".equals(strArg))
			{
				re=new ValueArg(null, argType);
			}
			else
				re=new KeyArg(strArg, argType);
		}
		
		return re;
	}
	
	/**
	 * 创建空的配置对象，用于从配置文件解析并设置其属性
	 * @return
	 */
	protected Configuration createConfigurationInstance()
	{
		return new Configuration();
	}
	
	protected ResolverObjectFactory createResolverObjectFactoryInstance()
	{
		return new DefaultResolverObjectFactory();
	}
	
	/**
	 * 创建空的通用转换器对象，用于设置其属性
	 * @return
	 */
	protected GenericConverter createGenericConverterInstance()
	{
		return new DefaultGenericConverter();
	}
	
	/**
	 * 创建空的拦截器信息对象，用于设置其属性
	 * @return
	 */
	protected Interceptor createInterceptorInfoInstance()
	{
		return new Interceptor();
	}
	
	/**
	 * 创建空的可执行对象，用于从配置文件解析并设置其属性
	 * @return
	 */
	protected Executable createExecutableInstance(String type)
	{
		if(TAG_ACTION.equals(type))
			return createActionIntance();
		else if(TAG_INVOKE.equals(type))
			return createInvokeIntance();
		else
			throw new ParseException("illegal Executable type <"+type+">");
	}
	
	/**
	 * 创建空的动作对象，用于从配置文件解析并设置其属性
	 * @return
	 */
	protected Action createActionIntance()
	{
		return new Action();
	}
	
	/**
	 * 创建空的调用对象，用于从配置文件解析并设置其属性
	 * @return
	 */
	protected Invoke createInvokeIntance()
	{
		return new Invoke();
	}
	
	/**
	 * 创建对象
	 * @param clazz
	 * @return
	 */
	protected Object createClassInstance(String clazz)
	{
		try
		{
			return SbmUtils.narrowToClass(SbmUtils.nameToType(clazz)).newInstance();
		}
		catch(Exception e)
		{
			throw new ParseException(e);
		}
	}
	
	/**
	 * 由类型名称获取其类型
	 * @param type
	 * @return
	 * @date 2012-5-27
	 */
	protected Type nameToType(String type)
	{
		try
		{
			return SbmUtils.nameToType(type);
		}
		catch(Exception e)
		{
			throw new ParseException(e);
		}
	}
	
	/**
	 * 可执行对象代理，用于可执行对象引用的延迟初始化
	 * @author earthangry@gmail.com
	 * @date 2010-10-28
	 *
	 */
	protected static class ExecutableRefProxy implements Executable
	{
		private String refName;
		private String currentExecutablePrefix;
		
		public ExecutableRefProxy(String refName, String currentExecutablePrefix)
		{
			super();
			this.refName = refName;
			this.currentExecutablePrefix=currentExecutablePrefix;
		}

		public String getRefName() {
			return refName;
		}

		public void setRefName(String refName) {
			this.refName = refName;
		}

		public String getCurrentExecutablePrefix()
		{
			return currentExecutablePrefix;
		}

		public void setCurrentExecutablePrefix(String currentExecutablePrefix)
		{
			this.currentExecutablePrefix = currentExecutablePrefix;
		}

		//@Override
		public void execute(ObjectSource objectSource) throws ExecuteException
		{
			throw new UnsupportedOperationException();
		}

		//@Override
		public String getName()
		{
			throw new UnsupportedOperationException();
		}

		//@Override
		public String toString()
		{
			return Executable.class.getSimpleName()+" [name=" + refName + "]";
		}
	}
}
