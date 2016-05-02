package simPL;

public class If implements Expression
{

	public Expression condition, thenPart, elsePart;

	public If(Expression c, Expression t, Expression e)
	{
		condition = c;
		thenPart = t;
		elsePart = e;
	}

	// Eliminate let in the condition, then part and else part
	public Expression eliminateLet()
	{
		return new If(condition.eliminateLet(), thenPart.eliminateLet(), elsePart.eliminateLet());
	}

	// to be implemented by student

	public Type check(TypeEnvironment G) throws TypeError
	{
		Type result1 = condition.check(G);
		Type result2 = thenPart.check(G);
		Type result3 = elsePart.check(G);
		
		if (result1 instanceof BoolType && EqualType.equalType(result2, result3))
		{
			return result2;
		}
		else
		{
			throw new TypeError("ill-typed if expression " + this);
		}
	}

	// //////////////////////
	// Support Functions
	// //////////////////////

	public String toString()
	{
		return " if " + condition + " then " + thenPart + " else " + elsePart + " end";
	}
}

