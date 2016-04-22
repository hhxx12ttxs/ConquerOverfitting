package su.boleyn.bsl.compiler.type;

import su.boleyn.bsl.compiler.exception.TypeException;

public class PrimitiveType extends Type {
	public String typename;

	public PrimitiveType(String t) {
		typename = t;
	}

	public String toString_() {
		return typename;
	}

	public void merge(Type t) throws TypeException {
		if (t instanceof PrimitiveType) {
			if (!((PrimitiveType) t).typename.equals(typename)) {
				throw new TypeException(((PrimitiveType) t).typename + " != "
						+ typename);
			}
			t.root = this;
		} else if (t instanceof FunctionType) {
			throw new TypeException(toString() + " != " + t.toString());
		} else {
			t.root = this;
		}
	}
}

