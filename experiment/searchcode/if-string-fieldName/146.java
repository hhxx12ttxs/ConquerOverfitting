package com.brokerexpress.gate.plaza2.impl;

import com.brokerexpress.gate.plaza2.Plaza2Api;
import com.brokerexpress.gate.plaza2.Plaza2Callbacks;
import com.brokerexpress.gate.plaza2.PlazaException;
import com.brokerexpress.gate.plaza2.PlazaMessage;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: brokerexpress
 * Date: 3/1/11
 * Time: 9:16 PM
*/
public class PlazaMessageImpl implements PlazaMessage {
	private static final Logger logger = LoggerFactory.getLogger(PlazaMessage.class);
	Pointer messageRef;
	//private PlazaMessageFactoryImpl messageFactory;
	String messageName;
	boolean released = false;
	private PlazaConnectionImpl plazaConnection;


	public PlazaMessageImpl() throws PlazaException {}

	public PlazaMessageImpl(Pointer messageRef) throws PlazaException {
		this.messageRef = messageRef;
	}

	public void initialize() throws PlazaException {
		//messageRef = Plaza2Api.INSTANCE.createMessage(messageFactory.getFactoryRef(), messageName);
	}

	public void setPlazaConnection(PlazaConnectionImpl plazaConnection) {
		this.plazaConnection = plazaConnection;
	}

	/*public void setMessageFactory(PlazaMessageFactoryImpl messageFactory) {
		this.messageFactory = messageFactory;
	}*/

	public void release() throws PlazaException {
		Plaza2Api.INSTANCE.releaseMessage(messageRef);
		released = true;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!released)
			release();
	}


	public Pointer getMessageRef() {
		return messageRef;
	}

	@Override
	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	@Override
	public void setField(String name, int value) throws PlazaException {
		Plaza2Api.INSTANCE.setMessageField(messageRef, name, Integer.toString(value));
	}

	@Override
	public void setField(String name, long value) throws PlazaException  {
		Plaza2Api.INSTANCE.setMessageField(messageRef, name, Long.toString(value));
	}

	@Override
	public void setField(String name, String value) throws PlazaException  {
		Plaza2Api.INSTANCE.setMessageField(messageRef, name, value);
	}

	@Override
	public void setField(String name, double value) throws PlazaException  {
		Plaza2Api.INSTANCE.setMessageField(messageRef, name, Double.toString(value));
	}

	@Override
	public void send( long timeout)  throws PlazaException{
		Plaza2Api.INSTANCE.sendMessage(messageRef, plazaConnection.getConnectionRef(), timeout);
	}

	@Override
	public void sendAsync(long timeout) throws PlazaException {
		Plaza2Api.INSTANCE.sendMessageAsync(messageRef, plazaConnection.getConnectionRef(), timeout, asyncReplyCallback);
	}

	@Override
	public void sendAsync(long timeout, Plaza2Callbacks.SendMessageAsyncCallback callback) throws PlazaException {
		Plaza2Api.INSTANCE.sendMessageAsync(messageRef, plazaConnection.getConnectionRef(), timeout, callback);
	}

	public void asyncReplyReceived(long errorCode) {
		//TODO: not implemented
	}

	public String 	readString(String fieldName) throws PlazaException {
		return Plaza2Api.INSTANCE.readMessageString(messageRef, fieldName);
	}

	public int 	readInt(String fieldName) throws PlazaException{
		return Plaza2Api.INSTANCE.readMessageInt(messageRef, fieldName);
	}
	public long 	readLong(String fieldName) throws PlazaException{
		return Plaza2Api.INSTANCE.readMessageLong(messageRef, fieldName);
	}
	public double readDouble(String fieldName) throws PlazaException{
		return Plaza2Api.INSTANCE.readMessageDouble(messageRef, fieldName);
	}


	private final  Plaza2Callbacks.SendMessageAsyncCallback asyncReplyCallback = new Plaza2Callbacks.SendMessageAsyncCallback() {
		@Override
		public void callback(Pointer messageId, long errorCode) {
			try{
				asyncReplyReceived(errorCode);
			}catch(Exception e){
				logger.error("Async callback error", e);
			}
		}
	};

}

