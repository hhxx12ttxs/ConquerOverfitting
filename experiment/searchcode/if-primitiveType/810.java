/*
 * MIPL: Mining Integrated Programming Language
 *
 * File: BuiltinRowSum.java
 * Author: YoungHoon Jung <yj2244@columbia.edu>
 * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
 * Description: A Class that implements Built-in RowSum Job
 */
package edu.columbia.mipl.builtin.job;

import java.util.*;
import java.lang.reflect.*;

import edu.columbia.mipl.datastr.*;
import edu.columbia.mipl.runtime.execute.*;

public class BuiltinRowSum implements BuiltinJob {
	public String getName() {
		return "rowsum";
	}

	public List<PrimitiveType> jobImplementation(PrimitiveType ... args) throws MiplRuntimeException {
		if (args.length != 1)
			throw new MiplRuntimeException();

		if (!(args[0] instanceof PrimitiveMatrix))
			throw new MiplRuntimeException();

		PrimitiveMatrix<Double> m = (PrimitiveMatrix<Double>) args[0];

		List<PrimitiveType> list = new ArrayList<PrimitiveType>();

		list.add(PrimitiveOperations.rowsum(m));
		
		return list;
	}
}

