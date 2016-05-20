/**
 * Copyright (C) 2010 Alfredo Morresi
 * 
 * This file is part of SmsForFree project.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */

package it.rainbowbreeze.smsforfree.domain;

import android.content.Intent;

/**
 * A simple message representation
 * 
 * @author Alfredo "Rainbowbreeze" Morresi
 *
 */
public class TextMessage {
    //---------- Private fields

    
    //---------- Constructors
    protected TextMessage() {
        super();
    }
    
    
    
    
    //---------- Public properties
    //names of db columns
    public static final String TABLE_NAME = "TextMessage";
    public static final String FIELD_ID = "ID";
    public static final String FIELD_DESTINATION = "DESTINATION";
    public static final String FIELD_MESSAGE = "MESSAGE";
    public static final String FIELD_PROVIDERID = "PROVIDERID";
    public static final String FIELD_SERVICEID = "SERVICEID";
    public static final String FIELD_PROCESSING_STATUS = "PROCESSING_STATUS";
    public static final String FIELD_SEND_INTERVAL = "SEND_INTERVAL";
    public static final String DEFAULT_SORT_ORDER = "ID ASC";
    
    public static final int PROCESSING_NONE = 0;
    public static final int PROCESSING_QUEUED = 1;
    public static final int PROCESSING_SENT = 2;
    public static final int PROCESSING_ERROR_SENDING = 3;
    
    
    protected long mId;
    public long getId()
    { return mId; }
    public TextMessage setId(long newValue)
    { mId = newValue; return this; }

    protected String mDestination;
    public String getDestination()
    { return mDestination; }
    public TextMessage setDestination(String newValue)
    { mDestination = newValue; return this; }

    protected String mMessage;
    public String getMessage()
    { return mMessage; }
    public TextMessage setMessage(String newValue)
    { mMessage = newValue; return this; }
    
    protected String mProviderId;
    public String getProviderId()
    { return mProviderId; }
    public TextMessage setProviderId(String newValue)
    { mProviderId = newValue; return this; }

    protected String mServiceId;
    public String getServiceId()
    { return mServiceId; }
    public TextMessage setServiceId(String newValue)
    { mServiceId = newValue; return this; }

    protected int mProcessingStatus;
    public int getProcessingStatus()
    { return mProcessingStatus; }
    public TextMessage setProcessingStatus(int newValue)
    { mProcessingStatus = newValue; return this; }
    
    protected long mSendInterval;
    public long getSendInterval()
    { return mSendInterval; }
    public TextMessage setSendInterval(long newValue)
    { mSendInterval = newValue; return this; }


    //---------- Public methods
    
    /**
     * Creates new text messages
     */
    public static class Factory {
        public static TextMessage create(
                long id,
                String destination,
                String message,
                String providerId,
                String serviceId,
                long sendInterval,
                int processingStatus) {
            TextMessage textMessage = new TextMessage()
                .setId(id)
                .setDestination(destination)
                .setMessage(message)
                .setProviderId(providerId)
                .setServiceId(serviceId)
                .setSendInterval(sendInterval)
                .setProcessingStatus(processingStatus);
            
            return textMessage;
        }

        public static TextMessage create() {
            TextMessage textMessage = new TextMessage()
                .setProcessingStatus(PROCESSING_NONE);
            
            return textMessage;
        }
        
        public static TextMessage create(Intent intent) {
            TextMessage textMessage = new TextMessage();
            textMessage.deserializeFromIntent(intent);
            return textMessage;
        }
    }

    /**
     * Serialize the message inside an intent
     * 
     * @param intent the intent where serialize the message. {@code null} to create a new intent
     * @return the intent with the result
     */
    public Intent serializeToIntent(Intent intent) {
        if (null == intent) intent = new Intent();
        
        intent.putExtra(FIELD_ID, getId());
        intent.putExtra(FIELD_DESTINATION, getDestination());
        intent.putExtra(FIELD_MESSAGE, getMessage());
        intent.putExtra(FIELD_PROVIDERID, getProviderId());
        intent.putExtra(FIELD_SERVICEID, getServiceId());
        intent.putExtra(FIELD_SEND_INTERVAL, getSendInterval());
        intent.putExtra(FIELD_PROCESSING_STATUS, getProcessingStatus());
        return intent;
    }

    /**
     * Retrieves message values from an intent
     * 
     * @param intent
     */
    public void deserializeFromIntent(Intent intent) {
        if (null == intent) return;
        
        setId(intent.getLongExtra(FIELD_ID, 0));
        setDestination(intent.getStringExtra(FIELD_DESTINATION));
        setMessage(intent.getStringExtra(FIELD_MESSAGE));
        setProviderId(intent.getStringExtra(FIELD_PROVIDERID));
        setServiceId(intent.getStringExtra(FIELD_SERVICEID));
        setSendInterval(intent.getLongExtra(FIELD_SEND_INTERVAL, 0));
        setProcessingStatus(intent.getIntExtra(FIELD_PROCESSING_STATUS, PROCESSING_NONE));
    }
    
    
    
    //---------- Private methods
}

