<<<<<<< HEAD
package net.sf.saxon.value;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.*;
import net.sf.saxon.functions.Count;
import net.sf.saxon.om.*;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.*;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.tree.util.FastStringBuffer;

import java.io.Serializable;

/**
* A value is the result of an expression but it is also an expression in its own right.
* Note that every value can be regarded as a sequence - in many cases, a sequence of
* length one.
*/

public abstract class Value
        implements Serializable, SequenceIterable, ValueRepresentation {

    /**
     * Static method to make a Value from a given Item (which may be either an AtomicValue
     * or a NodeInfo or a FunctionItem
     * @param val       The supplied value, or null, indicating the empty sequence.
     * @return          The supplied value, if it is a value, or a SingletonNode that
     *                  wraps the item, if it is a node. If the supplied value was null,
     *                  return an EmptySequence
     */

    public static Value asValue(ValueRepresentation val) {
        if (val instanceof Value) {
            return (Value)val;
        } else if (val == null) {
            return EmptySequence.getInstance();
        } else {
            return new SingletonItem((Item)val);
        }
    }

    /**
     * Static method to make an Item from a Value
     * @param value the value to be converted
     * @return null if the value is an empty sequence; or the only item in the value
     * if it is a singleton sequence
     * @throws XPathException if the Value contains multiple items
     */

    public static Item asItem(ValueRepresentation value) throws XPathException {
        if (value instanceof Item) {
            return (Item)value;
        } else {
            return ((Value)value).asItem();
        }
    }

    /**
     * Return the value in the form of an Item
     * @return the value in the form of an Item
     */

    public Item asItem() throws XPathException {
        SequenceIterator iter = iterate();
        Item item = iter.next();
        if (item == null) {
            return null;
        } else if (iter.next() != null) {
            throw new XPathException("Attempting to access a sequence as a singleton item");
        } else {
            return item;
        }
    }

    /**
     * Static method to get a Value from an Item
     * @param item the supplied item
     * @return the item expressed as a Value
     */

    public static Value fromItem(Item item) {
        if (item == null) {
            return EmptySequence.getInstance();
        } else if (item instanceof AtomicValue) {
            return (AtomicValue)item;
        } else {
            return new SingletonItem(item);
        }
    }

    /**
     * Static method to get an Iterator over any ValueRepresentation (which may be either a Value
     * or a NodeInfo or a FunctionItem
     * @param val       The supplied value, or null, indicating the empty sequence.
     * @return          The supplied value, if it is a value, or a SingletonNode that
     *                  wraps the item, if it is a node. If the supplied value was null,
     *                  return an EmptySequence
     */

    public static SequenceIterator asIterator(ValueRepresentation val) throws XPathException {
        if (val instanceof Value) {
            return ((Value)val).iterate();
        } else if (val == null) {
            return EmptyIterator.getInstance();
        } else {
            return SingletonIterator.makeIterator((Item)val);
        }
    }

    /**
     * Get a SequenceIterator over a ValueRepresentation
     * @param val the value to iterate over
     * @return the iterator
     */

    public static SequenceIterator getIterator(ValueRepresentation val) throws XPathException {
        if (val instanceof Value) {
            return ((Value)val).iterate();
        } else if (val instanceof Item) {
            return SingletonIterator.makeIterator((Item)val);
        } else if (val == null) {
            throw new AssertionError("Value of variable is undefined (null)");
        } else {
            throw new AssertionError("Unknown value representation " + val.getClass());
        }
    }

    /**
     * Iterate over the items contained in this value.
     * @return an iterator over the sequence of items
     * @throws XPathException if a dynamic error occurs. This is possible only in the case of values
     * that are materialized lazily, that is, where the iterate() method leads to computation of an
     * expression that delivers the values.
     */

    public abstract SequenceIterator iterate() throws XPathException;

    /**
     * Return an iterator over the results of evaluating an expression
     * @param context the dynamic evaluation context (not used in this implementation)
     * @return an iterator over the items delivered by the expression
     */

    public final SequenceIterator iterate(XPathContext context) throws XPathException {
        // Note, this method, and the SequenceIterable interface, are used from XQuery compiled code
        return iterate();
    }

    /**
     * Get the value of the item as a CharSequence. This is in some cases more efficient than
     * the version of the method that returns a String.
     */

    public CharSequence getStringValueCS() throws XPathException {
        return getStringValue();
    }

    /**
     * Get the canonical lexical representation as defined in XML Schema. This is not always the same
     * as the result of casting to a string according to the XPath rules.
     * @return the canonical lexical representation if defined in XML Schema; otherwise, the result
     * of casting to string according to the XPath 2.0 rules
     */

    public CharSequence getCanonicalLexicalRepresentation() {
        try {
            return getStringValueCS();
        } catch (XPathException err) {
            throw new IllegalStateException("Failed to get canonical lexical representation: " + err.getMessage());
        }
    }

    /**
     * Determine the data type of the items in the expression, if possible
     * @return for the default implementation: AnyItemType (not known)
     * @param th The TypeHierarchy. Can be null if the target is an AtomicValue.
     */

    public ItemType getItemType(TypeHierarchy th) {
        return AnyItemType.getInstance();
    }

    /**
     * Determine the cardinality
     * @return the cardinality
     */

    public int getCardinality() {
        try {
            SequenceIterator iter = iterate();
            Item next = iter.next();
            if (next == null) {
                return StaticProperty.EMPTY;
            } else {
                if (iter.next() != null) {
                    return StaticProperty.ALLOWS_ONE_OR_MORE;
                } else {
                    return StaticProperty.EXACTLY_ONE;
                }
            }
        } catch (XPathException err) {
            // can't actually happen
            return StaticProperty.ALLOWS_ZERO_OR_MORE;
        }
    }

    /**
     * Get the n'th item in the sequence (starting from 0). This is defined for all
     * Values, but its real benefits come for a sequence Value stored extensionally
     * (or for a MemoClosure, once all the values have been read)
     * @param n position of the required item, counting from zero.
     * @return the n'th item in the sequence, where the first item in the sequence is
     * numbered zero. If n is negative or >= the length of the sequence, returns null.
     */

    public Item itemAt(int n) throws XPathException {
        if (n < 0) {
            return null;
        }
        int i = 0;        // indexing is zero-based
        SequenceIterator iter = iterate();
        while (true) {
            Item item = iter.next();
            if (item == null) {
                return null;
            }
            if (i++ == n) {
                return item;
            }
        }
    }

    /**
     * Get the length of the sequence
     * @return the number of items in the sequence
     */

    public int getLength() throws XPathException {
        return Count.count(iterate());
    }

    /**
      * Process the value as an instruction, without returning any tail calls
      * @param context The dynamic context, giving access to the current node,
      * the current variables, etc.
      */

    public void process(XPathContext context) throws XPathException {
        SequenceIterator iter = iterate();
        SequenceReceiver out = context.getReceiver();
        while (true) {
            Item it = iter.next();
            if (it==null) break;
            out.append(it, 0, NodeInfo.ALL_NAMESPACES);
        }
    }


    /**
     * Convert the value to a string, using the serialization rules.
     * For atomic values this is the same as a cast; for sequence values
     * it gives a space-separated list.
     * @throws XPathException The method can fail if evaluation of the value
     * has been deferred, and if a failure occurs during the deferred evaluation.
     * No failure is possible in the case of an AtomicValue.
     */

    public String getStringValue() throws XPathException {
        FastStringBuffer sb = new FastStringBuffer(FastStringBuffer.SMALL);
        SequenceIterator iter = iterate();
        Item item = iter.next();
        if (item != null) {
            while (true) {
                sb.append(item.getStringValueCS());
                item = iter.next();
                if (item == null) {
                    break;
                }
                sb.append(' ');
            }
        }
        return sb.toString();
    }


    /**
     * Get the effective boolean value of the expression. This returns false if the value
     * is the empty sequence, a zero-length string, a number equal to zero, or the boolean
     * false. Otherwise it returns true.
     *
     * @exception XPathException if any dynamic error occurs evaluating the
     *     expression
     * @return the effective boolean value
     */

    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(iterate());
    }

    /**
     * Get a Comparable value that implements the XML Schema ordering comparison semantics for this value.
     * The default implementation is written to compare sequences of atomic values.
     * This method is overridden for AtomicValue and its subclasses.
     *
     * <p>In the case of data types that are partially ordered, the returned Comparable extends the standard
     * semantics of the compareTo() method by returning the value {@link #INDETERMINATE_ORDERING} when there
     * is no defined order relationship between two given values.</p>
     *
     * @return a Comparable that follows XML Schema comparison rules
     */

    public Comparable getSchemaComparable() {
        return new ValueSchemaComparable();
    }

    private class ValueSchemaComparable implements Comparable {
        public Value getValue() {
            return Value.this;
        }
        public int compareTo(Object obj) {
            try {
                if (obj instanceof ValueSchemaComparable) {
                    SequenceIterator iter1 = getValue().iterate();
                    SequenceIterator iter2 = ((ValueSchemaComparable)obj).getValue().iterate();
                    while (true) {
                        Item item1 = iter1.next();
                        Item item2 = iter2.next();
                        if (item1 == null && item2 == null) {
                            return 0;
                        }
                        if (item1 == null) {
                            return -1;
                        } else if (item2 == null) {
                            return +1;
                        }
                        if (!(item1 instanceof AtomicValue && item2 instanceof AtomicValue)) {
                            throw new UnsupportedOperationException(
                                    "Sequences containing nodes or function items are not schema-comparable");
                        }
                        int c = ((AtomicValue)item1).getSchemaComparable().compareTo(
                                    ((AtomicValue)item2).getSchemaComparable());
                        if (c != 0) {
                            return c;
                        }
                    }
                } else {
                    return INDETERMINATE_ORDERING;
                }
            } catch (XPathException e) {
                throw new AssertionError("Failure comparing schema values: " + e.getMessage());
            }
        }

        public boolean equals(Object obj) {
            return compareTo(obj) == 0;
        }

        public int hashCode() {
            try {
                int hash = 0x06639662;  // arbitrary seed
                SequenceIterator iter = getValue().iterate();
                while (true) {
                    Item item = iter.next();
                    if (item == null) {
                        return hash;
                    }
                    hash ^= ((AtomicValue)item).getSchemaComparable().hashCode();
                }
            } catch (XPathException e) {
                return 0;
            }
        }
    }

    /**
     * Constant returned by compareTo() method to indicate an indeterminate ordering between two values
     */

    public static final int INDETERMINATE_ORDERING = Integer.MIN_VALUE;

    /**
     * Compare two (sequence) values for equality. This method throws an UnsupportedOperationException,
     * because it should not be used: there are too many "equality" operators that can be defined on
     * values for the concept to be meaningful.
     * <p>Consider creating an XPathComparable from each value, and comparing those; or creating a
     * SchemaComparable to achieve equality comparison as defined in XML Schema.</p>
     * @throws UnsupportedOperationException (always)
     */

    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Value.equals()");
    }

    public int hashCode() {
        return 42;
    }

    /**
     * Determine whether two values are identical, as determined by XML Schema rules. This is a stronger
     * test than equality (even schema-equality); for example two dateTime values are not identical unless
     * they are in the same timezone.
     * <p>Note that even this check ignores the type annotation of the value. The integer 3 and the short 3
     * are considered identical, even though they are not fully interchangeable. "Identical" means the
     * same point in the value space, regardless of type annotation.</p>
     * <p>Although the schema rules cover atomic values only, this method also handles values that include nodes,
     * using node identity in this case.</p>
     * <p>The empty sequence is considered identical to the empty sequence.</p>
     * <p>NaN is identical to itself.</p>
     * <p>Function items are not identical to anything except themselves
     * @param v the other value to be compared with this one
     * @return true if the two values are identical, false otherwise.
     */

    public boolean isIdentical(Value v) {
        try {
            SequenceIterator i0 = iterate();
            SequenceIterator i1 = v.iterate();
            while (true) {
                Item m0 = i0.next();
                Item m1 = i1.next();
                if (m0==null && m1==null) {
                    return true;
                }
                if (m0==null || m1==null) {
                    return false;
                }
                boolean n0 = (m0 instanceof NodeInfo);
                boolean n1 = (m1 instanceof NodeInfo);
                if (n0 != n1) {
                    return false;
                }
                if (n0 && n1 && !((NodeInfo)m0).isSameNodeInfo((NodeInfo)m1)) {
                    return false;
                }
                boolean a0 = (m0 instanceof AtomicValue);
                boolean a1 = (m1 instanceof AtomicValue);
                if (a0 && a1 && !((AtomicValue)m0).isIdentical((AtomicValue)m1)) {
                    return false;
                }
                if ((!a0 || !a1) && m0 != m1) {
                    // one of them is a function item, and they are not the same function item
                    return false;
                }
            }
        } catch (XPathException err) {
            return false;
        }
    }


    /**
     * Check statically that the results of the expression are capable of constructing the content
     * of a given schema type.
     * @param parentType The schema type
     * @param env the static context
     * @param whole true if this value accounts for the entire content of the containing node
     * @throws XPathException if the expression doesn't match the required content type
     */

    public void checkPermittedContents(SchemaType parentType, StaticContext env, boolean whole) throws XPathException {
        //return;
    }

    /**
     * Reduce a value to its simplest form. If the value is a closure or some other form of deferred value
     * such as a FunctionCallPackage, then it is reduced to a SequenceExtent. If it is a SequenceExtent containing
     * a single item, then it is reduced to that item. One consequence that is exploited by class FilterExpression
     * is that if the value is a singleton numeric value, then the result will be an instance of NumericValue
     * @return the value in simplified form
     */

    public Value reduce() throws XPathException {
        return this;
    }


    /**
     * Convert an XPath value to a Java object.
     * An atomic value is returned as an instance
     * of the best available Java class. If the item is a node, the node is "unwrapped",
     * to return the underlying node in the original model (which might be, for example,
     * a DOM or JDOM node).
     * @param item the item to be converted
     * @return the value after conversion
    */

    public static Object convertToJava(Item item) throws XPathException {
        if (item instanceof NodeInfo) {
            Object node = item;
            while (node instanceof VirtualNode) {
                // strip off any layers of wrapping
                node = ((VirtualNode)node).getRealNode();
            }
            return node;
        } else if (item instanceof FunctionItem) {
            return item;
        } else if (item instanceof ObjectValue) {
            return ((ObjectValue)item).getObject();
        } else {
            AtomicValue value = (AtomicValue)item;
            switch (value.getItemType(null).getPrimitiveType()) {
                case StandardNames.XS_STRING:
                case StandardNames.XS_UNTYPED_ATOMIC:
                case StandardNames.XS_ANY_URI:
                case StandardNames.XS_DURATION:
                    return value.getStringValue();
                case StandardNames.XS_BOOLEAN:
                    return (((BooleanValue)value).getBooleanValue() ? Boolean.TRUE : Boolean.FALSE );
                case StandardNames.XS_DECIMAL:
                    return ((DecimalValue)value).getDecimalValue();
                case StandardNames.XS_INTEGER:
                    return Long.valueOf(((NumericValue)value).longValue());
                case StandardNames.XS_DOUBLE:
                    return new Double(((DoubleValue)value).getDoubleValue());
                case StandardNames.XS_FLOAT:
                    return new Float(((FloatValue)value).getFloatValue());
                case StandardNames.XS_DATE_TIME:
                    return ((DateTimeValue)value).getCalendar().getTime();
                case StandardNames.XS_DATE:
                    return ((DateValue)value).getCalendar().getTime();
                case StandardNames.XS_TIME:
                    return value.getStringValue();
                case StandardNames.XS_BASE64_BINARY:
                    return ((Base64BinaryValue)value).getBinaryValue();
                case StandardNames.XS_HEX_BINARY:
                    return ((HexBinaryValue)value).getBinaryValue();
                default:
                    return item;
            }
        }
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
=======
package ncsa.d2k.modules.projects.dtcheng;
import ncsa.d2k.modules.core.datatype.table.*;
import java.util.*;

public class GeneralExampleSet implements java.io.Serializable {

  int numExamples;
  int numInputs;
  int numOutputs;
  public String [] inputNames;
  public String [] outputNames;

  MutableExample [] examples;


  public GeneralExampleSet()
    {
    }

  public ExampleTable shallowCopy()
    {
    GeneralExampleSet copy = new GeneralExampleSet();
    copy.examples    = this.examples;
    copy.numExamples = this.numExamples;
    copy.numInputs   = this.numInputs;
    copy.numOutputs  = this.numOutputs;
    copy.inputNames  = this.inputNames;
    copy.outputNames = this.outputNames;
    return (ExampleTable) copy;
    }

  public GeneralExampleSet(MutableExample [] examples)
    {
    this.examples    = examples;
    this.numExamples = examples.length;
    this.numInputs   = ((ExampleTable) (examples[0].getTable())).getNumInputFeatures();
    this.numOutputs  = ((ExampleTable) (examples[0].getTable())).getNumOutputFeatures();
    }

  public void setExample(int e1, ExampleTable exampleSet, int e2)
    {
    this.examples[e1] = ((GeneralExampleSet) exampleSet).examples[e2].copy();
    }


  public double getInputDouble(int e, int i)
    {
    return examples[e].getInputDouble(i);
    }

  public double getOutputDouble(int e, int i)
    {
    return examples[e].getOutputDouble(i);
    }


  public String getInputString(int e, int i)
    {
    return Double.toString(examples[e].getInputDouble(i));
    }

  public String getOutputString(int e, int i)
    {
    return Double.toString(examples[e].getOutputDouble(i));
    }

  public int getInputInt(int e, int i)
    {
    return (int) examples[e].getInputDouble(i);
    }

  public int getOutputInt(int e, int i)
    {
    return (int) examples[e].getOutputDouble(i);
    }

  public float getInputFloat(int e, int i)
    {
    return (float) examples[e].getInputDouble(i);
    }

  public float getOutputFloat(int e, int i)
    {
    return (float) examples[e].getOutputDouble(i);
    }

  public short getInputShort(int e, int i)
    {
    return (short) examples[e].getInputDouble(i);
    }

  public short getOutputShort(int e, int i)
    {
    return (short) examples[e].getOutputDouble(i);
    }

  public long getInputLong(int e, int i)
    {
    return (long) examples[e].getInputDouble(i);
    }

  public long getOutputLong(int e, int i)
    {
    return (long) examples[e].getOutputDouble(i);
    }

  public byte getInputByte(int e, int i)
    {
    return (byte) examples[e].getInputDouble(i);
    }

  public byte getOutputByte(int e, int i)
    {
    return (byte) examples[e].getOutputDouble(i);
    }

  public Object getInputObject(int e, int i)
    {
    return (Object) new Double(examples[e].getInputDouble(i));
    }

  public Object getOutputObject(int e, int i)
    {
    return (Object) new Double(examples[e].getOutputDouble(i));
    }

  public char getInputChar(int e, int i)
    {
    return (char) examples[e].getInputDouble(i);
    }

  public char getOutputChar(int e, int i)
    {
    return (char) examples[e].getOutputDouble(i);
    }

  public byte[] getInputBytes(int e, int i)
    {
    byte [] bytes = new byte[1];
    bytes[0] = (byte) examples[e].getInputDouble(i);
    return bytes;
    }

  public byte[] getOutputBytes(int e, int i)
    {
    byte [] bytes = new byte[1];
    bytes[0] = (byte) examples[e].getOutputDouble(i);
    return bytes;
    }

  public char[] getInputChars(int e, int i)
    {
    char [] chars = new char[1];
    chars[0] = (char) examples[e].getInputDouble(i);
    return chars;
    }

  public char[] getOutputChars(int e, int i)
    {
    char [] chars = new char[1];
    chars[0] = (char) examples[e].getOutputDouble(i);
    return chars;
    }

  public boolean getInputBoolean(int e, int i)
    {
    if (examples[e].getInputDouble(i) < 0.5)
      return false;
    else
      return true;
    }

  public boolean getOutputBoolean(int e, int i)
    {
    if (examples[e].getOutputDouble(i) < 0.5)
      return false;
    else
      return true;
    }

  public int getNumInputs(int e)
    {
    return this.numInputs;
    }

  public int getNumOutputs(int e)
    {
    return this.numOutputs;
    }

  public MutableExample getExample(int e)
    {
    return (MutableExample) examples[e];
    }


  public String [] getInputNames()
    {
    return this.inputNames;
    }

  public String [] getOutputNames()
    {
    return this.outputNames;
    }

  public String getInputName(int i)
    {
    return this.inputNames[i];
    }

  public String getOutputName(int i)
    {
    return this.outputNames[i];
    }

  public int getInputType(int i)
    {
    System.out.println("Must override this method!");
    return ColumnTypes.DOUBLE;
    }

  public int getOutputType(int i)
    {
    System.out.println("Must override this method!");
    return ColumnTypes.DOUBLE;
    }

  public boolean isInputNominal(int i)
    {
    return false;
    }

  public boolean isOutputNominal(int i)
    {
    return false;
    }

  public boolean isInputScalar(int i)
    {
    return true;
    }

  public boolean isOutputScalar(int i)
    {
    return true;
    }





  public void setInput(int e, int i, double value)
    {
    examples[e].setInputDouble(i, value);
    }

  public void setOutput(int e, int i, double value)
    {
    examples[e].setOutputDouble(i, value);
    }

  public void deleteInputs(boolean [] deleteFeatures)
    {
    System.out.println("!!! deleteInputs not defined");
    }








  /**
       * Get an Object from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the Object at (row, column)
   */
  public Object getObject(int row, int column)
    {
    return null;
    }

  /**
       * Get an int value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the int at (row, column)
   */
  public int getInt(int row, int column)
    {
    if (column < numInputs)
      return (int) examples[row].getInputDouble(column);
    else
      return (int) examples[row].getOutputDouble(column - numInputs);
    }

  /**
       * Get a short value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the short at (row, column)
   */
  public short getShort(int row, int column)
    {
    if (column < numInputs)
      return (short) examples[row].getInputDouble(column);
    else
      return (short) examples[row].getOutputDouble(column - numInputs);
    }

  /**
       * Get a float value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the float at (row, column)
   */
  public float getFloat(int row, int column)
    {
    if (column < numInputs)
      return (float) examples[row].getInputDouble(column);
    else
      return (float) examples[row].getOutputDouble(column - numInputs);
    }

  /**
       * Get a double value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the double at (row, column)
   */
  public double getDouble(int row, int column)
    {
    if (column < numInputs)
      return (double) examples[row].getInputDouble(column);
    else
      return (double) examples[row].getOutputDouble(column - numInputs);
    }

  /**
       * Get a long value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the long at (row, column)
   */
  public long getLong(int row, int column)
    {
    if (column < numInputs)
      return (long) examples[row].getInputDouble(column);
    else
      return (long) examples[row].getOutputDouble(column - numInputs);
    }

  /**
       * Get a String value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the String at (row, column)
   */
  public String getString(int row, int column)
    {
    if (column < numInputs)
      {
      return Double.toString(examples[row].getInputDouble(column));
      }
    else
      return Double.toString(examples[row].getOutputDouble(column - numInputs));
    }

  /**
       * Get a value from the table as an array of bytes.
   * @param row the row of the table
   * @param column the column of the table
   * @return the value at (row, column) as an array of bytes
   */
  public byte[] getBytes(int row, int column)
    {
    return null;
    }

  /**
       * Get a boolean value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the boolean value at (row, column)
   */
  public boolean getBoolean(int row, int column)
    {
    return false;
    }

  /**
       * Get a value from the table as an array of chars.
   * @param row the row of the table
   * @param column the column of the table
   * @return the value at (row, column) as an array of chars
   */
  public char[] getChars(int row, int column)
    {
    return null;
    }

  /**
       * Get a byte value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the byte value at (row, column)
   */
  public byte getByte(int row, int column)
    {
    return 0;
    }

  /**
       * Get a char value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the char value at (row, column)
   */
  public char getChar(int row, int column)
    {
    return 0;
    }

  //////////////////////////////////////
  //// Accessing Table Metadata

  /**
          Return the index which represents the key column of this table.
          @return the key column index
  */
  public int getKeyColumn()
    {
    return 0;
    }

  /**
          Sets the key column index of this table.
          @param position the Column which is key for identifying unique rows
  */
  public void setKeyColumn(int position)
    {
    }

  /**
          Returns the name associated with the column.
          @param position the index of the Column name to get.
          @returns the name associated with the column.
  */
  public String getColumnLabel(int position)
    {
    String label = null;
    if (position < numInputs)
      label = inputNames[position];
    else
      label = outputNames[position - numInputs];
    return label;
    }

  /**
          Returns the comment associated with the column.
          @param position the index of the Column name to get.
          @returns the comment associated with the column.
  */
  public String getColumnComment(int position)
    {
    return null;
    }

  /**
          Get the label associated with this Table.
          @return the label which describes this Table
  */
  public String getLabel()
    {
    return null;
    }

  /**
          Set the label associated with this Table.
          @param labl the label which describes this Table
  */
  public void setLabel(String labl)
    {
    }

  /**
          Get the comment associated with this Table.
          @return the comment which describes this Table
  */
  public String getComment()
    {
    return null;
    }

  /**
          Set the comment associated with this Table.
          @param comment the comment which describes this Table
  */
  public void setComment(String comment)
    {
    }

  /**
          Get the number of rows in this Table.  Same as getCapacity().
          @return the number of rows in this Table.
  */
  public int getNumRows()
    {
    return numExamples;
    }

  /**
          Get the number of entries this Table holds.
          @return this Table's number of entries
  */
  public int getNumEntries()
    {
    return numExamples;
    }

  /**
          Return the number of columns this table holds.
          @return the number of columns in this table
  */
  public int getNumColumns()
    {
    return numInputs + numOutputs;
    }
   /**
    * Get all the entries from the specified row.  The caller must pass in
    * a buffer for the data to be copied into.  This buffer should be one of
    * following data types: int[], float[], double[], long[], short[], boolean[],
    * String[], char[][], byte[][], Object[], byte[], or char[].  The data from
    * the specified row will then be copied into the buffer.  If the length of
    * the buffer is greater than the number of columns in the table, an
    * ArrayIndexOutOfBoundsException will be thrown.
    * @param buffer the array to copy data into
    * @param pos the index of the row to copy
    */
    public void getRow (Object buffer, int pos) {
      if(buffer instanceof int[]) {
         int[] b1 = (int[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getInt(pos, i);
      }
      else if(buffer instanceof float[]) {
         float[] b1 = (float[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getFloat(pos, i);
      }
      else if(buffer instanceof double[]) {
         double[] b1 = (double[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getDouble(pos, i);
      }
      else if(buffer instanceof long[]) {
         long[] b1 = (long[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getLong(pos, i);
      }
      else if(buffer instanceof short[]) {
         short[] b1 = (short[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getShort(pos, i);
      }
      else if(buffer instanceof boolean[]) {
         boolean[] b1 = (boolean[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getBoolean(pos, i);
      }
      else if(buffer instanceof String[]) {
         String[] b1 = (String[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getString(pos, i);
      }
      else if(buffer instanceof char[][]) {
         char[][] b1 = (char[][])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getChars(pos, i);
      }
      else if(buffer instanceof byte[][]) {
         byte[][] b1 = (byte[][])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getBytes(pos, i);
      }
      else if(buffer instanceof Object[]) {
         Object[] b1 = (Object[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getObject(pos, i);
      }
      else if(buffer instanceof byte[]) {
         byte[] b1 = (byte[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getByte(pos, i);
      }
      else if(buffer instanceof char[]) {
         char[] b1 = (char[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getChar(pos, i);
      }
    }

   /**
    * Get all the entries from the specified column.  The caller must pass in
    * a buffer for the data to be copied into.  This buffer should be one of
    * following data types: int[], float[], double[], long[], short[], boolean[],
    * String[], char[][], byte[][], Object[], byte[], or char[].  The data from
    * the specified row will then be copied into the buffer.  If the length of
    * the buffer is greater than the number of rows in the table, an
    * ArrayIndexOutOfBoundsException will be thrown.
    * @param buffer the array to copy data into
    * @param pos the index of the column to copy
    */
   public void getColumn (Object buffer, int pos) {
      if(buffer instanceof int[]) {
         int[] b1 = (int[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getInt(i, pos);
      }
      else if(buffer instanceof float[]) {
         float[] b1 = (float[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getFloat(i, pos);
      }
      else if(buffer instanceof double[]) {
         double[] b1 = (double[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getDouble(i, pos);
      }
      else if(buffer instanceof long[]) {
         long[] b1 = (long[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getLong(i, pos);
      }
      else if(buffer instanceof short[]) {
         short[] b1 = (short[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getShort(i, pos);
      }
      else if(buffer instanceof boolean[]) {
         boolean[] b1 = (boolean[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getBoolean(i, pos);
      }
      else if(buffer instanceof String[]) {
         String[] b1 = (String[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getString(i, pos);
      }
      else if(buffer instanceof char[][]) {
         char[][] b1 = (char[][])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getChars(i, pos);
      }
      else if(buffer instanceof byte[][]) {
         byte[][] b1 = (byte[][])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getBytes(i, pos);
      }
      else if(buffer instanceof Object[]) {
         Object[] b1 = (Object[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getObject(i, pos);
      }
      else if(buffer instanceof byte[]) {
         byte[] b1 = (byte[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getByte(i, pos);
      }
      else if(buffer instanceof char[]) {
         char[] b1 = (char[])buffer;
         for(int i = 0; i < b1.length; i++)
            b1[i] = getChar(i, pos);
      }
   }


  /**
          Get a subset of this Table, given a start position and length.  The
          subset will be a new Table.
          @param start the start position for the subset
          @param len the length of the subset
          @return a subset of this Table
  */
  public Table getSubset(int start, int len)
    {
    return null;
    }

  /**
          Create a copy of this Table.
          @return a copy of this Table
  */
  public Table copy()
    {
    return null;
    }

  /**
   * Get a TableFactory for this Table.
   * @return The appropriate TableFactory for this Table.
   */
  public TableFactory getTableFactory()
    {
    return null;
    }

   /**
    * Returns true if the column at position contains nominal data, false
    * otherwise.
    * @param position the index of the column
    * @return true if the column contains nominal data, false otherwise.
    */
  public boolean isColumnNominal(int position)
    {
    return false;
    }

   /**
    * Returns true if the column at position contains scalar data, false
    * otherwise
    * @param position
    * @return true if the column contains scalar data, false otherwise
    */
  public boolean isColumnScalar(int position)
    {
    return true;
    }

   /**
    * Set whether the column at position contains nominal data or not.
    * @param value true if the column at position holds nominal data, false otherwise
    * @param position the index of the column
    */
  public void setColumnIsNominal(boolean value, int position)
    {
    }

  /**
  * Set whether the column at position contains scalar data or not.
  * @param value true if the column at position holds scalar data, false otherwise
  * @param position the index of the column
  */
  public void setColumnIsScalar(boolean value, int position)
    {
    }

  /**
  * Returns true if the column at position contains only numeric values,
  * false otherwise.
  * @param position the index of the column
  * @return true if the column contains only numeric values, false otherwise
  */
  public boolean isColumnNumeric(int position)
    {
    return true;
    }

  /**
  * Return the type of column located at the given position.
  * @param position the index of the column
  * @return the column type
  * @see ColumnTypes
  */
  public int getColumnType(int position)
    {
    return 0;
    }

  /**
  * Return this Table as an ExampleTable.
  * @return This object as an ExampleTable
  */
  public ExampleTable toExampleTable()
    {
    return (ExampleTable) this;
    }








  /////////// Collect the transformations that were performed. /////////
  /**
   Add the transformation to the list.
   @param tm the TransformationModule that performed the reversable transform.
   */
  /*
  public void addTransformation (TransformationModule tm)
    {
    }
*/
  /**
   Returns the list of all reversable transformations there were performed
   on the original dataset.
   @returns an ArrayList containing the TransformationModules which transformed the data.
   */
  public ArrayList getTransformations ()
    {
    return null;
    }

  //////////////  Input, output, test and train. ///////////////
  /**
   Returns an array of ints, the indices of the input columns.
   @return an array of ints, the indices of the input columns.
   */
  public int[] getInputFeatures ()
    {
    int [] inputFeatures = new int[numInputs];
    for (int i = 0; i < numInputs; i++)
      {
      inputFeatures[i] = i;
      }
    return inputFeatures;
    }

  /**
   Returns the number of input features.
   @returns the number of input features.
   */
  public int getNumInputFeatures ()
    {
    return numInputs;
    }

  /**
   Returns the number of example rows.
   @returns the number of example rows.
   */
  public int getNumExamples ()
    {
    return numExamples;
    }

  /**
   Return the number of examples in the training set.
   @returns the number of examples in the training set.
   */
  public int getNumTrainExamples ()
    {
    return 0;
    }

  /**
   Return the number of examples in the testing set.
   @returns the number of examples in the testing set.
   */
  public int getNumTestExamples ()
    {
    return 0;
    }

  /**
   Returns an array of ints, the indices of the output columns.
   @return an array of ints, the indices of the output columns.
   */
  public int[] getOutputFeatures ()
    {
    int [] outputFeatures = new int[numOutputs];
    for (int i = 0; i < numOutputs; i++)
      {
      outputFeatures[i] = i + numInputs;
      }
    return outputFeatures;
    }

  /**
   Get the number of output features.
   @returns the number of output features.
   */
  public int getNumOutputFeatures ()
    {
    return numOutputs;
    }

  /**
   Set the input features.
   @param inputs the indexes of the columns to be used as input features.
   */
  public void setInputFeatures (int[] inputs)
    {
    }

  /**
   Set the output features.
   @param outs the indexes of the columns to be used as output features.
   */
  public void setOutputFeatures (int[] outs)
    {
    }

  /**
   Set the indexes of the rows in the training set.
   @param trainingSet the indexes of the items to be used to train the model.
   */
  public void setTrainingSet (int[] trainingSet)
    {
    }

  /**
   Get the training set.
   @return the indices of the rows of the training set.
   */
  public int[] getTrainingSet ()
    {
    return null;
    }

  /**
   Set the indexes of the rows in the testing set.
   @param testingSet the indexes of the items to be used to test the model.
   */
  public void setTestingSet (int[] testingSet)
    {
    }

  /**
   Get the testing set.
   @return the indices of the rows of the testing set.
   */
  public int[] getTestingSet ()
    {
    return null;
    }

  /**
       * Return a reference to a Table referencing only the testing data.
       @return a reference to a Table referencing only the testing data
   */
  public TestTable getTestTable ()
    {
    return null;
    }

  /**
   Return a reference to a Table referencing only the training data.
   @return a reference to a Table referencing only the training data.
   */
  public TrainTable getTrainTable ()
    {
    return null;
    }

  /**
   * Return this ExampleTable as a PredictionTable.
   * @return This object as a PredictionTable
   */
  public PredictionTable toPredictionTable()
    {
    return null;
    }



  /**
   * Set the prediction set
       * @return the prediciton set
   */
  public int[] getPredictionSet ()
    {
    return null;
    }

  /**
       * Set the prediction set
       * @param p the new prediciton set
   */
  public void setPredictionSet (int[] p)
    {
    }

  /**
   * Set an int prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setIntPrediction(int prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a float prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setFloatPrediction(float prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a double prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setDoublePrediction(double prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a long prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setLongPrediction(long prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a short prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setShortPrediction(short prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a boolean prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setBooleanPrediction(boolean prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a String prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setStringPrediction(String prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a char[] prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setCharsPrediction(char[] prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a byte[] prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setBytesPrediction(byte[] prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set an Object prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setObjectPrediction(Object prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a byte prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setBytePrediction(byte prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Set a char prediciton in the specified prediction column.   The index into
   * the prediction set is used, not the actual column index.
   * @param prediction the value of the prediciton
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   */
  public void setCharPrediction(char prediction, int row, int predictionColIdx)
    {
    }

  /**
   * Get an int prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public int getIntPrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Get a float prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public float getFloatPrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Get a double prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public double getDoublePrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Get a long prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public long getLongPrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Get a short prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public short getShortPrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Get a boolean prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public boolean getBooleanPrediction(int row, int predictionColIdx)
    {
    return false;
    }

  /**
   * Get a String prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public String getStringPrediction(int row, int predictionColIdx)
    {
    return null;
    }

  /**
   * Get a char[] prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public char[] getCharsPrediction(int row, int predictionColIdx)
    {
    return null;
    }

  /**
   * Get a byte[] prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public byte[] getBytesPrediction(int row, int predictionColIdx)
    {
    return null;
    }

  /**
   * Get an Object prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public Object getObjectPrediction(int row, int predictionColIdx)
    {
    return null;
    }

  /**
   * Get a byte prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public byte getBytePrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Get a char prediciton in the specified prediction column.  The index into
   * the prediction set is used, not the actual column index.
   * @param row the row of the table
   * @param predictionColIdx the index into the prediction set
   * @return the prediction at (row, getPredictionSet()[predictionColIdx])
   */
  public char getCharPrediction(int row, int predictionColIdx)
    {
    return 0;
    }

  /**
   * Add a column of integer predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(int[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of float predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(float[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of double predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(double[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of long predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(long[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of short predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(short[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of boolean predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(boolean[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of String predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(String[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of char[] predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(char[][] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of byte[] predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(byte[][] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of Object predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(Object[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of byte predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(byte[] predictions, String label)
    {
    return 0;
    }

  /**
   * Add a column of char predictions to this PredictionTable.
   * @param predictions the predictions
   * @param label the label for the new column
   * @return the index of the prediction column in the prediction set
   */
  public int addPredictionColumn(char[] predictions, String label)
    {
    return 0;
    }

  //
  // The following methods for Missing and Empty values were added
  // by Ruth to get things to compile.  They do not correctly
  // support the Missing/Empty value functionality, and should be
  // revisited at a later date and fully implemented.
  //

  /**
   * Return true if the value at (row, col) is a missing value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is missing, false otherwise
   */
  public boolean isValueMissing(int row, int col)
    {
    return false;
    }

  /**
   * Return true if the value at (row, col) is an empty value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is empty, false otherwise
   */
  public boolean isValueEmpty(int row, int col)
   {
   return false;
   }

  /**
   * Return the value used to signify a scalar missing value in col
   * @param col the column index
   * @return the value used to signify a scalar missing value in col
   */
  public Number getScalarMissingValue(int col)
   {
   return null;
   }

  /**
   * Return the value used to signify a nominal missing value in col
   * @param col the column index
   * @return the value used to signify a nominal missing value in col
   */
  public String getNominalMissingValue(int col)
   {
   return null;
   }

  /**
   * Return the value used to signify a scalar empty value in col
   * @param col the column index
   * @return the value used to signify a scalar empty value in col
   */
  public Number getScalarEmptyValue(int col)
   {
   return null;
   }

  /**
   * Return the value used to signify a nominal empty value in col
   * @param col the column index
   * @return the value used to signify a nominal empty value in col
   */
  public String getNominalEmptyValue(int col)
   {
   return null;
   }



  }
>>>>>>> 76aa07461566a5976980e6696204781271955163

