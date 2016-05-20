package clrcomparison;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class BubbleSort {

	private static int MAX_ITEMS = 66537;
	private static int ITEM_COUNT = 2;
	private static int RATE = 2;
	private static Random _random = new Random();
	private static Document _log;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		int items = ITEM_COUNT;
		
        DocumentBuilderFactory factory = 
            DocumentBuilderFactory.newInstance();
        
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        File logFile = new File("log.xml");
        
        if(logFile.createNewFile())
        {
        	FileWriter writer = new FileWriter(logFile);
        	writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><PerformanceData/>");
        	writer.flush();
        	writer.close();
        }
        _log = builder.parse(logFile);

        while(items < MAX_ITEMS)
		{
			System.out.println("Bubble sorting " + items + " integer items.");
			runIntegerTests(items);
			items *= RATE;
		}

		System.out.println("");
		items = ITEM_COUNT;
		
        while(items < MAX_ITEMS)
		{
			System.out.println("Bubble sorting " + items + " single float items.");
			runSingleTests(items);
			items *= RATE;
		}

		System.out.println("");
		items = ITEM_COUNT;
		
        while(items < MAX_ITEMS)
		{
			System.out.println("Bubble sorting " + items + " double float items.");
			runDoubleTests(items);
			items *= RATE;
		}

		System.out.println("");
		System.out.println("Done.");
	}
	
	private static void runIntegerTests(int itemCount) throws FileNotFoundException, TransformerException {
		long millis = 0;
		
		millis = System.currentTimeMillis();
		sortIntegerArray(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", Integer[].class, Integer.class, itemCount, millis);
		System.out.println("\tSorting array: " + millis + " ms.");
		
		millis = System.currentTimeMillis();
		sortGenericIntegerList(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", ArrayList.class, Integer.class, itemCount, millis);
		System.out.println("\tSorting generic list: " + millis + " ms.");
		
		millis = System.currentTimeMillis();
		sortObjectIntegerList(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", ArrayList.class, Integer.class, itemCount, millis);
		System.out.println("\tSorting object list: " + millis + " ms.");
	}
	
	private static void runSingleTests(int itemCount) throws FileNotFoundException, TransformerException {
		long millis = 0;
		
		millis = System.currentTimeMillis();
		sortSingleArray(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", Float[].class, Float.class, itemCount, millis);
		System.out.println("\tSorting array: " + millis + " ms.");
		
		millis = System.currentTimeMillis();
		sortGenericSingleList(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", ArrayList.class, Float.class, itemCount, millis);
		System.out.println("\tSorting generic list: " + millis + " ms.");
		
		millis = System.currentTimeMillis();
		sortObjectSingleList(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", ArrayList.class, Float.class, itemCount, millis);
		System.out.println("\tSorting object list: " + millis + " ms.");
	}

	private static void runDoubleTests(int itemCount) throws FileNotFoundException, TransformerException {
		long millis = 0;
		
		millis = System.currentTimeMillis();
		sortDoubleArray(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", Double[].class, Double.class, itemCount, millis);
		System.out.println("\tSorting array: " + millis + " ms.");
		
		millis = System.currentTimeMillis();
		sortGenericDoubleList(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", ArrayList.class, Double.class, itemCount, millis);
		System.out.println("\tSorting generic list: " + millis + " ms.");
		
		millis = System.currentTimeMillis();
		sortObjectDoubleList(itemCount);
		millis = System.currentTimeMillis() - millis;
        log("Java Sun Hotspot JVM", ArrayList.class, Double.class, itemCount, millis);
		System.out.println("\tSorting object list: " + millis + " ms.");
	}
	
	private static void sortObjectIntegerList(int itemCount)
	{
		ArrayList values = new ArrayList(itemCount);
		
		for(int i = 0; i < itemCount; i++)
		{
			values.add(_random.nextInt());
		}
		
		sortIntegers(values);
	}
	
	private static void sortObjectSingleList(int itemCount)
	{
		ArrayList values = new ArrayList(itemCount);
		
		for(int i = 0; i < itemCount; i++)
		{
			values.add(_random.nextFloat());
		}
		
		sortSingles(values);
	}
	
	private static void sortObjectDoubleList(int itemCount)
	{
		ArrayList values = new ArrayList(itemCount);
		
		for(int i = 0; i < itemCount; i++)
		{
			values.add(Math.random());
		}
		
		sortDoubles(values);
	}
	
	private static void sortGenericIntegerList(int itemCount)
	{
		ArrayList<Integer> values = new ArrayList<Integer>(itemCount);
		
		for(int i = 0; i < itemCount; i++)
		{
			values.add(_random.nextInt());
		}
		
		sortGenericIntegers(values);
	}
	
	private static void sortGenericSingleList(int itemCount)
	{
		ArrayList<Float> values = new ArrayList<Float>(itemCount);
		
		for(int i = 0; i < itemCount; i++)
		{
			values.add(_random.nextFloat());
		}
		
		sortGenericSingles(values);
	}
	
	private static void sortGenericDoubleList(int itemCount)
	{
		ArrayList<Double> values = new ArrayList<Double>(itemCount);
		
		for(int i = 0; i < itemCount; i++)
		{
			values.add(Math.random());
		}
		
		sortGenericDoubles(values);
	}

	private static void sortIntegerArray(int itemCount) {
		int[] values = new int[itemCount];
		
		for(int i = 0; i < itemCount; i++)
		{
			values[i] = _random.nextInt();
		}

		sortIntegers(values);
	}

	private static void sortSingleArray(int itemCount) {
		float[] values = new float[itemCount];
		
		for(int i = 0; i < itemCount; i++)
		{
			values[i] = _random.nextFloat();
		}

		sortSingles(values);
	}

	private static void sortDoubleArray(int itemCount) {
		double[] values = new double[itemCount];
		
		for(int i = 0; i < itemCount; i++)
		{
			values[i] = Math.random();
		}

		sortDoubles(values);
	}

	private static void sortIntegers(ArrayList values) {
		for (int i = 0; i < values.size(); i++) {
			for (int j = 0; j < values.size() - 1; j++) {
				int v1 = (Integer)values.get(j);
				int v2 = (Integer)values.get(j+1);
				if(v1 > v2)
				{
					int tmp = v1;
					values.set(j, v2);
					values.set(j+1, tmp);
				}
			}
		}
	}

	private static void sortSingles(ArrayList values) {
		for (int i = 0; i < values.size(); i++) {
			for (int j = 0; j < values.size() - 1; j++) {
				float v1 = (Float)values.get(j);
				float v2 = (Float)values.get(j+1);
				if(v1 > v2)
				{
					float tmp = v1;
					values.set(j, v2);
					values.set(j+1, tmp);
				}
			}
		}
	}

	private static void sortDoubles(ArrayList values) {
		for (int i = 0; i < values.size(); i++) {
			for (int j = 0; j < values.size() - 1; j++) {
				double v1 = (Double)values.get(j);
				double v2 = (Double)values.get(j+1);
				if(v1 > v2)
				{
					double tmp = v1;
					values.set(j, v2);
					values.set(j+1, tmp);
				}
			}
		}
	}
	
	private static void sortGenericIntegers(ArrayList<Integer> values) {
		for (int i = 0; i < values.size(); i++) {
			for (int j = 0; j < values.size() - 1; j++) {
				int v1 = values.get(j);
				int v2 = values.get(j+1);
				if(v1 > v2)
				{
					int tmp = v1;
					values.set(j, v2);
					values.set(j+1, tmp);
				}
			}
		}
	}
	
	private static void sortGenericSingles(ArrayList<Float> values) {
		for (int i = 0; i < values.size(); i++) {
			for (int j = 0; j < values.size() - 1; j++) {
				float v1 = values.get(j);
				float v2 = values.get(j+1);
				if(v1 > v2)
				{
					float tmp = v1;
					values.set(j, v2);
					values.set(j+1, tmp);
				}
			}
		}
	}
	
	private static void sortGenericDoubles(ArrayList<Double> values) {
		for (int i = 0; i < values.size(); i++) {
			for (int j = 0; j < values.size() - 1; j++) {
				double v1 = values.get(j);
				double v2 = values.get(j+1);
				if(v1 > v2)
				{
					double tmp = v1;
					values.set(j, v2);
					values.set(j+1, tmp);
				}
			}
		}
	}

	private static void sortIntegers(int[] values) {
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length - 1; j++) {
				if(values[j] > values[j+1])
				{
					int tmp = values[j];
					values[j] = values[j+1];
					values[j+1] = tmp;
				}
			}
		}
	}

	private static void sortSingles(float[] values) {
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length - 1; j++) {
				if(values[j] > values[j+1])
				{
					float tmp = values[j];
					values[j] = values[j+1];
					values[j+1] = tmp;
				}
			}
		}
	}

	private static void sortDoubles(double[] values) {
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length - 1; j++) {
				if(values[j] > values[j+1])
				{
					double tmp = values[j];
					values[j] = values[j+1];
					values[j+1] = tmp;
				}
			}
		}
	}
	
	private static void log(String technology, Class container, Class itemType, int itemCount, long duration) throws FileNotFoundException, TransformerException
	{
		Element timeResultElement = _log.createElement("TimeResult");
		_log.getDocumentElement().appendChild(timeResultElement);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		timeResultElement.setAttribute("Timestamp", dateFormat.format(new Date()));
		timeResultElement.setAttribute("Technology", technology);
		timeResultElement.setAttribute("Container", container.getSimpleName());
		timeResultElement.setAttribute("ItemType", itemType.getSimpleName());
		timeResultElement.setAttribute("ItemCount", Integer.toString(itemCount));
		timeResultElement.setAttribute("Duration", Long.toString(duration));
		
		OutputStream outStream = new FileOutputStream("log.xml");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(_log);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
	}

}

