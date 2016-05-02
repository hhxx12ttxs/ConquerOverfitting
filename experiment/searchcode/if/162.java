/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David, Max Panasenkov.
 * See full copyright license terms in file ../GPL.txt
 */
package sfdl.program;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.cace.fairplay2viff.ast.ASTVisitor;
import org.cace.fairplay2viff.ast.DepthFirstVisitor;
import org.cace.fairplay2viff.util.Joinable;

import sfdl.CompilerError;
import sfdl.bits.Bit;
import sfdl.bits.BitsManager;
import sfdl.bits.Variable;
import sfdl.types.TypesFactory;

/***
 * This class represents if statement.
 * 
 * If statement evaluation uses algorithm described in the paper to 
 * 	generate single assignment for all the variables changes inside
 * 	then or else blocks of if.
 * 
 * @author Max Panasenkov
 * @version 2.1
 */
public class If implements Statement
{
	/** If condition */
	public Expression _condition;
	
	/** Then statement */
	public Block _then;
	
	/** Else statement */
	public Block _else;
	
	/**
	 * Ctor.
	 * @param condition if condition
	 * @param then then statement
	 * @param else_ else statement
	 */
	public If(Expression condition, Statement then, Statement else_)
	{
		_condition = condition;
		_then = StatementsFactory.createBlockWith(then);
		setElseStatement(else_);
	}
	
	/**
	 * Ctor. else is set to empty block.
	 * @param condition if condition
	 * @param then then statement
	 */
	public If(Expression condition, Statement then)
	{
		this(condition, then, null);
	}

    /**
	 * Sets else statement for this if.
	 * @param else_ else statement
	 */
	public void setElseStatement(Statement else_)
	{
		if(null == else_)
		{
			_else = StatementsFactory.createBlock();
		}
		else
		{
			_else = StatementsFactory.createBlockWith(else_);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String res = String.format("if(%1$s)\n%2$s", _condition, _then);
		if(_else != null)
		{
			res = res.concat(String.format("\nelse %1$s", _else));
		}
		return res;
	}
	
	/* (non-Javadoc)
	 * @see sfdl.program.Statement#evaluate()
	 */
	@Override
	public void evaluate()
	{
		Variable condition = _condition.eval();
		assert condition.getSize() == 1;
		
		// evaluate then with condition
		if(!_then.isEmpty())
		{
			_evalSimpleIf(condition.getBit(0), _then);
		}		
		
		// evaluate else with !condition (if required)
		if(!_else.isEmpty())
		{
			_evalSimpleIf(condition.getBit(0).negate(), _else);
		}
	}
	
	/**
	 * Do the algo magic
	 * @param condition condition bit
	 * @param then then statement
	 */
	private void _evalSimpleIf(Bit condition, Block then)
	{
		// push the variables state
		BitsManager.instance.push();
		
		// evaluate then block
		then.evaluate();
		
		// get list of changed variables
		Hashtable<String, Variable> changed = BitsManager.instance.pop();
		
		// for each changed bit, put mux that decides new value.
		for(Entry<String, Variable> entry : changed.entrySet())
		{
			if(BitsManager.instance.isTempVariable(entry.getKey())
				&& (null == BitsManager.instance.getVariable(entry.getKey())))
			{
				continue; // ignore temp variables that were allocated inside then
			}
			_putMuxOnBits(entry.getKey(), entry.getValue(), condition);
		}
	}
	
	/**
	 * Puts mux on every bit of the specified variable.
	 * Mux gets on its inputs value of the variable before if and after if, 
	 * 	and in its control bit mux gets condition of the if. 
	 * @param name name of the variable to change.
	 * @param thenVar value after if
	 * @param condition if's condition
	 */
	private void _putMuxOnBits(String name, Variable thenVar, Bit condition)
	{
		Variable curVar = BitsManager.instance.getVariable(name);
		
		for(int i = 0; i < curVar.getSize(); i++)
		{
			Bit muxedBit = BitsManager.instance.allocateMux(
											curVar.getBit(i), // cur 
											thenVar.getBit(i), // new
											condition); // if new
			curVar.setBit(i, muxedBit);
		}
	}
	
	/* (non-Javadoc)
	 * @see sfdl.program.Statement#resolve(sfdl.program.Environment)
	 */
	@Override
	public void resolve(Environment env) throws CompilerError
	{
		_condition.resolve(env);
		_then.resolve(env);
		_else.resolve(env);
	
		// Just in case
		if(_condition.getType().getSize() 
				!= TypesFactory.BOOLEAN.getSize())
		{
			throw new CompilerError(String.format(
					"Condition in if must be boolean: %1$s", _condition));
					
		}
	}
	
	/* (non-Javadoc)
	 * @see sfdl.program.Statement#duplicate()
	 */
	@Override
	public If duplicate()
	{
		return new If(_condition.duplicate(), 
						_then.duplicate(), 
						_else.duplicate());
	}
	
    public Statement statementApply(ASTVisitor caller) {
        return caller.caseAIf(this);
    }
    
    public <T extends Joinable<T>> T apply(DepthFirstVisitor<T> caller) {
        return caller.caseAIf(this);
    }

}

