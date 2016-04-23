package util;

import symbol.PrimitiveType;
import symbol.Symbol;
import symbol.UndefinedFunctionSymbol;
import symbol.UndefinedVariableSymbol;

public class UndefinedSymbolResolver {

	protected Symbol undefinedSymbol(String name, PrimitiveType primitiveType) {
		Symbol undefined = null;

		if (primitiveType == PrimitiveType.VARIABLE) {
			undefined = new UndefinedVariableSymbol(name, false);
		} else if (primitiveType == PrimitiveType.FUNCTION) {
			undefined = new UndefinedFunctionSymbol(name);
		}

		return undefined;
	}

	public Symbol resolveOrUndefined(Symbol from, String name, PrimitiveType primitiveType, boolean resolveBackwards) {
		Symbol resolved = null;

		if (resolveBackwards) {
			resolved = from.resolveBackwards(name, primitiveType);
		} else {
			resolved = from.resolve(name, primitiveType);
		}

		if (resolved == null) {
			resolved = this.undefinedSymbol(name, primitiveType);
		}

		return resolved;
	}

	public Symbol resolve(Symbol from, String name, PrimitiveType primitiveType) {
		return this.resolveOrUndefined(from, name, primitiveType, false);
	}

	public Symbol resolveBackwards(Symbol from, String name, PrimitiveType primitiveType) {
		return this.resolveOrUndefined(from, name, primitiveType, true);
	}

}

