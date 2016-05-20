/************************************************************************
*				
*    
***********************************************************************/

/**
 * $Log: ParserTestCase.java,v $
 */


package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;


public class Dom4jParserTestCase {
	private static String DATA = "data"; 
	
	private static String readXml(String url) throws IOException {
		// ??xml??
		FileInputStream fis = new FileInputStream(url);
		byte[] b = new byte[1024];
		int n = 0;
		String s = "";
		StringBuffer sb = new StringBuffer();
		while ((n = fis.read(b)) != -1) {
			s = new String(b, 0, n);// ???????????????String?
			// ?????????
			sb.append(s);
		}
		return sb.toString();
	}
	
	private static String readXml(InputStream is) throws IOException {
		// ??xml??
		byte[] b = new byte[1024];
		int n = 0;
		String s = "";
		StringBuffer sb = new StringBuffer();
		while ((n = is.read(b)) != -1) {
			s = new String(b, 0, n);// ???????????????String?
			// ?????????
			sb.append(s);
		}
		return sb.toString();
	}

	private static String[] parseXml(String xml) {
		String flag[] = { "", "", "", "", "" };
		try {
			SAXReader reader = new SAXReader();
			reader.setEncoding("UTF-8");

			Document doc = reader.read(new StringReader(xml));
			Element root = doc.getRootElement();
			String roots = root.attributeValue("version");
			List rootList = root.elements();
			System.out.println(rootList.size());
			
			XPath path = new DefaultXPath("//forecast_conditions");
			List lists = path.selectNodes(doc);
			Iterator i = lists.iterator();
			PrintStream out = System.out;
	        while (i.hasNext()) {
	            Element foreElement = (Element) i.next();
	            out.print("\t" + foreElement.element("day_of_week").attributeValue("data")
						+ " low " + foreElement.element("low").attributeValue("data")
						+ " high " + foreElement.element("high").attributeValue("data") + " " + foreElement.element("condition").attributeValue("data"));
	        }
			
//			if (rootList.size() > 0) {
//				Element element = (Element) root.element("STATUS");
//				flag[0] = element.getText();
//
//				element = (Element) root.element("NSRSBH");
//				flag[1] = element.getText();
//
//				List list = root.elements("NSRINFO");
//				if (list.size() > 0) {
//					for (int k = 0; k < list.size(); k++) {
//						Element ele = (Element) list.get(k);
//						// ?????
//						element = (Element) ele.element("NSRMC");
//						flag[2] = element.getText();
//						// ??????
//						element = (Element) ele.element("SWJGDM");
//						flag[3] = element.getText();
//					}
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	public static ArrayList<WeatherData> parseWeatherXml(InputStream is) {
		ArrayList<WeatherData> weatherData = new ArrayList<WeatherData>();
		try {
			SAXReader reader = new SAXReader();
			reader.setEncoding("UTF-8");

			String xml = readXml(is);
			Document doc = reader.read(new StringReader(xml));
			Element root = doc.getRootElement();
			String version = root.attributeValue("version");

			if ("1".equals(version)) {
				List nodeLists = doc.selectNodes("//weather/problem_cause[@data]");
				if (nodeLists != null && nodeLists.size() > 0) {
					Element promblemElement = (Element) nodeLists.get(0);
					if(promblemElement != null){
						String problem = promblemElement.getTextTrim();
						if ("".equals(problem)) {
							throw (new Exception("Error fetching weather data"));
						} else {
							throw (new Exception(problem));
						}
					}
				}
				
				XPath currentPath = new DefaultXPath("//current_conditions");
				nodeLists = currentPath.selectNodes(doc);
				Iterator i = nodeLists.iterator();
				while (i.hasNext()) {
					Element curElement = (Element) i.next();
					weatherData.add(createWeatherData(curElement, "??"));
				}
				
				XPath forecastPath = new DefaultXPath("//forecast_conditions");
				nodeLists = forecastPath.selectNodes(doc);
				i = nodeLists.iterator();
				while (i.hasNext()) {
					Element foreElement = (Element) i.next();
					weatherData.add(createWeatherData(foreElement, null));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return weatherData;
	}

	public static void main(String[] args) throws IOException {
		String sFile = "D:\\myxml.xml";

		URL url = new URL(
				"http://www.google.com/ig/api?hl=zh-cn&weather=beijing");
		InputStream is = url.openStream();
		
		ArrayList<WeatherData> al = parseWeatherXml(is);
		
//		FileHandler file = new FileHandler("1.6.txt");
//		ParserTestCase ptc = new ParserTestCase();
//		byte[] b = ptc.readUrlStream(new BufferedInputStream(is));
//		String gbk = new String(b, "GBK");
//		String unicodes = ptc.string2unicode(gbk);
//		file.writeFile(ptc.unicode2string(unicodes));
//		String s = Dom4jParserTestCase.readXml(file.getPathAndFileName());
		String s = Dom4jParserTestCase.readXml(is);
//		String[] ret = Dom4jParserTestCase.parseXml(s);
//		System.out.println(ret[1]);
		System.out.println(s);
		parseXml(s);
	}
	
	private static WeatherData createWeatherData(Element xmlElement,
			String dayOfWeek) {
		WeatherData weatherData = new WeatherData();
		if (dayOfWeek != null) {//??
			weatherData.setDayOfWeek(dayOfWeek);
			weatherData.setLowTemp(Integer.parseInt(xmlElement
					.element("temp_c").attributeValue(DATA)));
			weatherData.setHighTemp(Integer.parseInt(xmlElement.element(
					"temp_f").attributeValue(DATA)));
			weatherData.setWind(xmlElement.element("wind_condition")
					.attributeValue(DATA));
			weatherData.setHumidity(xmlElement.element("humidity").attributeValue(
					DATA));
		} else {
			weatherData.setDayOfWeek(xmlElement.element("day_of_week")
					.attributeValue(DATA));
			weatherData.setLowTemp(Integer.parseInt(xmlElement.element("low")
					.attributeValue(DATA)));
			weatherData.setHighTemp(Integer.parseInt(xmlElement.element("high")
					.attributeValue(DATA)));
			weatherData.setWind("");
			weatherData.setHumidity("");
		}
		weatherData.setCondition(xmlElement.element("condition")
				.attributeValue(DATA));
		return weatherData;
	}
}

