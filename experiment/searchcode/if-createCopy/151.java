/*
 * ClientView.java
 */
package client;

import Classes.Config;
import Classes.DebugClass;
import Classes.EventTypesForClientPlugin;
import Events.OnConnectDisconnectEvent;
import Events.OnConnectDisconnectEventInterface;
import Interfaces.IClient;
import Interfaces.IClientPlugin;
import Interfaces.INetClientPlugin;
import Interfaces.IPluginsBase;
import Shells.IClientShell;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sql.rowset.CachedRowSet;
import java.util.Iterator;
import java.awt.Toolkit;
import java.io.File;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * The application's main frame.
 */
public class ClientView extends FrameView implements IClient, Interfaces.IClientForPlugin {

    private DebugClass debugClass = new DebugClass(this);
    public Config config;
    public int CurentFilter;
    private Boolean InitializeFinal;
    private String lastProject = "";
    public CachedRowSet vProj = null;
    private String QUERY_STATUSES = "";
    int N_COUNT_QA_PARAMS = 17;
    public Vector<String> _CurrentUser;
    int nProj;
    public NodeData curNode;
    public String LangFile;

    public Vector<String> getCurrentUser()
    {
        return _CurrentUser;
    }

    public class TreeSaving {

        public TreeModel Model;
        public Enumeration<javax.swing.tree.TreePath> ExpandPath;
    }
    public Vector<TreeSaving> tabs_array;

    @Override
    protected void finalize() throws Throwable {
        Net.DisconnectFromServer();
        super.finalize();
    }

    public ClientView(SingleFrameApplication app) {
        super(app);
        InitializeFinal = false;
        _CurrentUser = new Vector<String>();
        initComponents();
        tree.setCellRenderer(new ImageTreeRender());
        tree2.setCellRenderer(new ImageTreeRender());
        tree.setModel(new DefaultTreeModel(null));
        tree2.setModel(new DefaultTreeModel(null));
        QAtree.setModel(new DefaultTreeModel(null));
        QAtree.setCellRenderer(new ImageTreeRender());
        config = new Config("config.xml");
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        String ssq = config.get("[Config]->[Filters]->current");
        if (ssq.equals("")) {
            CurentFilter = 0;
            config.set("[Config]->[Filters]->current", "0");
            config.set("[Config]->[Filters]->count", "1");
            setFilterProperty(CurentFilter, "name", GetLang("Client", "DefaultFilterName", "?????"));
        } else {
            CurentFilter = Integer.parseInt(ssq);
        }

        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel0.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);
        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel0.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        setInterfaceLang();
        _pluginService = PluginsLoaderFactory.createPluginService();
        _pluginService.initPlugins();
        Iterator<IPluginsBase> iterator = _pluginService.getPlugins();
        if (!iterator.hasNext()) {
            System.err.println("No plugins were found! Shutdown");
            System.exit(0);
        }
        String NetPluginName = config.get("[Config]->[Network]->Plugin");
        Boolean NetPluginInit = false;
        while (iterator.hasNext()) {
            IPluginsBase plugin = (IPluginsBase) iterator.next();
            switch (plugin.GetType()) {
                case Database:
                    break;
                case Net:
                    if (plugin.getClass().getName().equals(NetPluginName)) {
                        while (!AutorizateUser((IClientShell) plugin)) {
                        }
                        Net = (INetClientPlugin) plugin;
                        NetPluginInit = true;
                    }
                    break;
                case Shell:
                    break;
            }
            Class[] IntPlugins = plugin.getClass().getInterfaces();
            for (int i = 0; i < IntPlugins.length; i++) {
                if (IntPlugins[i].getClass().getName().equals("OnConnectDisconnectEventInterface")) {
                    this.AddConnectDisconnectListener((OnConnectDisconnectEventInterface) plugin);
                }
            }
        }
        statusMessageLabel0.setText(_CurrentUser.get(6));
        statusMessageLabel1.setText(_CurrentUser.get(1));
        if (getFilterProperty(CurentFilter, "show_draft").equals("")) {
            setFilterProperty(CurentFilter, "show_draft", "1");
        }

        TabSheet1.setVisible(getFilterProperty(CurentFilter, "show_draft").equals("1"));
        TabSheet2.setVisible(getFilterProperty(CurentFilter, "show_draft").equals("1"));

        if (getFilterProperty(CurentFilter, "show_draft").equals("1")) {
            TabSheet1.add(RichEdit1);
        } else {
            mainPanel.add(RichEdit1);
        }
        if (config.get("[Config]->[ClientOptions]->language").equals("")) {
            LangFile = "Russian.xml";
        } else {
            LangFile = config.get("[Config]->[ClientOptions]->language");
        }
        if (config.get("[Config]->[ClientOptions]->current_view").equals("")) {
            SetView(0);

        } else {
            SetView(Integer.parseInt(config.get("[Config]->[ClientOptions]->current_view")));

        }
        if (!NetPluginInit) {
            System.err.println("Error! Network plugin " + NetPluginName + " not found! Shutdown.");
            System.exit(0);
        }
        try {
            vProj = Net.GetProjects(_CurrentUser);
            CachedRowSet tProj = vProj.createCopy();
            cbProj.removeAllItems();
            int Count = 0;
            cbProj.addItem("???????? ??????????");
            while (tProj.next()) {
                cbProj.addItem(tProj.getString(2));
                Count++;
            }
            Count *= 2;
            tabs_array = new Vector<TreeSaving>();
            for (int i = 0; i < Count; i++) {
                tabs_array.add(null);
            }



        } catch (Exception ex) {
            debugClass.Except(ex);
        }
        iterator = _pluginService.getPlugins();
        while (iterator.hasNext()) {
            IPluginsBase plugin = (IPluginsBase) iterator.next();
            switch (plugin.GetType()) {
                case Other:
                    ((IClientPlugin) plugin).Init(this);
                    break;
            }
        }
        pmnu_PRemove.setVisible((_CurrentUser.get(3).equals("2")));
        N15.setVisible((_CurrentUser.get(3).equals("2")));
        pmnu_remove.setVisible((_CurrentUser.get(3).equals("2")));
        menuTests.setEnabled(!(_CurrentUser.get(3).equals("2")));
        menuTTask.setEnabled(!(_CurrentUser.get(3).equals("2")));
        menuDemos.setEnabled(!(_CurrentUser.get(3).equals("2")));
        jSplitPane2.setDividerLocation(0);
        InitializeFinal = true;

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = ClientApp.getApplication().getMainFrame();
            aboutBox = new ClientAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ClientApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        pp1 = new javax.swing.JPanel();
        pp1inner = new javax.swing.JSplitPane();
        pp1innerLeft = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        tree = new javax.swing.JTree();
        tabs = new javax.swing.JTabbedPane();
        Panel1 = new javax.swing.JPanel();
        Label1 = new javax.swing.JLabel();
        Label2 = new javax.swing.JLabel();
        Label3 = new javax.swing.JLabel();
        Label4 = new javax.swing.JLabel();
        Label5 = new javax.swing.JLabel();
        Label6 = new javax.swing.JLabel();
        PageControl1 = new javax.swing.JTabbedPane();
        TabSheet1 = new javax.swing.JPanel();
        RichEdit1 = new javax.swing.JTextArea();
        TabSheet2 = new javax.swing.JPanel();
        RichEdit2 = new javax.swing.JTextArea();
        pp1innerRight = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        tabs2 = new javax.swing.JTabbedPane();
        tree2 = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        QAtree = new javax.swing.JTree();
        pp2 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        Progect1 = new javax.swing.JMenu();
        N4 = new javax.swing.JMenuItem();
        N10 = new javax.swing.JMenu();
        mnu_cur = new javax.swing.JMenu();
        N9 = new javax.swing.JMenuItem();
        N8 = new javax.swing.JPopupMenu.Separator();
        N12 = new javax.swing.JMenuItem();
        N13 = new javax.swing.JMenuItem();
        N2 = new javax.swing.JPopupMenu.Separator();
        mnu_MSWord = new javax.swing.JMenuItem();
        mnu_XT = new javax.swing.JMenuItem();
        N11 = new javax.swing.JPopupMenu.Separator();
        FindMnu = new javax.swing.JMenuItem();
        MNUplugin = new javax.swing.JMenu();
        N7 = new javax.swing.JPopupMenu.Separator();
        N14 = new javax.swing.JMenuItem();
        N16 = new javax.swing.JMenuItem();
        N17 = new javax.swing.JMenuItem();
        mnu_filter = new javax.swing.JMenuItem();
        MNUproperty = new javax.swing.JMenuItem();
        N15 = new javax.swing.JMenu();
        menuQA1 = new javax.swing.JMenuItem();
        menuQA2 = new javax.swing.JMenuItem();
        menuQA3 = new javax.swing.JMenuItem();
        menuDisc = new javax.swing.JMenuItem();
        menuUsers = new javax.swing.JMenuItem();
        menuDemos = new javax.swing.JMenuItem();
        menuTests = new javax.swing.JMenuItem();
        menuTTask = new javax.swing.JMenuItem();
        javax.swing.JMenu N1 = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel1 = new javax.swing.JLabel();
        statusMessageLabel0 = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jSeparator6 = new javax.swing.JSeparator();
        ToolBarMain = new javax.swing.JToolBar();
        cbProj = new javax.swing.JComboBox();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        AddPrjButton = new javax.swing.JButton();
        ExpDiscButton = new javax.swing.JButton();
        ImpDiscButton = new javax.swing.JButton();
        MenuButton = new javax.swing.JButton();
        CopyAllButton = new javax.swing.JButton();
        CopyOneButton = new javax.swing.JButton();
        FilterButton = new javax.swing.JButton();
        FindButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        WordButton = new javax.swing.JButton();
        TxtButton = new javax.swing.JButton();
        PopupMenu1 = new javax.swing.JPopupMenu();
        pmnu_addQ = new javax.swing.JMenuItem();
        pmnu_addA = new javax.swing.JMenuItem();
        pmnu_addZ = new javax.swing.JMenuItem();
        split2 = new javax.swing.JPopupMenu.Separator();
        pmnu_chStat = new javax.swing.JMenuItem();
        pmnu_edit = new javax.swing.JMenuItem();
        pmnu_edit_ch = new javax.swing.JMenuItem();
        pmnu_remove = new javax.swing.JMenuItem();
        split3 = new javax.swing.JPopupMenu.Separator();
        pmnu_history = new javax.swing.JMenuItem();
        pmnu_all = new javax.swing.JMenuItem();
        PopupMenu2 = new javax.swing.JPopupMenu();
        MenuItem3 = new javax.swing.JMenuItem();
        pmnu_PRemove = new javax.swing.JMenuItem();

        mainPanel.setAlignmentX(0.0F);
        mainPanel.setAlignmentY(0.0F);
        mainPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainPanel.setName("mainPanel"); // NOI18N

        pp1.setName("pp1"); // NOI18N

        pp1inner.setDividerLocation(mainPanel.getWidth() / 2);
        pp1inner.setName("pp1inner"); // NOI18N

        pp1innerLeft.setName("pp1innerLeft"); // NOI18N

        jSplitPane1.setDividerLocation((mainPanel.getHeight() / 3) * 2);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        tree.setName("tree"); // NOI18N
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                treeMouseReleased(evt);
            }
        });
        tree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                treeTreeExpanded(evt);
            }
        });
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeValueChanged(evt);
            }
        });

        tabs.setName("tabs"); // NOI18N
        tabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabsStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
            .addComponent(tree, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tree, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        Panel1.setName("Panel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(client.ClientApp.class).getContext().getResourceMap(ClientView.class);
        Label1.setText(resourceMap.getString("Label1.text")); // NOI18N
        Label1.setName("Label1"); // NOI18N

        Label2.setText(resourceMap.getString("Label2.text")); // NOI18N
        Label2.setName("Label2"); // NOI18N

        Label3.setText(resourceMap.getString("Label3.text")); // NOI18N
        Label3.setName("Label3"); // NOI18N

        Label4.setText(resourceMap.getString("Label4.text")); // NOI18N
        Label4.setName("Label4"); // NOI18N

        Label5.setText(resourceMap.getString("Label5.text")); // NOI18N
        Label5.setName("Label5"); // NOI18N

        Label6.setText(resourceMap.getString("Label6.text")); // NOI18N
        Label6.setName("Label6"); // NOI18N

        PageControl1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        PageControl1.setName("PageControl1"); // NOI18N

        TabSheet1.setName("TabSheet1"); // NOI18N

        RichEdit1.setColumns(20);
        RichEdit1.setEditable(false);
        RichEdit1.setRows(5);
        RichEdit1.setText(resourceMap.getString("RichEdit1.text")); // NOI18N
        RichEdit1.setWrapStyleWord(true);
        RichEdit1.setName("RichEdit1"); // NOI18N

        javax.swing.GroupLayout TabSheet1Layout = new javax.swing.GroupLayout(TabSheet1);
        TabSheet1.setLayout(TabSheet1Layout);
        TabSheet1Layout.setHorizontalGroup(
            TabSheet1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RichEdit1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
        );
        TabSheet1Layout.setVerticalGroup(
            TabSheet1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RichEdit1, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
        );

        PageControl1.addTab(resourceMap.getString("TabSheet1.TabConstraints.tabTitle"), TabSheet1); // NOI18N

        TabSheet2.setName("TabSheet2"); // NOI18N

        RichEdit2.setColumns(20);
        RichEdit2.setEditable(false);
        RichEdit2.setRows(5);
        RichEdit2.setWrapStyleWord(true);
        RichEdit2.setName("RichEdit2"); // NOI18N
        RichEdit2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                RichEdit2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                RichEdit2MouseReleased(evt);
            }
        });

        javax.swing.GroupLayout TabSheet2Layout = new javax.swing.GroupLayout(TabSheet2);
        TabSheet2.setLayout(TabSheet2Layout);
        TabSheet2Layout.setHorizontalGroup(
            TabSheet2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RichEdit2, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
        );
        TabSheet2Layout.setVerticalGroup(
            TabSheet2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RichEdit2, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
        );

        PageControl1.addTab(resourceMap.getString("TabSheet2.TabConstraints.tabTitle"), TabSheet2); // NOI18N

        javax.swing.GroupLayout Panel1Layout = new javax.swing.GroupLayout(Panel1);
        Panel1.setLayout(Panel1Layout);
        Panel1Layout.setHorizontalGroup(
            Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Label4)
                    .addComponent(Label5)
                    .addComponent(Label6))
                .addGap(18, 18, 18)
                .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Label1)
                    .addComponent(Label2)
                    .addComponent(Label3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(PageControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        );
        Panel1Layout.setVerticalGroup(
            Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel1Layout.createSequentialGroup()
                        .addComponent(Label1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Label2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Label3))
                    .addGroup(Panel1Layout.createSequentialGroup()
                        .addComponent(Label4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Label5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Label6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PageControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(Panel1);

        javax.swing.GroupLayout pp1innerLeftLayout = new javax.swing.GroupLayout(pp1innerLeft);
        pp1innerLeft.setLayout(pp1innerLeftLayout);
        pp1innerLeftLayout.setHorizontalGroup(
            pp1innerLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
        );
        pp1innerLeftLayout.setVerticalGroup(
            pp1innerLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
        );

        pp1inner.setLeftComponent(pp1innerLeft);

        pp1innerRight.setName("pp1innerRight"); // NOI18N

        jSplitPane2.setDividerLocation(0);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        tabs2.setName("tabs2"); // NOI18N
        tabs2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabs2StateChanged(evt);
            }
        });

        tree2.setName("tree2"); // NOI18N
        tree2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tree2MouseReleased(evt);
            }
        });
        tree2.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                tree2TreeExpanded(evt);
            }
        });
        tree2.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                tree2ValueChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 958, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(tabs2, javax.swing.GroupLayout.DEFAULT_SIZE, 958, Short.MAX_VALUE)
                .addComponent(tree2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 958, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(tabs2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(tree2, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)))
        );

        jSplitPane2.setLeftComponent(jPanel2);

        jPanel3.setName("jPanel3"); // NOI18N

        QAtree.setName("QAtree"); // NOI18N
        QAtree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                QAtreeMouseReleased(evt);
            }
        });
        QAtree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                QAtreeTreeExpanded(evt);
            }
        });
        QAtree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                QAtreeValueChanged(evt);
            }
        });

        pp2.setName("pp2"); // NOI18N

        javax.swing.GroupLayout pp2Layout = new javax.swing.GroupLayout(pp2);
        pp2.setLayout(pp2Layout);
        pp2Layout.setHorizontalGroup(
            pp2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        pp2Layout.setVerticalGroup(
            pp2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(566, Short.MAX_VALUE)
                .addComponent(pp2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(292, 292, 292))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(QAtree, javax.swing.GroupLayout.DEFAULT_SIZE, 958, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(pp2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(592, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(QAtree, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 840, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel3);

        javax.swing.GroupLayout pp1innerRightLayout = new javax.swing.GroupLayout(pp1innerRight);
        pp1innerRight.setLayout(pp1innerRightLayout);
        pp1innerRightLayout.setHorizontalGroup(
            pp1innerRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
        );
        pp1innerRightLayout.setVerticalGroup(
            pp1innerRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pp1innerRightLayout.createSequentialGroup()
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
                .addContainerGap())
        );

        pp1inner.setRightComponent(pp1innerRight);

        javax.swing.GroupLayout pp1Layout = new javax.swing.GroupLayout(pp1);
        pp1.setLayout(pp1Layout);
        pp1Layout.setHorizontalGroup(
            pp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1075, Short.MAX_VALUE)
            .addGroup(pp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pp1inner, javax.swing.GroupLayout.DEFAULT_SIZE, 1075, Short.MAX_VALUE))
        );
        pp1Layout.setVerticalGroup(
            pp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 860, Short.MAX_VALUE)
            .addGroup(pp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pp1inner, javax.swing.GroupLayout.DEFAULT_SIZE, 860, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(pp1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        Progect1.setText(resourceMap.getString("Progect1.text")); // NOI18N
        Progect1.setName("Progect1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(client.ClientApp.class).getContext().getActionMap(ClientView.class, this);
        N4.setAction(actionMap.get("quit")); // NOI18N
        N4.setText(resourceMap.getString("N4.text")); // NOI18N
        N4.setToolTipText(resourceMap.getString("N4.toolTipText")); // NOI18N
        N4.setName("N4"); // NOI18N
        N4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                N4ActionPerformed(evt);
            }
        });
        Progect1.add(N4);

        menuBar.add(Progect1);

        N10.setText(resourceMap.getString("N10.text")); // NOI18N
        N10.setName("N10"); // NOI18N

        mnu_cur.setText(resourceMap.getString("mnu_cur.text")); // NOI18N
        mnu_cur.setName("mnu_cur"); // NOI18N

        N9.setText(resourceMap.getString("N9.text")); // NOI18N
        N9.setEnabled(false);
        N9.setName("N9"); // NOI18N
        mnu_cur.add(N9);

        N10.add(mnu_cur);

        N8.setName("N8"); // NOI18N
        N10.add(N8);

        N12.setText(resourceMap.getString("N12.text")); // NOI18N
        N12.setName("N12"); // NOI18N
        N12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                N12ActionPerformed(evt);
            }
        });
        N10.add(N12);

        N13.setText(resourceMap.getString("N13.text")); // NOI18N
        N13.setName("N13"); // NOI18N
        N13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                N13ActionPerformed(evt);
            }
        });
        N10.add(N13);

        N2.setName("N2"); // NOI18N
        N10.add(N2);

        mnu_MSWord.setText(resourceMap.getString("mnu_MSWord.text")); // NOI18N
        mnu_MSWord.setActionCommand(resourceMap.getString("mnu_MSWord.actionCommand")); // NOI18N
        mnu_MSWord.setName("mnu_MSWord"); // NOI18N
        mnu_MSWord.setRequestFocusEnabled(false);
        mnu_MSWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnu_MSWordActionPerformed(evt);
            }
        });
        N10.add(mnu_MSWord);

        mnu_XT.setText(resourceMap.getString("mnu_XT.text")); // NOI18N
        mnu_XT.setName("mnu_XT"); // NOI18N
        mnu_XT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnu_XTActionPerformed(evt);
            }
        });
        N10.add(mnu_XT);

        N11.setName("N11"); // NOI18N
        N10.add(N11);

        FindMnu.setText(resourceMap.getString("FindMnu.text")); // NOI18N
        FindMnu.setName("FindMnu"); // NOI18N
        FindMnu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindMnuActionPerformed(evt);
            }
        });
        N10.add(FindMnu);

        menuBar.add(N10);

        MNUplugin.setText(resourceMap.getString("MNUplugin.text")); // NOI18N
        MNUplugin.setName("MNUplugin"); // NOI18N

        N7.setName("N7"); // NOI18N
        MNUplugin.add(N7);

        N14.setText(resourceMap.getString("N14.text")); // NOI18N
        N14.setName("N14"); // NOI18N
        N14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                N14ActionPerformed(evt);
            }
        });
        MNUplugin.add(N14);

        N16.setText(resourceMap.getString("N16.text")); // NOI18N
        N16.setName("N16"); // NOI18N
        N16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                N16ActionPerformed(evt);
            }
        });
        MNUplugin.add(N16);

        N17.setText(resourceMap.getString("N17.text")); // NOI18N
        N17.setName("N17"); // NOI18N
        N17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                N17ActionPerformed(evt);
            }
        });
        MNUplugin.add(N17);

        mnu_filter.setText(resourceMap.getString("mnu_filter.text")); // NOI18N
        mnu_filter.setName("mnu_filter"); // NOI18N
        mnu_filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnu_filterActionPerformed(evt);
            }
        });
        MNUplugin.add(mnu_filter);

        MNUproperty.setText(resourceMap.getString("MNUproperty.text")); // NOI18N
        MNUproperty.setName("MNUproperty"); // NOI18N
        MNUproperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MNUpropertyActionPerformed(evt);
            }
        });
        MNUplugin.add(MNUproperty);

        menuBar.add(MNUplugin);

        N15.setText(resourceMap.getString("N15.text")); // NOI18N
        N15.setName("N15"); // NOI18N

        menuQA1.setText(resourceMap.getString("menuQA1.text")); // NOI18N
        menuQA1.setName("menuQA1"); // NOI18N
        menuQA1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuQA1ActionPerformed(evt);
            }
        });
        N15.add(menuQA1);

        menuQA2.setText(resourceMap.getString("menuQA2.text")); // NOI18N
        menuQA2.setName("menuQA2"); // NOI18N
        menuQA2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuQA2ActionPerformed(evt);
            }
        });
        N15.add(menuQA2);

        menuQA3.setText(resourceMap.getString("menuQA3.text")); // NOI18N
        menuQA3.setName("menuQA3"); // NOI18N
        menuQA3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuQA3ActionPerformed(evt);
            }
        });
        N15.add(menuQA3);

        menuDisc.setText(resourceMap.getString("menuDisc.text")); // NOI18N
        menuDisc.setName("menuDisc"); // NOI18N
        menuDisc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDiscActionPerformed(evt);
            }
        });
        N15.add(menuDisc);

        menuUsers.setText(resourceMap.getString("menuUsers.text")); // NOI18N
        menuUsers.setName("menuUsers"); // NOI18N
        menuUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuUsersActionPerformed(evt);
            }
        });
        N15.add(menuUsers);

        menuDemos.setText(resourceMap.getString("menuDemos.text")); // NOI18N
        menuDemos.setName("menuDemos"); // NOI18N
        menuDemos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDemosActionPerformed(evt);
            }
        });
        N15.add(menuDemos);

        menuTests.setText(resourceMap.getString("menuTests.text")); // NOI18N
        menuTests.setName("menuTests"); // NOI18N
        menuTests.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTestsActionPerformed(evt);
            }
        });
        N15.add(menuTests);

        menuTTask.setText(resourceMap.getString("menuTTask.text")); // NOI18N
        menuTTask.setName("menuTTask"); // NOI18N
        menuTTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTTaskActionPerformed(evt);
            }
        });
        N15.add(menuTTask);

        menuBar.add(N15);

        N1.setText(resourceMap.getString("N1.text")); // NOI18N
        N1.setName("N1"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        N1.add(aboutMenuItem);

        menuBar.add(N1);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel1.setText(resourceMap.getString("statusMessageLabel1.text")); // NOI18N
        statusMessageLabel1.setName("statusMessageLabel1"); // NOI18N

        statusMessageLabel0.setText(resourceMap.getString("statusMessageLabel0.text")); // NOI18N
        statusMessageLabel0.setName("statusMessageLabel0"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setText(resourceMap.getString("statusAnimationLabel.text")); // NOI18N
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator6.setName("jSeparator6"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1095, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel0)
                .addGap(33, 33, 33)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusMessageLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 787, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusMessageLabel0, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                    .addComponent(statusMessageLabel1)
                    .addComponent(statusAnimationLabel))
                .addContainerGap())
        );

        ToolBarMain.setRollover(true);
        ToolBarMain.setAlignmentX(0.0F);
        ToolBarMain.setMinimumSize(new java.awt.Dimension(609, 18));
        ToolBarMain.setName("ToolBarMain"); // NOI18N
        ToolBarMain.setPreferredSize(new java.awt.Dimension(626, 20));

        cbProj.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbProj.setName("cbProj"); // NOI18N
        cbProj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProjActionPerformed(evt);
            }
        });
        ToolBarMain.add(cbProj);

        jSeparator4.setName("jSeparator4"); // NOI18N
        ToolBarMain.add(jSeparator4);

        AddPrjButton.setIcon(resourceMap.getIcon("AddPrjButton.icon")); // NOI18N
        AddPrjButton.setText(resourceMap.getString("AddPrjButton.text")); // NOI18N
        AddPrjButton.setFocusable(false);
        AddPrjButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        AddPrjButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        AddPrjButton.setMaximumSize(new java.awt.Dimension(16, 16));
        AddPrjButton.setMinimumSize(new java.awt.Dimension(16, 16));
        AddPrjButton.setName("AddPrjButton"); // NOI18N
        AddPrjButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        AddPrjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddPrjButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(AddPrjButton);

        ExpDiscButton.setIcon(resourceMap.getIcon("ExpDiscButton.icon")); // NOI18N
        ExpDiscButton.setText(resourceMap.getString("ExpDiscButton.text")); // NOI18N
        ExpDiscButton.setFocusable(false);
        ExpDiscButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ExpDiscButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ExpDiscButton.setMaximumSize(new java.awt.Dimension(16, 16));
        ExpDiscButton.setMinimumSize(new java.awt.Dimension(16, 16));
        ExpDiscButton.setName("ExpDiscButton"); // NOI18N
        ExpDiscButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ExpDiscButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExpDiscButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(ExpDiscButton);

        ImpDiscButton.setIcon(resourceMap.getIcon("ImpDiscButton.icon")); // NOI18N
        ImpDiscButton.setText(resourceMap.getString("ImpDiscButton.text")); // NOI18N
        ImpDiscButton.setFocusable(false);
        ImpDiscButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ImpDiscButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ImpDiscButton.setMaximumSize(new java.awt.Dimension(16, 16));
        ImpDiscButton.setMinimumSize(new java.awt.Dimension(16, 16));
        ImpDiscButton.setName("ImpDiscButton"); // NOI18N
        ImpDiscButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ImpDiscButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImpDiscButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(ImpDiscButton);

        MenuButton.setIcon(resourceMap.getIcon("MenuButton.icon")); // NOI18N
        MenuButton.setText(resourceMap.getString("MenuButton.text")); // NOI18N
        MenuButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        MenuButton.setMaximumSize(new java.awt.Dimension(16, 16));
        MenuButton.setMinimumSize(new java.awt.Dimension(16, 16));
        MenuButton.setName("MenuButton"); // NOI18N
        MenuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(MenuButton);

        CopyAllButton.setIcon(resourceMap.getIcon("CopyAllButton.icon")); // NOI18N
        CopyAllButton.setText(resourceMap.getString("CopyAllButton.text")); // NOI18N
        CopyAllButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        CopyAllButton.setMaximumSize(new java.awt.Dimension(16, 16));
        CopyAllButton.setMinimumSize(new java.awt.Dimension(16, 16));
        CopyAllButton.setName("CopyAllButton"); // NOI18N
        CopyAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyAllButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(CopyAllButton);

        CopyOneButton.setIcon(resourceMap.getIcon("CopyOneButton.icon")); // NOI18N
        CopyOneButton.setText(resourceMap.getString("CopyOneButton.text")); // NOI18N
        CopyOneButton.setFocusable(false);
        CopyOneButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        CopyOneButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        CopyOneButton.setMaximumSize(new java.awt.Dimension(16, 16));
        CopyOneButton.setMinimumSize(new java.awt.Dimension(16, 16));
        CopyOneButton.setName("CopyOneButton"); // NOI18N
        CopyOneButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        CopyOneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyOneButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(CopyOneButton);

        FilterButton.setIcon(resourceMap.getIcon("FilterButton.icon")); // NOI18N
        FilterButton.setText(resourceMap.getString("FilterButton.text")); // NOI18N
        FilterButton.setFocusable(false);
        FilterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        FilterButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FilterButton.setMaximumSize(new java.awt.Dimension(16, 16));
        FilterButton.setMinimumSize(new java.awt.Dimension(16, 16));
        FilterButton.setName("FilterButton"); // NOI18N
        FilterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        FilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(FilterButton);

        FindButton.setIcon(resourceMap.getIcon("FindButton.icon")); // NOI18N
        FindButton.setText(resourceMap.getString("FindButton.text")); // NOI18N
        FindButton.setFocusable(false);
        FindButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        FindButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FindButton.setMaximumSize(new java.awt.Dimension(16, 16));
        FindButton.setMinimumSize(new java.awt.Dimension(16, 16));
        FindButton.setName("FindButton"); // NOI18N
        FindButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        FindButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(FindButton);

        jSeparator5.setMaximumSize(new java.awt.Dimension(6, 32000));
        jSeparator5.setName("jSeparator5"); // NOI18N
        ToolBarMain.add(jSeparator5);

        WordButton.setIcon(resourceMap.getIcon("WordButton.icon")); // NOI18N
        WordButton.setText(resourceMap.getString("WordButton.text")); // NOI18N
        WordButton.setFocusable(false);
        WordButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        WordButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        WordButton.setMaximumSize(new java.awt.Dimension(16, 16));
        WordButton.setMinimumSize(new java.awt.Dimension(16, 16));
        WordButton.setName("WordButton"); // NOI18N
        WordButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        WordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WordButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(WordButton);

        TxtButton.setIcon(resourceMap.getIcon("TxtButton.icon")); // NOI18N
        TxtButton.setText(resourceMap.getString("TxtButton.text")); // NOI18N
        TxtButton.setFocusable(false);
        TxtButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        TxtButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        TxtButton.setMaximumSize(new java.awt.Dimension(16, 16));
        TxtButton.setMinimumSize(new java.awt.Dimension(16, 16));
        TxtButton.setName("TxtButton"); // NOI18N
        TxtButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TxtButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TxtButtonActionPerformed(evt);
            }
        });
        ToolBarMain.add(TxtButton);

        PopupMenu1.setName("PopupMenu1"); // NOI18N

        pmnu_addQ.setText(resourceMap.getString("pmnu_addQ.text")); // NOI18N
        pmnu_addQ.setName("pmnu_addQ"); // NOI18N
        pmnu_addQ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_addQActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_addQ);
        pmnu_addQ.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_addQ.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_addA.setText(resourceMap.getString("pmnu_addA.text")); // NOI18N
        pmnu_addA.setName("pmnu_addA"); // NOI18N
        pmnu_addA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_addAActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_addA);
        pmnu_addA.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_addA.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_addZ.setText(resourceMap.getString("pmnu_addZ.text")); // NOI18N
        pmnu_addZ.setName("pmnu_addZ"); // NOI18N
        pmnu_addZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_addZActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_addZ);
        pmnu_addZ.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_addZ.AccessibleContext.accessibleDescription")); // NOI18N

        split2.setName("split2"); // NOI18N
        PopupMenu1.add(split2);
        split2.getAccessibleContext().setAccessibleDescription(resourceMap.getString("split2.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_chStat.setText(resourceMap.getString("pmnu_chStat.text")); // NOI18N
        pmnu_chStat.setName("pmnu_chStat"); // NOI18N
        pmnu_chStat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_chStatActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_chStat);
        pmnu_chStat.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_chStat.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_edit.setText(resourceMap.getString("pmnu_edit.text")); // NOI18N
        pmnu_edit.setName("pmnu_edit"); // NOI18N
        pmnu_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_editActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_edit);
        pmnu_edit.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_edit.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_edit_ch.setText(resourceMap.getString("pmnu_edit_ch.text")); // NOI18N
        pmnu_edit_ch.setName("pmnu_edit_ch"); // NOI18N
        pmnu_edit_ch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_edit_chActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_edit_ch);
        pmnu_edit_ch.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_edit_ch.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_remove.setText(resourceMap.getString("pmnu_remove.text")); // NOI18N
        pmnu_remove.setName("pmnu_remove"); // NOI18N
        pmnu_remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_removeActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_remove);
        pmnu_remove.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_remove.AccessibleContext.accessibleDescription")); // NOI18N

        split3.setName("split3"); // NOI18N
        PopupMenu1.add(split3);

        pmnu_history.setText(resourceMap.getString("pmnu_history.text")); // NOI18N
        pmnu_history.setName("pmnu_history"); // NOI18N
        pmnu_history.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_historyActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_history);
        pmnu_history.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_history.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_all.setText(resourceMap.getString("pmnu_all.text")); // NOI18N
        pmnu_all.setName("pmnu_all"); // NOI18N
        pmnu_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_allActionPerformed(evt);
            }
        });
        PopupMenu1.add(pmnu_all);
        pmnu_all.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_all.AccessibleContext.accessibleDescription")); // NOI18N

        PopupMenu2.setName("PopupMenu2"); // NOI18N

        MenuItem3.setText(resourceMap.getString("MenuItem3.text")); // NOI18N
        MenuItem3.setName("MenuItem3"); // NOI18N
        MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItem3ActionPerformed(evt);
            }
        });
        PopupMenu2.add(MenuItem3);
        MenuItem3.getAccessibleContext().setAccessibleDescription(resourceMap.getString("MenuItem3.AccessibleContext.accessibleDescription")); // NOI18N

        pmnu_PRemove.setText(resourceMap.getString("pmnu_PRemove.text")); // NOI18N
        pmnu_PRemove.setName("pmnu_PRemove"); // NOI18N
        pmnu_PRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmnu_PRemoveActionPerformed(evt);
            }
        });
        PopupMenu2.add(pmnu_PRemove);
        pmnu_PRemove.getAccessibleContext().setAccessibleDescription(resourceMap.getString("pmnu_PRemove.AccessibleContext.accessibleDescription")); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(ToolBarMain);
    }// </editor-fold>//GEN-END:initComponents

    private Boolean AutorizateUser(IClientShell plugin) {
        AutorizationForm authForm = new AutorizationForm(this.getFrame(), true);

        authForm.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - authForm.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - authForm.getHeight()) / 2);
        authForm.setVisible(true);
        while (authForm.isVisible()) {
        }
        if (authForm.Canceled) {
            System.exit(0);
            return false;
        }
        Vector<String> TempID =
                plugin.init(this, config.get("[Config]->[Network]->Address"),
                config.get("[Config]->[Network]->Port"),
                authForm.TextLogin.getText(),
                authForm.TextPassword.getText());
        if (TempID.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this.getFrame(), "?? ?????? ??? ???????????? ??? ??????", "?????? ?????", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        _CurrentUser = TempID;
        return true;
    }
    private void N4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_N4ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_N4ActionPerformed

    private void cbProjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProjActionPerformed
        if (!InitializeFinal) {
            return;
        }
        for (int i = 0; i < cbProj.getItemCount(); i++) {
            if (cbProj.getItemAt(i).equals("???????? ??????????")) {
                cbProj.removeItemAt(i);
            }
        }
        if (cbProj.getSelectedItem().equals("???????? ??????????")) {
            return;
        }
        ChangeProject();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (cbProj.getSelectedItem().equals(tabs.getTitleAt(i))) {
                tabs.setSelectedIndex(i);
                break;
            }
        }
        if (!((DefaultMutableTreeNode) tree.getModel().getRoot()).getUserObject().equals(cbProj.getSelectedItem().toString())) {
            Vector<Object> v = (Vector<Object>) ((NodeData) tree.getModel().getRoot()).GetData();
            v.set(0, cbProj.getSelectedItem().toString());
            ((NodeData) tree.getModel().getRoot()).SetData(v);
            ((NodeData) tree.getModel().getRoot()).setUserObject(cbProj.getSelectedItem().toString());
        }
        if (tree2.getRowCount() > 1) {
            if (!((DefaultMutableTreeNode) tree2.getModel().getRoot()).getUserObject().equals(cbProj.getSelectedItem().toString())) {
                Vector<Object> v = (Vector<Object>) ((NodeData) tree2.getModel().getRoot()).GetData();
                v.set(0, cbProj.getSelectedItem().toString());
                ((NodeData) tree2.getModel().getRoot()).SetData(v);
                ((NodeData) tree2.getModel().getRoot()).setUserObject(cbProj.getSelectedItem().toString());
            }
        }
        menuTests.setEnabled(true);
        menuTTask.setEnabled(true);
        menuDemos.setEnabled(true);
    }//GEN-LAST:event_cbProjActionPerformed

    private void mnu_filterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnu_filterActionPerformed
        FilterForm filterForm = new FilterForm(this, true);
        filterForm.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - filterForm.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - filterForm.getHeight()) / 2);
        filterForm.setTitle("?????? ?????????");
        filterForm.setVisible(true);
    }//GEN-LAST:event_mnu_filterActionPerformed

    private void FindMnuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FindMnuActionPerformed
        FindButtonActionPerformed(evt);
    }//GEN-LAST:event_FindMnuActionPerformed

    private void treeTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_treeTreeExpanded
        JTree Sender = (JTree) evt.getSource();
        NodeData Node = (NodeData) evt.getPath().getLastPathComponent();
        if (Node.getChildCount() == 1 && ((NodeData) Node.getFirstChild()).GetData().equals("EmptyRowDeleteMe!")) {
            ((DefaultTreeModel) Sender.getModel()).removeNodeFromParent((DefaultMutableTreeNode) Node.getFirstChild());
            String sFilter;
            sFilter = "";
            if (!QUERY_STATUSES.equals("")) {
                sFilter += " AND (" + QUERY_STATUSES + ") ";
            }
            if (!config.get(getFilterProperty(CurentFilter, "date")).equals("")) {
                DateFormat d = DateFormat.getDateInstance(DateFormat.MEDIUM);
                sFilter += " AND DcreateDT<#" + d.format(getFilterProperty(CurentFilter, "date")) + "#";
            }

            Waiting(true);

            if (config.get("[Config]->[ClientOptions]->filemanager_view").equals("1")) {
                CreateTree(Sender, (Integer) ((Vector<Object>) Node.GetData()).get(1), Node, "0", "IType<>1 OR IType=1" + sFilter, 0);
            } else if (Sender.equals(QAtree)) {
                CreateTree(Sender, (Integer) ((Vector<Object>) Node.GetData()).get(1), Node, "0", "IType<>1 AND IType<>8" + sFilter, 0);
            } else {
                CreateTree(Sender, (Integer) ((Vector<Object>) Node.GetData()).get(1), Node, "0", "IType=1 OR IType=8" + sFilter, 0);
            }
            if (Node.getChildCount() == 0) {
                NodeData tmp2 = new NodeData();
                tmp2.setUserObject("");
                tmp2.SetData("EmptyRowDeleteMe!");
                ((DefaultTreeModel) Sender.getModel()).insertNodeInto(tmp2, Node, 0);
                Sender.collapsePath(new TreePath(Node));
            }

            Waiting(false);
        }
    }//GEN-LAST:event_treeTreeExpanded

    private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
        JTree Sender = (JTree) evt.getSource();
        curNode = (NodeData) evt.getPath().getLastPathComponent();

        nProj = GetActiveProjectIndexFromTree(Sender);
        if (Sender == null) {
            return;
        }
        Vector<Object> v = ShowQAAtributes(Sender);
        if (v == null) {
            return;
        }
        CreatePopup((Integer) v.get(3), (Integer) v.get(10));
        ClearTree(QAtree);
        if (v.get(7).equals("0")) {
            SendMsgPlugins(EventTypesForClientPlugin.ON_CHANGE_QA, v.get(1).toString(), v.get(3).toString());
            return;
        }
        Vector<Object> PQA = (Vector<Object>) curNode.GetData();

        Waiting(true);

        EnabledAll(false);
        NodeData newNode = new NodeData();
        newNode.setUserObject(ToShortVisibleString((String) PQA.get(0)));
        newNode.SetData(PQA);
        newNode.ImageIndex = Integer.parseInt(PQA.get(3).toString()) * 10 + (Integer.parseInt(PQA.get(10).toString()) - 1) * 2;
        newNode.SelectedIndex = newNode.ImageIndex + 1;
        QAtree.setModel(new DefaultTreeModel(newNode));
        String sFilter;
        sFilter = "";
        if (!QUERY_STATUSES.equals("")) {
            sFilter += " AND (" + QUERY_STATUSES + ") ";
        }
        if (!getFilterProperty(CurentFilter, "date").equals("")) {
            DateFormat d = DateFormat.getDateInstance(DateFormat.MEDIUM);
            sFilter += " AND DcreateDT<#" + d.format(getFilterProperty(CurentFilter, "date")) + "#";
        }
        CreateTree(QAtree, (Integer) PQA.get(1), newNode, "0", "IType<>1 AND IType<>8" + sFilter, 0);
        QAtree.expandPath(new TreePath(newNode));
        EnabledAll(true);

        Waiting(false);

        SendMsgPlugins(EventTypesForClientPlugin.ON_CHANGE_QA, v.get(1).toString(), v.get(3).toString());
    }//GEN-LAST:event_treeValueChanged

    public void ChangeProject() {
        if (tabs.getSelectedIndex() >= 0) {
            if (cbProj.getItemAt(cbProj.getSelectedIndex()).equals(tabs.getTitleAt(tabs.getSelectedIndex()))) {
                return;
            }
            int k = 0;
            for (int i = 0; i < cbProj.getItemCount(); i++) {
                if (cbProj.getItemAt(i).equals(tabs.getTitleAt(tabs.getSelectedIndex()))) {
                    k = i;
                    break;
                }
            }
            Save_old_tree(tree, k);
            Save_old_tree(tree2, k);
        }
        Create_new_tree(tree, cbProj.getSelectedIndex());
        Create_new_tree(tree2, cbProj.getSelectedIndex());
    }

    private void treeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseReleased
        if (evt.getButton() == MouseEvent.BUTTON3) {
            JTree tree_ = (JTree) evt.getSource();
            if (tree_.getRowForLocation(evt.getX(), evt.getY()) != -1) {
                TreePath selPath = tree_.getPathForLocation(evt.getX(), evt.getY());
                curNode = (NodeData) selPath.getLastPathComponent();
                tree_.setSelectionPath(selPath);
                if (((NodeData) selPath.getLastPathComponent()).getUserObject().equals(cbProj.getSelectedItem())) {
                    PopupMenu2.show((Component) evt.getSource(), evt.getX(), evt.getY());
                } else {
                    PopupMenu1.show((Component) evt.getSource(), evt.getX(), evt.getY());
                }
            }
        }
    }//GEN-LAST:event_treeMouseReleased

    private void tree2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tree2MouseReleased
        treeMouseReleased(evt);
    }//GEN-LAST:event_tree2MouseReleased

    private void QAtreeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_QAtreeMouseReleased
        treeMouseReleased(evt);
    }//GEN-LAST:event_QAtreeMouseReleased

    private void QAtreeTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_QAtreeTreeExpanded
        treeTreeExpanded(evt);
    }//GEN-LAST:event_QAtreeTreeExpanded

    private void tree2TreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_tree2TreeExpanded
        treeTreeExpanded(evt);
    }//GEN-LAST:event_tree2TreeExpanded

    private void tabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabsStateChanged
        JTree tree_;
        JTabbedPane Sender = (JTabbedPane) evt.getSource();
        if (Sender.equals(tabs)) {
            tree_ = tree;
        } else {
            tree_ = tree2;
        }
        int k = -1;
        for (int i = 0; i < cbProj.getItemCount(); i++) {
            if (cbProj.getItemAt(i).equals(Sender.getTitleAt(Sender.getSelectedIndex()))) {
                k = i;
                break;
            }
        }
        Save_old_tree(tree_, cbProj.getSelectedIndex());
        Create_new_tree(tree_, k);
        cbProj.setSelectedIndex(k);
        if (!((DefaultMutableTreeNode) tree.getModel().getRoot()).getUserObject().equals(tabs.getTitleAt(tabs.getSelectedIndex()))) {
            Vector<Object> v = (Vector<Object>) ((NodeData) tree.getModel().getRoot()).GetData();
            v.set(0, tabs.getTitleAt(tabs.getSelectedIndex()));
            ((NodeData) tree.getModel().getRoot()).SetData(v);
            ((NodeData) tree.getModel().getRoot()).setUserObject(tabs.getTitleAt(tabs.getSelectedIndex()));
        }
        if (tree2.getRowCount() > 1) {
            if (!((DefaultMutableTreeNode) tree2.getModel().getRoot()).getUserObject().equals(tabs2.getTitleAt(tabs2.getSelectedIndex()))) {
                Vector<Object> v = (Vector<Object>) ((NodeData) tree2.getModel().getRoot()).GetData();
                v.set(0, tabs2.getTitleAt(tabs2.getSelectedIndex()));
                ((NodeData) tree2.getModel().getRoot()).SetData(v);
                ((NodeData) tree2.getModel().getRoot()).setUserObject(tabs2.getTitleAt(tabs2.getSelectedIndex()));
            }
        }
    }//GEN-LAST:event_tabsStateChanged

    private void tree2ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_tree2ValueChanged
        treeValueChanged(evt);
    }//GEN-LAST:event_tree2ValueChanged

    private void QAtreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_QAtreeValueChanged
        ShowQAAtributes(QAtree);
        curNode = (NodeData) evt.getPath().getLastPathComponent();
        Vector<Object> v = (Vector<Object>) curNode.GetData();
        CreatePopup((Integer) v.get(3), (Integer) v.get(10));
        SendMsgPlugins(EventTypesForClientPlugin.ON_CHANGE_QA, v.get(1).toString(), v.get(3).toString());
    }//GEN-LAST:event_QAtreeValueChanged

    private void pmnu_addQActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_addQActionPerformed
        String sOwn;
        if (GetTreeByNode(curNode).equals(tree) && tree.getSelectionPath().getLastPathComponent().equals(tree.getModel().getRoot())) {
            sOwn = "0";
        } else {
            Vector<Object> v = (Vector<Object>) ((NodeData) tree.getSelectionPath().getLastPathComponent()).GetData();
            sOwn = v.get(1).toString();
        }
        Vector<Vector<String>> aTypes = new Vector<Vector<String>>();
        for (int i = 1; i < 4; i++) {
            Vector<String> tmp = new Vector<String>();
            tmp.add(GetLang("Client", "AUCapt" + i, "?????????? ???????"));
            tmp.add(GetLang("Client", "AUText" + i, "????? ???????"));
            aTypes.add(tmp);
        }
        JMenuItem item = (JMenuItem) evt.getSource();
        int tp = 8;
        switch (Integer.parseInt(item.getAccessibleContext().getAccessibleDescription())) {
            case 1:
                tp = 2;
                break;
            case 2:
                tp = 3;
                break;
            case 3:
                tp = 8;
                break;
        }
        String tmp = "";
        try {
            CachedRowSet rs = vProj.createCopy();
            int i = -1;
            while (rs.next()) {
                i++;
                if (i == nProj) {
                    break;
                }
            }
            tmp = rs.getString(1);
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
        Add_QA("" + tp, aTypes.get(Integer.parseInt(item.getAccessibleContext().getAccessibleDescription()) - 1).get(0), "",
                "", sOwn, tmp, aTypes.get(Integer.parseInt(item.getAccessibleContext().getAccessibleDescription()) - 1).get(1));
    }//GEN-LAST:event_pmnu_addQActionPerformed

    private void pmnu_addAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_addAActionPerformed
        pmnu_addQActionPerformed(evt);
    }//GEN-LAST:event_pmnu_addAActionPerformed

    private void pmnu_addZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_addZActionPerformed
        pmnu_addQActionPerformed(evt);
    }//GEN-LAST:event_pmnu_addZActionPerformed

    private void pmnu_chStatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_chStatActionPerformed
        try {
            StatusForm statusForm = new StatusForm(this, true);
            statusForm.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - statusForm.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - statusForm.getHeight()) / 2);
            statusForm.cbStatus.setModel(new DefaultComboBoxModel());
            CachedRowSet v = Net.GetAllStatuses(_CurrentUser);
            while (v.next()) {
                ((DefaultComboBoxModel) statusForm.cbStatus.getModel()).addElement(v.getString(2));
            }

            statusForm.cbReasons.setModel(new DefaultComboBoxModel());
            v = null;
            v = Net.GetAllReasons(_CurrentUser);
            while (v.next()) {
                ((DefaultComboBoxModel) statusForm.cbReasons.getModel()).addElement(v.getString(2));
            }

            Vector<Object> v1 = (Vector<Object>) curNode.GetData();
            statusForm.cbStatus.setSelectedIndex(Integer.parseInt(v1.get(10).toString()) - 1);
            statusForm.cbReasons.setSelectedIndex(Integer.parseInt(v1.get(11).toString()) - 1);
            statusForm.setVisible(true);
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_pmnu_chStatActionPerformed

    private void pmnu_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_editActionPerformed
        Vector<Object> v = (Vector<Object>) curNode.GetData();
        String tmp = "";
        try {
            CachedRowSet rs = vProj.createCopy();
            int i = -1;
            while (rs.next()) {
                i++;
                if (i == nProj) {
                    break;
                }
            }
            tmp = rs.getString(1);
            Add_QA(v.get(3).toString(), GetLang("Client", "EditCapt", "?????????????? ???????"), v.get(6).toString(), v.get(5).toString(), v.get(1).toString(), tmp, v.get(0).toString());
        } catch (Exception ex) {
            debugClass.Except(ex);
        }

    }//GEN-LAST:event_pmnu_editActionPerformed

    private void pmnu_edit_chActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_edit_chActionPerformed
        if (curNode != null) {
            Ch ch = new Ch(this, true);
            ch.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - ch.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - ch.getHeight()) / 2);
            ch.RichEdit1.setText(RichEdit2.getText());
            ch.setVisible(true);
        }
    }//GEN-LAST:event_pmnu_edit_chActionPerformed

    private void RichEdit2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RichEdit2MouseReleased
    }//GEN-LAST:event_RichEdit2MouseReleased

    private void pmnu_historyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_historyActionPerformed
        try {
            HistoryView historyView = new HistoryView(this, true);
            historyView.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - historyView.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - historyView.getHeight()) / 2);
            historyView.vQA = Net.GetAllVersionsByID(_CurrentUser, Integer.parseInt(((Vector<Object>) curNode.GetData()).get(1).toString()));
            if (historyView.vQA == null) {
                javax.swing.JOptionPane.showMessageDialog(this.getFrame(), GetLang("Client", "RightError", "?? ?? ?????? ???? ?? ??????!"), "NetWIQA", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                try {
                    if (historyView.vQA.wasNull()) {
                        javax.swing.JOptionPane.showMessageDialog(this.getFrame(), GetLang("Client", "HistError", "?? ?? ?????? ???? ?? ?????? ???????!"), "NetWIQA", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    historyView.c_action = "history";

                } catch (SQLException ex) {
                    debugClass.Except(ex);
                }
            }

            historyView.setVisible(true);
        } catch (RemoteException ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_pmnu_historyActionPerformed

    private void RichEdit2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RichEdit2MousePressed
        pmnu_edit_chActionPerformed(null);
    }//GEN-LAST:event_RichEdit2MousePressed

    private void tabs2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabs2StateChanged
        tabsStateChanged(evt);
    }//GEN-LAST:event_tabs2StateChanged

    private void pmnu_allActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_allActionPerformed
        try {
            Vector<Object> v = (Vector<Object>) curNode.GetData();
            ItogForm itogForm = new ItogForm(this, true);
            itogForm.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - itogForm.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - itogForm.getHeight()) / 2);
            String[][] rows = new String[v.size()][2];
            for (int i = 0; i < v.size(); i++) {
                rows[i][0] = GetLang("Client", "SummaryPar" + (i + 1), "???????" + (i + 1));
                rows[i][1] = v.get(i).toString();
            }
            rows[2][1] = Net.GetUserByID(Integer.parseInt(v.get(2).toString())).get(2);
            rows[3][1] = Net.GetQATypeByID(Integer.parseInt(v.get(3).toString())).get(2);
            CachedRowSet v2 = Net.GetAllStatuses(_CurrentUser);
            while (v2.next()) {
                if (v2.getString(1).equals(v.get(10).toString())) {
                    rows[10][1] = v2.getString(2);
                    break;
                }
            }

            v2 = Net.GetAllReasons(_CurrentUser);
            while (v2.next()) {
                if (v2.getString(1).equals(v.get(11).toString())) {
                    rows[11][1] = v2.getString(2);
                    break;
                }
            }
            DefaultTableModel model = new DefaultTableModel(rows, new String[]{"", ""});
            itogForm.Grid.setModel(model);
            itogForm.setVisible(true);
        } catch (Exception ex) {
            debugClass.Except(ex);
        }

    }//GEN-LAST:event_pmnu_allActionPerformed

    private void MenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItem3ActionPerformed
        try {
            String AUCapt = GetLang("Client", "AUCapt3", "?????????? ???????");
            String AUtext = GetLang("Client", "AUText3", "????? ???????");
            CachedRowSet rs = vProj.createCopy();
            rs.beforeFirst();
            int i = -1;
            while (rs.next()) {
                i++;
                if (i == nProj) {
                    break;
                }
            }
            Add_QA("8", AUCapt, "", "", "0", rs.getString(1), AUtext);
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_MenuItem3ActionPerformed

    private void menuQA1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuQA1ActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuQA1ActionPerformed

    private void menuQA2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuQA2ActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuQA2ActionPerformed

    private void menuQA3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuQA3ActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuQA3ActionPerformed

    private void menuDiscActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDiscActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuDiscActionPerformed

    private void menuTestsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuTestsActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuTestsActionPerformed

    private void menuTTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuTTaskActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuTTaskActionPerformed

    private void menuDemosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDemosActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuDemosActionPerformed

    private void menuUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUsersActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_menuUsersActionPerformed

    private void N14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_N14ActionPerformed
        AddPrjButtonActionPerformed(evt);
    }//GEN-LAST:event_N14ActionPerformed

    private void N16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_N16ActionPerformed
        ExpDiscButtonActionPerformed(evt);
    }//GEN-LAST:event_N16ActionPerformed

    private void N17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_N17ActionPerformed
        ImpDiscButtonActionPerformed(evt);
    }//GEN-LAST:event_N17ActionPerformed

    private void MNUpropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MNUpropertyActionPerformed
        OptionsForm optionsForm = new OptionsForm(this, true);
        optionsForm.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - optionsForm.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - optionsForm.getHeight()) / 2);
        optionsForm.setVisible(true);
        optionsForm.dispose();
    }//GEN-LAST:event_MNUpropertyActionPerformed

    private void N12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_N12ActionPerformed
        CopyAllButtonActionPerformed(evt);
    }//GEN-LAST:event_N12ActionPerformed

    private void N13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_N13ActionPerformed
        CopyOneButtonActionPerformed(evt);
    }//GEN-LAST:event_N13ActionPerformed

    private void mnu_MSWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnu_MSWordActionPerformed
        WordButtonActionPerformed(evt);
    }//GEN-LAST:event_mnu_MSWordActionPerformed

    private void mnu_XTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnu_XTActionPerformed
        TxtButtonActionPerformed(evt);
    }//GEN-LAST:event_mnu_XTActionPerformed

    private void pmnu_removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_removeActionPerformed
        Vector<Object> v = (Vector<Object>) curNode.GetData();
        if (JOptionPane.showConfirmDialog(this.getFrame(), GetLang("Client", "CRemove", "?? ????????????? ?????? ??????? ????????"), "EduWIQA", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                Net.RemoveQA(_CurrentUser, Integer.parseInt(v.get(1).toString()));
                ((DefaultTreeModel) GetTreeByNode(curNode).getModel()).reload(curNode);
                ((DefaultTreeModel) GetTreeByNode(curNode).getModel()).reload();
            } catch (RemoteException ex) {
                debugClass.Except(ex);
            }
        }

    }//GEN-LAST:event_pmnu_removeActionPerformed

    private void pmnu_PRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmnu_PRemoveActionPerformed
        Vector<Object> v = (Vector<Object>) curNode.GetData();
        if (JOptionPane.showConfirmDialog(this.getFrame(), GetLang("Client", "CPRemove", "?? ????????????? ?????? ??????? ???????"), "EduWIQA", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                int cbi = cbProj.getSelectedIndex();
                int ti = tabs.getSelectedIndex();
                CachedRowSet tcach = vProj.createCopy();
                tcach.beforeFirst();
                int ktmp = -1;
                while (tcach.next()) {
                    ktmp++;
                    if (ktmp == nProj) {
                        break;
                    }
                }
                int pi = tcach.getInt(1);
                Net.RemoveProject(_CurrentUser, pi);
                if (cbi > 1) {
                    if (cbi == 0) {
                        cbProj.setSelectedIndex(1);
                    } else {
                        cbProj.setSelectedIndex(0);
                    }
                    if (ti == 0) {
                        tabs.setSelectedIndex(1);
                    } else {
                        tabs.setSelectedIndex(0);
                    }
                    cbProjActionPerformed(evt);
                } else {
                    cbProj.setSelectedIndex(-1);
                    ClearTree(tree);
                }
                ((DefaultListModel) cbProj.getModel()).removeElementAt(cbi);
                tabs.remove(ti);
            } catch (Exception ex) {
                debugClass.Except(ex);
            }
        }
    }//GEN-LAST:event_pmnu_PRemoveActionPerformed

    private void AddPrjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddPrjButtonActionPerformed
        NewPrjForm newPrjForm = new NewPrjForm(this, true);
        newPrjForm.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - newPrjForm.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - newPrjForm.getHeight()) / 2);
        newPrjForm.Step = 1;
        newPrjForm.setVisible(true);

        newPrjForm.dispose();
    }//GEN-LAST:event_AddPrjButtonActionPerformed

    private void ExpDiscButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExpDiscButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_ExpDiscButtonActionPerformed

    private void ImpDiscButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImpDiscButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_ImpDiscButtonActionPerformed

    private void MenuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuButtonActionPerformed
        MNUpropertyActionPerformed(evt);
    }//GEN-LAST:event_MenuButtonActionPerformed

    private void CopyAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyAllButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_CopyAllButtonActionPerformed

    private void CopyOneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyOneButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_CopyOneButtonActionPerformed

    private void FilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterButtonActionPerformed
        mnu_filterActionPerformed(evt);
    }//GEN-LAST:event_FilterButtonActionPerformed

    private void FindButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FindButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_FindButtonActionPerformed

    private void WordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WordButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_WordButtonActionPerformed

    private void TxtButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TxtButtonActionPerformed
        try {
            throw new UnsupportedOperationException(GetLang("Client", "InDevelopment", "?????? ??????? ? ?????? ??????????"));
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
    }//GEN-LAST:event_TxtButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddPrjButton;
    private javax.swing.JButton CopyAllButton;
    private javax.swing.JButton CopyOneButton;
    private javax.swing.JButton ExpDiscButton;
    private javax.swing.JButton FilterButton;
    private javax.swing.JButton FindButton;
    private javax.swing.JMenuItem FindMnu;
    private javax.swing.JButton ImpDiscButton;
    private javax.swing.JLabel Label1;
    private javax.swing.JLabel Label2;
    private javax.swing.JLabel Label3;
    private javax.swing.JLabel Label4;
    private javax.swing.JLabel Label5;
    private javax.swing.JLabel Label6;
    private javax.swing.JMenu MNUplugin;
    private javax.swing.JMenuItem MNUproperty;
    private javax.swing.JButton MenuButton;
    private javax.swing.JMenuItem MenuItem3;
    private javax.swing.JMenu N10;
    private javax.swing.JPopupMenu.Separator N11;
    private javax.swing.JMenuItem N12;
    private javax.swing.JMenuItem N13;
    private javax.swing.JMenuItem N14;
    private javax.swing.JMenu N15;
    private javax.swing.JMenuItem N16;
    private javax.swing.JMenuItem N17;
    private javax.swing.JPopupMenu.Separator N2;
    private javax.swing.JMenuItem N4;
    private javax.swing.JPopupMenu.Separator N7;
    private javax.swing.JPopupMenu.Separator N8;
    private javax.swing.JMenuItem N9;
    private javax.swing.JTabbedPane PageControl1;
    private javax.swing.JPanel Panel1;
    private javax.swing.JPopupMenu PopupMenu1;
    private javax.swing.JPopupMenu PopupMenu2;
    private javax.swing.JMenu Progect1;
    public javax.swing.JTree QAtree;
    public javax.swing.JTextArea RichEdit1;
    public javax.swing.JTextArea RichEdit2;
    private javax.swing.JPanel TabSheet1;
    private javax.swing.JPanel TabSheet2;
    private javax.swing.JToolBar ToolBarMain;
    private javax.swing.JButton TxtButton;
    private javax.swing.JButton WordButton;
    public javax.swing.JComboBox cbProj;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuDemos;
    private javax.swing.JMenuItem menuDisc;
    private javax.swing.JMenuItem menuQA1;
    private javax.swing.JMenuItem menuQA2;
    private javax.swing.JMenuItem menuQA3;
    private javax.swing.JMenuItem menuTTask;
    private javax.swing.JMenuItem menuTests;
    private javax.swing.JMenuItem menuUsers;
    private javax.swing.JMenuItem mnu_MSWord;
    private javax.swing.JMenuItem mnu_XT;
    private javax.swing.JMenu mnu_cur;
    private javax.swing.JMenuItem mnu_filter;
    private javax.swing.JMenuItem pmnu_PRemove;
    private javax.swing.JMenuItem pmnu_addA;
    private javax.swing.JMenuItem pmnu_addQ;
    private javax.swing.JMenuItem pmnu_addZ;
    private javax.swing.JMenuItem pmnu_all;
    private javax.swing.JMenuItem pmnu_chStat;
    private javax.swing.JMenuItem pmnu_edit;
    private javax.swing.JMenuItem pmnu_edit_ch;
    private javax.swing.JMenuItem pmnu_history;
    private javax.swing.JMenuItem pmnu_remove;
    private javax.swing.JPanel pp1;
    private javax.swing.JSplitPane pp1inner;
    private javax.swing.JPanel pp1innerLeft;
    private javax.swing.JPanel pp1innerRight;
    private javax.swing.JPanel pp2;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPopupMenu.Separator split2;
    private javax.swing.JPopupMenu.Separator split3;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel0;
    private javax.swing.JLabel statusMessageLabel1;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTabbedPane tabs2;
    public javax.swing.JTree tree;
    public javax.swing.JTree tree2;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    protected PluginsLoaderService _pluginService;
    protected INetClientPlugin Net;

    public void Disconnect() {
        System.exit(0);
    }

    public void Alert(String Message) {
        JOptionPane.showMessageDialog(this.mainPanel, Message);
    }

    public JPanel getmainPanel() {
        return mainPanel;
    }

    public JMenuBar getmenuBar() {
        return menuBar;
    }

    public JPanel getstatusPanel() {
        return statusPanel;
    }

    public JToolBar getToolBarMain() {
        return ToolBarMain;
    }

    public void setToolBarMain(JToolBar toolBar) {
        this.ToolBarMain = toolBar;
        setComponent(this.ToolBarMain);
    }

    public void setmainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
        setComponent(this.mainPanel);
    }

    public void setmenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
        setMenuBar(this.menuBar);
    }

    public void setstatusPanel(JPanel statusPanel) {
        this.statusPanel = statusPanel;
        setStatusBar(this.statusPanel);
    }

    public INetClientPlugin getNet() {
        return Net;
    }

    public Object getClientPluginMethod(String ClassName, String Method, Object[] Params) {
        Iterator<IPluginsBase> iterator = _pluginService.getPlugins();
        if (!iterator.hasNext()) {
            System.err.print("No plugins were found!");
        }
        Object obj = null;
        while (iterator.hasNext()) {
            IPluginsBase plugin = iterator.next();
            if (plugin.getClass().getName().equals(ClassName)) {
                obj = plugin;
                break;
            }
        }
        if (obj.equals(null)) {
            return null;
        }
        try {
            return obj.getClass().getMethod(Method, new Class[0]).invoke(obj, Params);
        } catch (Exception ex) {
            return null;
        }
    }
    public Vector listenerConnectDisconnect = new Vector();

    protected boolean fireEventConnectDisconnect(OnConnectDisconnectEvent e) {
        boolean b = true;
        Vector list = (Vector) listenerConnectDisconnect.clone();
        for (int i = 0; i < list.size(); i++) {
            OnConnectDisconnectEventInterface listener = (OnConnectDisconnectEventInterface) list.elementAt(i);
            switch (e.GetStatus()) {
                case Connected:
                    if (!listener.Connect(e)) {
                        b = false;
                    }
                    break;
                case Connecting:
                    if (!listener.Connecting(e)) {
                        b = false;
                    }
                    break;
                case Disconnect:
                    if (!listener.Disconnect(e)) {
                        b = false;
                    }
                    break;
                case Disconnecting:
                    if (!listener.Disconnecting(e)) {
                        b = false;
                    }
                    break;
                case Error:
                    if (!listener.Error(e)) {
                        b = false;
                    }
                    break;
            }
        }
        return b;
    }

    public void AddConnectDisconnectListener(OnConnectDisconnectEventInterface e) {
        if (listenerConnectDisconnect == null) {
            listenerConnectDisconnect = new Vector();
        }
        listenerConnectDisconnect.add(e);
    }

    public void RemoveConnectDisconnectListener(OnConnectDisconnectEventInterface e) {
        if (listenerConnectDisconnect == null) {
            listenerConnectDisconnect = new Vector();
        }
        listenerConnectDisconnect.remove(e);
    }

    public boolean ClientDisconnectConnect(OnConnectDisconnectEvent e) {
        return fireEventConnectDisconnect(e);
    }

    public void HideForm() {
        getFrame().setVisible(false);
    }

    public void ShowForm() {
        getFrame().setVisible(true);
    }

    private void Save_old_tree(JTree tree_, int index) {
        int index_ = 0;
        if (!tree.equals(tree_)) {
            index_++;
        }
        index_ = index_ * cbProj.getItemCount() + index;
        TreeSaving tmp = new TreeSaving();
        tmp.Model = tree_.getModel();
        tmp.ExpandPath = tree_.getExpandedDescendants(new javax.swing.tree.TreePath(tree_.getModel().getRoot()));
        tabs_array.setElementAt(tmp, index_);
    }

    public void Create_new_tree(JTree tree_, int index) {
        Waiting(true);
        try {
            nProj = index;
            ClearTree(QAtree);
            ClearTree(tree_);
            Vector<Object> pQA = new Vector<Object>();
            CachedRowSet tProj = vProj.createCopy();
            int t = -1;
            while (tProj.next()) {
                t++;
                if (t == nProj) {
                    break;
                }
            }
            pQA.add(tProj.getObject(2));
            pQA.add(tProj.getObject(1));
            pQA.add(tProj.getObject(3));
            pQA.add(-1);
            pQA.add(tProj.getObject(6));
            pQA.add(tProj.getObject(7));
            pQA.add(tProj.getObject(8));
            pQA.add(0);
            NodeData node = new NodeData();
            node.setUserObject((String) pQA.get(0));
            node.SetData(pQA);

            tree_.setModel(new DefaultTreeModel(node));
            String sFilter;
            sFilter = "";
            if (!QUERY_STATUSES.equals("")) {
                sFilter += " AND (" + QUERY_STATUSES + ") ";
            }
            if (!getFilterProperty(CurentFilter, "date").equals("")) {
                DateFormat d = DateFormat.getDateInstance(DateFormat.MEDIUM);
                sFilter += " AND DcreateDT<#" + d.format(getFilterProperty(CurentFilter, "date")) + "#";
            }
            CreateTree(tree_, 0, node, "0", "Itype=1 or Itype=8 " + sFilter, 0);
            tree_.expandRow(0);
            String tabName = "";
            int index_ = 0;

            JTabbedPane tabs_;
            if (tree_ == tree) {
                tabName = "1_";
                index_ = 0;
                tabs_ = tabs;
            } else {
                tabName = "2_";
                index_ = 1;
                tabs_ = tabs2;
            }
            index_ = index_ * cbProj.getItemCount() + index;
            TreeSaving ptr = tabs_array.get(index_);
            if (ptr != null) {
                tree_.setModel(ptr.Model);
                while (ptr.ExpandPath.hasMoreElements()) {
                    tree_.expandPath(ptr.ExpandPath.nextElement());
                }
            }
            lastProject = String.valueOf(nProj);
            int k;
            k = nProj;
            tabName = (String) cbProj.getItemAt(k);
            k = -1;
            for (int ik = 0; ik < tabs_.getTabCount(); ik++) {
                if (tabs_.getTitleAt(ik).equals(tabName)) {
                    k = ik;
                    break;
                }
            }
            if (k >= 0) {
                tabs_.setSelectedIndex(k);
            } else {
                tabs_.addTab(tabName, null, new JPanel(), tabName);
                tabs_.setSelectedIndex(tabs_.getTabCount() - 1);
            }
            SendMsgPlugins(EventTypesForClientPlugin.ON_CHANGE_PROJ, tProj.getString(1), "");

        } catch (SQLException ex) {
            debugClass.Except(ex);
        }
        Waiting(false);
    }

    public void SendMsgPlugins(Classes.EventTypesForClientPlugin n, String s1, String s2) {
        Iterator<IPluginsBase> iterator = _pluginService.getPlugins();
        while (iterator.hasNext()) {
            IPluginsBase plugin = (IPluginsBase) iterator.next();
            plugin.onEvent(n, s1, s2);
        }
    }

    public void CreateTree(JTree l_tree, int OwnerID, NodeData OwnerNode, String sType, String sSQL, int vLen) {
        Waiting(true);
        try {

            NodeData node;

            CachedRowSet tProj = vProj.createCopy();
            int t = -1;
            while (tProj.next()) {
                t++;
                if (t == nProj) {
                    break;
                }
            }
            int sProj = tProj.getInt(1);
            CachedRowSet QA_unsorted = Net.GetQAEx(_CurrentUser, OwnerID, sProj, sType, sSQL);
            if (QA_unsorted.wasNull()) {
                return;
            }
            CachedRowSet QA = QA_unsorted; //??????? ??????????
            int i = -1;
            while (QA.next()) {
                i++;
                NodeData Node = null;
                if (getFilterProperty(CurentFilter, "show_history").equals("1")) {
                    CachedRowSet QA_hist = Net.GetAllVersionsByID(_CurrentUser, QA.getInt(2));
                    if (!QA_hist.wasNull()) {
                        int c = -1;
                        while (QA_hist.next()) {
                            c++;
                            Node = AddItemToTree(l_tree, OwnerNode, QA_hist, c);
                        }
                    } else {
                        Node = AddItemToTree(l_tree, OwnerNode, QA, i);
                    }
                } else {
                    Node = AddItemToTree(l_tree, OwnerNode, QA, i);
                }
                if (config.get("[Config]->[ClientOptions]->load_full_tree").equals("1")) {

                    int SHOW_LENGTH = 0;
                    if (!getFilterProperty(CurentFilter, "show_length").equals("")) {
                        SHOW_LENGTH = Integer.parseInt(getFilterProperty(CurentFilter, "show_length"));
                    }
                    if (QA.getInt(8) != 0 && (SHOW_LENGTH == 0 || vLen < SHOW_LENGTH - 1)) {
                        CreateTree(l_tree, QA.getInt(2), Node, sType, sSQL, vLen + 1);
                    }
                }
            }
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
        Waiting(false);
    }

    public NodeData AddItemToTree(JTree l_tree, NodeData OwnerNode, CachedRowSet QA, int i) {
        try {
            if (QA.isBeforeFirst()) {
                QA.next();
            }
            Vector<Object> pQA = new Vector<Object>();
            for (int j = 1; j <= N_COUNT_QA_PARAMS; j++) {
                pQA.add(QA.getObject(j));
            }
            String sName = ToShortVisibleString(QA.getString(1));
            if (config.get("[Config]->[ClientOptions]->show_symbol_name").equals("1")) {
                sName = QA.getString(9) + ": " + sName;
            }
            NodeData node = new NodeData();
            node.setUserObject(sName);
            node.SetData(pQA);
            node.ImageIndex = QA.getInt(4) * 10 + (QA.getInt(11) - 1) * 2;
            node.SelectedIndex = node.ImageIndex + 1;
            if (OwnerNode == null) {
                l_tree.setModel(new DefaultTreeModel(node));
                //((DefaultTreeModel) l_tree.getModel()).insertNodeInto(node, ((DefaultMutableTreeNode) l_tree.getModel().getRoot()), ((DefaultMutableTreeNode) l_tree.getModel().getRoot()).getChildCount());
            } else {
                ((DefaultTreeModel) l_tree.getModel()).insertNodeInto(node, OwnerNode, i);
            }
            if (Integer.parseInt(QA.getString(8).substring(0, 1)) > 0) {
                NodeData tmp2 = new NodeData();
                tmp2.setUserObject("");
                tmp2.SetData("EmptyRowDeleteMe!");
                node.add(tmp2);
                l_tree.collapsePath(new TreePath(node));
            }
            ((DefaultTreeModel) l_tree.getModel()).reload();
            return node;
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
        return null;
    }

    private void ReInitPanel(JPanel panel) {
        panel.setLayout(new GridBagLayout());
        for (int i = 0; i < panel.getComponentCount(); i++) {
            try {
                ReInitPanel((JPanel) panel.getComponent(i));
            } catch (Exception ex) {
                //
            }
        }
    }

    private void EditConstraints(Component component, GridBagConstraints c) {
        Container parent = component.getParent();
        int index = -1;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i).equals(component)) {
                index = i;
                break;
            }
        }
        parent.remove(component);
        parent.add(component, c, index);
    }

    public void SetView(int n) {
        if (n < 1 || n > 5) {
            n = 1;
        }
        //ReInitPanel(mainPanel);
        // GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 0;
//        c.fill = c.HORIZONTAL;
//        c.gridy = 0;
//        c.anchor = GridBagConstraints.PAGE_START;
//        EditConstraints(tabs, c);
//
//        switch (n) {
//            case 1:
//                c.anchor = GridBagConstraints.PAGE_END;
//                EditConstraints(pp2, c);
//                c.anchor = GridBagConstraints.PAGE_START;
//                c.fill = c.HORIZONTAL;
//                EditConstraints(pp1, c);
//                break;
//        }
    }

    private int GetActiveProjectIndexFromTree(JTree tree_) {
        JTabbedPane destTabs;
        if (tree_.equals(tree2)) {
            destTabs = tabs2;
        } else {
            destTabs = tabs;
        }
        for (int i = 0; i < cbProj.getItemCount(); i++) {
            if (cbProj.getItemAt(i).equals(destTabs.getTitleAt(destTabs.getSelectedIndex()))) {
                return i;
            }
        }
        return 0;
    }

    public Vector<Object> ShowQAAtributes(JTree l_tree) {
        try {
            if (l_tree.getSelectionCount() == 0) {
                return null;
            }
            Vector<Object> v = (Vector<Object>) ((NodeData) l_tree.getSelectionPath().getLastPathComponent()).GetData();
            RichEdit1.setText((String) v.get(0));

            if (v.size() >= 17) {
                RichEdit2.setText(v.get(16).toString());
            }

            Vector<String> vAvtor = new Vector<String>();
            try {
                vAvtor = Net.GetDataByID((Integer) v.get(2), "USERS");

            } catch (RemoteException ex) {
                debugClass.Except(ex);
            }
            if (!vAvtor.isEmpty()) {
                Label1.setText(vAvtor.get(2));
            }
            if (v.get(5).equals("")) {
                Label2.setText("???????????");
            } else {
                Label2.setText((String) v.get(5));
            }
            Label3.setText(((java.sql.Timestamp) v.get(4)).toString());
            return v;
        } catch (Exception ex) {
            debugClass.Except(ex);
        }
        return null;
    }

    public void CreatePopup(int t, int nStatus) {
        pmnu_chStat.setVisible(true);
        pmnu_edit.setVisible(true);
        pmnu_history.setVisible(true);
        pmnu_all.setVisible(true);
        pmnu_edit_ch.setVisible(true);
        split2.setVisible(true);

        for (int i = 0; i < PopupMenu1.getComponentCount(); i++) {
            if (PopupMenu1.getComponent(i).getName().startsWith("pmnu_add")) {
                PopupMenu1.getComponent(i).setVisible(false);
            }
        }
        if (nStatus != 1) {
            pmnu_edit.setVisible(false);
            pmnu_edit_ch.setVisible(false);
        }
        switch (t) {
            case 1:
                pmnu_addQ.setVisible(true);
                break;
            case 2:
                pmnu_addQ.setVisible(true);
                pmnu_addA.setVisible(true);
                break;
            case 3:
                if (nStatus != 2 && nStatus != 3) {
                    pmnu_addQ.setVisible(true);
                }
                break;
            case 8:
                pmnu_addZ.setVisible(true);
                pmnu_addQ.setVisible(true);
                break;
            case -1:
                pmnu_chStat.setVisible(false);
                pmnu_history.setVisible(false);
                pmnu_all.setVisible(false);
                pmnu_addZ.setVisible(true);
                break;
        }
        if (nStatus != 1) {
            for (int i = 0; i < PopupMenu1.getComponentCount(); i++) {
                if (PopupMenu1.getComponent(i).getName().startsWith("pmnu_add")) {
                    PopupMenu1.getComponent(i).setVisible(false);
                }
            }
        }
        mnu_cur.removeAll();
        for (int i = 0; i < PopupMenu1.getComponentCount(); i++) {
            try {
                if (!((JMenuItem) PopupMenu1.getComponent(i)).isVisible()) {
                    continue;
                }
                JMenuItem NewItem = new JMenuItem(((JMenuItem) PopupMenu1.getComponent(i)).getText());
//                NewItem.setText(((JMenuItem) PopupMenu1.getComponent(i)).getText());
//                NewItem.setAction(((JMenuItem) PopupMenu1.getComponent(i)).getAction());
                NewItem.setEnabled(PopupMenu1.getComponent(i).isEnabled());
                NewItem.getAccessibleContext().setAccessibleDescription(((JMenuItem) PopupMenu1.getComponent(i)).getAccessibleContext().getAccessibleDescription());
                mnu_cur.add(NewItem);
            } catch (Exception ex) {
                mnu_cur.add(new JSeparator());
            }


        }
    }

    public void ClearTree(JTree l_tree) {
        l_tree.setModel(new DefaultTreeModel(null));
    }

    public void EnabledAll(boolean b) {
        RichEdit1.setEditable(b);
        tree.setEditable(b);
        tree2.setEditable(b);
        QAtree.setEditable(b);
    }

    public String ToShortVisibleString(String str) {
        int len = 30;
        if (config.get("[Config]->[ClientOptions]->len_visible_message").equals("")) {
            len = 30;
        } else {
            len = Integer.parseInt(config.get("[Config]->[ClientOptions]->len_visible_message"));
        }
        if (str.length() > len) {
            return str.substring(0, len) + "...";
        }
        return str;
    }

    public JTree GetTreeByNode(NodeData Node) {
        if (tree.getRowForPath(new TreePath(Node)) != -1) {
            return tree;
        }
        if (tree2.getRowForPath(new TreePath(Node)) != -1) {
            return tree2;
        }
        return QAtree;
    }

    public String GetLang(String FileName, String KeySector, String KeyString, String DefaultValue) {
        if (FileName == null) {
            return DefaultValue;
        }
        if (!(new File(FileName)).exists()) {
            return DefaultValue;
        }
        Config LangsFile = new Config(FileName);
        if (LangsFile.get("[Lang]->[" + KeySector + "]->[" + KeyString + "]").equals("")) {
            return DefaultValue;
        }
        return LangsFile.get("[Lang]->[" + KeySector + "]->[" + KeyString + "]");
    }

    public String GetLang(String KeySector, String KeyString, String DefaultValue) {
        return GetLang(LangFile, KeySector, KeyString, DefaultValue);
    }

    private void Add_QA(String sType, String sCaption, String sFile, String sTextFile, String sOwn, String sProj, String sText) {
        UAdd uadd = new UAdd(this, true);
        uadd.Memo1.setText(sText);
        if (sCaption.equals(GetLang("Client", "EditCapt", "?????????????? ???????"))) {
            uadd.getAccessibleContext().setAccessibleDescription("1");
        } else {
            uadd.getAccessibleContext().setAccessibleDescription("0");
        }
        uadd.StaticText1.setText(sCaption);
        uadd.FileEdit.setText(sFile);
        uadd.TextEdit.setText(sTextFile);
        uadd.sOwn = sOwn;
        uadd.sType = sType;
        uadd.sPrj = sProj;
        uadd.setVisible(true);
    }
    //private WaitThread waitThread;

    private void Waiting(boolean Show) {
        if (Show) {
            EnabledAll(false);
            busyIconTimer.start();
        } else {
            EnabledAll(true);
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
        }
    }

    public String getFilterProperty(int ID, String Key) {
        return config.get("[Config]->[Filters]->[Filter" + ID + "]->" + Key);
    }

    public void setFilterProperty(int ID, String Key, String Value) {
        config.set("[Config]->[Filters]->[Filter" + ID + "]->" + Key, Value);
    }

    public void setInterfaceLang() {
        LangFile = config.get("[Config]->[ClientOptions]->language");
        
        if (nProj == 0) {
            DefaultComboBoxModel tmp = ((DefaultComboBoxModel) cbProj.getModel());
            tmp.removeElementAt(0);
            tmp.insertElementAt(GetLang("Client", "Choose", "???????? ??????"), 0);
            tmp.setSelectedItem(GetLang("Client", "Choose", "???????? ??????"));
            cbProj.setModel(tmp);
        }
        Label4.setText(GetLang("Client", "Auth", "?????"));
        Label5.setText(GetLang("Client", "Att", "????????"));
        Label6.setText(GetLang("Client", "Creat", "??????"));
        PageControl1.setTitleAt(0, GetLang("Client", "TextUn", "????? ???????"));
        PageControl1.setTitleAt(1, GetLang("Client", "Draft", "????????"));
        Progect1.setText(GetLang("Client","Main1","????????"));
        N10.setText(GetLang("Client", "Main2", "??????"));
        MNUplugin.setText(GetLang("Client", "Main3", "?????????????"));
        N4.setText(GetLang("Client", "Sub1_3", "?????"));
        mnu_cur.setText(GetLang("Client", "Sub2_1", "??????? ???????"));
        N9.setText(GetLang("Client", "Sub2_1_1", "??? ?????????"));
        N12.setText(GetLang("Client", "CopyNode", "??????????? ?????"));
        N13.setText(GetLang("Client", "CopyLeaf", "??????????? ???????"));
        mnu_MSWord.setText(GetLang("Client", "Sub2_2", "??????? (MS Word)"));
        WordButton.setToolTipText(GetLang("Client", "WordHint", "?????????????? ? MS Word"));
        mnu_XT.setText(GetLang("Client", "Sub2_3", "??????? (TXT ????)"));
        TxtButton.setToolTipText(GetLang("Client", "TXTHint", "?????????????? ? ????????? ????"));
        FilterButton.setToolTipText(GetLang("Client", "Sub3_1", "?????????? ?????????"));
        FindMnu.setText(GetLang("Client", "Sub2_4", "?????"));
        FindButton.setToolTipText(GetLang("Client", "Sub2_4", "?????"));
        MNUproperty.setText(GetLang("Client", "Sub3_2", "?????????"));
        mnu_filter.setText(GetLang("Client", "Sub3_1", "?????????? ?????????"));
        MenuButton.setToolTipText(GetLang("Client", "Sub3_2", "?????????"));
        pmnu_addQ.setText(GetLang("Client", "Popup3", "?????? ??????"));
        pmnu_addA.setText(GetLang("Client", "Popup4", "????????"));
        pmnu_addZ.setText(GetLang("Client", "Popup9", "??????? ?????????"));
        pmnu_chStat.setText(GetLang("Client", "Popup16", "?????? ???????"));
        pmnu_edit.setText(GetLang("Client", "Popup17", "????????????? ???????"));
        pmnu_remove.setText(GetLang("Client", "pmnu_remove", "??????? ???????"));
        pmnu_PRemove.setText(GetLang("Client", "pmnu_PRemove", "??????? ??????"));
        MenuItem3.setText(GetLang("Client", "pmnu_PAdd", "??????? ??????"));
        pmnu_edit_ch.setText(GetLang("Client", "Popup18", "????????????? ????????"));
        pmnu_history.setText(GetLang("Client", "Popup19", "??????? ?????????"));
        pmnu_all.setText(GetLang("Client", "Popup20", "???????"));
        this.getFrame().setTitle("EduWIQA");
        N14.setText(GetLang("Client", "DiscReg", "??????????? ??????????"));
        AddPrjButton.setToolTipText(GetLang("Client", "DiscReg", "??????????? ??????????"));
        N15.setText(GetLang("Client", "SupUser", "?????????????????"));
        menuQA1.setText(GetLang("QAService", "QAType", "???? ?????? QA"));
        menuQA2.setText(GetLang("QAService", "QAStatus", "??????? ?????? QA"));
        menuQA3.setText(GetLang("QAService", "QAReason", "??????? ???????? ?????? QA"));
        menuDisc.setText(GetLang("QAService", "QAProjects", "??????????"));
        menuTests.setText(GetLang("QAService", "QATests", "?????"));
        menuTTask.setText(GetLang("QAService", "QATTasks", "??????? ???????"));
        menuDemos.setText(GetLang("QAService", "QADemos", "????????????"));
        menuUsers.setText(GetLang("Client", "Users", " "));
        statusMessageLabel0.setText(GetLang("Client", "Status1", "??????"));
        statusMessageLabel1.setText(GetLang("Client", "Status2", "??? ?????"));
        //if (ConnectButton.isSelected) ConnectButton.setToolTipText();
        //else ConnectButton.setToolTipText();

    }
//    private WaitView waitView=new WaitView(null, true);;
//    private class WaitThread implements Runnable{
//        public Thread thr;
//
//        public WaitThread(){
//            thr= new Thread(this);
//            thr.setDaemon(true);
//            waitView.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - waitView.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - waitView.getHeight()) / 2);
//            thr.start();
//        }
//
//        public void run(){
//            waitView.setVisible(true);
//        }
//    }
}

