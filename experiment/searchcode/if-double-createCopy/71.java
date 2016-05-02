/*
 *  streams library
 *
 *  Copyright (C) 2011-2012 by Christian Bockermann, Hendrik Blom
 * 
 *  streams is a library, API and runtime environment for processing high
 *  volume data streams. It is composed of three submodules "stream-api",
 *  "stream-core" and "stream-runtime".
 *
 *  The streams library (and its submodules) is free software: you can 
 *  redistribute it and/or modify it under the terms of the 
 *  GNU Affero General Public License as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any 
 *  later version.
 *
 *  The stream.ai library (and its submodules) is distributed in the hope
 *  that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package stream.data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import stream.Data;
import stream.Measurable;
import stream.util.JavaSerializer;
import stream.util.SizeOf;

/**
 * <p>
 * This class is the default implementation of the Data item. The complete
 * implementation is based upon Java's core LinkedHashMap implementation.
 * </p>
 * <p>
 * Objects of this class should not be created directory, but rather by using a
 * {@link stream.data.DataFactory}.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class DataImpl extends LinkedHashMap<String, Serializable> implements
		Data, Measurable {

	/** The unique class ID */
	private static final long serialVersionUID = -7751681008628413236L;

	final static boolean deepClone = "true".equalsIgnoreCase(System
			.getProperty("stream.Data.deepClone"));

	final transient JavaSerializer javaSerializer = new JavaSerializer();

	/**
	 * Creation of Data items should be done with
	 * {@link stream.data.DataFactory#create()}
	 */
	protected DataImpl() {
	}

	/**
	 * @param data
	 * @deprecated Creation of Data items should be done with
	 *             {@link stream.data.DataFactory#create()}
	 */
	public DataImpl(Map<String, Serializable> data) {
		super(data);
	}

	/**
	 * @see stream.Measurable#getByteSize()
	 */
	@Override
	public double getByteSize() {

		double size = 0.0d;

		for (String key : keySet()) {
			size += key.length() + 1; // provide the rough size of one byte for
										// each character + a single terminating
										// 0-byte

			// add the size of each value of this map
			//
			Serializable value = get(key);
			size += SizeOf.sizeOf(value);
		}

		return size;
	}

	/**
	 * @see stream.Data#copy()
	 */
	@Override
	public Data createCopy() {
		if (this == Data.END_OF_STREAM)
			return Data.END_OF_STREAM;

		if (!deepClone) {
			return new DataImpl(this);
		}

		return DataFactory.copy(this);
	}
}

