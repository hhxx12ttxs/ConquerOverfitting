/**   
 * Copyright 2009-2010 Sönke Sothmann, Steffen Schäfer and others
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.gwtgl.wrapper;

import java.util.Iterator;

import com.google.gwt.typedarrays.client.ArrayBuffer;
import com.google.gwt.typedarrays.client.IntBasedTypedArray;
import com.google.gwt.typedarrays.client.TypedArray;
import com.googlecode.gwtgl.wrapper.enums.DataType;

/**
 * Base class for all wrapped {@link TypedArray} types.
 * 
 * @author Steffen Schäfer
 *
 * @param <T>
 * @param <S>
 */
public abstract class Array<T extends TypedArray<T>, S extends Number> implements Iterable<S> {
	private final T nativeArray;

	/**
	 * Creates an Array instance with the given native object to wrap.
	 * 
	 * @param nativeArray
	 */
	protected Array(T nativeArray) {
		this.nativeArray = nativeArray;
	}

	/**
	 * Returns the wrapped native array object.
	 * 
	 * @return the native array object
	 */
	protected T getNativeArray() {
		return nativeArray;
	}

	/**
	 * Returns the length of the Array in bytes.
	 * 
	 * @return the length of the Array in bytes
	 */
	public int getByteLength() {
		return nativeArray.getByteLength();
	}

	/**
	 * Returns the offset of the TypedArray in the underlying {@link ArrayBuffer}.
	 * 
	 * @return the offset of the WebGLArray in the ArrayBuffer
	 */
	public int getByteOffset() {
		return nativeArray.getByteOffset();
	}
	
	/**
	 * Returns the number of objects contained in the array.
	 * 
	 * @return the length of the array
	 */
	public int getLength() {
		return nativeArray.getLength();
	}

	/**
	 * Returns the type of the values contained in the Array.
	 * 
	 * @return the type of the values contained in the Array
	 */
	public abstract DataType getDataType();
	
	abstract static class IntBasedArray<T extends IntBasedTypedArray<T>> extends Array<T, Integer> {

		protected IntBasedArray(T nativeArray) {
			super(nativeArray);
		}
		/**
		 * Return the element at the given index. If the index is out of range, an exception is raised. This is an index getter function, and may be invoked via array index syntax where applicable.
		 * 
		 * @param index
		 * @return the element at the given index
		 */
		public int get(int index) {
			return getNativeArray().get(index);
		};
		
		/**
		 * 
		 * Sets the element at the given index to the given value. If the index is out of range, an exception is raised. This is an index setter function, and may be invoked via array index syntax where applicable.
		 * 
		 * If the given value is out of range for the type of the array, a C-style cast operation is performed to coerce the value to the valid range. No clamping or rounding is performed.
		 * 
		 * @param index
		 * @param value
		 */
		public void set(int index, int value) {
			getNativeArray().set(index, value);
		};
		
		/**
		 * Set multiple values, reading input values from the array.
		 * 
		 * @param array
		 */
		public void set(T array) {
			getNativeArray().set(array);
		};
		
		/**
		 * Set multiple values, reading input values from the array. The optional offset value indicates the index in the current array where values are written. If omitted, it is assumed to be 0.
		 * 
		 * If the offset plus the length of the given array is out of range for the current WebGLArray, an exception is raised.
		 * 
		 * @param array
		 * @param offset
		 */
		public void set(T array, int offset) {
			getNativeArray().set(array, offset);
		};
		
		/**
		 * Set multiple values, reading input values from the array.
		 * 
		 * @param array
		 */
		public void set(int[] array) {
			getNativeArray().set(array);
		};
		
		/**
		 * Set multiple values, reading input values from the array. The optional offset value indicates the index in the current array where values are written. If omitted, it is assumed to be 0.
		 * 
		 * If the offset plus the length of the given array is out of range for the current WebGLArray, an exception is raised.
		 * 
		 * @param array
		 * @param offset
		 */
		public void set(int[] array, int offset) {
			getNativeArray().set(array, offset);
		};
		
		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				private int idx = 0;

				@Override
				public boolean hasNext() {
					return getLength() > idx;
				}

				@Override
				public Integer next() {
					return get(idx++);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
	}

}

