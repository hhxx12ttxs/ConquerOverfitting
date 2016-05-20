/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.xcap.diff.dom.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.util.XML11Char;
import org.mobicents.protocols.xcap.diff.BuildPatchException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Some DOM related util logic.
 * 
 * @author baranowb
 * @author martins
 * 
 */
public class DOMXmlUtils {

	public static final String DEFAULT_NAMESPACE_PREFIX = "";
	// some vals

	public static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
	// ------------------------------ create methods
	// --------------------------------

	private static DocumentBuilderFactory factory;

	static {
		try {
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
		} catch (Exception e) {
			// should not happen :)
			e.printStackTrace();
		}
	}

	public static Document parseWellFormedDocumentFragment(Reader reader)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document dummyDocument = parser.parse(new InputSource(reader));
		return dummyDocument;
	}

	/**
	 * @param string
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Document parseWellFormedDocumentFragment(String string)
			throws ParserConfigurationException, SAXException, IOException {

		return (Document) parseWellFormedDocumentFragment(new StringReader(
				string));
	}

	public static Document createWellFormedDocumentFragment(String root,
			String namespace) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element rootElement = document.createElementNS(namespace, root);
		// rootElement.setAttribute("xmlns", namespace);
		document.appendChild(rootElement);
		return document;
	}

	public static Element createElement(String elementName,
			String defaultElementPrefix, String elementNamespace,
			Map<String, String> namespaceBindings) throws BuildPatchException {
		
		// calculate the prefix for the resulting element
		String patchComponentElementName = elementName;
		if (namespaceBindings != null
				&& namespaceBindings
						.containsKey(DOMXmlUtils.DEFAULT_NAMESPACE_PREFIX)) {
			int round = 1;
			String xcapDiffPrefix = defaultElementPrefix;
			while (true) {
				if (!namespaceBindings.containsKey(xcapDiffPrefix)) {
					patchComponentElementName = new StringBuilder(
							xcapDiffPrefix).append(':').append(elementName)
							.toString();
					break;
				} else {
					xcapDiffPrefix = defaultElementPrefix
							+ Integer.toString(round);
					round++;
				}
			}
		}

		// build element
		Element element = null;
		try {
			// no need to use elem name prefix
			element = DOMXmlUtils.createWellFormedDocumentFragment(
					patchComponentElementName, elementNamespace)
					.getDocumentElement();
		} catch (Throwable e) {
			throw new BuildPatchException("Failed to create DOM element", e);
		}
		// set other namespaces not in element
		if (namespaceBindings != null) {

			String nsPrefix = null;
			for (Entry<String, String> entry : namespaceBindings.entrySet()) {
				nsPrefix = entry.getKey();
				if (nsPrefix.equals("")) {
					element.setAttribute("xmlns",
							namespaceBindings.get(nsPrefix));
				} else {
					element.setAttribute("xmlns:" + nsPrefix,
							namespaceBindings.get(nsPrefix));
				}
			}
		}
		return element;
	}

	// some util methods

	public static Map<String, String> getDocumentNameSpaces(Element e) {

		Element documentElement = e.getOwnerDocument().getDocumentElement();
		return getNodeNameSpaces(documentElement);
	}
	
	public static Map<String, String> getNodeNameSpaces(Node e)
	{
		Map<String, String> nameSpaces = new HashMap<String, String>();
		NamedNodeMap nnm = e.getAttributes();
		for (int index = 0; index < nnm.getLength(); index++) {
			Node attrNode = nnm.item(index);
			String attrNodeName = attrNode.getNodeName();
			if (attrNodeName.startsWith("xmlns:")) {
				// non default namespace
				nameSpaces.put(attrNodeName.replaceFirst("xmlns:", ""),
						attrNode.getNodeValue());
			} else if (attrNodeName.startsWith("xmlns")) {
				// default name space
				nameSpaces.put("", attrNode.getNodeValue());
			} else {
				// some other attribute, ignore
			}
		}
		return nameSpaces;
	}
	

	public static String toString(Node node) throws TransformerException {
		Source source = new DOMSource(node);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		if (node instanceof Element) {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result);
		return stringWriter.getBuffer().toString();
	}

	public static boolean weaklyXmlEquals(String xml1, String xml2) {

		// clean xml1 string
		xml1 = xml1.trim().replaceAll("\n", "").replaceAll("\t", "")
				.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\f", "");

		// clean xml2 string
		xml2 = xml2.trim().replaceAll("\n", "").replaceAll("\t", "")
				.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\f", "");

		return xml1.equals(xml2);

	}
	/**
	 * 
	 * 
	 */
	public static boolean isQName(String name) {
		String[] qName = name.split(":");
		if (qName.length == 1) {
			return XML11Char.isXML11ValidNCName(name);
		} else if (qName.length == 2) {
			return XML11Char.isXML11ValidNCName(qName[0])
					&& XML11Char.isXML11ValidNCName(qName[1]);
		}
		return false;
	}

	/**
	 * Validates if the specifiedc string is a valid xml attribute value. Specs
	 * say that an attr value is validated by the following regex:
	 * 
	 * AttValue ::= '"' ([^<&"] | Reference)* '"' | "'" ([^<&'] | Reference)*
	 * "'" Reference ::= EntityRef | CharRef EntityRef ::= '&' Name ';' CharRef
	 * ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
	 * 
	 * 
	 * NOTE: The specified string doesn't come with surroundings " or ' so we
	 * can't accept both chars!!!!
	 * 
	 * @param value
	 * @return
	 */
	public static boolean checkAttValue(String value) {

		try {

			StringBuilder sb = new StringBuilder(value);

			// check and remove char refs

			// &#x [0-9a-fA-F]+ ;

			Set<String> set = new HashSet<String>();
			while (true) {
				int begin = sb.indexOf("&#x");
				if (begin > -1) {
					// found begin
					int end = sb.indexOf(";", begin + 3);
					if (end > -1) {
						// found an end
						set.add(sb.substring(begin + 3, end));
						sb = new StringBuilder(sb.substring(0, begin))
								.append(sb.substring(end + 1));
					} else {
						break;
					}
				} else {
					break;
				}
			}

			Pattern p = Pattern.compile("[0-9a-fA-F]+");
			for (Iterator<String> i = set.iterator(); i.hasNext();) {
				String t = i.next();
				Matcher m = p.matcher(t);
				if (!m.matches()) {
					return false;
				}
			}

			// &# [0-9]+ ;

			set = new HashSet<String>();
			while (true) {
				int begin = sb.indexOf("&#");
				if (begin > -1) {
					// found begin
					int end = sb.indexOf(";", begin + 2);
					if (end > -1) {
						// found an end
						set.add(sb.substring(begin + 2, end));
						sb = new StringBuilder(sb.substring(0, begin))
								.append(sb.substring(end + 1));
					} else {
						break;
					}
				} else {
					break;
				}
			}

			p = Pattern.compile("[0-9]+");
			for (Iterator<String> i = set.iterator(); i.hasNext();) {
				String t = i.next();
				Matcher m = p.matcher(t);
				if (!m.matches()) {
					return false;
				}
			}

			// check and remove entity refs
			// & name ;

			set = new HashSet<String>();
			while (true) {
				int begin = sb.indexOf("&");
				if (begin > -1) {
					// found begin
					int end = sb.indexOf(";", begin + 1);
					if (end > -1) {
						// found an end
						set.add(sb.substring(begin + 1, end));
						sb = new StringBuilder(sb.substring(0, begin))
								.append(sb.substring(end + 1));
					} else {
						break;
					}
				} else {
					break;
				}
			}

			// check all names found
			for (Iterator<String> i = set.iterator(); i.hasNext();) {
				String name = i.next();
				if (!XML11Char.isXML11ValidName(name)) {
					return false;
				}
			}

			// check remaining chars

			for (int i = 0; i < sb.length(); i++) {
				if (sb.charAt(i) == '&' || sb.charAt(i) == '\''
						|| sb.charAt(i) == '"' || sb.charAt(i) == '<') {
					return false;
				}
			}

		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static String getElementSelectorWithEmptyPrefix(String elementSelector) {
		StringBuilder sb = new StringBuilder();
		String[] elementSelectorParts = elementSelector.split("/");
		for (int i = 0; i < elementSelectorParts.length; i++) {
			if (elementSelectorParts[i].isEmpty()) {
				continue;
			}
			if (elementSelectorParts[i].charAt(0) == '*' || elementSelectorParts[i].charAt(0) == '@' || elementSelectorParts[i].charAt(0) == '[' || elementSelectorParts[i].startsWith("namespace::")
					//functions are allowed
					|| elementSelectorParts[i].contains("()")) {
				// wildcard, just copy
				sb.append('/').append(elementSelectorParts[i]);
			} else if (elementSelectorParts[i].indexOf(':') > -1) {
				// it has at least one :, check if it's not inside an attr
				// value
				int pos = elementSelectorParts[i].indexOf('[');
				if (pos > 0 && elementSelectorParts[i].indexOf(':') > pos) {
					// insert empty prefix
					sb.append("/:").append(elementSelectorParts[i]);
				} else {
					// already has a prefix
					sb.append('/').append(elementSelectorParts[i]);
				}
			} else{
				// insert empty prefix
				sb.append("/:").append(elementSelectorParts[i]);
			}			
		}
		return sb.toString();
	}

	public static Node getNode(Document domDocument, String elementSelectorWithEmptyPrefixes, Map<String, String> namespaceContext) throws Exception {

		// lets use xpath
		final XPath xpath = XPATH_FACTORY.newXPath();
		// set context to resolve namespace bindings

		xpath.setNamespaceContext(new NamespaceContext(namespaceContext));
		XPathExpression expr = xpath.compile(elementSelectorWithEmptyPrefixes);
		// exec query to get element
		//final NodeList elementNodeList = (NodeList) xpath.evaluate(elementSelectorWithEmptyPrefixes, domDocument, XPathConstants.NODESET);
		final NodeList elementNodeList = (NodeList)  expr.evaluate(domDocument, XPathConstants.NODESET);
		if (elementNodeList.getLength() == 1) {

			return elementNodeList.item(0);
		} else if (elementNodeList.getLength() == 0) {

			return null;
		} else {

			throw new Exception("multiple elements match " + elementSelectorWithEmptyPrefixes);
		}

	}
	
	public static String getElementName(Node node) {
		return node.getLocalName() == null ? node.getNodeName() : node
				.getLocalName();
	}
	
	private static class NamespaceContext implements javax.xml.namespace.NamespaceContext, Externalizable {

		private Map<String,String> namespaces;
		
		public NamespaceContext() {
			namespaces = new HashMap<String, String>();
		}
		
		/**
		 * 
		 * @param namespaces a hash map to ensure deserialization produces same not thread safe map
		 */
		public NamespaceContext(Map<String,String> namespaces) {
			this.namespaces = namespaces;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
		 */
	    public String getNamespaceURI(String prefix) {
	        if (prefix == null) {
	        	throw new IllegalArgumentException("Null prefix");
	        }
	        else {        	
	        	String namespace = namespaces.get(prefix);        	
	        	if (namespace == null) {
	        		return XMLConstants.NULL_NS_URI;
	        	} else {
	        		return namespace;
	        	}
	        }        
	    }

	    /*
	     * (non-Javadoc)
	     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	     */
	    public String getPrefix(String uri) {
	        for(Iterator<String> i=namespaces.keySet().iterator();i.hasNext();) {
	        	String prefix = i.next();
	        	if ((namespaces.get(prefix)).equals(uri)) {
	        		return prefix;
	        	}
	        }
	        return null;
	    }

	    /*
	     * (non-Javadoc)
	     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	     */
	    public Iterator<String> getPrefixes(String uri) {
	        return namespaces.keySet().iterator();
	    }

	    /**
	     * 
	     * @return
	     */
	    public Map<String, String> getNamespaces() {
			return namespaces;
		}
	    
	    /**
	     * 
	     * @param namespace
	     */
	    public void setDefaultDocNamespace(String namespace) {
	    	namespaces.put(XMLConstants.DEFAULT_NS_PREFIX,namespace);
	    }
	    
	    @Override
	    public void readExternal(ObjectInput in) throws IOException,
	    		ClassNotFoundException {
	    	for (MapEntry me : (MapEntry[]) in.readObject()) {
	    		namespaces.put(me.key, me.value);
	    	}
	    }
	    
	    @Override
	    public void writeExternal(ObjectOutput out) throws IOException {
	    	MapEntry[] a = EMPTY_ARRAY;
	    	int size = namespaces.size();
	    	if (size > 0) {
	    		a = new MapEntry[size];
	    		int i = 0;
	    		for(Entry<String, String> e : namespaces.entrySet()) {
	    			a[i] = new MapEntry(e.getKey(), e.getValue()); 
	    		}
	    	}
	    	out.writeObject(a);
	    }
	    
	    private static final MapEntry[] EMPTY_ARRAY = {};
	    
	    private static class MapEntry implements Externalizable {
	    	
	    	String key;
	    	String value;    	
	    	
	    	@SuppressWarnings("unused")
			public MapEntry() {
				// needed by Externalizable
			}
	    	
	    	public MapEntry(String key, String value) {
				this.key = key;
				this.value = value;
			}

			@Override
	    	public void readExternal(ObjectInput in) throws IOException,
	    			ClassNotFoundException {
	    		key = in.readUTF();
	    		value = in.readUTF();
	    	}
	    	
	    	@Override
	    	public void writeExternal(ObjectOutput out) throws IOException {
	    		out.writeUTF(key);
	    		out.writeUTF(value);
	    	}
	    	
	    }
	        
	}
}
