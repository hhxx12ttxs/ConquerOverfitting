/*
 * TaskDescription.java
 * 
 * last update: 16.01.2010 by Stefan Saru
 * 
 * author:	Alec(panovici@elcom.pub.ro)
 * 
 * Obs:
 */

//#include <defs.h>

package engine;

import middle.*;
import java.util.*;

@SuppressWarnings("serial")
class TaskDescription extends DefaultNameSpaceDescription{

	InstructionDescription instruction;
	NameSpaceDescription nsd;

	TaskDescription(NameSpaceDescription nsd, String name,
			int lineNo)throws ParseException{
		super(name);
		this.nsd = nsd;
		nsd.addScope(this, lineNo);
	}

	public boolean isTop() {
		return false;
	}

	public void addInstruction(InstructionDescription i){
		instruction = i;
	}
	//#idfdef DEBUG
	public void addModuleArray(String moduleName, int lineNo,
			String descName, ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("submodule inside a task ??");
	}

	public void addSubModule(String moduleName, int lineNo,
			String descName, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("submodule inside a task ??");
	}

	public void addInitial(InstructionDescription ins){
		throw new Error("initial inside a task ??");
	}

	public void addAlways(InstructionDescription ins){
		throw new Error("always inside a task ??");
	}

	public void addFunction(String name)throws ParseException{
		throw new Error("function inside a task ??");
	}

	public void addWire(String name, int lineNo, ExpressionDescription msb, 
			ExpressionDescription lsb, int expandType,
			int netType, Delay3Description delays)throws ParseException{
		throw new Error("wire insiide a task ??");
	}
	//#endif
	public void checkup()throws ParseException{}

	public void instantiateAll(NameSpace ns, ScopeNode thisScope)throws ParseException{
		super.instantiateAll(ns, thisScope);
	}

	/**
	 * Instantiates the instruction. This is done for every task-enable instruction, after the 
	 * actual parameters have been linked.
	 * @param paramHolders: e vector insode wich makeInstructionInstance will put the variables to wich the instructions
	 *                      inside this task-code instance will refer. To change the parameters, for an invocation, just set
	 *                      the values inside these variables.
	 */
	public DefaultInstruction makeInstructionInstance(NameSpace taskSpace)
	throws ParseException{

		return instruction == null ? null : instruction.instantiate(taskSpace);
	}

	/**
	 * Check that the tansfer of these parameters is possible and creates instances for the actual parameters.
	 * @param paramExps the Vector containing the Expressions corresponding to the paramaters
	 * @exception ParseException when the parameter list is incorrect
	 */
	Vector createParams(NameSpace taskSpace, Vector paramExps)
	throws ParseException
	{
		Enumeration param, port;
		Vector paramInstances = new Vector();
		//check that the ouputs can be successfully assigned
		for(param = paramExps.elements() , port = ports.elements();
		param.hasMoreElements() && port.hasMoreElements(); ){

			PortDescription p = (PortDescription)port.nextElement();

			Expression exp = (Expression) param.nextElement();
			if((p.type == PortDescription.output || p.type ==
				PortDescription.inout) && ! exp.isLeftValue())
				throw new ParseException("lValue required for param " + p);
			xConsole.debug("creating instance for port " + p + "value : " + exp);
			Result r;
			try{
				r = exp.evaluate();
			}catch(InterpretTimeException ex){
				xConsole.dumpStack(ex);
				throw new ParseException("illegal port value for port " + p.name + ": " + ex);
			}
			p.instantiate(taskSpace, null);
			BitVector b = r.getBits();
			b.setLength(Math.abs(p.nMsb - p.nLsb)+1);
			BitVector bb = new BitVector(p.nMsb, p.nLsb);
			try{
				bb.attrib(b);
			}catch(InterpretTimeException ex){
				xConsole.dumpStack(ex);
				throw new ParseException("illegal port value for port " + p.name + ": " + ex);
			}
			Register reg = new Register(taskSpace, p, bb);
			paramInstances.addElement(bb);
			taskSpace.scopeData.put(p.name, reg);
		}

		//check that the parameters number is correct
		if(param.hasMoreElements() || port.hasMoreElements())
			throw new ParseException("incorrect number of arguments for task " + name);

		return paramInstances;
	}

	void storeParamValues(Vector paramExps, Vector paramInstances)

	{
		for(Enumeration port = ports.elements(), exps = paramExps.elements(), instances = paramInstances.elements() ;
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
					throw new Error("TaskDescription.storeParamValues : paramInstances changed since createParams ?");
				}
				BitVector dest = (BitVector)instances.nextElement();
				source.setLength(dest.n);
				try{
					dest.attrib(source);
				}catch(InterpretTimeException ex){
					xConsole.dumpStack(ex);
					throw new Error("TaskDescription.storeParamValues : paramInstances or dests changed since createParams ?");
				}
			}else{
				exps.nextElement();
				instances.nextElement();
			}
		}
	}

	void fetchParamValues(Vector paramExps, Vector paramInstances){
		for(Enumeration port = ports.elements(), exps = paramExps.elements(), instances = paramInstances.elements() ;
		port.hasMoreElements() ; ){
			PortDescription p =  (PortDescription) port.nextElement();
			if(p.type == PortDescription.output || p.type == PortDescription.inout){
				try{
					((LeftValue)exps.nextElement()).assign((BitVector)instances.nextElement()); //since these expressions are checked, 
					//this cast should be valid
				}catch(InterpretTimeException ex){
					xConsole.dumpStack(ex);
					throw new Error("TaskDescription.fetchParamValues : error in exp. assign ??");
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










