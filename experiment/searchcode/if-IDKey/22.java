package fi.tkk.tml.xformsdb.dao.xmldocument;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fi.tkk.tml.xformsdb.core.Constants;
import fi.tkk.tml.xformsdb.dao.DAODataSource;
import fi.tkk.tml.xformsdb.dao.XFormsDBDAO;
import fi.tkk.tml.xformsdb.error.AuthenticationException;
import fi.tkk.tml.xformsdb.error.DAOException;
import fi.tkk.tml.xformsdb.error.ErrorConstants;
import fi.tkk.tml.xformsdb.error.FileException;
import fi.tkk.tml.xformsdb.error.XMLException;
import fi.tkk.tml.xformsdb.util.FileUtils;
import fi.tkk.tml.xformsdb.util.IOUtils;
import fi.tkk.tml.xformsdb.util.StringUtils;
import fi.tkk.tml.xformsdb.util.XMLUtils;
import fi.tkk.tml.xformsdb.xml.dom.CollectionHandler;


/**
 * XMLDocumentXFormsDBDAO implementation of the XFormsDBDAO
 * interface. This class can contain all XML document specific
 * code.
 * 
 *
 * @author Markku Laine
 * @version 1.0	 Created on February 23, 2010
 */
public final class XMLDocumentXFormsDBDAO implements XFormsDBDAO {
	

	
	// PRIVATE STATIC FINAL VARIABLES
	private static final Logger logger	= Logger.getLogger( XMLDocumentXFormsDBDAO.class );

	
	// PRIVATE VARIABLES
	private DAODataSource daoDataSource;

	
	// PRIVATE METHODS
	private String fixCollection( String collection ) {
		// Fix collection
		if ( collection != null ) {
			// Trim collection
			collection									= collection.trim();
			
			// Remove slashes from the end of collection
			int lastIndexOfSlash						= 0;
			boolean removeSlashesFromEnd				= true;
			while ( removeSlashesFromEnd == true ) {
				lastIndexOfSlash						= collection.lastIndexOf( "/" );
				if ( lastIndexOfSlash != -1 && collection.length() == ( lastIndexOfSlash + 1 ) ) {
					collection							= collection.substring( 0, lastIndexOfSlash );
				}
				else {
					removeSlashesFromEnd				= false;
				}
			}
		}

		
		return collection;
	}

	
	private String fixDoc( String doc ) {
		// Fix doc
		if ( doc != null ) {
			// Trim doc
			doc											= doc.trim();
	
			// Remove slashes from the beginning of doc
			int firstIndexOfSlash						= 0;
			boolean removeSlashesFromBeginning			= true;
			while ( removeSlashesFromBeginning == true ) {
				firstIndexOfSlash						= doc.indexOf( "/" );
				if ( firstIndexOfSlash == 0 ) {
					doc									= doc.substring( ( firstIndexOfSlash + 1 ) );
				}
				else {
					removeSlashesFromBeginning			= false;
				}
			}
		}

		
		return doc;
	}
	
	
	// PUBLIC CONSTRUCTORS
	/**
	 * Construct the XMLDocumentXFormsDBDAO.
	 * 
	 * 
	 * @param daoDataSource		DAO data source that enables the retrieval of the collection.
	 */
	public XMLDocumentXFormsDBDAO( DAODataSource daoDataSource ) {
		logger.log( Level.DEBUG, "Constructor has been called." );
		this.daoDataSource					= daoDataSource;
	}
	
	
	// PUBLIC METHODS
	/**
	 * Execute the select query expression and get the result in XML format.
	 * 
	 * 
	 * @param document			The XML document (<xformsdb:query> element).
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The result of the query in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String select( Document document, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		String collectionString									= null;
		String docString										= null;
		XQConnection xqc										= null;
		XQStaticContext xqsc									= null;
		List<String> collection									= null;
		CollectionHandler ch									= null;
		Element xformsdbQueryElement							= null;
		Element xformsdbExpressionElement						= null;
		Elements xformsdbXmlnsElements							= null;
		Elements xformsdbVarElements							= null;
		Elements xformsdbSecvarElements							= null;
		Element xformsdbXmlnsElement							= null;
		Element xformsdbVarElement								= null;
		Element xformsdbSecvarElement							= null;
		String xqe												= null;
		XQPreparedExpression xqpe								= null;
		XQResultSequence xqrs									= null;
		String resultItem										= null;
		String xmlString										= null;
		String xmlStringFiltered								= null;

		
		try {
			// Retrieve the needed elements
			xformsdbQueryElement								= document.getRootElement();
			xformsdbExpressionElement							= xformsdbQueryElement.getFirstChildElement( "expression", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbXmlnsElements								= xformsdbQueryElement.getChildElements( "xmlns", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElements									= xformsdbQueryElement.getChildElements( "var", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbSecvarElements								= xformsdbQueryElement.getChildElements( "secvar", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbXmlnsElement								= null;
			xformsdbVarElement									= null;
			xformsdbSecvarElement								= null;
			String xqueryStr									= null;
			String inlineXQueryStr								= null;

			// Fix collection and doc
			docString											= this.fixDoc( ( ( xformsdbQueryElement.getAttribute( "doc" ) == null ) ? null : xformsdbQueryElement.getAttributeValue( "doc" ) ) );
			collectionString									= this.fixCollection( this.daoDataSource.getCollection() );

			// Set or load the properties of the XQDataSource object
			Properties xqdsp									= new Properties();
			xqdsp.setProperty( "ClassName", "net.sf.saxon.xqj.SaxonXQDataSource" );
			
			// Create an XQDataSource instance using reflection
			String xqdscn										= xqdsp.getProperty( "ClassName" );
			Class xqdsc											= Class.forName( xqdscn );
			XQDataSource xqds									= ( XQDataSource ) xqdsc.newInstance();			

			// Remove the ClassName property
			//	The XQJ implementation will not recognize it and raise an error
			xqdsp.remove( "ClassName" );
			
			// Set the remaining properties
			xqds.setProperties( xqdsp );
			// -- or --
			// Create a new SaxonDataSource object (~factory) 
			//XQDataSource xqds									= new SaxonXQDataSource();

			
			// Establish a connection to the XQuery engine (~session)
			xqc													= xqds.getConnection();

			// Get a static context object with the implementation's defaults
			xqsc												= xqc.getStaticContext();

			// Make sure boundary-space policy is preserve
			xqsc.setBoundarySpacePolicy( XQConstants.BOUNDARY_SPACE_PRESERVE );

			// Make the changes effective
			xqc.setStaticContext( xqsc );
			
			
			// Handle the collection XML file: Retrieve documents' file names from the collection
			ch													= new CollectionHandler();
			ch.handle( new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + Constants.XFORMSDB_CONFIG_DEFAULT_DATA_SOURCE_COLLECTION_FILE_NAME ) );
			collection											= ch.getCollection();
			
			
			// Get the XPath object for compiling the xpath string
			//XPathFactory xpathFactory							= XPathFactory.newInstance();
			//XPath xpath										= xpathFactory.newXPath();
			String xpathStr										= null;


			// Check the query expression type
			if ( Constants.EXPRESSION_TYPE_SELECT4UPDATE.equals( xformsdbQueryElement.getAttributeValue( "type" ) ) == true ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_20, ErrorConstants.ERROR_MESSAGE_DAO_20 );
			}

			// Retrieve the XPath & XQuery strings, prefer resource over inline
			// The queryStr variable is just for testing the existence of the query as well as for optimistic printing of the query using XFormsDB encoding as there is no way to read/parse the encoding of the query itself
			if ( xformsdbExpressionElement.getAttribute( "resource" ) != null ) {
				try {
					xqueryStr									= IOUtils.convert( FileUtils.getFileAsInputStream( xformsdbExpressionElement.getAttributeValue( "resource" ) ), encoding, true );
				} catch ( FileException fex ) {
					throw new FileException( ErrorConstants.ERROR_CODE_DAO_25, ErrorConstants.ERROR_MESSAGE_DAO_25_1 + xformsdbQueryElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_25_2 );
				}
				
				if ( xqueryStr != null ) {
					xqueryStr									= xqueryStr.trim();
				}
				xpathStr										= xqueryStr;
			}
			else if ( xformsdbExpressionElement.getAttribute( "resource" ) == null && xformsdbExpressionElement.getChildCount() > 0 ) {
				inlineXQueryStr									= "";
				for ( int i = 0; i < xformsdbExpressionElement.getChildCount(); i++ ) {
					inlineXQueryStr								+= xformsdbExpressionElement.getChild( i ).toXML();
				}
				xqueryStr										= inlineXQueryStr.trim();
				xpathStr										= xqueryStr; 
			}
			else {
				// select4update
				if ( Constants.EXPRESSION_TYPE_SELECT4UPDATE.equals( xformsdbQueryElement.getAttributeValue( "type" ) ) == true ) {
					throw new DAOException( ErrorConstants.ERROR_CODE_DAO_21, ErrorConstants.ERROR_MESSAGE_DAO_21 );
				}
				// select
				else {
					throw new DAOException( ErrorConstants.ERROR_CODE_DAO_22, ErrorConstants.ERROR_MESSAGE_DAO_22 );
				}
			}

			// Check the XPath & XQuery expression strings
			if ( xpathStr == null && Constants.EXPRESSION_TYPE_SELECT4UPDATE.equals( xformsdbQueryElement.getAttributeValue( "type" ) ) == true ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_23, ErrorConstants.ERROR_MESSAGE_DAO_23 );
			}
			else if ( xqueryStr == null && Constants.EXPRESSION_TYPE_SELECT.equals( xformsdbQueryElement.getAttributeValue( "type" ) ) == true ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_24, ErrorConstants.ERROR_MESSAGE_DAO_24 );
			}


			// select4update
			if ( Constants.EXPRESSION_TYPE_SELECT4UPDATE.equals( xformsdbQueryElement.getAttributeValue( "type" ) ) == true ) {
				// Check/compile the XPath string, i.e. make sure the update query string uses XPath, not XQuery
				//xpath.compile( xpathStr );
				// Add XQuery version
				xqe												= "xquery version \"" + Constants.OUTPUT_XQUERY_VERSION_1_0 + "\" encoding \"" + encoding + "\";" + Constants.LINE_SEPARATOR + Constants.LINE_SEPARATOR;

				// Declare namespaces
				for ( int i = 0; i < xformsdbXmlnsElements.size(); i++ ) {
					xformsdbXmlnsElement						= xformsdbXmlnsElements.get( i );
					xqe											+= "declare namespace " + xformsdbXmlnsElement.getAttributeValue( "prefix" ) + "=\"" + xformsdbXmlnsElement.getAttributeValue( "uri" ) + "\";" + Constants.LINE_SEPARATOR;
				}

				// Declare external (normal) variables
				for ( int i = 0; i < xformsdbVarElements.size(); i++ ) {
					xformsdbVarElement							= xformsdbVarElements.get( i );
					xqe											+= "declare variable $" + xformsdbVarElement.getAttributeValue( "name" ) + " external;" + Constants.LINE_SEPARATOR;
				}
				// Declare external (security) variables
				for ( int i = 0; i < xformsdbSecvarElements.size(); i++ ) {
					xformsdbSecvarElement						= xformsdbSecvarElements.get( i );
					xqe											+= "declare variable $" + xformsdbSecvarElement.getAttributeValue( "name" ) + " external;" + Constants.LINE_SEPARATOR;
				}
				// Add extra empty lines
				xqe												+= Constants.LINE_SEPARATOR + Constants.LINE_SEPARATOR;
				xqe												+= xpathStr;
			}
			// select
			else {
				xqe												= xqueryStr;
			}
			// The XQuery expression to be executed
			logger.log( Level.DEBUG, "XQuery:" + Constants.LINE_SEPARATOR + xqe );

			// Set or load the serialization properties
			Properties sp										= new Properties();
			sp.setProperty( OutputKeys.OMIT_XML_DECLARATION, Constants.OUTPUT_XML_OMIT_XML_DECLARATION_NO );
			sp.setProperty( OutputKeys.METHOD, Constants.OUTPUT_XML_METHOD_XML );
			sp.setProperty( OutputKeys.VERSION, Constants.OUTPUT_XML_VERSION_1_0 );
			sp.setProperty( OutputKeys.ENCODING, encoding );
			sp.setProperty( OutputKeys.INDENT, Constants.OUTPUT_XML_INDENT_YES );
			//sp.setProperty( OutputKeys.STANDALONE, "no" );
			//sp.setProperty( OutputKeys.MEDIA_TYPE, "text/xml" );
			
			// Iterate through the collection
			for ( int i = 0; i < collection.size(); i++ ) {
				// Focus the XQuery on a collection instead of a single document if the single document has not been specified
				// -- or --
				// Focus the XQuery on a single document instead of a collection if the single document has been specified
				if ( docString == null || ( docString != null && docString.equals( this.fixDoc( collection.get( i ) ) ) == true ) ) {
					// Prepare the created XQuery expression for execution
					xqpe										= xqc.prepareExpression( xqe );
					// Bind a value to the given external variable or the context item
					xqpe.bindDocument( XQConstants.CONTEXT_ITEM, new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + this.fixDoc( collection.get( i ) ) ), null, null );
					// Bind values to the given external variables
					// Set the external (normal) variables
					Document xformsdbVarElementContentsDocument	= null;
					ByteArrayOutputStream outputStream			= null;
					Serializer serializer						= null;
					String xformsdbVarElementValue				= null;
					for ( int j = 0; j < xformsdbVarElements.size(); j++ ) {
						xformsdbVarElement						= xformsdbVarElements.get( j );
						xformsdbVarElementValue					= "";
						// Check the contents of the <xformsdb:var> element
						if ( xformsdbVarElement.getChildElements().size() == 0 ) {
							xformsdbVarElementValue				= xformsdbVarElement.getValue();
						}
						else {
							// Retrieve the contents of the <xformsdb:var> element
							xformsdbVarElementContentsDocument	= new Document( new Element( xformsdbVarElement.getChildElements().get( 0 ) ) );				

							// Serialize the document
							outputStream						= new ByteArrayOutputStream();
							serializer							= new Serializer( outputStream, encoding );
							serializer.write( xformsdbVarElementContentsDocument );			
							
							// Change the encoding
							xformsdbVarElementValue				= outputStream.toString( encoding );
						}
						// Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration or no type declaration (auto) in XQuery
						// For typed XPath variables: Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration in XQuery
						//xqpe.bindAtomicValue( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_INT ) );
						// -- or --
						// Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto), correct type declaration, or xs:string type declaration & correct conversation function in XQuery
						// For untyped XPath variables: Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto) in XQuery
						//xqpe.bindObject( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
						xqpe.bindObject( new QName( xformsdbVarElement.getAttributeValue( "name" ) ), xformsdbVarElementValue, xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
						logger.log( Level.DEBUG, "External (normal) variable: " + xformsdbVarElement.getAttributeValue( "name" ) + "=" + xformsdbVarElementValue );
					}
					// Set the external (security) variables
					for ( int j = 0; j < xformsdbSecvarElements.size(); j++ ) {
						xformsdbSecvarElement					= xformsdbSecvarElements.get( j );
						// Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration or no type declaration (auto) in XQuery
						// For typed XPath variables: Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration in XQuery
						//xqpe.bindAtomicValue( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_INT ) );
						// -- or --
						// Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto), correct type declaration, or xs:string type declaration & correct conversation function in XQuery
						// For untyped XPath variables: Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto) in XQuery
						//xqpe.bindObject( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
						xqpe.bindObject( new QName( xformsdbSecvarElement.getAttributeValue( "name" ) ), xformsdbSecvarElement.getValue(), xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
						logger.log( Level.DEBUG, "External (security) variable: " + xformsdbSecvarElement.getAttributeValue( "name" ) + "=" + xformsdbSecvarElement.getValue() );
					}
					
					// Execute the prepared query expression
					xqrs										= xqpe.executeQuery();
					// Iterate through a sequence
		            while ( xqrs.next() ) {
		            	// Serialize the current item according to the serialization properties
		                resultItem								= xqrs.getItemAsString( sp );
		            	if ( resultItem != null ) {
		            		if ( xmlString == null ) {
		            			xmlString						= resultItem + Constants.LINE_SEPARATOR;
		            		}
		            		else {
		            			xmlString						+= resultItem + Constants.LINE_SEPARATOR;
		            		}
		            	}
		            }
		            // Remove the new line character after the last document
		            if ( ( i + 1 ) == collection.size() ) {
		            	if ( xmlString != null ) {
		            		xmlString							= xmlString.substring( 0, ( xmlString.length() - 1 ) );
		            	}
		            }
				}
			}
            
	        // Ensure that the result does not consist only of the XML declaration
			try {
				xmlStringFiltered								= XMLUtils.filterXMLDeclaration( xmlString );
			} catch ( XMLException xmlex ) {
				xmlStringFiltered								= null;
			} catch ( Exception ex ) {
				xmlStringFiltered								= null;
			}

			// Query did not result any data 
			if ( xmlString == null || xmlStringFiltered.length() == 0 ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_51, ErrorConstants.ERROR_MESSAGE_DAO_51 );
			}

			logger.log( Level.DEBUG, "Result of the XQuery:" + Constants.LINE_SEPARATOR + xmlString );			
		} catch ( DAOException daoex ) {
			throw daoex;
		} catch ( FileException fex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_50, ErrorConstants.ERROR_MESSAGE_DAO_50_1 + xformsdbQueryElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_50_2, fex );
		} catch ( Exception ex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_26, ErrorConstants.ERROR_MESSAGE_DAO_26_1 + xformsdbQueryElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_26_2, ex );
		} finally {
			try {
	            // Free all resources allocated for the result
				if ( xqrs != null ) {
					xqrs.close();
				}
				// Free all resources allocated for the expression
				if ( xqpe != null ) {
					xqpe.close();
				}
	            // Free all resources allocated for the connection
				if ( xqc != null ) {
					xqc.close();
				}
			} catch ( XQException xqex ) {
				logger.log( Level.ERROR, "Failed to close the objects.", xqex );
			}
		}

		
		return xmlString;
	}
	
	
	/**
	 * Execute the update query expression and get the result in XML format.
	 * 
	 * 
	 * @param document				The XML document (<xformsdb:query> element).
	 * @param tm					The merged XML document; 3DM result.
	 * @param encoding				The character encoding to be used.
	 * @return xmlString			The result of the query in XML format.
	 * @throws DAOException			DAO exception.
	 */
	public String update( Document document, String tm, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		throw new DAOException( ErrorConstants.ERROR_CODE_DAO_27, ErrorConstants.ERROR_MESSAGE_DAO_27 );
	}
	
	
	/**
	 * Execute all query expression and get the result in XML format.
	 * 
	 * 
	 * @param document			The XML document (<xformsdb:query> element).
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The result of the query in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String all( Document document, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		throw new DAOException( ErrorConstants.ERROR_CODE_DAO_53, ErrorConstants.ERROR_MESSAGE_DAO_53 );
	}

	
	/**
	 * Authenticate the user with a username and password against the realm.
	 * 
	 * 
	 * @param document			The XML document (<xformsdb:login> element).
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The information of the authenticated user in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String authenticateUser( Document document, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		String collectionString									= null;
		String docString										= null;
		XQConnection xqc										= null;
		XQStaticContext xqsc									= null;
		List<String> collection									= null;
		CollectionHandler ch									= null;
		Element xformsdbLoginElement							= null;
		Element xformsdbExpressionElement						= null;
		Elements xformsdbVarElements							= null;
		Element xformsdbVarElement								= null;
		String xqe												= null;
		XQPreparedExpression xqpe								= null;
		XQResultSequence xqrs									= null;
		String resultItem										= null;
		String xmlString										= null;
		String xmlStringFiltered								= null;

		
		try {
			// Retrieve the needed elements
			xformsdbLoginElement								= document.getRootElement();
			xformsdbExpressionElement							= xformsdbLoginElement.getFirstChildElement( "expression", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElements									= xformsdbLoginElement.getChildElements( "var", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElement									= null;
			String xqueryStr									= null;

			// Fix collection and doc
			docString											= this.fixDoc( ( ( xformsdbLoginElement.getAttribute( "doc" ) == null ) ? null : xformsdbLoginElement.getAttributeValue( "doc" ) ) );
			collectionString									= this.fixCollection( this.daoDataSource.getCollection() );

			// Set or load the properties of the XQDataSource object
			Properties xqdsp									= new Properties();
			xqdsp.setProperty( "ClassName", "net.sf.saxon.xqj.SaxonXQDataSource" );
			
			// Create an XQDataSource instance using reflection
			String xqdscn										= xqdsp.getProperty( "ClassName" );
			Class xqdsc											= Class.forName( xqdscn );
			XQDataSource xqds									= ( XQDataSource ) xqdsc.newInstance();			

			// Remove the ClassName property
			//	The XQJ implementation will not recognize it and raise an error
			xqdsp.remove( "ClassName" );
			
			// Set the remaining properties
			xqds.setProperties( xqdsp );
			// -- or --
			// Create a new SaxonDataSource object (~factory) 
			//XQDataSource xqds									= new SaxonXQDataSource();

			
			// Establish a connection to the XQuery engine (~session)
			xqc													= xqds.getConnection();

			// Get a static context object with the implementation's defaults
			xqsc												= xqc.getStaticContext();

			// Make sure boundary-space policy is preserve
			xqsc.setBoundarySpacePolicy( XQConstants.BOUNDARY_SPACE_PRESERVE );

			// Make the changes effective
			xqc.setStaticContext( xqsc );
			
			
			// Handle the collection XML file: Retrieve documents' file names from the collection
			ch													= new CollectionHandler();
			ch.handle( new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + Constants.XFORMSDB_CONFIG_DEFAULT_DATA_SOURCE_COLLECTION_FILE_NAME ) );
			collection											= ch.getCollection();
			
						
			// Retrieve the XQuery string
			// The queryStr variable is just for testing the existence of the query as well as for optimistic printing of the query using XFormsDB encoding as there is no way to read/parse the encoding of the query itself
			// Here, however, we know that the encoding of the query file is UTF-8
			if ( xformsdbExpressionElement.getAttribute( "resource" ) != null ) {
				try {
					xqueryStr									= IOUtils.convert( Thread.currentThread().getContextClassLoader().getResourceAsStream( xformsdbExpressionElement.getAttributeValue( "resource" ) ), Constants.XML_DEFAULT_ENCODING, true );
				} catch ( Exception fex ) {
					throw new FileException( ErrorConstants.ERROR_CODE_DAO_42, ErrorConstants.ERROR_MESSAGE_DAO_42 );
				}
			}
			else {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_37, ErrorConstants.ERROR_MESSAGE_DAO_37 );
			}

			// Check the XQuery expression string
			if ( xqueryStr == null ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_38, ErrorConstants.ERROR_MESSAGE_DAO_38 );
			}


			xqe													= xqueryStr;
			// The XQuery expression to be executed
			logger.log( Level.DEBUG, "XQuery:" + Constants.LINE_SEPARATOR + xqe );
			
			// Set or load the serialization properties
			Properties sp										= new Properties();
			sp.setProperty( OutputKeys.OMIT_XML_DECLARATION, Constants.OUTPUT_XML_OMIT_XML_DECLARATION_NO );
			sp.setProperty( OutputKeys.METHOD, Constants.OUTPUT_XML_METHOD_XML );
			sp.setProperty( OutputKeys.VERSION, Constants.OUTPUT_XML_VERSION_1_0 );
			sp.setProperty( OutputKeys.ENCODING, encoding );
			sp.setProperty( OutputKeys.INDENT, Constants.OUTPUT_XML_INDENT_YES );
			//sp.setProperty( OutputKeys.STANDALONE, "no" );
			//sp.setProperty( OutputKeys.MEDIA_TYPE, "text/xml" );

			// Iterate through the collection
			for ( int i = 0; i < collection.size(); i++ ) {
				// Focus the XQuery on a collection instead of a single document if the single document has not been specified
				// -- or --
				// Focus the XQuery on a single document instead of a collection if the single document has been specified
				if ( docString == null || ( docString != null && docString.equals( this.fixDoc( collection.get( i ) ) ) == true ) ) {
					// Prepare the created XQuery expression for execution
					xqpe										= xqc.prepareExpression( xqe );
					// Bind a value to the given external variable or the context item
					xqpe.bindDocument( XQConstants.CONTEXT_ITEM, new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + this.fixDoc( collection.get( i ) ) ), null, null );
					// Bind values to the given external variables
					// Set the external (normal) variables
					Document xformsdbVarElementContentsDocument	= null;
					ByteArrayOutputStream outputStream			= null;
					Serializer serializer						= null;
					String xformsdbVarElementValue				= null;
					for ( int j = 0; j < xformsdbVarElements.size(); j++ ) {
						xformsdbVarElement						= xformsdbVarElements.get( j );
						xformsdbVarElementValue					= "";
						// Check the contents of the <xformsdb:var> element
						if ( xformsdbVarElement.getChildElements().size() == 0 ) {
							xformsdbVarElementValue				= xformsdbVarElement.getValue();
						}
						else {
							// Retrieve the contents of the <xformsdb:var> element
							xformsdbVarElementContentsDocument	= new Document( new Element( xformsdbVarElement.getChildElements().get( 0 ) ) );				

							// Serialize the document
							outputStream						= new ByteArrayOutputStream();
							serializer							= new Serializer( outputStream, encoding );
							serializer.write( xformsdbVarElementContentsDocument );			
							
							// Change the encoding
							xformsdbVarElementValue				= outputStream.toString( encoding );
						}
						// Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration or no type declaration (auto) in XQuery
						// For typed XPath variables: Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration in XQuery
						//xqpe.bindAtomicValue( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_INT ) );
						// -- or --
						// Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto), correct type declaration, or xs:string type declaration & correct conversation function in XQuery
						// For untyped XPath variables: Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto) in XQuery
						//xqpe.bindObject( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
						xqpe.bindObject( new QName( xformsdbVarElement.getAttributeValue( "name" ) ), xformsdbVarElementValue, xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
						logger.log( Level.DEBUG, "External (normal) variable: " + xformsdbVarElement.getAttributeValue( "name" ) + "=" + xformsdbVarElementValue );
					}
					
					// Execute the prepared query expression
					xqrs										= xqpe.executeQuery();
					// Iterate through a sequence
		            while ( xqrs.next() ) {
		            	// Serialize the current item according to the serialization properties
		                resultItem								= xqrs.getItemAsString( sp );
		            	if ( resultItem != null ) {
		            		if ( xmlString == null ) {
		            			xmlString						= resultItem + Constants.LINE_SEPARATOR;
		            		}
		            		else {
		            			xmlString						+= resultItem + Constants.LINE_SEPARATOR;
		            		}
		            	}
		            }
		            // Remove the new line character after the last document
		            if ( ( i + 1 ) == collection.size() ) {
		            	if ( xmlString != null ) {
		            		xmlString							= xmlString.substring( 0, ( xmlString.length() - 1 ) );
		            	}
		            }
				}
			}
            
	        // Ensure that the result does not consist only of the XML declaration
			try {
				xmlStringFiltered								= XMLUtils.filterXMLDeclaration( xmlString );
			} catch ( XMLException xmlex ) {
				xmlStringFiltered								= null;
			} catch ( Exception ex ) {
				xmlStringFiltered								= null;
			}
			
			// Incorrect username/password combination
			if ( xmlStringFiltered == null || xmlStringFiltered.length() == 0 ) {
				throw new AuthenticationException( ErrorConstants.ERROR_CODE_AUTHENTICATION_1, ErrorConstants.ERROR_MESSAGE_AUTHENTICATION_1 );
			}
			
			logger.log( Level.DEBUG, "Result of the XQuery:" + Constants.LINE_SEPARATOR + xmlString );
		} catch ( DAOException daoex ) {
			throw daoex;
		} catch ( FileException fex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_49, ErrorConstants.ERROR_MESSAGE_DAO_49_1 + xformsdbLoginElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_49_2, fex );
		} catch ( AuthenticationException aex ) {
			throw new DAOException( aex.getCode(), aex.getMessage(), aex );
		} catch ( Exception ex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_40, ErrorConstants.ERROR_MESSAGE_DAO_40_1 + xformsdbLoginElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_40_2, ex );
		} finally {
			try {
	            // Free all resources allocated for the result
				if ( xqrs != null ) {
					xqrs.close();
				}
				// Free all resources allocated for the expression
				if ( xqpe != null ) {
					xqpe.close();
				}
	            // Free all resources allocated for the connection
				if ( xqc != null ) {
					xqc.close();
				}
			} catch ( XQException xqex ) {
				logger.log( Level.ERROR, "Failed to close the objects.", xqex );
			}
		}
		
		
		return xmlString;
	}
	
	
	/**
	 * Select files from the files metadata data source.
	 * 
	 * 
	 * @param document			The XML document (<xformsdb:file> element).
	 * @param actionURL			The action URL, from which the request has been sent.
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The files list in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String selectFiles( Document document, String actionURL, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		String collectionString									= null;
		String docString										= null;
		XQConnection xqc										= null;
		XQStaticContext xqsc									= null;
		List<String> collection									= null;
		CollectionHandler ch									= null;
		Element xformsdbFileElement								= null;
		Element xformsdbExpressionElement						= null;
		Elements xformsdbVarElements							= null;
		Elements xformsdbSecvarElements							= null;
		Element xformsdbVarElement								= null;
		Element xformsdbSecvarElement							= null;
		boolean isIDsVariableSet								= false;
		boolean isUsernameVariableSet							= false;
		boolean isRolesVariableSet								= false;
		String xqe												= null;
		XQPreparedExpression xqpe								= null;
		XQResultSequence xqrs									= null;
		String resultItem										= null;
		String xmlString										= null;
		String xmlStringFiltered								= null;

		
		try {
			// Retrieve the needed elements
			xformsdbFileElement									= document.getRootElement();
			xformsdbExpressionElement							= xformsdbFileElement.getFirstChildElement( "expression", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElements									= xformsdbFileElement.getChildElements( "var", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbSecvarElements								= xformsdbFileElement.getChildElements( "secvar", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElement									= null;
			xformsdbSecvarElement								= null;
			String xqueryStr									= null;

			// Fix collection and doc
			docString											= this.fixDoc( ( ( xformsdbFileElement.getAttribute( "doc" ) == null ) ? null : xformsdbFileElement.getAttributeValue( "doc" ) ) );
			collectionString									= this.fixCollection( this.daoDataSource.getCollection() );

			// Set or load the properties of the XQDataSource object
			Properties xqdsp									= new Properties();
			xqdsp.setProperty( "ClassName", "net.sf.saxon.xqj.SaxonXQDataSource" );
			
			// Create an XQDataSource instance using reflection
			String xqdscn										= xqdsp.getProperty( "ClassName" );
			Class xqdsc											= Class.forName( xqdscn );
			XQDataSource xqds									= ( XQDataSource ) xqdsc.newInstance();			

			// Remove the ClassName property
			//	The XQJ implementation will not recognize it and raise an error
			xqdsp.remove( "ClassName" );
			
			// Set the remaining properties
			xqds.setProperties( xqdsp );
			// -- or --
			// Create a new SaxonDataSource object (~factory) 
			//XQDataSource xqds									= new SaxonXQDataSource();

			
			// Establish a connection to the XQuery engine (~session)
			xqc													= xqds.getConnection();

			// Get a static context object with the implementation's defaults
			xqsc												= xqc.getStaticContext();

			// Make sure boundary-space policy is preserve
			xqsc.setBoundarySpacePolicy( XQConstants.BOUNDARY_SPACE_PRESERVE );

			// Make the changes effective
			xqc.setStaticContext( xqsc );
			
			
			// Handle the collection XML file: Retrieve documents' file names from the collection
			ch													= new CollectionHandler();
			ch.handle( new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + Constants.XFORMSDB_CONFIG_DEFAULT_DATA_SOURCE_COLLECTION_FILE_NAME ) );
			collection											= ch.getCollection();

			
			// Retrieve the XQuery string
			// The queryStr variable is just for testing the existence of the query as well as for optimistic printing of the query using XFormsDB encoding as there is no way to read/parse the encoding of the query itself
			// Here, however, we know that the encoding of the query file is UTF-8
			if ( xformsdbExpressionElement.getAttribute( "resource" ) != null ) {
				try {
					xqueryStr									= IOUtils.convert( Thread.currentThread().getContextClassLoader().getResourceAsStream( xformsdbExpressionElement.getAttributeValue( "resource" ) ), Constants.XML_DEFAULT_ENCODING, true );
				} catch ( Exception fex ) {
					throw new FileException( ErrorConstants.ERROR_CODE_DAO_80, ErrorConstants.ERROR_MESSAGE_DAO_80 );
				}
			}
			else {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_81, ErrorConstants.ERROR_MESSAGE_DAO_81 );
			}

			// Check the XQuery expression string
			if ( xqueryStr == null ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_82, ErrorConstants.ERROR_MESSAGE_DAO_82 );
			}
			
	
			xqe													= xqueryStr;
			// The XQuery expression to be executed
			logger.log( Level.DEBUG, "XQuery:" + Constants.LINE_SEPARATOR + xqe );
			
			// Set or load the serialization properties
			Properties sp										= new Properties();
			sp.setProperty( OutputKeys.OMIT_XML_DECLARATION, Constants.OUTPUT_XML_OMIT_XML_DECLARATION_NO );
			sp.setProperty( OutputKeys.METHOD, Constants.OUTPUT_XML_METHOD_XML );
			sp.setProperty( OutputKeys.VERSION, Constants.OUTPUT_XML_VERSION_1_0 );
			sp.setProperty( OutputKeys.ENCODING, encoding );
			sp.setProperty( OutputKeys.INDENT, Constants.OUTPUT_XML_INDENT_YES );
			//sp.setProperty( OutputKeys.STANDALONE, "no" );
			//sp.setProperty( OutputKeys.MEDIA_TYPE, "text/xml" );

			// Iterate through the collection
			for ( int i = 0; i < collection.size(); i++ ) {
				// Focus the XQuery on a collection instead of a single document if the single document has not been specified
				// Prepare the created XQuery expression for execution
				xqpe											= xqc.prepareExpression( xqe );
				// Bind a value to the given external variable or the context item
				xqpe.bindDocument( XQConstants.CONTEXT_ITEM, new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + this.fixDoc( collection.get( i ) ) ), null, null );
				// Bind values to the given external variables
				// Set the external (normal) variables
				Document xformsdbVarElementContentsDocument		= null;
				ByteArrayOutputStream outputStream				= null;
				Serializer serializer							= null;
				String xformsdbVarElementValue					= null;
				for ( int j = 0; j < xformsdbVarElements.size(); j++ ) {
					xformsdbVarElement							= xformsdbVarElements.get( j );
					xformsdbVarElementValue						= "";
					// Check the contents of the <xformsdb:var> element
					if ( xformsdbVarElement.getChildElements().size() == 0 ) {
						xformsdbVarElementValue					= xformsdbVarElement.getValue();
					}
					else {
						// Retrieve the contents of the <xformsdb:var> element
						xformsdbVarElementContentsDocument		= new Document( new Element( xformsdbVarElement.getChildElements().get( 0 ) ) );				

						// Serialize the document
						outputStream							= new ByteArrayOutputStream();
						serializer								= new Serializer( outputStream, encoding );
						serializer.write( xformsdbVarElementContentsDocument );			
						
						// Change the encoding
						xformsdbVarElementValue					= outputStream.toString( encoding );
					}
					// Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration or no type declaration (auto) in XQuery
					// For typed XPath variables: Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration in XQuery
					//xqpe.bindAtomicValue( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_INT ) );
					// -- or --
					// Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto), correct type declaration, or xs:string type declaration & correct conversation function in XQuery
					// For untyped XPath variables: Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto) in XQuery
					//xqpe.bindObject( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					xqpe.bindObject( new QName( xformsdbVarElement.getAttributeValue( "name" ) ), xformsdbVarElementValue, xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (normal) variable: " + xformsdbVarElement.getAttributeValue( "name" ) + "=" + xformsdbVarElementValue );
					// Check the name of the variable
					if ( "username".equals( xformsdbVarElement.getAttributeValue( "name" ) ) == true ) {
						isUsernameVariableSet					= true;
					}
					else if ( "roles".equals( xformsdbVarElement.getAttributeValue( "name" ) ) == true ) {
						isRolesVariableSet						= true;
					}
					else if ( "ids".equals( xformsdbVarElement.getAttributeValue( "name" ) ) == true ) {
						isIDsVariableSet						= true;
					}
				}
				// Set the external (security) variables
				for ( int j = 0; j < xformsdbSecvarElements.size(); j++ ) {
					xformsdbSecvarElement						= xformsdbSecvarElements.get( j );
					// Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration or no type declaration (auto) in XQuery
					// For typed XPath variables: Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration in XQuery
					//xqpe.bindAtomicValue( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_INT ) );
					// -- or --
					// Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto), correct type declaration, or xs:string type declaration & correct conversation function in XQuery
					// For untyped XPath variables: Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto) in XQuery
					//xqpe.bindObject( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					xqpe.bindObject( new QName( xformsdbSecvarElement.getAttributeValue( "name" ) ), xformsdbSecvarElement.getValue(), xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (security) variable: " + xformsdbSecvarElement.getAttributeValue( "name" ) + "=" + xformsdbSecvarElement.getValue() );
					// Check the name of the variable
					if ( "username".equals( xformsdbSecvarElement.getAttributeValue( "name" ) ) == true ) {
						isUsernameVariableSet					= true;
					}
					else if ( "roles".equals( xformsdbSecvarElement.getAttributeValue( "name" ) ) == true ) {
						isRolesVariableSet						= true;
					}
				}
				// Set the external (extra) variables
				xqpe.bindObject( new QName( "action" ), actionURL, xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
				logger.log( Level.DEBUG, "External (extra) variable: action=" + actionURL );
				
				if ( isUsernameVariableSet == false && isRolesVariableSet == false && isIDsVariableSet == false ) {
					xqpe.bindObject( new QName( "type" ), "all", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (extra) variable: type=all" );
				}
				else if ( isUsernameVariableSet == true && isRolesVariableSet == false && isIDsVariableSet == false ) {
					xqpe.bindObject( new QName( "type" ), "username", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (extra) variable: type=username" );
				}
				else if ( isUsernameVariableSet == false && isRolesVariableSet == true && isIDsVariableSet == false ) {
					xqpe.bindObject( new QName( "type" ), "roles", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (extra) variable: type=roles" );
				}
				else if ( isUsernameVariableSet == false && isRolesVariableSet == false && isIDsVariableSet == true ) {
					xqpe.bindObject( new QName( "type" ), "ids", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (extra) variable: type=ids" );
				}
				else {
					throw new DAOException( ErrorConstants.ERROR_CODE_DAO_136, ErrorConstants.ERROR_MESSAGE_DAO_136 );
				}
				
				// Execute the prepared query expression
				xqrs											= xqpe.executeQuery();
				// Iterate through a sequence
	            while ( xqrs.next() ) {
	            	// Serialize the current item according to the serialization properties
	                resultItem									= xqrs.getItemAsString( sp );
	            	if ( resultItem != null ) {
	            		if ( xmlString == null ) {
	            			xmlString							= resultItem + Constants.LINE_SEPARATOR;
	            		}
	            		else {
	            			xmlString							+= resultItem + Constants.LINE_SEPARATOR;
	            		}
	            	}
	            }
	            // Remove the new line character after the last document
	            if ( ( i + 1 ) == collection.size() ) {
	            	if ( xmlString != null ) {
	            		xmlString								= xmlString.substring( 0, ( xmlString.length() - 1 ) );
	            	}
	            }
			}

	        // Ensure that the result does not consist only of the XML declaration
			try {
				xmlStringFiltered								= XMLUtils.filterXMLDeclaration( xmlString );
			} catch ( XMLException xmlex ) {
				xmlStringFiltered								= null;
			} catch ( Exception ex ) {
				xmlStringFiltered								= null;
			}
			
			// Select files query did not result any data
			if ( xmlString == null || xmlStringFiltered.length() == 0 ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_83, ErrorConstants.ERROR_MESSAGE_DAO_83 );
			}

			logger.log( Level.DEBUG, "Result of the XQuery:" + Constants.LINE_SEPARATOR + xmlString );
		} catch ( DAOException daoex ) {
			throw daoex;
		} catch ( FileException fex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_84, ErrorConstants.ERROR_MESSAGE_DAO_84_1 + xformsdbFileElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_84_2, fex );
		} catch ( Exception ex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_85, ErrorConstants.ERROR_MESSAGE_DAO_85_1 + xformsdbFileElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_85_2, ex );
		} finally {
			try {
	            // Free all resources allocated for the result
				if ( xqrs != null ) {
					xqrs.close();
				}
				// Free all resources allocated for the expression
				if ( xqpe != null ) {
					xqpe.close();
				}
	            // Free all resources allocated for the connection
				if ( xqc != null ) {
					xqc.close();
				}
			} catch ( XQException xqex ) {
				logger.log( Level.ERROR, "Failed to close the objects.", xqex );
			}
		}
		
		
		return xmlString;
	}
	
	
	/**
	 * Select file from the files metadata data source.
	 * 
	 * 
	 * @param document			The XML document (<xformsdb:file> element).
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The files list containing only one file in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String selectFile( Document document, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		String collectionString									= null;
		String docString										= null;
		XQConnection xqc										= null;
		XQStaticContext xqsc									= null;
		List<String> collection									= null;
		CollectionHandler ch									= null;
		Element xformsdbFileElement								= null;
		Element xformsdbExpressionElement						= null;
		Elements xformsdbVarElements							= null;
		Element xformsdbVarElement								= null;
		String xqe												= null;
		XQPreparedExpression xqpe								= null;
		XQResultSequence xqrs									= null;
		String resultItem										= null;
		String xmlString										= null;
		String xmlStringFiltered								= null;

		
		try {
			// Retrieve the needed elements
			xformsdbFileElement									= document.getRootElement();
			xformsdbExpressionElement							= xformsdbFileElement.getFirstChildElement( "expression", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElements									= xformsdbFileElement.getChildElements( "var", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVarElement									= null;
			String xqueryStr									= null;

			// Fix collection and doc
			docString											= this.fixDoc( ( ( xformsdbFileElement.getAttribute( "doc" ) == null ) ? null : xformsdbFileElement.getAttributeValue( "doc" ) ) );
			collectionString									= this.fixCollection( this.daoDataSource.getCollection() );

			// Set or load the properties of the XQDataSource object
			Properties xqdsp									= new Properties();
			xqdsp.setProperty( "ClassName", "net.sf.saxon.xqj.SaxonXQDataSource" );
			
			// Create an XQDataSource instance using reflection
			String xqdscn										= xqdsp.getProperty( "ClassName" );
			Class xqdsc											= Class.forName( xqdscn );
			XQDataSource xqds									= ( XQDataSource ) xqdsc.newInstance();			

			// Remove the ClassName property
			//	The XQJ implementation will not recognize it and raise an error
			xqdsp.remove( "ClassName" );
			
			// Set the remaining properties
			xqds.setProperties( xqdsp );
			// -- or --
			// Create a new SaxonDataSource object (~factory) 
			//XQDataSource xqds									= new SaxonXQDataSource();

			
			// Establish a connection to the XQuery engine (~session)
			xqc													= xqds.getConnection();

			// Get a static context object with the implementation's defaults
			xqsc												= xqc.getStaticContext();

			// Make sure boundary-space policy is preserve
			xqsc.setBoundarySpacePolicy( XQConstants.BOUNDARY_SPACE_PRESERVE );

			// Make the changes effective
			xqc.setStaticContext( xqsc );
			
			
			// Handle the collection XML file: Retrieve documents' file names from the collection
			ch													= new CollectionHandler();
			ch.handle( new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + Constants.XFORMSDB_CONFIG_DEFAULT_DATA_SOURCE_COLLECTION_FILE_NAME ) );
			collection											= ch.getCollection();

			
			// Retrieve the XQuery string
			// The queryStr variable is just for testing the existence of the query as well as for optimistic printing of the query using XFormsDB encoding as there is no way to read/parse the encoding of the query itself
			// Here, however, we know that the encoding of the query file is UTF-8
			if ( xformsdbExpressionElement.getAttribute( "resource" ) != null ) {
				try {
					xqueryStr									= IOUtils.convert( Thread.currentThread().getContextClassLoader().getResourceAsStream( xformsdbExpressionElement.getAttributeValue( "resource" ) ), Constants.XML_DEFAULT_ENCODING, true );
				} catch ( Exception fex ) {
					throw new FileException( ErrorConstants.ERROR_CODE_DAO_86, ErrorConstants.ERROR_MESSAGE_DAO_86 );
				}
			}
			else {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_87, ErrorConstants.ERROR_MESSAGE_DAO_87 );
			}

			// Check the XQuery expression string
			if ( xqueryStr == null ) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_88, ErrorConstants.ERROR_MESSAGE_DAO_88 );
			}
			
	
			xqe													= xqueryStr;
			// The XQuery expression to be executed
			logger.log( Level.DEBUG, "XQuery:" + Constants.LINE_SEPARATOR + xqe );
			
			// Set or load the serialization properties
			Properties sp										= new Properties();
			sp.setProperty( OutputKeys.OMIT_XML_DECLARATION, Constants.OUTPUT_XML_OMIT_XML_DECLARATION_NO );
			sp.setProperty( OutputKeys.METHOD, Constants.OUTPUT_XML_METHOD_XML );
			sp.setProperty( OutputKeys.VERSION, Constants.OUTPUT_XML_VERSION_1_0 );
			sp.setProperty( OutputKeys.ENCODING, encoding );
			sp.setProperty( OutputKeys.INDENT, Constants.OUTPUT_XML_INDENT_YES );
			//sp.setProperty( OutputKeys.STANDALONE, "no" );
			//sp.setProperty( OutputKeys.MEDIA_TYPE, "text/xml" );

			// Iterate through the collection
			for ( int i = 0; i < collection.size(); i++ ) {
				// Focus the XQuery on a collection instead of a single document if the single document has not been specified
				// Prepare the created XQuery expression for execution
				xqpe											= xqc.prepareExpression( xqe );
				// Bind a value to the given external variable or the context item
				xqpe.bindDocument( XQConstants.CONTEXT_ITEM, new FileInputStream( this.daoDataSource.getUri().substring( 0, ( this.daoDataSource.getUri().length() - 1 ) ) + collectionString + "/" + this.fixDoc( collection.get( i ) ) ), null, null );
				// Bind values to the given external variables
				// Set the external (normal) variables
				Document xformsdbVarElementContentsDocument		= null;
				ByteArrayOutputStream outputStream				= null;
				Serializer serializer							= null;
				String xformsdbVarElementValue					= null;
				for ( int j = 0; j < xformsdbVarElements.size(); j++ ) {
					xformsdbVarElement							= xformsdbVarElements.get( j );
					xformsdbVarElementValue						= "";
					// Check the contents of the <xformsdb:var> element
					if ( xformsdbVarElement.getChildElements().size() == 0 ) {
						xformsdbVarElementValue					= xformsdbVarElement.getValue();
					}
					else {
						// Retrieve the contents of the <xformsdb:var> element
						xformsdbVarElementContentsDocument		= new Document( new Element( xformsdbVarElement.getChildElements().get( 0 ) ) );				

						// Serialize the document
						outputStream							= new ByteArrayOutputStream();
						serializer								= new Serializer( outputStream, encoding );
						serializer.write( xformsdbVarElementContentsDocument );			
						
						// Change the encoding
						xformsdbVarElementValue					= outputStream.toString( encoding );
					}
					// Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration or no type declaration (auto) in XQuery
					// For typed XPath variables: Use bindAtomicValue( QName, String, correct XQItemType ) in Java -- and -- correct type declaration in XQuery
					//xqpe.bindAtomicValue( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_INT ) );
					// -- or --
					// Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto), correct type declaration, or xs:string type declaration & correct conversation function in XQuery
					// For untyped XPath variables: Use bindObject( QName, String, xs:untypedAtomic ) in Java -- and -- no type declaration (auto) in XQuery
					//xqpe.bindObject( new QName( "userOrderNumber" ), "3", xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					xqpe.bindObject( new QName( xformsdbVarElement.getAttributeValue( "name" ) ), xformsdbVarElementValue, xqc.createAtomicType( XQItemType.XQBASETYPE_UNTYPEDATOMIC ) );
					logger.log( Level.DEBUG, "External (normal) variable: " + xformsdbVarElement.getAttributeValue( "name" ) + "=" + xformsdbVarElementValue );
					// Check the name of the variable
				}
				
				// Execute the prepared query expression
				xqrs											= xqpe.executeQuery();
				// Iterate through a sequence
	            while ( xqrs.next() ) {
	            	// Serialize the current item according to the serialization properties
	                resultItem									= xqrs.getItemAsString( sp );
	            	if ( resultItem != null ) {
	            		if ( xmlString == null ) {
	            			xmlString							= resultItem + Constants.LINE_SEPARATOR;
	            		}
	            		else {
	            			xmlString							+= resultItem + Constants.LINE_SEPARATOR;
	            		}
	            	}
	            }
	            // Remove the new line character after the last document
	            if ( ( i + 1 ) == collection.size() ) {
	            	if ( xmlString != null ) {
	            		xmlString								= xmlString.substring( 0, ( xmlString.length() - 1 ) );
	            	}
	            }
			}

	        // Ensure that the result does not consist only of the XML declaration
			try {
				xmlStringFiltered								= XMLUtils.filterXMLDeclaration( xmlString );
			} catch ( XMLException xmlex ) {
				xmlStringFiltered								= null;
			} catch ( Exception ex ) {
				xmlStringFiltered								= null;
			}
			
			// Select file query did not result any data
			if ( xmlString == null || xmlStringFiltered.length() == 0 ) {
				try {
					// Wait for 2 seconds before returning the response in order to slow down malicious software for polling various download IDs
					Thread.sleep( 2000 );
				} catch ( InterruptedException iex ) {
				}
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_89, ErrorConstants.ERROR_MESSAGE_DAO_89 );
			}

			logger.log( Level.DEBUG, "Result of the XQuery:" + Constants.LINE_SEPARATOR + xmlString );
		} catch ( DAOException daoex ) {
			throw daoex;
		} catch ( FileException fex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_90, ErrorConstants.ERROR_MESSAGE_DAO_90_1 + xformsdbFileElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_90_2, fex );
		} catch ( Exception ex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_91, ErrorConstants.ERROR_MESSAGE_DAO_91_1 + xformsdbFileElement.getAttributeValue( "id" ) + ErrorConstants.ERROR_MESSAGE_DAO_91_2, ex );
		} finally {
			try {
	            // Free all resources allocated for the result
				if ( xqrs != null ) {
					xqrs.close();
				}
				// Free all resources allocated for the expression
				if ( xqpe != null ) {
					xqpe.close();
				}
	            // Free all resources allocated for the connection
				if ( xqc != null ) {
					xqc.close();
				}
			} catch ( XQException xqex ) {
				logger.log( Level.ERROR, "Failed to close the objects.", xqex );
			}
		}
		
		
		return xmlString;		
	}
	
	
	/**
	 * Insert files to the files metadata data source.
	 * 
	 * 
	 * @param document			The submitted XML document (<xformsdb:file> element (insert)).
	 * @param actionURL			The action URL, from which the request has been sent.
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The inserted files list in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String insertFiles( Document document, String actionURL, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		throw new DAOException( ErrorConstants.ERROR_CODE_DAO_93, ErrorConstants.ERROR_MESSAGE_DAO_93 );		
	}
	
	
	/**
	 * Delete files from the files metadata data source.
	 * 
	 * 
	 * @param document			The submitted XML document (<xformsdb:file> element (delete)).
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The deleted files list in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String deleteFiles( Document document, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		throw new DAOException( ErrorConstants.ERROR_CODE_DAO_102, ErrorConstants.ERROR_MESSAGE_DAO_102 );		
	}
	
	
	/**
	 * Update files to the files metadata data source.
	 * 
	 * 
	 * @param document			The submitted XML document (<xformsdb:file> element (update)).
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The updated files list in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String updateFiles( Document document, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		throw new DAOException( ErrorConstants.ERROR_CODE_DAO_111, ErrorConstants.ERROR_MESSAGE_DAO_111 );
	}
	
	
	/**
	 * Authenticate the widget user with an idkey and at against the widget realm.
	 * 
	 * 
	 * @param idkey				The idkey for authenticating the widget user.
	 * @param at				The at for authenticating the widget user.
	 * @param uriString			The URI string for connecting to the widget realm.
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The information of the authenticated widget user in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String authenticateWidgetUser( String idkey, String at, String uriString, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		HttpClient httpClient			= null;
		HttpGet httpGet					= null;
		HttpResponse httpResponse		= null;
		HttpEntity httpEntity			= null;
		StatusLine statusLine			= null;
		String xmlString				= null;
		String xmlStringFiltered		= null;

		
		try {
			// Update the URI string i.e. set the idkey and at parameter values
			uriString					= uriString.replace( "{idkey}", idkey );
			uriString					= uriString.replace( "{at}", at );

			// Create an instance of HttpClient
			httpClient					= new DefaultHttpClient();
		    
		    // Create an instance of HttpGet
		    httpGet						= new HttpGet( StringUtils.toSafeASCIIString( uriString ) ); // Important for encoding the URI correctly!
		    
		    // Execute the HTTP request
    		logger.log( Level.DEBUG, "Executing HTTP request: " + httpGet.getURI() );
	        httpResponse				= httpClient.execute( httpGet );

	        // Retrieve the HTTP response entity
    		httpEntity					= httpResponse.getEntity();

    		if ( httpEntity != null ) {
    			// Retrieve the HTTP response status line
    			statusLine				= httpResponse.getStatusLine();

    			if ( statusLine.getStatusCode() == HttpServletResponse.SC_OK ) {
    				// Retrieve the body of the response (content)
    				if ( EntityUtils.getContentCharSet( httpEntity ) != null ) {
        				// Decode using the response character encoding and transform to the given encoding format
    					// Work around for responses with non-matching encoding in the XML declaration and in the content charset
        				xmlString		= XMLUtils.getInputStreamAsString( httpEntity.getContent(), EntityUtils.getContentCharSet( httpEntity ), encoding );
    				}
    				else {
        				// Decode using the encoding in the XML declaration and transform to the given encoding format
        				xmlString		= XMLUtils.getInputStreamAsString( httpEntity.getContent(), encoding );    					
    				}
    			}    			
    		}
    		
	        // Ensure that the result does not consist only of the XML declaration
			try {
				xmlStringFiltered		= XMLUtils.filterXMLDeclaration( xmlString );
			} catch ( XMLException xmlex ) {
				xmlStringFiltered		= null;
			} catch ( Exception ex ) {
				xmlStringFiltered		= null;
			}

			// Incorrect idkey/at combination
			if ( xmlString == null || xmlStringFiltered.length() == 0 ) {
				throw new AuthenticationException( ErrorConstants.ERROR_CODE_AUTHENTICATION_3, ErrorConstants.ERROR_MESSAGE_AUTHENTICATION_3 );
			}

			logger.log( Level.DEBUG, "Result of the XQuery:" + Constants.LINE_SEPARATOR + xmlString );
		} catch ( ClientProtocolException cpex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_123, ErrorConstants.ERROR_MESSAGE_DAO_123, cpex );
		} catch ( IOException ioex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_123, ErrorConstants.ERROR_MESSAGE_DAO_123, ioex );			
		} catch ( AuthenticationException aex ) {
			throw new DAOException( aex.getCode(), aex.getMessage(), aex );
		} catch ( Exception ex ) {
			throw new DAOException( ErrorConstants.ERROR_CODE_DAO_120, ErrorConstants.ERROR_MESSAGE_DAO_120, ex );
		} finally {
			if ( httpClient != null ) {
		        // When HttpClient instance is no longer needed, 
		        // shut down the connection manager to ensure
		        // immediate deallocation of all system resources
		        httpClient.getConnectionManager().shutdown();			
			}
		}
		
		
		return xmlString;
	}
	
	
	/**
	 * Authenticate the widget data sources with an idkey and at against the widget data sources.
	 * 
	 * 
	 * @param idkey				The idkey for authenticating the widget data sources.
	 * @param at				The at for authenticating the widget data sources.
	 * @param uriString			The URI string for connecting to the widget data sources.
	 * @param encoding			The character encoding to be used.
	 * @return xmlString		The information of the authenticated widget user's data sources in XML format.
	 * @throws DAOException		DAO exception.
	 */
	public String authenticateWidgetDataSources( String idkey, String at, String uriString, String encoding ) throws DAOException {
		logger.log( Level.DEBUG, "Method has been called." );
		HttpClient httpClient			= null;
		HttpGet httpGet					= null;
		HttpResponse httpResponse		= null;
		HttpEntity httpEntity			= null;
		StatusLine statusLine			= null;
		String xmlString				= null;
		String xmlStringFiltered		= null;

		
		try {
			// Update the URI string i.e. set the idkey and at parameter values
			uriString					= uriString.repl
