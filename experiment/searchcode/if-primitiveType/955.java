/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crystalscriptcompiler.syntaxtree.types;

/**
 *
 * @author User
 */
public class PrimitiveType extends Type {
	
	public static enum Kind {
		ARRAY,
		BOOLEAN,
		NUMBER,
		OBJECT,
		STRING,
		NULL
	}
	
	private Kind kind;

	public PrimitiveType(Kind kind) {
		this.kind = kind;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof ClassOrInterfaceType && kind == Kind.NULL) {
			// For null to object assignment
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PrimitiveType other = (PrimitiveType) obj;
		if (this.kind != other.kind) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + (this.kind != null ? this.kind.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return kind.toString().toLowerCase();
	}
	
}

