/*
 * Web IDE - project
 * 
 * Copyright (C) 2000 - 2011 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Contact:
 * Changhyun Lee <changhyun1.lee@samsung.com>
 * Jihoon Song <jihoon80.song@samsung.com>
 * Kangho Kim <kh5325.kim@samsung.com>
 * Hyeongseok Heo <hyeongseok.heo@samsung.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 * - S-Core Co., Ltd
 * 
 */

package org.tizen.web.project.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.tizen.common.AppIdGenerator;
import org.tizen.common.util.StringUtil;
import org.tizen.common.util.log.Logger;
import org.tizen.web.config.schema.model.Access;
import org.tizen.web.config.schema.model.ActivationType;
import org.tizen.web.config.schema.model.Application;
import org.tizen.web.config.schema.model.Appservice;
import org.tizen.web.config.schema.model.Author;
import org.tizen.web.config.schema.model.Content;
import org.tizen.web.config.schema.model.DataBoolean;
import org.tizen.web.config.schema.model.Description;
import org.tizen.web.config.schema.model.Feature;
import org.tizen.web.config.schema.model.Icon;
import org.tizen.web.config.schema.model.License;
import org.tizen.web.config.schema.model.Name;
import org.tizen.web.config.schema.model.Preference;
import org.tizen.web.config.schema.model.ScreenOrientationType;
import org.tizen.web.config.schema.model.Setting;
import org.tizen.web.project.config.model.IWidgetConfigurator;
import org.tizen.web.project.config.model.WidgetConfiguratorFactory;
import org.tizen.web.project.core.configuration.ExternalIDLHandler;
import org.tizen.web.project.core.configuration.FeatureWrapper;
import org.tizen.web.project.core.configuration.InternalIDLHandler;
import org.tizen.web.project.core.configuration.Module;

import static org.tizen.web.common.WebConstant.*;

/**
 * Configurator is representation of configuration file.
 * @author Changhyun Lee {@literal <changhyun1.lee@samsung.com>} (S-Core)
 */
public class WidgetConfigurator {

    public static final String TIZEN = "tizen:" + WIDGET_BASE_IDENTIFIER + "ns/widgets"; //$NON-NLS-1$ //$NON-NLS-2$

    // Hardcoded modules that are always used in project.
    private static final Dictionary<String, Collection<String>> alwaysUsedFeatures;

    // excluded feature
    public static final String[] EXCLUDED_FEATURES = { // Not implemented features in TIzen 1.0 Official Release.
                                                                                };

    static {
        alwaysUsedFeatures = new Hashtable<String, Collection<String>>();
        alwaysUsedFeatures.put(TIZEN, Arrays.asList("http://tizen.org/api/tizen")); //$NON-NLS-1$
    }

    private IProject project = null;
    private IFile file = null;

    private Collection<IConfigurationListener> listeners;

    private IWidgetConfigurator widgetConfig = null;

    // Feature names known in this configuration.
    private Collection<String> knownFeatures;

    // Features existing in this configuration.
    private Collection<FeatureWrapper> features;

    // Modules known by this configuration (from internal and external .widlprocxml definition files)
    private Collection<Module> availableModules;

    // Modules used by this configuration.
    private Collection<Module> usedModules;

    private Dictionary<String, String> moduleReferences;

    /**
     * WACWidgetConfigurator constructor basing on project
     */
    public WidgetConfigurator(IProject project) throws ConfigurationException {
        Assert.isNotNull(project);

        this.project = project;
        if (!project.isAccessible()) {
            throw new ConfigurationException(Messages.WACWidgetConfigurator_2);
        }

        this.file = project.getFile(WIDGET_CONFIGURATION_FILE);
        if (!file.isAccessible()) {
            throw new ConfigurationException(Messages.WACWidgetConfigurator_3);
        }

        try {
            widgetConfig = WidgetConfiguratorFactory.unmarshalWidgetConfiguration(file.getLocation().toFile());
        } catch (FileNotFoundException e) {
            throw new ConfigurationException(e);
        }

        getData();
        listeners = new LinkedList<IConfigurationListener>();
    }

    public IProject getProject() {
        return this.project;
    }

    private boolean isExcludeFeature(String feature) {
        for (int i = 0; i < EXCLUDED_FEATURES.length; i++) {
            if (EXCLUDED_FEATURES[i].equalsIgnoreCase(feature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates model basing on file contents
     */
    private void getData() {
        moduleReferences = new Hashtable<String, String>();

        // Collecting all modules that are provided with this plugin.
        Collection<Module> internalModules = InternalIDLHandler.getInstance().getModules(TIZEN);

        // Collecting all modules that are provided in imported by user .widlprocxml files.
        Collection<Module> externalModules = ExternalIDLHandler.getInstance().getModules(project);

        availableModules = new LinkedList<Module>();
        availableModules.addAll(internalModules);
        availableModules.addAll(externalModules);

        knownFeatures = new HashSet<String>();
        for (Module module : availableModules) {
            String featureName = module.getFeatureName();
            // exclude some features
            if (!StringUtil.isEmpty(featureName) && !isExcludeFeature(featureName)) {
                // Adding parent features
                knownFeatures.add(featureName);

                Collection<String> subfeatureNames = module.getSubfeatureNames();
                for (String subfeatureName : subfeatureNames) {
                    // Adding sub features
                    knownFeatures.add(subfeatureName);
                    moduleReferences.put(subfeatureName, featureName);
                }
            }
        }

        Collection<FeatureWrapper> documentFeatures = new HashSet<FeatureWrapper>();

        List<Feature> featureElements = widgetConfig.getFeatures();
        for (Feature feature : featureElements) {
            if (feature.getName() != null) {
                FeatureWrapper featureWrapper = new FeatureWrapper(feature);
                documentFeatures.add(featureWrapper);
            }
        }

        // Collecting features used in configuration document.
        features = new LinkedList<FeatureWrapper>(documentFeatures);

        for (FeatureWrapper child : documentFeatures) {
            String childName = child.getName();
            String parentName = moduleReferences.get(childName);

            if (parentName != null) {
                for (FeatureWrapper potentialParent : documentFeatures) {
                    String potentialParentName = potentialParent.getName();
                    if (parentName.equals(potentialParentName)) {
                        potentialParent.getSubfeatures().add(child);
                        child.setParent(potentialParent);
                        features.remove(child);
                    }
                }
            }
        }

        // Collecting top-level feature names that are used in configuration document.
        Collection<String> topLevelFeatureNames = new HashSet<String>();
        for (FeatureWrapper feature : features) {
            // Handling subfeatures too.
            String featureName = feature.getName();
            String parentName = moduleReferences.get(featureName);

            // Adding parent feature name if this is only a subfeature.
            topLevelFeatureNames.add((parentName != null) ? parentName 
                                                                                   : featureName);
        }

        // Selecting modules, that have feature in used feature collection.
        usedModules = new HashSet<Module>();
        for (Module module : availableModules) {
            String moduleFeatureName = module.getFeatureName();

            // Providing modules if their features are mentioned in config.xml.
            if (topLevelFeatureNames.contains(moduleFeatureName)) {
                usedModules.add(module);
            }

            // Adding hardcoded modules.
            Collection<String> featureNames = alwaysUsedFeatures.get(TIZEN);
            if (featureNames.contains(moduleFeatureName)) {
                usedModules.add(module);
            }
        }
    }

    /**
     * Adapts incompatible input.
     */
    @Deprecated
    public void transformIncompatibleInput() {
        /*
        // ID transforming
        Element IDelement = widgetElement.element(ID);

        if (IDelement != null) {
            Element hostElement = IDelement.element(HOST);
            Element nameElement = IDelement.element(NAME);

            for (Object el : IDelement.elements()) {
                IDelement.remove((Element) el);
            }

            if (hostElement != null) {
                this.setIdentifier(hostElement.getText());
            }

            if (nameElement != null) {
                this.setName(nameElement.getText());
            }
        }

        widgetElement.remove(IDelement); // ID data is kept as attribute of root  "widget" element.
        // End of ID transforming.

        // Author transforming
        Element authorElement = widgetElement.element(AUTHOR);

        if (authorElement != null) {
            Element nameElement = authorElement.element(NAME);
            Element hostElement = authorElement.element(HOST);
            Element emailElement = authorElement.element(EMAIL);
            Element linkElement = authorElement.element(LINK);

            for (Object el : authorElement.elements()) {
                authorElement.remove((Element) el);
            }

            if (nameElement != null) {
                this.setAuthor(nameElement.getText());
            }

            if (hostElement != null) {
                this.setWebSite(hostElement.getText());
            }

            if (emailElement != null) {
                this.setEMail(emailElement.getText());
            }

            if (linkElement != null) {
                this.setWebSite(linkElement.getText());
            }
        } // End of author transforming.
        */
    }

    /**
     * Performs saving of model tree to filesystem.
     */
    public void doSave() throws IOException, ConfigurationException {
        Assert.isNotNull(project);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file.getLocation().toFile());
            WidgetConfiguratorFactory.marshalObject(widgetConfig.getWidget(), outputStream);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        } finally {
            if (outputStream != null) {
                try { outputStream.close();
                } catch (IOException e) {
                    throw new IOException(e);
                }
            }
        }

        try {
            IFile configFile = project.getFile(WIDGET_CONFIGURATION_FILE);
            if (configFile.exists() && configFile.isAccessible()) {
                configFile.refreshLocal(IFile.DEPTH_INFINITE, null);
            }
        } catch (CoreException ex) {
            // In case of any error propagate higher.
            throw new ConfigurationException(ex);
        }
    }

    public void addConfigurationListener(IConfigurationListener listener) {
        Assert.isNotNull(listeners);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeConfigurationListener(IConfigurationListener listener) {
        Assert.isNotNull(listeners);
        listeners.remove(listener);
    }

    private void notifyListeners(ConfigurationEvent event) {
        Assert.isNotNull(listeners);
        for (IConfigurationListener listener : listeners) {
            listener.configurationChanged(event);
        }
    }

    public void setLocalizedElement(String name, String text, String lang) {
        if (text == null && lang == null) {
            // remove all localizedElement
            Iterator<?> iterator = getWidgetSubElements(name).iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof Name) {
                    if(((Name) element).getLang() != null) {
                        widgetConfig.removeElement(element);
                    }
                } else if (element instanceof Author) {
                    if(((Author) element).getLang() != null) {
                        widgetConfig.removeElement(element);
                    }
                } else if (element instanceof Description) {
                    if(((Description) element).getLang() != null) {
                        widgetConfig.removeElement(element);
                    }
                }
            }
        } else {
            Object element = getLocalizedElement(name, lang);
            if (text == null || text.length() == 0) {
                // remove localizedElement
                if (element != null) {
                    widgetConfig.removeElement(element);
                }
            } else {
                // add localizedElement
                if (element == null) {
                    // create new Element
                    if ("name".equals(name)) {
                        element = new Name();
                        ((Name) element).setContent(text);
                        ((Name) element).setLang(lang);
                    } else if ("author".equals(name)) {
                        element = new Author();
                        ((Author) element).setContent(text);
                        ((Author) element).setLang(lang);
                    } else if ("description".equals(name)) {
                        element = new Description();
                        ((Description) element).setContent(text);
                        ((Description) element).setLang(lang);
                    }
                    widgetConfig.getContents().add(element);
                } else {
                    // modified content
                    if ("name".equals(name)) {
                        ((Name) element).setContent(text);
                    } else if ("author".equals(name)) {
                        ((Author) element).setContent(text);
                    } else if ("description".equals(name)) {
                        ((Description) element).setContent(text);
                    }
                }
            }
        }
    }

    public Object getLocalizedElement(String name, String lang) {
        Iterator<?> iterator = getWidgetSubElements(name).iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof Name) {
                if(lang.equals(((Name) element).getLang())) {
                    return element;
                }
            } else if (element instanceof Author) {
                if(lang.equals(((Author) element).getLang())) {
                    return element;
                }
            } else if (element instanceof Description) {
                if(lang.equals(((Description) element).getLang())) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Provides a collection of modules that are used by this widget configuration.
     */
    public Collection<Module> getUsedModules() {
        return usedModules;
    }

    /**
     * Provides a collection of features that are used by this widget configuration.
     */
    public Collection<FeatureWrapper> getFeatures() {
        return features;
    }

    /**
     * Provides a collection of features that are used and required by this widget configuration.
     * @return Features required in configuration document.
     */
    public Collection<FeatureWrapper> getRequiredFeatures() {
        Collection<FeatureWrapper> result = new LinkedList<FeatureWrapper>();
        for (FeatureWrapper feature : getFeatures()) {
            if (feature.getRequired()) {
                result.add(feature);
            }
            for (FeatureWrapper subfeature : feature.getSubfeatures()) {
                if (subfeature.getRequired()) {
                    result.add(subfeature);
                }
            }
        }
        return result;
    }

    /**
     * Provides a collection of known feature names.
     * @return Known feature names.
     */
    public Collection<String> getKnownFeatures() {
        return knownFeatures;
    }

    /**
     * Creates a new feature with given name in configuration document.
     */
    public FeatureWrapper createFeature(String name) {
        Feature feature = new Feature();
        feature.setName(name);
        feature.setRequired(DataBoolean.FALSE);
        widgetConfig.getContents().add(feature);
        FeatureWrapper featureWrapper = new FeatureWrapper(feature);

        // Looking for parent feature, if exists.
        String parentName = moduleReferences.get(name);

        // This feature should not have a parent.
        if (parentName == null) {
            // Checking currently existing features if they should be moved under newly added feature.
            Collection<FeatureWrapper> childFeatures = new HashSet<FeatureWrapper>();
            for (FeatureWrapper potentialChild : features) {
                String childName = potentialChild.getName();
                String childsParentName = moduleReferences.get(childName);
                if (childsParentName != null && childsParentName.equals(name)) {
                    potentialChild.setParent(featureWrapper);
                    featureWrapper.getSubfeatures().add(potentialChild);
                    childFeatures.add(potentialChild);
                }
            }
            features.removeAll(childFeatures);
            features.add(featureWrapper);
        } else { // This feature should have a parent.
            // Searching for a parent.
            FeatureWrapper parent = null;
            for (FeatureWrapper potentialParent : features) {
                if (potentialParent.getName().equals(parentName)) {
                    parent = potentialParent;
                }
            }

            // Parent already exists in document, adding new feature as its child.
            if (parent != null) {
                parent.getSubfeatures().add(featureWrapper);
                featureWrapper.setParent(parent);
            } else { // Parent does not exist. Adding new feature as top-level feature.
                features.add(featureWrapper);
                createFeature(parentName);
            }
        }

        ConfigurationEvent event = new ConfigurationEvent();
        notifyListeners(event);

        return featureWrapper;
    }

    /**
     * Removes a feature from document.
     */
    public void removeFeature(FeatureWrapper featureWrapper) {
        // Removing feature element from document.
        List<Feature> featureElements = widgetConfig.getFeatures();
        for (Feature feature : featureElements) {
            String name = feature.getName();
            if (name.equals(featureWrapper.getName())) {
                widgetConfig.removeElement(feature);
            }
        }

        FeatureWrapper parentFeature = featureWrapper.getParent();
        if (parentFeature != null) {
            parentFeature.getSubfeatures().remove(featureWrapper);
        } else {
            features.remove(featureWrapper);
            for (FeatureWrapper childFeature : featureWrapper.getSubfeatures()) {
                childFeature.setParent(null);
                features.add(childFeature);
            }
        }

        ConfigurationEvent deletionEvent = new ConfigurationEvent();
        notifyListeners(deletionEvent);
    }

    /**
     * Handles new definition file appearing in project.
     */
    public void addExternalDefinition(IFile newFile) {
        Collection<Module> newModules = ExternalIDLHandler.getInstance().scanDocument(newFile);

        // Providing new modules.
        availableModules.addAll(newModules);
        Collection<String> newFeatures = new HashSet<String>();
        for (Module module : newModules) {
            String featureName = module.getFeatureName();
            if (!StringUtil.isEmpty(featureName)) {
                // Adding parent features
                knownFeatures.add(featureName);
                newFeatures.add(featureName);

                Collection<String> subfeatureNames = module.getSubfeatureNames();
                for (String subfeatureName : subfeatureNames) {
                    // Adding sub features
                    knownFeatures.add(subfeatureName);
                    moduleReferences.put(subfeatureName, featureName);
                    newFeatures.add(subfeatureName);
                }
            }
        }

        // create new features.
        for (String featureName : newFeatures) {
            createFeature(featureName);
        }
    }

    public void setAccess(String uri, DataBoolean subDomains) {
        Access access = new Access();
        access.setOrigin(uri);
        access.setSubdomains(subDomains);
        widgetConfig.getContents().add(access);
    }

    public List<Access> getAccess() {
        return widgetConfig.getAccess();
    }

    public void removeAllAccess() {
        List<Access> accesses = widgetConfig.getAccess();
        for (Access access : accesses) {
            widgetConfig.removeElement(access);
        }
    }

    public void setPreference(String name, String value, DataBoolean isReadOnly) {
        Preference preference = new Preference();
        preference.setName(name);
        preference.setValue(value);
        preference.setReadonly(isReadOnly);
        widgetConfig.getContents().add(preference);
    }

    public List<Preference> getPreferences() {
        return widgetConfig.getPreferences();
    }

    public void removeAllPreferences() {
        List<Preference> preferences = widgetConfig.getPreferences();
        for (Preference preference : preferences) {
            widgetConfig.removeElement(preference);
        }
    }

    /**
     * @return list of locales that are supported by this project
     */
    public List<Locale> getLocales() {
        List<Locale> locales = new ArrayList<Locale>();

        IProject project = getProject();
        if (project != null) {
            List<IFolder> folders = getLocaleFolders(project);
            Iterator<IFolder> iterator = folders.iterator();
            while (iterator.hasNext()) {
                IFolder iFolder = iterator.next();
                locales.add(new Locale(iFolder.getName()));
            }
        }
        return locales;
    }

    private List<IFolder> getLocaleFolders(IProject project) {
        List<IFolder> folders = new ArrayList<IFolder>();

        IResource[] resources = null;
        try {
            resources = project.members();
        } catch (CoreException e) {
            e.printStackTrace();
        }

        for (IResource resource : resources) {
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                if (folder.getName().equals("locales")) { //$NON-NLS-1$
                    IResource[] locales = null;
                    try {
                        locales = folder.members();
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                    for (IResource locale : locales) {
                        if (locale instanceof IFolder) {
                            folders.add((IFolder) locale);
                        }
                    }
                }
            }
        }
        return folders;
    }

    // return "name", "author", "description"
    public List<?> getWidgetSubElements(String name) {
        if ("name".equals(name)) {
            return widgetConfig.getNames();
        } else if ("author".equals(name)) {
            return widgetConfig.getAuthors();
        } else if ("description".equals(name)) {
            return widgetConfig.getDescriptions();
        }
        return null;

    }

    /* Widget Scheme */
    // General Information
    public void setId(String value) {
        widgetConfig.setId(value);
    }

    public String getId() {
        return widgetConfig.getId();
    }

    public void setVersion(String value) {
        widgetConfig.setVersion(value);
    }

    public String getVersion() {
        return widgetConfig.getVersion();
    }

    public void setWidth(String value) {
        if (Integer.parseInt(value) > 0) {
            widgetConfig.setWidth(value);
        }
    }

    public String getWidth() {
        return widgetConfig.getWidth();
    }

    public void setHeight(String value) {
        if (Integer.parseInt(value) > 0) {
            widgetConfig.setHeight(value);
        }
    }

    public String getHeight() {
        return widgetConfig.getHeight();
    }

    public void setViewMode(String viewmode) {
        widgetConfig.setViewmode(viewmode);
    }

    public String getViewMode() {
        return widgetConfig.getViewmode();
    }

    public void setContent(String value) {
        Content content = widgetConfig.getContent();
        if (content == null) {
            content = new Content();
            widgetConfig.getContents().add(content);
        }
        content.setSrc(value);
    }

    public String getContent() {
        Content content = widgetConfig.getContent();
        if (content != null) {
            return content.getSrc();
        }
        return null;
    }

    public void setIcon(String value) {
        Icon icon = widgetConfig.getIcon();
        if (icon == null) {
            icon = new Icon();
            widgetConfig.getContents().add(icon);
        }
        icon.setSrc(value);
    }

    public String getIcon() {
        Icon icon = widgetConfig.getIcon();
        if (icon != null) {
            return icon.getSrc();
        }
        return null;
    }

    public void setName(String value) {
        List<Name> names = widgetConfig.getNames();
        Name name;
        if (!isEmpty(value)) {
            if (names.isEmpty()) {
                name = new Name();
                widgetConfig.getContents().add(name);
            } else {
                name = names.get(0);
            }
            name.setContent(value);
        } else {
            if (!names.isEmpty()) {
                name = names.get(0);
                widgetConfig.removeElement(name);
            }
        }
    }

    public String getName() {
        List<Name> names = widgetConfig.getNames();
        if (!names.isEmpty()) {
            Name name = names.get(0);
            if (name != null) {
                return name.getContent();
            }
        }
        return null;
    }

    public void setDescription(String value) {
        List<Description> descriptions = widgetConfig.getDescriptions();
        Description description;
        if (!isEmpty(value)) {
            if (descriptions.isEmpty()) {
                description = new Description();
                widgetConfig.getContents().add(description);
            } else {
                description = descriptions.get(0);
            }
            description.setContent(value);
        } else {
            if (!descriptions.isEmpty()) {
                description = descriptions.get(0);
                widgetConfig.removeElement(description);
            }
        }
    }

    public String getDescription() {
        List<Description> descriptions = widgetConfig.getDescriptions();
        if (!descriptions.isEmpty()) {
            Description description = descriptions.get(0);
            if (description != null) {
                return description.getContent();
            }
        }
        return null;
    }

    public void setAuthor(String content, String href, String email) {
        List<Author> authors = widgetConfig.getAuthors();
        if (!authors.isEmpty()) {
            widgetConfig.removeElement(authors.get(0));
        }
        if (isEmpty(content) && isEmpty(href) && isEmpty(email)) {
            return;
        }
        Author author = new Author();
        widgetConfig.getContents().add(author);
        if (!isEmpty(content)) {
            author.setContent(content);
        }
        if (!isEmpty(href)) {
            author.setHref(href);
        }
        if (!isEmpty(email)) {
            author.setEmail(email);
        }
    }

    public String getAuthorContent() {
        List<Author> authors = widgetConfig.getAuthors();
        if (!authors.isEmpty()) {
            Author author = authors.get(0);
            if (author != null) {
                return author.getContent();
            }
        }
        return null;
    }

    public String getAuthorEmail() {
        List<Author> authors = widgetConfig.getAuthors();
        if (!authors.isEmpty()) {
            Author author = authors.get(0);
            if (author != null) {
                return author.getEmail();
            }
        }
        return null;
    }

    public String getAuthorHref() {
        List<Author> authors = widgetConfig.getAuthors();
        if (!authors.isEmpty()) {
            Author author = authors.get(0);
            if (author != null) {
                return author.getHref();
            }
        }
        return null;
    }

    public void setLicense(String content, String href) {
        License license = widgetConfig.getLicense();
        if (license != null) {
            widgetConfig.removeElement(license);
        }
        if (isEmpty(content) && isEmpty(href)) {
            return;
        }
        license = new License();
        widgetConfig.getContents().add(license);
        if (!isEmpty(content)) {
            license.setContent(content);
        }
        if (!isEmpty(href)) {
            license.setHref(href);
        }
    }

    public String getLicenseContent() {
        License license = widgetConfig.getLicense();
        if (license != null) {
            return license.getContent();
        }
        return null;
    }

    public String getLicenseHref() {
        License license = widgetConfig.getLicense();
        if (license != null) {
            return license.getHref();
        }
        return null;
    }

    public void setApplication(String appID, float requiredVersion) {
        Application application = widgetConfig.getApplication();
        if (application == null) {
            application = new Application();
            widgetConfig.getContents().add(application);
        }

        if (appID == null || appID.isEmpty() || appID.length() != 10) {
            AppIdGenerator generator = new AppIdGenerator();
            try {
                appID = generator.generate();
            } catch (Exception e) {
                Logger.log(e);
            }
        }
        if (requiredVersion <= 0) {
            requiredVersion = (float)1.0;
        }
        application.setId(appID);
        application.setRequiredVersion(requiredVersion);
    }

    public String getApplicationAppID() {
        Application application = widgetConfig.getApplication();
        if (application != null) {
            return application.getId();
        }
        return null;
    }

    public float getApplicationRequiredVersion() {
        Application application = widgetConfig.getApplication();
        if (application != null) {
            return application.getRequiredVersion();
        }
        // default required_version
        return (float) 1.0;
    }

    public void setSettingScreenOrientation(ScreenOrientationType value) {
        // TODO::
        Setting setting = widgetConfig.getSetting();
        if (setting == null) {
            setting = new Setting();
            widgetConfig.getContents().add(setting);
        }
        setting.setScreenOrientation(value);
    }

    public ScreenOrientationType getSettingScreenOrientation() {
        Setting setting = widgetConfig.getSetting();
        if (setting != null) {
            return setting.getScreenOrientation();
        }
        return null;
    }

    public void setSettingContextmenu(ActivationType type) {
        // TODO::
        Setting setting = widgetConfig.getSetting();
        if (setting == null) {
            setting = new Setting();
            widgetConfig.getContents().add(setting);
        }
        setting.setContextmenu(type);
    }

    public ActivationType getSettingContextmenu() {
        Setting setting = widgetConfig.getSetting();
        if (setting != null) {
            return setting.getContextmenu();
        }
        return null;
    }

    public void setAppservice(String src, String operation, String sheme, String mime) {
        Appservice appservice = widgetConfig.getAppservice();
        if (appservice != null) {
            widgetConfig.removeElement(appservice);
        }
        appservice = new Appservice();
        widgetConfig.getContents().add(appservice);
        appservice.setSrc(src);
        appservice.setOperation(operation);
        if (!isEmpty(sheme)) {
            appservice.setScheme(sheme);
        }
        if (!isEmpty(mime)) {
            appservice.setMime(mime);
        }
    }

    public void removeAppservice() {
        Appservice appservice = widgetConfig.getAppservice();
        if (appservice != null) {
            widgetConfig.removeElement(appservice);
        }
    }

    public String getAppserviceSrc() {
        Appservice appservice = widgetConfig.getAppservice();
        if (appservice != null) {
            return appservice.getSrc();
        }
        return null;
    }

    public String getAppserviceOperation() {
        Appservice appservice = widgetConfig.getAppservice();
        if (appservice != null) {
            return appservice.getOperation();
        }
        return null;
    }

    public String getAppserviceScheme() {
        Appservice appservice = widgetConfig.getAppservice();
        if (appservice != null) {
            return appservice.getScheme();
        }
        return null;
    }

    public String getAppserviceMime() {
        Appservice appservice = widgetConfig.getAppservice();
        if (appservice != null) {
            return appservice.getMime();
        }
        return null;
    }

    private boolean isEmpty(String content) {
        if (content != null && !"".equals(content.trim())) {
            return false;
        }
        return true;
    }

    public IWidgetConfigurator getWidgetConfigurator() {
        return widgetConfig;
    }

}

