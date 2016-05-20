/*
 * UDPDescription.java
 * 
 * last update: 16.01.2010 by Stefan Saru
 * 
 * author:	Power
 * 			Alec(panovici@elcom.pub.ro)
 * 
 * Obs:
 */

//#include <defs.h>

package engine;

import java.util.*;
import middle.*;

/**
 * The description of an User Defined Primitive.
 */
class UDPDescription
extends UserModuleDescription
{

	Vector Table;
	/**
	 * Used instead of Table for faster
	 * access during simulation.
	 */
	UDPTableEntry[] entries;

	/*
	 * UDP Types : unknown/sequantia/combinational
	 */

	public static final byte UNKType = 0;
	public static final byte SEQType = 1;
	public static final byte COMType = 2;

	byte type;

	String fileName;
	int line;
	BitVector initialState;
	Delay3 delay;



	/**
	 * An exprssion that contains all the inputs
	 * (including the current state for the sequentials) .
	 */
	SelectionExpressionDescription inputExpression, outputExpression;

	UDPDescription (String name, int line, String fileName) {
		super(name);
		Table = new Vector();
		this.line = line;
		this.fileName = fileName;
		type = UNKType;
		delay = Delay3.nullDelay();
	}

	public boolean isTop() {
		return false;
	}

	public void addPort (String name, int lineNo, int type,
			ExpressionDescription msb,
			ExpressionDescription lsb) throws ParseException {
		PortDescription p;
		if ((p = isPort(name)) != null) {
			if (p.type != PortDescription.none && p.type != type)
				throw new ParseException(toString(lineNo) +
						": error: redeclaration of port " + name +
						" with different type");
			else{
				if(msb != null || lsb != null)
					throw new ParseException(toString(lineNo) +
							": error: UDP ports must be all 1-bit lenght");
				p.setPortType(type);
				p.setLine(lineNo);
				p.setRange(msb, lsb);
				xConsole.debug("setting port \"" + name + "\" to type " + type);
				return;
			}
		}
		ports.addElement(new PortDescription(name, lineNo, type,
				msb, lsb));
		xConsole.debug("adding port \"" + name + "\" of type " + type +
				" msb = " + msb +
				" lsb = " + lsb);
	}

	/**
	 * Adds an entry into the table. Note that no sanity check is made:
	 * we heavily rely on paring decision.
	 */
	public void addTableEntry (String ent, int lineNo) throws ParseException{
		try {
			xConsole.debug("UDPDescription.addTableEntry(" + ent +
					") length is " + ports.size());
			UDPTableEntry te = new UDPTableEntry(ent, ports.size() +
					(type == SEQType ? 1 : 0),
					type);
			if (te.isEdgeEntry()) //edge-sensitive entries first
			Table.add(0, te);
			else Table.add(te);
		} catch (ParseException pex) {
			throw new ParseException(toString(lineNo) + pex);
		}
	}

	public void checkup() throws ParseException {

		inputExpression = new SelectionExpressionDescription();
		outputExpression = new SelectionExpressionDescription();

		for (Enumeration e = ports.elements() ; e.hasMoreElements() ;) {
			PortDescription p = (PortDescription)e.nextElement();
			if (regs.get(p.name) == null)
				addWire( p.name, p.lineNo, p.msbExp, p.lsbExp,
						WireDescription.defaultExpandType,
						WireDescription.defaultNetType,
						Delay3Description.nullDelay);
			if (p.type == PortDescription.input)
				inputExpression.addChunk(new GenericSelectorDescription(new
						FQNDescription(p.name)));
			else outputExpression.addChunk(new GenericSelectorDescription(new
					FQNDescription(p.name)));
		}
		if(type == SEQType)
			inputExpression.addChunk(new
					GenericSelectorDescription(new
							FQNDescription(((RegisterDescription)
									regsVector.elementAt(0)).
									name)));
		//put everything from 'Table' into 'entries'.
		int n;
		entries = new UDPTableEntry[n = Table.size()];
		Enumeration e = Table.elements();
		for (int i = 0; i < n; i++)
			entries[i] = (UDPTableEntry) e.nextElement();
		Table = null; //.. and get rid of it
	}

	public String toString() {
		return fileName + ":" + name;
	}

	public String toString(int line) {
		return fileName + ":" + line + ":" + name;
	}

	public String getFileName() {
		return fileName;
	}

	//#ifdef DEBUG

	public void addSubModule (String moduleName, int lineNo,
			String descName, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength)
	throws ParseException
	{
		throw new Error("UDPDescription.addSubModule called");
	}

	public void addModuleArray(String moduleName, int lineNo,
			String descName, ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd, Vector portsVector,
			Hashtable portsHash, Vector parameters,
			byte strength, Delay3Description delays)
	throws ParseException
	{
		throw new Error("UDPDescription.addModuleArray called");
	}  

	public void addScope (NameSpaceDescription theScope,
			int lineNo) throws ParseException
			{
		throw new Error("UDPDescription.addScope called");
			}

	public void addAlways (InstructionDescription ins, int lineNo) {
		throw new Error("UDPDescription.addAlways called");
	}

	public void addInteger (String name, int lineNo,
			ExpressionDescription arrayStart,
			ExpressionDescription arrayEnd) throws ParseException
			{
		throw new Error("UDPDescription.addInteger called");
			}

	public void addReal (String name, int lineNo) throws ParseException {
		throw new Error("UDPDescription.addReal called"); 
	}
	public void addTime (String name, int lineNo) throws ParseException {
		throw new Error("UDPDescription.addTime called"); 
	}

	public void addRealTime (String name, int lineNo) throws ParseException {
		throw new Error("UDPDescription.addTime called"); 
	}

	public void addEvent (String name, int lineNo) throws ParseException {
		throw new Error("UDPDescription.addTime called"); 
	}

	//#endif


	/**
	 * What ModuleInstanceDescription whinks to be paarmeters,
	 * are actually delay values in this case. So proceed so:
	 */
	public void setParams (Vector results, Delay3 delay) throws ParseException {
		if (((results != null) && (delay != null)))
			throw new ParseException (": error: UDPs cannot have param specs");
		if (results != null && results.size() > 0) {
			if (results.size() > 2)
				throw new ParseException (": error: UDP's delay specs can have only 2 values");
			delay = new Delay3();
			int aux = (int) ((Result)results.elementAt(0)).getLong();
			delay.setDelay1(aux, aux, aux);
			aux = (int) ((Result)results.elementAt(1)).getLong();
			delay.setDelay2(aux, aux, aux);
			delay.setDelay3(0, 0, 0);
			delay.delaySpecs = 3;
		}

		if (delay != null) {
			this.delay = delay;
		}
	}

	public void unsetParams() {
		delay = Delay3.nullDelay();
	}

	///////////////////////////////
	// ModuleFactory implementation
	///////////////////////////////

	/**
	 * @see ModuleFactory
	 */
	public Object createNewModule (NameSpace parent,
			ModuleInstanceDescription miDesc,
			byte strength,
			ScopeNode thisScope) throws ParseException{
		UDP u = new UDP(this, parent, miDesc, thisScope);
		linkModule(u, parent, strength, miDesc);
		return u;
	}

	public Object createNewModuleArray(NameSpace parent,
			ModuleInstanceDescription miDesc,
			byte strength,
			ScopeNode thisScope, int ars, int are,
			int n, int increment)
	throws ParseException
	{
		UDP[] modules = new UDP[n];
		for (int i = 0; i < n ; i++)
			modules[i] = new UDP(this,
					parent,
					miDesc,
					thisScope.addScope(Constants.plaIcon,
							name + "[" +
							         (ars + i * increment) +
							         "]"));
		linkModuleArray(modules, parent, miDesc, strength,
				ars, are, n, increment);

		return new ModuleArray(this, parent, ars,
				are, n, increment, modules);
	}
}





