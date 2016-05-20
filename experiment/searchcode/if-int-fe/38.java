/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jbi.apisupport.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.jbi.apisupport.project.JbiCompActionProvider;
import org.netbeans.modules.jbi.apisupport.project.JbiCompProject;
import org.netbeans.modules.jbi.apisupport.project.JbiCompProjectType;
import org.netbeans.modules.jbi.apisupport.project.JbiCompProjectUtil;
import org.netbeans.modules.jbi.apisupport.project.SourceRoots;
import org.netbeans.modules.jbi.apisupport.project.UpdateHelper;
import org.netbeans.modules.jbi.apisupport.project.ui.customizer.CustomizerLibraries;
import org.netbeans.modules.jbi.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.jbi.apisupport.project.JbiCompProjectProperties;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Support for creating logical views.
 * @author chikkala, j2seproject
 */
public class JbiCompLogicalViewProvider implements LogicalViewProvider {
    
    private static final String JBI_PROJECT_ICON_PATH = "org/netbeans/modules/jbi/apisupport/resources/images/JBI.png"; // NOI18N
    
    private static final String SE_PROJECT_ICON_PATH = "org/netbeans/modules/jbi/apisupport/resources/images/ServiceEngine.png"; // NOI18N
    
    private static final String BC_PROJECT_ICON_PATH = "org/netbeans/modules/jbi/apisupport/resources/images/BindingComponent.png"; // NOI18N
    
    private static final Image DEF_PROJECT_ICON = Utilities.loadImage(JBI_PROJECT_ICON_PATH, true);
    private static final Image SE_PROJECT_ICON = Utilities.loadImage(SE_PROJECT_ICON_PATH, true);
    private static final Image BC_PROJECT_ICON = Utilities.loadImage(BC_PROJECT_ICON_PATH, true);
    
    private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("JbiCompPhysicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final JbiCompProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    private List changeListeners;
    
    private String mPrjViewIconPath = null;
    
    // Web service client
    private static final Object KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
    
    public JbiCompLogicalViewProvider(JbiCompProject project, UpdateHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
        
    }
    
    private void initProjectViewIconPath() {
        
        //        if ( mPrjViewIconPath != null ) {
        //            return;
        //        }
        
        String  iconPath = (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(JbiCompProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        JbiCompProjectType.COMP_TYPE_EL); // NOI18N
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String compType = ((org.w3c.dom.Text) nl.item(0)).getNodeValue();
                        String path = null;
                        if (JbiCompProjectType.ENGINE_TYPE.equalsIgnoreCase(compType) ) {
                            path = SE_PROJECT_ICON_PATH;
                        } else if ( JbiCompProjectType.BINDING_TYPE.equalsIgnoreCase(compType) ) {
                            path = BC_PROJECT_ICON_PATH;
                        } else {
                            System.out.println("XXX ViewNode component-type found was not se or bc " + compType);
                            path = JBI_PROJECT_ICON_PATH;
                        }
                        return path;
                    }
                }
                System.out.println("XXX ViewNode DID not find the component-type");
                return JBI_PROJECT_ICON_PATH; // NOI18N
            }
        });
        
        mPrjViewIconPath = iconPath;
        
    }
    
    public Node createLogicalView() {
        initProjectViewIconPath();
        return new JbiCompLogicalViewRootNode();
    }
    
    public Node findPath(Node root, Object target) {
        Project project = (Project) root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                Node result = PackageView.findPath(nodes[i], target);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    
    
    public synchronized void addChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            this.changeListeners = new ArrayList();
        }
        this.changeListeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            return;
        }
        this.changeListeners.remove(l);
    }
    
    /**
     * Used by JbiCompProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
     */
    public void testBroken() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (this.changeListeners == null) {
                return;
            }
            _listeners = (ChangeListener[]) this.changeListeners.toArray(
                    new ChangeListener[this.changeListeners.size()]);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (int i=0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(event);
        }
    }
    
    private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed(new Object[] {project, rootFolder});
    }
    
    
    // Private innerclasses ----------------------------------------------------
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        JbiCompProjectProperties.JAVAC_CLASSPATH,
        JbiCompProjectProperties.RUN_CLASSPATH,
        JbiCompProjectProperties.DEBUG_CLASSPATH,
        JbiCompProjectProperties.RUN_TEST_CLASSPATH,
        JbiCompProjectProperties.DEBUG_TEST_CLASSPATH,
        JbiCompProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    
    public boolean hasBrokenLinks() {
        return BrokenReferencesSupport.isBroken(helper.getAntProjectHelper(), resolver, getBreakableProperties(),
                new String[] {JbiCompProjectProperties.JAVA_PLATFORM});
    }
    
    public boolean hasInvalidJdkVersion() {
        String javaSource = this.evaluator.getProperty("javac.source");     //NOI18N
        String javaTarget = this.evaluator.getProperty("javac.target");    //NOI18N
        if (javaSource == null && javaTarget == null) {
            //No need to check anything
            return false;
        }
        
        final String platformId = this.evaluator.getProperty("platform.active");  //NOI18N
        final JavaPlatform activePlatform = JbiCompProjectUtil.getActivePlatform(platformId);
        if (activePlatform == null) {
            return true;
        }
        SpecificationVersion platformVersion = activePlatform.getSpecification().getVersion();
        try {
            return (javaSource != null && new SpecificationVersion(javaSource).compareTo(platformVersion)>0)
                    || (javaTarget != null && new SpecificationVersion(javaTarget).compareTo(platformVersion)>0);
        } catch (NumberFormatException nfe) {
            ErrorManager.getDefault().log("Invalid javac.source: "+javaSource+" or javac.target: "+javaTarget+" of project:"
                    +this.project.getProjectDirectory().getPath());
            return true;
        }
    }
    
    private String[] getBreakableProperties() {
        SourceRoots roots = this.project.getSourceRoots();
        String[] srcRootProps = roots.getRootProperties();
        roots = this.project.getTestSourceRoots();
        String[] testRootProps = roots.getRootProperties();
        String[] result = new String [BREAKABLE_PROPERTIES.length + srcRootProps.length + testRootProps.length];
        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
        return result;
    }
    
    private static Image brokenProjectBadge = Utilities.loadImage("org/netbeans/modules/jbi/apisupport/project/resources/brokenProjectBadge.gif", true);
    
    /** Filter node containin additional features for the JbiComp physical
     */
    private final class JbiCompLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        
        private Image icon;
        private Lookup lookup;
        private Action brokenLinksAction;
        private boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
        private boolean illegalState;   //Represents a state where project is not in legal state, eg invalid source/target level
        
        // icon badging >>>
        private Set files;
        private Map fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map groupsListeners;
        //private Project project;
        // icon badging <<<
        
        public JbiCompLogicalViewRootNode() {
            super(new LogicalViewChildren(project, evaluator, helper, resolver), Lookups.singleton(project));
            setIconBaseWithExtension(mPrjViewIconPath);
            
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            
            if (hasBrokenLinks()) {
                broken = true;
            } else if (hasInvalidJdkVersion()) {
                illegalState = true;
            }
            brokenLinksAction = new BrokenLinksAction();
            setProjectFiles(project);
        }
        
        
        protected final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }
        
        
        private final void setGroups(Collection groups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap();
            Set roots = new HashSet();
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                SourceGroup group = (SourceGroup) it.next();
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                roots.add(fo);
            }
            setFiles(roots);
        }
        
        protected final void setFiles(Set files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
                    FileStatusListener fsl = (FileStatusListener) fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }
            
            fileSystemListeners = new HashMap();
            this.files = files;
            if (files == null) {
                return;
            }
            
            Iterator it = files.iterator();
            Set hookedFileSystems = new HashSet();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            // XXX text colors should be taken from UIManager, not hard-coded!
            return broken || illegalState ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        public Image getIcon(int type) {
            Image img = getMyIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        private Image getMyIcon(int type) {
            Image original = super.getIcon(type);
            try {
                return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
            } catch ( Exception ex) {
                return (original == null ) ? DEF_PROJECT_ICON : original;
            }
        }
        
        public Image getOpenedIcon(int type) {
            Image img = getMyOpenedIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        private Image getMyOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            try {
                return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
            } catch ( Exception ex) {
                return (original == null ) ? DEF_PROJECT_ICON : original;
            }
        }
        
        public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
        }
        
        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }
            
            synchronized (privateLock) {
                if ((iconChange == false && event.isIconChange()) || (nameChange == false && event.isNameChange())) {
                    Iterator it = files.iterator();
                    while (it.hasNext()) {
                        FileObject fo = (FileObject) it.next();
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }
            
            task.schedule(50); // batch by 50 ms
        }
        
        // sources change
        public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
        }
        
        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
        }
        
        public Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        /*
        public boolean canDestroy() {
            return true;
        }
         
        public void destroy() throws IOException {
            System.out.println("Destroy " + project.getProjectDirectory());
            LogicalViews.closeProjectAction().actionPerformed(new ActionEvent(this, 0, ""));
            project.getProjectDirectory().delete();
        }
         */
        
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(JbiCompLogicalViewProvider.class);
            
            List actions = new ArrayList();
            
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(JavaProjectConstants.COMMAND_JAVADOC, bundle.getString("LBL_JavadocAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_INSTALL, bundle.getString("LBL_InstallAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_UNINSTALL, bundle.getString("LBL_UninstallAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_START, bundle.getString("LBL_StartAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_STOP, bundle.getString("LBL_StopAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_SHUTDOWN, bundle.getString("LBL_ShutdownAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_FORCED_UNINSTALL, bundle.getString("LBL_ForcedUninstallAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(JbiCompActionProvider.COMMAND_UPGRADE, bundle.getString("LBL_UpgradeAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            
            // honor 57874 contact
            
            try {
                FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Projects/Actions"); // NOI18N
                if (fo != null) {
                    DataObject dobj = DataObject.find(fo);
                    FolderLookup actionRegistry = new FolderLookup((DataFolder)dobj);
                    Lookup.Template query = new Lookup.Template(Object.class);
                    Lookup lookup = actionRegistry.getLookup();
                    Iterator it = lookup.lookup(query).allInstances().iterator();
                    if (it.hasNext()) {
                        actions.add(null);
                    }
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof Action) {
                            actions.add(next);
                        } else if (next instanceof JSeparator) {
                            actions.add(null);
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                // data folder for existing fileobject expected
                ErrorManager.getDefault().notify(ex);
            }
            
            actions.add(null);
            actions.add(SystemAction.get(ToolsAction.class));
            actions.add(null);
            if (broken) {
                actions.add(brokenLinksAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return (Action[]) actions.toArray(new Action[actions.size()]);
            
        }
        
        private boolean isBroken() {
            return this.broken;
        }
        
        private void setBroken(boolean broken) {
            this.broken = broken;
            brokenLinksAction.setEnabled(broken);
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        private void setIllegalState(boolean illegalState) {
            this.illegalState = illegalState;
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, ChangeListener, Runnable {
            
            private RequestProcessor.Task task = null;
            
            private PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                putValue(Action.NAME, NbBundle.getMessage(JbiCompLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                setEnabled(broken);
                evaluator.addPropertyChangeListener(this);
                // When evaluator fires changes that platform properties were
                // removed the platform still exists in JavaPlatformManager.
                // That's why I have to listen here also on JPM:
                weakPCL = WeakListeners.propertyChange(this, JavaPlatformManager.getDefault());
                JavaPlatformManager.getDefault().addPropertyChangeListener(weakPCL);
                JbiCompLogicalViewProvider.this.addChangeListener((ChangeListener) WeakListeners.change(this, JbiCompLogicalViewProvider.this));
            }
            
            public void actionPerformed(ActionEvent e) {
                try {
                    helper.requestSave();
                    BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, getBreakableProperties(), new String[] {JbiCompProjectProperties.JAVA_PLATFORM});
                    run();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                refsMayChanged();
            }
            
            
            public void stateChanged(ChangeEvent evt) {
                refsMayChanged();
            }
            
            public synchronized void run() {
                boolean old = JbiCompLogicalViewRootNode.this.broken;
                boolean broken = hasBrokenLinks();
                if (old != broken) {
                    setBroken(broken);
                }
                
                old = JbiCompLogicalViewRootNode.this.illegalState;
                broken = hasInvalidJdkVersion();
                if (old != broken) {
                    setIllegalState(broken);
                }
            }
            
            private void refsMayChanged() {
                // check project state whenever there was a property change
                // or change in list of platforms.
                // Coalesce changes since they can come quickly:
                if (task == null) {
                    task = BROKEN_LINKS_RP.create(this);
                }
                task.schedule(100);
            }
            
        }
        
    }
    
    private static final class LogicalViewChildren extends Children.Keys/*<SourceGroup>*/ implements ChangeListener {
        
        private static final String KEY_TEST_ASSEMBLIES = "TestAssemblies"; //NOI18N
        private static final String KEY_DEPLOY_PLUGIN = "deployPlugin"; //NOI18N
        private static final String KEY_CONF_FILES = "confFiles"; //NOI18N
        
        private static final Object LIBRARIES = "Libs"; //NOI18N
        private static final Object TEST_LIBRARIES = "TestLibs"; //NOI18N
        private static final String WSDL_FOLDER = "wsdl";
        
        private final JbiCompProject project;
        private final PropertyEvaluator evaluator;
        private final UpdateHelper helper;
        private final ReferenceHelper resolver;
        private final SourceRoots testSources;
        
        private final WsdlCreationListener wsdlListener;
        private final MetaInfListener metaInfListener;
        private final JaxWsChangeListener jaxWsListener;
        //        private FileObject wsdlFolder;
        //UNDO        private JaxWsModel jaxWsModel;
        
        public LogicalViewChildren(JbiCompProject project, PropertyEvaluator evaluator, UpdateHelper helper, ReferenceHelper resolver) {
            this.project = project;
            this.evaluator = evaluator;
            this.helper = helper;
            this.resolver = resolver;
            this.testSources = project.getTestSourceRoots();
            this.metaInfListener = new MetaInfListener();
            this.wsdlListener = new WsdlCreationListener();
            this.jaxWsListener = new JaxWsChangeListener();
        }
        
        protected void addNotify() {
            super.addNotify();
            getSources().addChangeListener(this);
            
            AntProjectHelper projectHelper = helper.getAntProjectHelper();
            
            String prop = evaluator.getProperty("meta.inf.dir"); //NOI18N
            if (prop!=null) {
                FileObject metaInf = projectHelper.resolveFileObject(prop);
                if (metaInf!=null) metaInf.addFileChangeListener(metaInfListener);
            }
            prop = evaluator.getProperty("src.dir"); //NOI18N
            if (prop!=null) {
                FileObject srcDir = projectHelper.resolveFileObject(prop);
                if (srcDir!=null) srcDir.addFileChangeListener(metaInfListener);
            }
            
            setKeys(getKeys());
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            getSources().removeChangeListener(this);
            
            AntProjectHelper projectHelper = helper.getAntProjectHelper();
            
            String prop = evaluator.getProperty("meta.inf.dir"); //NOI18N
            if (prop!=null) {
                FileObject metaInf = projectHelper.resolveFileObject(prop);
                if (metaInf!=null) metaInf.addFileChangeListener(metaInfListener);
            }
            prop = evaluator.getProperty("src.dir"); //NOI18N
            if (prop!=null) {
                FileObject srcDir = projectHelper.resolveFileObject(prop);
                if (srcDir!=null) srcDir.removeFileChangeListener(metaInfListener);
            }
            
            super.removeNotify();
        }
        
        protected Node[] createTestAssembliesView() {
            Node saNode = new TestAssembliesNode(project);
            Node[] result = new Node[] { saNode};
            return result;
//            Node[] result = new Node[0];
//            
//                FileObject testSAFO = null;
//                String testSAProp = evaluator.getProperty(JbiCompProjectProperties.PROJECT_TEST_SA); //NOI18N
//                if ( testSAProp != null ) {
//                    AntProjectHelper projectHelper = helper.getAntProjectHelper();
//                    testSAFO = projectHelper.resolveFileObject(testSAProp);
//                }
//                if ( testSAFO != null ) {
//                try {
//                    DataObject dataObject = DataObject.find(testSAFO);
//                    Node n = dataObject.getNodeDelegate().cloneNode();
//                    result = new Node[] { n };
//                } catch (DataObjectNotFoundException dnfe) {}
//                }
//            return result;
        }
        
        protected Node[] createNodes(Object key) {
            Node[] result = new Node[0];
            if (key == LIBRARIES) {
                //Libraries Node
                result = new Node[] {
                    new LibrariesNode(NbBundle.getMessage(JbiCompLogicalViewProvider.class,"CTL_LibrariesNode"),
                            project, evaluator, helper, resolver, JbiCompProjectProperties.RUN_CLASSPATH,
                            new String[] {JbiCompProjectProperties.BUILD_CLASSES_DIR},
                            "platform.active", // NOI18N
                            new Action[] {
                        LibrariesNode.createAddProjectAction(project, JbiCompProjectProperties.JAVAC_CLASSPATH),
                        LibrariesNode.createAddLibraryAction(project, JbiCompProjectProperties.JAVAC_CLASSPATH),
                        LibrariesNode.createAddFolderAction(project, JbiCompProjectProperties.JAVAC_CLASSPATH),
                        null,
                        new PreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE), // NOI18N
                    }
                    ),
                };
            } else if (key == TEST_LIBRARIES) {
                result = new Node[] {
                    new LibrariesNode(NbBundle.getMessage(JbiCompLogicalViewProvider.class,"CTL_TestLibrariesNode"),
                            project, evaluator, helper, resolver, JbiCompProjectProperties.RUN_TEST_CLASSPATH,
                            new String[] {
                        JbiCompProjectProperties.BUILD_TEST_CLASSES_DIR,
                        JbiCompProjectProperties.JAVAC_CLASSPATH,
                        JbiCompProjectProperties.BUILD_CLASSES_DIR,
                    },
                            null,
                            new Action[] {
                        LibrariesNode.createAddProjectAction(project, JbiCompProjectProperties.JAVAC_TEST_CLASSPATH),
                        LibrariesNode.createAddLibraryAction(project, JbiCompProjectProperties.JAVAC_TEST_CLASSPATH),
                        LibrariesNode.createAddFolderAction(project, JbiCompProjectProperties.JAVAC_TEST_CLASSPATH),
                        null,
                        new PreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE_TESTS), // NOI18N
                    }
                    ),
                };
            } else if (key instanceof SourceGroupKey) {
                //Source root
                result = new Node[] {new PackageViewFilterNode(((SourceGroupKey) key).group, project)};
                
            }  else if (key == KEY_CONF_FILES ) {
                result = new Node[] {
                    new ConfFilesNode(project)
                };
            }  else if (key == KEY_DEPLOY_PLUGIN ) {
                Project pluginPrj = null;
                String pluginPrjPath = evaluator.getProperty(JbiCompProjectProperties.PROJECT_JBI_DEPLOY_PLUGIN);
                AntProjectHelper projectHelper = helper.getAntProjectHelper();
                if (pluginPrjPath != null) {
                    FileObject prjDirFO = projectHelper.resolveFileObject(pluginPrjPath);
                    try {
                        pluginPrj = ProjectManager.getDefault().findProject(prjDirFO);
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                if ( pluginPrj != null ) {
                    result = new Node[] {
                        new PluginModuleNode(pluginPrj)
                    };
                } else {
                    result = new Node[0];
                }
             
            }  else if (key == KEY_TEST_ASSEMBLIES ) {
                result = createTestAssembliesView();
            } else {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }
        
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setKeys(getKeys());
                }
            });
        }
        
        // Private methods -----------------------------------------------------
        
        private Collection getKeys() {
            //#60800, #61584 - when the project is deleted externally do not try to create children, the source groups
            //are not valid
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.EMPTY_LIST;
            }
            
            List result =  new ArrayList();
            
            result.add(KEY_CONF_FILES);
            
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            
            // List result =  new ArrayList(groups.length);
            
            for( int i = 0; i < groups.length; i++ ) {
                result.add(new SourceGroupKey(groups[i]));
            }
            
            result.add(KEY_TEST_ASSEMBLIES);
            
            result.add(LIBRARIES);
            URL[] testRoots = this.testSources.getRootURLs();
            boolean addTestSources = false;
            for (int i = 0; i < testRoots.length; i++) {
                File f = new File(URI.create(testRoots[i].toExternalForm()));
                if (f.exists()) {
                    addTestSources = true;
                    break;
                }
            }
            if (addTestSources) {
                result.add(TEST_LIBRARIES);
            }
            
            // if plugin project exists add the plugin node
            String pluginPrjPath = evaluator.getProperty(JbiCompProjectProperties.PROJECT_JBI_DEPLOY_PLUGIN);
            // System.out.println("############## PLUGIN PRJ PATH " + pluginPrjPath);
            if ( pluginPrjPath != null && pluginPrjPath.trim().length() > 0 ) {
                result.add(KEY_DEPLOY_PLUGIN);
            }
            
            return result;
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }
        
        private static class SourceGroupKey {
            
            public final SourceGroup group;
            public final FileObject fileObject;
            
            SourceGroupKey(SourceGroup group) {
                this.group = group;
                this.fileObject = group.getRootFolder();
            }
            
            public int hashCode() {
                return fileObject.hashCode();
            }
            
            public boolean equals(Object obj) {
                if (!(obj instanceof SourceGroupKey)) {
                    return false;
                } else {
                    SourceGroupKey otherKey = (SourceGroupKey) obj;
                    String thisDisplayName = this.group.getDisplayName();
                    String otherDisplayName = otherKey.group.getDisplayName();
                    // XXX what is the operator binding order supposed to be here??
                    return fileObject.equals(otherKey.fileObject) &&
                            thisDisplayName == null ? otherDisplayName == null : thisDisplayName.equals(otherDisplayName);
                }
            }
            
        }
        
        private final class WsdlCreationListener extends FileChangeAdapter {
            
            public void fileDataCreated(FileEvent fe) {
                if (WSDL_FOLDER.equalsIgnoreCase(fe.getFile().getExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (WSDL_FOLDER.equalsIgnoreCase(fe.getFile().getExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                } else if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                }
            }
        }
        
        private final class MetaInfListener extends FileChangeAdapter {
            
            public void fileFolderCreated(FileEvent fe) {
                if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    fe.getFile().addFileChangeListener(wsdlListener);
                } else if (fe.getFile().isFolder() && "META-INF".equals(fe.getFile().getName())) { //NOI18N
                    fe.getFile().addFileChangeListener(metaInfListener);
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    fe.getFile().removeFileChangeListener(wsdlListener);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                } else if (fe.getFile().isFolder() && "META-INF".equals(fe.getFile().getName())) { //NOI18N
                    fe.getFile().removeFileChangeListener(metaInfListener);
                }
            }
        }
        
        private final class JaxWsChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        refreshKey(KEY_SERVICE_REFS);
                    }
                });
            }
        }
    }
    
    /** Yet another cool filter node just to add properties action
     */
    private static class PackageViewFilterNode extends FilterNode {
        
        private String nodeName;
        private Project project;
        
        Action[] actions;
        
        public PackageViewFilterNode(SourceGroup sourceGroup, Project project) {
            super(PackageView.createPackageView(sourceGroup));
            this.project = project;
            this.nodeName = "Sources";
        }
        
        
        public Action[] getActions(boolean context) {
            if (!context) {
                if (actions == null) {
                    Action superActions[] = super.getActions(context);
                    actions = new Action[superActions.length + 2];
                    System.arraycopy(superActions, 0, actions, 0, superActions.length);
                    actions[superActions.length] = null;
                    actions[superActions.length + 1] = new PreselectPropertiesAction(project, nodeName);
                }
                return actions;
            } else {
                return super.getActions(context);
            }
        }
        
    }
    
    
    /** The special properties action
     */
    private static class PreselectPropertiesAction extends AbstractAction {
        
        private final Project project;
        private final String nodeName;
        private final String panelName;
        
        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }
        
        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(JbiCompLogicalViewProvider.class, "LBL_Properties_Action"));
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }
        
        public void actionPerformed(ActionEvent e) {
            CustomizerProviderImpl cp = (CustomizerProviderImpl) project.getLookup().lookup(CustomizerProviderImpl.class);
            if (cp != null) {
                cp.showCustomizer(nodeName, panelName);
            }
            
        }
    }
    
    
    private static final class ConfFilesNode extends org.openide.nodes.AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        private static final Image CONFIGURATION_FILES_BADGE = Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/config-badge.gif", true ); // NOI18N
        
        private Node projectNode;
        
        // icon badging >>>
        private Set files;
        private Map fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map groupsListeners;
        private Project project;
        // icon badging <<<
        
        public ConfFilesNode(Project prj) {
            super(ConfFilesChildren.forProject(prj), createLookup(prj));
            this.project = prj;
            setName("configurationFiles"); // NOI18N
            
            FileObject projectDir = prj.getProjectDirectory();
            try {
                DataObject projectDo = DataObject.find(projectDir);
                if (projectDo != null)
                    projectNode = projectDo.getNodeDelegate();
            } catch (DataObjectNotFoundException e) {}
        }
        
        public Image getIcon(int type) {
            Image img = computeIcon(false, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        public Image getOpenedIcon(int type) {
            Image img = computeIcon(true, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        private Image computeIcon(boolean opened, int type) {
            if (projectNode == null)
                return null;
            
            Image image = opened ? projectNode.getOpenedIcon(type) : projectNode.getIcon(type);
            image = Utilities.mergeImages(image, CONFIGURATION_FILES_BADGE, 7, 7);
            return image;
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(JbiCompLogicalViewProvider.class, "LBL_Node_Config"); //NOI18N
        }
        
        public javax.swing.Action[] getActions(boolean context) {
            return new javax.swing.Action[] {
                SystemAction.get(FindAction.class),
            };
        }
        
        public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
        }
        
        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }
            
            synchronized (privateLock) {
                if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
                    Iterator it = files.iterator();
                    while (it.hasNext()) {
                        FileObject fo = (FileObject) it.next();
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }
            
            task.schedule(50);  // batch by 50 ms
        }
        
        public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
        }
        
        protected final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }
        
        private final void setGroups(Collection groups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap();
            Set roots = new HashSet();
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                SourceGroup group = (SourceGroup) it.next();
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                roots.add(fo);
            }
            setFiles(roots);
        }
        
        protected final void setFiles(Set files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
                    FileStatusListener fsl = (FileStatusListener) fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }
            
            fileSystemListeners = new HashMap();
            this.files = files;
            if (files == null) return;
            
            Iterator it = files.iterator();
            Set hookedFileSystems = new HashSet();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not get " + fo + " filesystem, ignoring...");  // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
    }
    
    private static final class ConfFilesChildren extends Children.Keys {
        
        private final static String[] wellKnownFiles = {
            "jbi.xml"
        }; //NOI18N
        
        private FileObject projectDir;
        private HashSet keys;
        private java.util.Comparator comparator = new NodeComparator();
        
        private FileChangeListener metaInfListener = new FileChangeAdapter() {
            public void fileDataCreated(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt()))
                    addKey(fe.getFile());
            }
            
            public void fileRenamed(FileRenameEvent fe) {
                // if the old file name was in keys, the new file name
                // is now there (since it's the same FileObject)
                if (keys.contains(fe.getFile())) {
                    // so we need to remove it if it's not well-known
                    if (!isWellKnownFile(fe.getFile().getNameExt()))
                        removeKey(fe.getFile());
                    else
                        // this causes resorting of the keys
                        doSetKeys();
                } else {
                    // the key is not contained, so add it if it's well-known
                    if (isWellKnownFile(fe.getFile().getNameExt()))
                        addKey(fe.getFile());
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    removeKey(fe.getFile());
                }
            }
        };
        
        private FileChangeListener anyFileListener = new FileChangeAdapter() {
            public void fileDataCreated(FileEvent fe) {
                addKey(fe.getFile());
            }
            
            public void fileFolderCreated(FileEvent fe) {
                addKey(fe.getFile());
            }
            
            public void fileRenamed(FileRenameEvent fe) {
                addKey(fe.getFile());
            }
            
            public void fileDeleted(FileEvent fe) {
                removeKey(fe.getFile());
            }
        };
        
        
        private ConfFilesChildren(FileObject projectDir) {
            this.projectDir = projectDir;
            keys = new HashSet();
        }
        
        public static Children forProject(Project project) {
            return new ConfFilesChildren(project.getProjectDirectory());
        }
        
        public static FileObject findConfigDir(FileObject prjDir) {
            try {
                return FileUtil.createFolder(prjDir, JbiCompProjectProperties.CONF_DIR_VALUE);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        
        protected void addNotify() {
            createKeys();
            doSetKeys();
        }
        
        protected void removeNotify() {
            removeListeners();
        }
        
        public Node[] createNodes(Object key) {
            Node n = null;
            
            if (keys.contains(key)) {
                FileObject fo = (FileObject)key;
                try {
                    DataObject dataObject = DataObject.find(fo);
                    n = dataObject.getNodeDelegate().cloneNode();
                } catch (DataObjectNotFoundException dnfe) {}
            }
            
            return (n == null) ? new Node[0] : new Node[] { n };
        }
        
        public synchronized void refreshNodes() {
            addNotify();
        }
        
        private synchronized void addKey(FileObject key) {
            if (VisibilityQuery.getDefault().isVisible(key)) {
                //System.out.println("Adding " + key.getPath());
                keys.add(key);
                doSetKeys();
            }
        }
        
        private synchronized void removeKey(FileObject key) {
            //System.out.println("Removing " + key.getPath());
            keys.remove(key);
            doSetKeys();
        }
        
        private synchronized void createKeys() {
            keys.clear();
            
            addWellKnownFiles();
            addConfDirectoryFiles();
        }
        
        private void doSetKeys() {
            Object[] result = keys.toArray();
            java.util.Arrays.sort(result, comparator);
            //for (int i = 0; i < result.length; i++)
            //    System.out.println(result[i]);
            setKeys(result);
        }
        
        private void addWellKnownFiles() {
            FileObject confDir = findConfigDir(projectDir);
            if ( confDir == null ) {
                return;
            }
            FileObject metaInf = null;
            try {
                metaInf = FileUtil.createFolder(confDir, "META-INF");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (metaInf == null) {
                return;
            }
            
            for (int i = 0; i < wellKnownFiles.length; i++) {
                FileObject fo = metaInf.getFileObject(wellKnownFiles[i]);
                if (fo != null)
                    keys.add(fo);
            }
            
            metaInf.addFileChangeListener(metaInfListener);
        }
        
        private void addConfDirectoryFiles() {
            //            FileObject conf = findConfigDir(projectDir);
            //            if (conf == null)
            //                return;
            //
            //            FileObject[] children = conf.getChildren();
            //            for (int i = 0; i < children.length; i++) {
            //                if (VisibilityQuery.getDefault().isVisible(children[i]))
            //                    keys.add(children[i]);
            //            }
            //
            //            conf.addFileChangeListener(anyFileListener);
        }
        
        private void removeListeners() {
            
            FileObject metaInf = findConfigDir(projectDir);
            if (metaInf != null) {
                metaInf.removeFileChangeListener(metaInfListener);
            }
            
            //            FileObject conf = pwm.getConfDir();
            //            if (conf != null)
            //                conf.removeFileChangeListener(anyFileListener);
        }
        
        private boolean isWellKnownFile(String name) {
            for (int i = 0; i < wellKnownFiles.length; i++)
                if (name.equals(wellKnownFiles[i]))
                    return true;
            
            return false;
        }
        
        private void dumpKeys() {
            java.util.Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                FileObject fo = (FileObject)iter.next();
                //System.out.println("Key: " + org.openide.filesystems.FileUtil.toFile(fo).getPath()); // NOI18N
            }
        }
        
        private static final class NodeComparator implements java.util.Comparator {
            public int compare(Object o1, Object o2) {
                FileObject fo1 = (FileObject)o1;
                FileObject fo2 = (FileObject)o2;
                
                int result = compareType(fo1, fo2);
                if (result == 0)
                    result = compareNames(fo1, fo2);
                if (result == 0)
                    return fo1.getPath().compareTo(fo2.getPath());
                
                return result;
            }
            
            private int compareType(FileObject fo1, FileObject fo2) {
                int folder1 = fo1.isFolder() ? 0 : 1;
                int folder2 = fo2.isFolder() ? 0 : 1;
                
                return folder1 - folder2;
            }
            
            private int compareNames(FileObject do1, FileObject do2) {
                return do1.getNameExt().compareTo(do2.getNameExt());
            }
            
            public boolean equals(Object o) {
                return (o instanceof NodeComparator);
            }
        }
    }
    
    private static class ConfFilesRefreshAction extends CookieAction {
        
        protected Class[] cookieClasses() {
            return new Class[] { RefreshCookie.class };
        }
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        protected int mode() {
            return CookieAction.MODE_EXACTLY_ONE;
        }
        
        protected boolean asynchronous() {
            return false;
        }
        
        public String getName() {
            return NbBundle.getMessage(JbiCompLogicalViewProvider.class, "LBL_Refresh"); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        public void performAction(Node[] selectedNodes) {
            for (int i = 0; i < selectedNodes.length; i++) {
                RefreshCookie cookie = (RefreshCookie)selectedNodes[i].getCookie(RefreshCookie.class);
                cookie.refresh();
            }
        }
        
        private interface RefreshCookie extends Node.Cookie {
            public void refresh();
        }
    }
    
    /** Represent plugin module node. */
    private static final class TestAssembliesNode extends AbstractNode {
//        private static final Image TEST_ASSEMBLIES_NODE_BADGE = 
//                Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/config-badge.gif", true ); // NOI18N
        Node mProjectNode;
        JbiCompProject mProject;
        
        public TestAssembliesNode(final JbiCompProject prj) {
            
            super(new TestAssembliesNodeChildren(prj), Lookups.fixed(new Object[] {prj}) );
            
            this.mProject = prj;
            setName("testAssemblies"); // NOI18N
            
            FileObject projectDir = prj.getProjectDirectory();
            try {
                DataObject projectDo = DataObject.find(projectDir);
                if (projectDo != null)
                    mProjectNode = projectDo.getNodeDelegate();
            } catch (DataObjectNotFoundException e) {}
            
        }
                
        public String getDisplayName() {
            return NbBundle.getMessage(JbiCompLogicalViewProvider.class, "LBL_TestAssembliesNode"); //NOI18N
        }
        
        public Image getIcon(int type) {
            Image img = computeIcon(false, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        public Image getOpenedIcon(int type) {
            Image img = computeIcon(true, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        private Image computeIcon(boolean opened, int type) {
            if (mProjectNode == null)
                return null;            
            Image image = opened ? mProjectNode.getOpenedIcon(type) : mProjectNode.getIcon(type);
            // image = Utilities.mergeImages(image, TEST_ASSEMBLIES_NODE_BADGE, 7, 7);
            return image;
        }
             
    }
    
 private static final class TestAssembliesNodeChildren extends Children.Keys {
                
        private JbiCompProject mProject;
        private HashSet keys;
        
        public TestAssembliesNodeChildren(JbiCompProject project) {
            this.mProject = project;
            keys = new HashSet();
        }
        
//        public Children forProject(Project project) {
//            return new TestAssembliesNodeChildren(project.getProjectDirectory());
//        }
        
//        public static FileObject findTestSADir(FileObject prjDir) {
//            try {
//                return FileUtil.createFolder(prjDir, JbiCompProjectProperties.PROJECT_TEST_SA_VALUE);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                return null;
//            }
//        }
        
        protected void addNotify() {
            createKeys();
            doSetKeys();
        }
        
        protected void removeNotify() {
            removeListeners();
        }
        
        public Node[] createNodes(Object key) {
            Node n = null;
            
            if (keys.contains(key)) {
                FileObject fo = (FileObject)key;
                try {
                    DataObject dataObject = DataObject.find(fo);
                    n = dataObject.getNodeDelegate().cloneNode();
                } catch (DataObjectNotFoundException dnfe) {}
            }
            
            return (n == null) ? new Node[0] : new Node[] { n };
        }
        
        public synchronized void refreshNodes() {
            addNotify();
        }
        
        private synchronized void addKey(FileObject key) {
            if (VisibilityQuery.getDefault().isVisible(key)) {
                //System.out.println("Adding " + key.getPath());
                keys.add(key);
                doSetKeys();
            }
        }
        
        private synchronized void removeKey(FileObject key) {
            //System.out.println("Removing " + key.getPath());
            keys.remove(key);
            doSetKeys();
        }
        
        private synchronized void createKeys() {
            keys.clear();
            addTestAssemblyDirectories();
        }
        
        private void doSetKeys() {
            Object[] result = keys.toArray();
            // java.util.Arrays.sort(result, comparator);
            //for (int i = 0; i < result.length; i++)
            //    System.out.println(result[i]);
            setKeys(result);
        }
        
        private void addTestAssemblyDirectories() {
            
                FileObject testSAFO = null;
                
                String testSAProp = JbiCompProjectProperties.PROJECT_TEST_SA_VALUE; //NOI18N
                // System.out.println("TestAssembliesNodeChildern: testSA Prop" + testSAProp);
                if ( testSAProp != null ) {
                    AntProjectHelper projectHelper = this.mProject.getAntProjectHelper();
                    testSAFO = projectHelper.resolveFileObject(testSAProp);
                }
                if ( testSAFO != null ) {
                    keys.add(testSAFO);
                }
        }
                
        private void removeListeners() {            
        }
                
        private void dumpKeys() {
            java.util.Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                FileObject fo = (FileObject)iter.next();
                //System.out.println("Key: " + org.openide.filesystems.FileUtil.toFile(fo).getPath()); // NOI18N
            }
        }
        
    }    
    
    /** Represent plugin module node. */
    private static final class PluginModuleNode extends AbstractNode {
        
        public static final String NB_PROJECT_ICON_PATH =
                "org/netbeans/modules/jbi/apisupport/resources/images/module.gif"; // NOI18N
        
        private static final Image NB_PROJECT_BADGE =
                Utilities.loadImage( "org/netbeans/modules/jbi/apisupport/resources/images/shortcut.png", true ); // NOI18N
        
        private final static Action OPEN_ACTION = new OpenProjectAction();
        
        public PluginModuleNode(final Project pluginProject) {
            super(Children.LEAF, Lookups.fixed(new Object[] {pluginProject}));
            ProjectInformation info = ProjectUtils.getInformation(pluginProject);
            setName(info.getName());
            setDisplayName(info.getDisplayName());
            setIconBaseWithExtension(NB_PROJECT_ICON_PATH);
            info.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == ProjectInformation.PROP_DISPLAY_NAME) {
                        PluginModuleNode.this.setDisplayName((String) evt.getNewValue());
                    } else if (evt.getPropertyName() == ProjectInformation.PROP_NAME) {
                        PluginModuleNode.this.setName((String) evt.getNewValue());
                    }
                }
            });
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                OPEN_ACTION
            };
        }
        
        public Action getPreferredAction() {
            return OPEN_ACTION;
        }
        
        public Image getIcon(int type) {
            Image img = computeIcon(false, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        public Image getOpenedIcon(int type) {
            Image img = computeIcon(true, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        private Image computeIcon(boolean opened, int type) {
            Image image = Utilities.loadImage( NB_PROJECT_ICON_PATH, true ); // NOI18N
            image = Utilities.mergeImages(image, NB_PROJECT_BADGE, 12, 8);
            // image = Utilities.mergeImages(image, NB_PROJECT_BADGE, 8, 0);
            return image;
        }
        
        
    }
    
    private static final class OpenProjectAction extends CookieAction {
        
        protected void performAction(Node[] activatedNodes) {
            final Project[] projects = new Project[activatedNodes.length];
            for (int i = 0; i < activatedNodes.length; i++) {
                Project project = (Project) activatedNodes[i].getLookup().lookup(Project.class);
                projects[i] = project;
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    String previousText = StatusDisplayer.getDefault().getStatusText();
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(JbiCompLogicalViewProvider.class, "MSG_OpeningProjects"));
                    OpenProjects.getDefault().open(projects, false);
                    StatusDisplayer.getDefault().setStatusText(previousText);
                }
            });
        }
        
        public String getName() {
            return NbBundle.getMessage(JbiCompLogicalViewProvider.class, "CTL_OpenProject");
        }
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        protected boolean asynchronous() {
            return false;
        }
        
        protected int mode() {
            return CookieAction.MODE_ALL;
        }
        
        protected Class[] cookieClasses() {
            return new Class[] { Project.class };
        }
        
    }
    
    
}

