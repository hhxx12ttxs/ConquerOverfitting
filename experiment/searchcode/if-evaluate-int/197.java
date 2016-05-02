/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)Utility.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.engine.bpel.core.bpel.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.BasicVariables;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.JXPathContextFactoryReferenceImpl;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.dom.DOMAttributePointer;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.bpel.model.BPELDocument;
import com.sun.bpel.model.BPELElement;
import com.sun.bpel.model.Copy;
import com.sun.bpel.model.EventHandlersOnAlarm;
import com.sun.bpel.model.SystemFault;
import com.sun.bpel.model.meta.RActivity;
import com.sun.bpel.model.meta.RExpressionElement;
import com.sun.bpel.model.meta.RStartElement;
import com.sun.bpel.model.meta.RVariable;
import com.sun.bpel.model.util.Utility.XpathVariablePropertyInfo;
import com.sun.bpel.xml.common.model.XMLElement;
import com.sun.jbi.common.classloader.CustomClassLoaderUtil;
import com.sun.jbi.common.qos.ServiceQuality;
import com.sun.jbi.common.util.Base64Utils;
import com.sun.jbi.engine.bpel.core.bpel.engine.BPELProcessManager;
import com.sun.jbi.engine.bpel.core.bpel.engine.BPELSERegistry;
import com.sun.jbi.engine.bpel.core.bpel.engine.EPReferenceComposer;
import com.sun.jbi.engine.bpel.core.bpel.engine.Engine;
import com.sun.jbi.engine.bpel.core.bpel.engine.Event;
import com.sun.jbi.engine.bpel.core.bpel.engine.ICallFrame;
import com.sun.jbi.engine.bpel.core.bpel.engine.InComingEventModel;
import com.sun.jbi.engine.bpel.core.bpel.engine.InComingEventModelFactory;
import com.sun.jbi.engine.bpel.core.bpel.engine.XmlResourceProvider;
import com.sun.jbi.engine.bpel.core.bpel.engine.XmlResourceProviderPool;
import com.sun.jbi.engine.bpel.core.bpel.engine.Engine.TransformEngine;
import com.sun.jbi.engine.bpel.core.bpel.engine.impl.CorrelatingSAInComingEventKeyImpl;
import com.sun.jbi.engine.bpel.core.bpel.engine.impl.InComingEventKeyImpl;
import com.sun.jbi.engine.bpel.core.bpel.engine.impl.JBIMessageImpl;
import com.sun.jbi.engine.bpel.core.bpel.engine.impl.SAInComingEventKeyImpl;
import com.sun.jbi.engine.bpel.core.bpel.exception.BPELRuntimeException;
import com.sun.jbi.engine.bpel.core.bpel.exception.POJOException;
import com.sun.jbi.engine.bpel.core.bpel.exception.StandardException;
import com.sun.jbi.engine.bpel.core.bpel.model.runtime.ActivityUnit;
import com.sun.jbi.engine.bpel.core.bpel.model.runtime.Context;
import com.sun.jbi.engine.bpel.core.bpel.model.runtime.FaultHandlingContext;
import com.sun.jbi.engine.bpel.core.bpel.model.runtime.RuntimeVariable;
import com.sun.jbi.engine.bpel.core.bpel.model.runtime.VariableScope;
import com.sun.jbi.engine.bpel.core.bpel.model.runtime.WSMessage;
import com.sun.jbi.engine.bpel.core.bpel.xpath.functions.BPWSFunctions;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.wsdl4j.ext.NamespaceDeclarations;
import com.sun.wsdl4j.ext.WSDL4JExt;
import com.sun.wsdl4j.ext.bpel.MessagePropertyAlias;
import com.sun.wsdl4j.ext.bpel.MessagePropertyAlias.Query;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import javax.jbi.messaging.MessageExchange;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Utility class
 *
 * @author Sun Microsystems
 */
public final class Utility {
	private static final Logger LOGGER = Logger.getLogger(Utility.class.getName());

	/**
	 * JXPathContext Factory: be careful to use this instead of JXPathContext.newContext(). That
	 * creates a context factory internally, which, in turn, reads from the file system. By
	 * creating our own factory we only pay that penalty once.
	 */
	public static final JXPathContextFactory JXPATH_FACTORY = new BPELXPathContextFactoryImpl();

	private static BPWSFunctions bpwsFunctions = new BPWSFunctions();
	private static final Pattern pGt = Pattern.compile("(&gt;)");
	private static final Pattern pLt = Pattern.compile("(&lt;)");
	private static final Pattern pQm = Pattern.compile("(&quot;)");
	private static final Pattern pAp = Pattern.compile("(&apos;)");
	private static final Pattern pAmp = Pattern.compile("(&amp;)");

	public static String getCRMPInvokeId(ICallFrame callFrame, ActivityUnit actUnit) {
		String crmpInvokeId = callFrame.getCallFrameCRMPId()+ ":-" 
		+ actUnit.getStaticModelActivity().getUniqueId() + ":-" 
		+ callFrame.getBranchInvokeCounter();
		return crmpInvokeId;
	}

        public static String getCRMPId(MessageExchange exchange){
            String crmpId = null;
            Object mId = exchange.getProperty(ServiceQuality.MESSAGE_ID);

            if (mId != null && mId instanceof String)
                crmpId = ((String)crmpId).trim();

            Object grpId = exchange.getProperty(ServiceQuality.GROUP_ID);

            if (grpId != null && grpId instanceof String)
                crmpId = crmpId == null ? (String)grpId : crmpId.concat((String)grpId);                

            return crmpId;

        }

	/**
	 * Test if a string is empty.
	 *
	 * @param s String to test
	 *
	 * @return <code>true</code> if string is empty
	 */
	public static boolean isEmpty(String s) {
		return ((null == s) || (s.trim().length() == 0));
	}

	/**
	 * Test if two objects are equal.
	 *
	 * @param s1 First object.
	 * @param s2 Second object.
	 *
	 * @return <code>true</code> if objects are equal.
	 */
	public static boolean areEqual(Object o1, Object o2) {
		return ((o1 == null) ? (o2 == null) : o1.equals(o2));
	}

	/**
	 * Test if two strings are equal in XML.
	 *
	 * @param s1 First string.
	 * @param s2 Second string.
	 *
	 * @return <code>true</code> if strings are equal.
	 */
	public static boolean areEqualXMLValues(String s1, String s2) {
		return (((s1 == null) && (s2 == null)) || ((s1 == null) && isEmpty(s2)) ||
				(isEmpty(s1) && (s2 == null)) || ((s1 != null) && s1.equals(s2)));
	}

	/**
	 * converts to jxpath query
	 *
	 * @param part part string
	 * @param query query string
	 * @param var variable
	 *
	 * @return String query string
	 */
	public static String convertQuery(String part, String query, RVariable var) {
		Part partObj = var.getWSDLMessageType().getPart(part);
		QName elem = partObj.getElementName();

		if (elem != null) {
			if (isEmpty(query)) {
				query = "/" + elem.getLocalPart();
			} else {
				query = "/" + elem.getLocalPart() + query;
			}
		} else {
			if (isEmpty(query)) {
				query = ".";
			}
		}
		return query;
	}

	/** if the part is of type element, there are two possibilities, shown below.
	 * An absolute path and a relative path.
	 * 
    <message name="Invoke1parentMessage1">
        <part name="Invoke1parentPart"
              element="tns:Invoke1parentElement"></part>
    </message>

    <bpws:property name="property1"
                   type="xsd:string"/>

....

    <bpws:propertyAlias propertyName="tns:property1"
                        messageType="tns:Invoke1parentMessage1"
                        part="Invoke1parentPart">
            <bpws:query>/Invoke1parentElement/string</bpws:query>
    </bpws:propertyAlias> 
(or)
    <bpws:propertyAlias propertyName="tns:property1"
                        messageType="tns:Invoke1parentMessage1"
                        part="Invoke1parentPart">
            <bpws:query>string</bpws:query>
    </bpws:propertyAlias> 
	 * 
	 * 
	 * @param part
	 * @param query
	 * @param var
	 * @return
	 */
	public static String convertPropAliasQuery(String part, String query, RVariable var) {
		if (query != null && query.startsWith("/")) {
			// don't change the query, since the / means it is an absolute path.
			return query;
		}
		Part partObj = var.getWSDLMessageType().getPart(part);
		QName elem = partObj.getElementName();

		if (elem != null) {
			if (isEmpty(query)) {
				query = "/" + elem.getLocalPart();
			} else {
				query = "/" + elem.getLocalPart() + "/" + query;
			}
		} else {
			if (isEmpty(query)) {
				query = ".";
			}
		}

		return query;
	}

	/**
	 * Both Pick and Receive use this.
	 *
	 * @param se runtime start element
	 * @param frame callframe
	 *
	 * @return InComingEventKeyImpl incoming event key
	 *
	 * @throws CorrelationConsistencyConstraintFailed DOCUMENT ME!
	 */
	public static InComingEventKeyImpl createSAInComingEventKeyImpl(RStartElement se, 
			ICallFrame frame, BPELProcessManager procMgr) {

		String operPattern = procMgr.getOperationPattern(se);

		InComingEventModel model = InComingEventModelFactory.createModel(
				procMgr.getBPELProcess(), se, operPattern
		);
		int createFlag = se.getStartType();
		InComingEventKeyImpl event = null;

		if (createFlag == Engine.RECEIVE_TYPE_CREATE_ONLY) {
			event = new SAInComingEventKeyImpl(
					model, Event.REQUEST, frame.getBPId());
		} else if (createFlag == Engine.RECEIVE_TYPE_CORRELATE_ONLY) {
			List corrVals = frame.getProcessInstance().getInitiatedCorrelations(se);
			if (corrVals == null || corrVals.isEmpty()) {
				throw new BPELRuntimeException(BPELRuntimeException.CORRELATION_ERROR, 
						I18n.loc("BPCOR-6106: correlations with initiate value 'no' are not defined"));
			}
			event = new CorrelatingSAInComingEventKeyImpl(
					model, Event.REQUEST, corrVals);
		} else if (createFlag == Engine.RECEIVE_TYPE_CREATE_OR_CORRELATE) {
			List corrVals = frame.getProcessInstance().getCommonJoinInitiatedCorrelations(se);
			event = new CorrelatingSAInComingEventKeyImpl(
					model, Event.REQUEST, corrVals);
		} else if (createFlag == Engine.RECEIVE_TYPE_NO_COR_JOIN_ONLY) {
			//TODO: needs to be fixed when we implement engine correlation - ie. matching 
			//instances when there is no correlation defined on Receive activity
			event = new SAInComingEventKeyImpl(
					model, Event.REQUEST, frame.getBPId());
		}

		return event;
	}

	/**
	 * converts wsdl4j Message to wsdlmodel WSDLMessage
	 *
	 * @param message wsdl4j Message
	 *
	 * @return WSDLMessage wsdlmodel message
	 */
	public static Message getCommonMessage(Message message) {
//		WSDLMessage ret = new WSDLMessageImpl();
//		com.sun.wsdl.model.Part part = new com.sun.wsdl.model.impl.PartImpl();
//		part.setName(WrapperBuilder.RESULT_TAG);
//		ret.addPart(part);
//		QName qName = com.sun.wsdl.model.NamespaceUtility.getQName(WrapperBuilder.STATUS_TAG);
//		ret.setQualifiedName(qName);
//
//		return ret;
	    return message;
	}

	/**
	 * Converts xml encoded String to normal text, it converts '&gt;' to '>', '&lt;' to '<', 
	 * '&quot;' to  '"', '&apos;' to '\'', '&amp;' to '&'
	 * 
	 * @param toBeConverted
	 * @return String the converted String
	 */
	public static String convertXmlSpecialChars(String toBeConverted) {
		String inter1 = pGt.matcher(toBeConverted).replaceAll(">");
		inter1 = pLt.matcher(inter1).replaceAll("<");
		inter1 = pQm.matcher(inter1).replaceAll("\"");
		inter1 = pAp.matcher(inter1).replaceAll("'");
		inter1 = pAmp.matcher(inter1).replaceAll("&");
		return inter1;
	}

	public static void addToListInMap (Object key, Map map, Object value) {
		List list = (List) map.get(key);
		if (list == null) {
			list = new ArrayList();   
			map.put(key,list); 
		}
		list.add(value);
	}

	public static Object removeFromListInMap(Object key, Map map) {
		List list = (List) map.get(key);
		if (list == null || list.size() == 0) {
			return null;
		} else if (list.size() == 1) {
			//when there are no more objects for a key remove key/List from the map
			map.remove(key);
		}
		return list.remove(0);
	}

	/**
	 * Evaluate a boolean XPath expression
	 * 
	 * @param element BPEL context/element on which xpath would be executed.
	 * @param xpath expression
	 * @param variableScope context for resolving variables
	 * @param frame TODO
	 * @return Result of evaluation
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public static boolean evaluateCondition(BPELElement element, String xpath, 
            VariableScope variableScope, ICallFrame frame) {
		try {
			JXPathContext jxpathContext = newJXPathContext(element, variableScope, null);
            Object result = jxpathContext.getValue(xpath);
			return InfoSetUtil.booleanValue(result);
		} catch (JXPathException ex) {
            String msg = I18n.loc("BPCOR-6052: Exception while evaluating the xpath {0} " +
                    "in BPEL({1}) \n at line number {2} " + 
                    "Associated BPEL artifact is: {3}", ex.getMessage(), frame.getProcess().getBPELId(), 
                    element.getLocator().getLineNumber(), element);
            LOGGER.log(Level.WARNING, msg);
            throw new RuntimeException(msg);
		}
	}
	
	public static Object evaluateProperty(BPELElement element,
	        String variableName, String propName, RuntimeVariable var) {
	    Object propertyValue = null;

	    String key = variableName + "#" + propName; //$NON-NLS-1$
	    MessagePropertyAlias propertyAlias = ((RExpressionElement) element).getPropertyAliasForVariableProperty(key);
	    // it is in-lined NM property 
	    //ns1:getVariableProperty('OrderOperationIn', 'com.sun.jbi.jms.invoke.send')
	    //getVariableProperty -> getNMVariableProperty
	    if (propertyAlias == null) {
	        if(var.getVariableDef().getMessageType()== null){
	            throw selectionFailureProperty(propName);
	        }
	        WSMessage wsMessage = var.getWSMessage();
	        if (wsMessage == null) {
	            throw uninitializedVariableError(var.getVariableDef().getName());
	        }
	        propertyValue = wsMessage.getNMProperty(propName);
	        //check for selection failure
	        if (propertyValue == null) {
	            throw selectionFailureQueryNMProperty(propName);
	        }
	        return propertyValue;
	    }

	    String nmProperty = propertyAlias.getNMProperty();

	    // property points to NM property In the wsdl 
	    // <vprop:propertyAlias propertyName="tns:intProp" nmProperty="com.sun.jms.transport.properties"/>
	    if (nmProperty != null) {
	        WSMessage wsMessage = var.getWSMessage();
	        propertyValue = wsMessage.getNMProperty(nmProperty);
	        //check for selection failure
	        if (propertyValue == null) {
	            throw selectionFailureQueryNMProperty(nmProperty);
	        }

	        if (propertyAlias.getQuery() == null) {
	            return propertyValue;
	        }
	        // evaluate query
	        String query = propertyAlias.getQuery().getQueryString();
	        String retVal[] = getKeyForNMProperty(propertyAlias.getQuery(), query);
	        query = retVal[0];
	        String keyForNMProperty = retVal[1]; 
	        propertyValue = ((Map)propertyValue).get(keyForNMProperty);
	        if(propertyValue instanceof DocumentFragment){
	            propertyValue = ((DocumentFragment)propertyValue).getFirstChild();
	        }
	        //check for selection failure
	        if (propertyValue == null) {
	            throw selectionFailureQueryNMProperty(nmProperty);
	        }
	        if (Utility.isEmpty(query)) {
	            return propertyValue;
	        }

	        JXPathContext ctx = createJXPathContextOnObject(propertyAlias, propertyValue);
	        Pointer pointer = ctx.getPointer(query);
	        propertyValue = pointer.getValue();
	        //check for selection failure
	        if (propertyValue == null) {
	            throw selectionFailureQueryNMProperty(nmProperty, query);
	        }
	        return propertyValue;
	    }

	    //ws-bpel  <from variable="NCName" property="QName"/> & getVariableProperty support
	    if (propertyAlias.getMessageType() != null) {
	        WSMessage wsMessage = var.getWSMessage();
	        propertyValue = wsMessage.getPart(propertyAlias.getPartName());
	        if (propertyAlias.getQuery() == null) {
	            if(propertyValue instanceof Node){
	                return ((Node)propertyValue).getTextContent();
	            }
	            return propertyValue;
	        }
	        // evaluate query
	        Pointer pointer = executeQuery(propertyAlias, propertyAlias.getQuery().getQueryString(), propertyValue);
	        return pointer.getValue();
	    }
	    propertyValue = var.getXSDVariableData();
	    if (propertyAlias.getQuery() == null) {
	        return propertyValue;
	    }
	    // evaluate query
	    Pointer pointer = executeQuery(propertyAlias, propertyAlias.getQuery().getQueryString(), propertyValue);
	    return pointer.getValue();
	}

	public static QName getQNamefromString(Query queryElement, String propExpr) {
	    // remove leading / if it is present
	    propExpr = propExpr.trim();
	    if (propExpr.charAt(0) == '/') {
	        propExpr = propExpr.substring(1);
	    }

	    int colonIndex = propExpr.indexOf(":"); //$NON-NLS-1$
	    if (colonIndex < 0) {
	        QName propertyQName = new QName(propExpr);
	        return propertyQName;
	    }
	    // extract the namespace prefix from the propName.
	    String prefix = propExpr.substring(0, colonIndex);
	    String localPart = propExpr.substring(colonIndex + 1);
	    String namespaceURI = queryElement.getNamespaceDeclarations().lookUpNamespaceURI(prefix);
	    QName propertyQName = new QName(namespaceURI, localPart);
	    return propertyQName;
	}
	
	public static QName getQNamefromBPELElement(XMLElement element,
	        String propExpr) {
	    int colonIndex = propExpr.indexOf(":"); //$NON-NLS-1$
	    if (colonIndex < 0) {
	        return null;
	    }
	    // extract the namespace prefix from the propName.
	    String prefix = propExpr.substring(0, colonIndex);
	    if (Utility.isEmpty(prefix)) {
	        return null;
	    }
	    
	    String localPart = propExpr.substring(colonIndex + 1);
        if (Utility.isEmpty(localPart)) {
            return null;
        }

	    String namespaceURI = element.getNamespace(prefix);
	    if (Utility.isEmpty(namespaceURI)) {
            return null;
        }
	    
	    QName propertyQName = new QName(namespaceURI, localPart, prefix);
	    return propertyQName;
	}

	public static Pointer executeQuery(MessagePropertyAlias propAlias,
	        String query, Object src) {
	    query = adjustXpathQuery(query);

	    JXPathContext ctx = createJXPathContextOnObject(propAlias, src);

	    // property alias evaluation should always be a node.
	    // It always has to be an instance of the node.
	    return ctx.getPointer(query);
	}
	public static JXPathContext createJXPathContextOnObject(
	        MessagePropertyAlias propAlias, Object src) {
	    JXPathContext ctx = Utility.newJXPathContext(src, 
	            propAlias.getQuery().getNamespaceDeclarations());
	    return ctx;
	}

	public static JXPathContext createJXPathContextOnObject(
	        MessagePropertyAlias propAlias, Object src, DOMFactory factory) {
	    JXPathContext ctx = Utility.newJXPathContext(src, 
	            propAlias.getQuery().getNamespaceDeclarations());
	    ctx.setFactory(factory);
	    return ctx;
	}

	public static String adjustXpathQuery(String query) {
	    if (query.charAt(0) == '/') {
	        // ctx = Utility.newJXPathContext(src.getOwnerDocument(),
	        // propAlias);
	        // due to changes to JBIMessageImpl, the part are elements (no new
	        // document for part is created)
	        // hence stripping off the queries that start with / for e.g the for
	        // query '/partElement/subElement'
	        // the following will produce 'subElement'
	        query = query.substring(query.substring(1).indexOf("/") + 2);
	    }
	    return query;
	}

	public static String[] getKeyForNMProperty(Query queryElement, String query) {
	    // trimming query
	    query = query.trim();

	    String[] retVal = new String[2];
	    String key = null;
	    // valid one would be /sht:HeaderElement/e1/e2
	    if (query.charAt(0) == '/') {
	        query = query.substring(1);
	    }
	    int nextIndex = query.indexOf("/");
	    /* /sht:HeaderElement is also valid query */
	    if (nextIndex > 0) {
	        key = query.substring(0, nextIndex);
	        retVal[0] = query.substring(nextIndex + 1);
	    } else {
	      //it is simple query with one step, use it as key
	        key = query;
	        // no query, set it to null
	        retVal[0] = null;
	    }

	    retVal[1] = getQNamefromString(queryElement, key).toString();

	    return retVal;
	}
	public static DocumentFragment wrapInDocumentFragment(Node node) {
	    if (node instanceof DocumentFragment) {
	        return (DocumentFragment) node;
	    }
	    if(node instanceof Document){
	        node = ((Document)node).getDocumentElement();
	    }
	    XmlResourceProviderPool resourcePool = (XmlResourceProviderPool) BPELSERegistry.getInstance().lookup(
	            XmlResourceProviderPool.class.getName());
	    XmlResourceProvider xmlResourceProvider = resourcePool.acquireXmlResourceProvider();
	    Document document = xmlResourceProvider.getDocumentBuilder().newDocument();
	    resourcePool.releaseXmlResourceProvider(xmlResourceProvider);
	    xmlResourceProvider = null;

	    DocumentFragment df = document.createDocumentFragment();
	    Node importedElement = document.importNode(node, true);
	    df.appendChild(importedElement);

	    return df;
	}
    
	/**
     * Obtain the value of the xpath expression from the context
     * 
     * @param expr
     *            The xpath expression String, must not be null
     * @param exprList
     *            The expressionList, if null, the expr will be parsed to create
     *            an expression list
     * @param frame
     *            The callframe from which to obtain the variables value
     * @param varResolver
     *            The VaraibleResolver that resolves the runtime variable
     * @return The value object, either a String or a dom node
     * @throws Exception
     *             Any exception occurred
     */
	public static Object getXpathExpressionValue(String expr, List exprList,
	    BPELElement element, VariableScope variableScope) throws Exception {
		JXPathContext jxpathContext = newJXPathContext(element, variableScope);
	    if (exprList == null) {
		XpathVariablePropertyInfo info = com.sun.bpel.model.util.Utility
		    	.parseExprForVariables(expr, element);
		jxpathContext.setVariables(getXPathVariables(info.varInfoSet
			.iterator(), variableScope, null));
	    }

	    Pointer ptr = jxpathContext.getPointer(expr);
	    // DEVNOTE: If instance of variablePointer return node and not it's
	    // string value. Refer to CR 6516480
	    if (ptr instanceof BeanPointer || ptr instanceof DOMAttributePointer) { 
		// ||ptr instanceof VariablePointer) {		
		Object objValue = ptr.getValue();
		return removeTrailingZerosIfInt(objValue.toString());
	    } else {
		Node nodeValue = (Node) ptr.getNode();
		return nodeValue;
	    }
	}

	/**
	 * Obtain a java.util.Date from XPath expression.
	 * 
	 * @param isFor <code>true</code> if the <code>expr</code> represents a duration
	 * @return DOCUMENT ME!
	 */
	public static Date getDateFromExpr(BPELElement element, Context variableScope, 
            String expr, boolean isFor)
        {
            return getDateFromExpr(element, variableScope, expr, isFor, null);
	}

	public static Date getDateFromExpr(BPELElement element, Context variableScope,
            String expr, boolean isFor, DateTime presettedDateTime) {
		DateTime dateTime = null;

		Date date;
        Object value;
        expr = RApplicationVariablesHelper.updateExprWithAppVars(variableScope, expr);

        try {
            JXPathContext jxpathContext = newJXPathContext(element, variableScope, null);
            value = jxpathContext.getValue(expr);
        } catch (JXPathException ex){
            String msg = I18n.loc("BPCOR-6085: Exception while evaluating the xpath {0} at line number {1} " +
                    "Associated BPEL artifact is: {2}", ex.getMessage(),
                    element.getLocator().getLineNumber(), element);
            LOGGER.log(Level.WARNING, msg);
            throw ex;
        }
        if (null == value) {
            String msg1 = I18n.loc("BPCOR-6056: Expression can't evaluate to null");
            LOGGER.log(Level.WARNING, msg1);
            String msg2 = I18n.loc("BPCOR-6085: Exception while evaluating the xpath {0} at line number {1} " +
                    "Associated BPEL artifact is: {2}", expr,
                    element.getLocator().getLineNumber(), element);
            LOGGER.log(Level.WARNING, msg2);
            throw new StandardException(StandardException.Fault.InvalidExpressionValue, msg2);
        }
        String result = value.toString();
        try {

            if (!isFor) {
                // <wait until="...">
                dateTime = DateTime.parse(result);
            } else {
                // <wait for="...">
                // presettedDateTime mostly used in tests
                dateTime = presettedDateTime != null ? presettedDateTime : new DateTime();

                Duration dur = Duration.parse(result);
                dateTime.add(dur);
            }

            date = dateTime.toCalendar().getTime();
        } catch (RuntimeException ex) {
            // Unfortunately DateTime and Duration parser throws RuntimeException and not a specific parser exception.
            // Because of that we can't differentiate a parse problem or a code problem and we convert all of them
            // to  InvalidExpressionValue fault
            String msg = I18n.loc("BPCOR-6085: Exception while evaluating the xpath {0} at line number {1} " +
                    "Associated BPEL artifact is: {2}", ex.getMessage(),
                    element.getLocator().getLineNumber(), element);
            LOGGER.log(Level.WARNING, msg);
            throw new StandardException(StandardException.Fault.InvalidExpressionValue, msg);
        }

		return date;
	}

        /**
	 * Create a new JXPathContext
	 * 
	 * @param element BPEL context/element on which xpath would be executed.
	 * @param variableScope context for resolving variables
	 * @param runtimeVarForUpdate when this present use this variable instead of using variable
	 *            scope for resolving variable
	 * @param bean Context value
	 * @return The new context
	 */
	public static JXPathContext newJXPathContext(BPELElement element,
			VariableScope variableScope,
			RuntimeVariable runtimeVarForUpdate) {
		// Create a new JXPathContext
		JXPathContext jxpathContext = JXPATH_FACTORY.newContext(null, null);
		jxpathContext.setLenient(true);
		jxpathContext.setFunctions(new BPWSFunctions(variableScope, element));

		// Get the variables extracted from expression
		Iterator xpathExpressionsList = ((RExpressionElement) element).getVariables();

		if(xpathExpressionsList.hasNext()){
			jxpathContext.setVariables(getXPathVariables(xpathExpressionsList, variableScope,
					runtimeVarForUpdate));
		}

		// Register namespaces
		NamespaceResolver resolver = new BPELNamespaceResolver(element.getTotalNamespaces());
		BPELXPathContext bpelCtx = (BPELXPathContext) jxpathContext; 
		bpelCtx.setNamespaceResolver(resolver);
		// pass base URI to context
		bpelCtx.setBaseURI(element.getOwnerDocument().getBaseURI());

		return jxpathContext;
	}

	/**
	 * Create a new JXPathContext
	 * 
	 * @param element BPEL context/element on which xpath would be executed.
	 * @param variableScope context for resolving variables
	 * @param runtimeVarForUpdate when this present use this variable instead of using variable
	 *            scope for resolving variable
	 * @param bean Context value
	 * @return The new context
	 */
	public static JXPathContext newJXPathContext(BPELElement element, VariableScope variableScope) {
		// Create a new JXPathContext
		JXPathContext jxpathContext = JXPATH_FACTORY.newContext(null, null);
		jxpathContext.setLenient(true);
		jxpathContext.setFunctions(new BPWSFunctions(variableScope, element));


		// Register namespaces
		NamespaceResolver resolver = new BPELNamespaceResolver(element.getTotalNamespaces());
		((BPELXPathContext) jxpathContext).setNamespaceResolver(resolver);

		return jxpathContext;
	}

    /**
     * Create a new JXPathContext
     * 
     * @param bean Context value
     * @param namespaceDeclarations
     * @return The new context
     */
    public static JXPathContext newJXPathContext(Object bean, NamespaceDeclarations nsDeclarations) {
        // Create a new JXPathContext
        JXPathContext jxpathContext = JXPATH_FACTORY.newContext(null, bean);
        jxpathContext.setLenient(true);
        jxpathContext.setFunctions(bpwsFunctions);

        // Register namespaces
        NamespaceResolver resolver = new BPELNamespaceResolver(nsDeclarations.getAll());
        ((BPELXPathContext) jxpathContext).setNamespaceResolver(resolver);

        return jxpathContext;
    }

	private static Variables getXPathVariables(Iterator varIterator,
			VariableScope variableScope,
			RuntimeVariable runtimeVarForUpdate) {
		Variables retVariables = new BasicVariables();
		RVariable varModelForUpdate = 
			(runtimeVarForUpdate != null) ? runtimeVarForUpdate.getVariableDef() : null;

			while (varIterator.hasNext()) {
				com.sun.bpel.model.util.Utility.XpathVariableInfo varInfo = 
					(com.sun.bpel.model.util.Utility.XpathVariableInfo) varIterator.next();

				RVariable varDef = varInfo.varDefinition;
				String variableName = varInfo.variableName;
				RuntimeVariable runtimeVariable = null;

				if (varModelForUpdate != null && variableName.equals(varModelForUpdate.getName())) {// TO
					// Need to set this variable on the JXPath Context
					varDef = varModelForUpdate;
					runtimeVariable = runtimeVarForUpdate;
				} 
				else {                                                                              // FROM
					// No variable for update provided, so get it from the VariableScope.
					// This variable will be set on the JXPath Context
					runtimeVariable = variableScope.getRuntimeVariable(varDef);
					verifyValue(variableName, varInfo.partName, runtimeVariable);
				}

				String jxpathVarName = varInfo.xpathVariableName;
				if (varDef.getWSDLMessageType() != null) {
					Object variableData = runtimeVariable.getWSMessage();
					if (variableData == null) {
						initializeVariableValue(runtimeVariable);
						variableData = runtimeVariable.getWSMessage();
					}
					String partName = varInfo.partName;
					if (!Utility.isEmpty(partName)) {
						Element element = ((WSMessage) variableData).getPart(partName);
						// element could be null only for the 'TO' case. This is because in the 'FROM' case,
						// the method verifyValue() will make sure that an uninitialized variable standard
						// fault is thrown if element for the given partname is null.
						if (element == null) {
							element = ((WSMessage) variableData).createPart(partName);
						}
						variableData = element;
						updateRootElementSchemaType(varDef, partName, (Element) variableData);
					}
					retVariables.declareVariable(jxpathVarName, variableData);
				} 
				else {
					if (runtimeVariable.getXSDVariableData() == null) {
						initializeVariableValue(runtimeVariable);
					}

					if (runtimeVariable.isSimpleType()){
						Object variableData = runtimeVariable.getXSDVariableData();
						retVariables.declareVariable(jxpathVarName, variableData);
					} 
					else {
						Element variableData = (Element) runtimeVariable.getXSDVariableData();
						updateRootElementSchemaType(varDef, null, variableData);
						retVariables.declareVariable(jxpathVarName, variableData);
					}
				}
			}
			return retVariables;
	}

	public static void verifyValue(String variableName, String partName, RuntimeVariable rvar) {
		if (rvar == null) {
			throw uninitializedVariableError(variableName);
		}
		else if (rvar.getVariableDef().getWSDLMessageType() != null) {
			if (rvar.getWSMessage() == null) {
				throw uninitializedVariableError(variableName);
			}
			if (Utility.isEmpty(partName)) {
				return;
			} else if (rvar.getWSMessage().getPart(partName) == null) { // verify presence of part
				throw uninitializedVariableError(variableName, partName);
			}
		}
		else if (rvar.getXSDVariableData() == null) {
			throw uninitializedVariableError(variableName);
		}
	}
	
	public static void verifyValue(String variableName, RuntimeVariable rvar, boolean verifyParts) {
		Utility.verifyValue(variableName, null, rvar);
		// rval.getWSMessage will not return null here since we 
		// just checked for that in Utility.verifyValue(variableName, null, rvar);
		if (verifyParts) {
                    List<Part> nonInitParts = rvar.getWSMessage().getNonInitializedParts();
                    if (nonInitParts != null && !nonInitParts.isEmpty()) {
                        List<String> nonInitPartNames = new ArrayList<String>();
                        for (Part part : nonInitParts) {
                            nonInitPartNames.add(part.getName());
                        }
                        throw uninitializedVariableError(variableName, nonInitPartNames);
                    }
		}
	}

	public static void initializeVariableValue(RuntimeVariable rvar) {
		RVariable varDef = rvar.getVariableDef();
		if (varDef.getMessageType() != null) {
            WSMessage message = new JBIMessageImpl(varDef.getWSDLMessageType());
            message.addInternalReference(varDef);
            rvar.setWSMessage(message);
        }
		else {
			QName elementName = varDef.getElement();
			Object xsdData = null;

			if (elementName != null) {
				xsdData = constructDocumentElement(elementName);
			} 
			else {
				if (varDef.isBoolean()) {
					xsdData = new Boolean(false);
				} 
				else if (varDef.isNumber()) {
					xsdData = new Double(0.0);
				} 
				else if (varDef.isString()) {
					xsdData = new String();
				} 
				else {
					xsdData = constructDocumentElement(varDef.getType());
				}
			}
			rvar.setXSDVariableData(xsdData);
		}
	}

	public static Element constructDocumentElement(QName elementName) {
		XmlResourceProviderPool xmlResProviderpool = (XmlResourceProviderPool) 
		BPELSERegistry.getInstance().lookup(
				XmlResourceProviderPool.class.getName());
		XmlResourceProvider xmlResourceProvider = xmlResProviderpool.acquireXmlResourceProvider();
		Document doc = xmlResourceProvider.getDocumentBuilder().newDocument();
		xmlResProviderpool.releaseXmlResourceProvider(xmlResourceProvider);
		xmlResourceProvider = null;

		Element documentElement = doc.createElementNS(elementName.getNamespaceURI(), 
				elementName.getLocalPart());
		doc.appendChild(documentElement);
		return documentElement;
	}

    public static Node constructElementWrapinDF(QName elementName) {
        XmlResourceProviderPool xmlResProviderpool = (XmlResourceProviderPool) 
        BPELSERegistry.getInstance().lookup(
                XmlResourceProviderPool.class.getName());
        XmlResourceProvider xmlResourceProvider = xmlResProviderpool.acquireXmlResourceProvider();
        Document document = xmlResourceProvider.getDocumentBuilder().newDocument();
        xmlResProviderpool.releaseXmlResourceProvider(xmlResourceProvider);
        xmlResourceProvider = null;

        Element element = document.createElementNS(elementName.getNamespaceURI(), 
                elementName.getLocalPart());
        DocumentFragment df = document.createDocumentFragment();
        df.appendChild(element);

        return df;
    }

    public static Node wrapinWSAEndpointReference(DocumentFragment df) {
        return wrapinNewEl(df, EPReferenceComposer.ENDPOINT_REFERENCE , EPReferenceComposer.WS_ADDRESSING_NS);
    }

    public static Node wrapinServiceRef(DocumentFragment df) {
        return wrapinNewEl(df, EPReferenceComposer.SERVICE_REF, EPReferenceComposer.SERVICE_REF_NS);
    }

    public static Node wrapinNewEl(DocumentFragment df, String elName, String elNS) {
        assert elName != null && elNS != null;
        XmlResourceProviderPool xmlResProviderpool = (XmlResourceProviderPool)
        BPELSERegistry.getInstance().lookup(
                XmlResourceProviderPool.class.getName());
        XmlResourceProvider xmlResourceProvider = xmlResProviderpool.acquireXmlResourceProvider();
        Document document = xmlResourceProvider.getDocumentBuilder().newDocument();
        xmlResProviderpool.releaseXmlResourceProvider(xmlResourceProvider);
        xmlResourceProvider = null;

        Element element = document.createElementNS(elNS, elName);
        Node importedDocFrag = document.importNode(df, true);
        element.appendChild(importedDocFrag);
        df = document.createDocumentFragment();
        df.appendChild(element);

        return df;
    }

    public static Node wrapinSrefWSAEndpointRef(DocumentFragment df) {
        XmlResourceProviderPool xmlResProviderpool = (XmlResourceProviderPool)
        BPELSERegistry.getInstance().lookup(
                XmlResourceProviderPool.class.getName());
        XmlResourceProvider xmlResourceProvider = xmlResProviderpool.acquireXmlResourceProvider();
        Document document = xmlResourceProvider.getDocumentBuilder().newDocument();
        xmlResProviderpool.releaseXmlResourceProvider(xmlResourceProvider);
        xmlResourceProvider = null;

        Element wsaEPRElement = document.createElementNS(
                EPReferenceComposer.WS_ADDRESSING_NS,EPReferenceComposer.ENDPOINT_REFERENCE);
        Node importedDocFrag = document.importNode(df, true);
        wsaEPRElement.appendChild(importedDocFrag);


        Element srefElement = document.createElementNS(
                EPReferenceComposer.SERVICE_REF_NS,EPReferenceComposer.SERVICE_REF);
        srefElement.appendChild(wsaEPRElement);

        df = document.createDocumentFragment();
        df.appendChild(srefElement);

        return df;
    }

    public static StandardException uninitializedVariableError(String variableName){
        return uninitializedVariableError(variableName, (String)null);
    }

	public static StandardException uninitializedVariableError(String variableName,
			String partName) {
		String error = (partName == null)
		? I18n.loc("BPCOR-6177: attempt to access the value of an uninitialized variable \"{0}\"", variableName)
				: I18n.loc("BPCOR-6178: attempt to access the value of an uninitialized variable part \"{0}.{1}\"", 
						variableName, partName);
		return new StandardException(StandardException.Fault.UninitializedVariable, error);
	}

	public static StandardException uninitializedVariableError(String variableName,
			List<String> partNames) {

		String error = null;
                if (partNames == null || partNames.isEmpty()) {
                    error = I18n.loc("BPCOR-6177: attempt to access the value of an uninitialized variable \"{0}\"", variableName);
                } else {
                    error = I18n.loc("BPCOR-6178: attempt to access the value of an uninitialized variable part \"{0}.{1}\"",
						variableName, partNames.get(0));
                    for (int i = 1; i < partNames.size(); i++) {
                        String partName = partNames.get(i);
                        error = error + "; " + I18n.loc("BPCOR-6178: attempt to access the value of an uninitialized variable part \"{0}.{1}\"",
						variableName, partNames.get(i));
                    }
                }
		return new StandardException(StandardException.Fault.UninitializedVariable, error);
	}
        public static StandardException uninitializedVariableErrorQuery(QName message, String partName) {
        String error = I18n.loc(
                        "BPCOR-6176: attempt to access the value of an uninitialized message part \"{0}.{1}\" while evaluating a propertyAlias",
                        message, partName);
        return new StandardException(StandardException.Fault.UninitializedVariable, error);
    }
	
	public static StandardException selectionFailureQuery(String query) {
        String error = I18n.loc(
                        "BPCOR-6173: Selection Failure occurred while evaluating a propertyAlias {0}",
                        query);
        return new StandardException(StandardException.Fault.SelectionFailure, error);
    }

    public static StandardException selectionFailure(QName processName, int lineNumber) {
        String error = I18n.loc(
                        "BPCOR-6174: Selection Failure occurred in BPEL({0}) at line {1}",
                        processName, lineNumber);
        return new StandardException(StandardException.Fault.SelectionFailure, error);
    }

    public static StandardException selectionFailureQueryNMProperty(String property) {
        String error = I18n.loc(
                        "BPCOR-6180: Selection Failure occurred while evaluating {0} NM property",
                        property);
        return new StandardException(StandardException.Fault.SelectionFailure, error);
    }

    public static StandardException selectionFailureQueryNMProperty(String property, String query) {
        String error = I18n.loc(
                        "BPCOR-6181: Selection Failure occurred while evaluating {0} NM property {1} query",
                        property, query);
        return new StandardException(StandardException.Fault.SelectionFailure, error);
    }

    public static StandardException selectionFailureProperty(String property) {
        String error = I18n.loc(
                        "BPCOR-6182: Selection Failure due to invalid property: {0}", property);
        return new StandardException(StandardException.Fault.SelectionFailure, error);
    }
    
	private static void updateRootElementSchemaType(RVariable variableModel, String partName, Element element) {
		Message wsdlMessageType = variableModel.getWSDLMessageType();
		Object schemaType = null;
		if (wsdlMessageType != null) {
			Part part = wsdlMessageType.getPart(partName);
			if (part != null) {
				if (part.getTypeName() != null) {
				    SchemaType xmlType =
				        WSDL4JExt.getSchemaTypeLoader(part).findType(part.getTypeName());
					schemaType = getSchemaTypeForType(xmlType, variableModel, element);
				} else {
					schemaType = getSchemaTypeForElement(variableModel, element);
				}
			}
		} else if (variableModel.getXSDType() != null) {
			SchemaType xmlType = variableModel.getXSDType();
			if (xmlType != null) {
				schemaType = getSchemaTypeForType(xmlType, variableModel, element);
			}
		} 
		else if (variableModel.getXSDElement() != null) {
			schemaType = getSchemaTypeForElement(variableModel, element);
		}

		element.setUserData("schemaType", schemaType, null);
	}

	private static Object getSchemaTypeForType (SchemaType xmlType, RVariable variableModel, Node element) {
		Object schemaType = null;

		if (xmlType.isBuiltinType()) {
			// handling the built in type
			schemaType = xmlType;
		} else {
			// it is complex type
			schemaType = getGlobalType(variableModel, xmlType.getName());
		}

		return schemaType;
	}

	private static Object getSchemaTypeForElement (RVariable variableModel, Node element) {
		Object schemaType = null;

		javax.xml.namespace.QName qName = new javax.xml.namespace.QName(element.getNamespaceURI(),
				element.getLocalName());
		schemaType = getGlobalElement(variableModel, qName);

		return schemaType;
	}


	public static SchemaType getGlobalType(RVariable variableModel, QName qName) {
		SchemaTypeLoader schemaTypeLoader =
		    ((BPELDocument)variableModel.getOwnerDocument())
		        .getDocumentProcess().getSchemaTypeLoader();
		if (schemaTypeLoader != null) {
			return schemaTypeLoader.findType(qName);
		}
		return null;
	}

	public static SchemaField getGlobalElement(RVariable variableModel, QName qName) {
        SchemaTypeLoader schemaTypeLoader =
            ((BPELDocument)variableModel.getOwnerDocument())
                .getDocumentProcess().getSchemaTypeLoader();
        if (schemaTypeLoader != null) {
            return schemaTypeLoader.findElement(qName);
        }
        return null;
	}


	private static boolean areQNameEqual(QName qName1, QName qName2) {
		return equalStrings(qName1.getNamespaceURI(), qName2.getNamespaceURI())
		&& equalStrings(qName1.getLocalPart(), qName2.getLocalPart());
	}

	private static boolean equalStrings(String s1, String s2) {
		if (s1 == null && s2 != null) {
			return false;
		}
		if (s1 != null && s2 == null) {
			return false;
		}

		if (s1 != null && !s1.trim().equals(s2.trim())) {
			return false;
		}
		return true;
	}

	private static final Pattern TRAILING_ZEROS = Pattern.compile("^\\d+\\.[0]*$");

	private static String removeTrailingZerosIfInt(String src) {
		Matcher m = TRAILING_ZEROS.matcher(src);
		boolean matchFound = m.matches();
		if (matchFound) {
			int position = src.indexOf('.');
			String tmpString = src.substring(0, position);
			return tmpString;
		}
		return src;
	}

	public static String createIllegalPCChangeMessage(ActivityUnit actUnit, RActivity act, ICallFrame frame){

		String cfActUnitClassName = null;
		if (frame.getProgramCounter() != null) {
			cfActUnitClassName = frame.getProgramCounter().getClass().getName();
		}

		String cfActUnitLineLable = null;
		if(frame.getPC() != null) {
			cfActUnitLineLable = frame.getPC().getXPath(); 
		}

		String message = I18n.loc("BPCOR-6107: The program counter in the callframe was changed illegally. " + 
				"Expected: {0} (XPath: {1}). Found: {2} (XPath: {3})", actUnit.getClass().getName(), act.getXPath(), 
				cfActUnitClassName, cfActUnitLineLable);

		return message;
	}

    /**
     *
     * @param msgType
     * @param faultMsg
     * @return
     */
    public static WSMessage createGenericFaultMsg(Message msgDef, String faultMsg) {

        // The fault message can either be a plain string or an XML string. An attempt will be made
        // to parse the faultMsg assuming it is XML. If an exception is thrown then we will treat
        // it as plain text.
        boolean faultIsXML = true;
        if (faultMsg == null) {
            faultMsg = "null";
            faultIsXML = false;
        }

        Document errorDoc = null;

        //No need to parse the fault message if it is not XML
        if (faultIsXML) {
            XmlResourceProviderPool resourcePool = null;
            XmlResourceProvider xmlResourceProvider = null;
            DocumentBuilder docBuilder = null;

            try {
                //Get the DocumentBuilder and build the document
                resourcePool = (XmlResourceProviderPool) BPELSERegistry.getInstance().lookup(XmlResourceProviderPool.class.getName());
                xmlResourceProvider = resourcePool.acquireXmlResourceProvider();

                InputSource is = new InputSource(new StringReader(faultMsg));
                docBuilder = xmlResourceProvider.getDocumentBuilder();

                //The DocumentBuilder from the parser implementation logs error messages. To avoid that
                //we set our own ErrorHandler (instance of class DefaultHandler which does nothing). Once
                //we are done with using the DocumentBuilder, we will set the ErrorHandler as null which
                //resets it to the default.
                docBuilder.setErrorHandler(new DefaultHandler());
                errorDoc = docBuilder.parse(is);
            } catch (Exception e) {
                faultIsXML = false;
            } finally {
                // Release the XML resource.
                docBuilder.setErrorHandler(null);
                resourcePool.releaseXmlResourceProvider(xmlResourceProvider);
            }
        }
        //Create JBI message
        JBIMessageImpl jbiMessage = new JBIMessageImpl(msgDef);

        //Next we need to parse the fault XML and set it on the message. Currently the way it is done is to
        //build a document from the XML using the DOM builder and then copying it to the JBI message.
        Element docElement = jbiMessage.createPart(SystemFault.MESSAGE_PARTNAME);
        if (faultIsXML) {
            //Copy the content.
            updateNode(docElement, errorDoc.getDocumentElement());
        } else {
            Text txt = docElement.getOwnerDocument().createTextNode(faultMsg);
            docElement.appendChild(txt);
        }

        return jbiMessage;
    }


    public static WSMessage createPOJOFaultMsg(Message msgDef, POJOException pojoe, String activityName, int activityLineNumber) {
        //Create JBI message
        JBIMessageImpl jbiMessage = new JBIMessageImpl(msgDef);
        Element partElement = jbiMessage.createPart(SystemFault.MESSAGE_PARTNAME);
        String ns = SystemFault.NAMESPACE;

        String ehSunExtURI = "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/ErrorHandling";
        partElement.setAttributeNS(DOMHelper.XMLNS_URI, DOMHelper.XMLNS_PREFIX + ":sxeh", ehSunExtURI);
        partElement.setAttributeNS(DOMHelper.XMLSI_URI, DOMHelper.XMLSI_PREFIX + ":type", "sxeh:ErrorInfoType");

        Document doc = partElement.getOwnerDocument();
        //construct <pojoexception></pojoexception>
        Element pojoexceptionElement = doc.createElementNS(ns, "pojoException");
        partElement.appendChild(pojoexceptionElement);

        //construct <class></class>
        Element classElement = doc.createElementNS(ns, "class");
        pojoexceptionElement.appendChild(classElement);
        classElement.setTextContent(pojoe.getClassName());

        //construct <operation></operation>
        Element operationElement = doc.createElementNS(ns, "operation");
        pojoexceptionElement.appendChild(operationElement);
        operationElement.setTextContent(pojoe.getOperationName());

        //construct <exception></exception>
        Element exeElement = doc.createElementNS(ns, "exception");
        pojoexceptionElement.appendChild(exeElement);
        exeElement.setTextContent(pojoe.getCause().getClass().getName());

        //construct <activity></activity>
        Element activity = doc.createElementNS(ns, "activity");
        pojoexceptionElement.appendChild(activity);
        activity.setTextContent(activityName);

        //construct <activityLineNumber></activityLineNumber>
        Element lineNumber = doc.createElementNS(ns, "activityLineNumber");
        pojoexceptionElement.appendChild(lineNumber);
        lineNumber.setTextContent(String.valueOf(activityLineNumber));

        //construct <message></message>
        Element msgElement = doc.createElementNS(ns, "message");
        pojoexceptionElement.appendChild(msgElement);
        msgElement.setTextContent(pojoe.getCause().getMessage());

        //construct <cause></cause>
        Element causeElement = doc.createElementNS(ns, "cause");
        pojoexceptionElement.appendChild(causeElement);
        Map<String, Method> attributes = getMetaAttributes(pojoe.getCause().getClass());
        createPOJOFaultCauseNode(pojoe.getCause(), attributes, causeElement, doc);

        //construct <stackTrace></stackTrace>
        Element stElement = doc.createElementNS(ns, "stackTrace");
        pojoexceptionElement.appendChild(stElement);
        ByteArrayOutputStream myStackTrace = new ByteArrayOutputStream(1024);
        pojoe.getCause().printStackTrace(new PrintStream(myStackTrace));
        stElement.setTextContent(myStackTrace.toString());

        return jbiMessage;
    }
    /*
     * Utility to query fields/attributes for a given class if implements Meta
     * interfaces (interface name suffixed with Meta)
     */

    private static Map<String, Method> getMetaAttributes(Class cls) {
        Map<String, Method> retVal = new HashMap<String, Method>();
        Class[] interfaces = cls.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (!interfaces[i].getName().endsWith("Meta")) {
                continue;
            }
            Method[] methods = interfaces[i].getDeclaredMethods();
            for (int j = 0; j < methods.length; j++) {
                String methodName = methods[j].getName();
                retVal.put(methodName, methods[j]);
            }
        }
        return retVal;
    }

    private static void createPOJOFaultCauseNode(Object obj,
            Map<String, Method> attributes, Element causeElement, Document doc) {
        for (Entry<String, Method> entry : attributes.entrySet()) {
            try {
                Object value = entry.getValue().invoke(obj, new Object[]{});
                Node childNode;
                if (value instanceof Node) {
                    childNode = doc.importNode((Node) value, true);
                } else {
                    String ns = obj.getClass().getName().replace("$", "..");
                    childNode = doc.createElementNS(ns, entry.getKey());
                    childNode.setTextContent(value.toString());
                }
                causeElement.appendChild(childNode);
            } catch (Exception e) {
                // It is OK, to ignore exceptions. If there is exeception
                // particular attribute would not be in the map
            }
        }
    }


    /**
     * To mark the message as referenced externally and also to add the local reference. Used
     * by the IMAs
     * 
     * @param message
     * @param variable
     */
    public static void setReferencesForExternalMessage(WSMessage message, RVariable variable) {
        message.setAsExternalReference();
        message.addInternalReference(variable);
    }
    
    
    /**
     * If there are other references to this ws message create a copy 
     * of this ws message before changing its value.
     * Remove the reference of this variable from original message and 
     * add reference to the copy.
     * @param varForUpdate
     */
    public static void createCopyIfRequired(RuntimeVariable varForUpdate) {
        WSMessage message = varForUpdate.getWSMessage();
        RVariable variable = varForUpdate.getVariableDef();
        if (message != null && !message.isOnlyReference(variable)) {
            message.removeInternalReference(variable);

            message = message.copy();
            message.addInternalReference(variable);
            varForUpdate.setWSMessage(message);
        }
    }
    
    
	static class BPELXPathContextFactoryImpl extends JXPathContextFactoryReferenceImpl
			implements BPELXPathContextFactory {
		private TransformEngine mEngine;
		private CustomClassLoaderUtil classLoaderCtx;
		
		public JXPathContext newContext(JXPathContext parentContext, Object contextBean) {
			return new BPELXPathContextImpl(parentContext, contextBean, mEngine, classLoaderCtx);
		}
		
		public void setTransformEngine(TransformEngine transformEngine) {
			mEngine = transformEngine;
		}

		public void setClassLoaderContext(CustomClassLoaderUtil ctx) {
		    classLoaderCtx = ctx;
		}
	}

	static class BPELXPathContextImpl extends JXPathContextReferenceImpl
			implements BPELXPathContext {
	    private String mBaseURI = null;
	    private TransformEngine mEngine = null;
	    private CustomClassLoaderUtil classLoaderCtx;
		
		BPELXPathContextImpl(JXPathContext parentContext, Object contextBean,
							 TransformEngine engine, CustomClassLoaderUtil classLoaderCtx) {
			super(parentContext, contextBean);
			mEngine = (engine == null) ? TransformEngine.XSLT_1_0 : engine;
			this.classLoaderCtx = classLoaderCtx;
		}

		public void setNamespaceResolver(NamespaceResolver resolver) {
			namespaceResolver = resolver;
		}

		public void setBaseURI(String uri) {
			mBaseURI = uri;
		}
		public String getBaseURI() {
			return mBaseURI;
		}

		public TransformEngine getTransformEngine() {
			return mEngine;
		}
	    public CustomClassLoaderUtil getClassLoaderContext(){
	        return classLoaderCtx;
	    }
		
	    public Function getFunction(org.apache.commons.jxpath.ri.QName functionName, Object[] parameters) {
	        String prefix = functionName.getPrefix();
	        String namespace = getNamespaceURI(prefix);
	        String name = functionName.getName();
	        JXPathContext funcCtx = this;
	        Function func = null;
	        Functions funcs;
	        while (funcCtx != null) {
	            funcs = funcCtx.getFunctions();
	            if (funcs != null) {
	                func = funcs.getFunction(namespace, name, parameters);
	                if (func != null) {
	                    return func;
	                }
	            }
	            funcCtx = funcCtx.getParentContext();
	        }
	        throw new JXPathException(
	            "Undefined function: " + functionName.toString());
	    }
	}

	static class BPELNamespaceResolver extends NamespaceResolver {
        BPELNamespaceResolver(Map nsMap) {
            if (nsMap instanceof HashMap) {
                namespaceMap = (HashMap) nsMap;
            } else {
                namespaceMap = new HashMap(nsMap);
            }
        }
    }

	/**
	 * 
	 * @param targetNode
	 * @param fromVal
	 */
	public static void updateNode(Node targetNode, Object fromVal) {

	    if (fromVal instanceof Iterator) {
	        Pointer sourcePtr = (Pointer) ((Iterator) fromVal).next();
	        if (sourcePtr instanceof BeanPointer
	                || sourcePtr instanceof DOMAttributePointer) {
	            fromVal = sourcePtr.getValue();
	        } else {
	            fromVal = sourcePtr.getNode();
	        }
	    }        

	    // remove child elements, it is prerequisite
	    removeChildNodesAndAttributes(targetNode);

	    // add value node as a child node
	    if (fromVal instanceof Text) {
	        Node tmpNode = targetNode.getOwnerDocument().importNode((Node) fromVal, true);
	        targetNode.appendChild(tmpNode);
	    } else if (fromVal instanceof Node) {

	        DOMHelper.copyContent((Node) fromVal, targetNode);
	    } else if (fromVal instanceof Utility.AttachmentWrapper) {
	        // It is an attachment copy, we assume that the targetNode is defined 
		// as of type xsd:base64Binary, and hence all in-lined binary data is 
		// of base64 encoded. 
	        Utility.AttachmentWrapper fVal = (Utility.AttachmentWrapper) fromVal;
	        if (fVal.mBinaryCopy.equals(Copy.BINARY_COPY_ENUM_VALS[0])) {
	            // Copy.BINARY_COPY_ENUM_VALS[0] = "inlined". Inline the attachement.

	            try {
			InputStream input = fVal.mDHdlr.getInputStream();
			if (input != null) {
			    // TODO: should BufferedInputStream be used? 
			    byte[] bytes = new byte[input.available()];
			    input.read(bytes);

			    // reset the input stream.
			    input.reset();
			    
			    String encoded = Base64Utils.byteToBase64String(bytes);
			    Node textNode = targetNode.getOwnerDocument().createTextNode(encoded);
		            targetNode.appendChild(textNode);
			    
			}
		    } catch (IOException e) {
			// TODO Throw bpel exception ?.
			throw new RuntimeException(e);
		    } 
	            
	        } else { // else pass it as attachment after appending the <xop:include> child.
	            Element xopElement = fVal.mXopElement;
	            Node xopCopyNode = targetNode.getOwnerDocument().importNode(xopElement, true);
	            targetNode.appendChild(xopCopyNode);
	        }
	    } else {
	        // value is literal
	        // (either a constant or xpath expression resulted in constant)
	        setNodeValue(targetNode, fromVal);
	    }
	}

	 /**
	  * 
	  * @param targetNode
	  * @param value
	  * @return
	  */
	 public static Object adjustValueForDouble(Node targetNode, Object value) {
		 boolean isTargetNodeFloat = false;
		 if(value instanceof Double){
			 Object schemaType = targetNode.getUserData("schemaType");
			 if (schemaType != null) {
				 SchemaType schType = null;
				 if (schemaType instanceof SchemaType) {
					 schType = (SchemaType)schemaType;
				 } else {
					 schType = ((SchemaField) schemaType).getType();
				 }
				 if ((schType.getBuiltinTypeCode() == SchemaType.BTC_DECIMAL)
						 || (schType.getBuiltinTypeCode() == SchemaType.BTC_DOUBLE)
						 || (schType.getBuiltinTypeCode() == SchemaType.BTC_FLOAT)) {
					 isTargetNodeFloat = true;
				 }
			 }
			 //remove trailing zero
			 if((!isTargetNodeFloat)&& ((((Double)value).doubleValue()- Math.floor((Double)value))==0)){
				 value  = ((Double)value).longValue();
			 } 
		 } 
		 return value;
	 }

	 /**
	  * 
	  * @param element
	  * @param variableScope
	  * @param runtimeVarForUpdate
	  * @param factory
	  * @return
	  */
	 public static JXPathContext createJXPathContext(BPELElement element, VariableScope variableScope, 
			 RuntimeVariable runtimeVarForUpdate, AbstractFactory factory) {

		 //Create a new JXPathContext
		 JXPathContext jxpathContext = Utility.newJXPathContext(element, variableScope, runtimeVarForUpdate);

		 //Set the factory 
		 jxpathContext.setFactory(factory);

		 return jxpathContext;
	 }
     
     public static boolean passesTimeCriterionForThrashing(long lastActivityTime, long memRelTimeCriterion) {
         long inactivityTime = System.currentTimeMillis() - lastActivityTime;
         return inactivityTime > memRelTimeCriterion;
     }
     
     public static WSMessage getWSMessage(RVariable varDef, Reader value) {
        Document doc = DOMHelper.readDocument(value);
        return new JBIMessageImpl(doc, varDef.getWSDLMessageType());
    }
     
     public static boolean containsSingleRootElement(DocumentFragment docFrag) {

         NodeList children = docFrag.getChildNodes();
         Node node;
         boolean foundAnElement = false;
         for (int i = 0; i < children.getLength(); i++) {
             node = children.item(i);
             if (node instanceof Element) {
                 if (foundAnElement) {
                     // there are two Elements inside DocumentFragment
                     return false;
                 } else {
                     // first Element inside DocumentFragment                    
                     foundAnElement = true;
                 }
             }
         }
         if (foundAnElement) {
             return true;
         } 
         throw new RuntimeException("There is no Element defined within the document fragment");
     }
     
	 /*
	  * 
	  */
	 private static void setNodeValue(Node targetNode, Object value) {
		 value = adjustValueForDouble(targetNode, value);
		 Text txt = targetNode.getOwnerDocument().createTextNode(value.toString());
		 targetNode.appendChild(txt);
	 }

	 /*
	  * 
	  */
	 private static void removeChildNodesAndAttributes(Node node) {
		 NodeList children = node.getChildNodes();
		 int count = children.getLength();
		 for (int i = count; --i >= 0;) {
			 node.removeChild(children.item(i));
		 }
		 NamedNodeMap attrMap = node.getAttributes();
		 if (attrMap != null) {
		     count = attrMap.getLength();
		     Node item;
		     for (int i = 0; i < count; i++) {
	             item = attrMap.item(0);
	             if (!DOMHelper.XMLNS_URI.equals(item.getNamespaceURI())) {
    		         // attr can be unqualified. If an attr is unqualified, then 
                     // namespaceURI and localName are null and getName() should be used.
                     if (item.getNamespaceURI() == null && item.getLocalName() == null) {
                         String name = ((org.w3c.dom.Attr) item).getName();
                         attrMap.removeNamedItem(name);
                     } else {
                         attrMap.removeNamedItemNS(item.getNamespaceURI(),
                                 item.getLocalName());
                     }
	             }
		     }
		 }
	 }
	 public static void sortScopesByCompletionOrder(List <Context> scopes){
		 ScopeComparator scopeComparator = new ScopeComparator();
		 Collections.sort(scopes, scopeComparator);
	 } 
	 
	 private static class ScopeComparator implements Comparator <Context> {
		 public int compare(Context o1, Context o2) {
			 return (new Long(((FaultHandlingContext)o1).getCompletionOrder()).compareTo(new Long(((FaultHandlingContext)o2).getCompletionOrder())));
		 }
	 }
	 
	 /**
	  * Create a fault message using the message and concatenates the line number
	  * @param message A brief message indicating the error. For standard fault this should include
	  * the bpel standard fault name.
	  * @param activityUnit cannot be null
	  * @return
	  */
	 public static String appendLineNumberAndActName(String message, ActivityUnit activityUnit) {
		 String faultMessage = message + "\n" 
		 + I18n.loc("BPCOR-6129: Line Number is ") + activityUnit.getStaticModelActivity().getLocator().getLineNumber() + "\n"
		 + I18n.loc("BPCOR-6130: Activity Name is ") + activityUnit.getStaticModelActivity().getName();
		 return faultMessage;
	 }
	 
	 public static boolean isRepeatEveryDefined(EventHandlersOnAlarm onAlarm) {
		 String expr = onAlarm.getRepeatEvery();
		 return Utility.isEmpty(expr) ? false : true;
	 }
     
     public static String getNMPropertyKey(String nmProperty) {
         String retVal = "";
         retVal = nmProperty.substring(0, nmProperty.indexOf("::"));
         return retVal;
     }
     
     public static class AttachmentWrapper {
         String mAttchName;
         Element mXopElement;
         DataHandler mDHdlr;
         String mBinaryCopy;
     }


    /**
     * convertDomToXml converts a given element to Xml String.
     * This method does not use the Transformation from Xalan, Xslt etc but
     * relies on the XmlSerializer to serialize the given node to xml.
     * @param node
     * @return
     */
    public static String convertDomToXml(Document node) {
        if( node == null){
    		LOGGER.warning("The input node cannot be null ");
    		return null;
    	}
        String str = null;
        ByteArrayOutputStream outStream = null;
        try {
        	outStream = new ByteArrayOutputStream();
            OutputFormat outFormat = new OutputFormat( "xml", "UTF-8", true);
            outFormat.setPreserveSpace(false);
            	XMLSerializer serializer = new XMLSerializer();
            	serializer.setOutputFormat(outFormat);
            	serializer.setOutputByteStream(outStream);
            	serializer.serialize(node.getDocumentElement());
            str = outStream.toString("UTF-8");
        } catch (Exception e) {
        	LOGGER.severe(" Exception occured during the Dom conversion to XML string");
        	return null;
        }finally{
        	try{
        		outStream.close();
        	}catch(IOException io){
        		LOGGER.severe(" Failed to close the output stream");
        	}
        }
        return str;
    }
}

