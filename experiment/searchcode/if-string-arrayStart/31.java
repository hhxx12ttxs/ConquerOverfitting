/*
 * RegisterDescription.java
 * 
 * last update: 24.01.2010 by Olaru Victor
 * 
 * author:	Victor(victor.olaru@gmail.com)
 * 
 * Obs:
 */
package engine;

//import java.util.*;
import middle.*;

/**
 * Contains alll the infomation about a register or integer, or a memory 
 * of the above types.
 */
@SuppressWarnings("serial")
class RegisterDescription extends Symbol implements MemoryDescription{

	int nStart, nEnd, nMemStart, nMemEnd;
	ExpressionDescription msb, lsb,
	arrayStart, arrayEnd;

	/** 
	 * This makes the difference beteween reg and integer.
	 */
	boolean sign;

	RegisterDescription(String name, int lineNo, 
			ExpressionDescription msb, ExpressionDescription lsb, 
			ExpressionDescription arrayStart, ExpressionDescription arrayEnd){
		this(name, lineNo, msb, lsb, arrayStart, arrayEnd, false);
	}

	RegisterDescription(String name, int lineNo,
			ExpressionDescription msb, ExpressionDescription lsb, 
			ExpressionDescription arrayStart, ExpressionDescription arrayEnd, boolean sign){
		super(name, lineNo);
		this.msb = msb;
		this.lsb = lsb;
		this.arrayStart = arrayStart;
		this.arrayEnd = arrayEnd;
		this.sign = sign;
		nStart = nEnd = nMemStart = nMemEnd = 0;
		xConsole.debug("new register [" + nStart + ":" + nEnd + "] " + name);
	}

	public void setRange(int nStart, int nEnd){
		this.nStart = nStart;
		this.nEnd = nEnd;
	}

	public Object instantiate(NameSpace ns, ScopeNode thisScope)throws ParseException{
		try{
			if(nStart == 0)
				nStart = msb == null ? 0 : (int)msb.instantiate(ns).evaluate().getInt().value();
			if(nEnd == 0)
				nEnd = lsb == null ? 0 : (int)lsb.instantiate(ns).evaluate().getInt().value();
			nMemStart = arrayStart == null ? 0 : 
				(int)arrayStart.instantiate(ns).evaluate().getInt().value();
			nMemEnd = arrayEnd == null ? 0 : 
				(int)arrayEnd.instantiate(ns).evaluate().getInt().value();
		}catch(Exception e){
			xConsole.debug("" + e);
			throw new ParseException(ns.desc.toString(lineNo) + ": error:cannot evaluate LSB or MSB for register \"" + this + "\"");
		}

		Register r = new Register(ns, this, nStart, nEnd, nMemStart, nMemEnd, sign);
		String fqn = ns + "." + name;

		if(nMemStart != nMemEnd){
			//if it is a memory, make a sub-tree with each location as a leaf
			ScopeNode regNode = thisScope.addScope(Constants.memIcon, name);
			int increment = nMemStart <= nMemEnd ? 1 : -1;
			int i;

			for(i = nMemStart ; i != nMemEnd ; i += increment){
				(new WatchMonitor(r.getData(i), regNode.addLeaf(Constants.regIcon, "["+i+"]",
						r.getData(i)))).trigger();
				main.historyManager.registerVariable(r.getData(i), fqn + "[" + i + "]");
			}
			(new WatchMonitor(r.getData(i), regNode.addLeaf(Constants.regIcon, "["+i+"]",
					r.getData(i)))).trigger();
			main.historyManager.registerVariable(r.getData(i), fqn + "[" + i + "]");
		}else{
			(new WatchMonitor(r.getData(0), thisScope.addLeaf(Constants.regIcon,
					name, r.getData(0)))).trigger();
			main.historyManager.registerVariable(r.getData(0), fqn );
		}
		return r;
	}

	public int getArrayStart(){
		return nMemStart;
	}
	public int getArrayEnd(){
		return nMemEnd;
	}

	public int getType(){
		if(sign)return intType;
		return regType;
	}

	public boolean isMemory(){
		return (arrayStart != null) ||(arrayEnd != null) ||
		(nMemStart != 0) || (nMemEnd != 0);
	}

}

