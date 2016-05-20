/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openesb.components.camelse.nb.plugin.project.node;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.openesb.components.camelse.nb.plugin.project.SEPluginProjectProperties;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
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
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author chikkala
 */
public class ConfigFilesNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {

    private static final Image CONFIG_FILES_FOLDER = Utilities.loadImage("org/openesb/components/camelse/nb/plugin/project/resources/folder.png", true); // NOI18N

    private static final Image CONFIG_FILES_BADGE = Utilities.loadImage("org/openesb/components/camelse/nb/plugin/project/resources/config-badge.gif", true); // NOI18N

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

    public ConfigFilesNode(Project prj) {
        super(ConfFilesChildren.forProject(prj), createLookup(prj));
        this.project = prj;
        setName("configurationFiles"); // NOI18N

        FileObject projectDir = prj.getProjectDirectory();
        try {
            DataObject projectDo = DataObject.find(projectDir);
            if (projectDo != null) {
                projectNode = projectDo.getNodeDelegate();
            }
        } catch (DataObjectNotFoundException e) {
        }
    }

    @Override
    public Image getIcon(int type) {
        Image img = computeIcon(false, type);
        return (img != null) ? img : super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image img = computeIcon(true, type);
        return (img != null) ? img : super.getIcon(type);
    }

    private Image computeIcon(boolean opened, int type) {
        if (projectNode == null) {
            return null;
        }
        Image image = opened ? projectNode.getOpenedIcon(type) : projectNode.getIcon(type);
        image = Utilities.mergeImages(image, CONFIG_FILES_BADGE, 7, 7);
        return image;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConfigFilesNode.class, "LBL_Node_Config"); //NOI18N

    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        return new javax.swing.Action[]{
                    SystemAction.get(FindAction.class),
                };
    }

    private static Lookup createLookup(Project project) {
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed(new Object[]{project, rootFolder});
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
                err.annotate(e, "Can not get " + fo + " filesystem, ignoring...");  // NO18N

                err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    private static final class ConfFilesChildren extends Children.Keys {

        private final static String[] wellKnownFiles = {
            "jbi.xml",
            "spring/camel-context.xml"
        }; //NOI18N

        private FileObject projectDir;
        private HashSet keys;
        private java.util.Comparator comparator = new NodeComparator();
        private FileChangeListener metaInfListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    addKey(fe.getFile());
                }
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                // if the old file name was in keys, the new file name
                // is now there (since it's the same FileObject)
                if (keys.contains(fe.getFile())) {
                    // so we need to remove it if it's not well-known
                    if (!isWellKnownFile(fe.getFile().getNameExt())) {
                        removeKey(fe.getFile());
                    } else // this causes resorting of the keys
                    {
                        doSetKeys();
                    }
                } else {
                    // the key is not contained, so add it if it's well-known
                    if (isWellKnownFile(fe.getFile().getNameExt())) {
                        addKey(fe.getFile());
                    }
                }
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    removeKey(fe.getFile());
                }
            }
        };
        private FileChangeListener anyFileListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                addKey(fe.getFile());
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                addKey(fe.getFile());
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                addKey(fe.getFile());
            }

            @Override
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
            return prjDir.getFileObject(SEPluginProjectProperties.SRC_DIR_VALUE);
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
                FileObject fo = (FileObject) key;
                try {
                    DataObject dataObject = DataObject.find(fo);
                    n = dataObject.getNodeDelegate().cloneNode();
                } catch (DataObjectNotFoundException dnfe) {
                }
            }

            return (n == null) ? new Node[0] : new Node[]{n};
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
            
            FileObject suConfigFO = null;
            // suConfigFO = projectDir.getFileObject(SEPluginProjectProperties.SU_CONFIG_XML_PATH);
            if ( suConfigFO != null ) {
                keys.add(suConfigFO);
            }
            FileObject confDir = findConfigDir(projectDir);
            if (confDir == null) {
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
                if (fo != null) {
                    keys.add(fo);
                }
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
            for (int i = 0; i < wellKnownFiles.length; i++) {
                if (name.equals(wellKnownFiles[i])) {
                    return true;
                }
            }
            return false;
        }

        private void dumpKeys() {
            java.util.Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                FileObject fo = (FileObject) iter.next();
            //System.out.println("Key: " + org.openide.filesystems.FileUtil.toFile(fo).getPath()); // NOI18N
            }
        }

        private static final class NodeComparator implements java.util.Comparator {

            public int compare(Object o1, Object o2) {
                FileObject fo1 = (FileObject) o1;
                FileObject fo2 = (FileObject) o2;

                int result = compareType(fo1, fo2);
                if (result == 0) {
                    result = compareNames(fo1, fo2);
                }
                if (result == 0) {
                    return fo1.getPath().compareTo(fo2.getPath());
                }
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
            return new Class[]{RefreshCookie.class};
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
            return NbBundle.getMessage(ConfigFilesNode.class, "LBL_Refresh"); //NOI18N

        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void performAction(Node[] selectedNodes) {
            for (int i = 0; i < selectedNodes.length; i++) {
                RefreshCookie cookie = (RefreshCookie) selectedNodes[i].getCookie(RefreshCookie.class);
                cookie.refresh();
            }
        }

        private interface RefreshCookie extends Node.Cookie {

            public void refresh();
        }
    }
}
    
