package net.sf.colossus.client;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.colossus.server.Constants;
import net.sf.colossus.server.VariantSupport;
import net.sf.colossus.server.XMLSnapshotFilter;
import net.sf.colossus.util.HTMLColor;
import net.sf.colossus.util.Log;
import net.sf.colossus.util.Options;
import net.sf.colossus.util.ResourceLoader;
import net.sf.colossus.xmlparser.StrategicMapLoader;
import net.sf.colossus.xmlparser.TerrainRecruitLoader;


/**
 * Class MasterBoard implements the GUI for a Titan masterboard.
 * @version $Id: MasterBoard.java 2647 2007-04-11 22:45:48Z cleka $
 * @author David Ripton
 * @author Romain Dolbeau
 */

public final class MasterBoard extends JPanel
{
    private Image offScreenBuffer;
    private static int horizSize = 0;
    private static int vertSize = 0;

    /** "parity" of the board, so that Hexes are displayed the proper way */
    private static int boardParity = 0;

    private GUIMasterHex[][] guiHexArray = null;
    private static MasterHex[][] plainHexArray = null;

    /** The hexes in the horizSize*vertSize array that actually exist are
     *  represented by true. */
    private static boolean[][] show = null;

    private Client client;

    private JFrame masterFrame;
    private JMenu phaseMenu;
    private JPopupMenu popupMenu;
    private Map checkboxes = new HashMap();
    private JPanel[] legionFlyouts;

    /** Last point clicked is needed for popup menus. */
    private Point lastPoint;

    /**The scrollbarspanel, needed to correct lastPoint.*/
    private JScrollPane scrollPane;

    private Container contentPane;

    /** our own little bar implementation */
    private BottomBar bottomBar;

    public static final String saveGameAs = "Save game as";

    public static final String clearRecruitChits = "Clear recruit chits";

    public static final String undoLast = "Undo";
    public static final String undoAll = "Undo All";
    public static final String doneWithPhase = "Done";

    public static final String takeMulligan = "Take Mulligan";
    public static final String concedeBattle = "Concede battle";
    public static final String withdrawFromGame = "Withdraw from Game";

    public static final String viewFullRecruitTree = "View Full Recruit Tree";
    public static final String viewHexRecruitTree = "View Hex Recruit Tree";
    public static final String viewBattleMap = "View Battle Map";
    public static final String changeScale = "Change Scale";

    public static final String chooseScreen = "Choose Screen For Info Windows";

    public static final String about = "About";
    public static final String viewReadme = "Show Variant Readme";

    private AbstractAction newGameAction;
    private AbstractAction loadGameAction;
    private AbstractAction saveGameAction;
    private AbstractAction saveGameAsAction;
    private AbstractAction quitGameAction;

    private AbstractAction clearRecruitChitsAction;

    private AbstractAction undoLastAction;
    private AbstractAction undoAllAction;
    private AbstractAction doneWithPhaseAction;

    private AbstractAction takeMulliganAction;
    private AbstractAction withdrawFromGameAction;

    private AbstractAction viewFullRecruitTreeAction;
    private AbstractAction viewHexRecruitTreeAction;
    private AbstractAction viewBattleMapAction;
    private AbstractAction changeScaleAction;

    private AbstractAction chooseScreenAction;

    private AbstractAction aboutAction;
    private AbstractAction viewReadmeAction;

    /* a Set of label (String) of all Tower hex */
    private static Set towerSet = null;

    private boolean playerLabelDone;

    private JMenu lfMenu;
    private SaveWindow saveWindow;

    private final class InfoPopupHandler extends KeyAdapter {
        private static final int POPUP_KEY = KeyEvent.VK_SHIFT;
        private static final int PANEL_MARGIN = 4;
        private static final int PANEL_PADDING = 0;

        private final Client client;

        private InfoPopupHandler(Client client)
        {
            super();
            this.client = client;
        }

        public void keyPressed(KeyEvent e)
        {
            String playerName = client.getPlayerName();
            int viewMode      = client.getViewMode();
            if (e.getKeyCode() == POPUP_KEY)
            {
                if (legionFlyouts == null)
                {
                    List markers = client.getMarkers();
                    // copy to array so we don't get concurrent modification exceptions when iterating
                    Marker[] markerArray = (Marker[]) markers.toArray(new Marker[markers.size()]);
                    legionFlyouts = new JPanel[markers.size()];
                    for (int i = 0; i < markerArray.length; i++) {
						Marker marker = markerArray[i];
                        LegionInfo legion = client.getLegionInfo(marker.getId());
                        int scale = 2*Scale.get();
                        
                        boolean dubiousAsBlanks = client.getOption(
                            Options.dubiousAsBlanks);
						final JPanel panel = new LegionInfoPanel(legion, 
                            scale, PANEL_MARGIN, PANEL_PADDING, true, 
                            viewMode, playerName, dubiousAsBlanks);
                        add(panel);
                        legionFlyouts[i] = panel;

                        panel.setLocation(marker.getLocation());
                        panel.setVisible(true);
                        DragListener.makeDraggable(panel);

                        repaint();
                    }
                }
            }
            else
            {
                super.keyPressed(e);
            }
        }

        public void keyReleased(KeyEvent e)
        {
            if (e.getKeyCode() == POPUP_KEY)
            {
                if (legionFlyouts != null)
                {
                    for (int i = 0; i < legionFlyouts.length; i++)
                    {
                        remove(legionFlyouts[i]);
                    }
                    repaint();
                    legionFlyouts = null;
                }
            }
            else
            {
                super.keyReleased(e);
            }
        }
    }


    private static interface MasterHexVisitor
    {

        /** Returns true iff the Hex matches **/
        boolean visitHex(MasterHex hex);
    }


    private static interface GUIMasterHexVisitor
    {

        /** Returns true iff the Hex matches **/
        boolean visitHex(GUIMasterHex hex);
    }

    private static MasterHex visitMasterHexes(MasterHexVisitor visitor)
    {
        for (int i = 0; i < plainHexArray.length; i++)
        {
            for (int j = 0; j < plainHexArray[i].length; j++)
            {
                MasterHex hex = plainHexArray[i][j];
                if (hex == null)
                {
                    continue;
                }
                boolean hexFound = visitor.visitHex(hex);
                if (hexFound)
                {
                    return hex;
                }
            }
        }
        return null;
    }

    private GUIMasterHex visitGUIMasterHexes(GUIMasterHexVisitor visitor)
    {
        for (int i = 0; i < guiHexArray.length; i++)
        {
            for (int j = 0; j < guiHexArray[i].length; j++)
            {
                GUIMasterHex hex = guiHexArray[i][j];
                if (hex == null)
                {
                    continue;
                }
                boolean hexFound = visitor.visitHex(hex);
                if (hexFound)
                {
                    return hex;
                }
            }
        }
        return null;
    }

    /** Must ensure that variant is loaded before referencing this class,
     *  since readMapData() needs it. */
    public synchronized static void staticMasterboardInit()
    {
        // variant can change these
        horizSize = 0;
        vertSize = 0;
        boardParity = 0;
        plainHexArray = null;
        show = null;
        towerSet = null;

        try
        {
            readMapData();
        }
        catch (Exception e)
        {
            Log.error("Reading map data for non-GUI failed : " + e);
            e.printStackTrace();
            System.exit(1);
        }

        Log.debug("Setting up static TowerSet in MasterBoard");
        setupTowerSet();
    }

    MasterBoard(final Client client)
    {
        this.client = client;
        
        masterFrame = new JFrame("MasterBoard");
        masterFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        contentPane = masterFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        setOpaque(true);
        setupIcon();
        setBackground(Color.black);
        masterFrame.addWindowListener(new MasterBoardWindowHandler());
        addMouseListener(new MasterBoardMouseHandler());
        addMouseMotionListener(new MasterBoardMouseMotionHandler());
        addKeyListener(new InfoPopupHandler(client));

        setupGUIHexes();

        setupActions();
        setupPopupMenu();
        setupTopMenu();

        scrollPane = new JScrollPane(this);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        setupPlayerLabel();

        saveWindow = new SaveWindow(client, "MasterBoardScreen");
        Point loadLocation = saveWindow.loadLocation();

        if (loadLocation == null) 
        {
            // Copy of code from KDialog.centerOnScreen();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            masterFrame.setLocation(new Point(d.width / 2 - 
                getSize().width / 2, d.height / 2 - getSize().height / 2));
        }
        else 
        {
            masterFrame.setLocation(loadLocation);
        }

        masterFrame.pack();
        masterFrame.setVisible(true);
    }

    private void setupActions()
    {
        clearRecruitChitsAction = new AbstractAction(clearRecruitChits)
        {
            public void actionPerformed(ActionEvent e)
            {
                client.clearRecruitChits();
            }
        };

        undoLastAction = new AbstractAction(undoLast)
        {
            public void actionPerformed(ActionEvent e)
            {
                Constants.Phase phase = client.getPhase();
                if (phase == Constants.Phase.SPLIT)
                {
                    client.undoLastSplit();
                    alignAllLegions();
                    highlightTallLegions();
                    repaint();
                }
                else if (phase == Constants.Phase.MOVE)
                {
                    client.undoLastMove();
                    highlightUnmovedLegions();
                }
                else if (phase == Constants.Phase.FIGHT)
                {
                    Log.error("called undoLastAction in FIGHT");
                }
                else if (phase == Constants.Phase.MUSTER)
                {
                    client.undoLastRecruit();
                    highlightPossibleRecruits();
                }
                else
                {
                    Log.error("Bogus phase");
                }
            }
        };

        undoAllAction = new AbstractAction(undoAll)
        {
            public void actionPerformed(ActionEvent e)
            {
                Constants.Phase phase = client.getPhase();
                if (phase == Constants.Phase.SPLIT)
                {
                    client.undoAllSplits();
                    alignAllLegions();
                    highlightTallLegions();
                    repaint();
                }
                else if (phase == Constants.Phase.MOVE)
                {
                    client.undoAllMoves();
                    highlightUnmovedLegions();
                }
                else if (phase == Constants.Phase.FIGHT)
                {
                    Log.error("called undoAllAction in FIGHT");
                }
                else if (phase == Constants.Phase.MUSTER)
                {
                    client.undoAllRecruits();
                    highlightPossibleRecruits();
                }
                else
                {
                    Log.error("Bogus phase");
                }
            }
        };

        doneWithPhaseAction = new AbstractAction(doneWithPhase)
        {
            public void actionPerformed(ActionEvent e)
            {
                Constants.Phase phase = client.getPhase();
                if (phase == Constants.Phase.SPLIT)
                {
                    bottomBar.disableDoneButton();
                    client.doneWithSplits();
                }
                else if (phase == Constants.Phase.MOVE)
                {
                    bottomBar.disableDoneButton();
                    client.doneWithMoves();
                }
                else if (phase == Constants.Phase.FIGHT)
                {
                    bottomBar.disableDoneButton();
                    client.doneWithEngagements();
                }
                else if (phase == Constants.Phase.MUSTER)
                {
                    bottomBar.disableDoneButton();
                    client.doneWithRecruits();
                }
                else
                {
                    Log.error("Bogus phase");
                }
            }
        };

        takeMulliganAction = new AbstractAction(takeMulligan)
        {
            public void actionPerformed(ActionEvent e)
            {
                client.mulligan();
            }
        };

        withdrawFromGameAction = new AbstractAction(withdrawFromGame)
        {
            public void actionPerformed(ActionEvent e)
            {
                String[] options = new String[2];
                options[0] = "Yes";
                options[1] = "No";
                int answer = JOptionPane.showOptionDialog(masterFrame,
                    "Are you sure you with to withdraw from the game?",
                    "Confirm Withdrawal?",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[1]);

                if (answer == JOptionPane.YES_OPTION)
                {
                    client.withdrawFromGame();
                }
            }
        };

        viewFullRecruitTreeAction = new AbstractAction(viewFullRecruitTree)
        {
            public void actionPerformed(ActionEvent e)
            {
                new ShowAllRecruits(masterFrame,
                    client,
                    TerrainRecruitLoader.getTerrains(),
                    scrollPane);
            }
        };

        viewHexRecruitTreeAction = new AbstractAction(viewHexRecruitTree)
        {
            public void actionPerformed(ActionEvent e)
            {
                GUIMasterHex hex = getHexContainingPoint(lastPoint);
                if (hex != null)
                {
                    MasterHex hexModel = hex.getMasterHexModel();
                    new ShowRecruits(masterFrame, hexModel.getTerrain(), 
                        lastPoint, hexModel.getLabel(), scrollPane);
                }
            }
        };

        viewBattleMapAction = new AbstractAction(viewBattleMap)
        {
            public void actionPerformed(ActionEvent e)
            {
                GUIMasterHex hex = getHexContainingPoint(lastPoint);
                if (hex != null)
                {
                    new ShowBattleMap(masterFrame,
                        hex.getMasterHexModel().getLabel());
                    // Work around a Windows JDK 1.3 bug.
                    hex.repaint();
                }
            }
        };

        newGameAction = new AbstractAction(Constants.newGame)
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!client.isGameOver())
                {
                    String[] options = new String[2];
                    options[0] = "Yes";
                    options[1] = "No";
                    int answer = JOptionPane.showOptionDialog(masterFrame,
                        "Are you sure you with to start a new game?",
                        "New Game?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options,
                        options[1]);

                    if (answer != JOptionPane.YES_OPTION)
                    {
                        return;
                    }
                }
                client.newGame();
            }
        };

        loadGameAction = new AbstractAction(Constants.loadGame)
        {
            public void actionPerformed(ActionEvent e)
            {
                // No need for confirmation because the user can cancel
                // from the load game dialog.
                JFileChooser chooser = new JFileChooser(Constants.saveDirname);
                chooser.setFileFilter(new XMLSnapshotFilter());
                int returnVal = chooser.showOpenDialog(masterFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    client.loadGame(chooser.getSelectedFile().getPath());
                }
            }
        };

        saveGameAction = new AbstractAction(Constants.saveGame)
        {
            public void actionPerformed(ActionEvent e)
            {
                client.saveGame(null);
            }
        };

        saveGameAsAction = new AbstractAction(saveGameAs)
        {
            // TODO: Need a confirmation dialog on overwrite?
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser(Constants.saveDirname);
                chooser.setFileFilter(new XMLSnapshotFilter());
                int returnVal = chooser.showSaveDialog(masterFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    String dirname =
                        chooser.getCurrentDirectory().getAbsolutePath();
                    String basename = chooser.getSelectedFile().getName();
                    // Add default savegame extension.
                    if (!basename.endsWith(Constants.xmlExtension))
                    {
                        basename += Constants.xmlExtension;
                    }
                    client.saveGame(dirname + '/' + basename);
                }
            }
        };

        quitGameAction = new AbstractAction(Constants.quit)
        {
            public void actionPerformed(ActionEvent e)
            {
                if (client.isGameOver())
                {
                    client.dispose();
                    System.exit(0);
                }

                String[] options = new String[2];
                options[0] = "Yes";
                options[1] = "No";
                int answer = JOptionPane.showOptionDialog(masterFrame,
                    "Are you sure you wish to quit?",
                    "Quit Game?",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[1]);
                if (answer == JOptionPane.YES_OPTION)
                {
                    client.withdrawFromGame();
                    client.dispose();
                    System.exit(0);
                }
            }
        };

        changeScaleAction = new AbstractAction(changeScale)
        {
            public void actionPerformed(ActionEvent e)
            {
                final int oldScale = Scale.get();
                final int newScale = PickIntValue.pickIntValue(masterFrame,
                    oldScale, "Pick scale", 5, 25, 1, client);
                if (newScale != oldScale)
                {
                    client.setOption(Options.scale, newScale);
                    Scale.set(newScale);
                    net.sf.colossus.util.ResourceLoader.purgeImageCache();
                    client.rescaleAllWindows();
                }
            }
        };

        chooseScreenAction = new AbstractAction(chooseScreen)
        {
            public void actionPerformed(ActionEvent e)
            {
                new ChooseScreen(getFrame(), client);
            }
        };

        aboutAction = new AbstractAction(about)
        {
            public void actionPerformed(ActionEvent e)
            {
                client.showMessageDialog(
                    "Colossus build: " + Client.getVersion() +
                    "\n" +
                    "user.home:      " + System.getProperty("user.home") +
                    "\n" +
                    "java.version:   " + System.getProperty("java.version"));
            }
        };

        viewReadmeAction = new AbstractAction(viewReadme)
        {
            public void actionPerformed(ActionEvent e)
            {
                new ShowReadme(masterFrame, client);
            }
        };
    }

    private void setupPopupMenu()
    {
        popupMenu = new JPopupMenu();
        contentPane.add(popupMenu);

        JMenuItem mi = popupMenu.add(viewHexRecruitTreeAction);
        mi.setMnemonic(KeyEvent.VK_R);

        mi = popupMenu.add(viewBattleMapAction);
        mi.setMnemonic(KeyEvent.VK_B);
    }

    private ItemListener itemHandler = new MasterBoardItemHandler();

    private void addCheckBox(JMenu menu, String name, int mnemonic)
    {
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(name);
        cbmi.setMnemonic(mnemonic);
        cbmi.setSelected(client.getOption(name));

        cbmi.addItemListener(itemHandler);
        menu.add(cbmi);
        checkboxes.put(name, cbmi);
    }

    private void setupTopMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        masterFrame.setJMenuBar(menuBar);

        // File menu

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        JMenuItem mi;

        mi = fileMenu.add(newGameAction);
        mi.setMnemonic(KeyEvent.VK_N);
        mi = fileMenu.add(loadGameAction);
        mi.setMnemonic(KeyEvent.VK_L);
        mi = fileMenu.add(saveGameAction);
        mi.setMnemonic(KeyEvent.VK_S);
        mi = fileMenu.add(saveGameAsAction);
        mi.setMnemonic(KeyEvent.VK_A);
        mi = fileMenu.add(quitGameAction);
        mi.setMnemonic(KeyEvent.VK_Q);

        // Phase menu items change by phase and will be set up later.
        phaseMenu = new JMenu("Phase");
        phaseMenu.setMnemonic(KeyEvent.VK_P);
        menuBar.add(phaseMenu);

        // Then per-player options

        JMenu playerMenu = new JMenu("Player");
        playerMenu.setMnemonic(KeyEvent.VK_Y);
        menuBar.add(playerMenu);

        addCheckBox(playerMenu, Options.autoPickColor, KeyEvent.VK_C);
        addCheckBox(playerMenu, Options.autoPickMarker, KeyEvent.VK_I);
        addCheckBox(playerMenu, Options.autoPickEntrySide, KeyEvent.VK_E);
        addCheckBox(playerMenu, Options.autoForcedStrike, KeyEvent.VK_K);
        addCheckBox(playerMenu, Options.autoCarrySingle, KeyEvent.VK_Y);
        addCheckBox(playerMenu, Options.autoRangeSingle, KeyEvent.VK_G);
        addCheckBox(playerMenu, Options.autoSummonAngels, KeyEvent.VK_O);
        addCheckBox(playerMenu, Options.autoAcquireAngels, KeyEvent.VK_A);
        addCheckBox(playerMenu, Options.autoRecruit, KeyEvent.VK_R);
        addCheckBox(playerMenu, Options.autoPickRecruiter, KeyEvent.VK_U);
        addCheckBox(playerMenu, Options.autoReinforce, KeyEvent.VK_N);
        addCheckBox(playerMenu, Options.autoPlay, KeyEvent.VK_P);

        // Then per-client GUI options
        JMenu graphicsMenu = new JMenu("Graphics");
        graphicsMenu.setMnemonic(KeyEvent.VK_G);
        menuBar.add(graphicsMenu);

        addCheckBox(graphicsMenu, Options.useSVG, KeyEvent.VK_S);
        addCheckBox(graphicsMenu, Options.stealFocus, KeyEvent.VK_F);
        addCheckBox(graphicsMenu, Options.showCaretaker, KeyEvent.VK_C);
        addCheckBox(graphicsMenu, Options.showStatusScreen, KeyEvent.VK_G);
        addCheckBox(graphicsMenu, Options.showEngagementResults,
            KeyEvent.VK_E);
        addCheckBox(graphicsMenu, Options.showAutoInspector, KeyEvent.VK_I);
        addCheckBox(graphicsMenu, Options.showEventViewer, KeyEvent.VK_E);
        // This option makes only sense with the 
        //   "view what SplitPrediction tells us" mode.
        if (client.getViewMode() == Options.viewableEverNum)
        {
            addCheckBox(graphicsMenu, Options.dubiousAsBlanks, KeyEvent.VK_D);
        }
        addCheckBox(graphicsMenu, Options.showLogWindow, KeyEvent.VK_L);
        addCheckBox(graphicsMenu, Options.antialias, KeyEvent.VK_N);
        addCheckBox(graphicsMenu, Options.useOverlay, KeyEvent.VK_V);
        addCheckBox(graphicsMenu, Options.noBaseColor, KeyEvent.VK_W);
        addCheckBox(graphicsMenu, Options.useColoredBorders, 0);
        addCheckBox(graphicsMenu, Options.doNotInvertDefender, 0);
        addCheckBox(graphicsMenu, Options.showAllRecruitChits, 0);
        // change scale
        mi = graphicsMenu.add(changeScaleAction);
        mi.setMnemonic(KeyEvent.VK_S);
        // full recruit tree
        mi = graphicsMenu.add(viewFullRecruitTreeAction);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));
        mi.setMnemonic(KeyEvent.VK_R);

        if (GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getScreenDevices().length > 1)
        {
            mi = graphicsMenu.add(chooseScreenAction);
        }

        // Then Look & Feel
        lfMenu = new JMenu("Look & Feel");
        lfMenu.setMnemonic(KeyEvent.VK_L);
        menuBar.add(lfMenu);
        UIManager.LookAndFeelInfo[] lfInfo =
            UIManager.getInstalledLookAndFeels();
        String currentLF = UIManager.getLookAndFeel().getName();
        for (int i = 0; i < lfInfo.length; i++)
        {
            AbstractAction lfAction =
                new ChangeLookFeelAction(lfInfo[i].getName(),
                lfInfo[i].getClassName());
            JCheckBoxMenuItem temp = new JCheckBoxMenuItem(lfAction);
            lfMenu.add(temp);
            temp.setState(lfInfo[i].getName().equals(currentLF));
        }

        // Then help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        mi = helpMenu.add(aboutAction);
        mi = helpMenu.add(viewReadmeAction);
        mi.setMnemonic(KeyEvent.VK_V);
    }

    class ChangeLookFeelAction extends AbstractAction
    {
        String className;
        ChangeLookFeelAction(String t, String className)
        {
            super(t);
            this.className = className;
        }

        public void actionPerformed(ActionEvent e)
        {
            client.setLookAndFeel(className);
            String currentLF = UIManager.getLookAndFeel().getName();
            for (int i = 0; i < lfMenu.getItemCount(); i++)
            {
                JCheckBoxMenuItem it = (JCheckBoxMenuItem)lfMenu.getItem(i);
                it.setState(it.getText().equals(currentLF));
            }
        }
    }

    void twiddleOption(String name, boolean enable)
    {
        JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)checkboxes.get(name);
        if (cbmi != null)
        {
            // Only set the selected state if it has changed,
            // to avoid infinite feedback loops.
            boolean previous = cbmi.isSelected();
            if (enable != previous)
            {
                cbmi.setSelected(enable);
            }
        }
    }

    /** Show which player owns this board. */
    void setupPlayerLabel()
    {
        if (playerLabelDone)
        {
            return;
        }
        String playerName = client.getPlayerName();
        if (bottomBar == null)
        {
            // add a bottom bar
            bottomBar = new BottomBar();
            contentPane.add(bottomBar, BorderLayout.SOUTH);

            // notify
            masterFrame.pack();
        }
        bottomBar.setPlayerName(playerName);

        String colorName = client.getColor();
        // If we call this before player colors are chosen, just use
        // the defaults.
        if (colorName != null)
        {
            Color color = PickColor.getBackgroundColor(colorName);
            bottomBar.setPlayerColor(color);
            // Don't do this again.
            playerLabelDone = true;
        }
    }

    private void setupGUIHexes()
    {
        guiHexArray = new GUIMasterHex[horizSize][vertSize];

        int scale = Scale.get();
        int cx = 3 * scale;
        int cy = 0 * scale;

        for (int i = 0; i < guiHexArray.length; i++)
        {
            for (int j = 0; j < guiHexArray[0].length; j++)
            {
                if (show[i][j])
                {
                    GUIMasterHex hex = new GUIMasterHex(plainHexArray[i][j]);
                    hex.init(
                        cx + 4 * i * scale,
                        (int)Math.round(cy +
                        (3 * j +
                        ((i + boardParity) & 1) * (1 + 2 * (j / 2)) +
                        ((i + 1 + boardParity) & 1) * 2 *
                        ((j + 1) / 2)) *
                        GUIHex.SQRT3 *
                        scale),
                        scale,
                        isHexInverted(i, j),
                        this);
                    guiHexArray[i][j] = hex;
                }
            }
        }
        setupNeighbors(guiHexArray);
    }

    private static boolean isHexInverted(int i, int j)
    {
        return (((i + j) & 1) == boardParity);
    }

    /** reference to the 'h' the cache was built for.
     * we have to rebuild the cache for a new 'h'
     */
    private static MasterHex[][] _hexByLabel_last_h = null;

    /** the cache used inside 'hexByLabel'. */
    private static java.util.Vector _hexByLabel_cache = null;

    /**
     * towi changes: here is now a cache implemented so that the nested
     *   loop is not executed at every call. the cache is implemented with
     *   an array. it will work as long as the hex-labels-strings can be
     *   converted to int. this must be the case anyway since the
     *   param 'label' is an int here.
     */
    static MasterHex hexByLabel(MasterHex[][] h, int label)
    {
        // if the 'h' was the same last time we can use the cache
        if (_hexByLabel_last_h != h)
        {
            // alas, we have to rebuild the cache
            Log.debug("new 'MasterHex[][] h' in MasterBoard.hexByLabel()");
            _hexByLabel_last_h = h;
            // write all 'h' elements by their int-value into an Array.
            // we can do that here, because the 'label' arg is an int. if it
            // were a string we could not rely on that all h-entries are ints.
            //  (Vector: lots of unused space, i am afraid. about 80kB...)
            _hexByLabel_cache = new java.util.Vector(1000);
            for (int i = 0; i < h.length; i++)
            {
                for (int j = 0; j < h[i].length; j++)
                {
                    if (show[i][j])
                    {
                        final int iLabel =
                            Integer.parseInt(h[i][j].getLabel());
                        if (_hexByLabel_cache.size() <= iLabel)
                        {
                            _hexByLabel_cache.setSize(iLabel + 1);
                        }
                        _hexByLabel_cache.set(iLabel, h[i][j]);
                    }
                }
            }
        }
        // the cache is built and looks like this:
        //   _hexByLabel_cache[0...] =
        //      [ h00,h01,h02, ..., null, null, ..., h30,h31,... ]
        final MasterHex found = (MasterHex)_hexByLabel_cache.get(label);
        if (found == null)
        {
            Log.warn("Couldn't find Masterhex labeled " + label);
        }
        return found;
    }

    private static synchronized void readMapData()
        throws Exception
    {
        List directories = VariantSupport.getVarDirectoriesList();
        InputStream mapIS = ResourceLoader.getInputStream(
            VariantSupport.getMapName(), directories);
        if (mapIS == null)
        {
            throw new FileNotFoundException(VariantSupport.getMapName());
        }
        StrategicMapLoader sml = new StrategicMapLoader(mapIS);
        horizSize = sml.getHorizSize();
        vertSize = sml.getVertSize();
        show = sml.getShow();
        plainHexArray = sml.getHexes();

        computeBoardParity();
        setupExits(plainHexArray);
        setupEntrances(plainHexArray);
        setupHexLabelSides(plainHexArray);
        setupNeighbors(plainHexArray);
    }

    private static void computeBoardParity()
    {
        boardParity = 0;
        for (int x = 0; x < horizSize; x++)
        {
            for (int y = 0; y < vertSize - 1; y++)
            {
                if (show[x][y] && show[x][y + 1])
                {
                    boardParity = 1 - ((x + y) & 1);
                    return;
                }
            }
        }
    }

    private static void setupExits(MasterHex[][] h)
    {
        for (int i = 0; i < h.length; i++)
        {
            for (int j = 0; j < h[i].length; j++)
            {
                if (show[i][j])
                {
                    for (int k = 0; k < 3; k++)
                    {
                        if (h[i][j].getBaseExitType(k) != Constants.NONE)
                        {
                            setupOneExit(h, i, j, k);
                        }
                    }
                }
            }
        }
    }

    private static void setupOneExit(MasterHex[][] h, int i, int j, int k)
    {
        MasterHex dh = hexByLabel(h, h[i][j].getBaseExitLabel(k));
        if (dh == null)
        {
            Log.error("null pointer ; i=" + i + ", j=" + j + ", k=" + k);
            System.exit(1);
        }
        assert dh != null; // Static analysis of Eclipse doesn't grok System.exit()
        if (dh.getXCoord() == i)
        {
            if (dh.getYCoord() == (j - 1))
            {
                h[i][j].setExitType(0, h[i][j].getBaseExitType(k));
            }
            else if (dh.getYCoord() == (j + 1))
            {
                h[i][j].setExitType(3, h[i][j].getBaseExitType(k));
            }
            else
            {
                Log.warn("bad exit ; i=" + i + ", j=" + j + ", k=" + k);
            }
        }
        else if (dh.getXCoord() == (i + 1))
        {
            if (dh.getYCoord() == j)
            {
                h[i][j].setExitType(2 - ((i + j + boardParity) & 1),
                    h[i][j].getBaseExitType(k));
            }
            else
            {
                Log.warn("bad exit ; i=" + i + ", j=" + j + ", k=" + k);
            }
        }
        else if (dh.getXCoord() == (i - 1))
        {
            if (dh.getYCoord() == j)
            {
                h[i][j].setExitType(4 + ((i + j + boardParity) & 1),
                    h[i][j].getBaseExitType(k));
            }
            else
            {
                Log.warn("bad exit ; i=" + i + ", j=" + j + ", k=" + k);
            }
        }
        else
        {
            Log.warn("bad exit ; i=" + i + ", j=" + j + ", k=" + k);
        }
    }

    private static void setupEntrances(MasterHex[][] h)
    {
        for (int i = 0; i < h.length; i++)
        {
            for (int j = 0; j < h[0].length; j++)
            {
                if (show[i][j])
                {
                    for (int k = 0; k < 6; k++)
                    {
                        int gateType = h[i][j].getExitType(k);
                        if (gateType != Constants.NONE)
                        {
                            switch (k)
                            {
                                case 0:
                                    h[i][j - 1].setEntranceType(3, gateType);
                                    break;

                                case 1:
                                    h[i + 1][j].setEntranceType(4, gateType);
                                    break;

                                case 2:
                                    h[i + 1][j].setEntranceType(5, gateType);
                                    break;

                                case 3:
                                    h[i][j + 1].setEntranceType(0, gateType);
                                    break;

                                case 4:
                                    h[i - 1][j].setEntranceType(1, gateType);
                                    break;

                                case 5:
                                    h[i - 1][j].setEntranceType(2, gateType);
                                    break;

                                default:
                                    Log.error("Bogus hexside");
                            }
                        }
                    }
                }
            }
        }
    }

    /** If the shortest hexside closest to the center of the board
     *  is a short hexside, set the label side to it.
     *  Else set the label side to the opposite hexside. */
    private static void setupHexLabelSides(MasterHex[][] h)
    {
        // First find the center of the board.
        int width = h.length;
        int height = h[0].length;

        // Subtract 1 to account for 1-based length of 0-based array.
        double midX = (width - 1) / 2.0;
        double midY = (height - 1) / 2.0;

        for (int i = 0; i < h.length; i++)
        {
            for (int j = 0; j < h[0].length; j++)
            {
                if (show[i][j])
                {
                    double deltaX = i - midX;
                    // Adjust for aspect ratio of h array, which has roughly
                    // twice as many horizontal as vertical elements even
                    // though the board is roughly square.
                    double deltaY = (j - midY) * width / height;

                    double ratio;

                    // Watch for division by zero.
                    if (deltaY == 0)
                    {
                        ratio = deltaX * 99999999;
                    }
                    else
                    {
                        ratio = deltaX / deltaY;
                    }

                    // Derive the exact number if needed.
                    if (Math.abs(ratio) < 0.6)
                    {
                        // Vertically dominated, so top or bottom hexside.
                        // top, unless inverted
                        if (isHexInverted(i, j))
                        {
                            h[i][j].setLabelSide(3);
                        }
                        else
                        {
                            h[i][j].setLabelSide(0);
                        }
                    }
                    else
                    {
                        // One of the left or right side hexsides.
                        if (deltaX >= 0)
                        {
                            if (deltaY >= 0)
                            {
                                // 2 unless inverted
                                if (isHexInverted(i, j))
                                {
                                    h[i][j].setLabelSide(5);
                                }
                                else
                                {
                                    h[i][j].setLabelSide(2);
                                }
                            }
                            else
                            {
                                // 4 unless inverted
                                if (isHexInverted(i, j))
                                {
                                    h[i][j].setLabelSide(1);
                                }
                                else
                                {
                                    h[i][j].setLabelSide(4);
                                }
                            }
                        }
                        else
                        {
                            if (deltaY >= 0)
                            {
                                // 4 unless inverted
                                if (isHexInverted(i, j))
                                {
                                    h[i][j].setLabelSide(1);
                                }
                                else
                                {
                                    h[i][j].setLabelSide(4);
                                }
                            }
                            else
                            {
                                // 2 unless inverted
                                if (isHexInverted(i, j))
                                {
                                    h[i][j].setLabelSide(5);
                                }
                                else
                                {
                                    h[i][j].setLabelSide(2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setupNeighbors(MasterHex[][] h)
    {
        for (int i = 0; i < h.length; i++)
        {
            for (int j = 0; j < h[0].length; j++)
            {
                if (show[i][j])
                {
                    MasterHex hex = h[i][j];

                    if (hex.getExitType(0) != Constants.NONE ||
                        hex.getEntranceType(0) != Constants.NONE)
                    {
                        hex.setNeighbor(0, h[i][j - 1]);
                    }
                    if (hex.getExitType(1) != Constants.NONE ||
                        hex.getEntranceType(1) != Constants.NONE)
                    {
                        hex.setNeighbor(1, h[i + 1][j]);
                    }
                    if (hex.getExitType(2) != Constants.NONE ||
                        hex.getEntranceType(2) != Constants.NONE)
                    {
                        hex.setNeighbor(2, h[i + 1][j]);
                    }
                    if (hex.getExitType(3) != Constants.NONE ||
                        hex.getEntranceType(3) != Constants.NONE)
                    {
                        hex.setNeighbor(3, h[i][j + 1]);
                    }
                    if (hex.getExitType(4) != Constants.NONE ||
                        hex.getEntranceType(4) != Constants.NONE)
                    {
                        hex.setNeighbor(4, h[i - 1][j]);
                    }
                    if (hex.getExitType(5) != Constants.NONE ||
                        hex.getEntranceType(5) != Constants.NONE)
                    {
                        hex.setNeighbor(5, h[i - 1][j]);
                    }
                }
            }
        }
    }

    private static void setupNeighbors(GUIMasterHex[][] h)
    {
        for (int i = 0; i < h.length; i++)
        {
            for (int j = 0; j < h[0].length; j++)
            {
                if (show[i][j])
                {
                    GUIMasterHex hex = h[i][j];
                    MasterHex hexModel = hex.getMasterHexModel();

                    if (hexModel.getExitType(0) != Constants.NONE ||
                        hexModel.getEntranceType(0) != Constants.NONE)
                    {
                        hex.setNeighbor(0, h[i][j - 1]);
                    }
                    if (hexModel.getExitType(1) != Constants.NONE ||
                        hexModel.getEntranceType(1) != Constants.NONE)
                    {
                        hex.setNeighbor(1, h[i + 1][j]);
                    }
                    if (hexModel.getExitType(2) != Constants.NONE ||
                        hexModel.getEntranceType(2) != Constants.NONE)
                    {
                        hex.setNeighbor(2, h[i + 1][j]);
                    }
                    if (hexModel.getExitType(3) != Constants.NONE ||
                        hexModel.getEntranceType(3) != Constants.NONE)
                    {
                        hex.setNeighbor(3, h[i][j + 1]);
                    }
                    if (hexModel.getExitType(4) != Constants.NONE ||
                        hexModel.getEntranceType(4) != Constants.NONE)
                    {
                        hex.setNeighbor(4, h[i - 1][j]);
                    }
                    if (hexModel.getExitType(5) != Constants.NONE ||
                        hexModel.getEntranceType(5) != Constants.NONE)
                    {
                        hex.setNeighbor(5, h[i - 1][j]);
                    }
                }
            }
        }
    }

    void setupSplitMenu()
    {
        unselectAllHexes();
        reqFocus();

        String activePlayerName = client.getActivePlayerName();

        masterFrame.setTitle(activePlayerName + " Turn " +
            client.getTurnNumber() + " : Split stacks");

        phaseMenu.removeAll();

        if (client.getPlayerName().equals(activePlayerName))
        {
            bottomBar.setOwnPhase("Split stacks");

            JMenuItem mi;

            mi = phaseMenu.add(clearRecruitChitsAction);
            mi.setMnemonic(KeyEvent.VK_C);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(undoLastAction);
            mi.setMnemonic(KeyEvent.VK_U);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0));

            mi = phaseMenu.add(undoAllAction);
            mi.setMnemonic(KeyEvent.VK_A);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));

            mi = phaseMenu.add(doneWithPhaseAction);
            mi.setMnemonic(KeyEvent.VK_D);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(withdrawFromGameAction);
            mi.setMnemonic(KeyEvent.VK_W);

            highlightTallLegions();
        }
        else
        {
            bottomBar.setForeignPhase("(" + activePlayerName + " splits)");
        }
    }

    void setupMoveMenu()
    {
        unselectAllHexes();
        reqFocus();

        String activePlayerName = client.getActivePlayerName();
        masterFrame.setTitle(activePlayerName + " Turn " +
            client.getTurnNumber() + " : Movement Roll: " +
            client.getMovementRoll());

        phaseMenu.removeAll();

        if (client.getPlayerName().equals(activePlayerName))
        {
            bottomBar.setOwnPhase("Movement");
            bottomBar.disableDoneButton();
            
            JMenuItem mi;

            mi = phaseMenu.add(clearRecruitChitsAction);
            mi.setMnemonic(KeyEvent.VK_C);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(undoLastAction);
            mi.setMnemonic(KeyEvent.VK_U);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0));

            mi = phaseMenu.add(undoAllAction);
            mi.setMnemonic(KeyEvent.VK_A);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));

            mi = phaseMenu.add(doneWithPhaseAction);
            mi.setMnemonic(KeyEvent.VK_D);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));

            if (client.getMulligansLeft() > 0)
            {
                phaseMenu.addSeparator();
                mi = phaseMenu.add(takeMulliganAction);
                mi.setMnemonic(KeyEvent.VK_M);
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
            }

            phaseMenu.addSeparator();

            mi = phaseMenu.add(withdrawFromGameAction);
            mi.setMnemonic(KeyEvent.VK_W);

            highlightUnmovedLegions();
        }
        else
        {
            bottomBar.setForeignPhase("(" + activePlayerName + " moves)");
        }

        // Force showing the updated movement die.
        repaint();
    }

    void setupFightMenu()
    {
        unselectAllHexes();
        reqFocus();

        String activePlayerName = client.getActivePlayerName();

        masterFrame.setTitle(activePlayerName + " Turn " +
            client.getTurnNumber() + " : Resolve Engagements ");

        phaseMenu.removeAll();

        if (client.getPlayerName().equals(activePlayerName))
        {
            bottomBar.setOwnPhase("Resolve Engagements");
            bottomBar.disableDoneButton();

            JMenuItem mi;

            mi = phaseMenu.add(clearRecruitChitsAction);
            mi.setMnemonic(KeyEvent.VK_C);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(doneWithPhaseAction);
            mi.setMnemonic(KeyEvent.VK_D);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(withdrawFromGameAction);
            mi.setMnemonic(KeyEvent.VK_W);

            highlightEngagements();
        }
        else
        {
            bottomBar.setForeignPhase("(" + activePlayerName + " fights)");
        }
    }

    void setupMusterMenu()
    {
        unselectAllHexes();
        reqFocus();

        String activePlayerName = client.getActivePlayerName();

        masterFrame.setTitle(activePlayerName + " Turn " +
            client.getTurnNumber() + " : Muster Recruits ");

        phaseMenu.removeAll();

        if (client.getPlayerName().equals(activePlayerName))
        {
            bottomBar.setOwnPhase("Muster Recruits");

            JMenuItem mi;

            mi = phaseMenu.add(clearRecruitChitsAction);
            mi.setMnemonic(KeyEvent.VK_C);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(undoLastAction);
            mi.setMnemonic(KeyEvent.VK_U);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0));

            mi = phaseMenu.add(undoAllAction);
            mi.setMnemonic(KeyEvent.VK_A);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));

            mi = phaseMenu.add(doneWithPhaseAction);
            mi.setMnemonic(KeyEvent.VK_D);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));

            phaseMenu.addSeparator();

            mi = phaseMenu.add(withdrawFromGameAction);
            mi.setMnemonic(KeyEvent.VK_W);

            highlightPossibleRecruits();
        }
        else
        {
            bottomBar.setForeignPhase("(" + activePlayerName + " musters)");
        }
    }

    void highlightPossibleRecruits()
    {
        unselectAllHexes();
        selectHexesByLabels(client.getPossibleRecruitHexes());
    }

    JFrame getFrame()
    {
        return masterFrame;
    }

    void alignLegions(String hexLabel)
    {
        GUIMasterHex hex = getGUIHexByLabel(hexLabel);
        if (hex == null)
        {
            return;
        }
        List markerIds = client.getLegionsByHex(hexLabel);

        int numLegions = markerIds.size();
        if (numLegions == 0)
        {
            hex.repaint();
            return;
        }

        String markerId = (String)markerIds.get(0);
        Marker marker = client.getMarker(markerId);
        if (marker == null)
        {
            hex.repaint();
            return;
        }

        int chitScale = marker.getBounds().width;
        Point startingPoint = hex.getOffCenter();
        Point point = new Point(startingPoint);

        if (numLegions == 1)
        {
            // Place legion in the center of the hex.
            int chitScale2 = chitScale / 2;
            point.x -= chitScale2;
            point.y -= chitScale2;
            marker.setLocation(point);
        }
        else if (numLegions == 2)
        {
            // Place legions in NW and SE corners.
            int chitScale4 = chitScale / 4;
            point.x -= 3 * chitScale4;
            point.y -= 3 * chitScale4;
            marker.setLocation(point);

            point = new Point(startingPoint);
            point.x -= chitScale4;
            point.y -= chitScale4;
            markerId = (String)markerIds.get(1);
            marker = client.getMarker(markerId);
            if (marker != null)
            {
                // Second marker can be null when loading during
                // the engagement phase.
                marker.setLocation(point);
            }
        }
        else if (numLegions == 3)
        {
            // Place legions in NW, SE, NE corners.
            int chitScale4 = chitScale / 4;
            point.x -= 3 * chitScale4;
            point.y -= 3 * chitScale4;
            marker.setLocation(point);

            point = new Point(startingPoint);
            point.x -= chitScale4;
            point.y -= chitScale4;
            markerId = (String)markerIds.get(1);
            marker = client.getMarker(markerId);
            marker.setLocation(point);

            point = new Point(startingPoint);
            point.x -= chitScale4;
            point.y -= chitScale;
            markerId = (String)markerIds.get(2);
            marker = client.getMarker(markerId);
            marker.setLocation(point);
        }

        hex.repaint();
    }

    void alignLegions(Set hexLabels)
    {
        Iterator it = hexLabels.iterator();
        while (it.hasNext())
        {
            String hexLabel = (String)it.next();
            alignLegions(hexLabel);
        }
    }

    /** This is incredibly inefficient. */
    void alignAllLegions()
    {
        visitMasterHexes(new MasterHexVisitor()
        {
            public boolean visitHex(MasterHex hex)
            {
                alignLegions(hex.getLabel());
                return false;
            }
        }
        );
    }

    void highlightTallLegions()
    {
        unselectAllHexes();
        selectHexesByLabels(client.findTallLegionHexes());
    }

    void highlightUnmovedLegions()
    {
        unselectAllHexes();
        selectHexesByLabels(client.findUnmovedLegionHexes());
        repaint();
    }

    /** Select hexes where this legion can move. */
    private void highlightMoves(String markerId)
    {
        unselectAllHexes();

        Set teleport = client.listTeleportMoves(markerId);
        selectHexesByLabels(teleport, HTMLColor.purple);

        Set normal = client.listNormalMoves(markerId);
        selectHexesByLabels(normal, Color.white);

        Set combo = new HashSet();
        combo.addAll(teleport);
        combo.addAll(normal);

        client.addPossibleRecruitChits(markerId, combo);
    }

    void highlightEngagements()
    {
        Set set = client.findEngagements();
        unselectAllHexes();
        selectHexesByLabels(set);
    }

    /** Return number of legions with summonable angels. */
    int highlightSummonableAngels(String markerId)
    {
        Set set = client.findSummonableAngelHexes(markerId);
        unselectAllHexes();
        selectHexesByLabels(set);
        return set.size();
    }

    private void setupIcon()
    {
        List directories = new ArrayList();
        directories.add(Constants.defaultDirName +
            ResourceLoader.getPathSeparator() +
            Constants.imagesDirName);

        String[] iconNames = { Constants.masterboardIconImage,
            Constants.masterboardIconText +
                "-Name-" +
                Constants.masterboardIconTextColor,
            Constants.masterboardIconSubscript +
                "-Subscript-" +
                Constants.masterboardIconTextColor };

        Image image =
            ResourceLoader.getCompositeImage(iconNames,
            directories,
            60, 60);

        if (image == null)
        {
            Log.error("Couldn't find Colossus icon");
        }
        else
        {
            masterFrame.setIconImage(image);
        }
    }

    /** Do a brute-force search through the hex array, looking for
     *  a match.  Return the hex, or null if none is found. */
    public static MasterHex getHexByLabel(final String label)
    {
        return visitMasterHexes(new MasterHexVisitor()
        {
            public boolean visitHex(MasterHex hex)
            {
                if (hex.getLabel().equals(label))
                {
                    return true;
                }
                return false;
            }
        }
        );
    }

    /** Do a brute-force search through the hex array, looking for
     *  a match.  Return the hex, or null if none is found. */
    GUIMasterHex getGUIHexByLabel(final String label)
    {
        return visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                return hex.getMasterHexModel().getLabel().equals(label);
            }
        }
        );
    }

    /** Return the MasterHex that contains the given point, or
     *  null if none does. */
    private GUIMasterHex getHexContainingPoint(final Point point)
    {
        return visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                return hex.contains(point);
            }
        }
        );
    }

    /** Return the topmost Marker that contains the given point, or
     *  null if none does. */
    private Marker getMarkerAtPoint(Point point)
    {
        List markers = client.getMarkers();
        ListIterator lit = markers.listIterator(markers.size());
        while (lit.hasPrevious())
        {
            Marker marker = (Marker)lit.previous();
            if (marker != null && marker.contains(point))
            {
                return marker;
            }
        }
        return null;
    }

    void unselectAllHexes()
    {
        visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                if (hex.isSelected())
                {
                    hex.unselect();
                    hex.repaint();
                }
                return false; //keep going
            }
        }
        );
    }

    void unselectHexByLabel(final String label)
    {
        visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                if (hex.isSelected() &&
                    label.equals(hex.getMasterHexModel().getLabel()))
                {
                    hex.unselect();
                    hex.repaint();
                    return true;
                }
                return false; //keep going
            }
        }
        );
    }

    void unselectHexesByLabels(final Set labels)
    {
        visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                if (hex.isSelected() &&
                    labels.contains(hex.getMasterHexModel().getLabel()))
                {
                    hex.unselect();
                    hex.repaint();
                }
                return false; //keep going
            }
        }
        );
    }

    void selectHexByLabel(final String label)
    {
        visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                if (!hex.isSelected() &&
                    label.equals(hex.getMasterHexModel().getLabel()))
                {
                    hex.select();
                    hex.repaint();
                }
                return false; //keep going
            }
        }
        );
    }

    void selectHexesByLabels(final Set labels)
    {
        visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                if (!hex.isSelected() &&
                    labels.contains(hex.getMasterHexModel().getLabel()))
                {
                    hex.select();
                    hex.repaint();
                }
                return false; //keep going
            }
        }
        );
    }

    void selectHexesByLabels(final Set labels, final Color color)
    {
        visitGUIMasterHexes(new GUIMasterHexVisitor()
        {
            public boolean visitHex(GUIMasterHex hex)
            {
                if (labels.contains(hex.getMasterHexModel().getLabel()))
                {
                    hex.select();
                    hex.setSelectColor(color);
                    hex.repaint();
                }
                return false; // keep going
            }
        }
        );
    }

    void actOnMisclick()
    {
        Constants.Phase phase = client.getPhase();
        if (phase == Constants.Phase.SPLIT)
        {
            highlightTallLegions();
        }
        else if (phase == Constants.Phase.MOVE)
        {
            client.clearRecruitChits();
            client.setMoverId(null);
            highlightUnmovedLegions();
        }
        else if (phase == Constants.Phase.FIGHT)
        {
            SummonAngel summonAngel = client.getSummonAngel();
            if (summonAngel != null)
            {
                highlightSummonableAngels(summonAngel.getMarkerId());
                summonAngel.repaint();
            }
            else
            {
                highlightEngagements();
            }
        }
        else if (phase == Constants.Phase.MUSTER)
        {
            highlightPossibleRecruits();
        }
    }

    /** Return true if the MouseEvent e came from button 2 or 3.
     *  In theory, isPopupTrigger() is the right way to check for
     *  this.  In practice, the poor design choice of only having
     *  isPopupTrigger() fire on mouse release on Windows makes
     *  it useless here. */
    private static boolean isPopupButton(MouseEvent e)
    {
        int modifiers = e.getModifiers();
        return (((modifiers & InputEvent.BUTTON2_MASK) != 0) ||
            ((modifiers & InputEvent.BUTTON3_MASK) != 0) ||
            e.isAltDown() || e.isControlDown());
    }

    class MasterBoardMouseHandler extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            Point point = e.getPoint();
            Marker marker = getMarkerAtPoint(point);
            GUIMasterHex hex = getHexContainingPoint(point);
            if (marker != null)
            {
                String markerId = marker.getId();

                // Move the clicked-on marker to the top of the z-order.
                client.setMarker(markerId, marker);

                // What to do depends on which mouse button was used
                // and the current phase of the turn.

                // Right-click means to show the contents of the legion.
                if (isPopupButton(e))
                {
                	LegionInfo legion = client.getLegionInfo(markerId);
                    String playerName = client.getPlayerName();
                    int viewMode = client.getViewMode();
                    boolean dubiousAsBlanks =
                        client.getOption(Options.dubiousAsBlanks);
                    new ShowLegion(masterFrame, marker, legion,
                        point, scrollPane, 4 * Scale.get(), playerName,
                        viewMode, dubiousAsBlanks);
                    return;
                }
                else if (client.isMyLegion(markerId))
                {
                    if (hex != null)
                    {
                        actOnLegion(markerId,
                            hex.getMasterHexMod
