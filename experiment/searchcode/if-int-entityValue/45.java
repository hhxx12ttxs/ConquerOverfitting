package fi.tkk.tml.xformsdb.util;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Convenience class for handling string escapes
 * according to both XML 1.0 and XML 1.1 specifications.
 * 
 * Note: The strings must always be in the UTF-16 charset.
 * 
 * 
 * @author Markku Laine
 * @version 1.0	 Created on October 14, 2009
 */
public class XMLEscapeUtils {

	
		
	// PRIVATE STATIC FINAL VARIABLES
	private static final Logger logger	= Logger.getLogger( XMLEscapeUtils.class );

	
	// PRIVATE CONSTRUCTORS
	/**
	 * Prevent object construction outside of this class.
	 */
	private XMLEscapeUtils() {
		super();
		logger.log( Level.DEBUG, "Constructor has been called." );
	}

	
	// PUBLIC STATIC METHODS
	/**
	 * Escape the given XML string (character text; unescaped) according to
	 * XML 1.0 specification.
	 * 
	 * 
	 * @param str			The given XML string (character text; unescaped).
	 * @return escapedStr	The escaped XML string.
	 */
	public static String escapeXml( String str ) {
		return XMLEscapeUtils.escapeXml( str, "1.0", true );
	}
	
	
	/**
	 * Escape the given XML string (character text or attribute value; unescaped)
	 * according to either XML 1.0 or XML 1.1 specification.
	 * 
	 * 
	 * @param str				The given XML string (character text or attribute value; unescaped).
	 * @param xmlVersion		The XML version (1.0 or 1.1).
	 * @param isAttributeValue	<code>true</code> if the given XML string is attribute value,
	 * 							otherwise <code>false</code>.
	 * @return escapedStr		The escaped XML string.
	 */
	public static String escapeXml( String str, String xmlVersion, boolean isAttributeValue ) {
		if ( str == null ) {
			return null;
		}
		else {
			StringWriter stringWriter	= new StringWriter( ( int ) ( str.length() + ( str.length() * 0.1 ) ) );
		
			try {
				XMLEscapeUtils.escape( stringWriter, str, xmlVersion, isAttributeValue );
			} catch ( Exception ex ) {
			}
			return stringWriter.toString();
		}
	}
	
	
	/**
	 * Unescape the given string.
	 * 
	 * 
	 * @param str				The given XML string.
	 * @return unescapedStr		The unescaped XML string.
	 */
	public static String unescapeXml( String str ) {
		if ( str == null ) {
			return null;
		}
		else {
			StringWriter stringWriter	= new StringWriter( ( int ) ( str.length() + ( str.length() * 0.1 ) ) );
		
			try {
				XMLEscapeUtils.unescape( stringWriter, str );
			} catch ( Exception ex ) {
			}
			return stringWriter.toString();
		}
	}
	
	
	// PRIVATE STATIC METHODS
	/**
	 * Escape the given XML string (character text or attribute value; unescaped)
	 * according to either XML 1.0 or XML 1.1 specification.
	 * 
	 * Only element content (excluding markup) and attributes (values) need to be escaped.
	 * Markup, comments, CDATA sections, and prosessing instructions need to be left unchanged.
	 * 
	 *
	 * @param writer			The writer where the escaped string is written to.
	 * @param str				The given XML string (character text or attribute value; unescaped).
	 * @param xmlVersion		The XML version (1.0 or 1.1).
	 * @param isAttributeValue	<code>true</code> if the given XML string is attribute value,
	 * 							otherwise <code>false</code>.
	 */
	private static void escape( Writer writer, String str, String xmlVersion, boolean isAttributeValue ) throws Exception {
		StringBuffer sb	= new StringBuffer( "" );

		// First, escape all characters except the > character
		for ( int i = 0; i < str.length(); i++ ) {
			char c	= str.charAt( i );
			
			switch ( c ) {
				// less-than:
				// Always escape.
				case '<': {
					sb.append( "&lt;" );
					break;
				}
				// ampersand:
				// Always escape.
				case '&': {
					sb.append( "&amp;" );
					break;
				}
				// CR (CARRIAGE RETURN):
				// Always escape.
				case '\r': {
					sb.append( "&#xD;" );
					break;
				}
				// LF (LINE FEED) or sometimes NL (NEW LINE): 
				// Escape in attribute values.
				case '\n': {
					if ( isAttributeValue == true ) {
						sb.append( "&#xA;" );
					}
					else {
						sb.append( "\n" );
					}
					break;
				}
				// TAB (TABULATOR):
				// Escape in attribute values.
				case '\t': {
					if ( isAttributeValue == true ) {
						sb.append( "&#x9;" );
					}
					else {
						sb.append( "\t" );
					}
					break;
				}
				// single-quote:
				// Escape in attribute values quoted with the same kind of quote.
				case '\'': {
					if ( isAttributeValue == true ) {
						sb.append( "&apos;" );
					}
					else {
						sb.append( "\'" );
					}
					break;
				}
				// double-quote:
				// Escape in attribute values quoted with the same kind of quote.
				case '\"': {
					if ( isAttributeValue == true ) {
						sb.append( "&quot;" );
					}
					else {
						sb.append( "\"" );
					}
					break;
				}
				// else, default print char
				default: {
					if ( ( "1.1".equals( xmlVersion ) == true ) && ( ( ( c >= 0x1 ) && ( c <= 0x1F ) ) || ( ( c >= 0x7F ) && ( c <= 0x9F ) ) || ( c == 0x2028 ) ) ) {
						sb.append( "&#x" );
						sb.append( Integer.toHexString( c ).toUpperCase() );
						sb.append( ";" );
					}
					else {
						sb.append( c );
					}        
				}
			}
		}
		
		// Finally, escape the > character if necessary
		// greater-than:
		// Escape in text if it immediately follows two close-square-brackets, as
		// that sequence is only allowed as the end of a CDATA marked section.
		if ( isAttributeValue == false ) {
			sb.replace( 0, sb.length(), sb.toString().replace( "]]>", "]]&gt;" ) );
		}
		writer.write( sb.toString() );
	}
	
	
	/**
	 * Unescape (and normalize) the given XML string.
	 * 
	 * Only element content (excluding markup) and attributes (values) need to be unescaped.
	 * Markup, comments, CDATA sections, and prosessing instructions need to be left unchanged.
	 * 
	 *
	 * @param writer			The writer where the escaped string is written to.
	 * @param str				The given XML string (character text or attribute value; unescaped).
	 */
	private static void unescape( Writer writer, String str ) throws Exception {
		// First, normalize end-of-line delimiters
		str				= str.replaceAll( "(?i)&#xD;&#xA;",		"&#xA;" ); // case-insensitive matching
		str				= str.replaceAll( "(?i)&#xD;&#x85;",	"&#xA;" ); // case-insensitive matching
		str				= str.replaceAll( "(?i)&#x85;",			"&#xA;" ); // case-insensitive matching
		str				= str.replaceAll( "(?i)&#x2028;",		"&#xA;" ); // case-insensitive matching
		str				= str.replaceAll( "(?i)&#xD;",			"&#xA;" ); // case-insensitive matching
		
        
		// Finally, unescape characters
		for ( int i = 0; i < str.length(); i++ ) {
			char c		= str.charAt( i );
			
			switch ( c ) {
				// ampersand:
				case '&':
					int nextIdx							= i + 1;
					int semiColonIdx					= str.indexOf( ';', nextIdx );
					// Found ampersand did not start an entity, no following semi-colon exists
					if ( semiColonIdx == -1 ) {
						// The the text looks like &...
						writer.write( c );
						continue;
					}
					int amphersandIdx					= str.indexOf( '&', nextIdx );
					// Found ampersand did not start an entity, no following semi-colon before next ampersand
					if ( amphersandIdx != -1 && amphersandIdx < semiColonIdx ) {
						// Then the text looks like &...&...;
						writer.write( c );
						continue;
					}
					
					String entityContent				= str.substring( nextIdx, semiColonIdx );
					int entityValue						= -1;
					if ( entityContent.length() > 0 ) {
						// Escaped value content is an integer (decimal or hexadecimal)
						if ( entityContent.charAt( 0 ) == '#' ) {
							if ( entityContent.length() > 1 ) {
								char isHexChar			= entityContent.charAt( 1 );
								
								try {
									switch ( isHexChar ) {
										// hexadecimal, 16 base
										case 'X':
										case 'x':
											entityValue = Integer.parseInt( entityContent.substring( 2 ), 16 );
											break;
										// decimal, 10 base
										default:
											entityValue = Integer.parseInt( entityContent.substring( 1 ), 10 );
									}
									if ( entityValue > 0xFFFF ) {
										entityValue		= -1;
									}
								} catch ( NumberFormatException nfex ) {
									entityValue			= -1;
								}
							}
						}
						// Escaped value content is an entity name
						else {
							// less-than:
							if ( "lt".equals( entityContent ) == true ) {
								entityValue = 60;
							}
							// ampersand:
							else if ( "amp".equals( entityContent ) == true ) {
								entityValue = 38;
							}
							// greater-than:
							else if ( "gt".equals( entityContent ) == true ) {
								entityValue = 62;
							}
							// single-quote:
							else if ( "apos".equals( entityContent ) == true ) {
								entityValue = 39;
							}
							// double-quote:
							else if ( "quot".equals( entityContent ) == true ) {
								entityValue = 34;
							}
						}
					}
					
					// Entity value was not found (not an XML entity value)
					if ( entityValue == -1 ) {
						writer.write( '&' );
						writer.write( entityContent );
						writer.write( ';' );
					}
					// Entity value was found
					else {
						writer.write( entityValue );
					}
					i	= semiColonIdx; // move index up to the semi-colon
					break;
				// else, default print char
				default:
					writer.write( c );
			}
		}
	}
}
