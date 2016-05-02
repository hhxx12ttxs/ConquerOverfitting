/*
 * AdroitLogic UltraESB Enterprise Service Bus
 *
 * Copyright (c) 2010-2012 AdroitLogic Private Ltd. (http://adroitlogic.org). All Rights Reserved.
 *
 * GNU Affero General Public License Usage
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program (See LICENSE-AGPL.TXT).
 * If not, see http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Commercial Usage
 *
 * Licensees holding valid UltraESB Commercial licenses may use this file in accordance with the UltraESB Commercial
 * License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written
 * agreement between you and AdroitLogic.
 *
 * If you are unsure which license is appropriate for your use, or have questions regarding the use of this file,
 * please contact AdroitLogic at info@adroitlogic.com
 */

package org.adroitlogic.ultraesb.core;

import org.adroitlogic.ultraesb.api.*;
import org.adroitlogic.ultraesb.api.format.MessageFormat;
import org.adroitlogic.ultraesb.core.endpoint.Endpoint;
import org.adroitlogic.ultraesb.core.format.RawFileMessage;
import org.adroitlogic.ultraesb.core.spring.SpringPlatformTransactionManager;
import org.adroitlogic.ultraesb.core.work.WorkManager;
import org.adroitlogic.ultraesb.transport.CompletionHandler;
import org.adroitlogic.ultraesb.transport.ResponseTrigger;
import org.adroitlogic.ultraesb.transport.TransactionInformation;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.management.openmbean.CompositeData;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.io.File;
import java.util.*;

/**
 * This class defines a Message that passes through the UltraESB. Refer to the samples and Javadocs for use of the 
 * public methods and examples
 *
 * @author asankha
 */
public class MessageImpl implements org.adroitlogic.ultraesb.api.Message {

    private static final Logger logger = LoggerFactory.getLogger(MessageImpl.class);

    public static class CorrelatedProperties {

        public static final String REQ_MSG_UUID         = "ultra.internal.correlated.req_msg_uuid";
        public static final String REQ_MSG              = "ultra.internal.correlated.req_msg";
        public static final String REQ_MSG_CONTENT_TYPE = "ultra.internal.correlated.req_msg_content_type";
        public static final String REQ_MSG_EP           = "ultra.internal.correlated.req_msg_ep";
        public static final String REQ_MSG_PS           = "ultra.internal.correlated.req_msg_ps";
        public static final String CORRELATED_REQ_MSG_OUT_SEQ = "ultra.internal.correlated.req_msg_out_seq";
        public static final String RESPONSE_TRIGGER     = "ultra.internal.correlated.response_trigger";
        public static final String TXN_SYNCS            = "ultra.internal.correlated.txn_syncs";
        public static final String TXN_RESOURCES        = "ultra.internal.correlated.txn_resources";
        public static final String TXN_STATUS           = "ultra.internal.correlated.txn_status";
        public static final String TXN_MANAGER          = "ultra.internal.correlated.txn_manager";
        public static final String JTA_TXN              = "ultra.internal.correlated.jta_txn";
        public static final String COMPLETION_HANDLER   = "ultra.internal.correlated.completion_handler";
    }

    public static final String DROPPED_MESSAGE              = "ultra.internal.message.dropped";

    private static final FastDateFormat dfm = FastDateFormat.getInstance("HH:mm:ss.SSS");

    /** The proxy service to which this message belongs. This will never change once set */
    private final ProxyService proxyService;
    /** The unique ID of the message */
    private final UUID messageUUID = UUID.randomUUID();
    /** For a response, the original request message with which this correlates */
    private final UUID correlatedRequestUUID;
    /** For cloned message, the parents UUID */
    private UUID parentMessageUUID;
    /** The originating transport ID */
    private final String originatingTransport;
    /** Is this a request or a response? Request messages are dispatched to the inSequence/inDestination, while
     * response messages are dispatched to the outSequence [or a specified sequence] and the outDestination
     */
    private final boolean request;
    /** The Destination URL of the current message - may not be a full URL */
    private String destinationURL = null;
    /** The contet type of the request message payload */
    private String contentType = null;
    /** The WorkManager currently "owning" this message for recovery */
    private WorkManager workManager = null;
    /** A list of elements currently processing this message, e.g. Sequence, Endpoint IDs */
    private final List<String> processingElements = new ArrayList<String>();

    // original transport headers, payload and message context properties
    private Map<String, String> originalTransportHeaders = new HashMap<String, String>();
    private Map<String, List<String>> originalTransportHeaderDuplicates = null;
    private Map<String, Object> originalMessageProperties = new HashMap<String, Object>();
    private MessageFormat originalPayload;

    // updated transport headers, and message context properties
    private Map<String, String> currentTransportHeaders = null;
    private Map<String, List<String>> currentTransportHeaderDuplicates = null;
    private Map<String, Object> currentMessageProperties = null;
    private MessageFormat currentPayload;
    private Map<String, MessageFormat> attachments = null;

    // optional response trigger set by a request-response transport with context state - this may not survive a crash
    private ResponseTrigger responseTrigger = null;

    /** A Map which would be copied as the originalMessageProperties for a response to this message */
    private Map<String, Object> respCorrelateMap = new HashMap<String, Object>();

    /** The Spring PlatformTransactionManager */
    private PlatformTransactionManager txnManager;
    /** The Spring TransactionStatus for this message */
    private TransactionStatus txnStatus = null;
    /** A List of errors encountered by this message */
    private List<ErrorInfo> errorList;
    /** The error handler sequence to invoke on this message */
    private String errorHandlerToInvoke;
    /** Is this message currently in an Error Map of a WorkManager ? */
    private boolean markedAsFailed = false;
    /** A stack of fault handlers */
    private Stack<String> faultHandlerStack = null;
    /** The last endpoint that processed this message */
    private Endpoint lastEndpoint;
    /** A Runnable that should be executed when this message is completed successfully or with error */
    private CompletionHandler completionHandler = null;
    /** Should the completion tasks be paused until explicit confirmation? */
    private boolean holdCompletionTask = false;
    /** A list of used MessageFile instances that should be released for GC once this message completes */
    private Set<MessageFile> messageFilesToRelease = null;
    /** A list of used MessageFormat instances that should be released for GC once this message completes */
    private Set<MessageFormat> messageFormatInstancesToRelease = null;
    /** A list of other Message instances that should be released for GC once this message completes */
    private Set<MessageImpl> messagesToRelease = null;
    /** An indicator that the current payload has been specifically set to null */
    private volatile boolean currentPayloadIsNull = false;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("proxy=").append(proxyService.getId()).append(',');
        sb.append("request=").append(request ? "true," : "false,");

        if (lastEndpoint != null) {
            sb.append("last-ep=").append(lastEndpoint).append(',');
        }
        if (currentPayload instanceof RawFileMessage) {
            sb.append("file=").append(((RawFileMessage) currentPayload).getFile().getName()).append(',');
        }
        if (markedAsFailed) {
            sb.append("marked-as-failed=true,");
        }
        if (holdCompletionTask) {
            sb.append("hold-completion=true,");
        }
        if (!processingElements.isEmpty()) {
            sb.append("processing-elements=").append(processingElements).append(',');
        }
        if (errorList != null && !errorList.isEmpty()) {
            sb.append("error-list=").append(errorList).append(",");
        }
        Object wipStart = getMessageProperty(WorkManager.WIP_START_TIME_NANO);
        if (wipStart != null && wipStart instanceof Long) {
            long currentMillis = System.currentTimeMillis();
            long currentNano = System.nanoTime();
            sb.append("wm-start-time=").append(dfm.format(
                new Date(currentMillis - (currentNano-((Long) wipStart)) / 1000000L))).append(',');
        }
        sb.append("uuid=").append(messageUUID);
        return sb.toString();
    }

    //-------------------------------------------------------------------------------

    // --- getter and setter methods ---

    /**
     * Is this message currently marked as failed?
     * @return true if message has been marked as failed
     */
    public boolean isMarkedAsFailed() {
        return markedAsFailed;
    }

    /**
     * Mark this message as failed, by passing true. This will direct any message completion handlers to appropriately
     * perform completion tasks expected of a failed message. (e.g. moving a file to the failed directory)
     * @param markedAsFailed true marks this message as failed
     */
    public void setMarkedAsFailed(boolean markedAsFailed) {
        this.markedAsFailed = markedAsFailed;
    }

    /**
     * Is this message currently marked to hold completion tasks on at the end of the execution thread
     * @return true if marked for holding of completion tasks
     */
    public boolean isHoldCompletionTask() {
        return holdCompletionTask;
    }

    /**
     * Get the current completion handler for this message
     * @return the completion handler - if any
     */
    public CompletionHandler getCompletionHandler() {
        return completionHandler;
    }

    /**
     * Set a completion handler for this message. Note - currently we only support one completion handler
     * @param completionHandler the new completion handler
     */
    public void setCompletionHandler(CompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
    }

    /**
     * Used to hold execution of the message completion handlers when the current sequence completes. (i.e. the thread of
     * execution) This allows a message completion to be postponed from the inSequence to the outSequence,
     * where a successful receipt or status code can be checked and the message completed on its state
     */
    public void holdCompletion() {
        holdCompletionTask = true;
        respCorrelateMap.put(CorrelatedProperties.COMPLETION_HANDLER, completionHandler);
    }

    /**
     * The error handler sequence id to invoke, without processing this message. Thus when an error handler is set
     * for invocation, the proxy service will execute that handler sequence, instead of the in or out sequences
     * @return the sequence id of the error handler
     */
    public String getErrorHandlerToInvoke() {
        return errorHandlerToInvoke;
    }

    /**
     * Set the sequence id as the error handler to be invoked. Usually the most recent fault handler id is popped
     * from the fault handler stack and assigned as the error handler to invoke
     * @param errorHandlerToInvoke the sequence id that will act as the error handler
     */
    public void setErrorHandlerToInvoke(String errorHandlerToInvoke) {
        this.errorHandlerToInvoke = errorHandlerToInvoke;
    }

    /**
     * The current work manager owning this message
     * @return the current work manager
     */
    public WorkManager getWorkManager() {
        return workManager;
    }

    /**
     * Assign a work manager to this thread
     * @param workManager set the work manager assigned to this message
     */
    public void setWorkManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    /**
     * Mark that this message has entered processing by the element identified with the specified id. This
     * information is used to detect processing completion
     * @param id the id of the element that started processing this message
     */
    public synchronized void beginProcessing(String id) {
        synchronized (processingElements) {
            if (!processingElements.contains(id)) {
                processingElements.add(id);
            }
        }
    }

    /**
     * Notification that the message has completed processing by the element with the specified id. When all
     * elements complete processing, the message is ready to release its resources and complete execution
     * @param id the id of the element that ended processing this message
     * @param triggerFinalization release resources if true and processing elements is empty
     */
    public synchronized void endProcessing(String id, boolean triggerFinalization) {

        synchronized (processingElements) {
            processingElements.remove(id);

            if (logger.isDebugEnabled()) {
                logger.debug("Message ID : " + messageUUID + " has completed processing under : " + id +
                    " Pending completion by : [" + processingElements + "]");
            }

            if (triggerFinalization) {
                if (processingElements.isEmpty()) {
                    releaseResources();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Message ID : " + messageUUID + " has completed all processing");
                    }
                }
            }
        }
    }

    public synchronized void endProcessing(String id) {
        endProcessing(id, true);
    }

    /**
     * Add a transport header as an original transport header of the transport that received the message
     * @param key the header name
     * @param value the header value
     */
    public void addOriginalTransportHeader(String key, String value) {
        if (originalTransportHeaders.containsKey(key)) {
            if (originalTransportHeaderDuplicates == null) {
                originalTransportHeaderDuplicates = new HashMap<String, List<String>>();
            }
            if (!originalTransportHeaderDuplicates.containsKey(key)) {
                originalTransportHeaderDuplicates.put(key, new ArrayList<String>());
            }
            originalTransportHeaderDuplicates.get(key).add(value);
        } else {
            originalTransportHeaders.put(key, value);
        }
    }

    /**
     * Get the current payload of the message
     * @return the current payload
     */
    public MessageFormat getCurrentPayload() {
        if (currentPayloadIsNull) {
            return null;
        } else if (currentPayload != null) {
            return currentPayload;
        } else {
            return originalPayload;
        }
    }

    /**
     * Set the current payload of the message
     * @param newPayload the new payload of the message
     */
    public void setCurrentPayload(MessageFormat newPayload) {
        if (currentPayload != null) {
            if (messageFormatInstancesToRelease == null) {
                messageFormatInstancesToRelease = new HashSet<MessageFormat>();
            }
            messageFormatInstancesToRelease.add(currentPayload);
        }
        currentPayload = newPayload;
        currentPayloadIsNull = (currentPayload == null);
    }

    /**
     * Add an attachment to the message identified by the specified key
     * @param key the identification key for the attachment
     * @param att the attachment payload
     */
    public void addAttachment(String key, MessageFormat att) {
        if (attachments == null) {
            attachments = new HashMap<String, MessageFormat>();
        }
        attachments.put(key, att);
    }

    /**
     * Get the attachment with the given key
     * @param key the identifier of the attachment
     * @return the attachment payload
     */
    public MessageFormat getAttachment(String key) {
        if (attachments != null) {
            return attachments.get(key);
        }
        return null;
    }

    /**
     * Does this message contain attachments?
     * @return true if the message contains attachments
     */
    public boolean containsAttachments() {
        return attachments != null && attachments.size() > 0;
    }

    /**
     * Return the Map of attachments keyed with the identifying string
     * @return attachments map or an empty map
     */
    public Map<String, MessageFormat> getAttachments() {
        if (attachments == null) {
            return Collections.EMPTY_MAP;
        }
        return attachments;
    }

    /**
     * Get the last endpoint that processed this message
     * @return the last endpoint id
     */
    public synchronized Endpoint getLastEndpoint() {
        return lastEndpoint;
    }

    /**
     * Set the id as the last endpoint that processed this message
     * @param lastEndpoint the id of the last endpoint
     */
    public synchronized void setLastEndpoint(Endpoint lastEndpoint) {
        this.lastEndpoint = lastEndpoint;
    }

    /**
     * The original payload of the message - as received from the transport, unless modified
     * @return the original message payload
     */
    public MessageFormat getOriginalPayload() {
        return originalPayload;
    }

    /**
     * Set or replace the original message payload
     * @param originalPayload set or replace the original message payload
     */
    public void setOriginalPayload(MessageFormat originalPayload) {
        this.originalPayload = originalPayload;
    }

    /**
     * Detach the current payload from this message. A detached Payload should be reclaimed (e.g. if a RawFileMessage)
     * by the user code, and will not be reclaimed automatically as the Message object completes processing
     * @return
     */
    public synchronized MessageFormat detachPayload() {
        if (currentPayload != null) {
            currentPayload.detach();
            return currentPayload;
        } else if (originalPayload != null) {
            originalPayload.detach();
            return originalPayload;
        }
        return null;
    }

    /**
     * Get the first transport header with the given key
     * @param key the header name
     * @return the header value if any or null
     */
    public String getFirstTransportHeader(String key) {
        if (currentTransportHeaders != null) {
            return currentTransportHeaders.get(key);
        } else {
            return originalTransportHeaders.get(key);
        }
    }

    /**
     * This will perform a case insensitive lookup for the first occurrence of the given transport header
     * @param key the case insensitive header name
     * @return the header value if any or null
     */
    public String getFirstTransportHeaderIgnoreCase(String key) {
        String result = getFirstTransportHeader(key);
        if (result == null) {
            Map<String, String> map =
                currentTransportHeaders == null ? originalTransportHeaders : currentTransportHeaders;
            for (Map.Entry<String, String> header : map.entrySet()) {
                if (key.equalsIgnoreCase(header.getKey())) {
                    return header.getValue();
                }
            }
        }
        return result;
    }

    /**
     * Assign the ResponseTrigger for this message. When the message is received over a synchronous transport
     * such as HTTP/S, the response trigger allows the transport to issue the response message as appropriate
     * @param responseTrigger the transport specific response trigger
     */
    public void setResponseTrigger(ResponseTrigger responseTrigger) {
        this.responseTrigger = responseTrigger;
        this.respCorrelateMap.put(CorrelatedProperties.RESPONSE_TRIGGER, responseTrigger);
    }

    /**
     * Get the current response trigger for the message. The response trigger is capable of sending a synchronous
     * response back for this message - e.g. HTTP/S
     * @return the response trigger
     */
    public ResponseTrigger getResponseTrigger() {
        return responseTrigger;
    }

    /**
     * Add a transport header to this message
     * @param key header name
     * @param value header value
     */
    public void addTransportHeader(String key, String value) {
        if (currentTransportHeaders == null) {
            // create a new shallow copy of the original headers - since we only store immutable Strings, this is safe
            currentTransportHeaders = new HashMap<String, String>(originalTransportHeaders);
            currentTransportHeaderDuplicates = originalTransportHeaderDuplicates == null ?
                new HashMap<String, List<String>>() : new HashMap<String, List<String>>(originalTransportHeaderDuplicates);
        }

        if (currentTransportHeaders.containsKey(key)) {
            if (currentTransportHeaderDuplicates == null) {
                currentTransportHeaderDuplicates = new HashMap<String, List<String>>();
            }
            if (!currentTransportHeaderDuplicates.containsKey(key)) {
                currentTransportHeaderDuplicates.put(key, new ArrayList<String>());
            }
            currentTransportHeaderDuplicates.get(key).add(value);
        } else {
            currentTransportHeaders.put(key, value);
        }
    }

    /**
     * Remove all occurrences of the specified transport header
     * @param key header name to remove
     */
    public void removeTransportHeader(String key) {
        if (currentTransportHeaders == null) {
            // create a new shallow copy of the original headers - since we only store immutable Strings, this is safe
            currentTransportHeaders = new HashMap<String, String>(originalTransportHeaders);
            currentTransportHeaderDuplicates = originalTransportHeaderDuplicates == null ?
                new HashMap<String, List<String>>() : new HashMap<String, List<String>>(originalTransportHeaderDuplicates);
        }
        currentTransportHeaders.remove(key);
        currentTransportHeaderDuplicates.remove(key);
    }

    /**
     * Replace the first occurrence of the transport header with the given value
     * @param key the header name
     * @param value the new value
     */
    public void replaceTransportHeader(String key, String value) {
        if (currentTransportHeaders == null) {
            // create a new shallow copy of the original headers - since we only store immutable Strings, this is safe
            currentTransportHeaders = new HashMap<String, String>(originalTransportHeaders);
            currentTransportHeaderDuplicates = originalTransportHeaderDuplicates == null ?
                new HashMap<String, List<String>>() : new HashMap<String, List<String>>(originalTransportHeaderDuplicates);
        }

        currentTransportHeaders.put(key, value);
    }

    /**
     * Get a List of the current transport header values for the given key
     * @return transport headers as a List for the given key
     */
    public List<String> getTransportHeaders(String key) {
        List result = new ArrayList<String>();
        if (currentTransportHeaderDuplicates != null && currentTransportHeaderDuplicates.containsKey(key)) {
            result.addAll(currentTransportHeaderDuplicates.get(key));
        } else if (originalTransportHeaderDuplicates != null && currentTransportHeaderDuplicates.containsKey(key)) {
            result.addAll(originalTransportHeaderDuplicates.get(key));
        }
        if (currentTransportHeaders != null && currentTransportHeaders.containsKey(key)) {
            result.add(currentTransportHeaders.get(key));
        } else if (originalTransportHeaders.containsKey(key)) {
            result.add(originalTransportHeaders.get(key));
        }
        return result;
    }

    /**
     * Get the map of current transport header values ignoring duplicates
     * @return transport headers as a Map, when ignoring duplicates
     */
    public Map<String, String> getTransportHeaders() {
        if (currentTransportHeaders != null) {
            return currentTransportHeaders;
        } else {
            return originalTransportHeaders;
        }
    }

    /**
     * Get the map of current transport header duplicate values
     * @return transport header duplicates as a Map
     */
    public Map<String, List<String>> getDuplicateTransportHeaders() {
        if (currentTransportHeaderDuplicates != null) {
            return currentTransportHeaderDuplicates;
        } else if (originalTransportHeaderDuplicates != null) {
            return originalTransportHeaderDuplicates;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Add a message property - i.e. any Object can be a property
     * @param key property key
     * @param value property value
     */
    public synchronized void addMessageProperty(String key, Object value) {
        if (currentMessageProperties == null) {
            if (originalMessageProperties == null) {
                currentMessageProperties = new HashMap<String, Object>();
            } else {
                currentMessageProperties = new HashMap<String, Object>(originalMessageProperties);
            }
        }
        currentMessageProperties.put(key, value);
    }

    /**
     * Get the message property associated with the specified key
     * @param key property key
     * @return property value
     */
    public synchronized Object getMessageProperty(String key) {
        if (currentMessageProperties == null) {
            if (originalMessageProperties == null) {
                return null;
            } else {
                return originalMessageProperties.get(key);
            }
        } else {
            return currentMessageProperties.get(key);
        }
    }

    /**
     * Remove the message property associated with the specified key
     * @param key property key
     */
    public synchronized void removeMessageProperty(String key) {
        if (currentMessageProperties == null) {
            if (originalMessageProperties != null) {
                originalMessageProperties.remove(key);
            }
        } else {
            currentMessageProperties.remove(key);
        }
    }

    /**
     * Return current message properties as  an unmodifiable map
     * @return current message properties
     */
    public synchronized Map<String, Object> getMessageProperties() {
        if (currentMessageProperties == null) {
            if (originalMessageProperties == null) {
                return Collections.emptyMap();
            } else {
                return Collections.unmodifiableMap(originalMessageProperties);
            }
        } else {
            return Collections.unmodifiableMap(currentMessageProperties);
        }
    }

    /**
     * Is this message a request message?
     * @return true if a request
     */
    public boolean isRequest() {
        return request;
    }

    /**
     * Is this message a response message?
     * @return true if a response
     */
    public boolean isResponse() {
        return !request;
    }

    /**
     * Get the current destination URL for this message. This is the default target address if this message is
     * sent via a default address endpoint
     * @return the destination URL
     */
    public String getDestinationURL() {
        return destinationURL;
    }

    /**
     * Set the destination URL for this message
     * @param destinationURL the new destination URL
     */
    public void setDestinationURL(String destinationURL) {
        this.destinationURL = destinationURL;
    }

    /**
     * Get the content type of the message if set. Note: sometimes the content type maybe available as a transport header
     * @return the content type of the message
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Return the content type of the original request. On a response message, this method returns the content type
     * of the original request
     * @return the original request content type if known, or null
     */
    public String getRequestContentType() {
        return (String) getMessageProperty(CorrelatedProperties.REQ_MSG_CONTENT_TYPE);
    }

    /**
     * Set the content type of the message
     * @param contentType the content type string to be set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
        // save the original request content type
        if (getMessageProperty(CorrelatedProperties.REQ_MSG_CONTENT_TYPE) == null) {
            addMessageProperty(CorrelatedProperties.REQ_MSG_CONTENT_TYPE, contentType);
            addResponseCorrelation(CorrelatedProperties.REQ_MSG_CONTENT_TYPE, contentType);
        }
    }

    /**
     * Get the id of the proxy service to which this message belongs
     * @return the id of the owning proxy service
     */
    public String getProxyServiceID() {
        return proxyService.getId();
    }

    /**
     * Get the proxy service to which this message belongs
     * @return the owning proxy service
     */
    public ProxyService getProxyService() {
        return proxyService;
    }

    /**
     * Get the map of elements that are passed on to a response message, and thus correlated to the original message
     * @return the correlation map
     */
    public Map<String, Object> getRespCorrelateMap() {
        return respCorrelateMap;
    }

    /**
     * Add an entry for correlation into the response message for this message. Correlation is allowed for String values
     * @param key correlation map key
     * @param value value to correlate
     */
    public void addResponseCorrelation(String key, Object value) {
        respCorrelateMap.put(key, value);
    }

    /**
     * Get the UUID of the message
     * @return the UUID of this message
     */
    public UUID getMessageUUID() {
        return messageUUID;
    }

    /**
     * Add an Exception that this message encountered
     * @param e the Exception
     */
    public void addException(Exception e) {
        if (errorList == null) {
            errorList = new ArrayList<ErrorInfo>(1);
        }
        errorList.add(new ErrorInfo(e));
    }

    /**
     * Attach an error that this message encountered
     * @param e the ErrorInfo with optional error code and message
     */
    public void addException(ErrorInfo e) {
        if (errorList == null) {
            errorList = new ArrayList<ErrorInfo>(1);
        }
        errorList.add(e);
    }

    /**
     * Get a list of errors this message encountered
     * @return list of errors linked to this message
     */
    public List<ErrorInfo> getExceptions() {
        return errorList;
    }

    /**
     * Get the last error encountered and linked to this message
     * @return the last error if any, or null
     */
    public ErrorInfo getLastException() {
        if (errorList != null && !errorList.isEmpty()) {
            return errorList.get(errorList.size()-1);
        }
        return null;
    }

    /**
     * The request message UUID for a response message
     * @return the correlated request message UUID
     */
    public UUID getCorrelatedRequestUUID() {
        return correlatedRequestUUID;
    }

    /**
     * The parent message UUID
     * @return The parent message UUID
     */
    public UUID getParentMessageUUID() {
        return parentMessageUUID;
    }

    /**
     * Assign a Spring PlatformTransactionManager to this message
     * @param txnManager the platform transaction manager
     */
    public void setTxnManager(PlatformTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    /**
     * Get the Spring TransactionStatus for this message
     * @return the status of the transaction
     */
    public TransactionStatus getTxnStatus() {
        return txnStatus;
    }

    /**
     * Set the TransactionStatus for this message
     * @param txnStatus the Spring TransactionStatus
     */
    public void setTxnStatus(TransactionStatus txnStatus) {
        this.txnStatus = txnStatus;
    }

    /**
     * Register the given id as a fault handler of this message, to the fault handler stack
     * @param id the fault handler pushed
     */
    public void pushFaultHandlerId(String id) {
        if (faultHandlerStack == null) {
            faultHandlerStack = new Stack<String>();
        }
        faultHandlerStack.add(id);

        if (logger.isDebugEnabled()) {
            logger.debug("Messgae : " + messageUUID + " added : " + id + " to its fault stack");
        }
    }

    /**
     * Pop the id of the most recent fault handler for this message
     * @return id of the fault handler popped from the stack
     */
    public String popFaultHandlerId() {
        if (faultHandlerStack == null) {
            logger.debug("No error handler defined for Proxy Service : " + proxyService.getId() +
                " message [" + messageUUID + "] last Exception : " + getLastException());
        } else if (!faultHandlerStack.isEmpty()) {
            String top = faultHandlerStack.pop();
            if (logger.isDebugEnabled()) {
                logger.debug("Messgae : " + messageUUID + " returning : " + top + " from top of fault stack");
            }
            return top;
        }
        return null;
    }

    /**
     * Remove the specified fault handler - on completion of its scope
     * @param id the id of the fault handler to remove
     */
    public void removeFaultHandlerId(String id) {
        if (faultHandlerStack != null) {
            faultHandlerStack.remove(id);
        }
    }

    /**
     * Associate a MessageFile (a temporary file) for release back to the FileCache, once this message completes
     * @param mf the MessageFile to be registered for removal
     */
    public void addMessageFileForRelease(MessageFile mf) {
        if (messageFilesToRelease == null) {
            messageFilesToRelease = new HashSet<MessageFile>();
        }
        messageFilesToRelease.add(mf);
    }

    /**
     * Link a MessageFormat (i.e. payload) for GC along with this message
     * @param mf the payload to link
     */
    public void addMessageFormatForRelease(MessageFormat mf) {
        if (messageFormatInstancesToRelease == null) {
            messageFormatInstancesToRelease = new HashSet<MessageFormat>();
        }
        messageFormatInstancesToRelease.add(mf);
    }

    /**
     * Link a Message for GC along with this message
     * @param m the Message to link
     */
    public void addMessageForRelease(Message m) {
        MessageImpl msg = (MessageImpl) m;
        if (messagesToRelease == null) {
            messagesToRelease = new HashSet<MessageImpl>();
        }
        messagesToRelease.add(msg);
    }

    //----------- constructors and destructors -----------

    /**
     * Create a new Message for the specified ProxyService. The message may be marked as a request or response
     * @param request true to indicate a new request message
     * @param proxyService the target proxy service
     * @param originatingTransport the transport creating this message instance
     */
    public MessageImpl(boolean request, ProxyService proxyService, String originatingTransport) {
        if (logger.isDebugEnabled()) {
            logger.debug("Created message with UUID : " + this.messageUUID);
        }
        this.request = request;
        this.proxyService = proxyService;
        this.correlatedRequestUUID = null;
        this.originatingTransport = originatingTransport;

        this.respCorrelateMap.put(CorrelatedProperties.REQ_MSG_UUID, messageUUID);
        this.respCorrelateMap.put(CorrelatedProperties.REQ_MSG_PS, proxyService);
    }

    public static MessageImpl from(CompositeData cd) {
        return new MessageImpl(
                (Boolean) cd.get("request"),
                (ProxyService) cd.get("proxyService"),
                (String) cd.get("originatingTransport"));
    }

    /**
     * Create a new response message using the specified correlation properties
     * @param respCorrelationMap Map of correlation information with the request - this must include the proxy service via MessageImpl.REQ_MSG_PS
     * @param originatingTransport the transport creating this message instance
     */
    public MessageImpl(Map<String, Object> respCorrelationMap, String originatingTransport) {
        if (logger.isDebugEnabled()) {
            logger.debug("Created response message with UUID : " + this.messageUUID);
        }
        this.request = false;
        this.originalMessageProperties = respCorrelationMap;
        this.proxyService = (ProxyService) respCorrelationMap.get(CorrelatedProperties.REQ_MSG_PS);

        this.originatingTransport = originatingTransport;
        this.responseTrigger = (ResponseTrigger) respCorrelationMap.get(CorrelatedProperties.RESPONSE_TRIGGER);
        this.correlatedRequestUUID = (UUID) respCorrelationMap.get(CorrelatedProperties.REQ_MSG_UUID);
        this.completionHandler = (CompletionHandler) respCorrelationMap.get(CorrelatedProperties.COMPLETION_HANDLER);

        // make any responses correlate back to this
        this.respCorrelateMap.put(CorrelatedProperties.REQ_MSG_UUID, messageUUID);
        this.respCorrelateMap.put(CorrelatedProperties.REQ_MSG_PS, proxyService);
        this.respCorrelateMap.put(CorrelatedProperties.RESPONSE_TRIGGER, responseTrigger);
    }

    /**
     * Create a new Message for the specified ProxyService, by cloning the message passed
     * @param msg the message to clone from
     * @param proxyService the target proxy service
     * @param originatingTransport the transport creating this message instance
     */
    public MessageImpl(MessageImpl msg, ProxyService proxyService, String originatingTransport) {
        if (logger.isDebugEnabled()) {
            logger.debug("Created message with UUID : " + this.messageUUID + " by cloning message : " + msg.getMessageUUID());
        }
        this.request = msg.isRequest();
        this.proxyService = proxyService;
        this.correlatedRequestUUID = msg.correlatedRequestUUID;
        this.originatingTransport = originatingTransport;
        for (Map.Entry<String, Object> e : msg.respCorrelateMap.entrySet()) {
            if (!CorrelatedProperties.REQ_MSG_PS.equals(e.getKey())) {
                this.respCorrelateMap.put(e.getKey(), e.getValue());
            }
        }
        this.respCorrelateMap.put(CorrelatedProperties.REQ_MSG_PS, proxyService);
    }

    /**
     * Clone the current message - without transport headers, properties or payload
     * @return the cloned message
     */
    public Message cloneMessage() {
        /** BE CAREFUL with what refers to "msg" and what refers to "this" */
        MessageImpl msg = new MessageImpl(this, this.proxyService, this.originatingTransport);
        msg.setWorkManager(this.workManager);
        msg.parentMessageUUID = this.messageUUID;
        msg.respCorrelateMap.put(CorrelatedProperties.REQ_MSG_UUID, msg.parentMessageUUID);
        msg.responseTrigger = this.responseTrigger;
        msg.respCorrelateMap.put(CorrelatedProperties.RESPONSE_TRIGGER, msg.responseTrigger);
        msg.faultHandlerStack = this.faultHandlerStack;
        return msg;
    }

    /**
     * Create the default response message to this message
     * @return the response message created for this message
     */
    public MessageImpl createDefaultResponseMessage() {
        MessageImpl m = new MessageImpl(respCorrelateMap, originatingTransport);
        m.setWorkManager(workManager);
        return m;
    }

    /**
     * Drop the transport sender selector from the current destinationURL if one exists
     */
    public void prepareDestinationURL() {
        int endPos = destinationURL.indexOf('}');
        if (endPos != -1) {
            this.destinationURL = destinationURL.substring(endPos+1);
        }
    }

    /**
     * Release resourced linked to this message
     */
    public synchronized void releaseResources() {

        logger.debug("Releasing resources of message {}", messageUUID);

        workManager.handleCompletion(this);
        Set<Long> releasedFiles = new HashSet<Long>();

        if (currentPayload != null && !currentPayload.isDetached()) {
            currentPayload.releaseResources();
            if (currentPayload instanceof RawFileMessage) {
                File file = ((RawFileMessage) currentPayload).getFile();
                if (file instanceof MessageFile) {
                    releasedFiles.add(((MessageFile) file).getId());
                }
            }
            currentPayload = null;
        }

        if (originalPayload != null && !originalPayload.isDetached()) {
            if (originalPayload instanceof RawFileMessage) {
                File file = ((RawFileMessage) originalPayload).getFile();
                if (file instanceof MessageFile) {
                    long id = ((MessageFile) file).getId();
                    if (!releasedFiles.contains(id)) {
                        originalPayload.releaseResources();
                        releasedFiles.add(id);
                    }
                } else {
                    originalPayload.releaseResources();
                }
            } else {
                originalPayload.releaseResources();
            }
            originalPayload = null;
        }

        if (messageFormatInstancesToRelease != null) {
            for (MessageFormat messageFormat : messageFormatInstancesToRelease) {
                if (messageFormat instanceof RawFileMessage) {
                    File file = ((RawFileMessage) messageFormat).getFile();
                    if (file instanceof MessageFile) {
                        long id = ((MessageFile) file).getId();
                        if (!releasedFiles.contains(id)) {
                            messageFormat.releaseResources();
                            releasedFiles.add(id);
                        }
                    } else {
                        messageFormat.releaseResources();
                    }
                } else {
                    messageFormat.releaseResources();
                }
            }
        }
        messageFormatInstancesToRelease = null;

        if (messageFilesToRelease != null) {
            for (MessageFile messageFile : messageFilesToRelease) {
                long id = messageFile.getId();
                if (!releasedFiles.contains(id)) {
                    messageFile.release();
                    releasedFiles.add(id);
                }
            }
        }
        messageFilesToRelease = null;

        if (messagesToRelease != null) {
            for (MessageImpl m : messagesToRelease) {
                logger.debug("Release linked message : {}", m.getMessageUUID());
                m.releaseResources();
            }
        }
        messagesToRelease = null;

        respCorrelateMap = null;
        currentMessageProperties = null;
        originalMessageProperties = null;
        currentTransportHeaders = null;
        originalTransportHeaders = null;
        attachments = null;
    }

    //------------ Advanced methods -------------------

    /**
     * Begin a new Spring transaction and associate its context with this message
     *
     * @param transactionManagerBeanId the Spring bean ID of the transaction manager
     */
    public void beginTransaction(String transactionManagerBeanId) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txnManager = getMediation().getSpringBean(transactionManagerBeanId, PlatformTransactionManager.class);
        txnStatus = txnManager.getTransaction(def);
    }

    /**
     * Invoked typically in a request flow to suspend the active transaction, and to save its state
     * into the response correlation map
     * @param tmBeanId the Spring bean ID of the transaction manager
     */
    public void suspendTransaction(String tmBeanId) {

        logger.debug("Requesting transaction suspension for message : {} from TM : {}", messageUUID, tmBeanId);

        // Prevent a SpringPlatformTransactionManager from committing
        TransactionInformation.skipCommit.set(Boolean.TRUE);

        // suspend and save synchronizations
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            List syncs = TransactionSynchronizationManager.getSynchronizations();
            List<Object> savedSyncs = new ArrayList<Object>();
            for (Object o : syncs) {
                savedSyncs.add(o);
                TransactionSynchronization s = (TransactionSynchronization) o;
                s.suspend();
            }
            respCorrelateMap.put(CorrelatedProperties.TXN_SYNCS, savedSyncs);
            TransactionSynchronizationManager.clear();
        }

        // save transaction resources - used for non-JTA suspension
        Map<Object, Object> resources = TransactionSynchronizationManager.getResourceMap();
        if (resources != null) {
            Map<Object, Object> savedResources = new HashMap<Object, Object>();
            for (Map.Entry e : resources.entrySet()) {
                savedResources.put(e.getKey(), e.getValue());
                TransactionSynchronizationManager.unbindResource(e.getKey());
            }
            respCorrelateMap.put(CorrelatedProperties.TXN_RESOURCES, savedResources);
        }

        // suspend transaction
        respCorrelateMap.put(CorrelatedProperties.TXN_STATUS, txnStatus);
        PlatformTransactionManager ptm = getMediation().getSpringBean(
            tmBeanId, PlatformTransactionManager.class);
        respCorrelateMap.put(CorrelatedProperties.TXN_MANAGER, ptm);

        // if the transaction manager is our TM, get the parent TM
        if (ptm instanceof SpringPlatformTransactionManager) {
            ptm = ((SpringPlatformTransactionManager) ptm).getTxnManager();
        }

        if (ptm instanceof JtaTransactionManager) {
            try {
                addResponseCorrelation(CorrelatedProperties.JTA_TXN,
                    ((JtaTransactionManager) ptm).getTransactionManager().suspend());
            } catch (SystemException e) {
                handleException("Error suspending JTA transaction", e);
            }
        }
    }

    /**
     * To be invoked by an asynchronous response flow to re-energize a suspended transaction
     *
     * Note. This functionality may be available with only some JTA transaction managers - esp when resume happens
     * on a different thread.
     *
     * Refer the JTA spec 3.2.3: "Note that some transaction manager implementations allow a suspended transaction to
     * be resumed by a different thread. This feature is not required by JTA"
     */
    public void resumeTransaction() {

        logger.debug("Requesting transaction resumption for message : {} from TM : {}", messageUUID);
        Map<String, Object> propertyBag = null;

        // detect if we want to resume from same request or asynchronous response
        txnStatus = (TransactionStatus) originalMessageProperties.get(CorrelatedProperties.TXN_STATUS);
        if (txnStatus != null) {
            propertyBag = originalMessageProperties;
        } else {
            propertyBag = respCorrelateMap;
            txnStatus = (TransactionStatus) propertyBag.get(CorrelatedProperties.TXN_STATUS);
        }

        // get transaction manager, and synchronizations reloaded
        txnManager = (PlatformTransactionManager) propertyBag.get(CorrelatedProperties.TXN_MANAGER);

        List syncs = (List) propertyBag.get(CorrelatedProperties.TXN_SYNCS);
        if (syncs != null) {
            TransactionSynchronizationManager.initSynchronization();
            for (Object o : syncs) {
                TransactionSynchronization s = (TransactionSynchronization) o;
                s.resume();
                TransactionSynchronizationManager.registerSynchronization(s);
            }
        }

        // restore transaction resources for non-JTA
        Map<String, Object> txnResources = (Map) propertyBag.get(CorrelatedProperties.TXN_RESOURCES);
        if (txnResources != null) {
            for (Map.Entry<String, Object> e : txnResources.entrySet()) {
                TransactionSynchronizationManager.bindResource(e.getKey(), e.getValue());
            }
        }

        // resume JTA transaction
        PlatformTransactionManager ptm = txnManager;

        // if the transaction manager is our TM, get the parent TM
        if (ptm instanceof SpringPlatformTransactionManager) {
            ptm = ((SpringPlatformTransactionManager) ptm).getTxnManager();
        }

        if (ptm instanceof JtaTransactionManager) {
            try {
                ((JtaTransactionManager) ptm).getTransactionManager().resume(
                    (Transaction) getMessageProperty(CorrelatedProperties.JTA_TXN));
            } catch (Exception e) {
                handleException("Error resuming JTA transaction", e);
            }
        }

        // prevent a SpringPlatformTransactionManager from skipping this transaction
        if (TransactionInformation.skipCommit.get() != null) {
            TransactionInformation.skipCommit.remove();
        }
    }

    /**
     * To be invoked by an asynchronous response flow to re-energize a suspended transaction and commit it
     *
     * See resumeTransaction()
     */
    public void resumeAndCommitTransaction() {
        resumeTransaction();
        commitTransaction();
    }

    /**
     * To be invoked by an asynchronous response flow to re-energize a suspended transaction and commit it
     *
     * See resumeTransaction()
     */
    public void resumeAndRollbackTransaction() {
        resumeTransaction();
        rollbackTransaction();
    }

    /**
     * Invoked to request a rollback of the current transaction
     */
    public void rollbackTransaction() {
        if (txnStatus != null && txnManager != null) {
            txnManager.rollback(txnStatus);
            if (logger.isDebugEnabled()) {
                logger.debug("Transaction for message : " + messageUUID + " rolled back");
            }
        } else {
            logger.warn("No transaction for rollback detected");
        }
    }

    /**
     * Invoked to request a commit of the current transaction
     */
    public void commitTransaction() {
        if (txnStatus != null && txnManager != null) {
            txnManager.commit(txnStatus);
            if (logger.isDebugEnabled()) {
                logger.debug("Transaction for message : " + messageUUID + " committed");
            }
        } else {
            logger.warn("No transaction for commit detected");
        }
    }

    /**
     * Begin a transaction using the default (i.e. only) TransactionManager defined in the configuration
     */
    public void beginTransaction() {
        String tmBeanId = getMediation().getDefaultPlatformTM();
        if (tmBeanId != null) {
            beginTransaction(tmBeanId);
        } else {
            handleException("Cannot begin a transaction as the transaction manager bean cannot be detected. " +
                "Please pass the bean ID");
        }
    }

    /**
     * Suspend a transaction using the default (i.e. only) TransactionManager defined in the configuration
     */
    public void suspendTransaction() {
        String tmBeanId = getMediation().getDefaultPlatformTM();
        if (tmBeanId != null) {
            suspendTransaction(tmBeanId);
        } else {
            handleException("Cannot suspend the transaction as the transaction manager bean cannot be detected. " +
                "Please pass the bean ID");
        }
    }

    /**
     * Return a reference to Mediation utilities
     * @return a reference to Mediation
     */
    public Mediation getMediation() {
        return MediationImpl.getInstance();
    }

    /**
     * Get originating transport
     * @return originating transport
     */
    public String getOriginatingTransport() {
        return originatingTransport;
    }

    /**
     * Return the address value (e.g. Prefix, URL etc) to which the corresponding request message, or this message was last sent
     * @return the address value which generated this response, or the last address to which this message was sent
     */
    public String getLastAddressValue() {
        return (String) getMessageProperty(Endpoint.LAST_ADDRESS_VALUE);
    }

    /**
     * Return the absolute URL to which the corresponding request message, or this message was last sent
     * @return the absolute URL which generated this response, or the last absolute URL to which this message was sent
     */
    public String getLastSentURL() {
        return (String) getMessageProperty(Endpoint.LAST_SENT_URL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageImpl message = (MessageImpl) o;

        if (!messageUUID.equals(message.messageUUID)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return messageUUID.hashCode();
    }

    private void handleException(String msg, Exception e) {
        logger.error(msg, e);
        throw new BusRuntimeException(msg, e);
    }

    private void handleException(String msg) {
        logger.error(msg);
        throw new BusRuntimeException(msg);
    }
}

