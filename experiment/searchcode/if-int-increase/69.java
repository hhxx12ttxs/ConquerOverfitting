package edu.vub.at.doc.tmpl;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import edu.vub.at.doc.elem.*;
import edu.vub.at.doc.sub.ConceptType;
import edu.vub.at.doc.sub.InlineLink;
import edu.vub.at.doc.sub.ParameterDoc;
import edu.vub.at.doc.sub.ReturnDoc;
import edu.vub.at.doc.sub.SeeDoc;
import edu.vub.at.doc.sub.ThrowDoc;

/**
 * TemplateEngine utilizes StringTemplates created by Terrence Parr.
 * This class handles setting up all data so that it can be used in
 * StringTemplates. Which template system is used is hidden behind
 * the public API. Individual methods can be called as well to document
 * only parts of the code. These methods are also needed in the
 * templates themselves.
 * 
 * @author bcorne
 */
public class TemplateEngine {
	
	// SETTINGS
	/** The path to the directory containing .st template files */
	private File templateDir;
	
	/** The translator to be used wherever Markdown is allowed */
	private MarkdownTranslator markdown;
	
	/** The current StringTemplates to be used */
	private StringTemplateGroup templates;
	
	// STATE USED BY TEMPLATES
	
	/** Different type of methods, depending on the context */
	private enum ContextState {
		OBJECT, // methods
		ACTOR,  // messages
		FREE;	// Nowhere specific
		/** the current type of the method */
		public static ContextState state = FREE;		
	}
	
	/** This field can be used for id's inside templates. */
	private int id = 0;
	
	/**
	 * This field is used for keeping track of sensible concept
	 * names for lower nested concepts
	 **/
	private String canonical = "";

	/** The type of the current canonical name */
	private ConceptType canonicalType = ConceptType.ANY; 
	
	/** Append the name of the given concept to the canonical concept name */
	private void extendCanonical(ATDocGeneral g, ConceptType c) {
		canonical = canonical+"/"+g.name.getName();
		canonicalType = c;
	}
	
	/**
	 * Data class for canonical concepts providing their name and the
	 * ID used in the template in which it's documentation has been rendered.
	 * The getters are used by templates.
	 **/
	private class CanonicalConcept {
		private int id;
		private String name;
		public CanonicalConcept(int id, String name) {
			this.setId(id);
			if(name.length() > 1) // Cut off prefixed '/'
				name = name.substring(1);
			this.setName(name);		
		}
		public void setName(String name) {
			this.name = name;
		}
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
		public void setId(int id) {
			this.id = id;
		}
		@SuppressWarnings("unused")
		public int getId() {
			return id;
		}
	}
	
	/** Groups canonical concepts by ConceptType */
	private Map<ConceptType, LinkedList<CanonicalConcept>> concepts =
		new HashMap<ConceptType, LinkedList<CanonicalConcept>>();
	
	private void pushCanonicalConcept() {
		LinkedList<CanonicalConcept> type = concepts.get(canonicalType);
		if(type == null) {
			type = new LinkedList<TemplateEngine.CanonicalConcept>();
			concepts.put(canonicalType,type);
		}
		type.addLast(new CanonicalConcept(id, canonical));
	}
		
	// STATIC NAMES
	/** The names of the string templates (st = string template)*/
	private final static String
		stBegin 		= "begin",
		stFile			= "file",
		stVariable		= "variable",
		stClosure		= "closure",
		stParam			= "parameter",
		stReturn		= "return",
		stThrow			= "throw",
		stType			= "type",
		stObject		= "object",
		stActor			= "actor",
		stIsolate		= "isolate",
		stMirage		= "mirage",
		stMirror		= "mirror",
		stModule		= "module",
		stInline		= "inline",
		stGeneral		= "general";
	
	/** The extension of StringTemplate template files */
	private final static String
		templateExtension	= ".st";
	
	
	/** The names of the attributes (sta = string template attribute) */
	public final static String
	
		// General attributes
		staGeneral		= "general",
		staName			= "name",
		staAuthors		= "authors",
		staSee			= "see",
		staJavaSee		= "javasee",
		staDoc			= "doc",
		
		// Specific attributes
		staBegin		= "begin",
		staModule		= "module",		
		
		staVariables	= "variables",
		
		staClosures		= "closures",
		staParams		= "params",		staParamsFull	= "paramsfull",
		staReturns		= "returns",	staReturnsFull	= "returnsfull",
		staThrows		= "throws",		staThrowsFull	= "throwsfull",
		staInObject		= "inObject",	staInActor		= "inActor",
		
		staTypes		= "types",
		staType			= "type",
		staParents		= "parents",
		
		staObjects		= "objects",
		
		staActors		= "actors",
		
		staIsolates		= "isolates",
		
		staMirages		= "mirages",
		
		staMirrors		= "mirrors",
		staMirage		= "mirage",
		
		staInlineText	= "text",
		staInlineObject	= "object",
		staInlineLink	= "link",
		staInlineType	= "type",
		staInlineNative	= "native",
		
		// References to (parts of) the current executing engine
		staTE			= "te", // current TemplateEngine reference
		staID			= "id", // The id unique to each template rendering
		staMD			= "md"; // current MarkdownTranslator reference
	
	/**
	 * Give the templates access to the some parts of the template engine. 
	 */
	private void setEngineAttrs(StringTemplate st) {
		// Up the id whenever setting engine attributes which happens
		// every time a sub-template is rendered. This ensures the
		// uniqueness of ID per sub-template rendering.
		id++;

		// add the current canonical name to its group.
		pushCanonicalConcept();
		
		st.setAttribute(staID,			id);
		st.setAttribute(staMD,			markdown);
		st.setAttribute(staTE,			this);
	}
	
	/** Retrieve an instance for the given StringTemplate name */
	private StringTemplate getST(String st) {
		return templates.getInstanceOf(st);		
	}

	/**
	 * Pass a StringTemplate the attributes from a general documentation object.
	 * Also runs the template for general information like authors, docstring and
	 * related concepts. The general sub-template has the same ID as the template
	 * rendering this general instance.
	 */
	private void setGeneralAttrs(ATDocGeneral g, StringTemplate st) {
		
		// Run the general information template without raising unique ID
		StringTemplate stg = getST(stGeneral);
		stg.setAttribute(staID,				id);
		
		stg.setAttribute(	staDoc,			g.docstring.getDoc());
		for(String author : g.authors.getNames())
			stg.setAttribute(staAuthors,	author);
		for(SeeDoc see : g.see)
			stg.setAttribute(staSee,		runInline(see.getConcept()));
		for(SeeDoc see : g.javaSee)
			stg.setAttribute(staJavaSee,	runInline(see.getConcept()));

		// allow the collection of closures to know which kind they are
		setContextState(st);
		
		// Pass regular arguments
		setEngineAttrs(st);
		st.setAttribute(staName,			g.name.name);
		st.setAttribute(staGeneral,			stg.toString());
	}

	/** Pass a StringTemplate the attributes from a file documentation object */
	private void setFileAttrs(ATDocFile f, StringTemplate st) {
		setGeneralAttrs(f, st);
		st.setAttribute(staModule,		f.isModule);
		// Present the file documentation itself as the
		// documentation on the begin it represents
		st.setAttribute(staBegin,		runBegin(f));
		// Everything has been documented now. We now have a list canonical
		// names of documented concepts grouped per concept type.
		setFileConceptGroup(st, ConceptType.VARIABLE,	staVariables);
		setFileConceptGroup(st, ConceptType.CLOSURE,	staClosures);
		setFileConceptGroup(st, ConceptType.TYPE,		staTypes);
		setFileConceptGroup(st, ConceptType.OBJECT,		staObjects);
		setFileConceptGroup(st, ConceptType.ACTOR,		staActors);
		setFileConceptGroup(st, ConceptType.ISOLATE,	staIsolates);
		setFileConceptGroup(st, ConceptType.MIRAGE,		staMirages);
		setFileConceptGroup(st, ConceptType.MIRROR,		staMirrors);
	}
	
	private void setFileConceptGroup(StringTemplate st, ConceptType ct, String staKey) {
		LinkedList<CanonicalConcept> list = concepts.get(ct);
		// Add all lists that have inhabitants
		if(list != null) for(CanonicalConcept cc : list)
			st.setAttribute(staKey, cc);
	}

	/** Pass a StringTemplate the attributes from an inline link */
	private void setInlineAttrs(InlineLink i, StringTemplate st) {
		st.setAttribute(staInlineText,i.getText());
		switch(i.getType()) {
		case LINK	: st.setAttribute(staInlineLink,	true);break;
		case OBJECT	: st.setAttribute(staInlineObject,	true);break;
		case TYPE	: st.setAttribute(staInlineType,	true);break;
		case NATIVE	: st.setAttribute(staInlineNative,	true);break;
		}
	}
	
	private void setContextState(StringTemplate st) {
		switch(ContextState.state) {
		case ACTOR: st.setAttribute(staInActor, true);
		case OBJECT: st.setAttribute(staInObject, true);break;
		}
	}
	
	/** Pass a StringTemplate the attributes from a begin documentation object */
	private void setBeginAttrs(ATDocBegin b, StringTemplate st) {
		setGeneralAttrs(b, st);
		// add documented concepts to their respective concept type collection
		for(ATDocVariable v : b.variables)
			st.setAttribute( staVariables,	runVariable(v));
		for(ATDocClosure c : b.closures)
			st.setAttribute( staClosures,	runClosure(c));
		for(ATDocType t : b.types)
			st.setAttribute( staTypes,		runType(t));
		for(ATDocObject o : b.objects)
			st.setAttribute( staObjects,	runObject(o));
		for(ATDocActor a : b.actors)
			st.setAttribute( staActors,		runActor(a));
		for(ATDocIsolate i : b.isolates)
			st.setAttribute( staIsolates,	runIsolate(i));
		for(ATDocMirage m  : b.mirages)
			st.setAttribute( staMirages,	runMirage(m));
		for(ATDocMirror m : b.mirrors)
			st.setAttribute( staMirrors,	runMirror(m));
	}

	/** Pass a StringTemplate the attributes from a variable documentation object */
	private void setVariableAttrs(ATDocVariable v, StringTemplate st) {
		setGeneralAttrs(v, st);
		for(InlineLink link : v.types)
			st.setAttribute(staTypes,		runInline(link));
	}
	
	/** Pass a StringTemplate the attributes from a closure documentation object */
	private void setClosureAttrs(ATDocClosure c, StringTemplate st) {
		setGeneralAttrs(c, st);
		// Extract all names a method-definition look
		for(ParameterDoc p : c.parameters) {
			st.setAttribute(staParams,		p.getName());
			st.setAttribute(staParamsFull,	runParam(p));
		}
		for(ReturnDoc r : c.returns) {
			st.setAttribute(staReturns,		r.getConcept().getText());
			st.setAttribute(staReturnsFull, runReturn(r));
		}
		for(ThrowDoc t : c.throwings) {
			st.setAttribute(staThrows,		t.getReference().getText());
			st.setAttribute(staThrowsFull,	runThrows(t));
		}
	}
	
	/**
	 * Pass a StringTemplate the attributes from a parameter documentation object.
	 * Note: this template does not increase the unique ID as it is a subtemplate
	 * used by the closure template. 
	 **/
	private void setParamAttrs(ParameterDoc p, StringTemplate st) {
		st.setAttribute(staName,	p.getName());
		st.setAttribute(staDoc,		p.getDoc());
		String type = runInline(p.getType());
		if(type.length() != 0)
			st.setAttribute(staType,type);
	}
	
	/**
	 * Pass a StringTemplate the attributes from a return documentation object.
	 * Note: this template does not increase the unique ID as it is a subtemplate
	 * used by the closure template.
	 **/
	private void setReturnAttrs(ReturnDoc r, StringTemplate st) {
		st.setAttribute(staType,	runInline(r.getConcept()));
		st.setAttribute(staDoc,		r.getDoc());
	}
	
	/**
	 * Pass a StringTemplate the attributes from a throws documentation object.
	 * Note: this template does not increase the unique ID as it is a subtemplate
	 * used by the closure template. 
	 **/
	private void setThrowsAttrs(ThrowDoc t, StringTemplate st) {
		st.setAttribute(staType,	runInline(t.getReference()));
		st.setAttribute(staDoc,		t.getDoc());
	}
	
	/** Pass a StringTemplate the attributes from a type documentation object */
	private void setTypeAttrs(ATDocType t, StringTemplate st) {
		setGeneralAttrs(t, st);
		// Create an inline link for every parent's name
		for(ATDocType parent : t.parents) {
			InlineLink link = new InlineLink();
			link.setText(parent.name.name);
			link.setType(ConceptType.TYPE);
			st.setAttribute(staParents,		runInline(link));
		}
	}
	
	/** Pass a StringTemplate the attributes from an object documentation object */
	private void setObjectAttrs(ATDocObject o, StringTemplate st) {
		// Let closures know they are in the object context
		ContextState.state = ContextState.OBJECT;
		setGeneralAttrs(o, st);
		st.setAttribute(staTypes,		o.types);
		// insert the hierarchy
		ATDocObject parent = o.extendsFrom;
		while(parent != null) {
			InlineLink il = new InlineLink();
			il.setText(parent.name.name);
			il.setType(ConceptType.OBJECT);
			st.setAttribute(staParents,	runInline(il));
			parent = parent.extendsFrom;
		}
		st.setAttribute(staBegin,		runBegin(o));
	}
	
	/** Pass a StringTemplate the attributes from an actor documentation object */
	private void setActorAttrs(ATDocActor a, StringTemplate st) {
		setObjectAttrs(a, st);
		// Let closures know they are in the actor context, overriding the earlier
		// stated object context.
		ContextState.state = ContextState.ACTOR;
		
	}
	
	/** Pass a StringTemplate the attributes from an isolate documentation object */
	private void setIsolateAttrs(ATDocIsolate i, StringTemplate st) {
		setObjectAttrs(i, st);
	}
	
	/** Pass a StringTemplate the attributes from a mirage documentation object */
	private void setMirageAttrs(ATDocMirage m, StringTemplate st) {
		setObjectAttrs(m, st);
		// Add all mirrors
		for(ATDocObject mirror : m.mirrors) {
			InlineLink link = new InlineLink();
			link.setType(ConceptType.MIRROR);
			link.setText(mirror.name.getName());
			st.setAttribute(staMirrors, runInline(link));
		}
	}
	
	/** Pass a StringTemplate the attributes from a mirror documentation object */
	private void setMirrorAttrs(ATDocMirror m, StringTemplate st) {
		setObjectAttrs(m, st);
		InlineLink link = new InlineLink();
		// Add the object miraging this mirror
		link.setType(ConceptType.MIRAGE);
		link.setText(m.mirage.name.getName());
		st.setAttribute(staMirage, runInline(link));
	}
	
	// --------------------------- //
	// -- API used by templates -- //
	// --------------------------- //
	
	/**
	 * Render the templates for all sets of concepts at this
	 * nesting level by launching the appropriate run methods
	 * for each element of each set. 
	 * @return The template with attributes filled in.
	 */
	public String runBegin(ATDocBegin b) {
		StringTemplate st = getST(stBegin);
		setBeginAttrs(b, st);
		return st.toString();
	}
	
	/**
	 * Render the template for documenting
	 * @return
	 */
	public String runInline(InlineLink l) {
		StringTemplate st = getST(stInline);
		setInlineAttrs(l, st);
		return st.toString();
	}
	
	/** Render the template for documenting a file */
	public String runFile(ATDocFile f) {
		StringTemplate st = getST(stFile);
		setFileAttrs(f, st);
		return st.toString();
	}
	
	/** Render the template for documenting a variable */
	public String runVariable(ATDocVariable v) {
		String oldCanonical = canonical;
		extendCanonical(v, ConceptType.VARIABLE);
		StringTemplate st = getST(stVariable);
		setVariableAttrs(v, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}
	
	/** Render the template for documenting a closure */
	public String runClosure(ATDocClosure c) {
		String oldCanonical = canonical;
		extendCanonical(c, ConceptType.CLOSURE);
		StringTemplate st = getST(stClosure);
		setClosureAttrs(c, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}
	
	/** Render the template for a parameter from a closure. */
	public String runParam(ParameterDoc p) {
		StringTemplate st = getST(stParam);
		setParamAttrs(p, st);
		return st.toString();
	}
	
	/** Render the template for a return type from a closure. */
	public String runReturn(ReturnDoc r) {
		StringTemplate st = getST(stReturn);
		setReturnAttrs(r, st);
		return st.toString();
	}
	
	/** Render the template for a throws declaration from a closure. */
	public String runThrows(ThrowDoc t) {
		StringTemplate st = getST(stThrow);
		setThrowsAttrs(t, st);
		return st.toString();
	}
	
	/** Render the template for documenting a type */
	public String runType(ATDocType t) {
		String oldCanonical = canonical;
		StringTemplate st = getST(stType);
		extendCanonical(t, ConceptType.TYPE);
		setTypeAttrs(t, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}

	/** Render the template for documenting an object */
	public String runObject(ATDocObject o) {
		// Store the current canonical name to restore later
		String oldCanonical = canonical;
		extendCanonical(o, ConceptType.OBJECT);
		
		StringTemplate st = getST(stObject);
		setObjectAttrs(o, st);
		String result = st.toString();
		
		// Leave nested scope and restore the concept name
		canonical = oldCanonical;
		
		return result;
	}
	
	/** Render the template for documenting an actor */
	public String runActor(ATDocActor a) {
		String oldCanonical = canonical;
		extendCanonical(a, ConceptType.ACTOR);
		StringTemplate st = getST(stActor);
		setActorAttrs(a, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}
	
	/** Render the template for documenting an isolate */
	public String runIsolate(ATDocIsolate i) {
		String oldCanonical = canonical;
		extendCanonical(i, ConceptType.ISOLATE);
		StringTemplate st = getST(stIsolate);
		setIsolateAttrs(i, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}
	
	/** Render the template for documenting a mirage */
	public String runMirage(ATDocMirage m) {
		String oldCanonical = canonical;
		extendCanonical(m, ConceptType.MIRAGE);
		StringTemplate st = getST(stMirage);
		setMirageAttrs(m, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}
	
	/** Render the template for documenting a mirror */
	public String runMirror(ATDocMirror m) {
		String oldCanonical = canonical;
		extendCanonical(m, ConceptType.MIRAGE);
		StringTemplate st = getST(stMirror);
		setMirrorAttrs(m, st);
		String result = st.toString();
		canonical = oldCanonical;
		return result;
	}
	
	// ---------------- //
	// -- Public API -- //
	// ---------------- //
	/**
	 * Initializes the templating engine for the given template with the
	 * {@link DoNothingMarkdownTranslator} as markdown processor.
	 * @param templatePath The path to the template to be used.
	 * @throws InvalidTemplate When the given path contains an invalid template.
	 */
	public TemplateEngine(String templatePath)
	throws InvalidTemplate {
		
		this(templatePath, new DoNothingMarkdownTranslator());
				
	}
	
	/**
	 * Initializes the templating engine for the given template.
	 * @param templatePath The path to the template to be used.
	 * @param markdown The MarkdownTranslator to be used.
	 * possible.
	 * @throws InvalidTemplate When the given path contains an invalid template.
	 */
	public TemplateEngine(String templatePath, MarkdownTranslator markdown)
	throws InvalidTemplate {

		this.markdown = markdown;
		this.templateDir = new File(templatePath);
		checkTemplate(this.templateDir);
		templates = new StringTemplateGroup(templateDir.getName(),templateDir.getAbsolutePath());
		
	}
	
	/**
	 * Run the template engine starting from the root and write
	 * documentation to an output stream.
	 * @param root An ATDocFile from where to start documenting.
	 */
	public String run(ATDocFile root) {
		
		return runFile(root);
		
	}	
	
	/**
	 * Check if a given template contains all necessary files. Returns
	 * nothing if the template is valid.
	 * @param templateDir The folder in which to find the files.
	 * @throws InvalidTemplate when there is a technical issue or a
	 * problem with the template's structure.
	 */
	public static void checkTemplate(File templateDir) throws InvalidTemplate {
		
		if(!templateDir.isDirectory())
			throw new InvalidTemplate(
				templateDir +" is not a directory.");
		
		if(!templateDir.canRead())
			throw new InvalidTemplate(
				"Unable to read template directory (check permissions.");
		
		// Check whether every template is present, a file and readable
		String[] templates = {
				stGeneral, stInline,
				stModule, stBegin,stFile,stVariable,stClosure, stType,
		        stObject,stActor,stIsolate,stMirage,stMirror,
		        stParam, stReturn, stThrow,
		};
		for(String t : templates ) {
			File template = new File(
				templateDir.getAbsolutePath()+File.separator+t+templateExtension);
			if(!template.exists())
				throw new InvalidTemplate("Template "+t+" doesn't exist.");
			if(!template.isFile())
				throw new InvalidTemplate("Template "+t+" is not a file.");
			if(!template.canRead())
				throw new InvalidTemplate("Template "+t+" could not be read.");
		}
	}

}

