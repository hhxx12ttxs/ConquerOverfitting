<<<<<<< HEAD
/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */
package com.caucho.quercus.env;

import com.caucho.quercus.QuercusException;
import com.caucho.quercus.QuercusRuntimeException;
import com.caucho.quercus.function.AbstractFunction;
import com.caucho.quercus.marshal.Marshal;
import com.caucho.util.L10N;
import com.caucho.vfs.WriteStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Represents a PHP expression value.
 */
abstract public class Value implements java.io.Serializable {

   protected static final L10N L = new L10N(Value.class);
   private static final Value[] NULL_ARG_VALUES = new Value[0];
   public static final StringValue SCALAR_V = new ConstStringValue("scalar");
   public static final Value[] NULL_VALUE_ARRAY = new Value[0];
   public static final Value[] NULL_ARGS = new Value[0];

   //
   // Properties
   //
   /**
    * Returns the value's class name.
    */
   public String getClassName() {
      return getType();
   }

   /**
    * Returns the backing QuercusClass.
    */
   public QuercusClass getQuercusClass() {
      return null;
   }

   /**
    * Returns the called class
    */
   public Value getCalledClass(Env env) {
      QuercusClass qClass = getQuercusClass();

      if (qClass != null) {
         return env.createString(qClass.getName());
      } else {
         env.warning(L.l("get_called_class() must be called in a class context"));

         return BooleanValue.FALSE;
      }
   }

   //
   // Predicates and Relations
   //
   /**
    * Returns true for an implementation of a class
    */
   public boolean isA(String name) {
      return false;
   }

   /**
    * Returns true for an implementation of a class
    */
   final public boolean isA(Value value) {
      if (value.isObject()) {
         return isA(value.getClassName());
      } else {
         return isA(value.toString());
      }
   }

   /**
    * Checks if 'this' is a valid protected call for 'className'
    */
   public void checkProtected(Env env, String className) {
   }

   /**
    * Checks if 'this' is a valid private call for 'className'
    */
   public void checkPrivate(Env env, String className) {
   }

   /**
    * Returns the ValueType.
    */
   public ValueType getValueType() {
      return ValueType.VALUE;
   }

   /**
    * Returns true for an array.
    */
   public boolean isArray() {
      return false;
   }

   /**
    * Returns true for a double-value.
    */
   public boolean isDoubleConvertible() {
      return false;
   }

   /**
    * Returns true for a long-value.
    */
   public boolean isLongConvertible() {
      return false;
   }

   /**
    * Returns true for a long-value.
    */
   public boolean isLong() {
      return false;
   }

   /**
    * Returns true for a long-value.
    */
   public boolean isDouble() {
      return false;
   }

   /**
    * Returns true for a null.
    */
   public boolean isNull() {
      return false;
   }

   /**
    * Returns true for a number.
    */
   public boolean isNumberConvertible() {
      return isLongConvertible() || isDoubleConvertible();
   }

   /**
    * Matches is_numeric
    */
   public boolean isNumeric() {
      return false;
   }

   /**
    * Returns true for an object.
    */
   public boolean isObject() {
      return false;
   }

   /*
    * Returns true for a resource.
    */
   public boolean isResource() {
      return false;
   }

   /**
    * Returns true for a StringValue.
    */
   public boolean isString() {
      return false;
   }

   /**
    * Returns true for a BinaryValue.
    */
   public boolean isBinary() {
      return false;
   }

   /**
    * Returns true for a UnicodeValue.
    */
   public boolean isUnicode() {
      return false;
   }

   /**
    * Returns true for a BooleanValue
    */
   public boolean isBoolean() {
      return false;
   }

   /**
    * Returns true for a DefaultValue
    */
   public boolean isDefault() {
      return false;
   }

   //
   // marshal costs
   //
   /**
    * Cost to convert to a boolean
    */
   public int toBooleanMarshalCost() {
      return Marshal.COST_TO_BOOLEAN;
   }

   /**
    * Cost to convert to a byte
    */
   public int toByteMarshalCost() {
      return Marshal.COST_INCOMPATIBLE;
   }

   /**
    * Cost to convert to a short
    */
   public int toShortMarshalCost() {
      return Marshal.COST_INCOMPATIBLE;
   }

   /**
    * Cost to convert to an integer
    */
   public int toIntegerMarshalCost() {
      return Marshal.COST_INCOMPATIBLE;
   }

   /**
    * Cost to convert to a long
    */
   public int toLongMarshalCost() {
      return Marshal.COST_INCOMPATIBLE;
   }

   /**
    * Cost to convert to a double
    */
   public int toDoubleMarshalCost() {
      return Marshal.COST_INCOMPATIBLE;
   }

   /**
    * Cost to convert to a float
    */
   public int toFloatMarshalCost() {
      return toDoubleMarshalCost() + 10;
   }

   /**
    * Cost to convert to a character
    */
   public int toCharMarshalCost() {
      return Marshal.COST_TO_CHAR;
   }

   /**
    * Cost to convert to a string
    */
   public int toStringMarshalCost() {
      return Marshal.COST_TO_STRING;
   }

   /**
    * Cost to convert to a byte[]
    */
   public int toByteArrayMarshalCost() {
      return Marshal.COST_TO_BYTE_ARRAY;
   }

   /**
    * Cost to convert to a char[]
    */
   public int toCharArrayMarshalCost() {
      return Marshal.COST_TO_CHAR_ARRAY;
   }

   /**
    * Cost to convert to a Java object
    */
   public int toJavaObjectMarshalCost() {
      return Marshal.COST_TO_JAVA_OBJECT;
   }

   /**
    * Cost to convert to a binary value
    */
   public int toBinaryValueMarshalCost() {
      return Marshal.COST_TO_STRING + 1;
   }

   /**
    * Cost to convert to a StringValue
    */
   public int toStringValueMarshalCost() {
      return Marshal.COST_TO_STRING + 1;
   }

   /**
    * Cost to convert to a UnicodeValue
    */
   public int toUnicodeValueMarshalCost() {
      return Marshal.COST_TO_STRING + 1;
   }

   //
   // predicates
   //
   /**
    * Returns true if the value is set.
    */
   public boolean isset() {
      return true;
   }

   /**
    * Returns true if the value is empty
    */
   public boolean isEmpty() {
      return false;
   }

   /**
    * Returns true if there are more elements.
    */
   public boolean hasCurrent() {
      return false;
   }

   /**
    * Returns true for equality
    */
   public Value eqValue(Value rValue) {
      return eq(rValue) ? BooleanValue.TRUE : BooleanValue.FALSE;
   }

   /**
    * Returns true for equality
    */
   public boolean eq(Value rValue) {
      if (rValue.isArray()) {
         return rValue.eq(this);
      } else if (rValue instanceof BooleanValue) {
         return toBoolean() == rValue.toBoolean();
      } else if (isLongConvertible() && rValue.isLongConvertible()) {
         return toLong() == rValue.toLong();
      } else if (isNumberConvertible() || rValue.isNumberConvertible()) {
         return toDouble() == rValue.toDouble();
      } else {
         return toString().equals(rValue.toString());
      }
   }

   /**
    * Returns true for equality
    */
   public boolean eql(Value rValue) {
      return this == rValue.toValue();
   }

   /**
    * Returns a negative/positive integer if this Value is
    * lessthan/greaterthan rValue.
    */
   public int cmp(Value rValue) {
      // This is tricky: implemented according to Table 15-5 of
      // http://us2.php.net/manual/en/language.operators.comparison.php

      Value lVal = toValue();
      Value rVal = rValue.toValue();

      if (lVal instanceof StringValue && rVal instanceof NullValue) {
         return ((StringValue) lVal).cmpString(StringValue.EMPTY);
      }

      if (lVal instanceof NullValue && rVal instanceof StringValue) {
         return StringValue.EMPTY.cmpString((StringValue) rVal);
      }

      if (lVal instanceof StringValue && rVal instanceof StringValue) {
         return ((StringValue) lVal).cmpString((StringValue) rVal);
      }

      if (lVal instanceof NullValue
              || lVal instanceof BooleanValue
              || rVal instanceof NullValue
              || rVal instanceof BooleanValue) {
         boolean lBool = toBoolean();
         boolean rBool = rValue.toBoolean();

         if (!lBool && rBool) {
            return -1;
         }
         if (lBool && !rBool) {
            return 1;
         }
         return 0;
      }

      if (lVal.isObject() && rVal.isObject()) {
         return ((ObjectValue) lVal).cmpObject((ObjectValue) rVal);
      }

      if ((lVal instanceof StringValue
              || lVal instanceof NumberValue
              || lVal instanceof ResourceValue)
              && (rVal instanceof StringValue
              || rVal instanceof NumberValue
              || rVal instanceof ResourceValue)) {
         return NumberValue.compareNum(lVal, rVal);
      }

      if (lVal instanceof ArrayValue) {
         return 1;
      }
      if (rVal instanceof ArrayValue) {
         return -1;
      }
      if (lVal instanceof ObjectValue) {
         return 1;
      }
      if (rVal instanceof ObjectValue) {
         return -1;
      }

      // TODO: proper default case?
      throw new RuntimeException(
              "values are incomparable: " + lVal + " <=> " + rVal);
   }

   /**
    * Returns true for less than
    */
   public boolean lt(Value rValue) {
      return cmp(rValue) < 0;
   }

   /**
    * Returns true for less than or equal to
    */
   public boolean leq(Value rValue) {
      return cmp(rValue) <= 0;
   }

   /**
    * Returns true for greater than
    */
   public boolean gt(Value rValue) {
      return cmp(rValue) > 0;
   }

   /**
    * Returns true for greater than or equal to
    */
   public boolean geq(Value rValue) {
      return cmp(rValue) >= 0;
   }

   //
   // Conversions
   //
   /**
    * Converts to a boolean.
    */
   public boolean toBoolean() {
      return true;
   }

   /**
    * Converts to a long.
    */
   public long toLong() {
      return toBoolean() ? 1 : 0;
   }

   /**
    * Converts to an int
    */
   public int toInt() {
      return (int) toLong();
   }

   /**
    * Converts to a double.
    */
   public double toDouble() {
      return 0;
   }

   /**
    * Converts to a char
    */
   public char toChar() {
      String s = toString();

      if (s == null || s.length() < 1) {
         return 0;
      } else {
         return s.charAt(0);
      }
   }

   /**
    * Converts to a string.
    *
    * @param env
    */
   public StringValue toString(Env env) {
      return toStringValue();
   }

   /**
    * Converts to an array.
    */
   public Value toArray() {
      return new ArrayValueImpl().append(this);
   }

   /**
    * Converts to an array if null.
    */
   public Value toAutoArray() {
      Env.getCurrent().warning(L.l("'{0}' cannot be used as an array.",
              toDebugString()));

      return this;
   }

   /**
    * Casts to an array.
    */
   public ArrayValue toArrayValue(Env env) {
      env.warning(L.l("'{0}' ({1}) is not assignable to ArrayValue",
              this, getType()));

      return null;
   }

   /**
    * Converts to an object if null.
    */
   public Value toAutoObject(Env env) {
      return this;
   }

   /**
    * Converts to an object.
    */
   public Value toObject(Env env) {
      ObjectValue obj = env.createObject();

      obj.putField(env, env.createString("scalar"), this);

      return obj;
   }

   /**
    * Converts to a java object.
    */
   public Object toJavaObject() {
      return null;
   }

   /**
    * Converts to a java object.
    */
   public Object toJavaObject(Env env, Class type) {
      env.warning(L.l("Can't convert {0} to Java {1}",
              getClass().getName(), type.getName()));

      return null;
   }

   /**
    * Converts to a java object.
    */
   public Object toJavaObjectNotNull(Env env, Class type) {
      env.warning(L.l("Can't convert {0} to Java {1}",
              getClass().getName(), type.getName()));

      return null;
   }

   /**
    * Converts to a java boolean object.
    */
   public Boolean toJavaBoolean() {
      return toBoolean() ? Boolean.TRUE : Boolean.FALSE;
   }

   /**
    * Converts to a java byte object.
    */
   public Byte toJavaByte() {
      return new Byte((byte) toLong());
   }

   /**
    * Converts to a java short object.
    */
   public Short toJavaShort() {
      return new Short((short) toLong());
   }

   /**
    * Converts to a java Integer object.
    */
   public Integer toJavaInteger() {
      return new Integer((int) toLong());
   }

   /**
    * Converts to a java Long object.
    */
   public Long toJavaLong() {
      return new Long((int) toLong());
   }

   /**
    * Converts to a java Float object.
    */
   public Float toJavaFloat() {
      return new Float((float) toDouble());
   }

   /**
    * Converts to a java Double object.
    */
   public Double toJavaDouble() {
      return new Double(toDouble());
   }

   /**
    * Converts to a java Character object.
    */
   public Character toJavaCharacter() {
      return new Character(toChar());
   }

   /**
    * Converts to a java String object.
    */
   public String toJavaString() {
      return toString();
   }

   /**
    * Converts to a java Collection object.
    */
   public Collection<?> toJavaCollection(Env env, Class<?> type) {
      env.warning(L.l("Can't convert {0} to Java {1}",
              getClass().getName(), type.getName()));

      return null;
   }

   /**
    * Converts to a java List object.
    */
   public List<?> toJavaList(Env env, Class<?> type) {
      env.warning(L.l("Can't convert {0} to Java {1}",
              getClass().getName(), type.getName()));

      return null;
   }

   /**
    * Converts to a java Map object.
    */
   public Map<?, ?> toJavaMap(Env env, Class<?> type) {
      env.warning(L.l("Can't convert {0} to Java {1}",
              getClass().getName(), type.getName()));

      return null;
   }

   /**
    * Converts to a Java Calendar.
    */
   public Calendar toJavaCalendar() {
      Calendar cal = Calendar.getInstance();

      cal.setTimeInMillis(toLong());

      return cal;
   }

   /**
    * Converts to a Java Date.
    */
   public Date toJavaDate() {
      return new Date(toLong());
   }

   /**
    * Converts to a Java URL.
    */
   public URL toJavaURL(Env env) {
      try {
         return new URL(toString());
      } catch (MalformedURLException e) {
         env.warning(L.l(e.getMessage()));
         return null;
      }
   }

   /**
    * Converts to a Java BigDecimal.
    */
   public BigDecimal toBigDecimal() {
      return new BigDecimal(toString());
   }

   /**
    * Converts to a Java BigInteger.
    */
   public BigInteger toBigInteger() {
      return new BigInteger(toString());
   }

   /**
    * Converts to an exception.
    */
   public QuercusException toException(Env env, String file, int line) {
      putField(env, env.createString("file"), env.createString(file));
      putField(env, env.createString("line"), LongValue.create(line));

      return new QuercusLanguageException(this);
   }

   /**
    * Converts to a raw value.
    */
   public Value toValue() {
      return this;
   }

   /**
    * Converts to a key.
    */
   public Value toKey() {
      throw new QuercusRuntimeException(L.l("{0} is not a valid key", this));
   }

   /**
    * Convert to a ref.
    */
   public Value toRef() {
      return this;
   }

   /**
    * Convert to a function argument value, e.g. for
    *
    * function foo($a)
    *
    * where $a is never assigned or modified
    */
   public Value toLocalValueReadOnly() {
      return this;
   }

   /**
    * Convert to a function argument value, e.g. for
    *
    * function foo($a)
    *
    * where $a is never assigned, but might be modified, e.g. $a[3] = 9
    */
   public Value toLocalValue() {
      return this;
   }

   /**
    * Convert to a function argument value, e.g. for
    *
    * function foo($a)
    *
    * where $a may be assigned.
    */
   public Value toLocalRef() {
      return this;
   }

   /**
    * Convert to a function argument value, e.g. for
    *
    * function foo($a)
    *
    * where $a is used as a variable in the function
    */
   public Var toLocalVar() {
      return toLocalRef().toVar();
   }

   /**
    * Convert to a function argument reference value, e.g. for
    *
    * function foo(&$a)
    *
    * where $a is used as a variable in the function
    */
   public Var toLocalVarDeclAsRef() {
      return new Var(this);
   }

   /**
    * Converts to a local $this, which can depend on the calling class
    */
   public Value toLocalThis(QuercusClass qClass) {
      return this;
   }

   /**
    * Convert to a function argument reference value, e.g. for
    *
    * function foo(&$a)
    *
    * where $a is never assigned in the function
    */
   public Value toRefValue() {
      return this;
   }

   /**
    * Converts to a Var.
    */
   public Var toVar() {
      return new Var(this);
   }

   /**
    * Convert to a function argument reference value, e.g. for
    *
    * function foo(&$a)
    *
    * where $a is used as a variable in the function
    */
   public Value toArgRef() {
      Env.getCurrent().warning(L.l(
              "'{0}' is an invalid reference, because only "
              + "variables may be passed by reference.",
              this));

      return NullValue.NULL;
   }

   /**
    * Converts to a StringValue.
    */
   public StringValue toStringValue() {
      return toStringValue(Env.getInstance());
   }

   /*
    * Converts to a StringValue.
    */
   public StringValue toStringValue(Env env) {
      return toStringBuilder(env);
   }

   /**
    * Converts to a Unicode string.  For unicode.semantics=false, this will
    * still return a StringValue. For unicode.semantics=true, this will
    * return a UnicodeStringValue.
    */
   public StringValue toUnicode(Env env) {
      return toUnicodeValue(env);
   }

   /**
    * Converts to a UnicodeValue for marshaling, so it will create a
    * UnicodeValue event when unicode.semantics=false.
    */
   public StringValue toUnicodeValue() {
      return toUnicodeValue(Env.getInstance());
   }

   /**
    * Converts to a UnicodeValue for marshaling, so it will create a
    * UnicodeValue event when unicode.semantics=false.
    */
   public StringValue toUnicodeValue(Env env) {
      // php/0ci0
      return new UnicodeBuilderValue(env.createString(toString()));
   }

   /**
    * Converts to a BinaryValue.
    */
   public StringValue toBinaryValue() {
      return toBinaryValue(Env.getInstance());
   }

   /**
    * Converts to a BinaryValue.
    */
   public StringValue toBinaryValue(String charset) {
      return toBinaryValue();
   }

   /**
    * Converts to a BinaryValue.
    */
   public StringValue toBinaryValue(Env env) {
      StringValue bb = env.createBinaryBuilder();

      bb.append(this);

      return bb;

      /*
      try {
      int length = 0;
      while (true) {
      bb.ensureCapacity(bb.getLength() + 256);

      int sublen = is.read(bb.getBuffer(),
      bb.getOffset(),
      bb.getLength() - bb.getOffset());

      if (sublen <= 0)
      return bb;
      else {
      length += sublen;
      bb.setOffset(length);
      }
      }
      } catch (IOException e) {
      throw new QuercusException(e);
      }
       */
   }

   /**
    * Returns a byteArrayInputStream for the value.
    * See TempBufferStringValue for how this can be overriden
    *
    * @return InputStream
    */
   public InputStream toInputStream() {
      return new StringInputStream(toString());
   }

   /**
    * Converts to a string builder
    */
   public StringValue toStringBuilder() {
      return toStringBuilder(Env.getInstance());
   }

   /**
    * Converts to a string builder
    */
   public StringValue toStringBuilder(Env env) {
      return env.createUnicodeBuilder().appendUnicode(this);
   }

   /**
    * Converts to a string builder
    */
   public StringValue toStringBuilder(Env env, Value value) {
      return toStringBuilder(env).appendUnicode(value);
   }

   /**
    * Converts to a string builder
    */
   public StringValue toStringBuilder(Env env, StringValue value) {
      return toStringBuilder(env).appendUnicode(value);
   }

   /**
    * Converts to a string builder
    */
   public StringValue copyStringBuilder() {
      return toStringBuilder();
   }

   /**
    * Converts to a long vaule
    */
   public LongValue toLongValue() {
      return LongValue.create(toLong());
   }

   /**
    * Converts to a double vaule
    */
   public DoubleValue toDoubleValue() {
      return new DoubleValue(toDouble());
   }

   /**
    * Returns true for a callable object.
    */
   public boolean isCallable(Env env) {
      return false;
   }

   /**
    * Returns the callable's name for is_callable()
    */
   public String getCallableName() {
      return null;
   }

   /**
    * Converts to a callable
    */
   public Callable toCallable(Env env) {
      env.warning(L.l("Callable: '{0}' is not a valid callable argument",
              toString()));

      return new CallbackError(toString());
   }

   //
   // Operations
   //
   /**
    * Append to a string builder.
    */
   public StringValue appendTo(UnicodeBuilderValue sb) {
      return sb.append(toString());
   }

   /**
    * Append to a binary builder.
    */
   public StringValue appendTo(StringBuilderValue sb) {
      return sb.append(toString());
   }

   /**
    * Append to a binary builder.
    */
   public StringValue appendTo(BinaryBuilderValue sb) {
      return sb.appendBytes(toString());
   }

   /**
    * Append to a binary builder.
    */
   public StringValue appendTo(LargeStringBuilderValue sb) {
      return sb.append(toString());
   }

   /**
    * Copy for assignment.
    */
   public Value copy() {
      return this;
   }

   /**
    * Copy as an array item
    */
   public Value copyArrayItem() {
      return copy();
   }

   /**
    * Copy as a return value
    */
   public Value copyReturn() {
      // php/3a5d

      return this;
   }

   /**
    * Copy for serialization
    */
   public final Value copy(Env env) {
      return copy(env, new IdentityHashMap<Value, Value>());
   }

   /**
    * Copy for serialization
    */
   public Value copy(Env env, IdentityHashMap<Value, Value> map) {
      return this;
   }

   /**
    * Copy for serialization
    */
   public Value copyTree(Env env, CopyRoot root) {
      return this;
   }

   /**
    * Clone for the clone keyword
    */
   public Value clone(Env env) {
      return this;
   }

   /**
    * Copy for saving a method's arguments.
    */
   public Value copySaveFunArg() {
      return copy();
   }

   /**
    * Returns the type.
    */
   public String getType() {
      return "value";
   }

   /*
    * Returns the resource type.
    */
   public String getResourceType() {
      return null;
   }

   /**
    * Returns the current key
    */
   public Value key() {
      return BooleanValue.FALSE;
   }

   /**
    * Returns the current value
    */
   public Value current() {
      return BooleanValue.FALSE;
   }

   /**
    * Returns the next value
    */
   public Value next() {
      return BooleanValue.FALSE;
   }

   /**
    * Returns the previous value
    */
   public Value prev() {
      return BooleanValue.FALSE;
   }

   /**
    * Returns the end value.
    */
   public Value end() {
      return BooleanValue.FALSE;
   }

   /**
    * Returns the array pointer.
    */
   public Value reset() {
      return BooleanValue.FALSE;
   }

   /**
    * Shuffles the array.
    */
   public Value shuffle() {
      return BooleanValue.FALSE;
   }

   /**
    * Pops the top array element.
    */
   public Value pop(Env env) {
      env.warning("cannot pop a non-array");

      return NullValue.NULL;
   }

   /**
    * Finds the method name.
    */
   public AbstractFunction findFunction(String methodName) {
      return null;
   }

   //
   // function invocation
   //
   /**
    * Evaluates the function.
    */
   public Value call(Env env, Value[] args) {
      Callable call = toCallable(env);

      if (call != null) {
         return call.call(env, args);
      } else {
         return env.warning(L.l("{0} is not a valid function",
                 this));
      }
   }

   /**
    * Evaluates the function, returning a reference.
    */
   public Value callRef(Env env, Value[] args) {
      AbstractFunction fun = env.getFunction(this);

      if (fun != null) {
         return fun.callRef(env, args);
      } else {
         return env.warning(L.l("{0} is not a valid function",
                 this));
      }
   }

   /**
    * Evaluates the function, returning a copy
    */
   public Value callCopy(Env env, Value[] args) {
      AbstractFunction fun = env.getFunction(this);

      if (fun != null) {
         return fun.callCopy(env, args);
      } else {
         return env.warning(L.l("{0} is not a valid function",
                 this));
      }
   }

   /**
    * Evaluates the function.
    */
   public Value call(Env env) {
      return call(env, NULL_ARG_VALUES);
   }

   /**
    * Evaluates the function.
    */
   public Value callRef(Env env) {
      return callRef(env, NULL_ARG_VALUES);
   }

   /**
    * Evaluates the function with an argument .
    */
   public Value call(Env env, Value a1) {
      return call(env, new Value[]{a1});
   }

   /**
    * Evaluates the function with an argument .
    */
   public Value callRef(Env env, Value a1) {
      return callRef(env, new Value[]{a1});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value call(Env env, Value a1, Value a2) {
      return call(env, new Value[]{a1, a2});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value callRef(Env env, Value a1, Value a2) {
      return callRef(env, new Value[]{a1, a2});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value call(Env env, Value a1, Value a2, Value a3) {
      return call(env, new Value[]{a1, a2, a3});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value callRef(Env env, Value a1, Value a2, Value a3) {
      return callRef(env, new Value[]{a1, a2, a3});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value call(Env env, Value a1, Value a2, Value a3, Value a4) {
      return call(env, new Value[]{a1, a2, a3, a4});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value callRef(Env env, Value a1, Value a2, Value a3, Value a4) {
      return callRef(env, new Value[]{a1, a2, a3, a4});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value call(Env env, Value a1, Value a2, Value a3, Value a4, Value a5) {
      return call(env, new Value[]{a1, a2, a3, a4, a5});
   }

   /**
    * Evaluates the function with arguments
    */
   public Value callRef(Env env,
           Value a1, Value a2, Value a3, Value a4, Value a5) {
      return callRef(env, new Value[]{a1, a2, a3, a4, a5});
   }

   //
   // Methods invocation
   //
   /**
    * Evaluates a method.
    */
   public Value callMethod(Env env,
           StringValue methodName, int hash,
           Value[] args) {
      if (isNull()) {
         return env.error(L.l("Method call '{0}' is not allowed for a null value.",
                 methodName));
      } else {
         return env.error(L.l("'{0}' is an unknown method of {1}.",
                 methodName,
                 toDebugString()));
      }
   }

   /**
    * Evaluates a method.
    */
   public final Value callMethod(Env env,
           StringValue methodName,
           Value[] args) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash, args);
   }

   /**
    * Evaluates a method.
    */
   public Value callMethodRef(Env env,
           StringValue methodName, int hash,
           Value[] args) {
      return callMethod(env, methodName, hash, args);
   }

   /**
    * Evaluates a method.
    */
   public final Value callMethodRef(Env env,
           StringValue methodName,
           Value[] args) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash, args);
   }

   /**
    * Evaluates a method with 0 args.
    */
   public Value callMethod(Env env, StringValue methodName, int hash) {
      return callMethod(env, methodName, hash, NULL_ARG_VALUES);
   }

   /**
    * Evaluates a method with 0 args.
    */
   public final Value callMethod(Env env, StringValue methodName) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash);
   }

   /**
    * Evaluates a method with 0 args.
    */
   public Value callMethodRef(Env env, StringValue methodName, int hash) {
      return callMethodRef(env, methodName, hash, NULL_ARG_VALUES);
   }

   /**
    * Evaluates a method with 0 args.
    */
   public final Value callMethodRef(Env env, StringValue methodName) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash);
   }

   /**
    * Evaluates a method with 1 arg.
    */
   public Value callMethod(Env env,
           StringValue methodName, int hash,
           Value a1) {
      return callMethod(env, methodName, hash, new Value[]{a1});
   }

   /**
    * Evaluates a method with 1 arg.
    */
   public final Value callMethod(Env env,
           StringValue methodName,
           Value a1) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash, a1);
   }

   /**
    * Evaluates a method with 1 arg.
    */
   public Value callMethodRef(Env env,
           StringValue methodName, int hash,
           Value a1) {
      return callMethodRef(env, methodName, hash, new Value[]{a1});
   }

   /**
    * Evaluates a method with 1 arg.
    */
   public final Value callMethodRef(Env env,
           StringValue methodName,
           Value a1) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash, a1);
   }

   /**
    * Evaluates a method with 2 args.
    */
   public Value callMethod(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2) {
      return callMethod(env, methodName, hash, new Value[]{a1, a2});
   }

   /**
    * Evaluates a method with 2 args.
    */
   public final Value callMethod(Env env,
           StringValue methodName,
           Value a1, Value a2) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash,
              a1, a2);
   }

   /**
    * Evaluates a method with 2 args.
    */
   public Value callMethodRef(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2) {
      return callMethodRef(env, methodName, hash, new Value[]{a1, a2});
   }

   /**
    * Evaluates a method with 2 args.
    */
   public final Value callMethodRef(Env env,
           StringValue methodName,
           Value a1, Value a2) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash,
              a1, a2);
   }

   /**
    * Evaluates a method with 3 args.
    */
   public Value callMethod(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2, Value a3) {
      return callMethod(env, methodName, hash, new Value[]{a1, a2, a3});
   }

   /**
    * Evaluates a method with 3 args.
    */
   public final Value callMethod(Env env,
           StringValue methodName,
           Value a1, Value a2, Value a3) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash,
              a1, a2, a3);
   }

   /**
    * Evaluates a method with 3 args.
    */
   public Value callMethodRef(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2, Value a3) {
      return callMethodRef(env, methodName, hash, new Value[]{a1, a2, a3});
   }

   /**
    * Evaluates a method with 3 args.
    */
   public final Value callMethodRef(Env env,
           StringValue methodName,
           Value a1, Value a2, Value a3) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash,
              a1, a2, a3);
   }

   /**
    * Evaluates a method with 4 args.
    */
   public Value callMethod(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2, Value a3, Value a4) {
      return callMethod(env, methodName, hash,
              new Value[]{a1, a2, a3, a4});
   }

   /**
    * Evaluates a method with 4 args.
    */
   public final Value callMethod(Env env,
           StringValue methodName,
           Value a1, Value a2, Value a3, Value a4) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash,
              a1, a2, a3, a4);
   }

   /**
    * Evaluates a method with 4 args.
    */
   public Value callMethodRef(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2, Value a3, Value a4) {
      return callMethodRef(env, methodName, hash,
              new Value[]{a1, a2, a3, a4});
   }

   /**
    * Evaluates a method with 4 args.
    */
   public final Value callMethodRef(Env env,
           StringValue methodName,
           Value a1, Value a2, Value a3, Value a4) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash,
              a1, a2, a3, a4);
   }

   /**
    * Evaluates a method with 5 args.
    */
   public Value callMethod(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2, Value a3, Value a4, Value a5) {
      return callMethod(env, methodName, hash,
              new Value[]{a1, a2, a3, a4, a5});
   }

   /**
    * Evaluates a method with 5 args.
    */
   public final Value callMethod(Env env,
           StringValue methodName,
           Value a1, Value a2, Value a3, Value a4, Value a5) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethod(env, methodName, hash,
              a1, a2, a3, a4, a5);
   }

   /**
    * Evaluates a method with 5 args.
    */
   public Value callMethodRef(Env env,
           StringValue methodName, int hash,
           Value a1, Value a2, Value a3, Value a4, Value a5) {
      return callMethodRef(env, methodName, hash,
              new Value[]{a1, a2, a3, a4, a5});
   }

   /**
    * Evaluates a method with 5 args.
    */
   public final Value callMethodRef(Env env,
           StringValue methodName,
           Value a1, Value a2, Value a3, Value a4, Value a5) {
      int hash = methodName.hashCodeCaseInsensitive();

      return callMethodRef(env, methodName, hash,
              a1, a2, a3, a4, a5);
   }

   //
   // Methods from StringValue
   //
   /**
    * Evaluates a method.
    */
   private Value callClassMethod(Env env, AbstractFunction fun, Value[] args) {
      return NullValue.NULL;
   }

   private Value errorNoMethod(Env env, char[] name, int nameLen) {
      String methodName = new String(name, 0, nameLen);

      if (isNull()) {
         return env.error(L.l("Method call '{0}' is not allowed for a null value.",
                 methodName));
      } else {
         return env.error(L.l("'{0}' is an unknown method of {1}.",
                 methodName,
                 toDebugString()));
      }
   }

   //
   // Arithmetic operations
   //
   /**
    * Negates the value.
    */
   public Value neg() {
      return LongValue.create(-toLong());
   }

   /**
    * Negates the value.
    */
   public Value pos() {
      return LongValue.create(toLong());
   }

   /**
    * Adds to the following value.
    */
   public Value add(Value rValue) {
      if (getValueType().isLongAdd() && rValue.getValueType().isLongAdd()) {
         return LongValue.create(toLong() + rValue.toLong());
      }

      return DoubleValue.create(toDouble() + rValue.toDouble());
   }

   /**
    * Multiplies to the following value.
    */
   public Value add(long lLong) {
      return new DoubleValue(lLong + toDouble());
   }

   /**
    * Pre-increment the following value.
    */
   public Value preincr(int incr) {
      return increment(incr);
   }

   /**
    * Post-increment the following value.
    */
   public Value postincr(int incr) {
      return increment(incr);
   }

   /**
    * Return the next integer
    */
   public Value addOne() {
      return add(1);
   }

   /**
    * Return the previous integer
    */
   public Value subOne() {
      return sub(1);
   }

   /**
    * Pre-increment the following value.
    */
   public Value preincr() {
      return increment(1);
   }

   /**
    * Post-increment the following value.
    */
   public Value postincr() {
      return increment(1);
   }

   /**
    * Pre-increment the following value.
    */
   public Value predecr() {
      return increment(-1);
   }

   /**
    * Post-increment the following value.
    */
   public Value postdecr() {
      return increment(-1);
   }

   /**
    * Increment the following value.
    */
   public Value increment(int incr) {
      long lValue = toLong();

      return LongValue.create(lValue + incr);
   }

   /**
    * Subtracts to the following value.
    */
   public Value sub(Value rValue) {
      if (getValueType().isLongAdd() && rValue.getValueType().isLongAdd()) {
         return LongValue.create(toLong() - rValue.toLong());
      }

      return DoubleValue.create(toDouble() - rValue.toDouble());
   }

   /**
    * Subtracts
    */
   public Value sub(long rLong) {
      return new DoubleValue(toDouble() - rLong);
   }

   /**
    * Substracts from the previous value.
    */
   public Value sub_rev(long lLong) {
      if (getValueType().isLongAdd()) {
         return LongValue.create(lLong - toLong());
      } else {
         return new DoubleValue(lLong - toDouble());
      }
   }

   /**
    * Multiplies to the following value.
    */
   public Value mul(Value rValue) {
      if (getValueType().isLongAdd() && rValue.getValueType().isLongAdd()) {
         return LongValue.create(toLong() * rValue.toLong());
      } else {
         return new DoubleValue(toDouble() * rValue.toDouble());
      }
   }

   /**
    * Multiplies to the following value.
    */
   public Value mul(long r) {
      if (isLongConvertible()) {
         return LongValue.create(toLong() * r);
      } else {
         return new DoubleValue(toDouble() * r);
      }
   }

   /**
    * Divides the following value.
    */
   public Value div(Value rValue) {
      if (getValueType().isLongAdd() && rValue.getValueType().isLongAdd()) {
         long l = toLong();
         long r = rValue.toLong();

         if (r != 0 && l % r == 0) {
            return LongValue.create(l / r);
         } else {
            return new DoubleValue(toDouble() / rValue.toDouble());
         }
      } else {
         return new DoubleValue(toDouble() / rValue.toDouble());
      }
   }

   /**
    * Multiplies to the following value.
    */
   public Value div(long r) {
      long l = toLong();

      if (r != 0 && l % r == 0) {
         return LongValue.create(l / r);
      } else {
         return new DoubleValue(toDouble() / r);
      }
   }

   /**
    * modulo the following value.
    */
   public Value mod(Value rValue) {
      double lDouble = toDouble();
      double rDouble = rValue.toDouble();

      return LongValue.create((long) lDouble % rDouble);
   }

   /**
    * Shifts left by the value.
    */
   public Value lshift(Value rValue) {
      long lLong = toLong();
      long rLong = rValue.toLong();

      return LongValue.create(lLong << rLong);
   }

   /**
    * Shifts right by the value.
    */
   public Value rshift(Value rValue) {
      long lLong = toLong();
      long rLong = rValue.toLong();

      return LongValue.create(lLong >> rLong);
   }

   /*
    * Binary And.
    */
   public Value bitAnd(Value rValue) {
      return LongValue.create(toLong() & rValue.toLong());
   }

   /*
    * Binary or.
    */
   public Value bitOr(Value rValue) {
      return LongValue.create(toLong() | rValue.toLong());
   }

   /**
    * Binary xor.
    */
   public Value bitXor(Value rValue) {
      return LongValue.create(toLong() ^ rValue.toLong());
   }

   /**
    * Absolute value.
    */
   public Value abs() {
      if (getValueType().isDoubleCmp()) {
         return new DoubleValue(Math.abs(toDouble()));
      } else {
         return LongValue.create(Math.abs(toLong()));
      }
   }

   /**
    * Returns the next array index based on this value.
    */
   public long nextIndex(long oldIndex) {
      return oldIndex;
   }

   //
   // string functions
   //
   /**
    * Returns the length as a string.
    */
   public int length() {
      return toStringValue().length();
   }

   //
   // Array functions
   //
   /**
    * Returns the array size.
    */
   public int getSize() {
      return 1;
   }

   /**
    * Returns the count, as returned by the global php count() function
    */
   public int getCount(Env env) {
      return 1;
   }

   /**
    * Returns the count, as returned by the global php count() function
    */
   public int getCountRecursive(Env env) {
      return getCount(env);
   }

   /**
    * Returns an iterator for the key => value pairs.
    */
   public Iterator<Map.Entry<Value, Value>> getIterator(Env env) {
      return getBaseIterator(env);
   }

   /**
    * Returns an iterator for the key => value pairs.
    */
   public Iterator<Map.Entry<Value, Value>> getBaseIterator(Env env) {
      Set<Map.Entry<Value, Value>> emptySet = Collections.emptySet();

      return emptySet.iterator();
   }

   /**
    * Returns an iterator for the field keys.
    * The default implementation uses the Iterator returned
    * by {@link #getIterator(Env)}; derived classes may override and
    * provide a more efficient implementation.
    */
   public Iterator<Value> getKeyIterator(Env env) {
      final Iterator<Map.Entry<Value, Value>> iter = getIterator(env);

      return new Iterator<Value>() {

         @Override
         public boolean hasNext() {
            return iter.hasNext();
         }

         @Override
         public Value next() {
            return iter.next().getKey();
         }

         @Override
         public void remove() {
            iter.remove();
         }
      };
   }

   /**
    * Returns the field keys.
    */
   public Value[] getKeyArray(Env env) {
      return NULL_VALUE_ARRAY;
   }

   /**
    * Returns the field values.
    */
   public Value[] getValueArray(Env env) {
      return NULL_VALUE_ARRAY;
   }

   /**
    * Returns an iterator for the field values.
    * The default implementation uses the Iterator returned
    * by {@link #getIterator(Env)}; derived classes may override and
    * provide a more efficient implementation.
    */
   public Iterator<Value> getValueIterator(Env env) {
      final Iterator<Map.Entry<Value, Value>> iter = getIterator(env);

      return new Iterator<Value>() {

         @Override
         public boolean hasNext() {
            return iter.hasNext();
         }

         @Override
         public Value next() {
            return iter.next().getValue();
         }

         @Override
         public void remove() {
            iter.remove();
         }
      };
   }

   //
   // Object field references
   //
   /**
    * Returns the field value
    */
   public Value getField(Env env, StringValue name) {
      return NullValue.NULL;
   }

   /**
    * Returns the field ref.
    */
   public Var getFieldVar(Env env, StringValue name) {
      return getField(env, name).toVar();
   }

   /**
    * Returns the field used as a method argument
    */
   public Value getFieldArg(Env env, StringValue name, boolean isTop) {
      return getFieldVar(env, name);
   }

   /**
    * Returns the field ref for an argument.
    */
   public Value getFieldArgRef(Env env, StringValue name) {
      return getFieldVar(env, name);
   }

   /**
    * Returns the value for a field, creating an object if the field
    * is unset.
    */
   public Value getFieldObject(Env env, StringValue name) {
      Value v = getField(env, name);

      if (!v.isset()) {
         v = env.createObject();

         putField(env, name, v);
      }

      return v;
   }

   /**
    * Returns the value for a field, creating an object if the field
    * is unset.
    */
   public Value getFieldArray(Env env, StringValue name) {
      Value v = getField(env, name);

      Value array = v.toAutoArray();

      if (v == array) {
         return v;
      } else {
         putField(env, name, array);

         return array;
      }
   }

   /**
    * Returns the field ref.
    */
   public Value putField(Env env, StringValue name, Value object) {
      return NullValue.NULL;
   }

   public final Value putField(Env env, StringValue name, Value value,
           Value innerIndex, Value innerValue) {
      Value result = value.append(innerIndex, innerValue);

      return putField(env, name, result);
   }

   public void setFieldInit(boolean isInit) {
   }

   /**
    * Returns true if the object is in a __set() method call.
    * Prevents infinite recursion.
    */
   public boolean isFieldInit() {
      return false;
   }

   /**
    * Returns true if the field is set
    */
   public boolean issetField(StringValue name) {
      return false;
   }

   /**
    * Removes the field ref.
    */
   public void unsetField(StringValue name) {
   }

   /**
    * Removes the field ref.
    */
   public void unsetArray(Env env, StringValue name, Value index) {
   }

   /**
    * Removes the field ref.
    */
   public void unsetThisArray(Env env, StringValue name, Value index) {
   }

   /**
    * Returns the field as a Var or Value.
    */
   public Value getThisField(Env env, StringValue name) {
      return getField(env, name);
   }

   /**
    * Returns the field as a Var.
    */
   public Var getThisFieldVar(Env env, StringValue name) {
      return getThisField(env, name).toVar();
   }

   /**
    * Returns the field used as a method argument
    */
   public Value getThisFieldArg(Env env, StringValue name) {
      return getThisFieldVar(env, name);
   }

   /**
    * Returns the field ref for an argument.
    */
   public Value getThisFieldArgRef(Env env, StringValue name) {
      return getThisFieldVar(env, name);
   }

   /**
    * Returns the value for a field, creating an object if the field
    * is unset.
    */
   public Value getThisFieldObject(Env env, StringValue name) {
      Value v = getThisField(env, name);

      if (!v.isset()) {
         v = env.createObject();

         putThisField(env, name, v);
      }

      return v;
   }

   /**
    * Returns the value for a field, creating an object if the field
    * is unset.
    */
   public Value getThisFieldArray(Env env, StringValue name) {
      Value v = getThisField(env, name);

      Value array = v.toAutoArray();

      if (v == array) {
         return v;
      } else {
         putField(env, name, array);

         return array;
      }
   }

   /**
    * Initializes a new field, does not call __set if it is defined.
    */
   public void initField(StringValue key,
           Value value,
           FieldVisibility visibility) {
      putThisField(Env.getInstance(), key, value);
   }

   /**
    * Returns the field ref.
    */
   public Value putThisField(Env env, StringValue name, Value object) {
      return putField(env, name, object);
   }

   /**
    * Sets an array field ref.
    */
   public Value putThisField(Env env,
           StringValue name,
           Value array,
           Value index,
           Value value) {
      Value result = array.append(index, value);

      putThisField(env, name, result);

      return value;
   }

   /**
    * Returns true if the field is set
    */
   public boolean issetThisField(StringValue name) {
      return issetField(name);
   }

   /**
    * Removes the field ref.
    */
   public void unsetThisField(StringValue name) {
      unsetField(name);
   }

   //
   // field convenience
   //
   public Value putField(Env env, String name, Value value) {
      return putThisField(env, env.createString(name), value);
   }

   /**
    * Returns the array ref.
    */
   public Value get(Value index) {
      return UnsetValue.UNSET;
   }

   /**
    * Returns a reference to the array value.
    */
   public Var getVar(Value index) {
      Value value = get(index);

      if (value.isVar()) {
         return (Var) value;
      } else {
         return new Var(value);
      }
   }

   /**
    * Returns a reference to the array value.
    */
   public Value getRef(Value index) {
      return get(index);
   }

   /**
    * Returns the array ref as a function argument.
    */
   public Value getArg(Value index, boolean isTop) {
      return get(index);
   }

   /**
    * Returns the array value, copying on write if necessary.
    */
   public Value getDirty(Value index) {
      return get(index);
   }

   /**
    * Returns the value for a field, creating an array if the field
    * is unset.
    */
   public Value getArray() {
      return this;
   }

   /**
    * Returns the value for a field, creating an array if the field
    * is unset.
    */
   public Value getArray(Value index) {
      Value var = getVar(index);

      return var.toAutoArray();
   }

   /**
    * Returns the value for the variable, creating an object if the var
    * is unset.
    */
   public Value getObject(Env env) {
      return NullValue.NULL;
   }

   /**
    * Returns the value for a field, creating an object if the field
    * is unset.
    */
   public Value getObject(Env env, Value index) {
      Value var = getVar(index);

      if (var.isset()) {
         return var.toValue();
      } else {
         var.set(env.createObject());

         return var.toValue();
      }
   }

   public boolean isVar() {
      return false;
   }

   /**
    * Sets the value ref.
    */
   public Value set(Value value) {
      return value;
   }

   /**
    * Sets the array ref and returns the value
    */
   public Value put(Value index, Value value) {
      Env.getCurrent().warning(L.l("{0} cannot be used as an array",
              toDebugString()));

      return value;
   }

   /**
    * Sets the array ref.
    */
   public final Value put(Value index, Value value,
           Value innerIndex, Value innerValue) {
      Value result = value.append(innerIndex, innerValue);

      put(index, result);

      return innerValue;
   }

   /**
    * Appends an array value
    */
   public Value put(Value value) {
      /*
      Env.getCurrent().warning(L.l("{0} cannot be used as an array",
      toDebugString()));
       */


      return value;
   }

   /**
    * Sets the array value, returning the new array, e.g. to handle
    * string update ($a[0] = 'A').  Creates an array automatically if
    * necessary.
    */
   public Value append(Value index, Value value) {
      Value array = toAutoArray();

      if (array.isArray()) {
         return array.append(index, value);
      } else {
         return array;
      }
   }

   /**
    * Sets the array tail, returning the Var of the tail.
    */
   public Var putVar() {
      return new Var();
   }

   /**
    * Appends a new object
    */
   public Value putObject(Env env) {
      Value value = env.createObject();

      put(value);

      return value;
   }

   /**
    * Return true if the array value is set
    */
   public boolean isset(Value index) {
      return false;
   }

   /**
    * Returns true if the key exists in the array.
    */
   public boolean keyExists(Value key) {
      return isset(key);
   }

   /**
    * Returns the corresponding value if this array contains the given key
    *
    * @param key to search for in the array
    *
    * @return the value if it is found in the array, NULL otherwise
    */
   public Value containsKey(Value key) {
      return null;
   }

   /**
    * Return unset the value.
    */
   public Value remove(Value index) {
      return UnsetValue.UNSET;
   }

   /**
    * Takes the values of this array, unmarshalls them to objects of type
    * <i>elementType</i>, and puts them in a java array.
    */
   public Object valuesToArray(Env env, Class elementType) {
      env.error(L.l("Can't assign {0} with type {1} to {2}[]",
              this,
              this.getClass(),
              elementType));
      return null;
   }

   /**
    * Returns the character at the named index.
    */
   public Value charValueAt(long index) {
      return NullValue.NULL;
   }

   /**
    * Sets the character at the named index.
    */
   public Value setCharValueAt(long index, Value value) {
      return NullValue.NULL;
   }

   /**
    * Prints the value.
    * @param env
    */
   public void print(Env env) {
      env.print(toString(env));
   }

   /**
    * Prints the value.
    * @param env
    */
   public void print(Env env, WriteStream out) {
      try {
         out.print(toString(env));
      } catch (IOException e) {
         throw new QuercusRuntimeException(e);
      }
   }

   /**
    * Serializes the value.
    *
    * @param env
    * @param sb holds result of serialization
    * @param serializeMap holds reference indexes
    */
   public void serialize(Env env,
           StringBuilder sb,
           SerializeMap serializeMap) {
      serializeMap.incrementIndex();

      serialize(env, sb);
   }

   /**
    * Encodes the value in JSON.
    */
   public void jsonEncode(Env env, StringValue sb) {
      env.warning(L.l("type is unsupported; json encoded as null"));

      sb.append("null");
   }

   /**
    * Serializes the value.
    */
   public void serialize(Env env, StringBuilder sb) {
      throw new UnsupportedOperationException(getClass().getName());
   }

   /**
    * Exports the value.
    */
   public void varExport(StringBuilder sb) {
      throw new UnsupportedOperationException(getClass().getName());
   }

   /**
    * Binds a Java object to this object.
    */
   public void setJavaObject(Value value) {
   }

   //
   // Java generator code
   //
   /**
    * Generates code to recreate the expression.
    *
    * @param out the writer to the Java source code.
    */
   public void generate(PrintWriter out)
           throws IOException {
   }

   protected static void printJavaChar(PrintWriter out, char ch) {
      switch (ch) {
         case '\r':
            out.print("\\r");
            break;
         case '\n':
            out.print("\\n");
            break;
         //case '\"':
         //  out.print("\\\"");
         //  break;
         case '\'':
            out.print("\\\'");
            break;
         case '\\':
            out.print("\\\\");
            break;
         default:
            out.print(ch);
            break;
      }
   }

   protected static void printJavaString(PrintWriter out, StringValue s) {
      if (s == null) {
         out.print("");
         return;
      }

      int len = s.length();
      for (int i = 0; i < len; i++) {
         char ch = s.charAt(i);

         switch (ch) {
            case '\r':
               out.print("\\r");
               break;
            case '\n':
               out.print("\\n");
               break;
            case '\"':
               out.print("\\\"");
               break;
            case '\'':
               out.print("\\\'");
               break;
            case '\\':
               out.print("\\\\");
               break;
            default:
               out.print(ch);
               break;
         }
      }
   }

   public String toInternString() {
      return toString().intern();
   }

   public String toDebugString() {
      return toString();
   }

   public final void varDump(Env env,
           WriteStream out,
           int depth,
           IdentityHashMap<Value, String> valueSet)
           throws IOException {
      if (valueSet.get(this) != null) {
         out.print("*recursion*");
         return;
      }

      valueSet.put(this, "printing");

      try {
         varDumpImpl(env, out, depth, valueSet);
      } finally {
         valueSet.remove(this);
      }
   }

   protected void varDumpImpl(Env env,
           WriteStream out,
           int depth,
           IdentityHashMap<Value, String> valueSet)
           throws IOException {
      out.print("resource(" + toString() + ")");
   }

   public final void printR(Env env,
           WriteStream out,
           int depth,
           IdentityHashMap<Value, String> valueSet)
           throws IOException {
      if (valueSet.get(this) != null) {
         out.print("*recursion*");
         return;
      }

      valueSet.put(this, "printing");

      try {
         printRImpl(env, out, depth, valueSet);
      } finally {
         valueSet.remove(this);
      }
   }

   protected void printRImpl(Env env,
           WriteStream out,
           int depth,
           IdentityHashMap<Value, String> valueSet)
           throws IOException {
      out.print(toString());
   }

   protected void printDepth(WriteStream out, int depth)
           throws IOException {
      for (int i = 0; i < depth; i++) {
         out.print(' ');
      }
   }

   public int getHashCode() {
      return hashCode();
   }

   @Override
   public int hashCode() {
      return 1021;
   }
}
=======
package ncsa.d2k.modules.core.prediction.decisiontree.continuous;

import ncsa.d2k.modules.core.datatype.sort.*;
import ncsa.d2k.modules.core.prediction.*;
import ncsa.d2k.modules.core.prediction.regression.continuous.*;
import ncsa.d2k.modules.core.prediction.mean.continuous.*;
import ncsa.d2k.modules.core.datatype.table.*;
import ncsa.d2k.modules.core.datatype.model.*;
import ncsa.d2k.modules.core.datatype.parameter.*;
import ncsa.d2k.modules.core.datatype.parameter.impl.*;

import ncsa.d2k.core.modules.*;


public class DecisionTreeInducerOpt extends FunctionInducerOpt {

   boolean UseMeanNodeModels          = true;
   boolean UseLinearNodeModels        = false;
   boolean UseOneHalfSplit      = false;
   boolean UseMidPointBasedSplit      = false;
   boolean UseMeanBasedSplit          = true;
   boolean UsePopulationBasedSplit    = false;
   boolean SaveNodeExamples           = false;
   int     MinDecompositionPopulation = 20;
   double  MinErrorReduction          = 0.0;

   boolean PrintEvolvingModels = false;

  public String getModuleName() {
    return "Decision Tree Inducer Optimizable";
  }

  public String getModuleInfo() {

    String s = "";
    s += "<p>";
    s += "Overview: ";
    s += "This module builds a decision tree from an example table. </p>";
    s += "<p>Detailed Description: ";
    s += "The module implements a decision tree learning algorithm for continuous attributes. ";
    s += "For inducing functions within a node, one of two different learning algorithms can be used. ";
    s += "<ul>";
    s += "<li><i>Use the mean averaging for models</i>:  Simple averaging of output values.  </li>";
    s += "<li><i>Use multiple regression for models</i>:  Using the best 1-d linear function using a single input attribute.  </li>";
    s += "</ul>";

    s += "It allows for multiple decomposition strategies to be used simultaneously. ";

    s += "<ul>";
    s += "<li><i>Generate splits only at 1/2</i>:  Generate splits only at 0.5.  Works well when inputs are scaled from 0.0 to 1.0.</li>";
    s += "<li><i>Generate mean splits</i>:  Generate splits at the mean input attribute value within the node.  </li>";
    s += "<li><i>Generate midpoint splits</i>:  Generate splits at the midpoint between the min and max input attribute value within the node.  </li>";
    s += "<li><i>Generate median splits</i>:  Generate splits at the median input attribute value within the node which requires n log n sorting.  </li> ";
    s += "</ul>";

    s += "Determining whether to split a node is controlled by the following parameters: ";
    s += "<i>Minimum examples per leaf</i> and <i>Minimum split error reduction</i>.  ";
    s += "A split is not considered if it results in a node with less than <i>Minimum examples per leaf</i> examples in it.  ";
    s += "A split is not considered if the resubstitution based error weighted by population ";
    s += "does improve by at least <i>Minimum split error reduction</i>.  ";

    s += "<p>";
    s += "Restrictions: ";
    s += "This module will only classify examples with ";
    s += "continuous outputs.  </p>";

    s += "<p>Data Handling: This module does not modify the input data. </p>";

    s += "<p>Scalability: This module can efficiently process a data set that can be stored in memory.  ";
    s += "The ultimate limit is how much virtual memory java can access. </p> ";

    return s;
  }



  public void setControlParameters(ParameterPoint parameterPoint) {

    MinDecompositionPopulation  = (int) parameterPoint.getValue(0);
    MinErrorReduction           =       parameterPoint.getValue(1);

    UseOneHalfSplit       = false;
    if (parameterPoint.getValue(2) > 0.5) UseOneHalfSplit   = true;
    UseMidPointBasedSplit       = false;
    if (parameterPoint.getValue(3) > 0.5) UseMidPointBasedSplit   = true;
    UseMeanBasedSplit           = false;
    if (parameterPoint.getValue(4) > 0.5) UseMeanBasedSplit       = true;
    UsePopulationBasedSplit     = false;
    if (parameterPoint.getValue(5) > 0.5) UsePopulationBasedSplit = true;
    SaveNodeExamples            = false;
    if (parameterPoint.getValue(6) > 0.5) SaveNodeExamples        = true;
    UseMeanNodeModels           = false;
    if (parameterPoint.getValue(7) > 0.5) UseMeanNodeModels       = true;
    UseLinearNodeModels         = false;
    if (parameterPoint.getValue(8) > 0.5) UseLinearNodeModels     = true;
  }

  public Model generateModel(ExampleTable examples, ErrorFunction errorFunction) throws Exception {

    //call superclass constructor using example table to initialize the names;

    numInputs   = examples.getNumInputFeatures();
    numOutputs  = examples.getNumOutputFeatures();
    inputNames  = new String[numInputs];
    outputNames = new String[numOutputs];
    for (int i = 0; i < numInputs; i++)
      inputNames[i] = examples.getInputName(i);
    for (int i = 0; i < numOutputs; i++)
      outputNames[i] = examples.getOutputName(i);


    this.errorFunction = errorFunction;
    DecisionTreeNode decisionTree = createDecisionTree(examples);
    decisionTree.examples = null;

    DecisionTreeModel model = new DecisionTreeModel(examples, decisionTree);
    //model.instantiate(decisionTree);

    RootNode = null;
    return (Model) model;
  }


  ModelPrintOptions ModelPrintOptions = new ModelPrintOptions();

  public void calculateOutputMeansMinsMaxs(DecisionTreeNode node) throws Exception {

    ExampleTable examples = node.examples;

    int numExamples = examples.getNumRows();

    double [] outputSums = new double[numOutputs];
    double [] outputMins = new double[numOutputs];
    double [] outputMaxs = new double[numOutputs];

    for (int f = 0; f < numOutputs; f++) {
      outputMins[f] = Double.MAX_VALUE;
      outputMaxs[f] = Double.MIN_VALUE;
    }

    for (int e = 0; e < numExamples; e++) {
      for (int f = 0; f < numOutputs; f++) {
        double value = examples.getOutputDouble(e, f);
        outputSums[f] += value;
        if (value < outputMins[f])
          outputMins[f] = value;
        if (value > outputMaxs[f])
          outputMaxs[f] = value;
      }
    }

    for (int f = 0; f < numOutputs; f++){
      outputSums[f] /= numExamples;
    }

    node.outputMeans = outputSums;
    node.outputMins  = outputMins;
    node.outputMaxs  = outputMaxs;
  }

  public void createModel(DecisionTreeNode node) throws Exception {

    ExampleTable examples = node.examples;

    if (UseMeanNodeModels || (!UseMeanNodeModels && !UseLinearNodeModels)) {
      int numExamples = examples.getNumRows();

      double [] outputSums = new double[numOutputs];

      for (int e = 0; e < numExamples; e++) {
        for (int f = 0; f < numOutputs; f++) {
          outputSums[f] += examples.getOutputDouble(e, f);
        }
      }

      for (int f = 0; f < numOutputs; f++){
        outputSums[f] /= numExamples;
      }

      // instantiate model //

      MeanOutputModel model = null;
      if (RootNode.model == null)
        model = new MeanOutputModel(examples, outputSums);
      else
        model = new MeanOutputModel(RootNode.model.getTrainingSetSize(),
                              RootNode.model.getInputColumnLabels(),
                              RootNode.model.getOutputColumnLabels(),
                              RootNode.model.getInputFeatureTypes(),
                              RootNode.model.getOutputFeatureTypes(),
                              outputSums);

      //model.Instantiate(numInputs, numOutputs, inputNames, outputNames, outputSums);

      node.model = model;
    }
    if (UseLinearNodeModels) {

      double[] bias = new double[3];
      String[] biasNames = new String[3];

      int biasIndex = 0;

      biasNames[biasIndex] = "UseStepwise";
      bias[biasIndex] = 1;
      biasIndex++;
      biasNames[biasIndex] = "Direction";
      bias[biasIndex] = 1;
      biasIndex++;
      biasNames[biasIndex] = "NumRounds";
      bias[biasIndex] = 1;
      biasIndex++;

      //ParameterPointImpl parameterPoint = new ParameterPointImpl();
      //parameterPoint.createFromData(biasNames, bias);

      ParameterPoint parameterPoint
	  = ParameterPointImpl.getParameterPoint(biasNames, bias);

      StepwiseLinearInducerOpt inducer = new StepwiseLinearInducerOpt();

      inducer.setControlParameters(parameterPoint);

      Model model = inducer.generateModel(examples, errorFunction);

      node.model = model;
    }

  }

  ErrorFunction errorFunction = null;
  public void evaluateModel(DecisionTreeNode node) throws Exception {

    ExampleTable examples = node.examples;

    int numExamples = examples.getNumRows();

    PredictionTable predictionTable = examples.toPredictionTable();
    node.model.predict(predictionTable);

    double error = errorFunction.evaluate(node.examples, predictionTable) * numExamples;

    node.error = error;

    if (_Trace) {
      System.out.println("node.error = " + node.error);
    }
  }

  DecisionTreeNode RootNode;
  int              NodeIndex;
  public DecisionTreeNode createDecisionTree(ExampleTable examples) throws Exception {

    NodeIndex = 0;
    int depth = 0;
    RootNode = new DecisionTreeNode(NodeIndex, depth);
    NodeIndex++;
    RootNode.examples = examples;
    RootNode.numExamples = examples.getNumRows();
    RootNode.root = RootNode;


    calculateOutputMeansMinsMaxs(RootNode);

    if (_Trace) {
      System.out.println("calculating average outputs");
    }

    if (_Trace) {
      System.out.println("creating initial tree");
    }

    createModel(RootNode);

    if (_Trace) {
      System.out.println("evaluating initial tree");
    }

    evaluateModel(RootNode);

    if (PrintEvolvingModels) {
      DecisionTreeModel model = new DecisionTreeModel(examples, RootNode);
      //model.instantiate(numInputs, numOutputs, inputNames, outputNames, RootNode);
      model.print(ModelPrintOptions);
      System.out.println();
    }

    if (_Trace) {
      System.out.println("recursive partitioning");
    }

    recursiveDecomposition(RootNode, depth);

    return RootNode;
  }


  double [][] SortArray;

  public Decomposition [] createDecompositions(DecisionTreeNode node) {
    ExampleTable examples = node.examples;
    int numExamples = examples.getNumRows();

    int numSplitMethods = 0;

    if (UseOneHalfSplit)   numSplitMethods++;
    if (UseMidPointBasedSplit)   numSplitMethods++;
    if (UseMeanBasedSplit)       numSplitMethods++;
    if (UsePopulationBasedSplit) numSplitMethods++;


    int numDecompositions = numInputs * numSplitMethods;
    int decompositionIndex = 0;

    Decomposition [] decompositions = new Decomposition[numDecompositions];


    for (int inputIndex = 0; inputIndex < numInputs; inputIndex++){

      if (UseOneHalfSplit){
        decompositions[decompositionIndex] = new Decomposition();
        decompositions[decompositionIndex].inputIndex = inputIndex;
        decompositions[decompositionIndex].value      = 0.5;
        decompositionIndex++;
      }

      if (UseMidPointBasedSplit){
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int e = 0; e < numExamples; e++) {
          double value = examples.getInputDouble(e, inputIndex);
          if (value < min)
            min = value;
          if (value > max)
            max = value;
        }

        decompositions[decompositionIndex] = new Decomposition();
        decompositions[decompositionIndex].inputIndex = inputIndex;
        decompositions[decompositionIndex].value      = (max - min) / 2.0;
        decompositionIndex++;
      }

      if (UseMeanBasedSplit) {
        double sum = 0.0;
        for (int e = 0; e < numExamples; e++) {
          double value = examples.getInputDouble(e, inputIndex);
          sum += value;
        }

        decompositions[decompositionIndex] = new Decomposition();
        decompositions[decompositionIndex].inputIndex = inputIndex;
        decompositions[decompositionIndex].value      = sum / numExamples;
        decompositionIndex++;
      }

      if (UsePopulationBasedSplit) {
        if ((SortArray == null) || (SortArray.length != numExamples)) {
          SortArray = new double[numExamples][1];
        }
        for (int e = 0; e < numExamples; e++) {
          double value = examples.getInputDouble(e, inputIndex);

          SortArray[e][0] = value;
        }

        QuickSort.sort(SortArray);

        double splitValue = SortArray[(numExamples - 1) / 2][0];

        decompositions[decompositionIndex] = new Decomposition();
        decompositions[decompositionIndex].inputIndex = inputIndex;
        decompositions[decompositionIndex].value      = splitValue;
        decompositionIndex++;
      }
    }

    return decompositions;
  }

/*
This method partitions the given example table into two tables based on the given decompostion.
The example table assigned to node1 contains the examples that the decomposition function evaluates true for
and the example table assigned to node2 contains the examples that the decomposition function evaluates false for.
*/


  int [] workNode1ExampleIndices;
  int [] workNode2ExampleIndices;

  public void partitionExamples (ExampleTable examples,
                                 DecisionTreeNode parrentNode,
                                 DecisionTreeNode node1,
                                 DecisionTreeNode node2,
                                 Decomposition decomposition) throws Exception {

    if (workNode1ExampleIndices == null || workNode1ExampleIndices.length < examples.getNumRows()) {
      workNode1ExampleIndices = new int[examples.getNumRows()];
      workNode2ExampleIndices = new int[examples.getNumRows()];
    }

    int numExamples = examples.getNumRows();
    int numNode1Examples = 0;
    int numNode2Examples = 0;
    for (int e = 0; e < numExamples; e++) {

      if (decomposition.evaluate(examples, e)) {
        workNode1ExampleIndices[numNode1Examples++] = e;
      }
      else {
        workNode2ExampleIndices[numNode2Examples++] = e;
      }
    }

    int [] node1ExampleIndicies = new int[numNode1Examples];
    int [] node2ExampleIndicies = new int[numNode2Examples];

    for (int i = 0; i < numNode1Examples; i++) {
      node1ExampleIndicies[i] = workNode1ExampleIndices[i];
    }
    for (int i = 0; i < numNode2Examples; i++) {
      node2ExampleIndicies[i] = workNode2ExampleIndices[i];
    }



//!!! should I just use the train set method for example set subseting or both? what about n-way subsetting?
//luckily, for now I can assume that at least the train test set are independent;

    //ExampleTable node1ExampleSet = (ExampleTable) examples.copy();
    //node1ExampleSet.setTestingSet(node1ExampleIndicies);
    node1.examples = (ExampleTable) examples.getSubset(node1ExampleIndicies);
 
    //ExampleTable node2ExampleSet = (ExampleTable) examples.copy();
    //node2ExampleSet.setTestingSet(node2ExampleIndicies);
    node2.examples = (ExampleTable) examples.getSubset(node2ExampleIndicies);

    //System.out.println("node1.examples.numExamples() = " + node1.examples.getNumRows());
    //System.out.println("node2.examples.numExamples() = " + node2.examples.getNumRows());

    node1.numExamples = node1.examples.getNumRows();
    node2.numExamples = node2.examples.getNumRows();

    node1.root = parrentNode.root;
    node2.root = parrentNode.root;

  }


  public DecisionTreeNode recursiveDecomposition(DecisionTreeNode node, int depth) throws Exception {

    if (node.examples.getNumRows() < MinDecompositionPopulation)
      return node;

    double parentError = node.error;


    if (_Trace){
    System.out.println("creating decompositions");}

    Decomposition [] decompositions = createDecompositions(node);
    int numDecompositions = decompositions.length;

    //!!!
    //ExampleTable examples = (ExampleTable) node.examples.copy();
    ExampleTable examples = node.examples;
    int    numExamples = examples.getNumRows();



    double bestErrorReduction = Double.NEGATIVE_INFINITY;
    Decomposition bestDecomposition = null;

    DecisionTreeNode     childNode1 = new DecisionTreeNode();
    //childNode1.examples = examples.shallowCopy();
    //childNode1.examples.allocateExamplePointers(numExamples);

    DecisionTreeNode     childNode2 = new DecisionTreeNode();
    //childNode2.examples = examples.shallowCopy();
    //childNode2.examples.allocateExamplePointers(numExamples);

    DecisionTreeNode bestChildNode1 = new DecisionTreeNode();
    //bestChildNode1.examples = examples.shallowCopy();
    //bestChildNode1.examples.allocateExamplePointers(numExamples);

    DecisionTreeNode bestChildNode2 = new DecisionTreeNode();
    //bestChildNode2.examples = examples.shallowCopy();
    //bestChildNode2.examples.allocateExamplePointers(numExamples);

    if (_Trace) {
      System.out.println("evaluating decompositions");
    }

    for (int decompositionIndex = 0; decompositionIndex < numDecompositions; decompositionIndex++) {

      Decomposition decomposition = decompositions[decompositionIndex];


      // count examples in each child
      int numNode1Examples = 0;
      int numNode2Examples = 0;
      for (int e = 0; e < numExamples; e++) {
        if (decomposition.evaluate(examples, e)) {
          numNode1Examples++;
        }
        else {
          numNode2Examples++;
        }
      }

      if (_Trace) {
        System.out.println("numNode1Examples = " + numNode1Examples);
        System.out.println("numNode2Examples = " + numNode2Examples);
      }

      if ((numNode1Examples < MinDecompositionPopulation) ||
          (numNode2Examples < MinDecompositionPopulation))
        continue;


      // partition examples
      //childNode1.examples.allocateExamplePointers(numNode1Examples);
      //childNode2.examples.allocateExamplePointers(numNode2Examples);
      partitionExamples(examples, node, childNode1, childNode2, decomposition);

      createModel(childNode1);
      createModel(childNode2);

      // evaluate error
      evaluateModel(childNode1);
      evaluateModel(childNode2);

      double error1 = childNode1.error;
      double error2 = childNode2.error;

      double errorReduction = parentError - error1 - error2;

      if (_Trace) {
        System.out.println("error1 = " + error1);
        System.out.println("error2 = " + error2);
        System.out.println("errorReduction = " + errorReduction);
      }

      if ((errorReduction > bestErrorReduction) &&
          ((numNode1Examples > 0) && (numNode2Examples > 0))) {

        bestErrorReduction = errorReduction;
        bestDecomposition  = decomposition;

        DecisionTreeNode tempNode;

        tempNode = bestChildNode1;  bestChildNode1 = childNode1;  childNode1 = tempNode;
        tempNode = bestChildNode2;  bestChildNode2 = childNode2;  childNode2 = tempNode;
      }

    }

    if (_Trace) {
      System.out.println("bestErrorReduction = " + bestErrorReduction);
    }

    if (!SaveNodeExamples) {
      node.examples = null;
    }

    // !!!
    childNode1 = null;
    childNode2 = null;


    if ((bestErrorReduction > MinErrorReduction * RootNode.numExamples)) {
      if (_Trace) {
        System.out.println("bestDecomposition.inputIndex = " + bestDecomposition.inputIndex);
        System.out.println("bestDecomposition.value      = " + bestDecomposition.value);
      }

      node.decomposition = bestDecomposition;
      node.bestErrorReduction = bestErrorReduction;

      node.childNode1 = bestChildNode1;
      node.childNode1.parent = node;
      node.childNode1.index = NodeIndex++;
      node.childNode1.depth = depth + 1;

      node.childNode2 = bestChildNode2;
      node.childNode2.parent = node;
      node.childNode2.index = NodeIndex++;
      node.childNode2.depth = depth + 1;

      if (PrintEvolvingModels) {
        DecisionTreeModel model = new DecisionTreeModel(examples, RootNode);
        //model.instantiate(numInputs, numOutputs, inputNames, outputNames, RootNode);
        model.print(ModelPrintOptions);
        System.out.println();
      }

      recursiveDecomposition(bestChildNode1, depth + 1);
      bestChildNode1 = null;
      recursiveDecomposition(bestChildNode2, depth + 1);
      bestChildNode2 = null;
    }

    return node;
  }


    //QA Anca added this:
    public PropertyDescription[] getPropertiesDescriptions() {
	// so that "ordered and _trace" property are invisible
        return new PropertyDescription[0];
    }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

