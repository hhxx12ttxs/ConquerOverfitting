/*
 * Real.java
 * 
 * last update: 24.01.2010 by Olaru Victor
 * 
 * author:	Victor(victor.olaru@gmail.com)
 * 
 * Obs:
 */

package engine;

//import java.util.*;
//import java.math.*;

/**
 *  Real type (just like in IEEE 754 )
 */

class Real extends DataHolder{

	double data;

	public Real(){
		data = Double.NaN;
	}

	public Object clone(){
		return new Real(this);
	}

	public Real(Real r){
		data = r.data;
	}

	public Real(String s){
		xConsole.debug("new real");
		try{
			synchronized(s){
				data = Double.valueOf(s).doubleValue();
			}
		}catch(NumberFormatException ex){
			throw new Error("Real.<init> : NumberFormatException allowed by grammar !?");
		}
	}

	public Real(long l){
		data = l;
	}

	public  Real(double d){
		data = d;
	}

	/**
	 *  Does nothing, because a Real cannot be continuously assigned.
	 *
	 *  @exception Error
	 *      because a Real cannot be assigned, so 
	 *      the parser will catch this and terminate.
	 */
	public Object[] compute(){
		throw new Error("Real.compute : Real continuously assigned !");
	}

	/**
	 *
	 */
	public void attrib(Real r)
	throws InterpretTimeException
	{
		data = r.data;
		notifyMonitors();
	}

	//the Result implementation ->

	/**
	 *  This is simillar to the
	 *  the $rtoi system function.
	 *  It may be nonstandard (i.e. I guess the
	 *  conversion isn't made to the nearest integer value)
	 */
	public BitVector getInt(){

		if(Double.isNaN(data))return BitVector.bX();
		if(Math.abs(data) < 1)return BitVector.b0();

		long l = Double.doubleToLongBits(data);
		//l = ((1024l + 128l) << 52l) |  0x10000000000000l;

		int exp = ((int) ((l >> 52l) & 0x7ffl)) - 1023;
		long mantissa = (l & 0xfffffffffffffl) | 0x10000000000000l; //select mantissa and add the hidden bit
		BitVector b = new BitVector(mantissa, true); 
		xConsole.debug(" l= " + Long.toHexString(l) + " (bin): " + Long.toBinaryString(l) + "data = " + data + " mantissa: "
				+ Long.toHexString(mantissa) + " exp = " + exp + " (hex): " + Long.toHexString(exp) + "sign " +  (l >> 63));
		b.shl(exp-52);
		if((l >> 63l) != 0)b.neg();
		xConsole.debug("b is: " + b.toString(10));
		return b;
	}

	/**
	 *  Also known as the $realtobits system function.
	 */
	public BitVector getBits(){
		return new BitVector(Double.doubleToLongBits(data), false);
	}

	public long getLong(){
		return Math.round(data);
	}

	/**
	 *  Returns a copy of this.
	 */
	public Real getReal(){
		return new Real(this);
	}

	/**
	 *  Returns 1 if this Real is not 0, and 0 otherwise.
	 *
	 */
	public int getBool(){
		if(data !=0)return 1;
		return 0;
	}

	/**
	 * Returns the truth value of tis Real;
	 */
	public boolean isTrue(){
		return getBool() == 1;
	}

	public Result duplicate(){
		return new Real(this);
	}

	// <- the Result implementation

	public String toString(){
		return Double.toString(data);
	}

	public String toString(int base){
		xConsole.debug("Real.toString(base) not implemented yet");
		return toString();
	}

	/**
	 * Returns a brief representation of this real.
	 */
	public String toDecimalString(){
		//TODO: implementation 
		return toString();
	}

	public void  shl(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("shl is illegal on real types (use $realtobits)");
	}

	public void shr(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("shr is illegal on real types (use $realtobits)");
	}

	public void neg(){
		data = -data;
	}
	public void bAndR()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise reduction is illegal on real types (use $realtobits)");
	}
	public void bOrR()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise reduction is illegal on real types (use $realtobits)");
	}
	public void bXOrR()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise reduction is illegal on real types (use $realtobits)");
	}
	public void bNAndR()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise reduction is illegal on real types (use $realtobits)");
	}
	public void bNOrR()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise reduction is illegal on real types (use $realtobits)");
	}
	public void bNXOrR()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise reduction is illegal on real types (use $realtobits)");
	}

	public void bNot()throws InterpretTimeException{
		throw new InterpretTimeException("bitwise negation is illegal on real types (use $realtobits)");
	}

	public synchronized void lNot(){
		if(data != 0)data = 0;
		else data = 1;
	}

	public boolean isDefined(){
		return true;
	}

	public Result lEq(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			if(data == b.getReal().data)return BitVector.b1();
			return BitVector.b0();
		}catch(ClassCastException ex){ //r must be Real
			if(((Real)r).data == data)return BitVector.b1();
			return BitVector.b0();
		}
	}

	public Result lNEq(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			if(data == b.getReal().data)return BitVector.b0();
			return BitVector.b1();
		}catch(ClassCastException ex0){ //r is Real
			if(((Real)r).data == data)return BitVector.b0();
			return BitVector.b1();
		}
	}

	public void bOr(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public void bNOr(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public void bAnd(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public void bNAnd(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public void bXor(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public void bNXor(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public Result cEq(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public Result cNEq(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public Result lt(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			if(data < b.getReal().data)return BitVector.b1();
			return BitVector.b0();
		}catch(ClassCastException ex){
			if(data < ((Real)r).data)return BitVector.b1();
			return BitVector.b0();
		}
	}

	public Result ge(Result r){
		Result res =  lt(r);
		try{
			res.bNot();
		}catch(InterpretTimeException ex){
			xConsole.dumpStack(ex);
			throw new Error("real.ge : " + ex);
		}
		return res;
	}

	public Result gt(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			if(data > b.getReal().data)return BitVector.b1();
			return BitVector.b0();
		}catch(ClassCastException ex){
			if(data > ((Real)r).data)return BitVector.b1();
			return BitVector.b0();
		}
	}

	public Result le(Result r){
		Result res = gt(r);
		try{
			res.bNot();
		}catch(InterpretTimeException ex){
			xConsole.dumpStack(ex);
			throw new Error("real.le : " + ex);
		}
		return res;
	}

	public Result add(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			data += b.getReal().data;
		}catch(ClassCastException ex){
			data += ((Real)r).data;
		}
		return this;
	}

	public Result sub(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			data -= b.getReal().data;
		}catch(ClassCastException ex){
			data -= ((Real)r).data;
		}
		return this;
	}

	public Result mul(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			data *= b.getReal().data;
		}catch(ClassCastException ex){
			data *= ((Real)r).data;
		}
		return this;
	}
	public Result mod(Result r)throws InterpretTimeException{
		throw new InterpretTimeException("bitwise operator not allowed on real");
	}

	public Result div(Result r){
		try{
			BitVector b = (BitVector)r;
			if(!b.isDefined())return BitVector.bX();
			data /= b.getReal().data;
		}catch(ClassCastException ex){
			data /= ((Real)r).data;
		}
		return this;
	}

	public int getType(){
		return Symbol.realType;
	}

	public int length() {
		return 64;
	}
}











