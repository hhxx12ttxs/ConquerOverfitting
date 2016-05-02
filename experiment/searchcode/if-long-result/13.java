/*
 * Result.java
 * 
 * last update: 16.01.2010 by Stefan Saru
 * 
 * author:	Alec(panovici@elcom.pub.ro)
 * 
 * Obs:
 */

package engine;

/**
 *  Performs some useful conversions between DataHolders
 *    of various types. Is implemented by all DataHolders
 *  this is used to allow conversion between all types of
 *  data obtained as results of expression evaluation
 *  *NOTE* none of the operations that return a Result does not gurantees
 *  the integrity of the operands ( and often the returned Result is one of them).
 *  So, *do not* use them after one of these ! Use getxxx to make a copy when necessary.
 */
public interface Result {
	/**
	 *  @return a new Real object based on this DataHolder
	 */
	public Real getReal();

	/**
	 *  @return a new BitVector object containing the binary
	 *  representation of this DataHolder
	 */
	public BitVector getBits();

	/**
	 *  @return a new BitVector object representing an int
	 *  based on this DataHolder
	 */
	public BitVector getInt();

	/**
	 *  @return an int value representing some boolean (0, 1, X or Z)
	 *  based on the actual value of this Result
	 */
	public int getBool();

	/**
	 * Returns the long value closest to this Result
	 */
	public long getLong();

	/**
	 *  works pretty much like clone
	 */
	public Result duplicate();

	public void shl(Result r)throws InterpretTimeException;
	public void shr(Result r)throws InterpretTimeException;

	public void neg();

	public void bAndR()throws InterpretTimeException;
	public void bOrR()throws InterpretTimeException;
	public void bXOrR()throws InterpretTimeException;
	public void bNAndR()throws InterpretTimeException;
	public void bNOrR()throws InterpretTimeException;
	public void bNXOrR()throws InterpretTimeException;
	public void bNot()throws InterpretTimeException;
	public void lNot();

	public Result lEq(Result r);
	public Result lNEq(Result r);

	public void bOr(Result r)throws InterpretTimeException;
	public void bNOr(Result r)throws InterpretTimeException;
	public void bAnd(Result r)throws InterpretTimeException;
	public void bNAnd(Result r)throws InterpretTimeException;
	public void bXor(Result r)throws InterpretTimeException;
	public void bNXor(Result r)throws InterpretTimeException;

	/**
	 * case equality
	 */
	public Result cEq(Result r)throws InterpretTimeException;

	/**
	 * case nonequality
	 */
	public Result cNEq(Result r)throws InterpretTimeException;

	/**
	 * lower than
	 */
	public Result lt(Result r);

	/**
	 * greather than
	 */
	public Result gt(Result r);

	/**
	 * lower or equal than
	 */
	public Result le(Result r);

	/**
	 * greater than
	 */
	public Result ge(Result r);

	public Result add(Result r);
	public Result sub(Result r);
	public Result mul(Result r);
	public Result div(Result r);
	public Result mod(Result r)throws InterpretTimeException;

	/**
	 * Checks whether this reult is defined (unambiguous (x, z)).
	 * @return true if this result is unambiguous( no X's or Z's)
	 */
	public boolean isDefined();

	/**
	 * Checks whether this Result is true.
	 * @return true if this result is true ( i.e. has a nonnull and
	 * well defined value (no X's or Z's))
	 */
	public boolean isTrue();

	/**
	 * Returns the String representation in the specified base.
	 */
	public String toString(int base);


	/**
	 * Returns the type of this result.
	 * @see Symbol
	 */
	public int getType();

	/**
	 * @return the length (in bits) of the Result.
	 */
	public int length();
}












