package symbol;

import java.util.LinkedList;

import statement.LocalVariableStatement;
import statement.Statement;

public abstract class ScopeSymbol extends BasicSymbol {

	protected LinkedList<Statement> statements;
	protected LinkedList<Symbol> globals;

	public ScopeSymbol(String name, String type, PrimitiveType primitiveType, Visibility visibility, Symbol parent) {
		super(name, type, primitiveType, visibility, parent);

		this.statements = new LinkedList<Statement>();
		this.globals = new LinkedList<Symbol>();
	}

	protected boolean canStoreGlobals() {
		return true;
	}

	public Symbol defineStatement(Statement statement) {
		// JASS only allows local variable definitions at the beggining of
		// the block, but we're on vrJASS and we allow it anywhere _/m/
		if (statement instanceof LocalVariableStatement) {
			// So, local definition at the top
			/*this.statements.addFirst(statement);

			statement = new SetVariableStatement(
				this,
				(VariableExpression) statement.getSymbol(),
				((LocalVariableStatement) statement).getExpression()
			);*/
		}

		this.statements.add(statement);

		return this;
	}

	public LinkedList<Statement> getStatements() {
		return this.statements;
	}

	public LinkedList<Symbol> getGlobals() {
		return this.globals;
	}

	@Override
	public Symbol define(Symbol symbol) {
		if (symbol instanceof GlobalVariableSymbol) {
			if (this.canStoreGlobals()) {
				this.globals.add(symbol);
			}
		}

		return super.define(symbol);
	}

	@Override
	public Symbol resolve(String name, PrimitiveType primitiveType) {
		Symbol resolved = super.resolve(name, primitiveType);

		if (resolved == null) {
			for (Symbol child : this.getChilds().values()) {
				resolved = child.resolve(name, primitiveType);

				if (resolved != null) {
					break;
				}
			}
		}

		return resolved;
	}

}

