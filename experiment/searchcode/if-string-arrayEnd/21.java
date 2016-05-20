/*
 * Register.java
 * 
 * last update: 24.01.2010 by Olaru Victor
 * 
 * author:	Victor(victor.olaru@gmail.com)
 * 
 * Obs:
 */

package engine;

/**
 * An unsigned reg or reg array (memory)
 */

class Register{

	/**
	 * Type info.
	 */
	Symbol desc;

	NameSpace ns; //asta chiar trebuie ?

	BitVector[] data;             // memory capabilities
	int arrayStart, arrayEnd, 
	increment;     //the increment for iteration from arrayStart towards arrayEnd

	Register(NameSpace ns, Symbol desc, int nStart, int nEnd,
			int arrayStart, int arrayEnd){
		this(ns, desc, nStart, nEnd, arrayStart, arrayEnd, false);
	}

	Register(NameSpace ns, Symbol desc, int nStart, int nEnd, 
			int arrayStart, int arrayEnd, boolean signed){
		data = new BitVector[Math.abs(arrayStart - arrayEnd)+1];
		this.arrayStart = arrayStart;
		this.arrayEnd = arrayEnd;
		increment = arrayStart <= arrayEnd ? 1 : -1;
		for(int i = 0; i <= Math.abs(arrayStart - arrayEnd) ; i++)
			data[i] = new BitVector(nStart, nEnd, signed);
		this.ns = ns;
		this.desc = desc;
	}

	Register(NameSpace ns, Symbol desc, BitVector initData){
		this(ns, desc, initData.msb, initData.lsb, 0, 0, initData.signed);
		data[0] = initData;
	}

	public String toString(){
		String result = "register " + desc + " : ";
		for(int i = arrayStart ; i >= arrayEnd ; i--)
			result += data[i];
		return result;
	}

	BitVector getData(int index) {
		try{
			return data[increment > 0 ? index - arrayStart : arrayStart - index];
		}catch(ArrayIndexOutOfBoundsException ex){
			xConsole.warn("index out of range in memory selection: " + 
					desc + "[" + index + "]");
			return BitVector.bX();
		}
	}

	boolean isMemory(){
		if(arrayStart != 0 || arrayEnd != 0)return true;
		return false;
		//    return ((MemoryDescription)desc).isMemory();
	}

	int getType(){
		return data[0].getType();
	}
}










