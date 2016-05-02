/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
package ru.adv.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import ru.adv.db.app.request.Request;
import ru.adv.db.base.ContextAttrXmlGenerator;
import ru.adv.http.Query;
import ru.adv.xml.newt.Base;
import ru.adv.xml.newt.NewtProcessor;
import ru.adv.xml.parser.Parser;

/**
 * Algorithms for manipulate with DOM objects
 */

public abstract class XmlUtils {
	
	final static NamespaceContext DEFAULT_NS_CONTEXT = new DefaultNameSpaceContext();

	/**
	 * Select first Node by Xpath expression 
	 * @param node
	 * @param xpathExpression
	 * @return
	 */
	public static Node selectSingleNode(Node node, String xpathExpression) throws XPathExpressionException {
		return (Node)createXpath().evaluate(xpathExpression, node, XPathConstants.NODE);
	}
	
	/**
	 * @see XPathAPI#selectSingleNode(Node, String, Node)
	 * @param node
	 * @param xpathExpression
	 * @param namespaceNode
	 * @return
	 * @throws XPathExpressionException
	 */
	public static Node selectSingleNode(Node node, String xpathExpression, Node namespaceNode) throws XPathExpressionException {
		return (Node)createXpath().evaluate(xpathExpression, node, XPathConstants.NODE);
	}
	
	
	/**
	 * Select matched node list by XPath expression
	 * @param node
	 * @param xpathExpression
	 * @return
	 */
	public static NodeList selectNodeList(Node node, String xpathExpression) throws XPathExpressionException {
		return (NodeList)createXpath().evaluate(xpathExpression, node, XPathConstants.NODESET);
	}

	private static XPath createXpath() {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext( DEFAULT_NS_CONTEXT  );
		return xpath;
	}

    
    /**
     * Delete children elements for Node
     */
    public static void removeAllChildren(Node node) {
        if (node != null) {
            while (node.hasChildNodes()) {
                node.removeChild(node.getFirstChild());
            }
        }
    }

    /**
     * Get first Text node of the Element as String value
     * @param elem
     * @return
     */
    public static String getFirstTextValue(Element elem) {
        if (elem != null) {
            Node fc = elem.getFirstChild();
            if (null != fc && fc.getNodeType() == Node.TEXT_NODE) {
                return ((Text) fc).getData();
            }
        }
        return null;
    }

    /**
     * Delete namespace for Element
     *
     * @return copy of Elemetn without namespace
     */
    public static Element removeNS(Element elem) {
        Node parent = elem.getParentNode();
        Element newElem = elem.getOwnerDocument().createElement(elem.getLocalName());
        // ???????? ????????? ???? ? ????? ????
        while (elem.hasChildNodes()) {
            newElem.appendChild(elem.removeChild(elem.getFirstChild()));
        }
        // ???????? ????????
        NamedNodeMap attrs = elem.getAttributes();
        while (attrs.getLength() > 0) {
            newElem.setAttributeNode(elem.removeAttributeNode((Attr) attrs.item(0)));
        }
        // ????????? ???????
        if (parent != null) {
            parent.replaceChild(newElem, elem);
        }
        return newElem;
    }

    /**
     * Delete children elements for Element by element name
     */
    public static void removeAllChildren(Element node, String elementName) {
        if (node != null) {
            NodeList nl = node.getChildNodes();
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                Node childNode = nl.item(i);
                if (childNode != null
                        && childNode.getLocalName() != null
                        && childNode.getLocalName().equals(elementName))
                    node.removeChild(childNode);
            }
        }
    }
    
    /**
     * Delete children elements for Element by element name
     */
    public static List<Element> removeAllElements(Element node, String elementName) {
    	List<Element> result = new LinkedList<Element>();
        if (node != null) {
            List<Element> elements= findAllElements(node,elementName,true);
            for (Element element : elements) {
            	if (element != null && element.getTagName() != null
                    && element.getTagName().equals(elementName))
                    result.add((Element) node.removeChild(element));
            }
        }
        return result;
    }
    
    /**
     * Return array of children nodes by node name
     */
    public static Node[] childrenByNodeName(Element owner, String searchNode){
    	NodeList list = owner.getElementsByTagName(searchNode);
    	Node[] children = new Node[list.getLength()];
        for (int i = 0; i < children.length; i++) {
            if(isElement(list.item(i)) && searchNode.equalsIgnoreCase(list.item(i).getLocalName())){
            	children[i] = list.item(i);
            }
        }
        return children; 
    }

    public static String toString(XMLObject xmlObject) {
        return toString(xmlObject.toXML(Parser.createEmptyDocument()));
    }

    public static String toString(Node node) {
        if (node == null) {
            return "null";
        }
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                return XmlUtils.documentToString((Document) node);
            case Node.DOCUMENT_FRAGMENT_NODE:
                return XmlUtils.docfragmentToString((DocumentFragment) node);
            case Node.ELEMENT_NODE:
                return XmlUtils.elementToString((Element) node);
            case Node.PROCESSING_INSTRUCTION_NODE:
                return XmlUtils.processingInstructionToString((ProcessingInstruction) node);
        }
        return node.toString();
    }

    /**
     * Get Element like String
     */
    public static String elementToString(Element node) {
        return nodeToString(node);
    }

    public static String documentToString(Document node) {
    	return nodeToString(node);
    }

    public static String docfragmentToString(DocumentFragment node) {
    	return nodeToString(node);
    }
    
	private static String nodeToString(Node node) {
		if (node == null){
            return "null";
        }
        StringWriter stringWriter = new StringWriter();
        
        try{
	        DOMImplementation domImpl = getDocImplementation(node);
	        
	        if(!domImpl.hasFeature("XML", "1.0")){
				throw new ParserConfigurationException("XML 1.0 not supported");
	        }
	        if(!domImpl.hasFeature("LS", "3.0")){
	        	throw new ParserConfigurationException("Load/Save 3.0 not supported");
	        }
	        DOMImplementationLS domImplLS = (DOMImplementationLS)domImpl;
	        LSOutput output = domImplLS.createLSOutput();
	        output.setCharacterStream(stringWriter);
	        LSSerializer serializer = domImplLS.createLSSerializer();
	        DOMConfiguration config = serializer.getDomConfig();
	        if(config.canSetParameter("format-pretty-print", true)){
	        	config.setParameter("format-pretty-print", true);
	        }
	        config.setParameter("xml-declaration", false);
	        serializer.write(node, output); //serialize   
		} catch (Exception e){
			e.printStackTrace();
		}
		return stringWriter.toString();
	}

	private static DOMImplementation getDocImplementation(Node node) {
		DOMImplementation domImpl;
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			domImpl = ((Document)node).getImplementation();
		} else {
			domImpl = node.getOwnerDocument().getImplementation();
		}
		return domImpl;
	}


    /**
     * Get ProcessingInstruction like String
     */
    public static String processingInstructionToString(ProcessingInstruction pi) {
        return "<?" + pi.getTarget() + " " + pi.getData() + "?>";
    }

    /**
     * Generic clone method. It clones even Documents.
     */
    public static Node cloneNode(Node node, boolean deep) {
        if (deep && node.getNodeType() == Node.DOCUMENT_NODE) {
            Node root = ((Document) node).getDocumentElement();
            Document newDoc = Parser.createEmptyDocument();
            root = newDoc.importNode(root, true);
            newDoc.appendChild(root);
            return newDoc;
        }
        return node.cloneNode(deep);
    }


    /**
     * Return array of children nodes
     */
    public static Node[] childrenArray(Node node) {
        NodeList list = node.getChildNodes();
        Node[] children = new Node[list.getLength()];
        for (int i = 0; i < children.length; i++) {
            children[i] = list.item(i);
        }
        return children;
    }

    public static Node[] selectNodeArray(Node node, String xpath) {
        try {
            NodeList list = XmlUtils.selectNodeList(node, xpath);
            Node[] children = new Node[list.getLength()];
            for (int i = 0; i < children.length; i++) {
                children[i] = list.item(i);
            }
            return children;
        } catch (XPathExpressionException e) {
            throw new ADVRuntimeException("Cannot select node using xpath expression: " + xpath, e);
        }
    }

    public static ByteBuffer toBytes(final Node node, final boolean childrenOnly) {
        String s = convertToString(node, node, childrenOnly);
        ByteBuffer result = new ByteBuffer(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            result.append(s.charAt(i));
        }
        return result;
    }

    public static String convertToString(final Node node, final Node root, final boolean childrenOnly) {
        StringBuffer result = new StringBuffer(256);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (!(childrenOnly && node == root)) {
                result.append(startTagString((Element) node));
            }
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                result.append(convertToString(list.item(i), root, childrenOnly));
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            String text = ((Text) node).getData();
            if (text.length() != 0 && !text.equals("\n") && !text.equals("\r") && !text.equals("\r\n")) {
                result.append(text);
            }
        } else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
            result.append(((CDATASection) node).getData());
        }
        return result.toString();
    }

    public static ByteBuffer getElementMD5Digest(Element node) {
        return getElementByteBuffer(node).getMD5Digest();
    }

    public static ByteBuffer getElementByteBuffer(Element node) {
        ByteBuffer result = new ByteBuffer();
        getElementByteBuffer(node, result);
        return result;
    }

    private static void getElementByteBuffer(Element node, ByteBuffer buffer) {
        buffer.append(node.getTagName());
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getLength() > 0) {
            TreeMap<String,String> attrsMap = new TreeMap<String,String>();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                attrsMap.put(attr.getName(), attr.getValue());
            }
            for (String attrName : attrsMap.keySet()) {
                buffer.append(attrName).append( attrsMap.get(attrName) );
            }
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element child = checkIfElement(list.item(i));
            if (child != null) {
                getElementByteBuffer(child, buffer);
            }
        }
    }

    private static String startTagString(Element node) {
        StringBuffer result = new StringBuffer().append(node.getTagName());
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getLength() > 0) {
            TreeMap<String,String> attrsMap = new TreeMap<String,String>();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                attrsMap.put(attr.getName(), attr.getValue());
            }
            for (String attrName : attrsMap.keySet()) {
                result.append(attrName).append(attrsMap.get(attrName));
            }
        }
        return result.toString();
    }

    /**
     * ??? ??????? ??? Element.
     * ????????: ?????????? ??????? Element ? ????????? ??? ??????? ?????????? ???? String.hashCode()
     *
     * @return startTag.hashCode()+clildStrartTag.hashCode()+....
     */
    public static int hashCode(Node node) {
        int hash = 0;
        if (isElement(node)) {
            hash = startTagHash((Element) node);
        } else {
            //hash = 12345; //TODO node.hashCode(); for any type of DOM
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                hash = intHash(hash) + hashCode((Element) children.item(i));
            }
        }
        return hash;
    }

    /**
     * ????????? ????????? ??????? ??? int
     * ?????? ??????? word ? int
     *
     * @param i
     * @return
     */
    public static int intHash(int i) {
        return (i << 16) | (i >>> 16);
    }

    /**
     * ????????? ???????? ??? ??? ????????? Element
     */
    private static int startTagHash(Element node) {
        StringBuffer buff = new StringBuffer().append(node.getTagName());

        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getLength() > 0) {

            //??????? ???????? ??? ??? ?????????? ?????????? ?? ????? ?????????
            TreeMap<String,String> attrsMap = new TreeMap<String,String>();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                attrsMap.put(attr.getName(), attr.getValue());
            }

            // ?????? ??????? ???????? ? ??????
            buff.append(" ");
            for (Iterator<String> i = attrsMap.keySet().iterator(); i.hasNext();) {
                String attrName = i.next();
                buff.append(attrName).append("=").append(attrsMap.get(attrName));
                if (i.hasNext()) {
                    buff.append(" ");
                }
            }
        }
        return buff.toString().hashCode();
    }


    /**
     * ?????????, ??? ???? ???????? ???????? ???????????? ????
     *
     * @param testNode      ???????? ?? ??? ???? ????????
     * @param compareToNode ????, ???????????? ??????? testNode ?????? ???? ????????
     */
    public static boolean isDescendant(Node testNode, Node compareToNode) {
        if (testNode != null && compareToNode != null) {
            while (testNode.getParentNode() != null) {
                if (testNode.getParentNode() == compareToNode) {
                    return true;
                }
                testNode = testNode.getParentNode();
            }
        }
        return false;
    }

    /**
     * ??????? DocumentFragment ?? ?????? ?????????? ?????? stringXPath ?? ????
     * sourceNode. ? DocumentFragment ??????????? ?????? ?? ???? ??????? ?????????????
     * stringXPath ??? ????????? child Node. ??????????? ??????????? ???.
     *
     * @param sourceNode
     * @param stringXPath
     * @param doc         Document, ??? ???????? ????????? DocumentFragment
     */
    public static DocumentFragment nodeMatch(Node sourceNode, String stringXPath, Document doc)
            throws XPathExpressionException {
        DocumentFragment df = doc.createDocumentFragment();
        NodeList nodeList = XmlUtils.selectNodeList(sourceNode, stringXPath);

        List<Node> list = new ArrayList<Node>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i));
        }
        // ????? ?? ?????? while - ???????????? ???????? ????????????

        Object[] nodes = list.toArray();

        List<Node[]> nodesInDocFragment = new ArrayList<Node[]>(); // ??????????????? ??????????? ????
        for (int i = 0; i < nodes.length; i++) {
            Node node = (Node) nodes[i];
            // ????????? ??????? ???????? ????,
            // ???????? ?? ????? ? ???????
            if (!hasAncestorNode(node, list)) {
                Node dfNode = getNearestAncestorNode(node, nodesInDocFragment);
                Node appendedNode;
                if (dfNode == null) {
                    // ??????? ? ?????? DocumentFragment
                    appendedNode = df.appendChild(doc.importNode(node, false));
                } else {
                    // ??????? ? ????????? ???????????? ???? ? DocumentFragment
                    appendedNode = dfNode.appendChild(doc.importNode(node, false));
                }
                // ?????????? ??? ??????????? ????
                nodesInDocFragment.add(new Node[]{node, appendedNode});
                // ????????? ?????? ??? ?????????? ??????
                list.remove(node);
            }
        }
        return df;
    }

    /**
     * true, ???? ?????????? ancestor ? ?????? ??? ????
     */
    private static boolean hasAncestorNode(Node node, List<Node> list) {
        for (Node n : list) {
            if (isDescendant(node, n)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ?????????? ????????? ?????????? ???????????? ???? ?? ??????
     * List ???????? Node[]{realNode,insertedNode}
     */
    private static Node getNearestAncestorNode(Node node, List<Node[]> list) {
        for (ListIterator<Node[]> i = list.listIterator(list.size()); i.hasPrevious();) {
            Node[] nodes = i.previous();
            if (isDescendant(node, nodes[0])) {
                return nodes[1];
            }
        }
        return null;
    }

    public static Map<String,String> getMapOfAttributes(Node elem) {
        Map<String,String> attrs = new HashMap<String,String>();
        NamedNodeMap m = elem.getAttributes();
        if (m!=null) {
	        for (int i = 0; i < m.getLength(); ++i) {
	            Attr a = (Attr) m.item(i);
	            attrs.put(a.getName(), a.getValue());
	        }
        }
        return attrs;
    }

    public static boolean isText(Node node) {
        return node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE;
    }

    public static boolean isElement(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    public static boolean isAttribute(Node node) {
        return node.getNodeType() == Node.ATTRIBUTE_NODE;
    }

    public static final Element checkIfElement(Node node) {
        Element result = null;
        if (isElement(node)) {
            result = (Element) node;
        }
        return result;
    }

    public static final Element checkIfElement(Node node, String tag) {
        Element result = null;
        if (isElement(node)) {
            Element tmp = (Element) node;
            if (tag == null || tmp.getTagName().equals(tag)) {
                result = tmp;
            }
        }
        return result;
    }

    public static void insertBeforeFirstChild(Element object, Element attrId) {
        if (object.hasChildNodes()) {
            object.insertBefore(attrId, object.getFirstChild());
        } else {
            object.appendChild(attrId);
        }
    }

    public static String getText(Element element) {
        return getText(element,  true);
    }

    public static String getText(Element element, boolean trim) {
        StringBuffer result = new StringBuffer();
        NodeList list = element.getChildNodes();
        if (list != null) {
            for (int i = 0; i < list.getLength(); ++i) {
                Node node = list.item(i);
                if (!isText(node)) {
                    continue;
                }
                String value = ((Text) node).getData();
                if (trim)
                    value = value.trim();
                result.append(value);
            }
        }
        return result.toString();
    }



    public static List<ProcessingInstruction> findAllProcessingIstructions(Node node, String name) {
        List<ProcessingInstruction> result = new ArrayList<ProcessingInstruction>();
        findAllProcessingIstructions(node, name, result);
        return result;
    }

    private static void findAllProcessingIstructions(Node node, String name, List<ProcessingInstruction> result) {
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);
            if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                if (name == null || name.length() == 0 || n.getNodeName().equals(name)) {
                    result.add((ProcessingInstruction)n);
                }
            }
            findAllProcessingIstructions(n, name, result);
        }
    }

    public static List<Element> findAllElements(Node node, Collection<String> names, boolean deep) {
        try {
            return findAllElements(node, names, deep, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public static List<Element> findAllElements(Node node, Collection<String> names) {
        try {
            return findAllElements(node, names, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public static List<Element> findAllElements(Node node, String name, boolean deep) {
        try {
            return findAllElements(node, name, deep, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }
    
    public static List<Element> findAllElements(Element node, String name, boolean deep) {
        try {
            return findAllElements(node, name, deep, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public static List<Element> findAllElements(Node node, String name) {
        try {
            return findAllElements(node, name, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public static List<Element> findAllElements(Node node, Collection<String> names, boolean deep, XMLVisitor visitor) throws ErrorCodeException {
        List<Element> result = new LinkedList<Element>();
        findAllElements(node, names, result, deep, visitor, 0);
        return result;
    }

    public static List<Element> findAllElements(Node node, Collection<String> names, XMLVisitor visitor) throws ErrorCodeException {
        return findAllElements(node, names, true, visitor);
    }

    /**
     * Find elements by name
     * @param node
     * @param name element name
     * @param deep use recursion
     * @param visitor may be null
     * @return List of descendant or children elements with name
     * @throws ErrorCodeException
     */
    public static List<Element> findAllElements(Node node, String name, boolean deep, XMLVisitor visitor) throws ErrorCodeException {
        ArrayList<String> names = new ArrayList<String>(1);
        names.add(name);
        return findAllElements(node, names, deep, visitor);
    }

    public static List<Element> findAllElements(Node node, String name, XMLVisitor visitor) throws ErrorCodeException {
        return findAllElements(node, name, true, visitor);
    }

    private static void findAllElements(Node node, Collection<String> names, List<Element> result, boolean deep, XMLVisitor visitor, int level) throws ErrorCodeException {
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);
            if (isElement(n)) {
                for (String name : names) {
                    if (name == null || name.length() == 0 || n.getNodeName().equals(name)) {
                        result.add( (Element)n );
                        if (visitor != null) {
                            visitor.visit( (Element) n, level);
                        }
                    }
                }
            }
            if (deep) {
                findAllElements(n, names, result, deep, visitor, level+1);
            }
        }
    }

    public static Element findElement(Node node, String tagName) {
        Element result = null;
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null) {
                result = element;
                break;
            }
        }
        return result;
    }

    public static List<Element> findElementsWithAttribute(Node node, String tagName, String attrName) {
        try {
            return findElementsWithAttribute(node, tagName, attrName, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public static List<Element> findElementsWithAttribute(Node node, String tagName, String attrName, XMLVisitor visitor) throws ErrorCodeException {
        List<Element> result = new LinkedList<Element>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null && element.hasAttribute(attrName)) {
                result.add(element);
                if (visitor != null) {
                    visitor.visit(element, 0);
                }
            }
        }
        return result;
    }

    public static Element findElementWithAttribute(Node node, String tagName, String attrName) {
        Element result = null;
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null && element.hasAttribute(attrName)) {
                result = element;
                break;
            }
        }
        return result;
    }

    public static Element findElementByAttribute(Node node, String tagName, String attrName, String attrValue) {
        Element result = null;
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null && element.getAttribute(attrName).equals(attrValue)) {
                result = element;
                break;
            }
        }
        return result;
    }


    public static List<Element> findElementsByAttribute(Element node, String tagName, String attrName, List<String> attrValues) {
        List<Element> result = new LinkedList<Element>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null) {
                for (String value : attrValues) {
                    if (element.getAttribute(attrName).equals(value)) {
                        result.add(element);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static List<Element> findAllElementsByAttributes(Element node, String tagName, String attrName, List<String> attrValues) {
        List<Element> result = new ArrayList<Element>();
        findAllElementsByAttributes(node, tagName, attrName, attrValues, result);
        return result;
    }

    public static List<Element> findElementsByAttribute(Node node, String tagName, String attrName, String attrValue) {
        List<Element> result = new ArrayList<Element>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null && element.getAttribute(attrName).equals(attrValue)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Reqursion search in node for elements with paramantrs
     * @param node
     * @param tagName
     * @param attrName
     * @param attrValue
     * @return
     */
    public static List<Element> findAllElementsByAttribute(Node node, String tagName, String attrName, String attrValue) {
        List<Element> result = new LinkedList<Element>();
        findAllElementsByAttribute(node, tagName, attrName, attrValue, result);
        return result;
    }

    private static void findAllElementsByAttribute(Node node, String tagName, String attrName, String attrValue, List<Element> result) {
        if (node == null) {
            return;
        }
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node currNode = nodeList.item(i);
            Element element = checkIfElement(currNode, tagName);
            if (element != null && element.getAttribute(attrName).equals(attrValue)) {
                result.add(element);
                continue;
            }
            findAllElementsByAttribute(currNode, tagName, attrName, attrValue, result);
        }
    }

    private static void findAllElementsByAttributes(Node node, String tagName, String attrName, List<String> attrValues, List<Element> result) {
        if (node == null) {
            return;
        }
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node currNode = nodeList.item(i);
            Element element = checkIfElement(currNode, tagName);
            if (element != null) {
                for (String value : attrValues) {
                    if (element.getAttribute(attrName).equals(value)) {
                        result.add(element);
                        break;
                    }
                }
            }
            findAllElementsByAttributes(currNode, tagName, attrName, attrValues, result);
        }
    }

    public static Element findElementWithAttributes(Node node, String tagName, Collection<String> attrs) {
        Element result = null;
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null) {
                boolean match = true;
                for (String attrName : attrs) {
                    if (!element.hasAttribute(attrName)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    result = element;
                    break;
                }
            }
        }
        return result;
    }

    public static List<Element> findChildElements(Node node, String tagName) {
        try {
            return findChildElements(node, tagName, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public static List<Element> findChildElements(Node node, String tagName, XMLVisitor visitor) throws ErrorCodeException {
        List<Element> result = new LinkedList<Element>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null) {
                result.add(element);
                if (visitor != null) {
                    visitor.visit(element, 0);
                }
            }
        }
        return result;
    }

    public static Element findFirstElement(Node node, String tagName) {
        try {
            return findFirstElement(node, tagName, null);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }
    
    public static Element findFirstElement(Node node, String tagName, XMLVisitor visitor) throws ErrorCodeException {
        Element result = null;
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return result;
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Element element = checkIfElement(nodeList.item(i), tagName);
            if (element != null) {
                result = element;
                if (visitor != null) {
                    visitor.visit(element, 0);
                }
                break;
            }
        }
        return result;
    }
    
    public static List<Attr> findAllAttributes(Node node, XMLAttributeVisitor visitor) throws ErrorCodeException {
        List<Attr> result = new ArrayList<Attr>();
        findAllAttributes(node, result, visitor);
        return result;
    }

    private static void findAllAttributes(Node node, List<Attr> result, XMLAttributeVisitor visitor) throws ErrorCodeException {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);
            if (isElement(n)) {
                Element element = (Element) n;
                NamedNodeMap attrs = element.getAttributes();
                for (int j = 0; j < attrs.getLength(); ++j) {
                    Attr attr = (Attr) attrs.item(j);
                    result.add(attr);
                    if (visitor != null) {
                        visitor.visit(attr);
                    }
                }
            }
            findAllAttributes(n, result, visitor);
        }
    }

    public static void traversNodes(Node node, XMLNodeVisitor visitor) throws  ErrorCodeException {
        if (visitor == null) {
            throw new IllegalArgumentException();
        }
        Node[] nodes = childrenArray(node);
        visitor.visit(node);
        for (int i = 0; i < nodes.length; ++i) {
            Node n = nodes[i];
            traversNodes(n, visitor);
        }
    }

    public static long getNodeSize(Node node) {
        SizeVisitor visitor = new SizeVisitor();
        try {
            traversNodes(node, visitor);
        } catch (ErrorCodeException e) {
            throw new UnreachableCodeReachedException(e);
        }
        return visitor.getSize();
    }
}

class SizeVisitor implements XMLNodeVisitor {

    private long size;

    public void visit(Node node) {
        if (XmlUtils.isElement(node)) {
            size += ((Element)node).getTagName().length();
        } else if (XmlUtils.isAttribute(node)) {
            size += ((Attr)node).getName().length();
            size += ((Attr)node).getValue().length();
        } else if (XmlUtils.isText(node)) {
            size += ((Text)node).getData().length();
        }
    }

    public long getSize() {
        return size;
    }
}

class DefaultNameSpaceContext implements NamespaceContext {
	
	private HashMap<String, String> nsMap = new HashMap<String, String>();
	private HashMap<String, String> uriMap = new HashMap<String, String>();
	
	public DefaultNameSpaceContext() {
		nsMap.put(Request.DOM_NAMESPACE, Request.DOM_NAMESPACE_URI);
		nsMap.put(ContextAttrXmlGenerator.NAMESPACE_CONTEXT_NAME, ContextAttrXmlGenerator.NAMESPACE_CONTEXT_URI);
		nsMap.put(Query.DOM_GROUP_NAMESPACE, Query.DOM_GROUP_NAMESPACE_URI);
		nsMap.put(Base.ERROR_ELEMENT_NAMESPACE, Base.ERROR_ELEMENT_NAMESPACE_URI);
		nsMap.put(NewtProcessor.NEWT_NAMESPACE_PREEFIX, NewtProcessor.NEWT_NAMESPACE_URI);
		for ( Entry<String, String> entry : nsMap.entrySet()) {
			uriMap.put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public String getNamespaceURI(String prefix) {
		String result = nsMap.get(prefix);
		return result!=null ? result : XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return uriMap.get(namespaceURI);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator getPrefixes(String namespaceURI) {
		return null;
	}
	
}


