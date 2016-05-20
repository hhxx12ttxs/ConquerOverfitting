/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * $Id: ToStream.java,v 1.4 2005/11/10 06:43:26 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

//import com.sun.media.sound.IESecurity;

/**
 * This abstract class is a base class for other stream
 * serializers (xml, html, text ...) that write output to a stream.
 *
 * @xsl.usage internal
 */
abstract public class ToStream extends SerializerBase
{

    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";

    /** Stack to keep track of disabling output escaping. */
    protected BoolStack m_disableOutputEscapingStates = new BoolStack();


    /**
     * The encoding information associated with this serializer.
     * Although initially there is no encoding,
     * there is a dummy EncodingInfo object that will say
     * that every character is in the encoding. This is useful
     * for a serializer that is in temporary output state and has
     * no associated encoding. A serializer in final output state
     * will have an encoding, and will worry about whether
     * single chars or surrogate pairs of high/low chars form
     * characters in the output encoding.
     */
    EncodingInfo m_encodingInfo = new EncodingInfo(null,null);

    /**
     * Method reference to the sun.io.CharToByteConverter#canConvert method
     * for this encoding.  Invalid if m_charToByteConverter is null.
     */
    java.lang.reflect.Method m_canConvertMeth;



    /**
     * Boolean that tells if we already tried to get the converter.
     */
    boolean m_triedToGetConverter = false;


    /**
     * Opaque reference to the sun.io.CharToByteConverter for this
     * encoding.
     */
    Object m_charToByteConverter = null;


    /**
     * Stack to keep track of whether or not we need to
     * preserve whitespace.
     *
     * Used to push/pop values used for the field m_ispreserve, but
     * m_ispreserve is only relevant if m_doIndent is true.
     * If m_doIndent is false this field has no impact.
     *
     */
    protected BoolStack m_preserves = new BoolStack();

    /**
     * State flag to tell if preservation of whitespace
     * is important.
     *
     * Used only in shouldIndent() but only if m_doIndent is true.
     * If m_doIndent is false this flag has no impact.
     *
     */
    protected boolean m_ispreserve = false;

    /**
     * State flag that tells if the previous node processed
     * was text, so we can tell if we should preserve whitespace.
     *
     * Used in endDocument() and shouldIndent() but
     * only if m_doIndent is true.
     * If m_doIndent is false this flag has no impact.
     */
    protected boolean m_isprevtext = false;

    /**
     * The maximum character size before we have to resort
     * to escaping.
     */
    protected int m_maxCharacter = Encodings.getLastPrintable();


    /**
     * The system line separator for writing out line breaks.
     * The default value is from the system property,
     * but this value can be set through the xsl:output
     * extension attribute xalan:line-separator.
     */
    protected char[] m_lineSep =
        System.getProperty("line.separator").toCharArray();

    /**
     * True if the the system line separator is to be used.
     */
    protected boolean m_lineSepUse = true;

    /**
     * The length of the line seperator, since the write is done
     * one character at a time.
     */
    protected int m_lineSepLen = m_lineSep.length;

    /**
     * Map that tells which characters should have special treatment, and it
     *  provides character to entity name lookup.
     */
    protected CharInfo m_charInfo;

    /** True if we control the buffer, and we should flush the output on endDocument. */
    boolean m_shouldFlush = true;

    /**
     * Add space before '/>' for XHTML.
     */
    protected boolean m_spaceBeforeClose = false;

    /**
     * Flag to signal that a newline should be added.
     *
     * Used only in indent() which is called only if m_doIndent is true.
     * If m_doIndent is false this flag has no impact.
     */
    boolean m_startNewLine;

    /**
     * Tells if we're in an internal document type subset.
     */
    protected boolean m_inDoctype = false;

    /**
       * Flag to quickly tell if the encoding is UTF8.
       */
    boolean m_isUTF8 = false;

    /** The xsl:output properties. */
    protected Properties m_format;

    /**
     * remembers if we are in between the startCDATA() and endCDATA() callbacks
     */
    protected boolean m_cdataStartCalled = false;

    /**
     * If this flag is true DTD entity references are not left as-is,
     * which is exiting older behavior.
     */
    private boolean m_expandDTDEntities = true;


    /**
     * Default constructor
     */
    public ToStream()
    {
    }

    /**
     * This helper method to writes out "]]>" when closing a CDATA section.
     *
     * @throws org.xml.sax.SAXException
     */
    protected void closeCDATA() throws org.xml.sax.SAXException
    {
        try
        {
            m_writer.write(CDATA_DELIMITER_CLOSE);
            // write out a CDATA section closing "]]>"
            m_cdataTagOpen = false; // Remember that we have done so.
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Serializes the DOM node. Throws an exception only if an I/O
     * exception occured while serializing.
     *
     * @param node Node to serialize.
     * @throws IOException An I/O exception occured while serializing
     */
    public void serialize(Node node) throws IOException
    {

        try
        {
            TreeWalker walker =
                new TreeWalker(this);

            walker.traverse(node);
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new WrappedRuntimeException(se);
        }
    }

    /**
     * Return true if the character is the high member of a surrogate pair.
     *
     * NEEDSDOC @param c
     *
     * NEEDSDOC ($objectName$) @return
     */
    static final boolean isUTF16Surrogate(char c)
    {
        return (c & 0xFC00) == 0xD800;
    }

    /**
     * Taken from XSLTC
     */
    private boolean m_escaping = true;

    /**
     * Flush the formatter's result stream.
     *
     * @throws org.xml.sax.SAXException
     */
    protected final void flushWriter() throws org.xml.sax.SAXException
    {
        final java.io.Writer writer = m_writer;
        if (null != writer)
        {
            try
            {
                if (writer instanceof WriterToUTF8Buffered)
                {
                    if (m_shouldFlush)
                         ((WriterToUTF8Buffered) writer).flush();
                    else
                         ((WriterToUTF8Buffered) writer).flushBuffer();
                }
                if (writer instanceof WriterToASCI)
                {
                    if (m_shouldFlush)
                        writer.flush();
                }
                else
                {
                    // Flush always.
                    // Not a great thing if the writer was created
                    // by this class, but don't have a choice.
                    writer.flush();
                }
            }
            catch (IOException ioe)
            {
                throw new org.xml.sax.SAXException(ioe);
            }
        }
    }

    /**
     * Get the output stream where the events will be serialized to.
     *
     * @return reference to the result stream, or null of only a writer was
     * set.
     */
    public OutputStream getOutputStream()
    {

        if (m_writer instanceof WriterToUTF8Buffered)
            return ((WriterToUTF8Buffered) m_writer).getOutputStream();
        if (m_writer instanceof WriterToASCI)
            return ((WriterToASCI) m_writer).getOutputStream();
        else
            return null;
    }

    // Implement DeclHandler

    /**
     *   Report an element type declaration.
     *
     *   <p>The content model will consist of the string "EMPTY", the
     *   string "ANY", or a parenthesised group, optionally followed
     *   by an occurrence indicator.  The model will be normalized so
     *   that all whitespace is removed,and will include the enclosing
     *   parentheses.</p>
     *
     *   @param name The element type name.
     *   @param model The content model as a normalized string.
     *   @exception SAXException The application may raise an exception.
     */
    public void elementDecl(String name, String model) throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            final java.io.Writer writer = m_writer;
            DTDprolog();

            writer.write("<!ELEMENT ");
            writer.write(name);
            writer.write(' ');
            writer.write(model);
            writer.write('>');
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Report an internal entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param value The replacement text of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #externalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void internalEntityDecl(String name, String value)
        throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            DTDprolog();
            outputEntityDecl(name, value);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Output the doc type declaration.
     *
     * @param name non-null reference to document type name.
     * NEEDSDOC @param value
     *
     * @throws org.xml.sax.SAXException
     */
    void outputEntityDecl(String name, String value) throws IOException
    {
        final java.io.Writer writer = m_writer;
        writer.write("<!ENTITY ");
        writer.write(name);
        writer.write(" \"");
        writer.write(value);
        writer.write("\">");
        writer.write(m_lineSep, 0, m_lineSepLen);
    }

    /**
     * Output a system-dependent line break.
     *
     * @throws org.xml.sax.SAXException
     */
    protected final void outputLineSep() throws IOException
    {

        m_writer.write(m_lineSep, 0, m_lineSepLen);
    }

    /**
     * Specifies an output format for this serializer. It the
     * serializer has already been associated with an output format,
     * it will switch to the new format. This method should not be
     * called while the serializer is in the process of serializing
     * a document.
     *
     * @param format The output format to use
     */
    public void setOutputFormat(Properties format)
    {

        boolean shouldFlush = m_shouldFlush;

        init(m_writer, format, false, false);

        m_shouldFlush = shouldFlush;
    }

    /**
     * Initialize the serializer with the specified writer and output format.
     * Must be called before calling any of the serialize methods.
     * This method can be called multiple times and the xsl:output properties
     * passed in the 'format' parameter are accumulated across calls.
     *
     * @param writer The writer to use
     * @param format The output format
     * @param shouldFlush True if the writer should be flushed at EndDocument.
     */
    private synchronized void init(
        Writer writer,
        Properties format,
        boolean defaultProperties,
        boolean shouldFlush)
    {

        m_shouldFlush = shouldFlush;


        // if we are tracing events we need to trace what
        // characters are written to the output writer.
        if (m_tracer != null
         && !(writer instanceof SerializerTraceWriter)  )
            m_writer = new SerializerTraceWriter(writer, m_tracer);
        else
            m_writer = writer;


        m_format = format;
        //        m_cdataSectionNames =
        //            OutputProperties.getQNameProperties(
        //                OutputKeys.CDATA_SECTION_ELEMENTS,
        //                format);
        setCdataSectionElements(OutputKeys.CDATA_SECTION_ELEMENTS, format);

        setIndentAmount(
            OutputPropertyUtils.getIntProperty(
                OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
                format));
        setIndent(
            OutputPropertyUtils.getBooleanProperty(OutputKeys.INDENT, format));

        {
            String sep =
                    format.getProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR);
            if (sep != null) {
                m_lineSep = sep.toCharArray();
                m_lineSepLen = sep.length();
            }
        }

        boolean shouldNotWriteXMLHeader =
            OutputPropertyUtils.getBooleanProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                format);
        setOmitXMLDeclaration(shouldNotWriteXMLHeader);
        setDoctypeSystem(format.getProperty(OutputKeys.DOCTYPE_SYSTEM));
        String doctypePublic = format.getProperty(OutputKeys.DOCTYPE_PUBLIC);
        setDoctypePublic(doctypePublic);

        // if standalone was explicitly specified
        if (format.get(OutputKeys.STANDALONE) != null)
        {
            String val = format.getProperty(OutputKeys.STANDALONE);
            if (defaultProperties)
                setStandaloneInternal(val);
            else
                setStandalone(val);
        }

        setMediaType(format.getProperty(OutputKeys.MEDIA_TYPE));

        if (null != doctypePublic)
        {
            if (doctypePublic.startsWith("-//W3C//DTD XHTML"))
                m_spaceBeforeClose = true;
        }

        /*
         * This code is added for XML 1.1 Version output.
         */
        String version = getVersion();
        if (null == version)
        {
            version = format.getProperty(OutputKeys.VERSION);
            setVersion(version);
        }

        // initCharsMap();
        String encoding = getEncoding();
        if (null == encoding)
        {
            encoding =
                Encodings.getMimeEncoding(
                    format.getProperty(OutputKeys.ENCODING));
            setEncoding(encoding);
        }

        m_isUTF8 = encoding.equals(Encodings.DEFAULT_MIME_ENCODING);

        // Access this only from the Hashtable level... we don't want to
        // get default properties.
        String entitiesFileName =
            (String) format.get(OutputPropertiesFactory.S_KEY_ENTITIES);

        if (null != entitiesFileName)
        {

            String method =
                (String) format.get(OutputKeys.METHOD);

            m_charInfo = CharInfo.getCharInfo(entitiesFileName, method);
        }

    }

    /**
     * Initialize the serializer with the specified writer and output format.
     * Must be called before calling any of the serialize methods.
     *
     * @param writer The writer to use
     * @param format The output format
     */
    private synchronized void init(Writer writer, Properties format)
    {
        init(writer, format, false, false);
    }
    /**
     * Initialize the serializer with the specified output stream and output
     * format. Must be called before calling any of the serialize methods.
     *
     * @param output The output stream to use
     * @param format The output format
     * @param defaultProperties true if the properties are the default
     * properties
     *
     * @throws UnsupportedEncodingException The encoding specified   in the
     * output format is not supported
     */
    protected synchronized void init(
        OutputStream output,
        Properties format,
        boolean defaultProperties)
        throws UnsupportedEncodingException
    {

        String encoding = getEncoding();
        if (encoding == null)
        {
            // if not already set then get it from the properties
            encoding =
                Encodings.getMimeEncoding(
                    format.getProperty(OutputKeys.ENCODING));
            setEncoding(encoding);
        }

        if (encoding.equalsIgnoreCase("UTF-8"))
        {
            m_isUTF8 = true;
            //            if (output instanceof java.io.BufferedOutputStream)
            //            {
            //                init(new WriterToUTF8(output), format, defaultProperties, true);
            //            }
            //            else if (output instanceof java.io.FileOutputStream)
            //            {
            //                init(new WriterToUTF8Buffered(output), format, defaultProperties, true);
            //            }
            //            else
            //            {
            //                // Not sure what to do in this case.  I'm going to be conservative
            //                // and not buffer.
            //                init(new WriterToUTF8(output), format, defaultProperties, true);
            //            }


                init(
                    new WriterToUTF8Buffered(output),
                    format,
                    defaultProperties,
                    true);


        }
        else if (
            encoding.equals("WINDOWS-1250")
                || encoding.equals("US-ASCII")
                || encoding.equals("ASCII"))
        {
            init(new WriterToASCI(output), format, defaultProperties, true);
        }
        else
        {
            Writer osw;

            try
            {
                osw = Encodings.getWriter(output, encoding);
            }
            catch (UnsupportedEncodingException uee)
            {
                System.out.println(
                    "Warning: encoding \""
                        + encoding
                        + "\" not supported"
                        + ", using "
                        + Encodings.DEFAULT_MIME_ENCODING);

                encoding = Encodings.DEFAULT_MIME_ENCODING;
                setEncoding(encoding);
                osw = Encodings.getWriter(output, encoding);
            }

            init(osw, format, defaultProperties, true);
        }

    }

    /**
     * Returns the output format for this serializer.
     *
     * @return The output format in use
     */
    public Properties getOutputFormat()
    {
        return m_format;
    }

    /**
     * Specifies a writer to which the document should be serialized.
     * This method should not be called while the serializer is in
     * the process of serializing a document.
     *
     * @param writer The output writer stream
     */
    public void setWriter(Writer writer)
    {
        // if we are tracing events we need to trace what
        // characters are written to the output writer.
        if (m_tracer != null
         && !(writer instanceof SerializerTraceWriter)  )
            m_writer = new SerializerTraceWriter(writer, m_tracer);
        else
            m_writer = writer;
    }

    /**
     * Set if the operating systems end-of-line line separator should
     * be used when serializing.  If set false NL character
     * (decimal 10) is left alone, otherwise the new-line will be replaced on
     * output with the systems line separator. For example on UNIX this is
     * NL, while on Windows it is two characters, CR NL, where CR is the
     * carriage-return (decimal 13).
     *
     * @param use_sytem_line_break True if an input NL is replaced with the
     * operating systems end-of-line separator.
     * @return The previously set value of the serializer.
     */
    public boolean setLineSepUse(boolean use_sytem_line_break)
    {
        boolean oldValue = m_lineSepUse;
        m_lineSepUse = use_sytem_line_break;
        return oldValue;
    }

    /**
     * Specifies an output stream to which the document should be
     * serialized. This method should not be called while the
     * serializer is in the process of serializing a document.
     * <p>
     * The encoding specified in the output properties is used, or
     * if no encoding was specified, the default for the selected
     * output method.
     *
     * @param output The output stream
     */
    public void setOutputStream(OutputStream output)
    {

        try
        {
            Properties format;
            if (null == m_format)
                format =
                    OutputPropertiesFactory.getDefaultMethodProperties(
                        Method.XML);
            else
                format = m_format;
            init(output, format, true);
        }
        catch (UnsupportedEncodingException uee)
        {

            // Should have been warned in init, I guess...
        }
    }

    /**
     * @see SerializationHandler#setEscaping(boolean)
     */
    public boolean setEscaping(boolean escape)
    {
        final boolean temp = m_escaping;
        m_escaping = escape;
        return temp;

    }


    /**
     * Might print a newline character and the indentation amount
     * of the given depth.
     *
     * @param depth the indentation depth (element nesting depth)
     *
     * @throws org.xml.sax.SAXException if an error occurs during writing.
     */
    protected void indent(int depth) throws IOException
    {

        if (m_startNewLine)
            outputLineSep();
        /* For m_indentAmount > 0 this extra test might be slower
         * but Xalan's default value is 0, so this extra test
         * will run faster in that situation.
         */
        if (m_indentAmount > 0)
            printSpace(depth * m_indentAmount);

    }

    /**
     * Indent at the current element nesting depth.
     * @throws IOException
     */
    protected void indent() throws IOException
    {
        indent(m_elemContext.m_currentElemDepth);
    }
    /**
     * Prints <var>n</var> spaces.
     * @param n         Number of spaces to print.
     *
     * @throws org.xml.sax.SAXException if an error occurs when writing.
     */
    private void printSpace(int n) throws IOException
    {
        final java.io.Writer writer = m_writer;
        for (int i = 0; i < n; i++)
        {
            writer.write(' ');
        }

    }

    /**
     * Report an attribute type declaration.
     *
     * <p>Only the effective (first) declaration for an attribute will
     * be reported.  The type will be one of the strings "CDATA",
     * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
     * "ENTITIES", or "NOTATION", or a parenthesized token group with
     * the separator "|" and all whitespace removed.</p>
     *
     * @param eName The name of the associated element.
     * @param aName The name of the attribute.
     * @param type A string representing the attribute type.
     * @param valueDefault A string representing the attribute default
     *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
     *        none of these applies.
     * @param value A string representing the attribute's default value,
     *        or null if there is none.
     * @exception SAXException The application may raise an exception.
     */
    public void attributeDecl(
        String eName,
        String aName,
        String type,
        String valueDefault,
        String value)
        throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            final java.io.Writer writer = m_writer;
            DTDprolog();

            writer.write("<!ATTLIST ");
            writer.write(eName);
            writer.write(' ');

            writer.write(aName);
            writer.write(' ');
            writer.write(type);
            if (valueDefault != null)
            {
                writer.write(' ');
                writer.write(valueDefault);
            }

            //writer.write(" ");
            //writer.write(value);
            writer.write('>');
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Get the character stream where the events will be serialized to.
     *
     * @return Reference to the result Writer, or null.
     */
    public Writer getWriter()
    {
        return m_writer;
    }

    /**
     * Report a parsed external entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param publicId The declared public identifier of the entity, or
     *        null if none was declared.
     * @param systemId The declared system identifier of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #internalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void externalEntityDecl(
        String name,
        String publicId,
        String systemId)
        throws SAXException
    {
        try {
            DTDprolog();

            m_writer.write("<!ENTITY ");
            m_writer.write(name);
            if (publicId != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(publicId);

            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(systemId);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Tell if this character can be written without escaping.
     */
    protected boolean escapingNotNeeded(char ch)
    {
        final boolean ret;
        if (ch < 127)
        {
            // This is the old/fast code here, but is this
            // correct for all encodings?
            if (ch >= 0x20 || (0x0A == ch || 0x0D == ch || 0x09 == ch))
                ret= true;
            else
                ret = false;
        }
        else {
            ret = m_encodingInfo.isInEncoding(ch);
        }
        return ret;
    }

    /**
     * Once a surrogate has been detected, write out the pair of
     * characters if it is in the encoding, or if there is no
     * encoding, otherwise write out an entity reference
     * of the value of the unicode code point of the character
     * represented by the high/low surrogate pair.
     * <p>
     * An exception is thrown if there is no low surrogate in the pair,
     * because the array ends unexpectely, or if the low char is there
     * but its value is such that it is not a low surrogate.
     *
     * @param c the first (high) part of the surrogate, which
     * must be confirmed before calling this method.
     * @param ch Character array.
     * @param i position Where the surrogate was detected.
     * @param end The end index of the significant characters.
     * @return 0 if the pair of characters was written out as-is,
     * the unicode code point of the character represented by
     * the surrogate pair if an entity reference with that value
     * was written out.
     *
     * @throws IOException
     * @throws org.xml.sax.SAXException if invalid UTF-16 surrogate detected.
     */
    protected int writeUTF16Surrogate(char c, char ch[], int i, int end)
        throws IOException
    {
        int codePoint = 0;
        if (i + 1 >= end)
        {
            throw new IOException(
                Utils.messages.createMessage(
                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                    new Object[] { Integer.toHexString((int) c)}));
        }

        final char high = c;
        final char low = ch[i+1];
        if (!Encodings.isLowUTF16Surrogate(low)) {
            throw new IOException(
                Utils.messages.createMessage(
                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                    new Object[] {
                        Integer.toHexString((int) c)
                            + " "
                            + Integer.toHexString(low)}));
        }

        final java.io.Writer writer = m_writer;

        // If we make it to here we have a valid high, low surrogate pair
        if (m_encodingInfo.isInEncoding(c,low)) {
            // If the character formed by the surrogate pair
            // is in the encoding, so just write it out
            writer.write(ch,i,2);
        }
        else {
            // Don't know what to do with this char, it is
            // not in the encoding and not a high char in
            // a surrogate pair, so write out as an entity ref
            final String encoding = getEncoding();
            if (encoding != null) {
                /* The output encoding is known,
                 * so somthing is wrong.
                  */
                codePoint = Encodings.toCodePoint(high, low);
                // not in the encoding, so write out a character reference
                writer.write('&');
                writer.write('#');
                writer.write(Integer.toString(codePoint));
                writer.write(';');
            } else {
                /* The output encoding is not known,
                 * so just write it out as-is.
                 */
                writer.write(ch, i, 2);
            }
        }
        // non-zero only if character reference was written out.
        return codePoint;
    }

    /**
     * Handle one of the default entities, return false if it
     * is not a default entity.
     *
     * @param ch character to be escaped.
     * @param i index into character array.
     * @param chars non-null reference to character array.
     * @param len length of chars.
     * @param fromTextNode true if the characters being processed
     * are from a text node, false if they are from an attribute value
     * @param escLF true if the linefeed should be escaped.
     *
     * @return i+1 if the character was written, else i.
     *
     * @throws java.io.IOException
     */
    protected int accumDefaultEntity(
        java.io.Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean fromTextNode,
        boolean escLF)
        throws IOException
    {

        if (!escLF && CharInfo.S_LINEFEED == ch)
        {
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            // if this is text node character and a special one of those,
            // or if this is a character from attribute value and a special one of those
            if ((fromTextNode && m_charInfo.isSpecialTextChar(ch)) || (!fromTextNode && m_charInfo.isSpecialAttrChar(ch)))
            {
                String outputStringForChar = m_charInfo.getOutputStringForChar(ch);

                if (null != outputStringForChar)
                {
                    writer.write(outputStringForChar);
                }
                else
                    return i;
            }
            else
                return i;
        }

        return i + 1;

    }
    /**
     * Normalize the characters, but don't escape.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @param isCData true if a CDATA block should be built around the characters.
     * @param useSystemLineSeparator true if the operating systems
     * end-of-line separator should be output rather than a new-line character.
     *
     * @throws IOException
     * @throws org.xml.sax.SAXException
     */
    void writeNormalizedChars(
        char ch[],
        int start,
        int length,
        boolean isCData,
        boolean useSystemLineSeparator)
        throws IOException, org.xml.sax.SAXException
    {
        final java.io.Writer writer = m_writer;
        int end = start + length;

        for (int i = start; i < end; i++)
        {
            char c = ch[i];

            if (CharInfo.S_LINEFEED == c && useSystemLineSeparator)
            {
                writer.write(m_lineSep, 0, m_lineSepLen);
            }
            else if (isCData && (!escapingNotNeeded(c)))
            {
                //                if (i != 0)
                if (m_cdataTagOpen)
                    closeCDATA();

                // This needs to go into a function...
                if (Encodings.isHighUTF16Surrogate(c))
                {
                    writeUTF16Surrogate(c, ch, i, end);
                    i++ ; // process two input characters
                }
                else
                {
                    writer.write("&#");

                    String intStr = Integer.toString((int) c);

                    writer.write(intStr);
                    writer.write(';');
                }

                //                if ((i != 0) && (i < (end - 1)))
                //                if (!m_cdataTagOpen && (i < (end - 1)))
                //                {
                //                    writer.write(CDATA_DELIMITER_OPEN);
                //                    m_cdataTagOpen = true;
                //                }
            }
            else if (
                isCData
                    && ((i < (end - 2))
                        && (']' == c)
                        && (']' == ch[i + 1])
                        && ('>' == ch[i + 2])))
            {
                writer.write(CDATA_CONTINUE);

                i += 2;
            }
            else
            {
                if (escapingNotNeeded(c))
                {
                    if (isCData && !m_cdataTagOpen)
                    {
                        writer.write(CDATA_DELIMITER_OPEN);
                        m_cdataTagOpen = true;
                    }
                    writer.write(c);
                }

                // This needs to go into a function...
                else if (Encodings.isHighUTF16Surrogate(c))
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    writeUTF16Surrogate(c, ch, i, end);
                    i++; // process two input characters
                }
                else
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    writer.write("&#");

                    String intStr = Integer.toString((int) c);

                    writer.write(intStr);
                    writer.write(';');
                }
            }
        }

    }

    /**
     * Ends an un-escaping section.
     *
     * @see #startNonEscaping
     *
     * @throws org.xml.sax.SAXException
     */
    public void endNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.pop();
    }

    /**
     * Starts an un-escaping section. All characters printed within an un-
     * escaping section are printed as is, without escaping special characters
     * into entity references. Only XML and HTML serializers need to support
     * this method.
     * <p> The contents of the un-escaping section will be delivered through the
     * regular <tt>characters</tt> event.
     *
     * @throws org.xml.sax.SAXException
     */
    public void startNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.push(true);
    }

    /**
     * Receive notification of cdata.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     */
    protected void cdata(char ch[], int start, final int length)
        throws org.xml.sax.SAXException
    {

        try
        {
            final int old_start = start;
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }
            m_ispreserve = true;

            if (shouldIndent())
                indent();

            boolean writeCDataBrackets =
                (((length >= 1) && escapingNotNeeded(ch[start])));

            /* Write out the CDATA opening delimiter only if
             * we are supposed to, and if we are not already in
             * the middle of a CDATA section
             */
            if (writeCDataBrackets && !m_cdataTagOpen)
            {
                m_writer.write(CDATA_DELIMITER_OPEN);
                m_cdataTagOpen = true;
            }

            // writer.write(ch, start, length);
            if (isEscapingDisabled())
            {
                charactersRaw(ch, start, length);
            }
            else
                writeNormalizedChars(ch, start, length, true, m_lineSepUse);

            /* used to always write out CDATA closing delimiter here,
             * but now we delay, so that we can merge CDATA sections on output.
             * need to write closing delimiter later
             */
            if (writeCDataBrackets)
            {
                /* if the CDATA section ends with ] don't leave it open
                 * as there is a chance that an adjacent CDATA sections
                 * starts with ]>.
                 * We don't want to merge ]] with > , or ] with ]>
                 */
                if (ch[start + length - 1] == ']')
                    closeCDATA();
            }

            // time to fire off CDATA event
            if (m_tracer != null)
                super.fireCDATAEvent(ch, old_start, length);
        }
        catch (IOException ioe)
        {
            throw new org.xml.sax.SAXException(
                Utils.messages.createMessage(
                    MsgKey.ER_OIERROR,
                    null),
                ioe);
            //"IO error", ioe);
        }
    }

    /**
     * Tell if the character escaping should be disabled for the current state.
     *
     * @return true if the character escaping should be disabled.
     */
    private boolean isEscapingDisabled()
    {
        return m_disableOutputEscapingStates.peekOrFalse();
    }

    /**
     * If available, when the disable-output-escaping attribute is used,
     * output raw text without escaping.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     *
     * @throws org.xml.sax.SAXException
     */
    protected void charactersRaw(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        if (m_inEntityRef)
            return;
        try
        {
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }

            m_ispreserve = true;

            m_writer.write(ch, start, length);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Receive notification of character data.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters(final char chars[], final int start, final int length)
        throws org.xml.sax.SAXException
    {
        // It does not make sense to continue with rest of the method if the number of
        // characters to read from array is 0.
        // Section 7.6.1 of XSLT 1.0 (http://www.w3.org/TR/xslt#value-of) suggest no text node
        // is created if string is empty.
        if (length == 0 || (m_inEntityRef && !m_expandDTDEntities))
            return;
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }

        if (m_cdataStartCalled || m_elemContext.m_isCdataSection)
        {
            /* either due to startCDATA() being called or due to
             * cdata-section-elements atribute, we need this as cdata
             */
            cdata(chars, start, length);

            return;
        }

        if (m_cdataTagOpen)
            closeCDATA();
        // the check with _escaping is a bit of a hack for XLSTC

        if (m_disableOutputEscapingStates.peekOrFalse() || (!m_escaping))
        {
            charactersRaw(chars, start, length);

            // time to fire off characters generation event
            if (m_tracer != null)
                super.fireCharEvent(chars, start, length);

            return;
        }

        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }


        try
        {
            int i;
            char ch1;
            int startClean;

            // skip any leading whitspace
            // don't go off the end and use a hand inlined version
            // of isWhitespace(ch)
            final int end = start + length;
            int lastDirty = start - 1; // last character that needed processing
            for (i = start;
                ((i < end)
                    && ((ch1 = chars[i]) == 0x20
                        || (ch1 == 0xA && m_lineSepUse)
                        || ch1 == 0xD
                        || ch1 == 0x09));
                i++)
            {
                /*
                 * We are processing leading whitespace, but are doing the same
                 * processing for dirty characters here as for non-whitespace.
                 *
                 */
                if (!m_charInfo.isTextASCIIClean(ch1))
                {
                    lastDirty = processDirty(chars,end, i,ch1, lastDirty, true);
                    i = lastDirty;
                }
            }
            /* If there is some non-whitespace, mark that we may need
             * to preserve this. This is only important if we have indentation on.
             */
            if (i < end)
                m_ispreserve = true;


//            int lengthClean;    // number of clean characters in a row
//            final boolean[] isAsciiClean = m_charInfo.getASCIIClean();

            final boolean isXML10 = XMLVERSION10.equals(getVersion());
            // we've skipped the leading whitespace, now deal with the rest
            for (; i < end; i++)
            {
                {
                    // A tight loop to skip over common clean chars
                    // This tight loop makes it easier for the JIT
                    // to optimize.
                    char ch2;
                    while (i<end
                            && ((ch2 = chars[i])<127)
                            && m_charInfo.isTextASCIIClean(ch2))
                            i++;
                    if (i == end)
                        break;
                }

                final char ch = chars[i];
                /*  The check for isCharacterInC0orC1Ranger and
                 *  isNELorLSEPCharacter has been added
                 *  to support Control Characters in XML 1.1
                 */
                if (!isCharacterInC0orC1Range(ch) &&
                    (isXML10 || !isNELorLSEPCharacter(ch)) &&
                    (escapingNotNeeded(ch) && (!m_charInfo.isSpecialTextChar(ch)))
                        || ('"' == ch))
                {
                    ; // a character needing no special processing
                }
                else
                {
                    lastDirty = processDirty(chars,end, i, ch, lastDirty, true);
                    i = lastDirty;
                }
            }

            // we've reached the end. Any clean characters at the
            // end of the array than need to be written out?
            startClean = lastDirty + 1;
            if (i > startClean)
            {
                int lengthClean = i - startClean;
                m_writer.write(chars, startClean, lengthClean);
            }

            // For indentation purposes, mark that we've just writen text out
            m_isprevtext = true;
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        // time to fire off characters generation event
        if (m_tracer != null)
            super.fireCharEvent(chars, start, length);
    }
    /**
     * This method checks if a given character is between C0 or C1 range
     * of Control characters.
     * This method is added to support Control Characters for XML 1.1
     * If a given character is TAB (0x09), LF (0x0A) or CR (0x0D), this method
     * return false. Since they are whitespace characters, no special processing is needed.
     *
     * @param ch
     * @return boolean
     */
    private static boolean isCharacterInC0orC1Range(char ch)
    {
        if(ch == 0x09 || ch == 0x0A || ch == 0x0D)
                return false;
        else
                return (ch >= 0x7F && ch <= 0x9F)|| (ch >= 0x01 && ch <= 0x1F);
    }
    /**
     * This method checks if a given character either NEL (0x85) or LSEP (0x2028)
     * These are new end of line charcters added in XML 1.1.  These characters must be
     * written as Numeric Character References (NCR) in XML 1.1 output document.
     *
     * @param ch
     * @return boolean
     */
    private static boolean isNELorLSEPCharacter(char ch)
    {
        return (ch == 0x85 || ch == 0x2028);
    }
    /**
     * Process a dirty character and any preeceding clean characters
     * that were not yet processed.
     * @param chars array of characters being processed
     * @param end one (1) beyond the last character
     * in chars to be processed
     * @param i the index of the dirty character
     * @param ch the character in chars[i]
     * @param lastDirty the last dirty character previous to i
     * @param fromTextNode true if the characters being processed are
     * from a text node, false if they are from an attribute value.
     * @return the index of the last character processed
     */
    private int processDirty(
        char[] chars,
        int end,
        int i,
        char ch,
        int lastDirty,
        boolean fromTextNode) throws IOException
    {
        int startClean = lastDirty + 1;
        // if we have some clean characters accumulated
        // process them before the dirty one.
        if (i > startClean)
        {
            int lengthClean = i - startClean;
            m_writer.write(chars, startClean, lengthClean);
        }

        // process the "dirty" character
        if (CharInfo.S_LINEFEED == ch && fromTextNode)
        {
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            startClean =
                accumDefaultEscape(
                    m_writer,
                    (char)ch,
                    i,
                    chars,
                    end,
                    fromTextNode,
                    false);
            i = startClean - 1;
        }
        // Return the index of the last character that we just processed
        // which is a dirty character.
        return i;
    }

    /**
     * Receive notification of character data.
     *
     * @param s The string of characters to process.
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters(String s) throws org.xml.sax.SAXException
    {
        if (m_inEntityRef && !m_expandDTDEntities)
            return;
        final int length = s.length();
        if (length > m_charsBuff.length)
        {
            m_charsBuff = new char[length * 2 + 1];
        }
        s.getChars(0, length, m_charsBuff, 0);
        characters(m_charsBuff, 0, length);
    }

    /**
     * Escape and writer.write a character.
     *
     * @param ch character to be escaped.
     * @param i index into character array.
     * @param chars non-null reference to character array.
     * @param len length of chars.
     * @param fromTextNode true if the characters being processed are
     * from a text node, false if the characters being processed are from
     * an attribute value.
     * @param escLF true if the linefeed should be escaped.
     *
     * @return i+1 if a character was written, i+2 if two characters
     * were written out, else return i.
     *
     * @throws org.xml.sax.SAXException
     */
    protected int accumDefaultEscape(
        Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean fromTextNode,
        boolean escLF)
        throws IOException
    {

        int pos = accumDefaultEntity(writer, ch, i, chars, len, fromTextNode, escLF);

        if (i == pos)
        {
            if (Encodings.isHighUTF16Surrogate(ch))
            {

                // Should be the UTF-16 low surrogate of the hig/low pair.
                char next;
                // Unicode code point formed from the high/low pair.
                int codePoint = 0;

                if (i + 1 >= len)
                {
                    throw new IOException(
                        Utils.messages.createMessage(
                            MsgKey.ER_INVALID_UTF16_SURROGATE,
                            new Object[] { Integer.toHexString(ch)}));
                    //"Invalid UTF-16 surrogate detected: "

                    //+Integer.toHexString(ch)+ " ?");
                }
                else
                {
                    next = chars[++i];

                    if (!(Encodings.isLowUTF16Surrogate(next)))
                        throw new IOException(
                            Utils.messages.createMessage(
                                MsgKey
                                    .ER_INVALID_UTF16_SURROGATE,
                                new Object[] {
                                    Integer.toHexString(ch)
                                        + " "
                                        + Integer.toHexString(next)}));
                    //"Invalid UTF-16 surrogate detected: "

                    //+Integer.toHexString(ch)+" "+Integer.toHexString(next));
                    codePoint = Encodings.toCodePoint(ch,next);
                }

                writer.write("&#");
                writer.write(Integer.toString(codePoint));
                writer.write(';');
                pos += 2; // count the two characters that went into writing out this entity
            }
            else
            {
                /*  This if check is added to support control characters in XML 1.1.
                 *  If a character is a Control Character within C0 and C1 range, it is desirable
                 *  to write it out as Numeric Character Reference(NCR) regardless of XML Version
                 *  being used for output document.
                 */
                if (isCharacterInC0orC1Range(ch) ||
                        (XMLVERSION11.equals(getVersion()) && isNELorLSEPCharacter(ch)))
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else if ((!escapingNotNeeded(ch) ||
                    (  (fromTextNode && m_charInfo.isSpecialTextChar(ch))
                     || (!fromTextNode && m_charInfo.isSpecialAttrChar(ch))))
                && m_elemContext.m_currentElemDepth > 0)
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else
                {
                    writer.write(ch);
                }
                pos++;  // count the single character that was processed
            }

        }
        return pos;
    }

    /**
     * Receive notification of the beginning of an element, although this is a
     * SAX method additional namespace or attribute information can occur before
     * or after this call, that is associated with this element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The element type name.
     * @param atts The attributes attached to the element, if any.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     * @see org.xml.sax.ContentHandler#endElement
     * @see org.xml.sax.AttributeList
     *
     * @throws org.xml.sax.SAXException
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String name,
        Attributes atts)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;

        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }
        else if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            if ((true == m_needToOutputDocTypeDecl)
                && (null != getDoctypeSystem()))
            {
                outputDocTypeDecl(name, true);
            }

            m_needToOutputDocTypeDecl = false;

            /* before we over-write the current elementLocalName etc.
             * lets close out the old one (if we still need to)
             */
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }

            if (namespaceURI != null)
                ensurePrefixIsDeclared(namespaceURI, name);

            m_ispreserve = false;

            if (shouldIndent() && m_startNewLine)
            {
                indent();
            }

            m_startNewLine = true;

            final java.io.Writer writer = m_writer;
            writer.write('<');
            writer.write(name);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        // process the attributes now, because after this SAX call they might be gone
        if (atts != null)
            addAttributes(atts);

        m_elemContext = m_elemContext.push(namespaceURI,localName,name);
        m_isprevtext = false;

        if (m_tracer != null){
            firePseudoAttributes();
        }

    }

    /**
      * Receive notification of the beginning of an element, additional
      * namespace or attribute information can occur before or after this call,
      * that is associated with this element.
      *
      *
      * @param elementNamespaceURI The Namespace URI, or the empty string if the
      *        element has no Namespace URI or if Namespace
      *        processing is not being performed.
      * @param elementLocalName The local name (without prefix), or the
      *        empty string if Namespace processing is not being
      *        performed.
      * @param elementName The element type name.
      * @throws org.xml.sax.SAXException Any SAX exception, possibly
      *            wrapping another exception.
      * @see org.xml.sax.ContentHandler#startElement
      * @see org.xml.sax.ContentHandler#endElement
      * @see org.xml.sax.AttributeList
      *
      * @throws org.xml.sax.SAXException
      */
    public void startElement(
        String elementNamespaceURI,
        String elementLocalName,
        String elementName)
        throws SAXException
    {
        startElement(elementNamespaceURI, elementLocalName, elementName, null);
    }

    public void startElement(String elementName) throws SAXException
    {
        startElement(null, null, elementName, null);
    }

    /**
     * Output the doc type declaration.
     *
     * @param name non-null reference to document type name.
     * NEEDSDOC @param closeDecl
     *
     * @throws java.io.IOException
     */
    void outputDocTypeDecl(String name, boolean closeDecl) throws SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            final java.io.Writer writer = m_writer;
            writer.write("<!DOCTYPE ");
            writer.write(name);

            String doctypePublic = getDoctypePublic();
            if (null != doctypePublic)
            {
                writer.write(" PUBLIC \"");
                writer.write(doctypePublic);
                writer.write('\"');
            }

            String doctypeSystem = getDoctypeSystem();
            if (null != doctypeSystem)
            {
                if (null == doctypePublic)
                    writer.write(" SYSTEM \"");
                else
                    writer.write(" \"");

                writer.write(doctypeSystem);

                if (closeDecl)
                {
                    writer.write("\">");
                    writer.write(m_lineSep, 0, m_lineSepLen);
                    closeDecl = false; // done closing
                }
                else
                    writer.write('\"');
            }
            boolean dothis = false;
            if (dothis)
            {
                // at one point this code seemed right,
                // but not anymore - Brian M.
                if (closeDecl)
                {
                    writer.write('>');
                    writer.write(m_lineSep, 0, m_lineSepLen);
                }
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Process the attributes, which means to write out the currently
     * collected attributes to the writer. The attributes are not
     * cleared by this method
     *
     * @param writer the writer to write processed attributes to.
     * @param nAttrs the number of attributes in m_attributes
     * to be processed
     *
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void processAttributes(java.io.Writer writer, int nAttrs) throws IOException, SAXException
    {
            /* real SAX attributes are not passed in, so process the
             * attributes that were collected after the startElement call.
             * _attribVector is a "cheap" list for Stream serializer output
             * accumulated over a series of calls to attribute(name,value)
             */
            String encoding = getEncoding();
            for (int i = 0; i < nAttrs; i++)
            {
                // elementAt is JDK 1.1.8
                final String name = m_attributes.getQName(i);
                final String value = m_attributes.getValue(i);
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                writeAttrString(writer, value, encoding);
                writer.write('\"');
            }
    }

    /**
     * Returns the specified <var>string</var> after substituting <VAR>specials</VAR>,
     * and UTF-16 surrogates for chracter references <CODE>&amp;#xnn</CODE>.
     *
     * @param   string      String to convert to XML format.
     * @param   encoding    CURRENTLY NOT IMPLEMENTED.
     *
     * @throws java.io.IOException
     */
    public void writeAttrString(
        Writer writer,
        String string,
        String encoding)
        throws IOException
    {
        final int len = string.length();
        if (len > m_attrBuff.length)
        {
           m_attrBuff = new char[len*2 + 1];
        }
        string.getChars(0,len, m_attrBuff, 0);
        final char[] stringChars = m_attrBuff;

        for (int i = 0; i < len; )
        {
            char ch = stringChars[i];
            if (escapingNotNeeded(ch) && (!m_charInfo.isSpecialAttrChar(ch)))
            {
                writer.write(ch);
                i++;
            }
            else
            { // I guess the parser doesn't normalize cr/lf in attributes. -sb
//                if ((CharInfo.S_CARRIAGERETURN == ch)
//                    && ((i + 1) < len)
//                    && (CharInfo.S_LINEFEED == stringChars[i + 1]))
//                {
//                    i++;
//                    ch = CharInfo.S_LINEFEED;
//                }

                i = accumDefaultEscape(writer, ch, i, stringChars, len, false, true);
            }
        }

    }

    /**
     * Receive notification of the end of an element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     *
     * @throws org.xml.sax.SAXException
     */
    public void endElement(String namespaceURI, String localName, String name)
        throws org.xml.sax.SAXException
    {

        if (m_inEntityRef)
            return;

        // namespaces declared at the current depth are no longer valid
        // so get rid of them
        m_prefixMap.popNamespaces(m_elemContext.m_currentElemDepth, null);

        try
        {
            final java.io.Writer writer = m_writer;
            if (m_elemContext.m_startTagOpen)
            {
                if (m_tracer != null)
                    super.fireStartElem(m_elemContext.m_elementName);
       
