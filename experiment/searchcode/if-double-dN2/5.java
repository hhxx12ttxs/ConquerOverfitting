/*
 * JFrPlayersManager.java
 */
package gotha;

import java.rmi.*;

import java.net.*;
import java.awt.*;
import java.awt.PageAttributes.OriginType;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.URL;

/**
 *
 * @author  Luc Vannier
 */
public class JFrPlayersManager extends javax.swing.JFrame {
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    private int playersSortType = PlayerComparator.NAME_ORDER;
    private final static int PLAYER_MODE_NEW = 1;
    private final static int PLAYER_MODE_MODIF = 2;
    private int playerMode = PLAYER_MODE_NEW;
    private Player playerInModification = null;
    private static final int PLAYER_NUMBER_COL = 0;
    private static final int REG_COL = 1;
    private static final int NAME_COL = 2;
    private static final int FIRSTNAME_COL = 3;
    private static final int RANK_COL = 4;
    private static final int COUNTRY_COL = 5;
    private static final int CLUB_COL = 6;
    /**  current Tournament */
    private TournamentInterface tournament;
    /** Rating List */
    private RatingList ratingList = new RatingList();

    /**
     * Creates new form JFrPlayersManager
     */
    public JFrPlayersManager(TournamentInterface tournament) throws RemoteException {
        this.tournament = tournament;

        initComponents();
        customInitComponents();
        setupRefreshTimer();
    }

    private void setupRefreshTimer() {
        ActionListener taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (tournament.getLastTournamentModificationTime() > lastComponentsUpdateTime) {
                        updateAllViews();
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer).start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * Unlike initComponents, customInitComponents is editable
     */
    private void customInitComponents() throws RemoteException {
        int w = JFrGotha.MEDIUM_FRAME_WIDTH;
        int h = JFrGotha.MEDIUM_FRAME_HEIGHT;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dim.width - w) / 2, (dim.height - h) / 2, w, h);

        setIconImage(Gotha.getIconImage());

        AutoCompletion.enable(cbxRatingList);

        this.pgbRatingList.setVisible(false);
        this.scpWelcomeSheet.setVisible(false);

        tabCkbParticipation = new JCheckBox[Gotha.MAX_NUMBER_OF_ROUNDS];
        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            tabCkbParticipation[i] = new JCheckBox();
            tabCkbParticipation[i].setText("" + (i + 1));
            tabCkbParticipation[i].setFont(new Font("Default", Font.PLAIN, 9));
            pnlParticipation.add(tabCkbParticipation[i]);
            tabCkbParticipation[i].setBounds((i % 5) * 36 + 4, (i / 5) * 20 + 20, 36, 15);
        }

        // Column names in
        TableColumnModel tcm = this.tblRegisteredPlayers.getColumnModel();
        tcm.getColumn(0).setHeaderValue("Number");
        tcm.getColumn(1).setHeaderValue("R");
        tcm.getColumn(2).setHeaderValue("Name");
        tcm.getColumn(3).setHeaderValue("First name");
        tcm.getColumn(4).setHeaderValue("Rk");
        tcm.getColumn(5).setHeaderValue("Co");
        tcm.getColumn(6).setHeaderValue("Club");

//        resetPlayerControls();

        getRootPane().setDefaultButton(btnRegister);

        initCountryList();
        resetRatingListControls();
        resetPlayerControls();
        initPnlRegisteredPlayers();
    }

    private void initCountryList(){
        File f = new File(Gotha.runningDirectory, "documents/iso_3166-1_list_en.xml");
        if (f == null) {
            System.out.println("Contry list file not found");
            return;
        }
        ArrayList<Country> alCountries = CountryList.importCountriesFromXMLFile(f);
        this.cbxCountry.removeAllItems();
        this.cbxCountry.addItem("  ");

        if (alCountries == null) return;

        for(Country c : alCountries){
            cbxCountry.addItem(c.getAlpha2Code());
        }
    }

    private void initPnlRegisteredPlayers() throws RemoteException {
        tblRegisteredPlayers.getColumnModel().getColumn(PLAYER_NUMBER_COL).setMinWidth(0);
        tblRegisteredPlayers.getColumnModel().getColumn(PLAYER_NUMBER_COL).setPreferredWidth(0);
        tblRegisteredPlayers.getColumnModel().getColumn(REG_COL).setPreferredWidth(10);
        tblRegisteredPlayers.getColumnModel().getColumn(NAME_COL).setPreferredWidth(110);
        tblRegisteredPlayers.getColumnModel().getColumn(FIRSTNAME_COL).setPreferredWidth(80);
        tblRegisteredPlayers.getColumnModel().getColumn(RANK_COL).setPreferredWidth(30);
        tblRegisteredPlayers.getColumnModel().getColumn(COUNTRY_COL).setPreferredWidth(30);
        tblRegisteredPlayers.getColumnModel().getColumn(CLUB_COL).setPreferredWidth(40);

        // Left alignment for headers
        ((DefaultTableCellRenderer) tblRegisteredPlayers.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Single selection
        tblRegisteredPlayers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updatePnlRegisteredPlayers(tournament.playersList());
    }

    private void updatePnlRegisteredPlayers(ArrayList<Player> playersList) {
        try {
            if (!tournament.isOpen()) {
                dispose();
                return;
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        int nbPreliminary = 0;
        int nbFinal = 0;
        for (Player p : playersList) {
            if (p.getRegisteringStatus().compareTo("PRE") == 0) {
                nbPreliminary++;
            }
            if (p.getRegisteringStatus().compareTo("FIN") == 0) {
                nbFinal++;
            }
        }
        txfNbPlPre.setText("" + nbPreliminary);
        txfNbPlFin.setText("" + nbFinal);
        DefaultTableModel model = (DefaultTableModel) tblRegisteredPlayers.getModel();
//        while (model.getRowCount() > 0) model.removeRow(0);
        // sort
        ArrayList<Player> displayedPlayersList = new ArrayList<Player>(playersList);

        PlayerComparator playerComparator = new PlayerComparator(playersSortType);
        Collections.sort(displayedPlayersList, playerComparator);

//        for (Player p:displayedPlayersList){
//            Vector<String> row = new Vector<String>();
//            int iP = -1;
//            row.add("" + iP);
//            row.add((p.getRegisteringStatus().compareTo("PRE")==0)?"P":"F"); 
//            row.add(p.getName());
//            row.add(p.getFirstName());
//            row.add(Player.convertIntToKD(p.getRank()));
//            row.add(p.getCountry());
//            row.add(p.getClub());
//
//            model.addRow(row);
//        }

        model.setRowCount(displayedPlayersList.size());
        for (Player p : displayedPlayersList) {
            int iP = -1;
            int line = displayedPlayersList.indexOf(p);
            model.setValueAt("" + iP, line, JFrPlayersManager.PLAYER_NUMBER_COL);
            model.setValueAt((p.getRegisteringStatus().compareTo("PRE") == 0) ? "P" : "F", line, JFrPlayersManager.REG_COL);
            model.setValueAt(p.getName(), line, JFrPlayersManager.NAME_COL);
            model.setValueAt(p.getFirstName(), line, JFrPlayersManager.FIRSTNAME_COL);
            model.setValueAt(Player.convertIntToKD(p.getRank()), line, JFrPlayersManager.RANK_COL);
            model.setValueAt(p.getCountry(), line, JFrPlayersManager.COUNTRY_COL);
            model.setValueAt(p.getClub(), line, JFrPlayersManager.CLUB_COL);
        }


    }

    private void resetRatingListControls() {
        if (ratingList.getRatingListType() == RatingList.TYPE_UNDEFINED) {
            cbxRatingList.setEnabled(false);
            cbxRatingList.setVisible(true);
            txfPlayerNameChoice.setEnabled(false);
            txfPlayerNameChoice.setVisible(false);
            scpPlayerNameChoice.setEnabled(false);
            scpPlayerNameChoice.setVisible(false);
            lstPlayerNameChoice.setEnabled(false);
            lstPlayerNameChoice.setVisible(false);

            btnUseNoRatingList.setEnabled(false);
            rdbFirstCharacters.setEnabled(false);
            rdbLevenshtein.setEnabled(false);
            txfName.requestFocusInWindow();
        } else {
            if (rdbFirstCharacters.isSelected()) {
                resetControlsForFirstCharactersSearching();
            } else {
                resetControlsForLevenshteinSearching();
            }

            btnUseNoRatingList.setEnabled(true);
        }
    }

    // Reset player related controls
    private void resetPlayerControls() throws RemoteException {
        this.playerMode = JFrPlayersManager.PLAYER_MODE_NEW;
        txfName.setText("");
        txfFirstName.setText("");
        txfRank.setText("30K");
        txfRatingOrigin.setText("");
        txfRawRating.setText("");
//        txfCountry.setText("");
        cbxCountry.setSelectedItem("  ");
        txfClub.setText("");
        txfFfgLicence.setText("");
        txfFfgLicenceStatus.setText("");
        lblFfGLicenceStatus.setText("");
        txfEgfPin.setText("");
        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            tabCkbParticipation[i].setSelected(true);
        }
        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            tabCkbParticipation[i].setEnabled(true);
        }
        this.rdbPreliminary.setSelected(true);
        this.btnRegister.setText(("Register"));

        setPnlParticipationVisibility();
    }

    private void setPnlParticipationVisibility() throws RemoteException {
        //  set pnlPartipation height to what is good for actual number of rounds
        GeneralParameterSet gps = tournament.getTournamentParameterSet().getGeneralParameterSet();
        pnlParticipation.setSize(new Dimension(190, 30 + (gps.getNumberOfRounds() + 4) / 5 * 20));

        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            if (i < gps.getNumberOfRounds()) {
                tabCkbParticipation[i].setVisible(true);
            } else {
                tabCkbParticipation[i].setVisible(false);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpRegistration = new javax.swing.ButtonGroup();
        grpAlgo = new javax.swing.ButtonGroup();
        pupRegisteredPlayers = new javax.swing.JPopupMenu();
        mniSortByName = new javax.swing.JMenuItem();
        mniSortByRank = new javax.swing.JMenuItem();
        mniRemovePlayer = new javax.swing.JMenuItem();
        mniModifyPlayer = new javax.swing.JMenuItem();
        pnlPlayer = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txfName = new javax.swing.JTextField();
        txfFirstName = new javax.swing.JTextField();
        txfRank = new javax.swing.JTextField();
        txfClub = new javax.swing.JTextField();
        txfFfgLicence = new javax.swing.JTextField();
        txfEgfPin = new javax.swing.JTextField();
        txfRatingOrigin = new javax.swing.JTextField();
        txfRawRating = new javax.swing.JTextField();
        txfFfgLicenceStatus = new javax.swing.JTextField();
        pnlParticipation = new javax.swing.JPanel();
        btnReset = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        pnlRegistration = new javax.swing.JPanel();
        rdbPreliminary = new javax.swing.JRadioButton();
        rdbFinal = new javax.swing.JRadioButton();
        btnUseEGF = new javax.swing.JButton();
        btnUseFFG = new javax.swing.JButton();
        lblRatingList = new javax.swing.JLabel();
        btnUseNoRatingList = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblFfGLicenceStatus = new javax.swing.JLabel();
        rdbFirstCharacters = new javax.swing.JRadioButton();
        rdbLevenshtein = new javax.swing.JRadioButton();
        cbxRatingList = new javax.swing.JComboBox();
        txfPlayerNameChoice = new java.awt.TextField();
        scpPlayerNameChoice = new javax.swing.JScrollPane();
        lstPlayerNameChoice = new javax.swing.JList();
        txfSMMSCorrection = new javax.swing.JTextField();
        btnUpdateFFGRatingList = new javax.swing.JButton();
        btnUpdateEGFRatingList = new javax.swing.JButton();
        ckbWelcomeSheet = new javax.swing.JCheckBox();
        scpWelcomeSheet = new javax.swing.JScrollPane();
        txpWelcomeSheet = new javax.swing.JTextPane();
        cbxCountry = new javax.swing.JComboBox();
        pgbRatingList = new javax.swing.JProgressBar();
        pnlPlayersList = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txfNbPlFin = new javax.swing.JTextField();
        txfNbPlPre = new javax.swing.JTextField();
        scpRegisteredPlayers = new javax.swing.JScrollPane();
        tblRegisteredPlayers = new javax.swing.JTable();
        btnPrint = new javax.swing.JButton();
        btnQuit = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();

        pupRegisteredPlayers.setFont(new java.awt.Font("Arial", 0, 11));
        pupRegisteredPlayers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pupRegisteredPlayersMouseExited(evt);
            }
        });

        mniSortByName.setFont(new java.awt.Font("Arial", 0, 11));
        mniSortByName.setText("Sort by name");
        mniSortByName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSortByNameActionPerformed(evt);
            }
        });
        pupRegisteredPlayers.add(mniSortByName);

        mniSortByRank.setFont(new java.awt.Font("Arial", 0, 11));
        mniSortByRank.setText("Sort by rank");
        mniSortByRank.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSortByRankActionPerformed(evt);
            }
        });
        pupRegisteredPlayers.add(mniSortByRank);

        mniRemovePlayer.setFont(new java.awt.Font("Arial", 0, 11));
        mniRemovePlayer.setText("Remove player");
        mniRemovePlayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniRemovePlayerActionPerformed(evt);
            }
        });
        pupRegisteredPlayers.add(mniRemovePlayer);

        mniModifyPlayer.setFont(new java.awt.Font("Arial", 0, 11));
        mniModifyPlayer.setText("Modify player");
        mniModifyPlayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniModifyPlayerActionPerformed(evt);
            }
        });
        pupRegisteredPlayers.add(mniModifyPlayer);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Players Manager");
        setIconImage(getIconImage());
        setResizable(false);
        getContentPane().setLayout(null);

        pnlPlayer.setBorder(javax.swing.BorderFactory.createTitledBorder("Player"));
        pnlPlayer.setLayout(null);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel2.setText("First name");
        pnlPlayer.add(jLabel2);
        jLabel2.setBounds(10, 260, 80, 13);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel3.setText("Rank");
        jLabel3.setToolTipText("from 30K to 9D");
        pnlPlayer.add(jLabel3);
        jLabel3.setBounds(10, 280, 80, 13);

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel4.setText("Country");
        jLabel4.setToolTipText("Country where the player lives (2 letters)");
        pnlPlayer.add(jLabel4);
        jLabel4.setBounds(10, 320, 60, 13);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel5.setText("Club");
        jLabel5.setToolTipText("");
        pnlPlayer.add(jLabel5);
        jLabel5.setBounds(140, 320, 30, 13);

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel6.setText("Licence");
        pnlPlayer.add(jLabel6);
        jLabel6.setBounds(10, 350, 60, 13);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel7.setText("PIN");
        pnlPlayer.add(jLabel7);
        jLabel7.setBounds(10, 370, 60, 13);

        txfName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfNameFocusLost(evt);
            }
        });
        pnlPlayer.add(txfName);
        txfName.setBounds(80, 240, 140, 20);

        txfFirstName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfFirstNameFocusLost(evt);
            }
        });
        pnlPlayer.add(txfFirstName);
        txfFirstName.setBounds(80, 260, 140, 20);
        pnlPlayer.add(txfRank);
        txfRank.setBounds(80, 280, 40, 20);
        pnlPlayer.add(txfClub);
        txfClub.setBounds(180, 320, 40, 20);

        txfFfgLicence.setEditable(false);
        pnlPlayer.add(txfFfgLicence);
        txfFfgLicence.setBounds(80, 350, 80, 20);

        txfEgfPin.setEditable(false);
        pnlPlayer.add(txfEgfPin);
        txfEgfPin.setBounds(80, 370, 80, 20);

        txfRatingOrigin.setEditable(false);
        pnlPlayer.add(txfRatingOrigin);
        txfRatingOrigin.setBounds(150, 280, 30, 20);

        txfRawRating.setEditable(false);
        pnlPlayer.add(txfRawRating);
        txfRawRating.setBounds(180, 280, 40, 20);

        txfFfgLicenceStatus.setEditable(false);
        txfFfgLicenceStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pnlPlayer.add(txfFfgLicenceStatus);
        txfFfgLicenceStatus.setBounds(160, 350, 20, 20);

        pnlParticipation.setBorder(javax.swing.BorderFactory.createTitledBorder("Participation"));
        pnlParticipation.setLayout(null);
        pnlPlayer.add(pnlParticipation);
        pnlParticipation.setBounds(240, 230, 190, 170);

        btnReset.setText("Reset");
        btnReset.setToolTipText("Reset form");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnReset);
        btnReset.setBounds(240, 420, 190, 30);

        btnRegister.setText("Register");
        btnRegister.setToolTipText("Register player into tournament");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnRegister);
        btnRegister.setBounds(10, 460, 420, 30);

        pnlRegistration.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Registration"));
        pnlRegistration.setLayout(null);

        grpRegistration.add(rdbPreliminary);
        rdbPreliminary.setFont(new java.awt.Font("Tahoma", 0, 10));
        rdbPreliminary.setText("Preliminary");
        pnlRegistration.add(rdbPreliminary);
        rdbPreliminary.setBounds(10, 20, 90, 21);

        grpRegistration.add(rdbFinal);
        rdbFinal.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        rdbFinal.setSelected(true);
        rdbFinal.setText("Final");
        pnlRegistration.add(rdbFinal);
        rdbFinal.setBounds(110, 20, 90, 21);

        pnlPlayer.add(pnlRegistration);
        pnlRegistration.setBounds(10, 400, 210, 50);

        btnUseEGF.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnUseEGF.setText("Use EGF rating list");
        btnUseEGF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUseEGFActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnUseEGF);
        btnUseEGF.setBounds(10, 30, 180, 25);

        btnUseFFG.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnUseFFG.setText("Use FFG rating list");
        btnUseFFG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUseFFGActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnUseFFG);
        btnUseFFG.setBounds(10, 90, 180, 25);

        lblRatingList.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblRatingList.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRatingList.setText("No rating list has been loaded yet");
        pnlPlayer.add(lblRatingList);
        lblRatingList.setBounds(200, 10, 230, 14);

        btnUseNoRatingList.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnUseNoRatingList.setText("Do not use a rating list");
        btnUseNoRatingList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUseNoRatingListActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnUseNoRatingList);
        btnUseNoRatingList.setBounds(10, 190, 180, 25);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setText("Name");
        pnlPlayer.add(jLabel1);
        jLabel1.setBounds(10, 240, 80, 13);

        lblFfGLicenceStatus.setFont(new java.awt.Font("Tahoma", 2, 11));
        lblFfGLicenceStatus.setForeground(new java.awt.Color(255, 0, 102));
        pnlPlayer.add(lblFfGLicenceStatus);
        lblFfGLicenceStatus.setBounds(180, 350, 60, 20);

        grpAlgo.add(rdbFirstCharacters);
        rdbFirstCharacters.setFont(new java.awt.Font("Tahoma", 0, 10));
        rdbFirstCharacters.setSelected(true);
        rdbFirstCharacters.setText("Compare first characters");
        rdbFirstCharacters.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rdbFirstCharacters.setEnabled(false);
        rdbFirstCharacters.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rdbFirstCharacters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbFirstCharactersActionPerformed(evt);
            }
        });
        pnlPlayer.add(rdbFirstCharacters);
        rdbFirstCharacters.setBounds(10, 150, 190, 13);

        grpAlgo.add(rdbLevenshtein);
        rdbLevenshtein.setFont(new java.awt.Font("Tahoma", 0, 10));
        rdbLevenshtein.setText("Use Levenshtein algorithm");
        rdbLevenshtein.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rdbLevenshtein.setEnabled(false);
        rdbLevenshtein.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rdbLevenshtein.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbLevenshteinActionPerformed(evt);
            }
        });
        pnlPlayer.add(rdbLevenshtein);
        rdbLevenshtein.setBounds(10, 170, 190, 13);

        cbxRatingList.setMaximumRowCount(9);
        cbxRatingList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        cbxRatingList.setToolTipText("");
        cbxRatingList.setEnabled(false);
        cbxRatingList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxRatingListItemStateChanged(evt);
            }
        });
        pnlPlayer.add(cbxRatingList);
        cbxRatingList.setBounds(200, 30, 230, 20);

        txfPlayerNameChoice.setText("Enter approximate name and firstname");
        txfPlayerNameChoice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txfPlayerNameChoiceKeyPressed(evt);
            }
        });
        txfPlayerNameChoice.addTextListener(new java.awt.event.TextListener() {
            public void textValueChanged(java.awt.event.TextEvent evt) {
                txfPlayerNameChoiceTextValueChanged(evt);
            }
        });
        pnlPlayer.add(txfPlayerNameChoice);
        txfPlayerNameChoice.setBounds(200, 30, 230, 30);

        lstPlayerNameChoice.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPlayerNameChoiceValueChanged(evt);
            }
        });
        scpPlayerNameChoice.setViewportView(lstPlayerNameChoice);

        pnlPlayer.add(scpPlayerNameChoice);
        scpPlayerNameChoice.setBounds(200, 60, 230, 160);

        txfSMMSCorrection.setEditable(false);
        txfSMMSCorrection.setToolTipText("smms correction (relevant for Mac-Mahon super-groups)");
        pnlPlayer.add(txfSMMSCorrection);
        txfSMMSCorrection.setBounds(120, 280, 20, 20);

        btnUpdateFFGRatingList.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnUpdateFFGRatingList.setText("update FFG rating list from ...");
        btnUpdateFFGRatingList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateFFGRatingListActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnUpdateFFGRatingList);
        btnUpdateFFGRatingList.setBounds(10, 120, 180, 15);

        btnUpdateEGFRatingList.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnUpdateEGFRatingList.setText("update EGF rating list from ...");
        btnUpdateEGFRatingList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateEGFRatingListActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnUpdateEGFRatingList);
        btnUpdateEGFRatingList.setBounds(10, 60, 180, 15);

        ckbWelcomeSheet.setText("Print Welcome sheet");
        ckbWelcomeSheet.setToolTipText("Welcome sheet can be edited in welcomesheet/welcomesheet.html");
        pnlPlayer.add(ckbWelcomeSheet);
        ckbWelcomeSheet.setBounds(10, 490, 220, 23);

        scpWelcomeSheet.setViewportView(txpWelcomeSheet);

        pnlPlayer.add(scpWelcomeSheet);
        scpWelcomeSheet.setBounds(0, 600, 840, 1188);

        cbxCountry.setEditable(true);
        cbxCountry.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pnlPlayer.add(cbxCountry);
        cbxCountry.setBounds(80, 320, 50, 20);

        pgbRatingList.setStringPainted(true);
        pnlPlayer.add(pgbRatingList);
        pgbRatingList.setBounds(200, 30, 230, 19);

        getContentPane().add(pnlPlayer);
        pnlPlayer.setBounds(10, 0, 440, 520);

        pnlPlayersList.setBorder(javax.swing.BorderFactory.createTitledBorder("List of players"));
        pnlPlayersList.setLayout(null);

        jLabel8.setText("Registered players. Final (F)");
        pnlPlayersList.add(jLabel8);
        jLabel8.setBounds(60, 50, 250, 20);

        jLabel9.setText("Registered players. Preliminary (P)");
        pnlPlayersList.add(jLabel9);
        jLabel9.setBounds(60, 30, 250, 20);

        txfNbPlFin.setEditable(false);
        txfNbPlFin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pnlPlayersList.add(txfNbPlFin);
        txfNbPlFin.setBounds(10, 50, 40, 20);

        txfNbPlPre.setEditable(false);
        txfNbPlPre.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pnlPlayersList.add(txfNbPlPre);
        txfNbPlPre.setBounds(10, 30, 40, 20);

        scpRegisteredPlayers.setToolTipText("");

        tblRegisteredPlayers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Number", "R", "Name", "First name", "Rk", "Co", "Club"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblRegisteredPlayers.setToolTipText("To modify, right click !");
        tblRegisteredPlayers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tblRegisteredPlayersKeyPressed(evt);
            }
        });
        tblRegisteredPlayers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblRegisteredPlayersMouseClicked(evt);
            }
        });
        scpRegisteredPlayers.setViewportView(tblRegisteredPlayers);

        pnlPlayersList.add(scpRegisteredPlayers);
        scpRegisteredPlayers.setBounds(10, 80, 300, 340);

        btnPrint.setText("Print ...");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        pnlPlayersList.add(btnPrint);
        btnPrint.setBounds(10, 430, 300, 30);

        getContentPane().add(pnlPlayersList);
        pnlPlayersList.setBounds(460, 0, 320, 470);

        btnQuit.setText("Quit");
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitActionPerformed(evt);
            }
        });
        getContentPane().add(btnQuit);
        btnQuit.setBounds(590, 480, 190, 30);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gotha/gothalogo16.jpg"))); // NOI18N
        btnHelp.setText("help");
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });
        getContentPane().add(btnHelp);
        btnHelp.setBounds(470, 480, 110, 30);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txfFirstNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfFirstNameFocusLost
        txfFirstName.setText(normalizeCase(txfFirstName.getText()));
    }//GEN-LAST:event_txfFirstNameFocusLost

    private void txfNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfNameFocusLost
        txfName.setText(normalizeCase(txfName.getText()));
    }//GEN-LAST:event_txfNameFocusLost

    private String normalizeCase(String name) {
        StringBuffer sb = new StringBuffer();
        Pattern namePattern = Pattern.compile(
                "(?:(da|de|degli|del|der|di|el|la|le|ter|und|van|vom|von|zu|zum)" +
                "|(.+?))(?:\\b|(?=_))([- _]?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = namePattern.matcher(name.trim().toLowerCase());
        while (matcher.find()) {
            String noblePart = matcher.group(1);
            String namePart = matcher.group(2);
            String wordBreak = matcher.group(3);
            if (noblePart != null) {
                sb.append(noblePart);
            } else {
                sb.append(Character.toUpperCase(namePart.charAt(0)));
                sb.append(namePart.substring(1)); // always returns at least ""
            }
            if (wordBreak != null) {
                sb.append(wordBreak);
            }
        }
        return sb.toString();
    }

    private void pupRegisteredPlayersMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pupRegisteredPlayersMouseExited
        pupRegisteredPlayers.setVisible(false);
    }//GEN-LAST:event_pupRegisteredPlayersMouseExited

    private void tblRegisteredPlayersKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblRegisteredPlayersKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            removeSelectedPlayer();
        }
    }//GEN-LAST:event_tblRegisteredPlayersKeyPressed

    private void mniModifyPlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniModifyPlayerActionPerformed
        pupRegisteredPlayers.setVisible(false);
        modifySelectedPlayer();
    }//GEN-LAST:event_mniModifyPlayerActionPerformed

    private void modifySelectedPlayer() {
        try {
            resetPlayerControls();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        this.playerMode = JFrPlayersManager.PLAYER_MODE_MODIF;

        // What player ?
        int row = tblRegisteredPlayers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please, select a player !");
            return;
        }
        String name = (String) this.tblRegisteredPlayers.getModel().getValueAt(row, JFrPlayersManager.NAME_COL);
        String firstName = (String) this.tblRegisteredPlayers.getModel().getValueAt(row, JFrPlayersManager.FIRSTNAME_COL);
        try {
            playerInModification = tournament.getPlayerByKeyString(name + firstName);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        updatePlayerControlsFromPlayerInModification();
        this.btnRegister.setText("Save modification");
    }

    private void mniRemovePlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRemovePlayerActionPerformed
        pupRegisteredPlayers.setVisible(false);
        removeSelectedPlayer();
    }//GEN-LAST:event_mniRemovePlayerActionPerformed

    private void removeSelectedPlayer() {
        // What player ?
        int row = tblRegisteredPlayers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, ("Please, select a player !"));
            return;
        }
        String name = (String) this.tblRegisteredPlayers.getModel().getValueAt(row, JFrPlayersManager.NAME_COL);
        String firstName = (String) this.tblRegisteredPlayers.getModel().getValueAt(row, JFrPlayersManager.FIRSTNAME_COL);
        try {
            Player playerToRemove = tournament.getPlayerByKeyString(name + firstName);
            // You sure ?
            String strMessage = "Remove " + playerToRemove.fullName() + " ?";
            int rep = JOptionPane.showConfirmDialog(this, strMessage, ("Remove_player"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (rep == JOptionPane.YES_OPTION) {
                boolean b = tournament.removePlayer(playerToRemove);
                if (b) {
                    resetRatingListControls();
                    resetPlayerControls();
                    this.tournamentChanged();
                } else {
                    strMessage = "" + name + " " + firstName + "could not be removed";
                    JOptionPane.showMessageDialog(this, strMessage, "Message", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (TournamentException te) {
            JOptionPane.showMessageDialog(this, te.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void mniSortByNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSortByNameActionPerformed
        playersSortType = PlayerComparator.NAME_ORDER;
        pupRegisteredPlayers.setVisible(false);
        try {
            updatePnlRegisteredPlayers(tournament.playersList());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mniSortByNameActionPerformed

    private void mniSortByRankActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSortByRankActionPerformed
        playersSortType = PlayerComparator.RANK_ORDER;
        pupRegisteredPlayers.setVisible(false);
        try {
            updatePnlRegisteredPlayers(tournament.playersList());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mniSortByRankActionPerformed

    private void tblRegisteredPlayersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRegisteredPlayersMouseClicked
        // Double or multiple click
        if (evt.getClickCount() >= 2) {
            modifySelectedPlayer();
        }
        // Right click
        if (evt.getModifiers() == InputEvent.BUTTON3_MASK) {
            int x1 = this.getX();
            int x2 = this.pnlPlayersList.getX();
            int x3 = this.scpRegisteredPlayers.getX();
            int x4 = this.tblRegisteredPlayers.getX();
            int x = x1 + x2 + x3 + x4 + evt.getX();
            int y1 = this.getY();
            int y2 = this.pnlPlayersList.getY();
            int y3 = this.scpRegisteredPlayers.getY();
            int y4 = this.tblRegisteredPlayers.getY();
            int y = y1 + y2 + y3 + y4 + evt.getY();

            pupRegisteredPlayers.setLocation(x, y);
            this.pupRegisteredPlayers.setVisible(true);
        }
    }//GEN-LAST:event_tblRegisteredPlayersMouseClicked

    private void btnQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitActionPerformed
        dispose();
    }//GEN-LAST:event_btnQuitActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        TournamentPrinting tpr = new TournamentPrinting(tournament);
        tpr.setRoundNumber(-1);
        tpr.makePrinting(TournamentPrinting.TYPE_PLAYERSLIST, this.playersSortType);
    }//GEN-LAST:event_btnPrintActionPerformed

    private void txfPlayerNameChoiceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txfPlayerNameChoiceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            lstPlayerNameChoice.requestFocusInWindow();
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            lstPlayerNameChoice.requestFocusInWindow();
        }
    }//GEN-LAST:event_txfPlayerNameChoiceKeyPressed

    private void lstPlayerNameChoiceValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPlayerNameChoiceValueChanged
        String strItem = (String) lstPlayerNameChoice.getSelectedValue();
        if (strItem == null) {
            try {
                this.resetPlayerControls();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            return;
        }
        String strNumber = strItem.substring(3, 8).trim();
        int number = new Integer(strNumber).intValue();
        this.updatePlayerControlsFromRatingList(number);
    }//GEN-LAST:event_lstPlayerNameChoiceValueChanged

    private void txfPlayerNameChoiceTextValueChanged(java.awt.event.TextEvent evt) {//GEN-FIRST:event_txfPlayerNameChoiceTextValueChanged
        String str = txfPlayerNameChoice.getText().toLowerCase();
        if (str.length() == 0) {
            try {
                this.resetPlayerControls();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            return;
        }
        int pos = str.indexOf(" ");
        String str1;
        String str2;
        if (pos < 0) {
            str1 = str;
            str2 = "";
        } else {
            str1 = str.substring(0, pos);
            if (str.length() <= pos + 1) {
                str2 = "";
            } else {
                str2 = str.substring(pos + 1, str.length());
            }
        }

        Vector<String> vS = new Vector<String>();

        for (int iRP = 0; iRP < ratingList.getVPlayers().size(); iRP++) {
            RatedPlayer rP = ratingList.getVPlayers().get(iRP);
            String strName = rP.getName().toLowerCase();
            String strFirstName = rP.getFirstName().toLowerCase();
            int dn1 = RatedPlayer.distance_Levenshtein(str1, strName);
            int df1 = RatedPlayer.distance_Levenshtein(str2, strFirstName);
            int dn2 = RatedPlayer.distance_Levenshtein(str2, strName);
            int df2 = RatedPlayer.distance_Levenshtein(str1, strFirstName);
            int d = Math.min(dn1 + df1, dn2 + df2);
            int threshold = 9;
            if (d <= threshold) {
                String strNumber = "" + iRP;
                while (strNumber.length() < 5) {
                    strNumber = " " + strNumber;
                }
                vS.addElement("(" + d + ")" + strNumber + " " + rP.getName() + " " + rP.getFirstName() + " " +
                        rP.getCountry() + " " + rP.getClub() + " " + rP.getRawRating());
            }
        }
        if (vS.size() == 0) {
            try {
                this.resetPlayerControls();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        } else {
            Collections.sort(vS);
            lstPlayerNameChoice.setListData(vS);
            lstPlayerNameChoice.setVisible(true);
            scpPlayerNameChoice.setVisible(true);
            lstPlayerNameChoice.setSelectedIndex(0);
        }
    }//GEN-LAST:event_txfPlayerNameChoiceTextValueChanged

    private void rdbLevenshteinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbLevenshteinActionPerformed
        this.resetControlsForLevenshteinSearching();

    }//GEN-LAST:event_rdbLevenshteinActionPerformed

    private void rdbFirstCharactersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbFirstCharactersActionPerformed
        this.resetControlsForFirstCharactersSearching();
    }//GEN-LAST:event_rdbFirstCharactersActionPerformed

    private void btnUseNoRatingListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUseNoRatingListActionPerformed
        updateRatingList(RatingList.TYPE_UNDEFINED);
    }//GEN-LAST:event_btnUseNoRatingListActionPerformed

    private void cbxRatingListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxRatingListItemStateChanged
        int index = cbxRatingList.getSelectedIndex();
        if (index <= 0) {
            try {
                this.resetPlayerControls();
            } catch (RemoteException re) {
            }
        } else {
            updatePlayerControlsFromRatingList(index - 1);
        }
    }//GEN-LAST:event_cbxRatingListItemStateChanged

    private void btnUseFFGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUseFFGActionPerformed
        updateRatingList(RatingList.TYPE_FFG);
    }//GEN-LAST:event_btnUseFFGActionPerformed

    private void btnUseEGFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUseEGFActionPerformed
        updateRatingList(RatingList.TYPE_EGF);
    }//GEN-LAST:event_btnUseEGFActionPerformed

    private void updateRatingList(int typeRatingList) {
        switch (typeRatingList) {
            case RatingList.TYPE_EGF:
                lblRatingList.setText("Searching for EGF rating list");
                this.repaint();
                ratingList = new RatingList(RatingList.TYPE_EGF, new File(Gotha.runningDirectory, "ratinglists/egf_db.txt"));
                break;
            case RatingList.TYPE_FFG:
                lblRatingList.setText("Searching for FFG rating list");
                ratingList = new RatingList(RatingList.TYPE_FFG, new File(Gotha.runningDirectory, "ratinglists/ech_ffg.txt"));
                break;
            default:
                ratingList = new RatingList();
        }
        int nbPlayersInRL = ratingList.getVPlayers().size();
        cbxRatingList.removeAllItems();
        cbxRatingList.addItem("");
        for (RatedPlayer rP : ratingList.getVPlayers()) {
            cbxRatingList.addItem(rP.getName() + " " + rP.getFirstName() + " " +
                    rP.getCountry() + " " + rP.getClub() + " " + rP.getRawRating());
        }
        if (nbPlayersInRL == 0) {
            ratingList.setRatingListType(RatingList.TYPE_UNDEFINED);
            lblRatingList.setText("No rating list has been loaded yet");
            this.rdbFirstCharacters.setEnabled(false);
            this.rdbLevenshtein.setEnabled(false);
        } else {
            String strType = "";
            this.rdbFirstCharacters.setEnabled(true);
            this.rdbLevenshtein.setEnabled(true);

            switch (ratingList.getRatingListType()) {
                case RatingList.TYPE_EGF:
                    strType = "EGF rating list";
                    break;
                case RatingList.TYPE_FFG:
                    strType = "FFG rating list";
                    break;
            }
            lblRatingList.setText(strType + " " +
                    ratingList.getStrPublicationDate() +
                    " " + nbPlayersInRL + " players");
        }
        resetRatingListControls();
    }

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        resetRatingListControls();
        try {
            this.resetPlayerControls();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        txfFirstName.setText(normalizeCase(txfFirstName.getText()));
        txfName.setText(normalizeCase(txfName.getText()));

        String strRegistration = "FIN";
        if (grpRegistration.getSelection() == rdbPreliminary.getModel()) {
            strRegistration = "PRE";
        }

        int rawRating = -2950;
        if (txfRatingOrigin.getText().compareTo("FFG") == 0 || txfRatingOrigin.getText().compareTo("EGF") == 0) {
            rawRating = new Integer(txfRawRating.getText()).intValue();
        }
        Player p = null;

        int smmsCorrection = 0;
        try {
            smmsCorrection = new Integer(txfSMMSCorrection.getText());
        } catch (NumberFormatException ex) {
            smmsCorrection = 0;
        }

        try {
            p = new Player(
                    txfName.getText(),
                    txfFirstName.getText(),
//                    txfCountry.getText().trim(),
                    ((String)cbxCountry.getSelectedItem()).trim(),
                    txfClub.getText().trim(),
                    txfEgfPin.getText(),
                    txfFfgLicence.getText(),
                    Player.convertKDToInt(txfRank.getText()),
                    rawRating,
                    txfRatingOrigin.getText(),
                    smmsCorrection,
                    strRegistration);

            boolean[] bPart = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS];
            for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
                bPart[i] = tabCkbParticipation[i].isSelected();
            }
            p.setParticipating(bPart);
        } catch (PlayerException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), "Player Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (this.playerMode == JFrPlayersManager.PLAYER_MODE_NEW) {
            try {
                tournament.addPlayer(p);
                resetRatingListControls();
                resetPlayerControls();
                this.tournamentChanged();
            } catch (TournamentException te) {
                JOptionPane.showMessageDialog(this, te.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
                resetRatingListControls();
                return;
            } catch (RemoteException ex) {
            }
        } else if (this.playerMode == JFrPlayersManager.PLAYER_MODE_MODIF) {
            try {
                tournament.modifyPlayer(playerInModification, p);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (TournamentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.tournamentChanged();
            try {
                resetPlayerControls();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        // Print Welcome sheet
        if (this.ckbWelcomeSheet.isSelected()) {
            instanciateWelcomeSheet(new File("welcomesheet/welcomesheet.html"), new File("welcomesheet/actualwelcomesheet.html"), p);
            try {
                URL url = new File("welcomesheet/actualwelcomesheet.html").toURI().toURL();
                txpWelcomeSheet.setPage(url);
            } catch (IOException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            PageAttributes pa = new PageAttributes();
            pa.setPrinterResolution(100);
            pa.setOrigin(OriginType.PRINTABLE);
            PrintJob pj = getToolkit().getPrintJob(this, "Welcome Sheet", null, pa);
            if (pj != null) {
                Graphics pg = pj.getGraphics();
                txpWelcomeSheet.print(pg);
                pg.dispose();
                pj.end();
            }

        }
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnUpdateFFGRatingListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateFFGRatingListActionPerformed
        try {
            String strDefaultFFGURL = "http://ffg.jeudego.org/echelle/echtxt/ech_ffg.txt";
            File fDefaultFFGFile = new File(Gotha.runningDirectory , "ratinglists/ech_ffg.txt");
            String str = JOptionPane.showInputDialog("Download FFG Rating List from :", strDefaultFFGURL);
            this.lblRatingList.setText("Download in progress");
            lblRatingList.paintImmediately(0, 0, lblRatingList.getWidth(), lblRatingList.getHeight());
            Gotha.download(this.pgbRatingList, str, fDefaultFFGFile);
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, "Malformed URL\nRating list could not be loaded", "Message", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unreachable file\nRating list could not be loaded", "Message", JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateRatingList(RatingList.TYPE_FFG);
}//GEN-LAST:event_btnUpdateFFGRatingListActionPerformed

    private void btnUpdateEGFRatingListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateEGFRatingListActionPerformed
        try {
            String strDefaultEGFURL = "http://www.europeangodatabase.eu/EGD/EGD_2_0/downloads/allworld_lp.html";
            File fDefaultEGFFile = new File(Gotha.runningDirectory, "ratinglists/egf_db.txt");
            String str = JOptionPane.showInputDialog("Download EGF Rating List from :", strDefaultEGFURL);
            this.lblRatingList.setText("Download in progress");
            lblRatingList.paintImmediately(0, 0, lblRatingList.getWidth(), lblRatingList.getHeight());
            Gotha.download(this.pgbRatingList, str, fDefaultEGFFile);
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, "Malformed URL\nRating list could not be loaded", "Message", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unreachable file\nRating list could not be loaded", "Message", JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateRatingList(RatingList.TYPE_EGF);        

}//GEN-LAST:event_btnUpdateEGFRatingListActionPerformed

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHelpActionPerformed
        Gotha.displayGothaHelp("Players Manager frame");
}//GEN-LAST:event_btnHelpActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnQuit;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnUpdateEGFRatingList;
    private javax.swing.JButton btnUpdateFFGRatingList;
    private javax.swing.JButton btnUseEGF;
    private javax.swing.JButton btnUseFFG;
    private javax.swing.JButton btnUseNoRatingList;
    private javax.swing.JComboBox cbxCountry;
    private javax.swing.JComboBox cbxRatingList;
    private javax.swing.JCheckBox ckbWelcomeSheet;
    private javax.swing.ButtonGroup grpAlgo;
    private javax.swing.ButtonGroup grpRegistration;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblFfGLicenceStatus;
    private javax.swing.JLabel lblRatingList;
    private javax.swing.JList lstPlayerNameChoice;
    private javax.swing.JMenuItem mniModifyPlayer;
    private javax.swing.JMenuItem mniRemovePlayer;
    private javax.swing.JMenuItem mniSortByName;
    private javax.swing.JMenuItem mniSortByRank;
    private javax.swing.JProgressBar pgbRatingList;
    private javax.swing.JPanel pnlParticipation;
    private javax.swing.JPanel pnlPlayer;
    private javax.swing.JPanel pnlPlayersList;
    private javax.swing.JPanel pnlRegistration;
    private javax.swing.JPopupMenu pupRegisteredPlayers;
    private javax.swing.JRadioButton rdbFinal;
    private javax.swing.JRadioButton rdbFirstCharacters;
    private javax.swing.JRadioButton rdbLevenshtein;
    private javax.swing.JRadioButton rdbPreliminary;
    private javax.swing.JScrollPane scpPlayerNameChoice;
    private javax.swing.JScrollPane scpRegisteredPlayers;
    private javax.swing.JScrollPane scpWelcomeSheet;
    private javax.swing.JTable tblRegisteredPlayers;
    private javax.swing.JTextField txfClub;
    private javax.swing.JTextField txfEgfPin;
    private javax.swing.JTextField txfFfgLicence;
    private javax.swing.JTextField txfFfgLicenceStatus;
    private javax.swing.JTextField txfFirstName;
    private javax.swing.JTextField txfName;
    private javax.swing.JTextField txfNbPlFin;
    private javax.swing.JTextField txfNbPlPre;
    private java.awt.TextField txfPlayerNameChoice;
    private javax.swing.JTextField txfRank;
    private javax.swing.JTextField txfRatingOrigin;
    private javax.swing.JTextField txfRawRating;
    private javax.swing.JTextField txfSMMSCorrection;
    private javax.swing.JTextPane txpWelcomeSheet;
    // End of variables declaration//GEN-END:variables
    // Custom variable declarations. Editable
    private javax.swing.JCheckBox[] tabCkbParticipation;
    // End of custom variables declaration

    public void resetControlsForFirstCharactersSearching() {
        this.txfPlayerNameChoice.setVisible(false);
        this.scpPlayerNameChoice.setVisible(false);
        this.lstPlayerNameChoice.setVisible(false);
        this.cbxRatingList.setVisible(true);
        this.cbxRatingList.setEnabled(true);
        cbxRatingList.setSelectedIndex(0);
        cbxRatingList.requestFocusInWindow();
    }

    public void resetControlsForLevenshteinSearching() {
        this.cbxRatingList.setVisible(false);
        this.txfPlayerNameChoice.setVisible(true);
        this.txfPlayerNameChoice.setEnabled(true);
        this.lstPlayerNameChoice.setVisible(true);
        this.lstPlayerNameChoice.setEnabled(true);
        String strInvite = "Enter approximate name and first name";
        this.txfPlayerNameChoice.setText(strInvite);
        txfPlayerNameChoice.selectAll();
        txfPlayerNameChoice.requestFocusInWindow();
    }

    public void updatePlayerControlsFromRatingList(int index) {
        RatedPlayer rP = ratingList.getVPlayers().elementAt(index);
        txfName.setText(rP.getName());
        txfFirstName.setText(rP.getFirstName());
        txfRawRating.setText("" + rP.getRawRating());
        txfRatingOrigin.setText(rP.getRatingOrigin());
        this.txfSMMSCorrection.setText("" + 0);
        int rank = ((rP.getStdRating() + 3000) / 100) - 30;    // for proper rounding
        txfRank.setText(Player.convertIntToKD(rank));
//        txfCountry.setText(rP.getCountry());
          cbxCountry.setSelectedItem(rP.getCountry());
        txfClub.setText(rP.getClub());
        txfFfgLicence.setText(rP.getFfgLicence());
        txfFfgLicenceStatus.setText(rP.getFfgLicenceStatus());
        if (rP.getFfgLicenceStatus().compareTo("-") == 0) {
            lblFfGLicenceStatus.setText("Non licencié");
        } else {
            lblFfGLicenceStatus.setText("");
        }
        txfEgfPin.setText(rP.getEgfPin());
    }

    /**
     * Fills player controls with playerInModification fields
     */
    public void updatePlayerControlsFromPlayerInModification() {
        txfName.setText(playerInModification.getName());
        txfFirstName.setText(playerInModification.getFirstName());
        int rawRating = playerInModification.getRating();
        if (playerInModification.getRatingOrigin().compareTo("EGF") == 0) {
            rawRating += 2050;
        }
        txfRawRating.setText("" + rawRating);
        txfRatingOrigin.setText(playerInModification.getRatingOrigin());
        this.txfSMMSCorrection.setText("" + playerInModification.getSmmsCorrection());
        int rank = (playerInModification.getRank());
        txfRank.setText(Player.convertIntToKD(rank));
//        txfCountry.setText(playerInModification.getCountry());
        cbxCountry.setSelectedItem(playerInModification.getCountry());
        txfClub.setText(playerInModification.getClub());
        txfFfgLicence.setText(playerInModification.getFfgLicence());
        txfFfgLicenceStatus.setText("");
        lblFfGLicenceStatus.setText("");
        txfEgfPin.setText(playerInModification.getEgfPin());

        boolean[] bPart = playerInModification.getParticipating();
        for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            tabCkbParticipation[r].setSelected(bPart[r]);
        }
        if (playerInModification.getRegisteringStatus().compareTo("FIN") == 0) {
            this.rdbFinal.setSelected(true);
        } else {
            this.rdbPreliminary.setSelected(true);
        }
        boolean bImplied = false;
        try {
            bImplied = tournament.isPlayerImplied(playerInModification);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        this.rdbPreliminary.setEnabled(!bImplied);
        this.rdbFinal.setEnabled(!bImplied);

        for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            try {
                tabCkbParticipation[r].setEnabled(!tournament.isPlayerImpliedInRound(playerInModification, r));
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void instanciateWelcomeSheet(File templateFile, File actualFile, Player p) {
        Vector<String> vLines = new Vector<String>();
        try {
            FileInputStream fis = new FileInputStream(templateFile);
            BufferedReader d = new BufferedReader(new InputStreamReader(fis, java.nio.charset.Charset.forName("ISO8859-15")));

            String s;
            do {
                s = d.readLine();
                if (s != null) {
                    vLines.add(s);
                }
            } while (s != null);
            d.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Replace player tags
        Vector<String> vActualLines = new Vector<String>();
        for (String strLine : vLines) {
            if (strLine.length() == 0) {
                continue;
            }
            strLine = strLine.replaceAll("<name>", p.getName());
            strLine = strLine.replaceAll("<firstname>", p.getFirstName());
            strLine = strLine.replaceAll("<country>", p.getCountry());
            strLine = strLine.replaceAll("<club>", p.getClub());
            strLine = strLine.replaceAll("<rank>", Player.convertIntToKD(p.getRank()));
            int rawRating = p.getRating();
            String ratingOrigin = p.getRatingOrigin();
            if (ratingOrigin.compareTo("EGF") == 0) {
                rawRating += 2050;
            }
            strLine = strLine.replaceAll("<rating>", Integer.valueOf(rawRating).toString());
            strLine = strLine.replaceAll("<ratingorigin>", ratingOrigin);
            boolean[] bPart = p.getParticipating();
            String strPart = "";
            int nbRounds = 0;
            try {
                nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int r = 0; r < nbRounds; r++) {
                if (bPart[r]) {
                    strPart += " " + (r + 1);
                } else {
                    strPart += " -";
                }
            }
            strLine = strLine.replaceAll("<participation>", strPart);
            vActualLines.add(strLine);
        }

        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(actualFile));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        for (String strLine : vActualLines) {
            try {
                output.write(strLine + "\n");
            } catch (IOException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void tournamentChanged() {
        try {
            if (!tournament.isOpen()) {
                dispose();
                return;
            }
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        updateAllViews();
    }

    private void updateAllViews() {
        try {
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle("Players Manager. " + tournament.getTournamentParameterSet().getGeneralParameterSet().getShortName());
            updatePnlRegisteredPlayers(tournament.playersList());
            setPnlParticipationVisibility();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}




