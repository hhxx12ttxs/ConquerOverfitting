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

package org.mobicents.protocols.smpp.message;

import java.io.IOException;

import org.mobicents.protocols.smpp.Address;
import org.mobicents.protocols.smpp.message.tlv.TLVTable;
import org.mobicents.protocols.smpp.message.tlv.Tag;
import org.mobicents.protocols.smpp.util.PacketDecoder;
import org.mobicents.protocols.smpp.util.PacketEncoder;
import org.mobicents.protocols.smpp.util.SMPPDate;
import org.mobicents.protocols.smpp.version.SMPPVersion;

/**
 * Submit a broadcast message to the SMSC.
 * @version $Id: BroadcastSM.java 457 2009-01-15 17:37:42Z orank $
 * @since 0.4.0
 */
public class BroadcastSM extends SMPPPacket {
    private static final long serialVersionUID = 2L;
    
    private String serviceType;
    private Address source;
    private String messageId;
    private int priority;
    private SMPPDate deliveryTime;
    private SMPPDate expiryTime;
    private int replaceIfPresent;
    private int dataCoding;
    private int defaultMsg;
    
    public BroadcastSM() {
        super (CommandId.BROADCAST_SM);
    }

    public int getDataCoding() {
        return dataCoding;
    }

    public void setDataCoding(int dataCoding) {
        this.dataCoding = dataCoding;
    }

    public int getDefaultMsg() {
        return defaultMsg;
    }

    public void setDefaultMsg(int defaultMsg) {
        this.defaultMsg = defaultMsg;
    }

    public SMPPDate getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(SMPPDate deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public SMPPDate getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(SMPPDate expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getReplaceIfPresent() {
        return replaceIfPresent;
    }

    public void setReplaceIfPresent(int replaceIfPresent) {
        this.replaceIfPresent = replaceIfPresent;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Address getSource() {
        return source;
    }

    public void setSource(Address source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            BroadcastSM other = (BroadcastSM) obj;
            equals |= safeCompare(serviceType, other.serviceType);
            equals |= safeCompare(source, other.source);
            equals |= safeCompare(messageId, other.messageId);
            equals |= priority == other.priority;
            equals |= safeCompare(deliveryTime, other.deliveryTime);
            equals |= safeCompare(expiryTime, other.expiryTime);
            equals |= replaceIfPresent == other.replaceIfPresent;
            equals |= dataCoding == other.dataCoding;
            equals |= defaultMsg == other.defaultMsg;
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        int hc = super.hashCode();
        hc += (serviceType != null) ? serviceType.hashCode() : 13;
        hc += (source != null) ? source.hashCode() : 13;
        hc += (messageId != null) ? messageId.hashCode() : 13;
        hc += Integer.valueOf(priority).hashCode();
        hc += (deliveryTime != null) ? deliveryTime.hashCode() : 13;
        hc += (expiryTime != null) ? expiryTime.hashCode() : 13;
        hc += Integer.valueOf(replaceIfPresent).hashCode();
        hc += Integer.valueOf(dataCoding).hashCode();
        hc += Integer.valueOf(defaultMsg).hashCode();
        return hc;
    }
    
    @Override
    protected void readMandatory(PacketDecoder decoder) {
        serviceType = decoder.readCString();
        source = decoder.readAddress();
        messageId = decoder.readCString();
        priority = decoder.readUInt1();
        deliveryTime = decoder.readDate();
        expiryTime = decoder.readDate();
        replaceIfPresent = decoder.readUInt1();
        dataCoding = decoder.readUInt1();
        defaultMsg = decoder.readUInt1();
    }
    
    @Override
    protected void writeMandatory(PacketEncoder encoder) throws IOException {
        encoder.writeCString(serviceType);
        encoder.writeAddress(source);
        encoder.writeCString(messageId);
        encoder.writeUInt1(priority);
        encoder.writeDate(deliveryTime);
        encoder.writeDate(expiryTime);
        encoder.writeUInt1(replaceIfPresent);
        encoder.writeUInt1(dataCoding);
        encoder.writeUInt1(defaultMsg);
    }
    
    @Override
    protected int getMandatorySize() {
        int length = 6;
        length += sizeOf(serviceType);
        length += sizeOf(source);
        length += sizeOf(messageId);
        length += sizeOf(deliveryTime);
        length += sizeOf(expiryTime);
        return length;
    }
    
    @Override
    protected void validateMandatory(SMPPVersion smppVersion) {
        smppVersion.validateServiceType(serviceType);
        smppVersion.validateAddress(source);
        smppVersion.validateMessageId(messageId);
        smppVersion.validatePriorityFlag(priority);
        smppVersion.validateReplaceIfPresent(replaceIfPresent);
        smppVersion.validateDataCoding(dataCoding);
        smppVersion.validateDefaultMsg(defaultMsg);
    }
    
    protected boolean validateTLVTable(SMPPVersion version) {
        boolean valid = true;
        TLVTable tlvTable = getTLVTable();
        valid &= tlvTable.containsKey(Tag.BROADCAST_AREA_IDENTIFIER);
        valid &= tlvTable.containsKey(Tag.BROADCAST_CONTENT_TYPE);
        valid &= tlvTable.containsKey(Tag.BROADCAST_REP_NUM);
        valid &= tlvTable.containsKey(Tag.BROADCAST_FREQUENCY_INTERVAL);
        return valid;
    }
}

