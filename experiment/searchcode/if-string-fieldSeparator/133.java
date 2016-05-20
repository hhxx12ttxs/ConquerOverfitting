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
 * @(#)SwiftExtSerializer.java
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.swiftbc.extensions;

import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.xml.DOMUtils;
import com.sun.jbi.internationalization.Messages;
import com.sun.jbi.swiftbc.SAGConstants;


/**
 *
 * @author Sriram, S. Nageswara Rao
 */
public class SwiftExtSerializer implements ExtensionSerializer, ExtensionDeserializer, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ATTR_ENCODING_STYLE = "encodingStyle";
    public static final String ATTR_PART = "part";
    public static final String ATTR_ACK_MODE = "acknowledgmentMode";
    public static final String ATTR_LLP_TYPE = "llpType";
    public static final String ATTR_START_BLOCK_CHARACTER = "startBlockCharacter";
    public static final String ATTR_END_DATA_CHARACTER = "endDataCharacter";
    public static final String ATTR_END_BLOCK_CHARACTER = "endBlockCharacter";
    public static final String ATTR_HLLP_CHECKSUM_ENABLED = "hllpChecksumEnabled";
    public static final String ATTR_SEQNUM_ENABLED = "seqNumEnabled";
    public static final String ATTR_SWIFT_SERVER_PORT = "SwiftServerPort";
    public static final String ATTR_SWIFT_TRANSPORT_PROTOCOL_TYPE = "transportProtocolType";
    public static final String ATTR_SWIFT_USE = "use";
    public static final String ATTR_VALIDATE_MSH = "validateMSH";
    public static final String ATTR_FIELD_SEPARACTOR = "fieldSeparator";
    public static final String ATTR_ENCODING_CHARACTERS = "encodingCharacters";
    public static final String ATTR_PROCESSING_ID = "processingID";
    public static final String ATTR_VERSION_ID = "versionID";
    public static final String ATTR_ENABLED_SFT = "enabledSFT";
    public static final String ATTR_SOFTWARE_VENDOR_ORGANIZATION = "softwareVendorOrganization";
    public static final String ATTR_SOFTWARE_CERTIFIED_VERSION = "softwareCertifiedVersion";
    public static final String ATTR_SOFTWARE_PRODUCT_NAME = "softwareProductName";
    public static final String ATTR_SOFTWARE_BINARY_ID = "softwareBinaryID";
    public static final String ATTR_SOFTWARE_PRODUCT_INFORMATION = "softwareProductInformation";
    public static final String ATTR_SOFTWARE_INSTALLED_DATE = "softwareInstalledDate";
    
    
    // environment variable configurations
    protected Map mEnvVariableMap;
    private static final Messages mMessages = Messages.getMessages(SwiftExtSerializer.class);
    
    
    /**
     * Creates a new instance of SwiftExtSerializer
     */
    public SwiftExtSerializer() {
    }
    
    public SwiftExtSerializer(Map envVariableMap) {
        mEnvVariableMap = envVariableMap;
    }
    
    /**
     * Registers the serializers / deserializers
     */
    public void registerSerializer(ExtensionRegistry registry) {
        registry.registerSerializer(Binding.class, SAGConstants.QNAME_BINDING, this);
        registry.registerDeserializer(Binding.class, SAGConstants.QNAME_BINDING, this);
        registry.mapExtensionTypes(Binding.class, SAGConstants.QNAME_BINDING, SwiftBinding.class);
        
        registry.registerSerializer(BindingOperation.class, SAGConstants.QNAME_OPERATION, this);
        registry.registerDeserializer(BindingOperation.class, SAGConstants.QNAME_OPERATION, this);
        registry.mapExtensionTypes(BindingOperation.class, SAGConstants.QNAME_OPERATION, SwiftOperation.class);
        
        registry.registerSerializer(BindingInput.class, SAGConstants.QNAME_MESSAGE, this);
        registry.registerDeserializer(BindingInput.class, SAGConstants.QNAME_MESSAGE, this);
        registry.mapExtensionTypes(BindingInput.class, SAGConstants.QNAME_MESSAGE, SwiftMessage.class);
        
        registry.registerSerializer(BindingOutput.class, SAGConstants.QNAME_MESSAGE, this);
        registry.registerDeserializer(BindingOutput.class, SAGConstants.QNAME_MESSAGE, this);
        registry.mapExtensionTypes(BindingOutput.class, SAGConstants.QNAME_MESSAGE, SwiftMessage.class);
        
        registry.registerSerializer(Port.class, SAGConstants.QNAME_ADDRESS, this);
        registry.registerDeserializer(Port.class, SAGConstants.QNAME_ADDRESS, this);
        registry.mapExtensionTypes(Port.class, SAGConstants.QNAME_ADDRESS, SwiftAddress.class);
        
        registry.registerSerializer(Port.class, SAGConstants.QNAME_PROTOCOLPROPERTIES, this);
        registry.registerDeserializer(Port.class, SAGConstants.QNAME_PROTOCOLPROPERTIES, this);
        registry.mapExtensionTypes(Port.class, SAGConstants.QNAME_PROTOCOLPROPERTIES, SwiftProtocolProperties.class);
    }
    
    
    public void marshall(Class parentType,
            QName elementType,
            ExtensibilityElement extension,
            PrintWriter pw,
            javax.wsdl.Definition def,
            ExtensionRegistry extReg) throws WSDLException {
        
        if (extension == null) {
            return;
        }
        
        if (extension instanceof SwiftBinding) {
            SwiftBinding swiftBinding = (SwiftBinding) extension;
            pw.print("      <swift:binding");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof SwiftOperation) {
            SwiftOperation swiftOperation = (SwiftOperation) extension;
            pw.print("      <swift:operation");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof SwiftMessage) {
            SwiftMessage swiftMessage = (SwiftMessage) extension;
            pw.print("      <swift:message");
            // if (swiftMessage.getPart() != null) {
            //     DOMUtils.printAttribute(ATTR_PART, swiftMessage.getPart(), pw);
            // }
            if (swiftMessage.getUseType() != null) {
                DOMUtils.printAttribute(ATTR_SWIFT_USE, swiftMessage.getUseType(), pw);
            }
            if (swiftMessage.getEncodingStyle() != null) {
                DOMUtils.printAttribute(ATTR_ENCODING_STYLE, swiftMessage.getEncodingStyle(), pw);
            }
            if (extension.getRequired() != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, extension.getRequired().toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof SwiftAddress) {
            SwiftAddress swiftAddress = (SwiftAddress) extension;
            pw.print("      <swift:address");
            String SwiftServerLocationURL = swiftAddress.getSwiftServerLocationURL();
            if (SwiftServerLocationURL != null) {
                DOMUtils.printAttribute(SwiftAddress.ATTR_SWIFT_SVR_LOCATIONURL, SwiftServerLocationURL, pw);
            }
            String SwiftServerLocation = swiftAddress.getSwiftServerLocation();
            if (SwiftServerLocation != null) {
                DOMUtils.printAttribute(SwiftAddress.ATTR_SWIFT_SVR_LOCATION, SwiftServerLocation, pw);
            }
            Integer swiftServerPort = swiftAddress.getSwiftServerPort();
            if (swiftServerPort != null) {
                DOMUtils.printAttribute(SwiftAddress.ATTR_SWIFT_SVR_PORT, swiftServerPort.toString(), pw);
            }
            String transportProtocolName = swiftAddress.getTransportProtocolName();
            if (transportProtocolName != null) {
                DOMUtils.printAttribute(SwiftAddress.ATTR_SWIFT_TRANS_PROTOCOL_NAME, transportProtocolName, pw);
            }
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof SwiftProtocolProperties) {
            SwiftProtocolProperties swiftProtocolProperties = (SwiftProtocolProperties) extension;
            pw.print("      <swift:protocolproperties");
            if (swiftProtocolProperties.getVersionID() != null) {
                DOMUtils.printAttribute(ATTR_VERSION_ID,
                                        swiftProtocolProperties.getVersionID(),
                                        pw);
            }
            if (swiftProtocolProperties.getSoftwareVendorOrganization() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_VENDOR_ORGANIZATION,
                                        swiftProtocolProperties.getSoftwareVendorOrganization(),
                                        pw);
            }
            if (swiftProtocolProperties.getSoftwareCertifiedVersionOrReleaseNumber() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_CERTIFIED_VERSION,
                                        swiftProtocolProperties.getSoftwareCertifiedVersionOrReleaseNumber(),
                                        pw);
            }
            if (swiftProtocolProperties.getSoftwareProductName() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_PRODUCT_NAME,
                                        swiftProtocolProperties.getSoftwareProductName(),
                                        pw);
            }
            if (swiftProtocolProperties.getSoftwareBinaryID() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_BINARY_ID,
                                        swiftProtocolProperties.getSoftwareBinaryID(),
                                        pw);
            }
            if (swiftProtocolProperties.getSoftwareProductInformation() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_PRODUCT_INFORMATION,
                                        swiftProtocolProperties.getSoftwareProductInformation(),
                                        pw);
            }
            if (swiftProtocolProperties.getSoftwareInstallDate() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_INSTALLED_DATE,
                                        swiftProtocolProperties.getSoftwareInstallDate(),
                                        pw);
            }
             
            pw.println("/>");
            
        }
    }
    
    
    public javax.wsdl.extensions.ExtensibilityElement unmarshall(Class parentType,
            QName elementType,
            Element el,
            Definition def,
            ExtensionRegistry extReg) throws WSDLException {
        
        
        ExtensibilityElement returnValue = null;
        
        if (SAGConstants.QNAME_BINDING.equals(elementType)) {
            SwiftBinding swiftBinding = new SwiftBinding();
            returnValue = swiftBinding;
        } else if (SAGConstants.QNAME_OPERATION.equals(elementType)) {
            SwiftOperation swiftOperation = new SwiftOperation();
            returnValue = swiftOperation;
        } else if (SAGConstants.QNAME_MESSAGE.equals(elementType)) {
            SwiftMessage swiftMessage = (new SAGObjectFactoryFactory()).getObjectFactory().getNewMessage();
            
            String encodingStyle = DOMUtils.getAttribute(el, ATTR_ENCODING_STYLE);
            if (nonEmptyString(encodingStyle)) {
                swiftMessage.setEncodingStyle(encodingStyle);
            }
          //  String part = DOMUtils.getAttribute(el, ATTR_PART);
          //  if (nonEmptyString(part)) {
          //      SwiftMessage.setPart(part);
          //  }
            String use = DOMUtils.getAttribute(el, ATTR_SWIFT_USE);
            if (nonEmptyString(use)) {
                swiftMessage.setUseType(use);
            }
            
            returnValue = swiftMessage;
        } else if (SAGConstants.QNAME_ADDRESS.equals(elementType)) {
            SwiftAddress swiftAddress = new SwiftAddress();
            
            String SwiftSvrLocationURL = DOMUtils.getAttribute(el, SwiftAddress.ATTR_SWIFT_SVR_LOCATIONURL);
            if (nonEmptyString(SwiftSvrLocationURL)) {
                try	{
                    if (isAToken(SwiftSvrLocationURL)) {
                        String token = SwiftSvrLocationURL;
                        SwiftSvrLocationURL = (String) mEnvVariableMap.get(getEnvVariableName(SwiftSvrLocationURL));
                        if (SwiftSvrLocationURL == null) {
                            throw new WSDLException("INVALID_WSDL",
                                    mMessages.getString("SwiftES_Invalid_token_no_value", new Object[] {token, SwiftAddress.ATTR_SWIFT_SVR_LOCATIONURL}));
                        }
                    }
                    swiftAddress.setSwiftServerLocationURL(SwiftSvrLocationURL);
                } catch (WSDLException e) {
                    throw e;
                } catch (Exception e) {
                    throw new WSDLException("INVALID_WSDL", e.getMessage());
                }
                
                URI url = null;
                try {
                    url = new URI(SwiftSvrLocationURL);
                } catch (URISyntaxException mue) {
                    throw new WSDLException(WSDLException.CONFIGURATION_ERROR, "Invalid URL Format");
                }
                swiftAddress.setSwiftServerLocation(url.getHost());
                swiftAddress.setSwiftServerPort(url.getPort());
            }
            
            String transportProtocolName = DOMUtils.getAttribute(el, SwiftAddress.ATTR_SWIFT_TRANS_PROTOCOL_NAME);
            if (nonEmptyString(transportProtocolName)) {
                swiftAddress.setTransportProtocolName(transportProtocolName);
            }
            returnValue = swiftAddress;
        } else if (SAGConstants.QNAME_PROTOCOLPROPERTIES.equals(elementType)) {
            SwiftProtocolProperties SwiftProtocolProperties = new SwiftProtocolProperties();
            String acknowledgmentMode = DOMUtils.getAttribute(el, ATTR_ACK_MODE);
            String startBlockCharacter = DOMUtils.getAttribute(el, ATTR_START_BLOCK_CHARACTER);
            String endBlockCharacter = DOMUtils.getAttribute(el, ATTR_END_BLOCK_CHARACTER);
            String seqNumEnabled = DOMUtils.getAttribute(el, ATTR_SEQNUM_ENABLED);
            String processiongID = DOMUtils.getAttribute(el, ATTR_PROCESSING_ID);
            String versionID = DOMUtils.getAttribute(el, ATTR_VERSION_ID);
            if (nonEmptyString(versionID)) {
                SwiftProtocolProperties.setVersionID(versionID);
            }
            String enabledSFT = DOMUtils.getAttribute(el, ATTR_ENABLED_SFT);
            String softVendorOrg = DOMUtils.getAttribute(el, ATTR_SOFTWARE_VENDOR_ORGANIZATION);
            if (nonEmptyString(softVendorOrg)) {
                SwiftProtocolProperties.setSoftwareVendorOrganization(softVendorOrg);
            }
            String softCertifiedVersion = DOMUtils.getAttribute(el, ATTR_SOFTWARE_CERTIFIED_VERSION);
            if (nonEmptyString(softCertifiedVersion)) {
                SwiftProtocolProperties.setSoftwareCertifiedVersionOrReleaseNumber(softCertifiedVersion);
            }
            String softProductName = DOMUtils.getAttribute(el, ATTR_SOFTWARE_PRODUCT_NAME);
            if (nonEmptyString(softProductName)) {
                SwiftProtocolProperties.setSoftwareProductName(softProductName);
            }
            String softBinaryID = DOMUtils.getAttribute(el, ATTR_SOFTWARE_BINARY_ID);
            if (nonEmptyString(softBinaryID)) {
                SwiftProtocolProperties.setSoftwareBinaryID(softBinaryID);
            }
            String softProductInfo = DOMUtils.getAttribute(el, ATTR_SOFTWARE_PRODUCT_INFORMATION);
            if (nonEmptyString(softProductInfo)) {
                SwiftProtocolProperties.setSoftwareProductInformation(softProductInfo);
            }
            String softInstalledDate = DOMUtils.getAttribute(el, ATTR_SOFTWARE_INSTALLED_DATE);
            if (nonEmptyString(softInstalledDate)) {
                SwiftProtocolProperties.setSoftwareInstallDate(softInstalledDate);
            }
            returnValue = SwiftProtocolProperties;
        }
        
        return returnValue;
    }
    
    protected boolean nonEmptyString(String strToTest) {
        boolean nonEmpty = false;
        if (strToTest != null && strToTest.length() > 0) {
            nonEmpty = true;
        }
        return nonEmpty;
    }
    
    public Map getEnvVariableMap() {
        return mEnvVariableMap;
    }
    
    protected boolean isAToken(String name) throws Exception {
        boolean isToken = false;
        
        if (name.startsWith("${")) {
            if (name.endsWith("}")) {
                isToken = true;
            } else {
                throw new Exception(mMessages.getString("SwiftES_Invalid_token_name", name));
            }
        }
        
        return isToken;
    }
    
    protected String getEnvVariableName(String aToken) throws Exception {
        String tokenName = null;
        
        if (aToken == null || "".equals(aToken)) {
            throw new Exception(mMessages.getString("SwiftES_Invalid_token_name", aToken));
        }
        
        tokenName = aToken.substring(2, aToken.length() - 1);
        if ("".equals(tokenName)) {
            throw new Exception(mMessages.getString("SwiftES_Invalid_empty_token_name", aToken));
        }
        
        return tokenName;
        
    }
    
}

