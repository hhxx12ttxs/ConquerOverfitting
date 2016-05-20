/**  
* @(#)NewsGrabber.Java 1.00 2012/05/22  
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

import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jeasy.analysis.MMAnalyzer;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Text;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.HtmlPage;
import org.htmlparser.visitors.NodeVisitor;

/** NewsExtractor提供对给定新闻网页提取标题、时间、来源及正文的接口  */
public class NewsExtractor {
//	private static Boolean debug = false;
//	private static int startpos = 1501, samplecount = 100;
//	private static String sqlstate = "select * from " + table + " where Id >= (select Id from " + table + " order by Id limit " + (startpos - 1) + ", 1) limit " + samplecount;

	private static String table = "cat2news";
	private static String sqlstate = "select * from " + table + " where Url = 'http://www.mofcom.gov.cn/aarticle/ae/ai/201206/20120608163382.html'";
	
	// 单个网页测试
//	private static Boolean debug = true;
//	
//	/** 为true且在debug模式下，显示所有的文本处理过程 */	
//	public static Boolean showAll = false; // 显示所有的文本处理过程
//	/** 为true且在debug模式下，显示运行步骤 */
//	public static Boolean step = true; // 显示运行步骤
//	/** 为true且在debug模式下，跟踪来源搜索 */
//	public static Boolean sourceGen = false; // 跟踪来源搜索
//	/** 为true输出标题、时间 */
//	public static Boolean ttOutput = true; // 输出标题、时间
//	/** 为true输出来源信息 */
//	public static Boolean srcOutput = true; // 输出来源信息
//	/** 为true显示标题、时间、来源提取的辅助信息 */
//	public static Boolean ttDetails = false; // 显示标题、时间、来源提取的辅助信息
//	/** 为true输出正文 */
//	public static Boolean textOutput = true; // 输出正文
//	/** 为true显示新闻条目的分隔信息 */
//	public static Boolean urlxing = true; // 显示新闻条目的分隔信息
	
	/** 为true开启debug模式 */
	public static Boolean debug = false;
	/** 为true且在debug模式下，显示所有的文本处理过程 */	
	public static Boolean showAll = false; // 显示所有的文本处理过程
	/** 为true且在debug模式下，显示运行步骤 */
	public static Boolean step = false; // 显示运行步骤
	/** 为true且在debug模式下，跟踪来源搜索 */
	public static Boolean sourceGen = false; // 跟踪来源搜索
	/** 为true输出标题、时间 */
	public static Boolean ttOutput = false; // 输出标题、时间
	/** 为true输出来源信息 */
	public static Boolean srcOutput = false; // 输出来源信息
	/** 为true显示标题、时间、来源提取的辅助信息 */
	public static Boolean ttDetails = false; // 显示标题、时间、来源提取的辅助信息
	/** 为true输出正文 */
	public static Boolean textOutput = false; // 输出正文
	/** 为true显示新闻条目的分隔信息 */
	public static Boolean urlxing = false; // 显示新闻条目的分隔信息
	/** 中文分词器 */
	public static MMAnalyzer mmAnalyzer = new MMAnalyzer(); // 分词器
	
	private static int titletype = 0; // 标题类型 1：直接提取 2：正文标题（提取正文时获得） 3：指定标题（正文第一句话） 
	private static int timetype = 0; // 时间类型 1：直接提取 2：征文时间（提取正文时获得） 3：指定时间 （正文前的最后一个时间）
	private static int sourcetype = 0; // 来源类型 1:直接提取 2：指定来源（正文前的第一个来源） 3：正文来源（提取正文时获得）  4：搜索来源（根据媒体词的特点） 5：host
	private static int texttype = 0; // 正文类型 1：一次探查（非候选集信息多） 2：二次探查（根据关键词得到的信息少） 3：重要语句（根据文本重要性度量） 4：直接提取
	
	private static int minnum = 2;  // 首次正文提取的关键词个数阈值
	private static int potentialNum = 0; // 非候选集的重要文本数
	private static int hitonceNum = 0; // 非候选集中命中一个关键词的文本个数
	private static int longest = 0; // 非候选集中的最长文本长度
	private static int startPosition = 0; // 正文起始位置点
	private static String totalText = ""; // 最终提取的正文 
	private static String title = ""; // 网页标题
	private static String host = ""; // Url中的主站部分
	private static Boolean titleGot = false; // 标记是否找到网页标题
	private static Boolean timeGot = false; // 标记是否找到时间
	private static Boolean sourceGot = false; // 标记是否找到来源
	private static Boolean textTitleGot = false; // 标记是否找到正文中第一个标题节点
	private static Boolean textTimeGot = false; // 标记是否找到正文中第一个时间节点
	private static Boolean textSourceGot = false; // 标记是否找到正文中第一个来源节点 
	private static Boolean pointSourceGot = false; // 标记是否找到指定来源节点
	private static Boolean secondTimeSea = false; // 第二次找标题时间的标记
	private static Boolean titleTotime = false; // 从标题开始处寻找来源
	private static double firstSim = 0.45; // 第一次寻找标题时的相似度阈值
	private static double lastSim = 0.35; // 第二次找标题时相似度阈值放宽
	private static double textSim = 0.3; // 在正文中寻找正文标题的相似度阈值
	private static double avgtsDis = 0.0; // 时间与来源的平均距离
	private static int missingItemNum = 0; // 找不到标题、时间或正文的文章个数
	private static int falseSrc = 0; // 找不到真正标题的个数
	private static int avgtsNum = 0; // 辅助计算时间、来源的平均距离
	private static ResultSet res = null; // 数据库返回结果
	private static ArrayList<String> urlList = new ArrayList<String>(); // 网页网址
	private static ArrayList<String> webList = new ArrayList<String>(); // 网页代码
	private static ArrayList<String> hitList = new ArrayList<String>(); // 每一段文本包含的关键词
	private static ArrayList<Node> TextNodeList = new ArrayList<Node>(); // 候选集
	private static ArrayList<Integer> posList = new ArrayList<Integer>(); // 候选集的位置
	private static ArrayList<Node> potNodeList = new ArrayList<Node>(); // 非候选集
	private static ArrayList<Double> contentVal = new ArrayList<Double>(); // 非候选集的重要性
	private static ArrayList<Node> ttList = new ArrayList<Node>(); // 保存无标题情况下的标题候选
	private static ArrayList<Integer> ttHitList = new ArrayList<Integer>(); // 保存无标题情况下的标题候选的命中数
	private static ArrayList<Node> potentialSrc = new ArrayList<Node>(); // 保存可能的来源
	private static Parser parser = null; // 网页解析器
	private static HtmlPage html = null; // 网页内容
	private static Node titleNode = null; // 正文标题节点
	private static Node timeNode = null; // 正文时间节点
	private static Node sourceNode = null; // 来源节点
	private static Node textTitleNode = null; // 正文中第一个标题节点
	private static Node textTimeNode = null; // 正文中第一个时间节点
	private static Node textSourceNode = null; // 正文中第一个含有来源字样的节点
	private static Node pointTitleNode = null; // 取正文第一句话
	private static Node pointTimeNode = null; // 取正文之前最后一个时间点
	private static Node pointSourceNode = null; // 含有来源字样的节点，可以是任何类型、任何长度
	private static Node topNode = null; // 最小父节点，即正文起始节点
	private static Node longestStringNode = null; // 非候选集中的最长文本节点
	private static NodeFilter filter_meta = null; // meta节点过滤器
	private static NodeFilter filter_title = null; // title节点过滤器
	private static NodeList node_meta = null; // meta节点
	private static NodeList node_title = null; // title节点
	private static Set<String> classSet = new HashSet<String>(); // 统计候选集父节点的标签种类
	private static Set<String> keywordSet = new HashSet<String>(); // 关键词
	private static HashMap<Node, Integer> childCounter = new HashMap<Node, Integer>(); // 计算最小父节点的数据结构 
	private static HashMap<Node, Integer> generalTarget = new HashMap<Node, Integer>(); // 非候选集与其位置信息

	/** KeyWordChecker类的实例 */
	public KeyWordChecker kwc; // 关键词过滤器，去除数字、字符等
	/** 数据库接口 */
	public DatabaseUtils daUtils;  // 数据库接口
	
	/** 生成函数
	 * @param kwcout KeyWordChecker类的实例 */
	public NewsExtractor(KeyWordChecker kwcout) {
		kwc = kwcout;
		daUtils = new DatabaseUtils();
		System.out.println("NewsExtractor starts successfully!");
	}
	
	/** 返回分词器
	 * @return 分词器 */
	public MMAnalyzer getMMAnalyzer() {
		return mmAnalyzer;
	}

	/** 返回关键词集合
	 * @return 关键词集合 */
	public Set<String> getKeyword() {
		return keywordSet;
	}
	
	/** 装载网页网址与内容到相应的数据结构中
	 * @param sqlstate 以sql查询语句表示处理的网页 */
	public void extract(String sqlstate) {
		res = daUtils.sqlSelect(sqlstate);
		try {
			while (res.next()) {
				webList.add(res.getString("Content"));
				urlList.add(res.getString("Url"));
			}
			res.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 打印关键词列表 */
	public void keywordDisplay() {
		System.out.println("关键词列表：");
		Iterator<String> it = keywordSet.iterator();
		while (it.hasNext()) {
			System.out.print(it.next() + "    ");
		}
		System.out.println();
		System.out.println();
	}

	// 创建网页解析器
	private Parser createParser(String inputHTML) {
		Lexer mLexer = new Lexer(new Page(inputHTML));
		return new Parser(mLexer, new DefaultParserFeedback(
				DefaultParserFeedback.QUIET));
	}
	
	/** 打印关键词列表
	 * @param page 待解析网页内容
	 * @param url 待解析网页Url
	 * */
	public void extractKeyword(String page, String url) {
		String str = "";
		String[] words;
		
		try {
			// *****************************************清空数据结构***************************************************
			keywordSet.clear();
			title = "";
			host = "";
			startPosition = 0;
			titletype = 0;
			timetype = 0;
			sourcetype = 0;
			texttype = 0;
			titleGot = false;
			timeGot = false;
			sourceGot = false;
			textTimeGot = false;
			textTitleGot = false;
			textSourceGot = false;
			pointSourceGot = false;
			titleTotime = false;
			titleNode = null;
			timeNode = null;
			sourceNode = null;
			textTimeNode = null;
			textTitleNode = null;
			textSourceNode = null;
			pointTimeNode = null;
			pointTitleNode = null;
			pointSourceNode = null;
			lastSim = 0.35;
			
			URL myurl = new URL(url);
			host = myurl.getHost();
			
			// *****************************************从有用信息中提取正文关键词***************************************************
			// 准备解析，寻找title与meta节点
			parser = createParser(page);
			html = new HtmlPage(parser);
			parser.visitAllNodesWith(html);

			filter_meta = new NodeClassFilter(MetaTag.class);
			filter_title = new NodeClassFilter(TitleTag.class);

			// 找寻网页title，提取关键词
			parser.reset();
			node_title = parser.extractAllNodesThatMatch(filter_title);
			TitleTag tTag = (TitleTag) node_title.elementAt(0);
			if (null != tTag) {
				str = tTag.getTitle();
				title = textClean(str);
				words = mmAnalyzer.segment(str, " ").split(" ");
				for (String word : words) {
					if (word.length() < 2 || !kwc.isLegal(word)
							|| url.contains(word))
						continue;
					keywordSet.add(word);
				}
			} else {
				title = "";
			}

			// 找寻meta节点，提取关键词
			parser.reset();
			node_meta = parser.extractAllNodesThatMatch(filter_meta);
			for (int i = 0; i < node_meta.size(); i++) {
				MetaTag mTag = (MetaTag) node_meta.elementAt(i);
				String name = mTag.getAttribute("name");
				if (null == name || name.equals(""))
					continue;
				if (name.equalsIgnoreCase("keywords")
						|| name.equalsIgnoreCase("description")) {
					str = textClean(mTag.getAttribute("content"));
					
					words = mmAnalyzer.segment(str, " ").split(" ");
					for (String word : words) {
						if (word.length() < 2 || !kwc.isLegal(word)
								| url.contains(word))
							continue;
						keywordSet.add(word);
					}
				}
			}

			// 打印提取出的关键词
			if (debug)
				keywordDisplay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 查找时间、标题
	private void dealTT(Node string, String content) {
		// *****************************************标题处理***************************************************
		// 查看文本是否包含了网页标题，标题未必是在HeadingTag下的
		if (!title.equals("") && !titleGot && 3.5*content.length() >= title.length() && title.contains(content) && (title.indexOf(content) < 5) && keywordSet.size() >= 2) {
			if (debug) {
				System.out.println(string.getParent().getClass());
				System.out.println("包含的文本标题为：" + content);
				System.out.println(string.getParent().getStartPosition()
						+ "---------------"
						+ string.getParent().getEndPosition());
			}
			titleGot = true;
			titleNode = string;
			TextNodeList.clear();
			posList.clear();
			classSet.clear();
			return;
		}
		
		// 计算文本与标题的相似度
		double sim = StringSimilarity.getSimilarity(title, content);
		if(debug && showAll)
			System.out.println(sim);
		
		if(!secondTimeSea) {
			if (!title.equals("") && !titleGot && sim > firstSim && keywordSet.size() >= 2) {
				if (debug) {
					System.out.println(string.getParent().getClass());
					System.out.println("文本标题为：" + content);
					System.out.println("相似度为：" + sim);
					System.out.println(string.getParent().getStartPosition()
							+ "---------------"
							+ string.getParent().getEndPosition());
				}
				titleGot = true;
				titleNode = string;
				TextNodeList.clear();
				posList.clear();
				classSet.clear();
				return;
			}
		} else {
			if (sim >= lastSim && keywordSet.size() >= 2) {
				lastSim = sim;
				titleGot = true;
				titleNode = string;
			}
		}

		// *****************************************日期提取***************************************************
		// 查看可能的发布日期
		if (titleGot && !timeGot && kwc.isConDateFormat(content)) {
			if (debug) {
				System.out.println(string.getParent().getClass());
				System.out.println("发布日期为:" + content);
				System.out.println(string.getParent().getStartPosition()
						+ "---------------"
						+ string.getParent().getEndPosition());
			}
			
			if(!sourceGot && kwc.isSourceFormat(content)) {
				sourceGot = true;
				sourceNode = string;
			}
			timeGot = true;
			timeNode = string;
			return;
		}
	}
	
	// 对网页进行语义分析，提取候选集
	private void operate() {
		try {
			classSet.clear();
			posList.clear();
			TextNodeList.clear();
			generalTarget.clear();
			potNodeList.clear();
			contentVal.clear();
			potentialSrc.clear();
			potentialNum = 0;
			hitonceNum = 0;
			longest = 0;
			longestStringNode = null;
			
			// *****************************************文本节点访问器***************************************************
			parser.reset();
			NodeVisitor nVisitor = new NodeVisitor() {
				public void visitStringNode(Text string) {
					Node parent = string.getParent();
					if (parent == null)
						return;
					
					String content = textClean(string.getText().trim());
					if (titleGot && !pointSourceGot && kwc.isSourceFormat(content)) {
						pointSourceGot = true;
						pointSourceNode = string;
					}
					
					// *****************************************标签过滤***************************************************
					// 只考察部分标签
					if (!(parent instanceof Div
							|| parent instanceof ParagraphTag
							|| parent instanceof TableColumn
							|| parent instanceof HeadingTag || parent instanceof Span || parent instanceof LinkTag))
						// || parent instanceof Bullet
						return;

					// 如果在Div标签中，查看是不是不显示
					if (parent instanceof Div) {
						Div div = (Div) parent;
						String style = div.getAttribute("style");
						if (null != style && style.contains("display:none"))
							return;
					}
					
					// 如果在Span标签中，查看是不是不显示
					if (parent instanceof Span) {
						Span div = (Span) parent;
						String style = div.getAttribute("style");
						if (null != style && style.contains("display:none"))
							return;
					}
					
					if(debug && showAll) {
						System.out.println(parent.getClass());
						System.out.println(content);
					}
					
					// 查找时间、标题
					dealTT(string, content);
					
					if(titleGot && (string.getStartPosition() == titleNode.getStartPosition()) && string.getText().equals(titleNode.getText()))
						return;
					
					if (titleGot && !sourceGot && kwc.isSourceFormat(content)) {
							sourceGot = true;
							sourceNode = string;
					}
					
					if(titleGot && string.getStartPosition() == titleNode.getStartPosition() && string.getText().equals(titleNode.getText())) {
						TextNodeList.clear();
						posList.clear();
						classSet.clear();
					}
					
					// 如果在Div标签或者Span中，为排除导航等可能性，要对文本长度设置阈值
					if ((parent instanceof Span || parent instanceof Div)
							&& (content.length() <= 15))
						return;
					
					if(parent instanceof LinkTag)
						return;
					
					// 对于Heading标签，我们只考虑其为标题或时间
					if(titleGot && timeGot && parent instanceof HeadingTag)
						return;
					
					// 对于表示文章转载信息的语句，不予考虑
					if(kwc.isReproducedFormat(content) || kwc.isWeibo(content))
						return;
					
					// 处于导航部分，不予考虑
					if(content.contains("&gt"))
						return;
					
					// *****************************************非标题、非日期，进行分词统计***************************************************
					// 获取文本内容并进行分词统计
					hitList.clear();
					int count = 0;
					Iterator<String> it = keywordSet.iterator();
					while (it.hasNext()) {
						String key = (String) it.next();
						if (content.contains(key)) {
							hitList.add(key);
							count++;
						}
					}

					// *****************************************存在含有两个及以上关键词的文本***************************************************
					if (count >= minnum) {
						if (debug && !showAll) {
							// 输出满足关键词相关条件的文本内容
							System.out.println(parent.getClass());
							System.out.println(content);

							// 输出命中的关键词
							for (int i = 0; i < hitList.size(); i++) {
								System.out.print(hitList.get(i) + "    ");
							}
							System.out.println();

							// 输出起始、结束位置
							System.out.println(parent.getStartPosition()
									+ "---------------"
									+ parent.getEndPosition());
						}

						// 保存这一正文节点、内容、起始位置、节点类型
						TextNodeList.add(string);
						posList.add(parent.getStartPosition());
						classSet.add(parent.getClass().getName());
					} else {
						// *****************************************保存潜在正文***************************************************
						String tmpString = content.replaceAll(
								"[^\\u4e00-\\u9fa5]", "");
						int tmpLength = tmpString.length();
						
						if ((tmpLength > 10 && count == 1) || tmpLength > 20) 
							generalTarget.put(string, count);
						if (tmpLength > longest) {
							longest = tmpLength;
							longestStringNode = string;
						}
					}
				}
			};
			parser.visitAllNodesWith(nVisitor);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 再次搜索文章标题、日期
	private void searchTT() {
		if(debug && step)
			System.out.println("@-@ 标题、日期二次搜索：");
		
		try {
			parser.reset();
			NodeVisitor nVisitor = new NodeVisitor() {
				public void visitStringNode(Text string) {
					if(null != string.getParent() && string.getParent() instanceof TitleTag)
						return;
					if(string.getStartPosition() >= startPosition)
						return;
					String content = textClean(string.getText());
					dealTT(string, content);
					if (titleGot && !sourceGot && kwc.isSourceFormat(content)) {
						sourceGot = true;
						sourceNode = string;
					}
					if(kwc.isConDateFormat(content))
						pointTimeNode = string;
				}
			};
			parser.visitAllNodesWith(nVisitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	// 在没有网页标题的情况下搜索标题、日期
	private void searchTTwithoutTitle() {
		keywordSet.clear();
		ttList.clear();
		ttHitList.clear();
		String words[] = null;
		parser.reset();
		
		try {
			words = mmAnalyzer.segment(totalText, " ").split(" ");
			for (String word : words) {
				if (word.length() < 2 || !kwc.isLegal(word))
					continue;
				keywordSet.add(word);
			}
			
			if(debug && step)
				keywordDisplay();
			
			NodeVisitor nVisitor = new NodeVisitor() {
				public void visitStringNode(Text string) {
					if(null ==  string.getParent())
						return;
					if(string.getParent() instanceof TitleTag)
						return;
					
					if(string.getParent().getStartPosition() >= startPosition)
						return;
					String content = textClean(string.getText());
					if(content.length() <= 5)
						return;
					
					int count = 0;
					Iterator<String> it = keywordSet.iterator();
					while (it.hasNext()) {
						String key = (String) it.next();
						if (content.contains(key)) 
							count++;
					}
					
					if(kwc.isConDateFormat(content))
						pointTimeNode = string;
					
					if(count > 0 || kwc.isConDateFormat(content) || kwc.isSourceFormat(content)) {
						if(debug && step)
							System.out.println(content + "----" + count);
						ttList.add(string);
						ttHitList.add(count);
					}
				}
			};
			parser.visitAllNodesWith(nVisitor);
			
			if(ttList.size() == 1) 
				if(textClean(ttList.get(0).getText()).length() >= 50)
					return;
			
			int maxval = -1, maxindex = -1;
			
			for(int i=0; i<ttList.size(); i++) {
				if(ttHitList.get(i) > maxval) {
					maxval = ttHitList.get(i);
					maxindex = i;
				}
			}
			
			if(maxindex == -1)
				return;
			
			titleGot = true;
			titleNode = ttList.get(maxindex);
			
			for(int i=maxindex+1; i<ttList.size(); i++) { 
				if(kwc.isConDateFormat(textClean(ttList.get(i).getText()))) {
					timeGot = true;
					timeNode = ttList.get(i);
				}
				if(kwc.isSourceFormat(textClean(ttList.get(i).getText()))) {
					sourceGot = true;
					sourceNode = ttList.get(i);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 二次搜索来源
	private void searchSource() {
		if(!titleGot || !timeGot)
			return;
		if(debug && step)
			System.out.println("@-@ 开始搜索来源：");
		if(debug && sourceGen)
			System.out.println("标题--------时间：" + titleNode.getStartPosition() + "--------" + timeNode.getStartPosition());
		
		try {
			parser.reset();
			NodeVisitor nVisitor = new NodeVisitor() {
				public void visitStringNode(Text string) {
					// 从标题开始提取疑似来源的节点
					if(string.getStartPosition() < titleNode.getStartPosition())
						return;
					if((string.getStartPosition() == titleNode.getStartPosition()) && string.getText().equals(titleNode.getText())) {
						titleTotime = true;
						return;
					}
					
					String content = textClean(string.getText());
					if(debug && sourceGen)
						System.out.println(string.getText());
					
					if(titleTotime && kwc.isEnglishName(content)) {
						potentialSrc.add(string);
					} else if(titleTotime && kwc.isConChineseChar(content)) {
						content = content.replaceAll("[^\\u4e00-\\u9fa5]", "");
						if(content.length() <= 10)
							potentialSrc.add(string);
						else if (string.getText().equals(timeNode.getText())) 
							potentialSrc.add(string);
						else
							if(string.getStartPosition() > timeNode.getStartPosition())
								titleTotime = false;
					} 
				}
			};
			parser.visitAllNodesWith(nVisitor);
			
			// 疑似节点列表中选择最有可能是来源的节点
			if (potentialSrc.size() != 0) {
				int i=0;
				for( ; i<potentialSrc.size(); i++) {
					String tmpString = textClean(potentialSrc.get(i).getText());
					if(kwc.isSourceFormat(tmpString))
						break;
				}
				
				if(debug && sourceGen) 
					System.out.println("#######" + i);
				
				if(i < potentialSrc.size() - 1){
					String tmpString = textClean(potentialSrc.get(i).getText());
					tmpString = kwc.extractSource(tmpString);
					if(debug && sourceGen) 
						System.out.println(tmpString);
					
					if(!kwc.isTrueSource(tmpString)) {
						tmpString = textClean(potentialSrc.get(i+1).getText());
						if(kwc.isSourceFormat(tmpString) || kwc.isMediaWord(tmpString))
							tmpString = kwc.extractSource(tmpString);
		
						if(!kwc.isTrueSource(tmpString)) {
							if(i < potentialSrc.size() -2) {
								sourceGot = true;
								sourceNode = potentialSrc.get(i+2);
							} else {
								sourceGot = true;
								sourceNode = potentialSrc.get(i+1);
							}
						} else {
							sourceGot = true;
							sourceNode = potentialSrc.get(i+1);
						}
					} else {
						sourceGot = true;
						sourceNode = potentialSrc.get(i);
					}
				} else if (i == potentialSrc.size() - 1) {
					sourceGot = true;
					sourceNode = potentialSrc.get(i);
				} else {					
					// 枚举媒体名称					
					for(i=0; i<potentialSrc.size(); i++)
						if(kwc.isMediaWord(textClean(potentialSrc.get(i).getText())))
							break;
					
					if(i != potentialSrc.size()) {
						sourceGot = true;
						sourceNode = potentialSrc.get(i);
					} else {
						sourceGot = false;
						sourceNode = null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 搜寻每个节点的祖辈
	private void searchParent(Node node) {
		node = node.getParent();
		while (null != node) {
			if (childCounter.containsKey(node))
				childCounter.put(node, childCounter.get(node) + 1);
			else
				childCounter.put(node, 1);
			node = node.getParent();
		}
	}

	// 检查链接是否是下拉列表下的，该情况认为不是正文
	private Boolean checkLink(Node node) {
		while (null != node) {
			if (node instanceof Bullet)
				return false;
			node = node.getParent();
		}
		return true;
	}

	// 对候选集进行聚类
	private List<Integer> kmeans(List<Integer> list) {
		int len = list.size();
		List<Integer> midList = new ArrayList<Integer>();
		
		// 只有一个节点，如果非候选集重要文本数大于2，则认为候选集信息不够
		if (len == 1) {
			if(hitonceNum == 1 && potNodeList.size() == 1) 
				if(Math.abs(potNodeList.get(0).getStartPosition() - list.get(0)) < 1000)
					return null;
			
			if (potentialNum >= 2 || hitonceNum >= 2)
				return null;
			else
				midList.add(0);
		}

		// 只有两个节点，如果非候选集重要文本数大于2，则认为候选集信息不够
		// 两个节点距离很大，则取第一个
		if (len == 2) {
			if (potentialNum >= 2 || hitonceNum >= 2)
				return null;
			else {
				midList.add(0);
				if (Math.abs(list.get(0) - list.get(1)) < 2000)
					midList.add(1);
			}
		}

		// 有三或四个节点，如果非候选集重要文本数大于候选集的二倍，则认为候选集信息不够
		// 只取三个，去除两边与均值差别最大的一个，第一个保护程度更高
		if (len == 3 || len == 4) {
			if(potentialNum >= 3 || hitonceNum >= len)
				return null;
			double mean = 0.0;
			int pos = -1;
			for (int i = 0; i < len; i++)
				mean = mean + list.get(i);
			mean = mean / len;

			if (Math.abs(list.get(0) - mean) > 1000)
				pos = 0;
			if (Math.abs(list.get(len - 1) - mean) > 1000)
				pos = len - 1;

			for (int i = 0; i < len; i++)
				if (i != pos)
					midList.add(i);
		}

		// kmeans聚类，可能会出现空类，之后按照一定标准选取
		if (len > 4) {
			if (hitonceNum >= Math.round(1.5*len)) 
				return null;
			// 初始化类别
			List<Integer> labelList = new ArrayList<Integer>();
			for (int i = 0; i < len; i++)
				labelList.add(1);
			labelList.set(0, 0);
			labelList.set(len - 1, 2);
			// 初始化类均值
			double mean[] = { 0.0, 0.0, 0.0 };
			int num[] = { 1, len - 2, 1 };
			mean[0] = list.get(0);
			mean[2] = list.get(len - 1);
			for (int i = 1; i < len - 1; i++)
				mean[1] = mean[1] + list.get(i);
			mean[1] = (double) (mean[1]) / (len - 2);

			// 迭代停止标记
			boolean flag = true;
			double tmp1, tmp2, tmp3;
			int newlabel = 0;

			while (flag) {
				flag = false;
				for (int i = 0; i < len; i++) {
					tmp1 = Math.abs(list.get(i) - mean[0]);
					tmp2 = Math.abs(list.get(i) - mean[1]);
					tmp3 = Math.abs(list.get(i) - mean[2]);
					if (tmp1 < tmp2)
						if (tmp1 < tmp3)
							newlabel = 0;
						else
							newlabel = 2;
					else if (tmp2 < tmp3)
						newlabel = 1;
					else
						newlabel = 2;
					// 如果有元素的类别发生变化，则修改每一类的计数
					if (newlabel != labelList.get(i)) {
						num[newlabel]++;
						num[labelList.get(i)]--;
						labelList.set(i, newlabel);
						flag = true;
					}
				}

				for (int i = 0; i < 3; i++)
					if (num[i] != 0)
						mean[i] = 0;

				for (int i = 0; i < len; i++)
					mean[labelList.get(i)] += list.get(i);
				for (int i = 0; i < 3; i++)
					mean[i] = mean[i] / num[i];
			}

			if (debug)
				System.out.println(labelList.toString());

			// 如果第一类只有一个点且与第二类距离很近，则合并
			if (num[0] == 1 && num[1] == 1)
				if (Math.abs(posList.get(0) - posList.get(1)) < 500) {
					labelList.set(1, 0);
					num[0]++;
					num[1]--;
				}
			
			if (num[0] == 1 && num[1] > 1)
				if (Math.abs(posList.get(0) - posList.get(1)) < 500) {
					labelList.set(0, 1);
					num[0]--;
					num[1]++;
				}

			// 如果第二类很少，则取第一类和第三类较多的一个
			// 如果第二类较多，那么视情况合并第一类
			int max = 1;
			if (num[1] == 0) {
				if (num[0] >= num[2])
					max = 0;
				else
					max = 2;
				
				for (int i = 0; i < len; i++)
					if (labelList.get(i) == max)
						midList.add(i);
				return midList;
			} else if(num[1] == 1) {
				if (num[0] >= num[2])
					max = 0;
				else
					max = 2;
				
				int dis = 0, index = 0;
				for (int i = 0; i < len; i++) {
					if(labelList.get(i) == 1) {
						index = i;
						if(max == 0)
							dis = Math.abs(posList.get(i)-posList.get(i-1));
						else 
							dis = Math.abs(posList.get(i)-posList.get(i+1));
						break;
					}
				}
				
				if(dis < 2500)
					labelList.set(index, max);
				
				for (int i = 0; i < len; i++)
					if (labelList.get(i) == max)
						midList.add(i);
			} else {
				if (num[0] >= num[1]) {
					for (int i = 0; i < len; i++)
						if (labelList.get(i) == 0)
							labelList.set(i, 1);
				} else if (num[2] >= num[1]) {
					for (int i = 0; i < len; i++)
						if (labelList.get(i) == 2)
							labelList.set(i, 1);
				}

				for (int i = 0; i < len; i++)
					if (labelList.get(i) == max)
						midList.add(i);
			}
		}
		return midList;
	}
	
	// 寻找最小父节点
	private void minFather() {
		// topNode清空
		topNode = null;

		if (posList.size() == 0 || null == posList)
			return;

		// 寻找最小父节点map清空，首先对各节点的位置进行kmeans聚类，分3类
		List<Integer> midList = kmeans(posList);
		// 这种情况是候选集的信息量不足
		if (null == midList) {
			topNode = null;
			return;
		}
		if (debug) {
			System.out.println(posList.toString());
			System.out.println(midList.toString());
		}

		// 装载父节点map
		childCounter.clear();
		for (int j = 0; j < midList.size(); j++)
			searchParent(TextNodeList.get((Integer) midList.get(j)));

		// 对父节点map进行排序
		List<Map.Entry<Node, Integer>> nodes = new ArrayList<Map.Entry<Node, Integer>>(
				childCounter.entrySet());
		Collections.sort(nodes, new Comparator<Map.Entry<Node, Integer>>() {
			public int compare(Map.Entry<Node, Integer> o1,
					Map.Entry<Node, Integer> o2) {
				int first = o2.getValue() - o1.getValue();
				int second = o2.getKey().getStartPosition()
						- o1.getKey().getStartPosition();
				if (first > 0)
					return 1;
				else if (first == 0)
					if (second > 0)
						return 1;
				return -1;
			}
		});

		if (debug)
			for (int i = 0; i < nodes.size(); i++)
				System.out.println(nodes.get(i).getKey().getStartPosition()
						+ ": " + nodes.get(i).getValue());

		topNode = nodes.get(0).getKey();
		
		// 如果最小父节点在标题之后，不进行处理
		int curmax = nodes.get(0).getValue();
		if(titleGot) {
			if(debug && step) 
				System.out.println(topNode.getStartPosition() + "#####" + titleNode.getParent().getParent().getStartPosition());
			
			if((topNode.getStartPosition() + 100) < titleNode.getParent().getParent().getStartPosition()) {
				if(curmax == 1) {
					topNode = null;
				} else {
					for (int i = 0; i < nodes.size(); i++)
						if (nodes.get(i).getValue() < curmax) {
							topNode = nodes.get(i).getKey();
							break;
						}
				}
			}
			return;
		}
		
		// 如果最小父节点在我们排除的节点之前很多，我们代之以次优解
		if(midList.get(0) == 1 &&  (posList.get(0) - topNode.getStartPosition()) > 200) {
			if(curmax == 1) {
				topNode = null;
			} else {
				for (int i = 0; i < nodes.size(); i++)
					if (nodes.get(i).getValue() < curmax) {
						topNode = nodes.get(i).getKey();
						break;
					}
			}
		}
	}

	// 计算非候选集的重要性
	private void calTargetVal() {
		if (null == generalTarget || generalTarget.size() == 0)
			return;
		
		List<Map.Entry<Node, Integer>> contentList = new ArrayList<Map.Entry<Node, Integer>>(
				generalTarget.entrySet());
		Collections.sort(contentList,
				new Comparator<Map.Entry<Node, Integer>>() {
					public int compare(Map.Entry<Node, Integer> o1,
							Map.Entry<Node, Integer> o2) {
						if ((o2.getKey().getParent().getStartPosition() - o1
								.getKey().getParent().getStartPosition()) < 0)
							return 1;
						else
							return -1;
					}
				});
		
		if(generalTarget.size() == 1) {
			potNodeList.add(contentList.get(0).getKey());
			if(contentList.get(0).getValue() == 1)
				hitonceNum++;
			contentVal.add(0.0);
			if(debug) {
				if(minnum == 2)
					System.out.println("二命中：" + posList.size() + "---------一命中：" + hitonceNum);
				System.out.println(potNodeList.get(0).getParent()
						.getStartPosition()
						+ "---"
						+ contentList.get(0).getValue()
						+ "---"
						+ contentVal.get(0)
						+ ": "
						+ textClean(contentList.get(0).getKey().getText()));
			}
			return;
		}

		for (int i = 0; i < contentList.size() - 1; i++) {
			potNodeList.add(contentList.get(i).getKey());
			int pos1 = contentList.get(i).getKey().getParent()
					.getStartPosition();
			int pos2 = contentList.get(i + 1).getKey().getParent()
					.getStartPosition();
			String tmpString = contentList.get(i).getKey().getText().trim();
			if (pos1 == pos2)
				contentVal.add(1.0);
			else
				contentVal.add((double) tmpString.length() / (pos2 - pos1));
			// 如果不包含任何关键词，重要性要乘以0.8
			if (minnum == 2 && contentList.get(i).getValue() == 0)
				contentVal.set(i, contentVal.get(i) * 0.8);
			
			if(tmpString.length() <= 40)
				contentVal.set(i, contentVal.get(i) * 0.8);
		}
		
		// 最后一个也要放入，但是权重设置为0
		potNodeList.add(contentList.get(contentList.size()-1).getKey());
		contentVal.add(0.0);
		if(contentList.get(contentList.size()-1).getValue() == 1)
			hitonceNum++;
		
		for (int i = contentVal.size() - 2; i > 0; i--) {
			contentVal.set(i, (contentVal.get(i) + contentVal.get(i - 1)) / 2);
			if (contentVal.get(i) >= 0.5)
				potentialNum++;
			if(contentList.get(i).getValue() == 1 && contentVal.get(i) >= 0.1)
				hitonceNum++;
		}
		
		if(contentVal.get(0) >= 0.5)
			potentialNum++;
		if(contentList.get(0).getValue() == 1 && contentVal.get(0) >= 0.1)
			hitonceNum++;
		
		if (debug) {
			if(minnum == 2)
				System.out.println("二命中：" + posList.size() + "---------一命中：" + hitonceNum);
			for (int i = 0; i < contentList.size(); i++)
				System.out.println(potNodeList.get(i).getParent()
						.getStartPosition()
						+ "---"
						+ contentList.get(i).getValue()
						+ "---"
						+ contentVal.get(i)
						+ ": "
						+ textClean(contentList.get(i).getKey().getText()));
		}
	}

	// 直接从非候选集中选取文本
	private void directText() {
		if(null == potNodeList || potNodeList.size() == 0)
			return;
		if(debug && step)
			System.out.println("@-@ 直接选取文本段作为正文：");
		
		totalText = "";
		if(potNodeList.size() <= 3 && null != longestStringNode) {
			totalText = textClean(longestStringNode.getText());
			startPosition = longestStringNode.getStartPosition();
		} else {
			posList.clear();
			for(int j=0; j<potNodeList.size();j++) 
				posList.add(potNodeList.get(j).getParent().getStartPosition());
			
			// 从候选集最初开始找比较邻近的点作为正文起始点
			int startidx=1;
			if(titleGot)
				startidx = 0;
			for(int i=startidx; i<posList.size()-1; i++) {
				if(Math.abs(posList.get(i)-posList.get(i+1)) < 500) {
					startidx = i;
					break;
				}
				if(i > 0 && kwc.isConDateFormat(potNodeList.get(i-1).getText().replace("\r\n", "").trim())) {
					startidx = i;
					break;
				}
			}
			
			startPosition = potNodeList.get(startidx).getParent().getStartPosition();
			pointTitleNode = potNodeList.get(startidx);
			String tmpString = textClean(pointTitleNode.getText());
			totalText = totalText + tmpString;
			
			for(int j=startidx+1; j<posList.size(); j++) 
				if(posList.get(j) - posList.get(j-1) < 200) {
					tmpString = textClean(potNodeList.get(j).getText());
											
					if (!title.equals("") && !textTitleGot && 3.5*tmpString.length() >= title.length() && title.contains(tmpString) && (title.indexOf(tmpString) < 5)) {
						textTitleGot = true;
						textTitleNode = potNodeList.get(j);
					}
					
					double sim = StringSimilarity.getSimilarity(title, tmpString);
					if (!title.equals("") && !textTitleGot && sim > textSim) {
						textTitleGot = true;
						textTitleNode = potNodeList.get(j);
					}
					
					if (!textTimeGot && kwc.isConDateFormat(tmpString)) {
						textTimeGot = true;
						textTimeNode = potNodeList.get(j);
					}
			
					if (!textSourceGot && kwc.isSourceFormat(tmpString)) {
						textSourceGot = true;
						textSourceNode = potNodeList.get(j);
					}
					totalText = totalText + tmpString;
				}
				else 
					break;
		}
	}
	
	/** 输出最终结果 */
	public void resultDisplay() {
		if(ttDetails) {
			if(title.equals(""))
				System.out.println("找不到网页标题");
			else 
				System.out.println("网页标题：" + title);
			
		}
		
		if(ttOutput) {
			if(!titleGot)
				System.out.println("找不到正文标题");
			else 
				System.out.println("正文标题：" + textClean(titleNode.getText()));
			
			if(!timeGot)
				System.out.println("找不到发布时间");
			else 
				System.out.println("发布时间：" + kwc.extractDate(textClean(timeNode.getText())));
		}
		
		if(srcOutput) {
			if(!sourceGot) {
				System.out.println("消息来源：" + host);
//				System.out.println("找不到来源");
//				if(potentialSrc.size() == 0) {
//					System.out.println("没有媒体信息可找");
//				} else {
//					System.out.println("媒体信息不足：");
//					if(!debug)
//						for(int i=0; i<potentialSrc.size(); i++)
//							System.out.println(potentialSrc.get(i).getText());
//				}
			}
			else {
					if(kwc.isSourceFormat(textClean(sourceNode.getText())))
						System.out.println("消息来源：" + kwc.extractSource(textClean(sourceNode.getText())));
					else 
						if(kwc.isMediaWord(textClean(sourceNode.getText())))
							System.out.println("消息来源：" + kwc.extractSource(textClean(sourceNode.getText())));
						else
							System.out.println("消息来源：" + sourceNode.getText().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", ""));
			}
			
			if(sourceGen) {
				for(int i=0; i<potentialSrc.size(); i++)
					System.out.println(potentialSrc.get(i).getText());
			}
		}
		
		if(ttDetails) {
			if(null != textTitleNode)
				System.out.println("正文标题：" + textClean(textTitleNode.getText()));
			else
				System.out.println("不包含正文标题");
			
			if(null != textTimeNode)
				System.out.println("正文时间：" + kwc.extractDate(textClean(textTimeNode.getText())));
			else
				System.out.println("不包含正文时间");
			
			if(null != textSourceNode)
				System.out.println("正文来源：" + textClean(sourceNode.getText()));
			else
				System.out.println("不包含正文来源");
			
			if(null != pointTitleNode)
				System.out.println("指定标题：" + textClean(pointTitleNode.getText()));
			else
				System.out.println("不包含指定标题");
			
			if(null != pointTimeNode)
				System.out.println("指定时间：" + kwc.extractDate(textClean(pointTimeNode.getText())));
			else
				System.out.println("不包含指定时间");
			
			if(null != pointSourceNode)
				System.out.println("指定来源：" + textClean(pointSourceNode.getText()));
			else
				System.out.println("不包含指定来源");
		}
		
		if(textOutput)
			System.out.println("正文：" + totalText);

		if(urlxing)
			System.out.println("*******************************************************");
	}
	
	/** 清除文本中的编程标记或其它无效信息
	 * @param str 待清理的文本
	 * @return 清理后的结果 */
	public String textClean(String str) {   
        str = str.replaceAll("[\\n\\r]","");
		str = str.replaceAll("(&nbsp;)|(&nbsp)", " ");
		str = str.replaceAll("(&gt;)|(&gt)", " ");
		str = str.replaceAll("(&lt;)|(&lt)", " ");	
		str = str.replaceAll("(&quot;)|(&quot)", " ");
		str = str.replaceAll("(&raquo;)|(&raquo)", " ");
		return str.replaceAll("(&ldquo;)|(&ldquo)|(&rdquo;)|(&rdquo)", "").trim();
	}
	
	/** 对单个页面进行解析
	 * @param tmp 网页代码
	 * @param url 网页Url */
	public void extractSinglePage(String tmp, String url) {
		NodeVisitor nVisitor = new NodeVisitor() {
			public void visitStringNode(Text string) {
				Node parent = string.getParent();
				String tmpString = textClean(string.getText());
					
				if (!title.equals("") && !textTitleGot && 2*tmpString.length() > title.length() && title.contains(tmpString)  && (title.indexOf(tmpString) < 5)) {
					textTitleGot = true;
					textTitleNode = string;
				}
				
				double sim = StringSimilarity.getSimilarity(title, tmpString);
				if (!title.equals("") && !textTitleGot && sim > textSim) {
					textTitleGot = true;
					textTitleNode = string;
				}
				
				if (!textTimeGot && kwc.isConDateFormat(tmpString)) {
					textTimeGot = true;
					textTimeNode = string;
				}
				
				if(!textSourceGot && kwc.isSourceFormat(tmpString)) {
					textSourceGot = true;
					textSourceNode = string;
				}
				
				if(titleGot && string.getStartPosition() == titleNode.getStartPosition() && string.getText().equals(titleNode.getText())) {
					totalText = "";
					return;	
				}
				
				if(timeGot && string.getStartPosition() == timeNode.getStartPosition() && string.getText().equals(timeNode.getText())) {
					totalText = "";
					return;	
				}
				
				if (parent instanceof ParagraphTag || parent instanceof Div
						|| parent instanceof TableColumn
						|| parent instanceof HeadingTag
						|| parent instanceof Span) {
					if(totalText.equals(""))
						pointTitleNode = string;
					totalText = totalText + tmpString;
				} else if (parent instanceof LinkTag && checkLink(parent)) {
					if(totalText.equals(""))
						pointTitleNode = string;
					totalText = totalText + textClean(string.getText());
				}
			}
		};

		//  页面解析开始
		if(urlxing)
			System.out.println(url);
		
		//*****************************************过滤Javascript代码***************************************************
		String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
		Pattern p_script = Pattern.compile(regEx_script,
				Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(tmp);
		tmp = m_script.replaceAll(""); // 过滤script标签
		
		//*****************************************过滤链接***************************************************
//		String regEx_href = "<a\\s*href\\s*=.+?>";
//		Pattern l_script = Pattern.compile(regEx_href,
//				Pattern.CASE_INSENSITIVE);
//		Matcher r_script = l_script.matcher(tmp);
//		tmp = r_script.replaceAll("");
//		tmp = tmp.replaceAll("</a>", "");
				
		//*****************************************处理网页文本，填充数据结构***************************************************
		extractKeyword(tmp, url);
		operate();
		calTargetVal();
		minFather();
		totalText = "";
		
		//*****************************************有正文则输出，没有正文采取特殊处理***************************************************
		if (null != topNode) {
			texttype = 1;
			startPosition = topNode.getStartPosition();
			if (debug)
				System.out.println("起始位置：" + startPosition);
			topNode.accept(nVisitor);
		} else {
			// 候选集信息量不足
			if(keywordSet.size() < 2) {
				texttype = 4;
				directText();
			} else {
				if(debug && step)
					System.out.println("@-@ 进行二次探查：");
				minnum = 1;
				operate();
				calTargetVal();
				minFather();
				minnum = 2;
				totalText = "";
				if(null != topNode) {
					texttype = 2;
					startPosition = topNode.getStartPosition();
					if (debug)
						System.out.println("起始位置：" + startPosition);
					topNode.accept(nVisitor);
				} else {
					// 候选集信息量仍旧不足，很可能的原因是没有提供meta与title的信息
					if(debug && step)
						System.out.println("@-@ 根据语句重要性评估提取正文：");
					potentialNum = 0;
					TextNodeList.clear();
					posList.clear();
					
					for(int j=0; j<potNodeList.size()-1;j++) {
						// 在这里选取的是成片的疑似正文
						if(contentVal.get(j) >= 0.5) {
							TextNodeList.add(potNodeList.get(j));
							posList.add(potNodeList.get(j).getParent().getStartPosition());
						}
					}
					minFather();
					totalText = "";
					if(null != topNode) {
						texttype = 3;
						startPosition = topNode.getStartPosition();
						if (debug)
							System.out.println("起始位置：" + startPosition);
						topNode.accept(nVisitor);
					} else {
						texttype = 4;
						// 非候选集的重要性没有那么高，我们只能直接从非候选集里提取，可能的一种情况是正文很短
						directText();
					}
				}
			}
		}
		
//		if(titleGot && timeGot && !debug)
//			continue;
	
		if ((!titleGot && !title.equals("") && keywordSet.size() > 2) || (titleGot && !timeGot)) {
			secondTimeSea = true;
			searchTT();
			secondTimeSea = false;
		}
		else if (title.equals("") || keywordSet.size() < 2) {
			if(debug && step)
				System.out.println("@-@ 网页标题不存在或不可靠，开始使用关键词搜索标题、日期：");
			searchTTwithoutTitle();
		}
		
		// 综合考虑所有情况，确定标题、时间和来源节点
		if(!titleGot) {
			if(textTitleGot) {
				titletype = 2;
				titleNode = textTitleNode;
				titleGot = true;
			} else {
				if(null != pointTitleNode) {
					titletype = 3;
					titleNode = pointTitleNode;
					titleGot = true;
				} else 
					titleNode = null;
			}
		} else {
			titletype = 1;
		}
		
		if(!timeGot) {
			if(textTimeGot) {
				timetype = 2;
				timeNode = textTimeNode;
				timeGot = true;
			} else
				if(null != pointTimeNode) {
					timetype = 3;
					timeNode = pointTimeNode;
					timeGot = true;
				} else 
					timeNode = null;
		} else {
			timetype = 1;
		}		
		
		Node tmpNode = null;
		String sourceTmp = "";
		if(sourceGot) { 
			tmpNode = sourceNode;
			sourceGot = false;
			sourceNode = null;
		}
		searchSource();
		
		if(!sourceGot) {
			if(null != tmpNode) {
				sourceGot = true;
				sourceNode = tmpNode;
			} else {
				if(pointSourceGot) {
					sourceGot = true;
					sourceNode = pointSourceNode;
				} else if(textSourceGot) {
					sourceGot = true;
					sourceNode = textSourceNode;
				}
			}				
		}
		
		if(sourceGot) {
			sourceTmp = textClean(sourceNode.getText());
			if(kwc.isSourceFormat(sourceTmp) || kwc.isMediaWord(sourceTmp))
				sourceTmp = kwc.extractSource(sourceTmp);
			if(sourceTmp.length() > 50 || !kwc.isTrueSource(sourceTmp)) {
				sourceGot = false;
				sourceNode = null;
			}
		}
		
		if(sourceGot && timeGot && (Math.abs(sourceNode.getStartPosition() - timeNode.getStartPosition())) > 1000) {
			sourceGot = false;
			sourceNode = null;
		}
		
		if(!sourceGot) {
			sourcetype = 5;
		} else {
			if(null != tmpNode && tmpNode.getStartPosition() == sourceNode.getStartPosition()) 
				sourcetype = 1;
			else if(pointSourceGot && pointSourceNode.getStartPosition() == sourceNode.getStartPosition())
				sourcetype = 2;
			else if(textSourceGot && textSourceNode.getStartPosition() == sourceNode.getStartPosition())
				sourcetype = 3;
			else
				sourcetype = 4;  
		}
		
		if(!timeGot || !titleGot || totalText.equals(""))
			missingItemNum++;
		
		if(!sourceGot)
			falseSrc++;
		
		if(timeGot && sourceGot) {
			avgtsNum++;
			avgtsDis = avgtsDis + Math.abs(timeNode.getStartPosition() - sourceNode.getStartPosition());
		}
		
		resultDisplay();
	}
	
	
	/** 返回新闻标题
	 * @return 新闻标题 */
	public String getTitle() {
		if(!titleGot)
			return "未找到新闻标题";
		else 
			return textClean(titleNode.getText());
	}

	/** 返回发布时间
	 * @return 发布时间 */
	public String getTime() {			
		if(!timeGot)
			return "未找到发布时间";
		else 
			return kwc.extractDate(textClean(timeNode.getText()));
		
	}
		
	/** 返回消息来源
	 * @return 消息来源 */
	public String getSource() {
		if(!sourceGot) 
			return host;
		else {
			if(kwc.isSourceFormat(textClean(sourceNode.getText())))
				return kwc.extractSource(textClean(sourceNode.getText()));
			else 
				if(kwc.isMediaWord(textClean(sourceNode.getText())))
					return kwc.extractSource(textClean(sourceNode.getText()));
				else
					return sourceNode.getText().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
		}
	}
	
	/** 返回正文
	 * @return 正文 */
	public String getText() {
		return totalText;
	}
	
	/** 返回标题提取方式
	 * @return 标题提取方式 */
	public int getTitleType() {
		return titletype;
	}
	
	/** 返回时间提取方式
	 * @return 时间提取方式 */
	public int getTimeType() {
		return timetype;
	}
	
	/** 返回来源提取方式
	 * @return 来源提取方式 */
	public int getSourceType() {
		return sourcetype;
	}
	
	/** 返回正文提取方式
	 * @return 正文提取方式 */
	public int getTextType() {
		return texttype;
	}
	
	/** 返回疑似来源列表
	 * @return 疑似来源列表 */
	public ArrayList<String> getPTSrc() {
		ArrayList<String> ptList = new ArrayList<String>();
		if(potentialSrc.size() != 0) 
			for(int i=0; i<potentialSrc.size(); i++) {
				if(kwc.isMediaWord(textClean(potentialSrc.get(i).getText())))
					continue;
				ptList.add(textClean(potentialSrc.get(i).getText()).replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", ""));
			}
		return ptList;
	}
	
//	public static void main(String args[]) {
//		KeyWordChecker kwc = new KeyWordChecker(DatabaseInfo.uselesspath, DatabaseInfo.mediapath);
//		NewsExtractor nex = new NewsExtractor(kwc);
//		nex.extract(sqlstate);
//		for(int i=0; i<urlList.size(); i++)
//			nex.extractSinglePage(webList.get(i), urlList.get(i));
//	}
	
}
