/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package org.apache.xml.dtm.ref;

import javax.xml.transform.SourceLocator;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

/**
 * This is the implementation of the DTM document interface.  It receives
 * requests from an XML content handler similar to that of an XML DOM or SAX parser
 * to store information from the xml document in an array based
 * dtm table structure.  This informtion is used later for document navigation,
 * query, and SAX event dispatch functions. The DTM can also be used directly as a
 * document composition model for an application.  The requests received are:
 * <ul>
 * <li>initiating DTM to set the doc handle</li>
 * <li>resetting DTM for data structure reuse</li>
 * <li>hinting the end of document to adjust the end of data structure pointers</li>
 * <li>createnodes (element, comment, text, attribute, ....)</li>
 * <li>hinting the end of an element to patch parent and siblings<li>
 * <li>setting application provided symbol name stringpool data structures</li>
 * </ul>
 * <p>State: In progress!!</p>
 *
 * %REVIEW% I _think_ the SAX convention is that "no namespace" is expressed
 * as "" rather than as null (which is the DOM's convention). What should
 * DTM expect? What should it do with the other?
 *
 * <p>Origin: the implemention is a composite logic based on the DTM of XalanJ1 and
 *     DocImpl, DocumentImpl, ElementImpl, TextImpl, etc. of XalanJ2</p>
 */
public class DTMDocumentImpl
implements DTM, org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler
{

        // Number of lower bits used to represent node index.
        protected static final byte DOCHANDLE_SHIFT = 22;
        // Masks the lower order of node handle.
        // Same as {@link DTMConstructor.IDENT_NODE_DEFAULT}
        protected static final int NODEHANDLE_MASK = (1 << (DOCHANDLE_SHIFT + 1)) - 1;
        // Masks the higher order Document handle
        // Same as {@link DTMConstructor.IDENT_DOC_DEFAULT}
        protected static final int DOCHANDLE_MASK = -1 - NODEHANDLE_MASK;

        int m_docHandle = NULL;		 // masked document handle for this dtm document
        int m_docElement = NULL;	 // nodeHandle to the root of the actual dtm doc content

        // Context for parse-and-append operations
        int currentParent = 0;			// current parent - default is document root
        int previousSibling = 0;		// previous sibling - no previous sibling
        protected int m_currentNode = -1;		// current node

        // The tree under construction can itself be used as
        // the element stack, so m_elemStack isn't needed.
        //protected Stack m_elemStack = new Stack();	 // element stack

        private boolean previousSiblingWasParent = false;
        // Local cache for record-at-a-time fetch
        int gotslot[] = new int[4];

        // endDocument recieved?
        private boolean done = false;
        boolean m_isError = false;

        static private final boolean DEBUG = false;

        /** The document base URI. */
        protected String m_documentBaseURI;

  /** If we're building the model incrementally on demand, we need to
   * be able to tell the source when to send us more data.
   *
   * Note that if this has not been set, and you attempt to read ahead
   * of the current build point, we'll probably throw a null-pointer
   * exception. We could try to wait-and-retry instead, as a very poor
   * fallback, but that has all the known problems with multithreading
   * on multiprocessors and we Don't Want to Go There.
   *
   * @see setIncrementalSAXSource
   */
  private IncrementalSAXSource m_incrSAXSource=null;


        // ========= DTM data structure declarations. ==============

        // nodes array: integer array blocks to hold the first level reference of the nodes,
        // each reference slot is addressed by a nodeHandle index value.
        // Assumes indices are not larger than {@link NODEHANDLE_MASK}
        // ({@link DOCHANDLE_SHIFT} bits).
        ChunkedIntArray nodes = new ChunkedIntArray(4);

        // text/comment table: string buffer to hold the text string values of the document,
        // each of which is addressed by the absolute offset and length in the buffer
        private FastStringBuffer m_char = new FastStringBuffer();
        // Start of string currently being accumulated into m_char;
        // needed because the string may be appended in several chunks.
        private int m_char_current_start=0;

        // %TBD% INITIALIZATION/STARTUP ISSUES
        // -- Should we really be creating these, or should they be
        // passed in from outside? Scott want to be able to share
        // pools across multiple documents, so setting them here is
        // probably not the right default.
        private DTMStringPool m_localNames = new DTMStringPool();
        private DTMStringPool m_nsNames = new DTMStringPool();
        private DTMStringPool m_prefixNames = new DTMStringPool();

        // %TBD% If we use the current ExpandedNameTable mapper, it
        // needs to be bound to the NS and local name pools. Which
        // means it needs to attach to them AFTER we've resolved their
        // startup. Or it needs to attach to this document and
        // retrieve them each time. Or this needs to be
        // an interface _implemented_ by this class... which might be simplest!
        private ExpandedNameTable m_expandedNames=
                new ExpandedNameTable();

        private XMLStringFactory m_xsf;


        /**
         * Construct a DTM.
         *
         * @param documentNumber the ID number assigned to this document.
         * It will be shifted up into the high bits and returned as part of
         * all node ID numbers, so those IDs indicate which document they
         * came from as well as a location within the document. It is the
         * DTMManager's responsibility to assign a unique number to each
         * document.
         */
        public DTMDocumentImpl(DTMManager mgr, int documentNumber,
                               DTMWSFilter whiteSpaceFilter,
                               XMLStringFactory xstringfactory){
                initDocument(documentNumber);	 // clear nodes and document handle
                m_xsf = xstringfactory;
        }

  /** Bind a IncrementalSAXSource to this DTM. If we discover we need nodes
   * that have not yet been built, we will ask this object to send us more
   * events, and it will manage interactions with its data sources.
   *
   * Note that we do not actually build the IncrementalSAXSource, since we don't
   * know what source it's reading from, what thread that source will run in,
   * or when it will run.
   *
   * @param source The IncrementalSAXSource that we want to recieve events from
   * on demand.
   */
  public void setIncrementalSAXSource(IncrementalSAXSource source)
  {
    m_incrSAXSource=source;

    // Establish SAX-stream link so we can receive the requested data
    source.setContentHandler(this);
    source.setLexicalHandler(this);

    // Are the following really needed? IncrementalSAXSource doesn't yet
    // support them, and they're mostly no-ops here...
    //source.setErrorHandler(this);
    //source.setDTDHandler(this);
    //source.setDeclHandler(this);
  }

        /**
         * Wrapper for ChunkedIntArray.append, to automatically update the
         * previous sibling's "next" reference (if necessary) and periodically
         * wake a reader who may have encountered incomplete data and entered
         * a wait state.
         * @param w0 int As in ChunkedIntArray.append
         * @param w1 int As in ChunkedIntArray.append
         * @param w2 int As in ChunkedIntArray.append
         * @param w3 int As in ChunkedIntArray.append
         * @return int As in ChunkedIntArray.append
         * @see ChunkedIntArray.append
         */
        private final int appendNode(int w0, int w1, int w2, int w3)
        {
                // A decent compiler may inline this.
                int slotnumber = nodes.appendSlot(w0, w1, w2, w3);

                if (DEBUG) System.out.println(slotnumber+": "+w0+" "+w1+" "+w2+" "+w3);

                if (previousSiblingWasParent)
                        nodes.writeEntry(previousSibling,2,slotnumber);

                previousSiblingWasParent = false;	// Set the default; endElement overrides

                return slotnumber;
        }

        // ========= DTM Implementation Control Functions. ==============

        /**
         * Set an implementation dependent feature.
         * <p>
         * %REVIEW% Do we really expect to set features on DTMs?
         *
         * @param featureId A feature URL.
         * @param state true if this feature should be on, false otherwise.
         */
        public void setFeature(String featureId, boolean state) {};

        /**
         * Set a reference pointer to the element name symbol table.
         * %REVIEW% Should this really be Public? Changing it while
         * DTM is in use would be a disaster.
         *
         * @param poolRef DTMStringPool reference to an instance of table.
         */
        public void setLocalNameTable(DTMStringPool poolRef) {
                m_localNames = poolRef;
        }

        /**
         * Get a reference pointer to the element name symbol table.
         *
         * @return DTMStringPool reference to an instance of table.
         */
        public DTMStringPool getLocalNameTable() {
                 return m_localNames;
         }

        /**
         * Set a reference pointer to the namespace URI symbol table.
         * %REVIEW% Should this really be Public? Changing it while
         * DTM is in use would be a disaster.
         *
         * @param poolRef DTMStringPool reference to an instance of table.
         */
        public void setNsNameTable(DTMStringPool poolRef) {
                m_nsNames = poolRef;
        }

        /**
         * Get a reference pointer to the namespace URI symbol table.
         *
         * @return DTMStringPool reference to an instance of table.
         */
        public DTMStringPool getNsNameTable() {
                 return m_nsNames;
         }

        /**
         * Set a reference pointer to the prefix name symbol table.
         * %REVIEW% Should this really be Public? Changing it while
         * DTM is in use would be a disaster.
         *
         * @param poolRef DTMStringPool reference to an instance of table.
         */
        public void setPrefixNameTable(DTMStringPool poolRef) {
                m_prefixNames = poolRef;
        }

        /**
         * Get a reference pointer to the prefix name symbol table.
         *
         * @return DTMStringPool reference to an instance of table.
         */
        public DTMStringPool getPrefixNameTable() {
                return m_prefixNames;
        }

         /**
          * Set a reference pointer to the content-text repository
          *
          * @param buffer FastStringBuffer reference to an instance of
          * buffer
          */
         void setContentBuffer(FastStringBuffer buffer) {
                 m_char = buffer;
         }

         /**
          * Get a reference pointer to the content-text repository
          *
          * @return FastStringBuffer reference to an instance of buffer
          */
         FastStringBuffer getContentBuffer() {
                 return m_char;
         }

  /** getContentHandler returns "our SAX builder" -- the thing that
   * someone else should send SAX events to in order to extend this
   * DTM model.
   *
   * @return null if this model doesn't respond to SAX events,
   * "this" if the DTM object has a built-in SAX ContentHandler,
   * the IncrementalSAXSource if we're bound to one and should receive
   * the SAX stream via it for incremental build purposes...
   * */
  public org.xml.sax.ContentHandler getContentHandler()
  {
    if (m_incrSAXSource instanceof IncrementalSAXSource_Filter)
      return (ContentHandler) m_incrSAXSource;
    else
      return this;
  }

  /**
   * Return this DTM's lexical handler.
   *
   * %REVIEW% Should this return null if constrution already done/begun?
   *
   * @return null if this model doesn't respond to lexical SAX events,
   * "this" if the DTM object has a built-in SAX ContentHandler,
   * the IncrementalSAXSource if we're bound to one and should receive
   * the SAX stream via it for incremental build purposes...
   */
  public LexicalHandler getLexicalHandler()
  {

    if (m_incrSAXSource instanceof IncrementalSAXSource_Filter)
      return (LexicalHandler) m_incrSAXSource;
    else
      return this;
  }

  /**
   * Return this DTM's EntityResolver.
   *
   * @return null if this model doesn't respond to SAX entity ref events.
   */
  public org.xml.sax.EntityResolver getEntityResolver()
  {

    return null;
  }

  /**
   * Return this DTM's DTDHandler.
   *
   * @return null if this model doesn't respond to SAX dtd events.
   */
  public org.xml.sax.DTDHandler getDTDHandler()
  {

    return null;
  }

  /**
   * Return this DTM's ErrorHandler.
   *
   * @return null if this model doesn't respond to SAX error events.
   */
  public org.xml.sax.ErrorHandler getErrorHandler()
  {

    return null;
  }

  /**
   * Return this DTM's DeclHandler.
   *
   * @return null if this model doesn't respond to SAX Decl events.
   */
  public org.xml.sax.ext.DeclHandler getDeclHandler()
  {

    return null;
  }

  /** @return true iff we're building this model incrementally (eg
   * we're partnered with a IncrementalSAXSource) and thus require that the
   * transformation and the parse run simultaneously. Guidance to the
   * DTMManager.
   * */
  public boolean needsTwoThreads()
  {
    return null!=m_incrSAXSource;
  }

  //================================================================
  // ========= SAX2 ContentHandler methods =========
  // Accept SAX events, use them to build/extend the DTM tree.
  // Replaces the deprecated DocumentHandler interface.

  public void characters(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
    // Actually creating the text node is handled by
    // processAccumulatedText(); here we just accumulate the
    // characters into the buffer.
    m_char.append(ch,start,length);
  }

  // Flush string accumulation into a text node
  private void processAccumulatedText()
  {
    int len=m_char.length();
    if(len!=m_char_current_start)
      {
        // The FastStringBuffer has been previously agreed upon
        appendTextChild(m_char_current_start,len-m_char_current_start);
        m_char_current_start=len;
      }
  }
  public void endDocument()
       throws org.xml.sax.SAXException
  {
    // May need to tell the low-level builder code to pop up a level.
    // There _should't_ be any significant pending text at this point.
    appendEndDocument();
  }
  public void endElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
    // No args but we do need to tell the low-level builder code to
    // pop up a level.
    appendEndElement();
  }
  public void endPrefixMapping(java.lang.String prefix)
       throws org.xml.sax.SAXException
  {
    // No-op
  }
  public void ignorableWhitespace(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
    // %TBD% I believe ignorable text isn't part of the DTM model...?
  }
  public void processingInstruction(java.lang.String target, java.lang.String data)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
    // %TBD% Which pools do target and data go into?
  }
  public void setDocumentLocator(Locator locator)
  {
    // No-op for DTM
  }
  public void skippedEntity(java.lang.String name)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
    //%TBD%
  }
  public void startDocument()
       throws org.xml.sax.SAXException
  {
    appendStartDocument();
  }
  public void startElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName, Attributes atts)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();

    // %TBD% Split prefix off qname
    String prefix=null;
    int colon=qName.indexOf(':');
    if(colon>0)
      prefix=qName.substring(0,colon);

    // %TBD% Where do we pool expandedName, or is it just the union, or...
    /**/System.out.println("Prefix="+prefix+" index="+m_prefixNames.stringToIndex(prefix));
    appendStartElement(m_nsNames.stringToIndex(namespaceURI),
                     m_localNames.stringToIndex(localName),
                     m_prefixNames.stringToIndex(prefix)); /////// %TBD%

    // %TBD% I'm assuming that DTM will require resequencing of
    // NS decls before other attrs, hence two passes are taken.
    // %TBD% Is there an easier way to test for NSDecl?
    int nAtts=(atts==null) ? 0 : atts.getLength();
    // %TBD% Countdown is more efficient if nobody cares about sequence.
    for(int i=nAtts-1;i>=0;--i)
      {
        qName=atts.getQName(i);
        if(qName.startsWith("xmlns:") || "xmlns".equals(qName))
          {
            prefix=null;
            colon=qName.indexOf(':');
            if(colon>0)
              {
                prefix=qName.substring(0,colon);
              }
            else
              {
                // %REVEIW% Null or ""?
                prefix=null; // Default prefix
              }


            appendNSDeclaration(
                                    m_prefixNames.stringToIndex(prefix),
                                    m_nsNames.stringToIndex(atts.getValue(i)),
                                    atts.getType(i).equalsIgnoreCase("ID"));
          }
      }

    for(int i=nAtts-1;i>=0;--i)
      {
        qName=atts.getQName(i);
        if(!(qName.startsWith("xmlns:") || "xmlns".equals(qName)))
          {
            // %TBD% I hate having to extract the prefix into a new
            // string when we may never use it. Consider pooling whole
            // qNames, which are already strings?
            prefix=null;
            colon=qName.indexOf(':');
            if(colon>0)
              {
                prefix=qName.substring(0,colon);
                localName=qName.substring(colon+1);
              }
            else
              {
                prefix=""; // Default prefix
                localName=qName;
              }


            m_char.append(atts.getValue(i)); // Single-string value
            int contentEnd=m_char.length();

            if(!("xmlns".equals(prefix) || "xmlns".equals(qName)))
              appendAttribute(m_nsNames.stringToIndex(atts.getURI(i)),
                                  m_localNames.stringToIndex(localName),
                                  m_prefixNames.stringToIndex(prefix),
                                  atts.getType(i).equalsIgnoreCase("ID"),
                                  m_char_current_start, contentEnd-m_char_current_start);
            m_char_current_start=contentEnd;
          }
      }
  }
  public void startPrefixMapping(java.lang.String prefix, java.lang.String uri)
       throws org.xml.sax.SAXException
  {
    // No-op in DTM, handled during element/attr processing?
  }

  //
  // LexicalHandler support. Not all SAX2 parsers support these events
  // but we may want to pass them through when they exist...
  //
  public void comment(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();

    m_char.append(ch,start,length); // Single-string value
    appendComment(m_char_current_start,length);
    m_char_current_start+=length;
  }
  public void endCDATA()
       throws org.xml.sax.SAXException
  {
    // No-op in DTM
  }
  public void endDTD()
       throws org.xml.sax.SAXException
  {
    // No-op in DTM
  }
  public void endEntity(java.lang.String name)
       throws org.xml.sax.SAXException
  {
    // No-op in DTM
  }
  public void startCDATA()
       throws org.xml.sax.SAXException
  {
    // No-op in DTM
  }
  public void startDTD(java.lang.String name, java.lang.String publicId,
      java.lang.String systemId)
       throws org.xml.sax.SAXException
  {
    // No-op in DTM
  }
  public void startEntity(java.lang.String name)
       throws org.xml.sax.SAXException
  {
    // No-op in DTM
  }


  //================================================================
  // ========= Document Handler Functions =========
  // %REVIEW% jjk -- DocumentHandler is  SAX Level 1, and deprecated....
  // and this wasn't a fully compliant or declared implementation of that API
  // in any case. Phase out in favor of SAX2 ContentHandler/LexicalHandler

        /**
         * Reset a dtm document to its initial (empty) state.
         *
         * The DTMManager will invoke this method when the dtm is created.
         *
         * @param documentNumber the handle for the DTM document.
         */
        final void initDocument(int documentNumber)
        {
                // save masked DTM document handle
                m_docHandle = documentNumber<<DOCHANDLE_SHIFT;

                // Initialize the doc -- no parent, no next-sib
                nodes.writeSlot(0,DOCUMENT_NODE,-1,-1,0);
                // wait for the first startElement to create the doc root node
                done = false;
        }

// 	/**
// 	 * Receive hint of the end of a document.
// 	 *
// 	 * <p>The content handler will invoke this method only once, and it will
// 	 * be the last method invoked during the parse.  The handler shall not
// 	 * not invoke this method until it has either abandoned parsing
// 	 * (because of an unrecoverable error) or reached the end of
// 	 * input.</p>
// 	 */
// 	public void documentEnd()
// 	{
// 		done = true;
// 		// %TBD% may need to notice the last slot number and slot count to avoid
// 		// residual data from provious use of this DTM
// 	}

// 	/**
// 	 * Receive notification of the beginning of a document.
// 	 *
// 	 * <p>The SAX parser will invoke this method only once, before any
// 	 * other methods in this interface.</p>
// 	 */
// 	public void reset()
// 	{

// 		// %TBD% reset slot 0 to indicate ChunkedIntArray reuse or wait for
// 		//       the next initDocument().
// 		m_docElement = NULL;	 // reset nodeHandle to the root of the actual dtm doc content
// 		initDocument(0);
// 	}

// 	/**
// 	 * Factory method; creates an Element node in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * <p>The XML content handler will invoke endElement() method after all
// 	 * of the element's content are processed in order to give DTM the indication
// 	 * to prepare and patch up parent and sibling node pointers.</p>
// 	 *
// 	 * <p>The following interface for createElement will use an index value corresponds
// 	 * to the symbol entry in the DTMDStringPool based symbol tables.</p>
// 	 *
// 	 * @param nsIndex The namespace of the node
// 	 * @param nameIndex The element name.
// 	 * @see #endElement
// 	 * @see org.xml.sax.Attributes
// 	 * @return nodeHandle int of the element created
// 	 */
// 	public int createElement(int nsIndex, int nameIndex, Attributes atts)
// 	{
// 		// do document root node creation here on the first element, create nodes for
// 		// this element and its attributes, store the element, namespace, and attritute
// 		// name indexes to the nodes array, keep track of the current node and parent
// 		// element used

// 		// W0  High:  Namespace  Low:  Node Type
// 		int w0 = (nsIndex << 16) | ELEMENT_NODE;
// 		// W1: Parent
// 		int w1 = currentParent;
// 		// W2: Next  (initialized as 0)
// 		int w2 = 0;
// 		// W3: Tagname
// 		int w3 = nameIndex;
// 		//int ourslot = nodes.appendSlot(w0, w1, w2, w3);
// 		int ourslot = appendNode(w0, w1, w2, w3);
// 		currentParent = ourslot;
// 		previousSibling = 0;
// 		setAttributes(atts);

// 		// set the root element pointer when creating the first element node
// 		if (m_docElement == NULL)
// 			m_docElement = ourslot;
// 		return (m_docHandle | ourslot);
// 	}

// 	// Factory method to create an Element node not associated with a given name space
// 	// using String value parameters passed in from a content handler or application
// 	/**
// 	 * Factory method; creates an Element node not associated with a given name space in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * <p>The XML content handler or application will invoke endElement() method after all
// 	 * of the element's content are processed in order to give DTM the indication
// 	 * to prepare and patch up parent and sibling node pointers.</p>
// 	 *
// 	 * <p>The following parameters for createElement contains raw string values for name
// 	 * symbols used in an Element node.</p>
// 	 *
// 	 * @param name String the element name, including the prefix if any.
// 	 * @param atts The attributes attached to the element, if any.
// 	 * @see #endElement
// 	 * @see org.xml.sax.Attributes
// 	 */
// 	public int createElement(String name, Attributes atts)
// 	{
// 		// This method wraps around the index valued interface of the createElement interface.
// 		// The raw string values are stored into the current DTM name symbol tables.  The method
// 		// method will then use the index values returned to invoke the other createElement()
// 		// onverted to index values modified to match a
// 		// method.
// 		int nsIndex = NULL;
// 		int nameIndex = m_localNames.stringToIndex(name);
// 		// note - there should be no prefix separator in the name because it is not associated
// 		// with a name space

// 		return createElement(nsIndex, nameIndex, atts);
// 	}

// 	// Factory method to create an Element node associated with a given name space
// 	// using String value parameters passed in from a content handler or application
// 	/**
// 	 * Factory method; creates an Element node associated with a given name space in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * <p>The XML content handler or application will invoke endElement() method after all
// 	 * of the element's content are processed in order to give DTM the indication
// 	 * to prepare and patch up parent and sibling node pointers.</p>
// 	 *
// 	 * <p>The following parameters for createElementNS contains raw string values for name
// 	 * symbols used in an Element node.</p>
// 	 *
// 	 * @param ns String the namespace of the node
// 	 * @param name String the element name, including the prefix if any.
// 	 * @param atts The attributes attached to the element, if any.
// 	 * @see #endElement
// 	 * @see org.xml.sax.Attributes
// 	 */
// 	public int createElementNS(String ns, String name, Attributes atts)
// 	{
// 		// This method wraps around the index valued interface of the createElement interface.
// 		// The raw string values are stored into the current DTM name symbol tables.  The method
// 		// method will then use the index values returned to invoke the other createElement()
// 		// onverted to index values modified to match a
// 		// method.
// 		int nsIndex = m_nsNames.stringToIndex(ns);
// 		int nameIndex = m_localNames.stringToIndex(name);
// 		// The prefixIndex is not needed by the indexed interface of the createElement method
// 		int prefixSep = name.indexOf(":");
// 		int prefixIndex = m_prefixNames.stringToIndex(name.substring(0, prefixSep));
// 		return createElement(nsIndex, nameIndex, atts);
// 	}

// 	/**
// 	 * Receive an indication for the end of an element.
// 	 *
// 	 * <p>The XML content handler will invoke this method at the end of every
// 	 * element in the XML document to give hint its time to pop up the current
// 	 * element and parent and patch up parent and sibling pointers if necessary
// 	 *
// 	 * <p>%tbd% The following interface may need to be modified to match a
// 	 * coordinated access to the DTMDStringPool based symbol tables.</p>
// 		 *
// 	 * @param ns the namespace of the element
// 	 * @param name The element name
// 	 */
// 	public void endElement(String ns, String name)
// 	{
// 		// pop up the stacks

// 		//
// 		if (previousSiblingWasParent)
// 			nodes.writeEntry(previousSibling, 2, NULL);

// 		// Pop parentage
// 		previousSibling = currentParent;
// 		nodes.readSlot(currentParent, gotslot);
// 		currentParent = gotslot[1] & 0xFFFF;

// 		// The element just being finished will be
// 		// the previous sibling for the next operation
// 		previousSiblingWasParent = true;

// 		// Pop a level of namespace table
// 		// namespaceTable.removeLastElem();
// 	}

// 	/**
// 	 * Creates attributes for the current node.
// 	 *
// 	 * @param atts Attributes to be created.
// 	 */
// 	void setAttributes(Attributes atts) {
// 		int atLength = (null == atts) ? 0 : atts.getLength();
// 		for (int i=0; i < atLength; i++) {
// 			String qname = atts.getQName(i);
// 			createAttribute(atts.getQName(i), atts.getValue(i));
// 		}
// 	}

// 	/**
// 	 * Appends an attribute to the document.
// 	 * @param qname Qualified Name of the attribute
// 	 * @param value Value of the attribute
// 	 * @return Handle of node
// 	 */
// 	public int createAttribute(String qname, String value) {
// 		int colonpos = qname.indexOf(":");
// 		String attName = qname.substring(colonpos+1);
// 		int w0 = 0;
// 		if (colonpos > 0) {
// 			String prefix = qname.substring(0, colonpos);
// 			if (prefix.equals("xml")) {
// 				//w0 = ATTRIBUTE_NODE |
// 				//	(org.apache.xalan.templates.Constants.S_XMLNAMESPACEURI << 16);
// 			} else {
// 				//w0 = ATTRIBUTE_NODE |
// 			}
// 		} else {
// 			w0 = ATTRIBUTE_NODE;
// 		}
// 		// W1:  Parent
// 		int w1 = currentParent;
// 		// W2:  Next (not yet resolved)
// 		int w2 = 0;
// 		// W3:  Tag name
// 		int w3 = m_localNames.stringToIndex(attName);
// 		// Add node
// 		int ourslot = appendNode(w0, w1, w2, w3);
// 		previousSibling = ourslot;	// Should attributes be previous siblings

// 		// W0: Node Type
// 		w0 = TEXT_NODE;
// 		// W1: Parent
// 		w1 = ourslot;
// 		// W2: Start Position within buffer
// 		w2 = m_char.length();
// 		m_char.append(value);
// 		// W3: Length
// 		w3 = m_char.length() - w2;
// 		appendNode(w0, w1, w2, w3);
// 		charStringStart=m_char.length();
// 		charStringLength = 0;
// 		//previousSibling = ourslot;
// 		// Attrs are Parents
// 		previousSiblingWasParent = true;
// 		return (m_docHandle | ourslot);
// 	}

// 	/**
// 	 * Factory method; creates a Text node in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * @param text String The characters text string from the XML document.
// 	 * @return int DTM node-number of the text node created
// 	 */
// 	public int createTextNode(String text)
// 	throws DTMException
// 	{
// 		// wraps around the index value based createTextNode method
// 		return createTextNode(text.toCharArray(), 0, text.length());
// 	}

// 	/**
// 	 * Factory method; creates a Text node in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * %REVIEW% for text normalization issues, unless we are willing to
// 	 * insist that all adjacent text must be merged before this method
// 	 * is called.
// 	 *
// 	 * @param ch The characters from the XML document.
// 	 * @param start The start position in the array.
// 	 * @param length The number of characters to read from the array.
// 	 */
// 	public int createTextNode(char ch[], int start, int length)
// 	throws DTMException
// 	{
// 		m_char.append(ch, start, length);		// store the chunk to the text/comment string table

// 		// create a Text Node
// 		// %TBD% may be possible to combine with appendNode()to replace the next chunk of code
// 		int w0 = TEXT_NODE;
// 		// W1: Parent
// 		int w1 = currentParent;
// 		// W2: Start position within m_char
// 		int w2 = charStringStart;
// 		// W3: Length of the full string
// 		int w3 = length;
// 		int ourslot = appendNode(w0, w1, w2, w3);
// 		previousSibling = ourslot;

// 		charStringStart=m_char.length();
// 		charStringLength = 0;
// 		return (m_docHandle | ourslot);
// 	}

// 	/**
// 	 * Factory method; creates a Comment node in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * @param text String The characters text string from the XML document.
// 	 * @return int DTM node-number of the text node created
// 	 */
// 	public int createComment(String text)
// 	throws DTMException
// 	{
// 		// wraps around the index value based createTextNode method
// 		return createComment(text.toCharArray(), 0, text.length());
// 	}

// 	/**
// 	 * Factory method; creates a Comment node in this document.
// 	 *
// 	 * The node created will be chained according to its natural order of request
// 	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
// 	 *
// 	 * @param ch An array holding the characters in the comment.
// 	 * @param start The starting position in the array.
// 	 * @param length The number of characters to use from the array.
// 	 * @see DTMException
// 	 */
// 	public int createComment(char ch[], int start, int length)
// 	throws DTMException
// 	{
// 		m_char.append(ch, start, length);		// store the comment string to the text/comment string table

// 		// create a Comment Node
// 		// %TBD% may be possible to combine with appendNode()to replace the next chunk of code
// 		int w0 = COMMENT_NODE;
// 		// W1: Parent
// 		int w1 = currentParent;
// 		// W2: Start position within m_char
// 		int w2 = charStringStart;
// 		// W3: Length of the full string
// 		int w3 = length;
// 		int ourslot = appendNode(w0, w1, w2, w3);
// 		previousSibling = ourslot;

// 		charStringStart=m_char.length();
// 		charStringLength = 0;
// 		return (m_docHandle | ourslot);
// 	}

// 	// Counters to keep track of the current text string being accumulated with respect
// 	// to the text/comment string table: charStringStart should point to the starting
// 	// offset of the string in the table and charStringLength the acccumulated length when
// 	// appendAccumulatedText starts, and reset to the end of the table and 0 at the end
// 	// of appendAccumulatedText for the next set of characters receives
// 	int charStringStart=0,charStringLength=0;

        // ========= Document Navigation Functions =========

        /** Given a node handle, test if it has child nodes.
         * <p> %REVIEW% This is obviously useful at the DOM layer, where it
         * would permit testing this without having to create a proxy
         * node. It's less useful in the DTM API, where
         * (dtm.getFirstChild(nodeHandle)!=DTM.NULL) is just as fast and
         * almost as self-evident. But it's a convenience, and eases porting
         * of DOM code to DTM.  </p>
         *
         * @param nodeHandle int Handle of the node.
         * @return int true if the given node has child nodes.
         */
        public boolean hasChildNodes(int nodeHandle) {
                return(getFirstChild(nodeHandle) != NULL);
        }

        /**
         * Given a node handle, get the handle of the node's first child.
         * If not yet resolved, waits for more nodes to be added to the document and
         * tries again.
         *
         * @param nodeHandle int Handle of the node.
         * @return int DTM node-number of first child, or DTM.NULL to indicate none exists.
         */
        public int getFirstChild(int nodeHandle) {

                // ###shs worry about tracing/debug later
                nodeHandle &= NODEHANDLE_MASK;
                // Read node into variable
                nodes.readSlot(nodeHandle, gotslot);

                // type is the last half of first slot
                short type = (short) (gotslot[0] & 0xFFFF);

                // Check to see if Element or Document node
                if ((type == ELEMENT_NODE) || (type == DOCUMENT_NODE) ||
                                (type == ENTITY_REFERENCE_NODE)) {

                        // In case when Document root is given
                        //	if (nodeHandle == 0) nodeHandle = 1;
                        // %TBD% Probably was a mistake.
                        // If someone explicitly asks for first child
                        // of Document, I would expect them to want
                        // that and only that.

                        int kid = nodeHandle + 1;
                        nodes.readSlot(kid, gotslot);
                        while (ATTRIBUTE_NODE == (gotslot[0] & 0xFFFF)) {
                                // points to next sibling
                                kid = gotslot[2];
                                // Return NULL if node has only attributes
                                if (kid == NULL) return NULL;
                                nodes.readSlot(kid, gotslot);
                        }
                        // If parent slot matches given parent, return kid
                        if (gotslot[1] == nodeHandle)
                        {
                          int firstChild = kid | m_docHandle;

                          return firstChild;
                        }
                }
                // No child found

                return NULL;
        }

        /**
        * Given a node handle, advance to its last child.
        * If not yet resolved, waits for more nodes to be added to the document and
        * tries again.
        *
        * @param nodeHandle int Handle of the node.
        * @return int Node-number of last child,
        * or DTM.NULL to indicate none exists.
        */
        public int getLastChild(int nodeHandle) {
                // ###shs put trace/debug later
                nodeHandle &= NODEHANDLE_MASK;
                // do not need to test node type since getFirstChild does that
                int lastChild = NULL;
                for (int nextkid = getFirstChild(nodeHandle); nextkid != NULL;
                                nextkid = getNextSibling(nextkid)) {
                        lastChild = nextkid;
                }
                return lastChild | m_docHandle;
        }

        /**
         * Retrieves an attribute node by by qualified name and namespace URI.
         *
         * @param nodeHandle int Handle of the node upon which to look up this attribute.
         * @param namespaceURI The namespace URI of the attribute to
         *   retrieve, or null.
         * @param name The local name of the attribute to
         *   retrieve.
         * @return The attribute node handle with the specified name (
         *   <code>nodeName</code>) or <code>DTM.NULL</code> if there is no such
         *   attribute.
         */
        public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
                int nsIndex = m_nsNames.stringToIndex(namespaceURI),
                                                                        nameIndex = m_localNames.stringToIndex(name);
                nodeHandle &= NODEHANDLE_MASK;
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                // If nodeHandle points to element next slot would be first attribute
                if (type == ELEMENT_NODE)
                        nodeHandle++;
                // Iterate through Attribute Nodes
                while (type == ATTRIBUTE_NODE) {
                        if ((nsIndex == (gotslot[0] << 16)) && (gotslot[3] == nameIndex))
                                return nodeHandle | m_docHandle;
                        // Goto next sibling
                        nodeHandle = gotslot[2];
                        nodes.readSlot(nodeHandle, gotslot);
                }
                return NULL;
        }

        /**
         * Given a node handle, get the index of the node's first attribute.
         *
         * @param nodeHandle int Handle of the Element node.
         * @return Handle of first attribute, or DTM.NULL to indicate none exists.
         */
        public int getFirstAttribute(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;

                // %REVIEW% jjk: Just a quick observation: If you're going to
                // call readEntry repeatedly on the same node, it may be
                // more efficiently to do a readSlot to get the data locally,
                // reducing the addressing and call-and-return overhead.

                // Should we check if handle is element (do we want sanity checks?)
                if (ELEMENT_NODE != (nodes.readEntry(nodeHandle, 0) & 0xFFFF))
                        return NULL;
                // First Attribute (if any) should be at next position in table
                nodeHandle++;
                return(ATTRIBUTE_NODE == (nodes.readEntry(nodeHandle, 0) & 0xFFFF)) ?
                nodeHandle | m_docHandle : NULL;
        }

        /**
         * Given a node handle, get the index of the node's first child.
         * If not yet resolved, waits for more nodes to be added to the document and
         * tries again
         *
         * @param nodeHandle handle to node, which should probably be an element
         *                   node, but need not be.
         *
         * @param inScope    true if all namespaces in scope should be returned,
         *                   false if only the namespace declarations should be
         *                   returned.
         * @return handle of first namespace, or DTM.NULL to indicate none exists.
         */
        public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {

                return NULL;
        }

        /**
         * Given a node handle, advance to its next sibling.
         *
         * %TBD% This currently uses the DTM-internal definition of
         * sibling; eg, the last attr's next sib is the first
         * child. In the old DTM, the DOM proxy layer provided the
         * additional logic for the public view.  If we're rewriting
         * for XPath emulation, that test must be done here.
         *
         * %TBD% CODE INTERACTION WITH INCREMENTAL PARSE - If not yet
         * resolved, should wait for more nodes to be added to the document
         * and tries again.
         *
         * @param nodeHandle int Handle of the node.
         * @return int Node-number of next sibling,
         * or DTM.NULL to indicate none exists.
         * */
        public int getNextSibling(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                // Document root has no next sibling
                if (nodeHandle == 0)
                        return NULL;

                short type = (short) (nodes.readEntry(nodeHandle, 0) & 0xFFFF);
                if ((type == ELEMENT_NODE) || (type == ATTRIBUTE_NODE) ||
                                (type == ENTITY_REFERENCE_NODE)) {
                        int nextSib = nodes.readEntry(nodeHandle, 2);
                        if (nextSib == NULL)
                                return NULL;
                        if (nextSib != 0)
                                return (m_docHandle | nextSib);
                        // ###shs should cycle/wait if nextSib is 0? Working on threading next
                }
                // Next Sibling is in the next position if it shares the same parent
                int thisParent = nodes.readEntry(nodeHandle, 1);

                if (nodes.readEntry(++nodeHandle, 1) == thisParent)
                        return (m_docHandle | nodeHandle);

                return NULL;
        }

        /**
         * Given a node handle, find its preceeding sibling.
         * WARNING: DTM is asymmetric; this operation is resolved by search, and is
         * relatively expensive.
         *
         * @param nodeHandle the id of the node.
         * @return int Node-number of the previous sib,
         * or DTM.NULL to indicate none exists.
         */
        public int getPreviousSibling(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                // Document root has no previous sibling
                if (nodeHandle == 0)
                        return NULL;

                int parent = nodes.readEntry(nodeHandle, 1);
                int kid = NULL;
                for (int nextkid = getFirstChild(parent); nextkid != nodeHandle;
                                nextkid = getNextSibling(nextkid)) {
                        kid = nextkid;
                }
                return kid | m_docHandle;
        }

        /**
         * Given a node handle, advance to the next attribute. If an
         * element, we advance to its first attribute; if an attr, we advance to
         * the next attr on the same node.
         *
         * @param nodeHandle int Handle of the node.
         * @return int DTM node-number of the resolved attr,
         * or DTM.NULL to indicate none exists.
         */
        public int getNextAttribute(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                nodes.readSlot(nodeHandle, gotslot);

                //%REVIEW% Why are we using short here? There's no storage
                //reduction for an automatic variable, especially one used
                //so briefly, and it typically costs more cycles to process
                //than an int would.
                short type = (short) (gotslot[0] & 0xFFFF);

                if (type == ELEMENT_NODE) {
                        return getFirstAttribute(nodeHandle);
                } else if (type == ATTRIBUTE_NODE) {
                        if (gotslot[2] != NULL)
                                return (m_docHandle | gotslot[2]);
                }
                return NULL;
        }

        /**
         * Given a namespace handle, advance to the next namespace.
         *
         * %TBD% THIS METHOD DOES NOT MATCH THE CURRENT SIGNATURE IN
         * THE DTM INTERFACE.  FIX IT, OR JUSTIFY CHANGING THE DTM
         * API.
         *
         * @param namespaceHandle handle to node which must be of type NAMESPACE_NODE.
         * @return handle of next namespace, or DTM.NULL to indicate none exists.
         */
        public int getNextNamespaceNode(int baseHandle,int namespaceHandle, boolean inScope) {
                // ###shs need to work on namespace
                return NULL;
        }

        /**
         * Given a node handle, advance to its next descendant.
         * If not yet resolved, waits for more nodes to be added to the document and
         * tries again.
         *
         * @param subtreeRootHandle
         * @param nodeHandle int Handle of the node.
         * @return handle of next descendant,
         * or DTM.NULL to indicate none exists.
         */
        public int getNextDescendant(int subtreeRootHandle, int nodeHandle) {
                subtreeRootHandle &= NODEHANDLE_MASK;
                nodeHandle &= NODEHANDLE_MASK;
                // Document root [Document Node? -- jjk] - no next-sib
                if (nodeHandle == 0)
                        return NULL;
                while (!m_isError) {
                        // Document done and node out of bounds
                        if (done && (nodeHandle > nodes.slotsUsed()))
                                break;
                        if (nodeHandle > subtreeRootHandle) {
                                nodes.readSlot(nodeHandle+1, gotslot);
                                if (gotslot[2] != 0) {
                                        short type = (short) (gotslot[0] & 0xFFFF);
                                        if (type == ATTRIBUTE_NODE) {
                                                nodeHandle +=2;
                                        } else {
                                                int nextParentPos = gotslot[1];
                                                if (nextParentPos >= subtreeRootHandle)
                                                        return (m_docHandle | (nodeHandle+1));
                                                else
                                                        break;
                                        }
                                } else if (!done) {
                                        // Add wait logic here
                                } else
                                        break;
                        } else {
                                nodeHandle++;
                        }
                }
                // Probably should throw error here like original instead of returning
                return NULL;
        }

        /**
         * Given a node handle, advance to the next node on the following axis.
         *
         * @param axisContextHandle the start of the axis that is being traversed.
         * @param nodeHandle
         * @return handle of next sibling,
         * or DTM.NULL to indicate none exists.
         */
        public int getNextFollowing(int axisContextHandle, int nodeHandle) {
                //###shs still working on
                return NULL;
        }

        /**
         * Given a node handle, advance to the next node on the preceding axis.
         *
         * @param axisContextHandle the start of the axis that is being traversed.
         * @param nodeHandle the id of the node.
         * @return int Node-number of preceding sibling,
         * or DTM.NULL to indicate none exists.
         */
        public int getNextPreceding(int axisContextHandle, int nodeHandle) {
                // ###shs copied from Xalan 1, what is this suppose to do?
                nodeHandle &= NODEHANDLE_MASK;
                while (nodeHandle > 1) {
                        nodeHandle--;
                        if (ATTRIBUTE_NODE == (nodes.readEntry(nodeHandle, 0) & 0xFFFF))
                                continue;

                        // if nodeHandle is _not_ an ancestor of
                        // axisContextHandle, specialFind will return it.
                        // If it _is_ an ancestor, specialFind will return -1

                        // %REVIEW% unconditional return defeats the
                        // purpose of the while loop -- does this
                        // logic make any sense?

                        return (m_docHandle | nodes.specialFind(axisContextHandle, nodeHandle));
                }
                return NULL;
        }

        /**
         * Given a node handle, find its parent node.
         *
         * @param nodeHandle the id of the node.
         * @return int Node-number of parent,
         * or DTM.NULL to indicate none exists.
         */
        public int getParent(int nodeHandle) {
                // Should check to see within range?

                // Document Root should not have to be handled differently
                return (m_docHandle | nodes.readEntry(nodeHandle, 1));
        }

        /**
         * Returns the root element of the document.
         * @return nodeHandle to the Document Root.
         */
        public int getDocumentRoot() {
                return (m_docHandle | m_docElement);
        }

        /**
         * Given a node handle, find the owning document node.
         *
         * @return int Node handle of document, which should always be valid.
         */
        public int getDocument() {
                return m_docHandle;
        }

        /**
         * Given a node handle, find the owning document node.  This has the exact
         * same semantics as the DOM Document method of the same name, in that if
         * the nodeHandle is a document node, it will return NULL.
         *
         * <p>%REVIEW% Since this is DOM-specific, it may belong at the DOM
         * binding layer. Included here as a convenience function and to
         * aid porting of DOM code to DTM.</p>
         *
         * @param nodeHandle the id of the node.
         * @return int Node handle of owning document, or NULL if the nodeHandle is
         *             a document.
         */
        public int getOwnerDocument(int nodeHandle) {
                // Assumption that Document Node is always in 0 slot
                if ((nodeHandle & NODEHANDLE_MASK) == 0)
                        return NULL;
                return (nodeHandle & DOCHANDLE_MASK);
        }

        /**
         * Given a node handle, find the owning document node.  This has the DTM
         * semantics; a Document node is its own owner.
         *
         * <p>%REVIEW% Since this is DOM-specific, it may belong at the DOM
         * binding layer. Included here as a convenience function and to
         * aid porting of DOM code to DTM.</p>
         *
         * @param nodeHandle the id of the node.
         * @return int Node handle of owning document, or NULL if the nodeHandle is
         *             a document.
         */
        public int getDocumentRoot(int nodeHandle) {
                // Assumption that Document Node is always in 0 slot
                if ((nodeHandle & NODEHANDLE_MASK) == 0)
                        return NULL;
                return (nodeHandle & DOCHANDLE_MASK);
        }

        /**
         * Get the string-value of a node as a String object
         * (see http://www.w3.org/TR/xpath#data-model
         * for the definition of a node's string-value).
         *
         * @param nodeHandle The node ID.
         *
         * @return A string object that represents the string-value of the given node.
         */
        public XMLString getStringValue(int nodeHandle) {
        // ###zaj - researching
        nodes.readSlot(nodeHandle, gotslot);
        int nodetype=gotslot[0] & 0xFF;
        String value=null;

        switch (nodetype) {
        case TEXT_NODE:
        case COMMENT_NODE:
        case CDATA_SECTION_NODE:
                value= m_char.getString(gotslot[2], gotslot[3]);
                break;
        case PROCESSING_INSTRUCTION_NODE:
        case ATTRIBUTE_NODE:
        case ELEMENT_NODE:
        case ENTITY_REFERENCE_NODE:
        default:
                break;
        }
        return m_xsf.newstr( value );

        }

        /**
         * Get number of character array chunks in
         * the string-value of a node.
         * (see http://www.w3.org/TR/xpath#data-model
         * for the definition of a node's string-value).
         * Note that a single text node may have multiple text chunks.
         *
         * EXPLANATION: This method is an artifact of the fact that the
         * underlying m_chars object may not store characters in a
         * single contiguous array -- for example,the current
         * FastStringBuffer may split a single node's text across
         * multiple allocation units.  This call tells us how many
         * separate accesses will be required to retrieve the entire
         * content. PLEASE NOTE that this may not be the same as the
         * number of SAX characters() events that caused the text node
         * to be built in the first place, since m_chars buffering may
         * be on different boundaries than the parser's buffers.
         *
         * @param nodeHandle The node ID.
         *
         * @return number of character array chunks in
         *         the string-value of a node.
         * */
        //###zaj - tbd
        public int getStringValueChunkCount(int nodeHandle)
        {
                //###zaj    return value
                return 0;
        }

        /**
         * Get a character array chunk in the string-value of a node.
         * (see http://www.w3.org/TR/xpath#data-model
         * for the definition of a node's string-value).
         * Note that a single text node may have multiple text chunks.
         *
         * EXPLANATION: This method is an artifact of the fact that
         * the underlying m_chars object may not store characters in a
         * single contiguous array -- for example,the current
         * FastStringBuffer may split a single node's text across
         * multiple allocation units.  This call retrieves a single
         * contiguous portion of the text -- as much as m-chars was
         * able to store in a single allocation unit.  PLEASE NOTE
         * that this may not be the same granularityas the SAX
         * characters() events that caused the text node to be built
         * in the first place, since m_chars buffering may be on
         * different boundaries than the parser's buffers.
         *
         * @param nodeHandle The node ID.
         * @param chunkIndex Which chunk to get.
         * @param startAndLen An array of 2 where the start position and length of
         *                    the chunk will be returned.
         *
         * @return The character array reference where the chunk occurs.  */
        //###zaj - tbd
        public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
                                                                                                                                                int[] startAndLen) {return new char[0];}

        /**
         * Given a node handle, return an ID that represents the node's expanded name.
         *
         * @param nodeHandle The handle to the node in question.
         *
         * @return the expanded-name id of the node.
         */
        public int getExpandedTypeID(int nodeHandle) {
           nodes.readSlot(nodeHandle, gotslot);
           String qName = m_localNames.indexToString(gotslot[3]);
           // Remove prefix from qName
           // %TBD% jjk This is assuming the elementName is the qName.
           int colonpos = qName.indexOf(":");
           String localName = qName.substring(colonpos+1);
           // Get NS
           String namespace = m_nsNames.indexToString(gotslot[0] << 16);
           // Create expanded name
           String expandedName = namespace + ":" + localName;
           int expandedNameID = m_nsNames.stringToIndex(expandedName);

        return expandedNameID;
        }


        /**
         * Given an expanded name, return an ID.  If the expanded-name does not
         * exist in the internal tables, the entry will be created, and the ID will
         * be returned.  Any additional nodes that are created that have this
         * expanded name will use this ID.
         *
         * @return the expanded-name id of the node.
         */
        public int getExpandedTypeID(String namespace, String localName, int type) {
           // Create expanded name
          // %TBD% jjk Expanded name is bitfield-encoded as
          // typeID[6]nsuriID[10]localID[16]. Switch to that form, and to
          // accessing the ns/local via their tables rather than confusing
          // nsnames and expandednames.
           String expandedName = namespace + ":" + localName;
           int expandedNameID = m_nsNames.stringToIndex(expandedName);

           return expandedNameID;
        }


        /**
         * Given an expanded-name ID, return the local name part.
         *
         * @param ExpandedNameID an ID that represents an expanded-name.
         * @return String Local name of this node.
         */
        public String getLocalNameFromExpandedNameID(int ExpandedNameID) {

           // Get expanded name
           String expandedName = m_localNames.indexToString(ExpandedNameID);
           // Remove prefix from expanded name
           int colonpos = expandedName.indexOf(":");
           String localName = expandedName.substring(colonpos+1);
           return localName;
        }


        /**
         * Given an expanded-name ID, return the namespace URI part.
         *
         * @param ExpandedNameID an ID that represents an expanded-name.
         * @return String URI value of this node's namespace, or null if no
         * namespace was resolved.
        */
        public String getNamespaceFromExpandedNameID(int ExpandedNameID) {

           String expandedName = m_localNames.indexToString(ExpandedNameID);
           // Remove local name from expanded name
           int colonpos = expandedName.indexOf(":");
           String nsName = expandedName.substring(0, colonpos);

        return nsName;
        }


        /**
         * fixednames
        */
        private static final String[] fixednames=
        {
                null,null,							// nothing, Element
                null,"#text",						// Attr, Text
                "#cdata_section",null,	// CDATA, EntityReference
                null,null,							// Entity, PI
                "#comment","#document",	// Comment, Document
                null,"#document-fragment", // Doctype, DocumentFragment
                null};									// Notation

        /**
         * Given a node handle, return its DOM-style node name. This will
         * include names such as #text or #document.
         *
         * @param nodeHandle the id of the node.
         * @return String Name of this node, which may be an empty string.
         * %REVIEW% Document when empty string is possible...
         */
        public String getNodeName(int nodeHandle) {
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                String name = fixednames[type];
                if (null == name) {
                  int i=gotslot[3];
                  /**/System.out.println("got i="+i+" "+(i>>16)+"/"+(i&0xffff));

                  name=m_localNames.indexToString(i & 0xFFFF);
                  String prefix=m_prefixNames.indexToString(i >>16);
                  if(prefix!=null && prefix.length()>0)
                    name=prefix+":"+name;
                }
                return name;
        }

        /**
         * Given a node handle, return the XPath node name.  This should be
         * the name as described by the XPath data model, NOT the DOM-style
         * name.
         *
         * @param nodeHandle the id of the node.
         * @return String Name of this node.
         */
        public String getNodeNameX(int nodeHandle) {return null;}

        /**
         * Given a node handle, return its DOM-style localname.
         * (As defined in Namespaces, this is the portion of the name after any
         * colon character)
         *
         * %REVIEW% What's the local name of something other than Element/Attr?
         * Should this be DOM-style (undefined unless namespaced), or other?
         *
         * @param nodeHandle the id of the node.
         * @return String Local name of this node.
         */
        public String getLocalName(int nodeHandle) {
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                String name = "";
                if ((type==ELEMENT_NODE) || (type==ATTRIBUTE_NODE)) {
                  int i=gotslot[3];
                  name=m_localNames.indexToString(i & 0xFFFF);
                  if(name==null) name="";
                }
                return name;
        }

        /**
         * Given a namespace handle, return the prefix that the namespace decl is
         * mapping.
         * Given a node handle, return the prefix used to map to the namespace.
         *
         * <p> %REVIEW% Are you sure you want "" for no prefix?  </p>
         *
         * %REVIEW%  Should this be DOM-style (undefined unless namespaced),
         * or other?
         *
         * @param nodeHandle the id of the node.
         * @return String prefix of this node's name, or "" if no explicit
         * namespace prefix was given.
  
