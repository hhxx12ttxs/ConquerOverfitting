package ccu.pllab.tcgen.libs.pivotmodel.type;

import java.util.ArrayList;
import java.util.List;

import ccu.pllab.tcgen.libs.pivotmodel.Operation;

public class PrimitiveType extends DataType {

	private String name;
	private List<Operation> operations;

	protected PrimitiveType(String name, List<Operation> ops) {
		this.name = name;
		this.operations = ops;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrimitiveType other = (PrimitiveType) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public List<Operation> getOwnedOperations() {
		return new ArrayList<Operation>(this.operations);
	}

}

