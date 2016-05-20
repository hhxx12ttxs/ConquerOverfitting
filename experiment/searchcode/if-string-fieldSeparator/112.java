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
 * @(#)InboundMessageProcessor.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.hl7bc;

import com.sun.jbi.hl7bc.util.HL7MMUtil;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.concurrent.Callable;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import com.sun.jbi.common.qos.messaging.BaseExchangeTemplates;
import com.sun.jbi.common.qos.messaging.MessagingChannel;
import com.sun.jbi.common.qos.messaging.SendFailureListener;
import com.sun.jbi.hl7bc.extensions.HL7CommunicationControl;
import com.sun.jbi.hl7bc.extensions.HL7Input;
import com.sun.jbi.hl7bc.extensions.HL7Address;
import com.sun.jbi.hl7bc.extensions.HL7Operation;
import com.sun.jbi.hl7bc.extensions.HL7Output;
import com.sun.jbi.hl7bc.extensions.HL7Message;
import com.sun.jbi.hl7bc.extservice.ack.ACKBuilder;
import com.sun.jbi.hl7bc.extservice.ack.HL7v25ACKBuilder;
import com.sun.jbi.hl7bc.extservice.ack.HL7v251ACKBuilder;
import com.sun.jbi.hl7bc.extservice.ack.HL7v26ACKBuilder;
import com.sun.jbi.hl7bc.extservice.ack.MSHInfo;
import com.sun.jbi.hl7bc.extservice.ack.ACKErrorCodes;
import com.sun.jbi.hl7bc.extservice.server.HL7Callback;
import com.sun.jbi.hl7bc.extservice.persist.connection.DBConnection;
import com.sun.jbi.hl7bc.extservice.persist.connection.DBConnectionFactory;
import com.sun.jbi.hl7bc.extservice.persist.dbo.SequenceNumDBO;
import com.sun.jbi.hl7bc.extservice.persist.dbo.JournalHL7MessageLogDBO;
import com.sun.jbi.hl7bc.extservice.persist.dbo.DBObjectFactory;
import com.sun.jbi.hl7bc.extservice.HL7InboundMessageValidationProcessor;
import com.sun.jbi.hl7bc.extservice.ProtocolInfo;
import com.sun.jbi.hl7bc.extservice.ValidationInfo;
import com.sun.jbi.hl7bc.extservice.support.hl7v3.HL7V3MessageValidator;
import com.sun.jbi.hl7bc.extservice.support.hl7v3.HL7V3MessageEleParse;
import com.sun.jbi.hl7bc.extservice.support.hl7v3.HL7V3TransmissionWrapperContext;
import com.sun.jbi.hl7bc.configuration.RuntimeConfiguration;
import com.sun.jbi.hl7bc.util.AlertsUtil;
import com.sun.jbi.hl7bc.util.LogSupport;
import com.sun.jbi.hl7bc.util.NMPropertiesUtil;
import com.sun.jbi.hl7bc.util.UniqueMsgIdGenerator;
import com.sun.jbi.hl7bc.I18n;
import com.sun.encoder.Encoder;
import com.sun.jbi.alerter.NotificationEvent;

import com.sun.jbi.hl7bc.extservice.ack.ACKBuilderFactory;
import com.sun.jbi.hl7bc.extservice.client.HL7Connector;
import com.sun.jbi.hl7bc.extservice.communicationcontrols.HL7CommunicationControlsInfo;
import com.sun.jbi.hl7bc.extservice.persist.MLLPV1PersistenceHandler;

import static com.sun.jbi.hl7bc.util.XmlUtil.*;

import net.java.hulp.measure.Probe;

/**
 * Processes inbound messages
 */
public class InboundMessageProcessor implements Callable<String>, MessageExchangeReplyListener, SendFailureListener,
        HL7Constants {

    private static final Logger mLog = Logger.getLogger(InboundMessageProcessor.class.getName());

    private Endpoint mEndpoint;

    private ComponentContext mContext;

    private MessagingChannel mChannel;

    private RuntimeConfiguration mRuntimeConfig;

    private UniqueMsgIdGenerator mMsgGenerator;

    private HL7Callback mHL7Callback;

    private String mHL7Version;

    private String mHL7Msg;

	private String mMessageControlID; /* MSH.10 value */
    private MessageExchangeFactory mMsgExchangeFactory;

    private HL7Normalizer mHL7Normalizer;

    private HL7Denormalizer mHL7Denormalizer;

    private QName mOperationName;

    private String mMsgExchangePattern;

    private HL7Operation mHL7Operation;

    private String mAckMode;

    private boolean mSeqNoEnabled;
    
    private boolean mJournallingEnabled;
    
    private boolean mPersistenceEnabled;

    private boolean mSFTEnabled;

    private boolean mUACEnabled;
    
    private int mExpSeqno;

    private Encoder mEncoder;

    private DBConnectionFactory mDBConnectionFactory;

    private DBObjectFactory mDBObjectFactory;

    private Transformer mTrans;

    private HL7InboundMessageValidationProcessor mHL7InbMsgValidationprocessor;

    private HL7CommunicationControlsInfo mHL7CommunicationControlsInfo;
    
    private MLLPV1PersistenceHandler mPersistenceHandler;
    
    private Transaction mTx = null;

    // used by receive channels for saving message exchanges for inbound request/reply exchanges
    private Map<String, InboundReplyContext> mInboundExchanges;

    private Map<String, HL7Callback> mSessionMap;

    private static final String PROVISIONING_ID = "Provider";

    private static final String CONSUMING_ID = "Consumer";

    public static final String REDELIVERY_QOS_MSG_ID = "REDELIVERY_QOS_MSG_ID";

    // <checkpoint>
    private boolean monitorEnabled = false;
    private boolean ndcEnabled = false;
    private String chkptMsgId = null;

    // </checkpoint>


    public InboundMessageProcessor() {
    }

    protected void initialize() throws Exception {
        mHL7Normalizer = new HL7Normalizer();
        mHL7Denormalizer = new HL7Denormalizer();
        mHL7Operation = (HL7Operation) mEndpoint.getHL7Operations().get(mOperationName);
        mMsgExchangePattern = (String) mEndpoint.getOperationMsgExchangePattern().get(mOperationName);
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            mTrans = factory.newTransformer();
        } catch (TransformerFactoryConfigurationError ex) {
            throw new Exception(I18n.msg(
                    "E0188: Exception when creating transformer in InboundMessageProcessor, e=[{0}]", ex.getMessage()),
                    ex);
        }
        mAckMode = mEndpoint.getHL7ProtocolProperties().getAckMode();
        mHL7Version = mEndpoint.getHL7ProtocolProperties().getVersionID();
        if (mLog.isLoggable(Level.FINE)) {
            mLog.log(Level.FINE, I18n.msg("I0121: Acknowledgement Mode is {0}", mAckMode));
        }
        mSFTEnabled = mEndpoint.getHL7ProtocolProperties().getSFTEnabled();
		mUACEnabled = mEndpoint.getHL7ProtocolProperties().getUACEnabled();
        mSeqNoEnabled = mEndpoint.getHL7ProtocolProperties().getSeqNumEnabled();
        mJournallingEnabled = mEndpoint.getHL7ProtocolProperties().getJournallingEnabled();
        mPersistenceEnabled = mEndpoint.getHL7ProtocolProperties().getMLLPv1PersistanceEnabled();

        if (mSeqNoEnabled || mJournallingEnabled || mPersistenceEnabled) {
            // get the Initial Context
            InitialContext ic = mContext.getNamingContext();
            // get the properties
            Properties props = mRuntimeConfig.getProperties();
            mDBConnectionFactory = new DBConnectionFactory(props, ic, mContext.getInstallRoot());
            mDBObjectFactory = DBObjectFactory.getDBObjectFactory(mDBConnectionFactory.getType());
        }
        if(mPersistenceEnabled){
            mPersistenceHandler = new MLLPV1PersistenceHandler(mDBConnectionFactory, mDBObjectFactory);
            mPersistenceHandler.setApplicationId(mEndpoint.getEndpointName());
        }
        mHL7CommunicationControlsInfo = new HL7CommunicationControlsInfo(mEndpoint);
        mMsgExchangeFactory = mChannel.createExchangeFactory();
        mChannel.addSendFailureListener(this);

        // <checkpoint>
        // force a config refresh for each message
        HL7MMUtil.getCustomConfiguration(true);
        monitorEnabled = HL7MMUtil.isMonitorEnabled(mEndpoint);
        ndcEnabled = HL7MMUtil.isNdcEnabled(mEndpoint);
        // </checkpoint>

    }

    protected void setComponentContext(ComponentContext compContext) {
        mContext = compContext;
    }

    protected void setMessagingChannel(MessagingChannel messageChannel) {
        mChannel = messageChannel;
    }

    protected void setRuntimeConfiguration(RuntimeConfiguration runtimeConfig) {
        mRuntimeConfig = runtimeConfig;
    }

    protected void setEndpoint(Endpoint endpoint) {
        mEndpoint = endpoint;
    }

    protected void setOperationName(QName operName) {
        mOperationName = operName;
    }

    protected void setMessage(String hl7Msg) {
        mHL7Msg = hl7Msg;
    }

    protected void setCallback(HL7Callback hl7Callback) {
        mHL7Callback = hl7Callback;
    }

    protected void setHL7Encoder(Encoder encoder) {
        this.mEncoder = encoder;
    }

    protected void setSessionMap(Map<String, HL7Callback> sessionMap) {
        this.mSessionMap = sessionMap;
    }

    protected void setInboundMsgExchanges(Map<String, InboundReplyContext> inboundMsgExchanges) {
        mInboundExchanges = inboundMsgExchanges;
    }

    protected void setUniqueMsgIdGenerator(UniqueMsgIdGenerator uidGenerator) {
        mMsgGenerator = uidGenerator;
    }

    public String call() throws Exception {
        try {
            if (messageIsAck()) {
                processInboundAck();
            } else if (mHL7Version.startsWith(V3)) {
                // v3 request processing
                processHL7V3InboundRequest();
            } else {
                processInboundRequest();
            }
            return SUCCESS;
        } finally {
            // When we're done, make sure we're no longer registered for
            // failure notifications
            mChannel.removeSendFailureListener(this);
        }
    }

    /**
     * Scans the message to determine the existence of MSA segment
     * 
     * @return true/false
     * @throws Exception 
     */
   /* private boolean hasMSASegment() {
        int pos = mHL7Msg.indexOf(MSA);
        if (pos != -1)
            return true;
        return false;
    }*/
    
    /**
     * Scans the message to determine if it is an HL7 ACK
     * 
     * @return true/false
     */
    private boolean messageIsAck() throws Exception {
        if(mHL7Msg.startsWith(MSH)){
            
        	char fieldSeparator = mHL7Msg.charAt(3);
            int endOfFirstSegment = mHL7Msg.indexOf('\r');
            
            String msgToTest = mHL7Msg.substring(0, endOfFirstSegment);
            int nextPos = 3;
            for (int i = 0; i < 7 && nextPos != -1 && msgToTest.length() > (nextPos + 1); i++) {
            	nextPos = msgToTest.indexOf(fieldSeparator, nextPos + 1); 
            }
            
            if (nextPos == -1) {
            	return false;
            }
            
            if (msgToTest.length() <= (nextPos + 1)) {
            	return false;
            }
            
            String msgType = msgToTest.substring(nextPos + 1);
            return msgType.startsWith("ACK");
            
        // TODO, need to change the below condition. This is required to handle hl7 xml messages
        }else if(mHL7Msg.startsWith("<")){ // to xml message
            Document document = createDocumentFromXML(true, new String(mHL7Msg));
            Element element = document.getDocumentElement();
            DOMSource src = new DOMSource((Node) element);
            NodeList segments = src.getNode().getChildNodes();
            for (int i = 0; i < segments.getLength(); i++) {
                Node segment = segments.item(i);
                String name = segment.getLocalName();
                if (name != null && name.contains(MSH)) {

                	NodeList fields = segment.getChildNodes();
                	for (int j = 0; j < fields.getLength(); j++) {
                		
                		Node field = fields.item(j);
                		String fieldName = field.getLocalName();
                		if (fieldName != null && fieldName.equals("MSH.9")) {
                			for (int k = 0; k < field.getChildNodes().getLength(); k++) {
                				String content = field.getChildNodes().item(k).getTextContent();
                				if (content.equals("ACK")) {
                					return true;
                				}
                			}
                		}
                		
                	}
                	
                }
            }
            
        }
        
        return false;
    }

    /**
     * Process Inbound Acknowledgments Locate the target extenral system for this message and then
     * route it
     * 
     * @param source HL7 Acknowledgment
     * @throws Exception
     */
    private void processInboundAck() throws Exception {
        String use = mHL7Operation.getHL7OperationInput().getHL7Message().getUseType();
        Source src;
		// here if ack message received in case of original mode
		// should not process the message. and send the nack back to the sender.
		if(mAckMode.equals(ACK_MODE_ORIGINAL)){
            String nakMsg = generateCannedNakMessage("MessageType MSH.9 is ACK, hence rejecting the message processing");
            mHL7Callback.onReply(nakMsg, false);
			return;
		}
        if (use.equals(HL7Message.ATTR_USE_TYPE_ENCODED)) {
            src = mEncoder.decodeFromString(mHL7Msg);
        } else {
            Document document = createDocumentFromXML(true, new String(mHL7Msg));
            Element element = document.getDocumentElement();
            src = new DOMSource((Node) element);
        }
        DOMResult result = transformToDOMResult(mTrans, src);
        Node mshNode = ((Document) result.getNode()).getFirstChild().getFirstChild();
        MSHInfo mshInfo = new MSHInfo();
        mshInfo.setMSHSegment(mshNode);
        mshInfo.unmarshal();
        String key = buildKeyForACK(mshInfo);
        HL7Callback hl7Callback = mSessionMap.get(key);
        sendACKToExtSys(hl7Callback);
        // remove from map once acknowledgment is sent
        mSessionMap.remove(key);
    }

    /**
     * Process Inbound Requests
     * 
     * @throws Exception
     */
    private void processInboundRequest() throws Exception {
        String msgID = null;
        String key = null;
        try {
            // <checkpoint>
            if ( ndcEnabled ) {
                // "Push" NDC context
                Logger.getLogger("com.sun.EnterContext")
                      .log(Level.FINE, "{0}={1}", new Object[] {HL7MMUtil.SOLUTION_GROUP,
                      HL7MMUtil.getSolutionGroup(mEndpoint)});
            }
            if (mLog.isLoggable(Level.FINE)) {
                mLog.log(Level.FINE, I18n.msg("I0122: HL7 message received to process inbound request"));
            }
            // </checkpoint>
            long hl7MsgReceiveTime = System.currentTimeMillis();
            // enforce the throttling here, seems a per endpoint max concurrency limit
            // get one throttle permit
            // if there is no token available,
            // we won't poll the external and pump messages
            // into the system
            try {
                // Get the maxConcurrencyLimit for this endpoint
                int pendingMsgs = mInboundExchanges.size();
                int maxCC = mEndpoint.getMaxConcurrencyLimit();
                if (maxCC > 0) {
                    if (pendingMsgs >= maxCC) {
                        if (mLog.isLoggable(Level.FINE)) {
                            mLog.log(Level.FINE, I18n.msg(
                                    "I0123: The number of messages {0} exceed the throttle limit {1}",
                                    Integer.toString(pendingMsgs), Integer.toString(maxCC)));
                        }
                    } else {
                        if (mLog.isLoggable(Level.FINE)) {
                            mLog.log(Level.FINE, I18n.msg(
                                    "I0124: The number of messages {0} are within the throttle limit {1}",
                                    Integer.toString(pendingMsgs), Integer.toString(maxCC)));
                        }
                    }
                    mEndpoint.acquireThrottle();
                } else {
                    if (mLog.isLoggable(Level.FINE)) {
                        mLog.log(Level.FINE, I18n.msg("I0125: Throttling configuration is not defined on the endpoint"));
                    }
                }
            } catch (Exception ie) {
                // warning but allow message routing proceed
                mLog.log(Level.WARNING, I18n.msg("W0117: acquireThrottle() interrupted, endpoint = {0}",
                        mEndpoint.getEndpointName()));
            }

            HL7Input hl7Input = mEndpoint.getHL7OperationInput(mHL7Operation);
            HL7Message hl7Message = hl7Input.getHL7Message();
            HL7CommunicationControl commCntrl = mHL7CommunicationControlsInfo.getMaxCannedNakSentCommControl();

            Source inMsg = null;
            String endPointID = (mEndpoint.getEndpointType() == Endpoint.EndpointType.INBOUND) ? (createConsumingEndpointIdentifier(
                    mEndpoint.getServiceName(), mEndpoint.getEndpointName()))
                    : (createProvisioningEndpointIdentifier(mEndpoint.getServiceName(), mEndpoint.getEndpointName()));
            Probe normalizationMeasurement = Probe.info(getClass(), endPointID,
                    HL7BindingComponent.PERF_CAT_NORMALIZATION);
            try {
				HL7Address adrs = mEndpoint.getHL7Address();
                mLog.log(Level.INFO, I18n.msg(
                         "I9124: Received HL7 message from the client address {0}  : on to the server port {1}. ",
                              mHL7Callback.getClientInfo(), Integer.toString(adrs.getHL7ServerPort())));
				mLog.log(Level.INFO, I18n.msg(
                         "Msg: {0} ",
                              mHL7Msg.toString()));
                inMsg = mHL7Normalizer.normalize(mOperationName, mEndpoint, hl7Message, mHL7Msg);
            } catch (Exception exe) {
                // Freeze further processing since message validation against xml schema failed
                String nakMsg = generateCannedNakMessage(exe.getLocalizedMessage());
                mHL7Callback.onReply(nakMsg, false);
                if (commCntrl != null && commCntrl.getEnabled().booleanValue()) {
                    hanldeMaxCannedNakSent();
                }
                return;
            } finally {
                if (normalizationMeasurement != null) {
                    normalizationMeasurement.end();
                }
            }

            // get the MSH node
            Node mshNode = ((DOMSource) inMsg).getNode().getFirstChild().getFirstChild().getFirstChild().getFirstChild();
            MSHInfo mshInfo = new MSHInfo();
            mshInfo.setMSHSegment(mshNode);
            mshInfo.unmarshal();
            mMessageControlID = mshInfo.getMsgControlID();
            mHL7InbMsgValidationprocessor = new HL7InboundMessageValidationProcessor();

            // <checkpoint>
            if ( monitorEnabled ) {
                String mode = HL7MMUtil.getMessageTrackingIDModeInbound();
                if ( HL7MMUtil.MESSAGE_TRACKING_MODE_MSG.equals(mode) ) {
                   chkptMsgId =  mMessageControlID;
                } else {
                    chkptMsgId = HL7MMUtil.getUniqueID();
                }
                // Converting Message node to xml string for checkpointing
                String hl7XMLMessage = transformToString(((DOMSource) inMsg).getNode().getFirstChild().getFirstChild().getFirstChild(),
                    "UTF-8", true, "no", "xml");
                HL7MMUtil.setCheckpoint(mEndpoint, chkptMsgId, "Processing-Inbound-Request", hl7XMLMessage);
                //HL7MMUtil.setCheckpoint(mEndpoint, chkptMsgId, "Processing-Inbound-Request", this.mHL7Msg);
            }
            // </checkpoint>

            if (mSeqNoEnabled) {
                // retrieve the value from DB
                mExpSeqno = retrieveSequenceNumber(mEndpoint.getServiceUnitPath());
                // retrieve the MSH-13 field value from MSH segment of the request
                int recvSeqno = mshInfo.getSequenceNumber();
                int status = processSequenceNumbering(mshNode, recvSeqno);
                // <checkpoint>
                if ( monitorEnabled ) {
                    HL7MMUtil.setCheckpoint(chkptMsgId, "Sequence-Number-Processed", mEndpoint);
                }
                // </checkpoint>
                if (status == -1) {
                    return;
                }
            }
            boolean validateMSH = mEndpoint.getHL7ProtocolProperties().getValidateMSHEnabled().booleanValue();
            // validate only when user requested
            if (validateMSH) {
                ValidationInfo validationInfo = mHL7InbMsgValidationprocessor.messageValidationProcess(mEndpoint, inMsg);
                // when validation fails construct a NAK and send to the HL7 Message Initiator
                if (!validationInfo.getValidationStatus()) {
                    String acceptAckCond = InboundReplyContext.getAcceptAckCondtion(inMsg);
                    String ackCode = (mAckMode.equals(ACK_MODE_ORIGINAL)) ? ACKBuilder.APP_REJECT
                            : ACKBuilder.COMMIT_REJECT;
                    // Enhanced Mode -- COMMIT REJECT, Original Mode -- APPLICATION REJECT
                    if (mAckMode.equals(ACK_MODE_ORIGINAL)
                            || (mAckMode.equals(ACK_MODE_ENHANCED) && null != acceptAckCond && (acceptAckCond.equals(ALWAYS_CONDITION) || acceptAckCond.equals(ERROR_CONDITION)))) {
                        String nakString = makeNAK(mshNode, ackCode, validationInfo.getErrorCode(),
                                validationInfo.getErrorMessage());
                        mHL7Callback.onReply(nakString, false);
                        commCntrl = mHL7CommunicationControlsInfo.getMaxNakSentCommControl();
                        if (commCntrl != null && commCntrl.getEnabled().booleanValue()) {
                            hanldeMaxNakSent();
                        }
                        // Freeze further processing since message validation fails
                    }
                    return;
                }

				// <checkpoint>
				if ( monitorEnabled ) {
					HL7MMUtil.setCheckpoint(chkptMsgId, "Message-Validated", mEndpoint);
				}
				// </checkpoint>
            }

            InboundReplyContext inReplyContext = null;
            if (mAckMode.equals(ACK_MODE_ORIGINAL)) {
                // In case of original mode acknowledgement, sending application acknowledgement is
                // defaulted
                inReplyContext = new InboundReplyContext(hl7MsgReceiveTime, inMsg, this, mHL7Callback, ALWAYS_CONDITION);
            } else {
                inReplyContext = new InboundReplyContext(hl7MsgReceiveTime, inMsg, this, mHL7Callback);
                if (inReplyContext.getAppAckCondition() != null
                        && (inReplyContext.getAppAckCondition().equals(ALWAYS_CONDITION) || inReplyContext.getAppAckCondition().equals(
                                SUCCESS_CONDITION))) {
                    key = buildKey(mshInfo);
                    mSessionMap.put(key, mHL7Callback);
                }
            }
            msgID = sendMessageToNMR(inMsg, inReplyContext);
            // <checkpoint>
            if ( monitorEnabled ) {
                HL7MMUtil.setCheckpoint(chkptMsgId, "Message-Routed-To-NMR", mEndpoint );
            }
            // </checkpoint>


        } catch (Exception ex) {
            mInboundExchanges.remove(msgID);
            if (mSessionMap.containsKey(key)) {
                mSessionMap.remove(key);
            }
            if(mPersistenceEnabled && mTx != null){
               try{
                     mTx.rollback();
                  } catch (Exception ex1) {
                      mLog.log(Level.SEVERE, I18n.msg("Failed to roll back the transaction {0}",
                              ex1.getLocalizedMessage()));                      
                  }
            }
            mLog.log(Level.SEVERE, I18n.msg("E0189: Failed to process an inbound HL7 Message request: {0}",
                    ex.getLocalizedMessage()), ex);
            throw ex;
        } finally {
            // <checkpoint>
            if ( ndcEnabled ) {
                // "Pop" NDC context
                Logger.getLogger("com.sun.ExitContext")
                      .log(Level.FINE, "{0}={1}", new Object[] {HL7MMUtil.SOLUTION_GROUP,
                      HL7MMUtil.getSolutionGroup(mEndpoint)});
            }
            // </checkpoint>
			 mEndpoint.releaseThrottle();

        }
    }

    private void hanldeMaxCannedNakSent() throws Exception {
        mHL7Callback.increaseCannedNakSentCount();
        // handle max canned nak sent recourse action
        HL7CommunicationControl commCntrl = mHL7CommunicationControlsInfo.getMaxCannedNakSentCommControl();
        if (mHL7Callback.getCannedNakSentCount() > commCntrl.getValue()) {
            // take exit recourse action
            if (ACTION_SUSPEND.equalsIgnoreCase(commCntrl.getRecourseAction())) {
                suspendRecourseAction("Exiting on reaching the MaxCannedNakSent");
            } else if (ACTION_RESET.equalsIgnoreCase(commCntrl.getRecourseAction())) {
                resetRecourseAction("Reset on reaching the MaxCannedNakSent");
            }
        }
    }

    private void hanldeMaxNakSent() throws Exception {
        mHL7Callback.increaseNakSentCount();
        // handle max nak sent recourse action
        HL7CommunicationControl commCntrl = mHL7CommunicationControlsInfo.getMaxNakSentCommControl();
        if (mHL7Callback.getNakSentCount() > commCntrl.getValue()) {
            // take exit recourse action
            if (ACTION_SUSPEND.equalsIgnoreCase(commCntrl.getRecourseAction())) {
                suspendRecourseAction("Exiting on reaching the MaxNakSent");
            } else if (ACTION_RESET.equalsIgnoreCase(commCntrl.getRecourseAction())) {
                resetRecourseAction("Reset on reaching the MaxNakSent");
            }
        }
    }

    private void suspendRecourseAction(String suspendReason) throws Exception {
        mLog.log(Level.WARNING, I18n.msg("W0118: Closing Connection to the External System for the reason: {0}",
                suspendReason));
        mHL7Callback.closeConnection();
        String msg = null;
        try {
            // emit warning logs and send alerts
            msg = I18n.msg("W0119: About to suspend endpoint: serviceName=[{0}], endpointName=[{1}].",
                    String.valueOf(mEndpoint.getServiceName()), mEndpoint.getEndpointName());
            if (mLog.isLoggable(Level.WARNING)) {
                mLog.log(Level.WARNING, msg);
            }
            mEndpoint.getServiceUnit().suspend(mEndpoint);
            AlertsUtil.getAlerter().warning(msg, HL7BindingComponent.SHORT_DISPLAY_NAME, mEndpoint.getUniqueName(),
                    AlertsUtil.getServerType(), AlertsUtil.COMPONENT_TYPE_BINDING,
                    NotificationEvent.OPERATIONAL_STATE_RUNNING, NotificationEvent.EVENT_TYPE_ALERT, "HL7BC-W0005");
        } catch (Exception e) {
            String errorMsg = e.getLocalizedMessage();
            if (errorMsg != null) {
                msg = I18n.msg(
                        "E0190: MBeanException caught when try to suspend endpoint (redelivery): serviceName=[{0}], endpointName=[{1}], error message=[{2}].",
                        String.valueOf(mEndpoint.getServiceName()), mEndpoint.getEndpointName(), errorMsg);
                mLog.log(Level.SEVERE, msg);
                AlertsUtil.getAlerter().critical(msg, HL7BindingComponent.SHORT_DISPLAY_NAME,
                        mEndpoint.getUniqueName(), AlertsUtil.getServerType(), AlertsUtil.COMPONENT_TYPE_BINDING,
                        NotificationEvent.OPERATIONAL_STATE_RUNNING, NotificationEvent.EVENT_TYPE_ALERT, "HL7BC-E0190");
            }
        }
    }

    private void resetRecourseAction(String resetReason) {
        String msg = I18n.msg("W0118: Closing Connection to the External System for the reason: {0}", resetReason);
        if (mLog.isLoggable(Level.WARNING)) {
            mLog.log(Level.WARNING, msg);
        }
        mHL7Callback.closeConnection();
        AlertsUtil.getAlerter().warning(msg, HL7BindingComponent.SHORT_DISPLAY_NAME, mEndpoint.getUniqueName(),
                AlertsUtil.getServerType(), AlertsUtil.COMPONENT_TYPE_BINDING,
                NotificationEvent.OPERATIONAL_STATE_RUNNING, NotificationEvent.EVENT_TYPE_ALERT, null);
    }

    private void processHL7V3InboundRequest() throws Exception {
        String msgXchangeID = null;
        String key = null;
        try {
            if (mLog.isLoggable(Level.INFO)) {
                mLog.log(Level.INFO, I18n.msg("I0126: HL7 v3 message received to process inbound request"));
            }
            long hl7MsgReceiveTime = System.currentTimeMillis();
            MessageExchange msgXchange = createMessageExchange(mMsgExchangePattern);
            msgXchangeID = msgXchange.getExchangeId();
            HL7Input hl7Input = mEndpoint.getHL7OperationInput(mHL7Operation);
            HL7Message hl7Message = hl7Input.getHL7Message();
            // check whether xml instance is validated successfully against the schema
            boolean validate = HL7V3MessageValidator.validateMsg(mOperationName, mEndpoint, hl7Message, mHL7Msg);
            if (validate) {
                HL7V3MessageEleParse.initEventReader(new DOMSource(createDocumentFromXML(true, mHL7Msg)));
                // get the transmission wrapper context
                HL7V3TransmissionWrapperContext tWContext = HL7V3MessageEleParse.getTransmissionWrapperContext();
                if (mSeqNoEnabled) {
                    // retrieve the value from DB
                    mExpSeqno = retrieveSequenceNumber(mEndpoint.getServiceUnitPath());
                    int recvSeqno = tWContext.getSequenceNumber();
                    // Todo : Handling Sequence Numbering
                }
            }
        } catch (Exception exc) {

        }
    }

    /**
     * Returns the DBConnection
     * 
     * @return DBConnection
     * @throws SQLException
     * @throws Exception
     */
    private DBConnection getDBConnection() throws SQLException, Exception {
        return mDBConnectionFactory.createConnection();
    }

    /**
     * Retrieve the sequence number from the Database
     * 
     * @param queryString Unique ID for the records in EXPSEQUENCENO Table
     * @throws SQLException
     * @throws Exception
     */
    private int retrieveSequenceNumber(String queryKey) throws SQLException, Exception {
        int seqNO = -1;
        DBConnection dbConnection = null;
		ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            SequenceNumDBO seqNoDBO = mDBObjectFactory.createSequenceNumDBO(queryKey);
            rs = dbConnection.getRow(seqNoDBO);
            if (rs.next()) {
                seqNoDBO.populateDBO(rs);
            } else {
                seqNoDBO.setESN(INITIAL_ESN);
                seqNoDBO.setESNState(NONE_ESN_STATE);
                dbConnection.insert(seqNoDBO);
                dbConnection.getUnderlyingConnection().commit();
            }
            seqNO = seqNoDBO.getESN();
        } finally {
			if(rs != null){
				rs.close();
			}
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return seqNO;
    }

    private int processSequenceNumbering(Node mshNode, int recvSeqNo) throws Exception {
        String ackCode = null;
        HL7CommunicationControl commCntrl = mHL7CommunicationControlsInfo.getMaxNakSentCommControl();
        if (recvSeqNo < -1) {
            if (mAckMode.equals(ACK_MODE_ENHANCED)) {
                ackCode = ACKBuilder.COMMIT_ERROR;
            } else {
                ackCode = ACKBuilder.APP_REJECT;
            }
            String nakString = makeNAK(mshNode, ackCode, "", RECV_INVALID_SEQNO);
            mHL7Callback.onReply(nakString, false);
            if (commCntrl != null && commCntrl.getEnabled().booleanValue()) {
                hanldeMaxNakSent();
            }
            return -1;
        } else if (recvSeqNo > 0 && mExpSeqno > 0 && recvSeqNo != mExpSeqno) {
            if (mAckMode.equals(ACK_MODE_ENHANCED)) {
                ackCode = ACKBuilder.COMMIT_ERROR;
            } else {
                ackCode = ACKBuilder.APP_REJECT;
            }
            String nakString = makeNAK(mshNode, ackCode, "", MISSMATCH_SEQNO);
            mHL7Callback.onReply(nakString, false);
            if (commCntrl != null && commCntrl.getEnabled().booleanValue()) {
                hanldeMaxNakSent();
            }
            return -1;

        }
        return 0;
    }

    /**
     * Build and return the key. The key is used while locating the HL7Callback
     * 
     * @param mshInfo MSHInfo
     * @return
     */
    private String buildKey(MSHInfo mshInfo) {
        StringBuilder sb = new StringBuilder(mshInfo.getMsgControlID()).append(mshInfo.getSendingApplication());
        sb.append(mshInfo.getSendingFacility()).append(mHL7Version);
        return sb.toString();
    }

    /**
     * Build and return the key. The key is used while locating the HL7Callback
     * 
     * @param mshInfo MSHInfo
     * @return
     */
    private String buildKeyForACK(MSHInfo mshInfo) {
        StringBuilder sb = new StringBuilder(mshInfo.getMsgControlID()).append(mshInfo.getReceivingApplication());
        sb.append(mshInfo.getReceivingFacility()).append(mHL7Version);
        return sb.toString();
    }

    /**
     * Process the "output" of an InOut message exchange
     * 
     * @param inout The InOut message exchange created by the inbound HL7 BC to process the HL7
     *            message request/response.
     * @throws Exception upon error processing the output.
     */
    public void onOutput(InOut inout) throws ApplicationException, Exception {
        Probe deNormalizationMeasurement = null;
        boolean isTransacted = inout.isTransacted();
        Transaction tx = null;
        String messageId = (String) ((MessageExchange)inout).getProperty(REDELIVERY_QOS_MSG_ID);
        InboundReplyContext inbReplyContext = mInboundExchanges.get(messageId);
        String appAckCond = inbReplyContext.getAppAckCondition();
        try {
            // <checkpoint>
            if ( monitorEnabled ) {
                HL7MMUtil.setCheckpoint(chkptMsgId, "Output-Processing-Initiated", mEndpoint);
            }
            if ( ndcEnabled ) {
                // "Push" NDC context
                Logger.getLogger("com.sun.EnterContext")
                      .log(Level.FINE, "{0}={1}", new Object[] {HL7MMUtil.SOLUTION_GROUP,
                      HL7MMUtil.getSolutionGroup(mEndpoint)});
            }
            if (mLog.isLoggable(LogSupport.LEVEL_DEBUG)) {
                mLog.log(LogSupport.LEVEL_DEBUG, I18n.msg(
                        "I0127: The onOutput method was called; output is for message exchange ID {0}",
                        inout.getExchangeId()));
            }
            // if recovery enabled, use the transaction message exchange
            if (isTransacted && mPersistenceEnabled) {
                tx = (Transaction) inout.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
                try {
                    // we have to resume the suspended transaction
                    resumeThreadTx(tx);
                } catch (Exception ex) {
                    // failure will be logged
                    mLog.log(Level.WARNING, I18n.msg("W0140: Exception occured while resuming the transaction {0}",
                            new Object[] { ex.getLocalizedMessage() }));
                }
                try {
                    if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                        try {
                            // As we are the initiator for tx we have to rollback
                            rollbackThreadTx(inout);
                            return;
                        } catch (Exception ex) {
                            // failure will be logged
                            mLog.log(Level.WARNING, I18n.msg("W0141: Exception occured while rollback the transaction {0}",
                                    new Object[] { ex.getLocalizedMessage() }));
                        }
                    }
                } catch (Exception ex) {
                    mLog.log(Level.SEVERE, I18n.msg("Exception occured while comparing the transaction status {0}",
                            ex.getLocalizedMessage()));                    
                }
            }
            // Todo: inserting sequence number
            QName qualOperName = inout.getOperation();
            HL7Operation hl7Operation = locateHL7Operation(qualOperName, mEndpoint);
            HL7Output hl7Output = hl7Operation.getHL7OperationOutput();
            HL7Message hl7Message = hl7Output.getHL7Message();
            NormalizedMessage outNormalizedMsg = inout.getOutMessage();
            // calculate performance Measurment
            String endPointID = (mEndpoint.getEndpointType() == Endpoint.EndpointType.INBOUND) ? createConsumingEndpointIdentifier(
                    mEndpoint.getServiceName(), mEndpoint.getEndpointName())
                    : createProvisioningEndpointIdentifier(mEndpoint.getServiceName(), mEndpoint.getEndpointName());
            deNormalizationMeasurement = Probe.info(getClass(), endPointID,
                    HL7BindingComponent.PERF_CAT_DENORMALIZATION);
            String ackPayLoad = mHL7Denormalizer.denormalize(outNormalizedMsg, qualOperName, mEndpoint, hl7Message);
            if (mAckMode.equals(ACK_MODE_ENHANCED) && null != appAckCond
                    && (appAckCond.equals(ALWAYS_CONDITION) || appAckCond.equals(SUCCESS_CONDITION))) {
                mHL7Callback.onReply(ackPayLoad, true);
                
            }else if(mAckMode.equals(ACK_MODE_ORIGINAL)){
                mHL7Callback.onReply(ackPayLoad, true);                
            }
            // if persistance enabled, store the message and ack into the database for recovery
            if(mPersistenceEnabled){
                try{
                    mPersistenceHandler.storeMessageInHL7MessageLog(mHL7Msg, ackPayLoad);
                    if (isTransacted) {
                        try {
                            // As we are the initiator for tx we have to commit
                            commitThreadTx(inout);
                        } catch (Exception ex) {
                            // failure will be logged
                            mLog.log(Level.WARNING, I18n.msg("W0142: Exception occured while commiting the transaction {0}",
                                    new Object[] { ex.getLocalizedMessage() }));
                        }
                    }
                }catch(Exception e){
                    mLog.log(Level.WARNING, I18n.msg("W0143: Exception occured while storing the HL7Message" +
                            "and ACK into the DB {0}",
                            new Object[] { e.getLocalizedMessage() }));                    
                }
            }
			// Journal the Message if Journalling is enabled
			if(mJournallingEnabled){
				try{
					journalMessageInDB(this.mEndpoint.getUniqueName() , mMessageControlID, "Server", mHL7Msg, 
												ackPayLoad, null, "DONE");
					if (mLog.isLoggable(Level.FINE)) {
							mLog.log(Level.FINE, I18n.msg("I0174: Successfully journal the message in DB"));
					}
				}catch(Exception e){
					mLog.log(Level.WARNING, I18n.msg("W0131: Failed to journal the message due to {0}",
							e.getLocalizedMessage()));
				}
			}
        } catch (Exception exe) {
            String nakMsg = generateCannedNakMessage(exe.getLocalizedMessage());
            mHL7Callback.onReply(nakMsg, false);
            if (isTransacted && mPersistenceEnabled) {
                try {
                    // As we are the initiator for tx we have to commit
                    commitThreadTx(inout);
                } catch (Exception ex) {
                    // failure will be logged
                    mLog.log(Level.WARNING, I18n.msg("W0142: Exception occured while commiting the transaction {0}",
                            new Object[] { ex.getLocalizedMessage() }));
                }
            }
        } finally {
            if (deNormalizationMeasurement != null) {
                deNormalizationMeasurement.end();
            }
            // <checkpoint>
            if ( monitorEnabled ) {
                HL7MMUtil.setCheckpoint(chkptMsgId, "Output-Processing-Completed", mEndpoint);
            }
            if ( ndcEnabled ) {
                // "Pop" NDC context
                Logger.getLogger("com.sun.ExitContext")
                      .log(Level.FINE, "{0}={1}", new Object[] {HL7MMUtil.SOLUTION_GROUP,
                      HL7MMUtil.getSolutionGroup(mEndpoint)});
            }
            // </checkpoint>
        }
    }

    /**
     * Called by the service engine after done with processing the in-only message exchange
     * 
     * @param msgXChange Message Exchange
     * @param processReplySuccess boolean value true means succesful processing of the message
     *            exchange. False indicates unsuccesful processing
     */
    public void onReply(MessageExchange msgXChange, boolean processReplySuccess) throws ApplicationException {
        String messageId = (String) msgXChange.getProperty(REDELIVERY_QOS_MSG_ID);
        boolean isTransacted = msgXChange.isTransacted();
        Transaction tx = null;
        try {
            // <checkpoint>
            if ( monitorEnabled ) {
                HL7MMUtil.setCheckpoint(chkptMsgId, "Message-Exchange-Initiated", mEndpoint);
            }
            if ( ndcEnabled ) {
                // "Push" NDC context
                Logger.getLogger("com.sun.EnterContext")
                      .log(Level.FINE, "{0}={1}", new Object[] {HL7MMUtil.SOLUTION_GROUP,
                      HL7MMUtil.getSolutionGroup(mEndpoint)});
            }
            InboundReplyContext inbReplyContext = mInboundExchanges.get(messageId);
            Source src = inbReplyContext.getHL7Request();
            String acceptAckCond = inbReplyContext.getAcceptAckCondition();
            String appAckCond = inbReplyContext.getAppAckCondition();
            // get the MSH node
            Node mshNode = ((DOMSource) src).getNode().getFirstChild().getFirstChild().getFirstChild().getFirstChild();
            String errorMsg = ACKErrorCodes.ApplicationInternalError.errorMessage;
            if (msgXChange instanceof InOut) {
                errorMsg = msgXChange.getError().getLocalizedMessage();
            }
            if (!processReplySuccess) {
                String ackCode = (mAckMode.equals(ACK_MODE_ORIGINAL)) ? ACKBuilder.APP_REJECT
                        : ACKBuilder.COMMIT_REJECT;
                if (mAckMode.equals(ACK_MODE_ORIGINAL)
                        || (mAckMode.equals(ACK_MODE_ENHANCED) && null != acceptAckCond && (acceptAckCond.equals(ALWAYS_CONDITION) || acceptAckCond.equals(ERROR_CONDITION)))) {
                    String nakString = makeNAK(mshNode, ackCode, ACKErrorCodes.ApplicationInternalError.errorCode,
                            errorMsg);
                    if (mLog.isLoggable(Level.FINE)) {
                        mLog.log(Level.FINE, I18n.msg("I0128: Sending NAK back to the sender"));
                    }
                    mHL7Callback.onReply(nakString, false);
                }
            } else {
                if (isTransacted && mPersistenceEnabled && msgXChange instanceof InOnly) {
                    tx = (Transaction) msgXChange.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
                    try {
                        // we have to resume the suspended transaction
                        resumeThreadTx(tx);
                    } catch (Exception ex) {
                        // failure will be logged
                        mLog.log(Level.WARNING, I18n.msg("W0140: Exception occured while resuming the transaction {0}",
                                new Object[] { ex.getLocalizedMessage() }));
                    }
                    try {
                        if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                            try {
                                // As we are the initiator for tx we have to rollback
                                rollbackThreadTx(msgXChange);
                                // put back one permit for throttling handling
                                // This is call is usefule for Inonly and InOut contians error.
                                mEndpoint.releaseThrottle();
                                // Remove the message exchange from the inbound message exchange map
                                mInboundExchanges.remove(messageId);
                                return;
                            } catch (Exception ex) {
                                // failure will be logged
                                mLog.log(Level.WARNING, I18n.msg("W0141: Exception occured while rollback the transaction {0}",
                                        new Object[] { ex.getLocalizedMessage() }));
                            }
                        }
                    } catch (Exception ex) {
                        mLog.log(Level.SEVERE, I18n.msg("Exception occured while comparing the transaction status {0}",
                                ex.getLocalizedMessage()));                          
                    }
                }
                boolean appAckFound = true;
                String ackString = null;
                if (mAckMode.equals(ACK_MODE_ENHANCED) && null != acceptAckCond
                        && (acceptAckCond.equals(ALWAYS_CONDITION) || acceptAckCond.equals(SUCCESS_CONDITION))) {
                    if (mSeqNoEnabled) {
                        if (appAckCond == null || appAckCond.equals(NEVER_CONDITION)) {
                            DBConnection dbConn = null;
                            try {
                                dbConn = getDBConnection();
                                int recvSeqno = getSeqNumFromMsg(src);
                                int seqNo = processSequenceNumbering(recvSeqno, mEndpoint.getServiceUnitPath(), dbConn);
                                mExpSeqno = seqNo;
                                appAckFound = false;
                            } finally {
                                if (dbConn != null) {
                                    dbConn.close();
                                }
                            }
                        }
                    }
                    ackString = makeACK(mshNode, ACKBuilder.COMMIT_ACCEPT, appAckFound);
                    if (mLog.isLoggable(Level.FINE)) {
                        mLog.log(Level.FINE, I18n.msg("I0129: Sending ACK back to the sender"));
                    }
                    mHL7Callback.onReply(ackString, true);
                } else if (mAckMode.equals(ACK_MODE_ORIGINAL)) {
                    if (mSeqNoEnabled) {
                        DBConnection dbConn = null;
                        try {
                            dbConn = getDBConnection();
                            int recvSeqno = getSeqNumFromMsg(src);
                            int seqNo = processSequenceNumbering(recvSeqno, mEndpoint.getServiceUnitPath(), dbConn);
                            mExpSeqno = seqNo;
                        } finally {
                            if (dbConn != null) {
                                dbConn.close();
                            }
                        }
                        if (mLog.isLoggable(Level.FINEST)) {
                            mLog.log(Level.FINEST, I18n.msg("I0130: Sending ACK with sequence number back to the sender"));
                        }
                    }
                    ackString = makeACK(mshNode, ACKBuilder.APP_ACCEPT, appAckFound);
                    if (mLog.isLoggable(Level.FINE)) {
                        mLog.log(Level.FINE, I18n.msg("I0129: Sending ACK back to the sender"));
                    }
                    mHL7Callback.onReply(ackString, true);
                }
                if(mPersistenceEnabled){
                    try{
                        mPersistenceHandler.storeMessageInHL7MessageLog(mHL7Msg, ackString);
                        if (isTransacted && msgXChange instanceof InOnly) {
                            try {
                                // As we are the initiator for tx we have to commit
                                commitThreadTx(msgXChange);
                            } catch (Exception ex) {
                                // failure will be logged
                                mLog.log(Level.WARNING, I18n.msg("W0142: Exception occured while commiting the transaction {0}",
                                        new Object[] { ex.getLocalizedMessage() }));
                            }
                        }
                    }catch(Exception e){
                        mLog.log(Level.WARNING, I18n.msg("W0143: Exception occured while storing the HL7Message" +
                                "and ACK into the DB {0}",
                                new Object[] { e.getLocalizedMessage() }));                        
                    }
                }
            }
        } catch (Exception ex) {
            if(mPersistenceEnabled){
                try {
                    // As we are the initiator for tx we have to rollback
                    rollbackThreadTx(msgXChange);
                } catch (Exception ex1) {
                    // failure will be logged
                    mLog.log(Level.WARNING, I18n.msg("W0141: Exception occured while rollback the transaction {0}",
                            new Object[] { ex1.getLocalizedMessage() }));
                }
            }
            throw new ApplicationException(ex.getCause());
        }  finally {
            // <checkpoint>
            if ( monitorEnabled ) {
                HL7MMUtil.setCheckpoint(chkptMsgId, "Message-Exchange-Completed", mEndpoint);
            }
            if ( ndcEnabled ) {
                // "Pop" NDC context
                Logger.getLogger("com.sun.ExitContext")
                      .log(Level.FINE, "{0}={1}", new Object[] {HL7MMUtil.SOLUTION_GROUP,
                      HL7MMUtil.getSolutionGroup(mEndpoint)});
            }
            // </checkpoint>

        }
        // put back one permit for throttling handling
        // This is call is usefule for Inonly and InOut contians error.
        mEndpoint.releaseThrottle();
        // Remove the message exchange from the inbound message exchange map
        mInboundExchanges.remove(messageId);
    }

    private HL7Operation locateHL7Operation(QName opname, Endpoint endpoint) {
        // return (HL7Operation) endpoint.getHL7Operations().get(opname);
        PortType portType = getPortType(endpoint);
        return (HL7Operation) endpoint.getHL7Operations().get(
                new QName(portType.getQName().getNamespaceURI(), opname.getLocalPart()));
    }

    private PortType getPortType(Endpoint endpoint) {
        String serviceName = endpoint.getServiceName().toString();
        String endpointName = endpoint.getEndpointName();
        Definition def = endpoint.getDefinition();
        Map services = def.getServices();

        // DO NOT use the getService() method.
        // It checks all imported WSDLs.
        Service svc = (Service) services.get(QName.valueOf(serviceName));
        if (svc == null) {
            return null;
        }

        Port port = svc.getPort(QName.valueOf(endpointName).getLocalPart());
        if (port == null) {
            return null;
        }

        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }
        return binding.getPortType();
    }

    /**
     * Logs the normalized message
     * 
     * @param exchangeId message exchange Id
     * @param msgSrc javax.xml.transform.Source
     */
    private void logNormalizedMessage(String exchangeId, Source msgSrc) {
        if (mLog.isLoggable(Level.FINEST)) {
            StringWriter out = null;
            if (msgSrc != null) {
                try {
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer trans = tFactory.newTransformer();
                    trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    trans.setOutputProperty(OutputKeys.INDENT, "yes");
                    trans.setOutputProperty(OutputKeys.METHOD, "xml");
                    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    out = new StringWriter();
                    StreamResult result = new StreamResult(out);
                    trans.transform(msgSrc, result);
                    out.flush();
                    out.close();
                } catch (Throwable t) {
                    ;
                    ;
                }
            }

            if (mLog.isLoggable(Level.FINEST)) {
                mLog.log(Level.FINEST, I18n.msg("I0131: Message exchange {0}; content source [{1}]", exchangeId,
                        (out == null ? "null" : out.toString())));
            }
        }
    }

    /**
     * Send a MessageExchange to the NMR
     */
    private String sendMessageToNMR(Source input, InboundReplyContext inReplyContext) throws Exception {
        // A unique message ID for each message (not message exchange)
        // is required for Redelivery. Retries will not work without it.
        // Group ID is optional, however.
        String uniqMsgID = mMsgGenerator.nextId();
        ServiceEndpoint se = mContext.getEndpoint(mEndpoint.getServiceName(), mEndpoint.getEndpointName());

        if (se == null) {
            mLog.log(Level.SEVERE, I18n.msg("E0191: Failed to locate endpoint {0}, service {1}",
                    mEndpoint.getServiceName(), mEndpoint.getEndpointName()));
            String errMsg = I18n.msg("E0191: Failed to locate endpoint {0}, service {1}", mEndpoint.getServiceName(),
                    mEndpoint.getEndpointName());
            throw new MessagingException(errMsg);
        }
        boolean isOneWay = mMsgExchangePattern.equals(Endpoint.EndpointMessageType.IN_ONLY) ? true : false;

        BaseExchangeTemplates template = new BaseExchangeTemplates(se, mOperationName, isOneWay, null, uniqMsgID,
                input, mMsgExchangeFactory);
        if(mPersistenceEnabled){
            getTransactionManager().begin();
            mTx = getTransactionManager().getTransaction();
            template.setTransactionManager(getTransactionManager());
            template.setTransaction(mTx);
        }

        Properties MEProp = new Properties();
        MEProp.setProperty(REDELIVERY_QOS_MSG_ID, template.getUniqueId());
        template.setPropExchange(MEProp);

        Properties NMProp = new Properties();
        // MsgID, Endpoint Name
       // NMProp.put(NMPropertiesUtil.NM_PROP_MESSAGE_ID, uniqMsgID);
        NMProp.put(NMPropertiesUtil.NM_PROP_EP_NAME, mEndpoint.getServiceName().toString() + ","
                + mEndpoint.getEndpointName());

        // <checkpoint>
        // send the MSG-ID downstream via NM Property
        if (monitorEnabled) {
            NMProp.put(HL7MMUtil.MESSAGE_TRACKING_ID_KEY, chkptMsgId);
        }else {
            NMProp.put(NMPropertiesUtil.NM_PROP_MESSAGE_ID, uniqMsgID);            
        }
        // </checkpoint>

        if (mRuntimeConfig.getAllowDynamicEndpoint().booleanValue()) {
            // extract hl7bc specific NM properties info
            // from address and protocolproperties elements
            Map nmPropsMap = NMPropertiesUtil.extractNMProperties(mEndpoint.getHL7Address(),
                    mEndpoint.getHL7ProtocolProperties());
            if (nmPropsMap != null)
                NMProp.putAll(nmPropsMap);
            NMProp.put(NMPropertiesUtil.CLIENT_ADDRESS, mHL7Callback.getClientInfo());

        }
        template.setPropNM(NMProp);

        // Save the reply context in inbound message exchange map
        // so that the reply from the NMR can be processed accordingly.
        // The reply will be processed by the outbound message exchange
        // loop using this context.
        mInboundExchanges.put(template.getUniqueId(), inReplyContext);
        if(mPersistenceEnabled){
            //template.createExchange();
            getTransactionManager().suspend();
        }

        mChannel.send(template);
        mEndpoint.getEndpointStatus().incrementSentRequests();
        return uniqMsgID;

    }

    /**
     * Sends an acknowledgment to the HL7 System
     * 
     * @param hl7Callback holds the routing information for HL7 external system
     * @throws Exception
     */
    private void sendACKToExtSys(HL7Callback hl7Callback) throws Exception {
        // TODO: handling recourse actions
        hl7Callback.onReply(mHL7Msg, true);
    }

    /**
     * Adds the Sft segment fields to the ACK
     * 
     * @param ackBuilder reference to the ACKBuilder
     */
    private void addSftSegmentFields(ACKBuilder ackBuilder) {
        if (ackBuilder instanceof HL7v25ACKBuilder) {
            ((HL7v25ACKBuilder) ackBuilder).setSFTEnabled(mSFTEnabled);
            ((HL7v25ACKBuilder) ackBuilder).setSoftwareVendorOrganization(mEndpoint.getHL7ProtocolProperties().getSoftwareVendorOrganization());
            ((HL7v25ACKBuilder) ackBuilder).setSoftwareCertifiedVersionOrReleaseNumber(mEndpoint.getHL7ProtocolProperties().getSoftwareCertifiedVersionOrReleaseNumber());
            ((HL7v25ACKBuilder) ackBuilder).setSoftwareProductName(mEndpoint.getHL7ProtocolProperties().getSoftwareProductName());
            ((HL7v25ACKBuilder) ackBuilder).setSoftwareBinaryID(mEndpoint.getHL7ProtocolProperties().getSoftwareBinaryID());
            ((HL7v25ACKBuilder) ackBuilder).setSoftwareProductInformation(mEndpoint.getHL7ProtocolProperties().getSoftwareProductInformation());
            ((HL7v25ACKBuilder) ackBuilder).setSoftwareInstallDate(mEndpoint.getHL7ProtocolProperties().getSoftwareInstallDate());
        } else if (ackBuilder instanceof HL7v251ACKBuilder) {
            ((HL7v251ACKBuilder) ackBuilder).setSFTEnabled(mSFTEnabled);
            ((HL7v251ACKBuilder) ackBuilder).setSoftwareVendorOrganization(mEndpoint.getHL7ProtocolProperties().getSoftwareVendorOrganization());
            ((HL7v251ACKBuilder) ackBuilder).setSoftwareCertifiedVersionOrReleaseNumber(mEndpoint.getHL7ProtocolProperties().getSoftwareCertifiedVersionOrReleaseNumber());
            ((HL7v251ACKBuilder) ackBuilder).setSoftwareProductName(mEndpoint.getHL7ProtocolProperties().getSoftwareProductName());
            ((HL7v251ACKBuilder) ackBuilder).setSoftwareBinaryID(mEndpoint.getHL7ProtocolProperties().getSoftwareBinaryID());
            ((HL7v251ACKBuilder) ackBuilder).setSoftwareProductInformation(mEndpoint.getHL7ProtocolProperties().getSoftwareProductInformation());
            ((HL7v251ACKBuilder) ackBuilder).setSoftwareInstallDate(mEndpoint.getHL7ProtocolProperties().getSoftwareInstallDate());
        } else if (ackBuilder instanceof HL7v26ACKBuilder) {
            ((HL7v26ACKBuilder) ackBuilder).setSFTEnabled(mSFTEnabled);
            ((HL7v26ACKBuilder) ackBuilder).setSoftwareVendorOrganization(mEndpoint.getHL7ProtocolProperties().getSoftwareVendorOrganization());
            ((HL7v26ACKBuilder) ackBuilder).setSoftwareCertifiedVersionOrReleaseNumber(mEndpoint.getHL7ProtocolProperties().getSoftwareCertifiedVersionOrReleaseNumber());
            ((HL7v26ACKBuilder) ackBuilder).setSoftwareProductName(mEndpoint.getHL7ProtocolProperties().getSoftwareProductName());
            ((HL7v26ACKBuilder) ackBuilder).setSoftwareBinaryID(mEndpoint.getHL7ProtocolProperties().getSoftwareBinaryID());
            ((HL7v26ACKBuilder) ackBuilder).setSoftwareProductInformation(mEndpoint.getHL7ProtocolProperties().getSoftwareProductInformation());
            ((HL7v26ACKBuilder) ackBuilder).setSoftwareInstallDate(mEndpoint.getHL7ProtocolProperties().getSoftwareInstallDate());
        }
    }
    /**
     * Adds the UAC segment fields to the ACK
     * 
     * @param ackBuilder reference to the ACKBuilder
     */
    private void addUACSegmentFields(ACKBuilder ackBuilder) {
        if (ackBuilder instanceof HL7v26ACKBuilder) {
            ((HL7v26ACKBuilder) ackBuilder).setUACEnabled(mUACEnabled);
            ((HL7v26ACKBuilder) ackBuilder).setUserAuthenticationCredentialTypeCode(mEndpoint.getHL7ProtocolProperties().getUserAuthenticationCredentialTypeCode());
            ((HL7v26ACKBuilder) ackBuilder).setUserAuthenticationCredential(mEndpoint.getHL7ProtocolProperties().getUserAuthenticationCredential());
        }
    }

    /**
     * Method toEmptyStringOnNull. Returns an empty string if the parameter is null, otherwise
     * returns the string simply.
     * 
     * @param str
     * @return String
     */
    private String toEmptyStringOnNull(String str) {
        return (null == str) ? "" : str;
    }

    private String generateCannedNakMessage(String errTextMessage) throws Exception {
        char fldSeparator = (char) mEndpoint.getHL7ProtocolProperties().getFieldSeparator().byteValue();
        StringBuilder sb = new StringBuilder(MSH);
        sb.append(fldSeparator);
        sb.append(toEmptyStringOnNull(mEndpoint.getHL7ProtocolProperties().getEncodingCharacters())); // MSH-02
        sb.append(fldSeparator);
        sb.append(toEmptyStringOnNull(mEndpoint.getHL7ProtocolProperties().getSendingApplication())); // MSH-03
        sb.append(fldSeparator);
        sb.append(toEmptyStringOnNull(mEndpoint.getHL7ProtocolProperties().getSendingFacility())); // MSH-04
        sb.append(fldSeparator);
        sb.append(toEmptyStringOnNull(mEndpoint.getHL7ProtocolProperties().getSendingApplication())); // MSH-05
        sb.append(fldSeparator);
        sb.append(toEmptyStringOnNull(mEndpoint.getHL7ProtocolProperties().getSendingFacility())); // MSH-06
        sb.append(fldSeparator);
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String formattedStr = formatter.format(new java.util.Date());
        sb.append(formattedStr); // MSH-07
        sb.append(fldSeparator);
        sb.append(""); // MSH-08
        sb.append(fldSeparator);
        sb.append(ACK); // MSH-09
        sb.append(fldSeparator);
        String messageControlID = "" + System.currentTimeMillis(); // ?? how to set this field?
        sb.append(messageControlID); // MSH-10
        sb.append(fldSeparator);

        if ((null == mEndpoint.getHL7ProtocolProperties().getProcessingID())
                || (0 == mEndpoint.getHL7ProtocolProperties().getProcessingID().length())) {
            sb.append("D"); // MSH-11 "D" - Debugging
        } else {
            sb.append(mEndpoint.getHL7ProtocolProperties().getProcessingID()); // MSH-11
        }

        sb.append(fldSeparator);
        sb.append(mEndpoint.getHL7ProtocolProperties().getVersionID()); // MSH-12
        sb.append(fldSeparator);
        sb.append(""); // MSH-13
        sb.append(fldSeparator);
        sb.append(""); // MSH-14

        if (!mHL7Version.equals(HL7v21)) {
            sb.append(fldSeparator);
            // ??
            sb.append(ERROR_CONDITION); // MSH-15: "ER" - Error/Reject
            sb.append(fldSeparator);
            sb.append(ERROR_CONDITION); // MSH-16: "ER" - Error/Reject
        }

        sb.append(DEFAULT_SEGMENT_TERMINATOR_STRING);
        boolean isSftAllowed = mHL7Version.equals(HL7v25) || mHL7Version.equals(HL7v251) ||
					mHL7Version.equals(HL7v26);
        if (isSftAllowed && mSFTEnabled) {
            // SFT segment
            sb.append(SFT); // SFT-00
            sb.append(fldSeparator);
            sb.append(mEndpoint.getHL7ProtocolProperties().getSoftwareVendorOrganization()); // SFT-01
            sb.append(fldSeparator);
            sb.append(mEndpoint.getHL7ProtocolProperties().getSoftwareCertifiedVersionOrReleaseNumber()); // SFT-02
            sb.append(fldSeparator);
            sb.append(mEndpoint.getHL7ProtocolProperties().getSoftwareProductName()); // SFT-03
            sb.append(fldSeparator);
            sb.append(mEndpoint.getHL7ProtocolProperties().getSoftwareBinaryID()); // SFT-04
            sb.append(fldSeparator);
            sb.append(mEndpoint.getHL7ProtocolProperties().getSoftwareProductInformation()); // SFT-05
            sb.append(fldSeparator);
            sb.append(mEndpoint.getHL7ProtocolProperties().getSoftwareInstallDate()); // SFT-06
            sb.append(DEFAULT_SEGMENT_TERMINATOR_STRING);
        }

        // MSA segment
        sb.append(MSA); // MSA-00
        sb.append(fldSeparator);

        // ??
        if (mAckMode.equals(ACK_MODE_ORIGINAL)) {
            sb.append(APP_REJECT); // MSA-01: "AR" - Application Acknowledgment Reject
        } else {
            sb.append(COMMIT_REJECT); // MSA-01: "CR" - Commit Acknowledgment Reject
        }

        sb.append(fldSeparator);
        sb.append(messageControlID); // MSA-02
        sb.append(fldSeparator);

        sb.append(errTextMessage); // MSA-03
        sb.append(fldSeparator);
        sb.append(""); // MSA-04
        sb.append(fldSeparator);
        sb.append(""); // MSA-05
        sb.append(fldSeparator);
        sb.append(""); // MSA-06

        sb.append(DEFAULT_SEGMENT_TERMINATOR_STRING);

        // ERR segment
        sb.append(ERR); // ERR-00
        sb.append(fldSeparator);
        sb.append(""); // ERR-01

        if (isSftAllowed) {
            sb.append(fldSeparator);
            sb.append(""); // ERR-02
            sb.append(fldSeparator);
            sb.append(ACKErrorCodes.ApplicationInternalError.errorCode); // ERR-03 - HL7 error
            // code
            sb.append(fldSeparator);
            sb.append("E"); // ERR-04 - Severity
            sb.append(fldSeparator);
            sb.append(""); // ERR-05
            sb.append(fldSeparator);
            sb.append(""); // ERR-06
            sb.append(fldSeparator);
            sb.append(""); // ERR-07
            sb.append(fldSeparator);
            sb.append(""); // ERR-08
            sb.append(fldSeparator);
            sb.append(""); // ERR-09
            sb.append(fldSeparator);
            sb.append(""); // ERR-10
            sb.append(fldSeparator);
            sb.append(""); // ERR-11
            sb.append(fldSeparator);
            sb.append(""); // ERR-12
        }
        return sb.toString();
    }

    /**
     * Create and returns a NAK
     * 
     * @param mshNode MSH Node
     * @param ackCode acknowledgment code
     * @param errCode error code
     * @param errMsg error message
     * @return Negative Acknowledgment
     * @throws Exception
     */
    private String makeNAK(Node mshNode, String ackCode, String errCode, String errMsg) throws Exception {
        String nakMessage = null;
        ACKBuilderFactory.HL7Version hl7Version = ACKBuilderFactory.Util.stringToEnumValue(mHL7Version);
        if (mLog.isLoggable(Level.FINEST)) {
            mLog.log(Level.FINEST, I18n.msg("I0133: Constructing the Nak message with version ID {0}", hl7Version));
        }
        ACKBuilder ackBuilder = ACKBuilderFactory.createACKBuilder(hl7Version);
        ackBuilder.setAcknowledgmentCode(ackCode);
        ackBuilder.setErrorCode(errCode);
        ackBuilder.setErrorMessage(errMsg);
        ackBuilder.setMSHSegment(mshNode);
        // if sft segment is enabled then add its fields
        if (mSFTEnabled) {
            addSftSegmentFields(ackBuilder);
        }
        // if UAC segment is enabled then add its fields
        if (mUACEnabled) {
            addUACSegmentFields(ackBuilder);
        }
        if (mSeqNoEnabled) {
            ackBuilder.setExpectdSeqNo(mExpSeqno);
        }
        Node ackNode = ackBuilder.buildACK();
        String use = mHL7Operation.getHL7OperationInput().getHL7Message().getUseType();
        if (use.equals(HL7Message.ATTR_USE_TYPE_ENCODED)) {
            // Encode DOM source to HL7 raw data format
            Source source = new DOMSource(ackNode);
            nakMessage = mEncoder.encodeToString(source);
        } else {
            // to support xml message payload
            nakMessage = transformToString(ackNode, "UTF-8", true, "no", "xml");
        }
        if (mLog.isLoggable(Level.FINEST)) {
            mLog.log(Level.FINEST, I18n.msg("I0134: Constructed Nak message to be send to the sender {0}", nakMessage));
        }
		// journal message and NAK if Journalling is enabled
		if(mJournallingEnabled){
			try{
				journalMessageInDB(this.mEndpoint.getUniqueName() , mMessageControlID, "Server", mHL7Msg, 
										null, nakMessage, "ERROR");								
				if (mLog.isLoggable(Level.FINE)) {
						mLog.log(Level.FINE, I18n.msg("I0175: Successfully archive the Error message in DB"));
			 	}
			}catch(Exception e){
				mLog.log(Level.WARNING, I18n.msg("W0132: Failed to archive the Error message due to {0}",
									e.getLocalizedMessage()));
			}
		}
        return nakMessage;
    }

    /**
     * Create and returns an Accept Acknowledgment
     * 
     * @param mshNode MSH Node
     * @param ackCode acknowledgment code
     * @param appAckFound Application Ack value
     * @return accept acknowledgment
     * @throws Exception
     */
    private String makeACK(Node mshNode, String ackCode, boolean appAckFound) throws Exception {
        String ackMessage = null;
        ACKBuilderFactory.HL7Version hl7Version = ACKBuilderFactory.Util.stringToEnumValue(mHL7Version);
        if (mLog.isLoggable(Level.FINE)) {
            mLog.log(Level.FINE, I18n.msg("I0135: Constructing the ACK message with version ID {0}", hl7Version));
        }
        ACKBuilder ackBuilder = ACKBuilderFactory.createACKBuilder(hl7Version);
        ackBuilder.setAcknowledgmentCode(ackCode);
        ackBuilder.setMSHSegment(mshNode);
        // if sft segment is enabled then add its fields
        if (mSFTEnabled) {
            addSftSegmentFields(ackBuilder);
        }
        // if UAC segment is enabled then add its fields
        if (mUACEnabled) {
            addUACSegmentFields(ackBuilder);
        }
        if (mSeqNoEnabled
                && ((ackCode.equals(ACKBuilder.APP_ACCEPT)) || ((ackCode.equals(ACKBuilder.COMMIT_ACCEPT) && !appAckFound)))) {
            ackBuilder.setExpectdSeqNo(mExpSeqno);
        }
        Node ackNode = ackBuilder.buildACK();
        String use = mHL7Operation.getHL7OperationInput().getHL7Message().getUseType();
        if (use.equals(HL7Message.ATTR_USE_TYPE_ENCODED)) {
            // Encode DOM source to HL7 raw data format
            Source source = new DOMSource(ackNode);
            ackMessage = mEncoder.encodeToString(source);
        } else {
            // to support xml message payload
            ackMessage = transformToString(ackNode, "UTF-8", true, "no", "xml");
        }
        if (mLog.isLoggable(Level.FINE)) {
            mLog.log(Level.FINE, I18n.msg("I0136: Constructed ACK message to be send to the sender {0}", ackMessage));
        }
		// Journal the Message if Journalling is enabled
		if(mJournallingEnabled){
			try{
				journalMessageInDB(this.mEndpoint.getUniqueName() , mMessageControlID, "Server", mHL7Msg, 
											ackMessage, null, "DONE");
				if (mLog.isLoggable(Level.FINE)) {
						mLog.log(Level.FINE, I18n.msg("I0174: Successfully journal the message in DB"));
				}
			}catch(Exception e){
				mLog.log(Level.WARNING, I18n.msg("W0131: Failed to journal the message due to {0}",
						e.getLocalizedMessage()));
			}
        }
        return ackMessage;
    }

    /**
     * Given the message exchange pattern create Message exchange and returns
     * 
     * @param mepType message exchange pattern type
     * @return MessageExchange
     * @throws Exception
     */
    private MessageExchange createMessageExchange(String mepType) throws Exception {
        MessageExchange msgEx = null;
        // Create the MessageExchangeFactory
        if (mMsgExchangeFactory == null) {
            mMsgExchangeFactory = mChannel.createExchangeFactory();
        }
        try {
            // in case of enhanced acknowledgement mode create InOnly MessageExchange
            if (mepType.equals(Endpoint.EndpointMessageType.IN_ONLY)) {
                msgEx = mMsgExchangeFactory.createInOnlyExchange();
            } else if (mepType.equals(Endpoint.EndpointMessageType.IN_OUT)) {
                msgEx = mMsgExchangeFactory.createInOutExchange();
            } else {
                throw new MessageExchangeProcessingException(I18n.msg(
                        "E0192: Unable to create Message Exchange with mepType : {0}", mepType));
            }
        } catch (MessagingException ex) {
            throw new MessageExchangeProcessingException(I18n.msg(
                    "E0193: Unable to create Message Exchange with mepType : {0} and e=[{1}]", mepType, ex));
        }
        return msgEx;
    }

    /**
     * Get the sequence number from the HL7 Message
     * 
     * @param src javax.xml.transform.Source
     */
    private int getSeqNumFromMsg(Source src) {
        Node mshNode = ((DOMSource) src).getNode().getFirstChild().getFirstChild().getFirstChild().getFirstChild();
        NodeList mshList = mshNode.getChildNodes();
        int seqNo = -1;
        Node node = null;
        String name = null;
        for (int i = 0; i < mshList.getLength(); i++) {
            node = mshList.item(i);
            name = node.getLocalName();
            if (name.equals(MSH13)) {
                String seqNumber = node.getFirstChild().getNodeValue();
                try {
                    seqNo = Integer.parseInt(seqNumber);
                } catch (Exception e) {
                    // throw new Exception("Invalid Sequence Numer Exists in the Message");
                }
                break;

            }
        }
        return seqNo;
    }

    /**
     * process the sequence number and update the status into Database
     * 
     * @param recvSeqNo the sequence number in the received message
     * @param queryingKey Unique ID for the records in EXPSEQUENCENO Table
     * @param dbConnection Connection to the underlying Database
     * @throws Exception
     */
    private int processSequenceNumbering(int recvSeqNo, String queryingKey, DBConnection dbConnection) throws Exception {
        if (mExpSeqno == -1) { // ESN State = NONE
            if (recvSeqNo == -1) { // incoming sequence number = -1
                return mExpSeqno;
            } else if (recvSeqNo == 0) { // incoming sequence number == 0
                return mExpSeqno;
            } else {// incoming sequence number >= 1
                mExpSeqno = recvSeqNo;
                int seqNoInDB = recvSeqNo + 1;
                // update the data base.
                updateSequenceNumberInDB(queryingKey, seqNoInDB, VALID_ESN_STATE, dbConnection);
                return mExpSeqno;
            }
        } else { // ESN State >= 1
            if (recvSeqNo == -1) { // incoming sequence number = -1
                mExpSeqno = recvSeqNo;
                int seqNoInDB = recvSeqNo;
                updateSequenceNumberInDB(queryingKey, seqNoInDB, NONE_ESN_STATE, dbConnection);
                return recvSeqNo;
            } else if (recvSeqNo == 0) { // incoming sequence number == 0
                return mExpSeqno;
            } else {// incoming sequence number >= 1
                mExpSeqno = recvSeqNo;
                int seqNoInDB = recvSeqNo + 1;
                // update the data base.
                updateSequenceNumberInDB(queryingKey, seqNoInDB, VALID_ESN_STATE, dbConnection);
                return mExpSeqno;
            }
        }
    }

    /**
     * Updates the sequence number in Database
     * 
     * @param queryString Unique ID for the records in EXPSEQUENCENO Table
     * @param esn expected sequence number
     * @param esnState expected sequence number state
     */
    private void updateSequenceNumberInDB(String queryingKey, int esn, String esnState, DBConnection dbConnection)
            throws SQLException, Exception {
        SequenceNumDBO seqNoDBO = mDBObjectFactory.createSequenceNumDBO(queryingKey, esn, esnState);
        dbConnection.update(seqNoDBO);
        dbConnection.getUnderlyingConnection().commit();
    }

    /**
     * Get a unique provisioning endpoint identifer for use with add/remove endpoints and data
     * retrieval
     * 
     * @param serviceName qualified service name the endpoint belongs to
     * @param portName the local name of the port
     * @return unique endpoint identifier string.
     */
    private String createProvisioningEndpointIdentifier(QName serviceName, String portName) {
        return serviceName.getNamespaceURI() + "," + serviceName.getLocalPart() + "," + portName + ","
                + PROVISIONING_ID;
    }

    /**
     * Get a unique consuming endpoint identifer for use with add/remove endpoints and data
     * retrieval
     * 
     * @param serviceName qualified service name the endpoint belongs to
     * @param portName the local name of the port
     * @return unique endpoint identifier string.
     */
    private String createConsumingEndpointIdentifier(QName serviceName, String portName) {
        return serviceName.getNamespaceURI() + "," + serviceName.getLocalPart() + "," + portName + "," + CONSUMING_ID;
    }

    /**
     * Handles {@link javax.jbi.messaging.MessagingException}s that occur when a
     * {@link javax.jbi.messaging.MessageExchange} cannot be sent on the {@link
     * javax.jbi.messaging.DeliveryChannel}.
     * 
     * @param error The exception that is caught or prepared by {@link
     *            com.sun.jbi.common.qos.messaging.MessagingChannel}.
     * @param mex The message exchange related to the specified error.
     */

    public void handleSendFailure(MessagingException error, MessageExchange mex) {
        String messageId = (String) mex.getProperty(REDELIVERY_QOS_MSG_ID);
        if (mInboundExchanges.containsKey(messageId)) {
            // remove it from IB exchange map
            mInboundExchanges.remove(messageId);
        }
        if (mLog.isLoggable(Level.FINE)) {
            mLog.log(Level.FINE, I18n.msg("handleSendFailure() : messageID with QoS Redelivery = {0}", messageId));
        }
    }

	/**
	 * Journal message into the database
     * 
     * @param applicationID  endpoint Unique Name.
	 * @param MSH.9th field value from the message
     * @param mode specify server mode ot client mode
	 * @param hl7message Received HL7 message
	 * @param hl7ACKMessage generated ACK message
	 * @param hl7NAKMessage generated NAK message
	 * @statue specify DONE or ERROR.
	 */
	private void journalMessageInDB(String applicationID, String messageControlID, String mode, String hl7Message, String hl7ACKMessage,
								String hl7NAKMessage, String status) throws Exception {	
		
	    DBConnection dbCon = null; 
	    try { 
		    	dbCon = getDBConnection(); 
		    	JournalHL7MessageLogDBO journalHL7LogDBO =  mDBObjectFactory.createJournalHL7MessageLogDBO();

				journalHL7LogDBO.setApplicationId(applicationID);		    	
		    	journalHL7LogDBO.setMessageControlId(messageControlID);
		    	journalHL7LogDBO.setMode(mode);
		    	journalHL7LogDBO.setRequestHL7Message(hl7Message);
		    	journalHL7LogDBO.setResponseACKMessage(hl7ACKMessage);
		    	journalHL7LogDBO.setResponseNAKMessage(hl7NAKMessage);
		    	journalHL7LogDBO.setStatus(status);
		    	dbCon.insert(journalHL7LogDBO);
		    	dbCon.getUnderlyingConnection().commit();
    	} catch(Exception ex) {
    		dbCon.getUnderlyingConnection().rollback();
    		throw ex; 
		} finally {
			if (dbCon != null) {
					dbCon.close(); 
			} 
		} 
	}
    
    private TransactionManager getTransactionManager() {
        return (TransactionManager)mContext.getTransactionManager();
    }

    // suspend thread transactional context
    private void resumeThreadTx(Transaction tx) throws Exception {
        if (tx != null) {
            ((TransactionManager) mContext.getTransactionManager()).resume(tx);
            if (mLog.isLoggable(Level.FINER)) {
                mLog.log(Level.FINER, " resuming txn  ");
            }
            if (mLog.isLoggable(Level.FINER)) {
                mLog.log(Level.FINER, " resuming txn  ", new Object[] { tx.toString() });
            }
            
        }
    }

    private void rollbackThreadTx(MessageExchange msgXChange) throws Exception {
        if (msgXChange.isTransacted()) {
            Transaction tx = (Transaction) msgXChange.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    throw ex;
                }
            } else {
                mLog.log(Level.FINER, " Transaction not found in the msg exchange  ");
            }
        }
    }

    private void commitThreadTx(MessageExchange msgXChange) throws Exception {
        if (msgXChange.isTransacted()) {
            Transaction tx = (Transaction) msgXChange.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
            if (tx != null) {
                try {
                    tx.commit();
                } catch (Exception ex) {
                    throw ex;
                }
            }
        } else {
            mLog.log(Level.FINER, " Transaction not found in the msg exchange  ");
        }
    }
}// end of class

