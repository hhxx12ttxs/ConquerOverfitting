/*
 * MIPL: Mining Integrated Programming Language
 *
 * File: BuiltinUCol.java
 * Author: YoungHoon Jung <yj2244@columbia.edu>
 * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
 * Description: A Class that implements Built-in UCol Job that returns Unbound Col Matrix
 */
package edu.columbia.mipl.builtin.job;

import java.util.*;

import edu.columbia.mipl.datastr.*;
import edu.columbia.mipl.runtime.execute.*;
import edu.columbia.mipl.builtin.matrix.*;

public class BuiltinUCol implements BuiltinJob {
	public String getName() {
		return "ucol";
	}

	public List<PrimitiveType> jobImplementation(PrimitiveType ... args) throws MiplRuntimeException {
		if (args.length != 1)
			throw new MiplRuntimeException();

		if (!(args[0] instanceof PrimitiveMatrix))
			throw new MiplRuntimeException();

		PrimitiveMatrix<Double> m = (PrimitiveMatrix<Double>) args[0];

		List<PrimitiveType> list = new ArrayList<PrimitiveType>();

		list.add(new UnboundColMatrix(m));
		
		return list;
	}
}

