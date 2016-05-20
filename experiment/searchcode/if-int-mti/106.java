/*
 * Copyright (c) 2010-2012 The Amdatu Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.amdatu.deployment.autoconf;

import static org.osgi.service.deploymentadmin.spi.ResourceProcessorException.CODE_OTHER_ERROR;
import static org.osgi.service.deploymentadmin.spi.ResourceProcessorException.CODE_RESOURCE_SHARING_VIOLATION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.felix.deployment.rp.autoconf.AutoConfResource;
import org.apache.felix.deployment.rp.autoconf.ObjectClassDefinitionImpl;
import org.apache.felix.deployment.rp.autoconf.PersistencyManager;
import org.apache.felix.metatype.Attribute;
import org.apache.felix.metatype.Designate;
import org.apache.felix.metatype.MetaData;
import org.apache.felix.metatype.MetaDataReader;
import org.apache.felix.metatype.OCD;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.deploymentadmin.spi.DeploymentSession;
import org.osgi.service.deploymentadmin.spi.ResourceProcessor;
import org.osgi.service.deploymentadmin.spi.ResourceProcessorException;
import org.osgi.service.log.LogService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.xmlpull.v1.XmlPullParserException;

/**
 * OSGI autoconf processor based on the implementation from Apache Felix, with several bugfixes and two
 * important additional features.
 * 
 * 1) Deferred delivery allowing that supports the fact that ConfigurationAdmin services may show up
 * late due to async behavior.
 * 
 * 2) Bundle-Location wildcard support in Metadata supporting use cases where the bundle location is
 * unknown or irrelevant such as fileinstall and integration tests.
 * 
 * Note on implementation:
 * 
 * interface methods to not check or protect transaction state in a strict way because the spec demands
 * only one client (DeploymentAdmin) act at any one time.
 * 
 * TODO the scheduled tasks queue is not persistent so it wont survive a restart
 * TODO subsequent updates should be able to override outstanding scheduled tasks
 * TODO the ORIGINAL_PID_KEY construct should use alias according to spec?
 * TODO high concurrent stress tests with out-of-order delivery
 * TODO check & test bundle location stuff to spec
 * TODO clean code copied from felix
 * 
 * @author <a href="mailto:amdatu-developers@amdatu.org">Amdatu Project Team</a>
 * 
 */
public final class AutoConfResourceProcessor implements ResourceProcessor {

    public static final String ORIGINAL_PID_KEY = "org.apache.felix.fileinstall.metatype.orgPid";
    public static final String CONFIGURATION_ADMIN_FILTER_ATTRIBUTE = "filter";
    public static final String DP_LOCATION_PREFIX = "osgi-dp:";

    // injected by dependency management
    private volatile BundleContext m_bundleContext;
    private volatile MetaTypeService m_metaTypeService;
    private volatile LogService m_logService;

    // session scoped state
    private volatile DeploymentSession m_session = null;
    private final Map<String, List<AutoConfResource>> m_toBeInstalled = new HashMap<String, List<AutoConfResource>>();
    private final Map<String, List<AutoConfResource>> m_toBeDeleted = new HashMap<String, List<AutoConfResource>>();
    private final List<ConfigurationResourcesTask> m_updateTasks =
        new LinkedList<ConfigurationResourcesTask>();

    // service scoped state
    private final Map<ServiceReference, ConfigurationAdmin> m_configurationAdmins =
        new HashMap<ServiceReference, ConfigurationAdmin>();
    private final List<ConfigurationResourcesTask> m_scheduledTasks =
        new LinkedList<ConfigurationResourcesTask>();

    private PersistencyManager m_persistencyManager;

    public void start() throws IOException {
        File root = m_bundleContext.getDataFile("");
        if (root == null) {
            throw new IOException("No file system support");
        }
        m_persistencyManager = new PersistencyManager(root);
    }

    public void addConfigurationAdmin(ServiceReference reference, ConfigurationAdmin admin) {
        synchronized (m_configurationAdmins) {
            m_configurationAdmins.put(reference, admin);
        }
        runConfigurationResourcesTasks();
    }

    public void removeConfigurationAdmin(ServiceReference reference, ConfigurationAdmin admin) {
        synchronized (m_configurationAdmins) {
            m_configurationAdmins.remove(reference);
        }
    }

    /**
     * @see ResourceProcessor#begin(org.osgi.service.deploymentadmin.spi.DeploymentSession)
     */
    public void begin(DeploymentSession session) {
        if (m_session != null) {
            throw new IllegalStateException(
                "Can't start a new session when there is one in progress. Commit or rollack the active "
                    + "transaction first!");
        }
        m_session = session;
        m_toBeInstalled.clear();
        m_toBeDeleted.clear();
        m_updateTasks.clear();
    }

    /**
     * @see ResourceProcessor#process(java.lang.String, java.io.InputStream)
     */
    public void process(String name, InputStream stream) throws ResourceProcessorException {
        if (m_session == null) {
            throw new ResourceProcessorException(CODE_OTHER_ERROR,
                "Can not process resource without a Deployment Session");
        }

        if (m_toBeInstalled.containsKey(name) || m_toBeDeleted.containsKey(name)) {
            // TODO check: Do we throw an exception or do we override
            throw new ResourceProcessorException(CODE_RESOURCE_SHARING_VIOLATION,
                "Duplicate resource within one session is not allowed " + name);
        }

        MetaData data = parseMetaData(stream);
        if (data == null) {
            throw new ResourceProcessorException(CODE_OTHER_ERROR,
                "Supplied configuration is not conform the metatype xml specification.");
        }

        Map<String, Designate> designates = data.getDesignates();
        if (designates == null) {
            m_logService
                .log(LogService.LOG_INFO, "No designates found in the resource, so there's nothing to process.");
            return;
        }

        String filter = getMetaDataFilter(data);

        Map<String, OCD> localOcds = data.getObjectClassDefinitions();

        // process designates
        List<AutoConfResource> installList = new LinkedList<AutoConfResource>();
        for (Designate designate : designates.values()) {

            // check bundle location
            String bundle = designate.getBundleLocation();
            if (bundle == null || !(bundle.equals("*") || bundle.startsWith(DP_LOCATION_PREFIX))) {
                throw new ResourceProcessorException(CODE_OTHER_ERROR,
                    "Designate bundle location attribute missing or invalid");
            }

            // check object
            if (designate.getObject() == null) {
                throw new ResourceProcessorException(CODE_OTHER_ERROR,
                    "Designate Object child missing or invalid");
            }
            // check ocdRef
            String ocdRef = designate.getObject().getOcdRef();
            if (ocdRef == null || "".equals(ocdRef)) {
                throw new ResourceProcessorException(CODE_OTHER_ERROR,
                    "Object ocdRef attribute missing or invalid");
            }

            // check attributes
            if (designate.getObject().getAttributes() == null || designate.getObject().getAttributes().size() == 0) {
                throw new ResourceProcessorException(CODE_OTHER_ERROR,
                    "Object Attributes child missing or invalid");
            }

            // determine objectclass definition
            OCD localOcd = null;
            ObjectClassDefinition ocd = null;
            if (localOcds != null) {
                localOcd = localOcds.get(ocdRef);
            }
            if (localOcd != null) {
                ocd = new ObjectClassDefinitionImpl(localOcd);
            }
            else {
                ocd = getMetaTypeOCD(designate);
            }
            if (ocd == null) {
                throw new ResourceProcessorException(CODE_OTHER_ERROR,
                    "No Object Class Definition found with id=" + ocdRef);
            }

            // get properties
            Dictionary dict = getProperties(designate, ocd);
            if (dict == null) {
                continue;
            }

            installList.add(new AutoConfResource(name, designate.getPid(), designate.getFactoryPid(), designate
                .getBundleLocation(), designate.isMerge(), dict, filter));
        }

        // commit state
        m_toBeInstalled.put(name, installList);
    }

    /**
     * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#dropped(java.lang.String)
     */
    public void dropped(String name) throws ResourceProcessorException {
        if (m_session == null) {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                "Can not process resource without a Deployment Session");
        }

        try {
            List resources = m_persistencyManager.load(name);
            if (!m_toBeDeleted.containsKey(name)) {
                m_toBeDeleted.put(name, resources);
            }
        }
        catch (IOException ioe) {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                "Unable to drop resource: " + name, ioe);
        }
    }

    /**
     * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#dropAllResources()
     */
    public void dropAllResources() throws ResourceProcessorException {
        if (m_session == null) {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                "Can not drop all resources without a Deployment Session");
        }

        try {
            Map loadAll = m_persistencyManager.loadAll();
            for (Iterator i = loadAll.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                dropped(name);
            }
        }
        catch (IOException ioe) {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                "Unable to drop all resources.", ioe);
        }

        // TODO check this
        File basedir = m_bundleContext.getDataFile("");
        if (basedir != null && basedir.isDirectory()) {
            String[] files = basedir.list();
            for (int i = 0; i < files.length; i++) {
                dropped(files[i]);
            }
        }
        else {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                "Unable to drop resources, data area is not accessible");
        }
    }

    /**
     * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#prepare()
     */
    public void prepare() throws ResourceProcessorException {
        if (m_session == null) {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                "Can not process resource without a Deployment Session");
        }

        List<ConfigurationResourcesTask> updateList = new LinkedList<ConfigurationResourcesTask>();
        try {
            for (Entry<String, List<AutoConfResource>> entry : m_toBeDeleted.entrySet()) {
                String name = entry.getKey();
                List<AutoConfResource> resources = entry.getValue();
                for (AutoConfResource resource : resources) {
                    updateList.add(new DropConfigurationResourceTask(resource));
                }
                // TODO delay actual delete to commit
                m_persistencyManager.delete(name);
            }

            for (Entry<String, List<AutoConfResource>> entry : m_toBeInstalled.entrySet()) {
                String name = entry.getKey();
                List<AutoConfResource> resources = entry.getValue();
                List<AutoConfResource> existingResources = m_persistencyManager.load(name);

                for (AutoConfResource resource : resources) {
                    AutoConfResource updatesExisting = null;
                    for (AutoConfResource existingResource : existingResources) {
                        if (resource.equalsTargetConfiguration(existingResource)) {
                            updatesExisting = existingResource;
                        }
                    }
                    if (updatesExisting != null) {
                        existingResources.remove(updatesExisting);
                    }
                    resource.setGeneratedPid(resource.getPid());
                    updateList.add(new InstallorUpdateConfigurationResourceTask(resource));
                }

                for (AutoConfResource resource : existingResources) {
                    updateList.add(new DropConfigurationResourceTask(resource));
                }
                // TODO delay actual delete to commit
                m_persistencyManager.store(name, resources);
            }
        }
        catch (Exception ioe) {
            throw new ResourceProcessorException(ResourceProcessorException.CODE_PREPARE,
                "Unable to prepare for commit for resource", ioe);
        }

        // commit to session
        // TODO check: this means nothing gets done if one resource has an error. I think thats correct
        m_updateTasks.addAll(updateList);
    }

    /**
     * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#commit()
     */
    public void commit() {
        if (m_session == null) {
            m_logService.log(LogService.LOG_ERROR, "Commit without a session can't happen. Aborting!");
            rollback();
        }

        // commit tasks
        synchronized (m_scheduledTasks) {
            m_scheduledTasks.addAll(m_updateTasks);
        }

        // clear session
        rollback();
        runConfigurationResourcesTasks();
    }

    /**
     * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#rollback()
     */
    public void rollback() {

        // TODO check: does this make any sense?
        Set keys = m_toBeInstalled.keySet();
        for (Iterator i = keys.iterator(); i.hasNext();) {
            List configs = m_toBeInstalled.get(i.next());
            for (Iterator j = configs.iterator(); j.hasNext();) {
                AutoConfResource resource = (AutoConfResource) j.next();
                String name = resource.getName();
                try {
                    dropped(name);
                }
                catch (ResourceProcessorException e) {
                    m_logService.log(LogService.LOG_ERROR,
                        "Unable to roll back resource '" + name + "', reason: " + e.getMessage() + ", caused by: "
                            + e.getCause().getMessage());
                }
                break;
            }
        }
        m_toBeInstalled.clear();
        m_toBeDeleted.clear();
        m_updateTasks.clear();
        m_session = null;
    }

    /**
     * @see org.osgi.service.deploymentadmin.spi.ResourceProcessor#cancel()
     */
    public synchronized void cancel() {
        rollback();
    }

    /**
     * Gets the optional filter attribute and checks if is a valid filter.
     */
    private String getMetaDataFilter(MetaData data) throws ResourceProcessorException {
        String filterString = null;
        Map optionalAttributes = data.getOptionalAttributes();
        if (optionalAttributes != null) {
            filterString = (String) optionalAttributes.get(CONFIGURATION_ADMIN_FILTER_ATTRIBUTE);
        }
        if (filterString != null) {
            try {
                // just checking here as AutoConfResource doesn't let us pass it on
                FrameworkUtil.createFilter(filterString);
            }
            catch (InvalidSyntaxException e) {
                throw new ResourceProcessorException(CODE_OTHER_ERROR,
                    "Supplied configuration is has an invalid filter attribute.", e);
            }
        }
        return filterString;
    }

    /**
     * Parses the provided MetaData XMl inputstream into an object representation.
     */
    private MetaData parseMetaData(InputStream stream) throws ResourceProcessorException {
        MetaDataReader reader = new MetaDataReader();
        MetaData data = null;
        try {
            data = reader.parse(stream);
        }
        catch (IOException e) {
            throw new ResourceProcessorException(CODE_OTHER_ERROR,
                "Unable to process resource.", e);
        }
        catch (XmlPullParserException e) {
            throw new ResourceProcessorException(CODE_OTHER_ERROR,
                "Supplied configuration is not conform the metatype xml specification.", e);
        }
        return data;
    }

    private void runConfigurationResourcesTasks() {

        List<ConfigurationResourcesTask> toBeRemoved = new LinkedList<ConfigurationResourcesTask>();
        Map<ConfigurationResourcesTask, ConfigurationAdmin> toBeExecuted =
            new HashMap<ConfigurationResourcesTask, ConfigurationAdmin>();

        synchronized (m_configurationAdmins) {
            synchronized (m_scheduledTasks) {
                for (Entry<ServiceReference, ConfigurationAdmin> entry : m_configurationAdmins.entrySet()) {
                    ServiceReference serviceReference = entry.getKey();
                    ConfigurationAdmin configurationAdmin = entry.getValue();
                    for (ConfigurationResourcesTask task : m_scheduledTasks) {
                        if (task.getConfigurationAdminFilter() == null
                            || task.getConfigurationAdminFilter().match(serviceReference)) {
                            toBeExecuted.put(task, configurationAdmin);
                            toBeRemoved.add(task);
                        }
                    }
                }
                m_scheduledTasks.removeAll(toBeRemoved);
            }
        }
        for (Entry<ConfigurationResourcesTask, ConfigurationAdmin> entry : toBeExecuted.entrySet()) {
            ConfigurationResourcesTask task = entry.getKey();
            ConfigurationAdmin configurationAdmin = entry.getValue();
            try {
                task.run(configurationAdmin);
            }
            catch (Exception e) {
                System.err.println("Failed to executed configuration resource task");
                e.printStackTrace();
                m_logService.log(LogService.LOG_ERROR, "Failed to executed configuration resource task", e);
            }
        }
    }

    /**
     * Determines the actual configuration data based on the specified designate and object class definition.
     * 
     * @param designate The designate object containing the values for the properties
     * @param ocd The object class definition
     * @return A dictionary containing data as described in the designate and ocd objects, or <code>null</code> if
     *         the designate does not match it's definition and the designate was marked as optional.
     * @throws ResourceProcessorException If the designate does not match the ocd and the designate is not marked
     *         as optional.
     */
    private Dictionary getProperties(Designate designate, ObjectClassDefinition ocd) throws ResourceProcessorException {
        Dictionary properties = new Hashtable();
        AttributeDefinition[] attributeDefs = ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
        List attributes = designate.getObject().getAttributes();

        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Attribute attribute = (Attribute) i.next();

            String adRef = attribute.getAdRef();
            boolean found = false;
            for (int j = 0; j < attributeDefs.length; j++) {
                AttributeDefinition ad = attributeDefs[j];
                if (adRef.equals(ad.getID())) {
                    // found attribute definition
                    Object value = getValue(attribute, ad);
                    if (value == null) {
                        if (designate.isOptional()) {
                            properties = null;
                            break;
                        }
                        else {
                            throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                                "Could not match attribute to it's definition: adref=" + adRef);
                        }
                    }
                    properties.put(adRef, value);
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (designate.isOptional()) {
                    properties = null;
                    break;
                }
                else {
                    throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR,
                        "Could not find attribute definition: adref=" + adRef);
                }
            }
        }

        return properties;
    }

    /**
     * Determines the object class definition matching the specified designate. We support the
     * wildcard bunlde location to search all bundles.
     * 
     * TODO this is not spec compliant yet (eg. pid should be in deploymentpackage
     */
    private ObjectClassDefinition getMetaTypeOCD(Designate designate) {

        ObjectClassDefinition ocd = null;
        String ocdRef = designate.getObject().getOcdRef();

        String bundleLocation = designate.getBundleLocation();
        boolean allBundles = "*".equals(bundleLocation);

        boolean isFactoryPid = isFactoryConfig(designate);
        String pid = isFactoryPid ? designate.getFactoryPid() : designate.getPid();

        Bundle[] bundles = m_bundleContext.getBundles();
        for (int i = 0; i < bundles.length; i++) {
            if (allBundles || bundleLocation.equals(bundles[i].getLocation())) {
                MetaTypeInformation mti = m_metaTypeService.getMetaTypeInformation(bundles[i]);
                if (mti != null) {
                    String[] pids = isFactoryPid ? mti.getFactoryPids() : mti.getPids();
                    if (Arrays.binarySearch(pids, pid) > -1) {
                        ObjectClassDefinition tempOcd = mti.getObjectClassDefinition(pid, null);
                        if (tempOcd != null && ocdRef.equals(tempOcd.getID())) {
                            ocd = tempOcd;
                            break;
                        }
                    }
                }
            }
        }
        return ocd;
    }

    private boolean isFactoryConfig(Designate designate) {
        String factoryPid = designate.getFactoryPid();
        return (factoryPid != null && !"".equals(factoryPid));
    }

    /**
     * Determines the value of an attribute based on an attribute definition.
     * 
     * @param attribute The attribute containing value(s)
     * @param ad The attribute definition
     * @return An <code>Object</code> reflecting what was specified in the attribute and it's definition
     *         or <code>null</code> if the value did not match it's definition.
     */
    private Object getValue(Attribute attribute, AttributeDefinition ad) {
        if (attribute == null || ad == null || !attribute.getAdRef().equals(ad.getID())) {
            // wrong attribute or definition
            return null;
        }
        String[] content = attribute.getContent();

        // verify correct type of the value(s)
        int type = ad.getType();
        Object[] typedContent = null;
        try {
            for (int i = 0; i < content.length; i++) {
                String value = content[i];
                switch (type) {
                    case AttributeDefinition.BOOLEAN:
                        typedContent = (typedContent == null) ? new Boolean[content.length] : typedContent;
                        typedContent[i] = Boolean.valueOf(value);
                        break;
                    case AttributeDefinition.BYTE:
                        typedContent = (typedContent == null) ? new Byte[content.length] : typedContent;
                        typedContent[i] = Byte.valueOf(value);
                        break;
                    case AttributeDefinition.CHARACTER:
                        typedContent = (typedContent == null) ? new Character[content.length] : typedContent;
                        char[] charArray = value.toCharArray();
                        if (charArray.length == 1) {
                            typedContent[i] = new Character(charArray[0]);
                        }
                        else {
                            return null;
                        }
                        break;
                    case AttributeDefinition.DOUBLE:
                        typedContent = (typedContent == null) ? new Double[content.length] : typedContent;
                        typedContent[i] = Double.valueOf(value);
                        break;
                    case AttributeDefinition.FLOAT:
                        typedContent = (typedContent == null) ? new Float[content.length] : typedContent;
                        typedContent[i] = Float.valueOf(value);
                        break;
                    case AttributeDefinition.INTEGER:
                        typedContent = (typedContent == null) ? new Integer[content.length] : typedContent;
                        typedContent[i] = Integer.valueOf(value);
                        break;
                    case AttributeDefinition.LONG:
                        typedContent = (typedContent == null) ? new Long[content.length] : typedContent;
                        typedContent[i] = Long.valueOf(value);
                        break;
                    case AttributeDefinition.SHORT:
                        typedContent = (typedContent == null) ? new Short[content.length] : typedContent;
                        typedContent[i] = Short.valueOf(value);
                        break;
                    case AttributeDefinition.STRING:
                        typedContent = (typedContent == null) ? new String[content.length] : typedContent;
                        typedContent[i] = value;
                        break;
                    default:
                        // unsupported type
                        return null;
                }
            }
        }
        catch (NumberFormatException nfe) {
            return null;
        }

        // verify cardinality of value(s)
        int cardinality = ad.getCardinality();
        Object result = null;
        if (cardinality == 0) {
            if (typedContent.length == 1) {
                result = typedContent[0];
            }
            else {
                result = null;
            }
        }
        else if (cardinality == Integer.MIN_VALUE) {
            result = new Vector(Arrays.asList(typedContent));
        }
        else if (cardinality == Integer.MAX_VALUE) {
            result = typedContent;
        }
        else if (cardinality < 0) {
            if (typedContent.length <= Math.abs(cardinality)) {
                result = new Vector(Arrays.asList(typedContent));
            }
            else {
                result = null;
            }
        }
        else if (cardinality > 0) {
            if (typedContent.length <= cardinality) {
                result = typedContent;
            }
            else {
                result = null;
            }
        }
        return result;
    }

    interface ConfigurationResourcesTask {
        AutoConfResource getAutoConfResource();

        Filter getConfigurationAdminFilter();

        void run(ConfigurationAdmin admin) throws Exception;
    }

    abstract static class BaseConfigurationResourceTask implements ConfigurationResourcesTask {

        protected final AutoConfResource m_autoConfResource;
        protected final Filter m_configurationAdminFilter;

        public BaseConfigurationResourceTask(AutoConfResource resource) throws ConfigurationException {
            m_autoConfResource = resource;
            if (resource.getFilter() == null || "".equals(resource.getFilter())) {
                m_configurationAdminFilter = null;
            }
            else {
                try {
                    m_configurationAdminFilter = FrameworkUtil.createFilter(resource.getFilter());
                }
                catch (InvalidSyntaxException e) {
                    throw new ConfigurationException("filter",
                        "Unable to parse the provided ConfigurationAdmin filter", e);
                }
            }
        }

        public final Filter getConfigurationAdminFilter() {
            return m_configurationAdminFilter;
        }

        public final AutoConfResource getAutoConfResource() {
            return m_autoConfResource;
        }
    }

    static class DropConfigurationResourceTask extends BaseConfigurationResourceTask {

        public DropConfigurationResourceTask(AutoConfResource resource) throws ConfigurationException {
            super(resource);
        }

        public void run(ConfigurationAdmin configAdmin) throws Exception {
            if (m_autoConfResource.isFactoryConfig()) {

                Configuration configuration = null;
                Configuration[] configurations =
                    configAdmin.listConfigurations("(" + AutoConfResourceProcessor.ORIGINAL_PID_KEY + "="
                        + m_autoConfResource.getPid() + ")");
                if (configurations != null && configurations.length > 0) {
                    configuration = configurations[0];
                }
                if (configuration != null) {
                    configuration.delete();
                }
            }
            else {
                Configuration configuration =
                    configAdmin.getConfiguration(m_autoConfResource.getPid(), m_autoConfResource.getBundleLocation());
                if (configuration != null) {
                    configuration.delete();
                }
            }
        }
    }

    static class InstallorUpdateConfigurationResourceTask extends BaseConfigurationResourceTask {

        public InstallorUpdateConfigurationResourceTask(AutoConfResource resource) throws ConfigurationException {
            super(resource);
        }

        public void run(ConfigurationAdmin configAdmin) throws Exception {

            // TODO check bundle location logic
            String searchBundleLocation = null;
            if (!m_autoConfResource.getBundleLocation().equals("*")) {
                searchBundleLocation = m_autoConfResource.getBundleLocation();
            }

            Configuration configuration = null;
            if (m_autoConfResource.isFactoryConfig()) {
                Configuration[] configurations =
                    configAdmin.listConfigurations("(" + AutoConfResourceProcessor.ORIGINAL_PID_KEY + "="
                        + m_autoConfResource.getPid() + ")");
                if (configurations != null && configurations.length > 0) {
                    configuration = configurations[0];
                }
                if (configuration == null) {
                    configuration =
                        configAdmin.createFactoryConfiguration(m_autoConfResource.getFactoryPid(),
                            searchBundleLocation);
                }
                m_autoConfResource.setGeneratedPid(configuration.getPid());
            }
            else {
                configuration =
                    configAdmin.getConfiguration(m_autoConfResource.getPid(), searchBundleLocation);
                // if (m_resource.getBundleLocation() != configuration.getBundleLocation()) {
                // // an existing configuration exists that is bound to a different location, which is not allowed
                // throw new ResourceProcessorException(ResourceProcessorException.CODE_PREPARE,
                // "Existing configuration was not unbound and not bound to the specified bundlelocation");
                // }
            }

            Dictionary<Object, Object> properties = new Hashtable<Object, Object>();
            if (m_autoConfResource.isMerge()) {
                Dictionary<Object, Object> existingProperties = configuration.getProperties();
                if (existingProperties != null) {
                    Enumeration keys = existingProperties.keys();
                    while (keys.hasMoreElements()) {
                        Object key = keys.nextElement();
                        properties.put(key, existingProperties.get(key));
                    }
                }
            }

            Dictionary<Object, Object> newProperties = m_autoConfResource.getProperties();
            if (newProperties != null) {
                Enumeration keys = newProperties.keys();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    properties.put(key, newProperties.get(key));
                }
            }

            properties.put(AutoConfResourceProcessor.ORIGINAL_PID_KEY, m_autoConfResource.getPid());

            configuration.update(properties);
        }
    }
}

