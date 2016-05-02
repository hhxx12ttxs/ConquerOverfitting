//package org.ansj.util.newWordFind;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeSet;
//
//import love.cq.util.IOUtil;
//
//import org.ansj.dic.DicReader;
//import org.ansj.domain.Term;
//import org.ansj.splitWord.analysis.ToAnalysis;
//import org.ansj.util.recognition.NatureRecognition;
//
//public class NewWordFind2 {
//
//	public static TreeSet<NewTerm> getNewWords(String content,int record,int way) throws IOException {
//		TreeSet<NewTerm> score = new TreeSet<NewTerm>();
//		HashMap<String, NewTerm> hash = new HashMap<String, NewTerm>();
//
//		/**
//		 * 分词得到结果,列表
//		 */
//		LinkedList<NewTerm> all = listTerm(content,record);
//
//		for (int i = 1; i < 5; i++) {
//			/**
//			 * 构建hashmap的倒排索引
//			 */
//			HashMap<String, Map<String, List<NewTerm>>> hm = makeIndex(all,way);
//			//System.out.println(hm);
//
//			/**
//			 * 如果返回true说明没有可合并的了
//			 */
//			if (mergerTop(hm)) {
//				break;
//			}
//
//			NewTerm tempTerm;
//			for (NewTerm charcterTerm : all) {
//				if (charcterTerm.getVersion() > 0) {
//					if ((tempTerm = hash.get(charcterTerm.getName())) != null) {
//						tempTerm.updateScore(charcterTerm);
//					} else {
//						hash.put(charcterTerm.getName(), new NewTerm(charcterTerm));
//					}
//				}
//			}
//
//			clearn(all);
//		}
//		score.addAll(hash.values());
//		return score;
//	}
//
//	private static void clearn(LinkedList<NewTerm> all) {
//		// TODO Auto-generated method stub
//		Iterator<NewTerm> iterator = all.iterator();
//		NewTerm temp = null;
//		while (iterator.hasNext()) {
//			temp = iterator.next();
//			if (temp.isRemove()) {
//				iterator.remove();
//			} else {
//				temp.clean();
//			}
//		}
//	}
//
//	private static boolean mergerTop(HashMap<String, Map<String, List<NewTerm>>> hm) {
//		/**
//		 * 计算top5的重复数字
//		 */
//		List<List<NewTerm>> all = new ArrayList<List<NewTerm>>();
//		for (Map<String, List<NewTerm>> index : hm.values()) {
//			for (List<NewTerm> segement : index.values()) {
//				all.add(segement);
//			}
//		}
//
//		Object temp = null;
//
//		List<NewTerm> tempI;
//		List<NewTerm> tempJ;
//
//		Object[] array = all.toArray();
//
//		for (int i = 0; i < array.length; i++) {
//			for (int j = i; j < array.length; j++) {
//				tempI = (List<NewTerm>) array[i];
//				tempJ = (List<NewTerm>) array[j];
//				if (tempI.size() < tempJ.size()) {
//					temp = array[i];
//					array[i] = array[j];
//					array[j] = temp;
//				}
//			}
//		}
//
//		List<NewTerm> merger;
//		int min = Math.min(array.length, 5);
//		if (min < 1) {
//			return true;
//		}
//		for (int i = 0; i < min; i++) {
//			merger = (List<NewTerm>) array[i];
//			for (NewTerm charcterTerm : merger) {
//				charcterTerm.mergerFrom();
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * 构建倒排索引
//	 * 
//	 * @param all
//	 * @return
//	 */
//	private static HashMap<String, Map<String, List<NewTerm>>> makeIndex(LinkedList<NewTerm> all,int way) {
//		NewTerm term = null;
//		Iterator<NewTerm> iterator = all.iterator();
//
//		HashMap<String, Map<String, List<NewTerm>>> hm = new HashMap<String, Map<String, List<NewTerm>>>();
//
//		Map<String, List<NewTerm>> tempBegin = null;
//
//		NewTerm tempTerm = null;
//		NewTerm secondTerm = null;
//
//		List<NewTerm> chartCharcterTerms = null;
//		
//		while (iterator.hasNext()) {
//			term = iterator.next();
//			if (term.isRemove()) {
//				iterator.remove();
//				continue;
//			}
//
//			if (tempTerm == null) {
//				tempTerm = term;
//			} else {
//				if(way == 0){
//					
//				}else if(way == 1){
//					if(secondTerm != null){//新词发现改进部分
//						Map<String, List<NewTerm>> secondTempBegin = createTempBegin(secondTerm,term);
//						hm.put(secondTerm.getName(),secondTempBegin);
//					}
//				}
//				
//				
//				String name = term.getName();
//				if (tempTerm.getOffe() + tempTerm.getName().length() != term.getOffe()) {
//					tempTerm = term;
//					continue;
//				}
//				tempBegin = hm.get(tempTerm.getName());
//
//				term.setFrom(tempTerm);
//				if (tempBegin == null) {
//					tempBegin = new HashMap<String, List<NewTerm>>();
//					chartCharcterTerms = new ArrayList<NewTerm>();
//					chartCharcterTerms.add(term);
//					tempBegin.put(term.getName(), chartCharcterTerms);
//				} else {
//					chartCharcterTerms = tempBegin.get(name);
//					if (chartCharcterTerms == null) {
//						chartCharcterTerms = new ArrayList<NewTerm>();
//						tempBegin.put(term.getName(), chartCharcterTerms);
//					}
//					chartCharcterTerms.add(term);
//				}
//				hm.put(tempTerm.getName(), tempBegin);
//				secondTerm = term;
//			}
//		}
//		return hm;
//	}
//
//	/**
//	 * @param tempTerm
//	 * @param term
//	 * @return
//	 */
//	private static Map<String, List<NewTerm>> createTempBegin(NewTerm tempTerm,NewTerm term){
//		List<NewTerm> chartCharcterTerms = null;
//		Map<String, List<NewTerm>> tempBegin = null;
//		term.setFrom(tempTerm);
//		if (tempBegin == null) {
//			tempBegin = new HashMap<String, List<NewTerm>>();
//			chartCharcterTerms = new ArrayList<NewTerm>();
//			chartCharcterTerms.add(term);
//			tempBegin.put(term.getName(), chartCharcterTerms);
//		} 
////		else {
////			chartCharcterTerms = tempBegin.get(term.getName());
////			if (chartCharcterTerms == null) {
////				chartCharcterTerms = new ArrayList<NewTerm>();
////				tempBegin.put(term.getName(), chartCharcterTerms);
////			}
////			chartCharcterTerms.add(term);
////		}
//		return tempBegin;
//	}
//	
//	/**
//	 * 获得分词后的结果
//	 * 
//	 * @param content
//	 * @return
//	 * @throws IOException
//	 */
//	private static LinkedList<NewTerm> listTerm(String content,int record) throws IOException {
//		List<Term> resultList = new ArrayList<Term>();
//		List<Term> terms = ToAnalysis.paser(content);
//
//		new NatureRecognition(terms).recogntion();
//
//		resultList.addAll(terms);
//
//		Iterator<Term> iterator = resultList.iterator();
//
//		LinkedList<NewTerm> all = new LinkedList<NewTerm>();
//		Term term = null;
//		while (iterator.hasNext()) {
//			term = iterator.next();
//			String natureStr = term.getNatrue().natureStr;
//			if (hs.contains(term.getName()) || natureStr.equals("null") || natureStr.contains("w") || natureStr.equals("r") || natureStr.contains("m")
//					|| natureStr.contains("q") || natureStr.toString().contains("a") || natureStr.contains("f")) {
//				iterator.remove();
//				continue;
//			} else {
//				all.add(new NewTerm(term));
//			}
//		}
//		return all;
//	}
//
//	private static HashSet<String> hs = new HashSet<String>();
//	
//	static{
//		try {
//			BufferedReader filter = DicReader.getReaderResource("library/stop/stopLibrary.dic");
//			String temp = null;
//			while ((temp = filter.readLine()) != null) {
//				temp = temp.trim().toLowerCase();
//				hs.add(temp);
//			}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void main(String[] args) throws IOException {
//		
//		String content = "DOMDOM分为HTML DOM和XML DOM两种。它们分别定义了访问和操作HTML/XML文档的标准方法，并将对应的文档呈现为带有元素、属性和文本的树结构（节点树） 1）DOM树定义了HTML/XML文档的逻辑结构，给出了一种应用程序访问和处理XML文档的方法。 2）在DOM树中，有一个根节点，所有其他的节点都是根节点的后代。 3) 在应用过程中，基于DOM的HTML/XML分析器将一个HTML/XML文档转换成一棵DOM树，应用程序通过对DOM树的操作，来实现对HTML/XML文档数据的操作。DOM= Document Object Model，文档对象模型，DOM可以以一种独立于平台和语言的方式访问和修改一个文档的内容和结构。换句话说，这是表示和处理一个HTML或XML文档的常用方法。有一点很重要，DOM的设计是以对象管理组织（OMG）的规约为基础的，因此可以用于任何编程语言.•               D：文档 – html 文档 或 xml 文档•         O：对象 – document 对象的属性和方法•         M：模型•               DOM 是针对xml(html)的基于树的API。•               DOM树:节点（node）的层次。•               DOM 把一个文档表示为一棵家谱树（父，子，兄弟）•               DOM定义了Node的接口以及许多种节点类型来表示XML节点的多个方面DOM的结构<html>:       <head>              <title>HTML DOM</title>       </head>       <body>       <h1>DOM的结构</h1>       <p><a href=”href”>链接</a></p></body></html>html是根节点，head，body是html的子节点，title是head的子节点，body是h1，p的父节点 节点l       根据 DOM，HTML 文档中的每个成分都是一个节点。DOM 是这样规定的：•         整个文档是一个文档节点 •         每个 HTML 标签是一个元素节点 •         包含在 HTML 元素中的文本是文本节点 •         每一个 HTML 属性是一个属性节点 •         注释属于注释节点 Node 层次l       节点彼此都有等级关系。HTML 文档中的所有节点组成了一个文档树（或节点树）。HTML 文档中的每个元素、属性、文本等都代表着树中的一个节点。树起始于文档节点，并由此继续伸出枝条，直到处于这棵树最低级别的所有文本节点为止。节点及其类型l       节点       由结构图中我们可以看到，整个文档就是一个文档节点。        而每一个HMTL标签都是一个元素节点。        标签中的文字则是文本节点。        标签的属性是属性节点。        一切都是节点……l       节点树        节点树的概念从图中一目了然，最上面的就是“树根”了。节点之间有父子关系，祖先与子孙关系，兄妹关系。这些关系从图中也很好看出来，直接连线的就是父子关系了。而有一个父亲的就是兄妹关系……  NODE接口的特性和方法        特性/方法     类型/返回类型     说    明         nodeName     String     节点的名字；根据节点的类型而定义         nodeValue     String     节点的值；根据节点的类型而定义         nodeType     Number     节点的类型常量值之一         ownerDocument     Document     指向这个节点所属的文档         firstChild     Node     指向在childNodes列表中的第一个节点         lastChild     Node     指向在childNodes列表中的最后一个节点         childNodes     NodeList     所有子节点的列表         previousSibling     Node     指向前一个兄弟节点；如果这个节点就是第一个兄 弟节点，那么该值为null         nextSibling     Node     指向后一个兄弟节点；如果这个节点就是最后一个兄 弟节点，那么该值为null         hasChildNodes()     Boolean     当childNodes包含一个或多个节点时，返回真         attributes     NamedNodeMap     包含了代表一个元素的特性的Attr对象；仅用于 Element节点         appendChild(node)       Node     将node添加到childNodes的末尾         removeChild(node)     Node     从childNodes中删除node         replaceChild (newnode, oldnode)     Node     将childNodes中的oldnode替换成newnode         insertBefore (newnode, refnode)     Node     在childNodes中的refnode之前插入newnode        查找并访问节点你可通过若干种方法来查找您希望操作的元素：通过使用 getElementById() 和 getElementsByTagName() 方法 通过使用一个元素节点的 parentNode、firstChild 以及 lastChild 属性 查找元素节点1）getElementById() var element = document.getElementById ( ID )2）getElementsByName()<input type='text' name='tname' value='国庆60年_1' /><br><input type='text' name='tname' value='国庆60年_2' /><br><input type='text' name='tname' value='国庆60年_3' /><br> function test(){       var tnameArray=document.getElementsByName('tname');  alert(tnameArray.length);        for(var i=0;i<tnameArray.length;i++){               window.alert(tnameArray[i].value);            }         }3）getElementsByTagName()var elements = document.getElementsByTagName(tagName);     var elements = element.getElementsByTagName(tagName);或var container =   document.getElementById(“sid”);   var elements = container.getElementsByTagName(“p”);   alert(elements .length);处理inputvar inputElements=document.getElementsByTagName('input');    //输出input标签的长度alert(inputElements.length);       for(var i=0;i<inputElements.length;i++){      if(inputElements[i].type!=\'button\'){alert(inputElements[i].value);}}处理select//获取select标签var selectElements=document.getElementsByTagName('select');     //获取select下的子标签  for(var j=0;j<selectElements.length;j++){       var optionElements=selectElements[j].getElementsByTagName('option');       for(var i=0;i<optionElements.length;i++){            alert(optionElements[i].value); } }一些常用的访问节点的属性及用法parentNode：父节点、firstChil：第一个子节点、lastChild最后一个子节点hasChildNodes()   :该方法用来检查一个元素是否有子节点，返回值是 true 或 false.  var booleanValue = element.hasChildNodes();文本节点和属性节点不可能再包含任何子节点，所以对这两类节点使用 hasChildNodes 方法的返回值永远是 false.如果 hasChildNodes 方法的返回值是 false，则 childNodes,firstChild,lastChild 将是空数组和空字符串。Document。documentElement：返回存在于 XML 以及 HTML 文档中的文档根节点document.body ：是对 HTML 页面的特殊扩展，提供了对 <body> 标签的直接访问nodeName（节点名称） nodeValue（节点值） nodeType（节点类型）  注：nodeName 所包含的 XML 元素的标签名称永远是大写的nodeName 是一个只读属性。 案例：获取节点的名称及value<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'><html>    <head>       <title>demo01.html</title>       <meta http-equiv='keywords' content='keyword1,keyword2,keyword3'>       <meta http-equiv='description' content='this is my page'>       <meta http-equiv='content-type' content='text/html; charset=UTF-8'>       <!--<link rel='stylesheet' type='text/css' href='./styles.css'>-->    </head>    <body>       <h1>           你好，kouxiaolin       </h1>    </body></html><script type='text/javascript'><!--window.onload=function(){var root=document.documentElement;alert('跟标签的名称：'+root.nodeName); var ss=root.firstChild;alert('root的第一个子标签head：'+ss.nodeName); var ee=root.lastChild;alert('root的最后一个子标签body：'+ee.nodeName); var body=document.body;alert('body标签：'+body.nodeName); var h1=body.firstChild;alert('body标签的第一个子标签h1：'+h1.nodeName); var tt=h1.firstChild;alert('h1标签的子标签名称：'+tt.nodeName);alert('h1标签的文本内容：'+tt.nodeValue);}//  -->  </script>遍历select中所有的option对象的文本值<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'><html>    <head>       <title>Demo02.html</title>       <meta http-equiv='keywords' content='keyword1,keyword2,keyword3'>       <meta http-equiv='description' content='this is my page'>       <meta http-equiv='content-type' content='text/html; charset=UTF-8'>       <!--<link rel='stylesheet' type='text/css' href='./styles.css'>-->       <script type='text/javascript'>window.onload = function() {    //通过getElementById('edu')方法获取select的对象    var edus = document.getElementById('edu');    //通过select中options集合获取所有的option对象    var edus1  =   edus.options;    var msg ='';    //遍历所有的option对象的文本值      for(var i=0;i<edus1.length;i++){          msg+=edus1[i].innerHTML+',';      }     //把获取的msg信息写入到showMsg的div中     document.getElementById('showMsg').innerHTML=msg;    }</script>    </head>    <body>    <div>       <form action=''>       用户名:<input type='text' name='name' />       学历:<select id='edu' name='education'>           <option value='大专'>大专生</option>           <option value='本科'>本科生</option>           <option value='硕士'>硕士生</option>           <option value='博士'>博士生</option>       </select>       <input type='submit' value='注册' />       </form>    </div>          用户的学历是：   <div id='showMsg'>   </div>    </body></html>或<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'><html>    <head>       <title>Demo02.html</title>       <meta http-equiv='keywords' content='keyword1,keyword2,keyword3'>       <meta http-equiv='description' content='this is my page'>       <meta http-equiv='content-type' content='text/html; charset=UTF-8'>       <!--<link rel='stylesheet' type='text/css' href='./styles.css'>-->       <script type='text/javascript'>window.onload = function() {    //通过getElementById('edu')方法获取select的对象    var edus = document.getElementById('edu');    var edus1 = edus.childNodes;    alert(edus1.length);    var fc = edus.firstChild;    alert(fc.nodeName);    var lc = edus.lastChild;    alert(lc.nodeName);    var llc = lc.firstChild;    alert(llc.nodeValue);     var msg = '';    for ( var i = 0; i < edus1.length; i++) {       if (edus1[i].nodeType == 1)           msg += edus1[i].lastChild.nodeValue + ',';    }    document.getElementById('showMsg').innerHTML = msg;}</script>    </head>    <body>       <div>           <form action=''>              用户名:              <input type='text' name='name' />              学历:              <select id='edu' name='education'>                  <option value='大专'>                     大专生                  </option>                  <option value='本科'>                     本科生                  </option>                  <option value='硕士'>                     硕士生                  </option>                  <option value='博士'>                     博士生                  </option>              </select>              <input type='submit' value='注册' />           </form>       </div>       用户的学历是：       <div id='showMsg'>       </div>    </body></html>  ' 6302859, 0, 23, '2011-3-16 20:42:00', ' 反射的概念是由Smith在1982年首次提出的，主要是指程序可以访问、检测和修改它本身状态或行为的一种能力。这一概念的提出很快引发了计算机科学领域关于应用反射性的研究。它首先被程序语言的设计领域所采用,并在Lisp和面向对象方面取得了成绩。其中LEAD/LEAD++ 、OpenC++ 、MetaXa和OpenJava等就是基于反射机制的语言。最近，反射机制也被应用到了视窗系统、操作系统和文件系统中。 反射本身并不是一个新概念，它可能会使我们联想到光学中的反射概念，尽管计算机科学赋予了反射概念新的含义，但是，从现象上来说，它们确实有某些相通之处，这些有助于我们的理解。在计算机科学领域，反射是指一类应用，它们能够自描述和自控制。也就是说，这类应用通过采用某种机制来实现对自己行为的描述（self-representation）和监测（examination），并能根据自身行为的状态和结果，调整或修改应用所描述行为的状态和相关的语义。可以看出，同一般的反射概念相比，计算机科学领域的反射不单单指反射本身，还包括对反射结果所采取的措施。所有采用反射机制的系统（即反射系统）都希望使系统的实现更开放。可以说，实现了反射机制的系统都具有开放性，但具有开放性的系统并不一定采用了反射机制，开放性是反射系统的必要条件。一般来说，反射系统除了满足开放性条件外还必须满足原因连接（Causally-connected）。所谓原因连接是指对反射系统自描述的改变能够立即反映到系统底层的实际状态和行为上的情况，反之亦然。开放性和原因连接是反射系统的两大基本要素。 Java中，反射是一种强大的工具。它使您能够创建灵活的代码，这些代码可以在运行时装配，无需在组件之间进行源代表链接。反射允许我们在编写与执行时，使我们的程序代码能够接入装载到JVM中的类的内部信息，而不是源代码中选定的类协作的代码。这使反射成为构建灵活的应用的主要工具。但需注意的是：如果使用不当，反射的成本很高。 二、Java中的类反射： Reflection 是 Java 程序开发语言的特征之一，它允许运行中的 Java 程序对自身进行检查，或者说“自审”，并能直接操作程序的内部属性。Java 的这一能力在实际应用中也许用得不是很多，但是在其它的程序设计语言中根本就不存在这一特性。例如，Pascal、C 或者 C++ 中就没有办法在程序中获得函数定义相关的信息。 1．检测类： 1.1 reflection的工作机制 考虑下面这个简单的例子，让我们看看 reflection 是如何工作的。 import java.lang.reflect.*; public class DumpMethods { public static void main(String args[]) { try { Class c = Class.forName(args[0]); Method m[] = c.getDeclaredMethods(); for (int i = 0; i < m.length; i++) System.out.println(m[i].toString()); } catch (Throwable e) { System.err.println(e); } } } 按如下语句执行： java DumpMethods java.util.Stack 它的结果输出为： public java.lang.Object java.util.Stack.push(java.lang.Object) public synchronized java.lang.Object java.util.Stack.pop() public synchronized java.lang.Object java.util.Stack.peek() public boolean java.util.Stack.empty() public synchronized int java.util.Stack.search(java.lang.Object) 这样就列出了java.util.Stack 类的各方法名以及它们的限制符和返回类型。 这个程序使用 Class.forName 载入指定的类，然后调用 getDeclaredMethods 来获取这个类中定义了的方法列表。java.lang.reflect.Methods 是用来描述某个类中单个方法的一个类。 1.2 Java类反射中的主要方法 对于以下三类组件中的任何一类来说 -- 构造函数、字段和方法 -- java.lang.Class 提供四种独立的反射调用，以不同的方式来获得信息。调用都遵循一种标准格式。以下是用于查找构造函数的一组反射调用： l Constructor getConstructor(Class[] params) -- 获得使用特殊的参数类型的公共构造函数， l Constructor[] getConstructors() -- 获得类的所有公共构造函数 l Constructor getDeclaredConstructor(Class[] params) -- 获得使用特定参数类型的构造函数(与接入级别无关) l Constructor[] getDeclaredConstructors() -- 获得类的所有构造函数(与接入级别无关) 获得字段信息的Class 反射调用不同于那些用于接入构造函数的调用，在参数类型数组中使用了字段名： l Field getField(String name) -- 获得命名的公共字段 l Field[] getFields() -- 获得类的所有公共字段 l Field getDeclaredField(String name) -- 获得类声明的命名的字段 l Field[] getDeclaredFields() -- 获得类声明的所有字段 用于获得方法信息函数： l Method getMethod(String name, Class[] params) -- 使用特定的参数类型，获得命名的公共方法 l Method[] getMethods() -- 获得类的所有公共方法 l Method getDeclaredMethod(String name, Class[] params) -- 使用特写的参数类型，获得类声明的命名的方法 l Method[] getDeclaredMethods() -- 获得类声明的所有方法 1.3开始使用 Reflection： 用于 reflection 的类，如 Method，可以在 java.lang.relfect 包中找到。使用这些类的时候必须要遵循三个步骤：第一步是获得你想操作的类的 java.lang.Class 对象。在运行中的 Java 程序中，用 java.lang.Class 类来描述类和接口等。 下面就是获得一个 Class 对象的方法之一： Class c = Class.forName('java.lang.String'); 这条语句得到一个 String 类的类对象。还有另一种方法，如下面的语句： Class c = int.class; 或者 Class c = Integer.TYPE; 它们可获得基本类型的类信息。其中后一种方法中访问的是基本类型的封装类 (如 Integer) 中预先定义好的 TYPE 字段。 第二步是调用诸如 getDeclaredMethods 的方法，以取得该类中定义的所有方法的列表。 一旦取得这个信息，就可以进行第三步了——使用 reflection API 来操作这些信息，如下面这段代码： Class c = Class.forName('java.lang.String'); Method m[] = c.getDeclaredMethods(); System.out.println(m[0].toString()); 它将以文本方式打印出 String 中定义的第一个方法的原型。 2．处理对象： 如果要作一个开发工具像debugger之类的，你必须能发现filed values,以下是三个步骤: a.创建一个Class对象 b.通过getField 创建一个Field对象 c.调用Field.getXXX(Object)方法(XXX是Int,Float等，如果是对象就省略；Object是指实例). 例如： import java.lang.reflect.*; import java.awt.*; class SampleGet { public static void main(String[] args) { Rectangle r = new Rectangle(100, 325); printHeight(r); } static void printHeight(Rectangle r) { Field heightField; Integer heightValue; Class c = r.getClass(); try { heightField = c.getField('height'); heightValue = (Integer) heightField.get(r); System.out.println('Height: ' + heightValue.toString()); } catch (NoSuchFieldException e) { System.out.println(e); } catch (SecurityException e) { System.out.println(e); } catch (IllegalAccessException e) { System.out.println(e); } } } 三、安全性和反射： 在处理反射时安全性是一个较复杂的问题。反射经常由框架型代码使用，由于这一点，我们可能希望框架能够全面接入代码，无需考虑常规的接入限制。但是，在其它情况下，不受控制的接入会带来严重的安全性风险，例如当代码在不值得信任的代码共享的环境中运行时。 由于这些互相矛盾的需求，Java编程语言定义一种多级别方法来处理反射的安全性。基本模式是对反射实施与应用于源代码接入相同的限制： n 从任意位置到类公共组件的接入 n 类自身外部无任何到私有组件的接入 n 受保护和打包（缺省接入）组件的有限接入 不过至少有些时候，围绕这些限制还有一种简单的方法。我们可以在我们所写的类中，扩展一个普通的基本类java.lang.reflect.AccessibleObject 类。这个类定义了一种setAccessible方法，使我们能够启动或关闭对这些类中其中一个类的实例的接入检测。唯一的问题在于如果使用了安全性管理器，它将检测正在关闭接入检测的代码是否许可了这样做。如果未许可，安全性管理器抛出一个例外。 下面是一段程序，在TwoString 类的一个实例上使用反射来显示安全性正在运行： public class ReflectSecurity { public static void main(String[] args) { try { TwoString ts = new TwoString('a', 'b'); Field field = clas.getDeclaredField('m_s1'); // field.setAccessible(true); System.out.println('Retrieved value is ' + field.get(inst)); } catch (Exception ex) { ex.printStackTrace(System.out); } } } 如果我们编译这一程序时，不使用任何特定参数直接从命令行运行，它将在field .get(inst)调用中抛出一个IllegalAccessException异常。如果我们不注释field.setAccessible(true)代码行，那么重新编译并重新运行该代码，它将编译成功。最后，如果我们在命令行添加了JVM参数-Djava.security.manager以实现安全性管理器，它仍然将不能通过编译，除非我们定义了ReflectSecurity类的许可权限。 四、反射性能： 反射是一种强大的工具，但也存在一些不足。一个主要的缺点是对性能有影响。使用反射基本上是一种解释操作，我们可以告诉JVM，我们希望做什么并且它满足我们的要求。这类操作总是慢于只直接执行相同的操作。 下面的程序是字段接入性能测试的一个例子，包括基本的测试方法。每种方法测试字段接入的一种形式 -- accessSame 与同一对象的成员字段协作，accessOther 使用可直接接入的另一对象的字段，accessReflection 使用可通过反射接入的另一对象的字段。在每种情况下，方法执行相同的计算 -- 循环中简单的加/乘顺序。 程序如下： public int accessSame(int loops) { m_value = 0; for (int index = 0; index < loops; index++) { m_value = (m_value + ADDITIVE_VALUE) * MULTIPLIER_VALUE; } return m_value; } public int accessReference(int loops) { TimingClass timing = new TimingClass(); for (int index = 0; index < loops; index++) { timing.m_value = (timing.m_value + ADDITIVE_VALUE) * MULTIPLIER_VALUE; } return timing.m_value; } public int accessReflection(int loops) throws Exception { TimingClass timing = new TimingClass(); try { Field field = TimingClass.class. getDeclaredField('m_value'); for (int index = 0; index < loops; index++) { int value = (field.getInt(timing) + ADDITIVE_VALUE) * MULTIPLIER_VALUE; field.setInt(timing, value); } return timing.m_value; } catch (Exception ex) { System.out.println('Error using reflection'); throw ex; } } 在上面的例子中，测试程序重复调用每种方法，使用一个大循环数，从而平均多次调用的时间衡量结果。平均值中不包括每种方法第一次调用的时间，因此初始化时间不是结果中的一个因素。下面的图清楚的向我们展示了每种方法字段接入的时间： 图 1：字段接入时间 ： 我们可以看出：在前两副图中(Sun JVM)，使用反射的执行时间超过使用直接接入的1000倍以上。通过比较，IBM JVM可能稍好一些，但反射方法仍旧需要比其它方法长700倍以上的时间。任何JVM上其它两种方法之间时间方面无任何显著差异，但IBM JVM几乎比Sun JVM快一倍。最有可能的是这种差异反映了Sun Hot Spot JVM的专业优化，它在简单基准方面表现得很糟糕。反射性能是Sun开发1.4 JVM时关注的一个方面，它在反射方法调用结果中显示。在这类操作的性能方面，Sun 1.4.1 JVM显示了比1.3.1版本很大的改进。 如果为为创建使用反射的对象编写了类似的计时测试程序，我们会发现这种情况下的差异不象字段和方法调用情况下那么显著。使用newInstance()调用创建一个简单的java.lang.Object实例耗用的时间大约是在Sun 1.3.1 JVM上使用new Object()的12倍，是在IBM 1.4.0 JVM的四倍，只是Sun 1.4.1 JVM上的两部。使用Array.newInstance(type, size)创建一个数组耗用的时间是任何测试的JVM上使用new type[size]的两倍，随着数组大小的增加，差异逐步缩小。 结束语： Java语言反射提供一种动态链接程序组件的多功能方法。它允许程序创建和控制任何类的对象(根据安全性限制)，无需提前硬编码目标类。这些特性使得反射特别适用于创建以非常普通的方式与对象协作的库。例如，反射经常在持续存储对象为数据库、XML或其它外部格式的框架中使用。Java reflection 非常有用，它使类和数据结构能按名称动态检索相关信息，并允许在运行着的程序中操作这些信息。Java 的这一特性非常强大，并且是其它一些常用语言，如 C、C++、Fortran 或者 Pascal 等都不具备的。 但反射有两个缺点。第一个是性能问题。用于字段和方法接入时反射要远慢于直接代码。性能问题的程度取决于程序中是如何使用反射的。如果它作为程序运行中相对很少涉及的部分，缓慢的性能将不会是一个问题。即使测试中最坏情况下的计时图显示的反射操作只耗用几微秒。仅反射在性能关键的应用的核心逻辑中使用时性能问题才变得至关重要。 许多应用中更严重的一个缺点是使用反射会模糊程序内部实际要发生的事情。程序人员希望在源代码中看到程序的逻辑，反射等绕过了源代码的技术会带来维护问题。反射代码比相应的直接代码更复杂，正如性能比较的代码实例中看到的一样。解决这些问题的最佳方案是保守地使用反射——仅在它可以真正增加灵活性的地方——记录其在目标类中的使用。 最近在成都写一个移动增值项目，俺负责后台server端。功能很简单，手机用户通过GPRS打开Socket与服务器连接，我则根据用户传过来的数据做出响应。做过类似项目的兄弟一定都知道，首先需要定义一个类似于MSNP的通讯协议，不过今天的话题是如何把这个系统设计得具有高度的扩展性。由于这个项目本身没有进行过较为完善的客户沟通和需求分析，所以以后肯定会有很多功能上的扩展，通讯协议肯定会越来越庞大，而我作为一个不那么勤快的人，当然不想以后再去修改写好的程序，所以这个项目是实践面向对象设计的好机会。 首先定义一个接口来隔离类： package org.bromon.reflect; public interface Operator { public java.util.List act(java.util.List params) } 根据设计模式的原理，我们可以为不同的功能编写不同的类，每个类都继承Operator接口，客户端只需要针对Operator接口编程就可以避免很多麻烦。比如这个类： package org.bromon.reflect.*; public class Success implements Operator { public java.util.List act(java.util.List params) { List result=new ArrayList();   Java 的反射机制是使其具有动态特性的非常关键的一种机制，也是在JavaBean 中广泛应用的一种特性。 运用JavaBean 的最常见的问题是：根据指定的类名，类字段名和所对应的数据，得到该类的实例，下面的一个例子演示了这一实现。 -|Base.java //抽象基类 |Son1.java //基类扩展1 |Son2.java //基类扩展2 |Util.java /** * @author metaphy * create 2005-4-14 9:06:56 * 说明： */ （1）Base.java 抽象基类只是一个定义 public abstract class Base { } （2）Son1.java /Son2.java 是已经实现的JavaBean public class Son1 extends Base{ private int id ; private String name ; public int getId() { return id; } public void setId(int id) { this.id = id; } public String getName() { return name; } public void setName(String name) { this.name = name; } public void son1Method(String s){ System.out.println(s) ; } } （3） public class Son2 extends Base{ private int id; private double salary; public int getId() { return id; } public void setId(int id) { this.id = id; } public double getSalary() { return salary; } public void setSalary(double salary) { this.salary = salary; } } （4）Util.java 演示了如何根据指定的类名，类字段名和所对应的数据，得到一个类的实例 import java.lang.reflect.Method; public class Util { //此方法的最大好处是没有类名Son1,Son2 可以通过参数来指定，程序里面根本不用出现 public static Base convertStr2ServiceBean(String beanName,String fieldSetter,String paraValue){ Base base = null ; try { Class cls = Class.forName(beanName) ; base = (Base)cls.newInstance() ; Class[] paraTypes = new Class[]{String.class }; Method method = cls.getMethod(fieldSetter, paraTypes) ; String[] paraValues = new String[]{paraValue} ; method.invoke(base, paraValues) ; } catch (Throwable e) { System.err.println(e); } return base ; } public static void main(String[] args){ Son1 son1 =(Son1) Util.convertStr2ServiceBean('trying.reflect.Son1','setName','wang da sha'); System.out.println('son1.getName() :'+son1.getName()) ; } } //调用结果： //son1.getName() :wang da sha 谢谢！希望能给大家一点启发！ －－－－－－－－－－－－－－－－－－－－ 附： //下面这篇文档来源于Internet，作者不详 Reflection 是 Java 程序开发语言的特征之一，它允许运行中的 Java 程序对自身进行检查，或者说“自审”，并能直接操作程序的内部属性。例如，使用它能获得 Java 类中各成员的名称并显示出来。 Java 的这一能力在实际应用中也许用得不是很多，但是在其它的程序设计语言中根本就不存在这一特性。例如，Pascal、C 或者 C++ 中就没有办法在程序中获得函数定义相关的信息。 JavaBean 是 reflection 的实际应用之一，它能让一些工具可视化的操作软件组件。这些工具通过 reflection 动态的载入并取得 Java 组件(类) 的属性。 1. 一个简单的例子 考虑下面这个简单的例子，让我们看看 reflection 是如何工作的。 import java.lang.reflect.*; public class DumpMethods { public static void main(String args[]) { try { Class c = Class.forName(args[0]); Method m[] = c.getDeclaredMethods(); for (int i = 0; i < m.length; i++) System.out.println(m[i].toString()); } catch (Throwable e) { System.err.println(e); } } } 按如下语句执行： java DumpMethods java.util.Stack 它的结果输出为： public java.lang.Object java.util.Stack.push(java.lang.Object) public synchronized java.lang.Object java.util.Stack.pop() public synchronized java.lang.Object java.util.Stack.peek() public boolean java.util.Stack.empty() public synchronized int java.util.Stack.search(java.lang.Object) 这样就列出了java.util.Stack 类的各方法名以及它们的限制符和返回类型。 这个程序使用 Class.forName 载入指定的类，然后调用 getDeclaredMethods 来获取这个类中定义了的方法列表。java.lang.reflect.Methods 是用来描述某个类中单个方法的一个类。 2.开始使用 Reflection 用于 reflection 的类，如 Method，可以在 java.lang.relfect 包中找到。使用这些类的时候必须要遵循三个步骤：第一步是获得你想操作的类的 java.lang.Class 对象。在运行中的 Java 程序中，用 java.lang.Class 类来描述类和接口等。 下面就是获得一个 Class 对象的方法之一： Class c = Class.forName('java.lang.String'); 这条语句得到一个 String 类的类对象。还有另一种方法，如下面的语句： Class c = int.class; 或者 Class c = Integer.TYPE; 它们可获得基本类型的类信息。其中后一种方法中访问的是基本类型的封装类 (如 Integer) 中预先定义好的 TYPE 字段。 第二步是调用诸如 getDeclaredMethods 的方法，以取得该类中定义的所有方法的列表。 一旦取得这个信息，就可以进行第三步了——使用 reflection API 来操作这些信息，如下面这段代码： Class c = Class.forName('java.lang.String'); Method m[] = c.getDeclaredMethods(); System.out.println(m[0].toString()); 它将以文本方式打印出 String 中定义的第一个方法的原型。 在下面的例子中，这三个步骤将为使用 reflection 处理特殊应用程序提供例证。 模拟 instanceof 操作符 得到类信息之后，通常下一个步骤就是解决关于 Class 对象的一些基本的问题。例如，Class.isInstance 方法可以用于模拟 instanceof 操作符： class A { } public class instance1 { public static void main(String args[]) { try { Class cls = Class.forName('A'); boolean b1 = cls.isInstance(new Integer(37)); System.out.println(b1); boolean b2 = cls.isInstance(new A()); System.out.println(b2); } catch (Throwable e) { System.err.println(e); } } } 在这个例子中创建了一个 A 类的 Class 对象，然后检查一些对象是否是 A 的实例。Integer(37) 不是，但 new A() 是。 3.找出类的方法 找出一个类中定义了些什么方法，这是一个非常有价值也非常基础的 reflection 用法。下面的代码就实现了这一用法： import java.lang.reflect.*; public class method1 { private int f1(Object p, int x) throws NullPointerException { if (p == null) throw new NullPointerException(); return x; } public static void main(String args[]) { try { Class cls = Class.forName('method1'); Method methlist[] = cls.getDeclaredMethods(); for (int i = 0; i < methlist.length; i++) { Method m = methlist[i]; System.out.println('name = ' + m.getName()); System.out.println('decl class = ' + m.getDeclaringClass()); Class pvec[] = m.getParameterTypes(); for (int j = 0; j < pvec.length; j++) System.out.println('param #' + j + ' ' + pvec[j]); Class evec[] = m.getExceptionTypes(); for (int j = 0; j < evec.length; j++) System.out.println('exc #' + j + ' ' + evec[j]); System.out.println('return type = ' + m.getReturnType()); System.out.println('-----'); } } catch (Throwable e) { System.err.println(e); } } } 这个程序首先取得 method1 类的描述，然后调用 getDeclaredMethods 来获取一系列的 Method 对象，它们分别描述了定义在类中的每一个方法，包括 public 方法、protected 方法、package 方法和 private 方法等。如果你在程序中使用 getMethods 来代替 getDeclaredMethods，你还能获得继承来的各个方法的信息。 取得了 Method 对象列表之后，要显示这些方法的参数类型、异常类型和返回值类型等就不难了。这些类型是基本类型还是类类型，都可以由描述类的对象按顺序给出。 输出的结果如下： name = f1 decl class = class method1 param #0 class java.lang.Object param #1 int exc #0 class java.lang.NullPointerException return type = int ----- name = main decl class = class method1 param #0 class [Ljava.lang.String; return type = void ----- 4.获取构造器信息 获取类构造器的用法与上述获取方法的用法类似，如： import java.lang.reflect.*; public class constructor1 { public constructor1() { } protected constructor1(int i, double d) { } public static void main(String args[]) { try { Class cls = Class.forName('constructor1'); Constructor ctorlist[] = cls.getDeclaredConstructors(); for (int i = 0; i < ctorlist.length; i++) { Constructor ct = ctorlist[i]; System.out.println('name = ' + ct.getName()); System.out.println('decl class = ' + ct.getDeclaringClass()); Class pvec[] = ct.getParameterTypes(); for (int j = 0; j < pvec.length; j++) System.out.println('param #' + j + ' ' + pvec[j]); Class evec[] = ct.getExceptionTypes(); for (int j = 0; j < evec.length; j++) System.out.println('exc #' + j + ' ' + evec[j]); System.out.println('-----'); } } catch (Throwable e) { System.err.println(e); } } } 这个例子中没能获得返回类型的相关信息，那是因为构造器没有返回类型。 这个程序运行的结果是： name = constructor1 decl class = class constructor1 ----- name = constructor1 decl class = class constructor1 param #0 int param #1 double ----- 5.获取类的字段(域) 找出一个类中定义了哪些数据字段也是可能的，下面的代码就在干这个事情： import java.lang.reflect.*; public class field1 { private double d; public static final int i = 37; String s = 'testing'; public static void main(String args[]) { try { Class cls = Class.forName('field1'); Field fieldlist[] = cls.getDeclaredFields(); for (int i = 0; i < fieldlist.length; i++) { Field fld = fieldlist[i]; System.out.println('name = ' + fld.getName()); System.out.println('decl class = ' + fld.getDeclaringClass()); System.out.println('type = ' + fld.getType()); int mod = fld.getModifiers(); System.out.println('modifiers = ' + Modifier.toString(mod)); System.out.println('-----'); } } catch (Throwable e) { System.err.println(e); } } } 这个例子和前面那个例子非常相似。例中使用了一个新东西 Modifier，它也是一个 reflection 类，用来描述字段成员的修饰语，如“private int”。这些修饰语自身由整数描述，而且使用 Modifier.toString 来返回以“官方”顺序排列的字符串描述 (如“static”在“final”之前)。这个程序的输出是： name = d decl class = class field1 type = double modifiers = private ----- name = i decl class = class field1 type = int modifiers = public static final ----- name = s decl class = class field1 type = class java.lang.String modifiers = ----- 和获取方法的情况一下，获取字段的时候也可以只取得在当前类中申明了的字段信息 (getDeclaredFields)，或者也可以取得父类中定义的字段 (getFields) 。 6.根据方法的名称来执行方法 文本到这里，所举的例子无一例外都与如何获取类的信息有关。我们也可以用 reflection 来做一些其它的事情，比如执行一个指定了名称的方法。下面的示例演示了这一操作： import java.lang.reflect.*; public class method2 { public int add(int a, int b) { return a + b; } public static void main(String args[]) { try { Class cls = Class.forName('method2'); Class partypes[] = new Class[2]; partypes[0] = Integer.TYPE; partypes[1] = Integer.TYPE; Method meth = cls.getMethod('add', partypes); method2 methobj = new method2(); Object arglist[] = new Object[2]; arglist[0] = new Integer(37); arglist[1] = new Integer(47); Object retobj = meth.invoke(methobj, arglist); Integer retval = (Integer) retobj; System.out.println(retval.intvalue()); } catch (Throwable e) { System.err.println(e); } } } 假如一个程序在执行的某处的时候才知道需要执行某个方法，这个方法的名称是在程序的运行过程中指定的 (例如，JavaBean 开发环境中就会做这样的事)，那么上面的程序演示了如何做到。 上例中，getMethod 用于查找一个具有两个整型参数且名为 add 的方法。找到该方法并创建了相应的 Method 对象之后，在正确的对象实例中执行它。执行该方法的时候，需要提供一个参数列表，这在上例中是分别包装了整数 37 和 47 的两个 Integer 对象。执行方法的返回的同样是一个 Integer 对象，它封装了返回值 84。 7.创建新的对象 对于构造器，则不能像执行方法那样进行，因为执行一个构造器就意味着创建了一个新的对象 (准确的说，创建一个对象的过程包括分配内存和构造对象)。所以，与上例最相似的例子如下： import java.lang.reflect.*; public class constructor2 { public constructor2() { } public constructor2(int a, int b) { System.out.println('a = ' + a + ' b = ' + b); } public static void main(String args[]) { try { Class cls = Class.forName('constructor2'); Class partypes[] = new Class[2]; partypes[0] = Integer.TYPE; partypes[1] = Integer.TYPE; Constructor ct = cls.getConstructor(partypes); Object arglist[] = new Object[2]; arglist[0] = new Integer(37); arglist[1] = new Integer(47); Object retobj = ct.newInstance(arglist); } catch (Throwable e) { System.err.println(e); } } } 根据指定的参数类型找到相应的构造函数并执行它，以创建一个新的对象实例。使用这种方法可以在程序运行时动态地创建对象，而不是在编译的时候创建对象，这一点非常有价值。 8.改变字段(域)的值 reflection 的还有一个用处就是改变对象数据字段的值。reflection 可以从正在运行的程序中根据名称找到对象的字段并改变它，下面的例子可以说明这一点： import java.lang.reflect.*; public class field2 { public double d; public static void main(String args[]) { try { Class cls = Class.forName('field2'); Field fld = cls.getField('d'); field2 f2obj = new field2(); System.out.println('d = ' + f2obj.d); fld.setDouble(f2obj, 12.34); System.out.println('d = ' + f2obj.d); } catch (Throwable e) { System.err.println(e); } } } 这个例子中，字段 d 的值被变为了 12.34。 9.使用数组 本文介绍的 reflection 的最后一种用法是创建的操作数组。数组在 Java 语言中是一种特殊的类类型，一个数组的引用可以赋给 Object 引用。观察下面的例子看看数组是怎么工作的： import java.lang.reflect.*; public class array1 { public static void main(String args[]) { try { Class cls = Class.forName('java.lang.String'); Object arr = Array.newInstance(cls, 10); Array.set(arr, 5, 'this is a test'); String s = (String) Array.get(arr, 5); System.out.println(s); } catch (Throwable e) { System.err.println(e); } } } 例中创建了 10 个单位长度的 String 数组，为第 5 个位置的字符串赋了值，最后将这个字符串从数组中取得并打印了出来。 下面这段代码提供了一个更复杂的例子： import java.lang.reflect.*; public class array2 { public static void main(String args[]) { int dims[] = new int[]{5, 10, 15}; Object arr = Array.newInstance(Integer.TYPE, dims); Object arrobj = Array.get(arr, 3); Class cls = arrobj.getClass().getComponentType(); System.out.println(cls); arrobj = Array.get(arrobj, 5); Array.setInt(arrobj, 10, 37); int arrcast[][][] = (int[][][]) arr; System.out.println(arrcast[3][5][10]); } } 例中创建了一个 5 x 10 x 15 的整型数组，并为处于 [3][5][10] 的元素赋了值为 37。注意，**数组实际上就是数组的数组，例如，第一个 Array.get 之后，arrobj 是一个 10 x 15 的数组。进而取得其中的一个元素，即长度为 15 的数组，并使用 Array.setInt 为它的第 10 个元素赋值。 注意创建数组时的类型是动态的，在编译时并不知道其类型。' 6302860, 0, 17, '2011-3-16 20:45:00', '人是生命的有机体，成功有赖于良好的休息，睡眠是其中的关键。睡眠不在于睡多久，而在于睡多好，睡眠的好坏取决于你放松的程度。催眠术的核心就在于完全的放松。世界上多数人都在追逐求索如何成功，却不知成功的关键之一是从每天安然入眠开始。常常是因为太想成功了，所以寝食难安，而与成功失之交臂的例证不胜枚举。原来，错失成功竟这样容易。鲁迅说过，会休息的人才会工作。而睡眠是最好的休息。当你在忙碌、奋斗、打拼了一天的时候，晚上躺在床上的时候，一定要放松身心，去除杂念，抛开烦琐的一切，尽情而充分享受一顿美觉。待第二天清晨起来，崭新的一天开始的时候，你将充满活力，精神焕发，力量无穷。 -——《每天进步》' 6302862, 0, 0, '2011-3-16 20:54:00', ' 一.构造函数  1.构造函数的特征:  构造函数是一个特殊的函数.  没有返回值类型. 构造函数在创建对象的时候, 由虚拟机调用. 注意: 没有返回值类型, 不要写void  方法名和类名相同.  不能使用return返回一个值. 但可以使用return结束方法. 2.构造函数的作用:  当我们使用new关键字创建对象的时候, 一定会调用构造函数  我们通常在构造函数中做一些初始化的工作 3.构造函数可以重载  和普通函数相同, 只要参数列表不同即可. 4.构造函数之间的调用  可以使用this关键字加参数列表调用其他的构造函数  使用this调用其他构造函数时, 只能在构造函数的第一行. 5.每个类至少要有一个构造函数  如果没有显式的声明. 虚拟机会为这个类添加上一个默认无参的构造函数  如果声明了一个有参的构造函数, 那么就不会添加默认无参的构造函数了 6.构造函数的访问权限  一般构造函数都会声明为public, 可以供其他类中创建对象.  在某些特殊的情况下, 不向让别人创建对象, 可以设置为private 7.构造函数执行顺序  new Person();  堆内存中创建对象, 初始化成员变量  运行构造函数' 6302863, 0, 13, '2011-3-16 20:59:00', '睡眠贵好人是生命的有机体，成功有赖于良好的休息，睡眠是其中的关键。睡眠不在于睡多久，而在于睡多好，睡眠的好坏取决于你放松的程度。催眠术的核心就在于完全的放松。世界上多数人都在追逐求索如何成功，却不知成功的关键之一是从每天安然入眠开始。常常是因为太想成功了，所以寝食难安，而与成功失之交臂的例证不胜枚举。原来，错失成功竟这样容易。鲁迅说过，会休息的人才会工作。而睡眠是最好的休息。当你在忙碌、奋斗、打拼了一天的时候，晚上躺在床上的时候，一定要放松身心，去除杂念，抛开烦琐的一切，尽情而充分享受一顿美觉。待第二天清晨起来，崭新的一天开始的时候，你将充满活力，精神焕发，力量无穷。 ' 6302865, 0, 39, '2011-3-16 21:06:00', ' Dom4j的简单介绍1、 Dom4j是一个简单、灵活的开放源代码的库。Dom4j是由早期开发JDOM开发的。与JDOM不同的是，dom4j使用接口和抽象的人分离出来而后独立基类，虽然Dom4j的API相对要复杂一些，但它提供了比JDOM更好的灵活性。2、 Dom4j是一个非常优秀的Java XML API，具有性能优异、功能强大和极易使用的特点。现在很多软件采用的Dom4j，例如Hibernate，包括sun公司自己的JAXM也用了Dom4j。3、  使用Dom4j开发，需下载dom4j相应的jar文件。DOM4j中，获得Document对象的方式有三种：       1.读取XML文件,获得document对象                       SAXReader reader = new SAXReader();                 Document   document = reader.read(new File('input.xml'));        2.解析XML形式的文本,得到document对象.                 String text = '<members></members>';                  Document document = DocumentHelper.parseText(text);        3.主动创建document对象.                  Document document = DocumentHelper.createDocument();             //创建根节点            Element root = document.addElement('members');节点对象：1、获取文档的根节点.      Element root = document.getRootElement(); 2、取得某个节点的子节点.       Element element=node.element(“书名'); 3、取得节点的文字      String text=node.getText();4、取得某节点下所有名为“member”的子节点，并进行遍历. List nodes = rootElm.elements('member');  for (Iterator it = nodes.iterator(); it.hasNext();) {     Element elm = (Element) it.next();    // do something } 5、对某节点下的所有子节点进行遍历.    for(Iterator it=root.elementIterator();it.hasNext();){       Element element = (Element) it.next();       // do something    } 6、在某节点下添加子节点.Element ageElm = newMemberElm.addElement('age'); 7、设置节点文字. element.setText('29'); 8、删除某节点.//childElm是待删除的节点,parentElm是其父节点    parentElm.remove(childElm);9、添加一个CDATA节点.Element contentElm = infoElm.addElement('content');contentElm.addCDATA(diary.getContent()); 事例：              publicclass GoodWell03 {     publicstaticvoid main(String[] args) throws Exception {              SAXReader sreader = new SAXReader();              Document doc = sreader.read(new File('src//Book.xml'));              Element root = doc.getRootElement();               System.out.println(root.getName());              perse(root);    }        privatestaticvoid perse(Element root) {       atter(root);       for(Iterator<Element> it=root.elementIterator();it.hasNext();){           Element entity=it.next();           if(entity.isTextOnly()){              atter(entity);              System.out.println(entity.getText());//遍历所有           }else{              perse(entity);           }       }           }    privatestaticvoid atter(Element root) {       // TODO Auto-generated method stub       for(Iterator<Attribute> it=root.attributeIterator();it.hasNext();){           Attribute entity=it.next();           System.out.println(entity.getName()+'    '+entity.getValue());                  }    }} 节点对象属性： 1、取得某节点下的某属性    Element root=document.getRootElement();        //属性名name         Attribute attribute=root.attribute('size');2、取得属性的文字    String text=attribute.getText(); 3、删除某属性 Attribute attribute=root.attribute('size'); root.remove(attribute);.遍历某节点的所有属性   Element root=document.getRootElement();      for(Iterator it=root.attributeIterator();it.hasNext();){         Attribute attribute = (Attribute) it.next();         String text=attribute.getText();         System.out.println(text);    } 4、设置某节点的属性和文字.   newMemberElm.addAttribute('name', 'sitinspring'); 5、设置属性的文字   Attribute attribute=root.attribute('name');   attribute.setText('sitinspring');       将文档写入xml文件1、文档中全为英文,不设置编码,直接写入的形式. XMLWriter writer = new XMLWriter(new FileWriter('output.xml')); writer.write(document); writer.close(); 2、文档中含有中文,设置编码格式写入的形式.OutputFormat format = OutputFormat.createPrettyPrint();// 指定XML编码                  format.setEncoding('GBK');       XMLWriter writer = new XMLWriter(newFileWriter('output.xml'),format);writer.write(document);writer.close(); 事例：              Document doc=DocumentHelper.createDocument();              Element root=doc.addElement('books');              Element book=root.addElement('book');              book.addAttribute('isbn', '112');              Element name=book.addElement('name');              name.setText('java');              Element author=book.addElement('author');              author.setText('goodwell');              Element price=book.addElement('price');              price.setText('112');              OutputFormat format=new OutputFormat('    ',true,'UTF-8');       XMLWriter xw=new XMLWriter(new FileWriter('src//bk.xml'),format);              xw.write(doc);          xw.close(); dom4j在指定位置插入节点：       1.得到插入位置的节点列表（list）2.调用list.add(index,elemnent)，由index决定element的插入位置。Element元素可以通过DocumentHelper对象得到。示例代码：Element aaa = DocumentHelper.createElement('aaa');aaa.setText('aaa');List list = root.element('书').elements();list.add(1, aaa);//更新document                     SAXReader reader=new SAXReader();              Document doc=reader.read(new File('src//bk.xml'));              Element root=doc.getRootElement();              Element address=DocumentHelper.createElement('address');              address.setText('保定');              Element el= (Element) root.elements('book').get(1);              List list1=el.elements();              list1.add(2, address);              //删除第二个book中的地址              Element element=(Element) root.elements('book').get(1);              Element el=element.element('address');              element.remove(el);              OutputFormat format=new OutputFormat('    ',true,'UTF-8');              XMLWriter xw=new XMLWriter(new FileWriter('src//bk.xml'),format);              xw.write(doc);              xw.close(); ' 6302866, 2, 44, '2011-3-16 21:07:00', 'JavaScript与DOM一、什么是DOM DOM（Document Object Model），文档对象模型，DOM可以以一种独立于平台和语言的方式访问和修改一个文档的内容和结构。换句话说，这是表示和处理一个HTML或XML文档的常用方法。有一点很重要，DOM的设计是以对象管理组织（OMG）的规约为基础的，因此可以用于任何编程语言.二、DOM树DOM树顾名思义就像树一样，有一个主干派生出很多的枝干。DOM树的根统一为文档对象—document，DOM既然是树状结构，那么它们自然有如下的几种关系: 根结点(document)   父结点(parentNode) 子结点(childNodes) 兄弟结点   兄弟结点       (sibling)   (sibling)。我们再来看一下kHTML的结构：<html> <head> <title> </title></head> <body></body></html>我们参照树的概念，画出该HTML文档结构的DOM树：            html        body  head         div        title         文本        文本 从上面的图示可以看出 html有两个子结点，而html就是这两个子节点的父结点 head有节点title，title下有一个文本节点 body下有节点div，div下有一个文本节点三、操作DOM树详细代码如下：<html>  <head>    <title>Dom.html</title><script type='text/javascript'>window.onload = function(){ //通过docuemnt.documentElement获取根节点 ==>html var zhwHtml = document.documentElement; //打印节点名称 HTML 大写 alert(zhwHtml.nodeName); //获取body标签节点 var zhwBody = document.body; //打印BODY节点的名称 alert(zhwBody.nodeName); //获取body的第一个子节点 var fH = zhwBody.firstChild; alert(fH+'body的第一个子节点'); //获取body的最后一个子节点 var lH = zhwBody.lastChild; alert(lH+'body的最后一个子节点'); //通过id获取<h1> var ht = document.getElementById('zhw'); alert(ht.nodeName); var text = ht.childNodes; alert(text.length); var txt = ht.firstChild; alert(txt.nodeName); alert(txt.nodeValue); alert(ht.innerHTML); alert(ht.innerText+'Text'); }</script>  </head>    <body>    <h1 id='zhw'>你好</h1>  </body></html>欢迎来访，如有问题请留言。'";
//		content = "Adobe在今年3月份称，从8月1日开始，如果开发者的应用使用了Flash Player的高级功能（即支持硬件加速的Stage 3D和域内存功能），且净收入超过了5万美元，则需要向Adobe公司支付净收入的9%。 Flash Player的高级功能主要针对使用C/C++（通过Alchemy编译器）或第三方工具（如Unity）开发Flash Player平台商业游戏的开发商。 近日 Adobe 称，将延长这一决策开始执行的时间，开发者至少还有8周的时间来准备以及获得许可证。 Adobe表示，将会提供一个网站，开发者可以在8月底之前通过该网站获取许可证。 Flash Player开始执行高级功能许可要求后，需要使用Flash Player高级功能而未被许可的应用程序，将自动使用软件渲染功能来运行。 ";
//		content = "PhoneGap开发团队近日正式发布了PhoneGap 2.0版本。 PhoneGap是一个开源的跨平台移动应用开发框架，能使开发者们在只使用标准网络技术（HTML5、CSS和JavaScript）的情况下开发跨平台应用。PhoneGap的目标是实现“一次开发，运行于任何移动终端平台”。 PhoneGap最初由Nitobi开发，2011年10月，Nitobi被Adobe收购，而PhoneGap项目也被贡献给Apache软件基金会，并有了一个新的名字Apache Cordova。 PhoneGap 2.0 的新特性包括： Cordova WebView：允许将PhoneGap作为一个视图片段整合进更大的本地应用程序中。命令行工具（CLI）：可用于Android、iOS和BlackBerry平台，为跨平台任务（如创建、调试、模拟等）提供了一个标准的命令操作方式。文档改善：包括快速入门指南、插件迁移指南以及其他文档，以帮助开发者加快和简化移动应用的开发，Web Inspector Remote（Weinre）移植到nodejs：意味着可以通过NMP（Node Package Manager）来轻松安装。Cordovajs：性能、安全性、平台间API一致性得到了显著改进。项目过渡到Apache Cordova，即将从孵化器中毕业。改善了iOS应用的创建PhoneGap首席开发人员Brian•LeRoux表示： 引用PhoneGap 2.0大大提升了开发人员的体验。我们的文档现在更全面，其中包含了开发者所要求的Plugin API。开发者将可以使用PhoneGap作为桥梁，创建自己的浏览器API，以用于本地调用。";
//		content = "java文件加密 spring属性文件加密解密java文件加密 spring属性文件加密解密java文件加密 spring属性文件加密解密java文件加密 spring属性文件加密解密java文件加密 spring属性文件加密解密java文件加密 spring属性文件加密解密";
//		content = "苹果今天正式发布了新一代操作系统OS X Mountain Lion，版本号为10.8，该系统已经在Mac App Store上架，售价为128元。 苹果称，OS X Mountain Lion中包含了200多项新功能，并将iPhone、iPad和iPod touch中的诸多精彩功能带到了Mac上。此外，该系统还针对中国用户进行了本土化定制。 1.  Mountain Lion的中国本土化 OS X Mountain Lion 针对许多热门的中文功能与服务提供全新支持：词典 app 现包含《现代汉语规范词典》。通过升级的文本输入方法，输入中文变得更轻松、更快速、更准确。通过八种全新字体，你的书写内容可以用正式、非正式或有趣的形式来呈现。Mail 支持 QQ、163 和 126 邮箱。Safari中内置了百度搜索选项。现在，你还可以从你的 app 直接把内容发布到网上：在优酷和土豆上发布视频，在新浪微博上发布微博。2.  iCloud iCloud可以让Mac、iPad、iPhone 和iPod touch之间的协作更加紧密。无论你在哪里使用电子邮件、日历、通讯录、提醒事项、文档、备忘录等内容，它都会让它们保持更新。只需用你的 Apple ID 登录一次，iCloud 就可以在所有使用它的 app 中设置好了。 3.  iMessage 该系统还引入了iMessage功能。现在你也可以向任何运行 iOS 5 的 iPhone、iPad 或 iPod touch 用户发送信息。信息会在你的 Mac 及你使用的任何设备上显示，这就意味着你可以在 Mac 上开始对话，然后无论走到哪里都能在 iPhone 或 iPad 上继续进行。你还可以发送照片、视频、文档和通讯录，甚至发送群组消息。 4.  通知中心 苹果还将iOS 5中的通知中心引入到了Mountain Lion中，你可以在通知中心中轻松查看电子邮件、信息、软件更新或日历提醒等消息。如果有任何通知，你就会在第一时间知道。 5.  Gatekeeper Gatekeeper可以帮助你避免在 Mac 上下载和安装恶意软件，还能为你进一步控制哪些 app 可以被安装。这是 OS X 保卫 Mac 安全的一种全新方式。 此外，OS X Mountain Lion在日历、通讯录、信息、文本编辑、分享等方面进行了大量改进，详细信息可参阅：OS X Mountain Lion新特性 ";
//		// content =
//		// "甲骨文的MySQL开发者工具团队今天发布了MySQL Workbench 5.2.41版本，该版本中包含了一个新的数据库迁移向导插件。 新的迁移向导提供了一个易于使用的图形界面，帮助开发者将数据库从第三方产品中迁移到MySQL。在这个初期版本的插件中，支持迁移的数据库管理系统包括微软的SQL Server，以及其他支持ODBC的数据库，如PostgreSQL等。 MySQL Workbench是一个可视化的数据库设计软件，前身是 FabForce 公司的 DB Designer 4。它为数据库管理员、程序开发者和系统规划师提供可视化设计、模型建立、以及数据库管理功能。 除了迁移工具外，MySQL Workbench 5.2.41中的其他改进包括： 修复了100多个bugSQL编辑器中的代码完成功能（测试版）更好地处理建模时的模式同步";
//		System.out.println(getNewWords(content,0,0));
//		System.out.println(getNewWords(content,0,1));
//	}
//}

