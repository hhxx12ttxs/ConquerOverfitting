/*
 * FunctionDescription.java
 * 
 * last update: 01.02.2010 by Florea Mihai
 * 
 * author:	Alec(panovici@elcom.pub.ro)
 * 			Florea Mihai(florea.mihai@gmail.com)
 * 
 * Obs: Am adaugat <Object>, @SuppressWarnings("serial") si 
 * @SuppressWarnings("unchecked") acolo unde era cazul.
 */

//#include <defs.h>

package engine;

import middle.*;
import java.util.*;

@SuppressWarnings("serial")
class FunctionDescription extends DefaultNameSpaceDescription{

	InstructionDescription instruction;
	NameSpaceDescription nsd;
	Symbol resultDescription;

	private FunctionDescription(NameSpaceDescription nsd,
			String name, int lineNo)throws ParseException{
		super(name);
		this.nsd = nsd;
		nsd.addScope(this, lineNo);
	}

	FunctionDescription(NameSpaceDescription nsd,
			String name, int lineNo, int type)throws ParseException{
		this(nsd, name, lineNo);
		switch(type){
		case Symbol.realType:
			resultDescription = new RealDescription(name, lineNo);
			break;
		case Symbol.intType:
			resultDescription = new IntegerDescription(name, lineNo);
		}
	}

	FunctionDescription(NameSpaceDescription nsd, String name, int lineNo,
			ExpressionDescription msb, ExpressionDescription lsb)
			throws ParseException{
		this(nsd, name, lineNo);
		resultDescription = new RegisterDescription(name, lineNo, msb, lsb, null, null);
	}

	public boolean isTop() {
		return false;
	}

	public void addInstruction(InstructionDescription i){
		instruction = i;
	}
	//#ifdef DEBUG
	@SuppressWarnings("unchecked")
	public void addModuleArray(String moduleName, int lineNo,
			String descName, ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("submodule inside a function ??");
	}
	
	@SuppressWarnings("unchecked")
	public void addSubModule(String moduleName, int lineNo,
			String descName, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("submodule inside a function ??");
	}

	public void addInitial(InstructionDescription ins, int lineNo){
		throw new Error("initial inside a function ??");
	}

	public void addAlways(InstructionDescription ins, int lineNo){
		throw new Error("always inside a function ??");
	}

	public void addFunction(String name, int lineNo)throws ParseException{
		throw new Error("function inside a function ??");
	}

	public void addWire(String name, int lineNo,  ExpressionDescription msb, 
			ExpressionDescription lsb, int expandType,
			int netType, Delay3Description delays)throws ParseException{
		throw new Error("wire inside a function ??");
	}
	//#endif
	public void checkup()throws ParseException{}

	public void instantiateAll(NameSpace ns, ScopeNode thisScope)
	throws ParseException{

		super.instantiateAll(ns, thisScope);
	}

	/**
	 * Instantiates the instruction. This is done for every function call instruction, after the 
	 * actual parameters have been linked.
	 * @param paramHolders: e vector inside wich makeInstructionInstance will put the variables to wich the instructions
	 *                      inside this function-code instance will refer. To change the parameters, for an invocation, just set
	 *                      the values inside these variables.
	 */
	public DefaultInstruction makeInstructionInstance(NameSpace funcSpace)
	throws ParseException{

		DefaultInstruction inst = instruction.instantiate(funcSpace);
		inst.checkIfIsAllowedInFunction(); //this may throw some
		return inst;
	}

	/**
	 * Check that the transfer of these parameters if possible and creates instances for the actual parameters.
	 * @param paramExps the Vector containing the Expressions corresponding to the paramaters
	 * @exception ParseException when the parameter list is incorrect
	 */
	
	@SuppressWarnings("unchecked")
	Vector<Object> createParams(NameSpace funcSpace, Vector paramExps)
	throws ParseException
	{
		Enumeration<Object> param, port;
		Vector<Object> paramInstances = new Vector<Object>();

		// first, create the result holder:
		// be aware that the first paramInstance is a 
		// Register or something like this, NOT a aBitVector !
		Object o = resultDescription.instantiate(funcSpace, new DummyWatchNode());
		paramInstances.addElement(o);
		funcSpace.scopeData.put(resultDescription.name, o);

		for(param = paramExps.elements(), port = ports.elements();
		param.hasMoreElements() && port.hasMoreElements(); ){

			PortDescription p = (PortDescription)port.nextElement();

			Expression exp = (Expression) param.nextElement();
			xConsole.debug("creating instance for port \"" + p + "\" value : " + exp);
			Result r;
			try{
				r = exp.evaluate();
			}catch(InterpretTimeException ex){
				xConsole.dumpStack(ex);
				throw new ParseException("illegal port value for port \"" + p.name + "\": " + ex);
			}
			p.instantiate(funcSpace, null);
			BitVector b = r.getBits();
			b.setLength(Math.abs(p.nMsb - p.nLsb)+1);
			BitVector bb = new BitVector(p.nMsb, p.nLsb);
			try{
				bb.attrib(b);
			}catch(InterpretTimeException ex){
				xConsole.dumpStack(ex);
				throw new ParseException("illegal port value for port \"" + p.name + "\": " + ex);
			}
			Register reg = new Register(funcSpace, p, bb);
			paramInstances.addElement(bb);
			funcSpace.scopeData.put(p.name, reg);
		}

		//check that the parameters number is correct
		if(param.hasMoreElements() || port.hasMoreElements())
			throw new ParseException("incorrect number of arguments for function \"" +
					name + "\"");

		return paramInstances;
	}

	@SuppressWarnings("unchecked")
	void storeParamValues(Vector paramExps, Vector paramInstances)

	{

		Enumeration instances = paramInstances.elements();
		//skip the result
		instances.nextElement();

		for(Enumeration port = ports.elements(), exps = paramExps.elements() ;
		port.hasMoreElements() ; ){
			PortDescription p =  (PortDescription) port.nextElement();
			if(p.type == PortDescription.input || p.type == PortDescription.inout){
				BitVector source = null;
				try{
					source = ((Expression)exps.nextElement()).evaluate().getBits();
				}catch(InterpretTimeException ex){
					xConsole.dumpStack(ex);
					//this is errorneous, since these instances are received from createParams, which
					//has checked hem as well
					throw new Error("FunctionDescription.storeParamValues : paramInstances changed since createParams ?");
				}
				BitVector dest = (BitVector)instances.nextElement();
				source.setLength(dest.n);
				try{
					dest.attrib(source);
				}catch(InterpretTimeException ex){
					xConsole.dumpStack(ex);
					throw new Error("FunctionDescription.storeParamValues : paramInstances or dests changed since createParams ?");
				}
			}else{
				exps.nextElement();
				instances.nextElement();
			}
		}
	}

	public String toString(){
		return nsd + "." + name;
	}

	public String toString(int line){
		return nsd.toString(line) + "." + name;
	}

	public String getFileName(){
		return nsd.getFileName();
	}
}










