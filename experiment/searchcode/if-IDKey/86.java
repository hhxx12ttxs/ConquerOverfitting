package fi.tkk.tml.xformsdb.handler;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fi.tkk.tml.xformsdb.core.Constants;
import fi.tkk.tml.xformsdb.dao.DAOFactory;
import fi.tkk.tml.xformsdb.dao.XFormsDBDAO;
import fi.tkk.tml.xformsdb.error.DAOException;
import fi.tkk.tml.xformsdb.error.ErrorConstants;
import fi.tkk.tml.xformsdb.error.HandlerException;
import fi.tkk.tml.xformsdb.error.ManagerException;
import fi.tkk.tml.xformsdb.manager.xformsdb.XFormsDBUserManager;
import fi.tkk.tml.xformsdb.manager.xformsdb.XFormsDBWidgetDataSourcesManager;
import fi.tkk.tml.xformsdb.servlet.XFormsDBServlet;
import fi.tkk.tml.xformsdb.xml.to.xformsdb.XFormsDBConfigTO;


/**
 * Handle the XFormsDB widget logins.
 * 
 *
 * @author Markku Laine
 * @version 1.0	 Created on October 22, 2009
 */
public class XFormsDBWidgetLoginHandler extends ResponseHandler {
	
	
	
	// PRIVATE STATIC FINAL VARIABLES
	private static final Logger logger			= Logger.getLogger( XFormsDBWidgetLoginHandler.class );

	
	// PUBLIC CONSTURCTORS
	public XFormsDBWidgetLoginHandler() {
		logger.log( Level.DEBUG, "Constructor has been called." );
	}
	
	
	// PRIVATE METHODS	
	private XFormsDBDAO getXFormsDBDAO( XFormsDBServlet xformsdbServlet ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		Map<String, DAOFactory> daoFactories	= null;
		XFormsDBDAO xformsdbDAO					= null;
		
		
		try {
			// Retrieve the DAO Factories from the servlet context
			daoFactories						= xformsdbServlet.getDAOFactories();
			
			// Use the default DAO factory implementation
			DAOFactory daoFactory				= daoFactories.get( Constants.XFORMSDB_CONFIG_DEFAULT_DATA_SOURCE_ID );
			
			if ( daoFactory == null) {
				throw new DAOException( ErrorConstants.ERROR_CODE_DAO_122, ErrorConstants.ERROR_MESSAGE_DAO_122 );
			}
			
			xformsdbDAO							= daoFactory.getXFormsDBDAO();
			logger.log( Level.DEBUG, "The XFormsDB DAO of the default DAO factory implementation has been successfully retrieved." );
		} catch ( DAOException daoex ) {
			throw new HandlerException( daoex.getCode(), daoex.getMessage(), daoex );
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_XFORMSDBWIDGETLOGINHANDLER_1, ErrorConstants.ERROR_MESSAGE_XFORMSDBWIDGETLOGINHANDLER_1, ex  );
		}
		
		
		return xformsdbDAO;
	}

	
	// PUBLIC METHODS
	public String handle( XFormsDBServlet xformsdbServlet, HttpSession session, String idKey, String at ) throws HandlerException {
		logger.log( Level.DEBUG, "Method has been called." );
		XFormsDBConfigTO xformsDBConfigTO		= null;
		String xformsdbWidgetLogin				= null;
		String xformsdbWidgetDataSources		= null;
		XFormsDBDAO xformsdbDAO					= null;
		
		
		try {
			// Retrieve the XFormsDB configuration file
			xformsDBConfigTO					= xformsdbServlet.getXFormsDBConfigTO();

			// Retrieve the XFormsDB DAO of the default DAO Factory implementation
			xformsdbDAO							= this.getXFormsDBDAO( xformsdbServlet );

			// Retrieve the information of the authenticated widget user
			xformsdbWidgetLogin					= xformsdbDAO.authenticateWidgetUser( idKey, at, xformsDBConfigTO.getWidgetAuthenticationURI(), xformsDBConfigTO.getEncoding() );

			// Store the logged in XFormsDB widget user into the session
			XFormsDBUserManager.setLoggedInXFormsDBUser( session, xformsdbWidgetLogin );

			// Retrieve the information of the authenticated widget user's data sources
			xformsdbWidgetDataSources			= xformsdbDAO.authenticateWidgetDataSources( idKey, at, xformsDBConfigTO.getWidgetDataSourcesURI(), xformsDBConfigTO.getEncoding() );

			// Store the logged in XFormsDB widget user's data sources into the session
			XFormsDBWidgetDataSourcesManager.setXFormsDBWidgetDataSources( session, xformsdbWidgetDataSources );
			logger.log( Level.DEBUG, "The user has been successfully logged in to XFormsDB widget." );
		} catch ( DAOException daoex ) {
			throw new HandlerException( daoex.getCode(), daoex.getMessage(), daoex );
		} catch ( ManagerException mex ) {
			throw new HandlerException( mex.getCode(), mex.getMessage(), mex );
		} catch ( HandlerException hex ) {
			throw hex;
		} catch ( Exception ex ) {
			throw new HandlerException( ErrorConstants.ERROR_CODE_XFORMSDBWIDGETLOGINHANDLER_2, ErrorConstants.ERROR_MESSAGE_XFORMSDBWIDGETLOGINHANDLER_2, ex  );
		}
		
		
		return xformsdbWidgetLogin;
	}	
}
