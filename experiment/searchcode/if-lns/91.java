package com.mdsws.z80.prsr;

import java.util.StringTokenizer;

import com.mdsws.z80.Instr;

public class Prsr {

	public Instr[] prs(String[] lns) {
		
		
		Instr[] ins=new Instr[lns.length];
		for (int i=0;i<lns.length;i++){
			ins[i]=prsInstr(lns[i]); 
				
			}
		return ins;
	}

	private Instr prsInstr(String txt) {
		
		String[]ts= splt(txt," ,");
		for (InstrPrsr prsr: InstrPrsr.values()) {
			if(prsr.getSntx().equals(ts[0]))
			{
				return prsr.prs(ts);
			}
		}

		
		throw new CdSntxExc("unknown instruction " + txt);
	}

	private String[] splt(String txt,String seps) {
		StringTokenizer t=new StringTokenizer(txt,seps);
		String[]tks=new String[t.countTokens()];
		 int i=0;
		while(t.hasMoreTokens())
		{
		 tks[++i]=t.nextToken();
		}
		return tks;
	}

}

