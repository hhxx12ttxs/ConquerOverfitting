package symbol;

public class MethodSymbol extends FunctionSymbol {

	protected boolean _static;
	
	public MethodSymbol(String name, boolean _static, Visibility visibility, Symbol parent) {
		super(name, visibility, parent);
		
		this.primitiveType = PrimitiveType.METHOD;
		this._static = _static;
		
		if (!this.isStatic()) {
			// not define(...) because it auto-defines itself in the constructor
			new ArgumentSymbol("this", this.getParent().getType(), this);
		}
	}
	
	public boolean isStatic() {
		return this._static;
	}
	
}

