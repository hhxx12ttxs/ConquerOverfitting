package com.brokerexpress.gate.plaza2.impl.api;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;
//import open.commons.ArchUtils;
import com.brokerexpress.gate.plaza2.Plaza2Api;
import com.brokerexpress.gate.plaza2.Plaza2Callbacks;
import com.brokerexpress.gate.plaza2.PlazaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
  * User: brokerexpress
 * Date: 2/22/11
 * Time: 3:35 PM
 */
//TODO:
public class Plaza2RuntimeApiWrapper extends Plaza2ApiWrapper implements Plaza2Api {
    static final Logger logger = LoggerFactory.getLogger(Plaza2RuntimeApi.class);
	static final Plaza2Callbacks.LoggingCallback loggerCallback = new Plaza2Callbacks.LoggingCallback(){
		public void callback(int streamId, String message){
			logger.info(message);
		}
	};

    static{
		System.setProperty("jna.boot.library.path",  "./lib/x32/");//ArchUtils.is32Bit()  ? "./lib/x32/" : "./lib/x64/");
        //Plaza2RuntimeApi.INSTANCE.registerLoggingCallback(loggerCallback);
    }

    public Pointer createConnection(String host, int port, String app, String password, int timeout) throws PlazaException {
        PointerByReference connRef = new PointerByReference();
        checkError(Plaza2RuntimeApi.INSTANCE.createConnection(host, port, app, password, timeout, connRef));
        return connRef.getValue();
    }

	public void openConnection(Pointer id) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.openConnection(id));
    }

	public void closeConnection(Pointer id) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.closeConnection(id));
    }

	public void processMessages(Pointer id) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.processMessages(id));
    }

    private IntByReference stateRef = new IntByReference();
	public int getConnectionState(Pointer id) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.getConnectionState(id, stateRef));
        return  stateRef.getValue();
    }

	public Pointer createStream(String streamName, int mode, String schemaFile, String schemaName) throws PlazaException {
        PointerByReference streamIdRef = new PointerByReference();
        checkError(Plaza2RuntimeApi.INSTANCE.createStream(streamName, mode, schemaFile, schemaName, streamIdRef));
        return  streamIdRef.getValue();

    }

	public void openStream(Pointer id, Pointer connection) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.openStream(id, connection));
    }

	public void closeStream(Pointer id) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.closeStream(id));
    }

	public void setStreamLifeNum(Pointer id, long lifenum) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.setStreamLifeNum(id, lifenum));
    }

	public void setStreamTableRev(Pointer id, String table, long rev) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.setStreamTableRev(id, table, rev));
    }

	public int getStreamState(Pointer id) throws PlazaException {
        IntByReference stateRef = new IntByReference();
        checkError(Plaza2RuntimeApi.INSTANCE.getStreamState(id, stateRef));
        return  stateRef.getValue();
    }

	public String getStreamTableSchema(Pointer streamId, String tableName) throws PlazaException {
        //byte[] buff = new byte[4048];
        Memory mem = new Memory(4048);
        checkError(Plaza2RuntimeApi.INSTANCE.getStreamTableSchema(streamId, tableName, mem));
        return mem.getString(0);
    }

    @Override
    public String getStreamTableName(Pointer streamId, int tableIndex) throws PlazaException {
        Memory mem = new Memory(250);
        checkError(Plaza2RuntimeApi.INSTANCE.getStreamTableName(streamId, tableIndex, mem));
        return mem.getString(0);
    }

    //public void addStreamTable(int streamId, String tableName, String schema);
	public void registerStreamDataInsertedCallback(Pointer id, Plaza2Callbacks.StreamDataInsertedCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamDataInsertedCallback(id, callback));
    }
	public void registerStreamDataUpdatedCallback(Pointer id, Plaza2Callbacks.StreamDataUpdatedCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamDataUpdatedCallback(id, callback));
    }
	public void registerStreamDataDeletedCallback(Pointer id, Plaza2Callbacks.StreamDataDeletedCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamDataDeletedCallback(id, callback));
    }
	public void registerStreamDataBeginCallback(Pointer id, Plaza2Callbacks.StreamDataBeginCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamDataBeginCallback(id, callback));
    }
	public void registerStreamDataEndCallback(Pointer id, Plaza2Callbacks.StreamDataEndCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamDataEndCallback(id, callback));
    }
	public void registerStreamLifeNumChangedCallback(Pointer id, Plaza2Callbacks.StreamLifeNumChangedCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamLifeNumChangedCallback(id, callback));
    }
	public void registerStreamStateChangedCallback(Pointer id, Plaza2Callbacks.StreamStateChangedCallback callback) throws PlazaException {
        checkError(Plaza2RuntimeApi.INSTANCE.registerStreamStateChangedCallback(id, callback));
    }

    IntByReference intRef = new IntByReference();
	public int readInt(Pointer streamId, int fieldIndex){
        Plaza2RuntimeApi.INSTANCE.readRecordInt(streamId, fieldIndex, intRef);
        return intRef.getValue();
    }

    LongByReference longRef = new LongByReference();
	public long readLong(Pointer streamId, int fieldIndex){
        Plaza2RuntimeApi.INSTANCE.readRecordLong(streamId, fieldIndex, longRef);
        return longRef.getValue();
    }

    DoubleByReference doubleRef = new DoubleByReference();
	public double readDouble(Pointer streamId, int fieldIndex){
        Plaza2RuntimeApi.INSTANCE.readRecordDouble(streamId, fieldIndex, doubleRef);
        return doubleRef.getValue();
    }

	ShortByReference shortRef = new ShortByReference();
	public BigDecimal readDecimal(Pointer streamId, int fieldIndex){
        Plaza2RuntimeApi.INSTANCE.readRecordDecimal(streamId, fieldIndex, longRef, shortRef);
        return BigDecimal.valueOf(longRef.getValue(), shortRef.getValue());
    }

    Memory stringBuf = new Memory(8096);
    public String readString(Pointer streamId, int fieldIndex){
        Plaza2RuntimeApi.INSTANCE.readRecordWString(streamId, fieldIndex, stringBuf);
        return stringBuf.getString(0,true);
	}


	public  Pointer createMessageFactory(String schemaFile) throws PlazaException {
		PointerByReference factoryId = new PointerByReference();
        checkError(Plaza2RuntimeApi.INSTANCE.createMessageFactory(schemaFile, factoryId));
		return  factoryId.getValue();
	}

	public  Pointer createMessage(Pointer factoryId, String name) throws PlazaException {
		PointerByReference msgId = new PointerByReference();
        checkError(Plaza2RuntimeApi.INSTANCE.createMessage(factoryId, name, msgId));
		return  msgId.getValue();
	}

	public void releaseMessage(Pointer msgId) throws PlazaException {
		checkError(Plaza2RuntimeApi.INSTANCE.releaseMessage(msgId));
	}

	public  void setMessageField(Pointer msgId, String fieldName, String value) throws PlazaException {
		checkError(Plaza2RuntimeApi.INSTANCE.setMessageField(msgId, fieldName, value));
	}

	public  void sendMessage(Pointer msgId, Pointer connection, long timeout) throws PlazaException {
		checkError(Plaza2RuntimeApi.INSTANCE.sendMessage(msgId, connection, timeout));
	}

	public  void sendMessageAsync(Pointer msgId, Pointer connection, long timeout, Plaza2Callbacks.SendMessageAsyncCallback callback) throws PlazaException {
		checkError(Plaza2RuntimeApi.INSTANCE.sendMessageAsync(msgId, connection, timeout, callback));
	}

	public  void registerSendMessageAsyncCallback(Plaza2Callbacks.SendMessageAsyncCallback callback){
		Plaza2RuntimeApi.INSTANCE.registerSendMessageAsyncCallback(callback);
	}


	public int readMessageInt(Pointer msgId, String fieldName){
		IntByReference intRef = new IntByReference();
        Plaza2RuntimeApi.INSTANCE.readMessageInt(msgId, fieldName, intRef);
        return intRef.getValue();
    }

	public long readMessageLong(Pointer msgId, String fieldName){
		LongByReference longRef = new LongByReference();
        Plaza2RuntimeApi.INSTANCE.readMessageLong(msgId, fieldName, longRef);
        return longRef.getValue();
    }

	public double readMessageDouble(Pointer msgId, String fieldName){
		DoubleByReference doubleRef = new DoubleByReference();
        Plaza2RuntimeApi.INSTANCE.readMessageDouble(msgId, fieldName, doubleRef);
        return doubleRef.getValue();
    }

    public String readMessageString(Pointer msgId, String fieldName){
		Memory buf = new Memory(8096);
        Plaza2RuntimeApi.INSTANCE.readMessageWString(msgId, fieldName, buf);
        return buf.getString(0,true);
    }

	private void checkError(String err) throws PlazaException {
        if (err != null)
            throw new PlazaException(err);
    }

	@Override
	public void reloadLibrary() {}
}

