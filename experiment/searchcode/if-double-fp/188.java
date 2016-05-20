package ca.compsci.opent.compiler.semantics;

import java.util.List;

import ca.compsci.opent.compiler.Utils;
import ca.compsci.opent.compiler.analysis.AnalysisAdapter;
import ca.compsci.opent.compiler.analysis.DepthFirstAdapter;
import ca.compsci.opent.compiler.node.*;

/* TODO: Implement all semantic analysis here
 *   - check lvalues
 *      - variables are not redeclared in same scope
 *   - check rvalues
 *      - variables visible in scope
 *      - initialisation before use
 *      - mark variables as _used_
 *   - check types
 *      - appropriate type conversions / promotions
 *      - assignments are of the right type
 *      - operators have appropriately typed operands
 */

public class SemanticAnalyzer extends DepthFirstAdapter {
	private SymbolTable scope = null;

	private Type last_type = null;

	private int line, pos;

	private boolean hasObservedError = false, isLValueAssignExpr = false;

	private final String filename;

	public SemanticAnalyzer(String fn) {
		this.filename = fn;
	}
	
	@Override
	public void inAProgram(AProgram node) {
		scope = SymbolTable.newScope();
	}

	@Override
	public void caseABlockStmt(ABlockStmt node) {
		SymbolTable current_scope = scope;
		scope = current_scope.newSubScope();
		super.caseABlockStmt(node);
		scope = current_scope;
	}

	@Override
	public void outAInferDeclStmt(AInferDeclStmt node) {
		TIdentifier first = node.getIdentifier();
		List<TIdentifier> rest = node.getRest();

		// Uninitialised
		createVariable(first, false);
		createVariables(rest, false);
	}
	
	@Override
	public void outAInferInitStmt(AInferInitStmt node) {
		TIdentifier first = node.getIdentifier();
		List<TIdentifier> rest = node.getRest();

		// set initialised flag
		createVariable(first, true);
		createVariables(rest, true);

		// if expr, then check if type matches (other checks are done in outABinOprExpr();)
		Type type = last_type;
		checkOrSetVariableType(first, type);
		checkOrSetVariablesType(rest, type);
	}
	
	@Override
	public void outATypedDeclStmt(ATypedDeclStmt node) {
		TIdentifier first = node.getIdentifier();
		List<TIdentifier> rest = node.getRest();

		TIdentifier type_ident = node.getType();
		Type type = toType(type_ident);

		// Uninitialised
		createVariable(first, false);
		createVariables(rest, false);

		checkOrSetVariableType(first, type);
		checkOrSetVariablesType(rest, type);
	}
	
	@Override
	public void caseATypedInitStmt(ATypedInitStmt node) {
		TIdentifier first = node.getIdentifier();
		List<TIdentifier> rest = node.getRest();

		// set initialised flag
		createVariable(first, true);
		createVariables(rest, true);

		TIdentifier type_ident = node.getType();
		Type type = toType(type_ident);
		checkOrSetVariableType(first, type);
		checkOrSetVariablesType(rest, type);

		PExpr expr_node = node.getExpr();
		if (expr_node != null) { 
			convertLiterals(expr_node, type); // attempt safe literal conversion
			expr_node.apply(this); // evaluate expression
		}

		// if expr, then check if type matches (other checks are done in
		// outABinOprExpr();)
		Type expr_type = last_type;
		if (!type.equals(expr_type))
			reportError("expected " + type + " but found " + expr_type, line, pos);
	}

	@Override
	public void caseAExprStmt(AExprStmt node) {
		// flags a chain of lone assign_expr as lvalue (making it eligible for dead-code removal)
		isLValueAssignExpr = true;
		super.caseAExprStmt(node);
		isLValueAssignExpr = false;
	}
	
	@Override
	public void caseAIfStmt(AIfStmt node) {
		PExpr expr = node.getExpr();
		
		if (expr != null)
			expr.apply(this);
		
		if (!last_type.equals(Type.Boolean))
			reportError("expected " + Type.Boolean + " but found " + last_type, line, pos);
		
		PStmt if_body = node.getIfBody();
		PStmt else_body = node.getElseBody();
		
		if (if_body != null)
			if_body.apply(this);
		
		for (PStmt stmt : node.getElsifs())
			stmt.apply(this);
		
		if (else_body != null)
			else_body.apply(this);
	}

	@Override
	public void outAExitWhenStmt(AExitWhenStmt node) {
		Type type = last_type;
		if (!type.equals(Type.Boolean))
			reportError("expected " + Type.Boolean + " but found " + type, line, pos);
	}

	@Override
	public void caseABinOprExpr(ABinOprExpr node) {
		Type left_type = null, right_type = null;

		if (node.getLeft() != null)
			node.getLeft().apply(this);
		left_type = last_type;

		int left_line = line, left_pos = pos;

		if (node.getRight() != null)
			node.getRight().apply(this);
		right_type = last_type;

		BinOprTypeValidator validator = new BinOprTypeValidator(left_type, right_type);

		if (node.getOpr() != null)
			node.getOpr().apply(validator);

		try {
			validator.validate();
		} catch (TypeInequalityException tie) {
			reportError("expected both operands of similar type", line, pos);
		} catch (LeftTypeMismatchException ltme) {
			reportError("expected " + ltme.getExpectedTypesMsg() + " but found " + left_type, left_line, left_pos);
		} catch (RightTypeMismatchException rtme) {
			reportError("expected " + rtme.getExpectedTypesMsg() + " but found " + right_type, line, pos);
		}

		scope.add(mkABinOprExprKey(node), 
				new Expression(last_type = validator.getResultantType(), validator.getPromotedType()));
	}

	@Override
	public void outAUnrOprExpr(AUnrOprExpr node) {
		node.getOpr().apply(new AnalysisAdapter() {
			@Override
			public void caseAIdentityOpr(AIdentityOpr node) {
				if (!isNumerical(last_type))
					reportError("expected a numerical type but found " + last_type, line, pos);
			}

			@Override
			public void caseANegateOpr(ANegateOpr node) {
				if (!isNumerical(last_type))
					reportError("expected a numerical type but found " + last_type, line, pos);
			}

			@Override
			public void caseANotOpr(ANotOpr node) {
				if (!last_type.equals(Type.Boolean))
					reportError("expected type " + Type.Boolean + " but found " + last_type, line, pos);
			}
			
			private boolean isNumerical(Type t) {
				switch (t) {
				case String:
				case Reference:
				case DEFERRED:
					return false;
				default:
					return true;
				}
			}
		});
	}

	@Override
	public void caseAAssignExpr(AAssignExpr node) {
		super.outAAssignExpr(node);
		
		TIdentifier ident = node.getIdentifier();

		// check if variable is visible
		checkVisibility(ident);

		Variable var = (Variable) scope.getInfo(mkTIdentifierKey(ident));
		// when initialising typed-declared variables
		// try and convert literals in the expression to the variables's type
		if (!var.isInitialized())
			convertLiterals(node.getExpr(), var.getType());
		
		// evaluate expression (before initialising this variable)
		node.getExpr().apply(this);
		
		// if this is an RValue statement, update meta-info
		if (!isLValueAssignExpr) var.setUsedAsRValue(true);
		
		// initialise the variable
		var.setInitialized(true);

		// check for type-mismatch
		checkOrSetVariableType(ident, last_type);
	}

	// R-Value identifiers
	@Override
	public void outAIdentifierExpr(AIdentifierExpr node) {
		TIdentifier ident = node.getIdentifier();

		line = ident.getLine();
		pos = ident.getPos();

		checkVisibility(ident);
		checkInitialized(ident);

		TypedSymbol var = (TypedSymbol) scope.getInfo(mkTIdentifierKey(ident));
		
		if (var instanceof Variable)
			((Variable) var).setUsedAsRValue(true);
		
		last_type = var.getType();
	}

	@Override
	public void outAStringExpr(AStringExpr node) {
		TStringLit str_lit = node.getStringLit();

		line = str_lit.getLine();
		pos = str_lit.getPos();

		String str = str_lit.getText().trim();
		// remove the enclosing double quotes
		String value = str.substring(1, str.length() - 1);
		// parse the escape sequences
		value = value.replaceAll("\\\\\"", "\"")
		             .replaceAll("\\\\n", "\n")
				     .replaceAll("\\\\t", "\t")
				     .replaceAll("\\\\r", "\r")
				     .replaceAll("\\\\f", "\f")
				     .replaceAll("\\\\\\\\", "\\");
		scope.add(mkAStringExprKey(node), new Literal(Type.String, value));

		last_type = Type.String;
	}

	@Override
	public void outANumberExpr(ANumberExpr node) {
		String key = mkANumberExprKey(node);
		if (!scope.isVisible(key))
			parseANumberExpr(node);
		last_type = ((Literal) scope.getInfo(key)).getType();
	}

	@Override
	public void outABooleanExpr(ABooleanExpr node) {
		TBooleanLit bool = node.getBooleanLit();
		String value = bool.getText().trim();

		line = bool.getLine();
		pos = bool.getPos();

		if (value.equals(Literal.TRUE))
			scope.add(mkABooleanExprKey(node), new Literal(Type.Boolean, Boolean.TRUE));
		if (value.equals(Literal.FALSE))
			scope.add(mkABooleanExprKey(node), new Literal(Type.Boolean, Boolean.FALSE));

		last_type = Type.Boolean;
	}

	public SymbolTable getTable() throws SemanticException {
		if (hasObservedError)
			throw new SemanticException();
		return scope;
	}

	public static String mkTIdentifierKey(TIdentifier node) {
		return node.getText().trim();
	}

	public static String mkABooleanExprKey(ABooleanExpr node) {
		return node.getBooleanLit().getText().trim();
	}

	public static String mkANumberExprKey(ANumberExpr node) {
		return node.getNumericLit().getText().trim() + ":" + node.hashCode();
	}

	public static String mkAStringExprKey(AStringExpr node) {
		return node.getStringLit().getText().trim();
	}

	public static String mkABinOprExprKey(ABinOprExpr node) {
		return "~Expression:" + node.hashCode();
	}

	// Private Helper methods...

	private void checkVisibility(TIdentifier ident) {
		String ident_name = ident.getText().trim();
		//XXX: currently the error message assumes the `ident` is a Variable ONLY.
		if (!scope.isVisible(mkTIdentifierKey(ident))) {
			reportError("variable " + ident_name + " may not have been declared (or is not visible in scope)",
					ident.getLine(), ident.getPos());
			scope.add(mkTIdentifierKey(ident), new Variable(Type.ERRONEOUS, true));
		}
	}

	private void createVariable(TIdentifier ident, boolean initialized) {
		String ident_name = ident.getText().trim();
		if (scope.isVisible(mkTIdentifierKey(ident)))
			reportError("variable " + ident_name + " cannot be redeclared in scope", ident.getLine(), ident.getPos());
		scope.add(mkTIdentifierKey(ident), new Variable(Type.DEFERRED, initialized));
	}

	private void createVariables(List<TIdentifier> idents, boolean initialized) {
		for (TIdentifier ident : idents)
			createVariable(ident, initialized);
	}

	private void checkInitialized(TIdentifier ident) {
		String var_name = ident.getText().trim();
		Symbol sym = scope.getInfo(mkTIdentifierKey(ident));
		if (sym instanceof Variable) {
			Variable var = (Variable) sym;
			if (!var.isInitialized())
				reportError("variable " + var_name + " may not have a value", ident.getLine(), ident.getPos());
		}
	}

	private void checkOrSetVariableType(TIdentifier var_ident, Type type) {
		Variable var = (Variable) scope.getInfo(mkTIdentifierKey(var_ident));
		Type var_type = var.getType();
		if (var_type.equals(Type.DEFERRED))
			var.setType(type);
		else if (!var_type.equals(type))
			reportError("expected " + var_type + " but found " + type, var_ident.getLine(), var_ident.getPos());
	}

	private void checkOrSetVariablesType(List<TIdentifier> idents, Type type) {
		for (TIdentifier ident : idents)
			checkOrSetVariableType(ident, type);
	}

	private Type toType(TIdentifier type) {
		String value = type.getText().trim();
		for (Type t : Type.values())
			if (t.pattern() != null && value.matches(t.pattern()))
				return t;
		
		//TODO: change this when custom data types are supported
		reportError("custom data types are currently not supported!", type.getLine(), type.getPos());
		return Type.ERRONEOUS;
	}

	private void convertLiterals(PExpr node, Type t) {
		if (t.equals(Type.DEFERRED) || t.equals(Type.ERRONEOUS))
			return;

		if (node instanceof ABinOprExpr) {
			ABinOprExpr binexpr = (ABinOprExpr) node;
			try {
				// Check if `t` is a permitted type
				BinOprTypeValidator validator = new BinOprTypeValidator(t, t);
				binexpr.getOpr().apply(validator);
				validator.validate(); // throws an exception on error

				// Do conversions.
				convertLiterals(binexpr.getLeft(), t);
				convertLiterals(binexpr.getRight(), t);
			} catch (Exception e) {
				// skip conversion
			}
		} else if (node instanceof ANumberExpr) {
			ANumberExpr num = (ANumberExpr) node;
			parseANumberExpr(num);

			TNumericLit num_lit = num.getNumericLit();

			Literal lit = (Literal) scope.getInfo(mkANumberExprKey(num));

			if (loosesPrecision(lit.getType(), (Number) lit.getValue(), t))
				reportError("loss in precision in converting " + lit.getType() + " literal to type " + t, 
						num_lit.getLine(), num_lit.getPos());
			
			lit.setType(t);
		}
	}

	private void parseANumberExpr(ANumberExpr node) {
		TNumericLit num_lit = node.getNumericLit();
		String value = num_lit.getText().trim();

		line = num_lit.getLine();
		pos = num_lit.getPos();

		// Default type for integrals is Integer32 and for reals is Real32
		// When compiling we store these values in the compiler (not the
		// program) as Longs/Doubles so as to be safe (i.e, we don't loose any information).
		if (value.matches(".*#.*")) {
			String[] result = value.split("#");
			int base = Integer.valueOf(result[0]);
			try {
				Long long_value = (base < 0 ? -1L : 1L) * Long.valueOf(fixOctalAndHex(result[1]), Math.abs(base));
				scope.add(mkANumberExprKey(node), new Literal(last_type = getSemanticType(long_value), long_value));
			} catch (NumberFormatException e) {
				if (Math.abs(base) < 2 || Math.abs(base) > 36)
					reportError("integer literal's base should be the range of 2 to 36 inclusive", line, pos);
				else
					reportError("invalid base " + (base < 0 ? -1 * base : base) + " integer literal", line, pos);
				scope.add(mkANumberExprKey(node), new Literal(last_type = Type.ERRONEOUS, Double.NaN));
			}
		} else if (value.matches(".*(\\.|[eE]).*")) {
			Double double_value = Double.valueOf(fixOctalAndHex(value));
			scope.add(mkANumberExprKey(node), new Literal(last_type = getSemanticType(double_value), double_value));
		} else {
			Long long_value = Long.valueOf(fixOctalAndHex(value));
			scope.add(mkANumberExprKey(node), new Literal(last_type = getSemanticType(long_value), long_value));
		}
	}
	
	// Account for Java's octal and hex literal collision.
	private String fixOctalAndHex(String s) {
		if (s.matches("^0+$"))
			return "0";
		else if (s.matches("^0+\\.0*$"))
			return "0.0";
		else if (s.matches("^0+\\.?0*[eE][+-]?\\d+$"))
			return "0.0";
		else
			return s.replaceFirst("^0*", "");
	}

	private Type getSemanticType(Long long_value) {
		if (long_value >= Integer.MIN_VALUE && long_value <= Integer.MAX_VALUE)
			return Type.Integer32;
		return Type.Integer64;
	}

	private Type getSemanticType(Double double_value) {
		if (Math.abs(double_value) >= Float.MIN_VALUE && double_value <= Float.MAX_VALUE)
			return Type.Real32;
		return Type.Real64;
	}

	private boolean loosesPrecision(Type of_type, Number n, Type to_type) {
		switch (of_type) {
		case Integer8:
		case Integer16:
		case Integer32:
		case Integer64:
			return loosesPrecision((Long) n, to_type);
		default:
			return loosesPrecision((Double) n, to_type);
		}
	}

	private boolean loosesPrecision(Long l, Type to_type) {
		switch (to_type) {
		case Integer8:
			if (l < Byte.MIN_VALUE || l > Byte.MAX_VALUE)
				return true;
			break;
		case Integer16:
			if (l < Short.MIN_VALUE || l > Short.MAX_VALUE)
				return true;
			break;
		case Integer32:
			if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
				return true;
			break;
		case Real32:
			// TODO: check if integer values can be precisely represented by single-precision FP
			double d = l.doubleValue();
			if (Math.abs(d) < Float.MIN_VALUE || d > Float.MAX_VALUE)
				return true;
			break;
		}
		return false;
	}

	private boolean loosesPrecision(Double d, Type to_type) {
		long rounded_d = Math.round(d);
		switch (to_type) {
		case Integer8:
			if (Double.isNaN(d) || rounded_d < Byte.MIN_VALUE || rounded_d > Byte.MAX_VALUE)
				return true;
			break;
		case Integer16:
			if (Double.isNaN(d) || rounded_d < Short.MIN_VALUE || rounded_d > Short.MAX_VALUE)
				return true;
			break;
		case Integer32:
			if (Double.isNaN(d) || rounded_d < Integer.MIN_VALUE || rounded_d > Integer.MAX_VALUE)
				return true;
			break;
		case Real32:
			if (Double.isNaN(d) || Math.abs(d) < Float.MIN_VALUE || d > Float.MAX_VALUE)
				return true;
			break;
		}
		return false;
	}
	
	private void reportError(String msg, int line, int col) {
		hasObservedError = true;
		Utils.reportError(Utils.ErrorType.SemanticError, msg, filename, line, col);
	}
}

