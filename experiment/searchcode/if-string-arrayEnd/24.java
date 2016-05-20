/*
 * DefaultNameSpaceDescription.java
 * 
 * last update: 16.01.2010 by Stefan Saru
 * 
 * author:	Alec(panovici@elcom.pub.ro)
 * 
 * Obs:
 */

package engine;

import java.util.*;
import middle.*;

/**
 * The description of a NameSpace.
 */

class DefaultNameSpaceDescription implements NameSpaceDescription {

	/**
	 * The name of this namespace.
	 */
	String name;

	Hashtable regs;       //reg, integer, real, time, realtime, parameter
	Vector regsVector;   //has the same content as regs, but keeps
	//the declarations textual order
	Hashtable events;     //event
	Hashtable subScopes;

	Vector monitors;
	Vector initial;
	Vector  always;
	Vector functions; 

	/**
	 * The list of ports & their types
	 */
	Vector ports;

	/**
	 * Contains the list of the connections to the outside world
	 */
	Vector connectionList;

	/**
	 * Contains the same data as connectionList, but
	 * <STRONG>only<STRONG> if this module
	 * supports named connections.
	 */
	Hashtable connectionHash;

	DefaultNameSpaceDescription (String name) {
		this.name = name;
		regs = new Hashtable();
		regsVector = new Vector();

		events = new Hashtable();
		subScopes = new Hashtable();

		functions = new Vector();
		ports = new Vector();

		connectionList = new Vector();
		connectionHash = new Hashtable();
	}

	public String name() {
		return name;
	}

	public void addParameter (String name, int lineNo) throws ParseException {
		checkSymbolUnique(name, lineNo);
		Object o = new ParameterDescription(name, lineNo);
		regs.put(name, o);
		regsVector.addElement(o);
	}

	public void defParam (String name, ExpressionDescription ex)
	throws ParseException
	{
		ParameterDescription p;
		if ((p = (ParameterDescription)regs.get(name)) == null)
			throw new
			ParseException("DefaultNameSpaceDescription.defParam : undeclared parameter \"" +
					name + "\" ???");
		p.assign(ex);
	}

	public void addPort (String name, int lineNo,
			int type)throws ParseException {
		addPort(name, lineNo, type, null, null);
	}

	public void addPort (String name, int lineNo, int type,
			ExpressionDescription msb,
			ExpressionDescription lsb) throws ParseException {
		PortDescription p;
		if ((p = isPort(name)) != null) {
			if (p.type != PortDescription.none)
				throw new ParseException(toString(lineNo) +
						": error:redeclaration of port " + name +
				" with different type");
			else{
				p.setPortType(type);
				p.setLine(lineNo);
				p.setRange(msb, lsb);
				xConsole.debug("setting port \"" + name + "\" to type " + type);
				return;
			}
		}
		ports.addElement(new PortDescription(name, lineNo, type,
				msb, lsb));
		xConsole.debug("adding port \"" + name + "\" of type " + type + " msb = " + msb +
				" lsb = " + lsb);
	}

	public void addConnection (String name, int lineNo,
			ExpressionDescription portExp)
	throws ParseException
	{
		if (connectionHash.get(name) != null) {
			throw new ParseException(toString(lineNo) + 
					": error : duplicate identifier \"" +
					name +"\" in port list");
		}
		ConnectionDescription cd = new ConnectionDescription(name, lineNo, portExp);
		connectionList.addElement(cd);
		connectionHash.put(cd.name, cd);
		xConsole.debug("adding connection \"" + name + "\" with expression " + portExp);
	}

	/**
	 * Adds a submodule into this nameSpace.
	 * portsHash and portsVactor cannot be both non-empty.
	 */
	public void addSubModule (String moduleName, int lineNo,
			String descName, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		checkSymbolUnique(moduleName, lineNo);
		if(portsHash.isEmpty())
			subScopes.put(moduleName,
					new ModuleInstanceDescription(moduleName, lineNo,
							descName, null,
							strength, delays,
							portsVector, null,
							parameters));
		else
			subScopes.put(moduleName,
					new ModuleInstanceDescription(moduleName, lineNo,
							descName, null,
							strength, delays,
							null, portsHash,
							parameters));
	}

	public void addModuleArray(String moduleName, int lineNo,
			String descName, ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		checkSymbolUnique(moduleName, lineNo);
		if(portsHash.isEmpty())
			subScopes.put(moduleName,
					new ModuleArrayDescription(moduleName,
							lineNo,
							arrayStart,
							arrayEnd,
							descName,
							strength,
							delays,
							portsVector,
							null,
							parameters));
		else
			subScopes.put(moduleName,
					new ModuleArrayDescription(moduleName,
							lineNo,
							arrayStart,
							arrayEnd,
							descName,
							strength,
							delays,
							null,
							portsHash,
							parameters));
	}

	/**
	 * Adds a new sub-scope within this nameSpace.
	 */
	 public void addScope (NameSpaceDescription theScope,
			 int lineNo) throws ParseException {

		checkSymbolUnique(theScope.name(), lineNo);

		subScopes.put(theScope.name(),
				new ScopeInstanceDescription(theScope, lineNo));
	}

	/*
	 * ModuleDescription will override this.
	 */
	 public boolean isTop() {
		return false;
	}

	public void addInitial (InstructionDescription ins, int lineNo) {
		if (initial == null)initial = new Vector();
		initial.addElement(ins);
	}

	public void addAlways (InstructionDescription ins, int lineNo) {
		if (always == null)always = new Vector();
		always.addElement(ins);
	}

	public void addFunction (String name, int lineNo) throws ParseException {  //asta mai trebuie ??
	}


	/**
	 * Checks whether this name is unique inside this namespace.
	 * @param name is the name to be checked. It should be a simple name (no FQN's).
	 * @param checkPorts specifies if ports should be considered in this check
	 */
	void checkSymbolUnique (String name, int lineNo, boolean checkPorts) throws
	ParseException {

		boolean unique = true;

		if (regs.get(name) != null 
				|| events.get(name) != null || subScopes.get(name) != null)
			unique = false;

		if (checkPorts)
			if (isPort(name) != null)
				unique = false;

		if (!unique) throw new ParseException(toString(lineNo) +
				": error: duplicate symbol \"" +
				name + "\"");
	}

	void checkSymbolUnique (String name, int lineNo) throws ParseException {
		checkSymbolUnique(name, lineNo, true);
	}

	public PortDescription isPort (String name) {
		for (Enumeration e = ports.elements() ; e.hasMoreElements() ; ) {
			PortDescription p = (PortDescription)e.nextElement();
			if (p.name.equals(name))return p;
		}
		return null;
	}

	public void addRegister (String name, int lineNo,
			ExpressionDescription msb,
			ExpressionDescription lsb, 
			ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd) throws ParseException
			{

		PortDescription p;
		if ( (p = isPort(name)) != null) {
			if (p.type != PortDescription.output)
				throw new ParseException(toString(lineNo) + ": error: port \""
						+ name + "\" cannot be of type register");
		}
		checkSymbolUnique(name, lineNo, false);
		Object o = new RegisterDescription(name, lineNo, msb, lsb, arrayStart, arrayEnd);
		regs.put(name, o);
		regsVector.addElement(o);
			}

	public void addWire (String name, int lineNo, ExpressionDescription msb, 
			ExpressionDescription lsb, int expandType, int netType,
			Delay3Description delays) throws ParseException
			{
		checkSymbolUnique(name, lineNo, false); //we don't have to check ports
		Object o = new WireDescription(name, lineNo, msb, lsb, expandType, netType, delays);
		regs.put(name, o);
		regsVector.addElement(o);
			}

	public void addInteger (String name, int lineNo,
			ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd) throws ParseException
			{
		checkSymbolUnique(name, lineNo);
		Object o = new IntegerDescription(name, lineNo, arrayStart, arrayEnd);
		regs.put(name, o);
		regsVector.addElement(o);
			}

	public void addReal (String name, int lineNo) throws ParseException {
		checkSymbolUnique(name, lineNo);
		Object o = new RealDescription(name, lineNo);
		regs.put(name, o);
		regsVector.addElement(o);
	}

	public void addConnection(int lineNo, AssignableSelection lValue,
			ExpressionDescription rValue,
			Delay3Description delays, byte strength)
	throws ParseException
	{
		if (monitors == null)monitors = new Vector();
		monitors.addElement(new ContAssignMonitorDescription(this, lineNo,
				lValue,
				rValue,
				delays,
				strength));
	}

	//TODO: implementations for these

	public void addTime (String name, int lineNo) throws ParseException {
		checkSymbolUnique(name, lineNo);
		//regs.put(name, new TimeDescription(name));
		//regsVector...
	}

	public void addRealTime (String name, int lineNo) throws ParseException {
		checkSymbolUnique(name, lineNo);
		//regs.put(name, new RealTimeDescription(name));
		//regsVector...
	}

	public void addEvent (String name, int lineNo) throws ParseException { //more parameters ?
		checkSymbolUnique(name, lineNo);
		//  events.put(name, new EventDescription(name)); //??
	}

	/**
	 *  the default here implementation does nothing
	 * @see ModuleDescription.checkup
	 */
	public void checkup () throws ParseException {
		xConsole.debug("WARNING: DefnameSpaceDesc.checkup called !!");
	}

	/**
	 * The default implementation here does nothing
	 */
	void checkPorts(NameSpace ns) throws ParseException {}

	public void instantiateAll (NameSpace ns,
			ScopeNode thisScope) throws ParseException {

		int i;

		xConsole.debug("instantiating " + name);

		//first create the local data
		for (Enumeration e = regsVector.elements() ; e.hasMoreElements() ; ) {
			Symbol s = (Symbol) e.nextElement();
			ns.scopeData.put(s.name, s.instantiate(ns, thisScope));
			xConsole.debug("added symbol " + s.name + " into " + name);
		}

		//..and next assure that inner scopes are created

		ns.scopeData.put(name, ns);

		checkPorts(ns);

		for (Enumeration e = subScopes.elements() ; e.hasMoreElements() ; ) {
			Symbol s = (Symbol) e.nextElement();
			ns.scopeData.put(s.name, s.instantiate(ns, thisScope));
			xConsole.debug("added scope " + s.name + " into " + name);
		}

		if (monitors != null) {
			xConsole.debug("instantiating monitors");
			for (Enumeration e = monitors.elements() ; e.hasMoreElements() ; )
				((ContAssignMonitorDescription)e.nextElement()).instantiate(ns);
		}

		if (initial != null) {
			xConsole.debug("instantiating initials");
			for (Enumeration e = initial.elements() ; e.hasMoreElements() ; ) {
				Time.controlThreads.addElement( 
						new ControlThread(((InstructionDescription)
								e.nextElement()).instantiate(ns)));
			}
		}

		if (always != null) {
			xConsole.debug("instantiating always's");
			for (Enumeration e = always.elements() ; e.hasMoreElements() ; ) {
				Time.controlThreads.addElement( 
						new AlwaysThread(((InstructionDescription)e.nextElement()).instantiate(ns)));
			}
		}
	}

	public String toString (int line) {
		return name;
	}

	public String getFileName () {
		throw new Error("DefaultNameSpaceDescription.getFileName doesn't knows it !");
	}
}

