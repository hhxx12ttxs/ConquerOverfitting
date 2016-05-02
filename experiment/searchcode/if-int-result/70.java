package com.webobjects.foundation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Implemented EXCEPT for:
 * - sortedArrayUsingComparator
 * - Enumeration methods
 * - makeObjectsPerformSelector
 * - NSCoding
 */
public class NSArray<E> extends ArrayList<E> implements NSKeyValueCoding, 
	NSKeyValueCodingAdditions {

	public static interface Operator {
		public Object compute(NSArray values, String keyPath);
	}
	
	public static final transient boolean IsEmulationClass = true;
	
	public static final transient String AverageOperatorName = "avg";
	public static final transient String CountOperatorName = "count";
	public static final transient String MaximumOperatorName = "max";
	public static final transient String MinimumOperatorName = "min";
	public static final transient String SumOperatorName = "sum";
	
	public static final transient int NotFound = -1;
	
	public static final transient NSArray EmptyArray = new NSArray();
	
	public static final transient boolean CheckForNull = true;
	public static final transient boolean IgnoreNull = true;
	
	private static final transient String UNSUPPORTED = " is not a supported operation in com.webobjects.foundation.NSArray";
	protected static final transient String NULL_NOT_ALLOWED = "Attempt to insert null into an NSArray.";
	
	public NSArray() {
		super();
	}
	
	protected NSArray(int capacity) {
		super(capacity);
	}
	
	public NSArray(E object) {
		superDotAdd(object);
	}
	
	public NSArray(E[] objects) {
		if (objects == null)
			throw new IllegalArgumentException("objects may not be null");
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] != null)
				superDotAdd(objects[i]);
		}
	}
	
	public NSArray(E[] objects, NSRange range) {
		if (objects == null)
			throw new IllegalArgumentException("objects may not be null");
		if (range == null)
			throw new IllegalArgumentException("range may not be null");
		
		NSArray sourceObjects = new NSArray(objects);
		NSArray items = sourceObjects.subarrayWithRange(range);
		superDotAddAll(items);
	}
	
	public NSArray(NSArray<? extends E> otherArray) {
		if (otherArray == null)
			throw new IllegalArgumentException("otherArray may not be null");
		superDotAddAll(otherArray);
	}
		
	public NSArray(Collection<? extends E> collection, boolean checkForNull) {
		if (checkForNull && collection.contains(null))
			throw new IllegalArgumentException(NULL_NOT_ALLOWED);
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			superDotAdd(e);
		}
	}
	
	public NSArray(Collection<? extends E> collection)  {
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			if (e == null) {
				throw new IllegalArgumentException(NULL_NOT_ALLOWED);
			}
			superDotAdd(e);
		}
	}
	
	public NSArray(List<? extends E> list, NSRange range, boolean ignoreNull) {
		if (list == null)
			throw new IllegalArgumentException("list may not be null");
		if (range == null)
			throw new IllegalArgumentException("range may not be null");
		
		for (int i = range.location(); i <= range.maxRange()-1; i++) {
			E element = (E)list.get(i);
			if (element == null) {
				if (!ignoreNull)
					throw new IllegalArgumentException(NULL_NOT_ALLOWED);
			} else {
				superDotAdd(element);
			}
		}
	}
	
	public NSArray<E> arrayByAddingObject(E object) {
		if (object == null)
			throw new IllegalArgumentException("object may not be null");
		NSMutableArray<E> result = this.mutableClone();
		result.add(object);
		return result.immutableClone();
	}
	
	public NSArray<E> arrayByAddingObjectsFromArray(NSArray<E> otherArray) {
		if (otherArray == null)
			return this;
		NSMutableArray<E> result = this.mutableClone();
		result.addAll(otherArray);
		return result.immutableClone();
	}
	
	public ArrayList<E> arrayList() {
		return this;
	}
	
	@Override
	public Object clone() {
		return this;
	}
	
	public String componentsJoinedByString(String separator) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < size(); i++) {
			result.append(get(i).toString());
			if (i < size()-1 && separator != null)
				result.append(separator);
		}
		return result.toString();
	}
	
	/* copied from Project Wonder's NSArray implementation */
	public static NSArray componentsSeparatedByString(String string, String separator) {
		NSMutableArray objects;
		label0: {
			if (string == null) {
				return EmptyArray;
			}
			int stringLength = string.length();
			int separatorLength = separator == null ? 0 : separator.length();
			if (stringLength == 0) {
				return EmptyArray;
			}
			if (separatorLength == 0) {
				return new NSArray(string);
			}
			int start = 0;
			int index = 0;
			int count = 0;
			if (separatorLength == 1 && stringLength < 256) {
				char parseData[] = string.toCharArray();
				char charSeparator = separator.charAt(0);
				for (int i = 0; i < stringLength; i++) {
					if (parseData[i] == charSeparator) {
						count++;
					}
				}

				if (count == 0) {
					return new NSMutableArray(string);
				}
				objects = new NSMutableArray();
				int end = stringLength - 1;
				for (index = 0; index <= end; index++) {
					if (parseData[index] != charSeparator) {
						continue;
					}
					if (start == index) {
						objects.addObject("");
					}
					else {
						objects.addObject(string.substring(start, index));
					}
					start = index + 1;
				}

				if (parseData[end] == charSeparator) {
					if (start < end) {
						objects.addObject(string.substring(start, end));
					}
					objects.addObject("");
				}
				else {
					objects.addObject(string.substring(start, stringLength));
				}
				break label0;
			}
			objects = new NSMutableArray(4);
			int end = stringLength - separatorLength;
			do {
				if (start >= stringLength) {
					break label0;
				}
				index = string.indexOf(separator, start);
				if (index < 0) {
					index = stringLength;
				}
				if (index == end) {
					break;
				}
				objects.addObject(string.substring(start, index));
				start = index + separatorLength;
			}
			while (true);
			if (start < index) {
				objects.addObject(string.substring(start, index));
			}
			objects.addObject("");
		}
		return objects;
	}
	
	public boolean containsObject(Object object) {
		return contains(object);
	}
	
	public int count() {
		return size();
	}
	
	public static <T> NSArray<T> emptyArray() {
		return new NSArray();
	}
	
	public Object firstObjectCommonWithArray(NSArray otherArray) {
		if (otherArray == null || otherArray.isEmpty())
			return null;
		
		for (int i = 0; i < size(); i++) {
			if (otherArray.contains(get(i)))
				return get(i);
		}
		
		return null;
	}
	
	public NSArray immutableClone() {
		return this;
	}
	
	public int indexOfIdenticalObject(Object object) {
		if (object == null)
			return NotFound;
		for (int i = 0; i < size(); i++) {
			if (get(i) == object)
				return i;
		}
		return NotFound;
	}
	
	public int indexOfIdenticalObject(Object object, NSRange range) {
		if (object == null)
			return NotFound;
		if (range == null)
			throw new IllegalArgumentException("range may not be null");
		NSArray subArray = subarrayWithRange(range);
		int result = subArray.indexOfIdenticalObject(object);
		if (result != NotFound) {
			result += range.location;
		} 
		return result;
	}
	
	public int indexOfObject(Object object) {
		return indexOf(object);
	}
	
	public int indexOfObject(Object object, NSRange range) {
		if (object == null)
			return NotFound;
		if (range == null)
			throw new IllegalArgumentException("range may not be null");
		NSArray subArray = subarrayWithRange(range);
		return subArray.indexOfObject(object);
	}
	
	public boolean isEqualToArray(NSArray otherArray) {
		return equals(otherArray);
	}
	
	public E lastObject() {
		if (isEmpty()) {
			return null;
		} else {
			return get(size()-1);
		}
	}
	
	// since reflection isn't available in GWT, we can't do this
//	public void makeObjectsPerformSelector(NSSelector selector, Object[] parameters) {
//	throw new RuntimeException("not implemented");		
//	}
	
	public NSMutableArray<E> mutableClone() {
		return new NSMutableArray<E>(this);
	}
	
	public E objectAtIndex(int index) {
		return (E)get(index);
	}
	
	public Enumeration<E> objectEnumerator() {
		return new ListEnumeration();
	}
	
	public E[] objects() {
		return (E[])toArray();
	}
	
	public Object[] objects(NSRange range) {
		if (range == null)
			throw new IllegalArgumentException("range may not be null");
		return subarrayWithRange(range).toArray();
	}
	
	public static NSArray<String> operatorNames() {
		return new NSArray( new Object[] {
				AverageOperatorName,
				CountOperatorName,
				MaximumOperatorName,
				MinimumOperatorName,
				SumOperatorName
		});
	}
	
//	public static Operator operatorForKey(String operatorName) {
//		throw new RuntimeException("not implemented");
//	}
//	
//	public static void removeOperatorForKey(String operatorName) {
//		throw new RuntimeException("not implemented");
//	}
//	
//	public static void setOperatorForKey(String operatorName, NSArray.Operator arrayOperator) {
//		throw new RuntimeException("not implemented");
//	}
	
	public Enumeration<E> reverseObjectEnumerator() {
		return new ListReverseEnumeration();
	}
	
	public NSArray<E> sortedArrayUsingComparator(NSComparator comparator) 
		throws NSComparator.ComparisonException {
		NSArray result = mutableClone();
		Collections.sort(result, (Comparator)comparator);
		return result.immutableClone();
	}
	
	public NSArray<E> subarrayWithRange(NSRange range) {
		if (range == null)
			throw new IllegalArgumentException("range may not be null");
		NSMutableArray<E> result = new NSMutableArray<E>();
		for (int i = range.location(); i <= range.maxRange()-1; i++) {
			result.add(get(i));
		}
		return result.immutableClone();
	}
	
	public Object valueForKey(String key) {
		if (key == null)
			return null;
		NSMutableArray result = new NSMutableArray();
		for (int i = 0; i < size(); i++) {
			Object item = get(i);
			if (item instanceof NSKeyValueCoding) {
				Object value = ((NSKeyValueCoding)item).valueForKey(key);
				if (value != null)
					result.add(value);
			}
		}
		return result.immutableClone();
	}
	
	public void takeValueForKey(Object value, String key) {
		if (key == null)
			return;
		for (int i = 0; i < size(); i++) {
			Object item = get(i);
			if (item instanceof NSKeyValueCoding) {
				((NSKeyValueCoding)item).takeValueForKey(value, key);
			}
		}
	}
	
	public Object valueForKeyPath(String keyPath) {
		if (keyPath == null)
			return null;
		else if (keyPath.startsWith("@" + CountOperatorName)) {
			return size();
		}
		
		String aggregateFunction;
		String keyAfterAggregate;
		if (keyPath.startsWith("@")) {
			aggregateFunction = NSKeyValueCodingAdditions.DefaultImplementation.firstPartOfKeyPath(keyPath);
			keyAfterAggregate = NSKeyValueCodingAdditions.DefaultImplementation.restOfKeyPath(keyPath);
		} else {
			aggregateFunction = null;
			keyAfterAggregate = keyPath;
		}
		
		NSMutableArray result = new NSMutableArray();
		for (int i = 0; i < size(); i++) {
			Object item = get(i);
			if (item instanceof NSKeyValueCodingAdditions) {
				Object value = ((NSKeyValueCodingAdditions)item).valueForKeyPath(keyAfterAggregate);
				if (value != null)
					result.add(value);
			}
		}
		
		if (aggregateFunction == null) {
			return result.immutableClone();
		} else if (aggregateFunction.equals("@" + SumOperatorName)) {
			return result.sum();
		} else if (aggregateFunction.equals("@" + AverageOperatorName)) {
			return result.avg();
		} else if (aggregateFunction.equals("@" + MaximumOperatorName)) {
			return result.max();
		} else if (aggregateFunction.equals("@" + MinimumOperatorName)) {
			return result.min();
		} else {
			throw new IllegalArgumentException("Invalid aggregate function");
		}
	}
	
	protected Object max() {
		if (isEmpty()) {
			return NSKeyValueCoding.NullValue;
		}
		
		Comparable max = (Comparable) get(0);
		for (int i = 0; i < size(); i++) {
			Comparable element = (Comparable) get(i);
			if (element.equals(NSKeyValueCoding.NullValue))
				continue;
			if (element.compareTo(max) > 0) {
				max = element;
			}
		}
		
		return max;
	}
	
	protected Object min() {
		if (isEmpty()) {
			return NSKeyValueCoding.NullValue;
		}
		
		Comparable min = (Comparable) get(0);
		for (int i = 0; i < size(); i++) {
			Comparable element = (Comparable) get(i);
			if (element.equals(NSKeyValueCoding.NullValue))
				continue;
			if (element.compareTo(min) < 0) {
				min = element;
			}
		}
		
		return min;
	}
	
	protected Object sum() {
		if (isEmpty()) {
			return NSKeyValueCoding.NullValue;
		}
		
		BigDecimal sum = new BigDecimal("0");
		for (int i = 0; i < size(); i++) {
			Object element = get(i);
			if (element.equals(NSKeyValueCoding.NullValue))
				continue;
			if (element instanceof BigDecimal) {
				sum = sum.add((BigDecimal)element);
			} else {
				sum = sum.add(new BigDecimal(((Number)element).toString()));
			}
		}
		
		return sum;
	}
	
	protected int nonNullCount() {
		int result = 0;
		for (int i = 0; i < size(); i++) {
			Object element = get(i);
			if (!element.equals(NSKeyValueCoding.NullValue))
				result++;
		}
		return result;
	}
	
	protected Object avg() {
		if (isEmpty()) {
			return NSKeyValueCoding.NullValue;
		}
		
		return ((BigDecimal)sum()).divide(
				new BigDecimal(new Integer(nonNullCount()).toString()), 
				BigDecimal.ROUND_HALF_UP);
	}
	
	public void takeValueForKeyPath(Object value, String keyPath) {
		if (keyPath == null)
			return;
		for (int i = 0; i < size(); i++) {
			Object item = get(i);
			if (item instanceof NSKeyValueCodingAdditions) {
				((NSKeyValueCodingAdditions)item).takeValueForKeyPath(value, keyPath);
			}
		}
	}
	
	public Vector<E> vector() {
		return new Vector(this);
	}
	
	protected boolean superDotAdd(E o) {
		//if (o == null)
		//	throw new IllegalArgumentException(NULL_NOT_ALLOWED);
		return super.add(o);
	}
	
	protected void superDotAdd(int index, E element) {
		//if (element == null)
		//	throw new IllegalArgumentException(NULL_NOT_ALLOWED);
		super.add(index, element);
	}
	
	protected boolean superDotAddAll(Collection c) {
		//if (c.contains(null))
		//	throw new IllegalArgumentException(NULL_NOT_ALLOWED);
		return super.addAll(c);
	}
	
	protected boolean superDotAddAll(int index, Collection c) {
		//if (c.contains(null))
		//	throw new IllegalArgumentException(NULL_NOT_ALLOWED);
		return super.addAll(index, c);
	}
	
	protected void superDotClear() {
		super.clear();
	}
	
	protected E superDotRemove(int index) {
		return super.remove(index);
	}
	
	protected boolean superDotRemove(Object o) {
		return super.remove(o);
	}
	
	protected boolean superDotRemoveAll(Collection c) {
		return super.removeAll(c);
	}
	
	protected boolean superDotRetainAll(Collection c) {
		return super.retainAll(c);
	}
	
	protected E superDotSet(int index, E element) {
		return super.set(index, element);
	}
		
	@Override
	public boolean add(E o) {
		throw new UnsupportedOperationException("add" + UNSUPPORTED);
	}
	
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("add" + UNSUPPORTED);
	}
	
	@Override
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException("addAll" + UNSUPPORTED);
	}
	
	@Override
	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException("addAll" + UNSUPPORTED);
	}	
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("clear" + UNSUPPORTED);
	}
	
	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException("remove" + UNSUPPORTED);
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("remove" + UNSUPPORTED);
	}
	
	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException("removeAll" + UNSUPPORTED);
	}
	
	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException("retainAll" + UNSUPPORTED);
	}
	
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("set" + UNSUPPORTED);
	}
	
	private class ListEnumeration implements Enumeration<E> {
		private int currentIndex = -1;
		
		public ListEnumeration() {
			super();
		}
		
		public boolean hasMoreElements() {
			return currentIndex < size()-1;
		}
		
		public E nextElement() {
			if (hasMoreElements()) {
				currentIndex++;
				return get(currentIndex);
			} else {
				throw new NoSuchElementException();
			}
		}
	}
	
	private class ListReverseEnumeration implements Enumeration<E> {
		private int currentIndex = size();
		
		public ListReverseEnumeration() {
			super();
		}
		
		public boolean hasMoreElements() {
			return currentIndex > 0;
		}
		
		public E nextElement() {
			if (hasMoreElements()) {
				currentIndex--;
				return get(currentIndex);
			} else {
				throw new NoSuchElementException();
			}
		}
	}
}

