package org.mentalsmash.crossroads.parsing;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mentalsmash.crossroads.Configuration;
import org.mentalsmash.crossroads.Crossword;
import org.mentalsmash.crossroads.CrosswordException;

import alice.tuprolog.Int;
import alice.tuprolog.InvalidLibraryException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.UnknownVarException;

/**
 * This class parses a crossword schema definition and the relative definitions file into an istance of the Crossword class, using a 
 * Prolog engine as parser (and actually as lexer too).
 * @author Andrea Reale
 *
 */
public class PrologParser implements CrosswordParser {

	private static Logger log = Logger.getLogger(PrologParser.class);
	
	
	private final static String TUPROLOG_DCG_LIB = "alice.tuprolog.lib.DCGLibrary";
	
	private Prolog _parserEngine;

	
	
	/**
	 * Builds a parser witch uses prolog for parsing. 
	 * @param scanner an istance of CrossroadsScanner that will pass "tokens" to this parser
	 * @throws ParsingException if the parser is not succesfully created
	 */
	public PrologParser() throws ParsingException{
		
		try {
			setParserEngine(new Prolog());
			getParserEngine().loadLibrary(TUPROLOG_DCG_LIB);

			loadTheories();
		} catch(InvalidLibraryException e)
		{
			log.fatal("Cannot load tuprolog library " + TUPROLOG_DCG_LIB, e);
			throw new ParsingException("Unable to instantiate parser.",e);
		}
		
	}




	
	/**
	 * Returns the tuProlog engine used to Parse   the schema
	 * @return the prolog engine
	 */
	private Prolog getParserEngine() {
		return _parserEngine;
	}
	

	
	/**
	 * Change the engine used by the parser. 
	 * @param parserEngine the tuProlog (or one with the same interface) parsing engine
	 */
	private void setParserEngine(Prolog parserEngine) {
		if(parserEngine == null)
			throw new IllegalArgumentException("Cannot set a null parser");
		_parserEngine = parserEngine;
	}
	

	/**
	 * Loads the prolog theories for the parser.
	 */
	private void loadTheories(){
		if(getParserEngine()== null )
			throw new IllegalArgumentException("prolog engine reference is null");

		FileInputStream lexerTheory = null;
		FileInputStream parserTheory = null;
		FileInputStream builderTheory = null;
		try{
			lexerTheory = new FileInputStream(Configuration.getInstance().getLexerTheoryPath());
			parserTheory = new FileInputStream(Configuration.getInstance().getParserTheoryPath());
			builderTheory = new FileInputStream(Configuration.getInstance().getBuilderTheoryPath());
			getParserEngine().addTheory(new Theory(lexerTheory));
			getParserEngine().addTheory(new Theory(parserTheory));
			getParserEngine().addTheory(new Theory(builderTheory));
		} catch (Exception e){
			log.fatal("Invalid Theory Files",e);
			e.printStackTrace();
			System.exit(-1);
		} finally {
			try{
				lexerTheory.close();
				parserTheory.close();
			} catch(IOException e) {
				log.warn("Error in closing theory streams");
			}
		}
	}





	//for documentation take a look at  CrosswordParser's javadoc
	@Override
	public Crossword parseCrosswordSource(File schema,File defintions) 
		throws SyntacticException,SemanticException, ParsingException,FileNotFoundException {
		
		FileInputStream schemaStream = new FileInputStream(schema);
		FileInputStream defStream = new FileInputStream(defintions);
		return parseCrosswordSource(schemaStream,defStream);
	}



	@Override
	public Crossword parseCrosswordSchemaOnly(InputStream schema) throws SemanticException,SyntacticException, ParsingException{
		ArrayList<Term> stokens = generateTokens(schema);
		Term parseTree = parseSchemaTokens(stokens);
		
		return buildSchemaOnlyCwdInstance(parseTree);
	}

	private Crossword buildSchemaOnlyCwdInstance(Term parseTree) throws SemanticException, SyntacticException, ParsingException {
		//Using prolog unification facilietes to retrievs necessary parameters from the parse tree
		Struct paramGoal = new Struct("schema_parameters",parseTree,Term.createTerm("Id"),
				Term.createTerm("Size"),Term.createTerm("HolesList"));
		SolveInfo parametersBindings = getParserEngine().solve(paramGoal);
		
		Struct size = null;
		Term id = null;
		Struct holes = null;

		try {
			size = (Struct)parametersBindings.getVarValue("Size");
			id = parametersBindings.getVarValue("Id");
			holes = (Struct)parametersBindings.getVarValue("HolesList");
		} catch(NoSolutionException e){
			log.fatal("No solution found for binding parameters. This may be due a wrong parse tree :(",e);
			throw new SyntacticException("Cannot bind to PT parameters. Wrong PT?");
		}catch (ClassCastException e) {
			log.fatal("Invalid cast from Term to struct?",e);
			System.exit(-5);
		}
		
		if(size.getArity() != 2 || !size.getName().equals("size"))
			
			throw new SyntacticException("Illegal arity for size term");
		
		int nRows = ((Int)size.getArg(0).getTerm()).intValue();
		int nCols = ((Int)size.getArg(1).getTerm()).intValue();
		
		String identifier = id.toString();
		HashSet<Point> holesSet = extractHolesMap(holes);		

		
		
		Crossword instance = null;
		try {
			instance = new Crossword(identifier,nRows,nCols,holesSet);
		} catch (CrosswordException e) {
			log.fatal("Unable to instantiate crossword",e);
			throw new SemanticException("Inconsistent crossword parameters: " +e.getMessage(),e);
		}

		
		
		
		return instance;
	}





	//for documentation take a look at  CrosswordParser's javadoc
	@Override
	public Crossword parseCrosswordSource(String schema,String definitions)
		throws SyntacticException,SemanticException,ParsingException {
		
		InputStream schemaStringInput = null, defsStringInput = null;
		try{ 
			try {
				schemaStringInput = new ByteArrayInputStream(schema.getBytes(Configuration.getInstance().getEncoding()));
				defsStringInput = new ByteArrayInputStream(definitions.getBytes(Configuration.getInstance().getEncoding()));
			} catch (UnsupportedEncodingException e) {
				log.fatal("Encoding unsupported",e);
				System.exit(-15);
			}
			return parseCrosswordSource(schemaStringInput, defsStringInput);
		} finally{
			try{
				schemaStringInput.close();
				defsStringInput.close();
			} catch (IOException e){
				log.warn("Unable to close strin streams", e);
			}
		}
	}




	//for documentation take a look at  CrosswordParser's javadoc
	@Override
	public Crossword parseCrosswordSource(InputStream schema,InputStream definitions) 
		throws SyntacticException,SemanticException,ParsingException {
		
		ArrayList<Term> stokens = generateTokens(schema);
		ArrayList<Term> dtokens = generateTokens(definitions);
		Term parseTree = buildParseTree(stokens,dtokens);
		
		return buildCwdInstance(parseTree);
		
	}





	/**
	 * Creates a Crossroads tokenizer and generates tokens (i.e. atoms) to pass to the prolog engine for the actual parsing
	 * @param toScan the stream to generate tokens from
	 * @return a list of tuProlog Terms (i.e. prolog atoms) to pass to the prolog parsing engine 
	 * @throws ParsingException if the scanner cannot parse the given input stream
	 */
	protected ArrayList<Term> generateTokens(InputStream toScan) throws ParsingException {
		CrossroadsScanner scanner = new CrossroadsScanner(toScan);

		
		ArrayList<Term> tokens = new ArrayList<Term>();
		while(scanner.hasNext()){
				tokens.add(Term.createTerm("'" + scanner.next() +"'"));

		}

		return tokens;
	}





	/**
	 * This function does the actual call to prolog engines to parse (not concurrently for now) both crossword schema and definitions file.
	 * @param schemaTokens a list of atoms representing tokens (from schema file) to pass to the engine. See generateTokens() to see how this tokens should be created
	 * @param defsTokens a list of atoms representing tokens  (from defintions file) to pass to the engine. See generateTokens() to see how this tokens should be created
	 * @return a term representing the prolog parse tree for the tokens from the crossword description files
	 * @throws SyntacticException if the parsing of the two files encounters some syntactic error (currently i.e. if the prolog parsing fails :) )
	 * @throws ParsingException if some kind of parsing exception happens on the prolog side
	 */
	protected Term buildParseTree(ArrayList<Term> schemaTokens,ArrayList<Term> defsTokens)
			throws SyntacticException,ParsingException {
		Term parseTree = null;
	
		
		Term schemaPT = parseSchemaTokens(schemaTokens);
		Term defsPT = parseDefsTokens(defsTokens);
		
		parseTree = new Struct("crossword",schemaPT,defsPT);
		return parseTree;
	}





	private Term parseDefsTokens(ArrayList<Term> defsTokens)
			throws ParsingException, SyntacticException {
		Term defsPT = null;
		Struct tokenList;
		SolveInfo solution;
		tokenList = new Struct(defsTokens.toArray(new Term[defsTokens.size()]));
		Struct defsGoal = new Struct("phrase",Term.createTerm("definitions_file(DefsParseTree)"),tokenList,Term.createTerm("[]"));
		
		log.debug("Parsing definitions file.");
		solution = getParserEngine().solve(defsGoal);
		try {
			defsPT = solution.getTerm("DefsParseTree");
		} catch (NoSolutionException e) {
			log.warn("Parsing error: errors in input files.",e);
			throw new SyntacticException("Syntactic error in definitions file.");
		} catch (UnknownVarException e) {
			log.fatal("Fatal error: ParseTree var was unbound",e);
			throw new SyntacticException("Something was wrong while parsing defintions: the parse tree was unbound");
		}
		log.debug("Done parsing definitions file.");
		return defsPT;
	}





	private Term parseSchemaTokens(ArrayList<Term> schemaTokens)
			throws SyntacticException {
		Term schemaPT = null;
		//Creates a term representing a prolog list of tokens
		Struct tokenList = new Struct(schemaTokens.toArray(new Term[schemaTokens.size()]));
		Struct schemaGoal = new Struct("phrase",Term.createTerm("schema_file(SchemaParseTree)"),tokenList,Term.createTerm("[]"));

		log.debug("Parsing schema file.");
		SolveInfo solution = getParserEngine().solve(schemaGoal);
		try {
			schemaPT = solution.getTerm("SchemaParseTree");
		} catch (NoSolutionException e) {
			log.warn("Parsing error: errors in input files.",e);
			throw new SyntacticException("Syntactic error in schema file.");
		} catch (UnknownVarException e) {
			log.warn("Fatal error: ParseTree var was unbound",e);
			throw new SyntacticException("Something was wrong while parsing the schema: the parse tree was unbound");
		}
		log.debug("Done parsing schema file.");
		return schemaPT;
	}



	


	/**
	 * Generates a crossword instance given the prolog Term representing its APT
	 * @param parseTree a prolog term representing the parse tree for the crossword instance
	 * @return an istance of Crossword generated according to the parse tree
	 * @throws SyntacticException if a syntactic parse error occurred
	 * @throws SemanticException if a semantic parse error occured
	 * @throws ParsingException if another kinf of parsing erro occurrs
	 */
	public Crossword buildCwdInstance(Term parseTree) throws SyntacticException,SemanticException,ParsingException {
		//Using prolog unification facilietes to retrievs necessary parameters from the parse tree
		Struct paramGoal = new Struct("crossword_parameters",parseTree,Term.createTerm(Configuration.getInstance().getLanguage()),Term.createTerm("Id"),
				Term.createTerm("Size"),Term.createTerm("HolesList"),Term.createTerm("ADefs"),Term.createTerm("DDefs"));
		SolveInfo parametersBindings = getParserEngine().solve(paramGoal);
		
		Struct size = null;
		Term id = null;
		Struct holes = null;
		Struct across = null;
		Struct down = null;
		try {
			size = (Struct)parametersBindings.getVarValue("Size");
			id = parametersBindings.getVarValue("Id");
			holes = (Struct)parametersBindings.getVarValue("HolesList");
			across = (Struct)parametersBindings.getVarValue("ADefs");
			down = (Struct)parametersBindings.getVarValue("DDefs");
		} catch(NoSolutionException e){
			log.fatal("No solution found for binding parameters. This may be due a wrong parse tree :(",e);
			throw new SyntacticException("Cannot bind to PT parameters. Wrong PT?");
		}catch (ClassCastException e) {
			log.fatal("Invalid cast from Term to struct?",e);
			System.exit(-5);
		}
		
		if(size.getArity() != 2 || !size.getName().equals("size"))
			throw new SyntacticException("Illegal arity for size term");
		
		int nRows = ((Int)size.getArg(0).getTerm()).intValue();
		int nCols = ((Int)size.getArg(1).getTerm()).intValue();
		
		String identifier = id.toString();
		HashSet<Point> holesSet = extractHolesMap(holes);		
		ArrayList<String> acrDefs = extractDefinitions(across);
		ArrayList<String> dwnDefs = extractDefinitions(down);
		
		
		Crossword instance = null;
		try {
			instance = new Crossword(identifier,nRows,nCols,holesSet,acrDefs,dwnDefs,Configuration.getInstance().getLanguage());
		} catch (CrosswordException e) {
			log.fatal("Unable to instantiate crossword",e);
			throw new SemanticException("Inconsistent crossword parameters: " + e.getMessage(),e);
		}

		
		
		
		return instance;
	}




	/**
	 * Given a prolog Term of the form [point(x1,y1),...,point(xN,yN)] generates a set containing instances
	 * of java.awt.Point representing those points
	 * @param holes a alice.tuprolog.Struct representing a list of point terms.
	 * @return an HashSet of java.awt.Point instances representing the list passed as input.
	 * @throws ParsingException if the list passed as input has something wrong
	 */
	@SuppressWarnings("unchecked")
	private HashSet<Point> extractHolesMap(Struct holes) throws ParsingException {
		if(!holes.isList())
			throw new ParsingException("Invalid holes structure in ParseTree");
		HashSet<Point> holesSet = new HashSet<Point>(); 
		Iterator<Term> it = holes.listIterator();
		while(it.hasNext()){
			Term t = it.next();
			if(!(t instanceof Struct))
			{
				log.fatal("there should be a struct describing struct but it is not");
				
			}
			Struct point = (Struct)t;
			if(point.getArity() !=2 && !point.getName().equals("point"))
				throw new ParsingException("Invalid holes structure in ParseTree");
			Point jp = new Point(((Int)point.getArg(0).getTerm()).intValue(),((Int)point.getArg(1).getTerm()).intValue());
			if(!holesSet.add(jp))
				log.warn("HolesSet already contains a point " +jp);
		}
		return holesSet;
	}


	/**
	 * Given a prolog term like ['definition 1', ..., 'definition N'] transforms the list into
	 * a list of strings each representing one of the atoms in the original list
	 * @param defList a prolog Struct (i.e. compund term) that is a list of ATOMS 
	 * @return an ArrayList of string representing the atoms in the input list
	 * @throws ParsingException if the passed term is not in the desired form
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String> extractDefinitions(Struct defList)
			throws ParsingException {
		ArrayList<String> list = new ArrayList<String>();
		if(!defList.isList())
			throw new ParsingException("Invalid across defs structure in ParseTree");
		Iterator<Term> it = defList.listIterator();
		while(it.hasNext()){
			Term def = it.next();
			if(!def.isAtomic())
				throw new ParsingException("Not an atomic definitions");
			Matcher mtch = Pattern.compile("^'(.*)'$").matcher(def.toString());
			if(mtch.matches())
				list.add(mtch.group(1));
			else list.add(def.toString());
		}
		
		return list;
	}
	


}





