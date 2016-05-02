package org.archstudio.prolog.term;

public class ConstantTerm implements Term {

	private final Object value;

	public ConstantTerm(Object value) {
		this.value = value;
	}

	@Override
	public boolean contains(Term v) {
		return false;
	}

	@Override
	public Term replace(Term v, Term t) {
		return this;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public Object getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConstantTerm other = (ConstantTerm) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		}
		else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}

