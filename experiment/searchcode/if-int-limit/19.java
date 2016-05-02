/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.datastax.demo.portfolio;

import java.util.*;

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortfolioMgr {

  public interface Iface {

    public List<Portfolio> get_portfolios(String start_token, int limit) throws org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void get_portfolios(String start_token, int limit, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.get_portfolios_call> resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client implements org.apache.thrift.TServiceClient, Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      this(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot)
    {
      iprot_ = iprot;
      oprot_ = oprot;
    }

    protected org.apache.thrift.protocol.TProtocol iprot_;
    protected org.apache.thrift.protocol.TProtocol oprot_;

    protected int seqid_;

    public org.apache.thrift.protocol.TProtocol getInputProtocol()
    {
      return this.iprot_;
    }

    public org.apache.thrift.protocol.TProtocol getOutputProtocol()
    {
      return this.oprot_;
    }

    public List<Portfolio> get_portfolios(String start_token, int limit) throws org.apache.thrift.TException
    {
      send_get_portfolios(start_token, limit);
      return recv_get_portfolios();
    }

    public void send_get_portfolios(String start_token, int limit) throws org.apache.thrift.TException
    {
      oprot_.writeMessageBegin(new org.apache.thrift.protocol.TMessage("get_portfolios", org.apache.thrift.protocol.TMessageType.CALL, ++seqid_));
      get_portfolios_args args = new get_portfolios_args();
      args.setStart_token(start_token);
      args.setLimit(limit);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }

    public List<Portfolio> recv_get_portfolios() throws org.apache.thrift.TException
    {
      org.apache.thrift.protocol.TMessage msg = iprot_.readMessageBegin();
      if (msg.type == org.apache.thrift.protocol.TMessageType.EXCEPTION) {
        org.apache.thrift.TApplicationException x = org.apache.thrift.TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.BAD_SEQUENCE_ID, "get_portfolios failed: out of sequence response");
      }
      get_portfolios_result result = new get_portfolios_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "get_portfolios failed: unknown result");
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void get_portfolios(String start_token, int limit, org.apache.thrift.async.AsyncMethodCallback<get_portfolios_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      get_portfolios_call method_call = new get_portfolios_call(start_token, limit, resultHandler, this, protocolFactory, transport);
      this.currentMethod = method_call;
      manager.call(method_call);
    }

    public static class get_portfolios_call extends org.apache.thrift.async.TAsyncMethodCall {
      private String start_token;
      private int limit;
      public get_portfolios_call(String start_token, int limit, org.apache.thrift.async.AsyncMethodCallback<get_portfolios_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.start_token = start_token;
        this.limit = limit;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("get_portfolios", org.apache.thrift.protocol.TMessageType.CALL, 0));
        get_portfolios_args args = new get_portfolios_args();
        args.setStart_token(start_token);
        args.setLimit(limit);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public List<Portfolio> getResult() throws org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_portfolios();
      }
    }

  }

  public static class Processor implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(Iface iface)
    {
      iface_ = iface;
      processMap_.put("get_portfolios", new get_portfolios());
    }

    protected static interface ProcessFunction {
      public void process(int seqid, org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException;
    }

    private Iface iface_;
    protected final HashMap<String,ProcessFunction> processMap_ = new HashMap<String,ProcessFunction>();

    public boolean process(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException
    {
      org.apache.thrift.protocol.TMessage msg = iprot.readMessageBegin();
      ProcessFunction fn = processMap_.get(msg.name);
      if (fn == null) {
        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, org.apache.thrift.protocol.TType.STRUCT);
        iprot.readMessageEnd();
        org.apache.thrift.TApplicationException x = new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.UNKNOWN_METHOD, "Invalid method name: '"+msg.name+"'");
        oprot.writeMessageBegin(new org.apache.thrift.protocol.TMessage(msg.name, org.apache.thrift.protocol.TMessageType.EXCEPTION, msg.seqid));
        x.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
        return true;
      }
      fn.process(msg.seqid, iprot, oprot);
      return true;
    }

    private class get_portfolios implements ProcessFunction {
      public void process(int seqid, org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException
      {
        get_portfolios_args args = new get_portfolios_args();
        try {
          args.read(iprot);
        } catch (org.apache.thrift.protocol.TProtocolException e) {
          iprot.readMessageEnd();
          org.apache.thrift.TApplicationException x = new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("get_portfolios", org.apache.thrift.protocol.TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_portfolios_result result = new get_portfolios_result();
        result.success = iface_.get_portfolios(args.start_token, args.limit);
        oprot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("get_portfolios", org.apache.thrift.protocol.TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }

    }

  }

  public static class get_portfolios_args implements org.apache.thrift.TBase<get_portfolios_args, get_portfolios_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("get_portfolios_args");

    private static final org.apache.thrift.protocol.TField START_TOKEN_FIELD_DESC = new org.apache.thrift.protocol.TField("start_token", org.apache.thrift.protocol.TType.STRING, (short)1);
    private static final org.apache.thrift.protocol.TField LIMIT_FIELD_DESC = new org.apache.thrift.protocol.TField("limit", org.apache.thrift.protocol.TType.I32, (short)2);

    public String start_token;
    public int limit;

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      START_TOKEN((short)1, "start_token"),
      LIMIT((short)2, "limit");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // START_TOKEN
            return START_TOKEN;
          case 2: // LIMIT
            return LIMIT;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    private static final int __LIMIT_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.START_TOKEN, new org.apache.thrift.meta_data.FieldMetaData("start_token", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
      tmpMap.put(_Fields.LIMIT, new org.apache.thrift.meta_data.FieldMetaData("limit", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(get_portfolios_args.class, metaDataMap);
    }

    public get_portfolios_args() {
    }

    public get_portfolios_args(
      String start_token,
      int limit)
    {
      this();
      this.start_token = start_token;
      this.limit = limit;
      setLimitIsSet(true);
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public get_portfolios_args(get_portfolios_args other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      if (other.isSetStart_token()) {
        this.start_token = other.start_token;
      }
      this.limit = other.limit;
    }

    public get_portfolios_args deepCopy() {
      return new get_portfolios_args(this);
    }

    @Override
    public void clear() {
      this.start_token = null;
      setLimitIsSet(false);
      this.limit = 0;
    }

    public String getStart_token() {
      return this.start_token;
    }

    public get_portfolios_args setStart_token(String start_token) {
      this.start_token = start_token;
      return this;
    }

    public void unsetStart_token() {
      this.start_token = null;
    }

    /** Returns true if field start_token is set (has been assigned a value) and false otherwise */
    public boolean isSetStart_token() {
      return this.start_token != null;
    }

    public void setStart_tokenIsSet(boolean value) {
      if (!value) {
        this.start_token = null;
      }
    }

    public int getLimit() {
      return this.limit;
    }

    public get_portfolios_args setLimit(int limit) {
      this.limit = limit;
      setLimitIsSet(true);
      return this;
    }

    public void unsetLimit() {
      __isset_bit_vector.clear(__LIMIT_ISSET_ID);
    }

    /** Returns true if field limit is set (has been assigned a value) and false otherwise */
    public boolean isSetLimit() {
      return __isset_bit_vector.get(__LIMIT_ISSET_ID);
    }

    public void setLimitIsSet(boolean value) {
      __isset_bit_vector.set(__LIMIT_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case START_TOKEN:
        if (value == null) {
          unsetStart_token();
        } else {
          setStart_token((String)value);
        }
        break;

      case LIMIT:
        if (value == null) {
          unsetLimit();
        } else {
          setLimit((Integer)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case START_TOKEN:
        return getStart_token();

      case LIMIT:
        return new Integer(getLimit());

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case START_TOKEN:
        return isSetStart_token();
      case LIMIT:
        return isSetLimit();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_portfolios_args)
        return this.equals((get_portfolios_args)that);
      return false;
    }

    public boolean equals(get_portfolios_args that) {
      if (that == null)
        return false;

      boolean this_present_start_token = true && this.isSetStart_token();
      boolean that_present_start_token = true && that.isSetStart_token();
      if (this_present_start_token || that_present_start_token) {
        if (!(this_present_start_token && that_present_start_token))
          return false;
        if (!this.start_token.equals(that.start_token))
          return false;
      }

      boolean this_present_limit = true;
      boolean that_present_limit = true;
      if (this_present_limit || that_present_limit) {
        if (!(this_present_limit && that_present_limit))
          return false;
        if (this.limit != that.limit)
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();

      boolean present_start_token = true && (isSetStart_token());
      builder.append(present_start_token);
      if (present_start_token)
        builder.append(start_token);

      boolean present_limit = true;
      builder.append(present_limit);
      if (present_limit)
        builder.append(limit);

      return builder.toHashCode();
    }

    public int compareTo(get_portfolios_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      get_portfolios_args typedOther = (get_portfolios_args)other;

      lastComparison = Boolean.valueOf(isSetStart_token()).compareTo(typedOther.isSetStart_token());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetStart_token()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.start_token, typedOther.start_token);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetLimit()).compareTo(typedOther.isSetLimit());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetLimit()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.limit, typedOther.limit);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: // START_TOKEN
            if (field.type == org.apache.thrift.protocol.TType.STRING) {
              this.start_token = iprot.readString();
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: // LIMIT
            if (field.type == org.apache.thrift.protocol.TType.I32) {
              this.limit = iprot.readI32();
              setLimitIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (this.start_token != null) {
        oprot.writeFieldBegin(START_TOKEN_FIELD_DESC);
        oprot.writeString(this.start_token);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(LIMIT_FIELD_DESC);
      oprot.writeI32(this.limit);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_portfolios_args(");
      boolean first = true;

      sb.append("start_token:");
      if (this.start_token == null) {
        sb.append("null");
      } else {
        sb.append(this.start_token);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("limit:");
      sb.append(this.limit);
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

  }

  public static class get_portfolios_result implements org.apache.thrift.TBase<get_portfolios_result, get_portfolios_result._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("get_portfolios_result");

    private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.LIST, (short)0);

    public List<Portfolio> success;

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      SUCCESS((short)0, "success");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: // SUCCESS
            return SUCCESS;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
              new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Portfolio.class))));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(get_portfolios_result.class, metaDataMap);
    }

    public get_portfolios_result() {
    }

    public get_portfolios_result(
      List<Portfolio> success)
    {
      this();
      this.success = success;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public get_portfolios_result(get_portfolios_result other) {
      if (other.isSetSuccess()) {
        List<Portfolio> __this__success = new ArrayList<Portfolio>();
        for (Portfolio other_element : other.success) {
          __this__success.add(new Portfolio(other_element));
        }
        this.success = __this__success;
      }
    }

    public get_portfolios_result deepCopy() {
      return new get_portfolios_result(this);
    }

    @Override
    public void clear() {
      this.success = null;
    }

    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }

    public java.util.Iterator<Portfolio> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }

    public void addToSuccess(Portfolio elem) {
      if (this.success == null) {
        this.success = new ArrayList<Portfolio>();
      }
      this.success.add(elem);
    }

    public List<Portfolio> getSuccess() {
      return this.success;
    }

    public get_portfolios_result setSuccess(List<Portfolio> success) {
      this.success = success;
      return this;
    }

    public void unsetSuccess() {
      this.success = null;
    }

    /** Returns true if field success is set (has been assigned a value) and false otherwise */
    public boolean isSetSuccess() {
      return this.success != null;
    }

    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<Portfolio>)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_portfolios_result)
        return this.equals((get_portfolios_result)that);
      return false;
    }

    public boolean equals(get_portfolios_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();

      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);

      return builder.toHashCode();
    }

    public int compareTo(get_portfolios_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      get_portfolios_result typedOther = (get_portfolios_result)other;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: // SUCCESS
            if (field.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                this.success = new ArrayList<Portfolio>(_list8.size);
                for (int _i9 = 0; _i9 < _list8.size; ++_i9)
                {
                  Portfolio _elem10;
                  _elem10 = new Portfolio();
                  _elem10.read(iprot);
                  this.success.add(_elem10);
                }
                iprot.readListEnd();
              }
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      oprot.writeStructBegin(STRUCT_DESC);

      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, this.success.size()));
          for (Portfolio _iter11 : this.success)
          {
            _iter11.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_portfolios_result(");
      boolean first = true;

      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

  }

}

