package IC.AST;

import IC.DataTypes;
import IC.Semantic.SymbolTables.SymbolTable;

/**
 * Primitive data type AST node.
 * 
 * @author Tovi Almozlino
 */
public class PrimitiveType extends Type {

	private DataTypes type;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public Object accept(EnviromentVisitor visitor, SymbolTable sym) {
		return visitor.visit(this,sym);
	}

	/**
	 * Constructs a new primitive data type node.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 * @param type
	 *            Specific primitive data type.
	 */
	public PrimitiveType(int line, DataTypes type) {
		//super(line);
		//this.type = type;
		this(line,0,type);
	}

	private PrimitiveType(int line, int dim, DataTypes type) {
		super(line, dim);
		this.type = type;
	}
	
	public String getName() {
		return type.getDescription();
	}
	
	@Override
	public Type getLowerDimType() {
		return new PrimitiveType(this.getLine(), this.getDimension() - 1, type);
	}
	
	@Override
	public Type getHigherDimType() {
		return new PrimitiveType(this.getLine(), this.getDimension() + 1, type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrimitiveType other = (PrimitiveType) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
