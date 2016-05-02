package ro.pub.pt.yanl.parser.v1;

import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

public class If extends Statement {

	List<Statement> thenBranch = new LinkedList<Statement>();
	List<Statement> elseBranch = new LinkedList<Statement>();
	/** Boolean condition for then */
	Expression condition;
	// TODO Typecheck if boolean
	
	public If(CommonTree t) {
		super(t);
	}

	public List<Statement> getThenBranch() {
		return thenBranch;
	}

	public List<Statement> getElseBranch() {
		return elseBranch;
	}

	public void setCondition(Expression condition) {
		this.condition = condition;
	}

	@Override
	public void resolve(Scope scope) throws YANLException {
		super.resolve(scope);
		
		// Check if the condition is boolean
		condition = condition.resolveExpression(scope);
		Type.BOOLEAN.checkCanAssign(condition.type, condition.node);
		
		// Check branches
		Statement.resolveAll(scope, thenBranch);
		Statement.resolveAll(scope, elseBranch);
	}

}

