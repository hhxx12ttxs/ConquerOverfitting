package lb2k.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import lb2k.model.Basegame;
import lb2k.model.Game;
import lb2k.model.LB2K;
import lb2k.model.Player;
import lb2k.model.Tag;

@SuppressWarnings("serial")
public class LB2Kview extends JFrame {

    private Color light_blue = new Color(186, 207, 226, 255); // last integer is alpha

    private LB2K model;
    private JPopupMenu gamepopup;
    private JPopupMenu playerpopup;

    // private boolean debug;
    private JTable gametable1;
    private JTable gametable2;
    private JTable playertable1;
    private JTable stattable;
    private DetailedMatchPanel matchdetails;
    // private JTable playertable2;
    private String title = "LENZBERT2000 - Boardgame Manager";

    public JTable getComplexgamelist() {
        return gametable2;
    }

    public DetailedMatchPanel getMatchDetails() {
        return matchdetails;
    }

    public LB2Kview(boolean debug, boolean max, LB2K model) {
        this.model = model;
        // this.debug = debug;
        setup(max);
    }

    public void error(int errorcode) {
        if (errorcode == 1) {
            JOptionPane.showMessageDialog(this, "What a heavy burden is a name that has become too famous.\n  -Voltaire", "Name required!",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public void info(int infocode) {
        if (infocode == 1) {
            JLabel label = new JLabel("<html>This software is developed by\n" + "<ul><b>Lorentz RoĂbacher: </b><br>lancetekk@googlemail.com</ul</html>");

            JOptionPane.showMessageDialog(this, label, "Lenzbert2000, Boardgame Manager", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private void setup(boolean max) {
        setJMenuBar(getMenu());
        this.setMinimumSize(new Dimension(1024, 768));
        if (max)
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle(title);
        this.getContentPane().setLayout(new BorderLayout());
        this.setLocationByPlatform(false);
        this.pack();
        this.setVisible(true);

        this.getContentPane().add(getTabs());
    }

    private JTabbedPane getTabs() {
        JTabbedPane tabmain = new JTabbedPane();
        tabmain.add("Games", getGameTab());
        tabmain.add("Players", getPlayerTab());
        tabmain.add("Advanced", getAdvancedTab());
        tabmain.add("Matches", getMatchTab());
        return tabmain;
    }

    private JPanel getGameTab() {
        JPanel gametab = new JPanel();
        gametab.setLayout(new BorderLayout());
        gametab.add(getGameHeader(), BorderLayout.NORTH);
        gametab.add(getGameList());

        return gametab;
    }

    private JPanel getPlayerTab() {
        JPanel playertab = new JPanel();
        playertab.setLayout(new BorderLayout());
        playertab.add(getPlayerHeader(), BorderLayout.NORTH);
        playertab.add(getPlayerList());

        return playertab;
    }

    private Component getAdvancedTab() {
        JPanel advancedTab = new JPanel();
        advancedTab.setLayout(new BorderLayout());
        advancedTab.add(getAdvancedHeader(), BorderLayout.NORTH);
        advancedTab.add(getAdvancedPlayerList(), BorderLayout.CENTER);
        advancedTab.add(getAdvancedGameList(), BorderLayout.SOUTH);
        return advancedTab;
    }

    private Component getAdvancedHeader() {
        JPanel header = new JPanel();
        header.setPreferredSize(new Dimension(this.getSize().width, 36));
        header.setBackground(light_blue);
        header.setMaximumSize(new Dimension(this.getSize().width, 36));
        return header;
    }

    private JPanel getGameHeader() {
        JPanel header = new JPanel();
        header.setPreferredSize(new Dimension(this.getSize().width, 36));
        header.setBackground(light_blue);
        header.setMaximumSize(new Dimension(this.getSize().width, 36));

        final JTextField number = new HintTextField("0", 2);
        JLabel numberstext = new JLabel("Amount of Players:");

        final JTextField time = new HintTextField("0", 2);
        JLabel timetext = new JLabel("Amount of Time:");

        final JTextField tags = new HintTextField("enter Tags here", 15);
        JLabel tagtext = new JLabel("Tags:");

        JButton calc = new JButton("Update Gamelist");
        calc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int playersreq = 0;
                try {
                    playersreq = Integer.parseInt(number.getText());
                } catch (NumberFormatException e) {
                }
                if (playersreq < 0) {
                    playersreq = 0;
                }
                int timereq = 0;
                try {
                    timereq = Integer.parseInt(time.getText());
                } catch (NumberFormatException e) {
                }
                if (timereq < 0) {
                    timereq = 0;
                }

                String tagtext = tags.getText();
                String[] tagreqs;
                if (tagtext.equalsIgnoreCase("")) {
                    tagreqs = null;
                } else {
                    tagreqs = tags.getText().split(",");
                    for (int i = 0; i < tagreqs.length; i++) {
                        tagreqs[i] = tagreqs[i].trim();
                    }
                }
                ((GameOverviewModel) gametable1.getModel()).refillData(playersreq, timereq, tagreqs);
            }
        });

        JButton reset = new JButton("Reset Filters");
        reset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                number.setText("");
                number.grabFocus();
                number.transferFocus();
                time.setText("");
                time.grabFocus();
                time.transferFocus();
                tags.setText("enter Tags here");
                tags.grabFocus();
                tags.transferFocus();
                gametable1.grabFocus();
                ((GameOverviewModel) gametable1.getModel()).refillData(0, 0, null);
            }
        });

        header.add(numberstext);
        header.add(number);
        header.add(timetext);
        header.add(time);
        header.add(tagtext);
        header.add(tags);
        header.add(reset);
        // header.add(Box.createHorizontalStrut(150));
        header.add(calc);

        return header;
    }

    private JPanel getPlayerHeader() {
        JPanel header = new JPanel();
        header.setPreferredSize(new Dimension(this.getSize().width, 36));
        header.setBackground(light_blue);
        header.setMaximumSize(new Dimension(this.getSize().width, 36));

        final JTextField number = new HintTextField("0", 2);
        JLabel numberstext = new JLabel("Games played:");

        // final SimpleDateFormat dt1 = new SimpleDateFormat("dd.MM.yyyy");

        // final JTextField date = new HintTextField(dt1.format(new Date()), 6);
        // JLabel datetext = new JLabel("last Game played:");

        final JTextField tags = new HintTextField("enter Tags here", 15);
        JLabel tagtext = new JLabel("Tags:");

        final JTextField games = new HintTextField("enter Games here", 15);
        JLabel gametext = new JLabel("Games:");

        JButton calc = new JButton("Update Playerlist");
        calc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int gamesreq = 0;
                try {
                    gamesreq = Integer.parseInt(number.getText());
                } catch (NumberFormatException e) {
                }
                if (gamesreq < 0) {
                    gamesreq = 0;
                }
                Date datereq = new Date(0l);
                // try {
                // datereq = dt1.parse(date.getText());
                // } catch (ParseException e) {
                // }

                String tagtext = tags.getText();
                String[] tagreqs;
                if (tagtext.equalsIgnoreCase("")) {
                    tagreqs = null;
                } else {
                    tagreqs = tags.getText().split(",");
                    for (int i = 0; i < tagreqs.length; i++) {
                        tagreqs[i] = tagreqs[i].trim();
                    }
                }

                String gametext = games.getText();
                String[] gamereqs;
                if (gametext.equalsIgnoreCase("")) {
                    gamereqs = null;
                } else {
                    gamereqs = games.getText().split(",");
                    for (int i = 0; i < gamereqs.length; i++) {
                        gamereqs[i] = gamereqs[i].trim();
                    }
                }
                ((PlayerOverviewModel) playertable1.getModel()).refillData(gamesreq, datereq, tagreqs, gamereqs);

            }

        });

        JButton reset = new JButton("Reset Filters");
        reset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                number.setText("");
                number.grabFocus();
                number.transferFocus();
                // date.setText("");
                // date.grabFocus();
                // date.transferFocus();
                tags.setText("");
                tags.grabFocus();
                tags.transferFocus();
                games.setText("");
                games.grabFocus();
                games.transferFocus();
                playertable1.grabFocus();
                ((PlayerOverviewModel) playertable1.getModel()).refillData(0, new Date(0l), null, null);
            }
        });

        header.add(numberstext);
        header.add(number);
        // header.add(datetext);
        // header.add(date);
        header.add(tagtext);
        header.add(tags);
        header.add(gametext);
        header.add(games);
        header.add(reset);
        header.add(calc);

        return header;
    }

    private JPanel getPlayerList() {

        JPanel playerlist = new JPanel();

        playerlist.setBackground(light_blue);
        playerlist.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        // c.gridx = 0;
        // c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        PlayerOverviewModel tablemodel = new PlayerOverviewModel(model);
        playertable1 = new JTable(tablemodel);
        playertable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playertable1.addMouseListener(new PlayerTableMouseListener(playertable1, this));

        playertable1.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(playertable1);
        playerlist.add(scrollPane, c);

        playerpopup = getPlayerPopup(playertable1);

        TableColumn column = null;
        for (int i = 0; i < playertable1.getColumnCount(); i++) {
            column = playertable1.getColumnModel().getColumn(i);
            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

            if (i == 0) {
                column.setMaxWidth(360);
                column.setMinWidth(100);
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                playertable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(playertable1, SwingConstants.LEFT));

            } else if (i == 1) {
                column.setMaxWidth(50);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                playertable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(playertable1, SwingConstants.CENTER));
            } else if (i == 2) {
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                column.setMaxWidth(100);
                column.setMinWidth(80);
            } else if (i == 3) {
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                playertable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(playertable1, SwingConstants.LEFT));
            } else if (i == 4) {
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                playertable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(playertable1, SwingConstants.LEFT));
            } else {
                column.setMaxWidth(40);
                column.setMinWidth(40);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
            }
            column.setCellRenderer(dtcr);
        }

        return playerlist;
    }

    private Component getAdvancedPlayerList() {
        // JPanel advplayerlist = new JPanel();
        // advplayerlist.setMinimumSize(new Dimension(this.getSize().width, (this.getSize().height - 36) / 2));
        //
        // advplayerlist.setLayout(new GridBagLayout());
        //
        // GridBagConstraints c = new GridBagConstraints();
        // // c.gridx = 0;
        // // c.gridy = 0;
        // c.fill = GridBagConstraints.BOTH;
        // c.weightx = 1;
        // c.weighty = 1;
        //
        // PlayerOverviewModel tablemodel = new PlayerOverviewModel(model);
        // playertable2 = new JTable(tablemodel);
        // playertable2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //
        // playertable2.getTableHeader().setReorderingAllowed(false);
        // JScrollPane scrollPane = new JScrollPane(playertable2);
        // advplayerlist.add(scrollPane, c);
        //
        // TableColumn column = null;
        // for (int i = 0; i < playertable2.getColumnCount(); i++) {
        // column = playertable2.getColumnModel().getColumn(i);
        // DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        //
        // if (i == 0) {
        // column.setMaxWidth(360);
        // column.setMinWidth(100);
        // dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        // playertable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new
        // HeaderRenderer(playertable2, SwingConstants.LEFT));
        //
        // } else if (i == 1) {
        // column.setMaxWidth(50);
        // dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        // playertable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new
        // HeaderRenderer(playertable2, SwingConstants.CENTER));
        // } else if (i == 2) {
        // dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        // column.setMaxWidth(100);
        // column.setMinWidth(80);
        // } else if (i == 3) {
        // dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        // playertable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new
        // HeaderRenderer(playertable2, SwingConstants.LEFT));
        // } else if (i == 4) {
        // dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        // playertable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new
        // HeaderRenderer(playertable2, SwingConstants.LEFT));
        // } else {
        // column.setMaxWidth(40);
        // column.setMinWidth(40);
        // dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        // }
        // column.setCellRenderer(dtcr);
        // }
        DualListBox<Player> dual = new DualListBox<Player>(model, this, "Players", light_blue);
        dual.addSourceElements(model.getAllPlayers().toArray());
        return dual;
        // return advplayerlist;
    }

    private JPanel getGameList() {

        JPanel gamelist = new JPanel();
        gamelist.setBackground(light_blue);
        gamelist.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        // c.gridx = 0;
        // c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        GameOverviewModel tablemodel = new GameOverviewModel(model);
        gametable1 = new JTable(tablemodel);
        gametable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gametable1.addMouseListener(new GameTableMouseListener(gametable1, this));
        gamepopup = getGamePopup(gametable1);
        /*
         * sort here
         */
        // RowSorter<GameOverviewModel> sorter = new TableRowSorter<GameOverviewModel>(tablemodel);
        // gametable1.setRowSorter(sorter);

        gametable1.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(gametable1);
        gamelist.add(scrollPane, c);

        TableColumn column = null;
        for (int i = 0; i < gametable1.getColumnCount(); i++) {
            column = gametable1.getColumnModel().getColumn(i);
            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

            if (i == 0) {
                column.setMaxWidth(360);
                column.setMinWidth(100);
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                gametable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(gametable1, SwingConstants.LEFT));

            } else if (i == 1) {
                column.setMaxWidth(50);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                gametable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(gametable1, SwingConstants.CENTER));
            } else if (i == 2) {
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                column.setMaxWidth(50);
            } else if (i == 3) {
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                gametable1.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(gametable1, SwingConstants.LEFT));
            } else {
                column.setMaxWidth(40);
                column.setMinWidth(40);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
            }
            column.setCellRenderer(dtcr);
        }

        return gamelist;
    }

    private Component getAdvancedGameList() {
        JPanel advgamelist = new JPanel();
        advgamelist.setPreferredSize(new Dimension(this.getSize().width, (this.getSize().height - 36) / 2));

        // TODO windowresize listener

        advgamelist.setBackground(light_blue);

        advgamelist.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        ComplexGameOverviewModel tablemodel = new ComplexGameOverviewModel(model);
        gametable2 = new JTable(tablemodel);
        gametable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gametable2.setAutoCreateRowSorter(true);

        /*
         * sort here
         */
        // RowSorter<GameOverviewModel> sorter = new TableRowSorter<GameOverviewModel>(tablemodel);
        // gametable1.setRowSorter(sorter);

        gametable2.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(gametable2);
        advgamelist.add(scrollPane, c);

        TableColumn column = null;
        for (int i = 0; i < gametable2.getColumnCount(); i++) {
            column = gametable2.getColumnModel().getColumn(i);
            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

            if (i == 0) {
                column.setMaxWidth(360);
                column.setMinWidth(100);
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                gametable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(gametable2, SwingConstants.LEFT));

            } else if (i == 1) {
                column.setMaxWidth(50);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                gametable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(gametable2, SwingConstants.CENTER));
            } else if (i == 2) {
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                column.setMaxWidth(50);
            } else if (i == 3) {
            } else if (i == 4) {
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                gametable2.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(gametable2, SwingConstants.LEFT));
            } else {
                column.setMaxWidth(40);
                column.setMinWidth(40);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
            }
            column.setCellRenderer(dtcr);
        }
        return advgamelist;
    }

    protected void showGamelistPopup(MouseEvent me, JTable table) {
        Point p = me.getPoint();
        // int row = table.rowAtPoint(p);
        gamepopup.show(table, p.x, p.y);
    }

    private JPopupMenu getGamePopup(final JTable table) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem one = new JMenuItem("Edit Game");
        one.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = table.getSelectedRow();
                Game g = ((GameOverviewModel) table.getModel()).getGame(row);

                displayGameDialog(g);
            }
        });
        JMenuItem two = new JMenuItem("Delete Game");
        menu.add(one);
        menu.add(two);
        two.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int row = table.getSelectedRow();
                Game p = ((GameOverviewModel) table.getModel()).getGame(row);
                if (sureDialog(p)) {

                    model.deleteGame(p);
                    ((GameOverviewModel) gametable1.getModel()).refillData(0, 0, null);
                }
            }
        });
        return menu;
    }

    protected void showPlayerlistPopup(MouseEvent me, JTable table) {
        Point p = me.getPoint();
        // int row = table.rowAtPoint(p);
        playerpopup.show(table, p.x, p.y);

    }

    private JPopupMenu getPlayerPopup(final JTable table) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem one = new JMenuItem("Edit Player");
        one.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = table.getSelectedRow();
                Player p = ((PlayerOverviewModel) table.getModel()).getPlayer(row);

                displayPlayerDialog(p);
            }
        });
        JMenuItem two = new JMenuItem("Delete Player");
        two.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int row = table.getSelectedRow();
                Player p = ((PlayerOverviewModel) table.getModel()).getPlayer(row);
                if (sureDialog(p)) {

                    model.deletePlayer(p);
                    ((PlayerOverviewModel) playertable1.getModel()).refillData(0, null, null, null);
                }
            }
        });

        JMenuItem three = new JMenuItem("View Statistics");
        two.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            }
        });
        menu.add(one);
        menu.add(two);
        menu.add(three);
        return menu;
    }

    private void displayGameDialog(Game g) {
        JTextField name = new JTextField(g.getName());
        JTextField time = new JTextField(String.valueOf(g.getTime()), 2);
        JTextField min = new JTextField(String.valueOf(g.getMin()), 2);
        JTextField max = new JTextField(String.valueOf(g.getMax()), 2);

        time.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    getToolkit().beep();
                    e.consume();
                }
            }
        });

        min.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    getToolkit().beep();
                    e.consume();
                }
            }
        });

        max.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    getToolkit().beep();
                    e.consume();
                }
            }
        });

        JLabel lname = new JLabel(" Name");
        JLabel ltime = new JLabel(" Time");
        JLabel lmin = new JLabel(" Min");
        JLabel lmax = new JLabel(" Max");

        GameTagDualList<Tag> taglist = new GameTagDualList<Tag>(model, this, "Tags", null, g);
        taglist.addDestinationElements(g.getTags().toArray());
        LinkedList<Tag> srct = new LinkedList<Tag>(model.getTags());
        for (Tag t : g.getTags()) {
            if (srct.contains(t)) {
                srct.remove(t);
            }
        }
        taglist.addSourceElements(srct.toArray());

        JPanel panel = new JPanel();
        // panel.setMaximumSize(new Dimension(500, 200));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(lname, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.gridx = 1;
        c.gridy = 0;
        panel.add(ltime, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.gridx = 2;
        c.gridy = 0;
        panel.add(lmin, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.2;
        c.gridx = 3;
        c.gridy = 0;
        panel.add(lmax, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0; // reset to default
        c.anchor = GridBagConstraints.PAGE_END; // bottom of space
        c.gridx = 0; // aligned with button 2
        c.gridy = 1; // third row
        panel.add(name, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0; // reset to default
        c.anchor = GridBagConstraints.PAGE_END; // bottom of space
        c.gridx = 1; // aligned with button 2
        c.gridy = 1; // third row
        panel.add(time, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0; // reset to default
        c.anchor = GridBagConstraints.PAGE_END; // bottom of space
        c.gridx = 2; // aligned with button 2
        c.gridy = 1; // third row
        panel.add(min, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0; // reset to default
        c.anchor = GridBagConstraints.PAGE_END; // bottom of space
        c.gridx = 3; // aligned with button 2
        c.gridy = 1; // third row
        panel.add(max, c);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        final JComponent[] inputs = new JComponent[] { panel, taglist };

        final JOptionPane optionPane = new JOptionPane(inputs, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);

        final JDialog dialog = new JDialog(this, "Edit " + g.getName(), true);
        dialog.setContentPane(optionPane);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
            }
        });
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
                    // If you were going to check something
                    // before closing the window, you'd do
                    // it here.
                    dialog.setVisible(false);
                }
            }
        });

        dialog.setMinimumSize(new Dimension(360, 400));
        dialog.setBackground(light_blue);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        g.setName(name.getText());
        int imax = 0;
        int imin = 0;
        int itime = 0;
        try {
            imax = Integer.parseInt(max.getText());
            imin = Integer.parseInt(min.getText());
            itime = Integer.parseInt(time.getText());
        } catch (Exception e) {

        }
        g.setMin(imin);
        g.setMax(imax);
        g.setTime(itime);
        ((GameOverviewModel) gametable1.getModel()).refillData(0, 0, null);
        return;
    }

    private void displayPlayerDialog(Player p) {

        JTextField name = new JTextField(p.getName());

        TagDualList<Tag> taglist = new TagDualList<>(model, this, "Tags", null, p);
        taglist.addDestinationElements(p.getTags().toArray());
        LinkedList<Tag> srct = new LinkedList<Tag>(model.getTags());
        for (Tag t : p.getTags()) {
            if (srct.contains(t)) {
                srct.remove(t);
            }
        }
        taglist.addSourceElements(srct.toArray());

        GameDualList<Game> gamelist = new GameDualList<>(model, this, "Games", null, p);
        gamelist.addDestinationElements(p.getGames().toArray());
        LinkedList<Game> srcg = new LinkedList<Game>(model.getAllGames());
        for (Game g : p.getGames()) {
            if (srcg.contains(g)) {
                srcg.remove(g);
            }
        }
        gamelist.addSourceElements(srcg.toArray());

        final JComponent[] inputs = new JComponent[] { new JLabel("Name of the player"), name, taglist, gamelist };

        final JOptionPane optionPane = new JOptionPane(inputs, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);

        // You can't use pane.createDialog() because that
        // method sets up the JDialog with a property change
        // listener that automatically closes the window
        // when a button is clicked.
        final JDialog dialog = new JDialog(this, "Edit " + p.getName(), true);
        dialog.setContentPane(optionPane);
        // dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
            }
        });
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
                    // If you were going to check something
                    // before closing the window, you'd do
                    // it here.
                    dialog.setVisible(false);
                }
            }
        });

        dialog.setPreferredSize(new Dimension(500, 700));
        dialog.setMinimumSize(new Dimension(500, 700));
        dialog.setBackground(light_blue);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // int value = ((Integer) optionPane.getValue()).intValue();
        // if (value == JOptionPane.YES_OPTION) {
        // // setLabel("Good.");
        // } else if (value == JOptionPane.NO_OPTION) {
        // // setLabel("Try using the window decorations "
        // // + "to close the non-auto-closing dialog. "
        // // + "You can't!");
        // } else {
        // // setLabel("Window unavoidably closed (ESC?).");
        // }

        p.setName(name.getText());
        ((PlayerOverviewModel) playertable1.getModel()).refillData(0, null, null, null);
        return;
    }

    private boolean sureDialog(Object o) {
        String name = "";
        if (o instanceof Game) {
            name = ((Game) o).getName();
        }

        if (o instanceof Tag) {
            name = ((Tag) o).getName();
        }

        if (o instanceof Player) {
            name = ((Player) o).getName();
        }
        int i = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + name, "Confirm deletion", JOptionPane.WARNING_MESSAGE,
                JOptionPane.YES_NO_OPTION);
        return i == 0;
    }

    private JMenuBar getMenu() {
        JMenuBar result = new JMenuBar();
        JMenu[] menu = { new JMenu("File"), new JMenu("Edit"), new JMenu("?") };
        for (JMenu derp : menu) {
            result.add(derp);
        }
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem newPlayer = new JMenuItem("New Player");

        newPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                addPlayer();
            }
        });

        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        newGame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                addBaseGame();
            }
        });
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                info(1);
            }
        });

        JMenuItem print = new JMenuItem("Print");
        print.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    gametable1.print();
                } catch (PrinterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        menu[0].add(newGame);
        menu[0].add(newPlayer);
        menu[0].add(print);
        // print.setEnabled(false);
        menu[0].add(close);
        menu[2].add(about);
        return result;

    }

    private void addPlayer() {
        Player player = new Player();
        displayPlayerDialog(player);

        if (player.getName().equals("")) {
            error(1);
        } else {
            model.addPlayer(player);
            ((PlayerOverviewModel) playertable1.getModel()).refillData(0, null, null, null);
        }
    }

    private void addBaseGame() {
        Game game = new Basegame();
        displayGameDialog(game);

        if (game.getName().equals("")) {
            error(1);
        } else {
            model.addGame(game);
            ((GameOverviewModel) gametable1.getModel()).refillData(0, 0, null);
        }
    }

    private JPanel getMatchHeader() {
        JPanel header = new JPanel();
        header.setPreferredSize(new Dimension(this.getSize().width, 36));
        header.setBackground(light_blue);
        header.setMaximumSize(new Dimension(this.getSize().width, 36));
        return header;
    }

    private JPanel getMatchList() {
        stattable = new JTable();
        JPanel tablepanel = new JPanel();

        tablepanel.setPreferredSize(new Dimension(this.getSize().width / 3, this.getSize().height - 36));
        tablepanel.setBackground(light_blue);

        tablepanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        MatchOverviewModel tablemodel = new MatchOverviewModel(model);
        stattable = new JTable(tablemodel);
        stattable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stattable.setAutoCreateRowSorter(true);

        stattable.addMouseListener(new MatchTableMouseListener(stattable, this));

        stattable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(stattable);
        tablepanel.add(scrollPane, c);

        TableColumn column = null;
        for (int i = 0; i < stattable.getColumnCount(); i++) {
            column = stattable.getColumnModel().getColumn(i);
            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

            if (i == 0) {
                column.setMaxWidth(360);
                column.setMinWidth(100);
                dtcr.setHorizontalAlignment(SwingConstants.LEFT);
                stattable.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(stattable, SwingConstants.LEFT));

            } else if (i == 1) {
                column.setMaxWidth(75);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
                stattable.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer(stattable, SwingConstants.CENTER));
            } else {
                column.setMaxWidth(40);
                column.setMinWidth(40);
                dtcr.setHorizontalAlignment(SwingConstants.CENTER);
            }
            column.setCellRenderer(dtcr);
        }

        // playerpopup = getPlayerPopup(playertable1);
        return tablepanel;
    }

    private DetailedMatchPanel getMatchDetail() {
        matchdetails = new DetailedMatchPanel(light_blue, model);
        matchdetails.setPreferredSize(new Dimension((this.getSize().width / 3) * 2, (this.getSize().height - 36)));

        return matchdetails;
    }

    private JPanel getMatchTab() {
        JPanel matchtab = new JPanel();

        matchtab.setLayout(new BorderLayout());

        matchtab.add(getMatchHeader(), BorderLayout.PAGE_START);
        matchtab.add(getMatchList(), BorderLayout.LINE_START);
        matchtab.add(getMatchDetail());

        return matchtab;
    }
}

