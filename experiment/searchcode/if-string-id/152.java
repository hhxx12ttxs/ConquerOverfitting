/*
 * AdroitLogic UltraESB Enterprise Service Bus
 *
 * Copyright (c) 2010-2012 AdroitLogic Private Ltd. (http://adroitlogic.org). All Rights Reserved.
 *
 * GNU Affero General Public License Usage
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program (See LICENSE-AGPL.TXT).
 * If not, see http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Commercial Usage
 *
 * Licensees holding valid UltraESB Commercial licenses may use this file in accordance with the UltraESB Commercial
 * License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written
 * agreement between you and AdroitLogic.
 *
 * If you are unsure which license is appropriate for your use, or have questions regarding the use of this file,
 * please contact AdroitLogic at info@adroitlogic.com
 */

package org.adroitlogic.ultraesb.core;

import org.adroitlogic.soapbox.CryptoSupport;
import org.adroitlogic.ultraesb.LicenseManager;
import org.adroitlogic.ultraesb.ServerManager;
import org.adroitlogic.ultraesb.api.ConfigurationConstants;
import org.adroitlogic.ultraesb.api.ConfigurationElement;
import org.adroitlogic.ultraesb.api.ConfigurationWatcher;
import org.adroitlogic.ultraesb.api.FileCache;
import org.adroitlogic.ultraesb.cache.CacheManager;
import org.adroitlogic.ultraesb.core.config.AbstractConfigurationElement;
import org.adroitlogic.ultraesb.core.endpoint.Address;
import org.adroitlogic.ultraesb.core.endpoint.Endpoint;
import org.adroitlogic.ultraesb.core.helper.FastInfosetUtils;
import org.adroitlogic.ultraesb.core.helper.JSONUtils;
import org.adroitlogic.ultraesb.core.helper.TransformationUtils;
import org.adroitlogic.ultraesb.core.helper.XMLSupport;
import org.adroitlogic.ultraesb.core.work.SimpleQueueWorkManager;
import org.adroitlogic.ultraesb.core.work.WorkManager;
import org.adroitlogic.ultraesb.transport.TransportListener;
import org.adroitlogic.ultraesb.transport.TransportSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.util.*;

/**
 * The root class that holds the complete UltraESB configuration loaded from one or more configuration files
 *
 */
public class ConfigurationImpl implements org.adroitlogic.ultraesb.api.Configuration,
    ApplicationContextAware, ApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationImpl.class);

    public static final String VERSION = "1.8.0-SNAPSHOT (GA)";
    public static final String PRODUCT = "UltraESB/" + VERSION;
    public static final String PRODUCT_DESC = "AdroitLogic (http://adroitlogic.org) - " + PRODUCT;
    public static final String COPYRIGHT = PRODUCT + " (c) 2010-2012 AdroitLogic. All Rights Reserved";

    /** The [Spring] bean ID of the FileCache bean */
    public static final String FILECACHE_KEY = "fileCache";
    /** The root application context - this cannot be unloaded. Contents can only be managed via JMX */
    private ApplicationContext rootContext;
    /** The list of dynamically loaded sub-context file names */
    private List<String> dynamicSubContexts = null;
    /** Enable dynamic sub contexts to contain custom Spring beans */
    private boolean dynamicSpringBeansEnabled = false;
    /** Child contexts which maybe unloaded and re-loaded at runtime */
    private Map<String, GenericApplicationContext> childContexts = new HashMap<String, GenericApplicationContext>();
    /** A map of configuration elements by their id's - includes elements of root and child contexts */
    private Map<String, ConfigurationElement> localBeanMap = new HashMap<String, ConfigurationElement>();
    /** Child contexts that have been outdated by newer versions of the same configuration */
    private Map<String, List<GenericApplicationContext>> outdatedContexts = new HashMap<String, List<GenericApplicationContext>>();

    /** The transport senders by id */
    private Map<String, TransportSender> trpSenders = new HashMap<String, TransportSender>();
    /** The default work manager */
    private WorkManager defaultWorkManager;
    /** The default response endpoint */
    private Endpoint defaultResponseEndpoint;
    /** The cache manager */
    private CacheManager cacheManager;

    // -- performance tuning options --
    private static final boolean unitTestMode = Boolean.getBoolean("unittest");
    //XML Support
    /** Number of DOM L3 LS Parser instances to use */
    private int parserCount = unitTestMode ? 10 : 2048;
    /** Number of DOM L3 LS Serializer instances to use */
    private int serializerCount = unitTestMode ? 10 : 2048;
    /** Number XPath instances to use */
    private int xPathCount = unitTestMode ? 10 : 2048;
    /** Number of XSLT instances to use */
    private int transformerCount = unitTestMode ? 10 : 2048;
    /** Should XML/XSLT secure processing be enabled? - default true*/
    private boolean secureProcessingEnabled = true;
    // Crypto Support
    /** Maximum size of the Cipher cache - per cipher algorithm instance */
    private int cipherCacheMax = unitTestMode ? 10 : 2048;
    /** Maximum size of the XMLCipher cache */
    private int xmlCipherCacheMax = unitTestMode ? 10 : 2048;
    /** Maximum size of the KeyGenerator cache */
    private int keyGeneratorCacheMax = unitTestMode ? 10 : 2048;
    /** List of configuration watchers to be notified on configuration change */
    private List<ConfigurationWatcher> configurationWatchers;

    private boolean contextBeingDestroyed = false;

    /** Thread local to keep track of calling sequences */
    static ThreadLocal currentSubContext = new ThreadLocal();

    public ConfigurationImpl() {}

    private FileCache getFileCache() {
        Object o = getBean(FILECACHE_KEY);
        if (o instanceof FileCache) {
            return (FileCache) o;
        } else {
            Map fcMap = rootContext.getBeansOfType(FileCache.class);
            if (fcMap != null && !fcMap.isEmpty()) {
                return (FileCache) fcMap.values().iterator().next();
            }
        }
        return null;
    }

    /**
     * Get the default work manager
     * @return default work manager
     */
    public WorkManager getDefaultWorkManager() {
        return defaultWorkManager;
    }

    /**
     * Get the default response endpoint
     * @return default response endpoint
     */
    public Endpoint getDefaultResponseEndpoint() {
        return defaultResponseEndpoint;
    }

    /**
     * Set the default work manager
     * @param defaultWorkManager work manager to set as default
     */
    public void setDefaultWorkManager(WorkManager defaultWorkManager) {
        this.defaultWorkManager = defaultWorkManager;
    }

    /**
     * Set the cache manager
     * @param cacheManager cache manager to set
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get the cache manager
     * @return cache manager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Get all work managers
     * @return all work managers
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, WorkManager> getWorkManagers() {
        return rootContext.getBeansOfType(WorkManager.class);
    }

    /**
     * Get all file caches (only one should exist though)
     * @return all file caches
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, FileCache> getFileCaches() {
        return rootContext.getBeansOfType(FileCache.class);
    }

    /**
     * Return the WorkManager with the given id
     * @param id WorkManager id
     * @return corresponding WorkManager
     */
    public WorkManager getWorkManager(String id) {
        ConfigurationElement o = localBeanMap.get(id);
        if (o != null && o instanceof WorkManager) {
            return (WorkManager) o;
        }
        return getAppContextBean(null, id, WorkManager.class);
    }

    /**
     * Register a new transport sender
     * @param transport sender id
     * @param trpSender sender instance
     */
    public void registerTransportSender(String transport, TransportSender trpSender) {
        this.trpSenders.put(transport, trpSender);
    }

    /**
     * Return the transport sender identified by the given id
     * @param transport id of the sender
     * @return the corresponding transport sender instance
     */
    public TransportSender getTransportSender(String transport) {
        return this.trpSenders.get(transport);
    }

    /**
     * Remove the transport sender identified with the given id
     * @param transport the id of the sender to remove
     */
    public void removeTransportSender(String transport) {
        this.trpSenders.remove(transport);
    }

    /**
     * Get all transport senders
     * @return all transport senders
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, TransportSender> getTransportSenders() {
        return rootContext.getBeansOfType(TransportSender.class);
    }

    /**
     * Get all transport listeners
     * @return all transport listeners
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, TransportListener> getTransportListeners() {
        return rootContext.getBeansOfType(TransportListener.class);
    }

    /**
     * Return the TransportListener with the given id
     * @param elem an optional child configuration app context to use or null
     * @param id TransportListener id
     * @return corresponding TransportListener
     */
    public TransportListener getTransportListener(AbstractConfigurationElement elem, String id) {
        ConfigurationElement o = localBeanMap.get(id);
        if (o != null && o instanceof TransportListener) {
            return (TransportListener) o;
        }
        return getAppContextBean(elem.getAppCtx(), id, TransportListener.class);
    }

    /**
     * Return the Endpoint with the given id
     * @param elem an optional child configuration app context to use or null
     * @param id endpoint id
     * @return corresponding Endpoint
     */
    public Endpoint getEndpoint(AbstractConfigurationElement elem, String id) {
        ConfigurationElement o = localBeanMap.get(id);
        if (o != null && o instanceof Endpoint) {
            return (Endpoint) o;
        }
        return getAppContextBean(elem.getAppCtx(), id, Endpoint.class);
    }

    /**
     * Return the Proxy Service with the given id
     * @param elem an optional child configuration app context to use or null
     * @param id the proxy service id
     * @return corresponding ProxyService
     */
    public ProxyService getProxyService(AbstractConfigurationElement elem, String id) {
        ConfigurationElement o = localBeanMap.get(id);
        if (o != null && o instanceof ProxyService) {
            return (ProxyService) o;
        }
        return getAppContextBean(elem.getAppCtx(), id, ProxyService.class);
    }

    /**
     * Return the list of Proxy service IDs
     * @return a List of proxy service IDs
     */
    public List<String> getProxyServiceIDList() {
        List<String> list = new ArrayList<String>();
        for (Map.Entry<String, ConfigurationElement> e : localBeanMap.entrySet()) {
            if (e.getValue() instanceof ProxyService) {
                list.add(e.getKey());
            }
        }
        return list;
    }

    /**
     * Return the Sequence with the given id
     * @param elem an optional child configuration app context to use or null
     * @param id sequence id
     * @return corresponding Sequence
     */
    public Sequence getSequence(AbstractConfigurationElement elem, String id) {
        ConfigurationElement o = localBeanMap.get(id);
        if (o != null && o instanceof Sequence) {
            return (Sequence) o;
        }
        return getAppContextBean(elem.getAppCtx(), id, Sequence.class);
    }

    /**
     * Get the bean with the given id and type from the Spring application context/s
     * @param id bean id to fetch
     * @param clazz the class of the requested bean
     * @return corresponding bean
     */
    private <T extends Object> T getAppContextBean(GenericApplicationContext ctx, String id, Class<? extends T> clazz) {
        Object o = ctx == null ? null : ctx.getBean(id, clazz);
        if (o == null) {
            o = rootContext.getBean(id, clazz);
        }
        return (T) o;
    }

    /**
     * Get the bean with the given id from the Spring application context
     * @param id bean id to fetch
     * @return corresponding bean
     */
    private Object internalGetBean(String id) {
        ApplicationContext currentContext = (ApplicationContext) ConfigurationImpl.currentSubContext.get();
        if (currentContext != null && currentContext.containsLocalBean(id)) {
            return currentContext.getBean(id);
        } else if (rootContext.containsBean(id)) {
            return rootContext.getBean(id);
        }
        return null;
    }

    /**
     * Get a bean from the Spring configuration with the given id
     * @param id bean id
     * @return corresponding bean
     */
    public Object getBean(String id) {
        if (localBeanMap.containsKey(id)) {
            return localBeanMap.get(id);
        } else {
            return internalGetBean(id);
        }
    }

    /**
     * Get the Spring bean with the given ID and type
     * @param id the bean id
     * @param clazz the class of the bean
     * @param <T>
     * @return the Spring bean with the given information
     */
    public <T extends Object> T getSpringBean(String id, Class<? extends T> clazz) {

        ApplicationContext currentContext = (ApplicationContext) ConfigurationImpl.currentSubContext.get();
        if (currentContext != null && currentContext.containsLocalBean(id)) {
            return (T) currentContext.getBean(id, clazz);
        } else if (rootContext.containsBean(id)) {
            return (T) rootContext.getBean(id, clazz);
        }
        return null;
    }

    /**
     * Get the id of the only Spring bean for the given type
     * @param clazz the class of the bean
     * @return the Spring bean name for the given type
     */
    public String getTheSpringBeanIdForType(Class clazz) {
        String[] names = rootContext.getBeanNamesForType(clazz);
        if (names.length == 1) {
            return names[0];
        }
        return null;
    }

    /**
     * Define a new bean to the configuration with the given id
     * @param id the bean id
     * @param bean bean instance
     */
    public void defineConfigurationBean(String id, ConfigurationElement bean) {
        localBeanMap.put(id, bean);
    }

    /**
     * Unregister bean (e.g. service, sequence, endpoint) from local bean map
     * @param id the bean id to be unregistered
     */
    public void removeConfigurationBean(String id) {
        localBeanMap.remove(id);
    }

    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.rootContext = appContext;
    }

    /**
     * Unload an executing configuration subset (usually one or more proxy services)
     * @param fileName the filename that contained the subset of the configuration to unload
     * @return true if config was unloaded successfully
     */
    public synchronized boolean unloadConfigFile(String fileName) {

        GenericApplicationContext ctx = childContexts.get(fileName);
        if (ctx != null) {
            try {
                ServerManager.getInstance().stopDynamicElements(ctx, false);
                ctx.close();
                childContexts.remove(fileName);
                logger.info("Unloaded live configuration : {}", fileName);
            } catch (Exception e) {
                throwIllegalArgumentException("Error unloading live configuration", e);
            }
        }
        return true;
    }

    /**
     * Unload all child configurations before a shutdown
     * @return true if call was completed successfully
     */
    public synchronized boolean unloadAllChildConfigs() {

        boolean noErrorsEncountered = true;

        // to prevent a concurrent modification exception in the next loop
        List<String> toRemove = new ArrayList<String>();
        for (String fileName : childContexts.keySet()) {
            toRemove.add(fileName);
        }

        for (String fileName : toRemove) {
            if (!unloadConfigFile(fileName)) {
                noErrorsEncountered = false;
            }
        }
        return noErrorsEncountered;
    }

    /**
     * purge all outdated child configurations
     * @return true if call was completed successfully
     */
    public synchronized boolean purgeAllOutdatedConfigs() {

        boolean noErrorsEncountered = true;

        for (Map.Entry<String, List<GenericApplicationContext>> child : outdatedContexts.entrySet()) {
            logger.info("Purging outdated versions of configuration : {}", child.getKey());
            for (GenericApplicationContext ctx : child.getValue()) {
                try {
                    ServerManager.getInstance().stopDynamicElements(ctx, true);
                } catch (Exception e) {
                    logger.warn("Error while purging an outdated version of child configuration : {}", child.getKey(), e);
                    noErrorsEncountered = false;
                } finally {
                    try { ctx.close(); } catch (Exception ignore) {}
                }
            }
        }
        outdatedContexts.clear();
        return noErrorsEncountered;
    }

    /**
     * Add configuration fragment or Switch existing version to a new version
     * @param filePath the file path for the configuration fragment
     */
    public synchronized void addOrUpdateConfigFromFile(String filePath) {

        GenericApplicationContext newContext  = prepareConfigFromFile(filePath);
        GenericApplicationContext prevContext = childContexts.get(filePath);

        boolean encounteredFaults = false;
        try {
            ServerManager.getInstance().startDynamicElements(newContext);
            childContexts.put(filePath, newContext);

            // if an outdated config now remains, keep it within the outdated map
            if (prevContext != null) {
                logger.info("Marking previous version of : {} as outdated", filePath);

                List<GenericApplicationContext> ctxList = outdatedContexts.get(filePath);
                if (ctxList == null) {
                    ctxList = new ArrayList<GenericApplicationContext>();
                    outdatedContexts.put(filePath, ctxList);
                }
                ctxList.add(prevContext);

                // mark all old proxies as outdated
                Map<String, ProxyService> oldServices = prevContext.getBeansOfType(ProxyService.class);
                for (ProxyService ps : oldServices.values()) {
                    ps.markAsOutdated();
                }
                Map<String, ProxyService> newServices = newContext.getBeansOfType(ProxyService.class);

                // Stop sending anymore messages to removed proxy services
                oldServices.keySet().removeAll(newServices.keySet());
                for (ProxyService ps : oldServices.values()) {
                    if (!ps.unregisterFromTransports()) {
                        encounteredFaults = true;
                    } else {
                        logger.info("Marked previous version of proxy : {} as outdated by transports", ps.getId());
                    }
                }

                // unregister unwanted detailed MXBeans etc
                ServerManager.getInstance().outdateConfiguration(prevContext, newContext);
            }

            if (encounteredFaults) {
                logger.warn("Configuration switch for : {} encountered faults - check log for details", filePath);
            } else {
                logger.info("Successfully switched configuration : {}", filePath);
            }

        } catch (Exception e) {
            throwIllegalArgumentException("Error switching configuration : " +  filePath, e);
        }
    }

    /**
     * Prepares a configuration fragment from the given file by compiling sequences and services
     * @param filePath the file path for the configuration fragment
     * @return the prepared GenericApplicationContext
     */
    private GenericApplicationContext prepareConfigFromFile(String filePath) {

        GenericApplicationContext ctx = new GenericApplicationContext(rootContext);

        try {
            XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
            xmlReader.loadBeanDefinitions(new FileSystemResource(filePath));
            ctx.refresh();
            ctx.setDisplayName(filePath);

            // check duplicates and unsupported options
            for (String beanName : ctx.getBeanDefinitionNames()) {

                final Object o = ctx.getBean(beanName);
                if ((o instanceof ProxyService) || (o instanceof Sequence) || (o instanceof Endpoint)) {
                    // duplicates are not allowed
                    if (rootContext.containsBean(beanName)) {
                        throwIllegalArgumentException(
                            "A bean with id : " + beanName + " defined by configuration : " + filePath +
                                " is already defined in the root configuration");
                    }
                    for (GenericApplicationContext child : childContexts.values()) {
                        if (child.containsLocalBean(beanName) && !filePath.equals(child.getDisplayName())) {
                            throwIllegalArgumentException(
                                "A bean with id : " + beanName + " defined by configuration : " + filePath +
                                    " is already defined by child configuration : " + child.getDisplayName());
                        }
                    }

                } else if (!dynamicSpringBeansEnabled) {
                    throwIllegalArgumentException("Configuration : " + filePath +
                        " defines an un-supported bean (i.e. generic Spring bean) : " + beanName +
                        "; use 'dynamicSpringBeansEnabled' property on the configuration definition if applicable");
                }
            }

            // Start sequences of the child
            ServerManager.getInstance().prepareSequences(ctx);
            logger.info("Successfully prepared configuration : " + filePath);
            return ctx;

        } catch (Exception e) {
            try { ctx.close(); } catch (Exception ignore) {}
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            } else {
                logger.error("Error preparing configuration : {}", filePath, e);
                throw new IllegalArgumentException("Error preparing configuration : " + filePath + " : " + e.getMessage());
            }
        }
    }

    /**
     * Is a child configuration for this path already loaded?
     * @param filePath configuration path
     * @return true if loaded
     */
    public boolean isConfigLoaded(String filePath) {
        return childContexts.containsKey(filePath);
    }

    // -------- lifecycle methods -----------
    /**
     * This is the core point of entry for initialization of the UltraESB
     * This is invoked as the Spring ApplicationContext initializes
     */
    public void onApplicationEvent(ApplicationEvent event) {

        // if this is a notification for a child context, ignore
        if (event.getSource() instanceof GenericApplicationContext &&
            ((GenericApplicationContext) event.getSource()).getParent() != null) {
            return;
        }

        if (event instanceof ContextRefreshedEvent) {
            if (!LicenseManager.getInstance().validate(null)) {
                logger.error("License validation failed");
                return;
            }

            if (!LicenseManager.LICENSE_CHECK_ACTIVE) {
                 logger.info("Starting {} - {}", PRODUCT, ServerManager.getInstance().getServerName());
                 logger.info("****************************************************************************");
                 logger.info("***  Copyright (C) 2010-11 AdroitLogic Private Ltd. All Rights Reserved  ***");
                 logger.info("***                                                                      ***");
                 logger.info("***         Licensed under the GNU Affero General Public License         ***");
                 logger.info("***   See LICENSE-AGPL.TXT or http://www.gnu.org/licenses/agpl-3.0.html  ***");
                 logger.info("****************************************************************************");
            } else {
                logger.info("Starting {} - {}", COPYRIGHT, ServerManager.getInstance().getServerName());
                logger.info("Server name : " + ServerManager.getInstance().getServerName());
            }

            // pre-initialization
            logger.info("Pre-initialization of the engine..");
            XMLSupport.initializeInstance(parserCount, serializerCount, xPathCount, transformerCount, secureProcessingEnabled);
            CryptoSupport.initializeInstance(cipherCacheMax, xmlCipherCacheMax, keyGeneratorCacheMax);
            TransformationUtils.initializeInstance(getFileCache());
            try {
                if (Class.forName("org.codehaus.jackson.map.ObjectMapper") != null) {
                    JSONUtils.initializeInstance(getFileCache());
                }
            } catch (Exception ignore) {}
            try {
                if (Class.forName("com.sun.xml.fastinfoset.sax.SAXDocumentSerializer") != null &&
                    Class.forName("org.jvnet.fastinfoset.FastInfosetSource") != null) {
                    FastInfosetUtils.initializeInstance(getFileCache());
                }
            } catch (Exception ignore) {}

            // initialize mediation and create a default work manager if none-exist
            MediationImpl.initialize(this);
            if (defaultWorkManager == null) {
                defaultWorkManager = new SimpleQueueWorkManager(this);
            }

            // define the default response endpoint
            defaultResponseEndpoint = new Endpoint();
            defaultResponseEndpoint.setId(ConfigurationConstants.MEDIATION_RESPONSE_ENDPOINT_NAME);
            List<Address> addressList = new ArrayList<Address>();
            Address responseAddr = new Address();
            responseAddr.setAddressType(Address.AddressType.RESPONSE);
            addressList.add(responseAddr);
            defaultResponseEndpoint.setAddressList(addressList);

            ServerManager.getInstance().initialize(rootContext, this);

            if (!ServerManager.getInstance().isFailed()) {
                // start dynamic sub-contexts
                try {
                    if (dynamicSubContexts != null) {
                        logger.info("Starting dynamic sub-contexts..");
                        for (String file : dynamicSubContexts) {
                            addOrUpdateConfigFromFile(file);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Configuration error detected, stopping the UltraESB!", e);
                    ServerManager.getInstance().stop();
                    System.exit(1);
                }

                // let the messages begin to flow..
                ServerManager.getInstance().startTransportListeners();

                logger.info("{} - {} started with root configuration..", PRODUCT,
                    ServerManager.getInstance().getServerName());
            }

        } else if (event instanceof ContextClosedEvent) {
            destroy();
        }
    }

    /**
     * Sets the dynamically loaded sub-context file names
     * @param dynamicSubContexts list of file names
     */
    public void setDynamicSubContexts(List<String> dynamicSubContexts) {
        this.dynamicSubContexts = dynamicSubContexts;
    }

    /**
     * Destroy the configuration - and thus the server instance
     */
    @SuppressWarnings({"unchecked"})
    public void destroy() {
        if (ServerManager.getInstance().isStarted()) {
            logger.info("ApplicationContext shutdown triggered");
            contextBeingDestroyed = true;
            ServerManager.getInstance().shutdown(1000);
        }
    }

    public void destroyContext() {
        if (!contextBeingDestroyed && rootContext instanceof GenericApplicationContext) {
            ((GenericApplicationContext) rootContext).destroy();
        }
        rootContext = null;
    }

    public List<String> getLoadedChildContexts() {
        List<String> list = new ArrayList<String>(childContexts.size());
        for (String key : childContexts.keySet()) {
            list.add(key);
        }
        return list;
    }

    public boolean isUnitTestMode() {
        return unitTestMode;
    }

    // setters for Spring configurable properties

    /**
     * Tuning parameter - Enable or disable XML and XSLT Secure processing, to safeguard the ESB against possible attacks
     * @param secureProcessingEnabled will enable XML and XSLT secure processing if true (defaults to true)
     */
    public void setSecureProcessingEnabled(boolean secureProcessingEnabled) {
        this.secureProcessingEnabled = secureProcessingEnabled;
    }

    /**
     * Tuning parameter - The number of XSL transformer instances to cache and re-use
     * @param transformerCount XSL instances (default 2048)
     */
    public void setTransformerCount(int transformerCount) {
        this.transformerCount = transformerCount;
    }

    /**
     * Tuning parameter - The number of XPath instances to cache and re-use
     * @param xPathCount XPath instances (default 2048)
     */
    public void setxPathCount(int xPathCount) {
        this.xPathCount = xPathCount;
    }

    /**
     * Tuning parameter - The number of XML serializer instances to cache and re-use
     * @param serializerCount XML serializer instances (default 2048)
     */
    public void setSerializerCount(int serializerCount) {
        this.serializerCount = serializerCount;
    }

    /**
     * Tuning parameter - The number of XML parser instances to cache and re-use
     * @param parserCount XML parser instances (default 2048)
     */
    public void setParserCount(int parserCount) {
        this.parserCount = parserCount;
    }

    /**
     * Tuning parameter - The number of Cipher instances to cache and re-use
     * @param cipherCacheMax Cipher instances (related to WS-Security processing)
     */
    public void setCipherCacheMax(int cipherCacheMax) {
        this.cipherCacheMax = cipherCacheMax;
    }

    /**
     * Tuning parameter - The number of XMLCipher instances to cache and re-use
     * @param xmlCipherCacheMax XMLCipher instances (related to WS-Security processing)
     */
    public void setXmlCipherCacheMax(int xmlCipherCacheMax) {
        this.xmlCipherCacheMax = xmlCipherCacheMax;
    }

    /**
     * Tuning parameter - The number of KeyGenerator instances to cache and re-use
     * @param keyGeneratorCacheMax KeyGenerator instances (related to WS-Security processing)
     */
    public void setKeyGeneratorCacheMax(int keyGeneratorCacheMax) {
        this.keyGeneratorCacheMax = keyGeneratorCacheMax;
    }

    public List<ConfigurationWatcher> getConfigurationWatchers() {
        return configurationWatchers;
    }

    public void setConfigurationWatchers(List<ConfigurationWatcher> configurationWatchers) {
        this.configurationWatchers = configurationWatchers;
    }

    public void setDynamicSpringBeansEnabled(boolean dynamicSpringBeansEnabled) {
        this.dynamicSpringBeansEnabled = dynamicSpringBeansEnabled;
    }

    private void throwIllegalArgumentException(String msg) {
        logger.error(msg);
        throw new IllegalArgumentException(msg);
    }

    private void throwIllegalArgumentException(String msg, Exception e) {
        logger.error(msg, e);
        throw new IllegalArgumentException(msg, e);
    }

}

