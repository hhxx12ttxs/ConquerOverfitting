package com.brokerexpress.gate.plaza2.impl.api;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;
import com.brokerexpress.gate.plaza2.Plaza2Api;
import com.brokerexpress.gate.plaza2.Plaza2Callbacks;
import com.brokerexpress.gate.plaza2.PlazaException;

import java.math.BigDecimal;

/**
 * User: brokerexpress
 * Date: 2/22/11
 * Time: 1:45 PM
 */
public class Plaza2DirectApiWrapper extends Plaza2ApiWrapper implements Plaza2Api {
    public Pointer createConnection(String host, int port, String app, String password, int timeout) throws PlazaException {
        PointerByReference conIdRef = new PointerByReference();
        checkError(Plaza2DirectApi.createConnection(host, port, app, password, timeout, conIdRef));
        return conIdRef.getValue();
    }

	public void openConnection(Pointer id) throws PlazaException {
        checkError(Plaza2DirectApi.openConnection(id));
    }

	public void closeConnection(Pointer id) throws PlazaException {
        checkError(Plaza2DirectApi.closeConnection(id));
    }

	public void processMessages(Pointer id) throws PlazaException {
        checkError(Plaza2DirectApi.processMessages(id));
    }


	public int getConnectionState(Pointer id) throws PlazaException {
		IntByReference stateRef = new IntByReference();
        checkError(Plaza2DirectApi.getConnectionState(id, stateRef));
        return  stateRef.getValue();
    }

	public Pointer createStream(String streamName, int mode, String schemaFile, String schemaName) throws PlazaException {
       PointerByReference streamIdRef = new PointerByReference();
        checkError(Plaza2DirectApi.createStream(streamName, mode, schemaFile, schemaName, streamIdRef));
        return  streamIdRef.getValue();

    }

	public void openStream(Pointer id, Pointer connection) throws PlazaException {
        checkError(Plaza2DirectApi.openStream(id, connection));
    }

	public void closeStream(Pointer id) throws PlazaException {
        checkError(Plaza2DirectApi.closeStream(id));
    }

	public void setStreamLifeNum(Pointer id, long lifenum) throws PlazaException {
        checkError(Plaza2DirectApi.setStreamLifeNum(id, lifenum));
    }

	public void setStreamTableRev(Pointer id, String table, long rev) throws PlazaException {
        checkError(Plaza2DirectApi.setStreamTableRev(id, table, rev));
    }

	public int getStreamState(Pointer id) throws PlazaException {
        IntByReference stateRef = new IntByReference();
        checkError(Plaza2DirectApi.getStreamState(id, stateRef));
        return  stateRef.getValue();
    }

	public String getStreamTableSchema(Pointer streamId, String tableName) throws PlazaException {
        Memory mem = new Memory(4048);
        checkError(Plaza2DirectApi.getStreamTableSchema(streamId, tableName, mem));
        return mem.getString(0);
    }

    @Override
    public String getStreamTableName(Pointer streamId, int tableIndex) throws PlazaException {
        Memory mem = new Memory(250);
        checkError(Plaza2DirectApi.getStreamTableName(streamId, tableIndex, mem));
        return mem.getString(0);
    }

    //public void addStreamTable(int streamId, String tableName, String schema);
	public void registerStreamDataInsertedCallback(Pointer id, Plaza2Callbacks.StreamDataInsertedCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamDataInsertedCallback(id, callback));
    }
	public void registerStreamDataUpdatedCallback(Pointer id, Plaza2Callbacks.StreamDataUpdatedCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamDataUpdatedCallback(id, callback));
    }
	public void registerStreamDataDeletedCallback(Pointer id, Plaza2Callbacks.StreamDataDeletedCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamDataDeletedCallback(id, callback));
    }
	public void registerStreamDataBeginCallback(Pointer id, Plaza2Callbacks.StreamDataBeginCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamDataBeginCallback(id, callback));
    }
	public void registerStreamDataEndCallback(Pointer id, Plaza2Callbacks.StreamDataEndCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamDataEndCallback(id, callback));
    }
	public void registerStreamLifeNumChangedCallback(Pointer id, Plaza2Callbacks.StreamLifeNumChangedCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamLifeNumChangedCallback(id, callback));
    }
	public void registerStreamStateChangedCallback(Pointer id, Plaza2Callbacks.StreamStateChangedCallback callback) throws PlazaException {
        checkError(Plaza2DirectApi.registerStreamStateChangedCallback(id, callback));
    }

    private IntByReference intRef = new IntByReference();
	public int readInt(Pointer streamId, int fieldIndex){
        Plaza2DirectApi.readRecordInt(streamId, fieldIndex, intRef);
        return intRef.getValue();
    }

    private LongByReference longRef = new LongByReference();
	public long readLong(Pointer streamId, int fieldIndex){
        Plaza2DirectApi.readRecordLong(streamId, fieldIndex, longRef);
        return longRef.getValue();
    }

    private DoubleByReference doubleRef = new DoubleByReference();
	public double readDouble(Pointer streamId, int fieldIndex){
        Plaza2DirectApi.readRecordDouble(streamId, fieldIndex, doubleRef);
        return doubleRef.getValue();
    }

    private Memory buf = new Memory(8096);
    public String readString(Pointer streamId, int fieldIndex){
        Plaza2DirectApi.readRecordWString(streamId, fieldIndex, buf);
		return buf.getChar(0) != 0 ? buf.getString(0,true) : "";
    }

	private ShortByReference shortRef = new ShortByReference();
	public BigDecimal readDecimal(Pointer streamId, int fieldIndex){
        Plaza2DirectApi.readRecordDecimal(streamId, fieldIndex, longRef, shortRef);
        return BigDecimal.valueOf(longRef.getValue(), shortRef.getValue());
    }

    private void checkError(String err) throws PlazaException {
        if (err != null)
            throw new PlazaException(err);
    }
	
	
	public  Pointer createMessageFactory(String schemaFile) throws PlazaException {
		PointerByReference factoryId = new PointerByReference();
        checkError(Plaza2DirectApi.createMessageFactory(schemaFile, factoryId));
		return  factoryId.getValue();
	}

	public  Pointer createMessage(Pointer factoryId, String name) throws PlazaException {
		PointerByReference msgId = new PointerByReference();
        checkError(Plaza2DirectApi.createMessage(factoryId, name, msgId));
		return  msgId.getValue();
	}
	public void releaseMessage(Pointer msgId) throws PlazaException {
		checkError(Plaza2DirectApi.releaseMessage(msgId));
	}

	public  void setMessageField(Pointer msgId, String fieldName, String value) throws PlazaException {
		checkError(Plaza2DirectApi.setMessageField(msgId, fieldName, value));
	}

	public  void sendMessage(Pointer msgId, Pointer connection, long timeout) throws PlazaException {
		checkError(Plaza2DirectApi.sendMessage(msgId, connection, timeout));
	}

	public  void sendMessageAsync(Pointer msgId, Pointer connection, long timeout, Plaza2Callbacks.SendMessageAsyncCallback callback) throws PlazaException {
		checkError(Plaza2DirectApi.sendMessageAsync(msgId, connection, timeout, callback));
	}

	public  void registerSendMessageAsyncCallback(Plaza2Callbacks.SendMessageAsyncCallback callback){
		Plaza2DirectApi.registerSendMessageAsyncCallback(callback);
	}


	public int readMessageInt(Pointer msgId, String fieldName){
		IntByReference intRef = new IntByReference();
        Plaza2DirectApi.readMessageInt(msgId, fieldName, intRef);
        return intRef.getValue();
    }

	public long readMessageLong(Pointer msgId, String fieldName){
		LongByReference longRef = new LongByReference();
        Plaza2DirectApi.readMessageLong(msgId, fieldName, longRef);
        return longRef.getValue();
    }

	public double readMessageDouble(Pointer msgId, String fieldName){
		DoubleByReference doubleRef = new DoubleByReference();
        Plaza2DirectApi.readMessageDouble(msgId, fieldName, doubleRef);
        return doubleRef.getValue();
    }

    public String readMessageString(Pointer msgId, String fieldName){
		Memory buf = new Memory(8096);
        Plaza2DirectApi.readMessageWString(msgId, fieldName, buf);
        return buf.getString(0,true);
    }

	@Override
	public void reloadLibrary() {
		Plaza2DirectApi.reloadLibrary();
	}
}

