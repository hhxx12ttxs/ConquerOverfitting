package prooftool.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import prooftool.tool.Main;
import prooftool.util.ExpressionUtils;
import prooftool.util.Path;
import prooftool.util.Symbol;

/**
 * A literal is a constant expression with a value. 
 * 
 * @author robert
 *
 */

public class Literal extends Expression
{
	public Object value;
	public static final Literal inf = new Literal("?");
	public static final Literal top = new Literal("?");
	public static final Literal bottom = new Literal("?");
	public static final Literal ok = new Literal("ok");
	public static final Literal nil = new Literal("nil");
	
	
	
	public Literal(Object value)
	{		
		this.value = value;
		this.type = this.determineType(value);
	}
	
	private Literal(Object value, boolean determineType)
	{	
		this.value = value;
		if (determineType) {
			this.type = this.determineType(value);
		}
	}
	
	private Expression determineType(Object value) {
		String typeString = "";	
		if (value instanceof Double) {			
			if (((Double) value).doubleValue() == ((Double) value).intValue() 
					&& ((Double) value).intValue() >= 0) {
				typeString = "nat";
				this.value = new Integer(((Double) value).intValue());				
			} else {
				typeString = "rat";
			}
		} else if (value instanceof Integer) {
			typeString = "nat";
			assert(((Integer)value).intValue() >= 0);
		} else if (value instanceof String) {	
			String uniValue = Symbol.lookupUnicode((String)value); 
			if ("?".equals(uniValue)||"?".equals(uniValue)) {
				typeString = "bool";
			} else if ("?".equals(uniValue)) {
				typeString = "inf";
			} else if ("ok".equals(uniValue)) {
				typeString = "bool";
			} else if ("nil".equals(uniValue)) {
				typeString = "nil";
				return new Literal("nil",false);
			} else {
				typeString = "text";
			}
		}
		return ExpressionUtils.parse(typeString);
	}

	@Override
	/**
	 * Turn every identifier in this expression into its corresponding variable.
	 * Since literals are can never be variables, the literal is returned 
	 * unchanged. 
	 * 
	 * @return This expression, with all its identifiers turned into variables
	 */
	protected Expression make_variables(Dictionary d)
	{
		return this;
	}
	protected Expression sync_variables(Dictionary d) {
		return this;
	}

//	public Expression unmake_variables() {
//		return this;
//	}
	
	@Override
	/**
	 * Creates a list of expression parts returned are at one zoom level down.
	 * In this case its just the string representation of this literal's value. 
	 * 
	 * @return A list of String and Object, where the object parts are zoomable
	 *         parts and the String parts are operators/keywords.
	 */
	public Object[] toParts()
	{
		return new Object[] {value.toString()};
	}

	@Override
	/**
	 * Returns whether the given variable x is in this expression. A literal
	 * can never contain a variable so false is always returned.
	 * 
	 * @return Whether x is in this expression
	 */
	public boolean contains(Variable x)
	{
		return false;
	}
		
	public Expression instantiate(Map<Variable, Expression> subst) {		
		return this;
	}
	
	public Expression instantiate(Substitution s){		
		return this;
	}
	
	public Map<Variable,Expression> getAllVars(Map<Variable,Expression> currentVars){
		return currentVars;
	}
	
	public Map<Variable,Variable> getAllVarsHelper(){		
		return new IdentityHashMap<Variable,Variable>();
	}
	
	/**
	 * Should never be called, since literals have no children
	 */
	public void setChild(int i, Expression replacement) {
		throw new UnsupportedOperationException();		
	}
	
	@Override
	public Expression getChild(int i) {	
		return null;
	}
	
	@Override
	public List<Expression> getChildren() {	
		return new ArrayList<Expression>();
	}

	@Override
	public void flatten() {		
		//nothing to do
	}

	@Override
	public boolean contains(Identifier x) {
		return false;
	}

//	@Override
//	public Expression structClone() {
//		return new Literal(value);
//	}
	
	protected Expression cloneHelper(Map<Variable,Variable> m) {
		return new Literal(value);
	}

	
	@Override
	protected void makeTypesHelper(List<Law> context) {
		assert this.type != null; // literals would be given a type in the constructor
	}

	@Override
	public void removeTopCorners() {
		//nothing to do		
	}

	@Override
	public String getTreeRep() {
		return "(literal " + Symbol.lookupAscii(value.toString()) + ")";
	}

	@Override
	public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}

