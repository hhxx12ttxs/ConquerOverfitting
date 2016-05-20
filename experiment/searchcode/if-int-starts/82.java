/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// -*- java -*-
// $Id: HostContext.java 1343 2009-09-23 09:59:32Z vic $
// $Name:  $

package ru.adv.mozart.framework;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletConfigAware;

import ru.adv.cache.Cache;
import ru.adv.cache.CacheManager;
import ru.adv.cache.CacheManagerImpl;
import ru.adv.cache.CacheParameters;
import ru.adv.db.app.request.ResourceLimits;
import ru.adv.db.config.DBConfig;
import ru.adv.io.UnknownIOSourceException;
import ru.adv.logger.TLogger;
import ru.adv.mozart.Defaults;
import ru.adv.mozart.controller.ControllerFactory;
import ru.adv.mozart.servlet.MimeTypeResolver;
import ru.adv.mozart.servlet.ServletMimeTypeResolver;
import ru.adv.repository.Repository;
import ru.adv.repository.RepositoryException;
import ru.adv.util.Files;
import ru.adv.util.IPAddress;
import ru.adv.util.InputOutput;
import ru.adv.util.InvalidIPAddressException;
import ru.adv.util.Path;
import ru.adv.util.Status;
import ru.adv.util.UnreachableCodeReachedException;
import ru.adv.web.captcha.CaptchaService;
import ru.adv.web.mail.MailService;
import ru.adv.xml.newt.tmanager.TManagerImportService;
import ru.adv.xml.transformer.Transformer;

/**
 * ??????????? ????????? ???????????? ?????, ???????????????? ?????????
 * {@link ru.adv.mozart.Mozart} ???????? ??? ????????.<br/> ??????????
 * ????????? ????????? ??? ?????? ???????????? ?????, ???? ??????? ???? ?
 * ?????????? ?????, ???? ? ????? ????????????? {@link SiteTree} ? ?.?.
 * ????????? ?????? ?????
 * {@link ru.adv.mozart.Mozart#init(javax.servlet.ServletConfig)}.<br/> ??
 * ????? ????????????, ??? ??????? ????????????.
 * 
 * @version $Revision: 1.74 $
 */

public class HostContext implements Defaults, Status, ServletConfigAware, InitializingBean, ApplicationListener {

    private static final String XMLSRC                      = "xml-source";
    private static final String IMAGES                      = "images";
    private static final String VERIFIER                    = "verifier";
    private static final String LINK                        = "link";
    private static final String CALENDAR                    = "calendar";
    private static final String XSL                         = "xsl";
    private static final String XML                         = "xml";
    private static final String HTML                        = "html";
    private static final String TRANSFORM                   = "transform";
    private static final String REQUEST                     = "request";
    private static final String BASE                        = "base";
    private static final String DEFAULT_LOADER              = "ru.adv.cache.DefaultCacheLoader";
    private static final String DOM_LOADER                  = "ru.adv.cache.DOMCacheLoader";
    private static final String HTML_LOADER                 = "ru.adv.mozart.framework.HTMLCacheLoader";
    private static final String BDB_STORE                   = "ru.adv.cache.BDBStore";
    private static final String REFERENCE_POOL              = "ru.adv.cache.ReferencePool";
    private static final String TMP_DIR_DEFAULT             = "/tmp";
    
    private static final String BASE_DISK_PATH              = CacheManagerImpl.CACHE_PROP_PREFIX + BASE + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String BASE_MAX_POOL_SIZE          = CacheManagerImpl.CACHE_PROP_PREFIX + BASE + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String BASE_MAX_STORE_SIZE         = CacheManagerImpl.CACHE_PROP_PREFIX + BASE + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_STORE_SIZE;
    private static final String BASE_STORE_CLASS_NAME       = CacheManagerImpl.CACHE_PROP_PREFIX + BASE + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String BASE_LOADER_CLASS_NAME      = CacheManagerImpl.CACHE_PROP_PREFIX + BASE + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String REQUEST_DISK_PATH           = CacheManagerImpl.CACHE_PROP_PREFIX + REQUEST + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String REQUEST_MAX_POOL_SIZE       = CacheManagerImpl.CACHE_PROP_PREFIX + REQUEST + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String REQUEST_MAX_STORE_SIZE      = CacheManagerImpl.CACHE_PROP_PREFIX + REQUEST + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_STORE_SIZE;
    private static final String REQUEST_STORE_CLASS_NAME    = CacheManagerImpl.CACHE_PROP_PREFIX + REQUEST + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String REQUEST_LOADER_CLASS_NAME   = CacheManagerImpl.CACHE_PROP_PREFIX + REQUEST + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String TRANSFORM_DISK_PATH         = CacheManagerImpl.CACHE_PROP_PREFIX + TRANSFORM + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String TRANSFORM_MAX_POOL_SIZE     = CacheManagerImpl.CACHE_PROP_PREFIX + TRANSFORM + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String TRANSFORM_MAX_STORE_SIZE    = CacheManagerImpl.CACHE_PROP_PREFIX + TRANSFORM + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_STORE_SIZE;
    private static final String TRANSFORM_STORE_CLASS_NAME  = CacheManagerImpl.CACHE_PROP_PREFIX + TRANSFORM + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String TRANSFORM_LOADER_CLASS_NAME = CacheManagerImpl.CACHE_PROP_PREFIX + TRANSFORM + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String HTML_DISK_PATH              = CacheManagerImpl.CACHE_PROP_PREFIX + HTML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String HTML_MAX_POOL_SIZE          = CacheManagerImpl.CACHE_PROP_PREFIX + HTML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String HTML_MAX_STORE_SIZE         = CacheManagerImpl.CACHE_PROP_PREFIX + HTML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_STORE_SIZE;
    private static final String HTML_STORE_CLASS_NAME       = CacheManagerImpl.CACHE_PROP_PREFIX + HTML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String HTML_LOADER_CLASS_NAME      = CacheManagerImpl.CACHE_PROP_PREFIX + HTML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String XML_DISK_PATH               = CacheManagerImpl.CACHE_PROP_PREFIX + XML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String XML_MAX_POOL_SIZE           = CacheManagerImpl.CACHE_PROP_PREFIX + XML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String XML_POOL_CLASS_NAME         = CacheManagerImpl.CACHE_PROP_PREFIX + XML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.POOL_CLASS_NAME;
    private static final String XML_STORE_CLASS_NAME        = CacheManagerImpl.CACHE_PROP_PREFIX + XML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String XML_LOADER_CLASS_NAME       = CacheManagerImpl.CACHE_PROP_PREFIX + XML + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String XMLSRC_DISK_PATH            = CacheManagerImpl.CACHE_PROP_PREFIX + XMLSRC + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String XMLSRC_MAX_POOL_SIZE        = CacheManagerImpl.CACHE_PROP_PREFIX + XMLSRC + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String XMLSRC_POOL_CLASS_NAME      = CacheManagerImpl.CACHE_PROP_PREFIX + XMLSRC + CacheManagerImpl.SEPARATOR + CacheManagerImpl.POOL_CLASS_NAME;
    private static final String XMLSRC_STORE_CLASS_NAME     = CacheManagerImpl.CACHE_PROP_PREFIX + XMLSRC + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String XMLSRC_LOADER_CLASS_NAME    = CacheManagerImpl.CACHE_PROP_PREFIX + XMLSRC + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String XSL_DISK_PATH               = CacheManagerImpl.CACHE_PROP_PREFIX + XSL + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String XSL_MAX_POOL_SIZE           = CacheManagerImpl.CACHE_PROP_PREFIX + XSL + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String XSL_POOL_CLASS_NAME         = CacheManagerImpl.CACHE_PROP_PREFIX + XSL + CacheManagerImpl.SEPARATOR + CacheManagerImpl.POOL_CLASS_NAME;
    private static final String XSL_STORE_CLASS_NAME        = CacheManagerImpl.CACHE_PROP_PREFIX + XSL + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String XSL_LOADER_CLASS_NAME       = CacheManagerImpl.CACHE_PROP_PREFIX + XSL + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String CALENDAR_DISK_PATH          = CacheManagerImpl.CACHE_PROP_PREFIX + CALENDAR + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String CALENDAR_MAX_POOL_SIZE      = CacheManagerImpl.CACHE_PROP_PREFIX + CALENDAR + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String CALENDAR_POOL_CLASS_NAME    = CacheManagerImpl.CACHE_PROP_PREFIX + CALENDAR + CacheManagerImpl.SEPARATOR + CacheManagerImpl.POOL_CLASS_NAME;
    private static final String CALENDAR_STORE_CLASS_NAME   = CacheManagerImpl.CACHE_PROP_PREFIX + CALENDAR + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String CALENDAR_LOADER_CLASS_NAME  = CacheManagerImpl.CACHE_PROP_PREFIX + CALENDAR + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String LINK_DISK_PATH              = CacheManagerImpl.CACHE_PROP_PREFIX + LINK + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String LINK_MAX_POOL_SIZE          = CacheManagerImpl.CACHE_PROP_PREFIX + LINK + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String LINK_POOL_CLASS_NAME        = CacheManagerImpl.CACHE_PROP_PREFIX + LINK + CacheManagerImpl.SEPARATOR + CacheManagerImpl.POOL_CLASS_NAME;
    private static final String LINK_STORE_CLASS_NAME       = CacheManagerImpl.CACHE_PROP_PREFIX + LINK + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String LINK_LOADER_CLASS_NAME      = CacheManagerImpl.CACHE_PROP_PREFIX + LINK + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;
    private static final String IMAGES_DISK_PATH            = CacheManagerImpl.CACHE_PROP_PREFIX + IMAGES + CacheManagerImpl.SEPARATOR + CacheManagerImpl.DISK_PATH;
    private static final String IMAGES_MAX_POOL_SIZE        = CacheManagerImpl.CACHE_PROP_PREFIX + IMAGES + CacheManagerImpl.SEPARATOR + CacheManagerImpl.MAX_POOL_SIZE;
    private static final String IMAGES_POOL_CLASS_NAME      = CacheManagerImpl.CACHE_PROP_PREFIX + IMAGES + CacheManagerImpl.SEPARATOR + CacheManagerImpl.POOL_CLASS_NAME;
    private static final String IMAGES_STORE_CLASS_NAME     = CacheManagerImpl.CACHE_PROP_PREFIX + IMAGES + CacheManagerImpl.SEPARATOR + CacheManagerImpl.STORE_CLASS_NAME;
    private static final String IMAGES_LOADER_CLASS_NAME    = CacheManagerImpl.CACHE_PROP_PREFIX + IMAGES + CacheManagerImpl.SEPARATOR + CacheManagerImpl.LOADER_CLASS_NAME;

    private TLogger logger = new TLogger(HostContext.class);
    private String  mozartConfigFile;
    private String	servletPath;
    private Repository repository; 
    private MimeTypeResolver mimeTypeResolver;
    private ControllerFactory controllerFactory;
    
    private Map<String,Object>	applicationScope = new ConcurrentHashMap<String, Object>();    
    
    private MozartConfig  mozartConfig = new MozartConfig();
    private CacheManagerImpl        cacheManager;
    private CaptchaService 		captchaService;
    private MailService 		mailService;
    private MAppContext         rootMAppContext;
    private Map<String,MAppContext>  _appContexts  = new HashMap<String,MAppContext>();
    private List<IPAddress>     debuggingIps;
    private long                _watchConfigInterval        = RELOAD_INTERVAL_DEFAULT;
    private String              webAppDir; // directory of current Web Application
    private String              hostRootDir; // directory of ROOT web application
    private String              defaultSMTPServer = SMTPSERVER_DEFAULT ;
    private boolean             _sessionIdInURL;
    private long                _lastWatchTime;
    private String              cacheRootDir, uploadDir, sessionDir, mailLogDir;
    private Exception           _errorXslException;
    private Transformer         _errorTransformer           = null;
    
    public static final int     DEF_HTML_CACHE_SIZE         = 5000;
    public static final int     DEF_BASE_CACHE_SIZE         = 3000;
    public static final int     DEF_XML_CACHE_SIZE          = 50;
    public static final int     DEF_XSL_CACHE_SIZE          = 50;
    public static final int     DEF_CALENDAR_CACHE_SIZE     = 50;
    public static final int     DEF_LINK_CACHE_SIZE         = 100;
    public static final int     DEF_VERIFY_CACHE_SIZE       = 100;
    public static final int     DEF_IMAGES_CACHE_SIZE       = 250;
    public static final int     DEF_XMLSRC_CACHE_SIZE       = 10;

    public static final String  TMP_DIR_ATTR_CONTEXT        = "javax.servlet.context.tempdir";

    private int                 _limitConnectionCount       = LIMIT_CONNECTION_DEFAULT;
    private int                 _proxyPort                  = Defaults.PROXY_PORT_DEFAULT;
    private long                _maxExpires                 = 3600000L; // one hour
    /** query-stat */
    private String				_statisticURI;
    
	private Set<CacheParameters> extraRegisteredCaches = new HashSet<CacheParameters>();
    
    private List<EnvironmentCalculator> environmentCalculators = new ArrayList<EnvironmentCalculator>();
    
    private TManagerImportService tManagerImportService;
    
    private boolean isStartedUp = false;
    
    private List<ApplicationListener> applicationEventListeners = new ArrayList<ApplicationListener>();  

    /**
     * ???????????.</br>
     * 
     * {@link ru.adv.mozart.Defaults#CONFIG}.
     */
    public HostContext(Repository repository) {
    	Assert.notNull(repository, "Repository must not be null");
    	this.repository = repository;
    	// Init TManagerImportService
    	this.tManagerImportService = new TManagerImportService();
    	registerApplicationListener(tManagerImportService);
    }
    
    public Repository getRepository() {
        return repository;
    }

    public TManagerImportService getTManagerImportService() {
    	return tManagerImportService;
    }

    public synchronized CacheManager getCacheManager() {
    	return cacheManager;
    }

	public Map<String, Object> getApplicationScope() {
		return applicationScope;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull( getRepository(), "Property repository is not set" );
		Assert.hasText( getMozartConfigFile(), "Property mozartConfigFile is not set" );
		Assert.notEmpty( getEnvironmentCalculators(), "Property environmentCalculators is empty");
	}
	
	public MimeTypeResolver getMimeTypeResolver() {
		return mimeTypeResolver;
	}
	
	public ControllerFactory getControllerFactory() {
		return controllerFactory;
	}

	public void setControllerFactory(ControllerFactory controllerFactory) {
		this.controllerFactory = controllerFactory;
	}

	public List<EnvironmentCalculator> getEnvironmentCalculators() {
		return environmentCalculators;
	}
	public void setEnvironmentCalculators( List<EnvironmentCalculator> environmentCalculators) {
		this.environmentCalculators = environmentCalculators;
	}

	/**
     * Do initialization on startup, if it starts under Springframework
     */
    @Override
	public void setServletConfig(ServletConfig servletConfig) {
    	try {
    		init(servletConfig);
    	} catch (ServletException e) {
    		logger.error(e);
		}
	}

	/**
     * Reinit {@link HostContext} if config is changed
     * 
     * @throws ServletException
     */
    public synchronized void watch(ServletContext sc) throws ServletException {
        if (_lastWatchTime + getWatchConfigInterval() <= System.currentTimeMillis()) {
            long currentTime = System.currentTimeMillis();
            if (mozartConfig.isChanged()) {
                init(mozartConfigFile, hostRootDir, webAppDir, sc);
            }
            _lastWatchTime = currentTime;
        }
    }
    
    public String getWebAppDir() {
    	return webAppDir;
    }

	/**
     * ?????????? ???????? ?????????? ?? ??????????? requestURI
     * 
     * @param requestURI
     * @return
     */
    public MAppContext getMAppContext(String requestURI) throws SiteTreeLoadException, RedirectToStaticFileException, MozartRedirectToLocation {
        logger.info("requestURI="+requestURI);
        for (Map.Entry<String,MAppContext> entry : _appContexts.entrySet()) {
            String prefix = entry.getKey();
            if (requestURI.startsWith(prefix)) {
                return checkMAppContext(requestURI, prefix, entry.getValue());
            }
        }
        if (requestURI.length() == 0 || requestURI.endsWith("/") || fileNameNotContainsDot(requestURI)) {
            tryRedirectToIndex(rootMAppContext, requestURI); // exception on redirect
        }
        return checkMAppContext(requestURI, "/", rootMAppContext);
    }

    private MAppContext checkMAppContext(String requestURI, String prefix, MAppContext appContext) throws MozartRedirectToLocation, SiteTreeLoadException, RedirectToStaticFileException {
        tryRedirectToIndex(appContext, requestURI); // exception on
        // redirect
        try {
            // ???????? ??????? ? SiteTree; get info simply
            appContext.getSiteTree().getDocumentInfo(requestURI);
            return appContext;
        } catch (MozartNotFoundException e) {
            // check static file
            try {
                String uriInApp = requestURI.replaceFirst(prefix, "htdocs://");
                InputOutput io = appContext.createInputOutput(uriInApp);
                if (io.canOpen()) {
                	// FIXME do not send static files
                    logger.debug("Send static file " + io.toString());
                    throw new RedirectToStaticFileException(io);
                }
            } catch (UnknownIOSourceException ee) {
                logger.warning(ee);
            }
        }
        // then will be 404 ???????? ? ????????? ?????????
        return rootMAppContext;
    }

    private boolean fileNameNotContainsDot(String requestURI) {
        return Path.getFileName(requestURI).indexOf('.') == -1;
    }

    private void tryRedirectToIndex(MAppContext appContext, String queryPath) throws MozartRedirectToLocation, SiteTreeLoadException {
        if (!queryPath.endsWith("/")) {
            queryPath = queryPath + "/";
        }
        for (String indexFile : appContext.getIndexFiles()) {
            String path = queryPath + indexFile;
            try {
                appContext.getSiteTree().getDocumentInfo(path);
                throw new MozartRedirectToLocation(path);
            } catch (MozartNotFoundException e) {
            }
        }
    }

    public void setDefaultSMTPServer(String defaultSMTPServer) {
    	if (StringUtils.hasText(defaultSMTPServer)) {
    		this.defaultSMTPServer = defaultSMTPServer;
    	}
    }


    private String getDefaultSMTPServer() {
        return this.defaultSMTPServer;
    }

    public int getLimitConnectionCount() {
        return _limitConnectionCount;
    }

    private long _time;

    private void startTiming() {
        _time = System.currentTimeMillis();
    }

    private long getTiming() {
        long result = System.currentTimeMillis() - _time;
        startTiming();
        return result;
    }
    
    public String getMozartConfigFile() {
		return mozartConfigFile;
	}
	public void setMozartConfigFile(String mozartConfigFile) {
		this.mozartConfigFile = mozartConfigFile;
	}

	/**
     * Wrapper to {@link #init(String, String, ServletContext)}
     * 
     * @param servletConfig
     */
    public synchronized void init( ServletConfig servletConfig ) throws ServletException {
    	
    	if (isStartedUp) {
    		logger.debug("HostContext has been initialized already");
    		return;
    	}
    	
		String webAppDir = servletConfig.getServletContext().getRealPath("");
		String hostRootDir = webAppDir; // default value;
		
		logger.debug("webAppDir is " + webAppDir);
		
		// find ROOT web application
		ServletContext serverRootCtx = servletConfig.getServletContext().getContext("/");
		if (serverRootCtx!=null) {
			hostRootDir = serverRootCtx.getRealPath("");
			logger.info("hostRoot application directory: " + hostRootDir);
		} else {
			logger.error("Can't calculate directory of the ROOT web application. " +
					"Please, set crossContext=\"true\" attribute into deafult Context element if " +
					"you are using Tomcat");
		}
		
		// fix config to full file path
		if ( ! Files.isAbsolute( getMozartConfigFile() )) {
			setMozartConfigFile( Files.normalize(webAppDir + "/" + getMozartConfigFile()) );
		}
		
		// try parameters from servlet config
		String config = servletConfig.getInitParameter(Defaults.CONFIG);
    	if (StringUtils.hasText( config ) ) {
    		setMozartConfigFile( Files.normalize(webAppDir + "/" + config) );
    	}
		String smtpServer = servletConfig.getInitParameter(Defaults.SMTPSERVER_PROP);
		if (StringUtils.hasText(smtpServer)) {
			setDefaultSMTPServer(smtpServer);
		}

		init( getMozartConfigFile(), hostRootDir, webAppDir, servletConfig.getServletContext() );
		
		isStartedUp = true;
    }
    
    /**
     * ?????????? ????????????? ??????????? ??????? <code>HostContext</code>,
     * ?????????? ? {@link ru.adv.mozart.Mozart}. ????????? ????????????????
     * ???? ?????????? Mozart ?? ???? ????????? ? <code>config</code>
     * ?????????? {@link ru.adv.mozart.Defaults#CONFIG}.<br/> ??? ??????????
     * ?? ????????????? ? ???????????????? ????? ?????????? ??????????? ?????? ?
     * {@link ru.adv.mozart.Defaults}. <br/> ???????? System.err ?? ????? ?
     * ????, ????????? ? ???????. <br/> ??????
     * {@link ru.adv.cache.CacheFactory}.<br/> ???????????? ?
     * {@link ru.adv.cache.CacheFactory} ???? ????? :<br/>
     * {@link ru.adv.cache.FileCache},{@link ru.adv.cache.MemCache},
     * {@link ru.adv.cache.DBConnectCache}.<br/> ???????????? ?
     * {@link ru.adv.cache.CacheFactory} ???????? ?????:
     * <code>html, xml, xsl, calendar, db</code>.
     * 
     */
    private synchronized void init(String configPath, String hostRootDir, String webAppDir, ServletContext servletContext) throws ServletException {
        try {
        	
            startTiming();
            this.hostRootDir = hostRootDir;
            this.webAppDir = webAppDir;
            this.mozartConfigFile = configPath;
            this.servletPath = servletContext.getContextPath();
            this.mimeTypeResolver = new ServletMimeTypeResolver(servletContext);
            
            initCacheRootDir(servletContext);
            initUploadDir(servletContext);
            initSessionDir(servletContext);
            initMailLogDir(servletContext);
            
            // read mozart config
            mozartConfig.init(this.mozartConfigFile, hostRootDir, webAppDir, getDefaultSMTPServer());
            
            logger.debug("host root="+hostRootDir);
            logger.debug("web app root="+webAppDir);
            logger.debug("HostContext.init(): mozartConfig.init() takes " + getTiming());
            initCache();
            logger.debug("HostContext.init(" + getCacheRootDir() + "): initCache() takes " + getTiming());
            
            _watchConfigInterval = mozartConfig.getWatchConfigInterval();
            
            debuggingIps = Collections.unmodifiableList(mozartConfig.getDebbugingIPs());
            _sessionIdInURL = mozartConfig.getSessionIdInURL();
            _errorTransformer = mozartConfig.getErrorTransformer();
            _errorXslException = mozartConfig.getErrorXslException();
            _limitConnectionCount = mozartConfig.getResourceLimits().getLimitConnectionCount();
            _proxyPort = mozartConfig.getProxyPort();
            _maxExpires = mozartConfig.getMaxExpires();
            _statisticURI = mozartConfig.getStatisticURI();
            logger.debug("HostContext.init(): init parameters takes " + getTiming());
            logger.debug("HostContext.init(): initTimimgLog() takes " + getTiming());
            rootMAppContext = new MAppContext(this, mozartConfig.getDefaultMAppConfig());
            logger.debug("HostContext.init(): new MAppContext() takes " + getTiming());
            _appContexts = new HashMap<String,MAppContext>();
            logger.debug("Exists mozart applications: " + mozartConfig.getAppNames().toString());
            for (String appName : mozartConfig.getAppNames()) {
                logger.debug("Load mozart application: " + appName);
                MAppConfig mAppConfig = mozartConfig.getMAppConfig(appName);
                MAppContext appContext = new MAppContext(this, mAppConfig);
                _appContexts.put(mAppConfig.getUrlPrefix(), appContext);
                logger.debug("HostContext.init(): mapp creation takes " + getTiming());
            }
            
        } catch (Throwable t) {
            logger.logFatalStackTrace("Error on init HostContext",t);
            logger.error(mozartConfig);
            throw new ServletException("Cannot initialize host context: " + t.getMessage(), t);
        }
        logger.debug(mozartConfigFile + " reloaded.");
    }

    public boolean isRootApplication() {
    	return this.webAppDir.equals(this.hostRootDir);
    }
    
    public MAppContext getRootMAppContext() {
		return rootMAppContext;
	}

	/**
     * Get servlet prefix in URI path
     * @return
     */
    public String getServletPath() {
		return servletPath;
	}

    /* query-stat */
    public String getStatisticURI(){
    	return _statisticURI;
    }
    
    public synchronized String getQueryStatLogFile() {
        return mozartConfig.getQueryStatLogFile();
    }

    public long getMaxExpires() {
        return _maxExpires;
    }

    public String getLocation404() throws SiteTreeLoadException {
        return rootMAppContext.getSiteTree().getLocation404();
    }

    public static String getUploadDir(ServletContext servletContext) {
        return createTmpDirectoryInServletContext(servletContext,Defaults.UPLOAD_ROOT);
    }

    /**
     * @param servletContext
     * @return
     */
    private static Object calcTempDir(ServletContext servletContext, String defaultUploadDir) {
        Object o = null;
        if (servletContext == null) {
            o = new File(defaultUploadDir);
        } else {
            o = servletContext.getAttribute(TMP_DIR_ATTR_CONTEXT);
        }
        return o;
    }

    private void initUploadDir(ServletContext servletContext) {
        this.uploadDir = getUploadDir(servletContext);
    }
	public String getUploadDir() {
        return uploadDir;
    }
    
    private void initCacheRootDir(ServletContext servletContext) {
        this.cacheRootDir = calculateCacheRootDir(servletContext);
    }
    public String getCacheRootDir() {
		return cacheRootDir;
	}
    
	public void registerCache(CacheParameters cacheParameters) {
		cacheManager.registerCache(cacheParameters);
		extraRegisteredCaches.add(cacheParameters);
	}
    
    private void initCache() {
    	if (null != cacheManager) {
    		cacheManager.destroy();
    		cacheManager = null;
    	}
    	cacheManager = new CacheManagerImpl( 
    			getCacheProperties(), 
    			( repository==null ? null : repository.getObjectModifyingHelper() ),
    			getServletPath()
    	);
    	for ( CacheParameters cacheParameters : extraRegisteredCaches ) {
    		cacheManager.registerCache(cacheParameters);
    	}
    }
    
    private Properties getCacheProperties() {
    	
        final Properties result = new Properties();
        result.putAll(mozartConfig.getCacheProperties());
        logger.debug("getCacheProperties(): result=" + result);
        
        String diskPath = getCacheRootDir();

        if (!result.containsKey(CALENDAR_MAX_POOL_SIZE))
            result.put(CALENDAR_MAX_POOL_SIZE, "" + DEF_CALENDAR_CACHE_SIZE);
        if (!result.containsKey(CALENDAR_POOL_CLASS_NAME))
            result.put(CALENDAR_POOL_CLASS_NAME, REFERENCE_POOL);
        if ((result.containsKey(CALENDAR_STORE_CLASS_NAME) || result.containsKey(CALENDAR_LOADER_CLASS_NAME)))
            result.put(CALENDAR_DISK_PATH, diskPath);

        if (!result.containsKey(LINK_MAX_POOL_SIZE))
            result.put(LINK_MAX_POOL_SIZE, "" + DEF_LINK_CACHE_SIZE);
        if (!result.containsKey(LINK_POOL_CLASS_NAME))
            result.put(LINK_POOL_CLASS_NAME, REFERENCE_POOL);
        if ((result.containsKey(LINK_STORE_CLASS_NAME) || result.containsKey(LINK_LOADER_CLASS_NAME)))
            result.put(LINK_DISK_PATH, diskPath);

        if (!result.containsKey(IMAGES_MAX_POOL_SIZE))
            result.put(IMAGES_MAX_POOL_SIZE, "" + DEF_IMAGES_CACHE_SIZE);
        if (!result.containsKey(IMAGES_POOL_CLASS_NAME))
            result.put(IMAGES_POOL_CLASS_NAME, REFERENCE_POOL);
        if ((result.containsKey(IMAGES_STORE_CLASS_NAME) || result.containsKey(IMAGES_LOADER_CLASS_NAME)))
            result.put(IMAGES_DISK_PATH, diskPath);

        if (!result.containsKey(XML_MAX_POOL_SIZE))
            result.put(XML_MAX_POOL_SIZE, "" + DEF_XML_CACHE_SIZE);
        if (!result.containsKey(XML_POOL_CLASS_NAME))
            result.put(XML_POOL_CLASS_NAME, REFERENCE_POOL);
        if ((result.containsKey(XML_STORE_CLASS_NAME) || result.containsKey(XML_LOADER_CLASS_NAME)))
            result.put(XML_DISK_PATH, diskPath);

        if (!result.containsKey(XMLSRC_MAX_POOL_SIZE))
            result.put(XMLSRC_MAX_POOL_SIZE, "" + DEF_XMLSRC_CACHE_SIZE);
        if (!result.containsKey(XMLSRC_POOL_CLASS_NAME))
            result.put(XMLSRC_POOL_CLASS_NAME, REFERENCE_POOL);
        if ((result.containsKey(XMLSRC_STORE_CLASS_NAME) || result.containsKey(XMLSRC_LOADER_CLASS_NAME)))
            result.put(XMLSRC_DISK_PATH, diskPath);

        if (!result.containsKey(XSL_MAX_POOL_SIZE))
            result.put(XSL_MAX_POOL_SIZE, "" + DEF_XSL_CACHE_SIZE);
        if (!result.containsKey(XSL_POOL_CLASS_NAME)) {
            result.put(XSL_POOL_CLASS_NAME, REFERENCE_POOL);
        }
        if ((result.containsKey(XSL_STORE_CLASS_NAME) || result.containsKey(XSL_LOADER_CLASS_NAME)))
            result.put(XSL_DISK_PATH, diskPath);

        if (!result.containsKey(HTML_MAX_STORE_SIZE))
            result.put(HTML_MAX_STORE_SIZE, "" + DEF_HTML_CACHE_SIZE);
        if (!result.containsKey(HTML_MAX_POOL_SIZE))
            result.put(HTML_MAX_POOL_SIZE, "" + (new Integer(result.getProperty(HTML_MAX_STORE_SIZE)).intValue() / 10));
        if (!result.containsKey(HTML_STORE_CLASS_NAME))
            result.put(HTML_STORE_CLASS_NAME, BDB_STORE);
        if (!result.containsKey(HTML_LOADER_CLASS_NAME))
            result.put(HTML_LOADER_CLASS_NAME, HTML_LOADER);
        if ((result.containsKey(HTML_STORE_CLASS_NAME) || result.containsKey(HTML_LOADER_CLASS_NAME)))
            result.put(HTML_DISK_PATH, diskPath);

        if (!result.containsKey(BASE_MAX_STORE_SIZE))
            result.put(BASE_MAX_STORE_SIZE, "" + DEF_BASE_CACHE_SIZE);
        if (!result.containsKey(BASE_MAX_POOL_SIZE))
            result.put(BASE_MAX_POOL_SIZE, "" + (new Integer(result.getProperty(BASE_MAX_STORE_SIZE)).intValue() / 10));
        if (!result.containsKey(BASE_STORE_CLASS_NAME))
            result.put(BASE_STORE_CLASS_NAME, BDB_STORE);
        if (!result.containsKey(BASE_LOADER_CLASS_NAME))
            result.put(BASE_LOADER_CLASS_NAME, DEFAULT_LOADER);
        if ((result.containsKey(BASE_STORE_CLASS_NAME) || result.containsKey(BASE_LOADER_CLASS_NAME)))
            result.put(BASE_DISK_PATH, diskPath);

        if (!result.containsKey(REQUEST_MAX_STORE_SIZE))
            result.put(REQUEST_MAX_STORE_SIZE, "" + DEF_BASE_CACHE_SIZE);
        if (!result.containsKey(REQUEST_MAX_POOL_SIZE))
            result.put(REQUEST_MAX_POOL_SIZE, "" + (new Integer(result.getProperty(REQUEST_MAX_STORE_SIZE)).intValue() / 10));
        if (!result.containsKey(REQUEST_STORE_CLASS_NAME))
            result.put(REQUEST_STORE_CLASS_NAME, BDB_STORE);
        if (!result.containsKey(REQUEST_LOADER_CLASS_NAME))
            result.put(REQUEST_LOADER_CLASS_NAME, DOM_LOADER);
        if ((result.containsKey(REQUEST_STORE_CLASS_NAME) || result.containsKey(REQUEST_LOADER_CLASS_NAME)))
            result.put(REQUEST_DISK_PATH, diskPath);

        if (!result.containsKey(TRANSFORM_MAX_STORE_SIZE))
            result.put(TRANSFORM_MAX_STORE_SIZE, "" + DEF_BASE_CACHE_SIZE);
        if (!result.containsKey(TRANSFORM_MAX_POOL_SIZE))
            result.put(TRANSFORM_MAX_POOL_SIZE, "" + (new Integer(result.getProperty(TRANSFORM_MAX_STORE_SIZE)).intValue() / 10));
        if (!result.containsKey(TRANSFORM_STORE_CLASS_NAME))
            result.put(TRANSFORM_STORE_CLASS_NAME, BDB_STORE);
        if (!result.containsKey(TRANSFORM_LOADER_CLASS_NAME))
            result.put(TRANSFORM_LOADER_CLASS_NAME, DOM_LOADER);
        if ((result.containsKey(TRANSFORM_STORE_CLASS_NAME) || result.containsKey(TRANSFORM_LOADER_CLASS_NAME)))
            result.put(TRANSFORM_DISK_PATH, diskPath);

        logger.debug("getCacheProperties(): result=" + result);
        return result;
    }

    public synchronized List<IPAddress> getDebuggingIps() {
        return debuggingIps;
    }

    public synchronized long getWatchConfigInterval() {
        return _watchConfigInterval;
    }
    
    public CaptchaService getCaptchaService() {
        return captchaService;
    }
    
    public void registerCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    /**
     * Getter to delegate mail service to
     * local contexts, such as newt context
     * from application context
     * 
     * @return {@link MailService} object
     */
    public MailService getMailService() {
        return mailService;
    }
    
    /**
     * Setter to delegate mail service to
     * local contexts, such as newt context
     * from application context
     * Calling from real mail service that
     * configuring in spring configuration
     * file as example
     * 
     * @param {@link MailService} object
     */
    public void registerMailService(MailService mailService) {
        this.mailService = mailService;
    }

    private void initSessionDir(ServletContext servletContext) {
        sessionDir = createTmpDirectoryInServletContext(servletContext,Defaults.SESSION_ROOT);
    }

    public synchronized String getSessionDir() {
        return sessionDir;
    }
    
    private void initMailLogDir(ServletContext servletContext) {
        mailLogDir = createTmpDirectoryInServletContext(servletContext,Defaults.MAIL_LOG_ROOT);
    }
    
    public synchronized String getMailLogDir() {
        return mailLogDir;
    }

    /**
     * ???????? destroy ??? <code>CacheFactory,HostLogger</code>
     */
    public synchronized void destroy() {
        cacheManager.destroy();
        cacheManager = null;
        tManagerImportService.destroy();
    }

    public synchronized Cache getHtmlCache() {
        return cacheManager.getCache(HTML);
    }

    public synchronized Cache getBaseCache() {
        return cacheManager.getCache(BASE);
    }

    public synchronized Cache getRequestCache() {
        return cacheManager.getCache(REQUEST);
    }

    public synchronized Cache getTransformCache() {
        return cacheManager.getCache(TRANSFORM);
    }

    public synchronized Cache getXmlCache() {
        return cacheManager.getCache(XML);
    }

    public synchronized Cache getXmlSourceCache() {
        return cacheManager.getCache(XMLSRC);
    }

    public synchronized Cache getXslCache() {
        return cacheManager.getCache(XSL);
    }

    public synchronized Cache getCalendarCache() {
        return cacheManager.getCache(CALENDAR);
    }

    public synchronized Cache getLinkCache() {
        return cacheManager.getCache(LINK);
    }

    public synchronized Cache getVerifierCache() {
        return cacheManager.getCache(VERIFIER);
    }

    public synchronized Cache getImagesCache() {
        return cacheManager.getCache(IMAGES);
    }

    public synchronized String getSmtpServer() {
        return mozartConfig.getSmtpServer();
    }

    public String getFileStoragePath(String dbName) throws RepositoryException, RepositoryNotInitializedException {
        return getRepository().getDatabaseFileDir(dbName);
    }

    public DBConfig getDBConfig(String db) throws RepositoryException, RepositoryNotInitializedException {
        return getRepository().getDBConfig(db);
    }

    public boolean canRemoteIpUseDebugModes(String ipAddress) {
        try {
            boolean result = false;
            IPAddress remoteAddr = new IPAddress(ipAddress);
            for (IPAddress allowedIp : getDebuggingIps()) {
                if (allowedIp.match(remoteAddr)) {
                    result = true;
                    break;
                }
            }
            return result;
        } catch (InvalidIPAddressException e) {
            throw new UnreachableCodeReachedException(e);
        }
    }

    public synchronized boolean isSessionIdInURL() {
        return _sessionIdInURL;
    }

    public synchronized Exception getErrorXslException() {
        return _errorXslException;
    }

    public synchronized Transformer getErrorTransformer() {
        return _errorTransformer;
    }
    
    @Override
	public void onApplicationEvent(ApplicationEvent event) {
    	logger.info(event);
    	synchronized (this.applicationEventListeners) {
			for (ApplicationListener listener : this.applicationEventListeners) {
				listener.onApplicationEvent(event);
			}
		}
	}
    
    public void registerApplicationListener( ApplicationListener listener) {
    	synchronized (this.applicationEventListeners) {
			this.applicationEventListeners.add(listener);
		}
    }

	@SuppressWarnings("unchecked")
	public synchronized List<String> getStatus() {
        List<String> result = new LinkedList<String>(cacheManager.getStatus());
        Properties cacheProps = getCacheProperties();
        result.add("");
        for (Iterator i = new TreeSet(cacheProps.keySet()).iterator(); i.hasNext();) {
            String key = (String) i.next();
            result.add(key + "=" + cacheProps.getProperty(key));
        }
        result.add("");
        return result;
    }

    public synchronized void writeCacheInfoDump(String segmentName, OutputStream out, boolean withMemoryInfo) throws IOException {
        cacheManager.getCache(segmentName).writeCacheInfoDump(out, withMemoryInfo);
    }

    public ResourceLimits getResourceLimits() {
        return mozartConfig.getResourceLimits();
    }

    public int getProxyPort() {
        return _proxyPort;
    }
    
    public MozartConfig getMozartConfig(){
    	return this.mozartConfig;
    }
    
    
    
    private String calculateCacheRootDir(ServletContext servletContext) {
        String result = getMozartConfig().getProperty(CACHEROOT_PROP);
        if ( StringUtils.hasLength(result) ) {
        	result = calculateCacheRootInFileSystem(result); 
        } else {
        	result = createTmpDirectoryInServletContext(servletContext, Defaults.CACHE_ROOT);
        }
        return result;
    }
    
	private static String createTmpDirectoryInServletContext(ServletContext servletContext, String subDir) {
		String tmpDir = TMP_DIR_DEFAULT;
        Object o = calcTempDir(servletContext, tmpDir);
        if (o instanceof File) {
        	tmpDir = ((File) o).getPath() + File.separator + subDir;
            File f = new File(tmpDir);
            if (!f.exists() && !f.mkdirs()) {
                TLogger.error(HostContext.class, "Can't create dir " + tmpDir);
            }
        } else {
        	TLogger.error(HostContext.class, "HostContext.init(): can't get temporary file from " + TMP_DIR_ATTR_CONTEXT);
        }
		return tmpDir;
	}


	private String calculateCacheRootInFileSystem(String result) {
		String suffix = System.getProperty(CACHESUFFIX_PROP);
		if (suffix != null) {
			result += suffix;
		}
		result = StringUtils.cleanPath(result);
		if (Files.isAbsolute(result)) {
			// to split cache directories into site's directories
			result += "/" + getLastTwoSubdirs( getHostRoot().getRoot() );
		} else {
			result = getHostRoot().getRoot() + "/" + result;
		}
		return result;
	}

	private Path getHostRoot() {
		return getMozartConfig().getHostRoot();
	}
    
    private String getLastTwoSubdirs(String dirPath) {
    	String[] dirsArray = StringUtils.tokenizeToStringArray(
    			StringUtils.cleanPath(dirPath),	"/"
    	);
    	StringBuilder buff = new StringBuilder();
    	if (dirsArray.length>=1) {
    		buff.append( dirsArray[dirsArray.length-1] );
    		if (dirsArray.length>=2) {
    			buff.insert(0, "/");
    			buff.insert(0, dirsArray[dirsArray.length-2] );
    		}
    	}
    	return buff.toString();
    }
    
}

