/*
 * MIPL: Mining Integrated Programming Language
 *
 * File: BuiltinURow.java
 * Author: YoungHoon Jung <yj2244@columbia.edu>
 * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
 * Description: A Class that implements Built-in URow Job that returns Unbound Row Matrix
 */
package edu.columbia.mipl.builtin.job;

import java.util.*;

import edu.columbia.mipl.datastr.*;
import edu.columbia.mipl.runtime.execute.*;
import edu.columbia.mipl.builtin.matrix.*;

public class BuiltinURow implements BuiltinJob {
	public String getName() {
		return "urow";
	}

	public List<PrimitiveType> jobImplementation(PrimitiveType ... args) throws MiplRuntimeException {
		if (args.length != 1)
			throw new MiplRuntimeException();

		if (!(args[0] instanceof PrimitiveMatrix))
			throw new MiplRuntimeException();

		PrimitiveMatrix<Double> m = (PrimitiveMatrix<Double>) args[0];

		List<PrimitiveType> list = new ArrayList<PrimitiveType>();

		list.add(new UnboundRowMatrix(m));
		
		return list;
	}
}

