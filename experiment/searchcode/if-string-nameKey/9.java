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
 * @(#)FacadeInformationReader.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

/**
 * 
 */
package com.sun.jbi.cam.plugins.aspects.support.model.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.jbi.cam.plugins.aspects.common.XmlConstants;
import com.sun.jbi.cam.plugins.aspects.support.model.FacadeConfiguration;
import com.sun.jbi.cam.plugins.aspects.support.model.FacadeServicesInformation;

/**
 * @author graj
 *
 */
public class FacadeInformationReader extends DefaultHandler implements
		Serializable {
	private static final long serialVersionUID = 1L;
	
    // Private members needed to parse the XML document
    private boolean parsingInProgress; // keep track of parsing

    private Stack<String> qNameStack = new Stack<String>(); // keep track of
                                                            // QName
    FacadeServicesInformation facadeServicesInformation = null;
    FacadeConfiguration facadeConfiguration = null;
	/**
	 * 
	 */
	public FacadeInformationReader() {
		// TODO Auto-generated constructor stub
	}

    /**
	 * @return the facadeServicesInformation
	 */
	public FacadeServicesInformation getFacadeServicesInformation() {
		return facadeServicesInformation;
	}

	/**
     * Start of document processing.
     * 
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void startDocument() throws SAXException {
        parsingInProgress = true;
        qNameStack.removeAllElements();
    }

    /**
     * End of document processing.
     * 
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void endDocument() throws SAXException {
        parsingInProgress = false;
        // We have encountered the end of the document. Do any processing that
        // is desired,
        // for example dump all collected element2 values.

    }

    /**
     * Process the new element.
     * 
     * @param uri
     *            is the Namespace URI, or the empty string if the element has
     *            no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            is the The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param qName
     *            is the qualified name (with prefix), or the empty string if
     *            qualified names are not available.
     * @param attributes
     *            is the attributes attached to the element. If there are no
     *            attributes, it shall be an empty Attributes object.
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName != null) {
            if (qName.equals(XmlConstants.FACADEINFO_FACADE_KEY)) {
                // ELEMENT1 has an attribute, get it by name
                // Do something with the attribute
                this.facadeServicesInformation = new FacadeServicesInformation();
            } else if (qName.equals(XmlConstants.FACADEINFO_CONFIG_KEY)) {
                // ELEMENT1 has an attribute, get it by name
                // Do something with the attribute
            	this.facadeConfiguration = new FacadeConfiguration();
            } else if (qName.equals(XmlConstants.FACADEINFO_PROPERTY_KEY)) {
                // ELEMENT1 has an attribute, get it by name
                // Do something with the attribute
                if ((attributes != null) && (attributes.getLength() > 0)) {
                	String targetNamespace = null;
                	String serviceName = null;
                	String portName = null;
                	String locationUri = null;
                	
                    String nameKey = attributes
                            .getValue(XmlConstants.FACADEINFO_NAME_KEY);
                    if(nameKey.equals(XmlConstants.FACADEINFO_TARGETNAMESPACE_KEY)) {
                         targetNamespace = attributes
                        .getValue(XmlConstants.FACADEINFO_VALUE_KEY);
                         this.facadeConfiguration.setTargetNamespace(targetNamespace);
                    }
                    if(nameKey.equals(XmlConstants.FACADEINFO_SERVICENAME_KEY)) {
                    	serviceName = attributes
                        .getValue(XmlConstants.FACADEINFO_VALUE_KEY);
                    	this.facadeConfiguration.setFacadeServiceName(serviceName);
                    }
                    if(nameKey.equals(XmlConstants.FACADEINFO_PORTNAME_KEY)) {
                    	portName = attributes
                        .getValue(XmlConstants.FACADEINFO_VALUE_KEY);
                    	this.facadeConfiguration.setFacadePortName(portName);
                    }
                    if(nameKey.equals(XmlConstants.FACADEINFO_SOAPADDRESS_KEY)) {
                    	locationUri = attributes
                        .getValue(XmlConstants.FACADEINFO_VALUE_KEY);
                    	this.facadeConfiguration.setLocationURI(locationUri);
                    }
                }
            }
            // Keep track of QNames
            qNameStack.push(qName);
        }
    }

    /**
     * Process the end element tag.
     * 
     * @param uri
     *            is the Namespace URI, or the empty string if the element has
     *            no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            is the The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param qName
     *            is the qualified name (with prefix), or the empty string if
     *            qualified names are not available.
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // Pop QName, since we are done with it
        qNameStack.pop();
        if (qName != null) {
            if (qName.equals(XmlConstants.FACADEINFO_FACADE_KEY)) {
                // We have encountered the end of ELEMENT1
                // ...
            } else if (qName.equals(XmlConstants.FACADEINFO_CONFIG_KEY)) {
                // We have encountered the end of ELEMENT1
                // ...
            	if(this.facadeConfiguration != null) {
            		this.facadeServicesInformation.addFacadeConfiguration(facadeConfiguration);
            	}
            	this.facadeConfiguration = null;
            } else if (qName.equals(XmlConstants.FACADEINFO_PROPERTY_KEY)) {
                // We have encountered the end of ELEMENT1
                // ...
            }
        }
    }
    
    /**
     * 
     * @param rawXMLData
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static FacadeInformationReader parseFromXMLData(String rawXMLData)            
    throws MalformedURLException, ParserConfigurationException,
    SAXException, URISyntaxException, IOException {
        // System.out.println("Parsing file: "+uriString);
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();

        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();

        // Initialize the XML Document InputStream
        Reader reader = new StringReader(rawXMLData);
        
        // Create an InputSource from the InputStream
        InputSource inputSource = new InputSource(reader);

        // Parse the aspectInput XML document stream, using my event handler
        FacadeInformationReader parser = new FacadeInformationReader();
        saxParser.parse(inputSource, parser);

        return parser;
        
    }
 
    /**
     * 
     * @param fileName
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static FacadeInformationReader parseFromFile(String fileName)
            throws MalformedURLException, ParserConfigurationException,
            SAXException, URISyntaxException, IOException {
        File file = new File(fileName);
        return parseFromFile(file);
    }

    /**
     * 
     * @param fileName
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static FacadeInformationReader parseFromFile(File file)
            throws MalformedURLException, ParserConfigurationException,
            SAXException, URISyntaxException, IOException {

        //System.out.println("Parsing file: "+file.getAbsolutePath());
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();

        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();

        // Initialize the URI and XML Document InputStream
        InputStream inputStream = new FileInputStream(file);

        // Create an InputSource from the InputStream
        InputSource inputSource = new InputSource(inputStream);

        // Parse the aspectInput XML document stream, using my event handler
        FacadeInformationReader parser = new FacadeInformationReader();
        saxParser.parse(inputSource, parser);

        return parser;
    }

    /**
     * 
     * @param uriString
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static FacadeInformationReader parseFromURI(String uriString)
            throws MalformedURLException, ParserConfigurationException,
            SAXException, URISyntaxException, IOException {
        URI uri = new URI(uriString);
        return parseFromURI(uri);
    }
    
    /**
     * 
     * @param uri
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static FacadeInformationReader parseFromURI(URI uri)
            throws MalformedURLException, ParserConfigurationException,
            SAXException, URISyntaxException, IOException {

        //System.out.println("Parsing URI: "+uri);
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();

        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();

        // Initialize the URI and XML Document InputStream
        InputStream inputStream = uri.toURL().openStream();

        // Create an InputSource from the InputStream
        InputSource inputSource = new InputSource(inputStream);

        // Parse the aspectInput XML document stream, using my event handler
        FacadeInformationReader parser = new FacadeInformationReader();
        saxParser.parse(inputSource, parser);

        return parser;
    }
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        String uri = "C:/test/facadeinfo.xml";
        FacadeInformationReader parser = null;
        FacadeServicesInformation info = null;
        try {
            parser = FacadeInformationReader.parseFromFile(uri);
            info = parser.getFacadeServicesInformation();
            if(info != null) {
            	List<FacadeConfiguration> list = info.getFacadeConfigurationList();
            	for(FacadeConfiguration config : list) {
            		System.out.println("targetNamespace is:"+config.getTargetNamespace());
            		System.out.println("facadeServiceName is:"+config.getFacadeServiceName());
            		System.out.println("facadeServiceName is:"+config.getFacadePortName());
            		System.out.println("locationURI is:"+config.getLocationURI());
            	}
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


	}

}

