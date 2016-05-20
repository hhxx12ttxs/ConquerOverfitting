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
 * @(#)HL7ExtSerializer.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.hl7bc.extensions;

import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.xml.DOMUtils;
import com.sun.jbi.hl7bc.HL7Constants;
import com.sun.jbi.hl7bc.I18n;

/**
 * @author Sriram, S. Nageswara Rao
 */
public class HL7ExtSerializer implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    private static final Logger mLogger = Logger.getLogger(HL7ExtSerializer.class.getName());

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

    public static final String ATTR_JOURNALLING_ENABLED = "journallingEnabled";
    public static final String ATTR_HL7_SERVER_PORT = "hl7ServerPort";

    public static final String ATTR_HL7_TRANSPORT_PROTOCOL_TYPE = "transportProtocolType";

    public static final String ATTR_HL7_USE = "use";

    public static final String ATTR_HL7_MESSAGETYPE = "messageType";

    public static final String ATTR_VALIDATE_MSH = "validateMSH";

    public static final String ATTR_FIELD_SEPARATOR = "fieldSeparator";

    public static final String ATTR_ENCODING_CHARACTERS = "encodingCharacters";

    public static final String ATTR_PROCESSING_ID = "processingID";

    public static final String ATTR_VERSION_ID = "versionID";

    public static final String ATTR_ENABLED_SFT = "enabledSFT";

    public static final String ATTR_ENABLED_MLLPV1PERSISTANCE = "persistenceEnabled";
    public static final String ATTR_SOFTWARE_VENDOR_ORGANIZATION = "softwareVendorOrganization";

    public static final String ATTR_SOFTWARE_CERTIFIED_VERSION = "softwareCertifiedVersion";

    public static final String ATTR_SOFTWARE_PRODUCT_NAME = "softwareProductName";

    public static final String ATTR_SOFTWARE_BINARY_ID = "softwareBinaryID";

    public static final String ATTR_SOFTWARE_PRODUCT_INFORMATION = "softwareProductInformation";

    public static final String ATTR_SOFTWARE_INSTALLED_DATE = "softwareInstallDate";

    public static final String ATTR_SENDING_APPLICATION = "sendingApplication";

    public static final String ATTR_SENDING_FACILITY = "sendingFacility";

    public static final String ATTR_ACCEPT_ACK_XSD = "acceptAckXsd";

    public static final String ATTR_APPLICATION_ACK_XSD = "applicationAckXsd";

    public static final String ATTR_MLLPV2_RETRIES_COUNT_ON_NAK = "mllpv2RetriesCountOnNak";

    public static final String ATTR_MLLPV2_RETRY_INTERVAL = "mllpv2RetryInterval";

    public static final String ATTR_MLLPV2_TIME_TO_WAIT_FOR_ACK_NAK = "mllpv2TimeToWaitForAckNak";
    
    private static final String ENV_VAR_REGEX = "\\$\\{([a-zA-Z0-9\\.\\-\\_^\\{\\}]+)\\}";

    private static final Pattern mPattern = Pattern.compile(ENV_VAR_REGEX);

    // environment variable configurations
    protected final Map<String, String[]> mEnvVariableMap = new HashMap<String, String[]>();

    /** Creates a new instance of HL7ExtSerializer */
    public HL7ExtSerializer() {
    }

    public HL7ExtSerializer(Map<String, String[]> envVariableMap) {
        this();
        mEnvVariableMap.putAll(envVariableMap);
    }

    /**
     * Registers the serializers / deserializers
     */
    public void registerSerializer(ExtensionRegistry registry) {
        registry.registerSerializer(Binding.class, HL7Constants.QNAME_BINDING, this);
        registry.registerDeserializer(Binding.class, HL7Constants.QNAME_BINDING, this);
        registry.mapExtensionTypes(Binding.class, HL7Constants.QNAME_BINDING, HL7Binding.class);

        registry.registerSerializer(BindingOperation.class, HL7Constants.QNAME_OPERATION, this);
        registry.registerDeserializer(BindingOperation.class, HL7Constants.QNAME_OPERATION, this);
        registry.mapExtensionTypes(BindingOperation.class, HL7Constants.QNAME_OPERATION, HL7Operation.class);

        registry.registerSerializer(BindingInput.class, HL7Constants.QNAME_MESSAGE, this);
        registry.registerDeserializer(BindingInput.class, HL7Constants.QNAME_MESSAGE, this);
        registry.mapExtensionTypes(BindingInput.class, HL7Constants.QNAME_MESSAGE, HL7Message.class);

        registry.registerSerializer(BindingOutput.class, HL7Constants.QNAME_MESSAGE, this);
        registry.registerDeserializer(BindingOutput.class, HL7Constants.QNAME_MESSAGE, this);
        registry.mapExtensionTypes(BindingOutput.class, HL7Constants.QNAME_MESSAGE, HL7Message.class);

        registry.registerSerializer(Port.class, HL7Constants.QNAME_ADDRESS, this);
        registry.registerDeserializer(Port.class, HL7Constants.QNAME_ADDRESS, this);
        registry.mapExtensionTypes(Port.class, HL7Constants.QNAME_ADDRESS, HL7Address.class);

        registry.registerSerializer(Port.class, HL7Constants.QNAME_PROTOCOLPROPERTIES, this);
        registry.registerDeserializer(Port.class, HL7Constants.QNAME_PROTOCOLPROPERTIES, this);
        registry.mapExtensionTypes(Port.class, HL7Constants.QNAME_PROTOCOLPROPERTIES, HL7ProtocolProperties.class);

        registry.registerSerializer(Port.class, HL7Constants.QNAME_COMMUNICATIONCONTROLS, this);
        registry.registerDeserializer(Port.class, HL7Constants.QNAME_COMMUNICATIONCONTROLS, this);
        registry.mapExtensionTypes(Port.class, HL7Constants.QNAME_COMMUNICATIONCONTROLS, HL7CommunicationControls.class);

        registry.registerSerializer(Port.class, HL7Constants.QNAME_COMMUNICATIONCONTROL, this);
        registry.registerDeserializer(Port.class, HL7Constants.QNAME_COMMUNICATIONCONTROL, this);
        registry.mapExtensionTypes(Port.class, HL7Constants.QNAME_COMMUNICATIONCONTROLS, HL7CommunicationControl.class);

    }

    public Map<String, String[]> getEnvVariableMap() {
        return Collections.unmodifiableMap(mEnvVariableMap);
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

        if (extension instanceof HL7Binding) {
            HL7Binding hl7Binding = (HL7Binding) extension;
            pw.print("      <hl7:binding");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof HL7Operation) {
            HL7Operation hl7Operation = (HL7Operation) extension;
            pw.print("      <hl7:operation");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            if (hl7Operation.getMessageType() != null) {
                DOMUtils.printAttribute(ATTR_HL7_MESSAGETYPE, hl7Operation.getMessageType(), pw);
            }
            pw.println("/>");
        } else if (extension instanceof HL7Message) {
            HL7Message hl7Message = (HL7Message) extension;
            pw.print("      <hl7:message");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            if (hl7Message.getPart() != null) {
                DOMUtils.printAttribute(ATTR_PART, hl7Message.getPart(), pw);
            }
            if (hl7Message.getUseType() != null) {
                DOMUtils.printAttribute(ATTR_HL7_USE, hl7Message.getUseType(), pw);
            }
            if (hl7Message.getEncodingStyle() != null) {
                DOMUtils.printAttribute(ATTR_ENCODING_STYLE, hl7Message.getEncodingStyle(), pw);
            }
            if (extension.getRequired() != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, extension.getRequired().toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof HL7Address) {
            HL7Address hl7Address = (HL7Address) extension;
            pw.print("      <hl7:address");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            String hl7ServerLocationURL = hl7Address.getHL7ServerLocationURL();
            if (hl7ServerLocationURL != null) {
                DOMUtils.printAttribute(HL7Address.ATTR_HL7_SVR_LOCATIONURL, hl7ServerLocationURL, pw);
            }
            String hl7ServerLocation = hl7Address.getHL7ServerLocation();
            if (hl7ServerLocation != null) {
                DOMUtils.printAttribute(HL7Address.ATTR_HL7_SVR_LOCATION, hl7ServerLocation, pw);
            }
            Integer hl7ServerPort = hl7Address.getHL7ServerPort();
            if (hl7ServerPort != null) {
                DOMUtils.printAttribute(HL7Address.ATTR_HL7_SVR_PORT, hl7ServerPort.toString(), pw);
            }
            String transportProtocolName = hl7Address.getTransportProtocolName();
            if (transportProtocolName != null) {
                DOMUtils.printAttribute(HL7Address.ATTR_HL7_TRANS_PROTOCOL_NAME, transportProtocolName, pw);
            }
            pw.println("/>");
        } else if (extension instanceof HL7ProtocolProperties) {
            HL7ProtocolProperties hl7ProtocolProperties = (HL7ProtocolProperties) extension;
            pw.print("      <hl7:protocolproperties");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            if (hl7ProtocolProperties.getAckMode() != null) {
                DOMUtils.printAttribute(ATTR_ACK_MODE, hl7ProtocolProperties.getAckMode(), pw);
            }

            if (hl7ProtocolProperties.getLLPType() != null) {
                DOMUtils.printAttribute(ATTR_LLP_TYPE, hl7ProtocolProperties.getLLPType(), pw);
            }

            if (hl7ProtocolProperties.getEndDataChar() != null) {
                DOMUtils.printAttribute(ATTR_END_DATA_CHARACTER, hl7ProtocolProperties.getEndDataChar().toString(), pw);
            }

            if (hl7ProtocolProperties.getEndBlockChar() != null) {
                DOMUtils.printAttribute(ATTR_END_BLOCK_CHARACTER, hl7ProtocolProperties.getEndBlockChar().toString(),
                        pw);
            }
            if (hl7ProtocolProperties.getStartBlockChar() != null) {
                DOMUtils.printAttribute(ATTR_START_BLOCK_CHARACTER,
                        hl7ProtocolProperties.getStartBlockChar().toString(), pw);
            }
            if (hl7ProtocolProperties.getHLLPChkSumEnabled() != null) {
                DOMUtils.printAttribute(ATTR_HLLP_CHECKSUM_ENABLED,
                        hl7ProtocolProperties.getHLLPChkSumEnabled().toString(), pw);
            }
            if (hl7ProtocolProperties.getSeqNumEnabled() != null) {
                DOMUtils.printAttribute(ATTR_SEQNUM_ENABLED, hl7ProtocolProperties.getSeqNumEnabled().toString(), pw);
            }
            if (hl7ProtocolProperties.getJournallingEnabled() != null) {
                DOMUtils.printAttribute(ATTR_JOURNALLING_ENABLED, hl7ProtocolProperties.getJournallingEnabled().toString(), pw);
            }
            if (hl7ProtocolProperties.getValidateMSHEnabled() != null) {
                DOMUtils.printAttribute(ATTR_VALIDATE_MSH, hl7ProtocolProperties.getValidateMSHEnabled().toString(), pw);
            }
            if (hl7ProtocolProperties.getProcessingID() != null) {
                DOMUtils.printAttribute(ATTR_PROCESSING_ID, hl7ProtocolProperties.getProcessingID(), pw);
            }
            if (hl7ProtocolProperties.getVersionID() != null) {
                DOMUtils.printAttribute(ATTR_VERSION_ID, hl7ProtocolProperties.getVersionID(), pw);
            }
            if (hl7ProtocolProperties.getFieldSeparator() != null) {
                DOMUtils.printAttribute(ATTR_FIELD_SEPARATOR, hl7ProtocolProperties.getFieldSeparator().toString(), pw);

            }
            if (hl7ProtocolProperties.getEncodingCharacters() != null) {
                DOMUtils.printAttribute(ATTR_ENCODING_CHARACTERS, hl7ProtocolProperties.getEncodingCharacters(), pw);

            }

            if (hl7ProtocolProperties.getSendingApplication() != null) {
                DOMUtils.printAttribute(ATTR_SENDING_APPLICATION, hl7ProtocolProperties.getSendingApplication(), pw);
            }
            if (hl7ProtocolProperties.getSendingFacility() != null) {
                DOMUtils.printAttribute(ATTR_SENDING_FACILITY, hl7ProtocolProperties.getSendingFacility(), pw);
            }
				
            if (hl7ProtocolProperties.getMLLPv1PersistanceEnabled() != null) {
                DOMUtils.printAttribute(ATTR_ENABLED_MLLPV1PERSISTANCE, hl7ProtocolProperties.getMLLPv1PersistanceEnabled().toString(), pw);
            }
				
            if (hl7ProtocolProperties.getSFTEnabled() != null) {
                DOMUtils.printAttribute(ATTR_ENABLED_SFT, hl7ProtocolProperties.getSFTEnabled().toString(), pw);
            }
            if (hl7ProtocolProperties.getSoftwareVendorOrganization() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_VENDOR_ORGANIZATION,
                        hl7ProtocolProperties.getSoftwareVendorOrganization(), pw);
            }
            if (hl7ProtocolProperties.getSoftwareCertifiedVersionOrReleaseNumber() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_CERTIFIED_VERSION,
                        hl7ProtocolProperties.getSoftwareCertifiedVersionOrReleaseNumber(), pw);
            }
            if (hl7ProtocolProperties.getSoftwareProductName() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_PRODUCT_NAME, hl7ProtocolProperties.getSoftwareProductName(), pw);
            }
            if (hl7ProtocolProperties.getSoftwareBinaryID() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_BINARY_ID, hl7ProtocolProperties.getSoftwareBinaryID(), pw);
            }
            if (hl7ProtocolProperties.getSoftwareProductInformation() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_PRODUCT_INFORMATION,
                        hl7ProtocolProperties.getSoftwareProductInformation(), pw);
            }
            if (hl7ProtocolProperties.getSoftwareInstallDate() != null) {
                DOMUtils.printAttribute(ATTR_SOFTWARE_INSTALLED_DATE, hl7ProtocolProperties.getSoftwareInstallDate(),
                        pw);
            }
            if (hl7ProtocolProperties.getAcceptAckXsd() != null) {
                DOMUtils.printAttribute(ATTR_ACCEPT_ACK_XSD, hl7ProtocolProperties.getAcceptAckXsd(), pw);
            }
            if (hl7ProtocolProperties.getApplicationAckXsd() != null) {
                DOMUtils.printAttribute(ATTR_APPLICATION_ACK_XSD, hl7ProtocolProperties.getApplicationAckXsd(), pw);
            }
            if (hl7ProtocolProperties.getMLLPV2RetriesCountOnNak() != null) {
                DOMUtils.printAttribute(ATTR_MLLPV2_RETRIES_COUNT_ON_NAK,
                        hl7ProtocolProperties.getMLLPV2RetriesCountOnNak().toString(), pw);
            }
            if (hl7ProtocolProperties.getMllpv2RetryInterval() != null) {
                DOMUtils.printAttribute(ATTR_MLLPV2_RETRY_INTERVAL,
                        hl7ProtocolProperties.getMllpv2RetryInterval().toString(), pw);
            }
            if (hl7ProtocolProperties.getMllpv2TimeToWaitForAckNak() != null) {
                DOMUtils.printAttribute(ATTR_MLLPV2_TIME_TO_WAIT_FOR_ACK_NAK,
                        hl7ProtocolProperties.getMllpv2TimeToWaitForAckNak().toString(), pw);
            }
         

            pw.println("/>");
        } else if (extension instanceof HL7CommunicationControls) {
            HL7CommunicationControls CommunicationControls = (HL7CommunicationControls) extension;
            pw.print("      <hl7:communicationcontrols");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            pw.println("/>");
        } else if (extension instanceof HL7CommunicationControl) {
            HL7CommunicationControl hl7CommCtrl = (HL7CommunicationControl) extension;
            pw.print("      <hl7:communicationcontrol");
            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            if (hl7CommCtrl.getName() != null) {
                DOMUtils.printAttribute(HL7CommunicationControl.ATTR_NAME, hl7CommCtrl.getName(), pw);
            }
            DOMUtils.printAttribute(HL7CommunicationControl.ATTR_VALUE, "" + hl7CommCtrl.getValue(), pw);
            if (hl7CommCtrl.getEnabled() != null) {
                DOMUtils.printAttribute(HL7CommunicationControl.ATTR_ENABLED, hl7CommCtrl.getEnabled().toString(), pw);
            }
            if (hl7CommCtrl.getRecourseAction() != null) {
                DOMUtils.printAttribute(HL7CommunicationControl.ATTR_RECOURSE_ACTION, hl7CommCtrl.getRecourseAction(),
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

        if (HL7Constants.QNAME_BINDING.equals(elementType)) {
            HL7Binding hl7Binding = new HL7Binding();
            returnValue = hl7Binding;
        } else if (HL7Constants.QNAME_OPERATION.equals(elementType)) {
            HL7Operation hl7Operation = new HL7Operation();
            String messageType = DOMUtils.getAttribute(el, ATTR_HL7_MESSAGETYPE);
            if (nonEmptyString(messageType)) {
                hl7Operation.setMessageType(messageType);
            }
            returnValue = hl7Operation;
        } else if (HL7Constants.QNAME_MESSAGE.equals(elementType)) {
            HL7Message hl7Message = new HL7Message();

            String encodingStyle = DOMUtils.getAttribute(el, ATTR_ENCODING_STYLE);
            if (nonEmptyString(encodingStyle)) {
                hl7Message.setEncodingStyle(encodingStyle);
            }
            String part = DOMUtils.getAttribute(el, ATTR_PART);
            if (nonEmptyString(part)) {
                hl7Message.setPart(part);
            }
            String use = DOMUtils.getAttribute(el, ATTR_HL7_USE);
            if (nonEmptyString(use)) {
                hl7Message.setUseType(use);
            }
            returnValue = hl7Message;
        } else if (HL7Constants.QNAME_ADDRESS.equals(elementType)) {
            HL7Address hl7Address = new HL7Address();
            String hl7SvrLocationURL = getAttrAndResolveEnvVar(el, HL7Address.ATTR_HL7_SVR_LOCATIONURL);
            if (nonEmptyString(hl7SvrLocationURL)) {
                hl7Address.setHL7ServerLocationURL(hl7SvrLocationURL);
                URI url = null;
                try {
                    url = new URI(hl7SvrLocationURL);
                } catch (URISyntaxException mue) {
                    throw new WSDLException(WSDLException.CONFIGURATION_ERROR, "Invalid URL Format");
                }
                hl7Address.setHL7ServerLocation(url.getHost());
                hl7Address.setHL7ServerPort(url.getPort());
            }

            String transportProtocolName = DOMUtils.getAttribute(el, HL7Address.ATTR_HL7_TRANS_PROTOCOL_NAME);
            if (nonEmptyString(transportProtocolName)) {
                hl7Address.setTransportProtocolName(transportProtocolName);
            }
            returnValue = hl7Address;
            
            String hl7role = getAttrAndResolveEnvVar(el, HL7Address.ATTR_ROLE);
            hl7Address.setTcpRoleString(hl7role);
            
        } else if (HL7Constants.QNAME_PROTOCOLPROPERTIES.equals(elementType)) {
            HL7ProtocolProperties hl7ProtocolProperties = new HL7ProtocolProperties();
            String acknowledgmentMode = DOMUtils.getAttribute(el, ATTR_ACK_MODE);
            if (nonEmptyString(acknowledgmentMode)) {
                hl7ProtocolProperties.setAckMode(acknowledgmentMode);
            }
            String llpType = DOMUtils.getAttribute(el, ATTR_LLP_TYPE);
            if (nonEmptyString(llpType)) {
                hl7ProtocolProperties.setLLPType(llpType);
            }
            String startBlockCharacter = getAttrAndResolveEnvVar(el, ATTR_START_BLOCK_CHARACTER);
            if (nonEmptyString(startBlockCharacter)) {
                hl7ProtocolProperties.setStartBlockChar(new Byte(startBlockCharacter));
            }

            String endDataCharacter = getAttrAndResolveEnvVar(el, ATTR_END_DATA_CHARACTER);
            if (nonEmptyString(endDataCharacter)) {
                hl7ProtocolProperties.setEndDataChar(new Byte(endDataCharacter));
            }
            String endBlockCharacter = getAttrAndResolveEnvVar(el, ATTR_END_BLOCK_CHARACTER);
            if (nonEmptyString(endBlockCharacter)) {
                hl7ProtocolProperties.setEndBlockChar(new Byte(endBlockCharacter));
            }
            String hllpCheckSumEnabled = DOMUtils.getAttribute(el, ATTR_HLLP_CHECKSUM_ENABLED);
            if (nonEmptyString(hllpCheckSumEnabled)) {
                hl7ProtocolProperties.setHLLPChkSumEnabled(new Boolean(hllpCheckSumEnabled));
            }
            String seqNumEnabled = getAttrAndResolveEnvVar(el, ATTR_SEQNUM_ENABLED);
            if (nonEmptyString(seqNumEnabled)) {
                hl7ProtocolProperties.setSeqNumEnabled(new Boolean(seqNumEnabled));
            }
            String journallingEnabled = getAttrAndResolveEnvVar(el, ATTR_JOURNALLING_ENABLED);
            if (nonEmptyString(journallingEnabled)) {
                hl7ProtocolProperties.setJournallingEnabled(new Boolean(journallingEnabled));
            }
            String validateMSHEnabled = getAttrAndResolveEnvVar(el, ATTR_VALIDATE_MSH);
            if (nonEmptyString(validateMSHEnabled)) {
                hl7ProtocolProperties.setValidateMSHEnabled(new Boolean(validateMSHEnabled));
            }
            String processiongID = getAttrAndResolveEnvVar(el, ATTR_PROCESSING_ID);
            if (nonEmptyString(processiongID)) {
                hl7ProtocolProperties.setProcessingID(processiongID);
            }
            String versionID = DOMUtils.getAttribute(el, ATTR_VERSION_ID);
            if (nonEmptyString(versionID)) {
                hl7ProtocolProperties.setVersionID(versionID);
            }
            String fieldSeparator = getAttrAndResolveEnvVar(el, ATTR_FIELD_SEPARATOR);
            if (nonEmptyString(fieldSeparator)) {
                hl7ProtocolProperties.setFieldSeparator(new Byte(fieldSeparator));
            }
            String encodingCharacters = getAttrAndResolveEnvVar(el, ATTR_ENCODING_CHARACTERS);
            if (nonEmptyString(encodingCharacters)) {
                hl7ProtocolProperties.setEncodingCharacters(encodingCharacters);
            }
            String sendingApplication = getAttrAndResolveEnvVar(el, ATTR_SENDING_APPLICATION);
            if (nonEmptyString(sendingApplication)) {
                hl7ProtocolProperties.setSendingApplication(sendingApplication);
            }
            String sendingFacility = getAttrAndResolveEnvVar(el, ATTR_SENDING_FACILITY);
            if (nonEmptyString(sendingFacility)) {
                hl7ProtocolProperties.SetSendingFacility(sendingFacility);
            }
            String enabledPersistance = getAttrAndResolveEnvVar(el, ATTR_ENABLED_MLLPV1PERSISTANCE);
            if (nonEmptyString(enabledPersistance)) {
                hl7ProtocolProperties.setMLLPv1PersistanceEnabled(new Boolean(enabledPersistance));
            }
            String enabledSFT = getAttrAndResolveEnvVar(el, ATTR_ENABLED_SFT);
            if (nonEmptyString(enabledSFT)) {
                hl7ProtocolProperties.setSFTEnabled(new Boolean(enabledSFT));
            }
            String softVendorOrg = getAttrAndResolveEnvVar(el, ATTR_SOFTWARE_VENDOR_ORGANIZATION);
            if (nonEmptyString(softVendorOrg)) {
                hl7ProtocolProperties.setSoftwareVendorOrganization(softVendorOrg);
            }
            String softCertifiedVersion = getAttrAndResolveEnvVar(el, ATTR_SOFTWARE_CERTIFIED_VERSION);
            if (nonEmptyString(softCertifiedVersion)) {
                hl7ProtocolProperties.setSoftwareCertifiedVersionOrReleaseNumber(softCertifiedVersion);
            }
            String softProductName = getAttrAndResolveEnvVar(el, ATTR_SOFTWARE_PRODUCT_NAME);
            if (nonEmptyString(softProductName)) {
                hl7ProtocolProperties.setSoftwareProductName(softProductName);
            }
            String softBinaryID = getAttrAndResolveEnvVar(el, ATTR_SOFTWARE_BINARY_ID);
            if (nonEmptyString(softBinaryID)) {
                hl7ProtocolProperties.setSoftwareBinaryID(softBinaryID);
            }
            String softProductInfo = getAttrAndResolveEnvVar(el, ATTR_SOFTWARE_PRODUCT_INFORMATION);
            if (nonEmptyString(softProductInfo)) {
                hl7ProtocolProperties.setSoftwareProductInformation(softProductInfo);
            }
            String softInstalledDate = getAttrAndResolveEnvVar(el, ATTR_SOFTWARE_INSTALLED_DATE);
            if (nonEmptyString(softInstalledDate)) {
                hl7ProtocolProperties.setSoftwareInstallDate(softInstalledDate);
            }
            String acceptAckXsd = DOMUtils.getAttribute(el, ATTR_ACCEPT_ACK_XSD);
            if (nonEmptyString(acceptAckXsd)) {
                hl7ProtocolProperties.setAcceptAckXsd(acceptAckXsd);
            }
            String applicationAckXsd = DOMUtils.getAttribute(el, ATTR_APPLICATION_ACK_XSD);
            if (nonEmptyString(applicationAckXsd)) {
                hl7ProtocolProperties.setApplicationAckXsd(applicationAckXsd);
            }

            String mllpv2RetriesCount = getAttrAndResolveEnvVar(el, ATTR_MLLPV2_RETRIES_COUNT_ON_NAK);
            if (nonEmptyString(mllpv2RetriesCount)) {
                hl7ProtocolProperties.setMLLPV2RetriesCountOnNak(Integer.valueOf(mllpv2RetriesCount));
            }

            String mllpv2RetryInterval = getAttrAndResolveEnvVar(el, ATTR_MLLPV2_RETRY_INTERVAL);
            if (nonEmptyString(mllpv2RetryInterval)) {
                hl7ProtocolProperties.setMllpv2RetryInterval(Long.valueOf(mllpv2RetryInterval));
            }

            String mllpv2TimeToWaitForAckNak = getAttrAndResolveEnvVar(el, ATTR_MLLPV2_TIME_TO_WAIT_FOR_ACK_NAK);
            if (nonEmptyString(mllpv2TimeToWaitForAckNak)) {
                hl7ProtocolProperties.setMllpv2TimeToWaitForAckNak(Long.valueOf(mllpv2TimeToWaitForAckNak));
            }
            
            returnValue = hl7ProtocolProperties;
        } else if (HL7Constants.QNAME_COMMUNICATIONCONTROLS.equals(elementType)) {
            HL7CommunicationControls hl7CommunicationControls = new HL7CommunicationControls();
            Map<String, HL7CommunicationControl> commControlColl = getCommControls(el);
            if (commControlColl != null && commControlColl.size() > 0) {
                hl7CommunicationControls.setCommunicationControls(commControlColl);
            }
            returnValue = hl7CommunicationControls;
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

    protected boolean hasMigrationEnvVarRef(String attrVal) throws Exception {
        return mPattern.matcher(attrVal).find();
    }

    protected Object[] getEnvVariableNames(String attrName, String attrVal) throws Exception {
        String tokenName = null;
        Matcher m = mPattern.matcher(attrVal);
        Vector refs = new Vector();
        while (m.find()) {
            tokenName = m.group(1);
            if (tokenName == null || tokenName.trim().length() == 0) {
                throw new Exception(I18n.msg("E0128: {0} is an invalid token name.", tokenName));
            }
            refs.add(tokenName);
        }

        if (attrVal.indexOf("${}") >= 0) {
            throw new Exception(I18n.msg("E0129: {0} is an invalid token name: it does not contain a valid non-empty environment variable name {1}.",
                    attrVal, attrName));
        }

        return refs.toArray();
    }

    protected String getAttrAndResolveEnvVar(Element el, String attrName) throws WSDLException {
        String attrVal = DOMUtils.getAttribute(el, attrName);
        if (attrVal != null) {
            try {
                if (hasMigrationEnvVarRef(attrVal)) {
                    // attribute contains env var reference(s)
                    String token = attrVal;
                    Object[] vars = getEnvVariableNames(attrName, attrVal);
                    if (vars != null) {
                        for (int i = 0; i < vars.length; i++) {
                            String[] varDesc = (String[]) mEnvVariableMap.get(vars[i]);
                            if (varDesc == null || varDesc.length != 2) {
                                throw new WSDLException("E0133 : INVALID_WSDL",
                                        I18n.msg("E0130: Environment variable reference {0} in attribute value {1} (attribute name = [{2}]) has no definition in MBean configuration parameter EnvironmentVariables.", vars[i],
                                                attrVal, attrName));
                            } else {
                                // check if the de-referenced value has ${ in it
                                String varVal = varDesc[0];
                                if (varVal == null) {
                                    throw new WSDLException("E0133 : INVALID_WSDL", I18n.msg(
                                            "E0131: Error :environment variable name = [{0}], has null value. It is referenced from attribute name = [{1}]", vars[i], attrName ));
                                }
                                if (varVal.indexOf("${") >= 0) {
                                    throw new WSDLException("E0133 : INVALID_WSDL", I18n.msg(
                                            "E0132: Error : de-referenced environment variable has further reference in it, attribute name = [{0}], attribute value = [{1}], environment variable name = [{2}], environment variable value = [{3}].", attrName,
                                                    attrVal, vars[i], varVal));
                                }
                                attrVal = attrVal.replace("${" + vars[i] + "}", varVal);
                            }
                        }
                    }
                    /*
                     * if (hasMigrationEnvVarRef(attrVal)) { // still has ref un-resolved throw new
                     * WSDLException( "INVALID_WSDL", mMessages .getString(
                     * "HL7ES_Invalid_attr_value_contains_unresolvable_ref", new Object[] { attrVal,
                     * attrName })); }
                     */
                }
            } catch (WSDLException e) {
                throw e;
            } catch (Exception e) {
                throw new WSDLException("E0133 : INVALID_WSDL", e.getMessage());
            }
        }
        return attrVal;
    }

    protected Map<String, HL7CommunicationControl> getCommControls(Element e1) throws WSDLException {
        // If e1 is not null , get the children property elements
        // and populate HL7CommunicationControl with them
        Map<String, HL7CommunicationControl> commControlColl = new HashMap<String, HL7CommunicationControl>();
        if (e1 != null) {
            NodeList commControlList = e1.getChildNodes();
            if (commControlList != null && commControlList.getLength() > 0) {
                Node node = null;
                Element commControlElem = null;
                String localName = null, name = null, value = null;
                Attr attribute = null;
                for (int i = 0; i < commControlList.getLength(); i++) {
                    node = commControlList.item(i);
                    if (node instanceof Element) {
                        commControlElem = (Element) commControlList.item(i);
                        localName = commControlElem.getLocalName();
                        if (localName.equals(HL7Constants.ELEM_COMMUNICATIONCONTROL)) {
                            HL7CommunicationControl commControl = new HL7CommunicationControl();
                            // Get the communicationcontrol attributes (name,
                            // value, enabled,
                            // recourseAction)
                            List listOfNodes = DOMUtils.getAttributes(commControlElem);
                            if (listOfNodes != null && listOfNodes.size() > 0) {
                                for (Object obj : listOfNodes) {
                                    attribute = (Attr) obj;
                                    name = attribute.getNodeName();
                                    if (name.equals(HL7CommunicationControl.ATTR_NAME)) {
                                        value = attribute.getValue();
                                        if (nonEmptyString(value)) {
                                            commControl.setName(value);
                                        }
                                    } else if (name.equals(HL7CommunicationControl.ATTR_VALUE)) {
                                        value = getAttrAndResolveEnvVar(commControlElem, HL7CommunicationControl.ATTR_VALUE);
                                        if (nonEmptyString(value)) {
                                            long l;
											try {
												l = Long.parseLong(value);
												if(0 > l ) {
												    mLogger.log(Level.WARNING, I18n.msg("W0105 : {0} is not a valid value for {1} attribute - it should be a non negative number.", 
												            l, "<hl7:communicationcontrol's \"" + HL7CommunicationControl.ATTR_VALUE + "\""));
												    String errMsg = I18n.msg("W0105 : {0} is not a valid value for {1} attribute - it should be a non negative number.",
												            l, "<hl7:communicationcontrol's \"" + HL7CommunicationControl.ATTR_VALUE + "\"");
												    throw new WSDLException(WSDLException.INVALID_WSDL, errMsg);

												}
												commControl.setValue(l);
											} catch (NumberFormatException e) {
												commControl.setValueAsString(value);
											}
                                            
                                        }
                                    } else if (name.equals(HL7CommunicationControl.ATTR_ENABLED)) {
                                        value = attribute.getValue();
                                        if (nonEmptyString(value)) {
                                            commControl.setEnabled(new Boolean(value));
                                        }
                                    } else if (name.equals(HL7CommunicationControl.ATTR_RECOURSE_ACTION)) {
                                        value = attribute.getValue();
                                        if (nonEmptyString(value)) {
                                            commControl.setRecourseAction(value);
                                        }
                                    } else {
                                        mLogger.log(Level.WARNING, I18n.msg("W0104: Unexpected attribute {0} for {1}; ignoring it", name,
                                                HL7Constants.ELEM_COMMUNICATIONCONTROL ));
                                    }
                                }
                            } else {
                                mLogger.log(Level.SEVERE, I18n.msg("E0126: {0} is being used but no attributes are defined for it",
                                        HL7Constants.ELEM_COMMUNICATIONCONTROL));

                                String errMsg = I18n.msg("E0126: {0} is being used but no attributes are defined for it",
                                        HL7Constants.ELEM_COMMUNICATIONCONTROL);

                                throw new WSDLException(WSDLException.INVALID_WSDL, errMsg);
                            }
                            // Add commControl to collection
                            commControlColl.put(commControl.getName(), commControl);
                        } else {
                            mLogger.log(Level.SEVERE, I18n.msg("E0127: Unexpected element {0} in the WSDL", node.getLocalName()));
                            String errMsg = I18n.msg("E0127: Unexpected element {0} in the WSDL",
                                    node.getLocalName());
                            throw new WSDLException(WSDLException.INVALID_WSDL, errMsg);
                        }
                    }
                } // for each hl7:communicationcontrol

            }
        }
        return commControlColl;

    }
}

