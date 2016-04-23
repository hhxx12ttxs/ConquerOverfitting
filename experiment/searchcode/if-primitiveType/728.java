package ccu.pllab.tcgen.libs.pivotmodel.type;

import ccu.pllab.tcgen.libs.pivotmodel.Model;
import ccu.pllab.tcgen.libs.pivotmodel.UML2Class;

public class TypeFactory {
	private static TypeFactory INSTANCE = null;

	private final PrimitiveType BOOLEAN_TYPE = new PrimitiveType("Boolean", null);
	private final PrimitiveType INTEGER_TYPE = new PrimitiveType("Integer", null);
	private final PrimitiveType REAL_TYPE = new PrimitiveType("Real", null);
	private final PrimitiveType STRING_TYPE = new PrimitiveType("String", null);
	private Model model;

	public static TypeFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TypeFactory();
		}
		return INSTANCE;
	}

	private TypeFactory() {
	}

	public void setModel(Model model) {
		this.model = model;
	}

	private PrimitiveType getPrimitiveType(String name) {
		name = name.toLowerCase();
		switch (name) {
		case "boolean":
			return BOOLEAN_TYPE;
		case "integer":
			return INTEGER_TYPE;
		case "real":
			return REAL_TYPE;
		case "string":
			return STRING_TYPE;
		}

		return null;
	}

	private CollectionType getCollectionType(String name) {
		if (name.contains("(")) {
			String setName = name.split("\\(")[0];
			switch (setName.toLowerCase()) {
			case "set":
				return new CollectionType(setName, getClassifier(name.split("\\(")[1].split("\\)")[0]), null);
			case "bag":
				return new CollectionType(setName, getClassifier(name.split("\\(")[1].split("\\)")[0]), null);
			case "sequence":
				return new CollectionType(setName, getClassifier(name.split("\\(")[1].split("\\)")[0]), null);
			case "orderedset":
				return new CollectionType(setName, getClassifier(name.split("\\(")[1].split("\\)")[0]), null);
			}
		}

		return null;
	}

	public Classifier getClassifier(String name) {
		if (this.model == null) {
			throw new IllegalStateException("please init TypeFactoy by invoking setModel");
		}
		Classifier c = this.getCollectionType(name);
		if (c != null) {
			return c;
		}
		c = this.getTypeFromModel(name);
		if (c != null) {
			return c;
		}
		c = this.getPrimitiveType(name);
		if (c != null) {
			return c;
		}
		throw new IllegalStateException("unsupported type " + name);
	}

	private Classifier getTypeFromModel(String name) {
		for (UML2Class clazz : this.model.getClasses()) {
			if (clazz.getName().equals(name)) {
				return clazz;
			}
		}
		return null;
	}
}

