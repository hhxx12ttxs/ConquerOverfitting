package kodkod.engine.satlab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SMTSolver implements SATSolver{

	int numVars = 0;
	int numClauses = 0;
	boolean[] varValues;
	boolean sat;
	//TODO WHY DOES OUT.TXT NOT WORK?!?!!
	//String file = "/home/ezulkosk/workspace/kodkod/out.txt";
	//TODO this needs to be dynamic
	String OUTFILE = "/home/ezulkosk/z3/test.smt2";
	String VARFILE = "/home/ezulkosk/z3/vars";
	String CLAUSEFILE = "/home/ezulkosk/z3/clauses";
	BufferedWriter outfile = null;
	BufferedWriter varfile = null;
	BufferedWriter clausefile = null;
	
	public SMTSolver()
	{
		init();
	}
	
	public void addGetValueCalls() {
		for (int i = 1; i <= numVars; i++) {
			writeln(outfile, "(get-value (bool" + i + "))");
		}
	}
		
	public void checkSATOld()
	{
		writeln(outfile, "(check-sat)");
	}
	
	public void checkSAT()
	{
			writeln(outfile, "(check-sat");
			for (int i = 1; i <= numClauses; i++)
				write(outfile, " clause"+i);
			writeln(outfile,")");
	}
	
	//clause declarations needed for unsat-core
	public void addClauseDeclarations()
	{
		for(int i = 0; i < numClauses; i++)
			writeln(varfile,"(declare-const clause" + (i + 1) + " Bool)\n");
	}
	
	public boolean solve()
	{
			varValues = new boolean[numVars+1];
			addClauseDeclarations();
			//writeln(clausefile, "))");
			closeFile(varfile);
			closeFile(clausefile);
			closeFile(outfile);
			concatFiles();
			try{
			FileWriter fstream = new FileWriter(OUTFILE, true);
			outfile = new BufferedWriter(fstream);
			}catch(Exception e){}
			checkSAT();
			addGetValueCalls();
			writeln(outfile, "(get-unsat-core)");
			closeFile(outfile);
		
		return runZ3();
		
	}
	
	public boolean valueOf(int num)
	{
		//System.out.println("VALUEOF" + num + " " + varValues[num]);
		return varValues[num];
	}
	
	public boolean addClauseOld(int [] lits)
	{
		//System.out.println(Arrays.toString(lits));
		StringBuilder s = new StringBuilder();
		s.append("  (or");
		for(int lit: lits)
		{
			if(lit > 0)
				s.append(" bool" + lit);
			else
				s.append(" (not bool" + (- lit) + ")");
		}
		s.append(")");
		//System.out.println(s);
		writeln(clausefile, s.toString());
		numClauses++;
		return true;
	}
	
	//assert => clause used to extract unsat core
	public boolean addClause(int [] lits)
	{
		//System.out.println(Arrays.toString(lits));
		StringBuilder s = new StringBuilder();
		s.append("(assert (=> clause" + (numClauses+1) + " (or");
		for(int lit: lits)
		{
			if(lit > 0)
				s.append(" bool" + lit);
			else
				s.append(" (not bool" + (- lit) + ")");
		}
		s.append(")))");
		//System.out.println(s);
		writeln(clausefile, s.toString());
		numClauses++;
		return true;
	}
	
	
	public void addVariables(int num)
	{
		System.out.println("adding variables: " + num);
		for(int i = numVars + 1; i <= num; i++)
			  {
				  writeln(varfile, "(declare-const bool" + i + " Bool)");
			  }
		numVars = num;
		
		
	}
	
	public int numberOfVariables()
	{
		return numVars;
	}

	public int numberOfClauses()
	{
		return numClauses;
	}
	
	private boolean runZ3()
	{
		try
	    {     
			BufferedReader in;
	        Runtime rt = Runtime.getRuntime();
	        Process proc = rt.exec(new String[]{"z3", OUTFILE});
	        InputStream is = proc.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        in = new BufferedReader(isr);
	        System.out.println("<OUTPUT>");
	        //int exitVal = proc.waitFor();
	        sat = parseOutput(in);
	        System.out.println("</OUTPUT>");
	        //System.out.println("Process exitValue: " + exitVal);
	    } catch (Throwable t)
	      {
	        t.printStackTrace();
	      }
		return sat;
	}
	
	
	private void parseSAT(BufferedReader in) throws Exception
	{
		String line;
		String words[];
		int var;
		boolean value;
		while ( (line = in.readLine()) != null)
		{
            //System.out.println(line);
            if(line.contains("((bool"))
            {
            	//System.out.println("CONTAINED: " + line);
            	words = line.split(" ");
            	//System.out.println(Arrays.toString(words[0].split("bool")));
            	var = Integer.parseInt(words[0].split("bool")[1].replace(')', ' '));
            	//System.out.println("A" + var);
            	//System.out.println("TETS" + Arrays.toString(words[1].split("\"\\)")));//.split(")")[0]);
            	value = Boolean.parseBoolean(words[1].split("\"\\)")[0].replace(')', ' ').trim());
            	//System.out.println("value" + words[1].split("\"\\)")[0].replace(')', ' '));
            	
            	varValues[var] = value;
            	//System.out.println("VAR: " + var + " " + varValues[var]);
            	
            }
            else
            {
            	//System.out.println("UNCONTAINED: " + line);
            }
		}
	}
	
	//TODO extract unsat core clauses
	private void parseUNSAT(BufferedReader in) throws Exception
	{
		String line;
		while ( (line = in.readLine()) != null)
		{
            System.out.println(line);
		}
	}
	
	private boolean parseOutput(BufferedReader in) throws Exception
	{
		String line;
		line = in.readLine();
		System.out.println(line);
		if(line.equals("sat")){
			sat = true;
			parseSAT(in);
			return sat;
		}
		else if(line.equals("unsat")){
			sat = false;
			parseUNSAT(in);
			return sat;
		}
		else
			throw new Exception("First line of output is not sat or unsat.");
	}
	
	private void write(BufferedWriter writer, String line)
	{
		try
		{
			writer.write(line);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	private void writeln(BufferedWriter writer, String line)
	{
		write(writer, line + "\n");
	}
	
	private void closeFile(BufferedWriter writer)
	{
		try
		{
			writer.close();
		}
		catch(Exception e){}
	}
	
	private void concatFiles()
	{
		try{
		OutputStream out = new FileOutputStream(OUTFILE, true);
	    byte[] buf = new byte[1024];
	    String[] files = new String[]{VARFILE, CLAUSEFILE};
	    for (String file : files) {
	        InputStream in = new FileInputStream(file);
	        int b = 0;
	        while( (b = in.read(buf)) >= 0) {
	        	//System.out.println(buf.toString());
	            out.write(buf, 0, b);
	            out.flush();
	        }
	        in.close();
	    }
	    out.close();
		}
		catch(Exception e){}
	}
	
	
	private void init()
	{
		try{
			FileWriter fstream = new FileWriter(OUTFILE);
			outfile = new BufferedWriter(fstream);
			fstream = new FileWriter(VARFILE);
			varfile = new BufferedWriter(fstream);
			fstream = new FileWriter(CLAUSEFILE);
			clausefile = new BufferedWriter(fstream);
		}
		catch(Exception e){}
		//writeln(clausefile, "(assert (and ");
		writeln(outfile, "(set-option :produce-unsat-cores true)");
	}
	
	public void free()
	{
		
	}
}

