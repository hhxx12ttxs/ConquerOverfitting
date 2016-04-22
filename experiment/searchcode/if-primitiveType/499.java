package symbol;

public class ModuleSymbol extends ClassSymbol {

	public ModuleSymbol(String name, Visibility visibility, Symbol parent) {
		super(name, visibility, parent);
		this.primitiveType = PrimitiveType.MODULE;
	}
	
	@Override
	public Symbol define(Symbol symbol) {
		if (symbol instanceof ExtendableSymbol) { 
			return this;
		}
		return super.define(symbol);
	}

}

