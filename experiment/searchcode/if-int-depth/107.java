package flatFileData;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.NodeList;

import dataInterfaces.Boring;
import dataInterfaces.Site;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import panels.SitePanel;
import panels.SitePanel.SiteType;
public class Saving {
	static final String FILENAME = "save.xml";
	
	/**
	 * @param siteName
	 * @param siteId
	 * @param numBores
	 * @param x
	 * @param y
	 * @param depth
	 * @return
	 * @throws TransformerException
	 */
	/**
	 * @param siteName
	 * @param siteId
	 * @param numBores
	 * @param x
	 * @param y
	 * @param depth
	 * @return
	 * @throws TransformerException
	 */
	public static void loadBorings(SitePanel sitePanel, String saveName){
		saveName = saveName.replace(".", "");
		System.out.println("loadBorings called with saveName: '"+ saveName.replace(".", "") + "'");
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(FILENAME);
			Node investigation = doc.getFirstChild();
			
			NodeList siteList;
			boolean siteFound = false;
			
			
			int[][] testArray = new int[50][5];
			Node tempNode = null;
			System.out.println("before the hasChildNodes");
			if(investigation.hasChildNodes()){
				System.out.println("Has Child Nodes");
				siteList = investigation.getChildNodes();
				System.out.println("siteList.getLength(): " + siteList.getLength());
				for(int i = 0; i < siteList.getLength(); i++){
					System.out.println("item: "+ i);
					Node currentSite = siteList.item(i);
//					NamedNodeMap siteMap = currentSite.getAttributes();
					if(currentSite.getNodeName().equals(saveName)){
						System.out.println("Same Name! " + saveName);
						siteFound = true;
						tempNode = siteList.item(i);
						
						break;
					} else {
						System.out.println("'" + currentSite.getNodeName() + "' != '" + saveName + "'");
					}
				}
				if(siteFound){
					if(tempNode.hasChildNodes()){
						
						NodeList borings = tempNode.getChildNodes();
						
						for(int j = 0; j < borings.getLength(); j++){
							boolean testFound = false;
							int testBoringIndex = -1;
							Node boring = borings.item(j);
							NodeList boringMap = boring.getChildNodes();
							int x = -1, y = -1, depth = -1;
							int top = -1, bottom = -1, boringIndex = -1;
							
							for(int k = 0; k < boringMap.getLength(); k++){
								Node attribute = boringMap.item(k);
								if(attribute.getNodeName().equals("x")){
									System.out.println(attribute.getTextContent());
									x = Integer.parseInt(attribute.getTextContent());
								}else if(attribute.getNodeName().equals("y")){
									y = Integer.parseInt(attribute.getTextContent());
								}else if(attribute.getNodeName().equals("depth")){
									depth = Integer.parseInt(attribute.getTextContent());
								}else if(attribute.getNodeName().equals("well")){
									
									NodeList wellMap = attribute.getChildNodes();
									for(int wellCounter = 0; wellCounter < wellMap.getLength(); wellCounter++){
										Node wellAttribute = wellMap.item(wellCounter);
										if(wellAttribute.getNodeName().equals("top")){
											top = Integer.parseInt(wellAttribute.getTextContent());
										} else if(wellAttribute.getNodeName().equals("bottom")) {
											bottom = Integer.parseInt(wellAttribute.getTextContent());
										}
										
									}
									boringIndex = j;
								} else if(attribute.getNodeName().equals("tests")){
									NodeList testList = attribute.getChildNodes();
									for(int i = 0; i < testList.getLength(); i++){
										Node tempTest = testList.item(i);
										String tempString = tempTest.getTextContent();
										String[] eachTestString = tempString.split(",");
										//testArray = new int[testList.getLength()][eachTestString.length];
										for(int jspecificTest = 0; jspecificTest < eachTestString.length; jspecificTest++){
											testArray[i][jspecificTest] = Integer.parseInt(eachTestString[jspecificTest]);
											if(testArray[i][jspecificTest] == 1){
												System.out.println("ARRAYOFTESTS[" + i + "][" + jspecificTest + "]" +  testArray[i][jspecificTest]);
											}
										}
										
									}
									testBoringIndex = j;
									testFound = true;
//									for(int[] row : testArray){
//										for(int testBool : row){
////											System.out.println("testBool" + testBool);
//										}
//									}
								}
							}
							
							if(x > -1  && y > -1 && depth > -1){
								sitePanel.createNewBoringLog(depth, x, y, true, false);
								if(testFound && testBoringIndex > -1){
									
									sitePanel.performTestsOnBoringLog(testBoringIndex, testArray, true);
									testFound = false;
								}
								if(top > -1 && bottom > -1){
									sitePanel.addWellToBoringLog(boringIndex, top, bottom, x, y, false);
								}
							} else {
								System.out.println("INCOMPLETE BORING DATA");
							}
							
						}
					}
					
				}
				
				
			} else {
				System.out.println("No Child Nodes");
				siteList = null;
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void saveTest(String siteSaveName, int[][] testsToSave, int depths, int numTests, int boringNum){
		siteSaveName = siteSaveName.replace(".", "");
		System.out.println("saveTest Called");
		System.out.println("numTests: " + numTests);
		System.out.println("depths: " + depths);
		//testsToSave goes depth,test
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(FILENAME);
			
			Node investigation = doc.getFirstChild();
			NodeList siteList;
			Node tempNode;
			Node tempBoring;
			//Creating the Well Element
			Element tests = doc.createElement("tests");
			
			for(int i = 0; i < depths; i++){
				Element tempTest = doc.createElement("depth" + i);
				String tempTestTypes = "";
				for(int j = 0; j < numTests; j++){
//					System.out.println("testsToSave[" + i + "][" + j + "]:" + testsToSave[i][j]);
					tempTestTypes += Integer.toString(testsToSave[i][j]);
					if(j < numTests - 1){
						tempTestTypes += ",";
					}
				}
//				System.out.println("tempTestTypes: " + tempTestTypes);
				tempTest.setTextContent(tempTestTypes);
//				System.out.println("tempTest.getNodeValue(): " + tempTest.getNodeValue());
				tests.appendChild(tempTest);
			}

			
			if(investigation.hasChildNodes()){
				siteList = investigation.getChildNodes();
				for(int i = 0; i < siteList.getLength(); i++){
					
					if(siteList.item(i).getNodeName().equals(siteSaveName)){
						tempNode = siteList.item(i);
						NodeList boringList = tempNode.getChildNodes();
						for(int j = 0; j < boringList.getLength(); j++){
							tempBoring = boringList.item(j);
							if(tempBoring.getNodeName().equals("boring" + boringNum)){
								if(tempBoring.hasChildNodes()){
									NodeList boringChildren = tempBoring.getChildNodes();
									for(int k = 0; k < boringChildren.getLength(); k++){
										Node oldTests = boringChildren.item(k);
										if(oldTests.getNodeName().equals("tests")){
											tempBoring.removeChild(oldTests);
										} 
									}
								} else {
									System.out.println("tempBoring had not child nodes saving tests");
								}
								tempBoring.appendChild(tests);
								System.out.println("tests added to tempBoring");
								break;
							} else {
								System.out.println("Boring not found for test saving");
							}
						}
//						NamedNodeMap siteMap = siteList.item(i).getAttributes();

						
//						tempNode.appendChild(boring);
						break;
					} else {
						System.out.println(siteList.item(0).getNodeName() + "!=" + siteSaveName);
					}
				}

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(FILENAME));
				
				transformer.transform(source, result);
				
			} else {
				System.out.println("No Child Nodes");
				siteList = null;
			}
			
		}catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   } catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int[][] extractTests(Node tests){
		int[][] tempArray = null;
		NodeList testList = tests.getChildNodes();
		for(int i = 0; i < testList.getLength(); i++){
			Node tempTest = testList.item(i);
			String tempString = tempTest.getTextContent();
			String[] eachTestString = tempString.split(",");
			tempArray = new int[testList.getLength()][eachTestString.length];
			for(int j = 0; j < eachTestString.length; j++){
				tempArray[i][j] = Integer.parseInt(eachTestString[j]);
				if(tempArray[i][j] == 1){
					System.out.println("ARRAYOFTESTS[" + i + "][" + j + "]" +  tempArray[i][j]);
				}
			}
			
		}
		
		return tempArray;
	}
	
	public static void saveWell(String siteSaveName, int top, int bottom, int boringNum){
		siteSaveName = siteSaveName.replace(".", "");
		siteSaveName = siteSaveName.replace(" ", "");
		System.out.println("saveWell Called");
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(FILENAME);
			
			Node investigation = doc.getFirstChild();
			NodeList siteList;
			Node tempNode;
			Node tempBoring;
			//Creating the Well Element
			Element well = doc.createElement("well");
			Element wellTop = doc.createElement("top");
			wellTop.setTextContent(Integer.toString(top));
			Element wellBottom = doc.createElement("bottom");
			wellBottom.setTextContent(Integer.toString(bottom));
			
			well.appendChild(wellTop);
			well.appendChild(wellBottom);
			
			if(investigation.hasChildNodes()){
				siteList = investigation.getChildNodes();
				System.out.println("siteList.getLenght(): " + siteList.getLength());
				for(int i = 0; i < siteList.getLength(); i++){
					
					if(siteList.item(i).getNodeName().equals(siteSaveName)){
						tempNode = siteList.item(i);
						NodeList boringList = tempNode.getChildNodes();
						for(int j = 0; j < boringList.getLength(); j++){
							tempBoring = boringList.item(j);
							if(tempBoring.getNodeName().equals("boring" + boringNum)){
								tempBoring.appendChild(well);
								break;
							}
						}
//						NamedNodeMap siteMap = siteList.item(i).getAttributes();
						System.out.println("same name saving: " + siteList.item(0).getNodeName());
						
//						tempNode.appendChild(boring);
						break;
					} else {
//						System.out.println(siteList.item(0).getNodeName() + "!=" + siteType + siteName + siteId);
					}
				}
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(FILENAME));
				
				transformer.transform(source, result);
			} else {
				System.out.println("No Child Nodes");
				siteList = null;
			}
			
		}catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   } catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static int saveBoring(String siteType, String siteName, int siteId, int numBores, int x, int y, int depth) throws TransformerException{
		siteName = siteName.replace(".", "");
		siteName = siteName.replace(" ", "");
		System.out.println("SAVEBORING CALLED");
		//readFile();
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(FILENAME);
			Node investigation = doc.getFirstChild();
//			Document doc
//			System.out.println("document Parsed");
			NodeList siteList;
			boolean siteFound = false;
			Node tempNode;
			
			Element boring = doc.createElement("boring" + numBores);
//			boring.setAttribute("id", Integer.toString(numBores));
			Element boringX = doc.createElement("x");
			boringX.setTextContent(Integer.toString(x));
			Element boringY = doc.createElement("y");
			boringY.setTextContent(Integer.toString(y));
			Element boringDepth = doc.createElement("depth");
			boringDepth.setTextContent(Integer.toString(depth));
			System.out.println("BORING x: " + x + " y: " + y + " depth: " + depth);
			boring.appendChild(boringX);
			boring.appendChild(boringY);
			boring.appendChild(boringDepth);

			
			if(investigation.hasChildNodes()){
				siteList = investigation.getChildNodes();
				System.out.println("siteList.getLenght(): " + siteList.getLength());
				for(int i = 0; i < siteList.getLength(); i++){
					
					if(siteList.item(i).getNodeName().equals(siteType + siteName + siteId)){
//						NamedNodeMap siteMap = siteList.item(i).getAttributes();
						System.out.println("same name saving: " + siteList.item(0).getNodeName());
						siteFound = true;
						tempNode = siteList.item(i);
						tempNode.appendChild(boring);
						break;
					} else {
						System.out.println(siteList.item(0).getNodeName() + "!=" + siteType + siteName + siteId);
					}
				}
			} else {
				System.out.println("No Child Nodes");
				siteList = null;
			}
				
			//System.out.println("siteList.getLength(): " + siteList.getLength());

			
			if(!siteFound){
				System.out.println("site not found");
				System.out.println("siteId: " + siteName + siteId);
				System.out.println("siteType: " + siteType);
				Element site = doc.createElement(siteType + siteName + siteId);

				site.appendChild(boring);
				investigation.appendChild(site);
				System.out.println("appended site");
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(FILENAME));
				
				transformer.transform(source, result);
			} else {
				
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(FILENAME));
				
				transformer.transform(source, result);
			
				
			}
			
			
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
	   } catch (IOException ioe) {
		ioe.printStackTrace();
	   } catch (SAXException sae) {
		sae.printStackTrace();
	   }
		return 0;
	}


	
	public static void writeXML(){
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse("test2.xml");
			Node investigation = doc.getFirstChild();
//			Element rootElement = doc.createElement("investigation");
//			doc.appendChild(rootElement);
			Element site = doc.createElement("site");
			Element boring = doc.createElement("boring");
			boring.setAttribute("x", Integer.toString(200));
			boring.setAttribute("y", Integer.toString(300));
			boring.setAttribute("depth", Integer.toString(10));
			site.appendChild(boring);
			investigation.appendChild(site);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("test2.xml"));
			
			transformer.transform(source, result);
			System.out.println("File Written");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	//Resetting the save file, this should be password protected
	public static void reset(){
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try{
			 
    		File file = new File(FILENAME);
 
    		if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    			docBuilder = docFactory.newDocumentBuilder();
    			Document doc = docBuilder.newDocument();
//    			Node investigation = doc.getFirstChild();
    			Element rootElement = doc.createElement("investigation");
    			doc.appendChild(rootElement);
    			
    			TransformerFactory transformerFactory = TransformerFactory.newInstance();
    			Transformer transformer = transformerFactory.newTransformer();
    			DOMSource source = new DOMSource(doc);
    			StreamResult result = new StreamResult(new File(FILENAME));
    			
    			transformer.transform(source, result);
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
    		
 
    	}catch(Exception e){
 
    		e.printStackTrace();
 
    	}
	}
	public static int readFile(){
		try {
			File file = new File(FILENAME);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			
			if(doc.hasChildNodes()) {
				printNote(doc.getChildNodes());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		return 0;
	}
	
	private static void printNote(NodeList nodeList) {
		 
	    for (int count = 0; count < nodeList.getLength(); count++) {
	 
		Node tempNode = nodeList.item(count);
	 
		// make sure it's element node.
		if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
	 
			// get node name and value
			System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
			System.out.println("Node Value =" + tempNode.getTextContent());
	 
			if (tempNode.hasAttributes()) {
	 
				// get attributes names and values
				NamedNodeMap nodeMap = tempNode.getAttributes();
	 
				for (int i = 0; i < nodeMap.getLength(); i++) {
	 
					Node node = nodeMap.item(i);
					System.out.println("attr name : " + node.getNodeName());
					System.out.println("attr value : " + node.getNodeValue());
	 
				}
	 
			}
	 
			if (tempNode.hasChildNodes()) {
	 
				// loop again it if has child nodes
				printNote(tempNode.getChildNodes());
	 
			}
	 
			System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
	 
		}
	 
	    }
	 
	  }
}

