/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)JGen.java 
 *
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.stc.jcs.ssc;

import java.util.*;
import java.io.*;

import com.sun.stc.jcs.JCSCompiler;
import com.sun.stc.jcs.ssc.ClassTree.PathNode;
import com.sun.stc.jcs.ssc.ParseXsc.NodeUser;
import com.sun.stc.jcsre.IStringCoder;
import com.sun.stc.jcsre.JCSProperties;
import com.sun.stc.jcsre.StringCoderFactory;

/**
 * Class to generate Java code for SSC-based ETD.
 * Part of the e*Gate 4.5.1 JCS.
 */
public class JGen extends Emit
{
    // Values for marshalCode flag.
    static final byte MONK = 0; // go through Monk and the Mocca library
    static final byte JAVA = 1; // use native marshaling in Java only
    static final byte BOTH = 2; // do both Monk and native, then compare

    // Prefixes for generated field-specific variables.
    static private final String bform = "bform"; // prefix for boolean formatter
    static private final String nform = "nform"; // prefix for numeric formatter
    static private final String bmask = "bmask"; // prefix for presence bit-mask
    static private final String bminx = "index"; // prefix for presence index

    /**
     * Option to assume ANTLR groks Unicode names.
     * The standard ANTLR version only accepts ASCII for rule names and
     * token names, which forces us to escape non-ASCII characters in names.
     * Defaults to the boolean JCS property "JGen.untlr", else "true".
     */
    static final boolean untlr =
	JCSProperties.getFlag("JGen.untlr", true);

    /**
     * Option to generate code in each node class for the generic set() method.
     * This is needed for ANTLR-less parsing (qv).
     * Defaults to the boolean JCS property "JGen.genSet", else "true".
     */
    static boolean genSet =
	JCSProperties.getFlag("JGen.genSet", true);

    /**
     * Option to emit code for setting array nodes via array.
     * Defaults to the boolean JCS property "JGen.genArrayGetSet", else "false".
     */
    public static boolean arrayGetSet =
	JCSProperties.getFlag("JGen.arrayGetSet", false);

    /**
     * Option to generate unmarshalling code that does not use ANTLR,
     * the yacc-like grammar generator.  In e*Gate 4.5 and 4.5.1, after
     * using a Monk engine instance to do the actual event parsing, we
     * use an ANTLR-generated parser to consume the unmarshaling token
     * stream from the Mocca JNI layer between Monk/C and Java.
     * In e*Gate 4.5.2, we can alternatively use the generic set()
     * methods in combination with the com.sun.stc.jcsre.ssc.Parse class.
     * Defaults to the boolean JCS property "JGen.antlrLess", else "true".
     */
    public static boolean antlrLess =
	JCSProperties.getFlag("JGen.antlrLess", true);

    /**
     * The name of the ANTLR grammar-generator package.
     * Used to override this when we need an alternate version.
     * Defaults to the string JCS property "JGen.antlr", else "antlr".
     */
    public static String antlr =
	JCSProperties.getProperty("JGen.antlr", "antlr");

    /**
     * Option to use class nesting, not node nesting, to determine the
     * order in which code is generated.  In e*Gate 4.5 and 4.5.1, node
     * nesting and class nesting are identical; in 4.5.2, they may differ
     * to accommodate deep nesting and such.
     * Defaults to the boolean JCS property "JGen.cnest", else false.
     */
    public static boolean cnest =
	JCSProperties.getFlag("JGen.cnest", false);

    /**
     * Option to generate ETD tester support code.
     * Defaults to the boolean JCS property "JGen.genXml", else true.
     */
    public static boolean genXml = JCSProperties.getFlag("JGen.genXml", true);

    /**
     * Option to find the SSC file at run-time by asking the collab controller.
     * Defaults to the boolean JCS property "JGen.genXml", else true.
     */
    public static boolean sscFromCollab =
	JCSProperties.getFlag("JGen.sscFromCollab", true);

    /**
     * Option to use encoding-specific padding.
     * Defaults to the boolean JCS property "JGen.codepad", else false.
     */
    public static boolean codepad =
	JCSProperties.getFlag("JGen.codepad", true);

    /**
     * Option for XML dump code, to wrap repeated nodes in extra &lt;data&gt;
     * tag without the index or values.
     * Defaults to the boolean JCS property "JGen.xmlWrap", else true.
     */
    public static boolean xwrap =
	JCSProperties.getFlag("JGen.xmlWrap", true);

    /**
     * Option to make parsing more verbose.
     * Defaults to the boolean JCS property "JGen.verboseSet", else false.
     */
    public static boolean verboseSet =
	JCSProperties.getFlag("JGen.verboseSet", false);

    /**
     * Option to implement code version checking.
     * Defaults to the boolean JCS property "JGen.codeVersion", else true.
     */
    public boolean doCodeVersion =
	JCSProperties.getFlag("JGen.codeVersion", false);

    /**
     * Option to generate byte constants as ASCII chars when possible.
     * This is to make the generated source code more legible, though
     * potentially less platform-independent.
     * Defaults to the boolean JCS property "JGen.byteChar", else true.
     */
    public static boolean byteAsChar =
	JCSProperties.getFlag("JGen.byteChar", true);

    public boolean debug = false;
    public byte marshalCode = JAVA;

    private static final int MASK_LEN = 32; // sizeOf(int)

    // name | boxed-value |initializer| accessor
    private final String[][] primitiveArray = {
        {"boolean", "java.lang.Boolean", "new java.lang.Boolean(false)", ".booleanValue()"}
        ,{"byte", "java.lang.Byte", "new java.lang.Byte((byte)0)", ".byteValue()"}
        ,{"char", "java.lang.Character", "new java.lang.Character('\\0')", ".charValue()"}
        ,{"short", "java.lang.Short",  "new java.lang.Short((short)0)", ".shortValue()"}
        ,{"int", "java.lang.Integer", "new java.lang.Integer(0)", ".intValue()"}
        ,{"long", "java.lang.Long", "new java.lang.Long(0L)", ".longValue()"}
        ,{"float", "java.lang.Float", "new java.lang.Float(0)", ".floatValue()"}
        ,{"double", "java.lang.Double", "new java.lang.Double(0)", ".doubleValue()"}
        ,{"byte[]", "byte[]", "new byte[0]", ""}
        ,{"char[]", "char[]", "new byte[0]", ""}
        ,{"java.lang.String", "java.lang.String", "new java.lang.String()", ".toString()"}
    };

    private String outputPkg;
    private String outputDir;
    private SSC_FileSet fileSet;
    private ClassTree ctree;

    private Set subRules = new HashSet();

    private int valueCount;
    private int iCount;
    private PrintWriter _antlrpw;
    private int subRuleCount = 0;

    /**
     * Constructs a new code generation context. Builds from an ETD file
     * closure (the main XSC files plus the external XSC template files),
     * a class tree derived from this, an output directory for generated
     * files, and an optional output package for the main ETD class.
     *
     * @param fileSet  the ETD XSC file closure
     * @param ctree  the derived class tree
     * @param outputDir  root of generated files
     * @param outputPkg  package of main class
     */
    public JGen (SSC_FileSet fileSet, ClassTree ctree, String outputDir,
	String outputPkg)
    {
	super(null);

	// Determine kind of marshaling.
	String marshal = JCSProperties.getProperty("JGen.marshal");
	if (marshal == null)
	    marshal = "java";
	if (marshal.equals("monk"))
	    marshalCode = JGen.MONK;
	else if (marshal.equals("java"))
	    marshalCode = JGen.JAVA;
	else if (marshal.equals("both"))
	    marshalCode = JGen.BOTH;
	else
	    throw new RuntimeException("illegal \"JGen.marshal\" value ["
		+ marshal + "]");

	if (debug)
	{
	    System.out.println("JGen: outputDir = " + outputDir);
	    System.out.println("JGen: marshal = " + marshalCode);
	    System.out.println("JGen: untlr = " + untlr);
	}
	this.fileSet = fileSet;
	this.ctree = ctree;
	this.outputPkg = outputPkg;
	this.outputDir = outputDir;
    }

    /**
     * The mapping of all each file to a set-wide unique ID.
     */
    private Hashtable fileMap = null;

    /**
     * Generates all the code needed to support the set of files
     * passed to the constructor.
     *
     * @throws IOException for output generation problems
     */
    public void gen ()
	throws IOException
    {
	Iterator iter;
	if (debug)
	    for (iter = fileSet.getFiles(); iter.hasNext(); )
		System.out.println("[ gen got <"
		    + ((SSC_File)iter.next()).getXscFileName() + "> ]");
	codeMap = collectEncodings();

	// Map all files to some unique ID.
	fileMap = new Hashtable();
	iter = fileSet.getFiles();
	for (int i = 0; iter.hasNext(); i ++)
	    fileMap.put(iter.next(), "x" + i);

	SSC_File main = fileSet.resolveGlobalTemplate(null);
	if (main == null)
	    throw new RuntimeException("SSC file-set has no main template");
	String mainName = main.getEtd().getJavaName();
	for (iter = fileSet.getFiles(); iter.hasNext(); )
	{
	    SSC_File ssc = (SSC_File) iter.next();
	    if (debug)
		System.out.println("[ "
		    + (ssc.generate ? "make" : "skip")
		    + " main/external <" + ssc.getXscFileName()
		    + ">, codeVersion=" + ssc.getCodeVersion() + " ]");
	    if (ssc.generate)
		genFile(ssc, main == ssc, mainName);
	}
    }

    /**
     * Generate the Java code to implement the structure described by the
     * given SSC-based XSC file.  This does not include the structure storage
     * classes implementation code for external templates referenced by this
     * file.
     *
     * @param file  the internal representation of an SSC-based XSC file
     * @param isMain  flag: is this the main template in the file-set?
     * @param mainName  the Java name of the main template
     * @throws IOException when the generated code file cannot be written
     */
    private void genFile (SSC_File file, boolean isMain, String mainName)
	throws IOException
    {
	String globalName = file.getGlobalName();
	MessageStructureNode etd = file.getEtd();
	String className = etd.getJavaName();
	redent();
	// Make sure coderClass is defined.
	if (isMain && ! codeMap.isEmpty())
	    genCoders(etd.getJavaName(), outputDir, outputPkg);
	// Generate class for the etd.
	if (cnest)
	{
	    // Use the class tree.
	    genClass(ctree.getRoot(), etd.getJavaName());
	}
	else
	{
	    // Assume class nesting follows node nesting.
	    genClass(file, etd, mainName, isMain);
	    // Generate classes for the local templates.
	    String names[] = file.localTemplateNames();
	    for (int i = 0; i < names.length; i++)
		genClass(file, file.resolveLocalTemplate(names[i]),
		    null, isMain);
	    if (isMain && !antlrLess)
		genGrammarFiles(etd.getJavaName(),
		    outputDir, outputPkg, "grammar.g");
	}
    }

    /**
     * Walks the given node and its descendants, to see if there is any node
     * that we have claimed.  We need to known this to determine if we need
     * to generate any code for this subtree.
     *
     * @param pnode  the root path node of the subtree
     * @return the answer
     */
    private boolean isClaimed (PathNode pnode)
    {
	if (pnode == null)
	    return false;
	if (pnode.getUsed())
	{
	    // This node's been claimed somewhere.
	    ClassTree.PathUser user = pnode.getUser();
	    if (user != null && user instanceof NodeUser)
		return true;
	}
	// Check the descendants recursively.
	for (PathNode kid = pnode.getKids(); kid != null; kid = kid.getNext())
	    if (isClaimed(kid))
		return true;
	return false;
    }

    // Dummy...
    private void genCodeFile (PathNode pnode) { /*NYI*/ }

    /**
     * Walks the given class subtree, and generate code files for outermost
     * classes.  We only generate code if the outermost class is either
     * claimed, or contains a claimed inner class.
     *
     * @param pnode  the root path node of the subtree
     */
    private void genTreeFiles (PathNode pnode)
    {
	if (pnode == null) { return; }
	PathNode kid = pnode.getKids();
	switch (pnode.getType())
	{
	case PathNode.THEROOT:
	case PathNode.PACKAGE:
	case PathNode.UNKNOWN:
	    // Nothing to do but dive down.
	    for (; kid != null; kid = kid.getNext())
		genTreeFiles(kid);
	    break;

	case PathNode.INCLASS:
	    // Impossible.
	    throw new RuntimeException ("rotten class tree, unexpected inner ["
		    + pnode.getPath() + "]");

	case PathNode.UPCLASS:
	case PathNode.ISCLASS:
	    // Outermost class; make file.
	    if (isClaimed(pnode))
		genCodeFile(pnode);
	}
    }

    /**
     * Test if node is a reference to a global (external) template.
     *
     * @param node  a non-null structure node reference
     * @return the test result
     */
    public static boolean isGlobalTemplate (MessageStructureNode node)
    {
	switch (node.getNodeType() & node.SOT_MASK)
	{
	case MessageStructureNode.GTS:
	case MessageStructureNode.GTN:
	case MessageStructureNode.GTF:
	    return true;
	}
	return false;
    }

    /**
     * Test if node is a reference to a local (internal) template.
     *
     * @param node  a non-null structure node reference
     * @return the test result
     */
    public static boolean isLocalTemplate (MessageStructureNode node)
    {
	switch (node.getNodeType() & node.SOT_MASK)
	{
	case MessageStructureNode.LTS:
	case MessageStructureNode.LTN:
	case MessageStructureNode.LTF:
	    return true;
	}
	return false;
    }

    /**
     * Given an external reference node in XSC, i.e. something
     * like &lt;node type="REFERENCE" reference="file.xsc" ...&gt;,
     * return the assocaited XSC file of the external description.
     * If not found, throw an exception instead.
     *
     * @param node  the structure node containing the reference
     * @return the referred XSC template file
     */
    public SSC_File resolveGlobalFile (MessageStructureNode node)
    {
	if (! isGlobalTemplate(node))
	    throw new RuntimeException("\"" + node.getNodeName()
		+ "\" is not global");
	String templatePath = node.getRefPath();
	SSC_File file = fileSet.resolveGlobalTemplate(templatePath);
	if (file == null)
	{
	    // External reference broken; should have been caught earlier.
	    throw new RuntimeException(
		"\"" + templatePath + "\" unresolved for global \""
		+ node.getNodeName() + "\"");
	}
	return file;
    }

    /**
     * Given an external reference node in XSC, i.e. something
     * like &lt;node type="REFERENCE" reference="file.xsc" ...&gt;,
     * return the top-level node of the external description.
     * If not found, throw an exception instead.
     *
     * @param node  the structure node containing the reference
     * @return the root node of the referred structure
     */
    private MessageStructureNode resolveGlobalTemplate
	(MessageStructureNode node)
    {
	return resolveGlobalFile(node).getEtd();
    }

    private MessageStructureNode resolveLocalTemplate
	(MessageStructureNode node)
    {
        if (! isLocalTemplate(node))
            throw new RuntimeException("\"" + node.getNodeName()
                + "\" is not local");
        String templateName = node.getLink();
        // HACK!!!
        if (templateName.endsWith("-struct"))
            templateName = templateName.substring(0, templateName.length()-7);
        MessageStructureNode ref =
            node.getFile().resolveLocalTemplate(templateName);
        if (ref == null)
        {
            // Internal reference broken; should have been caught earlier.
            throw new RuntimeException(
                "\"" + templateName + "\" unresolved for local \""
                + node.getNodeName() + "\"");
        }
        return ref;
    }

    /**
     * The mapping of all explicitly used "encoding" attributes to an
     * equivalent set of unique Java names.  The Java names are used for
     * the set of unique coders that we generate; see genCoders().
     */
    private Hashtable codeMap = null;

    /**
     * The unqualified name of the generated class that holds all the
     * string coders.  When codeMap is empty, there will be no coder class.
     */
    private String coderClass = null;

    /**
     * Collects a list of all explicit encodings specified for leaf nodes,
     * and computes a Java name equivalent for each encoding.
     *
     * @return a hashtable mapping encodings to Java names
     */
    public Hashtable collectEncodings ()
    {
	HashSet codeNames = new HashSet();
	HashSet javaNames = new HashSet();
	for (Iterator iter = fileSet.getFiles(); iter.hasNext(); )
	    collectEncodings(((SSC_File) iter.next()).getEtd(), codeNames);
	Hashtable codeMap = new Hashtable(codeNames.size());
	for (Iterator iter = codeNames.iterator(); iter.hasNext(); )
	{
	    String code = (String) iter.next();
	    String name = SscConv.makeJavaId(code, javaNames, null, false);
	    SscConv.addJavaId(name, javaNames);
	    codeMap.put(code, name);
	    if (debug)
		System.out.println("[ encoding <" + code + "> maps to <"
		    + name + "> ]");
	}
	return codeMap;
    }

    /**
     * Collects a list of all explicit encodings specified for leaf nodes
     * at or below the given node.
     *
     * @param node  the node to examine for encoding
     * @param codes  the set of encodings found so far
     */
    private void collectEncodings (MessageStructureNode node, HashSet codes)
    {
	String code = node.getEncoding();
	if (code != null)
	    codes.add(code);
	for (MessageStructureNode child = node.getFirstChild();
	    child != null; child = child.getNextSibling())
	{
	    // Do subnode.
	    collectEncodings(child, codes);
	}
    }

    /**
     * Collects a map of all versioned external template references
     * at or below the given node, mapping the reference to the version.
     *
     * @param node  the node to examine for references
     * @param refs  the set of external references found so far
     */
    private void collectReferences (MessageStructureNode node, Hashtable refs)
    {
	if (node.isReference())
	{
	    String path = node.getRefPath();
	    int vnum = node.getRefCode();
	    if (path != null && vnum > 0)
	    {
		String seen = (String) refs.get(path);
		String vstr = Integer.toString(vnum);
		if (seen == null)
		    refs.put(path, vstr);
		else if (! seen.equals(vstr))
		    throw new RuntimeException("incompatible versions ("
			+ seen + ", " + vstr + ") in references to ["
			+ path + "]");
	    }
	}
	for (MessageStructureNode child = node.getFirstChild();
	    child != null; child = child.getNextSibling())
	{
	    // Do subnode.
	    collectReferences(child, refs);
	}
    }

    /**
     * Sets current output stream to the new given file.
     * Creates the directory if it does not yet exist.
     *
     * @param dir  directory path
     * @param file  filename
     * @return the writer
     */
    private Writer setOut (String dir, String file)
	throws FileNotFoundException, IOException
    {
	File d = new File(dir);
	if (! d.exists()) { d.mkdirs(); }
	Writer w =
	    new BufferedWriter(
		new OutputStreamWriter(
		    new FileOutputStream(
			new File(d, file)),
		    "UTF-8"));
	super.setOut(w);
	return w;
    }

    /**
     * Generates the class to hold the string coders used by this ETD.
     * If the package of the main ETD is P, and its root is named R,
     * then we generate a public class P.RCoder, containing one static
     * public string coder for each distinct encoding specified in the
     * "encoding" attribute of any &lt;node&gt; in the XSC file.  The mapping
     * of encoding names to the public coder variable names is held in
     * the "codeMap" table.
     *
     * @param parseName  basename for the parser class
     * @param rootDir  the directory for the Java sources
     * @param pkg  package name
     * @param fileName  name of file to generate
     * @see com.sun.stc.jcsre.IStringCoder
     */
    private void genCoders (String coderName, String rootDir, String pkg)
	throws IOException
    {
	String p2path = JCSCompiler.package2Path(rootDir, pkg);
	coderClass = coderName + "Coder";
	Writer w = setOut(p2path, coderClass + ".java");
	redent();
	emit("// Generated by the e*Gate 4.5.1 SSC-based XSC back-end.");
	emit();
	emit("package " + pkg + ";");
	emit();
	emit("import com.sun.stc.jcsre.IStringCoder;");
	emit("import com.sun.stc.jcsre.StringCoderFactory;");
	emit();
	emit("/**");
	emit(" * Class to hold all the string coders used by this ETD.");
	emit(" * All methods here are static, so you don't need an instance.");
	emit(" */");
	emit("public class "+ coderClass);
	down("{");
	emit("/**");
	emit(" * Wrapper class to make the actual encoder class without");
	emit(" * throwing a non-Runtime exception, because we are not allowed");
	emit(" * such exceptions during static class initialization.");
	emit(" */");
	emit("private static IStringCoder get (String code)");
	down("{");
	emit("try");
	down("{");
	emit("return StringCoderFactory.getStringCoder(code);");
	done("}");
	emit("catch (java.io.UnsupportedEncodingException u)");
	down("{");
	emit("throw new RuntimeException(\"coder failure"
	    + "[\" + code + \"]: \" +");
	emi1("u.getMessage());");
	done("}");
	done("}");
	emit();
	emit("// The actual coder instances.");
	for (Enumeration enu = codeMap.keys(); enu.hasMoreElements(); )
	{
	    String code = (String) enu.nextElement();
	    String name = (String) codeMap.get(code);
	    emit("public static final IStringCoder " + uEncode(name) + " = "
		+ "get(" + makeStringLiteral(code) + ");");
	    if (codepad)
		emit("public static final byte[] " + uEncode(name) + "_ = "
		    + uEncode(name) + ".encodeChar(' ');");
	}
	done("}");
	close();
    }

    /**
     * Given a node, returns the expression to get the string coder.
     * If the encoding is implicit, returns the given default.
     *
     * @param node  the event node
     * @param deft  expression for the default coder
     * @return the Java expression
     */
    private String getCoderAccess (MessageStructureNode node, String deft)
    {
	String code = node.getEncoding();
	if (code == null || code.equals("")) { return deft; }
	String coder = (String) codeMap.get(code);
	if (coder == null)
	    throw new RuntimeException("no coder for [" + code + "] ?");
	return coderClass + "." + coder;
    }

    /**
     * Given a node, returns the expression to get the padding sequence.
     * If the encoding is implicit, returns the given default.
     *
     * @param node  the event node
     * @param deft  expression for the default sequence
     * @return the Java expression
     */
    private String getPadderAccess (MessageStructureNode node, String deft)
    {
	String code = node.getEncoding();
	if (code == null || code.equals("")) { return deft; }
	if (! codepad) { return "null"; }
	String coder = (String) codeMap.get(code);
	if (coder == null)
	    throw new RuntimeException("no padding for [" + code + "] ?");
	return coderClass + "." + coder + "_";
    }

    // Test: is this node the main root of the ETD?
    private boolean isMainRoot (MessageStructureNode node)
    {
	return node != null
	    && (node.getNodeType() & node.MAIN_ROOT) != 0;
    }

    // Test: is this node the root of a file (the <etd>'s "name")?
    private boolean isFileRoot (MessageStructureNode node)
    {
	return node != null
	    && (node.getNodeType() & node.FILE_ROOT) != 0;
    }

    // Test: is this node the root of a member, i.e. local template?
    private boolean isTempRoot (MessageStructureNode node)
    {
	return node != null
	    && (node.getNodeType() & node.TEMP_ROOT) != 0;
    }

    /**
     * Generates the Java code to implement the "codeVersion" check.
     * This implements the com.sun.stc.jcsre.ETDCodeVersion interface.
     * Note that the generated checkCodeVersion() method must be
     * synchronized, because it has a class variable (codeVersionDone)
     * to protect.
     */
    private void genCodeVersion (SSC_File xsc)
    {
	int cv = xsc.getCodeVersion();
	Hashtable refs = new Hashtable();
	collectReferences(xsc.getEtd(), refs);
	emit();
	emit("private static final int codeVersion = " + cv + ";");
	if (refs.size() > 0)
	    emit("private static boolean codeVersionDone = false;");
	emit("private static final String here = "
	    + makeStringLiteral(xsc.getXscFileName()) + ";");
	emit();
	emit("public static int codeVersion ()");
	emi1("{ return codeVersion; }");
	emit();
	emit("/**");
	emit(" * Verifies that the compiled version of this class matches the");
	emit(" * code version expected by the caller.");
	emit(" *");
	emit(" * @param source  an indication of the caller");
	emit(" * @param version  the expected code version number");
	emit(" * @throws com.sun.stc.jcsre.CodeVersionException if "
	    + "the version differs");
	emit(" */");
	emit("public static synchronized void checkCodeVersion");
	emi1("(String source, int version)");
	down("{");
	if (cv > 0)
	{
	    emit("if (codeVersion != version)");
	    indent();
	    emit("throw new com.sun.stc.jcsre.CodeVersionException(source, here,");
	    emi1("codeVersion, version);");
	    undent();
	}
	if (refs.size() > 0)
	{
	    // Generate check for the external templates.
	    emit("if (! codeVersionDone)");
	    down("{");
	    emit("codeVersionDone = true;");
	    for (Enumeration e = refs.keys(); e.hasMoreElements(); )
	    {
		String path = (String) e.nextElement();
		String vers = (String) refs.get(path);
		SSC_File ref = fileSet.resolveGlobalTemplate(path);
		if (ref == null)
		    throw new RuntimeException("uncaught broken XSC reference");
		String type = packPrefix(ref, xsc) + ref.getEtd().getJavaName();
		emit(type + ".checkCodeVersion(here, " + vers + ");");
	    }
	    done("}");
	}
	done("}");
    }

    /**
     * Generates the Java code to implement the class defined by the
     * given node.
     *
     * @param ssc  the XSC file this node is in
     * @param node   the node for which to generate code
     * @param parserName  basename of generated ANLTR classes
     * @param isMain  flag: is the the main XSC file?
     * @throws IOException for output write problems
     */
    private void genClass (SSC_File ssc, MessageStructureNode node,
	String parserName, boolean isMain)
	throws IOException
    {
	boolean isTop = isTempRoot(node);
	boolean isMarshallable = isMainRoot(node);
	String uJavaName = uEncode(node.getJavaName());
	String uNickName = uEncode(node.getNickName());
	String javaType = node.getJavaType();
	String className =
	    ((javaType == null || javaType.equals(""))
	    ? node.getJavaName()
	    : javaType.substring(javaType.lastIndexOf('.') + 1));
	Writer w = null;
	int save = isdent();
	if (debug)
	    System.out.println("[ class <" + uJavaName + "> has "
		+ (uNickName == null ? "no nickname"
		: ("nickname <" + uNickName + ">")) + " ]");
	if (! isTop)
	{
	    emit("public static class " + uJavaName
		+ (genSet ? " implements com.sun.stc.jcsre.ssc.IClassNode" : ""));
	    down("{");
	}
	else
	{
	    if (debug)
		System.out.println("[ open class file <" + outputDir
		    + ">, <" + outputPkg + ">, <" + className + "> ]");
	    String pack = ssc.getPackageName();
	    String path = JCSCompiler.package2Path(outputDir,
		ssc.getPackageName());
	    if (uNickName != null && ! uJavaName.equals(uNickName))
	    {
		/* Alternate class required for backward compatibility. */
		if (debug)
		    System.out.println("[ nickname class <" + uNickName
			+ "> in <" + path + "> ]");
		redent();
		w = setOut(path, node.getNickName() + ".java");
		emit("// Generated by e*Gate 4.5.1, for compatibility"
		    + " with e*Gate 4.5.0,");
		emit("// from the XSC file: "
		    + uEncode(ssc.getXscFileName()));
		emit();
		if (pack != null && ! pack.equals(""))
		{
		    emit("package " + uEncode(ssc.getPackageName()) + ";");
		    emit();
		}
		emit("public class " + uNickName + " extends "
		    + uEncode(className));
		emi1("{ /* Class alias; no independent code. */ }");
		close();
	    }
	    if (debug)
		System.out.println("[ regular class <" + className
		    + "> for node <" + uJavaName + "> in <" + path + "> ]");
	    w = setOut(path, className + ".java");
	    redent();
	    emit("// Generated by the e*Gate 4.5.1 SSC-XSC back-end,");
	    emit("// from the XSC file: " + uEncode(ssc.getXscFileName()));
	    if (pack != null && ! pack.equals(""))
	    {
		emit();
		emit("package " + uEncode(ssc.getPackageName()) + ";");
	    }
	    emit();
	    emit("import com.sun.stc.jcsre.ssc.FormatterFactory;");
	    emit("import com.sun.stc.jcsre.ssc.BooleanFormat;");
	    emit("import com.sun.stc.jcsre.IStringCoder;");
	    emit("import com.sun.stc.jcsre.StringCoderFactory;");
	    emit("import com.sun.stc.jcsre.StringCoderUtil;");
	    if (marshalCode != JGen.MONK)
	    {
		emit();
                if (codepad)
                {
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer453;");
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer453.*;");
                }
                else
                {
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer;");
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer.*;");                    
                }
	    }
	    if (antlrLess)
	    {
		emit("import com.sun.stc.jcsre.ssc.GlassFactory;");
		emit("import com.sun.stc.jcsre.ssc.IGlass;");
		emit("import com.sun.stc.jcsre.ssc.Mocca;");
		emit("import com.sun.stc.jcsre.ssc.Parse;");
	    }
	    if (sscFromCollab && isMarshallable)
		emit("import com.sun.stc.common.collabService.*;");
	    emit();
	    emit("public class " + uEncode(className));
	    if (isMarshallable)
		emi1("extends com.sun.stc.jcsre.CustomETDImpl_451");
	    if (genSet)
		implement("com.sun.stc.jcsre.ssc.IClassNode");
	    if (genXml && isMarshallable)
		implement("com.sun.stc.jcsre.ETDTestable");
	    implement();
	    down("{");
	}
	genMembers(ssc, node, isMain);
	genConstructor(node);
	genAccessors(node);
	if (JGen.genSet) { genSet(node); }
	if (marshalCode != JGen.MONK)
	{
	    // Java-only marshaling support.
	    genAdd2(node);
	    if (isMarshallable)
	    {
		EmitBuffer e = new EmitBuffer(isdent());
		genJavaMarshal(node.getFile().getDelimiters(), e, node);
		emi0(e.toString());
	    }
	}
	// Marshaling in general.
	if (genXml)
	    genXmlDump(node);
	genMarshaller(node);
	if (isMarshallable)
	{
	    genUnmarshaller(node, parserName);
	    if (doCodeVersion)
		genCodeVersion(node.getFile());
	    emit();
	    emit("public static void main(String[] argv) throws Exception");
	    down("{");
	    emit("com.sun.stc.jcsre.CustomETDImpl_451.main(\"" + outputPkg
		+ "." + uEncode(className) + "\", argv);");
	    done("}");
	}
	done("}");
	if (w != null) { close(); }
	redent(save);
    }

    // Emitted any "implements" clause since last clear?
    private boolean gotImpl = false;

    /**
     * Ends the current "implements" list.
     */
    private void implement ()
    {
	if (gotImpl)
	{
	    emit();
	    undent();
	    undent();
	    gotImpl = false;
	}
    }

    /**
     * Starts new "implements" clause, or extends one already started.
     *
     * @param name  the interface name
     */
    private void implement (String name)
    {
	if (gotImpl)
	{
	    emit(",");
	    part(name);
	}
	else
	{
	    indent();
	    part("implements " + name);
	    indent();
	    gotImpl = true;
	}
    }

    /**
     * Generates the Java code to implement the classes in the class subtree
     * below the given node.
     *
     * @param pnode   the class tree node for which to generate code
     * @param parserName  basename of generated ANLTR classes
     */
    private void genClass (PathNode pnode, String parserName)
	throws IOException
    {
	if (pnode == null) { return; }
	if (debug)
	    System.out.println("[ genClass-tree <" + pnode.getPath() + ">, "
		+ ClassTree.typeName(pnode.getType()) + " ]");

	switch (pnode.getType())
	{
	case PathNode.THEROOT:
	case PathNode.PACKAGE:
	case PathNode.UNKNOWN:
	    // Dive down.
	    PathNode kid;
	    for (kid = pnode.getKids(); kid != null; kid = kid.getNext())
		genClass(kid, parserName);
	    break;

	case PathNode.UPCLASS:
	case PathNode.ISCLASS:
	    // Outermost class.
	    genClass(pnode, true, parserName);
	    break;

	case PathNode.INCLASS:
	    // Impossible.
	    throw new RuntimeException("orphaned "
		+ ctree.typeName(pnode.getType())
		+ " [" + pnode.getPath() + "] in tree");

	case PathNode.BLOCKED:
	    // End of the line.
	    break;
	}
    }

    /**
     * Generates the Java code to implement the class defined by the
     * given node.  If the node is unclaimed (meaning its existence is
     * implied by somebody having defined something else below it),
     * then just generate a bogus class that just serves as the enclosure
     * of the classes inside it.  Note that all generated classes are
     * static, so the class nesting is strictly a name space device;
     * inner classes have no link or reference to their parent classes.
     *
     * @param pnode   the class tree node for which to generate code
     * @param outer   flag: is outermost class, not inner class?
     * @param parserName  basename of generated ANLTR classes
     */
    private void genClass (PathNode pnode, boolean outer, String parserName)
	throws IOException
    {
	ClassTree.PathUser user = pnode.getUser();
	MessageStructureNode node = null;
	SSC_File file = null;
	if (user != null && user instanceof ParseXsc.NodeUser)
	{
	    // File was claimed by ParseXsc, maybe for node.
	    node = ((ParseXsc.NodeUser) user).getNode();
	    file = ((ParseXsc.NodeUser) user).getFile();
	}
	boolean empty = (node == null); // does not implement node?
	ClassTree.PathNode parent = pnode.getParent();
	String name = pnode.getName();
	String uJavaName = uEncode(pnode.getName());
	String outputPkg = (parent == null ? null : parent.getPath());
	if (debug)
	    System.out.println("[ "
		+ (empty ? "fake" : "used") + ' '
		+ (outer ? "outer" : "inner")
		+ " class <" + pnode.getName() + "> ]");

	//----------------------------------------------------
	boolean isTop = outer;
	boolean isForLocalTemplate = false; // ???NYI
	boolean isMarshallable = isMainRoot(node);
	//----------------------------------------------------

	String uNickName = null;
	if (! empty)
	{
	    uNickName = uEncode(node.getNickName());
	    if (debug)
		System.out.println("[ class <" + uJavaName + "> has "
		    + (uNickName == null ? "no nickname"
		    : ("nickname <" + uNickName + ">")) + " ]");
	}
	Writer w = null;
	if (outer)
	{
	    // Outer class: generate into a new file.
	    if (debug)
		System.out.println("[ open class file <" + outputDir
		    + ">, <" + outputPkg + ">, <" + uJavaName + "> ]");
	    String pack = file.getPackageName();
	    String path = JCSCompiler.package2Path(outputDir, outputPkg);
	    if (uNickName != null && ! uJavaName.equals(uNickName))
	    {
		/* Alternate class required for backward compatibility.
		 * NYI: should register nicknames via class tree as well...
		 */
		if (debug)
		    System.out.println("[ nickname class <" + uNickName
			+ "> in <" + path + "> ]");
		redent();
		w = setOut(path, node.getNickName() + ".java");
		emit("// Generated by e*Gate 4.5.1, for compatibility"
		    + " with e*Gate 4.5.0,");
		emit("// from the XSC file: "
		    + uEncode(file.getXscFileName()));
		emit();
		if (pack != null && ! pack.equals(""))
		{
		    emit("package " + uEncode(file.getPackageName()) + ";");
		    emit();
		}
		emit("public class " + uNickName + " extends " + uJavaName);
		emi1("{ /* Class alias; no independent code. */ }");
		close();
	    }
	    if (debug)
		System.out.println("[ regular class <" + uJavaName
		    + "> in <" + path + "> ]");
	    w = setOut(path, pnode.getName() + ".java");
	    redent();
	    emit("// Generated by the e*Gate 4.5.1 SSC-XSC back-end,");
	    emit("// from the XSC file: " + uEncode(file.getXscFileName()));
	    if (pack != null && ! pack.equals(""))
	    {
		emit();
		emit("package " + uEncode(outputPkg) + ";");
	    }
	    emit();
	    if (sscFromCollab && isMarshallable)
		emit("import com.sun.stc.common.collabService.*;");
	    emit("import com.sun.stc.jcsre.ssc.FormatterFactory;");
	    emit("import com.sun.stc.jcsre.ssc.BooleanFormat;");
	    emit("import com.sun.stc.jcsre.IStringCoder;");
	    emit("import com.sun.stc.jcsre.StringCoderFactory;");
	    emit("import com.sun.stc.jcsre.StringCoderUtil;");
	    if (marshalCode != JGen.MONK)
	    {
		emit();
                if (codepad)
                {
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer453;");
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer453.*;");
                }
                else
                {
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer;");
                    emit("import com.sun.stc.jcsre.ssc.SscOutBuffer.*;");                    
                }
	    }
	    if (antlrLess)
	    {
		emit("import com.sun.stc.jcsre.ssc.GlassFactory;");
		emit("import com.sun.stc.jcsre.ssc.IGlass;");
		emit("import com.sun.stc.jcsre.ssc.Mocca;");
		emit("import com.sun.stc.jcsre.ssc.Parse;");
	    }
	    emit();
	    emit("public class " + uJavaName);
	    if (! empty)
	    {
		if (isMarshallable)
		{
		    emi1("extends com.sun.stc.jcsre.CustomETDImpl_451");
		    if (genXml)
			implement("com.sun.stc.jcsre.ETDTestable");
		    if (doCodeVersion)
			implement("com.sun.stc.jcsre.ETDCodeVersion");
		}
	    }
	}
	else
	{
	    // Inner class: generate nested in current file.
	    emit("public static class " + uJavaName);
	}
	if (! empty && genSet)
	    implement("com.sun.stc.jcsre.ssc.IClassNode");
	implement();
	down("{");

	// Generate anything nested in this class.
	for (PathNode kid = pnode.getKids(); kid != null; kid = kid.getNext())
	    genClass(kid, false, parserName);

	if (! empty)
	{
	    genMembers(null, node, isMainRoot(node));
	    genConstructor(node);
	    genAccessors(node);
	    if (JGen.genSet) { genSet(node); }
	    if (marshalCode != JGen.MONK)
	    {
		// Java-only marshaling support.
		genAdd2(node);
		if (isMarshallable)
		{
		    EmitBuffer e = new EmitBuffer(isdent());
		    genJavaMarshal(node.getFile().getDelimiters(), e, node);
		    emi0(e.toString());
		}
	    }
	    // Marshaling in general.
	    genMarshaller(node);
	    if (genXml)
		genXmlDump(node);
	    if (isMarshallable)
	    {
		genUnmarshaller(node, parserName);
		if (doCodeVersion)
		    genCodeVersion(node.getFile());
		emit();
		emit("public static void main(String[] argv) throws Exception");
		down("{");
		emit("com.sun.stc.jcsre.CustomETDImpl_451.main(\"" + outputPkg
		    + "." + uJavaName + "\", argv);");
		done("}");
	    }
	}
	done("}");
	if (w != null)
	{
	    // Outer class, in its own file.
	    close();
	}
    }

    private static Hashtable defaultInit = null;

    /**
     * Generates the code for the constructor of the node class, plus
     * the reset() method that does the real work.  In an ELS context,
     * we don't clear the data, because it's kept unmarshaled.
     *
     * @param node  the message node
     */
    private void genConstructor (MessageStructureNode node)
    {
	MessageStructureNode mem = node.getFirstChild();
	String type = node.getJavaType();
	String className = node.getJavaName();
	if (cnest && type != null && ! type.equals(""))
	{
	    // Class explicit in "javaType" attribute.
	    className = type.substring(type.lastIndexOf('.') + 1);
	}
	boolean clearHasMasks = false;

	emit("public " + uEncode(className) + " () { reset(); }");
	emit();
	emit("// Clear all internal data, unless used as ELS-cache.");
	emit("public boolean reset ()");
	down("{");
	if (isMainRoot(node))
	{
	    emit("if (__jcollabController != null && "
		+ "__jcollabController.isELSEnabled())");
	    emi1("return false;");
	    emit("__dataAvailable = false;");
	    emit("__inputMessages = null;");
	    emit();
	}
	for (; mem != null; mem = mem.getNextSibling())
	{
	    if (mem.getMaxRep() == 0) continue; // fake node in SSC
	    String javaName = mem.getJavaName();
	    String uJavaName = uEncode(javaName);
	    MessageStructureNode ref = null;
	    if (isGlobalTemplate(mem))
	    {
		ref = resolveGlobalTemplate(mem);
	    }
	    else if (isLocalTemplate(mem))
	    {
		ref = resolveLocalTemplate(mem);
	    }
	    String javaType;
	    String javaInit;
	    if (mem.isLeaf())
	    {
		// Leaf node.
		javaType = mem.getJavaType();
		if (javaType == null)
		    throw new RuntimeException("leaf-node \""
			+ mem.getNodeName() + "\" has no Java type ("
			+ mem.getJavaType() + ')');
		String defaultValue = mem.getDefaultString();
		if (defaultValue != null)
		{
		    // We have a default value for the initialization.
		    if (javaType.equals("byte[]"))
		    {
			byte[] data = mem.getDefaultBytes();
			int length = mem.getLength();
			StringBuffer buf = new StringBuffer("new byte[");
			if (length > 0)
			{
			    //buf.append(length);
			    if (length < data.length)
				throw new RuntimeException("default value for ["
				    + mem.getNodeName()
				    + "] exceeds fixed length ("
				    + length + ")");
			}
			buf.append("] { ");
			for (int i = 0; i < data.length; i ++)
			{
			    if (i > 0) { buf.append(", "); }
			    buf.append(data[i]);
			}
			buf.append(" }");
			javaInit = buf.toString();
		    }
		    else if (javaType.equals("java.lang.String"))
		    {
			javaInit = makeStringLiteral(defaultValue);
		    }
		    else
		    {
			throw new RuntimeException("default value not yet "
			    + "supported for field type " + javaType);
		    }
		}
		else
		{
		    // No user-defined default, use type-dependent default.
		    if (defaultInit == null)
		    {
			defaultInit = new Hashtable();
			defaultInit.put("boolean", "false");
			defaultInit.put("byte",    "(byte)0");
			defaultInit.put("short",   "(short)0");
			defaultInit.put("int",     "0");
			defaultInit.put("long",    "0L");
			defaultInit.put("float",   "0.0f");
			defaultInit.put("double",  "0.0");
			defaultInit.put("char",  "'\\0'");
			defaultInit.put("byte[]",  "null");
			defaultInit.put("java.lang.String", "\"\"");
		    }
		    javaInit = (String) defaultInit.get(javaType);
		    if (javaInit == null)
			throw new RuntimeException("leaf type \"" + javaType
			    + "\" has no known default value");
		}
	    }
	    else
	    {
		// Composite node, or reference to template.
		javaInit = "null";
	    }
	    if (mem.getMaxRep() == 1)
		clearHasMasks = true;
	    else
		javaInit = "new java.util.Vector()";
	    emit("_" + uJavaName + " = " + javaInit + ";");
	}
	if (clearHasMasks)
	{
	    emit("// Clear all masks.");
	    emit("this.hasMasks = new int[this.hasMasks.length];");
	}
	emit("return true;");
	done("}");
    }

    /**
     * Generates the accessor method code to reach all children from the
     * given node.  Accessor methods for a node named X of type T may be:
     * T getX(), void setX(T), boolean hasX(), int countX(), and void omitX().
     *
     * @param node  the node in whose class we generate the methods
     */
    private void genAccessors (MessageStructureNode node)
    {
	MessageStructureNode mem = node.getFirstChild();
	for (; mem != null; mem = mem.getNextSibling())
	{
	    if (mem.getMaxRep() == 0) continue; // fake node in SSC
	    String javaName = mem.getJavaName();
	    String nickName = mem.getNickName();
	    int min = mem.getMinRep();
	    String javaType = genType(mem);
	    if (mem.getMaxRep() == 1)
	    {
		// Optional or mandatory.
		genClassAccessors(javaType, javaName, nickName,
		    mem.isReadable(), mem.isWritable(),
		    (node.isLeaf() || min == 0));
	    }
	    else
	    {
		// Repeated.
		genCollectionAccessors(javaType, javaName, nickName,
		    mem.isReadable(), mem.isWritable(),
		    (min == 0));
	    }
	}
    }

    /**
     * Generates the generic "set" methods for the class of the given node.
     * This code is used by the non-ANTLR based unmarshalling procedure.
     * Experimental code...
     *
     * @param node  the node in whose class we generate the methods
     */
    private void genSet (MessageStructureNode node)
    {
	MessageStructureNode mem;
	boolean anyNonLeaf = false;

	// The leaf-node part.
	emit();
	emit("// Generic set-method for leaf-node values.");
	emit("public void set (int field, int index, byte[] data,");
	indent();
	emit("com.sun.stc.jcsre.IStringCoder coder)");
	emit("throws java.text.ParseException");
	undent();
	down("{");
	if(JGen.verboseSet) {
	    emit("System.out.println(this+\".set(\"+field+\", "
		+ "\"+index+\", \" + new String(data)+\")\");");
	}
	emit("switch (field)");
	emit("{");
	mem = node.getFirstChild();
	int count = 0;
	for (; mem != null; mem = mem.getNextSibling(), count ++)
	{
	    int max = mem.getMaxRep();
	    if (max == 0) { continue; }
	    if (mem.isLeaf())
	    {
		// Emit code to set leaf-node value.
		String data = "data";
		String uJavaName = uEncode(mem.getJavaName());
		String javaType = mem.getJavaType();
		String coder = uEncode(getCoderAccess(mem, "coder"));
		if ("byte[]".equals(javaType))
		{
		   data = "data";
		}
		else if ("java.lang.String".equals(javaType))
		{
		    data = coder + ".decode(data)";
		}
		else if ("char".equals(javaType))
		{
		    data = coder + ".decode(data).charAt(0)";
		}
		else if ("boolean".equals(javaType))
		{
		   data = bform + uJavaName + ".parse("
		       + coder + ".decode(data))";
		}
		else
		{
		   data = nform + uJavaName + ".parse("
		       + coder + ".decode(data))."
		       + uEncode(javaType) + "Value()";
		}
		down("case " + count + ": //" + uJavaName);
		emit("set" + uJavaName
		    + "(" + (max == 1 ? "" : "index, ") + data + ");");
		emit("break;");
		undent();

		anyNonLeaf |= mem.isArray();
	    }
	    else
	    {
		// We need a real set() for composites.
		anyNonLeaf = true;
	    }
	}
	emit("default:");
	emi1("throw new IllegalArgumentException"
	    + "(\"Unknown field: \" + field);");
	emit("}");
	done("}");

	// The composite node part.
	emit();
	emit("// Generic set-method for composite node values.");
	emit("public com.sun.stc.jcsre.ssc.IClassNode set (int field, int index)");
	down("{");
	if (JGen.verboseSet) {
	    emit("System.out.println(this+\".set(\"+field+\", "
		+ "\"+index+\")\");");
	}
	if (anyNonLeaf)
	{
	    // Real method.
	    emit("com.sun.stc.jcsre.ssc.IClassNode data = null;");
	    emit("switch (field)");
	    emit("{");
	    down("case -1: // parent node");
	    emit("return this;");
	    undent();
	    mem = node.getFirstChild();
	    for (int i = 0; mem != null; mem = mem.getNextSibling(), i ++)
	    {
		int max = mem.getMaxRep();
		if ((max != 0 && !mem.isLeaf()) || mem.isArray())
		{
		    String data = "data";
		    String uJavaName = uEncode(mem.getJavaName());
		    String javaType = nodeJavaType(node.getFile(), mem, false);
		    down("case " + i + ": //" + uJavaName);
                    if (mem.isLeaf() && mem.isArray()) {
                        emit("data = this;");
                    } else {
                        emit("set" + uJavaName + "("
                            + (max == 1 ? "" : "index, ")
                            + "new " + uEncode(javaType) + "());");
                        emit("data = get" + uJavaName + "("
                            + (max == 1 ? "" : "index") + ");");
                    }
		    emit("break;");
		    undent();
		}
	    }
	    down("default:");
	    emit("throw new IllegalArgumentException"
		+ "(\"Unknown field: \" + field);");
	    done("}");
	    emit("return data;");
	}
	else
	{
	    // Empty method, but required by the IClassNode interface.
	    emit("// There are no non-leaf fields.");
	    emit("throw new IllegalArgumentException"
		+ "(\"Unknown field: \" + field);");
	}
	done("};");

	// The static child node information, for tracing and checking.
	emit();
	emit("// ETD static information.");
	emit("public boolean isArrayNode() {return " + node.isArray() +";}");
	emit();
	emit("public static final String nodePath = "
	    + makeStringLiteral(makeQName(node)) + ";");
	emit("public String etdNodePath ()");
	emi1("{ return nodePath; }");
	emit();
	emit("public static final int children = " + count + ";");
	emit("public int etdChildren ()");
	emi1("{ return " + count + "; }");
	emit();
	emit("public static final String[] childName = new String[]");
	down("{");
	mem = node.getFirstChild();
	for (int i = 0; mem != null; mem = mem.getNextSibling(), i ++)
	    emit("/*" + i + "*/ " + makeStringLiteral(mem.getNodeName())
		+ (i == count-1 ? "" : ","));
	done("};");
	emit("public String etdChildName (int index)");
	emi1("{ return childName[index]; }");
	emit("public static final boolean[] childLeaf = new boolean[]");
	down("{");
	mem = node.getFirstChild();
	for (int i = 0; mem != null; mem = mem.getNextSibling(), i ++)
	    emit("/*" + i + "*/ " + mem.isLeaf() + (i == count-1 ? "" : ","));
	done("};");
	emit("public boolean etdChildLeaf (int index)");
	emi1("{ return childLeaf[index]; }");

    }

    /**
     * ???
     *
     * @param node  ???
     */
    private void genMarshaller (MessageStructureNode node)
    {
	genMarshalHelper(node);
	if (isMainRoot(node)) { genMarshal(node); }
    }

    /**
     * Generates Java code for the auxiliary marshal() method that pumps
     * down the data for this non-leaf node to the Monk event peer object
     * for rendering as a byte array.  The generated code has this pattern:
     *
     * public void marshal (IParser parser)
     * {
     *  parser.StartNode(nodeName);
     *  for each child:
     *   parser.Value(childName, value);  -- if leaf
     *   child.marshal(parser);  -- if non-leaf
     *  parser.EndNode(nodeName);
     * }
     *
     * @param node  the node for which to generate the marshal() method
     */
    private void genMarshalHelper (MessageStructureNode node)
    {
	boolean isTop = isTempRoot(node);
	if (isFileRoot(node))
	{
	    /* The required interface when used as the main ETD.
	     * Just calls the main template, letting it use its own name.
	     */
	    emit();
	    emit("public void marshal (com.sun.stc.jcsre.ssc.IParser parser,"
		+ " int index, String code)");
	    emi1("throws java.io.UnsupportedEncodingException");
	    down("{");
	    emit("IStringCoder coder = "
		+ "StringCoderFactory.getStringCoder(code);");
	    emit("marshal(parser, index, coder, null);");
	    done("}");
	    emit();
	}
	emit("public void marshal (com.sun.stc.jcsre.ssc.IParser parser,"
	    + " int index,");
	emi1("com.sun.stc.jcsre.IStringCoder coder"
	    + (isTop ? ", String name" : "") + ")");
	down("{");
	if (isFileRoot(node))
	{
	    emit("if (name == null)");
	    emi1("name = " + makeStringLiteral(node.getNodeName()) + ';');
	}
	boolean gotTry = false;
	String name = (isTop ? "name" : makeStringLiteral(node.getNodeName()));
	emit("parser.StartNode(" + name + ", index);");
	MessageStructureNode mem = node.getFirstChild();
	for (; mem != null; mem = mem.getNextSibling())
	{
	    if (mem.getMaxRep() == 0) continue; // fake node in SSC
	    String membName = mem.getNodeName();
	    String javaName = mem.getJavaName();
	    String uMembName = uEncode(membName);
	    String uJavaName = uEncode(javaName);
	    MessageStructureNode ref = null;
	    String jtype = genType(mem);
	    if (isGlobalTemplate(mem))
	    {
		ref = resolveGlobalTemplate(mem);
	    }
	    else if (isLocalTemplate(mem))
	    {
		ref = resolveLocalTemplate(mem);
	    }
	    int min = mem.getMinRep();
	    int max = mem.getMaxRep();
	    boolean isMandatory = (min == 1 && max == 1);
	    boolean isOptional = (min == 0 && max == 1);
	    boolean condit = false;
	    boolean doloop = false;
	    String coder = uEncode(getCoderAccess(mem, "coder"));

	    if (isMandatory || isOptional)
	    {
		// At most one instance.
		condit = (isOptional || (mem.isLeaf() && !isMandatory));
	    }
	    else
	    {
		// Repeated element.
		doloop = true;
	    }
	    String get = (doloop ? "(i)" : "()");
	    if (mem.isLeaf() && ! gotTry)
	    {
		gotTry = true;
		down("try {");
	    }
	    if (condit)
	    {
		emit("if (has" + uJavaName + "())");
		down("{");
	    }
	    else if (doloop)
	    {
		emit("for (int i = 0, len = count"
		    + uJavaName + "(); i < len; i++)");
		down("{");
	    }
	    if (mem.isReference())
	    {
		/* Reference to template; requires name of referring node,
		 * cause that is used for marshaling.
		 */
		emit("get" + uJavaName
		    + get + ".marshal(parser, " + (doloop ? "i" : "-1")
		    + ", coder, " + makeStringLiteral(mem.getNodeName())
		    + ");");
	    }
	    else if (! mem.isLeaf())
	    {
		/* Simple parent; use inner class's marshaller. */
		emit("get" + uJavaName
		    + get + ".marshal(parser, " + (doloop ? "i" : "-1")
		    + ", coder);");
	    }
	    else
	    {
		/* Plain leaf field; marshal here in-line.
		 * Currently, the javaType determines directly how
		 * the Java values get tuned into raw byte data.
		 */
		if ("byte[]".equals(jtype))
		{
		    /* The simplest case: already raw byte[] in Java,
		     * passed down directly to Monk unchanged.
		     */
		    emit("parser.Value(\"" + uMembName
			+ "\", get" + uJavaName + get + ", -1);");
		}
		else if ("java.lang.String".equals(jtype))
		{
		    /* The second simplest case: we have a Unicode string
		     * in Java, so we encode it according to the required
		     * encoding (supplied by the "encoding" attribute in
		     * XSC, else by getMarshalEncoding() in the controller,
		     * else the "dataEncoding" attribute in the XSC file,
		     * else the "sscEncoding" attribute in the XSC file,
		     * else just "ASCII") to a byte stream.
		     */
		    emit("parser.Value(\"" + uMembName + "\", " + coder
			+ ".encode(get" + uJavaName + get + "), -1);"
		    );
		}
		else if ("char".equals(jtype))
		{
		    // Character values are handled similar to strings.
		    emit("parser.Value(\"" + uMembName + "\", " + coder
			+ ".encodeChar(get" + uJavaName + get + "), -1);");
		}
		else if ("boolean".equals(jtype))
		{
		    /* Boolean values are first converted to a string
		     * using the "format" specification in the XSC file
		     * (default to "true"/"false" strings), and then
		     * encodes the string just like we do for string
		     * an character values.  To make the calls a bit more
		     * compact, the call to the encoder is hidden in the
		     * formatter class.
		     */
		    emit("parser.Value(\"" + uMembName + "\", " + bform
			+ uJavaName + ".format(get" + uJavaName
			+ get + ", " + coder + "), -1);");
		}
		else if (JCSCompiler.isNumericType(jtype))
		{
		    /* The primitive Java numeric types (byte, short, ...)
		     * are handled, like boolean values, through a "format"
		     * specification.
		     */
		    emit("parser.Value(\"" + uMembName + "\", " + nform
			+ uJavaName + ".format(get" + uJavaName
			+ get + ", " + coder + "), -1);");
		}
		else
		{
		    /* In e*Gate 4.5.1, we have no support yet for
		     * user-defined, non-primitive values.
		     */
		    throw new RuntimeException("unsupported field type \""
			+ jtype + '"');
		}

	    }
	    if (condit || doloop)
	    {
		done("}");
	    }
	}
	emit("parser.EndNode(" + name + ", index);");
	if (gotTry)
	{
	    undent();
	    emit("} catch (Exception ex)"
		+ " { throw new com.sun.stc.jcsre.MarshalException(ex); }");
	}
	done("}");
    }

    /**
     * Generates Java code for the xmldump() method.  The xmldump() method
     * writes out the event object contents in XML format, each field as
     * a &lt;data&gt; entitity containing three normal-safe encoded
     * attributes: name (the XSC "name" attribute), "index" (only if node
     * has maxOccurs not 1), and "data" (only for a leaf-node).
     *
     * @param node  the node for which to generate the xmldump() method
     */
    private void genXmlDump (MessageStructureNode node)
    {
	if (isMainRoot(node))
	{
	    // Top-level XML dumper.
	    emit();
	    emit("// The top-level XML dump for the ETD tester.");
	    emit("public void xmldump (com.sun.stc.jcsre.INodeDump nd)");
	    emi1("throws java.io.IOException");
	    down("{");
	    emit("nd.head();");
	    emit("xmldump(nd, " + makeStringLiteral(node.getNodeName())
		+ ", -1);");
	    emit("nd.tail();");
	    done("}");
	}
	emit();
	down("public void xmldump");
	emit("(com.sun.stc.jcsre.INodeDump nd, String name, int index)");
	emit("throws java.io.IOException");
	undent();
	down("{");
	emit("nd.down(name, index);");
	MessageStructureNode mem = node.getFirstChild();
	for (; mem != null; mem = mem.getNextSibling())
	{
	    int min = mem.getMinRep();
	    int max = mem.getMaxRep();
	    if (max == 0) continue; // fake node in SSC
	    boolean isRepeat = (max != 1);
	    String membName = mem.getNodeName();
	    String javaName = mem.getJavaName();
	    String uMembName = uEncode(membName);
	    String uJavaName = uEncode(javaName);
	    MessageStructureNode ref = null;
	    String jtype = genType(mem);
	    if (isGlobalTemplate(mem))
	    {
		ref = resolveGlobalTemplate(mem);
	    }
	    else if (isLocalTemplate(mem))
	    {
		ref = resolveLocalTemplate(mem);
	    }
	    boolean isMandatory = (min == 1 && max == 1);
	    boolean isOptional = (min == 0 && max == 1);
	    boolean condit = false;
	    boolean doloop = false;
	    String memName = makeStringLiteral(mem.getNodeName());

	    if (isMandatory || isOptional)
	    {
		// At most one instance.
		condit = (isOptional || (mem.isLeaf() && !isMandatory));
	    }
	    else
	    {
		// Repeated element.
		doloop = true;
	    }
	    String get = (doloop ? "(i)" : "()");
	    if (condit)
	    {
		emit("if (has" + uJavaName + "())");
		down("{");
	    }
	    else if (doloop)
	    {
		if (xwrap)
		    emit("nd.down(" + memName + ", -1);");
		emit("for (int i = 0, len = count" + uJavaName
		    + "(); i < len; i++)");
		down("{");
	    }
	    if (mem.isReference() || ! mem.isLeaf())
	    {
		/* Reference to template; requires name of referring node,
		 * cause that is used for marshaling.
		 */
		emit("get" + uJavaName + get + ".xmldump(nd, " + memName + ", "
		    + (doloop ? "i" : "-1") + ");");
	    }
	    else
	    {
		// Plain leaf field; dump here in-line.
		emit("nd.emit(" + memName + ", "
		    + (doloop ? "i" : "-1") + ", "
		    + "get" + uJavaName + get + ");");
	    }
	    if (condit || doloop)
	    {
		done("}");
	    }
	    if (doloop && xwrap)
		emit("nd.done();");
	}
	emit("nd.done();");
	done("}");
    }

    /**
     * Generates the code for the main marshal() function, called directly
     * by the collaboration.
     *
     * @param node  the root node of the ETD
     */
    private void genMarshal (MessageStructureNode node)
    {
	SSC_File ssc = node.getFile();
	if (ssc.getEtd() != node)
	    return;
	String emonk_name = node.getNodeName();
	String event_name = node.getJavaName();
	emit();
	emit("public byte[] marshal"
	    + " (com.sun.stc.jcsre.Marshaller marshaller, String sscPath)");
	down("{");
	if (marshalCode != JGen.JAVA)
	{
	    emit("com.sun.stc.jcsre.ssc.IMonkMarshaller monkMarshaller =");
	    emi1("(com.sun.stc.jcsre.ssc.IMonkMarshaller) marshaller;");
	    emit("com.sun.stc.jcsre.ssc.IMonkParser parser =");
	    emi1("monkMarshaller.getMonkParser(sscPath, \""
		+ emonk_name + "\");");
	}

	/* Figure out the assumed input encoding for Monk.
	 * This is stored in the "dataEncoding" attribute in XSC, which
	 * defaults to the "sscEncoding" attribute, which defaults to ASCII.
	 */
	String datacode = ssc.getDataCode();
	if (datacode == null)
	    datacode = ssc.getEncoding();
	if (datacode == null)
	    datacode = "US-ASCII";
	emit("String key = retrieveKey();");
	down("String marshalEncoding =");
	emit("(__jcollabController != null)");
	emit("? __jcollabController.getMarshalEncoding453(key)");
	emit(": null;");
	undent();
	emit("if (marshalEncoding == null)");
	emi1("marshalEncoding = " + makeStringLiteral(datacode) + ";");
	down("String outgoingEncoding =");
	emit("(__jcollabController != null)");
	emit("? __jcollabController.getOutgoingEncoding(key)");
	emit(": null;");
	undent();
	if (marshalCode != JGen.JAVA)
	{
	    emit("try{");
	    emit("marshal((com.sun.stc.jcsre.ssc.IParser) parser, -1, "
		+ "marshalEncoding);");
	    emit("} catch(java.io.UnsupportedEncodingException ex) {");
	    emi1("throw new RuntimeException(\"jmarshal code error\");");
	    emit("}");
	}
	switch (marshalCode)
	{
	case JGen.MONK:
	    emit("byte[] result = parser.getBytes();");
	    break;
	case JGen.JAVA:
	    emit("byte[] result;");
	    emit("try { result = jmarshal(marshalEncoding); }");
	    emit("catch (java.io.UnsupportedEncodingException u)");
	    emi1("{ throw new RuntimeException(\"jmarshal code error\"); }");
	    break;
	case JGen.BOTH:
	    emit("byte[] result = parser.getBytes();");
	    emit("byte[] jresult;");
	    emit("try { jresult = jmarshal(marshalEncoding); }");
	    emit("catch (java.io.UnsupportedEncodingException u)");
	    emi1("{ throw new RuntimeException(\"jmarshal code error\"); }");                        
            emit("if (! SscOutBuffer"+(codepad?"453":"")+".beq(result, jresult))");
	    down("{");
	    down("try {");
	    emit("System.out.println(\"\\n**** monk result: \"");
	    emi1("+ new String(result, \"ISO-8859-1\"));");
	    emit("System.out.println(\"\\n**** java result: \"");
	    emi1("+ new String(jresult, \"ISO-8859-1\"));");
	    emit("System.out.println(\"\\n**** marshalcode: \"");
	    emi1("+ marshalEncoding);");
	    undent();
	    emit("} catch (java.io.UnsupportedEncodingException u) { }");
	    emit("throw new RuntimeException(\"Marshaling Java != Monk\");");
	    done("}");
	    break;
	default:
	    throw new RuntimeException("impossible marshalCode value");
	}
	down("if (outgoingEncoding != null &&");
	emit("marshalEncoding != null &&");
	emit("! outgoingEncoding.equals(marshalEncoding))");
	undent();
	down("{");
	emit("// Post-translation.");
	emit("try");
	down("{");
	emit("result = StringCoderUtil.translate");
	emi1("(result, marshalEncoding, outgoingEncoding);");
	done("}");
	emit("catch (Exception ex)");
	emi1("{ throw new RuntimeException(\"post-translation error\"); }");
	done("}");
	emit("return result;");
	done("}");
    }

    /**
     * Generates the code for the topl-evel unmarshal() method.
     * This code will render the ETD object as a byte array.
     *
     * @param node  the root node of the ETD
     * @param parserName  basename of generated ANLTR classes
     * @throws UnsupportedEncodingException when "dataEncoding" not supported
     */
    private void genUnmarshaller (MessageStructureNode node, String parserName)
	throws UnsupportedEncodingException
    {
	SSC_File ssc = node.getFile();
	if (ssc.getEtd() != node)
	    return;
	String emonk_name = node.getNodeName();
	String event_name = node.getJavaName();
	emit("/**");
	emit(" * Unmarshals a BLOB into the data content of this ETD.");
	emit(" *");
	emit(" * @param blob  byte array of the BLOB to be unmarshalled");
	emit(" * @throws UnmarshalException "
	    + "when the data does not match the ETD");
	emit(" */");
	emit("public void unmarshal (byte[] blob)");
	emi1("    throws com.sun.stc.jcsre.UnmarshalException");
	down("{");
	emit("String sscFile = " + makeStringLiteral(ssc.getSscFileName())
	    + ';');
	if (sscFromCollab)
	{
	    // Special hack for getting the SSC file through the controller.
	    emit("if (__jcollabController != null)");
	    down("{");
	    emit("String key = retrieveKey();");
	    emit("JCollabInstanceMap instanceMap =");
	    emi1("__jcollabController.getJCollabInstanceMap(key);");
	    emit("if (instanceMap != null)");
	    down("{");
	    emit("sscFile = instanceMap.getFullEventTypeDefinition();");
	    emit("if (sscFile.endsWith(\".xsc\"))");
	    emit("sscFile = sscFile.substring(0, sscFile.length() - 4)"
		+ " + \".ssc\";");
	    done("}");
	    done("}");
	}
        
        if (antlrLess)
        {
            emit("unmarshal(null, blob, sscFile, null);");
        }
        else
        {
            down("com.sun.stc.jcsre.ssc.IMonkMarshaller marshaller =");
            down("(com.sun.stc.jcsre.ssc.IMonkMarshaller)");
            emit("com.sun.stc.jcsre.MarshallerFactory.getInstance(");
            emi1("\"com.sun.stc.jcsre.ssc.IMonkMarshallerImpl\", sscFile);");
            undent();
            undent();
            emit("unmarshal(marshaller, blob, sscFile, null);");
        }

	done("}");
	emit();
	if (antlrLess)
	{
	    emit("private static GlassFactory glafa = new GlassFactory();");
	    emit();
	}
	emit("// Unmarshal method, 2nd stage.");
	emit("public void unmarshal (com.sun.stc.jcsre.Marshaller marshaller,");
	indent();
	emit("byte[] inputEvent, String sscPath, String dataCode)");
	emit("throws com.sun.stc.jcsre.UnmarshalException");
	undent();
	down("{");
	down("try {");
	Xsc.Delim globalDelims = node.getFile().getDelimiters();
	int nds = 0, ads = 0;
	for (Xsc.Delim d = globalDelims; d != null; d = d.getNext())
	{
	  Xsc.OneDelim oneBd = d.getGroups().getBegins();
	  Xsc.OneDelim oneEd = d.getGroups().getEnds();
	  boolean bdOffset = false; 
	  boolean edOffset = false;
	  if (null != oneBd)
	    {
	      bdOffset = oneBd.getOffset() != MessageStructureNode.UNDEFINED;
	    }
	  if (null != oneEd)
	    {
	      edOffset = oneEd.getOffset() != MessageStructureNode.UNDEFINED;
	    }

	  if (bdOffset)
	    {
	      if (false == d.isArray())
		{
		  emit("globalDelims[" + nds + "].setBegin(inputEvent, " +
		       oneBd.getOffset() + ", " +
		       oneBd.getLength() + ");");
		}
	      else
		{
		  emit("globalArrayDelims[" + ads + "].setBegin(inputEvent, " +
		       oneBd.getOffset() + ", " +
		       oneBd.getLength() + ");");
		}
	    }
	  if (edOffset)
	    {
	      if (false == d.isArray())
		{
		  emit("globalDelims[" + nds + "].setEnd(inputEvent, " +
		       oneEd.getOffset() + ", " +
		       oneEd.getLength() + ");");
		}
	      else
		{
		  emit("globalArrayDelims[" + ads + "].setEnd(inputEvent, " +
		       oneEd.getOffset() + ", " +
		       oneEd.getLength() + ");");
		}
	    }
	  if (false == d.isArray())
	    {
	      nds++;
	    }
	  else
	    {
	      ads++;
	    }
	} 
        if (!antlrLess)
        {
            emit("com.sun.stc.jcsre.ssc.IMonkMarshaller monkMarshaller =");
            emi1("(com.sun.stc.jcsre.ssc.IMonkMarshaller)marshaller;");
            emit("com.sun.stc.jcsre.ssc.IMonkLexer lexer = monkMarshaller.getLexer();");
        }
	/* Figure out the assumed input encoding for Monk.
	 * This is stored in the "dataEncoding" attribute in XSC, which
	 * defaults to the "sscEncoding" attribute, which defaults to ASCII.
	 */
	String datacode = ssc.getDataCode();
	if (datacode == null)
	    datacode = ssc.getEncoding();
	if (datacode == null)
	    datacode = "US-ASCII";
	emit("if (dataCode == null) dataCode = "
	    + makeStringLiteral(datacode) + ";");
	down("String incomingEncoding =");
	emit("(__jcollabController != null)");
	emit("? __jcollabController.getIncomingEncoding(__key)");
	emit(": null;");
	undent();
	down("String unmarshalEncoding =");
	emit("(__jcollabController != null)");
	emit("? __jcollabController.getUnmarshalEncoding453(__key)");
	emit(": null;");
	emit("if (unmarshalEncoding == null)");
	emi1("unmarshalEncoding = dataCode;");
	undent();
	down("if (incomingEncoding != null &&");
	emit("unmarshalEncoding != null &&");
	emit("! incomingEncoding.equals(unmarshalEncoding))");
	undent();
	down("{");
	emit("// Pre-translation.");
	emit("inputEvent = StringCoderUtil.translate");
	emi1("(inputEvent, incomingEncoding, unmarshalEncoding);");
	done("}");
	emit("String monkCode = unmarshalEncoding;");
	if (antlrLess)
	{
	    /* Generate unmarshaling code that does not use ANTLR,
	     * but calls the distributed parser code based on IClassNode
	     * as set() methods in the generated node classes.
	     * This method is new in e*Gate 4.5.2.
	     */
/*-
	    emit("Mocca mocca = monkMarshaller.getMocca();");
	    emit("mocca.load(sscPath);");
-*/
	    emit("IGlass glass = glafa.getInstance(sscPath, "
		+ makeStringLiteral(emonk_name) + ");");
	    emit("Parse parser = new Parse(glass);");
	    emit("reset();");
	    emit("parser.parse((com.sun.stc.jcsre.ssc.IClassNode) this,");
	    emi1("inputEvent, monkCode,");
	    emi1("StringCoderFactory.getStringCoder(unmarshalEncoding));");
	}
	else
	{
	    /* Generate unmarshaling code that uses a parser generated by
	     * the ANTLR parser generator, based on the grammar we produced.
	     * This method was used exclusively in e*Gate 4.5 and 4.5.1.
	     */
	    emit("lexer.setInput(sscPath, \"" + emonk_name
		+ "\", inputEvent, dataCode);");
	    emit(parserName + "ParserTokenStream stream = new " + parserName
		+ "ParserTokenStream(lexer);");
	    emit(parserName + "Parser parser = new " + parserName
		+ "Parser(stream);");
	    emit("parser.setEncoding(unmarshalEncoding);");
	    emit("reset();");
	    emit("parser." + makeRuleName(node) + "(this);");
	}
	undent();
	emit("} catch (java.lang.Exception exc) {");
	indent();
	emit("exc.printStackTrace();");
	emit("throw new com.sun.stc.jcsre.UnmarshalException(exc);");
	done("}");
	done("}");
    }

    /**
     * Return the implementation type for the given node, which may be a
     * repeating group or optional.  The type returned here is the type
     * of the field actually declared in the generated Java source code.
     * If "all" is set, returns the Java type for the whole collection;
     * is not set, returns the type for a single occurrence.  For single
     * (non-repeating) nodes, the "all" setting is irrelevant.
     *
     * @param ssc  the SSC-based XSC file this node is in
     * @param node  the node whose type we need
     * @param all  flag: Java type of all occurrences?
     * @return the name of the type
     */
    private String nodeJavaType
	(SSC_File ssc, MessageStructureNode node, boolean all)
    {
	String typeName = null;
	if (all && node.getMaxRep() != 1)
	{
	    // Repeated elements all use vector.
	    typeName = "java.util.Vector";
	}
	else if (isGlobalTemplate(node))
	{
	    /* Reference to an external template.  The type is the (fully
	     * qualified) class name of the external template's root node.
	     */
	    SSC_File ref = resolveGlobalFile(node);
	    if (ref == null)
		throw new RuntimeException(
		    "unresolved external template \"" + node.getLink() + "\"");
	    typeName = packPrefix(ref, ssc) + ref.getEtd().getJavaName();
	}
	else if (isLocalTemplate(node))
	{
	    /* Reference to an internal template.  The type is the
	     * name of the resolved root's type.
	     */
	    MessageStructureNode ref = resolveLocalTemplate(node);
	    if (ref == null)
		throw new RuntimeException(
		    "unresolved internal template \"" + node.getLink() + "\"");
	    typeName = ref.getJavaName();
	}
	else if (node.getFirstChild() != null)
	{
	    // Composite parent node.
	    typeName = node.getJavaType();
	    if (typeName == null || typeName.equals(""))
		typeName = node.getJavaName();
	}
	else
	{
	    /* Leaf node; type is given by [javaType] attribute
	     * directly, which defaults to java.lang.String in the reader.
	     */
	    typeName = node.getJavaType();
	    if (typeName == null || typeName.equals(""))
		typeName = "java.lang.String";
	}
	return typeName;
    }

    /**
     * Finds out the Java type that implements the given node.
     *
     * @param node  a non-null node reference
     * @return the name of the Java type
     */
    public String genType (MessageStructureNode node)
    {
	return nodeJavaType(node.getFile(), node, false);
    }

    private void genMembers
	(SSC_File ssc, MessageStructureNode node, boolean isMain)
	throws IOException
    {
	int memberCount = 0; // number of members

        MessageStructureNode mem = node.getFirstChild();
        for (; mem != null; mem = mem.getNextSibling())
        {
            if (mem.getMaxRep() == 0)
                continue; // fake node in SSC
            String memberName = mem.getJavaName();
            String uMemberName = uEncode(memberName);
            String uMemberVar = "_" + uMemberName;
            SSC_File refSsc = null;
            MessageStructureNode ref = null;
            if (isGlobalTemplate(mem))
	    {
		refSsc = resolveGlobalFile(mem);
                ref = refSsc.getEtd();
            }
	    else if (isLocalTemplate(mem))
	    {
		refSsc = ssc;
                ref = resolveLocalTemplate(mem);
            }
            else if (! mem.isLeaf() && ! cnest)
	    {
                // Generate inner class.
                genClass(ssc, mem, null, isMain);
            }
            int max = mem.getMaxRep();
            String typeName = uEncode(nodeJavaType(ssc, mem, true));
	    emit(typeName + " " + uMemberVar + ";");

	    if (max == 1)
	    {
		emit("private static final int " + bminx + uMemberName
		    + " = " + (memberCount/MASK_LEN) + ";");
		emit("private static final int " + bmask + uMemberName
		    + " = " + (1 << (memberCount % MASK_LEN)) + ";");
		memberCount++;
	    }

	    // Skip formatter output if none needed.
	    if (null == mem.getJavaType()) { continue; }

	    if (JCSCompiler.isNumericType(mem.getJavaType()))
	    {
		// Numerical formatter.
		emit("FormatterFactory.RangeCheckedDecimalFormat "
		    + nform + uMemberName + " =");
		emi1("FormatterFactory.getNumberFormat("
		    + ((mem.getFormat()==null)?"null":"\"" + mem.getFormat()
		    + "\"") + ");");
	    }
	    else if (-1 < "boolean".indexOf(mem.getJavaType()))
	    {
		// Boolean formatter.
		emit("BooleanFormat " + bform + uMemberName + " =");
		emi1("FormatterFactory.getBooleanFormat("
		    + ((mem.getFormat()==null)?"null":"\"" + mem.getFormat()
		    + "\"") + ");");
	    }
	}

	// Generate hasMasks alloc if we have non-repeating members.
	if (0 < memberCount)
	{
	    emit("private int[] hasMasks = new int["
		+ (1+(memberCount/MASK_LEN)) + "];");
	    emit();
	}
    }

    /**
     * Class EmitBuffer is used to generate Java code source text.
     * It maintains the current indentation as state.
     */
    public static class EmitBuffer extends Emit
    {
	private StringWriter w;

	// Creates with initial indentation level.
	public EmitBuffer (int level)
	{
	    super(null);
	    w = new StringWriter();
	    this.setOut(w);
	    this.redent(level);
	}

	// Returns the current text size.
	public int size ()
	{
	    return w.getBuffer().length();
	}

	// Inserts text unchanged at the given position.
	public void insert (int at, String text)
	{
	    w.getBuffer().insert(at, text);
	}

	// Appends text directly to current position.
	public void append (String text)
	{
	    w.getBuffer().append(text);
	}

	/**
	 * Returns buffer contents as a string.
	 *
	 * @return a text string
	 */
    @Override
	public String toString ()
	{
	     w.flush();
	     return w.getBuffer().toString();
	}

	/**
	 * Free the resources.  Don't try to use emit() after this.
	 */
	public void close () throws IOException
	{
	     w = null;
	     super.close();
	}
    }

    /**
     * Tests if this is an array-type node.
     *
     * @param node  the node to test
     * @return the test result
     */
    public boolean isArray (MessageStructureNode node)
    {
	int type = node.getNodeType();
	return
	    (type & node.S_MASK) == node.ARRAY &&
	    (type & (node.CLASS | node.FIELD)) != 0;
    }

    /**
     * Tests if this is a delimited-type node.
     *
     * @param node  the node to test
     * @return the test result
     */
    public boolean isDelim (MessageStructureNode node)
    {
	int type = node.getNodeType();
	return
	    (type & node.S_MASK) == node.DELIM &&
	    (type & (node.CLASS | node.FIELD)) != 0;
    }

    /**
     * Tests if this is a fixed-type node.
     *
     * @param node  the node to test
     * @return the test result
     */
    public boolean isFixed (MessageStructureNode node)
    {
	int type = node.getNodeType();
	return
	    (type & node.S_MASK) == node.FIXED &&
	    (type & (node.CLASS | node.FIELD)) != 0;
    }

    /**
     * Tests if this is a set-type node.
     *
     * @param node  the node to test
     * @return the test result
     */
    public boolean isSet (MessageStructureNode node)
    {
	int type = node.getNodeType();
	return
	    (type & node.S_MASK) == node.SET &&
	    (type & (node.CLASS | node.FIELD)) != 0;
    }

    /**
     * Gets first available delimiter string, as raw data.
     *
     * @param delim  the global or local delimiter level
     * @param array  flag: want array delimiter (not plain delimiter)?
     * @param begin  flag: want begin delimiter (not end delimiter)?
     * @return byte-sequence if available, else null
     */
    public static byte[] delimBytes
	(Xsc.Delim delim, boolean array, boolean begin)
	throws UnsupportedEncodingException
    {
	if (delim != null && (array == delim.isArray()))
	{
	    for (Xsc.DelimGroup group = delim.getGroups(); group != null;
		group = group.getNext())
	    {
		Xsc.OneDelim one = (begin
		    ? group.getBegins()
		    : group.getEnds());
		if (one != null)
		{
		    // First, try raw data.
		    byte[] data = one.getData();
		    if (data == null)
		    {
			// Second, convert from text.
			String text = one.getText();
			String code = one.getCode();
			IStringCoder coder =
			    StringCoderFactory.getStringCoder(
				(code == null ? "ASCII" : code));
			data = coder.encode(text);
		    }
		    return data;
		}
	    }
	}
	return null;
    }

    /**
     * Gets delimiter string, as raw data.
     *
     * @param one  delimiter to extract data from
     * @return byte-sequence if available, else null
     */
    public static byte[] delimBytes (Xsc.OneDelim one)
	throws UnsupportedEncodingException
    {
      if (one != null)
	{
	  // First, try raw data.
	  byte[] data = one.getData();
	  if (data == null)
	    {
	      // Second, convert from text.
	      String text = one.getText();
	      String code = one.getCode();
	      IStringCoder coder =
		StringCoderFactory.getStringCoder((code == null ? "ASCII" : code));
	      data = coder.encode(text);
	    }
	  return data;
	} 
      return null;
    }

    /**
     * Gets local delimiter if available.
     *
     * @param node  the node to look under
     * @param array  flag: want array delimiter (not plain delimiter)?
     * @param begin  flag: want begin delimiter (not end delimiter)?
     * @return byte-sequence if available, else null
     */
    public static byte[] localBytes
	(MessageStructureNode node, boolean array, boolean begin)
	throws UnsupportedEncodingException
    {
	return node != null
	    ? delimBytes(node.getDelim(), array, begin)
	    : null;
    }

    /**
     * Emits "NodeInfo" run-time descriptor with static node properties.
     * This is used by the generated "jmarshal()" method.
     *
     * @param node  a non-null structure node reference
     * @param name  the basename of the descriptor
     * @param e  output buffer for generated declaration
     * @throws UnsupportedEncodingException for invalid delimiter encoding
     */
    private static void genNodeInfo
	(MessageStructureNode node, String name, EmitBuffer e)
	throws UnsupportedEncodingException
    {
	Xsc.Delim delim = node.getDelim();
	int dtp = node.getDelimType();
	boolean array = ((dtp & Xsc.Delim.ARRAY)    != 0);
	boolean eorec = ((dtp & Xsc.Delim.ENDOFREC) != 0);
	boolean reqrd = ((dtp & Xsc.Delim.REQUIRED) != 0);

	byte[] bd = null;
	byte[] ed = null;
	if (null != delim)
	  {
	    Xsc.DelimGroup delimGroupToUse = delim.getGroups();
	    if (null != delimGroupToUse)
	      {
		bd = delimBytes(delimGroupToUse.getBegins());
		ed = delimBytes(delimGroupToUse.getEnds());
	      }
	  }
	e.emit();
	e.emit("static NodeInfo des" + uEncode(name) + " = new NodeInfo(");
	e.indent();
	e.emit("" + node.getMinRep()
		+ ", " + node.getMaxRep()
		+ ", " + node.getOffset()
		+ ", " + node.getLength()
		+ ", " + makeStringLiteral(node.getDefaultString())
		+ ","
	);

	if ((false == array) && (bd != null || ed != null || eorec || reqrd))
	{
	    // Use local plain delimiters.
	    e.emit("new Delimiter((String)" + makeByteLiteral(bd)
		+ ", (String)" + makeByteLiteral(ed)
		+ ", " + eorec
		+ ", " + reqrd + "),"
	    );
	}
	else
	{
	    // Use global plain delimiters.
	    e.emit("null,");
	}

	if ((true == array) && (bd != null || ed != null || eorec || reqrd))
	{
	    // Use local array delimiters.
	    e.emit("new Delimiter(" + makeByteLiteral(bd)
		+ ", " + makeByteLiteral(ed)
		+ ", " + eorec
		+ ", " + reqrd + "),"
	    );
	}
	else
	{
	    // Use global array delimiters.
	    e.emit("null,");
	}
	e.emit(""+(0!=(node.getNodeType() & MessageStructureNode.FIXED)));
	e.emit(");");
	e.undent();
    }

    // Make Java string literal for byte-array, with chars 0-255.
    public static String makeByteLiteral (byte[] b)
    {
	if (b == null) { return "null"; }
	char[] buf = new char[b.length];
	for (int i = 0; i < b.length; i ++) { buf[i] = (char)(b[i] & 0xFF); }
	return makeStringLiteral(new String(buf));
    }

    /*---------------------------------------------------------*\
    |	e*Gate 4.5.1 code to implement SSC marshaling directly	|
    |	in Java, instead of going through the Monk marshaling	|
    |	function "$event->string" using the Mocca JNI library.	|
    |	This is also known as "native marshaling".		|
    |								|
    |	Each class generated for a non-leaf SSC node receives	|
    |	an extra "add" method, which uses the "SscOutBuffer"	|
    |	class at run-time to generate its serialized contents.	|
    |	The "add" method serializes leaf nodes directly, and	|
    |	non-leaf nodes via calls to their "add" methods.	|
    \*---------------------------------------------------------*/

    /**
     * Generates code to marshal the given node at run-time.
     * <p>
     * To render a non-leaf node, the generated Java class for that
     * node has two methods: "add" and "addEmpty", both of which take
     * a render buffer into which to generate their output bytes.
     * Where "add" is an instance method that renders an existing node,
     * "addEmpty" is a class method that renders a required but missing
     * node as a delimiter and padding skeleton.
     * <p>
     * The basic rule for "add" is: emit any begin-delimiter, then
     * emit any default data and seek back to the start of the field;
     * render any children, then any end-delimiter and padding.
     * Repetition of the child nodes is handled in the parent's code,
     * as is all the rendering of any primitive (leaf) children.
     * <p>
     * The SscOutBuffer provides an open-ended, directly addressable
     * byte buffer, much like a simple file.  An instance also provides
     * a list of global default delimiters, and a level counter to find
     * the right global default.
     *
     * @param node  a non-null structure node reference
     * @param n  output buffer for the node-info declarations
     * @param a  output buffer for the add() method code
     * @param e  output buffer for the addEmpty() method code
     * @throws UnsupportedEncodingException for invalid delimiter encoding
     */
    public void genAdd (MessageStructureNode node,
	EmitBuffer n, EmitBuffer a, EmitBuffer e)
	throws UnsupportedEncodingException
    {
	String javaName = node.getJavaName();
	int size;

	// Functions for array node enter/leave.
	genNodeInfo(node, "", n);
	a.emit("public static void enter (SscOutBuffer" + (codepad ? "453" : "") + " b)");
	a.emit("{");
	a.indent();
	if (isArray(node)) {
	    a.emit("b.addBegin(des, false);");
	}
	a.undent();
	a.emit("}");
	a.emit("public static void leave (SscOutBuffer" + (codepad ? "453" : "") + " b)");
	a.emit("{");
	a.indent();
	if (isArray(node)) {
	    a.emit("b.addEnd(des, false);");
	}
	a.undent();
	a.emit("}");

	// Emit the common prologue.
	a.emit();
	a.emit("public void add (SscOutBuffer" + (codepad ? "453" : "") + " b, NodeInfo ni)");
	a.emi1("throws java.io.UnsupportedEncodingException");
	a.emit("{");
	a.indent();
	a.emit("int child, n;");
	a.emit("com.sun.stc.jcsre.IStringCoder coder = b.getDefaultCoder();");
	boolean got = (node.getDefaultString() != null);
	if (got)
	    a.emit("boolean got = b.gotData;");
	e.emit("public static void addEmpty "
	    + "(SscOutBuffer" + (codepad ? "453" : "") + " b, int count, NodeInfo ni)");
	e.emit("{");
	e.indent();
	e.emit("int child;");
	e.emit("com.sun.stc.jcsre.IStringCoder coder = b.getDefaultCoder();");
	e.emit("for (int i = 0; i < count; i ++)");
	e.emit("{");
	e.indent();

	// Emit begin-delimiter of class.
	if (isDelim(node) || isFixed(node))
	{
	    a.emit("b.addBegin((null!=ni)?ni:des, false);");
	    e.emit("b.addBegin((null!=ni)?ni:des, false);");
	}
	else if (isArray(node))
	{
	    a.emit("b.addBegin((null!=ni)?ni:des, true);");
	    e.emit("b.addBegin((null!=ni)?ni:des, true);");
	}

	if (! isSet(node))
	{
	    // If not a set, switch default delimiter level.
	    a.emit("b.enter();");
	    e.emit("b.enter();");
	}

	boolean doPad = (codepad && isFixed(node) && node.getLength() != 0);

        if(doPad) {
            a.emit("int start = b.getOffset();\n");
            e.emit("int start = b.getOffset();\n");
        }

        a.emit("try {");
        a.indent();

	// Loop over the children.
	MessageStructureNode mem = node.getFirstChild();
	for (int fno = 0; mem != null; mem = mem.getNextSibling())
	{
	    String memberName = mem.getJavaName();
	    String uMemberName = uEncode(memberName);
	    String uMemberVar = "_" + uMemberName; // mem's variable name
	    String uMemberDes = "des" + uMemberName; // mem's descriptor name
	    String memberType = genType(mem);
	    if (memberType == null)
		throw new RuntimeException("field [" + memberName
		    + "] has no java type");
	    String uMemberType = uEncode(memberType);
	    int min = mem.getMinRep();
	    int max = mem.getMaxRep();
	    if (max == 0)
		continue; // fake node in SSC
	    String coder = uEncode(getCoderAccess(mem, "coder"));
	    String padder = "";

	    if (fno++ == 0)
		a.emit("");
	    a.emit("// Field \"" + uMemberName + "\"");
	    a.emit("// min=" + min + ", max=" + max + ", leaf=" + mem.isLeaf());

	    if (mem.isLeaf())
	    {
		// Simple leaf node; contents handled by parent.
		genNodeInfo(mem, memberName, n);

		// Generate marshaling rules; differes per kind of node.
		String kind = null;
		if (isFixed(mem))
		{
		    kind = "Fix";
		    if (codepad)
			padder = ", " + uEncode(getPadderAccess(mem, "null"));
		}
		else if (isDelim(mem))
		    kind = "Del";
		else if (isArray(mem))
		    kind = "Arr";
		else
		    throw new RuntimeException("leaf node \"" + memberName
			+ "\" must be one of delimited, fixed, or array");
		if (min == 1 && max == 1)
		{
		    if ("byte[]".equals(mem.getJavaType())) {
			// Raw data, no encoding required.
			a.emit("b.addReq" + kind + "(" + uMemberVar
			    + ", " + uMemberDes + padder + ");"
			    );

		    } else if ("java.lang.String".equals(mem.getJavaType())) {
			// String data, encoding required.
			a.emit("b.addReq" + kind + "(" + coder
			    + ".encode(" + uMemberVar + "), "
			    + uMemberDes + padder + ");"
			    );

		    } else if ("char".equals(mem.getJavaType())) {
			// Char is like string data, encoding required.
			a.emit("b.addReq" + kind + "(" + coder
			    + ".encodeChar(" + uMemberVar + "), "
			    + uMemberDes + padder + ");"
			    );

		    } else if (JCSCompiler.isNumericType(mem.getJavaType())) {
			// Encoding and numerical formatting.
			a.emit("b.addReq" + kind + "(" + nform + uMemberName
			    + ".format(" + uMemberVar + ", " + coder + ")"
			    + ", " + uMemberDes + padder + ");"
			    );

		    } else if ("boolean".equals(mem.getJavaType())) {
			// Encoding and boolean formatting.
			a.emit("b.addReq" + kind + "(" + bform + uMemberName
			    + ".format(" + uMemberVar + ", " + coder +")"
			    + ", " + uMemberDes + padder + ");"
			    );

		    } else {
			// Should catch this earlier, normally.
			throw new RuntimeException("node [" + mem.getNodeName()
			    + " has type [" + mem.getJavaType() + "], not "
			    + "supported as a field type");
		    }

		    e.emit("b.addEmp" + kind + "(1, " + uMemberDes + padder
			+ ");");

		} else if (min == 0 && max == 1) {

                    // don't want to emit empty node if we have a tag
                    if(mem.getTag() != null && mem.getTag().length() > 0) {
                        a.emit("if (has" + uMemberName + "()) {");
                        a.indent();
                    }

		    if ("byte[]".equals(mem.getJavaType())) {
			// Raw data, no encoding required.
			a.emit("b.addOpt" + kind + "(" + uMemberVar
				+ ", " + uMemberDes + padder 
                                + ", has" + uMemberName + "());"
				);

		    } else if ("java.lang.String".equals(mem.getJavaType())) {
			// String data, encoding required.
			a.emit("b.addOpt" + kind + "(" + coder
			    + ".encode(" + uMemberVar + "), "
			    + uMemberDes + padder 
			    + ", has" + uMemberName + "());"
                            );

		    } else if ("char".equals(mem.getJavaType())) {
			// Char is like string data, encoding required.
			a.emit("b.addOpt" + kind + "(" + coder
			    + ".encodeChar(" + uMemberVar + "), "
			    + uMemberDes + padder 
			    + ", has" + uMemberName + "());"
                            );

		    } else if (JCSCompiler.isNumericType(mem.getJavaType())) {
			// Encoding and numerical formatting.
			a.emit("b.addOpt" + kind + "(" + nform
				+ uMemberName + ".format("
				+ uMemberVar + ", " + coder +")"
				+ ", " + uMemberDes + padder 
                                + ", has" + uMemberName + "());"
				);

		    } else if ("boolean".equals(mem.getJavaType())) {
			// Encoding and boolean formatting.
			a.emit("b.addOpt" + kind + "(" + bform
				+ uMemberName + ".format(" + uMemberVar
				+ ", " + coder +")"
				+ ", " + uMemberDes + padder
                                + ", has" + uMemberName + "());"
				);
		    }

                    // must close empty node check if we have a tag
                    if(mem.getTag() != null && mem.getTag().length() > 0) {
                        a.undent();
                        a.emit("}");
                    }

		} else {

		    // Repeating field.
		    if (max == -1 || max == MessageStructureNode.UNBOUNDED)
			a.emit("n = " + uMemberVar + ".size();");
		    else
			a.emit("n = Math.min(" + max + ", " + uMemberVar
			    + ".size());");
		    if (isArray(mem))
		    {
			a.emit("b.addBegin((null!=ni)?ni:" + uMemberDes + padder
			    + ", false);");
			e.emit("b.addBegin((null!=ni)?ni:" + uMemberDes + padder
			    + ", false);");
		    }
		    a.emit("for (int i = 0; i < n; i ++) {");
		    a.indent();

		    if ("byte[]".equals(mem.getJavaType())) {
			// Raw data, no encoding required.
			a.emit("b.addReq" + kind + "(get" + uMemberName + "(i)"
			    + ", " + uMemberDes + padder + ");"
			    );

		    } else if ("java.lang.String".equals(mem.getJavaType())) {
			// String data, encoding required.
			a.emit("b.addReq" + kind + "(" + coder
			    + ".encode(get" + uMemberName + "(i)), "
			    + uMemberDes + padder + ");"
			    );

		    } else if ("char".equals(mem.getJavaType())) {
			// Char is like string data, encoding required.
			a.emit("b.addReq" + kind + "(" + coder
			    + ".encodeChar(get" + uMemberName + "(i)), "
			    + uMemberDes + padder + ");"
			    );

		    } else if (JCSCompiler.isNumericType(mem.getJavaType())) {
			// Encoding and numerical formatting.
			a.emit("b.addReq" + kind + "(" + nform
			    + uMemberName + ".format(get"
			    + uMemberName + "(i)"
			    + ", " + coder +")"
			    + ", " + uMemberDes + padder + ");"
			    );

		    } else if ("boolean".equals(mem.getJavaType())) {
			// Encoding and boolean formatting.
			a.emit("b.addReq" + kind + "(" + bform
			    + uMemberName + ".format(get"
			    + uMemberName + "(i)"
			    + ", " + coder +")"
			    + ", " + uMemberDes + padder + ");"
			    );
		    }

		    a.undent();
		    a.emit("}");

		    if (min > 0) {
			// Minimum requirement.
			a.emit("b.addEmp" + kind + "(" + min + " - n, "
			    + uMemberDes + padder + ");");
			e.emit("b.addEmp" + kind + "(" + min + ", "
			    + uMemberDes + padder + ");");
		    }

		    if (isArray(mem)) {
			a.emit("b.addEnd((null!=ni)?ni:" + uMemberDes
			    + ", false);");
			e.emit("b.addEnd((null!=ni)?ni:" + uMemberDes
			    + ", false);");
		    }
		}
	    }
	    else
	    {
		boolean override = mem.isReference() && mem.getDelim() != null;
                if (override) {
                    genNodeInfo(mem, memberName, n);
                }

		/* Composite node; has its own "add" and "addEmpty" methods,
		 * and handles its own delimiters.  We just do repetitions.
		 */
		if (got)
		    a.emit("b.gotData = false;");
		a.emit(uMemberType + ".enter(b);");
		e.emit(uMemberType + ".enter(b);");
		if (min == 1 && max == 1)
		{
		    // Required field.
		    a.emit("if (" + uMemberVar + " != null)");
		    a.indent();
                    if(override) {
                        a.emit(uMemberVar + ".add(b, " + uMemberDes + ");");
                    } else {
                        a.emit(uMemberVar + ".add(b, null);");
                    }
		    a.undent();
		    a.emit("else");
		    a.indent();
                    if(override) {
                        a.emit(uMemberType + ".addEmpty(b, 1, "
			    + uMemberDes + ");");
                        e.emit(uMemberType + ".addEmpty(b, " + min
                            + ", " + uMemberDes + ");");
                    } else {
                        a.emit(uMemberType + ".addEmpty(b, 1, null);");
                        e.emit(uMemberType + ".addEmpty(b, " + min
                            + ", null);");
                     }
		    a.undent();
		}
		else if (min == 0 && max == 1)
		{
		    // Optional field.
		    a.emit("if (" + uMemberVar + " != null)");
		    a.indent();
                    if(override) {
                        a.emit(uMemberVar + ".add(b, " + uMemberDes + ");");
                    } else {
                        a.emit(uMemberVar + ".add(b, null);");
                    }
		    a.undent();
		}
		else
		{
		    // Repeating field.
		    if (max == -1 || max == MessageStructureNode.UNBOUNDED)
			a.emit("n = " + uMemberVar + ".size();");
		    else
			a.emit("n = Math.min(" + max + ", " + uMemberVar
			    + ".size());");

		    a.emit("for (int i = 0; i < n; i ++) {");
		    a.indent();
                    if(override) {
                        a.emit("((" + uMemberType + ") _" + uMemberName
			    + ".get(i)).add(b, " + uMemberDes + ");");
                    } else {
                        a.emit("((" + uMemberType + ") _" + uMemberName
			    + ".get(i)).add(b, null);");
                    }
		    a.undent();
                    a.emit("}");
		    /*-
                    if(min>0) {
                            a.emit("b.commit();");
                    }
		    -*/
		    if (min > 0)
		    {
			// Minimum requirement.
                        if(override) {
                            a.emit(uMemberType + ".addEmpty(b, " + min
			        + " - n, " + uMemberDes + ");");
                            e.emit(uMemberType + ".addEmpty(b, " + min
			        + ", " + uMemberDes + ");");
                        } else {
                            a.emit(uMemberType + ".addEmpty(b, " + min
			        + " - n, null);");
                            e.emit(uMemberType + ".addEmpty(b, " + min
			        + ", null);");
                        }
		    }
		}
		a.emit(uMemberType + ".leave(b);");
		e.emit(uMemberType + ".leave(b);");
		if (got)
		    a.emit("got |= b.gotData;");
	    }
	    a.emit("");
	}

    a.undent();
    a.emit("} catch(NullPointerException ex) {");
    a.indent();
    a.emit("throw new com.sun.stc.jcsre.MarshalException(\"Possible missing mandatory node.\", ex);");
    a.undent();
    a.emit("}\n");

	// Tail of main body.
	if (! isSet(node))
	{
	    // If not a set, switch default delimiter level.
	    a.emit("b.leave();");
	    e.emit("b.leave();");
	}


	if (isDelim(node))
	{
	    a.emit("b.addEnd((null!=ni)?ni:des, false);");
	    e.emit("b.addEnd((null!=ni)?ni:des, false);");
	}
	else if (isArray(node))
	{
	    a.emit("b.addEnd((null!=ni)?ni:des, true);");
	    e.emit("b.addEnd((null!=ni)?ni:des, true);");
	}
	if (got)
	{
	    a.emit("b.gotData = got;");
	    a.emit("if (! got)");
	    a.indent();
	    a.emit("b.raw("
		+ makeStringLiteral(node.getDefaultString()) + ");");
	    a.undent();
	}
	if (doPad)
	{
	    a.emit("b.pad(start, " + node.getLength() + ", null);");
	    e.emit("b.pad(start, " + node.getLength() + ", null);");
	}
	a.undent();
	a.emit("}");
	e.undent();
	e.emit("}");
	e.undent();
	e.emit("}");
    }

    /**
     * Returns byte-array declaration of given string, assuming ISO-8859-1
     * encoding.
     *
     * @param str  any Unicode string
     * @return a java code snippet with a byte declaration equivalent of "str"
     */
    private static String byteDecl (byte[] data)
    {
	if (data == null)
	    return "(byte[])null";
	StringBuffer b = new StringBuffer("new byte[] {");
	for (int i = 0; i < data.length; i ++)
	{
	    int c = data[i];
	    if ('\\' == c)
		b.append("'\\\\'");
	    else if (byteAsChar && 0x20 <= c && c < 0x7F && c != '\'')
		b.append("'" + (char) c + "'");
	    else
		b.append(c);
	    b.append(", ");
	}
	b.append("}");
	return b.toString();
    }

    /**
     * Generates code for the top-level marshal method "jmarshal()".
     * This code is pure Java, not using Monk or the Mocca library.
     *
     * @param dels  the list of global default delimiters in SSC
     * @param e  output buffer for generated "jmarshal" method
     * @param _root  the root node
     */
    private void genJavaMarshal (Xsc.Delim dels, EmitBuffer e, MessageStructureNode _root)
	throws UnsupportedEncodingException
    {
	int delCount = 0;
	for (Xsc.Delim d = dels; d != null; d = d.getNext())
	    delCount ++;

	/* Sort delimiters into ordinary and array.  In SSC and XSC,
	 * a global default array delimiter immediately follows the
	 * non-array delimiter immediately in the global delimiter list.
	 */
	Xsc.Delim[] nd = new Xsc.Delim[delCount];
	Xsc.Delim[] ad = new Xsc.Delim[delCount];
	int nds = 0, ads = 0;
	for (Xsc.Delim d = dels; d != null; d = d.getNext())
	{
	    if (! d.isArray())
	    {
		// Plain delimiter.
		nd[nds ++] = d;
	    }
	    else
	    {
		// Array delimiter.
		if (nds <= 0) { nds ++; }
		ad[nds-1] = d; ads = nds;
	    }
	}

	e.emit("private SscOutBuffer" + (codepad ? "453" : "") +
	       ".Delimiter[] globalDelims = ");

	// Emit non-array delimiters.
	e.indent();
	e.emit("// Global non-array delimiters.");
	e.emit("{");
	e.indent();
	for (int i = 0; i < nds; i ++)
	{
	    if (nd[i] == null)
	    {
		// No plain at this level.
		e.emit(i == nds-1 ? "null" : "null,");
		continue;
	    }
	    int type = nd[i].getFlags();
	    Xsc.DelimGroup delimGroupToUse = nd[i].getGroups();
	    byte[] bd = delimBytes(delimGroupToUse.getBegins());
	    byte[] ed = delimBytes(delimGroupToUse.getEnds());
	    e.emit("new SscOutBuffer" + (codepad ? "453" : "") + ".Delimiter(");
	    e.indent();
	    e.emit(byteDecl(bd) + ",");
	    e.emit(byteDecl(ed) + ",");
	    e.emit(String.valueOf((type & Xsc.Delim.ENDOFREC) != 0) + ",");
	    e.emit(String.valueOf((type & Xsc.Delim.REQUIRED) != 0));
	    e.undent();
	    e.emit((i == nds-1) ? ")" : "),");
	}
	e.undent();
	e.emit("};");
	e.undent();

	e.emit("private SscOutBuffer" + (codepad ? "453" : "") +
	       ".Delimiter[] globalArrayDelims = ");
	// Emit array delimiters.
	e.indent();
	e.emit("// Global array delimiters.");
	e.emit("{");
	e.indent();
	for (int i = 0; i < ads; i ++)
	{
	    if (ad[i] == null)
	    {
		// No array at this level.
		e.emit(i == ads-1 ? "null" : "null,");
		continue;
	    }
	    int type = ad[i].getFlags();
	    Xsc.DelimGroup delimGroupToUse = ad[i].getGroups();
	    byte[] bd = delimBytes(delimGroupToUse.getBegins());
	    byte[] ed = delimBytes(delimGroupToUse.getEnds());
	    e.emit("new SscOutBuffer" + (codepad ? "453" : "") + ".Delimiter(");
	    e.indent();
	    e.emit(byteDecl(bd) + ",");
	    e.emit(byteDecl(ed) + ",");
	    e.emit(String.valueOf((type & Xsc.Delim.ENDOFREC) != 0) + ",");
	    e.emit(String.valueOf((type & Xsc.Delim.REQUIRED) != 0));
	    e.undent();
	    e.emit((i == ads-1 ? ")" : "),"));
	}
	e.undent();
	e.emit("};");
	e.undent();

	e.emit("public byte[] jmarshal (String code)");
	e.emi1("throws java.io.UnsupportedEncodingException");
	e.emit("{");
	e.indent();
	e.emit("SscOutBuffer" + (codepad ? "453" : "") + " sob = new SscOutBuffer" + (codepad ? "453" : "") + "(");
	e.indent();
	e.emit("StringCoderFactory.getStringCoder(code),");
	e.emit("globalDelims,");
	e.emit("globalArrayDelims);");
	e.undent();
        if(isSet(_root)) {
            e.emit("// need to consume the root level delimiter for a set");
            e.emit("sob.enter();");
        }

	e.emit("add(sob, null);");

        if(isSet(_root)) {
            e.emit("sob.leave();");
        }

	e.emit("return sob.content();");
	e.undent();
	e.emit("}");
    }

    /**
     * Adds code for marshaling the given node in pure Java (not via Mocca).
     *
     * @param node  a non-null structure node reference
     * @param indent  space prefix for code line indentation
     * @throws UnsupportedEncodingException for invalid delimiter encoding
     */
    private void genAdd2 (MessageStructureNode node)
	throws UnsupportedEncodingException
    {
	EmitBuffer n = new EmitBuffer(isdent());
	EmitBuffer a = new EmitBuffer(isdent());
	EmitBuffer e = new EmitBuffer(isdent());
	genAdd(node, n, a, e);
	emi0(n.toString());
	emi0(a.toString());
	emi0(e.toString());
    }

    /**
     * Generates the class to hold the string coders used by this ETD.
     *
     * @param parseName  basename for the parser class
     * @param rootDir  the directory for the Java sources
     * @param pkg  package name
     * @param fileName  name of file to generate
     */
    private void genGrammarFiles
	(String parserName, String rootDir, String pkg, String fileName)
	throws IOException
    {
	String[] ipkgs = {};
	String p2path = JCSCompiler.package2Path(rootDir, pkg);
	if (debug)
	    System.out.println("[ genGrammarFile: root=<" + rootDir
		+ ">, pack=<" + pkg + ">, path=<" + p2path
		+ ">, file=<" + fileName + "> ]");
	Writer w = setOut(p2path, fileName);
	redent();
	emit("header");
	down("{");
	emit("package " + pkg + ";");
	emit("import com.sun.stc.jcsre.IStringCoder;");
	emit("import com.sun.stc.jcsre.StringCoderFactory;");
	if (! codeMap.isEmpty())
	    emit("import " + pkg + "." + coderClass + ";");
	done("}");
	emit();
	emit("class "+ parserName + "Parser extends Parser;");
	emit("options");
	down("{");
	emit("defaultErrorHandler=false;");
	emit("codeGenMakeSwitchThreshold=999;");
	emit("codeGenBitsetTestThreshold=10;");
	done("}");
	down("{");
	emit("private String encoding = null;");
	emit("private IStringCoder coder = null;");
	emit();
	emit("public String getEncoding () { return this.encoding; }");
	emit("public IStringCoder getCoder () { return this.coder; }");
	emit();
	emit("/**");
	emit(" * Sets the current default encoding, for fields that have no");
	emit(" * explicit 'encoding' attribute in the XSC file.");
	emit(" *");
	emit(" * @param e  the name of the encoding; cf. IStringCoder");
	emit(" * @throws UnsupportedEncodingException if e is invalid");
	emit(" */");
	emit("public void setEncoding (String e)");
	emi1("throws java.io.UnsupportedEncodingException");
	down("{");
	emit("this.encoding = e;");
	emit("this.coder = StringCoderFactory.getStringCoder(e);");
	done("}");
	emit();
	emit("static byte[] $getNodeValue (Token t)");
	down("{");
	emit("return ((com.sun.stc.jcsre.ssc.NodeValue_451)(("
	    + parserName + "ParserToken) t).getMonkEvent()).getValue();");
	done("}");
	emit();
	emit("String $getNodeString (Token t)");
	down("{");
	emit("if (null == coder) throw new RuntimeException"
	    + "(\"no coder set.\");");
	emit("return coder.decode($getNodeValue(t));");
	done("}");
	emit();
	emit("String $getNodeString (Token t, IStringCoder coder)");
	down("{");
	emit("return coder.decode($getNodeValue(t));");
	done("}");
	done("}");

	SSC_File main = fileSet.resolveGlobalTemplate(null);
	for (Iterator iter = fileSet.getFiles(); iter.hasNext(); )
	{
	    SSC_File ssc = (SSC_File) iter.next();
	    genRule(ssc, "obj", (ssc == main));
	}
	while (subRules.size() > 0)
	{
	    Set tmp = subRules;
	    subRules = new HashSet();
	    for (Iterator iter = tmp.iterator(); iter.hasNext(); )
	    {
		MessageStructureNode sub = (MessageStructureNode)iter.next();
		String ruleName = makeRuleName(sub);
		String javaType = makeQName(sub);
		genRule(sub, ruleName, javaType, "obj", false);
	    }
	}
	close();
	genTokenClass(parserName, rootDir, pkg);
	genTokenStreamClass(parserName, rootDir, pkg);
    }

    /**
     * Generates the ANTLR grammar rules for the entire given XSC file.
     *
     * @param file  the XSC file
     * @param accessor  ???
     * @param isMain  flag: is this the main ETD file (not a template)?
     * @throws IOException for output write problems
     */
    private void genRule (SSC_File file, String accessor, boolean isMain)
	throws IOException
    {
        MessageStructureNode node = file.getEtd();
        String ruleName = makeRuleName(node);
        String javaType = makeTypeName(node);
        valueCount = 0;
        iCount = 0;
        subRuleCount = 0;
        genRule(node, ruleName, javaType, accessor, ! isMain);
        String names[] = file.localTemplateNames();
        for (int i = 0; i < names.length; i++)
	{
            node = file.resolveLocalTemplate(names[i]);
            ruleName = makeRuleName(node);
            javaType = makeTypeName(node);
            genRule(node, ruleName, javaType, accessor, true);
        }
    }

    /**
     * Generates the ANTLR grammar rule for the given event node.
     *
     * @param node  ???
     * @param ruleName   ???
     * @param javaType  ???
     * @param accessor  ???
     * @param isForTemplate  ???
     * @throws IOException for output write problems
     */
    private void genRule (MessageStructureNode node, String ruleName,
	String javaType, String accessor, boolean isForTemplate)
	throws IOException
    {
        StringWriter sw = new StringWriter();
        _antlrpw = new PrintWriter(sw);
        genRuleBody(node, ruleName, accessor, null, javaType, isForTemplate);
        _antlrpw.close();
        sw.close();
        String moreRules = sw.toString();
        if (moreRules.length() > 0)
	{
	    emit();
	    emi0(moreRules);
        }
        _antlrpw = null;
    }

    /**
     * Generates the body text of the ANTLR rule.
     * This was added to generate rules that would otherwise have been coded
     * inline to alleviate the "code for a method longer than 65535 bytes"
     * problem. --Rico
     *
     * @param node  ???
     * @param ruleName  ???
     * @param accessor  ???
     * @param idx  ???
     * @param baseClass  ???
     * @param isForTemplate  ???
     * @throws IOException for ANTLR output problems
     */
    private String genRuleBody (MessageStructureNode node, String ruleName,
	String accessor, String idx, String baseClass, boolean isForTemplate)
        throws IOException
    {
	EmitBuffer w = new EmitBuffer(isdent());
        if (ruleName == null)
            ruleName = JCSCompiler.makeRuleName("x"
		+ JCSCompiler.makeJavaName(baseClass)
		+ "__" + makeAntlrName(node.getJavaName())
		+ "__" + subRuleCount++);
        String tokenName = null;
        if (! isForTemplate)
            tokenName = makeTokenName(node, false);
        StringTokenizer accToks = new StringTokenizer(accessor, ".");
        w.emit(ruleName + "[" + baseClass + " " + accToks.nextToken() + "]:");
	w.indent();
        if (! isForTemplate)
            w.emit("START_" + tokenName);
        MessageStructureNode mem = node.getFirstChild();
        for (; mem != null; mem = mem.getNextSibling())
	{
            if (mem.getMaxRep() == 0) continue; // fake node in SSC
            String nodeName = mem.getNodeName();
            String javaName = mem.getJavaName();
	    String uJavaName = uEncode(javaName);

	    if (isGlobalTemplate(mem))
	    {
		resolveGlobalTemplate(mem);
	    }
	    else if (isLocalTemplate(mem))
	    {
		resolveLocalTemplate(mem);
	    }
            String childTokenName = null;

            int min = mem.getMinRep();
            int max = mem.getMaxRep();
	    boolean isRepeat = (max != 1);
	    String iName = "";
	    w.emit("(");
	    w.indent();
	    if (isRepeat)
	    {
                if (isArray(mem))
		{
                    childTokenName = makeTokenName(mem, false);
                    w.emit("(START_" + childTokenName);
		    w.indent();
                }
		iName = "i" + iCount;
		iCount++;
		w.emit("{int " + iName + " = 0;}");
		w.emit("(");
		w.indent();
	    }
	    if (mem.isReference())
	    {
		// Reference to an internal or external template.
		String tokName = makeTokenName(mem, false);
		w.emit("START_" + tokName);
		w.emit(makeRuleName(mem)
		    + "[" + accessor + ".get" + uJavaName
		    + "(" + iName + ")]");
		w.emit("END_" + tokName);
	    }
	    else if (! mem.isLeaf())
	    {
		// Parent-node.
		if (max == 1)
		{
		    // Single occurrence.
		    w.emit(genRuleBody(mem, null,
			accessor + ".get" + uJavaName + "()", null,
			baseClass, false));
		}
		else
		{
		    // Repeated node requires a sub-rule.
		    subRules.add(mem);
		    w.emit(makeRuleName(mem)
			+ "[" + accessor + ".get" + uJavaName
			+ "(" + iName + ")]");
		}
	    }
	    else
	    {
		// Simple leaf-node.
		String tokName = makeTokenName(mem, true);
		if (max > 1) { /* NYI: check upper-bound */ }
		w.emit(
		    genValueRuleBody(_antlrpw, mem, null,
			"START_" + tokName, baseClass, accessor,
			javaName, (isRepeat ? iName : null)));
	    }
	    if (isRepeat) { w.emit("{ " + iName + "++; }"); }
	    w.undent();
            w.emit(")"
                + (max == 1 ? (min == 0 ? "?" : "")
                            : (min == 0 ? "*" : "+")
                )
            );
	    if (isRepeat)
	    {
                if (null != childTokenName)
		{
		    w.undent();
                    w.emit("END_" + childTokenName + ")"
                        + (max == 1 ? (min == 0 ? "?" : "")
                                    : (min == 0 ? "*" : "+")
                        )
                    );
                }
		w.undent();
		w.emit(")");
            }
	}
	if (! isForTemplate)
	    w.emit("END_" + tokenName);
	w.emit(";");
	_antlrpw.println(w.toString());
	String caller = genRuleCaller(ruleName, accessor, idx);
	return caller;
    }

    /**
     * Generate a grammar rule to match the given leaf-node.
     * The action of the rule match is to set the given field, converting the
     * raw data according to the encoding, Java type and format specified in
     * the field descriptor for this node in the XSC file.
     * This was added to generate rules that would otherwise have been coded
     * inline to alleviate the "code for a method longer than 65535 bytes"
     * problem (Rico).
     *
     * @param pw  the output stream for the code
     * @param node  the descriptor of the leaf-node
     * @param ruleName  ???
     * @param tokenName  ???
     * @param baseClass  ???
     * @param accessor  ???
     * @param beanName  ???
     * @param idx  the name of the index variable for repeated nodes, else null
     * @return a code snippet to invoke the generated rule
     */
    private String genValueRuleBody (PrintWriter pw, MessageStructureNode node,
	String ruleName, String tokenName, String baseClass,
	String accessor, String beanName, String idx)
    {
	String type = node.getJavaType();
	String code = node.getEncoding();
	String form = node.getFormat();
	// Internal check.
	if ("".equals(type))
	    throw new RuntimeException("empty [javaType] value");
	if ("".equals(code))
	    throw new RuntimeException("empty [encoding] value");
	if (type == null)
	    type = "java.lang.String";
	String coder = null;
	if (code != null)
	{
	    coder = (String) codeMap.get(code);
	    if (coder == null)
		throw new RuntimeException("no coder for [" + code + "] ?");
	    coder = coderClass + "." + coder;
	}
	if (ruleName == null)
	    ruleName = JCSCompiler.makeRuleName("x"
		+ JCSCompiler.makeJavaName(baseClass)
		+ "__" + makeAntlrName(beanName)
		+ "__" + tokenName
		+ "__" + subRuleCount++);
	StringTokenizer ch = new StringTokenizer(accessor, ".");
	String objStr = ch.nextToken();
	pw.print(ruleName + "[" + baseClass + " " + objStr);
	String iParam = "";
	while (ch.hasMoreTokens())
	{
	    String tmp = ch.nextToken();
	    int openParen = tmp.indexOf('(');
	    int closeParen = tmp.indexOf(')');
	    if ((closeParen-openParen) > 1)
	    {
		String iStr = tmp.substring(openParen+1, closeParen);
		pw.print(", int " + iStr);
	    }
	}
	if (idx != null)
	    pw.print(", int " + idx);
	pw.println("]:");
	pw.print("(value: " + tokenName + " { " + accessor + ".set"
	    + uEncode(beanName) + "(");
	if (idx != null)
	    pw.print(idx + ", ");
	if (type.equals("byte[]"))
	{
	    // Raw value, no decoding or formatting.
	    pw.print("$getNodeValue(value)");
	}
	else // if (type.equals("java.lang.String"))
	{
	    // Encoded value, but no formatting.
	    if (coder == null)
		pw.print("$getNodeString(value)");
	    else
		pw.print("$getNodeString(value, " + coder + ")");
	}
	pw.println("); })");
	pw.println(";");
	pw.println("");
	return genRuleCaller(ruleName, accessor, idx);
    }

    /**
     * ???
     *
     * @param ruleName  ???
     * @param accessor  ???
     * @param idx  expression for the index, or null if not repeated
     */
    private String genRuleCaller (String ruleName, String accessor, String idx)
    {
        StringTokenizer ch = new StringTokenizer(accessor, ".");
        String objStr = ch.nextToken();
        String caller = ruleName + "[" + objStr;
        while (ch.hasMoreTokens())
	{
            String tmp = ch.nextToken();
            int openParen = tmp.indexOf('(');
            int closeParen = tmp.indexOf(')');
            if ((closeParen-openParen) > 1)
	    {
                String iStr = tmp.substring(openParen+1, closeParen);
                caller += ", " + iStr;
            }
        }
        if (idx != null) { caller += ", " + idx; }
        caller += "]";
        return caller;
    }

    /**
     * Generates a top-level class header to the current output.
     *
     * @param pkg  the package name (or null if none)
     * @param className  the unqualified name of the class
     * @param inheritance  a string with the "extends/inherits" info
     * @throws IOException for output write problems
     */
    private void genClassHeader
	(String pkg, String className, String inheritance)
    {
	emit("// Generated by the e*Gate 4.5.1 SSC-based XSC back-end");
	emit("// Modify this at your own risk.");
	emit();
	if (pkg != null)
	    emit("package " + pkg + ";");
	emit();
	emit("public class " + className + inheritance);
	emit("{");
    }

    private HashSet start_tokens = new HashSet();
    private HashSet end_tokens = new HashSet();

    /**
     * ???
     *
     * @param className  ???
     * @param rootDir  ???
     * @param pkg  the package name of the main XSC file
     * @throws IOException for output write problems
     */
    private void genTokenClass (String className, String rootDir, String pkg)
	throws IOException
    {
	String cName = uEncode(className) + "ParserToken";
	String[] ipkgs = null;
	Writer w = setOut(JCSCompiler.package2Path(rootDir, pkg),
	    cName + ".java");
	redent();
	String inheritance = " extends " + antlr + ".Token implements "
	    + cName + "Types";
	genClassHeader(pkg, cName, inheritance);
	indent();
	Map map = tokens;
	emit("static java.util.HashMap startMap = new java.util.HashMap();");
	emit("static java.util.HashMap endMap = new java.util.HashMap();");

	/* Because of the 64K limit on methods, we need to break up the
	 * static initializations.
	 */
	EmitBuffer e = new EmitBuffer(isdent());
	e.indent();
	Iterator iter = tokens.keySet().iterator();
	int i = 1;
	int k = 0;
	while (iter.hasNext())
	{
	    if (k == 0)
	    {
		emit("private static void initTokenMap_" + i + "()");
		down("{");
	    }
	    String name = (String)iter.next();
	    String tok = (String)tokens.get(name);
	    if (! start_tokens.contains(name))
	    {
		start_tokens.add(name);
		emit("startMap.put(\"" + name
		    + "\", new Integer(START_"+ tok + "));");
	    }
	    if (! end_tokens.contains(name))
	    {
		end_tokens.add(name);
		emit("endMap.put(\"" + name
		    + "\", new Integer(END_"+ tok + "));");
	    }
	    k++;
	    if ((k == 50) || !iter.hasNext())
	    {
		done("}");
		e.emit("initTokenMap_" + i + "();");
		i++;
		k = 0;
	    }
	}
	iter = leafTokens.keySet().iterator();
	while (iter.hasNext())
	{
	    if (k == 0)
	    {
		emit("private static void initTokenMap_" + i + "()");
		down("{");
	    }
	    String name = (String)iter.next();
	    String tok = (String)leafTokens.get(name);
	    if (! start_tokens.contains(name))
	    {
		start_tokens.add(name);
		emit("startMap.put(\"" + name
		    + "\", new Integer(START_"+ tok + "));");
	    }
	    k++;
	    if ((k == 100) || !iter.hasNext())
	    {
		done("}");
		e.emit("initTokenMap_" + i + "();");
		i++;
		k = 0;
	    }
	}
	emit("static");
	emit("{");
	emi0(e.toString());
	emit("}");
	e.close();

	emit();
	emit("com.sun.stc.jcsre.ssc.MonkEvent monkEvent;");
	emit("public " + cName + "(com.sun.stc.jcsre.ssc.MonkEvent evt)");
	down("{");
	emit("this.monkEvent = evt;");
	emit("int type = INVALID_TYPE;");
	emit("java.lang.String nodeName = null;");
	emit("java.lang.Integer nodeType = null;");
	emit("if (monkEvent == null)");
	down("{");
	emit("type = EOF_TYPE;");
	done("}");
	emit("else if (monkEvent instanceof com.sun.stc.jcsre.ssc.StartNode)");
	down("{");
	emit("nodeName = ((com.sun.stc.jcsre.ssc.StartNode) monkEvent)"
	    + ".getNodeName();");
	emit("nodeType = (java.lang.Integer)startMap.get(nodeName);");
	emit("if (nodeType == null)");
	down("{");
	emit("throw new com.sun.stc.jcsre.UnmarshalException");
	emi1("(\"Error in data: unknown StartNode encountered; "
	    + "name=\" + nodeName);");
	done("}");
	emit("else");
	down("{");
	emit("type = nodeType.intValue();");
	done("}");
	done("}");
	emit("else if (monkEvent instanceof com.sun.stc.jcsre.ssc.EndNode)");
	down("{");
	emit("nodeName = ((com.sun.stc.jcsre.ssc.EndNode)monkEvent)"
	    + ".getNodeName();");
	emit("nodeType = (java.lang.Integer)endMap.get(nodeName);");
	emit("if (nodeType == null)");
	down("{");
	emit("throw new com.sun.stc.jcsre.UnmarshalException");
	emi1("(\"Error in data: unknown EndNode encountered; "
	    + "name=\" + nodeName);");
	done("}");
	emit("else");
	down("{");
	emit("type = nodeType.intValue();");
	done("}");
	done("}");
	emit("else if (monkEvent instanceof com.sun.stc.jcsre.ssc.NodeValue_451)");
	down("{");
	emit("nodeName = ((com.sun.stc.jcsre.ssc.NodeValue_451) monkEvent)"
	    + ".getNodeName();");
	emit("nodeType = (java.lang.Integer)startMap.get(nodeName);");
	emit("if (nodeType == null)");
	down("{");
	emit("throw new com.sun.stc.jcsre.UnmarshalException");
	emi1("(\"Error in data: unknown NodeValue encountered; "
	    + "name=\" + nodeName);");
	done("}");
	emit("else");
	down("{");
	emit("type = nodeType.intValue();");
	done("}");
	done("}");
	emit("setType(type);");
	done("}");
	emit();
	emit("public int getColumn()");
	emi1("{ return monkEvent.getColumnNumber(); }");
	emit();
	emit("public int getLine()");
	emi1("{ return monkEvent.getLineNumber(); }");
	emit();
	emit("public java.lang.String getText()");
	emi1("{ return toString() + \" token = \" + getType(); }");
	emit();
	emit("public com.sun.stc.jcsre.ssc.MonkEvent getMonkEvent()");
	emi1("{ return monkEvent; }");
	emit();
	emit("public java.lang.String toString()");
	emi1("{ return monkEvent.toString(); }");
	done("}");
	close();
    }

    /**
     * ???
     *
     * @param className  ???
     * @param rootDir  ???
     * @param pkg  the package name of the main XSC file
     * @throws IOException for output write problems
     */
    private void genTokenStreamClass
	(String className, String rootDir, String pkg)
	throws IOException
    {
	String cName = (className);
	String fName = cName + "ParserTokenStream.java";
	Writer w = setOut(JCSCompiler.package2Path(rootDir, pkg), fName);
	redent();
	if (pkg != null)
	    emit("package " + pkg + ";");
	emit();
	emit("class " + cName
	    + "ParserTokenStream implements " + antlr + ".TokenStream");
	down("{");
	emit("com.sun.stc.jcsre.ssc.IMonkLexer monkLexer;");
	emit("boolean hitEof;");
	emit();
	emit("public " + cName
	    + "ParserTokenStream(com.sun.stc.jcsre.ssc.IMonkLexer lexer)");
	down("{");
	emit("monkLexer = lexer;");
	done("}");
	emit();
	emit("public " + antlr + ".Token nextToken ()");
	emi1("throws " + antlr + ".TokenStreamException");
	down("{");
	emit("if (hitEof)");
	emi1("return new " + antlr + ".Token(" + antlr
	    + ".Token.EOF_TYPE);");
	emit();
	emit("com.sun.stc.jcsre.ssc.MonkEvent event = monkLexer.lex();");
	emit("if (event == null)");
	down("{");
	emit("hitEof = true;");
	emit("return new " + antlr + ".Token(" + antlr
	    + ".Token.EOF_TYPE);");
	done("}");
	emit("if (event instanceof com.sun.stc.jcsre.ssc.MonkException)");
	emi1("throw new " + antlr
	    + ".TokenStreamException(event.toString());");
	emit("return new " + cName + "ParserToken(event);");
	done("}");
	done("}");
	close();
    }

    private Hashtable tokens = new Hashtable();
    private Hashtable leafTokens = new Hashtable();

    /**
     * Given a generic Unicode name string, make a name acceptable for ANTLR.
     * ANTLR is the parser generator package that we use to generate part of
     * the message data unmarshaling (i.e. parsing) code.
     * ANTLR names are like C identifiers, only ASCII alphanumeric
     * and underscore.  This maps a generic Unicode string by escaping
     * "_" and "__", and the rest as "_u" + 4-digit hex charcode.
     * If the "untlr" flag is set, assume that we have a modified
     * Unicode-ANTLR that accpets full Java identifier syntax.
     *
     * @param name  a Unicode string containing any characters
     * @return a name acceptable to ANTLR
     */
    private String makeAntlrName (String name)
    {
	if (untlr)
	{
	    // Yes, ANTLR groks Unicode names.
	    return JCSCompiler.makeJavaName(name);
	}

	int len = name.length();
	StringBuffer b = new StringBuffer(len);
	boolean esc = false;
	for (int i = 0; i < len; i ++)
	{
	    char c = name.charAt(i);
	    if (('a' <= c && c <= 'z') ||
		('A' <= c && c <= 'Z') ||
		('0' <= c && c <= '9'))
	    {
		// Stays the same: a-z, A-Z, 0-9.
		if (esc && c != 'u') b.append('_');
		b.append(c);
		esc = false;
	    }
	    else if (c == '_')
	    {
		// Escaped.
		b.append("__");
	    }
	    else
	    {
		// Transform rest to Unicode-escape.
		b.append("_u");
		for (int sh = 12; sh >= 0; sh -= 4)
		    b.append("0123456789ABCDEF".charAt((c >> sh) & 0xF));
		esc = true;
	    }
	}
	return b.toString();
    }

    /**
     * Makes token name for given node.  Note that if two nodes have the same
     * name in Monk, then the token name should be the same as well,
     * because the lexer would not know how to distinguish the strings.
     * Hence, we cannot use the node's (disambiguated) Java name here.
     *
     * @param node  a non-null structure node reference
     * @param leaf  flag: does (expanded) node lack children?
     * @return the token name string
     */
    private String makeTokenName (MessageStructureNode node, boolean leaf)
    {
	String name = node.getNodeName();
	String tokenName = makeAntlrName(name);
	if (debug)
	    System.out.println("[ makeTokenName <" + name + ">, <"
		+ tokenName + ">, leaf=" + leaf + " ]");
	if (leaf)
	    leafTokens.put(name, tokenName);
	else
	    tokens.put(name, tokenName);
	return tokenName;
    }

    /**
     * Makes name for rule in ANTLR grammar file.
     * Name must start with a lower-case letter, so we prepend an "x".
     * Potential problem: the names may get very long for deeply nested
     * message structures.
     *
     * @param node  a non-null structure node reference
     * @return the rule name string
     */
    private String makeRuleName (MessageStructureNode node)
    {
        String prefix = (String) fileMap.get(node.getFile());

        if (node.getFile().getEtd() == node)
            return prefix + makeAntlrName(node.getJavaName());

        if (isLocalTemplate(node))
        {
            // Reference to internal template.
            MessageStructureNode ref = resolveLocalTemplate(node);
            return prefix + makeAntlrName(ref.getJavaName());
        }
        if (isGlobalTemplate(node))
        {
            // Reference to external template.
            MessageStructureNode ref = resolveGlobalTemplate(node);
	    prefix = (String) fileMap.get(ref.getFile());
            return prefix + makeAntlrName(ref.getJavaName());
        }

        // For nested rule, make name by prefixing all ancestor names.
        LinkedList list = new LinkedList();
        while (node != null)
	{
            list.addFirst(makeAntlrName(node.getJavaName()));
            node = node.getParent();
        }
        Iterator iter = list.iterator();
        StringBuffer buf = new StringBuffer();
        String separator = prefix;
        while (iter.hasNext())
	{
            buf.append(separator);
            buf.append((String) iter.next());
            separator = "__";
        }
        return buf.toString();
    }

    /**
     * Given a node, returns a qualified node name by prefixing all
     * of its ancestors' names, separated by ".".
     *
     * @param node  a non-null structure node reference
     * @return the fully qualified node name path
     */
    private String makeQName (MessageStructureNode node)
    {
        if (node.getFile().getEtd() == node)
        {
            // The top-level node has no ancestors.
            return "x" + node.getJavaName();
        }
        else
        {
            MessageStructureNode tmp = node;
            LinkedList list = new LinkedList();
            while (tmp != null)
            {
                list.addFirst(tmp.getJavaName());
                tmp = tmp.getParent();
            }
            Iterator iter = list.iterator();
            StringBuffer buf = new StringBuffer();
            String separator = "";
            while (iter.hasNext())
            {
                buf.append(separator);
                buf.append((String) iter.next());
                separator = ".";
            }
            return buf.toString();
        }
    }

    /**
     * Return the package prefix for the given XSC file, if it is
     * different from the (possibly null) second XSC file.  If the
     * two are the same, this returns an empty prefix.  The prefix
     * (if non-empty) ends with ".".
     *
     * @param ref  the XSC file whose prefix we want
     * @param ssc  another XSC file, possibly null, to compare
     */
    public static String packPrefix (SSC_File ref, SSC_File ssc)
    {
	String pack = ref.getPackageName();
	return
	    (ref == ssc || pack == null ||
		(ssc != null && pack.equals(ssc.getPackageName())))
	    ? ""
	    : (pack.equals("") || pack.endsWith("."))
	    ? pack
	    : (pack = pack + ".");
    }

    /**
     * Returns the class name of a field's type.
     * For composite nodes, the (unqualified) name of the generated
     * Java class is the same as the node's Java name.
     *
     * @param node  a non-null structure node reference
     * @return the name of the class generated to store this field
     */
    private String makeTypeName (MessageStructureNode node)
    {
	return packPrefix(node.getFile(), null) + node.getJavaName();
    }

    /**
     * Gets given string expressed as Java string literal.
     * Also works for null.
     *
     * @param value  the Unicode string to convert
     * @return a quoted, flat-ASCII Java string literal (code snippet)
     */
    public static String makeStringLiteral (String value)
    {
	if (value == null) { return "null"; }
	StringBuffer buf = new StringBuffer(value.length() + 2);
	buf.append('"');
	for (int i = 0, len = value.length(); i < len; i++)
	{
	    char ch = value.charAt(i);
	    switch (ch)
	    {
		// The usual special cases.
	    case '\b': buf.append("\\b");  break;
	    case '\n': buf.append("\\n");  break;
	    case '\r': buf.append("\\r");  break;
	    case '\t': buf.append("\\t");  break;
	    case '\\': buf.append("\\\\"); break;
	    case '\"': buf.append("\\\""); break;

	    default:
		if ('\u0020' <= ch && ch <= '\u007E')
		{
		    // Simple ASCII character.
		    buf.append(ch);
		}
		else
		{
		    // Unprintable or non-ASCII: do 4 hex-digit Unicode escape.
		    buf.append("\\u");
		    for (int n = 12; n >= 0; n -= 4)
			buf.append("0123456789ABCDEF".charAt((ch >> n) & 0xF));
		    continue;
		}
	    }
	}
	buf.append("\"");
	return buf.toString();
    }

    /**
     * Returns plain 7-bit ASCII string equivalent, in which "\\" and
     * all chars over 127 have been escaped with backslash-U sequences.
     * This can be used to incorporate non-ASCII chars in Java source.
     * We go to some effort to do nothing when fed pure ASCII here.
     *
     * @param value  the Unicode string to encode
     * @return the encoded, plain-ASCII string
     */
    public static String uEncode (String value)
    {
	if (value == null) { return null; }
	StringBuffer buf = null;
	boolean escaped = false;
	for (int i = 0, len = value.length(); i < len; i++)
	{
	    char ch = value.charAt(i);
	    //if (ch == '\\' || ch > 0x7E) //QAI 46896
	    if (ch > 0x7E)
	    {
		// Not simple ASCII.
		if (! escaped)
		{
		    // First time we found non-ASCII.
		    buf = new StringBuffer(value.length());
		    buf.append(value.substring(0, i));
		    escaped = true;
		}
		buf.append('\\');
		buf.append('u');
		for (int n = 12; n >= 0; n -= 4)
		    buf.append("0123456789ABCDEF".charAt((ch >> n) & 0xF));
	    }
	    else
	    {
		// Simple ASCII, don't escape.
		if (escaped) { buf.append(ch); }
	    }
	}
	return (escaped ? buf.toString() : value);
    }

    /**
     * Generates the accessor methods for a non-repeated node.
     *
     * @param javaType  the implementation class or primitive, if known
     * @param javaName  the basename of the accessors and the variable
     * @param nickName  if not null: alias for accessors (XSC 0.3 compatibility)
     * @param readable  flag: node should have "get" method etc.?
     * @param writable  flag: node should have "set" method etc.?
     * @param isChoice  flag: node is optional?
     */
    public void genClassAccessors (String javaType, String javaName,
	String nickName, boolean readable, boolean writable, boolean isChoice)
    {
	String uJavaName = uEncode(javaName);
	String uNickName = uEncode(nickName);
	String uJavaType = uEncode(javaType);

	String init = "new " + uJavaType + "()";
	String boxedType = null;
	String accessor = "";
	boolean primitive = false;

	for (int i = 0; i < primitiveArray.length; i++)
	{
	    if (javaType.equals(primitiveArray[i][0]))
	    {
		primitive = true;
		boxedType = primitiveArray[i][1];
		init = primitiveArray[i][2];
		accessor = primitiveArray[i][3];
		break;
	    }
	}
	emit();

	if (readable)
	{
	    emit("public " + uJavaType + " get" + uJavaName + " ()");
	    down("{");
	    if (!primitive)
	    {
		if (writable)
		{
		    emit("if (_" + uJavaName + " == null) set" + uJavaName
			+ "(" + init  + ");");
		}
		else
		{
		    emit("if (_" + uJavaName + " == null) { _" + uJavaName
			+ " = " + init  + "; }");
		}
	    }
	    emit("return _" + uJavaName + ";");
	    done("}");
	}

	if (writable)
	{
	    emit("public void set" + uJavaName + " (" + uJavaType + " val)");
	    down("{");

	    // Emit code to throw exception if null is passed.
	    if (!JCSCompiler.isPrimitiveType(javaType))
	    {
		down("if (null == val)");
		emit("throw new IllegalArgumentException");
		emi1("(\"Argument cannot be null. Use omit"
		    + uJavaName + " method to clear field.\");");
		undent();
	    }
	    emit("_" + uJavaName + " = val;");
	    emit("hasMasks[" + bminx + uJavaName + "] |= " + bmask + uJavaName
		+ ";");
	    done("}");

	    if (!"java.lang.String".equals(javaType))
	    {
		emit("public void set" + uJavaName + " (java.lang.String val)");
		down("{");
		down("if (null == val)");
		emit("throw new IllegalArgumentException");
		emi1("(\"Argument cannot be null. Use omit" + uJavaName
		    + " method to clear field.\");");
		undent();

		if (JCSCompiler.isNumericType(javaType))
		{
		    emit("try {");
		    emi1("set" + uJavaName + "(" + nform + uJavaName
			+ ".parse(val)." + uJavaType + "Value());");
		    emit("} catch (java.text.ParseException ex)");
		    emi1("{ throw new com.sun.stc.jcsre.UnmarshalException"
			+ "(ex.getMessage()); }");
		}
		else if ("boolean".equals(javaType))
		{
		    emit("try {");
		    emi1("set" + uJavaName + "(" + bform + uJavaName
			+ ".parse(val));");
		    emit("} catch (java.text.ParseException ex)");
		    emi1("{ throw new com.sun.stc.jcsre.UnmarshalException"
			+ "(ex.getMessage()); }");
		}
		else if ("char".equals(javaType))
		{
		    emit("set" + uJavaName + "(val.charAt(0));");
		}
		else
		{
		    emit("throw new UnsupportedOperationException"
			+ "(\"not implemented.\");");
		}
		done("}");
	    }
	}
	if (nickName != null && ! nickName.equalsIgnoreCase(uJavaName))
	//if (nickName != null && ! nickName.equals(javaName)) //QAI 46896
	{
	    // Generate accessor aliases.
	    if (readable)
	    {
		emit("public " + uJavaType + " get" + uNickName + " ()");
		emi1("{ return get" + uJavaName + "(); }");
	    }
	    if (writable)
	    {
		emit("public void set" + uNickName + " ("
		    + uJavaType + " val)");
		emi1("{ set" + uJavaName + "(val); }");
	    }
	    if (true)
	    {
		emit("public boolean has" + uNickName + " ()");
		emi1("{ return has" + uJavaName + "(); }");
	    }
	}

	//- if (isChoice)
	{
	    emit("public boolean has" + uJavaName + " ()");
	    down("{");
	    emit("return 0 != (hasMasks[" + bminx + uJavaName + "] & " + bmask
		+ uJavaName + ");");
	    done("}");
	}
	if (writable && isChoice)
	{
	    emit("public void omit" + uJavaName + " ()");
	    down("{");
	    emit("hasMasks[" + bminx + uJavaName + "] &= ~" + bmask
		+ uJavaName + ";");
	    done("}");
	}
    }

    /**
     * Generates the accessor methods for a repeated node.
     *
     * @param javaType  the implementation class or primitive, if known
     * @param javaName  the basename of the accessors and the variable
     * @param nickName  if not null: alias for accessors (XSC 0.3 compatibility)
     * @param readable  flag: node should have "get" method etc.?
     * @param writable  flag: node should have "set" method etc.?
     * @param isChoice  flag: node is optional?
     */
    public void genCollectionAccessors(String javaType, String javaName,
	String nickName, boolean readable, boolean writable, boolean isChoice)
    {
	String uJavaName = uEncode(javaName);
	String uNickName = uEncode(nickName);
	String uJavaType = uEncode(javaType);

	String castType = javaType;
	String init = "new " + javaType + "()";
	String boxedType = null;
	String accessor = "";
	for (int i = 0; i < primitiveArray.length; i++)
	{
	    if (javaType.equals(primitiveArray[i][0]))
	    {
		boxedType = primitiveArray[i][1];
		castType = boxedType;
		init = primitiveArray[i][2];
		accessor = primitiveArray[i][3];
		break;
	    }
	}
	if (readable)
	{
	    emit("public " + uJavaType + " get" + uJavaName + " (int i)");
	    down("{");
	    emit("if (i == _" + uJavaName + ".size()) _" + uJavaName + ".add("
		+ init + ");");
	    emit("return ((" + castType + ") _" + uJavaName + ".get(i))"
		+ accessor + ";");
	    done("}");
	}
	if (writable)
	{
	    // set(index, value)
	    emit("public void set" + uJavaName + " (int i, " + uJavaType
		+ " val)");
	    down("{");
	    if (!JCSCompiler.isPrimitiveType(javaType))
	    {
		// Emit code to throw exception if null is passed.
		down("if (null == val)");
		emit("throw new IllegalArgumentException");
		emi1("(\"Argument cannot be null. Use remove" + uJavaName
		    + " method to clear element.\");");
		undent();
	    }

	    if (boxedType != null && accessor.length() > 0)
	    {
		emit("if (i == _" + uJavaName + ".size()) _" + uJavaName+
		     ".add(new " + boxedType + "(val));");
		emit("else _" + uJavaName + ".set(i, new " + boxedType
		    + "(val));");
	    } else {
		emit("if (i == _" + uJavaName + ".size()) _" + uJavaName+
		     ".add(val); else _" + uJavaName + ".set(i, val);");
	    }
	    done("}");

	    if (!"java.lang.String".equals(javaType))
	    {

		emit("public void set" + uJavaName
		    + "(int i, java.lang.String val)");
		down("{");

		down("if (null == val)");
		emit("throw new IllegalArgumentException");
		emi1("(\"Argument cannot be null. Use remove" + uJavaName
		    + " method to clear element.\");");
		undent();

		if (JCSCompiler.isNumericType(javaType))
		{
		    emit("try {");
		    emi1("set" + uJavaName + "(i, " + nform + uJavaName
			+ ".parse(val)." + uJavaType + "Value());"
		   );
		    emit("} catch (java.text.ParseException ex)");
		    emi1("{ throw new com.sun.stc.jcsre.UnmarshalException"
			+ "(ex.getMessage()); }");
		}
		else if ("boolean".equals(javaType))
		{
		    emit("try {");
		    emi1("set" + uJavaName + "(i, " + bform + uJavaName
			+ ".parse(val));"
		    );
		    emit("} catch (java.text.ParseException ex)");
		    emi1("{ throw new com.sun.stc.jcsre.UnmarshalException"
			+ "(ex.getMessage()); }");
		}
		else if ("char".equals(javaType))
		{
		    emit("set" + uJavaName + "(i, val.charAt(0));");
		}
		else
		{
		    emit("throw new UnsupportedOperationException"
			+ "(\"not implemented.\");");
		}
		done("}");
	    }

	}
	if (nickName != null && ! nickName.equalsIgnoreCase(uJavaName))
	//if (nickName != null && ! nickName.equals(javaName)) //QAI 46896
	{
	    // Generate accessor aliases.
	    if (readable)
	    {
		emit("public " + uJavaType + " get" + uNickName + " (int i)");
		emi1("{ return get" + uJavaName + "(i); }");
	    }
	    if (writable)
	    {
		emit("public void set" + uNickName + " (int i, "
		    + uJavaType + " val)");
		emi1("{ set" + uJavaName + "(i, val); }");
	    }
	    if (isChoice)
	    {
		emit("public boolean has" + uNickName + " ()");
		emi1("{ return has" + uJavaName + "(); }");
	    }
	    emit("public int count" + uNickName + " ()");
	    emi1("{ return count" + uJavaName + "(); }");
	}

	// Generate array getters and setters.
	if (JGen.arrayGetSet)
	{
	    if (readable)
	    {
		emit("public " + uJavaType + "[] get" + uJavaName + "()");
		down("{");
		if ("byte[]".equals(javaType))
		{
		    emit(uJavaType + "[] tmp = new byte[_" + uJavaName
			+ ".size()][];");
		}
		else
		{
		    emit(uJavaType + "[] tmp = new " + uJavaType
			+ "[_" + uJavaName + ".size()];");
		}
		if (boxedType != null && accessor.length() > 0)
		{
		    emit("for (int i = 0, len = _" + uJavaName
			+ ".size(); i < len; i++) tmp[i] = ((" + boxedType
			+ ")_" + uJavaName + ".get(i))" + accessor + ";");
		}
		else
		{
		    emit("_" + uJavaName + ".toArray(tmp);");
		}
		emit("return tmp;");
		done("}");
	    }
	    if (writable)
	    {
		// set(value[])
		emit("public void set" + uJavaName + "(" + uJavaType
		    + " [] val)");
		down("{");
		down("if (null == val)");
		emit("throw new IllegalArgumentException");
		emi1("(\"Argument cannot be null. Use clear" + uJavaName
		    + " method to clear field.\");");
		undent();

		emit("_" + uJavaName + " = new java.util.Vector(val.length);");
		if (boxedType != null && accessor.length() > 0)
		{
		    emit("for (int i = 0; i < val.length; i++) _"
			+ uJavaName + ".add(new " + boxedType + "(val[i]));");
		}
		else
		{
		    emit("for (int i = 0; i < val.length; i++) _"
			+ uJavaName + ".add(val[i]);");
		}
		done("}");
	    }
	}
	// count
	emit("public int count" + uJavaName + " ()");
	emi1("{ return _" + uJavaName + ".size(); }");
	if (isChoice)
	{
	    //has
	    emit("public boolean has" + uJavaName + " ()");
	    emi1("{ return _" + uJavaName + ".size() > 0; }");
	}

	if (writable)
	{
	    // remove
	    emit("public void remove" + uJavaName + " (int index)");
	    emi1("{ _" + uJavaName + ".remove(index); }");

	    // add(value)
	    emit();
	    emit("public void add" + uJavaName + "(" + uJavaType + " value)");
	    down("{");
	    if (boxedType != null && accessor.length() > 0)
	    {
		emit("_" + uJavaName + ".add(new " + boxedType + "(value));");
	    }
	    else
	    {
		emit("_" + uJavaName + ".add(value);");
	    }
	    done("}");

	    // add(index, value)
	    emit("public void add" + uJavaName + "(int index, "
		+ uJavaType + " value)");
	    down("{");
	    if (boxedType != null && accessor.length() > 0)
	    {
		emit("_" + uJavaName + ".add(index, new " + boxedType
		    + "(value));");
	    }
	    else
	    {
		emit("_" + uJavaName + ".add(index, value);");
	    }
	    done("}");

	    // clear
	    emit("public void clear" + uJavaName + " ()");
	    emi1("{ _" + uJavaName + ".clear(); }");
	}
    }
}

