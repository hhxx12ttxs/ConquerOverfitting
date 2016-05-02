package com.iver.cit.gvsig.fmap.selection;

import java.util.BitSet;
import java.util.NoSuchElementException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
/**
 * <p>Allows to access a field from a selection of rows in a DataSource,
 * using an Iterator interface (instead of the painful BitSet interface).</p>
 * 
 * <p>This class is intended to smooth transition to 2.0 version, in which
 * random access has been replaced by iterators.</p>
 * 
 * <p>Warning: This object is not thread-safe!!! Use external synchronization
 * if you plan to use it from different threads, otherwise it is warranted
 * to fail!!</p>
 * 
 * @author Cesar Martinez Izquierdo <cesar.martinez@iver.es> 18/02/2009
 * @author IVER T.I. <http://www.iver.es> 18/02/2009
 */
public class SelectionFieldIterator
		implements ResettableIterator<Value>, Cloneable {
	DataSource ds = null;
	BitSet selectedRows = null;
	int fieldIndex;
	int currBit;
	int nextSet;
	boolean hasNext;
	boolean usedNext = true;

	/**
	 * <p>Creates a new Iterator, which is able to access the provided
	 * <code>selection</code> from <code>ds</code>.</p>
	 * 
	 * @param ds 
	 * @param selection The selection of rows to iterate through
	 * @param fieldIndex The index of the field to iterate on
	 */
	public SelectionFieldIterator(DataSource ds, BitSet selection, int fieldIndex) {
		this.selectedRows = selection;
		this.ds = ds;
		this.fieldIndex = fieldIndex;
		reset();
	}

	/**
	 * <p>Creates a new Iterator, which is able to access the provided
	 * <code>selection</code> from <code>ds</code>.</p>
	 * 
	 * @param ds 
	 * @param selection The selection of rows to iterate through
	 * @param fieldIndex The name of the field to iterate on
	 */
	public SelectionFieldIterator(DataSource ds, BitSet selection, String fieldName) throws ReadDriverException {
		this(ds, selection, ds.getFieldIndexByName(fieldName));
	}

	public boolean hasNext() {
		if (usedNext==true) {
			currBit = selectedRows.nextSetBit(currBit+1);
			if (currBit<0) {
				hasNext = false;
			}
			else {
				hasNext = true; 
			}
			usedNext = false;
		}
		return hasNext;
	}

	public Value next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		usedNext = true;
		try {
			ds.start();
			Value result = ds.getFieldValue(currBit, fieldIndex);
			ds.stop();
			return result;
		} catch (ReadDriverException e) {
			NoSuchElementException e1 = new NoSuchElementException();
			e1.initCause(e);
			throw e1;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void reset() {
		currBit = -1;
		usedNext = true;
	}

	public Object clone() throws CloneNotSupportedException {
		SelectionFieldIterator obj = (SelectionFieldIterator) super.clone();
		obj.reset();
		return obj;
	}
}

