package ncsa.d2k.modules.core.datatype.table.basic;

import ncsa.d2k.modules.core.datatype.table.*;

import java.io.*;
import java.util.*;

/**
 DoubleColumn is an implementation of NumericColumn which holds a double array
 as its internal representation.
 <br><br>
 It it optimized for: retrieval of doubles by index, compact representation
 of doubles,  swapping of doubles, setting of doubles by index, reOrdering by index,
 compareing of doubles
 It is very inefficient for: removals, insertions, additions
 */
final public class DoubleColumn extends MissingValuesColumn implements NumericColumn {
	//static final long serialVersionUID = 5514367304811178549L;
	static final long serialVersionUID = -5854760060261143830L;

    private double min, max;

    /** holds DoubleColumn's internal data rep */
    private double[] internal = null;
	private boolean[] empty = null;
    /**
     Create a new, emtpy DoubleColumn.
     */
    public DoubleColumn () {
        this(0);
    }

    /**
     Create a new DoubleColumn with the specified initial capacity.
     @param capacity the initial capacity for this column
     */
    public DoubleColumn (int capacity) {
        internal = new double[capacity];
        setIsScalar(true);
        type = ColumnTypes.DOUBLE;
        missing = new boolean[internal.length];
        empty = new boolean[internal.length];
        for(int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
		}
    }

    /**
     Create a new DoubleColumn with the specified values.
     @param vals the initial values to store in this column
     */
    public DoubleColumn (double[] vals) {
        internal = vals;
        setIsScalar(true);
        type = ColumnTypes.DOUBLE;
        missing = new boolean[internal.length];
        empty = new boolean[internal.length];
        for(int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
		}
    }

    private DoubleColumn(double[] vals, boolean[] miss, boolean[] emp, String lbl,
                         String com) {
        internal = vals;
        setIsScalar(true);
        type = ColumnTypes.DOUBLE;
        this.setMissingValues(miss);
        empty =emp;
        setLabel(lbl);
        setComment(com);
    }

    /**
     Return an exact copy of this column.  A deep copy is attempted, but if it
     fails a new column will be created, initialized with the same data as this
     column.
     @return A new Column with a copy of the contents of this column.
     */
    public Column copy () {
        DoubleColumn dc;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            byte buf[] = baos.toByteArray();
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            dc = (DoubleColumn)ois.readObject();
            ois.close();
            return  dc;
        } catch (Exception e) {
            //dc = new DoubleColumn(getNumRows());
            double[] newVals = new double[getNumRows()];
            for (int i = 0; i < getNumRows(); i++)
                //dc.setDouble(internal[i], i);
                newVals[i] = getDouble(i);
            boolean[] miss = new boolean[internal.length];
            boolean[] em = new boolean[internal.length];
            for(int i = 0; i < internal.length; i++) {
                miss[i] = missing[i];
                em[i] = empty[i];

            }
            dc = new DoubleColumn(newVals, miss, em, getLabel(), getComment());
            return  dc;
        }
    }

    //////////////////////////////////////
    //// Accessing Metadata
	/**
	 * Add the specified number of blank rows.
	 * @param number number of rows to add.
	 */
	public void addRows (int number) {
		int last = internal.length;
		double[] newInternal = new double[last + number];
		boolean[] newMissing = new boolean[last + number];
		boolean[] newEmpty = new boolean[last + number];

		System.arraycopy(internal, 0, newInternal, 0, last);
		System.arraycopy(missing, 0, newMissing, 0, missing.length);
		System.arraycopy(empty, 0, newEmpty, 0, empty.length);
		internal = newInternal;
		this.setMissingValues(newMissing);
		empty = newEmpty;
	}


    /**
     Return the count for the number of non-null entries.
     This variable is recomputed each time...as keeping
     track of it could be very inefficient.
     @return this DoubleColumn's number of entries
     */
    public int getNumEntries () {
        int numEntries = 0;
        for (int i = 0; i < internal.length; i++)
            if (!isValueMissing(i) && !isValueEmpty(i))
                numEntries++;
        return  numEntries;
    }

    /**
     * Get the number of rows that this column can hold.  Same as getCapacity
     * @return the number of rows this column can hold
     */
    public int getNumRows() {
        return internal.length;
    }

    /**
     Set a new capacity for this DoubleColumn.  The capacity is its potential
     max number of entries.  If numEntries is greater than newCapacity, the
     Column will be truncated.
     @param newCapacity the new capacity
     */
    public void setNumRows (int newCapacity) {
/*        if (internal != null) {
            double[] newInternal = new double[newCapacity];
            if (newCapacity > internal.length)
                newCapacity = internal.length;
            System.arraycopy(internal, 0, newInternal, 0, newCapacity);
            internal = newInternal;
        }
        else
            internal = new double[newCapacity];
        */

        if (internal != null) {
            double[] newInternal = new double[newCapacity];
            boolean[] newMissing = new boolean[newCapacity];
            boolean[] newEmpty = new boolean[newCapacity];
            if (newCapacity > internal.length)
                newCapacity = internal.length;
            System.arraycopy(internal, 0, newInternal, 0, newCapacity);
            System.arraycopy(missing, 0, newMissing, 0, missing.length);
            System.arraycopy(empty, 0, newEmpty, 0, empty.length);
            internal = newInternal;
			this.setMissingValues(newMissing);
            empty = newEmpty;
        }
        else {
            internal = new double[newCapacity];
            missing = new boolean[newCapacity];
            empty = new boolean[newCapacity];
		}
    }

    /**
     Get the minimum value contained in this Column
     @return the minimum value of this Column
     */
    public double getMin () {
        initRange();
        return  min;
    }

    /**
     Get the maximum value contained in this Column
     @return the maximum value of this Column
     */
    public double getMax () {
        initRange();
        return  max;
    }

    /**
     Sets the value which indicates an empty entry.
     This can by any subclass of Number
     @param emptyVal the value to which an empty entry is set
     /
    public void setEmptyValue (Number emptyVal) {
        emptyValue = ((Number)emptyVal).doubleValue();
    }

    /**
     Gets the value which indicates an empty entry.
     @return the value of an empty entry wrapped in a subclass of Number
     /
    public Number getEmptyValue () {
        return  new Double(emptyValue);
    }*/

    //////////////////////////////////////
    /**
     Initializes the min and max of this DoubleColumn.
     */
    protected void initRange () {
        max = min = internal[0];
        for (int i = 1; i < internal.length; i++) {
			if(!isValueMissing(i) && !isValueEmpty(i)) {
            	if (internal[i] > max)
                	max = internal[i];
            	if (internal[i] < min)
                	min = internal[i];
			}
        }
    }

    //////////////////////////////////////
    //// ACCESSING FIELD ELEMENTS
    /**
     Gets a reference to the internal representation of this Column
     (double[]).  Changes made to this object will be reflected in the Column.
     @return the internal representation of this Column.
     @deprecated
     */
    public Object getInternal () {
        return  this.internal;
    }

    /**
     Returns an array of doubles scaled to the range 0-1.
     @return the internal representation of this Column.
     */
    public double[] getScaledDoubles () {
        int size1 = this.internal.length;
        double[] tmp = new double[size1];
        double min = this.getMin();
        double scal = 1.0/(this.getMax() - this.getMin());
        for (int i = 0; i < size1; i++)
            tmp[i] = (this.internal[i] - min)*scal;
        return  tmp;
    }

    /**
     Gets a subset of this Column, given a start position and length.
     The primitive values are copied, so they have no destructive abilities
     as far as the Column is concerned.
     @param pos the start position for the subset
     @param len the length of the subset
     @return a subset of this Column
     */
    public Column getSubset (int pos, int len) {
        if ((pos + len) > internal.length)
            throw  new ArrayIndexOutOfBoundsException();
        double[] subset = new double[len];
        boolean[] newMissing = new boolean[len];
        boolean[] newEmpty = new boolean[len];
        System.arraycopy(internal, pos, subset, 0, len);
        System.arraycopy(missing, pos, newMissing, 0, len);
        System.arraycopy(empty, pos, newEmpty, 0, len);
        DoubleColumn dc = new DoubleColumn(subset, newMissing, newEmpty,
                getLabel(), getComment());

        return  dc;
    }

    /**
     * Gets a subset of this <code>Column</code>, given a start position and
     * length. The primitive values are copied, so they have no destructive
     * abilities as far as the <code>Column</code> is concerned.
     *
     * @param pos            the start position for the subset
     * @param len            the length of the subset
     * @return               a subset of this <code>Column</code>
     */
    public Column getSubset (int[] rows) {
        double[] subset = new double[rows.length];
        boolean[] newMissing = new boolean[rows.length];
        boolean[] newEmpty = new boolean[rows.length];
        for(int i = 0; i < rows.length; i++) {
          subset[i] = internal[rows[i]];
          newMissing[i] = missing[rows[i]];
          newEmpty[i] = empty[rows[i]];
        }
        DoubleColumn bc = new DoubleColumn(subset, newMissing, newEmpty, getLabel(), getComment());
        return  bc;
    }


    /**
     Get a String from this column at pos
     @param pos the position from which to get a String
     @return a String representation of the entry at that position
     */
    public String getString (int pos) {
        return  String.valueOf(this.internal[pos]);
    }

    /**
     Set the value at pos to be newEntry by calling Double.parseDouble()
     @param newEntry the new item
     @param pos the position
     */
    public void setString (String newEntry, int pos) {
        internal[pos] = Double.parseDouble(newEntry);
    }

    /**
     Get the value at pos, cast to an int
     @param pos the position
     @return the value at pos as an int
     */
    public int getInt (int pos) {
        return  (int)internal[pos];
    }

    /**
     Set the value at pos to be newEntry
     @param newEntry the new item
     @param pos the position
     */
    public void setInt (int newEntry, int pos) {
        this.internal[pos] = (double)newEntry;
    }

    /**
     Get the value at pos, cast to a short
     @param pos the position
     @return the value at pos as a short
     */
    public short getShort (int pos) {
        return  (short)internal[pos];
    }

    /**
     Set the value at pos to be newEntry
     @param newEntry the position
     @param pos the position
     */
    public void setShort (short newEntry, int pos) {
        this.internal[pos] = (double)newEntry;
    }

    /**
     Set the value at pos to be newEntry
     @param newEntry the position
     @param pos the position
     */
    public void setLong (long newEntry, int pos) {
        this.internal[pos] = (double)newEntry;
    }

    /**
     Get the value at pos, cast to a long
     @param pos the position
     @return the value at pos as a long
     */
    public long getLong (int pos) {
        return  (long)internal[pos];
    }

    /**
     Get the value at pos
     @param pos the position
     @return the value at pos
     */
    public double getDouble (int pos) {
        return  this.internal[pos];
    }

    /**
     Set the value at pos
     @param newEntry the new item
     @param pos the position
     */
    public void setDouble (double newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     Get the value at pos, cast to a float
     @param pos the position
     @return the value at pos as a float
     */
    public float getFloat (int pos) {
        return  (float)internal[pos];
    }

    /**
     Set the value at pos
     @param newEntry the new item
     @param pos the position
     */
    public void setFloat (float newEntry, int pos) {
        this.internal[pos] = (double)newEntry;
    }

    /**
     Returns the value at pos as an array of bytes.  The number
     is converted to a String and then its byte[] representation is
     returned.
     @param pos the position
     @return the value at pos as a byte[]
     */
    public byte[] getBytes (int pos) {
        return (String.valueOf(this.internal[pos])).getBytes();
    }

    /**
     Convert newEntry to a double.  newEntry is converted to a
     String and then to a double.
     @param newEntry the new item
     @param pos the position
     */
    public void setBytes (byte[] newEntry, int pos) {
        setString(new String(newEntry), pos);
    }

    /**
     Returns the value at pos as a byte.
     @param pos the position
     @return the value at pos as a byte[]
     */
    public byte getByte (int pos) {
        return (byte)getDouble(pos);
    }

    /**
     Convert newEntry to a double.  newEntry is converted to a
     String and then to a double.
     @param newEntry the new item
     @param pos the position
     */
    public void setByte (byte newEntry, int pos) {
        setDouble((double)newEntry, pos);
    }

    /**
     Get the value at pos as a Double object.
     @param pos the position
     @return the value as pos as a Double
     */
    public Object getObject (int pos) {
        return  new Double(internal[pos]);
    }

    /**
     Sets the value at pos to be newEntry.  If newEntry is a Number,
     it is converted to a double and stored accordingly.  Otherwise,
     setString() is called with newEntry.toString()
     @param newEntry the new item
     @param pos the position
     */
    public void setObject (Object newEntry, int pos) {
        if (newEntry instanceof Number)
            internal[pos] = ((Number)newEntry).doubleValue();
        else
            setString(newEntry.toString(), pos);
    }

    /**
     Converts the entry at pos to a String and returns it as a char[]
     @param pos
     @return the entry at pos as a char[]
     */
    public char[] getChars (int pos) {
        return  Double.toString(internal[pos]).toCharArray();
    }

    /**
     Converts newEntry to a String and calls setString()
     @param newEntry the new item
     @param pos the position
     */
    public void setChars (char[] newEntry, int pos) {
        setString(new String(newEntry), pos);
    }

    /**
     Casts the entry at pos to an int and returns it as a char
     @param pos
     @return the entry at pos as a char[]
     */
    public char getChar (int pos) {
        return (char)getInt(pos);
    }

    /**
     Converts newEntry to a String and calls setString()
     @param newEntry the new item
     @param pos the position
     */
    public void setChar (char newEntry, int pos) {
        /*char[] c = new char[1];
        c[0] = newEntry;
        setChars(c, pos);
        */
        setDouble((double)newEntry, pos);
    }

    /**
     Returns false if the entry at pos is equal to zero, true
     otherwise.
     @param pos the position
     @return false if the value at pos is equal to zero, true
     otherwise
     */
    public boolean getBoolean (int pos) {
        if (internal[pos] == 0)
            return  false;
        return  true;
    }

    /**
     Sets the value at pos to be 1.0 if newEntry is true, sets the value
     to 0.0 otherwise.
     @param newEntry the new item
     @param pos the position
     */
    public void setBoolean (boolean newEntry, int pos) {
        if (newEntry)
            internal[pos] = (double)1.0;
        else
            internal[pos] = 0;
    }

    //////////////////////////////////////
    //// SUPPORT FOR Column INTERFACE
    /**
     Sets the reference to the internal representation of this Column.
     @param newInternal a new internal representation for this Column

    public void setInternal (Object newInternal) {
        if (newInternal instanceof double[])
            this.internal = (double[])newInternal;
    }*/

    /**
     Gets an object representation of the entry at the indicated position in Column
     @param pos the position
     @return the entry at pos
     */
    public Object getRow (int pos) {
        return  new Double(internal[pos]);
    }

    /**
     Sets the entry at the given position to newEntry.
     The newEntry should be a subclass of Number, preferable Double.
     @param newEntry a new entry, a subclass of Number
     @param pos the position to set
     */
    public void setRow (Object newEntry, int pos) {
        internal[pos] = ((Number)newEntry).doubleValue();
    }

    /**
     Adds the new entry to the Column after the last non-empty position
     in the Column.
     @param newEntry a new entry
     */
    public void addRow (Object newEntry) {
        int last = internal.length;
        double[] newInternal = new double[internal.length + 1];
        boolean[] newMissing = new boolean[internal.length + 1];
        boolean[] newEmpty = new boolean[internal.length + 1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length);
        System.arraycopy(missing, 0, newMissing, 0, missing.length);
        System.arraycopy(empty, 0, newEmpty, 0, empty.length);
        newInternal[last] = ((Double)newEntry).doubleValue();
        internal = newInternal;
		this.setMissingValues(newMissing);
		empty = newEmpty;
    }

    /**
     Removes an entry from the Column, at pos.
     All entries from pos+1 will be moved back 1 position
     @param pos the position to remove
     @return a Double representation of the removed double
     */
    public Object removeRow (int pos) {
        double removed = internal[pos];
        System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                (pos + 1));
        System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                (pos + 1));
        System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                (pos + 1));
        double newInternal[] = new double[internal.length - 1];
        boolean newMissing[] = new boolean[internal.length-1];
        boolean newEmpty[] = new boolean[internal.length-1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
        System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
        System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);
        internal = newInternal;
		this.setMissingValues(newMissing);
        empty = newEmpty;
        return  new Double(removed);
   }

    /**
     Inserts a new entry in the Column at position pos.
     All elements from pos to capacity will be moved up one.
     @param newEntry a Double wrapped double as the newEntry to insert
     @param pos the position to insert at
     */
    public void insertRow (Object newEntry, int pos) {
        /*double[] newInternal = new double[internal.length+1];
         int last = 0;
         System.arraycopy(newInternal,0,internal,0,pos-1);
         System.arraycopy(newInternal,pos,internal,pos+1,internal.length-(pos+1));
         newInternal[pos] = ((Number)newEntry).doubleValue();
         internal = newInternal;
         */
        if (pos > getNumRows()) {
            addRow(newEntry);
            return;
        }
        double[] newInternal = new double[internal.length + 1];
        boolean[] newMissing = new boolean[internal.length + 1];
        boolean[] newEmpty = new boolean[internal.length + 1];
        if (pos == 0) {
            System.arraycopy(internal, 0, newInternal, 1, getNumRows());
            System.arraycopy(missing, 0, newMissing, 1, getNumRows());
            System.arraycopy(empty, 0, newEmpty, 1, getNumRows());
        }
        else {
            System.arraycopy(internal, 0, newInternal, 0, pos);
            System.arraycopy(internal, pos, newInternal, pos + 1, internal.length
                    - pos);
            System.arraycopy(missing, 0, newMissing, 0, pos);
            System.arraycopy(missing, pos, newMissing, pos + 1, internal.length
                    - pos);

            System.arraycopy(empty, 0, newEmpty, 0, pos);
            System.arraycopy(empty, pos, newEmpty, pos + 1, internal.length
                    - pos);
        }
        newInternal[pos] = ((Double)newEntry).doubleValue();
        internal = newInternal;
		this.setMissingValues(newMissing);
        empty = newEmpty;
    }

    /**
     Swaps two entries in the Column
     @param pos1 the position of the 1st entry to swap
     @param pos2 the position of the 2nd entry to swap
     */
    public void swapRows (int pos1, int pos2) {
        double d1 = internal[pos1];
        boolean miss = missing[pos1];
        boolean emp = empty[pos1];
        internal[pos1] = internal[pos2];
        internal[pos2] = d1;
        missing[pos1] = missing[pos2];
        missing[pos2] = miss;

        empty[pos1] = empty[pos2];
        empty[pos2] = emp;
    }

    /**
     Get a copy of this Column, reordered, based on the input array of indices.
     Does not overwrite this Column.
     @param newOrder an array of indices indicating a new order
     @return a copy of this column, re-ordered
     */
    public Column reorderRows (int[] newOrder) {
        double[] newInternal = null;
        boolean[] newMissing = null;
        boolean[] newEmpty = null;
        if (newOrder.length == internal.length) {
            newInternal = new double[internal.length];
			newMissing=new boolean[internal.length];
			newEmpty=new boolean[internal.length];
            for (int i = 0; i < internal.length; i++) {
                newInternal[i] = internal[newOrder[i]];
                newMissing[i] = missing[newOrder[i]];
                newEmpty[i] = empty[newOrder[i]];
            }
        }
        else
            throw  new ArrayIndexOutOfBoundsException();
        DoubleColumn dc = new DoubleColumn(newInternal, newMissing, newEmpty, getLabel(), getComment());
        return  dc;
    }

    /**
     Compare the values of the object passed in and pos. Return 0 if they
     are the same, greater than zero if element is greater,
     and less than zero if element is less.
     @param element the object to be passed in should be a subclass of Number
     @param pos the position of the element in Column to be compared with
     @return a value representing the relationship- >, <, or == 0
     */
    public int compareRows (Object element, int pos) {
        double d1 = ((Number)element).doubleValue();
        double d2 = internal[pos];
        /*if (d1 == scalarEmptyValue) {
            if (d2 == scalarEmptyValue)
                return  0;
            else
                return  -1;
        }
        else if (d2 == scalarEmptyValue)
            return  1;

		if(d1 == scalarMissingValue) {
			if(d2 == scalarMissingValue)
				return 0;
			else
				return -1;
		}
		else if(d2 == scalarMissingValue)
			return 1;
        */

        if (d1 > d2)
            return  1;
        else if (d1 < d2)
            return  -1;
        return  0;
    }

    /**
     Compare pos1 and pos2 positions in the Column. Return 0 if they
     are the same, greater than zero if pos1 is greater,
     and less than zero if pos1 is less.

     @param pos1 the position of the first element to compare
     @param pos2 the position of the second element to compare
     @return a value representing the relationship- >, <, or == 0
     */
    public int compareRows (int pos1, int pos2) {
        double d1 = internal[pos1];
        double d2 = internal[pos2];

        if (d1 > d2)
            return  1;
        else if (d1 < d2)
            return  -1;
        return  0;
    }

    //////////////////////////////////////

    /**
     Given an array of ints, will remove the positions in the Column
     which are indicated by the ints in the array.
     @param indices the int array of remove indices
     */
    public void removeRowsByIndex (int[] indices) {
        HashSet toRemove = new HashSet(indices.length);
        for (int i = 0; i < indices.length; i++) {
            Integer id = new Integer(indices[i]);
            toRemove.add(id);
        }
        double newInternal[] = new double[internal.length - indices.length];
        boolean newMissing[] = new boolean[internal.length - indices.length];
        boolean newEmpty[] = new boolean[internal.length - indices.length];
        int newIntIdx = 0;
        for (int i = 0; i < getNumRows(); i++) {
            // check if this row is in the list of rows to remove
            //Integer x = (Integer)toRemove.get(new Integer(i));
            // if this row is not in the list, copy it into the new internal
            //if (x == null) {
            if(!toRemove.contains(new Integer(i))){
                newInternal[newIntIdx] = internal[i];
                newMissing[newIntIdx] = missing[i];
                newEmpty[newIntIdx] = empty[i];
                newIntIdx++;
            }
        }
        internal = newInternal;
		this.setMissingValues(newMissing);
        empty = newEmpty;
    }

    /**
     Sort the elements in this column.
     @exception NotSupportedException when sorting is not supported
     */
    public void sort () {
        sort(null);
    }

    /**
     Sort the elements in this column, and swap the rows in the table
     we are a part of.
     @param t the Table to swap rows for
     @exception NotSupportedException when sorting is not supported
     */
    public void sort (MutableTable t) {
        internal = doSort(internal, 0, internal.length - 1, t);
    }

    /**
       Sort the elements in this column starting with row 'begin' up to row 'end',
       and swap the rows in the table  we are a part of.
       @param t the VerticalTable to swap rows for
       @param begin the row no. which marks the beginnig of the  column segment to be sorted
       @param end the row no. which marks the end of the column segment to be sorted
       @exception NotSupportedException when sorting is not supported
    */
    public void sort(MutableTable t,int begin, int end)
    {
    if (end > internal.length -1) {
        System.err.println(" end index was out of bounds");
        end = internal.length -1;
    }
    internal = doSort(internal, begin, end, t);

    }

    /**
     Implement the quicksort algorithm.  Partition the array and
     recursively call doSort.
     @param A the array to sort
     @param p the beginning index
     @param r the ending index
     @param t the Table to swap rows for
     @return a sorted array of doubles
     */
    private double[] doSort (double[] A, int p, int r, MutableTable t) {
        if (p < r) {
            int q = partition(A, p, r, t);
            doSort(A, p, q, t);
            doSort(A, q + 1, r, t);
        }
        return  A;
    }

    /**
     Rearrange the subarray A[p..r] in place.
     @param A the array to rearrange
     @param p the beginning index
     @param r the ending index
     @param t the Table to swap rows for
     @return the partition point
     */
    private int partition (double[] A, int p, int r, MutableTable t) {
		double x = A[p];
		boolean xMissing = this.isValueMissing(p);
		int i = p - 1;
		int j = r + 1;
		while (true) {
			if (xMissing) {
				j--;
				do {
					i++;
				} while (!this.isValueMissing(i));
			} else {
				do {
					j--;
				} while (this.isValueMissing(j) || (A[j] > x));
				do {
					i++;
				} while (!this.isValueMissing(i) && (A[i] < x));
			}
			if (i < j) {
				if (t == null)
					this.swapRows(i, j);
				else
					t.swapRows(i, j);
			}
			else
				return  j;
		}
    }

    public void setValueToEmpty(boolean b, int row) {
        empty[row] = b;
    }

    public boolean isValueEmpty(int row) {
        return empty[row];
	}
}
/*DoubleColumn*/

