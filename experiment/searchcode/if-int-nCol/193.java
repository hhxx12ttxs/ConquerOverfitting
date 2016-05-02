/*
 * JFrGamesResults.java
 */

package gotha;

import java.rmi.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author  Luc Vannier
 */
public class JFrGamesResults extends javax.swing.JFrame{
    public static final int TABLE_NUMBER_COL = 0;
    public static final int WHITE_PLAYER_COL = 1;
    public static final int BLACK_PLAYER_COL = 2;
    public static final int HANDICAP_COL = 3;    
    public static final int RESULT_COL = 4;

    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;

    public int gamesSortType = GameComparator.TABLE_NUMBER_ORDER;
    
    /**  current Tournament */
    private TournamentInterface tournament;
    
    /** current Round */
    private int processedRoundNumber = 0;
    
    /**
     * Creates new form JFrGamesResults
     */
    public JFrGamesResults() {
        initComponents();
    }

    /** Creates new form JFrPlayerManager */
    public JFrGamesResults(TournamentInterface tournament) throws RemoteException{
        this.tournament = tournament;
        
        processedRoundNumber = tournament.presumablyCurrentRoundNumber();
        initComponents();
        customInitComponents();
        setupRefreshTimer();
    }

    private void setupRefreshTimer(){
        ActionListener taskPerformer = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
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
    private void customInitComponents()throws RemoteException{
        int w = JFrGotha.MEDIUM_FRAME_WIDTH;
        int h = JFrGotha.MEDIUM_FRAME_HEIGHT;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dim.width - w)/2, (dim.height -h)/2, w, h);
        setIconImage(Gotha.getIconImage());

        initGamesComponents();
        this.updateAllViews();
    }
    
      /**
     */
    private void initGamesComponents(){
        final int TABLE_NUMBER_WIDTH = 40;
        final int PLAYER_WIDTH = 150;
        final int HANDICAP_WIDTH = 20;
        final int RESULT_WIDTH = 40;
        tblGames.getColumnModel().getColumn(TABLE_NUMBER_COL).setPreferredWidth(TABLE_NUMBER_WIDTH);
        tblGames.getColumnModel().getColumn(WHITE_PLAYER_COL).setPreferredWidth(PLAYER_WIDTH);
        tblGames.getColumnModel().getColumn(BLACK_PLAYER_COL).setPreferredWidth(PLAYER_WIDTH);
        tblGames.getColumnModel().getColumn(HANDICAP_COL).setPreferredWidth(HANDICAP_WIDTH);
        tblGames.getColumnModel().getColumn(RESULT_COL).setPreferredWidth(RESULT_WIDTH);
        
        // Column names in 
        TableColumnModel tcm = this.tblGames.getColumnModel();
        tcm.getColumn(0).setHeaderValue("Table");
        tcm.getColumn(1).setHeaderValue("White");
        tcm.getColumn(2).setHeaderValue("Black");
        tcm.getColumn(3).setHeaderValue("Hd");
        tcm.getColumn(4).setHeaderValue("Result");
    }
    
    private void updateComponents(){
        this.spnRoundNumber.setValue(this.processedRoundNumber + 1);

        ArrayList<Game> alCurrentActualGames = null;
        try {
            alCurrentActualGames = tournament.gamesList(processedRoundNumber);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return;
        }

        for (int nCol = 0; nCol < tblGames.getColumnCount(); nCol++){
            TableColumn col = tblGames.getColumnModel().getColumn(nCol);
            col.setCellRenderer(new ResultsTableCellRenderer());
        }
                  
        fillGamesTable(alCurrentActualGames, tblGames);
    }
        
        private void fillGamesTable(ArrayList<Game> alG, JTable tblG){
        // sort
        ArrayList<Game> alDisplayedGames = new ArrayList<Game>(alG);
        
        GameComparator gameComparator = new GameComparator(gamesSortType);
        Collections.sort(alDisplayedGames, gameComparator);

        DefaultTableModel model = (DefaultTableModel)tblGames.getModel();
        model.setRowCount(alDisplayedGames.size());
        
        for(int iG = 0; iG < alDisplayedGames.size(); iG++){
            Game g = alDisplayedGames.get(iG);
            String strResult = g.resultAsString();
            model.setValueAt(strResult, iG, RESULT_COL);
            int col = 0;
            model.setValueAt("" + (g.getTableNumber() + 1), iG, col++);      
            Player wP = g.getWhitePlayer();
            model.setValueAt(wP.fullName(), iG, col++);
            Player bP = g.getBlackPlayer();
            model.setValueAt(bP.fullName(), iG, col++);
            model.setValueAt("" + g.getHandicap(), iG, col++);      
        }        
    }
  
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlInternal = new javax.swing.JPanel();
        pnlGames = new javax.swing.JPanel();
        scpGames = new javax.swing.JScrollPane();
        tblGames = new javax.swing.JTable();
        btnPrint = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnQuit = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        txfSearchPlayer = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        spnRoundNumber = new javax.swing.JSpinner();
        btnHelp = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Games .. Results");
        setResizable(false);
        getContentPane().setLayout(null);

        pnlInternal.setLayout(null);

        pnlGames.setBorder(javax.swing.BorderFactory.createTitledBorder("Games"));
        pnlGames.setLayout(null);

        tblGames.setFont(new java.awt.Font("Arial", 0, 11));
        tblGames.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Table", "White", "Black", "Hd", "Result"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblGames.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblGamesMousePressed(evt);
            }
        });
        scpGames.setViewportView(tblGames);

        pnlGames.add(scpGames);
        scpGames.setBounds(10, 20, 400, 320);

        btnPrint.setText("Print...");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        pnlGames.add(btnPrint);
        btnPrint.setBounds(10, 430, 400, 30);

        jLabel1.setText("Click on winner's name");
        pnlGames.add(jLabel1);
        jLabel1.setBounds(10, 350, 260, 14);

        jLabel2.setText("To cancel a result, click on table number ");
        pnlGames.add(jLabel2);
        jLabel2.setBounds(10, 370, 390, 14);

        jLabel4.setText("For special results, click on result");
        pnlGames.add(jLabel4);
        jLabel4.setBounds(10, 390, 390, 14);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel5.setText("Right click on result to toggle Normal/By default");
        pnlGames.add(jLabel5);
        jLabel5.setBounds(40, 405, 360, 14);

        pnlInternal.add(pnlGames);
        pnlGames.setBounds(150, 10, 420, 470);

        btnQuit.setText("Quit");
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitActionPerformed(evt);
            }
        });
        pnlInternal.add(btnQuit);
        btnQuit.setBounds(150, 490, 420, 30);

        jLabel9.setText("Round");
        pnlInternal.add(jLabel9);
        jLabel9.setBounds(10, 40, 50, 14);
        pnlInternal.add(txfSearchPlayer);
        txfSearchPlayer.setBounds(10, 220, 130, 20);

        jLabel3.setText("Search for a player");
        pnlInternal.add(jLabel3);
        jLabel3.setBounds(10, 200, 130, 14);

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        pnlInternal.add(btnSearch);
        btnSearch.setBounds(10, 250, 130, 23);

        spnRoundNumber.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRoundNumberStateChanged(evt);
            }
        });
        pnlInternal.add(spnRoundNumber);
        spnRoundNumber.setBounds(60, 30, 40, 30);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gotha/gothalogo16.jpg"))); // NOI18N
        btnHelp.setText("help");
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });
        pnlInternal.add(btnHelp);
        btnHelp.setBounds(10, 490, 110, 30);

        getContentPane().add(pnlInternal);
        pnlInternal.setBounds(0, 0, 730, 540);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblGamesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblGamesMousePressed
        int r = tblGames.rowAtPoint(evt.getPoint());
        int c = tblGames.columnAtPoint(evt.getPoint());
        Game g = null;
        try {
            String strTableNumber = "" + tblGames.getModel().getValueAt(r, TABLE_NUMBER_COL);      
            int tableNumber = Integer.parseInt(strTableNumber) - 1;
            g = tournament.getGame(processedRoundNumber, tableNumber);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        int oldResult = g.getResult();
        int newResult = oldResult;
        if (c == WHITE_PLAYER_COL) newResult = Game.RESULT_WHITEWINS;
        else if (c == BLACK_PLAYER_COL) newResult = Game.RESULT_BLACKWINS;
        else if (c == TABLE_NUMBER_COL) newResult = Game.RESULT_UNKNOWN;
        else if (c == RESULT_COL){
            if (evt.getModifiers() == InputEvent.BUTTON3_MASK){
                if (oldResult ==  Game.RESULT_WHITEWINS) newResult = Game.RESULT_WHITEWINS_BYDEF;
                if (oldResult ==  Game.RESULT_BLACKWINS) newResult = Game.RESULT_BLACKWINS_BYDEF;
                if (oldResult ==  Game.RESULT_EQUAL) newResult = Game.RESULT_EQUAL_BYDEF;
                if (oldResult ==  Game.RESULT_BOTHWIN) newResult = Game.RESULT_BOTHWIN_BYDEF;
                if (oldResult ==  Game.RESULT_BOTHLOSE) newResult = Game.RESULT_BOTHLOSE_BYDEF;
                if (oldResult ==  Game.RESULT_WHITEWINS_BYDEF) newResult = Game.RESULT_WHITEWINS;
                if (oldResult ==  Game.RESULT_BLACKWINS_BYDEF) newResult = Game.RESULT_BLACKWINS;
                if (oldResult ==  Game.RESULT_EQUAL_BYDEF) newResult = Game.RESULT_EQUAL;
                if (oldResult ==  Game.RESULT_BOTHWIN_BYDEF) newResult = Game.RESULT_BOTHWIN;
                if (oldResult ==  Game.RESULT_BOTHLOSE_BYDEF) newResult = Game.RESULT_BOTHLOSE;
            }
            else{
                if (oldResult == Game.RESULT_UNKNOWN)               newResult = Game.RESULT_WHITEWINS;
                else if (oldResult == Game.RESULT_WHITEWINS)        newResult = Game.RESULT_BLACKWINS;
                else if (oldResult == Game.RESULT_BLACKWINS)        newResult = Game.RESULT_EQUAL;
                else if (oldResult == Game.RESULT_EQUAL)            newResult = Game.RESULT_BOTHWIN;
                else if (oldResult == Game.RESULT_BOTHWIN)          newResult = Game.RESULT_BOTHLOSE;
                else if (oldResult == Game.RESULT_BOTHLOSE)         newResult = Game.RESULT_WHITEWINS_BYDEF;
                else if (oldResult == Game.RESULT_WHITEWINS_BYDEF)  newResult = Game.RESULT_BLACKWINS_BYDEF;
                else if (oldResult == Game.RESULT_BLACKWINS_BYDEF)  newResult = Game.RESULT_EQUAL_BYDEF;
                else if (oldResult == Game.RESULT_EQUAL_BYDEF)      newResult = Game.RESULT_BOTHWIN_BYDEF;
                else if (oldResult == Game.RESULT_BOTHWIN_BYDEF)    newResult = Game.RESULT_BOTHLOSE_BYDEF;
                else if (oldResult == Game.RESULT_BOTHLOSE_BYDEF)   newResult = Game.RESULT_UNKNOWN;
            }
        }
        
        if (newResult == oldResult) return;
        try {
            tournament.setResult(g, newResult);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_tblGamesMousePressed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        TournamentPrinting tpr = new TournamentPrinting(tournament);
        tpr.setRoundNumber(processedRoundNumber);
        tpr.makePrinting(TournamentPrinting.TYPE_GAMESLIST, 0);
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitActionPerformed
        dispose();
    }//GEN-LAST:event_btnQuitActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String strSearchPlayer = this.txfSearchPlayer.getText().toLowerCase();
        if (strSearchPlayer.length() == 0) {
            tblGames.clearSelection();
            return;
        }
        TableModel model = tblGames.getModel();
        int rowNumber = -1;
        int startRow = tblGames.getSelectedRow() + 1;
        int nbRows = model.getRowCount();
        for (int iR = 0; iR < nbRows; iR++) {
            int row = (startRow + iR) % nbRows;
            String str = (String) model.getValueAt(row, WHITE_PLAYER_COL);
            str = str.toLowerCase();
            if (str.indexOf(strSearchPlayer) >= 0) {
                rowNumber = row;
                break;
            }
            str = (String) model.getValueAt(row, BLACK_PLAYER_COL);
            str = str.toLowerCase();
            if (str.indexOf(strSearchPlayer) >= 0) {
                rowNumber = row;
                break;
            }
        }

        tblGames.clearSelection();
        if (rowNumber == -1) {
            JOptionPane.showMessageDialog(this,
                "No player with the specified name is paired in round " + (this.processedRoundNumber + 1),
                "Message", JOptionPane.ERROR_MESSAGE);
        } else {
            tblGames.setRowSelectionAllowed(true);
            tblGames.clearSelection();
            tblGames.addRowSelectionInterval(rowNumber, rowNumber);

            Rectangle rect = tblGames.getCellRect(rowNumber, 0, true);
            tblGames.scrollRectToVisible(rect);
        }

        tblGames.repaint();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void spnRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRoundNumberStateChanged
        int demandedRN = (Integer)(spnRoundNumber.getValue()) - 1;
        this.demandedDisplayedRoundNumberHasChanged(demandedRN);
}//GEN-LAST:event_spnRoundNumberStateChanged

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHelpActionPerformed
        Gotha.displayGothaHelp("Games Results frame");
}//GEN-LAST:event_btnHelpActionPerformed
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnQuit;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel pnlGames;
    private javax.swing.JPanel pnlInternal;
    private javax.swing.JScrollPane scpGames;
    private javax.swing.JSpinner spnRoundNumber;
    private javax.swing.JTable tblGames;
    private javax.swing.JTextField txfSearchPlayer;
    // End of variables declaration//GEN-END:variables

    public void tournamentChanged(){
        try {
            if (!tournament.isOpen()){
                dispose();
                return;
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateAllViews();
    }

    private void updateAllViews(){
        try {
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle("Games .. Results. " + tournament.getTournamentParameterSet().getGeneralParameterSet().getShortName());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateComponents();
    }

    private void demandedDisplayedRoundNumberHasChanged(int demandedRN){
        int numberOfRounds = 0;
        try {
            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(demandedRN < 0 || demandedRN >= numberOfRounds){
            spnRoundNumber.setValue(processedRoundNumber + 1);
            return;
        }
        if(demandedRN == processedRoundNumber) return;

        processedRoundNumber = demandedRN;
        updateAllViews();
    }


}


class ResultsTableCellRenderer extends JLabel implements TableCellRenderer {
    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        TableModel model = table.getModel();
        setText("" + model.getValueAt(rowIndex, colIndex));       
        String strResult = "" + model.getValueAt(rowIndex, JFrGamesResults.RESULT_COL);      

        String strWR = strResult.substring(0, 1);
        String strBR = strResult.substring(2, 3);
        if (colIndex == JFrGamesResults.WHITE_PLAYER_COL){
            setFont(new Font("Arial", Font.BOLD, 12));
            if (strWR.compareTo("1") == 0) setForeground(Color.RED);      
            else if (strWR.compareTo("0") == 0) setForeground(Color.BLUE);      
            else if (strWR.compareTo("?") == 0) setForeground(Color.MAGENTA);      
            else {
                setForeground(Color.BLACK);
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        }
        else if (colIndex == JFrGamesResults.BLACK_PLAYER_COL){
            setFont(new Font("Arial", Font.BOLD, 12));
            if (strBR.compareTo("1") == 0) setForeground(Color.RED);      
            else if (strBR.compareTo("0") == 0) setForeground(Color.BLUE);      
            else if (strBR.compareTo("?") == 0) setForeground(Color.MAGENTA);      
            else {
                setForeground(Color.BLACK);
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        }
        else{
           setFont(new Font("Arial", Font.PLAIN, 12)); 
        }

        if (isSelected) setFont(new Font("Arial", Font.BOLD, 12));

        return this;
    }
}

