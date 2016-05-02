// XmlParser.java: the main parser class.
// NO WARRANTY! See README, and copyright below.
// $Id: XmlParser.java,v 1.1.1.1 2004/01/26 21:52:02 hyuklim Exp $

package com.microstar.xml;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;


/**
  * Parse XML documents and return parse events through call-backs.
  * <p>You need to define a class implementing the <code>XmlHandler</code>
  * interface: an object belonging to this class will receive the
  * callbacks for the events.  (As an alternative to implementing
  * the full XmlHandler interface, you can simply extend the 
  * <code>HandlerBase</code> convenience class.)
  * <p>Usage (assuming that <code>MyHandler</code> is your implementation
  * of the <code>XmlHandler</code> interface):
  * <pre>
  * XmlHandler handler = new MyHandler();
  * XmlParser parser = new XmlParser();
  * parser.setHandler(handler);
  * try {
  *   parser.parse("http://www.host.com/doc.xml", null);
  * } catch (Exception e) {
  *   [do something interesting]
  * }
  * </pre>
  * <p>Alternatively, you can use the standard SAX interfaces
  * with the <code>SAXDriver</code> class as your entry point.
  * @author Copyright (c) 1997, 1998 by Microstar Software Ltd.
  * @author Written by David Megginson &lt;dmeggins@microstar.com&gt;
  * @version 1.1
  * @see XmlHandler
  * @see HandlerBase
  */
public class XmlParser {

  //
  // Use special cheats that speed up the code (currently about 50%),
  // but may cause problems with future maintenance and add to the
  // class file size (about 500 bytes).
  //
  private final static boolean USE_CHEATS = true;



  //////////////////////////////////////////////////////////////////////
  // Constructors.
  ////////////////////////////////////////////////////////////////////////


  /**
    * Construct a new parser with no associated handler.
    * @see #setHandler
    * @see #parse
    */
  public XmlParser ()
  {
  }


  /**
    * Set the handler that will receive parsing events.
    * @param handler The handler to receive callback events.
    * @see #parse
    * @see XmlHandler
    */
  public void setHandler (XmlHandler handler)
  {
    this.handler = handler;
  }


  /**
    * Parse an XML document from a URI.
    * <p>You may parse a document more than once, but only one thread
    * may call this method for an object at one time.
    * @param systemId The URI of the document.
    * @param publicId The public identifier of the document, or null.
    * @param encoding The suggested encoding, or null if unknown.
    * @exception java.lang.Exception Any exception thrown by your
    *            own handlers, or any derivation of java.io.IOException
    *            thrown by the parser itself.
    */
  public void parse (String systemId, String publicId, String encoding)
    throws java.lang.Exception
  {
    doParse(systemId, publicId, null, null, encoding);
  }


  /**
    * Parse an XML document from a byte stream.
    * <p>The URI that you supply will become the base URI for
    * resolving relative links, but &AElig;lfred will actually read
    * the document from the supplied input stream.
    * <p>You may parse a document more than once, but only one thread
    * may call this method for an object at one time.
    * @param systemId The base URI of the document, or null if not
    *                 known.
    * @param publicId The public identifier of the document, or null
    *                 if not known.
    * @param stream A byte input stream.
    * @param encoding The suggested encoding, or null if unknown.
    * @exception java.lang.Exception Any exception thrown by your
    *            own handlers, or any derivation of java.io.IOException
    *            thrown by the parser itself.
    */
  public void parse (String systemId, String publicId,
         InputStream stream, String encoding)
    throws java.lang.Exception
  {
    doParse(systemId, publicId, null, stream, encoding);
  }


  /**
    * Parse an XML document from a character stream.
    * <p>The URI that you supply will become the base URI for
    * resolving relative links, but &AElig;lfred will actually read
    * the document from the supplied input stream.
    * <p>You may parse a document more than once, but only one thread
    * may call this method for an object at one time.
    * @param systemId The base URI of the document, or null if not
    *                 known.
    * @param publicId The public identifier of the document, or null
    *                 if not known.
    * @param reader A character stream.
    * @exception java.lang.Exception Any exception thrown by your
    *            own handlers, or any derivation of java.io.IOException
    *            thrown by the parser itself.
    */
  public void parse (String systemId, String publicId, Reader reader)
    throws java.lang.Exception
  {
    doParse(systemId, publicId, reader, null, null);
  }


  private synchronized void doParse (String systemId, String publicId,
             Reader reader, InputStream stream,
             String encoding)
    throws java.lang.Exception
  {
    basePublicId = publicId;
    baseURI = systemId;
    baseReader = reader;
    baseInputStream = stream;

    initializeVariables();

        // Set the default entities here.
    setInternalEntity(intern("amp"), "&#38;");
    setInternalEntity(intern("lt"), "&#60;");
    setInternalEntity(intern("gt"), "&#62;");
    setInternalEntity(intern("apos"), "&#39;");
    setInternalEntity(intern("quot"), "&#34;");

    if (handler != null) {
      handler.startDocument();
    }

    pushURL("[document]", basePublicId, baseURI, baseReader, baseInputStream,
      encoding);

    parseDocument();

    if (handler != null) {
      handler.endDocument();
    }
    cleanupVariables();
  }



  ////////////////////////////////////////////////////////////////////////
  // Constants.
  ////////////////////////////////////////////////////////////////////////

  //
  // Constants for element content type.
  //

  /**
    * Constant: an element has not been declared.
    * @see #getElementContentType
    */
  public final static int CONTENT_UNDECLARED = 0;

  /**
    * Constant: the element has a content model of ANY.
    * @see #getElementContentType
    */
  public final static int CONTENT_ANY = 1;

  /**
    * Constant: the element has declared content of EMPTY.
    * @see #getElementContentType
    */
  public final static int CONTENT_EMPTY = 2;

  /**
    * Constant: the element has mixed content.
    * @see #getElementContentType
    */
  public final static int CONTENT_MIXED = 3;

  /**
    * Constant: the element has element content.
    * @see #getElementContentType
    */
  public final static int CONTENT_ELEMENTS = 4;


  //
  // Constants for the entity type.
  //

  /**
    * Constant: the entity has not been declared.
    * @see #getEntityType
    */
  public final static int ENTITY_UNDECLARED = 0;

  /**
    * Constant: the entity is internal.
    * @see #getEntityType
    */
  public final static int ENTITY_INTERNAL = 1;

  /**
    * Constant: the entity is external, non-XML data.
    * @see #getEntityType
    */
  public final static int ENTITY_NDATA = 2;

  /**
    * Constant: the entity is external XML data.
    * @see #getEntityType
    */
  public final static int ENTITY_TEXT = 3;


  //
  // Constants for attribute type.
  //

  /**
    * Constant: the attribute has not been declared for this element type.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_UNDECLARED = 0;

  /**
    * Constant: the attribute value is a string value.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_CDATA = 1;

  /**
    * Constant: the attribute value is a unique identifier.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_ID = 2;

  /**
    * Constant: the attribute value is a reference to a unique identifier.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_IDREF = 3;

  /**
    * Constant: the attribute value is a list of ID references.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_IDREFS = 4;

  /**
    * Constant: the attribute value is the name of an entity.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_ENTITY = 5;

  /**
    * Constant: the attribute value is a list of entity names.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_ENTITIES = 6;

  /**
    * Constant: the attribute value is a name token.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_NMTOKEN = 7;

  /**
    * Constant: the attribute value is a list of name tokens.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_NMTOKENS = 8;

  /**
    * Constant: the attribute value is a token from an enumeration.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_ENUMERATED = 9;

  /**
    * Constant: the attribute is the name of a notation.
    * @see #getAttributeType
    */
  public final static int ATTRIBUTE_NOTATION = 10;


  //
  // When the class is loaded, populate the hash table of
  // attribute types.
  //

  /**
    * Hash table of attribute types.
    */
  private static Hashtable attributeTypeHash;
  static {
    attributeTypeHash = new Hashtable();
    attributeTypeHash.put("CDATA", new Integer(ATTRIBUTE_CDATA));
    attributeTypeHash.put("ID", new Integer(ATTRIBUTE_ID));
    attributeTypeHash.put("IDREF", new Integer(ATTRIBUTE_IDREF));
    attributeTypeHash.put("IDREFS", new Integer(ATTRIBUTE_IDREFS));
    attributeTypeHash.put("ENTITY", new Integer(ATTRIBUTE_ENTITY));
    attributeTypeHash.put("ENTITIES", new Integer(ATTRIBUTE_ENTITIES));
    attributeTypeHash.put("NMTOKEN", new Integer(ATTRIBUTE_NMTOKEN));
    attributeTypeHash.put("NMTOKENS", new Integer(ATTRIBUTE_NMTOKENS));
    attributeTypeHash.put("NOTATION", new Integer(ATTRIBUTE_NOTATION));
  }


  //
  // Constants for supported encodings.
  //
  private final static int ENCODING_UTF_8 = 1;
  private final static int ENCODING_ISO_8859_1 = 2;
  private final static int ENCODING_UCS_2_12 = 3;
  private final static int ENCODING_UCS_2_21 = 4;
  private final static int ENCODING_UCS_4_1234 = 5;
  private final static int ENCODING_UCS_4_4321 = 6;
  private final static int ENCODING_UCS_4_2143 = 7;
  private final static int ENCODING_UCS_4_3412 = 8;


  //
  // Constants for attribute default value.
  //

  /**
    * Constant: the attribute is not declared.
    * @see #getAttributeDefaultValueType
    */
  public final static int ATTRIBUTE_DEFAULT_UNDECLARED = 0;

  /**
    * Constant: the attribute has a literal default value specified.
    * @see #getAttributeDefaultValueType
    * @see #getAttributeDefaultValue
    */
  public final static int ATTRIBUTE_DEFAULT_SPECIFIED = 1;

  /**
    * Constant: the attribute was declared #IMPLIED.
    * @see #getAttributeDefaultValueType
    */
  public final static int ATTRIBUTE_DEFAULT_IMPLIED = 2;

  /**
    * Constant: the attribute was declared #REQUIRED.
    * @see #getAttributeDefaultValueType
    */
  public final static int ATTRIBUTE_DEFAULT_REQUIRED = 3;

  /**
    * Constant: the attribute was declared #FIXED.
    * @see #getAttributeDefaultValueType
    * @see #getAttributeDefaultValue
    */
  public final static int ATTRIBUTE_DEFAULT_FIXED = 4;


  //
  // Constants for input.
  //
  private final static int INPUT_NONE = 0;
  private final static int INPUT_INTERNAL = 1;
  private final static int INPUT_EXTERNAL = 2;
  private final static int INPUT_STREAM = 3;
  private final static int INPUT_BUFFER = 4;
  private final static int INPUT_READER = 5;


  //
  // Flags for reading literals.
  //
  private final static int LIT_CHAR_REF = 1;
  private final static int LIT_ENTITY_REF = 2;
  private final static int LIT_PE_REF = 4;
  private final static int LIT_NORMALIZE = 8;


  //
  // Flags for parsing context.
  //
  private final static int CONTEXT_NONE = 0;
  private final static int CONTEXT_DTD = 1;
  private final static int CONTEXT_ENTITYVALUE = 2;
  private final static int CONTEXT_ATTRIBUTEVALUE = 3;



  //////////////////////////////////////////////////////////////////////
  // Error reporting.
  //////////////////////////////////////////////////////////////////////


  /**
    * Report an error.
    * @param message The error message.
    * @param textFound The text that caused the error (or null).
    * @see XmlHandler#error
    * @see #line
    */
  void error (String message, String textFound, String textExpected)
    throws java.lang.Exception
  {
    errorCount++;
    if (textFound != null) {
      message = message + " (found \"" + textFound + "\")";
    }
    if (textExpected != null) {
      message = message + " (expected \"" + textExpected + "\")";
    }
    if (handler != null) {
      String uri = null;

      if (externalEntity != null) {
  uri = externalEntity.getURL().toString();
      }
      handler.error(message, uri, line, column);
    }
  }


  /**
    * Report a serious error.
    * @param message The error message.
    * @param textFound The text that caused the error (or null).
    */
  void error (String message, char textFound, String textExpected)
    throws java.lang.Exception
  {
    error(message, new Character(textFound).toString(), textExpected);
  }



  //////////////////////////////////////////////////////////////////////
  // Major syntactic productions.
  //////////////////////////////////////////////////////////////////////


  /**
    * Parse an XML document.
    * <pre>
    * [1] document ::= prolog element Misc*
    * </pre>
    * <p>This is the top-level parsing function for a single XML
    * document.  As a minimum, a well-formed document must have
    * a document element, and a valid document must have a prolog
    * as well.
    */
  void parseDocument ()
    throws java.lang.Exception
    {
    char c;

    parseProlog();
    require('<');
    parseElement();
    try
      {
      parseMisc();  //skip all white, PIs, and comments
      c=readCh();   //if this doesn't throw an exception...
      error("unexpected characters after document end",c,null);
      }
    catch (EOFException e)
      {return;}
    }


  /**
    * Skip a comment.
    * <pre>
    * [18] Comment ::= '&lt;!--' ((Char - '-') | ('-' (Char - '-')))* "-->"
    * </pre>
    * <p>(The <code>&lt;!--</code> has already been read.)
    */
  void parseComment ()
    throws java.lang.Exception
  {
    skipUntil("-->");
  }


  /**
    * Parse a processing instruction and do a call-back.
    * <pre>
    * [19] PI ::= '&lt;?' Name (S (Char* - (Char* '?&gt;' Char*)))? '?&gt;'
    * </pre>
    * <p>(The <code>&lt;?</code> has already been read.)
    * <p>An XML processing instruction <em>must</em> begin with
    * a Name, which is the instruction's target.
    */
  void parsePI ()
    throws java.lang.Exception
  {
    String name;

    name = readNmtoken(true);
    if (!tryRead("?>")) {
      requireWhitespace();
      parseUntil("?>");
    }
    if (handler != null) {
      handler.processingInstruction(name, dataBufferToString());
    }
  }


  /**
    * Parse a CDATA marked section.
    * <pre>
    * [20] CDSect ::= CDStart CData CDEnd
    * [21] CDStart ::= '&lt;![CDATA['
    * [22] CData ::= (Char* - (Char* ']]&gt;' Char*))
    * [23] CDEnd ::= ']]&gt;'
    * </pre>
    * <p>(The '&lt;![CDATA[' has already been read.)
    * <p>Note that this just appends characters to the dataBuffer,
    * without actually generating an event.
    */
  void parseCDSect ()
    throws java.lang.Exception
  {
    parseUntil("]]>");
  }


  /**
    * Parse the prolog of an XML document.
    * <pre>
    * [24] prolog ::= XMLDecl? Misc* (Doctypedecl Misc*)?
    * </pre>
    * <p>There are a couple of tricks here.  First, it is necessary to
    * declare the XML default attributes after the DTD (if present)
    * has been read.  Second, it is not possible to expand general
    * references in attribute value literals until after the entire
    * DTD (if present) has been parsed.
    * <p>We do not look for the XML declaration here, because it is
    * handled by pushURL().
    * @see pushURL
    */
  void parseProlog ()
    throws java.lang.Exception
  {
    parseMisc();

    if (tryRead("<!DOCTYPE")) {
      parseDoctypedecl();
      parseMisc();
    }
  }


  /**
    * Parse the XML declaration.
    * <pre>
    * [25] XMLDecl ::= '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?&gt;'
    * [26] VersionInfo ::= S 'version' Eq ('"1.0"' | "'1.0'")
    * [33] SDDecl ::= S 'standalone' Eq "'" ('yes' | 'no') "'"
    *               | S 'standalone' Eq '"' ("yes" | "no") '"'
    * [78] EncodingDecl ::= S 'encoding' Eq QEncoding
    * </pre>
    * <p>([80] to [82] are also significant.)
    * <p>(The <code>&lt;?xml</code> and whitespace have already been read.)
    * <p>TODO: validate value of standalone.
    * @see #parseTextDecl
    * @see #checkEncoding
    */
  void parseXMLDecl (boolean ignoreEncoding)
    throws java.lang.Exception
  {
    String version;
    String encodingName = null;
    String standalone = null;

        // Read the version.
    require("version");
    parseEq();
    version = readLiteral(0);
    if (!version.equals("1.0")) {
      error("unsupported XML version", version, "1.0");
    }

        // Try reading an encoding declaration.
    skipWhitespace();
    if (tryRead("encoding")) {
      parseEq();
      encodingName = readLiteral(0);
      checkEncoding(encodingName, ignoreEncoding);
    }

        // Try reading a standalone declaration
    skipWhitespace();
    if (tryRead("standalone")) {
      parseEq();
      standalone = readLiteral(0);
    }

    skipWhitespace();
    require("?>");
  }


  /**
    * Parse the Encoding PI.
    * <pre>
    * [78] EncodingDecl ::= S 'encoding' Eq QEncoding
    * [79] EncodingPI ::= '&lt;?xml' S 'encoding' Eq QEncoding S? '?&gt;'
    * [80] QEncoding ::= '"' Encoding '"' | "'" Encoding "'"
    * [81] Encoding ::= LatinName
    * [82] LatinName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
    * </pre>
    * <p>(The <code>&lt;?xml</code>' and whitespace have already been read.)
    * @see #parseXMLDecl
    * @see #checkEncoding
    */
  void parseTextDecl (boolean ignoreEncoding)
    throws java.lang.Exception
  {
    String encodingName = null;
    
        // Read an optional version.
    if (tryRead("version")) {
      String version;
      parseEq();
      version = readLiteral(0);
      if (!version.equals("1.0")) {
  error("unsupported XML version", version, "1.0");
      }
      requireWhitespace();
    }
      

        // Read the encoding.
    require("encoding");
    parseEq();
    encodingName = readLiteral(0);
    checkEncoding(encodingName, ignoreEncoding);

    skipWhitespace();
    require("?>");
  }


  /**
    * Check that the encoding specified makes sense.
    * <p>Compare what the author has specified in the XML declaration
    * or encoding PI with what we have detected.
    * <p>This is also important for distinguishing among the various
    * 7- and 8-bit encodings, such as ISO-LATIN-1 (I cannot autodetect
    * those).
    * @param encodingName The name of the encoding specified by the user.
    * @see #parseXMLDecl
    * @see #parseTextDecl
    */
  void checkEncoding (String encodingName, boolean ignoreEncoding)
    throws java.lang.Exception
  {
    encodingName = encodingName.toUpperCase();

    if (ignoreEncoding) {
      return;
    }

    switch (encoding) {
        // 8-bit encodings
    case ENCODING_UTF_8:
      if (encodingName.equals("ISO-8859-1")) {
  encoding = ENCODING_ISO_8859_1;
      } else if (!encodingName.equals("UTF-8")) {
  error("unsupported 8-bit encoding",
        encodingName,
        "UTF-8 or ISO-8859-1");
      }
      break;
        // 16-bit encodings
    case ENCODING_UCS_2_12:
    case ENCODING_UCS_2_21:
      if (!encodingName.equals("ISO-10646-UCS-2") &&
    !encodingName.equals("UTF-16")) {
  error("unsupported 16-bit encoding",
        encodingName,
        "ISO-10646-UCS-2");
      }
      break;
        // 32-bit encodings
    case ENCODING_UCS_4_1234:
    case ENCODING_UCS_4_4321:
    case ENCODING_UCS_4_2143:
    case ENCODING_UCS_4_3412:
      if (!encodingName.equals("ISO-10646-UCS-4")) {
  error("unsupported 32-bit encoding",
        encodingName,
        "ISO-10646-UCS-4");
      }
    }
  }


  /**
    * Parse miscellaneous markup outside the document element and DOCTYPE
    * declaration.
    * <pre>
    * [27] Misc ::= Comment | PI | S
    * </pre>
    */
  void parseMisc ()
    throws java.lang.Exception
    {
    while (true)
      {
      skipWhitespace();
      if (tryRead("<?"))
        {parsePI();}
      else if (tryRead("<!--"))
        {parseComment();}
      else
        {return;}
      }
    }


  /**
    * Parse a document type declaration.
    * <pre>
    * [28] doctypedecl ::= '&lt;!DOCTYPE' S Name (S ExternalID)? S?
    *                      ('[' %markupdecl* ']' S?)? '&gt;'
    * </pre>
    * <p>(The <code>&lt;!DOCTYPE</code> has already been read.)
    */
  void parseDoctypedecl ()
    throws java.lang.Exception
  {
    char c;
    String doctypeName, ids[];

        // Read the document type name.
    requireWhitespace();
    doctypeName = readNmtoken(true);

        // Read the ExternalIDs.
    skipWhitespace();
    ids = readExternalIds(false);

        // Look for a declaration subset.
    skipWhitespace();
    if (tryRead('[')) {

        // loop until the subset ends
      while (true) {
  context = CONTEXT_DTD;
  skipWhitespace();
  context = CONTEXT_NONE;
  if (tryRead(']')) {
    break;    // end of subset
  } else {
    context = CONTEXT_DTD;
    parseMarkupdecl();
    context = CONTEXT_NONE;
  }
      }
    }

        // Read the external subset, if any
    if (ids[1] != null) {
      pushURL("[external subset]", ids[0], ids[1], null, null, null);

        // Loop until we end up back at '>'
      while (true) {
  context = CONTEXT_DTD;
  skipWhitespace();
  context = CONTEXT_NONE;
  if (tryRead('>')) {
    break;
  } else {
    context = CONTEXT_DTD;
    parseMarkupdecl();
    context = CONTEXT_NONE;
  }
      }
    } else {
        // No external subset.
      skipWhitespace();
      require('>');
    }

    if (handler != null) {
      handler.doctypeDecl(doctypeName, ids[0], ids[1]);
    }

        // Expand general entities in
        // default values of attributes.
        // (Do this after the doctypeDecl
        // event!).
    // expandAttributeDefaultValues();
  }


  /**
    * Parse a markup declaration in the internal or external DTD subset.
    * <pre>
    * [29] markupdecl ::= ( %elementdecl | %AttlistDecl | %EntityDecl |
    *                       %NotationDecl | %PI | %S | %Comment |
    *                       InternalPERef )
    * [30] InternalPERef ::= PEReference
    * [31] extSubset ::= (%markupdecl | %conditionalSect)*
    * </pre>
    */
  void parseMarkupdecl ()
    throws java.lang.Exception
  {
    if (tryRead("<!ELEMENT")) {
      parseElementdecl();
    } else if (tryRead("<!ATTLIST")) {
      parseAttlistDecl();
    } else if (tryRead("<!ENTITY")) {
      parseEntityDecl();
    } else if (tryRead("<!NOTATION")) {
      parseNotationDecl();
    } else if (tryRead("<?")) {
      parsePI();
    } else if (tryRead("<!--")) {
      parseComment();
    } else if (tryRead("<![")) {
      parseConditionalSect();
    } else {
      error("expected markup declaration", null, null);
    }
  }


  /**
    * Parse an element, with its tags.
    * <pre>
    * [33] STag ::= '&lt;' Name (S Attribute)* S? '&gt;' [WFC: unique Att spec]
    * [38] element ::= EmptyElement | STag content ETag
    * [39] EmptyElement ::= '&lt;' Name (S Attribute)* S? '/&gt;'
    *                       [WFC: unique Att spec]
    * </pre>
    * <p>(The '&lt;' has already been read.)
    * <p>NOTE: this method actually chains onto parseContent(), if necessary,
    * and parseContent() will take care of calling parseETag().
    */
  void parseElement ()
    throws java.lang.Exception
  {
    String gi;
    char c;
    int oldElementContent = currentElementContent;
    String oldElement = currentElement;

        // This is the (global) counter for the
        // array of specified attributes.
    tagAttributePos = 0;

        // Read the element type name.
    gi = readNmtoken(true);

        // Determine the current content type.
    currentElement = gi;
    currentElementContent = getElementContentType(gi);
    if (currentElementContent == CONTENT_UNDECLARED) {
      currentElementContent = CONTENT_ANY;
    }

        // Read the attributes, if any.
        // After this loop, we should be just
        // in front of the closing delimiter.
    skipWhitespace();
    c = readCh();
    while (c != '/' && c != '>') {
      unread(c);
      parseAttribute(gi);
      skipWhitespace();
      c = readCh();
    }
    unread(c);

        // Supply any defaulted attributes.
    Enumeration atts = declaredAttributes(gi);
    if (atts != null) {
      String aname;
    loop: while (atts.hasMoreElements()) {
      aname = (String)atts.nextElement();
        // See if it was specified.
      for (int i = 0; i < tagAttributePos; i++) {
  if (tagAttributes[i] == aname) {
    continue loop;
  }
      }
        // I guess not...
      if (handler != null) {
  handler.attribute(aname,
        getAttributeExpandedValue(gi, aname),
        false);
      }
    }
    }

        // Figure out if this is a start tag
        // or an empty element, and dispatch an
        // event accordingly.
    c = readCh();
    switch (c) {
    case '>':
      if (handler != null) {
  handler.startElement(gi);
      }
      parseContent();
      break;
    case '/':
      require('>');
      if (handler != null) {
  handler.startElement(gi);
  handler.endElement(gi);
      }
      break;
    }

        // Restore the previous state.
    currentElement = oldElement;
    currentElementContent = oldElementContent;
  }


  /**
    * Parse an attribute assignment.
    * <pre>
    * [34] Attribute ::= Name Eq AttValue
    * </pre>
    * @param name The name of the attribute's element.
    * @see XmlHandler#attribute
    */
  void parseAttribute (String name)
    throws java.lang.Exception
  {
    String aname;
    int type;
    String value;

        // Read the attribute name.
    aname = readNmtoken(true).intern();
    type = getAttributeDefaultValueType(name, aname);

        // Parse '='
    parseEq();

        // Read the value, normalizing whitespace
        // if it is not CDATA.
    if (type == ATTRIBUTE_CDATA || type == ATTRIBUTE_UNDECLARED) {
      value = readLiteral(LIT_CHAR_REF | LIT_ENTITY_REF);
    } else {
      value = readLiteral(LIT_CHAR_REF | LIT_ENTITY_REF | LIT_NORMALIZE);
    }

        // Inform the handler about the
        // attribute.
    if (handler != null) {
      handler.attribute(aname, value, true);
    }
    dataBufferPos = 0;

        // Note that the attribute has been
        // specified.
    if (tagAttributePos == tagAttributes.length) {
      String newAttrib[] = new String[tagAttributes.length * 2];
      System.arraycopy(tagAttributes, 0, newAttrib, 0, tagAttributePos);
      tagAttributes = newAttrib;
    }
    tagAttributes[tagAttributePos++] = aname;
  }


  /**
    * Parse an equals sign surrounded by optional whitespace.
    * [35] Eq ::= S? '=' S?
    */
  void parseEq ()
    throws java.lang.Exception
  {
    skipWhitespace();
    require('=');
    skipWhitespace();
  }


  /**
    * Parse an end tag.
    * [36] ETag ::= '</' Name S? '>'
    * *NOTE: parseContent() chains to here.
    */
  void parseETag ()
    throws java.lang.Exception
  {
    String name;
    name = readNmtoken(true);
    if (name != currentElement) {
      error("mismatched end tag", name, currentElement);
    }
    skipWhitespace();
    require('>');
    if (handler != null) {
      handler.endElement(name);
    }
  }


  /**
    * Parse the content of an element.
    * [37] content ::= (element | PCData | Reference | CDSect | PI | Comment)*
    * [68] Reference ::= EntityRef | CharRef
    */
  void parseContent ()
    throws java.lang.Exception
  {
    String data;
    char c;

    while (true) {

      switch (currentElementContent) {
      case CONTENT_ANY:
      case CONTENT_MIXED:
  parsePCData();
  break;
      case CONTENT_ELEMENTS:
  parseWhitespace();
  break;
      }

        // Handle delimiters
      c = readCh();
      switch (c) {

      case '&':      // Found "&"
  c = readCh();
  if (c == '#') {
    parseCharRef();
  } else {
    unread(c);
    parseEntityRef(true);
  }
  break;

      case '<':      // Found "<"

  c = readCh();
  switch (c) {

  case '!':    // Found "<!"
    c = readCh();
    switch (c) {
    case '-':    // Found "<!-"
      require('-');
      parseComment();
      break;
    case '[':    // Found "<!["
      require("CDATA[");
      parseCDSect();
      break;
    default:
      error("expected comment or CDATA section", c, null);
      break;
    }
    break;

  case '?':    // Found "<?"
    dataBufferFlush();
    parsePI();
    break;

  case '/':    // Found "</"
    dataBufferFlush();
    parseETag();
    return;

  default:    // Found "<" followed by something else
    dataBufferFlush();
    unread(c);
    parseElement();
    break;
  }
      }
    }
  }


  /**
    * Parse an element type declaration.
    * [40] elementdecl ::= '<!ELEMENT' S %Name S (%S S)? %contentspec S? '>'
    *                      [VC: Unique Element Declaration]
    * *NOTE: the '<!ELEMENT' has already been read.
    */
  void parseElementdecl ()
    throws java.lang.Exception
  {
    String name;

    requireWhitespace();
        // Read the element type name.
    name = readNmtoken(true);

    requireWhitespace();
        // Read the content model.
    parseContentspec(name);

    skipWhitespace();
    require('>');
  }


  /**
    * Content specification.
    * [41] contentspec ::= 'EMPTY' | 'ANY' | Mixed | elements
    */
  void parseContentspec (String name)
    throws java.lang.Exception
  {
    if (tryRead("EMPTY")) {
      setElement(name, CONTENT_EMPTY, null, null);
      return;
    } else if (tryRead("ANY")) {
      setElement(name, CONTENT_ANY, null, null);
      return;
    } else {
      require('(');
      dataBufferAppend('(');
      skipWhitespace();
      if (tryRead("#PCDATA")) {
  dataBufferAppend("#PCDATA");
  parseMixed();
  setElement(name, CONTENT_MIXED, dataBufferToString(), null);
      } else {
  parseElements();
  setElement(name, CONTENT_ELEMENTS, dataBufferToString(), null);
      }
    }
  }


  /**
    * Parse an element-content model.
    * [42] elements ::= (choice | seq) ('?' | '*' | '+')?
    * [44] cps ::= S? %cp S?
    * [45] choice ::= '(' S? %ctokplus (S? '|' S? %ctoks)* S? ')'
    * [46] ctokplus ::= cps ('|' cps)+
    * [47] ctoks ::= cps ('|' cps)*
    * [48] seq ::= '(' S? %stoks (S? ',' S? %stoks)* S? ')'
    * [49] stoks ::= cps (',' cps)*
    * *NOTE: the opening '(' and S have already been read.
    * *TODO: go over parameter entity boundaries more carefully.
    */
  void parseElements ()
    throws java.lang.Exception
  {
    char c;
    char sep;

        // Parse the first content particle
    skipWhitespace();
    parseCp();

        // Check for end or for a separator.
    skipWhitespace();
    c = readCh();
    switch (c) {
    case ')':
      dataBufferAppend(')');
      c = readCh();
      switch (c) {
      case '*':
      case '+':
      case '?':
  dataBufferAppend(c);
  break;
      default:
  unread(c);
      }
      return;
    case ',':      // Register the separator.
    case '|':
      sep = c;
      dataBufferAppend(c);
      break;
    default:
      error("bad separator in content model", c, null);
      return;
    }

        // Parse the rest of the content model.
    while (true) {
      skipWhitespace();
      parseCp();
      skipWhitespace();
      c = readCh();
      if (c == ')') {
  dataBufferAppend(')');
  break;
      } else if (c != sep) {
  error("bad separator in content model", c, null);
  return;
      } else {
  dataBufferAppend(c);
      }
    }

        // Check for the occurrence indicator.
    c = readCh();
    switch (c) {
    case '?':
    case '*':
    case '+':
      dataBufferAppend(c);
      return;
    default:
      unread(c);
      return;
    }
  }


  /**
    * Parse a content particle.
    * [43] cp ::= (Name | choice | seq) ('?' | '*' | '+')
    * *NOTE: I actually use a slightly different production here:
    *        cp ::= (elements | (Name ('?' | '*' | '+')?))
    */
  void parseCp ()
    throws java.lang.Exception
  {
    char c;

    if (tryRead('(')) {
      dataBufferAppend('(');
      parseElements();
    } else {
      dataBufferAppend(readNmtoken(true));
      c = readCh();
      switch (c) {
      case '?':
      case '*':
      case '+':
  dataBufferAppend(c);
  break;
      default:
  unread(c);
  break;
      }
    }
  }


  /**
    * Parse mixed content.
    * [50] Mixed ::= '(' S? %( %'#PCDATA' (S? '|' S? %Mtoks)* ) S? ')*'
    *              | '(' S? %('#PCDATA') S? ')'
    * [51] Mtoks ::= %Name (S? '|' S? %Name)*
    * *NOTE: the S and '#PCDATA' have already been read.
    */
  void parseMixed ()
    throws java.lang.Exception
  {
    char c;

        // Check for PCDATA alone.
    skipWhitespace();
    if (tryRead(')')) {
      dataBufferAppend(")*");
      tryRead('*');
      return;
    }

        // Parse mixed content.
    skipWhitespace();
    while (!tryRead(")*")) {
      require('|');
      dataBufferAppend('|');
      skipWhitespace();
      dataBufferAppend(readNmtoken(true));
      skipWhitespace();
    }
    dataBufferAppend(")*");
  }


  /**
    * Parse an attribute list declaration.
    * [52] AttlistDecl ::= '<!ATTLIST' S %Name S? %AttDef+ S? '>'
    * *NOTE: the '<!ATTLIST' has already been read.
    */
  void parseAttlistDecl ()
    throws java.lang.Exception
  {
    String elementName;

    requireWhitespace();
    elementName = readNmtoken(true);
    requireWhitespace();
    while (!tryRead('>')) {
      parseAttDef(elementName);
      skipWhitespace();
    }
  }


  /**
    * Parse a single attribute definition.
    * [53] AttDef ::= S %Name S %AttType S %Default
    */
  void parseAttDef (String elementName)
    throws java.lang.Exception
  {
    String name;
    int type;
    String enum = null;

        // Read the attribute name.
    name = readNmtoken(true);

        // Read the attribute type.
    requireWhitespace();
    type = readAttType();

        // Get the string of enumerated values
        // if necessary.
    if (type == ATTRIBUTE_ENUMERATED || type == ATTRIBUTE_NOTATION) {
      enum = dataBufferToString();
    }

        // Read the default value.
    requireWhitespace();
    parseDefault(elementName, name, type, enum);
  }


  /**
    * Parse the attribute type.
    * [54] AttType ::= StringType | TokenizedType | EnumeratedType
    * [55] StringType ::= 'CDATA'
    * [56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES' |
    *                        'NMTOKEN' | 'NMTOKENS'
    * [57] EnumeratedType ::= NotationType | Enumeration
    * *TODO: validate the type!!
    */
  int readAttType ()
    throws java.lang.Exception
  {
    String typeString;
    Integer type;

    if (tryRead('(')) {
      parseEnumeration();
      return ATTRIBUTE_ENUMERATED;
    } else {
      typeString = readNmtoken(true);
      if (typeString.equals("NOTATION")) {
  parseNotationType();
      }
      type = (Integer)attributeTypeHash.get(typeString);
      if (type == null) {
  error("illegal attribute type", typeString, null);
  return ATTRIBUTE_UNDECLARED;
      } else {
  return type.intValue();
      }
    }
  }


  /**
    * Parse an enumeration.
    * [60] Enumeration ::= '(' S? %Etoks (S? '|' S? %Etoks)* S? ')'
    * [61] Etoks ::= %Nmtoken (S? '|' S? %Nmtoken)*
    * *NOTE: the '(' has already been read.
    */
  void parseEnumeration ()
    throws java.lang.Exception
  {
    char c;

    dataBufferAppend('(');

        // Read the first token.
    skipWhitespace();
    dataBufferAppend(readNmtoken(true));
        // Read the remaining tokens.
    skipWhitespace();
    while (!tryRead(')')) {
      require('|');
      dataBufferAppend('|');
      skipWhitespace();
      dataBufferAppend(readNmtoken(true));
      skipWhitespace();
    }
    dataBufferAppend(')');
  }


  /**
    * Parse a notation type for an attribute.
    * [58] NotationType ::= %'NOTATION' S '(' S? %Ntoks (S? '|' S? %Ntoks)*
    *                       S? ')'
    * [59] Ntoks ::= %Name (S? '|' S? %Name)
    * *NOTE: the 'NOTATION' has already been read
    */
  void parseNotationType ()
    throws java.lang.Exception
  {
    requireWhitespace();
    require('(');

    parseEnumeration();
  }


  /**
    * Parse the default value for an attribute.
    * [62] Default ::= '#REQUIRED' | '#IMPLIED' | ((%'#FIXED' S)? %AttValue
    */
  void parseDefault (String elementName, String name, int type, String enum)
    throws java.lang.Exception
  {
    int valueType = ATTRIBUTE_DEFAULT_SPECIFIED;
    String value = null;
    boolean normalizeWSFlag;

    if (tryRead('#')) {
      if (tryRead("FIXED")) {
  valueType = ATTRIBUTE_DEFAULT_FIXED;
  requireWhitespace();
  context = CONTEXT_ATTRIBUTEVALUE;
  value = readLiteral(LIT_CHAR_REF);
  context = CONTEXT_DTD;
      } else if (tryRead("REQUIRED")) {
  valueType = ATTRIBUTE_DEFAULT_REQUIRED;
      } else if (tryRead("IMPLIED")) {
  valueType = ATTRIBUTE_DEFAULT_IMPLIED;
      } else {
  error("illegal keyword for attribute default value", null, null);
      }
    } else {
      context = CONTEXT_ATTRIBUTEVALUE;
      value = readLiteral(LIT_CHAR_REF);
      context = CONTEXT_DTD;
    }
    setAttribute(elementName, name, type, enum, value, valueType);
  }


  /**
    * Parse a conditional section.
    * [63] conditionalSect ::= includeSect || ignoreSect
    * [64] includeSect ::= '<![' %'INCLUDE' '[' (%markupdecl*)* ']]>'
    * [65] ignoreSect ::= '<![' %'IGNORE' '[' ignoreSectContents* ']]>'
    * [66] ignoreSectContents ::= ((SkipLit | Comment | PI) -(Char* ']]>'))
    *                           | ('<![' ignoreSectContents* ']]>')
    *                           | (Char - (']' | [<'"]))
    *                           | ('<!' (Char - ('-' | '[')))
    * *NOTE: the '<![' has already been read.
    * *TODO: verify that I am handling ignoreSectContents right.
    */
  void parseConditionalSect ()
    throws java.lang.Exception
  {
    skipWhitespace();
    if (tryRead("INCLUDE")) {
      skipWhitespace();
      require('[');
      skipWhitespace();
      while (!tryRead("]]>")) {
  parseMarkupdecl();
  skipWhitespace();
      }
    } else if (tryRead("IGNORE")) {
      skipWhitespace();
      require('[');
      int nesting = 1;
      char c;
      for (int nest = 1; nest > 0; ) {
  c = readCh();
  switch (c) {
  case '<':
    if (tryRead("![")) {
      nest++;
    }
  case ']':
    if (tryRead("]>")) {
      nest--;
    }
  }
      }
    } else {
      error("conditional section must begin with INCLUDE or IGNORE",
      null, null);
    }
  }


  /**
    * Read a character reference.
    * [67] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
    * *NOTE: the '&#' has already been read.
    */
  void parseCharRef ()
    throws java.lang.Exception
  {
    int value = 0;
    char c;

    if (tryRead('x')) {
      loop1: while (true) {
  c = readCh();
  switch (c) {
  case '0':
  case '1':
  case '2':
  case '3':
  case '4':
  case '5':
  case '6':
  case '7':
  case '8':
  case '9':
  case 'a':
  case 'A':
  case 'b':
  case 'B':
  case 'c':
  case 'C':
  case 'd':
  case 'D':
  case 'e':
  case 'E':
  case 'f':
  case 'F':
    value *= 16;
    value += Integer.parseInt(new Character(c).toString(), 16);
    break;
  case ';':
    break loop1;
  default:
    error("illegal character in character reference", c, null);
    break loop1;
  }
      }
    } else {
      loop2: while (true) {
  c = readCh();
  switch (c) {
  case '0':
  case '1':
  case '2':
  case '3':
  case '4':
  case '5':
  case '6':
  case '7':
  case '8':
  case '9':
    value *= 10;
    value += Integer.parseInt(new Character(c).toString(), 10);
    break;
  case ';':
    break loop2;
  default:
    error("illegal character in character reference", c, null);
    break loop2;
  }
      }
    }

    // Check for surrogates: 00000000 0000xxxx yyyyyyyy zzzzzzzz
    //  (1101|10xx|xxyy|yyyy + 1101|11yy|zzzz|zzzz: 
    if (value <= 0x0000ffff) {
        // no surrogates needed
      dataBufferAppend((char)value);
    } else if (value <= 0x000fffff) {
        // > 16 bits, surrogate needed
      dataBufferAppend((char)(0xd8 | ((value & 0x000ffc00) >> 10)));
      dataBufferAppend((char)(0xdc | (value & 0x0003ff)));
    } else {
        // too big for surrogate
      error("character reference " + value + " is too large for UTF-16",
      new Integer(value).toString(), null);
    }
  }


  /**
    * Parse a reference.
    * [69] EntityRef ::= '&' Name ';'
    * *NOTE: the '&' has already been read.
    * @param externalAllowed External entities are allowed here.
    */
  void parseEntityRef (boolean externalAllowed)
    throws java.lang.Exception
  {
    String name;

    name = readNmtoken(true);
    require(';');
    switch (getEntityType(name)) {
    case ENTITY_UNDECLARED:
      error("reference to undeclared entity", name, null);
      break;
    case ENTITY_INTERNAL:
      pushString(name, getEntityValue(name));
      break;
    case ENTITY_TEXT:
      if (externalAllowed) {
  pushURL(name, getEntityPublicId(name),
    getEntitySystemId(name),
    null, null, null);
      } else {
  error("reference to external entity in attribute value.", name, null);
      }
      break;
    case ENTITY_NDATA:
      if (externalAllowed) {
  error("data entity reference in content", name, null);
      } else {
  error("reference to external entity in attribute value.", name, null);
      }
      break;
    }
  }


  /**
    * Parse a parameter entity reference.
    * [70] PEReference ::= '%' Name ';'
    * *NOTE: the '%' has already been read.
    */
  void parsePEReference (boolean isEntityValue)
    throws java.lang.Exception
  {
    String name;

    name = "%" + readNmtoken(true);
    require(';');
    switch (getEntityType(name)) {
    case ENTITY_UNDECLARED:
      error("reference to undeclared parameter entity", name, null);
      break;
    case ENTITY_INTERNAL:
      if (isEntityValue) {
  pushString(name, getEntityValue(name));
      } else {
  pushString(name, " " + getEntityValue(name) + ' ');
      }
      break;
    case ENTITY_TEXT:
      if (isEntityValue) {
  pushString(null, " ");
      }
      pushURL(name, getEntityPublicId(name),
        getEntitySystemId(name),
        null, null, null);
      if (isEntityValue) {
  pushString(null, " ");
      }
      break;
    }
  }


  /**
    * Parse an entity declaration.
    * [71] EntityDecl ::= '<!ENTITY' S %Name S %EntityDef S? '>'
    *                   | '<!ENTITY' S '%' S %Name S %EntityDef S? '>'
    * [72] EntityDef ::= EntityValue | ExternalDef
    * [73] ExternalDef ::= ExternalID %NDataDecl?
    * [74] ExternalID ::= 'SYSTEM' S SystemLiteral
    *                   | 'PUBLIC' S PubidLiteral S SystemLiteral
    * [75] NDataDecl ::= S %'NDATA' S %Name
    * *NOTE: the '<!ENTITY' has already been read.
    */
  void parseEntityDecl ()
    throws java.lang.Exception
  {
    char c;
    boolean peFlag = false;
    String name, value, notationName, ids[];

        // Check for a parameter entity.
    requireWhitespace();
    if (tryRead('%')) {
      peFlag = true;
      requireWhitespace();
    }

        // Read the entity name, and prepend
        // '%' if necessary.
    name = readNmtoken(true);
    if (peFlag) {
      name = "%" + name;
    }

        // Read the entity value.
    requireWhitespace();
    c = readCh();
    unread(c);
    if (c == '"' || c == '\'') {
        // Internal entity.
      context = CONTEXT_ENTITYVALUE;
      value = readLiteral(LIT_CHAR_REF|LIT_PE_REF);
      context = CONTEXT_DTD;
      setInternalEntity(name,value);
    } else {
        // Read the external IDs
      ids = readExternalIds(false);
      if (ids[1] == null) {
  error("system identifier missing", name, null);
      }

        // Check for NDATA declaration.
      skipWhitespace();
      if (tryRead("NDATA")) {
  requireWhitespace();
  notationName = readNmtoken(true);
  setExternalDataEntity(name, ids[0], ids[1], notationName);
      } else {
  setExternalTextEntity(name, ids[0], ids[1]);
      }
    }

        // Finish the declaration.
    skipWhitespace();
    require('>');
  }


  /**
    * Parse a notation declaration.
    * [81] NotationDecl ::= '<!NOTATION' S %Name S %ExternalID S? '>'
    * *NOTE: the '<!NOTATION' has already been read.
    */
  void parseNotationDecl ()
    throws java.lang.Exception
  {
    String nname, ids[];
    

    requireWhitespace();
    nname = readNmtoken(true);

    requireWhitespace();

        // Read the external identifiers.
    ids = readExternalIds(true);
    if (ids[0] == null && ids[1] == null) {
      error("external identifier missing", nname, null);
    }

        // Register the notation.
    setNotation(nname, ids[0], ids[1]);

    skipWhitespace();
    require('>');
  }


  /**
    * Parse PCDATA.
    * <pre>
    * [16] PCData ::= [^&lt;&amp;]*
    * </pre>
    * <p>The trick here is that the data stays in the dataBuffer without
    * necessarily being converted to a string right away.
    */
  void parsePCData ()
    throws java.lang.Exception
  {
    char c;

        // Start with a little cheat -- in most
        // cases, the entire sequence of
        // character data will already be in
        // the readBuffer; if not, fall through to
        // the normal approach.
    if (USE_CHEATS) {
      int lineAugment = 0;
      int columnAugment = 0;

      loop: for (int i = readBufferPos; i < readBufferLength; i++) {
  switch (readBuffer[i]) {
  case '\n':
    lineAugment++;
    columnAugment = 0;
    break;
  case '&':
  case '<':
    int start = readBufferPos;
    columnAugment++;
    readBufferPos = i;
    if (lineAugment > 0) {
      line += lineAugment;
      column = columnAugment;
    } else {
      column += columnAugment;
    }
    dataBufferAppend(readBuffer, start, i-start);
    return;
  default:
    columnAugment++;
  }
      }
    }

        // OK, the cheat didn't work; start over
        // and do it by the book.
    while (true) {
      c = readCh();
      switch (c) {
      case '<':
      case '&':
  unread(c);
  return;
      default:
  dataBufferAppend(c);
  break;
      }
    }
  }



  //////////////////////////////////////////////////////////////////////
  // High-level reading and scanning methods.
  //////////////////////////////////////////////////////////////////////

  /**
    * Require whitespace characters.
    * [1] S ::= (#x20 | #x9 | #xd | #xa)+
    */
  void requireWhitespace ()
    throws java.lang.Exception
  {
    char c = readCh();
    if (isWhitespace(c)) {
      skipWhitespace();
    } else {
      error("whitespace expected", c, null);
    }
  }


  /**
    * Parse whitespace characters, and leave them in the data buffer.
    */
  void parseWhitespace ()
    throws java.lang.Exception
  {
    char c = readCh();
    while (isWhitespace(c)) {
      dataBufferAppend(c);
      c = readCh();
    }
    unread(c);
  }


  /**
    * Skip whitespace characters.
    * [1] S ::= (#x20 | #x9 | #xd | #xa)+
    */
  void skipWhitespace ()
    throws java.lang.Exception
  {
        // Start with a little cheat.  Most of
        // the time, the white space will fall
        // within the current read buffer; if
        // not, then fall through.
    if (USE_CHEATS) {
      int lineAugment = 0;
      int columnAugment = 0;

      loop: for (int i = readBufferPos; i < readBufferLength; i++) {
  switch (readBuffer[i]) {
  case ' ':
  case '\t':
  case '\r':
    columnAugment++;
    break;
  case '\n':
    lineAugment++;
    columnAugment = 0;
    break;
  case '%':
    if (context == CONTEXT_DTD || context == CONTEXT_ENTITYVALUE) {
      break loop;
    } // else fall through...
  default:
    readBufferPos = i;
    if (lineAugment > 0) {
      line += lineAugment;
      column = columnAugment;
    } else {
      column += columnAugment;
    }
    return;
  }
      }
    }

        // OK, do it by the book.
    char c = readCh();
    while (isWhitespace(c)) {
      c = readCh();
    }
    unread(c);
  }


  /**
    * Read a name or name token.
    * [5] Name ::= (Letter | '_' | ':') (NameChar)*
    * [7] Nmtoken ::= (NameChar)+
    * *NOTE: [6] is implemented implicitly where required.
    */
  String readNmtoken (boolean isName)
    throws java.lang.Exception
  {
    char c;

    if (USE_CHEATS) {
      loop: for (int i = readBufferPos; i < readBufferLength; i++) {
  switch (readBuffer[i]) {
  case '%':
    if (context == CONTEXT_DTD || context == CONTEXT_ENTITYVALUE) {
      break loop;
    } // else fall through...
  case '<':
  case '>':
  case '&':
  case ',':
  case '|':
  case '*':
  case '+':
  case '?':
  case ')':
  case '=':
  case '\'':
  case '"':
  case '[':
  case ' ':
  case '\t':
  case '\r':
  case '\n':
  case ';':
  case '/':
  case '#':
    int start = readBufferPos;
    if (i == start) {
      error("name expected", readBuffer[i], null);
    }
    readBufferPos = i;
    return intern(readBuffer, start, i - start);
  }
      }
    }

    nameBufferPos = 0;

        // Read the first character.
    loop: while (true) {
      c = readCh();
      switch (c) {
      case '%':
      case '<':
      case '>':
      case '&':
      case ',':
      case '|':
      case '*':
      case '+':
      case '?':
      case ')':
      case '=':
      case '\'':
      case '"':
      case '[':
      case ' ':
      case '\t':
      case '\n':
      case '\r':
      case ';':
      case '/':
  unread(c);
  if (nameBufferPos == 0) {
    error("name expected", null, null);
  }
  String s = intern(nameBuffer,0,nameBufferPos);
  nameBufferPos = 0;
  return s;
      default:
  nameBuffer =
    (char[])extendArray(nameBuffer, nameBuffer.length, nameBufferPos);
  nameBuffer[nameBufferPos++] = c;
      }
    }
  }


  /**
    * Read a literal.
    * [10] AttValue ::= '"' ([^<&"] | Reference)* '"'
    *                 | "'" ([^<&'] | Reference)* "'"
    * [11] SystemLiteral ::= '"' URLchar* '"' | "'" (URLchar - "'")* "'"
    * [13] PubidLiteral ::= '"' PubidChar* '"' | "'" (PubidChar - "'")* "'"
    * [9] EntityValue ::= '"' ([^%&"] | PEReference | Reference)* '"'
    *                   | "'" ([^%&'] | PEReference | Reference)* "'"
    */
  String readLiteral (int flags)
    throws java.lang.Exception
  {
    char delim, c;
    int startLine = line;

        // Find the delimiter.
    delim = readCh();
    if (delim != '"' && delim != '\'' && delim != (char)0) {
      error("expected '\"' or \"'\"", delim, null);
      return null;
    }

        // Read the literal.
    try {
      c = readCh();

    loop: while (c != delim) {
      switch (c) {
        // Literals never have line ends
      case '\n':
      case '\r':
  c = ' ';
  break;
        // References may be allowed
      case '&':
  if ((flags & LIT_CHAR_REF) > 0) {
    c = readCh();
    if (c == '#') {
      parseCharRef();
      c = readCh();
      continue loop;    // check the next character
    } else if ((flags & LIT_ENTITY_REF) > 0) {
      unread(c);
      parseEntityRef(false);
      c = readCh();
      continue loop;
    } else {
      dataBufferAppend('&');
    }
  }
  break;

      default:
  break;
      }
      dataBufferAppend(c);
      c = readCh();
    }
    } catch (EOFException e) {
      error("end of input while looking for delimiter (started on line "
      + startLine + ')', null, new Character(delim).toString());
    }

        // Normalise whitespace if necessary.
    if ((flags & LIT_NORMALIZE) > 0) {
      dataBufferNormalize();
    }

        // Return the value.
    return dataBufferToString();
  }


  /**
    * Try reading external identifiers.
    * <p>The system identifier is not required for notations.
    * @param inNotation Are we in a notation?
    * @return A two-member String array containing the identifiers.
    */
  String[] readExternalIds (boolean inNotation)
    throws java.lang.Exception
  {
    char c;
    String ids[] = new String[2];

    if (tryRead("PUBLIC")) {
      requireWhitespace();
      ids[0] = readLiteral(LIT_NORMALIZE); // public id
      if (inNotation) {
  skipWhitespace();
  if (tryRead('"') || tryRead('\'')) {
    ids[1] = readLiteral(0);
  }
      } else {
  requireWhitespace();
  ids[1] = readLiteral(0); // system id
      }
    } else if (tryRead("SYSTEM")) {
      requireWhitespace();
      ids[1] = readLiteral(0);  // system id
    }

    return ids;
  }


  /**
    * Test if a character is whitespace.
    * <pre>
    * [1] S ::= (#x20 | #x9 | #xd | #xa)+
    * </pre>
    * @param c The character to test.
    * @return true if the character is whitespace.
    */
  final boolean isWhitespace (char c)
  {
    switch ((int)c) {
    case 0x20:
    case 0x09:
    case 0x0d:
    case 0x0a:
      return true;
    default:
      return false;
    }
  }



  //////////////////////////////////////////////////////////////////////
  // Utility routines.
  //////////////////////////////////////////////////////////////////////


  /**
    * Add a character to the data buffer.
    */
  void dataBufferAppend (char c)
  {
        // Expand buffer if necessary.
    dataBuffer =
      (char[])extendArray(dataBuffer, dataBuffer.length, dataBufferPos);
    dataBuffer[dataBufferPos++] = c;
  }


  /** 
    * Add a string to the data buffer.
    */
  void dataBufferAppend (String s)
  {
    dataBufferAppend(s.toCharArray(), 0, s.length());
  }


  /**
    * Append (part of) a character array to the data buffer.
    */
  void dataBufferAppend (char ch[], int start, int length)
  {
    dataBuffer =
      (char[])extendArray(dataBuffer, dataBuffer.length,
        dataBufferPos + length);
    System.arraycopy((Object)ch, start,
         (Object)dataBuffer, dataBufferPos,
         length);
    dataBufferPos += length;
  }


  /**
    * Normalise whitespace in the data buffer.
    */
  void dataBufferNormalize ()
  {
    int i = 0;
    int j = 0;
    int end = dataBufferPos;

        // Skip whitespace at the start.
    while (j < end && isWhitespace(dataBuffer[j])) {
      j++;
    }

        // Skip whitespace at the end.
    while (end > j && isWhitespace(dataBuffer[end - 1])) {
      end --;
    }

        // Start copying to the left.
    while (j < end) {

      char c = dataBuffer[j++];

        // Normalise all other whitespace to
        // a single space.
      if (isWhitespace(c)) {
  while (j < end && isWhitespace(dataBuffer[j++])) {
  }
  dataBuffer[i++] = ' ';
  dataBuffer[i++] = dataBuffer[j-1];
      } else {
  dataBuffer[i++] = c;
      }
    }

        // The new length is <= the old one.
    dataBufferPos = i;
  }


  /**
    * Convert the data buffer to a string.
    * @param internFlag true if the contents should be interned.
    * @see #intern(char[],int,int)
    */
  String dataBufferToString ()
  {
    String s = new String(dataBuffer, 0, dataBufferPos);
    dataBufferPos = 0;
    return s;
  }


  /**
    * Flush the contents of the data buffer to the handler, if
    * appropriate, and reset the buffer for new input.
    */
  void dataBufferFlush ()
    throws java.lang.Exception
  {
    if (dataBufferPos > 0) {
      switch (currentElementContent) {
      case CONTENT_UNDECLARED:
      case CONTENT_EMPTY:
  // do nothing
  break;
      case CONTENT_MIXED:
      case CONTENT_ANY:
  if (handler != null) {
    handler.charData(dataBuffer, 0, dataBufferPos);
  }
  break;
      case CONTENT_ELEMENTS:
  if (handler != null) {
    handler.ignorableWhitespace(dataBuffer, 0, dataBufferPos);
  }
  break;
      }
      dataBufferPos = 0;
    }
  }


  /**
    * Require a string to appear, or throw an exception.
    */
  void require (String delim)
    throws java.lang.Exception
  {
    char ch[] = delim.toCharArray();
    for (int i = 0; i < ch.length; i++) {
      require(ch[i]);
    }
  }


  /**
    * Require a character to appear, or throw an exception.
    */
  void require (char delim)
       throws java.lang.Exception
  {
    char c = readCh();

    if (c != delim) {
      error("expected character", c, new Character(delim).toString());
    }
  }


  /**
    * Return an internalised version of a string.
    * <p>&AElig;lfred uses this method to create an internalised version
    * of all names and attribute values, so that it can test equality
    * with <code>==</code> instead of <code>String.equals()</code>.
    * <p>If you want to be able to test for equality in the same way,
    * you can use this method to internalise your own strings first:
    * <pre>
    * String PARA = handler.intern("PARA");
    * </pre>
    * <p>Note that this will not return the same results as String.intern().
    * @param s The string to internalise.
    * @return An internalised version of the string.
    * @see #intern(char[],int,int)
    * @see java.lang.String#intern
    */
  public String intern (String s)
  {
    char ch[] = s.toCharArray();
    return intern(ch, 0, ch.length);
  }


  /**
    * Create an internalised string from a character array.
    * <p>This is much more efficient than constructing a non-internalised
    * string first, and then internalising it.
    * <p>Note that this will not return the same results as String.intern().
    * @param ch an array of characters for building the string.
    * @param start the starting position in the array.
    * @param length the number of characters to place in the string.
    * @return an internalised string.
    * @see #intern(String)
    * @see java.lang.String#intern
    */
  public String intern (char ch[], int start, int length)
  {
    int index;
    int hash = 0;

        // Generate a hash code.
    for (int i = start; i < start + length; i++) {
      hash = ((hash << 1) & 0xffffff) + (int)ch[i];
    }

    hash = hash % SYMBOL_TABLE_LENGTH;

        // Get the bucket.
    Object bucket[] = (Object[])symbolTable[hash];
    if (bucket == null) {
      symbolTable[hash] = bucket = new Object[8];
    }

        // Search for a matching tuple, and
        // return the string if we find one.
    for (index = 0; index < bucket.length; index += 2) {
      char chFound[] = (char[])bucket[index];

        // Stop when we hit a null index.
      if (chFound == null) {
  break;
      }

        // If they're the same length,
        // check for a match.
        // If the loop finishes, 'index' will
        // contain the current bucket
        // position.
      if (chFound.length == length) {
  for (int i = 0; i < chFound.length; i++) {
        // Stop if there are no more tuples.
    if (ch[start+i] != chFound[i]) {
      break;
    } else if (i == length-1) {
        // That's it, we have a match!
      return (String)bucket[index+1];
    }
  }
      }
    }

        // Not found -- we'll have to add it.

        // Do we have to grow the bucket?
    bucket =
      (Object[])extendArray(bucket, bucket.length, index);

        // OK, add it to the end of the
        // bucket.
    String s = new String(ch, start, length);
    bucket[index] = s.toCharArray();
    bucket[index+1] = s;
    symbolTable[hash] = bucket;
    return s;
  }


  /**
    * Ensure the capacity of an array, allocating a new one if
    * necessary.
    */
  Object extendArray (Object array, int currentSize, int requiredSize)
  {
    if (requiredSize < currentSize) {
      return array;
    } else {
      Object newArray = null;
      int newSize = currentSize * 2;

      if (newSize <= requiredSize) {
  newSize = requiredSize + 1;
      }

      if (array instanceof char[]) {
  newArray = new char[currentSize * 2];
      } else if (array instanceof Object[]) {
  newArray = new Object[currentSize * 2];
      }

      System.arraycopy(array, 0, newArray, 0, currentSize);
      return newArray;
    }
  }



  //////////////////////////////////////////////////////////////////////
  // XML query routines.
  //////////////////////////////////////////////////////////////////////


  //
  // Elements
  //

  /**
    * Get the declared elements for an XML document.
    * <p>The results will be valid only after the DTD (if any) has been
    * parsed.
    * @return An enumeration of all element types declared for this
    *         document (as Strings).
    * @see #getElementContentType
    * @see #getElementContentModel
    */
  public Enumeration declaredElements ()
  {
    return elementInfo.keys();
  }


  /**
    * Look up the content type of an element.
    * @param name The element type name.
    * @return An integer constant representing the content type.
    * @see #getElementContentModel
    * @see #CONTENT_UNDECLARED
    * @see #CONTENT_ANY
    * @see #CONTENT_EMPTY
    * @see #CONTENT_MIXED
    * @see #CONTENT_ELEMENTS
    */
  public int getElementContentType (String name)
  {
    Object element[] = (Object[])elementInfo.get(name);
    if (element == null) {
      return CONTENT_UNDECLARED;
    } else {
      return ((Integer)element[0]).intValue();
    }
  }


  /**
    * Look up the content model of an element.
    * <p>The result will always be null unless the content type is
    * CONTENT_ELEMENTS or CONTENT_MIXED.
    * @param name The element type name.
    * @return The normalised content model, as a string.
    * @see #getElementContentType
    */
  public String getElementContentModel (String name)
  {
    Object element[] = (Object[])elementInfo.get(name);
    if (element == null) {
      return null;
    } else {
      return (String)element[1];
    }
  }


  /**
    * Register an element.
    * Array format:
    *  element type
    *  attribute hash table
    */
  void setElement (String name, int contentType,
       String contentModel, Hashtable attributes)
    throws java.lang.Exception
  {
    Object element[];

        // Try looking up the element
    element = (Object[])elementInfo.get(name);

        // Make a new one if necessary.
    if (element == null) {
      element = new Object[3];
      element[0] = new Integer(CONTENT_UNDECLARED);
      element[1] = null;
      element[2] = null;
    } else if (contentType != CONTENT_UNDECLARED &&
         ((Integer)element[0]).intValue() != CONTENT_UNDECLARED) {
      error("multiple declarations for element type", name, null);
      return;
    }

        // Insert the content type
