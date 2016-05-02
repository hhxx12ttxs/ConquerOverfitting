package fi.tkk.tml.xformsdb.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fi.tkk.tml.xformsdb.core.Constants;
import fi.tkk.tml.xformsdb.error.ErrorConstants;
import fi.tkk.tml.xformsdb.error.HandlerException;
import fi.tkk.tml.xformsdb.error.ManagerException;
import fi.tkk.tml.xformsdb.error.TransformerException;
import fi.tkk.tml.xformsdb.manager.xformsdb.XFormsDBUserManager;
import fi.tkk.tml.xformsdb.servlet.XFormsDBServlet;
import fi.tkk.tml.xformsdb.transformer.XFormsDBTransformer;
import fi.tkk.tml.xformsdb.util.IOUtils;
import fi.tkk.tml.xformsdb.util.StringUtils;
import fi.tkk.tml.xformsdb.xml.to.WebAppTO;
import fi.tkk.tml.xformsdb.xml.to.xformsdb.MimeMappingTO;
import fi.tkk.tml.xformsdb.xml.to.xformsdb.XFormsDBConfigTO;


/**
 * Handle GET requests having the
 * content type: <code>null</code>.
 * 
 *
 * @author Markku Laine
 * @version 1.0	 Created on February 23, 2010
 */
public class GetNullHandler extends ResponseHandler {
	
	
	
	// PRIVATE STATIC FINAL VARIABLES
	private static final Logger logger					= Logger.getLogger( GetNullHandler.class );

	
	// PRIVATE VARIABLES
	private XFormsDBFileSelectHandler xformsdbFileSelectHandler;
	private XFormsDBWidgetLoginHandler xformsdbWidgetLoginHandler;

		
	// PUBLIC CONSTURCTORS
	public GetNullHandler() {
		super();
		logger.log( Level.DEBUG, "Constructor has been called." );
		this.xformsdbFileSelectHandler					= new XFormsDBFileSelectHandler();
		this.xformsdbWidgetLoginHandler					= new XFormsDBWidgetLoginHandler();
	}

	
	// PRIVATE METHODS
	private String getDecodedRequestParameterValue( HttpServletRequest request, String parameterName, boolean parameterNameInJavaDefaultEncoding, String serverURIEncoding ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		String convertedParameterName					= null;
		String decodedParameterValue					= null;
		
		
		try {
			if ( parameterNameInJavaDefaultEncoding ) {
				// Decode the name of the request parameter
				convertedParameterName					= StringUtils.decode( StringUtils.encode( parameterName, Constants.CLIENT_URI_ENCODING_DEFAULT ), serverURIEncoding );
			}
			else {
				convertedParameterName					= parameterName;				
			}

			// Decode the value of the request parameter
			decodedParameterValue						= StringUtils.decode( StringUtils.encode( request.getParameter( convertedParameterName ), serverURIEncoding ), Constants.CLIENT_URI_ENCODING_DEFAULT );				
			logger.log( Level.DEBUG, "The decoded value of a request parameter has been successfully retrieved." );
		} catch ( NullPointerException npex ) {
			decodedParameterValue						= null;
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_10, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_10, ex  );
		}
		
		
		return decodedParameterValue;
	}

	
	private String getDecodedQueryString( HttpServletRequest request ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		String decodedQueryString	= "";
		
		
		try {
			// Create decoded query string
			if ( request.getQueryString() != null ) {
				decodedQueryString	= "?" + URLDecoder.decode( request.getQueryString(), Constants.CLIENT_URI_ENCODING_DEFAULT );
			}

			logger.log( Level.DEBUG, "The decoded query string has been successfully retrieved." );
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_24, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_24, ex  );
		}
		
		
		return decodedQueryString;
	}

		
	private Element createXFormsDBFileElement( XFormsDBServlet xformsdbServlet, String id ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		Element xformsdbFileElement					= null;
		
		
		try {
			String namespacePrefixXFormsDBWithColon	= Constants.NAMESPACE_PREFIX_XFORMSDB + ( ( "".equals( Constants.NAMESPACE_PREFIX_XFORMSDB ) == true ) ? "" : ":" );
	
			// Create the <xformsdb:file> element
			xformsdbFileElement						= new Element( namespacePrefixXFormsDBWithColon + "file", Constants.NAMESPACE_URI_XFORMSDB );
	
			// Create the <xformsdb:expression> element
			Element xformsdbExpression				= new Element( namespacePrefixXFormsDBWithColon + "expression", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbExpression.addAttribute( new Attribute( "resource", Constants.XQ_SELECT_FILE_FILE_PATH ) );
			xformsdbFileElement.appendChild( xformsdbExpression );
			
			// Create the <xformsdb:var> element
			Element xformsdbVar						= new Element( namespacePrefixXFormsDBWithColon + "var", Constants.NAMESPACE_URI_XFORMSDB );
			xformsdbVar.addAttribute( new Attribute( "name", "id" ) );
			xformsdbVar.appendChild( id );
			xformsdbFileElement.appendChild( xformsdbVar );
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_11, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_11, ex  );
		}

		
		return xformsdbFileElement;
	}

	
	private String handleXFormsDBFile( XFormsDBServlet xformsdbServlet, Document document ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		String xformsdbFile							= null;
		
		
		try {
			// Handle the <xformsdb:file>
			xformsdbFile							= this.xformsdbFileSelectHandler.handle( xformsdbServlet, document );
			logger.log( Level.DEBUG, "The <xformsdb:file> request has been successfully handled." );
		} catch ( HandlerException hex ) {
			throw hex;
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_16, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_16, ex  );
		}
		
		
		return xformsdbFile;
	}
	
	
	private Document buildXOM( String xmlString, String encoding ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		Builder builder		= null;
		Document document	= null;
		
		
		try {
			// Parse the XML document in order to build the XOM document object			
			builder			= new Builder();
			document		= builder.build( IOUtils.convert( xmlString, encoding ) );
			logger.log( Level.DEBUG, "The XOM document object has been successfully built." );
		} catch ( IOException ioex ) {
			ioex.printStackTrace();
		} catch ( ValidityException vex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_13, "Validity exception" + ", line " + vex.getLineNumber() + ", column " + vex.getColumnNumber() + ", uri " + vex.getURI() + ". " + vex.getMessage(), vex );
		} catch ( ParsingException pex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_14, "Parsing exception" + ", line " + pex.getLineNumber() + ", column " + pex.getColumnNumber() + ", uri " + pex.getURI() + ". " + pex.getMessage(), pex );
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_15, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_15, ex );
		}
		
		
		return document;
	}

	
	private String getRolesOfTheLoggedInXFormsDBUser( HttpSession session, String encoding ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		String xformsdbUser								= null;
		String rolesOfTheLoggedInXFormsDBUser			= null;
		
		
		try {
			// Retrieve the logged in user from XFormsDB
			xformsdbUser								= XFormsDBUserManager.getLoggedInXFormsDBUser( session );
			
			// Retrieve the roles of the logged in XFormsDB user
			if ( xformsdbUser != null ) {
				Builder builder							= new Builder();
				Document document						= builder.build( IOUtils.convert( xformsdbUser, encoding ) );
				rolesOfTheLoggedInXFormsDBUser			= document.getRootElement().getAttributeValue( "roles" );
			}
			
			
			logger.log( Level.DEBUG, "The roles of the logged in user have been successfully retrieved from XFormsDB." );
		} catch ( ManagerException mex ) {
			throw new HandlerException( mex.getCode(), mex.getMessage(), mex );
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_18, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_18, ex  );
		}
		
		
		return rolesOfTheLoggedInXFormsDBUser;
	}

	
	private boolean hasXFormsDBUserAccessToTheFile( String xformsdbUserRolesStr, String fileRolesStr ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		boolean hasXFormsDBUserAccessToTheFile					= false;
		
		
		try {
			// Tokenize XFormsDB user's roles and file' roles
			String[] xformsdbUserRoles							= xformsdbUserRolesStr.split( "\\s" );
			String[] fileRoles									= fileRolesStr.split( "\\s" );
			
			// Try to find a matching role
			// The file is public
			if ( fileRoles.length == 1 && "".equals( fileRoles[ 0 ] ) == true ) {
				hasXFormsDBUserAccessToTheFile					= true;
			}
			// The file is not public
			else {
				String fileRole									= null;
				String xformsdbUserRole							= null;
				// Iterate over the file's roles
				for ( int i = 0; i < fileRoles.length; i++ ) {
					fileRole									= fileRoles[ i ].trim();
					if ( "".equals( fileRole ) == false ) {
						// Iterate over the XFormsDB user's roles
						for ( int j = 0; j < xformsdbUserRoles.length; j++ ) {
							xformsdbUserRole					= xformsdbUserRoles[ j ].trim();
							// Found a matching role
							if ( fileRole.equals( xformsdbUserRole ) == true ) {
								hasXFormsDBUserAccessToTheFile	= true;
								break;
							}
						}
					}
					
					// Stop looking for a matching role because one has been already found
					if ( hasXFormsDBUserAccessToTheFile == true ) {
						break;
					}
				}
			}
			
			logger.log( Level.DEBUG, "The logged in XFormsDB user's access to the file has been successfully checked." );
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_21, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_21, ex  );
		}
		
		
		return hasXFormsDBUserAccessToTheFile;
	}

	
	private String handleXFormsDBWidgetLogin( XFormsDBServlet xformsdbServlet, HttpSession session, String idkey, String at ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		String xformsdbWidgetLogin	= null;
		
		
		try {
			// Handle the XFormsDB widget login
			xformsdbWidgetLogin		= this.xformsdbWidgetLoginHandler.handle( xformsdbServlet, session, idkey, at );
			logger.log( Level.DEBUG, "The XFormsDB widget login has been successfully handled." );
		} catch ( HandlerException hex ) {
			throw hex;
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_23, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_23, ex  );
		}
		
		
		return xformsdbWidgetLogin;
	}

	
	// PUBLIC METHODS
	public void handle( XFormsDBServlet xformsdbServlet, HttpServletRequest request, HttpServletResponse response ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		WebAppTO webAppTO								= null;
		XFormsDBConfigTO xformsdbConfigTO				= null;
		String requestURL								= null;
		String requestURI								= null;
		String contextPath								= null;
		String requestedFilePath						= null;
		String realRequestedFilePath					= null;
		File realRequestedFile							= null;
		int requestedFileExtensionIndex					= 0;
		String requestedFileExtension					= null;
		String queryString								= null;
		MimeMappingTO mimeMappingTO						= null;
		MimeMappingTO mimeMappingTOTemp					= null;
		String welcomeFileName							= null;
		String welcomeFilePath							= null;
		String realWelcomeFilePath						= null;
		File realWelcomeFile							= null;
		boolean foundRealWelcomeFile					= false;
		String mimeType									= null;
		String idkey									= null;
		String at										= null;
		String id										= null;
		Document xformsdbFileDocument					= null;
		String xmlString								= null;
		Document document								= null;
				
		
		try {
			// Retrieve the Web application file
			webAppTO									= xformsdbServlet.getWebAppTO();
			
			// Retrieve the XFormsDB configuration file
			xformsdbConfigTO							= xformsdbServlet.getXFormsDBConfigTO();

			// Retrieve the real request file path
			requestURL									= URLDecoder.decode( request.getRequestURL().toString(), Constants.CLIENT_URI_ENCODING_DEFAULT ); // Without query string
			requestURI									= URLDecoder.decode( request.getRequestURI(), Constants.CLIENT_URI_ENCODING_DEFAULT ); // Without query string
			contextPath									= URLDecoder.decode( request.getContextPath(), Constants.CLIENT_URI_ENCODING_DEFAULT ); // Without query string
			requestedFilePath							= requestURI.substring( contextPath.length(), requestURI.length() ); // Request URI - context path
			realRequestedFilePath						= xformsdbServlet.getServletContext().getRealPath( requestedFilePath ); // File system path
			realRequestedFile							= new File( realRequestedFilePath );
			requestedFileExtensionIndex					= requestedFilePath.lastIndexOf( "." );
			if ( requestedFileExtensionIndex != -1 ) {
				requestedFileExtension					= requestedFilePath.substring( requestedFileExtensionIndex + 1 );				
			}
			queryString									= this.getDecodedQueryString( request ); // Query string
			logger.log( Level.DEBUG, "The real requested file path: " + realRequestedFilePath );
			logger.log( Level.DEBUG, "The request URL: " + requestURL + queryString );				

			
			// Set MIME mapping
			if ( requestedFileExtension != null ) {
				for ( int i = 0; i < xformsdbConfigTO.getMimeMappingTOs().size(); i++ ) {
					mimeMappingTOTemp					= xformsdbConfigTO.getMimeMappingTOs().get( i );
					// Find a valid MIME mapping --> use it
					if ( ( ( mimeMappingTOTemp.getExtension() ).equals( requestedFileExtension ) == true ) || ( ( mimeMappingTOTemp.getExtension() + "download" ).equals( requestedFileExtension ) == true ) ) {
						logger.log( Level.DEBUG, "Found a valid MIME mapping (extension: " + mimeMappingTOTemp.getExtension() + ") from XFormsDB MIME mappings." );
						mimeMappingTO					= mimeMappingTOTemp;
						break;
					}
				}
			}			
			// Did not found a valid MIME mapping --> use default
			if ( mimeMappingTO == null ) {
				mimeMappingTO							= xformsdbConfigTO.getMimeMappingTOs().get( 0 );
				logger.log( Level.DEBUG, "Did not found a valid MIME mapping from XFormsDB MIME mappings. Use the default XFormsDB MIME mapping (extension: " + mimeMappingTO.getExtension() + ")." );
			}

			
			// Not an XFormsDB download request
			if ( ( mimeMappingTO.getExtension() + "download" ).equals( requestedFileExtension ) == false ) {
				logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") is not for downloading a file from XFormsDB." );				
				// Check the existence of the real requested file
				if ( realRequestedFile.exists() == true && realRequestedFile.isFile() == true ) {
					logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") exists and it is a file." );
	
					// Check the accessibility of the real requested file
					if ( xformsdbConfigTO.getSecurityFileTOs().containsKey( requestedFileExtension ) == true ) {
						logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") is not accessible." );
						// Do not write a response due to redirecting
//						writeResponse					= false;
						// Send 403: Forbidden
						response.sendError( HttpServletResponse.SC_FORBIDDEN, requestURL + queryString );
					}
					
					// Check the extension type of the requested file
					if ( mimeMappingTO.getExtension().equals( requestedFileExtension ) == true ) {
						logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") has a valid XFormsDB file extension (" + requestedFileExtension + ") i.e. needs to be transformed." );
						// Retrieve the idkey request parameter
						idkey							= this.getDecodedRequestParameterValue( request, Constants.REQUEST_PARAMETER_IDKEY, true, xformsdbServlet.getServerURIEncoding() );

						// Retrieve the at request parameter
						at								= this.getDecodedRequestParameterValue( request, Constants.REQUEST_PARAMETER_AT, true, xformsdbServlet.getServerURIEncoding() );
						
						// Handle the XFormsDB widget login
						if ( idkey != null && at != null && "".equals( xformsdbConfigTO.getWidgetAuthenticationURI() ) == false && "".equals( xformsdbConfigTO.getWidgetDataSourcesURI() ) == false ) {
							this.handleXFormsDBWidgetLogin( xformsdbServlet, request.getSession(), idkey, at );
						}
						
						// Transform the file
						String xformsDocument			= XFormsDBTransformer.transform( xformsdbServlet, request, response, Constants.XFORMSDB_CONFIG_DEFAULT_ENCODING, xformsdbServlet.getServerURIEncoding() );
						// Write the response
						super.handle( response, request, mimeMappingTO.getMimeType(), xformsdbConfigTO.getEncoding(), xformsdbConfigTO.getEncoding(), IOUtils.convert( xformsDocument, xformsdbConfigTO.getEncoding() ), new Date().getTime(), true );
					}
					else {
						logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") has an invalid XFormsDB file extension (" + requestedFileExtension + ") i.e. no need for the transformation." );
						if ( webAppTO.getMimeMappingTOs().get( requestedFileExtension ) != null ) {
							mimeType					= webAppTO.getMimeMappingTOs().get( requestedFileExtension ).getMimeType();
						}
						long lastModified				= ( realRequestedFile.lastModified() / 1000 ) * 1000; // Get rid of milliseconds
						long ifModifiedSince			= request.getDateHeader( "If-Modified-Since" );
						//String ifModifiedSinceStr		= request.getHeader( "If-Modified-Since" );
						DateFormat formatter			= new SimpleDateFormat( Constants.RESOURCE_LAST_MODIFIED_FORMAT, Locale.ENGLISH );
						formatter.setTimeZone( TimeZone.getTimeZone( Constants.RESOURCE_LAST_MODIFIED_TIME_ZONE ) );
						Date lastModifiedDate			= new Date( lastModified );
						String lastModifiedStr			= formatter.format( lastModifiedDate );
						if ( lastModified == ifModifiedSince ) {
							logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") has not been modified and it can be found from the browser's cache." );
							response.setHeader( "Expires", "Sat, 6 May 1995 12:00:00 GMT" );
							response.setHeader( "Last-Modified", lastModifiedStr );
							response.setStatus( HttpServletResponse.SC_NOT_MODIFIED );							
						}
						else {
							logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") has been modified or it cannot be found from the browser's cache." );
							// Write the response
							super.handle( response, request, mimeType, null, xformsdbConfigTO.getEncoding(), new FileInputStream( realRequestedFile ), lastModified, false );
						}
					}				
				}
				else if ( realRequestedFile.exists() == true && realRequestedFile.isDirectory() == true ) {
					logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") exists and it is a directory." );
					// Check the need for an extra slash for the real requested file
					if ( ( requestedFilePath.lastIndexOf( "/" ) == -1 ) || ( ( requestedFilePath.lastIndexOf( "/" ) + 1 ) != requestedFilePath.length() ) ) {
						logger.log( Level.DEBUG, "Redirect the request to: " + requestURL + "/" + queryString );
						// Do not write a response due to redirecting
//						writeResponse					= false;
						// Redirect to a new URL having an extra slash at the end
						response.sendRedirect( StringUtils.toSafeASCIIString( requestURL + "/" + queryString, true, response ) );							
					}
					else {
						logger.log( Level.DEBUG, "Try to retrieve a welcome file." );
						// Try to retrieve a welcome file
						for ( int i = 0; i < webAppTO.getWelcomeFiles().size(); i++ ) {
							welcomeFileName				= webAppTO.getWelcomeFiles().get( i );
							welcomeFilePath				= requestedFilePath + welcomeFileName;
							realWelcomeFilePath			= xformsdbServlet.getServletContext().getRealPath( welcomeFilePath );
							realWelcomeFile				= new File( realWelcomeFilePath );
							logger.log( Level.DEBUG, "The real welcome file path: " + realWelcomeFilePath );
	
							// Check the existence of the real welcome file
							if ( realWelcomeFile.exists() == true && realWelcomeFile.isFile() == true ) {
								logger.log( Level.DEBUG, "The real welcome file path (" + realWelcomeFilePath + ") exists and it is a file." );
								// Do not write a response due to forwarding
//								writeResponse			= false;
								foundRealWelcomeFile	= true;
								// Redirect to a new URL having (welcome file)
								logger.log( Level.DEBUG, "Redirect the request to: " + requestURL + welcomeFileName + queryString );
								response.sendRedirect( StringUtils.toSafeASCIIString( requestURL + welcomeFileName + queryString, true, response ) );
								break;
							}
						}
	
						// Check the existence of a welcome file
						if ( foundRealWelcomeFile == false ) {
							logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") does not exist." );
							// Do not write a response due to 404
//							writeResponse				= false;
							// Send 404: Not Found
							response.sendError( HttpServletResponse.SC_NOT_FOUND, requestURL + queryString );
						}
					}
				}
				else {
					logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") does not exist." );
					// Do not write a response due to 404
//					writeResponse						= false;
					// Send 404: Not Found
					response.sendError( HttpServletResponse.SC_NOT_FOUND, requestURL + queryString );
				}
			}
			// XFormsDB download request
			else {
				logger.log( Level.DEBUG, "The real requested file path (" + realRequestedFilePath + ") is for downloading a file from XFormsDB." );
				// Retrieve the id request parameter
				id										= this.getDecodedRequestParameterValue( request, Constants.REQUEST_PARAMETER_ID, true, xformsdbServlet.getServerURIEncoding() );

				// Retrieve the idkey request parameter
				idkey									= this.getDecodedRequestParameterValue( request, Constants.REQUEST_PARAMETER_IDKEY, true, xformsdbServlet.getServerURIEncoding() );

				// Retrieve the at request parameter
				at										= this.getDecodedRequestParameterValue( request, Constants.REQUEST_PARAMETER_AT, true, xformsdbServlet.getServerURIEncoding() );
				
				// Handle the XFormsDB widget login
				if ( idkey != null && at != null && "".equals( xformsdbConfigTO.getWidgetAuthenticationURI() ) == false && "".equals( xformsdbConfigTO.getWidgetDataSourcesURI() ) == false ) {
					this.handleXFormsDBWidgetLogin( xformsdbServlet, request.getSession(), idkey, at );
				}
				

				// Create the XML document containing the <xformsdb:file> element
				xformsdbFileDocument					= new Document( this.createXFormsDBFileElement( xformsdbServlet, id ) );
				
				// Handle the <xformsdb:file> request
				xmlString								= this.handleXFormsDBFile( xformsdbServlet, xformsdbFileDocument );
				
				// Build the XOM document
				document								= this.buildXOM( xmlString, xformsdbConfigTO.getEncoding() );

				// Retrieve file information
				Element xformsdbFileElement				= document.getRootElement();
				String fileName							= xformsdbFileElement.getAttributeValue( "filename" );
				String fileMediaType					= xformsdbFileElement.getAttributeValue( "mediatype" );
				String fileRoles						= xformsdbFileElement.getAttributeValue( "roles" );

				// Retrieve the roles of the logged in XFormsDB user
				String xformsdbUserRoles				= this.getRolesOfTheLoggedInXFormsDBUser( request.getSession(), xformsdbConfigTO.getEncoding() );

				// XFormsDB does not hold the logged in user
				if ( xformsdbUserRoles == null ) {
					// The file to be downloaded is not public
					if ( "".equals( fileRoles.trim() ) == false ) {
						throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_19, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_19 );
					}
				}
				// XFormsDB holds the logged in user
				else {
					// XFormsDB user does not have access to the file to be downloaded
					if ( this.hasXFormsDBUserAccessToTheFile( xformsdbUserRoles, fileRoles ) == false ) {
						throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_20, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_20 );
					}
				}

				
				// Create a file
				File file								= new File( xformsdbConfigTO.getFilesFolder() + File.separator + id + "_" + fileName );

				// Test the accessibility of the file
				if ( file.exists() == false || file.isFile() == false || file.canRead() == false || file.canWrite() == false ) {
					throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_17, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_17 );
				}
				
				// Write the response for a file to be downloaded
				super.handleDownload( response, fileMediaType, file, id );
			}
			
			logger.log( Level.DEBUG, "The GET request having the content type: null has been handled." );
		} catch ( TransformerException tex ) {
			throw new HandlerException( tex.getCode(), tex.getMessage(), tex );
		} catch ( HandlerException hex ) {
			throw hex;
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_GETNULLHANDLER_5, ErrorConstants.ERROR_MESSAGE_GETNULLHANDLER_5, ex );
		}
	}	
}
