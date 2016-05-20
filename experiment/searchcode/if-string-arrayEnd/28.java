/*
 * ForkDescription.java
 * 
 * last update: 16.01.2010 by Stefan Saru
 * 
 * author:	Power(power@kermit.cs.pub.ro)
 * 
 * Obs:
 */

package engine;
import middle.*;
import java.util.*;

/**
 * A fork...join block's description
 */

class ForkDescription extends InstructionDescription implements NameSpaceDescription{

	InstructionDescription last = null; //ultima -\
	InstructionDescription first = null; //prima instructiune din bloc
	String name;
	DefaultNameSpaceDescription localScope;  

	ForkDescription(int delaybefore, int line, NameSpaceDescription nsd, String name)throws ParseException{
		super(delaybefore, line, nsd);
		if(name != null){ //add this scope as a nested sub-scope
			this.name = name;
			localScope = new DefaultNameSpaceDescription(name);
			nsd.addScope(this, line);
		}
	}

	DefaultInstruction instantiate(NameSpace ns)throws ParseException{
		//instantiaza blocul si apoi recursiv instructiunile pe care le
		//contine
		NameSpace local = ns;
		if (name != null) local = (NameSpace)ns.resolveName(new FQN(name));
		return new Fork(local, delayBefore, this, first == null ? null : first.instantiate(local),
				next == null ? null : next.instantiate(ns));
	}


	void addInside(InstructionDescription ids){
		if (ids == null){
			xConsole.debug("added null instruction ??");
			return;
		}
		if (first == null) first = ids;
		else last.next = ids;
		last = ids;
	}

	void add(InstructionDescription i){    
	}

	public String toString(){
		return super.toString() + "." + (name == null ? " ": name);
	}

	public String toString(int line){
		return nsd.toString(line) + "." + (name == null ? "": name + ":");
	}

	//////////////////////////////////
	//NameSpace implementation:
	/////////////////////////////////

	public String name(){
		return name;
	}

	public boolean isTop() {
		return false;
	}

	public void addParameter(String name, int lineNo)throws ParseException{
		if(localScope != null)localScope.addParameter(name, lineNo);
		else throw new ParseException("parameter not allowed here");
	}

	public void defParam(String name, ExpressionDescription ex)throws ParseException{
		throw new Error("defparam inside e block ??");
	}

	public void addConnection (String name, int lineNo,
			ExpressionDescription portExp)
	throws ParseException
	{
		throw new Error("connection for a block ??");
	}

	public void addPort(String name, int lineNo, int type) throws ParseException{
		throw new Error("port inside e block ??");
	}

	public void addPort(String name, int lineNo, int type,
			ExpressionDescription portExp)throws ParseException{
		throw new Error("port inside e block ??");
	}

	public void addPort(String name, int lineNo, int type,
			ExpressionDescription msb,
			ExpressionDescription lsb)throws ParseException{
		throw new Error("port inside e block ??");
	} 

	public void addModuleArray(String moduleName, int lineNo,
			String descName, ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("subModule inside e block ??");
	}
	public void addSubModule(String moduleName, int lineNo,
			String descName, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("subModule inside e block ??");
	}

	public void addScope(NameSpaceDescription theScope,
			int lineNo)throws ParseException{
		if(localScope != null)localScope.addScope(theScope, lineNo);
		else throw new ParseException("subscope not allowed here");
	}

	public void addInitial(InstructionDescription ins, int lineNo){
		throw new Error("initial inside e block ??");
	}

	public void addAlways(InstructionDescription ins, int lineNo){
		throw new Error("always inside e block ??");
	}

	public void addFunction(String name, int lineNo)throws ParseException{
		throw new Error("function inside e block ??");
	}

	public void addRegister(String name, int lineNo,
			ExpressionDescription msb, ExpressionDescription lsb, 
			ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd)throws ParseException{
		if(localScope != null)
			localScope.addRegister(name, lineNo, msb, lsb, arrayStart, arrayEnd);
		else throw new ParseException("register not allowed here");
	}


	public void addWire(String name, int lineNo, ExpressionDescription msb, 
			ExpressionDescription lsb, int expandType,
			int netType, Delay3Description delays)throws ParseException{
		throw new Error("wire inside e block ??");
	}

	public void addInteger(String name, int lineNo,
			ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd)
	throws ParseException{
		if(localScope != null)localScope.addInteger(name, lineNo, arrayStart, arrayEnd);
		else throw new ParseException("integer not allowed here");
	}

	public void addReal(String name, int lineNo)throws ParseException{
		if(localScope != null)localScope.addReal(name, lineNo);
		else throw new ParseException("real not allowed here");
	}

	public void addConnection(int lineNo, AssignableSelection lValue,
			ExpressionDescription rValue,
			Delay3Description delays, byte strength){
		throw new Error("assign inside e block ??");
	}

	public void addTime(String name, int lineNo)throws ParseException{
		if(localScope != null)localScope.addTime(name, lineNo);
		else throw new ParseException("time not allowed here");
	}

	public void addRealTime(String name, int lineNo)throws ParseException{
		if(localScope != null)localScope.addRealTime(name, lineNo);
		else throw new ParseException("realtime not allowed here");
	}

	public void addEvent(String name, int lineNo)throws ParseException{
		if(localScope != null)localScope.addEvent(name, lineNo);
		else throw new ParseException("event not allowed here");
	}

	public void instantiateAll(NameSpace ns, ScopeNode thisScope)throws ParseException{
		if(localScope != null)localScope.instantiateAll(ns, thisScope);
		else throw new Error("BlockDescription.instantiate called for noname block ??");
	}

	public String getFileName(){
		return nsd.getFileName();
	}
} 






