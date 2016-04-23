/*
 * MIPL: Mining Integrated Programming Language
 *
 * File: BuiltinLoad.java
 * Author: YoungHoon Jung <yj2244@columbia.edu>
 * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
 * Description: A Class that implements Built-in Load Job
 */
package edu.columbia.mipl.builtin.job;

import java.util.*;
import java.lang.reflect.*;

import edu.columbia.mipl.datastr.*;
import edu.columbia.mipl.runtime.execute.*;

public class BuiltinLoad implements BuiltinJob {
	public String getName() {
		return "load";
	}

	public List<PrimitiveType> jobImplementation(PrimitiveType ... args) throws MiplRuntimeException {
		if (args.length != 1)
			throw new MiplRuntimeException();

		if (!(args[0] instanceof PrimitiveString))
			throw new MiplRuntimeException();

		List<PrimitiveType> list = new ArrayList<PrimitiveType>();

		String filename = ((PrimitiveString) args[0]).getData();

		list.add(new PrimitiveMatrix<Double>(filename));
		
		return list;
	}
}

